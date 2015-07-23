/*
 * This is the source code of Telegram for Android v. 1.3.2.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013.
 */

package vc908.stickerfactory.ui.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;

import vc908.stickerfactory.StorageManager;
import vc908.stickerfactory.utils.Logger;
import vc908.stickerfactory.utils.Utils;

public class KeyboardHandleRelativeLayout extends RelativeLayout {

    private static final String TAG = KeyboardHandleRelativeLayout.class.getSimpleName();
    private OnKeyboardHideCallback keyboardHideCallback;
    private Rect rect = new Rect();
    public KeyboardSizeChangeListener listener;
    private boolean isKeyboardVisible = false;

    public interface KeyboardSizeChangeListener {
        void onKeyboardVisibilityChanged(boolean isVisible);
    }

    public interface OnKeyboardHideCallback {
        void onKeyboardHide();
    }

    public KeyboardHandleRelativeLayout(Context context) {
        super(context);
    }

    public KeyboardHandleRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public KeyboardHandleRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (listener != null && changed) {
            int keyboardHeight = StorageManager.getInstance().getKeyboardHeight(Utils.getCurrentOrientation());
            View rootView = this.getRootView();
            int usableViewHeight = rootView.getHeight() - Utils.statusBarHeight - Utils.getViewInset(rootView);
            this.getWindowVisibleDisplayFrame(rect);
            int calculatedKeyboardHeight = usableViewHeight - (rect.bottom - rect.top);

            if (calculatedKeyboardHeight > Utils.dp(50)) {
                isKeyboardVisible = true;
                if (keyboardHeight != calculatedKeyboardHeight) {
                    keyboardHeight = calculatedKeyboardHeight;
                    StorageManager.getInstance().storeKeyboardHeight(Utils.getCurrentOrientation(), keyboardHeight);
                }
            } else {
                isKeyboardVisible = false;
            }
            listener.onKeyboardVisibilityChanged(isKeyboardVisible);
            Logger.d(TAG, "Keyboard visible: " + isKeyboardVisible);
            if (!isKeyboardVisible && keyboardHideCallback != null) {
                keyboardHideCallback.onKeyboardHide();
                keyboardHideCallback = null;
            }
        }
    }

    public boolean isKeyboardVisible() {
        return isKeyboardVisible;
    }

    public void hideKeyboard(Activity activity, OnKeyboardHideCallback callback) {
        if (isKeyboardVisible) {
            keyboardHideCallback = callback;
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (!imm.isActive()) {
                return;
            }
            imm.hideSoftInputFromWindow(activity.findViewById(android.R.id.content).getWindowToken(), 0);
        } else {
            callback.onKeyboardHide();
        }
    }
}
