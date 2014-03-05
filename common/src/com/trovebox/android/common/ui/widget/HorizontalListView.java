/*
 * HorizontalListView.java v1.5
 *
 * 
 * The MIT License
 * Copyright (c) 2011 Paul Soucy (paul@dev-smart.com)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * http://www.dev-smart.com/archives/34
 * https://github.com/dinocore1/DevsmartLib-Android
 */

package com.trovebox.android.common.ui.widget;

import java.util.LinkedList;
import java.util.Queue;



import org.holoeverywhere.widget.AdapterView;
import org.holoeverywhere.widget.Scroller;

import com.trovebox.android.common.util.CommonUtils;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListAdapter;

public class HorizontalListView extends AdapterView<ListAdapter> {

    public static final String TAG = HorizontalListView.class.getSimpleName();

    public boolean mAlwaysOverrideTouch = true;
    protected ListAdapter mAdapter;
    private int mLeftViewIndex = -1;
    private int mRightViewIndex = 0;
    protected int mCurrentX;
    protected int mNextX;
    private int mMaxX = Integer.MAX_VALUE;
    private int mDisplayOffset = 0;
    protected Scroller mScroller;
    private GestureDetector mGesture;
    private Queue<View> mRemovedViewQueue = new LinkedList<View>();
    private OnItemSelectedListener mOnItemSelected;
    private OnItemClickListener mOnItemClicked;
    private OnItemLongClickListener mOnItemLongClicked;
    private OnDownListener mOnDownListener;
    private boolean mDataChanged = false;
    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            requestLayout();
        }
    };

    public HorizontalListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    private synchronized void initView() {
        mLeftViewIndex = -1;
        mRightViewIndex = 0;
        mDisplayOffset = 0;
        mCurrentX = 0;
        mNextX = 0;
        mMaxX = Integer.MAX_VALUE;
        mScroller = new Scroller(getContext());
        mGesture = new GestureDetector(getContext(), mOnGesture);
    }

    @Override
    public void setOnItemSelectedListener(AdapterView.OnItemSelectedListener listener) {
        mOnItemSelected = listener;
    }

    @Override
    public void setOnItemClickListener(AdapterView.OnItemClickListener listener) {
        mOnItemClicked = listener;
    }

    @Override
    public void setOnItemLongClickListener(AdapterView.OnItemLongClickListener listener) {
        mOnItemLongClicked = listener;
    }

    public void setOnDownListener(OnDownListener listener) {
        mOnDownListener = listener;
    }

    private DataSetObserver mDataObserver = new DataSetObserver() {

        @Override
        public void onChanged() {
            synchronized (HorizontalListView.this) {
                mDataChanged = true;
            }
            invalidate();
            requestLayout();
        }

        @Override
        public void onInvalidated() {
            reset();
            invalidate();
            requestLayout();
        }

    };

    @Override
    public ListAdapter getAdapter() {
        return mAdapter;
    }

    @Override
    public View getSelectedView() {
        // TODO: implement
        return null;
    }

    @Override
    public void setAdapter(ListAdapter adapter) {
        if (mAdapter != null) {
            mAdapter.unregisterDataSetObserver(mDataObserver);
        }
        mAdapter = adapter;
        mAdapter.registerDataSetObserver(mDataObserver);
        reset();
    }

    private synchronized void reset() {
        initView();
        removeAllViewsInLayout();
        requestLayout();
    }

    @Override
    public void setSelection(int position) {
        // TODO: implement
    }

    private void addAndMeasureChild(final View child, int viewPos) {
        LayoutParams params = child.getLayoutParams();
        if (params == null) {
            params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        } else
        {
        }

        addViewInLayout(child, viewPos, params, true);
        measureChild(child);
    }

    private void measureChild(final View child) {
        child.measure(MeasureSpec.makeMeasureSpec(getWidth(), MeasureSpec.AT_MOST),
                MeasureSpec.makeMeasureSpec(getHeight(), MeasureSpec.AT_MOST));
    }

    @Override
    protected synchronized void onLayout(boolean changed, int left, int top, int right, int bottom) {
        CommonUtils.verbose(TAG, "onLayout");
        super.onLayout(changed, left, top, right, bottom);

        if (mAdapter == null) {
            return;
        }

        if (mDataChanged) {
            CommonUtils.verbose(TAG, "data changed");
            int oldCurrentX = mCurrentX;
            initView();
            removeAllViewsInLayout();
            mNextX = oldCurrentX;
            mDataChanged = false;
        }

        if (mScroller.computeScrollOffset()) {
            int scrollx = mScroller.getCurrX();
            mNextX = scrollx;
            CommonUtils.verbose(TAG, "Computed scroll offset. Current x = %1$d", mNextX);
        }

        if (mNextX <= 0) {
            CommonUtils.verbose(TAG, "mNextX <= 0: %1$d", mNextX);
            mNextX = 0;
            mScroller.forceFinished(true);
        }
        if (mNextX >= mMaxX) {
            CommonUtils.verbose(TAG, "mNextX >= max: %1$d : %2$d", mNextX, mMaxX);
            mNextX = mMaxX;
            mScroller.forceFinished(true);
        }

        CommonUtils.verbose(TAG, "mCurrentX = %1$d; ; mNextX = %2$d", mCurrentX, mNextX);
        int dx = mCurrentX - mNextX;
        CommonUtils.verbose(TAG, "dx = %1$d", dx);

        removeNonVisibleItems(dx);
        fillList(dx);
        positionItems(dx);

        mCurrentX = mNextX;

        if (!mScroller.isFinished()) {
            CommonUtils.verbose(TAG, "Scroller is not finished");
            post(mRunnable);
        }
    }

    private void fillList(final int dx) {
        CommonUtils.verbose(TAG, "fillList: %1$d; getChildCount = %2$d", dx, getChildCount());
        int edge = mDisplayOffset;
        View child = getChildAt(getChildCount() - 1);
        if (child != null) {
            edge = child.getRight();
        }
        fillListRight(edge, dx);

        edge = 0;
        child = getChildAt(0);
        if (child != null) {
            edge = child.getLeft();
        }
        fillListLeft(edge, dx);

    }

    private void fillListRight(int rightEdge, final int dx) {
        CommonUtils.verbose(TAG, "fillListRight: rightEdge = %1$d; dx = %2$d", rightEdge, dx);
        int totalWidth = 0;
        Queue<View> viewQueue = new LinkedList<View>();
        while (rightEdge + dx < getWidth() && mRightViewIndex < mAdapter.getCount()) {
            CommonUtils.verbose(TAG, "mRemovedViewQueue.size = %1$d", mRemovedViewQueue.size());
            View child = mAdapter.getView(mRightViewIndex, mRemovedViewQueue.poll(), this);
            measureChild(child);
            viewQueue.offer(child);
            int childWidth = child.getMeasuredWidth();
            rightEdge += childWidth;
            totalWidth += childWidth;
            totalWidth = removeNonVisibleItemsFromLeft(totalWidth, getWidth() + childWidth,
                    viewQueue);
            CommonUtils.verbose(TAG,
                    "rightEdge = %1$d; childWidth = %2$d; rightViewIndex = %3$d"
                            + "; count = %4$d; prognosed max width = %5$d",
                     rightEdge, child.getMeasuredWidth(), mRightViewIndex,
                     mAdapter.getCount(), child.getMeasuredWidth() * mAdapter.getCount() - getWidth());

            if (mRightViewIndex == mAdapter.getCount() - 1) {
                mMaxX = mCurrentX + rightEdge - getWidth();
                CommonUtils.verbose(TAG, "Setting maxX to %1$d", mMaxX);
                CommonUtils.verbose(TAG, "mCurrentX = %1$d; rightEdge = %2$d; width = %3$d",
                        mCurrentX, rightEdge, getWidth());
            }

            if (mMaxX < 0) {
                mMaxX = 0;
            }
            mRightViewIndex++;
        }
        View child;
        while ((child = viewQueue.poll()) != null)
        {
            addAndMeasureChild(child, -1);
        }

    }

    private void fillListLeft(int leftEdge, final int dx) {
        CommonUtils.verbose(TAG, "fillListLeft: leftEdge = %1$d; dx = %2$d", leftEdge, dx);
        int totalWidth = 0;
        Queue<View> viewQueue = new LinkedList<View>();
        while (leftEdge + dx > 0 && mLeftViewIndex >= 0) {
            CommonUtils.verbose(TAG, "mRemovedViewQueue.size = %1$d", mRemovedViewQueue.size());
            View child = mAdapter.getView(mLeftViewIndex, mRemovedViewQueue.poll(), this);
            measureChild(child);
            viewQueue.offer(child);
            int childWidth = child.getMeasuredWidth();
            leftEdge -= childWidth;
            totalWidth += childWidth;
            totalWidth = removeNonVisibleItemsFromRight(totalWidth, getWidth(), viewQueue);
            mLeftViewIndex--;
            mDisplayOffset -= child.getMeasuredWidth();
        }
        View child;
        while ((child = viewQueue.poll()) != null)
        {
            addAndMeasureChild(child, 0);
        }
    }

    private void removeNonVisibleItems(final int dx) {
        CommonUtils.verbose(TAG, "removeNonVisibleItems: %1$d", dx);
        View child = getChildAt(0);
        while (child != null && child.getRight() + dx <= 0) {
            mDisplayOffset += child.getMeasuredWidth();
            mRemovedViewQueue.offer(child);
            removeViewInLayout(child);
            mLeftViewIndex++;
            child = getChildAt(0);

        }

        child = getChildAt(getChildCount() - 1);
        while (child != null && child.getLeft() + dx >= getWidth()) {
            mRemovedViewQueue.offer(child);
            removeViewInLayout(child);
            mRightViewIndex--;
            child = getChildAt(getChildCount() - 1);
        }
        CommonUtils.verbose(TAG, "mDisplayOffset = %1$d", mDisplayOffset);
    }

    private int removeNonVisibleItemsFromLeft(
            int totalWidth,
            int minRestWidth,
            Queue<View> viewQueue) {
        CommonUtils.verbose(TAG,
                "removeNonVisibleItemsFromLeft: totalWidth = %1$d, minRestWidth = %2$d",
                totalWidth, minRestWidth);
        CommonUtils.verbose(TAG, "mLeftViewIndex = %1$d", mLeftViewIndex);
        View child = viewQueue.peek();
        while (child != null) {
            int childWidth = child.getMeasuredWidth();
            CommonUtils
                    .verbose(TAG, "totalWidth = %1$d, childWidth = %2$d", totalWidth, childWidth);
            if (totalWidth - childWidth < minRestWidth)
            {
                break;
            }
            viewQueue.poll();
            mDisplayOffset += childWidth;
            totalWidth -= childWidth;
            mRemovedViewQueue.offer(child);
            mLeftViewIndex++;
            child = viewQueue.peek();
        }
        CommonUtils.verbose(TAG, "mDisplayOffset = %1$d; totalWidth = %2$d; mLeftViewIndex = %3$d",
                mDisplayOffset,
                totalWidth, mLeftViewIndex);
        return totalWidth;
    }

    private int removeNonVisibleItemsFromRight(
            int totalWidth,
            int minRestWidth,
            Queue<View> viewQueue)
    {
        CommonUtils.verbose(TAG,
                "removeNonVisibleItemsFromRight: totalWidth = %1$d, minRestWidth = %2$d",
                totalWidth, minRestWidth);
        CommonUtils.verbose(TAG, "mRightViewIndex = %1$d", mRightViewIndex);
        View child = viewQueue.peek();
        while (child != null) {
            int childWidth = child.getMeasuredWidth();
            CommonUtils
                    .verbose(TAG, "totalWidth = %1$d, childWidth = %2$d", totalWidth, childWidth);
            if (totalWidth - childWidth < minRestWidth)
            {
                break;
            }
            viewQueue.poll();
            totalWidth -= childWidth;
            mRemovedViewQueue.offer(child);
            mRightViewIndex--;
            child = viewQueue.peek();
        }
        CommonUtils.verbose(TAG, "totalWidth = %1$d; mRightViewIndex = %2$d",
                totalWidth, mRightViewIndex);
        return totalWidth;
    }

    private void positionItems(final int dx) {
        CommonUtils.verbose(TAG, "positionItems: dx = %1$d", dx);
        if (getChildCount() > 0) {
            mDisplayOffset += dx;
            int left = mDisplayOffset;
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                int childWidth = child.getMeasuredWidth();
                child.layout(left, 0, left + childWidth, child.getMeasuredHeight());
                left += childWidth + child.getPaddingRight();
            }
        }
    }

    public synchronized void scrollTo(int x) {
        CommonUtils.verbose(TAG, "Requested scroll from x = %1$d to x = %2$d", mNextX, x);
        mScroller.startScroll(mNextX, 0, x - mNextX, 0);
        requestLayout();
    }

    public int getStartX()
    {
        return mNextX;
    }
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        boolean handled = super.dispatchTouchEvent(ev);
        handled |= mGesture.onTouchEvent(ev);
        return handled;
    }

    protected boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
            float velocityY) {
        synchronized (HorizontalListView.this) {
            mScroller.fling(mNextX, 0, (int) -velocityX, 0, 0, mMaxX, 0, 0);
        }
        requestLayout();

        return true;
    }

    protected boolean onDown(MotionEvent e) {
        mScroller.forceFinished(true);
        if (mOnDownListener != null)
        {
            mOnDownListener.onDown(e);
        }
        return true;
    }

    private OnGestureListener mOnGesture = new GestureDetector.SimpleOnGestureListener() {

        @Override
        public boolean onDown(MotionEvent e) {
            return HorizontalListView.this.onDown(e);
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                float velocityY) {
            CommonUtils.verbose(TAG, "onFling");
            return HorizontalListView.this.onFling(e1, e2, velocityX, velocityY);
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                float distanceX, float distanceY) {

            CommonUtils.verbose(TAG, "onScroll: distanceX = %1$f", distanceX);
            synchronized (HorizontalListView.this) {
                mNextX += (int) distanceX;
            }
            requestLayout();

            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                if (isEventWithinView(e, child)) {
                    if (mOnItemClicked != null) {
                        mOnItemClicked.onItemClick(HorizontalListView.this, child, mLeftViewIndex
                                + 1 + i, mAdapter.getItemId(mLeftViewIndex + 1 + i));
                    }
                    if (mOnItemSelected != null) {
                        mOnItemSelected.onItemSelected(HorizontalListView.this, child,
                                mLeftViewIndex + 1 + i, mAdapter.getItemId(mLeftViewIndex + 1 + i));
                    }
                    break;
                }

            }
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            int childCount = getChildCount();
            for (int i = 0; i < childCount; i++) {
                View child = getChildAt(i);
                if (isEventWithinView(e, child)) {
                    if (mOnItemLongClicked != null) {
                        mOnItemLongClicked.onItemLongClick(HorizontalListView.this, child,
                                mLeftViewIndex + 1 + i, mAdapter.getItemId(mLeftViewIndex + 1 + i));
                    }
                    break;
                }

            }
        }

        private boolean isEventWithinView(MotionEvent e, View child) {
            Rect viewRect = new Rect();
            int[] childPosition = new int[2];
            child.getLocationOnScreen(childPosition);
            int left = childPosition[0];
            int right = left + child.getWidth();
            int top = childPosition[1];
            int bottom = top + child.getHeight();
            viewRect.set(left, top, right, bottom);
            return viewRect.contains((int) e.getRawX(), (int) e.getRawY());
        }
    };

    public static interface OnDownListener
    {
        void onDown(MotionEvent e);
    }
}