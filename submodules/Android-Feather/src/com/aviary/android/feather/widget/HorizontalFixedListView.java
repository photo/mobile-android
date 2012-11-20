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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import com.aviary.android.feather.R;
import com.aviary.android.feather.database.DataSetObserverExtended;
import com.aviary.android.feather.library.log.LoggerFactory;
import com.aviary.android.feather.library.log.LoggerFactory.Logger;
import com.aviary.android.feather.library.log.LoggerFactory.LoggerType;
import com.aviary.android.feather.library.utils.ReflectionUtils;
import com.aviary.android.feather.library.utils.ReflectionUtils.ReflectionException;
import com.aviary.android.feather.widget.IFlingRunnable.FlingRunnableView;
import com.aviary.android.feather.widget.wp.EdgeGlow;

// TODO: Auto-generated Javadoc
/**
 * The Class HorizontialFixedListView.
 */
public class HorizontalFixedListView extends AdapterView<ListAdapter> implements OnGestureListener, FlingRunnableView {

	/** The Constant LOG_TAG. */
	protected static final String LOG_TAG = "hv";

	/** The m adapter. */
	protected ListAdapter mAdapter;

	/** The m left view index. */
	private int mLeftViewIndex = -1;

	/** The m right view index. */
	private int mRightViewIndex = 0;

	/** The m gesture. */
	private GestureDetector mGesture;

	/** The m removed view queue. */
	private List<Queue<View>> mRecycleBin;

	/** The m on item selected. */
	private OnItemSelectedListener mOnItemSelected;

	/** The m on item clicked. */
	private OnItemClickListener mOnItemClicked;

	/** The m data changed. */
	private boolean mDataChanged = false;

	/** The m fling runnable. */
	private IFlingRunnable mFlingRunnable;

	/** The m force layout. */
	private boolean mForceLayout;

	private int mDragTolerance = 0;

	private boolean mDragScrollEnabled;

	protected int mItemCount = 0;

	protected EdgeGlow mEdgeGlowLeft, mEdgeGlowRight;

	private int mOverScrollMode = OVER_SCROLL_NEVER;

	static Logger logger = LoggerFactory.getLogger( "HorizontalFixedList", LoggerType.ConsoleLoggerType );

	/**
	 * Interface definition for a callback to be invoked when an item in this view has been clicked and held.
	 */
	public interface OnItemDragListener {

		/**
		 * Callback method to be invoked when an item in this view has been dragged outside the vertical tolerance area.
		 * 
		 * Implementers can call getItemAtPosition(position) if they need to access the data associated with the selected item.
		 * 
		 * @param parent
		 *           The AbsListView where the click happened
		 * @param view
		 *           The view within the AbsListView that was clicked
		 * @param position
		 *           The position of the view in the list
		 * @param id
		 *           The row id of the item that was clicked
		 * 
		 * @return true if the callback consumed the long click, false otherwise
		 */
		boolean onItemStartDrag( AdapterView<?> parent, View view, int position, long id );
	}

	private OnItemDragListener mItemDragListener;

	public void setOnItemDragListener( OnItemDragListener listener ) {
		mItemDragListener = listener;
	}

	public OnItemDragListener getOnItemDragListener() {
		return mItemDragListener;
	}

	/**
	 * Instantiates a new horizontial fixed list view.
	 * 
	 * @param context
	 *           the context
	 * @param attrs
	 *           the attrs
	 */
	public HorizontalFixedListView( Context context, AttributeSet attrs ) {
		super( context, attrs );
		initView();
	}

	public HorizontalFixedListView( Context context, AttributeSet attrs, int defStyle ) {
		super( context, attrs, defStyle );
		initView();
	}

	/**
	 * Inits the view.
	 */
	private synchronized void initView() {

		if ( Build.VERSION.SDK_INT > 8 ) {
			try {
				mFlingRunnable = (IFlingRunnable) ReflectionUtils.newInstance( "com.aviary.android.feather.widget.Fling9Runnable",
						new Class<?>[] { FlingRunnableView.class, int.class }, this, mAnimationDuration );
			} catch ( ReflectionException e ) {
				mFlingRunnable = new Fling8Runnable( this, mAnimationDuration );
			}
		} else {
			mFlingRunnable = new Fling8Runnable( this, mAnimationDuration );
		}

		mLeftViewIndex = -1;
		mRightViewIndex = 0;
		mMaxX = 0;
		mMinX = 0;
		mChildWidth = 0;
		mChildHeight = 0;
		mRightEdge = 0;
		mLeftEdge = 0;
		mGesture = new GestureDetector( getContext(), mGestureListener );
		mGesture.setIsLongpressEnabled( true );

		setFocusable( true );
		setFocusableInTouchMode( true );

		ViewConfiguration configuration = ViewConfiguration.get( getContext() );
		mTouchSlop = configuration.getScaledTouchSlop();
		mDragTolerance = mTouchSlop;
		mOverscrollDistance = 10;
		mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
		mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();

	}

	public void setOverScrollMode( int mode ) {
		mOverScrollMode = mode;

		if ( mode != OVER_SCROLL_NEVER ) {
			if ( mEdgeGlowLeft == null ) {
				Drawable glow = getContext().getResources().getDrawable( R.drawable.feather_overscroll_glow );
				Drawable edge = getContext().getResources().getDrawable( R.drawable.feather_overscroll_edge );
				mEdgeGlowLeft = new EdgeGlow( edge, glow );
				mEdgeGlowRight = new EdgeGlow( edge, glow );
				mEdgeGlowLeft.setColorFilter( 0xFF33b5e5, Mode.MULTIPLY );
			}
		} else {
			mEdgeGlowLeft = mEdgeGlowRight = null;
		}
	}

