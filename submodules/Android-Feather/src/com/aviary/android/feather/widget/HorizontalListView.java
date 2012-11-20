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
 *
 */

package com.aviary.android.feather.widget;

import java.util.LinkedList;
import java.util.Queue;
import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.Scroller;
import com.aviary.android.feather.library.log.LoggerFactory;

// TODO: Auto-generated Javadoc
/**
 * The Class HorizontialListView.
 */
public class HorizontalListView extends AdapterView<ListAdapter> {

	/** The m always override touch. */
	public boolean mAlwaysOverrideTouch = true;

	/** The m adapter. */
	protected ListAdapter mAdapter;

	/** The m left view index. */
	private int mLeftViewIndex = -1;

	/** The m right view index. */
	private int mRightViewIndex = 0;

	/** The m current x. */
	protected int mCurrentX;

	/** The m next x. */
	protected int mNextX;

	/** The m max x. */
	private int mMaxX = Integer.MAX_VALUE;

	/** The m display offset. */
	private int mDisplayOffset = 0;

	/** The m scroller. */
	protected Scroller mScroller;

	/** The m gesture. */
	private GestureDetector mGesture;

	/** The m removed view queue. */
	private Queue<View> mRemovedViewQueue = new LinkedList<View>();

	/** The m on item selected. */
	private OnItemSelectedListener mOnItemSelected;

	/** The m on item clicked. */
	private OnItemClickListener mOnItemClicked;

	/** The m data changed. */
	private boolean mDataChanged = false;

	/**
	 * Instantiates a new horizontial list view.
	 * 
	 * @param context
	 *           the context
	 * @param attrs
	 *           the attrs
	 */
	public HorizontalListView( Context context, AttributeSet attrs ) {
		super( context, attrs );
		initView();
	}

