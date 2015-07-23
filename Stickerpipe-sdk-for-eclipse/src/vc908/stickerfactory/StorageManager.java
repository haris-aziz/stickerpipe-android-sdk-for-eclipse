package vc908.stickerfactory;

import android.content.AsyncQueryHandler;
import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import vc908.stickerfactory.analytics.AnalyticsManager;
import vc908.stickerfactory.analytics.IAnalytics;
import vc908.stickerfactory.model.Sticker;
import vc908.stickerfactory.model.StickersPack;
import vc908.stickerfactory.provider.StickersProvider;
import vc908.stickerfactory.provider.analytics.AnalyticsColumns;
import vc908.stickerfactory.provider.analytics.AnalyticsContentValues;
import vc908.stickerfactory.provider.analytics.AnalyticsCursor;
import vc908.stickerfactory.provider.analytics.AnalyticsSelection;
import vc908.stickerfactory.provider.packs.PacksColumns;
import vc908.stickerfactory.provider.packs.PacksContentValues;
import vc908.stickerfactory.provider.packs.PacksCursor;
import vc908.stickerfactory.provider.packs.PacksSelection;
import vc908.stickerfactory.provider.recentlystickers.RecentlyStickersColumns;
import vc908.stickerfactory.provider.recentlystickers.RecentlyStickersContentValues;
import vc908.stickerfactory.provider.stickers.StickersColumns;
import vc908.stickerfactory.provider.stickers.StickersContentValues;
import vc908.stickerfactory.provider.stickers.StickersCursor;
import vc908.stickerfactory.provider.stickers.StickersSelection;
import vc908.stickerfactory.ui.activity.MoreActivity;
import vc908.stickerfactory.utils.Logger;
import vc908.stickerfactory.utils.NamesHelper;
import vc908.stickerfactory.utils.Utils;

/**
 * Storage manager is used for all interaction with internal storage, database and preferences
 *
 * @author Dmitry Nezhydenko
 */
public class StorageManager extends PreferenceHelper {

    private static final String TAG = StorageManager.class.getSimpleName();
    private static final String KEYBOARD_HEIGHT = "keyboard_height";
    private static final String DEVICE_ID = "device_id";
    private static final String LAST_MODIFY_DATE = "last_modify_date";
    private static final String LAST_USING_ITEM_ID = "last_using_item_id";
    private final AsyncQueryHandler asyncQueryHandler;

    private Context mContext;
    private static StorageManager instance;

    /**
     * Private constructor to prevent new objects creating
     *
     * @param context Storage manager context
     */
    private StorageManager(Context context) {
        super(context);
        mContext = context;
        asyncQueryHandler = new AsyncQueryHandler(mContext.getContentResolver()) {
        };
    }

    /**
     * Singleton getter implementation for StorageManager.
     * Manager must be initialized by {@link #init} before using
     *
     * @return Singleton instance
     * @throws StorageManagerNotInitializedException
     */
    public static StorageManager getInstance() throws StorageManagerNotInitializedException {
        if (instance == null) {
            Logger.e(TAG, "Trying get instance before initialize");
            throw new StorageManagerNotInitializedException();
        } else {
            return instance;
        }
    }

    /**
     * Manager initialization. Must be call before using.
     * Throws exception if called twice
     *
     * @param context Context for files manipulations
     * @throws StorageManagerAlreadyInitializedException
     */
    static void init(@NonNull Context context) throws StorageManagerAlreadyInitializedException {
        if (instance == null) {
            instance = new StorageManager(context);
        } else {
            Logger.e(TAG, "Trying to initialize when instance already initialized");
            throw new StorageManagerAlreadyInitializedException();
        }
    }