	public void setEdgeHeight( int value ) {
		mEdgesHeight = value;
	}

	public void setEdgeGravityY( int value ) {
		mEdgesGravityY = value;
	}

	@Override
	public void trackMotionScroll( int newX ) {

		scrollTo( newX, 0 );
		mCurrentX = getScrollX();
		removeNonVisibleItems( mCurrentX );
		fillList( mCurrentX );
		invalidate();
	}

	@Override
	protected void dispatchDraw( Canvas canvas ) {
		super.dispatchDraw( canvas );

		if ( getChildCount() > 0 ) {
			drawEdges( canvas );
		}
	}

	private Matrix mEdgeMatrix = new Matrix();

	/**
	 * Draw glow edges.
	 * 
	 * @param canvas
	 *           the canvas
	 */
	private void drawEdges( Canvas canvas ) {

		if ( mEdgeGlowLeft != null ) {
			if ( !mEdgeGlowLeft.isFinished() ) {
				final int restoreCount = canvas.save();
				final int height = mEdgesHeight;

				mEdgeMatrix.reset();
				mEdgeMatrix.postRotate( -90 );
				mEdgeMatrix.postTranslate( 0, height );

				if ( mEdgesGravityY == Gravity.BOTTOM ) {
					mEdgeMatrix.postTranslate( 0, getHeight() - height );
				}
				canvas.concat( mEdgeMatrix );

				mEdgeGlowLeft.setSize( height, height / 5 );

				if ( mEdgeGlowLeft.draw( canvas ) ) {
					postInvalidate();
				}
				canvas.restoreToCount( restoreCount );
			}
			if ( !mEdgeGlowRight.isFinished() ) {
				final int restoreCount = canvas.save();
				final int width = getWidth();
				final int height = mEdgesHeight;

				mEdgeMatrix.reset();
				mEdgeMatrix.postRotate( 90 );
				mEdgeMatrix.postTranslate( mCurrentX + width, 0 );

				if ( mEdgesGravityY == Gravity.BOTTOM ) {
					mEdgeMatrix.postTranslate( 0, getHeight() - height );
				}
				canvas.concat( mEdgeMatrix );

				mEdgeGlowRight.setSize( height, height / 5 );

				if ( mEdgeGlowRight.draw( canvas ) ) {
					postInvalidate();
				}
				canvas.restoreToCount( restoreCount );
			}
		}
	}

	/**
	 * Set if a vertical scroll movement will trigger a long click event
	 * 
	 * @param value
	 */
	public void setDragScrollEnabled( boolean value ) {
		mDragScrollEnabled = value;
	}

	public boolean getDragScrollEnabled() {
		return mDragScrollEnabled;
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

	private DataSetObserverExtended mDataObserverExtended = new DataSetObserverExtended() {

		public void onAdded() {
			logger.log( "DataSet::onAdded" );

			synchronized ( HorizontalFixedListView.this ) {
				mItemCount = mAdapter.getCount();
			}
			mDataChanged = true;
			requestLayout();

		};

		public void onRemoved() {
			logger.log( "DataSet::onRemoved" );
			this.onChanged();
		};

		public void onChanged() {
			logger.log( "DataSet::onChanged" );
			mItemCount = mAdapter.getCount();
			reset();
		};

		public void onInvalidated() {
			logger.log( "DataSet::onInvalidated" );
			this.onChanged();
		};
	};

	/** The m data observer. */
	private DataSetObserver mDataObserver = new DataSetObserver() {

		@Override
		public void onChanged() {
			logger.log( "DataSet::onChanged" );
			synchronized ( HorizontalFixedListView.this ) {
				mItemCount = mAdapter.getCount();
			}
			invalidate();
			reset();
		}

		@Override
		public void onInvalidated() {
			logger.log( "DataSet::onInvalidated" );
			mItemCount = mAdapter.getCount();
			invalidate();
			reset();
		}
	};

	public void requestFullLayout() {
		mForceLayout = true;
		invalidate();
		requestLayout();
	}

	/** The m height measure spec. */
	private int mHeightMeasureSpec;

	/** The m width measure spec. */
	private int mWidthMeasureSpec;

	/** The m left edge. */
	private int mRightEdge, mLeftEdge;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.AdapterView#getAdapter()
	 */
	@Override
	public ListAdapter getAdapter() {
		return mAdapter;
	}

	@Override
	public View getSelectedView() {
		return null;
	}

	@Override
	public void setAdapter( ListAdapter adapter ) {

		if ( mAdapter != null ) {

			if ( mAdapter instanceof BaseAdapterExtended ) {
				( (BaseAdapterExtended) mAdapter ).unregisterDataSetObserverExtended( mDataObserverExtended );
			} else {
				mAdapter.unregisterDataSetObserver( mDataObserver );
			}

			emptyRecycler();
			mItemCount = 0;
		}

		mAdapter = adapter;

		if ( mAdapter != null ) {
			mItemCount = mAdapter.getCount();

			if ( mAdapter instanceof BaseAdapterExtended ) {
				( (BaseAdapterExtended) mAdapter ).registerDataSetObserverExtended( mDataObserverExtended );
			} else {
				mAdapter.registerDataSetObserver( mDataObserver );
			}
			int total = mAdapter.getViewTypeCount();
			mRecycleBin = Collections.synchronizedList( new ArrayList<Queue<View>>() );
			for ( int i = 0; i < total; i++ ) {
				mRecycleBin.add( new LinkedList<View>() );
			}
		}
		reset();
	}
	
	private void emptyRecycler() {
		if ( null != mRecycleBin ) {
			while ( mRecycleBin.size() > 0 ) {
				Queue<View> recycler = mRecycleBin.remove( 0 );
				recycler.clear();
			}
			mRecycleBin.clear();
		}
	}

	/**
	 * Reset.
	 */
	private synchronized void reset() {
		mCurrentX = 0;
		initView();
		removeAllViewsInLayout();
		mForceLayout = true;
		requestLayout();
	}
	
	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		Log.d( LOG_TAG, "onDetachedFromWindow" );
		emptyRecycler();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.AdapterView#setSelection(int)
	 */
	@Override
	public void setSelection( int position ) {}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View#onMeasure(int, int)
	 */
	@Override
	protected void onMeasure( int widthMeasureSpec, int heightMeasureSpec ) {
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
			params = new LayoutParams( LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT );
		}

		addViewInLayout( child, viewPos, params, false );
		forceChildLayout( child, params );
	}

