package vc908.stickerfactory;

import android.app.Activity;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.Target;
import vc908.stickerfactory.R;

/**
 * @author Dmitry Nezhydenko
 */
public class StickerLoader {
    private Context mContext;
    private RequestManager requestManager;

    private String code;
    private int placeholderRes;
    private int placeholderColorFilterRes;
    private Drawable placeholderDrawable;

    public StickerLoader(Context context) {
        this.mContext = context;
        requestManager = Glide.with(context);
    }

    public StickerLoader(Activity activity) {
        this.mContext = activity;
        requestManager = Glide.with(activity);
    }

    public StickerLoader(android.support.v4.app.Fragment fragment) {
        this.mContext = fragment.getActivity();
        requestManager = Glide.with(fragment);
    }

    public StickerLoader loadSticker(String code) {
        this.code = code.toLowerCase();
        return this;
    }

    public StickerLoader setPlaceholderDrawableRes(@DrawableRes int placeholderRes) {
        this.placeholderRes = placeholderRes;
        return this;
    }

    public StickerLoader setPlaceholderColorFilterRes(@ColorRes int colorFilterRes) {
        this.placeholderColorFilterRes = colorFilterRes;
        return this;
    }


    public void into(final ImageView iv) {
        Uri stickerUri = NetworkManager.getInstance().getStickerUri(code);
        if (placeholderRes == 0) {
            placeholderRes = R.drawable.sticker_placeholder;
        }
        placeholderDrawable = iv.getContext().getResources().getDrawable(placeholderRes);
        if (placeholderColorFilterRes > 0) {
            placeholderDrawable.setColorFilter(mContext.getResources().getColor(placeholderColorFilterRes), PorterDuff.Mode.SRC_IN);
        }
        load(iv, stickerUri);
    }

    private void load(@NonNull final ImageView iv, @NonNull Uri stickerUri) {
        requestManager.load(stickerUri)
                .placeholder(placeholderDrawable)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                .into(iv);
    }

}
