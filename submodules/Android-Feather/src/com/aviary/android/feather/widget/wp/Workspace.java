/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.aviary.android.feather.widget.wp;

import java.util.ArrayList;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.Adapter;
import android.widget.LinearLayout;
import android.widget.Scroller;
import com.aviary.android.feather.R;
import com.aviary.android.feather.library.log.LoggerFactory;
import com.aviary.android.feather.library.log.LoggerFactory.Logger;
import com.aviary.android.feather.library.log.LoggerFactory.LoggerType;

// TODO: Auto-generated Javadoc
/**
 * The Class Workspace.
 */
public class Workspace extends ViewGroup {

	/** The Constant INVALID_SCREEN. */
	private static final int INVALID_SCREEN = -1;

	/** The Constant OVER_SCROLL_NEVER. */
	public static final int OVER_SCROLL_NEVER = 0;

	/** The Constant OVER_SCROLL_ALWAYS. */
	public static final int OVER_SCROLL_ALWAYS = 1;

	/** The Constant OVER_SCROLL_IF_CONTENT_SCROLLS. */
	public static final int OVER_SCROLL_IF_CONTENT_SCROLLS = 2;

	/** The velocity at which a fling gesture will cause us to snap to the next screen. */
	private static final int SNAP_VELOCITY = 600;

	/** The m default screen. */
	private int mDefaultScreen;

	/** The m padding bottom. */
	private int mPaddingLeft, mPaddingTop, mPaddingRight, mPaddingBottom;

	/** The m first layout. */
	private boolean mFirstLayout = true;

	/** The m current screen. */
	private int mCurrentScreen;

	/** The m next screen. */
	private int mNextScreen = INVALID_SCREEN;

	/** The m old selected position. */
	private int mOldSelectedPosition = INVALID_SCREEN;

	/** The m scroller. */
	private Scroller mScroller;

	/** The m velocity tracker. */
	private VelocityTracker mVelocityTracker;

	/** The m last motion x. */
	private float mLastMotionX;

	/** The m last motion x2. */
	private float mLastMotionX2;

	/** The m last motion y. */
	private float mLastMotionY;

	/** The Constant TOUCH_STATE_REST. */
	private final static int TOUCH_STATE_REST = 0;

	/** The Constant TOUCH_STATE_SCROLLING. */
	private final static int TOUCH_STATE_SCROLLING = 1;

	/** The m touch state. */
	private int mTouchState = TOUCH_STATE_REST;

	/** The m allow long press. */
	private boolean mAllowLongPress = true;

	/** The m touch slop. */
	private int mTouchSlop;

	/** The m maximum velocity. */
	private int mMaximumVelocity;

	/** The Constant INVALID_POINTER. */
	private static final int INVALID_POINTER = -1;

	/** The m active pointer id. */
	private int mActivePointerId = INVALID_POINTER;

	/** The m indicator. */
	private WorkspaceIndicator mIndicator;

	/** The Constant NANOTIME_DIV. */
	private static final float NANOTIME_DIV = 1000000000.0f;

	/** The Constant SMOOTHING_SPEED. */
	private static final float SMOOTHING_SPEED = 0.75f;

	/** The Constant SMOOTHING_CONSTANT. */
	private static final float SMOOTHING_CONSTANT = (float) ( 0.016 / Math.log( SMOOTHING_SPEED ) );

	/** The Constant BASELINE_FLING_VELOCITY. */
	private static final float BASELINE_FLING_VELOCITY = 2500.f;

	/** The Constant FLING_VELOCITY_INFLUENCE. */
	private static final float FLING_VELOCITY_INFLUENCE = 0.4f;
	
	/** The m smoothing time. */
	private float mSmoothingTime;

	/** The m touch x. */
	private float mTouchX;

	/** The m scroll interpolator. */
	private Interpolator mScrollInterpolator;

	/** The m adapter. */
	protected Adapter mAdapter;

	/** The m observer. */
	protected DataSetObserver mObserver;

	/** The m data changed. */
	protected boolean mDataChanged;

	/** The m first position. */
	protected int mFirstPosition;

	/** The m item count. */
	protected int mItemCount = 0;

	/** The m item type count. */
	protected int mItemTypeCount = 1;

	/** The m recycler. */
	protected RecycleBin mRecycler;

	/** The m height measure spec. */
	private int mHeightMeasureSpec;

	/** The m width measure spec. */
	private int mWidthMeasureSpec;

	/** The m edge glow left. */
	private EdgeGlow mEdgeGlowLeft;

	/** The m edge glow right. */
	private EdgeGlow mEdgeGlowRight;

	/** The m over scroll mode. */
	private int mOverScrollMode;

	/** The m allow child selection. */
	private boolean mAllowChildSelection = true;

	private boolean mCacheEnabled = false;

	/** The logger. */
	private Logger logger;

	/**
	 * The listener interface for receiving onPageChange events. The class that is interested in processing a onPageChange event
	 * implements this interface, and the object created with that class is registered with a component using the component's
	 * <code>addOnPageChangeListener<code> method. When
	 * the onPageChange event occurs, that object's appropriate
	 * method is invoked.
	 * 
	 * @see OnPageChangeEvent
	 */
	public interface OnPageChangeListener {

		/**
		 * On page changed.
		 * 
		 * @param which
		 *           the which
		 */
		void onPageChanged( int which, int old );
	}

	/** The m on page change listener. */
	private OnPageChangeListener mOnPageChangeListener;

	/**
	 * Sets the on page change listener.
	 * 
	 * @param listener
	 *           the new on page change listener
	 */
	public void setOnPageChangeListener( OnPageChangeListener listener ) {
		mOnPageChangeListener = listener;
	}

	/**
	 * The Class WorkspaceOvershootInterpolator.
	 */
	private static class WorkspaceOvershootInterpolator implements Interpolator {

		/** The Constant DEFAULT_TENSION. */
		private static final float DEFAULT_TENSION = 1.0f;

		/** The m tension. */
		private float mTension;

		/**
		 * Instantiates a new workspace overshoot interpolator.
		 */
		public WorkspaceOvershootInterpolator() {
			mTension = DEFAULT_TENSION;
		}

		/**
		 * Sets the distance.
		 * 
		 * @param distance
		 *           the new distance
		 */
		public void setDistance( int distance ) {
			mTension = distance > 0 ? DEFAULT_TENSION / distance : DEFAULT_TENSION;
		}

		/**
		 * Disable settle.
		 */
		public void disableSettle() {
			mTension = 0.f;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.animation.TimeInterpolator#getInterpolation(float)
		 */
		@Override
		public float getInterpolation( float t ) {
			t -= 1.0f;
			return t * t * ( ( mTension + 1 ) * t + mTension ) + 1.0f;
		}
	}

	/**
	 * Instantiates a new workspace.
	 * 
	 * @param context
	 *           the context
	 * @param attrs
	 *           the attrs
	 */
	public Workspace( Context context, AttributeSet attrs ) {
		this( context, attrs, 0 );
		initWorkspace( context, attrs, 0 );
	}