	public void forceChildLayout( View child, LayoutParams params ) {
		int childHeightSpec = ViewGroup.getChildMeasureSpec( mHeightMeasureSpec, getPaddingTop() + getPaddingBottom(), params.height );
		int childWidthSpec = ViewGroup.getChildMeasureSpec( mWidthMeasureSpec, getPaddingLeft() + getPaddingRight(), params.width );
		child.measure( childWidthSpec, childHeightSpec );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.AdapterView#onLayout(boolean, int, int, int, int)
	 */
	@Override
	protected void onLayout( boolean changed, int left, int top, int right, int bottom ) {
		super.onLayout( changed, left, top, right, bottom );

		if ( mAdapter == null ) {
			return;
		}

		if ( !changed && !mDataChanged ) {
			layoutChildren();
		}

		if ( changed ) {
			mCurrentX = mOldX = 0;
			initView();
			removeAllViewsInLayout();
			trackMotionScroll( 0 );
		}

		if ( mDataChanged ) {
			trackMotionScroll( mCurrentX );
			mDataChanged = false;
		}

		if ( mForceLayout ) {
			mOldX = mCurrentX;
			initView();
			removeAllViewsInLayout();
			trackMotionScroll( mOldX );
			mForceLayout = false;
		}

	}

	public void layoutChildren() {

		int paddingTop = getPaddingTop();
		int left, right;

		for ( int i = 0; i < getChildCount(); i++ ) {
			View child = getChildAt( i );

			forceChildLayout( child, child.getLayoutParams() );

			left = child.getLeft();
			right = child.getRight();
			child.layout( left, paddingTop, right, paddingTop + child.getMeasuredHeight() );
		}
	}

	/**
	 * Fill list.
	 * 
	 * @param positionX
	 *           the position x
	 */
	private void fillList( final int positionX ) {
		int edge = 0;

		View child = getChildAt( getChildCount() - 1 );
		if ( child != null ) {
			edge = child.getRight();
		}
		fillListRight( mCurrentX, edge );

		edge = 0;
		child = getChildAt( 0 );
		if ( child != null ) {
			edge = child.getLeft();
		}
		fillListLeft( mCurrentX, edge );
	}

	/**
	 * Fill list left.
	 * 
	 * @param positionX
	 *           the position x
	 * @param leftEdge
	 *           the left edge
	 */
	private void fillListLeft( int positionX, int leftEdge ) {

		if ( mAdapter == null ) return;

		while ( ( leftEdge - positionX ) > mLeftEdge && mLeftViewIndex >= 0 ) {
			int viewType = mAdapter.getItemViewType( mLeftViewIndex );
			View child = mAdapter.getView( mLeftViewIndex, mRecycleBin.get( viewType ).poll(), this );
			addAndMeasureChild( child, 0 );

			int childTop = getPaddingTop();
			child.layout( leftEdge - mChildWidth, childTop, leftEdge, childTop + mChildHeight );
			leftEdge -= mChildWidth;
			mLeftViewIndex--;
		}
	}

	public View getItemAt( int position ) {
		return getChildAt( position - ( mLeftViewIndex + 1 ) );
	}
	
	public int getScreenPositionForView( View view ) {
		View listItem = view;
		try {
			View v;
			while ( !( v = (View) listItem.getParent() ).equals( this ) ) {
				listItem = v;
			}
		} catch ( ClassCastException e ) {
			// We made it up to the window without find this list view
			return INVALID_POSITION;
		}

		// Search the children for the list item
		final int childCount = getChildCount();
		for ( int i = 0; i < childCount; i++ ) {
			if ( getChildAt( i ).equals( listItem ) ) {
				return i;
			}
		}

		// Child not found!
		return INVALID_POSITION;
	}

	@Override
	public int getPositionForView( View view ) {
		View listItem = view;
		try {
			View v;
			while ( !( v = (View) listItem.getParent() ).equals( this ) ) {
				listItem = v;
			}
		} catch ( ClassCastException e ) {
			// We made it up to the window without find this list view
			return INVALID_POSITION;
		}

		// Search the children for the list item
		final int childCount = getChildCount();
		for ( int i = 0; i < childCount; i++ ) {
			if ( getChildAt( i ).equals( listItem ) ) {
				return mLeftViewIndex + i + 1;
			}
		}

		// Child not found!
		return INVALID_POSITION;
	}

	public void setHideLastChild( boolean value ) {
		mHideLastChild = value;
	}

	/**
	 * Items will appear from right to left
	 * 
	 * @param value
	 */
	public void setInverted( boolean value ) {
		mInverted = value;
	}

	/**
	 * Fill list right.
	 * 
	 * @param positionX
	 *           the position x
	 * @param rightEdge
	 *           the right edge
	 */
	private void fillListRight( int positionX, int rightEdge ) {
		boolean firstChild = getChildCount() == 0 || mDataChanged || mForceLayout;

		if ( mAdapter == null ) return;

		while ( ( rightEdge - positionX ) < mRightEdge || firstChild ) {

			if ( mRightViewIndex >= mItemCount ) {
				break;
			}

			int viewType = mAdapter.getItemViewType( mRightViewIndex );
			View child = mAdapter.getView( mRightViewIndex, mRecycleBin.get( viewType ).poll(), this );
			addAndMeasureChild( child, -1 );

			if ( firstChild ) {
				mChildWidth = child.getMeasuredWidth();
				mChildHeight = child.getMeasuredHeight();
				if ( mEdgesHeight == -1 ) {
					mEdgesHeight = mChildHeight;
				}
				mRightEdge = getWidth() + mChildWidth;
				mLeftEdge = -mChildWidth;
				mMaxX = Math.max( mItemCount * ( mChildWidth ) - ( getWidth() ) - ( mHideLastChild ? mChildWidth / 2 : 0 ), 0 );
				mMinX = 0;
				firstChild = false;

				//Log.d( LOG_TAG, "min: " + mMinX + ", max: " + mMaxX );
				//Log.d( LOG_TAG, "left: " + mLeftEdge + ", right: " + mRightEdge );

				if ( mMaxX == 0 ) {
					if ( mInverted ) rightEdge += getWidth() - ( mItemCount * mChildWidth );
					mLeftEdge = 0;
					mRightEdge = getWidth();
					// Log.d( "hv", "new right: " + rightEdge );
				}
			}

			int childTop = getPaddingTop();
			child.layout( rightEdge, childTop, rightEdge + mChildWidth, childTop + child.getMeasuredHeight() );
			rightEdge += mChildWidth;
			mRightViewIndex++;
		}
	}

	/**
	 * Removes the non visible items.
	 * 
	 * @param positionX
	 *           the position x
	 */
	private void removeNonVisibleItems( final int positionX ) {
		View child = getChildAt( 0 );

		// remove to left...
		while ( child != null && child.getRight() - positionX <= mLeftEdge ) {

			if ( null != mAdapter ) {
				int position = getPositionForView( child );
				int viewType = mAdapter.getItemViewType( position );
				mRecycleBin.get( viewType ).offer( child );
			}
			removeViewInLayout( child );
			mLeftViewIndex++;
			child = getChildAt( 0 );
		}

		// remove to right...
		child = getChildAt( getChildCount() - 1 );
		while ( child != null && child.getLeft() - positionX >= mRightEdge ) {

			if ( null != mAdapter ) {
				int position = getPositionForView( child );
				int viewType = mAdapter.getItemViewType( position );
				mRecycleBin.get( viewType ).offer( child );
			}

			removeViewInLayout( child );
			mRightViewIndex--;
			child = getChildAt( getChildCount() - 1 );
		}
	}

	private float mTestDragX, mTestDragY;
	private boolean mCanCheckDrag;
	private boolean mWasFlinging;
	private WeakReference<View> mOriginalDragItem;

	@Override
	public boolean onDown( MotionEvent event ) {
		return true;
	}

	@Override
	public boolean onScroll( MotionEvent e1, MotionEvent e2, float distanceX, float distanceY ) {
		return true;
	}

	@Override
	public boolean onFling( MotionEvent event0, MotionEvent event1, float velocityX, float velocityY ) {
		if ( mMaxX == 0 ) return false;
		mCanCheckDrag = false;
		mWasFlinging = true;
		mFlingRunnable.startUsingVelocity( mCurrentX, (int) -velocityX );
		return true;
	}

	@Override
	public void onLongPress( MotionEvent e ) {
		if ( mWasFlinging ) return;

		OnItemLongClickListener listener = getOnItemLongClickListener();
		if ( null != listener ) {

			if ( !mFlingRunnable.isFinished() ) return;

			int i = getChildAtPosition( e.getX(), e.getY() );
			if ( i > -1 ) {
				View child = getChildAt( i );
				fireLongPress( child, mLeftViewIndex + 1 + i, mAdapter.getItemId( mLeftViewIndex + 1 + i ) );
			}
		}
	}

	private int getChildAtPosition( float x, float y ) {
		Rect viewRect = new Rect();

		for ( int i = 0; i < getChildCount(); i++ ) {
			View child = getChildAt( i );
			int left = child.getLeft();
			int right = child.getRight();
			int top = child.getTop();
			int bottom = child.getBottom();
			viewRect.set( left, top, right, bottom );
			viewRect.offset( -mCurrentX, 0 );

			if ( viewRect.contains( (int) x, (int) y ) ) {
				return i;
			}
		}
		return -1;
	}

	private boolean fireLongPress( View item, int position, long id ) {
		if ( getOnItemLongClickListener().onItemLongClick( HorizontalFixedListView.this, item, position, id ) ) {
			performHapticFeedback( HapticFeedbackConstants.LONG_PRESS );
			return true;
		}
		return false;
	}
	
	private boolean fireItemDragStart( View item, int position, long id ) {
		
		mCanCheckDrag = false;
		mIsBeingDragged = false;
		
		if ( mItemDragListener.onItemStartDrag( HorizontalFixedListView.this, item, position, id ) ) {
			performHapticFeedback( HapticFeedbackConstants.LONG_PRESS );
			mIsDragging = true;
			return true;
		}
		return false;
	}
	
	public void setIsDragging( boolean value ) {
		logger.info( "setIsDragging: " + value );
		mIsDragging = value;
	}

	private int getItemIndex( View view ) {
		final int total = getChildCount();
		for ( int i = 0; i < total; i++ ) {
			if ( view == getChildAt( i ) ) {
				return i;
			}
		}
		return -1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.GestureDetector.OnGestureListener#onShowPress(android.view.MotionEvent)
	 */
	@Override
	public void onShowPress( MotionEvent arg0 ) {}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.GestureDetector.OnGestureListener#onSingleTapUp(android.view.MotionEvent)
	 */
	@Override
	public boolean onSingleTapUp( MotionEvent arg0 ) {
		logger.error( "onSingleTapUp" );
		return false;
	}

	private boolean mIsDragging = false;
	private boolean mIsBeingDragged = false;
	private int mActivePointerId = -1;
	private int mLastMotionX;
	private float mLastMotionX2;
	private VelocityTracker mVelocityTracker;
	private static final int INVALID_POINTER = -1;
	private int mOverscrollDistance;
	private int mMinimumVelocity;
	private int mMaximumVelocity;

	private void initOrResetVelocityTracker() {
		if ( mVelocityTracker == null ) {
			mVelocityTracker = VelocityTracker.obtain();
		} else {
			mVelocityTracker.clear();
		}
	}

	private void initVelocityTrackerIfNotExists() {
		if ( mVelocityTracker == null ) {
			mVelocityTracker = VelocityTracker.obtain();
		}
	}

	private void recycleVelocityTracker() {
		if ( mVelocityTracker != null ) {
			mVelocityTracker.recycle();
			mVelocityTracker = null;
		}
	}

	@Override
	public void requestDisallowInterceptTouchEvent( boolean disallowIntercept ) {
		if ( disallowIntercept ) {
			recycleVelocityTracker();
		}
		super.requestDisallowInterceptTouchEvent( disallowIntercept );
	}

	@Override
	public boolean onInterceptTouchEvent( MotionEvent ev ) {

		if( mIsDragging ) return false;
		
		final int action = ev.getAction();
		mGesture.onTouchEvent( ev );

		/*
		 * Shortcut the most recurring case: the user is in the dragging state and he is moving his finger. We want to intercept this
		 * motion.
		 */
		if ( action == MotionEvent.ACTION_MOVE ) {
			if( mIsBeingDragged )
				return true;
			
		}

		switch ( action & MotionEvent.ACTION_MASK ) {
			case MotionEvent.ACTION_MOVE: {
				/*
				 * mIsBeingDragged == false, otherwise the shortcut would have caught it. Check whether the user has moved far enough
				 * from his original down touch.
				 */
				final int activePointerId = mActivePointerId;
				if ( activePointerId == INVALID_POINTER ) {
					// If we don't have a valid id, the touch down wasn't on content.
					break;
				}

				final int pointerIndex = ev.findPointerIndex( activePointerId );
				final int x = (int) ev.getX( pointerIndex );
				final int y = (int) ev.getY( pointerIndex );
				final int xDiff = Math.abs( x - mLastMotionX );
				mLastMotionX2 = x;
				
				if( checkDrag( x, y ) ){
					return false;
				}

				if ( xDiff > mTouchSlop ) {
					
					mIsBeingDragged = true;
					mLastMotionX = x;
					initVelocityTrackerIfNotExists();
					mVelocityTracker.addMovement( ev );
					final ViewParent parent = getParent();
					if ( parent != null ) {
						parent.requestDisallowInterceptTouchEvent( true );
					}
				}
				break;
			}

			case MotionEvent.ACTION_DOWN: {

				final int x = (int) ev.getX();
				final int y = (int) ev.getY();

				mTestDragX = x;
				mTestDragY = y;

				/*
				 * Remember location of down touch. ACTION_DOWN always refers to pointer index 0.
				 */
				mLastMotionX = x;
				mLastMotionX2 = x;
				mActivePointerId = ev.getPointerId( 0 );

				initOrResetVelocityTracker();
				mVelocityTracker.addMovement( ev );

				/*
				 * If being flinged and user touches the screen, initiate drag; otherwise don't. mScroller.isFinished should be false
				 * when being flinged.
				 */
				mIsBeingDragged = !mFlingRunnable.isFinished();

				mWasFlinging = !mFlingRunnable.isFinished();
				mFlingRunnable.stop( false );
				mCanCheckDrag = isLongClickable() && getDragScrollEnabled() && ( mItemDragListener != null );

				if ( mCanCheckDrag ) {
					int i = getChildAtPosition( x, y );
					if ( i > -1 ) {
						mOriginalDragItem = new WeakReference<View>( getChildAt( i ) );
					}
				}
				break;
			}

			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP:
				/* Release the drag */
				mIsBeingDragged = false;
				mActivePointerId = INVALID_POINTER;
				recycleVelocityTracker();

				if ( mFlingRunnable.springBack( mCurrentX, 0, mMinX, mMaxX, 0, 0 ) ) {
					postInvalidate();
				}

				mCanCheckDrag = false;
				break;

			case MotionEvent.ACTION_POINTER_UP:
				onSecondaryPointerUp( ev );
				break;
		}

		return mIsBeingDragged;
	}

	@Override
	public boolean onTouchEvent( MotionEvent ev ) {
		
		initVelocityTrackerIfNotExists();
		mVelocityTracker.addMovement( ev );

		final int action = ev.getAction();

		switch ( action & MotionEvent.ACTION_MASK ) {

			case MotionEvent.ACTION_DOWN: { // DOWN
				
				if ( getChildCount() == 0 ) {
					return false;
				}

				if ( ( mIsBeingDragged = !mFlingRunnable.isFinished() ) ) {
					final ViewParent parent = getParent();
					if ( parent != null ) {
						parent.requestDisallowInterceptTouchEvent( true );
					}
				}

				/*
				 * If being flinged and user touches, stop the fling. isFinished will be false if being flinged.
				 */
				if ( !mFlingRunnable.isFinished() ) {
					// mScroller.abortAnimation();
					mFlingRunnable.stop( false );
				}

				// Remember where the motion event started
				mTestDragX = ev.getX();
				mTestDragY = ev.getY();
				mLastMotionX2 = mLastMotionX = (int) ev.getX();
				mActivePointerId = ev.getPointerId( 0 );
				break;
			}

			case MotionEvent.ACTION_MOVE: {
				// MOVE
				final int activePointerIndex = ev.findPointerIndex( mActivePointerId );
				final int x = (int) ev.getX( activePointerIndex );
				final int y = (int) ev.getY( activePointerIndex );
				int deltaX = mLastMotionX - x;
				if ( !mIsBeingDragged && Math.abs( deltaX ) > mTouchSlop ) {
					final ViewParent parent = getParent();
					if ( parent != null ) {
						parent.requestDisallowInterceptTouchEvent( true );
					}
					mIsBeingDragged = true;
					if ( deltaX > 0 ) {
						deltaX -= mTouchSlop;
					} else {
						deltaX += mTouchSlop;
					}
				}
				
				
				// first check if we can drag the item
				if( checkDrag( x, y ) ){
					return false;
				}
				
				if ( mIsBeingDragged ) {
					// Scroll to follow the motion event
					mLastMotionX = x;
					
					final float deltaX2 = mLastMotionX2 - x;
					final int oldX = getScrollX();
					final int range = mMaxX - mMinX;
					final int overscrollMode = mOverScrollMode;
					final boolean canOverscroll = overscrollMode == OVER_SCROLL_ALWAYS
							|| ( overscrollMode == OVER_SCROLL_IF_CONTENT_SCROLLS && range > 0 );

					if ( overScrollingBy( deltaX, 0, mCurrentX, 0, range, 0, 0, mOverscrollDistance, true ) ) {
						mVelocityTracker.clear();
					}

					if ( canOverscroll && mEdgeGlowLeft != null ) {
						final int pulledToX = oldX + deltaX;
						if ( pulledToX < mMinX ) {
							float overscroll = ( (float) -deltaX2 * 1.5f ) / getWidth();
							mEdgeGlowLeft.onPull( overscroll );
							if ( !mEdgeGlowRight.isFinished() ) {
								mEdgeGlowRight.onRelease();
							}
						} else if ( pulledToX > mMaxX ) {
							float overscroll = ( (float) deltaX2 * 1.5f ) / getWidth();
							mEdgeGlowRight.onPull( overscroll );
							if ( !mEdgeGlowLeft.isFinished() ) {
								mEdgeGlowLeft.onRelease();
							}
						}
						if ( mEdgeGlowLeft != null && ( !mEdgeGlowLeft.isFinished() || !mEdgeGlowRight.isFinished() ) ) {
							postInvalidate();
						}
					}

				}
				break;
			}

			case MotionEvent.ACTION_UP: {
				
				if ( mIsBeingDragged ) {
					final VelocityTracker velocityTracker = mVelocityTracker;
					velocityTracker.computeCurrentVelocity( 1000, mMaximumVelocity );

					final float velocityY = velocityTracker.getYVelocity();
					final float velocityX = velocityTracker.getXVelocity();

					if ( getChildCount() > 0 ) {
						if ( ( Math.abs( velocityX ) > mMinimumVelocity ) ) {
							onFling( ev, null, velocityX, velocityY );
						} else {
							if ( mFlingRunnable.springBack( mCurrentX, 0, mMinX, mMaxX, 0, 0 ) ) {
								postInvalidate();
							}
						}
					}

					mActivePointerId = INVALID_POINTER;
					endDrag();

					mCanCheckDrag = false;
					if ( mFlingRunnable.isFinished() ) {
						scrollIntoSlots();
					}
				}
				break;
			}

			case MotionEvent.ACTION_CANCEL: {
				if ( mIsBeingDragged && getChildCount() > 0 ) {
					if ( mFlingRunnable.springBack( mCurrentX, 0, mMinX, mMaxX, 0, 0 ) ) {
						postInvalidate();
					}
					mActivePointerId = INVALID_POINTER;
					endDrag();
				}
				break;
			}

			case MotionEvent.ACTION_POINTER_UP: {
				onSecondaryPointerUp( ev );
				mTestDragX = mLastMotionX2 = mLastMotionX = (int) ev.getX( ev.findPointerIndex( mActivePointerId ) );
				mTestDragY = ev.getY( ev.findPointerIndex( mActivePointerId ) );
				break;
			}
		}
		return true;
	}

	private void onSecondaryPointerUp( MotionEvent ev ) {
		final int pointerIndex = ( ev.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK ) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
		final int pointerId = ev.getPointerId( pointerIndex );
		if ( pointerId == mActivePointerId ) {
			final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
			mTestDragX = mLastMotionX2 = mLastMotionX = (int) ev.getX( newPointerIndex );
			mTestDragY = ev.getY( newPointerIndex );
			mActivePointerId = ev.getPointerId( newPointerIndex );
			if ( mVelocityTracker != null ) {
				mVelocityTracker.clear();
			}
		}
	}
	
	/**
	 * Check if the movement will fire a drag start event
	 * @param x
	 * @param y
	 * @return
	 */
	private boolean checkDrag( int x, int y ) {
		
		if ( mCanCheckDrag && !mIsDragging ) {
			
			float dx = Math.abs( x - mTestDragX );
			
			if ( dx > mDragTolerance ) {
				mCanCheckDrag = false;
			} else {
				float dy = Math.abs( y - mTestDragY );
				if ( dy > ((double)mDragTolerance*1.5) ) {
					
					if ( mOriginalDragItem != null && mAdapter != null ) {
						
						View view = mOriginalDragItem.get();
						int position = getItemIndex( view );
						if ( null != view && position > -1 ) {
							getParent().requestDisallowInterceptTouchEvent( false );
							if ( mItemDragListener != null ) {
								fireItemDragStart( view, mLeftViewIndex + 1 + position, mAdapter.getItemId( mLeftViewIndex + 1 + position ) );
								return true;
							}
						}
					}
					mCanCheckDrag = false;
				}
			}
		}
		return false;
	}

	private void endDrag() {
		mIsBeingDragged = false;
		recycleVelocityTracker();

		if ( mEdgeGlowLeft != null ) {
			mEdgeGlowLeft.onRelease();
			mEdgeGlowRight.onRelease();
		}
	}

	/**
	 * Scroll the view with standard behavior for scrolling beyond the normal content boundaries. Views that call this method should
	 * override {@link #onOverScrolled(int, int, boolean, boolean)} to respond to the results of an over-scroll operation.
	 * 
	 * Views can use this method to handle any touch or fling-based scrolling.
	 * 
	 * @param deltaX
	 *           Change in X in pixels
	 * @param deltaY
	 *           Change in Y in pixels
	 * @param scrollX
	 *           Current X scroll value in pixels before applying deltaX
	 * @param scrollY
	 *           Current Y scroll value in pixels before applying deltaY
	 * @param scrollRangeX
	 *           Maximum content scroll range along the X axis
	 * @param scrollRangeY
	 *           Maximum content scroll range along the Y axis
	 * @param maxOverScrollX
	 *           Number of pixels to overscroll by in either direction along the X axis.
	 * @param maxOverScrollY
	 *           Number of pixels to overscroll by in either direction along the Y axis.
	 * @param isTouchEvent
	 *           true if this scroll operation is the result of a touch event.
	 * @return true if scrolling was clamped to an over-scroll boundary along either axis, false otherwise.
	 */
	protected boolean overScrollingBy( int deltaX, int deltaY, int scrollX, int scrollY, int scrollRangeX, int scrollRangeY,
			int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent ) {

		final int overScrollMode = mOverScrollMode;
		final boolean toLeft = deltaX > 0;
		final boolean overScrollHorizontal = overScrollMode == OVER_SCROLL_ALWAYS;

		int newScrollX = scrollX + deltaX;
		if ( !overScrollHorizontal ) {
			maxOverScrollX = 0;
		}

		// Clamp values if at the limits and record
		final int left = mMinX - maxOverScrollX;
		final int right = mMaxX + maxOverScrollX;

		boolean clampedX = false;
		if ( newScrollX > right && toLeft ) {
			newScrollX = right;
			deltaX = mMaxX - scrollX;
			clampedX = true;
		} else if ( newScrollX < left && !toLeft ) {
			newScrollX = left;
			deltaX = mMinX - scrollX;
			clampedX = true;
		}

		onScrolling( newScrollX, deltaX, clampedX );
		return clampedX;
	}

	public boolean onScrolling( int scrollX, int deltaX, boolean clampedX ) {
		if ( mAdapter == null ) return true;
		if ( mMaxX == 0 ) return true;

		if ( !mFlingRunnable.isFinished() ) {
			mCurrentX = getScrollX();
			if ( clampedX ) {
				mFlingRunnable.springBack( scrollX, 0, mMinX, mMaxX, 0, 0 );
			}
		} else {
			trackMotionScroll( scrollX );
		}

		return true;
	}

	@Override
	public void computeScroll() {

		if ( mFlingRunnable.computeScrollOffset() ) {
			int oldX = mCurrentX;
			int x = mFlingRunnable.getCurrX();

			if ( oldX != x ) {
				final int range = getScrollRange();
				final int overscrollMode = mOverScrollMode;
				final boolean canOverscroll = overscrollMode == OVER_SCROLL_ALWAYS
						|| ( overscrollMode == OVER_SCROLL_IF_CONTENT_SCROLLS && range > 0 );

				overScrollingBy( x - oldX, 0, oldX, 0, range, 0, mOverscrollDistance, 0, false );

				if ( canOverscroll && mEdgeGlowLeft != null ) {
					if ( x < 0 && oldX >= 0 ) {
						mEdgeGlowLeft.onAbsorb( (int) mFlingRunnable.getCurrVelocity() );
					} else if ( x > range && oldX <= range ) {
						mEdgeGlowRight.onAbsorb( (int) mFlingRunnable.getCurrVelocity() );
					}
				}
			}
			postInvalidate();
		}
	}

	int getScrollRange() {
		if ( getChildCount() > 0 ) {
			return mMaxX - mMinX;
		}
		return 0;
	}

	/** The m animation duration. */
	int mAnimationDuration = 400;

	/** The m child height. */
	int mMaxX, mMinX, mChildWidth, mChildHeight;

	boolean mHideLastChild;

	boolean mInverted;

	/** The m should stop fling. */
	boolean mShouldStopFling;

	/** The m to left. */
	boolean mToLeft;

	/** The m current x. */
	int mCurrentX = 0;

	/** The m old x. */
	int mOldX = 0;

	/** The m touch slop. */
	int mTouchSlop;

	int mEdgesHeight = -1;

	int mEdgesGravityY = Gravity.CENTER;

	@Override
	public void scrollIntoSlots() {
		if ( !mFlingRunnable.isFinished() ) {
			return;
		}

		// boolean greater_enough = mItemCount * ( mChildWidth ) > getWidth();

		if ( mCurrentX > mMaxX || mCurrentX < mMinX ) {
			if ( mCurrentX > mMaxX ) {
				if ( mMaxX < 0 ) {
					mFlingRunnable.startUsingDistance( mCurrentX, mMinX - mCurrentX );
				} else {
					mFlingRunnable.startUsingDistance( mCurrentX, mMaxX - mCurrentX );
				}
				return;
			} else {
				mFlingRunnable.startUsingDistance( mCurrentX, mMinX - mCurrentX );
				return;
			}
		}
		onFinishedMovement();
	}

	/**
	 * On finished movement.
	 */
	protected void onFinishedMovement() {
		logger.info( "onFinishedMovement" );
	}

	/** The m gesture listener. */
	private OnGestureListener mGestureListener = new GestureDetector.SimpleOnGestureListener() {

		@Override
		public boolean onDoubleTap( MotionEvent e ) {
			return false;
		};
		
		public boolean onSingleTapUp(MotionEvent e) {
			logger.error( "onSingleTapUp" );
			return onItemClick( e );
		};

		@Override
		public boolean onDown( MotionEvent e ) {
			return false;
			// return HorizontalFixedListView.this.onDown( e );
		};

		@Override
		public boolean onFling( MotionEvent e1, MotionEvent e2, float velocityX, float velocityY ) {
			return false;
			// return HorizontalFixedListView.this.onFling( e1, e2, velocityX, velocityY );
		};

		@Override
		public void onLongPress( MotionEvent e ) {
			HorizontalFixedListView.this.onLongPress( e );
		};

		@Override
		public boolean onScroll( MotionEvent e1, MotionEvent e2, float distanceX, float distanceY ) {
			return false;
			// return HorizontalFixedListView.this.onScroll( e1, e2, distanceX, distanceY );
		};

		@Override
		public void onShowPress( MotionEvent e ) {};

		@Override
		public boolean onSingleTapConfirmed( MotionEvent e ) {
			logger.error( "onSingleTapConfirmed" );
			return true;
		}
		
		private boolean onItemClick( MotionEvent ev ){
			if ( !mFlingRunnable.isFinished() || mWasFlinging ) return false;

			Rect viewRect = new Rect();

			for ( int i = 0; i < getChildCount(); i++ ) {
				View child = getChildAt( i );
				int left = child.getLeft();
				int right = child.getRight();
				int top = child.getTop();
				int bottom = child.getBottom();
				viewRect.set( left, top, right, bottom );
				viewRect.offset( -mCurrentX, 0 );

				if ( viewRect.contains( (int) ev.getX(), (int) ev.getY() ) ) {
					if ( mOnItemClicked != null ) {
						playSoundEffect( SoundEffectConstants.CLICK );
						mOnItemClicked.onItemClick( HorizontalFixedListView.this, child, mLeftViewIndex + 1 + i,
								mAdapter.getItemId( mLeftViewIndex + 1 + i ) );
					}
					if ( mOnItemSelected != null ) {
						mOnItemSelected.onItemSelected( HorizontalFixedListView.this, child, mLeftViewIndex + 1 + i,
								mAdapter.getItemId( mLeftViewIndex + 1 + i ) );
					}
					break;
				}
			}
			return true;
		}
	};

	public View getChild( MotionEvent e ) {
		Rect viewRect = new Rect();
		for ( int i = 0; i < getChildCount(); i++ ) {
			View child = getChildAt( i );
			int left = child.getLeft();
			int right = child.getRight();
			int top = child.getTop();
			int bottom = child.getBottom();
			viewRect.set( left, top, right, bottom );
			viewRect.offset( -mCurrentX, 0 );

			if ( viewRect.contains( (int) e.getX(), (int) e.getY() ) ) {
				return child;
			}
		}
		return null;
	}

	@Override
	public int getMinX() {
		return mMinX;
	}

	@Override
	public int getMaxX() {
		return mMaxX;
	}

	public void setDragTolerance( int value ) {
		mDragTolerance = value;
	}
}
