package vc908.stickerfactory.ui.fragment;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import vc908.stickerfactory.Constants;
import vc908.stickerfactory.NetworkManager;
import vc908.stickerfactory.R;
import vc908.stickerfactory.StickersManager;
import vc908.stickerfactory.StorageManager;
import vc908.stickerfactory.analytics.AnalyticsManager;
import vc908.stickerfactory.provider.packs.PacksColumns;
import vc908.stickerfactory.provider.packs.PacksCursor;
import vc908.stickerfactory.ui.OnEmojiBackspaceClickListener;
import vc908.stickerfactory.ui.OnStickerSelectedListener;
import vc908.stickerfactory.ui.activity.MoreActivity;
import vc908.stickerfactory.ui.adapter.IImagesTitleAdapter;
import vc908.stickerfactory.ui.view.SlidingTabLayout;
import vc908.stickerfactory.utils.Logger;
import vc908.stickerfactory.utils.NamesHelper;
import vc908.stickerfactory.utils.Utils;


/**
 * Fragment with stickers lists
 *
 * @author Dmitry Nezhydenko
 */

public class StickersFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String EMOJI_TAB_KEY = "emoji_tab";
    private static final String RECENT_TAB_KEY = "recent_tab";
    private static final int PACKS_LOADER_ID = 41;
    private static final String TAG = StickersFragment.class.getSimpleName();
    /**
     * Items constants for first tab showing selection
     */
    private static final int ITEM_EMOJI = 0;
    private static final int ITEM_STICKER = 1;

    private SlidingTabLayout mSlidingTabLayout;
    private int tabItemsCount;
    private int tabWidth;
    private int minTabSize;
    private int tabPadding;
    private OnStickerSelectedListener stickerSelectedListener;
    private OnEmojiBackspaceClickListener emojiBackspaceClickListener;
    private List<String> stickerTabs = new ArrayList<>();
    private Map<String, Integer> packsIds = new HashMap<>();
    private List<View> controlTabs;
    private PagerAdapterWithImages mPagerAdapter;
    private View contentView;

    @StringRes private int emptyRecentTextRes;
    @DrawableRes private int stickerPlaceholderRes;
    @DrawableRes private int stickersListBgDrawableRes;
    @DrawableRes private int tabPlaceholderDrawableRes;
    @DrawableRes private int emptyRecentImageRes;
    @ColorRes private int listBgColorRes;
    @ColorRes private int tabBgColorRes;
    @ColorRes private int tabUnderlineColorRes;
    @ColorRes private int tabIconsFilterColorRes;
    @ColorRes private int backspaceFilterColorRes;
    @ColorRes private int tabPlaceholderColorFilterRes;
    @ColorRes private int stickerPlaceholderColorFilterRes;
    @ColorRes private int emptyRecentColorFilterRes;
    @ColorRes private int emptyRecentTextColorRes;
    private int maxStickerWidth;

    private String[] firstTabs = new String[]{EMOJI_TAB_KEY, RECENT_TAB_KEY};
    private ViewPager mViewPager;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(PACKS_LOADER_ID, null, this);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (contentView == null) {
            contentView = inflater.inflate(R.layout.stickers_layout, container, false);
        } else if (contentView.getParent() != null) {
            ((ViewGroup) contentView.getParent()).removeView(contentView);
        }
        if (tabIconsFilterColorRes == 0) {
            tabIconsFilterColorRes = R.color.stickers_tab_icons_filter;
        }
        int tabUnderlineColor;
        if (tabUnderlineColorRes == 0) {
            tabUnderlineColor = getResources().getColor(R.color.stickers_tab_strip);
        } else {
            tabUnderlineColor = getResources().getColor(tabUnderlineColorRes);
        }
        int tabsBgColor;
        if (tabBgColorRes == 0) {
            tabsBgColor = getResources().getColor(R.color.stickers_tab_bg);
        } else {
            tabsBgColor = getResources().getColor(tabBgColorRes);
        }
        mSlidingTabLayout = (SlidingTabLayout) contentView.findViewById(R.id.sliding_tabs);
        mSlidingTabLayout.setBackgroundColor(tabsBgColor);
        mViewPager = (ViewPager) contentView.findViewById(R.id.view_pager);
        if (stickersListBgDrawableRes != 0) {
            BitmapDrawable bitmap = new BitmapDrawable(getResources(), BitmapFactory.decodeResource(getResources(), stickersListBgDrawableRes));
            bitmap.setTileModeX(Shader.TileMode.REPEAT);
            bitmap.setTileModeY(Shader.TileMode.REPEAT);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                mViewPager.setBackground(bitmap);
            } else {
                mViewPager.setBackgroundDrawable(bitmap);
            }
        } else {
            if (listBgColorRes == 0) {
                listBgColorRes = R.color.stickers_list_bg;
            }
            mViewPager.setBackgroundColor(getResources().getColor(listBgColorRes));
        }
        calculateTabSize();
        createControlTabs();
        mPagerAdapter = new PagerAdapterWithImages(getChildFragmentManager());
        Logger.d(TAG, "Adapter log: Tabs adapter: " + " : Looper : " + Looper.myLooper() + mPagerAdapter);
        mViewPager.setAdapter(mPagerAdapter);
        mSlidingTabLayout.setCustomTabView(R.layout.tab);
        mSlidingTabLayout.setViewPager(mViewPager, tabWidth);
        mSlidingTabLayout.setSelectedIndicatorColors(tabUnderlineColor);
        populateTabs();
        return contentView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        return new CursorLoader(
                getActivity(),
                PacksColumns.CONTENT_URI,
                new String[]{PacksColumns.NAME, PacksColumns._ID},
                null,
                null,
                PacksColumns.PACK_ORDER
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        PacksCursor packsCursor = new PacksCursor(cursor);
        int freeTabsCount = tabItemsCount - firstTabs.length - controlTabs.size();
        stickerTabs.clear();
        packsIds.clear();
        stickerTabs.addAll(Arrays.asList(firstTabs));
        int stickersTabCount = 0;
        if (packsCursor.moveToFirst()) {
            do {
                stickerTabs.add(packsCursor.getName());
                stickersTabCount++;
                // increment pack id to already added elements count
                // to avoid conflicts with packs _ID = 0, _ID = 1, etc
                packsIds.put(packsCursor.getName(), (int) (packsCursor.getId() + firstTabs.length));
            } while (packsCursor.moveToNext() && stickersTabCount < freeTabsCount);
        }
        populateTabs();
        mPagerAdapter.notifyDataSetChanged();
        mViewPager.setCurrentItem(StorageManager.getInstance().getLastUsingItem(ITEM_STICKER)); // select tab acording last using item
    }

    /**
     * Populate tab strip with stickers tabs, control tabs, etc
     */
    private void populateTabs() {
        if (tabPlaceholderDrawableRes > 0) {
            mSlidingTabLayout.setTabPlaceholderDrawableRes(tabPlaceholderDrawableRes);
        }
        mSlidingTabLayout.setTabPlaceholderColorFilter(tabPlaceholderColorFilterRes);
        mSlidingTabLayout.populateTabStrip();
        mSlidingTabLayout.addCustomTab(controlTabs.get(0), tabWidth, minTabSize);
        addTabsEmptySpace();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // nothing to do
    }


    /**
     * Create list with control tabs to add them into tabs layout later
     */
    private void createControlTabs() {
        controlTabs = new ArrayList<>();
        Bundle data = new Bundle();
        if (stickerPlaceholderRes > 0) {
            data.putInt(Constants.ARGUMENT_PLACEHOLDER_DRAWABLE, stickerPlaceholderRes);
        }
        if (stickerPlaceholderColorFilterRes > 0) {
            data.putInt(Constants.ARGUMENT_PLACEHOLDER_FILTER_COLOR, stickerPlaceholderColorFilterRes);
        }
        data.putInt(Constants.ARGUMENT_PRIMARY_COLOR, tabBgColorRes);
        controlTabs.add(createTab(R.drawable.ic_more, MoreActivity.class, data));
//        controlTabs.add(createTab(R.drawable.ic_shop, MarketActivity.class));
    }

    public View createTab(@DrawableRes int icon, final Class activityClass, final Bundle data) {
        ImageView tab = new ImageView(getActivity());
        tab.setImageResource(icon);
        tab.setPadding(tabPadding, tabPadding, tabPadding, tabPadding);
        tab.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
			    Intent intent = new Intent(getActivity(), activityClass);
			    if (data != null) {
			        intent.putExtras(data);
			    }
			    getActivity().startActivity(intent);
			}
		});
        tab.setColorFilter(getResources().getColor(tabIconsFilterColorRes));
        return tab;
    }

    /**
     * Сalculating the appropriate width of tab to fill all space
     */
    private void calculateTabSize() {
        tabPadding = getResources().getDimensionPixelSize(R.dimen.sticker_tab_padding);
        minTabSize = getResources().getDimensionPixelSize(R.dimen.sticker_tab_size);
        tabItemsCount = (int) (Math.floor(Utils.getScreenWidthInPx() / minTabSize));
        tabWidth = minTabSize;//(int) Math.floor(((float) Utils.getScreenWidthInPx()) / tabItemsCount);
    }

    /**
     * Fill free space between stickers and control tabs
     */
    private void addTabsEmptySpace() {
        int emptyTabsCount = tabItemsCount - stickerTabs.size() - controlTabs.size();
        if (emptyTabsCount > 0) {
            // TODO calculate and add unused space
            mSlidingTabLayout.addCustomTab(new View(getActivity()), tabWidth * emptyTabsCount, minTabSize);
        }
    }


    /**
     * Set listener for sticker selection
     *
     * @param listener Sticker select listener
     */
    public void setOnStickerSelectedListener(OnStickerSelectedListener listener) {
        this.stickerSelectedListener = listener;
    }

    /**
     * Set listener for emoji tab backspace icon
     *
     * @param listener Backspace click listener
     */
    public void setOnEmojiBackspaceClickListener(OnEmojiBackspaceClickListener listener) {
        this.emojiBackspaceClickListener = listener;
    }

    /**
     * Listener for emoji selection and store using for analytics
     */
    private OnStickerSelectedListener analyticsEmojiSelectedListener = new OnStickerSelectedListener() {
		@Override
		public void onStickerSelected(String code) {
			AnalyticsManager.getInstance().onEmojiSelected(code);
		}
	};

    /**
     * Listener for sticker selection and store using for analytics
     */
    private OnStickerSelectedListener analyticsStickerSelectedListener = new OnStickerSelectedListener() {
		@Override
		public void onStickerSelected(String code) {
			AnalyticsManager.getInstance().onStickerSelected(NamesHelper.getPackName(code), NamesHelper.getStickerName(code));
		}
	};
    /**
     * Listener for stickers selection and updating last using time
     */
    private OnStickerSelectedListener recentStickersTrackingListener = new OnStickerSelectedListener() {
		@Override
		public void onStickerSelected(String code) {
			StorageManager.getInstance().updateStickerUsingTime(code);
		}
	};


    /**
     * Listener to store last using item - sticker or emoji.
     */
    private OnStickerSelectedListener lastUsingListener = new OnStickerSelectedListener() {
		@Override
		public void onStickerSelected(String code) {
			StorageManager.getInstance().storeLastUsingItem(StickersManager.isSticker(code) ? ITEM_STICKER : ITEM_EMOJI);
		}
	};


    /**
     * Stickers pager adapter.
     * Handle stickers fragment and tab images
     *
     * @author Dmytro Nezhydenko
     */
    private class PagerAdapterWithImages extends FragmentPagerAdapter implements IImagesTitleAdapter {

        public PagerAdapterWithImages(FragmentManager childFragmentManager) {
            super(childFragmentManager);
        }

        @Override
        public int getCount() {
            return stickerTabs.size();
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    EmojiFragmnet emojiFragment = new EmojiFragmnet();
                    if (stickerSelectedListener != null) {
                        emojiFragment.addStickerSelectedListener(stickerSelectedListener);
                    }
                    if (backspaceFilterColorRes != 0) {
                        Bundle data = new Bundle();
                        data.putInt(EmojiFragmnet.ARGUMENT_BACKSPACE_COLOR_FILTER, backspaceFilterColorRes);
                        emojiFragment.setArguments(data);
                    }
                    emojiFragment.addStickerSelectedListener(analyticsEmojiSelectedListener);
                    emojiFragment.addStickerSelectedListener(lastUsingListener);
                    emojiFragment.setOnBackspaceClickListener(emojiBackspaceClickListener);
                    return emojiFragment;
                case 1:
                    RecentStickersFragment recentStickersfragment = new RecentStickersFragment();
                    if (stickerSelectedListener != null) {
                        recentStickersfragment.addStickerSelectedListener(stickerSelectedListener);
                    }
                    Bundle recentData = new Bundle();
                    recentData.putInt(RecentStickersFragment.ARGUMENT_EMPTY_TEXT, emptyRecentTextRes);
                    recentData.putInt(RecentStickersFragment.ARGUMENT_EMPTY_TEXT_COLOR, emptyRecentTextColorRes);
                    recentData.putInt(RecentStickersFragment.ARGUMENT_EMPTY_IMAGE, emptyRecentImageRes);
                    recentData.putInt(RecentStickersFragment.ARGUMENT_EMPTY_FILTER_COLOR, emptyRecentColorFilterRes);
                    addStickersListConfig(recentData);
                    recentStickersfragment.setArguments(recentData);
                    recentStickersfragment.addStickerSelectedListener(analyticsStickerSelectedListener);
                    recentStickersfragment.addStickerSelectedListener(lastUsingListener);
                    return recentStickersfragment;
                default:
                    Bundle data = new Bundle();
                    data.putString(Constants.ARGUMENT_PACK, stickerTabs.get(position));
                    addStickersListConfig(data);
                    StickersListFragment stickersListFragment = new StickersListFragment();
                    stickersListFragment.setArguments(data);
                    if (stickerSelectedListener != null) {
                        stickersListFragment.addStickerSelectedListener(stickerSelectedListener);
                    }
                    stickersListFragment.addStickerSelectedListener(recentStickersTrackingListener);
                    stickersListFragment.addStickerSelectedListener(analyticsStickerSelectedListener);
                    stickersListFragment.addStickerSelectedListener(lastUsingListener);
                    return stickersListFragment;
            }

        }

        private void addStickersListConfig(Bundle data) {
            if (stickerPlaceholderRes > 0) {
                data.putInt(Constants.ARGUMENT_PLACEHOLDER_DRAWABLE, stickerPlaceholderRes);
            }
            if (stickerPlaceholderColorFilterRes > 0) {
                data.putInt(Constants.ARGUMENT_PLACEHOLDER_FILTER_COLOR, stickerPlaceholderColorFilterRes);
            }
            if (maxStickerWidth > 0) {
                data.putInt(Constants.ARGUMENT_MAX_STICKER_WIDTH, maxStickerWidth);
            }
        }

        @Override
        public long getItemId(int position) {
            String packName = stickerTabs.get(position);
            if (EMOJI_TAB_KEY.equals(packName) || RECENT_TAB_KEY.equals(packName)) {
                return position;
            } else {
                return packsIds.get(packName);
            }
        }

        @Override
        public Uri getTabImage(int position) {
            switch (position) {
                case 0:
                    return Utils.getDrawableUri(R.drawable.ic_emoji, getActivity());
                case 1:
                    return Utils.getDrawableUri(R.drawable.ic_recent, getActivity());
                default:
                    return NetworkManager.getInstance().geIconUri(stickerTabs.get(position), NetworkManager.PACK_TAB_ICON);
            }
        }

        @Override
        public int getTabImageColorFilter(int position) {
            switch (position) {
                case 0:
                case 1:
                    return tabIconsFilterColorRes;
                default:
                    return 0;
            }
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (contentView != null && contentView.getParent() != null) {
            ((ViewGroup) contentView.getParent()).removeView(contentView);
        }
    }

    /**
     * Set tab icons color filter
     *
     * @param tabIconsFilterColorRes Color ID
     */
    private void setTabIconsFilterColorRes(@ColorRes int tabIconsFilterColorRes) {
        this.tabIconsFilterColorRes = tabIconsFilterColorRes;
    }

    /**
     * Set tab underline color
     *
     * @param tabUnderlineColorId Color ID
     */
    private void setTabUnderlineColor(@ColorRes int tabUnderlineColorId) {
        this.tabUnderlineColorRes = tabUnderlineColorId;
    }

    /**
     * Set tab background color
     *
     * @param tabBgColorRes Color ID
     */
    private void setTabBgColorRes(@ColorRes int tabBgColorRes) {
        this.tabBgColorRes = tabBgColorRes;
    }

    /**
     * Set tab placeholder drawable
     *
     * @param tabPlaceholderDrawableRes Drawable ID
     */
    private void setTabPlaceholderDrawableRes(@DrawableRes int tabPlaceholderDrawableRes) {
        this.tabPlaceholderDrawableRes = tabPlaceholderDrawableRes;
    }

    /**
     * Set placeholder for stickers
     *
     * @param stickerPlaceholderRes Drawable ID
     */
    private void setStickerPlaceholderRes(@DrawableRes int stickerPlaceholderRes) {
        this.stickerPlaceholderRes = stickerPlaceholderRes;
    }

    /**
     * Set stickers list background color
     *
     * @param listBgColorRes Color ID
     */
    private void setStickersListBackgroundColorRes(@ColorRes int listBgColorRes) {
        this.listBgColorRes = listBgColorRes;
    }

    ;

    /**
     * Set stickers list background drawable pattern
     *
     * @param stickersListBgDrawableRes Drawable ID
     */
    private void setStickersLisBackgroundDrawableRes(@DrawableRes int stickersListBgDrawableRes) {
        this.stickersListBgDrawableRes = stickersListBgDrawableRes;
    }

    /**
     * Set max sticker width at stickers list fragment
     *
     * @param maxStickerWidth Width in px
     */
    private void setMaxStickerWidth(int maxStickerWidth) {
        this.maxStickerWidth = maxStickerWidth;
    }

    /**
     * Set backspace icon filter color
     *
     * @param backspaceFilterColorRes Color ID
     */
    private void setBackspaceFilterColorRes(int backspaceFilterColorRes) {
        this.backspaceFilterColorRes = backspaceFilterColorRes;
    }

    /**
     * Set text for empty view at recent tab
     *
     * @param emptyRecentTextRes String resource
     */
    private void setEmptyRecentTextRes(@StringRes int emptyRecentTextRes) {
        this.emptyRecentTextRes = emptyRecentTextRes;
    }

    /**
     * Set text color for empty view at recent tB
     *
     * @param emptyRecentTextColor Color resource
     */
    private void setEmptyRecentTextColorRes(@ColorRes int emptyRecentTextColor) {
        this.emptyRecentTextColorRes = emptyRecentTextColor;
    }

    /**
     * Set drawable resource for empty view at recent tab
     *
     * @param emptyRecentImageRes Drawable resource
     */
    private void setEmptyRecentImagetRes(@DrawableRes int emptyRecentImageRes) {
        this.emptyRecentImageRes = emptyRecentImageRes;
    }

    /**
     * Set color filter for empty image at recent tab
     *
     * @param emptyRecentColorFilter Color resource
     */
    private void setEmptyRecentColorFilter(@ColorRes int emptyRecentColorFilter) {
        this.emptyRecentColorFilterRes = emptyRecentColorFilter;
    }

    /**
     * Set sticker placeholder color filter at stickers list
     *
     * @param stickerPlaceholderColorFilterRes Color resource
     */
    private void setStickerPlaceholderColorFilterRes(@ColorRes int stickerPlaceholderColorFilterRes) {
        this.stickerPlaceholderColorFilterRes = stickerPlaceholderColorFilterRes;
    }

    /**
     * Set tab placeholder color filter
     * @param tabPlaceholderColorFilterRes Color ID
     */
    private void setTabPlaceholderColorFilterRes(@ColorRes int tabPlaceholderColorFilterRes) {
        this.tabPlaceholderColorFilterRes = tabPlaceholderColorFilterRes;
    }


    /**
     * Builder for stickers fragment
     */
    public static class Builder {
        private OnStickerSelectedListener onStickerSelectedListener;
        private int stickerPlaceholderDrawableRes;
        private int stickersListBgRes;
        private int stickersListBackgroundColorRes;
        private int tabPlaceholderDrawableRes;
        private int tabBackgroundColorRes;
        private int tabUnderlineColorRes;
        private int tabIconsFilterColorRes;
        private int maxStickerWidth;
        private int backspaceFilterColorRes;
        private int stickerPlaceholderColorFilterRes;

        private int emptyRecentTextRes;
        private int emptyRecentTextColorRes;
        private int emptyRecentImageRes;
        private int emptyRecentColorFilterRes;
        private int tabPlaceholderColorFilterRes;

        public Builder() {
            if (!StickersManager.isInitialized()) {
                Logger.e(TAG, "Stickers manager not ini");
            }
        }

        public Builder setOnStickerSelectedListener(OnStickerSelectedListener listener) {
            this.onStickerSelectedListener = listener;
            return this;
        }

        public Builder setStickerPlaceholderDrawableRes(@DrawableRes int stickerPlaceholderDrawableRes) {
            this.stickerPlaceholderDrawableRes = stickerPlaceholderDrawableRes;
            return this;
        }

        public Builder setStickersListBackgroundDrawableRes(@DrawableRes int stickersListBgRes) {
            this.stickersListBgRes = stickersListBgRes;
            return this;
        }

        public Builder setStickersListBackgroundColorRes(@ColorRes int stickersListBackgroundColorRes) {
            this.stickersListBackgroundColorRes = stickersListBackgroundColorRes;
            return this;
        }

        public Builder setTabPlaceholderDrawableRes(@DrawableRes int tabPlaceholderDrawableRes) {
            this.tabPlaceholderDrawableRes = tabPlaceholderDrawableRes;
            return this;
        }

        public Builder setTabBackgroundColorRes(@ColorRes int tabBackgroundColorRes) {
            this.tabBackgroundColorRes = tabBackgroundColorRes;
            return this;
        }

        public Builder setTabUnderlineColorRes(@ColorRes int tabUnderlineColorRes) {
            this.tabUnderlineColorRes = tabUnderlineColorRes;
            return this;
        }

        public Builder setTabIconsFilterColorRes(@ColorRes int tabIconsFilterColorRes) {
            this.tabIconsFilterColorRes = tabIconsFilterColorRes;
            return this;
        }

        public Builder setMaxStickerWidth(int maxStickerWidth) {
            this.maxStickerWidth = maxStickerWidth;
            return this;
        }

        public Builder setBackspaceFilterColorRes(@ColorRes int backspaceFilterColorRes) {
            this.backspaceFilterColorRes = backspaceFilterColorRes;
            return this;
        }

        public Builder setEmptyRecentTextRes(@StringRes int emptyRecentTextRes) {
            this.emptyRecentTextRes = emptyRecentTextRes;
            return this;
        }

        public Builder setEmptyRecentTextColorRes(@ColorRes int emptyRecentTextColorRes) {
            this.emptyRecentTextColorRes = emptyRecentTextColorRes;
            return this;
        }

        public Builder setEmptyRecentImageRes(@DrawableRes int emptyRecentImageRes) {
            this.emptyRecentImageRes = emptyRecentImageRes;
            return this;
        }

        public Builder setEmptyRecentColorFilterRes(@ColorRes int emptyRecentColorFilterRes) {
            this.emptyRecentColorFilterRes = emptyRecentColorFilterRes;
            return this;
        }

        public Builder setStickerPlaceholderColorFilterRes(@ColorRes int stickerPlaceholderColorFilterRes) {
            this.stickerPlaceholderColorFilterRes = stickerPlaceholderColorFilterRes;
            return this;
        }

        public Builder setTabPlaceholderFilterColorRes(@ColorRes int tabPlaceholderColorFilterRes) {
            this.tabPlaceholderColorFilterRes = tabPlaceholderColorFilterRes;
            return this;
        }

        public StickersFragment build() {
            StickersFragment fragment = new StickersFragment();
            fragment.setOnStickerSelectedListener(onStickerSelectedListener);
            fragment.setStickerPlaceholderRes(stickerPlaceholderDrawableRes);
            fragment.setStickerPlaceholderColorFilterRes(stickerPlaceholderColorFilterRes);
            fragment.setStickersLisBackgroundDrawableRes(stickersListBgRes);
            fragment.setStickersListBackgroundColorRes(stickersListBackgroundColorRes);
            fragment.setTabBgColorRes(tabBackgroundColorRes);
            fragment.setTabIconsFilterColorRes(tabIconsFilterColorRes);
            fragment.setTabPlaceholderDrawableRes(tabPlaceholderDrawableRes);
            fragment.setTabUnderlineColor(tabUnderlineColorRes);
            fragment.setMaxStickerWidth(maxStickerWidth);
            fragment.setBackspaceFilterColorRes(backspaceFilterColorRes);
            fragment.setEmptyRecentTextRes(emptyRecentTextRes);
            fragment.setEmptyRecentTextColorRes(emptyRecentTextColorRes);
            fragment.setEmptyRecentImagetRes(emptyRecentImageRes);
            fragment.setEmptyRecentColorFilter(emptyRecentColorFilterRes);
            fragment.setTabPlaceholderColorFilterRes(tabPlaceholderColorFilterRes);
            return fragment;
        }
    }
}