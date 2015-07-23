package vc908.stickerfactory.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ListView;

public class BlockingListView extends ListView {

    private boolean mBlockLayoutChildren;
    private boolean mBlockLayoutAndMeasure;
    private int mRestoreViewPosition;
    private int mTopOffset;

    public BlockingListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setBlockLayoutChildren(boolean block) {
        mBlockLayoutChildren = block;
    }

    public void setBlockLayoutAndMeasure(boolean block) {
        mBlockLayoutAndMeasure = block;
    }

    public boolean isBlockLayoutAndMeasure() {
        return mBlockLayoutAndMeasure;
    }

    @Override
    protected void layoutChildren() {
        if (!mBlockLayoutChildren && !mBlockLayoutAndMeasure) {
            super.layoutChildren();
        }
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (!mBlockLayoutAndMeasure) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        } else {
            setMeasuredDimension(getWidth(), getHeight());
        }
    }

    public void saveCurrentPosition(boolean isNeedRestoreState) {
        if (isNeedRestoreState) {
            setBlockLayoutChildren(true);
            mRestoreViewPosition = getFirstVisiblePosition();
            boolean hasScrollbarOffset = mRestoreViewPosition == 0;
            boolean onlyOneVisible = getFirstVisiblePosition() == getLastVisiblePosition();
            View firstView = getChildAt(onlyOneVisible ? 0 : hasScrollbarOffset ? 1 : 0);
            mRestoreViewPosition += onlyOneVisible ? 0 : hasScrollbarOffset ? 1 : 0;
            mTopOffset = firstView == null ? 0 : firstView.getTop();
        }
    }

    public void restoreCurrentPosition(int itemsAppended, boolean needAppendChat, boolean isNeedRestoreState) {
        if (isNeedRestoreState) {
            setBlockLayoutChildren(false);
            setSelectionFromTop(mRestoreViewPosition + itemsAppended - (!needAppendChat ? 1 : 0), mTopOffset - getPaddingTop());
        }
    }
}
