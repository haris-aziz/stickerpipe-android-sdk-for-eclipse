package vc908.stickerfactory.ui.activity;

import android.database.Cursor;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListener;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.LinkedList;
import java.util.List;

import vc908.stickerfactory.Constants;
import vc908.stickerfactory.NetworkManager;
import vc908.stickerfactory.R;
import vc908.stickerfactory.StorageManager;
import vc908.stickerfactory.analytics.AnalyticsManager;
import vc908.stickerfactory.provider.packs.PacksColumns;
import vc908.stickerfactory.provider.packs.PacksCursor;
import vc908.stickerfactory.ui.advancedrecyclerview.animator.GeneralItemAnimator;
import vc908.stickerfactory.ui.advancedrecyclerview.animator.SwipeDismissItemAnimator;
import vc908.stickerfactory.ui.advancedrecyclerview.decoration.SimpleListDividerDecorator;
import vc908.stickerfactory.ui.advancedrecyclerview.draggable.DraggableItemAdapter;
import vc908.stickerfactory.ui.advancedrecyclerview.draggable.ItemDraggableRange;
import vc908.stickerfactory.ui.advancedrecyclerview.draggable.RecyclerViewDragDropManager;
import vc908.stickerfactory.ui.advancedrecyclerview.swipeable.RecyclerViewSwipeManager;
import vc908.stickerfactory.ui.advancedrecyclerview.swipeable.SwipeableItemAdapter;
import vc908.stickerfactory.ui.advancedrecyclerview.touchguard.RecyclerViewTouchActionGuardManager;
import vc908.stickerfactory.ui.advancedrecyclerview.utils.AbstractDraggableSwipeableItemViewHolder;
import vc908.stickerfactory.ui.advancedrecyclerview.utils.WrapperAdapterUtils;
import vc908.stickerfactory.utils.Logger;

/**
 * @author Dmitry Nezhydenko (dehimb@gmail.com)
 */
public class MoreActivity extends AppCompatActivity {

    private static final String TAG = MoreActivity.class.getSimpleName();

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter mWrappedAdapter;
    private RecyclerViewDragDropManager mRecyclerViewDragDropManager;
    private MyDraggableSwipeableItemAdapter myItemAdapter;
    private List<PackInfoHolder> data = new LinkedList<>();
    private Handler mHandler = new Handler();
    private RecyclerViewSwipeManager mRecyclerViewSwipeManager;
    private RecyclerViewTouchActionGuardManager mRecyclerViewTouchActionGuardManager;
    private int placeholderDrawableRes = R.drawable.sticker_placeholder;
    private PorterDuffColorFilter placeholderColorFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            toolbar.setNavigationOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					onBackPressed();
				}
			});
            getSupportActionBar().setTitle(R.string.collections);
        }

        if (getIntent().getExtras() != null) {
            placeholderDrawableRes = getIntent().getIntExtra(Constants.ARGUMENT_PLACEHOLDER_DRAWABLE, R.drawable.sticker_placeholder);
            int placeholderFilterColorRes = getIntent().getIntExtra(Constants.ARGUMENT_PLACEHOLDER_FILTER_COLOR, 0);
            if (placeholderFilterColorRes > 0) {
                placeholderColorFilter = new PorterDuffColorFilter(getResources().getColor(placeholderFilterColorRes), PorterDuff.Mode.SRC_IN);
            }
            int tabBgColorRes = getIntent().getIntExtra(Constants.ARGUMENT_PRIMARY_COLOR, 0);
            toolbar.setBackgroundColor(getResources().getColor(tabBgColorRes > 0 ? tabBgColorRes : R.color.stickers_tab_bg));
        }

        Cursor cursor = getContentResolver().query(PacksColumns.CONTENT_URI, new String[]{PacksColumns._ID, PacksColumns.NAME, PacksColumns.TITLE, PacksColumns.ARTIST}, null, null, PacksColumns.PACK_ORDER);
        PacksCursor packsCursor = new PacksCursor(cursor);
        while (packsCursor.moveToNext()) {
            data.add(new PackInfoHolder(false, packsCursor.getId(), packsCursor.getName(), packsCursor.getTitle(), packsCursor.getArtist()));
        }
        cursor.close();

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        mLayoutManager = new LinearLayoutManager(this);

        // touch guard manager  (this class is required to suppress scrolling while swipe-dismiss animation is running)
        mRecyclerViewTouchActionGuardManager = new RecyclerViewTouchActionGuardManager();
        mRecyclerViewTouchActionGuardManager.setInterceptVerticalScrollingWhileAnimationRunning(true);
        mRecyclerViewTouchActionGuardManager.setEnabled(true);

        mRecyclerViewDragDropManager = new RecyclerViewDragDropManager();
        mRecyclerViewDragDropManager.setDraggingItemShadowDrawable(
                (NinePatchDrawable) getResources().getDrawable(R.drawable.material_shadow));

        mRecyclerViewSwipeManager = new RecyclerViewSwipeManager();
        mRecyclerViewSwipeManager.setAdditionItemAnimatorListener(new ViewPropertyAnimatorListener() {
            @Override
            public void onAnimationStart(View view) {

            }

            @Override
            public void onAnimationEnd(View view) {

            }

            @Override
            public void onAnimationCancel(View view) {
                myItemAdapter.onItemAnimationEnd(view);
            }
        });
        myItemAdapter = new MyDraggableSwipeableItemAdapter();
        mWrappedAdapter = mRecyclerViewDragDropManager.createWrappedAdapter(myItemAdapter);
        mWrappedAdapter = mRecyclerViewSwipeManager.createWrappedAdapter(mWrappedAdapter);

        final GeneralItemAnimator animator = new SwipeDismissItemAnimator();
        // Change animations are enabled by default since support-v7-recyclerview v22.
        // Disable the change animation in order to make turning back animation of swiped item works properly.
        animator.setSupportsChangeAnimations(false);

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mWrappedAdapter);
        mRecyclerView.setItemAnimator(animator);

        mRecyclerView.addItemDecoration(new SimpleListDividerDecorator(getResources().getDrawable(R.drawable.list_divider), true));

        mRecyclerViewTouchActionGuardManager.attachRecyclerView(mRecyclerView);
        // TODO enable swipe later
