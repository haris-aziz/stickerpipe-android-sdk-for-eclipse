package vc908.stickerfactory.provider.recentlystickers;

import java.util.Date;

import android.content.ContentResolver;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import vc908.stickerfactory.provider.base.AbstractContentValues;

/**
 * Content values wrapper for the {@code recently_stickers} table.
 */
public class RecentlyStickersContentValues extends AbstractContentValues {
    @Override
    public Uri uri() {
        return RecentlyStickersColumns.CONTENT_URI;
    }

    /**
     * Update row(s) using the values stored by this object and the given selection.
     *
     * @param contentResolver The content resolver to use.
     * @param where The selection to use (can be {@code null}).
     */
    public int update(ContentResolver contentResolver, @Nullable RecentlyStickersSelection where) {
        return contentResolver.update(uri(), values(), where == null ? null : where.sel(), where == null ? null : where.args());
    }

    /**
     * Stickers pack name
     */
    public RecentlyStickersContentValues putPack(@Nullable String value) {
        mContentValues.put(RecentlyStickersColumns.PACK, value);
        return this;
    }

    public RecentlyStickersContentValues putPackNull() {
        mContentValues.putNull(RecentlyStickersColumns.PACK);
        return this;
    }

    /**
     * Sticker's name
     */
    public RecentlyStickersContentValues putName(@Nullable String value) {
        mContentValues.put(RecentlyStickersColumns.NAME, value);
        return this;
    }

    public RecentlyStickersContentValues putNameNull() {
        mContentValues.putNull(RecentlyStickersColumns.NAME);
        return this;
    }

    /**
     * Last using time
     */
    public RecentlyStickersContentValues putLastUsingTime(@Nullable Long value) {
        mContentValues.put(RecentlyStickersColumns.LAST_USING_TIME, value);
        return this;
    }

    public RecentlyStickersContentValues putLastUsingTimeNull() {
        mContentValues.putNull(RecentlyStickersColumns.LAST_USING_TIME);
        return this;
    }
}
