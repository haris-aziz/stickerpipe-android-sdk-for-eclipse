package vc908.stickerfactory.provider.recentlystickers;

import android.net.Uri;
import android.provider.BaseColumns;

import vc908.stickerfactory.provider.StickersProvider;
import vc908.stickerfactory.provider.analytics.AnalyticsColumns;
import vc908.stickerfactory.provider.packs.PacksColumns;
import vc908.stickerfactory.provider.recentlystickers.RecentlyStickersColumns;
import vc908.stickerfactory.provider.stickers.StickersColumns;

/**
 * Stickers list.
 */
public class RecentlyStickersColumns implements BaseColumns {
    public static final String TABLE_NAME = "recently_stickers";
    public static final Uri CONTENT_URI = Uri.parse(StickersProvider.CONTENT_URI_BASE + "/" + TABLE_NAME);

    /**
     * Primary key.
     */
    public static final String _ID = BaseColumns._ID;

    /**
     * Stickers pack name
     */
    public static final String PACK = "pack";

    /**
     * Sticker's name
     */
    public static final String NAME = "name";

    /**
     * Last using time
     */
    public static final String LAST_USING_TIME = "last_using_time";


    public static final String DEFAULT_ORDER = TABLE_NAME + "." +_ID;

    // @formatter:off
    public static final String[] ALL_COLUMNS = new String[] {
            _ID,
            PACK,
            NAME,
            LAST_USING_TIME
    };
    // @formatter:on

    public static boolean hasColumns(String[] projection) {
        if (projection == null) return true;
        for (String c : projection) {
            if (c.equals(PACK) || c.contains("." + PACK)) return true;
            if (c.equals(NAME) || c.contains("." + NAME)) return true;
            if (c.equals(LAST_USING_TIME) || c.contains("." + LAST_USING_TIME)) return true;
        }
        return false;
    }

}
