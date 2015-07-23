package vc908.stickerfactory;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;

import vc908.stickerfactory.analytics.AnalyticsManager;
import vc908.stickerfactory.analytics.GoogleAnalyticsImpl;
import vc908.stickerfactory.analytics.LocalAnalyticsImpl;
import vc908.stickerfactory.provider.StickersProvider;
import vc908.stickerfactory.utils.Logger;
import vc908.stickerfactory.utils.NamesHelper;
import vc908.stickerfactory.utils.Utils;

/**
 * Main stickers manager class
 *
 * @author Dmitry Nezhydenko
 */
public class StickersManager {
    private static final String TAG = StickersManager.class.getSimpleName();
    private static StickersManager instance;
    public static Context applicationContext;
    private static String clientApiKey;

    /**
     * Manager initialization. Must be call before using.
     * Throws exception if called twice
     *
     * @param context Manager context
     */
    public static void initialize(@NonNull String apiKey, @NonNull Context context) {
        if (instance == null) {
            instance = new StickersManager(apiKey, context);
        } else {
            Logger.e(TAG, "Sticker manager already initialized");
        }
    }

    /**
     * Change logging level
     *
     * @param isEnabled Is logging enabled
     */
    public static void setLoggingEnabled(boolean isEnabled) {
        Logger.setConsoleLoggingEnabled(isEnabled);
    }

    /**
     * Check, is given code is sticker
     *
     * @param code String code
     * @return Result of inspection
     */
    public static boolean isSticker(String code) {
        if (instance == null) {
            Logger.e(TAG, "Stickers manager not initialized");
            return false;
        } else {
            boolean result = NamesHelper.isSticker(code);
            AnalyticsManager.getInstance().onMessageCheck(result);
            return result;
        }
    }

    public static StickerLoader with(Context context) throws RuntimeException {
        if (instance == null) {
            Logger.e(TAG, "Stickers manager not initialized");
            throw new RuntimeException("Stickers manager not initialized");
        } else {
            return new StickerLoader(context);
        }
    }

    public static StickerLoader with(Activity activity) throws RuntimeException {
        if (instance == null) {
            Logger.e(TAG, "Stickers manager not initialized");
            throw new RuntimeException("Stickers manager not initialized");
        } else {
            return new StickerLoader(activity);
        }
    }

    public static StickerLoader with(android.support.v4.app.Fragment fragment) throws RuntimeException {
        if (instance == null) {
            Logger.e(TAG, "Stickers manager not initialized");
            throw new RuntimeException("Stickers manager not initialized");
        } else {
            return new StickerLoader(fragment);
        }
    }

    public static StickerLoader with(FragmentActivity fragmentActivity) throws RuntimeException {
        if (instance == null) {
            Logger.e(TAG, "Stickers manager not initialized");
            throw new RuntimeException("Stickers manager not initialized");
        } else {
            return new StickerLoader(fragmentActivity);
        }
    }

    /**
     * Private constructor to prevent another objects creation. Init main logic
     *
     * @param apiKey  Application api key
     * @param context Manager context
     */
    private StickersManager(String apiKey, Context context) {
        applicationContext = context;
        clientApiKey = apiKey;
        StickersProvider.initAuthority(context.getPackageName());
        StorageManager.init(context);
        NetworkManager.init(context, apiKey);
        initAnalytics(context, clientApiKey);
        startTasks(context);
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        BroadcastReceiver networkStateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (Utils.isNetworkAvailable(context) && !StorageManager.getInstance().isStickersExists()) {
                    NetworkManager.getInstance().updateStickersList();
                }
            }
        };
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            Utils.statusBarHeight = context.getResources().getDimensionPixelSize(resourceId);
        }
        context.registerReceiver(networkStateReceiver, filter);

    }

    /**
     * Start background job
     *
     * @param context Tasks context
     */
    private void startTasks(Context context) {
        JobScheduler.getInstance(context).start();
    }

    /**
     * Add analytics implementations and init them
     *
     * @param context Manager context
     */
    private void initAnalytics(Context context, String clientApiKey) {
        AnalyticsManager am = AnalyticsManager.getInstance();
        am.addAnalytics(new LocalAnalyticsImpl());
        am.addAnalytics(new GoogleAnalyticsImpl());
        am.init(context, false);
    }


    /**
     * Check, is manager initialized
     *
     * @return Result of inspection
     */
    public static boolean isInitialized() {
        return instance != null;
    }

    /**
     * Get apiKey manager initialized with
     *
     * @return Api key
     */
    public static String getApiKey() {
        return clientApiKey;
    }
}
