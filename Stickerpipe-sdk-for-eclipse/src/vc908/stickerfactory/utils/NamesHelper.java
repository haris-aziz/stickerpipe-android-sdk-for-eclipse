package vc908.stickerfactory.utils;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.regex.Pattern;

/**
 * Common sticker and pack name generator/separator helper
 *
 * @author Dmitry Nezhydenko
 */
public class NamesHelper {
    private static final String STICKER_NAME_SEPARATOR = "_";
    private static final String STICKER_NAME_FORMAT = "[[%s" + STICKER_NAME_SEPARATOR + "%s]]";
    private static final Pattern STICKER_NAME_PATTERN = Pattern.compile("^\\[\\[[a-zA-Z0-9]+_[a-zA-Z0-9]+\\]\\]$");


    /**
     * Create sticker's code by given pack name and sticker name
     *
     * @param packName    Pack name
     * @param stickerName Sticker name
     * @return Sticker code
     */
    public static String getStickerCode(String packName, String stickerName) {
        return String.format(STICKER_NAME_FORMAT, packName, stickerName);
    }

    /**
     * Get pack name from given sticker code
     *
     * @param code Sticker code
     * @return Pack name
     */
    @Nullable
    public static String getPackName(String code) {
        if (TextUtils.isEmpty(code)) {
            return null;
        } else {
            return substringStickerCode(code).split(STICKER_NAME_SEPARATOR)[0];
        }
    }

    /**
     * Substring open and close brackets from sticker code
     *
     * @param code Sticker code
     * @return Trimmed string
     */
    private static String substringStickerCode(@NonNull String code) {
        return code.substring(code.startsWith("[[") ? 2 : 0, code.endsWith("]]") ? code.length() - 2 : code.length());
    }

    /**
     * Get sticker name from given code
     *
     * @param code Sticker code
     * @return Sticker name
     */
    @Nullable
    public static String getStickerName(String code) {
        if (TextUtils.isEmpty(code)) {
            return null;
        } else {
            String[] parts = substringStickerCode(code).split(STICKER_NAME_SEPARATOR);
            if (parts.length > 1) {
                return parts[1];
            } else {
                return null;
            }
        }
    }

    /**
     * Check, is given code is a sticker code
     * @param code Input code
     * @return Result of check
     */
    public static boolean isSticker(String code) {
        return !TextUtils.isEmpty(code) && STICKER_NAME_PATTERN.matcher(code).matches();
    }
}