	/**
	 * Inits the view.
	 */
	private synchronized void initView() {
		mLeftViewIndex = -1;
		mRightViewIndex = 0;
		mDisplayOffset = 0;
		mCurrentX = 0;
		mNextX = 0;
		mMaxX = Integer.MAX_VALUE;
		mScroller = new Scroller( getContext() );
		mGesture = new GestureDetector( getContext(), mOnGesture );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.AdapterView#setOnItemSelectedListener(android.widget.AdapterView.OnItemSelectedListener)
	 */
	@Override
	public void setOnItemSelectedListener( AdapterView.OnItemSelectedListener listener ) {
		mOnItemSelected = listener;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.AdapterView#setOnItemClickListener(android.widget.AdapterView.OnItemClickListener)
	 */
	@Override
	public void setOnItemClickListener( AdapterView.OnItemClickListener listener ) {
		mOnItemClicked = listener;
	}

	/** The m data observer. */
	private DataSetObserver mDataObserver = new DataSetObserver() {

		@Override
		public void onChanged() {
			synchronized ( HorizontalListView.this ) {
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

	/** The m height measure spec. */
	private int mHeightMeasureSpec;

	/** The m width measure spec. */
	private int mWidthMeasureSpec;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.AdapterView#getAdapter()
	 */
	@Override
	public ListAdapter getAdapter() {
		return mAdapter;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.AdapterView#getSelectedView()
	 */
	@Override
	public View getSelectedView() {
		// TODO: implement
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.AdapterView#setAdapter(android.widget.Adapter)
	 */
	@Override
	public void setAdapter( ListAdapter adapter ) {
		if ( mAdapter != null ) {
			mAdapter.unregisterDataSetObserver( mDataObserver );
		}

		mAdapter = adapter;

		if ( null != mAdapter ) {
			mAdapter.registerDataSetObserver( mDataObserver );
		}
		reset();
	}

	/**
	 * Reset.
	 */
	private synchronized void reset() {
		initView();
		removeAllViewsInLayout();
		requestLayout();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.AdapterView#setSelection(int)
	 */
	@Override
	public void setSelection( int position ) {
		// TODO: implement
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View#onMeasure(int, int)
	 */
	@Override
	protected void onMeasure( int widthMeasureSpec, int heightMeasureSpec ) {
		Log.d( VIEW_LOG_TAG, "onMeasure" );
		super.onMeasure( widthMeasureSpec, heightMeasureSpec );

		mHeightMeasureSpec = heightMeasureSpec;
		mWidthMeasureSpec = widthMeasureSpec;
	}

	/**
	 * Adds the and measure child.
	 * 
	 * @param child
	 *           the child
	 * @param viewPos
	 *           the view pos
	 */
	private void addAndMeasureChild( final View child, int viewPos ) {
		LayoutParams params = child.getLayoutParams();

		if ( params == null ) {
			params = new LayoutParams( LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT );
		}

		addViewInLayout( child, viewPos, params );
		forceChildLayout( child, params );
	}

	public void forceChildLayout( final View child, final LayoutParams params ) {
		int childHeightSpec = ViewGroup.getChildMeasureSpec( mHeightMeasureSpec, getPaddingTop() + getPaddingBottom(), params.height );
		int childWidthSpec = ViewGroup.getChildMeasureSpec( mWidthMeasureSpec, getPaddingLeft() + getPaddingRight(), params.width );
		child.measure( childWidthSpec, childHeightSpec );
	}

	/**
	 * Gets the real height.
	 * 
	 * @return the real height
	 */
	@SuppressWarnings("unused")
	private int getRealHeight() {
		return getHeight() - ( getPaddingTop() + getPaddingBottom() );
	}

	/**
	 * Gets the real width.
	 * 
	 * @return the real width
	 */
	@SuppressWarnings("unused")
	private int getRealWidth() {
		return getWidth() - ( getPaddingLeft() + getPaddingRight() );
	}

	public void requestFullLayout() {
		mDataChanged = true;
		invalidate();
		requestLayout();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.AdapterView#onLayout(boolean, int, int, int, int)
	 */
	@Override
	protected synchronized void onLayout( boolean changed, int left, int top, int right, int bottom ) {
		super.onLayout( changed, left, top, right, bottom );

		Log.d( VIEW_LOG_TAG, "onLayout: " + changed );

		if ( mAdapter == null ) {
			return;
		}

		if ( mDataChanged ) {
			int oldCurrentX = mCurrentX;
			initView();
			removeAllViewsInLayout();
			mNextX = oldCurrentX;
			mDataChanged = false;
		}

		if ( mScroller.computeScrollOffset() ) {
			int scrollx = mScroller.getCurrX();
			mNextX = scrollx;
		}

		if ( mNextX < 0 ) {
			mNextX = 0;
			mScroller.forceFinished( true );
		}
		if ( mNextX > mMaxX ) {
			mNextX = mMaxX;
			mScroller.forceFinished( true );
		}

		int dx = mCurrentX - mNextX;

		removeNonVisibleItems( dx );
		fillList( dx );
		positionItems( dx );

		mCurrentX = mNextX;

		if ( !mScroller.isFinished() ) {
			post( mRequestLayoutRunnable );
		}
	}

	private Runnable mRequestLayoutRunnable = new Runnable() {

		@Override
		public void run() {
			requestLayout();
		}
	};

	/**
	 * Fill list.
	 * 
	 * @param dx
	 *           the dx
	 */
	private void fillList( final int dx ) {
		int edge = 0;
		View child = getChildAt( getChildCount() - 1 );
		if ( child != null ) {
			edge = child.getRight();
		}
		fillListRight( edge, dx );

		edge = 0;
		child = getChildAt( 0 );
		if ( child != null ) {
			edge = child.getLeft();
		}
		fillListLeft( edge, dx );

	}

	/**
	 * Fill list right.
	 * 
	 * @param rightEdge
	 *           the right edge
	 * @param dx
	 *           the dx
	 */
	private void fillListRight( int rightEdge, final int dx ) {
		while ( rightEdge + dx < getWidth() && mRightViewIndex < mAdapter.getCount() ) {

			View child = mAdapter.getView( mRightViewIndex, mRemovedViewQueue.poll(), this );
			addAndMeasureChild( child, -1 );
			rightEdge += child.getMeasuredWidth();

			if ( mRightViewIndex == mAdapter.getCount() - 1 ) {
				mMaxX = mCurrentX + rightEdge - getWidth();
			}
			mRightViewIndex++;
		}

	}

	/**
	 * Fill list left.
	 * 
	 * @param leftEdge
	 *           the left edge
	 * @param dx
	 *           the dx
	 */
	private void fillListLeft( int leftEdge, final int dx ) {
		while ( leftEdge + dx > 0 && mLeftViewIndex >= 0 ) {
			View child = mAdapter.getView( mLeftViewIndex, mRemovedViewQueue.poll(), this );
			addAndMeasureChild( child, 0 );
			leftEdge -= child.getMeasuredWidth();
			mLeftViewIndex--;
			mDisplayOffset -= child.getMeasuredWidth();
		}
	}

	/**
	 * Removes the non visible items.
	 * 
	 * @param dx
	 *           the dx
	 */
	private void removeNonVisibleItems( final int dx ) {
		View child = getChildAt( 0 );
		while ( child != null && child.getRight() + dx <= 0 ) {
			mDisplayOffset += child.getMeasuredWidth();
			mRemovedViewQueue.offer( child );
			removeViewInLayout( child );
			mLeftViewIndex++;
			child = getChildAt( 0 );

		}

		child = getChildAt( getChildCount() - 1 );
		while ( child != null && child.getLeft() + dx >= getWidth() ) {
			mRemovedViewQueue.offer( child );
			removeViewInLayout( child );
			mRightViewIndex--;
			child = getChildAt( getChildCount() - 1 );
		}
	}

	/**
	 * Position items.
	 * 
	 * @param dx
	 *           the dx
	 */
	private void positionItems( final int dx ) {
		if ( getChildCount() > 0 ) {
			mDisplayOffset += dx;
			int left = mDisplayOffset;
			for ( int i = 0; i < getChildCount(); i++ ) {
				View child = getChildAt( i );
				int childWidth = child.getMeasuredWidth();
				int childTop = getPaddingTop();
				child.layout( left, childTop, left + childWidth, childTop + child.getMeasuredHeight() );
				left += childWidth;
			}
		}
	}

	/**
	 * Scroll to.
	 * 
	 * @param x
	 *           the x
	 */
	public synchronized void scrollTo( int x ) {
		mScroller.startScroll( mNextX, 0, x - mNextX, 0 );
		requestLayout();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.ViewGroup#dispatchTouchEvent(android.view.MotionEvent)
	 */
	@Override
	public boolean dispatchTouchEvent( MotionEvent ev ) {
		boolean handled = mGesture.onTouchEvent( ev );
		return handled;
	}

	/**
	 * On fling.
	 * 
	 * @param e1
	 *           the e1
	 * @param e2
	 *           the e2
	 * @param velocityX
	 *           the velocity x
	 * @param velocityY
	 *           the velocity y
	 * @return true, if successful
	 */
	protected boolean onFling( MotionEvent e1, MotionEvent e2, float velocityX, float velocityY ) {
		synchronized ( HorizontalListView.this ) {
			Log.i( LoggerFactory.LOG_TAG, "onFling: " + -velocityX + ", " + mMaxX );
			mScroller.fling( mNextX, 0, (int) -( velocityX / 2 ), 0, Integer.MIN_VALUE, Integer.MAX_VALUE, 0, 0 );
		}
		requestLayout();

		return true;
	}

	/** The m scroller running. */
	boolean mScrollerRunning;

	/**
	 * On down.
	 * 
	 * @param e
	 *           the e
	 * @return true, if successful
	 */
	protected boolean onDown( MotionEvent e ) {

		if ( !mScroller.isFinished() ) {
			mScrollerRunning = true;
		} else {
			mScrollerRunning = false;
		}

		mScroller.forceFinished( true );
		return true;
	}

	/** The m on gesture. */
	private OnGestureListener mOnGesture = new GestureDetector.SimpleOnGestureListener() {

		@Override
		public boolean onDown( MotionEvent e ) {
			return HorizontalListView.this.onDown( e );
		}

		@Override
		public boolean onFling( MotionEvent e1, MotionEvent e2, float velocityX, float velocityY ) {
			return HorizontalListView.this.onFling( e1, e2, velocityX, velocityY );
		}

		@Override
		public boolean onScroll( MotionEvent e1, MotionEvent e2, float distanceX, float distanceY ) {

			synchronized ( HorizontalListView.this ) {
				mNextX += (int) distanceX;
			}
			requestLayout();

			return true;
		}

		@Override
		public boolean onSingleTapConfirmed( MotionEvent e ) {
			Log.i( LoggerFactory.LOG_TAG, "onSingleTapConfirmed" );

			if ( mScrollerRunning ) return false;

			Rect viewRect = new Rect();
			for ( int i = 0; i < getChildCount(); i++ ) {
				View child = getChildAt( i );
				int left = child.getLeft();
				int right = child.getRight();
				int top = child.getTop();
				int bottom = child.getBottom();
				viewRect.set( left, top, right, bottom );
				if ( viewRect.contains( (int) e.getX(), (int) e.getY() ) ) {
					if ( mOnItemClicked != null ) {
						mOnItemClicked.onItemClick( HorizontalListView.this, child, mLeftViewIndex + 1 + i,
								mAdapter.getItemId( mLeftViewIndex + 1 + i ) );
					}
					if ( mOnItemSelected != null ) {
						mOnItemSelected.onItemSelected( HorizontalListView.this, child, mLeftViewIndex + 1 + i,
								mAdapter.getItemId( mLeftViewIndex + 1 + i ) );
					}
					break;
				}

			}
			return true;
		}

	};

}
