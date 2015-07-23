package vc908.stickerfactory.analytics;

import android.content.Context;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import vc908.stickerfactory.StickersManager;
import vc908.stickerfactory.utils.Logger;
import vc908.stickerfactory.utils.Utils;

/**
 * @author Dmitry Nezhydenko (dehimb@gmail.com)
 */
public class GoogleAnalyticsImpl implements IAnalytics {
    private static final String TAG = GoogleAnalyticsImpl.class.getSimpleName();
    private Tracker tracker;
    private String deviceId;

    int checkedMessages = 0;
    int checkedStickersCount = 0;
    private String packageName;

    @Override
    public void init(Context context, boolean isDryRun) {
        GoogleAnalytics.getInstance(context).setDryRun(isDryRun);
        // own android tracker
        tracker = GoogleAnalytics.getInstance(context).newTracker("UA-1113296-76");
        tracker.setSessionTimeout(30);
        // Common tracker with iOS
//        tracker = GoogleAnalytics.getInstance(context).newTracker("UA-1113296-83");
        deviceId = Utils.getDeviceId(context);
        this.packageName = context.getPackageName();
    }

    @Override
    public void onStickerSelected(String packName, String stickerName) {
        sendAnalyticsEvent(Category.STICKER, packName, stickerName);
    }

    @Override
    public void onEmojiSelected(String code) {
        sendAnalyticsEvent(Category.EMOJI, Action.USE.getValue(), code);
    }

    @Override
    public void onPackStored(String packName) {
        sendAnalyticsEvent(Category.PACK, Action.INSTALL.getValue(), packName);
    }

    @Override
    public void onPackDeleted(String packName) {
        sendAnalyticsEvent(Category.PACK, Action.REMOVE.getValue(), packName);
    }

    @Override
    public void onMessageCheck(boolean isSticker) {
        checkedMessages += 1;
        if (isSticker) {
            checkedStickersCount += 1;
        }
        if (checkedMessages >= ITEMS_CHECK_SEND_LIMIT) {
            sendAnalyticsEvent(Category.MESSAGE, Action.CHECK.getValue(), "Events count", checkedMessages);
            sendAnalyticsEvent(Category.MESSAGE, Action.CHECK.getValue(), "Stickers count", checkedStickersCount);
            checkedMessages = 0;
            checkedStickersCount = 0;
        }
    }

    @Override
    public void onError(String message) {
        sendAnalyticsEvent(Category.DEV, Action.ERROR.getValue(), message);
    }

    @Override
    public void onWarning(String message) {
        sendAnalyticsEvent(Category.DEV, Action.WARNING.getValue(), message);
    }

    public void sendAnalyticsEvent(Category category, String action, String label) {
        sendAnalyticsEvent(category, action, label, 0);
    }

    public void sendAnalyticsEvent(Category category, String action, String label, int value) {
        tracker.send(new HitBuilders.EventBuilder()
                .setCategory(category.getValue())
                .setAction(action)
                .setLabel(label)
                .setValue(value)
                .setCustomDimension(1, StickersManager.getApiKey())
                .setCustomDimension(2, deviceId)
                .setCustomDimension(3, packageName)
                .build());
        Logger.i(TAG, "Send analytics: \nCategory: " + category.getValue() + "\nAction: " + action + "\nLabel: " + label + "\nValue: " + value);
    }
}
