package vc908.stickerfactory.ui.fragment;

import android.database.Cursor;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import vc908.stickerfactory.Constants;
import vc908.stickerfactory.NetworkManager;
import vc908.stickerfactory.R;
import vc908.stickerfactory.provider.stickers.StickersColumns;
import vc908.stickerfactory.provider.stickers.StickersCursor;
import vc908.stickerfactory.ui.OnStickerSelectedListener;
import vc908.stickerfactory.ui.adapter.CursorRecyclerViewAdapter;
import vc908.stickerfactory.ui.view.SquareImageView;
import vc908.stickerfactory.utils.Logger;
import vc908.stickerfactory.utils.NamesHelper;
import vc908.stickerfactory.utils.Utils;

/**
 * @author Dmitry Nezhydenko
 */
public class StickersListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = StickersListFragment.class.getSimpleName();
    private List<OnStickerSelectedListener> stickerSelectedListeners = new ArrayList<>();
    protected StickersAdapter adapter;

    private RecyclerView rv;
    private String packName;
    private View progress;
    private View layout;
    private int placeholderDrawableRes = R.drawable.sticker_placeholder;
    private int placeholderFilterColorRes;
    private int padding;
    private int maxStickerWidth = (int) Utils.dp(160);
    private PorterDuffColorFilter selectedItemFilterColor;
    private PorterDuffColorFilter placeholderColorFilter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            packName = getArguments().getString(Constants.ARGUMENT_PACK);
            int argumentPlaceholderDrawableRes = getArguments().getInt(Constants.ARGUMENT_PLACEHOLDER_DRAWABLE);
            if (argumentPlaceholderDrawableRes > 0) {
                placeholderDrawableRes = argumentPlaceholderDrawableRes;
            }
            placeholderFilterColorRes = getArguments().getInt(Constants.ARGUMENT_PLACEHOLDER_FILTER_COLOR);
            int argMaxStickerWidth = getArguments().getInt(Constants.ARGUMENT_MAX_STICKER_WIDTH);
            if (argMaxStickerWidth > 0) {
                maxStickerWidth = argMaxStickerWidth;
            }
        }
        getLoaderManager().initLoader(getLoaderId(), null, this);
        selectedItemFilterColor = new PorterDuffColorFilter(0xffdddddd, PorterDuff.Mode.MULTIPLY);
        if (placeholderFilterColorRes > 0) {
            placeholderColorFilter = new PorterDuffColorFilter(getResources().getColor(placeholderFilterColorRes), PorterDuff.Mode.SRC_IN);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (layout == null) {
            layout = inflater.inflate(getLayoutId(), container, false);
            rv = (RecyclerView) layout.findViewById(R.id.recycler_view);
            progress = layout.findViewById(R.id.progress);
            padding = getResources().getDimensionPixelSize(R.dimen.material_8);
            // calculate stickers columns count

            int columnsCount = (int) Math.ceil(Utils.getScreenWidthInPx() / ((float) maxStickerWidth + padding * 2));
            GridLayoutManager lm = (new GridLayoutManager(getActivity(), columnsCount));
            rv.setLayoutManager(lm);
        }
        return layout;
    }

    protected int getLayoutId() {
        return R.layout.fragment_stickers_list;
    }

    public void addStickerSelectedListener(OnStickerSelectedListener stickerSelectedListener) {
        this.stickerSelectedListeners.add(stickerSelectedListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(
                getActivity(),
                StickersColumns.CONTENT_URI,
                new String[]{StickersColumns._ID, StickersColumns.PACK, StickersColumns.NAME},
                StickersColumns.PACK + "=?",
                new String[]{packName},
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        progress.setVisibility(View.GONE);
        if (adapter == null) {
            adapter = new StickersAdapter(cursor);
            Logger.d(TAG, "Adapter log: List adapter: " + packName + " : Looper : " + Looper.myLooper() + " : " + adapter);
            rv.setAdapter(adapter);
        } else {
            adapter.changeCursor(cursor);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (adapter != null) {
            adapter.changeCursor(null);
        }
    }

    protected int getLoaderId() {
        return new Random().nextInt(Integer.MAX_VALUE);
    }


    protected class StickersAdapter extends CursorRecyclerViewAdapter<StickersAdapter.ViewHolder> {

        public StickersAdapter(Cursor cursor) {
            super(cursor);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            ImageView iv = new SquareImageView(getActivity());
            iv.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            iv.setPadding(padding, padding, padding, padding);
            return new ViewHolder(iv);
        }

        @Override
        public int getItemCount() {
            return getCursor().getCount();
        }

        @Override
        public void onBindViewHolder(final ViewHolder viewHolder, Cursor cursor) {
            StickersCursor stickerCursor = new StickersCursor(cursor);
            viewHolder.code = NamesHelper.getStickerCode(stickerCursor.getPack(), stickerCursor.getName());
            Uri stickerUri = NetworkManager.getInstance().getStickerUri(viewHolder.code);
            viewHolder.iv.setOnTouchListener(null);
            viewHolder.iv.setTag(R.drawable.sticker_placeholder, true);

            Glide.with(StickersListFragment.this)
                    .load(stickerUri)
                    .placeholder(viewHolder.placeholderDrawable)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                    .listener(new RequestListener<Uri, GlideDrawable>() {
                        @Override
                        public boolean onException(Exception e, Uri model, Target<GlideDrawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GlideDrawable resource, Uri model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            viewHolder.iv.setOnTouchListener(imageTouchListener);
                            viewHolder.iv.setTag(R.drawable.sticker_placeholder, false);
                            return false;
                        }
                    })
                    .into(viewHolder.iv);
            if (placeholderColorFilter != null && viewHolder.placeholderDrawable != null) {
                viewHolder.placeholderDrawable.setColorFilter(placeholderColorFilter);
            }
            viewHolder.iv.setOnTouchListener(imageTouchListener);
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            private ImageView iv;
            private String code;
            private Drawable placeholderDrawable;

            public ViewHolder(View itemView) {
                super(itemView);
                this.iv = (ImageView) itemView;
                iv.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
					    if (stickerSelectedListeners != null && stickerSelectedListeners.size() > 0) {
					        for (OnStickerSelectedListener listener : stickerSelectedListeners) {
					            listener.onStickerSelected(code);
					        }
					    }
					}
				});
                placeholderDrawable = getResources().getDrawable(placeholderDrawableRes);
            }
        }
    }

    View.OnTouchListener imageTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            Boolean isCurrentDrawablePlaceholder = (Boolean) v.getTag(R.drawable.sticker_placeholder);
            Logger.d(TAG, "Touch image. Is placeholder: " + isCurrentDrawablePlaceholder);
            if (v instanceof ImageView && isCurrentDrawablePlaceholder != null && !isCurrentDrawablePlaceholder) {
                ImageView touchedImageView = (ImageView) v;

                int action = event.getActionMasked();
                if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN) {
                    updateDrawable(touchedImageView, selectedItemFilterColor);
                } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP || action == MotionEvent.ACTION_CANCEL) {
                    updateDrawable(touchedImageView, null);
                }
            }
            return false;
        }

        private void updateDrawable(ImageView imageView, @Nullable ColorFilter colorFilter) {
            if (imageView.getDrawable() != null) {
                Logger.d(TAG, "Updating color filter");
                imageView.getDrawable().setColorFilter(colorFilter);
                imageView.invalidateDrawable(imageView.getDrawable());
            }
        }
    };
}