    /**
     * Store stickers and packs to database
     *
     * @param packsList Packs list
     */
    public void storeStickers(@NonNull List<StickersPack> packsList) {
        if (packsList.size() == 0) {
            Logger.w(TAG, "Trying to store empty sticker map");
        } else {
            List<ContentValues> packsBulk = new ArrayList<>();
            List<ContentValues> stickersBulk = new ArrayList<>();
            for (StickersPack pack : packsList) {
                PacksContentValues packsContentValues = new PacksContentValues();
                packsContentValues.putName(pack.getName().toLowerCase());
                packsContentValues.putTitle(pack.getTitle());
                packsContentValues.putArtist(pack.getArtist());
                packsContentValues.putPrice(pack.getPrice());
                packsBulk.add(packsContentValues.values());
                // stickers
                for (Sticker sticker : pack.getStickers()) {
                    StickersContentValues stickerCv = new StickersContentValues();
                    stickerCv.putPack(pack.getName().toLowerCase());
                    stickerCv.putName(sticker.getName().toLowerCase());
                    stickersBulk.add(stickerCv.values());
                }
                AnalyticsManager.getInstance().onPackStored(pack.getName());
            }
            mContext.getContentResolver().bulkInsert(PacksColumns.CONTENT_URI, packsBulk.toArray(new ContentValues[packsBulk.size()]));
            mContext.getContentResolver().bulkInsert(StickersColumns.CONTENT_URI, stickersBulk.toArray(new ContentValues[stickersBulk.size()]));
        }
    }

    /**
     * Update last using time for given sticker to current time.
     * NOTE: UNIQUE (pack, name) ON CONFLICT REPLACE
     * Create new record or replace existing with new time
     *
     * @param code Sticker code
     */
    public void updateStickerUsingTime(String code) {
        if (!TextUtils.isEmpty(code)) {
            RecentlyStickersContentValues rscv = new RecentlyStickersContentValues()
                    .putPack(NamesHelper.getPackName(code))
                    .putName(NamesHelper.getStickerName(code))
                    .putLastUsingTime(System.currentTimeMillis());
            asyncQueryHandler.startInsert(-1, null, RecentlyStickersColumns.CONTENT_URI, rscv.values());
        }
    }

    /**
     * Check is stickers list exists at database
     *
     * @return Result of inspection
     */
    public boolean isStickersExists() {
        StickersCursor cursor = new StickersSelection().query(mContext.getContentResolver());
        boolean result = cursor.getCount() > 0;
        cursor.close();
        return result;
    }

    /**
     * Add new record to analytics table
     *
     * @param category Item Category
     * @param action   Item action
     * @param label    Item label
     */
    public void addAnalyticsItem(IAnalytics.Category category, IAnalytics.Action action, String label) {
        addAnalyticsItem(category, action, label, 0);
    }

    /**
     * Add new record to analytics table
     *
     * @param category Item Category
     * @param action   Item action
     * @param label    Item label
     * @param count    Item count
     */
    public void addAnalyticsItem(IAnalytics.Category category, IAnalytics.Action action, String label, int count) {
        AnalyticsContentValues cv = new AnalyticsContentValues()
                .putCategory(category.getValue())
                .putAction(action.getValue())
                .putLabel(label)
                .putEventCount(count)
                .putEventtime(System.currentTimeMillis() / 1000L);
        asyncQueryHandler.startInsert(-1, null, AnalyticsColumns.CONTENT_URI, cv.values());
    }

    /**
     * Send pending analytics events
     */
    public void sendAnalyticsEvents() {
        AnalyticsCursor cursor = new AnalyticsSelection().query(mContext.getContentResolver());
        if (cursor.getCount() > 0) {
            JSONArray data = new JSONArray();
            while (cursor.moveToNext()) {
                JSONObject obj = new JSONObject();
                try {
                    obj.put("category", cursor.getCategory());
                    obj.put("action", cursor.getAction());
                    obj.put("label", cursor.getLabel());
                    obj.put("time", cursor.getEventtime());
                    if (cursor.getEventCount() != null && cursor.getEventCount() > 0) {
                        obj.put("value", cursor.getEventCount());
                    }
                    data.put(obj);
                } catch (JSONException e) {
                    Logger.e(TAG, "Can't create analytics request", e);
                }
            }
            Logger.d(TAG, "Send analytics data: " + data);
            NetworkManager.getInstance().sendAnalyticsData(data);
        }
        cursor.close();
    }

    /**
     * Get stored keyboard height according to screen orientation.
     *
     * @param orientation Orientation, like {@link android.content.res.Configuration#ORIENTATION_LANDSCAPE}
     *                    or {@link android.content.res.Configuration#ORIENTATION_PORTRAIT}
     * @return Stored or default value
     */
    public int getKeyboardHeight(int orientation) {
        int height = getIntValue(KEYBOARD_HEIGHT + orientation);
        if (height == 0) {
            height = Utils.dp(248);
        }
        return height;
    }

