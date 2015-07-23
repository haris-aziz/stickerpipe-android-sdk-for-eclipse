package vc908.stickerfactory.ui.adapter;

import android.net.Uri;
import android.support.annotation.ColorRes;
import android.support.annotation.Nullable;

/**
 * Interface for interact with tab images from adapter
 */
public interface IImagesTitleAdapter {
    /**
     * Get tab {@link Uri} for item.
     *
     * @param position Item position
     * @return Tab image {@link Uri}
     */
    @Nullable
    Uri getTabImage(int position);

    /**
     * Get tab image color filter
     *
     * @param position Item position
     * @return Color filter
     */
    @ColorRes
    int getTabImageColorFilter(int position);
}
