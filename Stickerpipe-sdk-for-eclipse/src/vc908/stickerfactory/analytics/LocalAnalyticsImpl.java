package vc908.stickerfactory.analytics;

import android.content.Context;

import vc908.stickerfactory.StorageManager;
import vc908.stickerfactory.utils.Logger;
import vc908.stickerfactory.utils.NamesHelper;

/**
 * @author Dmitry Nezhydenko
 */
public class LocalAnalyticsImpl implements IAnalytics {
    private static final String TAG = LocalAnalyticsImpl.class.getSimpleName();


    int checkedMessages = 0;
    int checkedStickersCount = 0;
    private boolean isDryRun;


    @Override
    public void init(Context context, boolean isDryRun) {
        this.isDryRun = isDryRun;
    }

    @Override
    public void onStickerSelected(String packName, String stickerName) {
        Logger.i(TAG, "Sticker use Event: " + packName + " " + stickerName);
        if (!isDryRun) {
            StorageManager.getInstance().addAnalyticsItem(Category.STICKER, Action.USE, NamesHelper.getStickerCode(packName, stickerName));
        }
    }

    @Override
    public void onEmojiSelected(String code) {
        Logger.i(TAG, "Emoji use Event: " + code);
        // TODO uncomment later, when serverside can store emoji
//        StorageManager.getInstance().addAnalyticsItem(CATEGORY_EMOJI, ACTION_USE, code);
    }

    @Override
    public void onPackStored(String packName) {
        Logger.i(TAG, "Pack stored event: " + packName);
        if (!isDryRun) {
            StorageManager.getInstance().addAnalyticsItem(Category.PACK, Action.INSTALL, packName);
        }
    }

    @Override
    public void onPackDeleted(String packName) {
        Logger.i(TAG, "Pack deleted event: " + packName);
        if (!isDryRun) {
            StorageManager.getInstance().addAnalyticsItem(Category.PACK, Action.REMOVE, packName);
        }
    }

    @Override
    public void onMessageCheck(boolean isSticker) {
        Logger.i(TAG, "Message check event: is sticker - " + isSticker);
        if (!isDryRun) {
            checkedMessages += 1;
            if (isSticker) {
                checkedStickersCount += 1;
            }
            if (checkedMessages >= ITEMS_CHECK_SEND_LIMIT) {
                StorageManager.getInstance().addAnalyticsItem(Category.MESSAGE, Action.CHECK, "Events count", checkedMessages);
                StorageManager.getInstance().addAnalyticsItem(Category.MESSAGE, Action.CHECK, "Stickers count", checkedStickersCount);
                checkedMessages = 0;
                checkedStickersCount = 0;
            }
        }
    }

    @Override
    public void onError(String message) {
        // Maybe it will be send later
    }

    @Override
    public void onWarning(String message) {
        // Maybe it will be send later
    }

}