    /**
     * Store keyboard height according to screen orientation.
     *
     * @param orientation Orientation, like {@link android.content.res.Configuration#ORIENTATION_LANDSCAPE}
     *                    or {@link android.content.res.Configuration#ORIENTATION_PORTRAIT}
     * @param value       Height value
     */
    public void storeKeyboardHeight(int orientation, int value) {
        storeValue(KEYBOARD_HEIGHT + orientation, value);
    }

    /**
     * Clear analytics data after successful request
     */
    public void clearAnalytics() {
        mContext.getContentResolver().delete(AnalyticsColumns.CONTENT_URI, null, null);
    }

    /**
     * Update packs order according to its positions at list
     *
     * @param data Ordered packs list
     */
    public void updatePacksOrder(List<MoreActivity.PackInfoHolder> data) {
        ArrayList<ContentProviderOperation> operations = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            operations.add(ContentProviderOperation.newUpdate(
                    PacksColumns.CONTENT_URI)
                    .withSelection(PacksColumns.NAME + "=?", new String[]{data.get(i).name})
                    .withValue(PacksColumns.PACK_ORDER, String.valueOf(i))
                    .build());
        }
        try {
            mContext.getContentResolver().applyBatch(StickersProvider.AUTHORITY, operations);
        } catch (RemoteException | OperationApplicationException e) {
            Logger.e(TAG, "Can't update packs order", e);
        }
    }

    /**
     * Remove pack and related info
     *
     * @param packName Name of pack
     */
    public void removePack(String packName) {
        // Remove record from packs table
        mContext.getContentResolver().delete(
                PacksColumns.CONTENT_URI,
                PacksColumns.NAME + "=?",
                new String[]{packName});
        // remove all related stickers records
        mContext.getContentResolver().delete(
                StickersColumns.CONTENT_URI,
                StickersColumns.PACK + "=?",
                new String[]{packName});
        // remove stickers from recent table
        mContext.getContentResolver().delete(
                RecentlyStickersColumns.CONTENT_URI,
                RecentlyStickersColumns.PACK + "=?",
                new String[]{packName});
    }

    /**
     * Get stored device id value
     */
    public String getDeviceId() {
        return getStringValue(DEVICE_ID);
    }

    /**
     * Store device id value to shared preferences
     *
     * @param deviceId Device id
     */
    public void storeDeviceId(String deviceId) {
        storeValue(DEVICE_ID, deviceId);
    }

    /**
     * Get last modify date for packs from preferences
     *
     * @return Last modify date
     */
    public long getPacksLastModifyDate() {
        return getLongValue(LAST_MODIFY_DATE);
    }

    /**
     * Store last modify date for packs to preferences
     *
     * @param value Last modify date
     */
    public void storeLastModifyDate(long value) {
        storeValue(LAST_MODIFY_DATE, value);
    }

    /**
     * Get list of pack names from database
     *
     * @return List of pack names
     */
    public List<String> getPacksName() {
        List<String> packs = new ArrayList<>();
        PacksCursor cursor = new PacksSelection().query(mContext.getContentResolver());
        while (cursor.moveToNext()) {
            packs.add(cursor.getName());
        }
        cursor.close();
        return packs;
    }

    /**
     * Store last using item (emoji or sticker)
     *
     * @param itemId Item id
     */
    public void storeLastUsingItem(int itemId) {
        storeValue(LAST_USING_ITEM_ID, itemId);
    }

    /**
     * Return last using item(emoji or sticker) id
     * Return default value, if not set yet
     *
     * @param defaultValue Default value
     * @return Item ID
     */
    public int getLastUsingItem(int defaultValue) {
        return getIntValue(LAST_USING_ITEM_ID, defaultValue);
    }

    /**
     * Class represents initialize missing exception.
     *
     * @author Dmitry Nezhydenko
     * @see StorageManager#init
     */
    public static class StorageManagerNotInitializedException extends RuntimeException {
        @Override
        public String getMessage() {
            return "Storage manager not initialized. Use init(...) method before.";
        }
    }

    /**
     * Class represents double initialize exception.
     *
     * @author Dmitry Nezhydenko
     * @see StorageManager#init
     */
    public static class StorageManagerAlreadyInitializedException extends RuntimeException {
        @Override
        public String getMessage() {
            return "Storage manager already initialized.";
        }
    }

}
