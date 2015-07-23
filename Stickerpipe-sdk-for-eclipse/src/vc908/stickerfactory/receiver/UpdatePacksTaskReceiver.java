package vc908.stickerfactory.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import vc908.stickerfactory.NetworkManager;
import vc908.stickerfactory.utils.Logger;

/**
 * @author Dmitry Nezhydenko (dehimb@gmail.com)
 */
public class UpdatePacksTaskReceiver extends BroadcastReceiver {

    private static final String TAG = UpdatePacksTaskReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Logger.i(TAG, "Start update packs task");
        NetworkManager.getInstance().checkPackUpdates();
    }
}
