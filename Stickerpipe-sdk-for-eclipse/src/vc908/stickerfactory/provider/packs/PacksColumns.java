package vc908.stickerfactory.provider.packs;

import android.net.Uri;
import android.provider.BaseColumns;

import vc908.stickerfactory.provider.StickersProvider;
import vc908.stickerfactory.provider.analytics.AnalyticsColumns;
import vc908.stickerfactory.provider.packs.PacksColumns;
import vc908.stickerfactory.provider.recentlystickers.RecentlyStickersColumns;
import vc908.stickerfactory.provider.stickers.StickersColumns;

/**
 * Packs list
 */
public class PacksColumns implements BaseColumns {
    public static final String TABLE_NAME = "packs";
    public static final Uri CONTENT_URI = Uri.parse(StickersProvider.CONTENT_URI_BASE + "/" + TABLE_NAME);

    /**
     * Primary key.
     */
    public static final String _ID = BaseColumns._ID;

    /**
     * Pack name
     */
    public static final String NAME = "name";

    /**
     * Pack order
     */
    public static final String PACK_ORDER = "pack_order";

    /**
     * Pack title
     */
    public static final String TITLE = "title";

    /**
     * Pack Artist
     */
    public static final String ARTIST = "artist";

    /**
     * Pack price
     */
    public static final String PRICE = "price";


    public static final String DEFAULT_ORDER = TABLE_NAME + "." +_ID;

    // @formatter:off
    public static final String[] ALL_COLUMNS = new String[] {
            _ID,
            NAME,
            PACK_ORDER,
            TITLE,
            ARTIST,
            PRICE
    };
    // @formatter:on

    public static boolean hasColumns(String[] projection) {
        if (projection == null) return true;
        for (String c : projection) {
            if (c.equals(NAME) || c.contains("." + NAME)) return true;
            if (c.equals(PACK_ORDER) || c.contains("." + PACK_ORDER)) return true;
            if (c.equals(TITLE) || c.contains("." + TITLE)) return true;
            if (c.equals(ARTIST) || c.contains("." + ARTIST)) return true;
            if (c.equals(PRICE) || c.contains("." + PRICE)) return true;
        }
        return false;
    }

}