	/**
	 * Instantiates a new workspace.
	 * 
	 * @param context
	 *           the context
	 * @param attrs
	 *           the attrs
	 * @param defStyle
	 *           the def style
	 */
	public Workspace( Context context, AttributeSet attrs, int defStyle ) {
		super( context, attrs, defStyle );
		initWorkspace( context, attrs, defStyle );
	}

	/**
	 * Inits the workspace.
	 * 
	 * @param context
	 *           the context
	 * @param attrs
	 *           the attrs
	 * @param defStyle
	 *           the def style
	 */
	private void initWorkspace( Context context, AttributeSet attrs, int defStyle ) {
		TypedArray a = context.obtainStyledAttributes( attrs, R.styleable.Workspace, defStyle, 0 );
		mDefaultScreen = a.getInt( R.styleable.Workspace_defaultScreen, 0 );
		a.recycle();

		logger = LoggerFactory.getLogger( "Workspace", LoggerType.ConsoleLoggerType );
		setHapticFeedbackEnabled( false );

		mScrollInterpolator = new DecelerateInterpolator( 1.0f );
		mScroller = new Scroller( context, mScrollInterpolator );
		mCurrentScreen = mDefaultScreen;

		final ViewConfiguration configuration = ViewConfiguration.get( getContext() );
		mTouchSlop = configuration.getScaledTouchSlop();
		mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();

		mPaddingTop = getPaddingTop();
		mPaddingBottom = getPaddingBottom();
		mPaddingLeft = getPaddingLeft();
		mPaddingRight = getPaddingRight();

		int overscrollMode = a.getInt( R.styleable.Workspace_overscroll, 0 );
		setOverScroll( overscrollMode );
	}

	/**
	 * Sets the over scroll.
	 * 
	 * @param mode
	 *           the new over scroll
	 */
	public void setOverScroll( int mode ) {
		if ( mode != OVER_SCROLL_NEVER ) {
			if ( mEdgeGlowLeft == null ) {
				final Resources res = getContext().getResources();
				final Drawable edge = res.getDrawable( R.drawable.feather_overscroll_edge );
				final Drawable glow = res.getDrawable( R.drawable.feather_overscroll_glow );
				mEdgeGlowLeft = new EdgeGlow( edge, glow );
				mEdgeGlowRight = new EdgeGlow( edge, glow );
				mEdgeGlowLeft.setColorFilter( 0xFF454545, Mode.MULTIPLY );
			}
		} else {
			mEdgeGlowLeft = null;
			mEdgeGlowRight = null;
		}
		mOverScrollMode = mode;
	}

	/**
	 * Gets the over scroll.
	 * 
	 * @return the over scroll
	 */
	public int getOverScroll() {
		return mOverScrollMode;
	}

	/**
	 * Sets the allow child selection.
	 * 
	 * @param value
	 *           the new allow child selection
	 */
	public void setAllowChildSelection( boolean value ) {
		mAllowChildSelection = value;
	}

	/**
	 * Gets the adapter.
	 * 
	 * @return the adapter
	 */
	public Adapter getAdapter() {
		return mAdapter;
	}

