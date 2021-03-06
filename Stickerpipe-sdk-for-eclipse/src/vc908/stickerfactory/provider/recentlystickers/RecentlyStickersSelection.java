package vc908.stickerfactory.provider.recentlystickers;

import java.util.Date;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;

import vc908.stickerfactory.provider.base.AbstractSelection;

/**
 * Selection for the {@code recently_stickers} table.
 */
public class RecentlyStickersSelection extends AbstractSelection<RecentlyStickersSelection> {
    @Override
    protected Uri baseUri() {
        return RecentlyStickersColumns.CONTENT_URI;
    }

    /**
     * Query the given content resolver using this selection.
     *
     * @param contentResolver The content resolver to query.
     * @param projection A list of which columns to return. Passing null will return all columns, which is inefficient.
     * @param sortOrder How to order the rows, formatted as an SQL ORDER BY clause (excluding the ORDER BY itself). Passing null will use the default sort
     *            order, which may be unordered.
     * @return A {@code RecentlyStickersCursor} object, which is positioned before the first entry, or null.
     */
    public RecentlyStickersCursor query(ContentResolver contentResolver, String[] projection, String sortOrder) {
        Cursor cursor = contentResolver.query(uri(), projection, sel(), args(), sortOrder);
        if (cursor == null) return null;
        return new RecentlyStickersCursor(cursor);
    }

    /**
     * Equivalent of calling {@code query(contentResolver, projection, null)}.
     */
    public RecentlyStickersCursor query(ContentResolver contentResolver, String[] projection) {
        return query(contentResolver, projection, null);
    }

    /**
     * Equivalent of calling {@code query(contentResolver, projection, null, null)}.
     */
    public RecentlyStickersCursor query(ContentResolver contentResolver) {
        return query(contentResolver, null, null);
    }


    public RecentlyStickersSelection id(long... value) {
        addEquals("recently_stickers." + RecentlyStickersColumns._ID, toObjectArray(value));
        return this;
    }

    public RecentlyStickersSelection pack(String... value) {
        addEquals(RecentlyStickersColumns.PACK, value);
        return this;
    }

    public RecentlyStickersSelection packNot(String... value) {
        addNotEquals(RecentlyStickersColumns.PACK, value);
        return this;
    }

    public RecentlyStickersSelection packLike(String... value) {
        addLike(RecentlyStickersColumns.PACK, value);
        return this;
    }

    public RecentlyStickersSelection packContains(String... value) {
        addContains(RecentlyStickersColumns.PACK, value);
        return this;
    }

    public RecentlyStickersSelection packStartsWith(String... value) {
        addStartsWith(RecentlyStickersColumns.PACK, value);
        return this;
    }

    public RecentlyStickersSelection packEndsWith(String... value) {
        addEndsWith(RecentlyStickersColumns.PACK, value);
        return this;
    }

    public RecentlyStickersSelection name(String... value) {
        addEquals(RecentlyStickersColumns.NAME, value);
        return this;
    }

    public RecentlyStickersSelection nameNot(String... value) {
        addNotEquals(RecentlyStickersColumns.NAME, value);
        return this;
    }

    public RecentlyStickersSelection nameLike(String... value) {
        addLike(RecentlyStickersColumns.NAME, value);
        return this;
    }

    public RecentlyStickersSelection nameContains(String... value) {
        addContains(RecentlyStickersColumns.NAME, value);
        return this;
    }

    public RecentlyStickersSelection nameStartsWith(String... value) {
        addStartsWith(RecentlyStickersColumns.NAME, value);
        return this;
    }

    public RecentlyStickersSelection nameEndsWith(String... value) {
        addEndsWith(RecentlyStickersColumns.NAME, value);
        return this;
    }

    public RecentlyStickersSelection lastUsingTime(Long... value) {
        addEquals(RecentlyStickersColumns.LAST_USING_TIME, value);
        return this;
    }

    public RecentlyStickersSelection lastUsingTimeNot(Long... value) {
        addNotEquals(RecentlyStickersColumns.LAST_USING_TIME, value);
        return this;
    }

    public RecentlyStickersSelection lastUsingTimeGt(long value) {
        addGreaterThan(RecentlyStickersColumns.LAST_USING_TIME, value);
        return this;
    }

    public RecentlyStickersSelection lastUsingTimeGtEq(long value) {
        addGreaterThanOrEquals(RecentlyStickersColumns.LAST_USING_TIME, value);
        return this;
    }

    public RecentlyStickersSelection lastUsingTimeLt(long value) {
        addLessThan(RecentlyStickersColumns.LAST_USING_TIME, value);
        return this;
    }

    public RecentlyStickersSelection lastUsingTimeLtEq(long value) {
        addLessThanOrEquals(RecentlyStickersColumns.LAST_USING_TIME, value);
        return this;
    }
}
