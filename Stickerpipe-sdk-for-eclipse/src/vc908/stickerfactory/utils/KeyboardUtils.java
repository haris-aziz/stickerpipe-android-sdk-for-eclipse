package vc908.stickerfactory.utils;

import vc908.stickerfactory.StorageManager;

/**
 * @author Dmitry Nezhydenko (dehimb@gmail.com)
 */
public class KeyboardUtils {
    public static final String TAG = KeyboardUtils.class.getSimpleName();

    public static int getKeyboardHeight() {
        return StorageManager.getInstance().getKeyboardHeight(Utils.getCurrentOrientation());
    }
}