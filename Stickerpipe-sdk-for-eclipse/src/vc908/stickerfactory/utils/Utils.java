package vc908.stickerfactory.utils;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;

import java.lang.reflect.Field;
import java.util.UUID;

import vc908.stickerfactory.StickersManager;
import vc908.stickerfactory.StorageManager;

/**
 * Common utils
 *
 * @author Dmitry Nezhydenko
 */
public class Utils {

    private static final String TAG = Utils.class.getSimpleName();
    private static Point size;
    public static int statusBarHeight;

    /**
     * Generate uniq device ID
     *
     * @param context Utils context
     * @return Device ID
     */
    public static String getDeviceId(Context context) {
        String deviceId = StorageManager.getInstance().getDeviceId();
        if (TextUtils.isEmpty(deviceId)) {
            final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

            final String tmDevice, tmSerial, androidId;
            tmDevice = "" + tm.getDeviceId();
            tmSerial = "" + tm.getSimSerialNumber();
            androidId = "" + android.provider.Settings.Secure.getString(context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);

            UUID deviceUuid = new UUID(androidId.hashCode(), ((long) tmDevice.hashCode() << 32) | tmSerial.hashCode());
            deviceId = deviceUuid.toString();
            StorageManager.getInstance().storeDeviceId(deviceId);
        }
        return deviceId;
    }

    /**
     * Get screen physical width in pixels according to orientation
     *
     * @return Screen width
     */
    public static int getScreenWidthInPx() {
        if (size == null) {
            calculateScreenSize();
        }
        switch (getCurrentOrientation()) {
            case Configuration.ORIENTATION_LANDSCAPE:
                return size.y;
            case Configuration.ORIENTATION_PORTRAIT:
            default:
                return size.x;
        }
    }

    /**
     * Get screen physical height in pixels according to orientation
     *
     * @return Screen width
     */
    public static int getScreenHeightInPx() {
        if (size == null) {
            calculateScreenSize();
        }
        switch (getCurrentOrientation()) {
            case Configuration.ORIENTATION_LANDSCAPE:
                return size.x;
            case Configuration.ORIENTATION_PORTRAIT:
            default:
                return size.y;
        }
    }

    /**
     * Calculate screen size
     */
    private static void calculateScreenSize() {
        Display display = ((WindowManager) StickersManager.applicationContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        size = new Point();
        switch (getCurrentOrientation()) {
            case Configuration.ORIENTATION_LANDSCAPE:
                size.y = display.getWidth();
                size.x = display.getHeight();
                break;
            case Configuration.ORIENTATION_PORTRAIT:
            default:
                size.x = display.getWidth();
                size.y = display.getHeight();
        }
    }

    /**
     * Create {@link Uri} for given drawable resource
     *
     * @param drawableId Drawable resource id
     * @param context    Util context
     * @return Absolute Uri for drawable resource
     */
    public static Uri getDrawableUri(@DrawableRes int drawableId, Context context) {
        return Uri.parse(String.format("%s://%s/%d", ContentResolver.SCHEME_ANDROID_RESOURCE, context.getPackageName(), drawableId));
    }

    /**
     * Check, is any network connection available
     *
     * @param context Util context
     * @return Result of inspection
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    /**
     * Convert density independent pixels to pixels
     *
     * @param val Value to convert
     * @return Size in pixels
     */
    public static int dp(int val) {
        return (int) (StickersManager.applicationContext.getResources().getDisplayMetrics().density * val);
    }

    /**
     * Return current screen orientation
     *
     * @return {@link Configuration#ORIENTATION_LANDSCAPE} or Configuration#ORIENTATION_LANDSCAPE
     */
    public static int getCurrentOrientation() {
        WindowManager wm = (WindowManager) StickersManager.applicationContext.getApplicationContext().getSystemService(Activity.WINDOW_SERVICE);
        int rotation = wm.getDefaultDisplay().getRotation();
        if (rotation == Surface.ROTATION_270 || rotation == Surface.ROTATION_90) {
            return Configuration.ORIENTATION_LANDSCAPE;
        } else {
            return Configuration.ORIENTATION_PORTRAIT;
        }
    }

    public static int getViewInset(View view) {
        if (view == null || Build.VERSION.SDK_INT < 21) {
            return 0;
        }
        try {
            Field mAttachInfoField = View.class.getDeclaredField("mAttachInfo");
            mAttachInfoField.setAccessible(true);
            Object mAttachInfo = mAttachInfoField.get(view);
            if (mAttachInfo != null) {
                Field mStableInsetsField = mAttachInfo.getClass().getDeclaredField("mStableInsets");
                mStableInsetsField.setAccessible(true);
                Rect insets = (Rect) mStableInsetsField.get(mAttachInfo);
                return insets.bottom;
            }
        } catch (Exception e) {
            Logger.e(TAG, "Failed to get view inset", e);
        }
        return 0;
    }

    /**
     * Get screen density
     *
     * @param context Util context
     * @return Screen density in PPI
     */
    public static float getDensity(Context context) {
        return context.getResources().getDisplayMetrics().density * 160f;
    }

    /**
     * Get density sting representation
     *
     * @param context Context
     * @return Density name
     */
    public static String getDensityName(Context context) {
        float density = context.getResources().getDisplayMetrics().density;
        if (density >= 3.0) {
            return "xxhdpi";
        } else if (density >= 2.0) {
            return "xhdpi";
        } else if (density >= 1.5) {
            return "hdpi";
        } else {
            return "mdpi";
        }
    }


    /**
     * Create selectable background according to theme
     *
     * @param context Drawable context
     * @return Selector drawable
     */
    public static Drawable createSelectableBackground(Context context) {
        int[] attrs = new int[]{android.R.attr.selectableItemBackground /* index 0 */};
        TypedArray ta = context.obtainStyledAttributes(attrs);
        Drawable drawableFromTheme = ta.getDrawable(0 /* index */);
        ta.recycle();
        return drawableFromTheme;
    }
}
