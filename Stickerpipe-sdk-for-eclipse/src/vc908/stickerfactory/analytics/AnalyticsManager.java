package vc908.stickerfactory.analytics;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import vc908.stickerfactory.utils.Logger;

/**
 * Class wrapper for holding all analytics implementations and interact
 * with them through {@link IAnalytics} interface
 *
 * @author Dmitry Nezhydenko
 */

public class AnalyticsManager implements IAnalytics {

    private static final String TAG = AnalyticsManager.class.getSimpleName();
    private List<IAnalytics> analyticsList = new ArrayList<>();

    private static AnalyticsManager instance;

    /**
     * Singleton getter implementation
     *
     * @return manager instance
     */
    public static synchronized AnalyticsManager getInstance() {
        if (instance == null) {
            instance = new AnalyticsManager();
        }
        return instance;
    }

    /**
     * Private constructor to prevent new instances creation
     */
    private AnalyticsManager() {
    }

    /**
     * Add analytic implementation to list
     *
     * @param analytics Analytic implementation
     */
    public void addAnalytics(IAnalytics analytics) {
        analyticsList.add(analytics);
    }

    @Override
    public void init(Context context, boolean isDryRun) {
        Logger.i(TAG, "Initialize analytics with dry run key: " + isDryRun);
        for (IAnalytics analytics : analyticsList) {
            analytics.init(context, isDryRun);
        }
    }

    @Override
    public void onStickerSelected(String packName, String stickerName) {
        for (IAnalytics analytics : analyticsList) {
            analytics.onStickerSelected(packName, stickerName);
        }
    }

    @Override
    public void onEmojiSelected(String code) {
        for (IAnalytics analytics : analyticsList) {
            analytics.onEmojiSelected(code);
        }
    }

    @Override
    public void onPackStored(String packName) {
        for (IAnalytics analytics : analyticsList) {
            analytics.onPackStored(packName);
        }
    }

    @Override
    public void onPackDeleted(String packName) {
        for (IAnalytics analytics : analyticsList) {
            analytics.onPackDeleted(packName);
        }
    }

    @Override
    public void onMessageCheck(boolean isSticker) {
        for (IAnalytics analytics : analyticsList) {
            analytics.onMessageCheck(isSticker);
        }
    }

    @Override
    public void onError(String message) {
        for (IAnalytics analytics : analyticsList) {
            analytics.onError(message);
        }
    }

    @Override
    public void onWarning(String message) {
        for (IAnalytics analytics : analyticsList) {
            analytics.onWarning(message);
        }
    }
}