	/**
	 * Sets the adapter.
	 * 
	 * @param adapter
	 *           the new adapter
	 */
	public void setAdapter( Adapter adapter ) {

		if ( mAdapter != null ) {
			mAdapter.unregisterDataSetObserver( mObserver );
			mAdapter = null;
		}

		mAdapter = adapter;
		resetList();

		if ( mAdapter != null ) {
			mObserver = new WorkspaceDataSetObserver();
			mAdapter.registerDataSetObserver( mObserver );
			mItemTypeCount = adapter.getViewTypeCount();
			mItemCount = adapter.getCount();
			mRecycler = new RecycleBin( mItemTypeCount, 10 );
		} else {
			mItemCount = 0;
		}

		mDataChanged = true;
		requestLayout();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.ViewGroup#addView(android.view.View, int, android.view.ViewGroup.LayoutParams)
	 */
	@Override
	public void addView( View child, int index, LayoutParams params ) {
		if ( !( child instanceof CellLayout ) ) {
			throw new IllegalArgumentException( "A Workspace can only have CellLayout children." );
		}
		super.addView( child, index, params );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.ViewGroup#addView(android.view.View)
	 */
	@Override
	public void addView( View child ) {
		if ( !( child instanceof CellLayout ) ) {
			throw new IllegalArgumentException( "A Workspace can only have CellLayout children." );
		}
		super.addView( child );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.ViewGroup#addView(android.view.View, int)
	 */
	@Override
	public void addView( View child, int index ) {
		if ( !( child instanceof CellLayout ) ) {
			throw new IllegalArgumentException( "A Workspace can only have CellLayout children." );
		}
		super.addView( child, index );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.ViewGroup#addView(android.view.View, int, int)
	 */
	@Override
	public void addView( View child, int width, int height ) {
		if ( !( child instanceof CellLayout ) ) {
			throw new IllegalArgumentException( "A Workspace can only have CellLayout children." );
		}
		super.addView( child, width, height );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.ViewGroup#addView(android.view.View, android.view.ViewGroup.LayoutParams)
	 */
	@Override
	public void addView( View child, LayoutParams params ) {
		if ( !( child instanceof CellLayout ) ) {
			throw new IllegalArgumentException( "A Workspace can only have CellLayout children." );
		}
		super.addView( child, params );
	}

	/**
	 * Checks if is default screen showing.
	 * 
	 * @return true, if is default screen showing
	 */
	boolean isDefaultScreenShowing() {
		return mCurrentScreen == mDefaultScreen;
	}

	/**
	 * Returns the index of the currently displayed screen.
	 * 
	 * @return The index of the currently displayed screen.
	 */
	public int getCurrentScreen() {
		return mCurrentScreen;
	}

	/**
	 * Gets the total pages.
	 * 
	 * @return the total pages
	 */
	public int getTotalPages() {
		return mItemCount;
	}

	/**
	 * Sets the current screen.
	 * 
	 * @param currentScreen
	 *           the new current screen
	 */
	void setCurrentScreen( int currentScreen ) {
		if ( !mScroller.isFinished() ) mScroller.abortAnimation();
		mCurrentScreen = Math.max( 0, Math.min( currentScreen, mItemCount - 1 ) );
		if ( mIndicator != null ) mIndicator.setLevel( mCurrentScreen, mItemCount );
		scrollTo( mCurrentScreen * getWidth(), 0 );
		invalidate();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View#scrollTo(int, int)
	 */
	@Override
	public void scrollTo( int x, int y ) {
		super.scrollTo( x, y );
		mTouchX = x;
		mSmoothingTime = System.nanoTime() / NANOTIME_DIV;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View#computeScroll()
	 */
	@Override
	public void computeScroll() {

		if ( mScroller.computeScrollOffset() ) {
			mTouchX = mScroller.getCurrX();
			float mScrollX = mTouchX;
			mSmoothingTime = System.nanoTime() / NANOTIME_DIV;
			float mScrollY = mScroller.getCurrY();
			scrollTo( (int) mScrollX, (int) mScrollY );
			postInvalidate();
		} else if ( mNextScreen != INVALID_SCREEN ) {
			int which = Math.max( 0, Math.min( mNextScreen, mItemCount - 1 ) );
			onFinishedAnimation( which );
		} else if ( mTouchState == TOUCH_STATE_SCROLLING ) {
			final float now = System.nanoTime() / NANOTIME_DIV;
			final float e = (float) Math.exp( ( now - mSmoothingTime ) / SMOOTHING_CONSTANT );
			final float dx = mTouchX - getScrollX();
			float mScrollX = getScrollX() + ( dx * e );
			scrollTo( (int) mScrollX, 0 );
			mSmoothingTime = now;

			// Keep generating points as long as we're more than 1px away from the target
			if ( dx > 1.f || dx < -1.f ) {
				postInvalidate();
			}
		}
	}

	/** The m old selected child. */
	private View mOldSelectedChild;

	/**
	 * On finished animation.
	 * 
	 * @param newScreen
	 *           the new screen
	 */
	private void onFinishedAnimation( int newScreen ) {

		logger.log( "onFinishedAnimation: " + newScreen );

		final int previousScreen = mCurrentScreen;

		final boolean toLeft = newScreen > mCurrentScreen;
		final boolean toRight = newScreen < mCurrentScreen;
		final boolean changed = newScreen != mCurrentScreen;

		mCurrentScreen = newScreen;
		if ( mIndicator != null ) mIndicator.setLevel( mCurrentScreen, mItemCount );
		mNextScreen = INVALID_SCREEN;

		fillToGalleryRight();
		fillToGalleryLeft();

		if ( toLeft ) {
			detachOffScreenChildren( true );
		} else if ( toRight ) {
			detachOffScreenChildren( false );
		}

		if ( changed || mItemCount == 1 || true ) {

			View child = getChildAt( mCurrentScreen - mFirstPosition );

			if ( null != child ) {
				if ( mAllowChildSelection ) {

					if ( null != mOldSelectedChild ) {
						mOldSelectedChild.setSelected( false );
						mOldSelectedChild = null;
					}

					child.setSelected( true );
					mOldSelectedChild = child;
				}
				// int index = indexOfChild( child ) + mFirstPosition;
				child.requestFocus();
			}
		}

		clearChildrenCache();

		if ( mOnPageChangeListener != null ) {
			post( new Runnable() {

				@Override
				public void run() {
					mOnPageChangeListener.onPageChanged( mCurrentScreen, previousScreen );
				}
			} );
		}

		postUpdateIndicator( mCurrentScreen, mItemCount );
	}

	/**
	 * Detach off screen children.
	 * 
	 * @param toLeft
	 *           the to left
	 */
	private void detachOffScreenChildren( boolean toLeft ) {
		int numChildren = getChildCount();
		int start = 0;
		int count = 0;

		if ( toLeft ) {
			final int galleryLeft = mPaddingLeft + getScreenScrollPositionX( mCurrentScreen - 1 );;
			for ( int i = 0; i < numChildren; i++ ) {
				final View child = getChildAt( i );
				if ( child.getRight() >= galleryLeft ) {
					break;
				} else {
					count++;
					mRecycler.add( mAdapter.getItemViewType( i + mFirstPosition ), child );
				}
			}
		} else {
			final int galleryRight = getTotalWidth() + getScreenScrollPositionX( mCurrentScreen + 1 );
			for ( int i = numChildren - 1; i >= 0; i-- ) {
				final View child = getChildAt( i );
				if ( child.getLeft() <= galleryRight ) {
					break;
				} else {
					start = i;
					count++;
					mRecycler.add( mAdapter.getItemViewType( i + mFirstPosition ), child );
				}
			}
		}

		detachViewsFromParent( start, count );

		if ( toLeft && count > 0 ) {
			mFirstPosition += count;
		}
	}

	private Matrix mEdgeMatrix = new Matrix();

	/**
	 * Draw edges.
	 * 
	 * @param canvas
	 *           the canvas
	 */
	private void drawEdges( Canvas canvas ) {

		if ( mEdgeGlowLeft != null ) {
			if ( !mEdgeGlowLeft.isFinished() ) {
				final int restoreCount = canvas.save();
				final int height = getHeight();

				mEdgeMatrix.reset();
				mEdgeMatrix.postRotate( -90 );
				mEdgeMatrix.postTranslate( 0, height );
				canvas.concat( mEdgeMatrix );

				mEdgeGlowLeft.setSize( height, height / 5 );

				if ( mEdgeGlowLeft.draw( canvas ) ) {
					invalidate();
				}
				canvas.restoreToCount( restoreCount );
			}

			if ( !mEdgeGlowRight.isFinished() ) {
				final int restoreCount = canvas.save();
				final int width = getWidth();
				final int height = getHeight();

				mEdgeMatrix.reset();
				mEdgeMatrix.postRotate( 90 );
				mEdgeMatrix.postTranslate( getScrollX() + width, 0 );
				canvas.concat( mEdgeMatrix );

				mEdgeGlowRight.setSize( height, height / 5 );

				if ( mEdgeGlowRight.draw( canvas ) ) {
					invalidate();
				}
				canvas.restoreToCount( restoreCount );
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.ViewGroup#dispatchDraw(android.graphics.Canvas)
	 */
	@Override
	protected void dispatchDraw( Canvas canvas ) {
		boolean restore = false;
		int restoreCount = 0;

		if ( mItemCount < 1 ) return;
		if ( mCurrentScreen < 0 ) return;

		boolean fastDraw = mTouchState != TOUCH_STATE_SCROLLING && mNextScreen == INVALID_SCREEN;
		// If we are not scrolling or flinging, draw only the current screen
		if ( fastDraw ) {
			try {
				drawChild( canvas, getChildAt( mCurrentScreen - mFirstPosition ), getDrawingTime() );
			} catch ( RuntimeException e ) {
				logger.error( e.getMessage() );
			}
		} else {
			final long drawingTime = getDrawingTime();
			final float scrollPos = (float) getScrollX() / getTotalWidth();
			final int leftScreen = (int) scrollPos;
			final int rightScreen = leftScreen + 1;
			if ( leftScreen >= 0 ) {
				try {
					drawChild( canvas, getChildAt( leftScreen - mFirstPosition ), drawingTime );
				} catch ( RuntimeException e ) {
					logger.error( e.getMessage() );
				}
			}
			if ( scrollPos != leftScreen && rightScreen < mItemCount ) {
				try {
					drawChild( canvas, getChildAt( rightScreen - mFirstPosition ), drawingTime );
				} catch ( RuntimeException e ) {
					logger.error( e.getMessage() );
				}
			}
		}

		// let's draw the edges only if we have more than 1 page
		if ( mEdgeGlowLeft != null && mItemCount > 1 ) {
			drawEdges( canvas );
		}

		if ( restore ) {
			canvas.restoreToCount( restoreCount );
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View#onAttachedToWindow()
	 */
	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		computeScroll();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View#onMeasure(int, int)
	 */
	@Override
	protected void onMeasure( int widthMeasureSpec, int heightMeasureSpec ) {
		super.onMeasure( widthMeasureSpec, heightMeasureSpec );

		mWidthMeasureSpec = widthMeasureSpec;
		mHeightMeasureSpec = heightMeasureSpec;

		if ( mDataChanged ) {
			mFirstLayout = true;
			resetList();
			handleDataChanged();
		}

		boolean needsMeasuring = true;

		if ( mNextScreen > INVALID_SCREEN && mAdapter != null && mNextScreen < mItemCount ) {

		}

		final int width = MeasureSpec.getSize( widthMeasureSpec );
		// final int height = MeasureSpec.getSize( heightMeasureSpec );
		final int widthMode = MeasureSpec.getMode( widthMeasureSpec );

		if ( widthMode != MeasureSpec.EXACTLY ) {
			throw new IllegalStateException( "Workspace can only be used in EXACTLY mode." );
		}

		final int heightMode = MeasureSpec.getMode( heightMeasureSpec );

		if ( heightMode != MeasureSpec.EXACTLY ) {
			throw new IllegalStateException( "Workspace can only be used in EXACTLY mode." );
		}

		// The children are given the same width and height as the workspace
		final int count = mItemCount;

		if ( !needsMeasuring ) {
			for ( int i = 0; i < count; i++ ) {
				getChildAt( i ).measure( widthMeasureSpec, heightMeasureSpec );
			}
		}

		if ( mItemCount < 1 ) {
			mCurrentScreen = INVALID_SCREEN;
			mFirstLayout = true;
		}

		if ( mFirstLayout ) {
			setHorizontalScrollBarEnabled( false );

			if ( mCurrentScreen > INVALID_SCREEN )
				scrollTo( mCurrentScreen * width, 0 );
			else
				scrollTo( 0, 0 );
			setHorizontalScrollBarEnabled( true );
			mFirstLayout = false;
		}

	}

	/**
	 * Handle data changed.
	 */
	private void handleDataChanged() {

		if ( mItemCount > 0 )
			setNextSelectedPositionInt( 0 );
		else
			setNextSelectedPositionInt( -1 );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.ViewGroup#onLayout(boolean, int, int, int, int)
	 */
	@Override
	protected void onLayout( boolean changed, int left, int top, int right, int bottom ) {

		if ( changed ) {
			if ( !mFirstLayout ) {
				mDataChanged = true;
				measure( mWidthMeasureSpec, mHeightMeasureSpec );
			}
		}
		layout( 0, false );
	}

	/**
	 * Layout.
	 * 
	 * @param delta
	 *           the delta
	 * @param animate
	 *           the animate
	 */
	void layout( int delta, boolean animate ) {

		int childrenLeft = mPaddingLeft;
		int childrenWidth = ( getRight() - getLeft() ) - ( mPaddingLeft + mPaddingRight );

		if ( mItemCount == 0 ) {
			return;
		}

		if ( mNextScreen > INVALID_SCREEN ) {
			setSelectedPositionInt( mNextScreen );
		}

		if ( mDataChanged ) {
			mFirstPosition = mCurrentScreen;
			View sel = makeAndAddView( mCurrentScreen, 0, 0, true );
			int selectedOffset = childrenLeft + ( childrenWidth / 2 ) - ( sel.getWidth() / 2 );
			sel.offsetLeftAndRight( selectedOffset );
			fillToGalleryRight();
			fillToGalleryLeft();
			checkSelectionChanged();
		}

		mDataChanged = false;
		setNextSelectedPositionInt( mCurrentScreen );
	}

	/**
	 * Check selection changed.
	 */
	void checkSelectionChanged() {
		if ( ( mCurrentScreen != mOldSelectedPosition ) ) {
			// selectionChanged();
			mOldSelectedPosition = mCurrentScreen;
		}
	}

	/**
	 * Make and add view.
	 * 
	 * @param position
	 *           the position
	 * @param offset
	 *           the offset
	 * @param x
	 *           the x
	 * @param fromLeft
	 *           the from left
	 * @return the view
	 */
	private View makeAndAddView( int position, int offset, int x, boolean fromLeft ) {

		View child;

		if ( !mDataChanged ) {
			child = mRecycler.remove( mAdapter.getItemViewType( position ) );
			if ( child != null ) {
				child = mAdapter.getView( position, child, this );
				setUpChild( child, offset, x, fromLeft );
				return child;
			}
		}

		// Nothing found in the recycler -- ask the adapter for a view
		child = mAdapter.getView( position, null, this );

		// Position the view
		setUpChild( child, offset, x, fromLeft );
		logger.info( "adding view: " + child );
		return child;
	}

	/**
	 * Sets the up child.
	 * 
	 * @param child
	 *           the child
	 * @param offset
	 *           the offset
	 * @param x
	 *           the x
	 * @param fromLeft
	 *           the from left
	 */
	private void setUpChild( View child, int offset, int x, boolean fromLeft ) {

		// Respect layout params that are already in the view. Otherwise
		// make some up...
		LayoutParams lp = child.getLayoutParams();
		if ( lp == null ) {
			lp = (LayoutParams) generateDefaultLayoutParams();
		}

		addViewInLayout( child, fromLeft ? -1 : 0, lp );

		if ( mAllowChildSelection ) {
			// final boolean wantfocus = offset == 0;
			// child.setSelected( wantfocus );
			// if( wantfocus ){
			// child.requestFocus();
			// }
		}

		// Get measure specs
		int childHeightSpec = ViewGroup.getChildMeasureSpec( mHeightMeasureSpec, mPaddingTop + mPaddingBottom, lp.height );
		int childWidthSpec = ViewGroup.getChildMeasureSpec( mWidthMeasureSpec, mPaddingLeft + mPaddingRight, lp.width );

		// Measure child
		child.measure( childWidthSpec, childHeightSpec );

		int childLeft;
		int childRight;

		// Position vertically based on gravity setting
		int childTop = calculateTop( child, true );
		int childBottom = childTop + child.getMeasuredHeight();

		int width = child.getMeasuredWidth();
		if ( fromLeft ) {
			childLeft = x;
			childRight = childLeft + width;
		} else {
			childLeft = x - width;
			childRight = x;
		}

		child.layout( childLeft, childTop, childRight, childBottom );
	}

	/**
	 * Calculate top.
	 * 
	 * @param child
	 *           the child
	 * @param duringLayout
	 *           the during layout
	 * @return the int
	 */
	private int calculateTop( View child, boolean duringLayout ) {
		return mPaddingTop;
	}

	/**
	 * Gets the total width.
	 * 
	 * @return the total width
	 */
	private int getTotalWidth() {
		return getWidth();
	}

	/**
	 * Gets the screen scroll position x.
	 * 
	 * @param screen
	 *           the screen
	 * @return the screen scroll position x
	 */
	private int getScreenScrollPositionX( int screen ) {
		return ( screen * getTotalWidth() );
	}

	/**
	 * Fill to gallery right.
	 */
	private void fillToGalleryRight() {
		int itemSpacing = 0;
		int galleryRight = getScreenScrollPositionX( mCurrentScreen + 3 );
		int numChildren = getChildCount();
		int numItems = mItemCount;

		// Set state for initial iteration
		View prevIterationView = getChildAt( numChildren - 1 );
		int curPosition;
		int curLeftEdge;

		if ( prevIterationView != null ) {
			curPosition = mFirstPosition + numChildren;
			curLeftEdge = prevIterationView.getRight() + itemSpacing;
		} else {
			mFirstPosition = curPosition = mItemCount - 1;
			curLeftEdge = mPaddingLeft;
		}

		while ( curLeftEdge < galleryRight && curPosition < numItems ) {
			prevIterationView = makeAndAddView( curPosition, curPosition - mCurrentScreen, curLeftEdge, true );

			// Set state for next iteration
			curLeftEdge = prevIterationView.getRight() + itemSpacing;
			curPosition++;
		}
	}

	/**
	 * Fill to gallery left.
	 */
	private void fillToGalleryLeft() {
		int itemSpacing = 0;
		int galleryLeft = getScreenScrollPositionX( mCurrentScreen - 3 );

		// Set state for initial iteration
		View prevIterationView = getChildAt( 0 );
		int curPosition;
		int curRightEdge;

		if ( prevIterationView != null ) {
			curPosition = mFirstPosition - 1;
			curRightEdge = prevIterationView.getLeft() - itemSpacing;
		} else {
			// No children available!
			curPosition = 0;
			curRightEdge = getRight() - getLeft() - mPaddingRight;
		}

		while ( curRightEdge > galleryLeft && curPosition >= 0 ) {
			prevIterationView = makeAndAddView( curPosition, curPosition - mCurrentScreen, curRightEdge, false );

			// Remember some state
			mFirstPosition = curPosition;

			// Set state for next iteration
			curRightEdge = prevIterationView.getLeft() - itemSpacing;
			curPosition--;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.ViewGroup#generateDefaultLayoutParams()
	 */
	@Override
	protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
		return new LinearLayout.LayoutParams( ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT );
	}

	/**
	 * Recycle all views.
	 */
	void recycleAllViews() {
		/*
		 * final int childCount = getChildCount();
		 * 
		 * for ( int i = 0; i < childCount; i++ ) { View v = getChildAt( i );
		 * 
		 * if( mRecycler != null ) mRecycler.add( v ); }
		 */
	}

	/**
	 * Reset list.
	 */
	void resetList() {

		recycleAllViews();

		while ( getChildCount() > 0 ) {
			View view = getChildAt( 0 );
			detachViewFromParent( view );
			removeDetachedView( view, false );
		}

		// detachAllViewsFromParent();

		if ( mRecycler != null ) mRecycler.clear();

		mOldSelectedPosition = INVALID_SCREEN;
		setSelectedPositionInt( INVALID_SCREEN );
		setNextSelectedPositionInt( INVALID_SCREEN );
		postInvalidate();
	}

	/**
	 * Sets the next selected position int.
	 * 
	 * @param screen
	 *           the new next selected position int
	 */
	private void setNextSelectedPositionInt( int screen ) {
		mNextScreen = screen;
	}

	/**
	 * Sets the selected position int.
	 * 
	 * @param screen
	 *           the new selected position int
	 */
	private void setSelectedPositionInt( int screen ) {
		mCurrentScreen = screen;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.ViewGroup#requestChildRectangleOnScreen(android.view.View, android.graphics.Rect, boolean)
	 */
	@Override
	public boolean requestChildRectangleOnScreen( View child, Rect rectangle, boolean immediate ) {
		int screen = indexOfChild( child ) + mFirstPosition;

		if ( screen != mCurrentScreen || !mScroller.isFinished() ) {
			snapToScreen( screen );
			return true;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.ViewGroup#onRequestFocusInDescendants(int, android.graphics.Rect)
	 */
	@Override
	protected boolean onRequestFocusInDescendants( int direction, Rect previouslyFocusedRect ) {

		if ( mItemCount < 1 ) return false;

		if ( isEnabled() ) {
			int focusableScreen;
			if ( mNextScreen != INVALID_SCREEN ) {
				focusableScreen = mNextScreen;
			} else {
				focusableScreen = mCurrentScreen;
			}

			if ( focusableScreen != INVALID_SCREEN ) {
				View child = getChildAt( focusableScreen );
				if ( null != child ) child.requestFocus( direction, previouslyFocusedRect );
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.ViewGroup#dispatchUnhandledMove(android.view.View, int)
	 */
	@Override
	public boolean dispatchUnhandledMove( View focused, int direction ) {

		if ( direction == View.FOCUS_LEFT ) {
			if ( getCurrentScreen() > 0 ) {
				snapToScreen( getCurrentScreen() - 1 );
				return true;
			}
		} else if ( direction == View.FOCUS_RIGHT ) {
			if ( getCurrentScreen() < mItemCount - 1 ) {
				snapToScreen( getCurrentScreen() + 1 );
				return true;
			}
		}
		return super.dispatchUnhandledMove( focused, direction );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View#setEnabled(boolean)
	 */
	@Override
	public void setEnabled( boolean enabled ) {
		super.setEnabled( enabled );

		for ( int i = 0; i < getChildCount(); i++ ) {
			getChildAt( i ).setEnabled( enabled );
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.ViewGroup#addFocusables(java.util.ArrayList, int, int)
	 */
	@Override
	public void addFocusables( ArrayList<View> views, int direction, int focusableMode ) {

		if ( isEnabled() ) {
			View child = getChildAt( mCurrentScreen );
			if ( null != child ) {
				child.addFocusables( views, direction );
			}

			if ( direction == View.FOCUS_LEFT ) {
				if ( mCurrentScreen > 0 ) {
					child = getChildAt( mCurrentScreen - 1 );
					if ( null != child ) {
						child.addFocusables( views, direction );
					}
				}
			} else if ( direction == View.FOCUS_RIGHT ) {
				if ( mCurrentScreen < mItemCount - 1 ) {
					child = getChildAt( mCurrentScreen + 1 );
					if ( null != child ) {
						child.addFocusables( views, direction );
					}
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.ViewGroup#onInterceptTouchEvent(android.view.MotionEvent)
	 */
	@Override
	public boolean onInterceptTouchEvent( MotionEvent ev ) {

		final int action = ev.getAction();

		if ( !isEnabled() ) {
			return false; // We don't want the events. Let them fall through to the all apps view.
		}

		if ( ( action == MotionEvent.ACTION_MOVE ) && ( mTouchState != TOUCH_STATE_REST ) ) {
			return true;
		}

		acquireVelocityTrackerAndAddMovement( ev );

		switch ( action & MotionEvent.ACTION_MASK ) {
			case MotionEvent.ACTION_MOVE: {

				/*
				 * Locally do absolute value. mLastMotionX is set to the y value of the down event.
				 */
				final int pointerIndex = ev.findPointerIndex( mActivePointerId );

				if ( pointerIndex < 0 ) {
					// invalid pointer
					return true;
				}

				final float x = ev.getX( pointerIndex );
				final float y = ev.getY( pointerIndex );
				final int xDiff = (int) Math.abs( x - mLastMotionX );
				final int yDiff = (int) Math.abs( y - mLastMotionY );

				final int touchSlop = mTouchSlop;
				boolean xMoved = xDiff > touchSlop;
				boolean yMoved = yDiff > touchSlop;
				mLastMotionX2 = x;

				if ( xMoved || yMoved ) {

					if ( xMoved ) {
						// Scroll if the user moved far enough along the X axis
						mTouchState = TOUCH_STATE_SCROLLING;
						mLastMotionX = x;
						mTouchX = getScrollX();
						mSmoothingTime = System.nanoTime() / NANOTIME_DIV;
						enableChildrenCache( mCurrentScreen - 1, mCurrentScreen + 1 );
					}

				}
				break;
			}

			case MotionEvent.ACTION_DOWN: {
				final float x = ev.getX();
				final float y = ev.getY();
				// Remember location of down touch
				mLastMotionX = x;
				mLastMotionX2 = x;
				mLastMotionY = y;
				mActivePointerId = ev.getPointerId( 0 );
				mAllowLongPress = true;

				mTouchState = mScroller.isFinished() ? TOUCH_STATE_REST : TOUCH_STATE_SCROLLING;
				break;
			}

			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP:
				// Release the drag
				clearChildrenCache();
				mTouchState = TOUCH_STATE_REST;
				mActivePointerId = INVALID_POINTER;
				mAllowLongPress = false;
				releaseVelocityTracker();
				break;

			case MotionEvent.ACTION_POINTER_UP:
				onSecondaryPointerUp( ev );
				break;
		}

		/*
		 * The only time we want to intercept motion events is if we are in the drag mode.
		 */
		return mTouchState != TOUCH_STATE_REST;
	}

	/**
	 * On secondary pointer up.
	 * 
	 * @param ev
	 *           the ev
	 */
	private void onSecondaryPointerUp( MotionEvent ev ) {
		final int pointerIndex = ( ev.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK ) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
		final int pointerId = ev.getPointerId( pointerIndex );
		if ( pointerId == mActivePointerId ) {
			// This was our active pointer going up. Choose a new
			// active pointer and adjust accordingly.
			// TODO: Make this decision more intelligent.
			final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
			mLastMotionX = ev.getX( newPointerIndex );
			mLastMotionX2 = ev.getX( newPointerIndex );
			mLastMotionY = ev.getY( newPointerIndex );
			mActivePointerId = ev.getPointerId( newPointerIndex );
			if ( mVelocityTracker != null ) {
				mVelocityTracker.clear();
			}
		}
	}

	/**
	 * If one of our descendant views decides that it could be focused now, only pass that along if it's on the current screen.
	 * 
	 * This happens when live folders requery, and if they're off screen, they end up calling requestFocus, which pulls it on screen.
	 * 
	 * @param focused
	 *           the focused
	 */
	@Override
	public void focusableViewAvailable( View focused ) {
		View current = getChildAt( mCurrentScreen );
		View v = focused;
		while ( true ) {
			if ( v == current ) {
				super.focusableViewAvailable( focused );
				return;
			}
			if ( v == this ) {
				return;
			}
			ViewParent parent = v.getParent();
			if ( parent instanceof View ) {
				v = (View) v.getParent();
			} else {
				return;
			}
		}
	}

	/**
	 * Enable children cache.
	 * 
	 * @param fromScreen
	 *           the from screen
	 * @param toScreen
	 *           the to screen
	 */
	public void enableChildrenCache( int fromScreen, int toScreen ) {

		if ( !mCacheEnabled ) return;

		if ( fromScreen > toScreen ) {
			final int temp = fromScreen;
			fromScreen = toScreen;
			toScreen = temp;
		}

		final int count = getChildCount();

		fromScreen = Math.max( fromScreen, 0 );
		toScreen = Math.min( toScreen, count - 1 );

		for ( int i = fromScreen; i <= toScreen; i++ ) {
			final CellLayout layout = (CellLayout) getChildAt( i );
			layout.setChildrenDrawnWithCacheEnabled( true );
			layout.setChildrenDrawingCacheEnabled( true );
		}
	}

	/**
	 * Clear children cache.
	 */
	public void clearChildrenCache() {

		if ( !mCacheEnabled ) return;

		final int count = getChildCount();
		for ( int i = 0; i < count; i++ ) {
			final CellLayout layout = (CellLayout) getChildAt( i );
			layout.setChildrenDrawnWithCacheEnabled( false );
			layout.setChildrenDrawingCacheEnabled( false );
		}
	}

	public void setCacheEnabled( boolean value ) {
		mCacheEnabled = value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View#onTouchEvent(android.view.MotionEvent)
	 */
	@Override
	public boolean onTouchEvent( MotionEvent ev ) {

		final int action = ev.getAction();

		if ( !isEnabled() ) {
			if ( !mScroller.isFinished() ) {
				mScroller.abortAnimation();
			}
			snapToScreen( mCurrentScreen );
			return false; // We don't want the events. Let them fall through to the all apps view.
		}

		acquireVelocityTrackerAndAddMovement( ev );

		switch ( action & MotionEvent.ACTION_MASK ) {
			case MotionEvent.ACTION_DOWN:
				/*
				 * If being flinged and user touches, stop the fling. isFinished will be false if being flinged.
				 */

				if ( !mScroller.isFinished() ) {
					mScroller.abortAnimation();
				}

				// Remember where the motion event started
				mLastMotionX = ev.getX();
				mLastMotionX2 = ev.getX();
				mActivePointerId = ev.getPointerId( 0 );
				if ( mTouchState == TOUCH_STATE_SCROLLING ) {
					enableChildrenCache( mCurrentScreen - 1, mCurrentScreen + 1 );
				}
				break;
			case MotionEvent.ACTION_MOVE:
				if ( mTouchState == TOUCH_STATE_SCROLLING ) {
					// Scroll to follow the motion event
					final int pointerIndex = ev.findPointerIndex( mActivePointerId );
					final float x = ev.getX( pointerIndex );
					final float deltaX = mLastMotionX - x;
					final float deltaX2 = mLastMotionX2 - x;
					final int mode = mOverScrollMode;

					mLastMotionX = x;

					// Log.d( "hv", "delta: " + deltaX );

					if ( deltaX < 0 ) {
						mTouchX += deltaX;
						mSmoothingTime = System.nanoTime() / NANOTIME_DIV;

						if ( mTouchX < 0 && mode != OVER_SCROLL_NEVER ) {
							mTouchX = mLastMotionX = 0;
							// mLastMotionX = x;

							if ( mEdgeGlowLeft != null && deltaX2 < 0 ) {
								float overscroll = ( (float) -deltaX2 * 1.5f ) / getWidth();
								mEdgeGlowLeft.onPull( overscroll );
								if ( !mEdgeGlowRight.isFinished() ) {
									mEdgeGlowRight.onRelease();
								}
							}
						}

						invalidate();

					} else if ( deltaX > 0 ) {
						final int totalWidth = getScreenScrollPositionX( mItemCount - 1 );
						final float availableToScroll = getScreenScrollPositionX( mItemCount ) - mTouchX;
						mSmoothingTime = System.nanoTime() / NANOTIME_DIV;

						mTouchX += Math.min( availableToScroll, deltaX );

						if ( availableToScroll <= getWidth() && mode != OVER_SCROLL_NEVER ) {
							mTouchX = mLastMotionX = totalWidth;
							// mLastMotionX = x;

							if ( mEdgeGlowLeft != null && deltaX2 > 0 ) {
								float overscroll = ( (float) deltaX2 * 1.5f ) / getWidth();
								mEdgeGlowRight.onPull( overscroll );
								if ( !mEdgeGlowLeft.isFinished() ) {
									mEdgeGlowLeft.onRelease();
								}
							}
						}
						invalidate();

					} else {
						awakenScrollBars();
					}
				}
				break;
			case MotionEvent.ACTION_UP:
				if ( mTouchState == TOUCH_STATE_SCROLLING ) {
					final VelocityTracker velocityTracker = mVelocityTracker;
					velocityTracker.computeCurrentVelocity( 1000, mMaximumVelocity );
					final int velocityX = (int) velocityTracker.getXVelocity( mActivePointerId );

					final int screenWidth = getWidth();
					final int whichScreen = ( getScrollX() + ( screenWidth / 2 ) ) / screenWidth;
					final float scrolledPos = (float) getScrollX() / screenWidth;

					if ( velocityX > SNAP_VELOCITY && mCurrentScreen > 0 ) {
						// Fling hard enough to move left.
						// Don't fling across more than one screen at a time.
						final int bound = scrolledPos < whichScreen ? mCurrentScreen - 1 : mCurrentScreen;
						snapToScreen( Math.min( whichScreen, bound ), velocityX, true );
					} else if ( velocityX < -SNAP_VELOCITY && mCurrentScreen < mItemCount - 1 ) {
						// Fling hard enough to move right
						// Don't fling across more than one screen at a time.
						final int bound = scrolledPos > whichScreen ? mCurrentScreen + 1 : mCurrentScreen;
						snapToScreen( Math.max( whichScreen, bound ), velocityX, true );
					} else {
						snapToScreen( whichScreen, 0, true );
					}

					if ( mEdgeGlowLeft != null ) {
						mEdgeGlowLeft.onRelease();
						mEdgeGlowRight.onRelease();
					}
				}
				mTouchState = TOUCH_STATE_REST;
				mActivePointerId = INVALID_POINTER;
				releaseVelocityTracker();
				break;
			case MotionEvent.ACTION_CANCEL:
				if ( mTouchState == TOUCH_STATE_SCROLLING ) {
					final int screenWidth = getWidth();
					final int whichScreen = ( getScrollX() + ( screenWidth / 2 ) ) / screenWidth;
					snapToScreen( whichScreen, 0, true );
				}
				mTouchState = TOUCH_STATE_REST;
				mActivePointerId = INVALID_POINTER;
				releaseVelocityTracker();

				if ( mEdgeGlowLeft != null ) {
					mEdgeGlowLeft.onRelease();
					mEdgeGlowRight.onRelease();
				}

				break;
			case MotionEvent.ACTION_POINTER_UP:
				onSecondaryPointerUp( ev );
				break;
		}

		return true;
	}

	/**
	 * Acquire velocity tracker and add movement.
	 * 
	 * @param ev
	 *           the ev
	 */
	private void acquireVelocityTrackerAndAddMovement( MotionEvent ev ) {
		if ( mVelocityTracker == null ) {
			mVelocityTracker = VelocityTracker.obtain();
		}
		mVelocityTracker.addMovement( ev );
	}

	/**
	 * Release velocity tracker.
	 */
	private void releaseVelocityTracker() {
		if ( mVelocityTracker != null ) {
			mVelocityTracker.recycle();
			mVelocityTracker = null;
		}
	}

	/**
	 * Snap to screen.
	 * 
	 * @param whichScreen
	 *           the which screen
	 */
	void snapToScreen( int whichScreen ) {
		snapToScreen( whichScreen, 0, false );
	}

	/**
	 * Snap to screen.
	 * 
	 * @param whichScreen
	 *           the which screen
	 * @param velocity
	 *           the velocity
	 * @param settle
	 *           the settle
	 */
	private void snapToScreen( int whichScreen, int velocity, boolean settle ) {

		whichScreen = Math.max( 0, Math.min( whichScreen, mItemCount - 1 ) );

		enableChildrenCache( mCurrentScreen, whichScreen );

		mNextScreen = whichScreen;

		View focusedChild = getFocusedChild();
		if ( focusedChild != null && whichScreen != mCurrentScreen && focusedChild == getChildAt( mCurrentScreen ) ) {
			focusedChild.clearFocus();
		}

		final int screenDelta = Math.max( 1, Math.abs( whichScreen - mCurrentScreen ) );
		final int newX = whichScreen * getWidth();
		final int delta = newX - getScrollX();
		int duration = ( screenDelta + 1 ) * 100;

		if ( !mScroller.isFinished() ) {
			mScroller.abortAnimation();
		}

		/*
		if ( mScrollInterpolator instanceof WorkspaceOvershootInterpolator ) {
			if ( settle ) {
				( (WorkspaceOvershootInterpolator) mScrollInterpolator ).setDistance( screenDelta );
			} else {
				( (WorkspaceOvershootInterpolator) mScrollInterpolator ).disableSettle();
			}
		}
		*/

		velocity = Math.abs( velocity );
		if ( velocity > 0 ) {
			duration += (duration / (velocity / BASELINE_FLING_VELOCITY)) * FLING_VELOCITY_INFLUENCE;
		} else {
			duration += 100;
		}

		mScroller.startScroll( getScrollX(), 0, delta, 0, duration );

		int mode = getOverScroll();

		if ( delta != 0 && ( mode == OVER_SCROLL_IF_CONTENT_SCROLLS ) ) {
			edgeReached( whichScreen, delta, velocity );
		}

		// postUpdateIndicator( mNextScreen, mItemCount );
		invalidate();
	}

	private void postUpdateIndicator( final int screen, final int count ) {
		getHandler().post( new Runnable() {

			@Override
			public void run() {
				if ( mIndicator != null ) mIndicator.setLevel( screen, count );
			}
		} );
	}

	/**
	 * Edge reached.
	 * 
	 * @param whichscreen
	 *           the whichscreen
	 * @param delta
	 *           the delta
	 * @param vel
	 *           the vel
	 */
	void edgeReached( int whichscreen, int delta, int vel ) {

		if ( whichscreen == 0 || whichscreen == ( mItemCount - 1 ) ) {
			if ( delta < 0 ) {
				mEdgeGlowLeft.onAbsorb( vel );
			} else {
				mEdgeGlowRight.onAbsorb( vel );
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View#onSaveInstanceState()
	 */
	@Override
	protected Parcelable onSaveInstanceState() {
		final SavedState state = new SavedState( super.onSaveInstanceState() );
		state.currentScreen = mCurrentScreen;
		return state;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View#onRestoreInstanceState(android.os.Parcelable)
	 */
	@Override
	protected void onRestoreInstanceState( Parcelable state ) {
		SavedState savedState = (SavedState) state;
		super.onRestoreInstanceState( savedState.getSuperState() );
		if ( savedState.currentScreen != -1 ) {
			mCurrentScreen = savedState.currentScreen;
		}
	}

	/**
	 * Scroll left.
	 */
	public void scrollLeft() {
		if ( mScroller.isFinished() ) {
			if ( mCurrentScreen > 0 ) snapToScreen( mCurrentScreen - 1 );
		} else {
			if ( mNextScreen > 0 ) snapToScreen( mNextScreen - 1 );
		}
	}

	/**
	 * Scroll right.
	 */
	public void scrollRight() {
		if ( mScroller.isFinished() ) {
			if ( mCurrentScreen < mItemCount - 1 ) snapToScreen( mCurrentScreen + 1 );
		} else {
			if ( mNextScreen < mItemCount - 1 ) snapToScreen( mNextScreen + 1 );
		}
	}

	/**
	 * Gets the screen for view.
	 * 
	 * @param v
	 *           the v
	 * @return the screen for view
	 */
	public int getScreenForView( View v ) {
		int result = -1;
		if ( v != null ) {
			ViewParent vp = v.getParent();
			int count = mItemCount;
			for ( int i = 0; i < count; i++ ) {
				if ( vp == getChildAt( i ) ) {
					return i;
				}
			}
		}
		return result;
	}

	/**
	 * Gets the view for tag.
	 * 
	 * @param tag
	 *           the tag
	 * @return the view for tag
	 */
	public View getViewForTag( Object tag ) {
		int screenCount = mItemCount;
		for ( int screen = 0; screen < screenCount; screen++ ) {
			CellLayout currentScreen = ( (CellLayout) getChildAt( screen ) );
			int count = currentScreen.getChildCount();
			for ( int i = 0; i < count; i++ ) {
				View child = currentScreen.getChildAt( i );
				if ( child.getTag() == tag ) {
					return child;
				}
			}
		}
		return null;
	}

	/**
	 * Allow long press.
	 * 
	 * @return True is long presses are still allowed for the current touch
	 */
	public boolean allowLongPress() {
		return mAllowLongPress;
	}

	/**
	 * Set true to allow long-press events to be triggered, usually checked by {@link Launcher} to accept or block dpad-initiated
	 * long-presses.
	 * 
	 * @param allowLongPress
	 *           the new allow long press
	 */
	public void setAllowLongPress( boolean allowLongPress ) {
		mAllowLongPress = allowLongPress;
	}

	/**
	 * Move to default screen.
	 * 
	 * @param animate
	 *           the animate
	 */
	void moveToDefaultScreen( boolean animate ) {
		if ( animate ) {
			snapToScreen( mDefaultScreen );
		} else {
			setCurrentScreen( mDefaultScreen );
		}
		getChildAt( mDefaultScreen ).requestFocus();
	}

	/**
	 * Sets the indicator.
	 * 
	 * @param indicator
	 *           the new indicator
	 */
	public void setIndicator( WorkspaceIndicator indicator ) {
		mIndicator = indicator;
		mIndicator.setLevel( mCurrentScreen, mItemCount );
	}

	/**
	 * The Class SavedState.
	 */
	public static class SavedState extends BaseSavedState {

		/** The current screen. */
		int currentScreen = -1;

		/**
		 * Instantiates a new saved state.
		 * 
		 * @param superState
		 *           the super state
		 */
		SavedState( Parcelable superState ) {
			super( superState );
		}

		/**
		 * Instantiates a new saved state.
		 * 
		 * @param in
		 *           the in
		 */
		private SavedState( Parcel in ) {
			super( in );
			currentScreen = in.readInt();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.view.AbsSavedState#writeToParcel(android.os.Parcel, int)
		 */
		@Override
		public void writeToParcel( Parcel out, int flags ) {
			super.writeToParcel( out, flags );
			out.writeInt( currentScreen );
		}

		/** The Constant CREATOR. */
		public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {

			@Override
			public SavedState createFromParcel( Parcel in ) {
				return new SavedState( in );
			}

			@Override
			public SavedState[] newArray( int size ) {
				return new SavedState[size];
			}
		};
	}

	/**
	 * An asynchronous update interface for receiving notifications about WorkspaceDataSet information as the WorkspaceDataSet is
	 * constructed.
	 */
	class WorkspaceDataSetObserver extends DataSetObserver {

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.database.DataSetObserver#onChanged()
		 */
		@Override
		public void onChanged() {
			super.onChanged();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.database.DataSetObserver#onInvalidated()
		 */
		@Override
		public void onInvalidated() {
			super.onInvalidated();
		}
	}

	/**
	 * The Class RecycleBin.
	 */
	class RecycleBin {

		/** The array. */
		protected View[][] array;

		/** The start. */
		protected int start[];

		/** The end. */
		protected int end[];

		/** The max size. */
		protected int maxSize;

		/** The full. */
		protected boolean full[];

		/**
		 * Instantiates a new recycle bin.
		 * 
		 * @param typeCount
		 *           the type count
		 * @param size
		 *           the size
		 */
		public RecycleBin( int typeCount, int size ) {
			maxSize = size;
			array = new View[typeCount][size];
			start = new int[typeCount];
			end = new int[typeCount];
			full = new boolean[typeCount];
		}

		/**
		 * Checks if is empty.
		 * 
		 * @param type
		 *           the type
		 * @return true, if is empty
		 */
		public boolean isEmpty( int type ) {
			return ( ( start[type] == end[type] ) && !full[type] );
		}

		/**
		 * Adds the.
		 * 
		 * @param type
		 *           the type
		 * @param o
		 *           the o
		 */
		public void add( int type, View o ) {
			if ( !full[type] ) array[type][start[type] = ( ++start[type] % array[type].length )] = o;
			if ( start[type] == end[type] ) full[type] = true;
		}

		/**
		 * Removes the.
		 * 
		 * @param type
		 *           the type
		 * @return the view
		 */
		public View remove( int type ) {
			if ( full[type] ) {
				full[type] = false;
			} else if ( isEmpty( type ) ) return null;
			return array[type][end[type] = ( ++end[type] % array[type].length )];
		}

		/**
		 * Clear.
		 */
		void clear() {
			int typeCount = array.length;

			for ( int i = 0; i < typeCount; i++ ) {
				while ( !isEmpty( i ) ) {
					final View view = remove( i );
					if ( view != null ) {
						removeDetachedView( view, true );
					}
				}
			}

			array = new View[typeCount][maxSize];
			start = new int[typeCount];
			end = new int[typeCount];
			full = new boolean[typeCount];
		}
	}

	/**
	 * Gets the screen at.
	 * 
	 * @param screen
	 *           the screen
	 * @return the screen at
	 */
	public View getScreenAt( int screen ) {
		return getChildAt( mCurrentScreen - mFirstPosition );
	}
}