//        mRecyclerViewSwipeManager.attachRecyclerView(mRecyclerView);
        mRecyclerViewDragDropManager.attachRecyclerView(mRecyclerView);

    }

    @Override
    public void onPause() {
        mRecyclerViewDragDropManager.cancelDrag();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (mRecyclerViewDragDropManager != null) {
            mRecyclerViewDragDropManager.release();
            mRecyclerViewDragDropManager = null;
        }
        if (mRecyclerViewSwipeManager != null) {
            mRecyclerViewSwipeManager.release();
            mRecyclerViewSwipeManager = null;
        }

        if (mRecyclerViewTouchActionGuardManager != null) {
            mRecyclerViewTouchActionGuardManager.release();
            mRecyclerViewTouchActionGuardManager = null;
        }
        if (mRecyclerView != null) {
            mRecyclerView.setItemAnimator(null);
            mRecyclerView.setAdapter(null);
            mRecyclerView = null;
        }

        if (mWrappedAdapter != null) {
            WrapperAdapterUtils.releaseAll(mWrappedAdapter);
            mWrappedAdapter = null;
        }
        myItemAdapter = null;
        mLayoutManager = null;

        super.onDestroy();
    }

    public class MyDraggableSwipeableItemAdapter extends RecyclerView.Adapter<MyDraggableSwipeableItemAdapter.MyViewHolder> implements DraggableItemAdapter<MyDraggableSwipeableItemAdapter.MyViewHolder>, SwipeableItemAdapter<MyDraggableSwipeableItemAdapter.MyViewHolder> {
        private static final String TAG = "MyDraggableItemAdapter";

        @Override
        public int onGetSwipeReactionType(MyViewHolder myViewHolder, int position, int x, int y) {
            Logger.d(TAG, "Getting swipe reaction");
            if (!data.get(position).isSwiped) {
                closeSwipedItems();
            }
            return RecyclerViewSwipeManager.REACTION_CAN_SWIPE_LEFT;
        }

        @Override
        public void onSetSwipeBackground(MyViewHolder viewHolder, int position, int type) {
            Logger.d(TAG, "onSetSwipeBackground: type = " + type);
            switch (type) {
                case RecyclerViewSwipeManager.DRAWABLE_SWIPE_LEFT_BACKGROUND:
//                    viewHolder.mRemoveView.setVisibility(View.VISIBLE);
                    break;
                default:
            }
        }

        @Override
        public int onSwipeItem(MyViewHolder myViewHolder, int i, int result) {
            Logger.d(TAG, "onSwipeItem: result = " + result);
            switch (result) {
                case RecyclerViewSwipeManager.RESULT_SWIPED_RIGHT:
                    return RecyclerViewSwipeManager.AFTER_SWIPE_REACTION_DEFAULT;
                case RecyclerViewSwipeManager.RESULT_SWIPED_LEFT:
                    return RecyclerViewSwipeManager.AFTER_SWIPE_REACTION_MOVE_TO_SWIPED_DIRECTION;
                case RecyclerViewSwipeManager.RESULT_CANCELED:
                    return RecyclerViewSwipeManager.AFTER_SWIPE_REACTION_DEFAULT;
                default:
                    return RecyclerViewSwipeManager.AFTER_SWIPE_REACTION_MOVE_TO_SWIPED_DIRECTION;
            }
        }

        @Override
        public void onPerformAfterSwipeReaction(MyViewHolder holder, int position, int result, int reaction) {
            Logger.d(TAG, "onPerformAfterSwipeReaction(result = " + result + ", reaction = " + reaction + ")");
            switch (reaction) {
                case RecyclerViewSwipeManager.AFTER_SWIPE_REACTION_MOVE_TO_SWIPED_DIRECTION:
                    data.get(position).isSwiped = true;
                    notifyItemChanged(position);
                    break;
                default:
                    data.get(position).isSwiped = false;
            }
        }

        public void onItemAnimationEnd(View view) {
            if (view != null && view.getTag() != null && view.getTag() instanceof Integer) {
                int position = (int) view.getTag();
                if (!data.get(position).isSwiped) {
                    notifyItemChanged(position);
                }
            }
        }


        public class MyViewHolder extends AbstractDraggableSwipeableItemViewHolder {
            public View frontViewContainer;
            public ImageView mRemoveView;
            public ViewGroup mContainer;
            public ImageView mDragHandle;
            public ImageView packImageView;
            public TextView titleView;
            public TextView artistView;
            public String packName;
            public Drawable placeholderDrawable;

            public MyViewHolder(View v) {
                super(v);
                mContainer = (ViewGroup) v.findViewById(R.id.container);
                frontViewContainer = v.findViewById(R.id.front_view_container);
                // prevent touch event at underlying view
                frontViewContainer.setOnTouchListener(new OnTouchListener() {
					@Override
					public boolean onTouch(View v1, MotionEvent event) {
						return false;
					}
				});
                mDragHandle = (ImageView) v.findViewById(R.id.drag_handle);
                mDragHandle.setColorFilter(getResources().getColor(R.color.reorder_icon));
                packImageView = (ImageView) v.findViewById(R.id.pack_image);
                mRemoveView = (ImageView) v.findViewById(R.id.delete);
                mRemoveView.setColorFilter(getResources().getColor(R.color.remove_icon));
                titleView = (TextView) v.findViewById(R.id.pack_title);
                artistView = (TextView) v.findViewById(R.id.pack_artist);

                placeholderDrawable = getResources().getDrawable(placeholderDrawableRes);
            }

            @Override
            public View getSwipeableContainerView() {
                return frontViewContainer;
            }
        }

        public MyDraggableSwipeableItemAdapter() {
            setHasStableIds(true);
        }

        @Override
        public long getItemId(int position) {
            return data.get(position).id;
        }

        @Override
        public void onBindViewHolder(final MyViewHolder holder, final int position) {
            Logger.d(TAG, "Binding view: position = " + position);
            holder.packName = data.get(position).name;
            Uri iconUri = NetworkManager.getInstance().geIconUri(holder.packName, NetworkManager.PACK_MAIN_ICON);
            if (placeholderColorFilter != null && holder.placeholderDrawable != null) {
                holder.placeholderDrawable.setColorFilter(placeholderColorFilter);
            }
            Glide.with(MoreActivity.this)
                    .load(iconUri)
                    .placeholder(holder.placeholderDrawable)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.packImageView);

            holder.titleView.setText(data.get(position).title);
            holder.artistView.setText(data.get(position).artist);

            holder.setMaxLeftSwipeAmount(-0.2f);
            holder.setMaxRightSwipeAmount(0);
            holder.setSwipeItemSlideAmount(data.get(position).isSwiped ? -0.2f : 0);
//            holder.mRemoveView.setVisibility(data.get(position).isSwiped ? View.VISIBLE : View.INVISIBLE);

            holder.frontViewContainer.setOnLongClickListener(new OnLongClickListener() {
				@Override
				public boolean onLongClick(View v) {
				    if (!data.get(position).isSwiped) {
				        closeSwipedItems();
				        // TODO enable swipe later
//                    data.get(position).isSwiped = true;
				        notifyItemChanged(position);
				    } else {
				        closeSwipedItems();
				    }
				    return true;
				}
			});
            holder.frontViewContainer.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					closeSwipedItems();
				}
			});
            holder.frontViewContainer.setTag(position);
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_dragable_pack, parent, false));
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        @Override
        public void onMoveItem(int fromPosition, int toPosition) {
            Logger.d(TAG, "onMoveItem(fromPosition = " + fromPosition + ", toPosition = " + toPosition + ")");

            if (fromPosition == toPosition) {
                return;
            }
            PackInfoHolder movedItem = data.remove(fromPosition);
            data.add(toPosition, movedItem);
            notifyItemMoved(fromPosition, toPosition);
            StorageManager.getInstance().updatePacksOrder(data);
        }

        @Override
        public boolean onCheckCanStartDrag(MyViewHolder holder, int position, int x, int y) {
            float deltaX = ViewCompat.getTranslationX(holder.frontViewContainer);
            return (x >= holder.mDragHandle.getLeft() + deltaX)
                    && (x <= holder.mDragHandle.getRight() + deltaX)
                    && (y >= holder.mDragHandle.getTop())
                    && (y <= holder.mDragHandle.getBottom());
        }

        @Override
        public ItemDraggableRange onGetItemDraggableRange(MyViewHolder holder, int position) {
            // no drag-sortable range specified
            return null;
        }
    }

    private void closeSwipedItems() {
        for (int i = 0; i < data.size(); i++) {
            if (data.get(i).isSwiped) {
                data.get(i).isSwiped = false;
                myItemAdapter.notifyItemChanged(i);
            }
        }
    }

    public class PackInfoHolder {
        boolean isSwiped;
        long id;
        public String name;
        String title;
        String artist;

        public PackInfoHolder(boolean isSwiped, long id, String name, String title, String artist) {
            this.isSwiped = isSwiped;
            this.id = id;
            this.name = name;
            this.title = title;
            this.artist = artist;
        }
    }
}
