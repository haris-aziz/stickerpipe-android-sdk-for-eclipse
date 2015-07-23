package vc908.stickerfactory.ui.fragment;

import android.database.Cursor;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import vc908.stickerfactory.Constants;
import vc908.stickerfactory.R;
import vc908.stickerfactory.provider.recentlystickers.RecentlyStickersColumns;
import vc908.stickerfactory.provider.stickers.StickersColumns;

/**
 * Recent stickers list
 *
 * @author Dmitry Nezhydenko
 */
public class RecentStickersFragment extends StickersListFragment {

    public static final String ARGUMENT_EMPTY_TEXT = "argument_empty_text";
    public static final String ARGUMENT_EMPTY_IMAGE = "argument_empty_image";
    public static final String ARGUMENT_EMPTY_TEXT_COLOR = "argument_empty_text_color";
    public static final String ARGUMENT_EMPTY_FILTER_COLOR = "argument_empty_filter_color";

    private TextView emptyView;
    private int emptyTextRes = R.string.recently_empty;
    private int emptyTextColorRes = R.color.primary;
    private int emptyImageRes = R.drawable.empty_recent;
    private int emptyFilterColor;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            int argumentEmptyText = getArguments().getInt(ARGUMENT_EMPTY_TEXT);
            int argumentEmptyImage = getArguments().getInt(ARGUMENT_EMPTY_IMAGE);
            int argumentEmptyTextColor = getArguments().getInt(ARGUMENT_EMPTY_TEXT_COLOR);
            if (argumentEmptyText > 0) {
                emptyTextRes = argumentEmptyText;
            }
            if (argumentEmptyTextColor > 0) {
                emptyTextColorRes = argumentEmptyTextColor;
            }
            if (argumentEmptyImage > 0) {
                emptyImageRes = argumentEmptyImage;
            }
            emptyFilterColor = getArguments().getInt(ARGUMENT_EMPTY_FILTER_COLOR);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View layout = super.onCreateView(inflater, container, savedInstanceState);
        if (layout != null) {
            emptyView = (TextView) layout.findViewById(R.id.empty_view);
        }
        return layout;
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(
                getActivity(),
                RecentlyStickersColumns.CONTENT_URI,
                new String[]{StickersColumns._ID, StickersColumns.PACK, StickersColumns.NAME, RecentlyStickersColumns.LAST_USING_TIME},
                null,
                null,
                RecentlyStickersColumns.LAST_USING_TIME + " DESC LIMIT " + Constants.RECENT_STICKERS_COUNT
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        super.onLoadFinished(loader, cursor);
        updateEmptyViewState();
    }

    private void updateEmptyViewState() {
        if (emptyView != null) {
            if (adapter.getItemCount() > 0) {
                emptyView.setVisibility(View.GONE);
            } else {
                emptyView.setVisibility(View.VISIBLE);
                emptyView.setText(getText(emptyTextRes));
                emptyView.setTextColor(getResources().getColor(emptyTextColorRes));
                emptyView.setCompoundDrawablesWithIntrinsicBounds(0, emptyImageRes, 0, 0);
                if (emptyFilterColor > 0) {
                    for (Drawable drawable : emptyView.getCompoundDrawables()) {
                        if (drawable != null) {
                            drawable.setColorFilter(getResources().getColor(emptyFilterColor), PorterDuff.Mode.SRC_IN);
                        }
                    }
                }
            }
        }

    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_recent_stickers_list;
    }
}
