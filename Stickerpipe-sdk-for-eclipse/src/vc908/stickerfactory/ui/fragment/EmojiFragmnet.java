package vc908.stickerfactory.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextPaint;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import vc908.stickerfactory.R;
import vc908.stickerfactory.emoji.Emoji;
import vc908.stickerfactory.emoji.People;
import vc908.stickerfactory.ui.OnEmojiBackspaceClickListener;
import vc908.stickerfactory.ui.OnStickerSelectedListener;
import vc908.stickerfactory.ui.view.SquareTextView;
import vc908.stickerfactory.utils.Utils;

/**
 * Fragment with emoji list
 *
 * @author Dmitry Nezhydenko
 */
public class EmojiFragmnet extends Fragment {

    public static final String ARGUMENT_BACKSPACE_COLOR_FILTER = "argument_backspace_color_filter";
    private List<OnStickerSelectedListener> stickerSelectedListeners = new ArrayList<>();
    private View layout;
    private int size;
    private int itemWidth;
    private int backspaceColorFilter = R.color.stickers_tab_bg;
    private OnEmojiBackspaceClickListener emojiBackspaceClickListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            int tempColorFilter = getArguments().getInt(ARGUMENT_BACKSPACE_COLOR_FILTER);
            if (tempColorFilter > 0) {
                backspaceColorFilter = tempColorFilter;
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (layout == null) {
            layout = inflater.inflate(R.layout.fragment_emoji_list, container, false);
            RecyclerView rv = (RecyclerView) layout.findViewById(R.id.recycler_view);
            size = 28;//(int) Utils.dp(getEmojiSize(People.DATA[0], 10));
            // calculate emoji columns count
            int minItemSize = Utils.dp(48);
            int backspaceColumnSize = getResources().getDimensionPixelSize(R.dimen.backspace_column_width);
            int itemsSpanCount = (int) (Math.floor((Utils.getScreenWidthInPx() - backspaceColumnSize) / minItemSize));
            itemWidth = (int) Math.floor(((float) Utils.getScreenWidthInPx()) / itemsSpanCount);
            int columnsCount = (Utils.getScreenWidthInPx() / itemWidth);
            GridLayoutManager lm = new GridLayoutManager(getActivity(), columnsCount);
            rv.setLayoutManager(lm);
            rv.setAdapter(new EmojiAdapter());
            ImageView backspaceView = (ImageView) layout.findViewById(R.id.clear_button);
            backspaceView.setColorFilter(getResources().getColor(backspaceColorFilter));
            backspaceView.getLayoutParams().height = (Utils.getScreenWidthInPx() - backspaceColumnSize) / itemsSpanCount;
            backspaceView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
				    if (emojiBackspaceClickListener != null) {
				        emojiBackspaceClickListener.onEmojiBackspaceClicked();
				    }
				}
			});
            backspaceView.setBackgroundDrawable(Utils.createSelectableBackground(getActivity()));
        }
        return layout;
    }

    /**
     * Add emoji selected listener
     *
     * @param stickerSelectedListener Listener
     */
    public void addStickerSelectedListener(OnStickerSelectedListener stickerSelectedListener) {
        stickerSelectedListeners.add(stickerSelectedListener);
    }

    /**
     * Calculate max text size according to cell width
     *
     * @param emoji    Emoji character
     * @param textSize Calculating text size
     * @return Max text size to fill cell
     */
    private int getEmojiSize(Emoji emoji, int textSize) {
        TextPaint textPaint = new TextPaint();
        textPaint.setTextSize(textSize);
        float textWidth = textPaint.measureText(emoji.getEmoji(), 0, emoji.getEmoji().length());
        if (Utils.dp((int) textWidth) > itemWidth) {
            return (int) (textSize - (Math.ceil(getResources().getDisplayMetrics().density - 2.5)) * 2);
        } else {
            return getEmojiSize(emoji, textSize + 1);
        }
    }

    /**
     * Set click listener for backspace icon
     *
     * @param listener Backspace click listener
     */
    public void setOnBackspaceClickListener(OnEmojiBackspaceClickListener listener) {
        this.emojiBackspaceClickListener = listener;
    }

    /**
     * Adapter for emoji list
     */
    private class EmojiAdapter extends RecyclerView.Adapter<EmojiAdapter.ViewHolder> {

        private final Emoji[] data;

        public EmojiAdapter() {
            data = People.DATA;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            TextView tv = new SquareTextView(getActivity());
            tv.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            tv.setTextSize(size);
            tv.setGravity(Gravity.CENTER);
            tv.setTextColor(getResources().getColor(R.color.stickers_emoji));
            tv.setBackgroundDrawable(Utils.createSelectableBackground(tv.getContext()));
            return new ViewHolder(tv);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            String code = data[position].getEmoji();
            holder.tv.setText(code);
            holder.code = code;
        }

        @Override
        public int getItemCount() {
            return data.length;
        }

        /**
         * Emoji item view holder
         */
        public class ViewHolder extends RecyclerView.ViewHolder {

            private TextView tv;
            private String code;

            public ViewHolder(View itemView) {
                super(itemView);
                tv = (TextView) itemView;
                tv.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
					    if (stickerSelectedListeners.size() > 0) {
					        for (OnStickerSelectedListener listener : stickerSelectedListeners) {
					            listener.onStickerSelected(code);
					        }
					    }
					}
				});
            }
        }
    }
}
