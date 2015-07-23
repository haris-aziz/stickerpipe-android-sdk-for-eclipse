package vc908.stickerfactory.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import vc908.stickerfactory.StorageManager;
import vc908.stickerfactory.utils.Logger;

/**
 * @author Dmitry Nezhydenko (dehimb@gmail.com)
 */
public class AnalyticsTaskReceiver extends BroadcastReceiver {

    private static final String TAG = AnalyticsTaskReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Logger.i(TAG, "Start analytics task");
        StorageManager.getInstance().sendAnalyticsEvents();
    }
}
