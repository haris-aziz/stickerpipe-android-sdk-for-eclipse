/*
 *    Copyright (C) 2015
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package vc908.stickerfactory.ui.advancedrecyclerview.utils;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import vc908.stickerfactory.ui.advancedrecyclerview.swipeable.RecyclerViewSwipeManager;
import vc908.stickerfactory.ui.advancedrecyclerview.swipeable.SwipeableItemViewHolder;

public abstract class AbstractSwipeableItemViewHolder extends RecyclerView.ViewHolder implements SwipeableItemViewHolder {
    private int mSwipeStateFlags;
    private int mSwipeResult = RecyclerViewSwipeManager.RESULT_NONE;
    private int mAfterSwipeReaction = RecyclerViewSwipeManager.AFTER_SWIPE_REACTION_DEFAULT;
    private float mSwipeAmount;
    private float mMaxLeftSwipeAmount = RecyclerViewSwipeManager.OUTSIDE_OF_THE_WINDOW_LEFT;
    private float mMaxRightSwipeAmount = RecyclerViewSwipeManager.OUTSIDE_OF_THE_WINDOW_RIGHT;

    public AbstractSwipeableItemViewHolder(View itemView) {
        super(itemView);
    }

    @Override
    public void setSwipeStateFlags(int flags) {
        mSwipeStateFlags = flags;
    }

    @Override
    public int getSwipeStateFlags() {
        return mSwipeStateFlags;
    }

    @Override
    public void setSwipeResult(int result) {
        mSwipeResult = result;
    }

    @Override
    public int getSwipeResult() {
        return mSwipeResult;
    }

    @Override
    public int getAfterSwipeReaction() {
        return mAfterSwipeReaction;
    }

    @Override
    public void setAfterSwipeReaction(int reaction) {
        mAfterSwipeReaction = reaction;
    }

    @Override
    public float getSwipeItemSlideAmount() {
        return mSwipeAmount;
    }

    @Override
    public void setSwipeItemSlideAmount(float amount) {
        mSwipeAmount = amount;
    }

    @Override
    public void setMaxLeftSwipeAmount(float amount) {
        mMaxLeftSwipeAmount = amount;
    }

    @Override
    public float getMaxLeftSwipeAmount() {
        return mMaxLeftSwipeAmount;
    }

    @Override
    public void setMaxRightSwipeAmount(float amount) {
        mMaxRightSwipeAmount = amount;
    }

    @Override
    public float getMaxRightSwipeAmount() {
        return mMaxRightSwipeAmount;
    }
}
