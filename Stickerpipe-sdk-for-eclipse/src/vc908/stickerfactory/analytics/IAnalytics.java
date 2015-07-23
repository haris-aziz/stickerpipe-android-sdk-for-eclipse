package vc908.stickerfactory.analytics;

import android.content.Context;

/**
 * Analytics interface. Describe common analytics methods
 *
 * @author Dmytro Nezhydenko
 */
public interface IAnalytics {
    /**
     * Limit of local stored items count before sending
     */
    int ITEMS_CHECK_SEND_LIMIT = 50;

    enum Category {
        PACK("pack"),
        STICKER("sticker"),
        EMOJI("emoji"),
        MESSAGE("message"),
        DEV("dev"),
        TEST("test");

        private final String value;

        Category(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    enum Action {
        USE("use"),
        INSTALL("install"),
        REMOVE("remove"),
        CHECK("check"),
        SHOW("show"),
        ERROR("error"),
        WARNING("warning");

        private final String value;

        Action(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    /**
     * Initialize analytic instance
     *
     * @param context  Analytic context
     * @param isDryRun Is prevent analytics to send
     */
    void init(Context context, boolean isDryRun);

    /**
     * Call when user select a sticker
     *
     * @param packName    Pack name
     * @param stickerName Sticker name
     */
    void onStickerSelected(String packName, String stickerName);

    /**
     * Call when user select emoji
     *
     * @param code Emoji code
     */
    void onEmojiSelected(String code);

    /**
     * Call when pack store al local storage
     *
     * @param packName Pack name
     */

    void onPackStored(String packName);

    /**
     * Call when pack deleted by user
     *
     * @param packName PAck name
     */
    void onPackDeleted(String packName);

    /**
     * Call when application check is message a sticker
     *
     * @param isSticker Is checked message a sticker
     */
    void onMessageCheck(boolean isSticker);

    /**
     * Call when error occurred
     *
     * @param message Error message
     */
    void onError(String message);

    /**
     * Call when warning occurred
     *
     * @param message Warning message
     */
    void onWarning(String message);

}
