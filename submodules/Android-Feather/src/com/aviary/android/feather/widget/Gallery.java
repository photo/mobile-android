/*
 * Copyright (C) 2007 The Android Open Source Project
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

package com.aviary.android.feather.widget;

import android.R;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Transformation;
import com.aviary.android.feather.library.utils.ReflectionUtils;
import com.aviary.android.feather.library.utils.ReflectionUtils.ReflectionException;
import com.aviary.android.feather.widget.IFlingRunnable.FlingRunnableView;

// TODO: Auto-generated Javadoc
/**
 * A view that shows items in a center-locked, horizontally scrolling list.
 * <p>
 * The default values for the Gallery assume you will be using {@link android.R.styleable#Theme_galleryItemBackground} as the
 * background for each View given to the Gallery from the Adapter. If you are not doing this, you may need to adjust some Gallery
 * properties, such as the spacing.
 * <p>
 * Views given to the Gallery should use {@link Gallery.LayoutParams} as their layout parameters type.
 * 
 * <p>
 * See the <a href="{@docRoot}resources/tutorials/views/hello-gallery.html">Gallery tutorial</a>.
 * </p>
 * 
 * @attr ref android.R.styleable#Gallery_animationDuration
 * @attr ref android.R.styleable#Gallery_spacing
 * @attr ref android.R.styleable#Gallery_gravity
 */
public class Gallery extends AbsSpinner implements GestureDetector.OnGestureListener, FlingRunnableView, VibrationWidget {

	/**
	 * The listener interface for receiving onItemsScroll events. The class that is interested in processing a onItemsScroll event
	 * implements this interface, and the object created with that class is registered with a component using the component's
	 * <code>addOnItemsScrollListener<code> method. When
	 * the onItemsScroll event occurs, that object's appropriate
	 * method is invoked.
	 * 
	 * @see OnItemsScrollEvent
	 */
	public interface OnItemsScrollListener {

		/**
		 * On scroll started.
		 * 
		 * @param parent
		 *           the parent
		 * @param view
		 *           the view
		 * @param position
		 *           the position
		 * @param id
		 *           the id
		 */
		void onScrollStarted( AdapterView<?> parent, View view, int position, long id );

		/**
		 * On scroll.
		 * 
		 * @param parent
		 *           the parent
		 * @param view
		 *           the view
		 * @param position
		 *           the position
		 * @param id
		 *           the id
		 */
		void onScroll( AdapterView<?> parent, View view, int position, long id );

		/**
		 * On scroll finished.
		 * 
		 * @param parent
		 *           the parent
		 * @param view
		 *           the view
		 * @param position
		 *           the position
		 * @param id
		 *           the id
		 */
		void onScrollFinished( AdapterView<?> parent, View view, int position, long id );
	}

	/** The Constant TAG. */
	private static final String TAG = "gallery";

	/** The Constant MSG_VIBRATE. */
	private static final int MSG_VIBRATE = 1;

	/** Vibration. */
	Vibrator mVibrator;

	/** The m vibration handler. */
	static Handler mVibrationHandler;

	/** set child selected automatically. */
	private boolean mAutoSelectChild = false;

	/** The m items scroll listener. */
	private OnItemsScrollListener mItemsScrollListener = null;

	/**
	 * Duration in milliseconds from the start of a scroll during which we're unsure whether the user is scrolling or flinging.
	 */
	private static final int SCROLL_TO_FLING_UNCERTAINTY_TIMEOUT = 250;

	/**
	 * Horizontal spacing between items.
	 */
	private int mSpacing = 0;

	/**
	 * How long the transition animation should run when a child view changes position, measured in milliseconds.
	 */
	private int mAnimationDuration = 400;

	/**
	 * The alpha of items that are not selected.
	 */
	private float mUnselectedAlpha;

	/**
	 * Left most edge of a child seen so far during layout.
	 */
	private int mLeftMost;

	/**
	 * Right most edge of a child seen so far during layout.
	 */
	private int mRightMost;

	/** The m gravity. */
	private int mGravity;

	/**
	 * Helper for detecting touch gestures.
	 */
	private GestureDetector mGestureDetector;

	/**
	 * The position of the item that received the user's down touch.
	 */
	private int mDownTouchPosition;

	/**
	 * The view of the item that received the user's down touch.
	 */
	private View mDownTouchView;

	/**
	 * Executes the delta scrolls from a fling or scroll movement.
	 */
	private IFlingRunnable mFlingRunnable;

	/** The m auto scroll to center. */
	private boolean mAutoScrollToCenter = true;

	/** The m touch slop. */
	int mTouchSlop;

	/**
	 * Sets mSuppressSelectionChanged = false. This is used to set it to false in the future. It will also trigger a selection
	 * changed.
	 */
	private Runnable mDisableSuppressSelectionChangedRunnable = new Runnable() {

		@Override
		public void run() {
			mSuppressSelectionChanged = false;
			selectionChanged();
		}
	};

	/**
	 * When fling runnable runs, it resets this to false. Any method along the path until the end of its run() can set this to true
	 * to abort any remaining fling. For example, if we've reached either the leftmost or rightmost item, we will set this to true.
	 */
	@SuppressWarnings("unused")
	private boolean mShouldStopFling;

	/**
	 * The currently selected item's child.
	 */
	private View mSelectedChild;

	/**
	 * Whether to continuously callback on the item selected listener during a fling.
	 */
	private boolean mShouldCallbackDuringFling = false;

	/**
	 * Whether to callback when an item that is not selected is clicked.
	 */
	private boolean mShouldCallbackOnUnselectedItemClick = true;

	/**
	 * If true, do not callback to item selected listener.
	 */
	private boolean mSuppressSelectionChanged = true;

	/**
	 * If true, we have received the "invoke" (center or enter buttons) key down. This is checked before we action on the "invoke"
	 * key up, and is subsequently cleared.
	 */
	private boolean mReceivedInvokeKeyDown;

	/** The m context menu info. */
	private AdapterContextMenuInfo mContextMenuInfo;

	/**
	 * If true, this onScroll is the first for this user's drag (remember, a drag sends many onScrolls).
	 */
	private boolean mIsFirstScroll;

	/**
	 * If true, mFirstPosition is the position of the rightmost child, and the children are ordered right to left.
	 */
	private boolean mIsRtl = true;

	/** The m last motion value. */
	private int mLastMotionValue;

	/**
	 * Instantiates a new gallery.
	 * 
	 * @param context
	 *           the context
	 */
	public Gallery( Context context ) {
		this( context, null );
	}

	/**
	 * Instantiates a new gallery.
	 * 
	 * @param context
	 *           the context
	 * @param attrs
	 *           the attrs
	 */
	public Gallery( Context context, AttributeSet attrs ) {
		this( context, attrs, R.attr.galleryStyle );
	}

	/**
	 * Instantiates a new gallery.
	 * 
	 * @param context
	 *           the context
	 * @param attrs
	 *           the attrs
	 * @param defStyle
	 *           the def style
	 */
	public Gallery( Context context, AttributeSet attrs, int defStyle ) {
		super( context, attrs, defStyle );

		mGestureDetector = new GestureDetector( context, this );
		mGestureDetector.setIsLongpressEnabled( false );

		ViewConfiguration configuration = ViewConfiguration.get( context );
		mTouchSlop = configuration.getScaledTouchSlop();

		if ( android.os.Build.VERSION.SDK_INT > 8 ) {
			try {
				mFlingRunnable = (IFlingRunnable) ReflectionUtils.newInstance( "com.aviary.android.feather.widget.Fling9Runnable",
						new Class<?>[] { FlingRunnableView.class, int.class }, this, mAnimationDuration );
			} catch ( ReflectionException e ) {
				mFlingRunnable = new Fling8Runnable( this, mAnimationDuration );
			}
		} else {
			mFlingRunnable = new Fling8Runnable( this, mAnimationDuration );
		}

		Log.d( VIEW_LOG_TAG, "fling class: " + mFlingRunnable.getClass().getName() );

		try {
			mVibrator = (Vibrator) context.getSystemService( Context.VIBRATOR_SERVICE );
		} catch ( Exception e ) {
			Log.e( TAG, e.toString() );
		}

		if ( mVibrator != null ) {
			setVibrationEnabled( true );
		}
	}

	@Override
	public synchronized void setVibrationEnabled( boolean value ) {
		if ( !value ) {
			mVibrationHandler = null;
		} else {
			if ( null == mVibrationHandler ) {
				mVibrationHandler = new Handler() {

					@Override
					public void handleMessage( Message msg ) {
						super.handleMessage( msg );

						switch ( msg.what ) {
							case MSG_VIBRATE:
								try {
									mVibrator.vibrate( 10 );
								} catch ( SecurityException e ) {
									// missing VIBRATE permission
								}
						}
					}
				};
			}
		}
	}

	@Override
	@SuppressLint("HandlerLeak")
	public synchronized boolean getVibrationEnabled() {
		return mVibrationHandler != null;
	}

	/**
	 * Sets the on items scroll listener.
	 * 
	 * @param value
	 *           the new on items scroll listener
	 */
	public void setOnItemsScrollListener( OnItemsScrollListener value ) {
		mItemsScrollListener = value;
	}

	/**
	 * Sets the auto scroll to center.
	 * 
	 * @param value
	 *           the new auto scroll to center
	 */
	public void setAutoScrollToCenter( boolean value ) {
		mAutoScrollToCenter = value;
	}

	/**
	 * Whether or not to callback on any {@link #getOnItemSelectedListener()} while the items are being flinged. If false, only the
	 * final selected item will cause the callback. If true, all items between the first and the final will cause callbacks.
	 * 
	 * @param shouldCallback
	 *           Whether or not to callback on the listener while the items are being flinged.
	 */
	public void setCallbackDuringFling( boolean shouldCallback ) {
		mShouldCallbackDuringFling = shouldCallback;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aviary.android.feather.widget.AdapterView#onDetachedFromWindow()
	 */
	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		removeCallbacks( mScrollSelectionNotifier );
	}

	/**
	 * Whether or not to callback when an item that is not selected is clicked. If false, the item will become selected (and
	 * re-centered). If true, the {@link #getOnItemClickListener()} will get the callback.
	 * 
	 * @param shouldCallback
	 *           Whether or not to callback on the listener when a item that is not selected is clicked.
	 * @hide
	 */
	public void setCallbackOnUnselectedItemClick( boolean shouldCallback ) {
		mShouldCallbackOnUnselectedItemClick = shouldCallback;
	}

	/**
	 * Sets how long the transition animation should run when a child view changes position. Only relevant if animation is turned on.
	 * 
	 * @param animationDurationMillis
	 *           The duration of the transition, in milliseconds.
	 * 
	 * @attr ref android.R.styleable#Gallery_animationDuration
	 */
	public void setAnimationDuration( int animationDurationMillis ) {
		mAnimationDuration = animationDurationMillis;
	}

	/**
	 * Sets the spacing between items in a Gallery.
	 * 
	 * @param spacing
	 *           The spacing in pixels between items in the Gallery
	 * @attr ref android.R.styleable#Gallery_spacing
	 */
	public void setSpacing( int spacing ) {
		mSpacing = spacing;
	}

	/**
	 * Sets the alpha of items that are not selected in the Gallery.
	 * 
	 * @param unselectedAlpha
	 *           the alpha for the items that are not selected.
	 * 
	 * @attr ref android.R.styleable#Gallery_unselectedAlpha
	 */
	public void setUnselectedAlpha( float unselectedAlpha ) {
		mUnselectedAlpha = unselectedAlpha;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.ViewGroup#getChildStaticTransformation(android.view.View, android.view.animation.Transformation)
	 */
	@Override
	protected boolean getChildStaticTransformation( View child, Transformation t ) {

		t.clear();
		t.setAlpha( child == mSelectedChild ? 1.0f : mUnselectedAlpha );

		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View#computeHorizontalScrollExtent()
	 */
	@Override
	protected int computeHorizontalScrollExtent() {
		// Only 1 item is considered to be selected
		return 1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View#computeHorizontalScrollOffset()
	 */
	@Override
	protected int computeHorizontalScrollOffset() {
		return mSelectedPosition;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View#computeHorizontalScrollRange()
	 */
	@Override
	protected int computeHorizontalScrollRange() {
		// Scroll range is the same as the item count
		return mItemCount;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.ViewGroup#checkLayoutParams(android.view.ViewGroup.LayoutParams)
	 */
	@Override
	protected boolean checkLayoutParams( ViewGroup.LayoutParams p ) {
		return p instanceof LayoutParams;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.ViewGroup#generateLayoutParams(android.view.ViewGroup.LayoutParams)
	 */
	@Override
	protected ViewGroup.LayoutParams generateLayoutParams( ViewGroup.LayoutParams p ) {
		return new LayoutParams( p );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.ViewGroup#generateLayoutParams(android.util.AttributeSet)
	 */
	@Override
	public ViewGroup.LayoutParams generateLayoutParams( AttributeSet attrs ) {
		return new LayoutParams( getContext(), attrs );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aviary.android.feather.widget.AbsSpinner#generateDefaultLayoutParams()
	 */
	@Override
	protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
		/*
		 * Gallery expects Gallery.LayoutParams.
		 */
		return new Gallery.LayoutParams( ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aviary.android.feather.widget.AdapterView#onLayout(boolean, int, int, int, int)
	 */
	@Override
	protected void onLayout( boolean changed, int l, int t, int r, int b ) {
		super.onLayout( changed, l, t, r, b );

		/*
		 * Remember that we are in layout to prevent more layout request from being generated.
		 */
		mInLayout = true;
		layout( 0, false, changed );
		mInLayout = false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aviary.android.feather.widget.AbsSpinner#getChildHeight(android.view.View)
	 */
	@Override
	int getChildHeight( View child ) {
		return child.getMeasuredHeight();
	}

	/**
	 * Tracks a motion scroll. In reality, this is used to do just about any movement to items (touch scroll, arrow-key scroll, set
	 * an item as selected).
	 * 
	 * @param deltaX
	 *           Change in X from the previous event.
	 */
	@Override
	public void trackMotionScroll( int delta ) {

		int deltaX = mFlingRunnable.getLastFlingX() - delta;

		// Pretend that each frame of a fling scroll is a touch scroll
		if ( delta > 0 ) {
			mDownTouchPosition = mIsRtl ? ( mFirstPosition + getChildCount() - 1 ) : mFirstPosition;
			// Don't fling more than 1 screen
			delta = Math.min( getWidth() - mPaddingLeft - mPaddingRight - 1, delta );
		} else {
			mDownTouchPosition = mIsRtl ? mFirstPosition : ( mFirstPosition + getChildCount() - 1 );
			// Don't fling more than 1 screen
			delta = Math.max( -( getWidth() - mPaddingRight - mPaddingLeft - 1 ), delta );
		}

		if ( getChildCount() == 0 ) {
			return;
		}

		boolean toLeft = deltaX < 0;

		int limitedDeltaX = deltaX; // getLimitedMotionScrollAmount(toLeft, deltaX);
		int realDeltaX = getLimitedMotionScrollAmount( toLeft, deltaX );

		if ( realDeltaX != deltaX ) {
			// mFlingRunnable.springBack( realDeltaX );
			limitedDeltaX = realDeltaX;
		}

		if ( limitedDeltaX != deltaX ) {
			mFlingRunnable.endFling( false );
			if ( limitedDeltaX == 0 ) onFinishedMovement();
		}

		offsetChildrenLeftAndRight( limitedDeltaX );
		detachOffScreenChildren( toLeft );

		if ( toLeft ) {
			// If moved left, there will be empty space on the right
			fillToGalleryRight();
		} else {
			// Similarly, empty space on the left
			fillToGalleryLeft();
		}

		// Clear unused views
		// mRecycler.clear();
		// mRecyclerInvalidItems.clear();

		setSelectionToCenterChild();

		onScrollChanged( 0, 0, 0, 0 ); // dummy values, View's implementation does not use these.

		invalidate();
	}

	/**
	 * Gets the limited motion scroll amount.
	 * 
	 * @param motionToLeft
	 *           the motion to left
	 * @param deltaX
	 *           the delta x
	 * @return the limited motion scroll amount
	 */
	int getLimitedMotionScrollAmount( boolean motionToLeft, int deltaX ) {
		int extremeItemPosition = motionToLeft != mIsRtl ? mItemCount - 1 : 0;
		View extremeChild = getChildAt( extremeItemPosition - mFirstPosition );

		if ( extremeChild == null ) {
			return deltaX;
		}

		int extremeChildCenter = getCenterOfView( extremeChild )
				+ ( motionToLeft ? extremeChild.getWidth() / 2 : -extremeChild.getWidth() / 2 );
		int galleryCenter = getCenterOfGallery();

		if ( motionToLeft ) {
			if ( extremeChildCenter <= galleryCenter ) {
				// The extreme child is past his boundary point!
				return 0;
			}
		} else {
			if ( extremeChildCenter >= galleryCenter ) {
				// The extreme child is past his boundary point!
				return 0;
			}
		}

		int centerDifference = galleryCenter - extremeChildCenter;

		return motionToLeft ? Math.max( centerDifference, deltaX ) : Math.min( centerDifference, deltaX );
	}

	/**
	 * Gets the limited motion scroll amount2.
	 * 
	 * @param motionToLeft
	 *           the motion to left
	 * @param deltaX
	 *           the delta x
	 * @return the limited motion scroll amount2
	 */
	int getLimitedMotionScrollAmount2( boolean motionToLeft, int deltaX ) {
		int extremeItemPosition = motionToLeft != mIsRtl ? mItemCount - 1 : 0;
		View extremeChild = getChildAt( extremeItemPosition - mFirstPosition );

		if ( extremeChild == null ) {
			return deltaX;
		}

		int extremeChildCenter = getCenterOfView( extremeChild )
				+ ( motionToLeft ? extremeChild.getWidth() / 2 : -extremeChild.getWidth() / 2 );
		int galleryCenter = getCenterOfGallery();
		int centerDifference = galleryCenter - extremeChildCenter;
		return motionToLeft ? Math.max( centerDifference, deltaX ) : Math.min( centerDifference, deltaX );
	}

	/**
	 * Gets the over scroll delta.
	 * 
	 * @param motionToLeft
	 *           the motion to left
	 * @param deltaX
	 *           the delta x
	 * @return the over scroll delta
	 */
	int getOverScrollDelta( boolean motionToLeft, int deltaX ) {
		int extremeItemPosition = motionToLeft != mIsRtl ? mItemCount - 1 : 0;
		View extremeChild = getChildAt( extremeItemPosition - mFirstPosition );

		if ( extremeChild == null ) {
			return 0;
		}

		int extremeChildCenter = getCenterOfView( extremeChild );
		int galleryCenter = getCenterOfGallery();

		if ( motionToLeft ) {
			if ( extremeChildCenter < galleryCenter ) {
				return extremeChildCenter - galleryCenter;
			}
		} else {
			if ( extremeChildCenter > galleryCenter ) {
				return galleryCenter - extremeChildCenter;
			}
		}
		return 0;
	}

	protected void onOverScrolled( int scrollX, int scrollY, boolean clampedX, boolean clampedY ) {}

	/**
	 * Offset the horizontal location of all children of this view by the specified number of pixels.
	 * 
	 * @param offset
	 *           the number of pixels to offset
	 */
	private void offsetChildrenLeftAndRight( int offset ) {
		for ( int i = getChildCount() - 1; i >= 0; i-- ) {
			getChildAt( i ).offsetLeftAndRight( offset );
		}
	}

	/**
	 * Gets the center of gallery.
	 * 
	 * @return The center of this Gallery.
	 */
	private int getCenterOfGallery() {
		return ( getWidth() - mPaddingLeft - mPaddingRight ) / 2 + mPaddingLeft;
	}

	/**
	 * Gets the center of view.
	 * 
	 * @param view
	 *           the view
	 * @return The center of the given view.
	 */
	private static int getCenterOfView( View view ) {
		return view.getLeft() + view.getWidth() / 2;
	}

	/**
	 * Detaches children that are off the screen (i.e.: Gallery bounds).
	 * 
	 * @param toLeft
	 *           Whether to detach children to the left of the Gallery, or to the right.
	 */
	private void detachOffScreenChildren( boolean toLeft ) {
		int numChildren = getChildCount();
		int firstPosition = mFirstPosition;
		int start = 0;
		int count = 0;

		if ( toLeft ) {
			final int galleryLeft = mPaddingLeft;
			for ( int i = 0; i < numChildren; i++ ) {
				int n = mIsRtl ? ( numChildren - 1 - i ) : i;
				final View child = getChildAt( n );
				if ( child.getRight() >= galleryLeft ) {
					break;
				} else {
					start = n;
					count++;
					
					int viewType = mAdapter.getItemViewType( firstPosition + n );
					mRecycleBin.get( viewType ).add( child );
					
					//if ( firstPosition + n < 0 ) {
					//	mRecyclerInvalidItems.put( firstPosition + n, child );
					//} else {
					//	mRecycler.put( firstPosition + n, child );
					//}
				}
			}
			if ( !mIsRtl ) {
				start = 0;
			}

		} else {
			final int galleryRight = getWidth() - mPaddingRight;
			for ( int i = numChildren - 1; i >= 0; i-- ) {
				int n = mIsRtl ? numChildren - 1 - i : i;
				final View child = getChildAt( n );
				if ( child.getLeft() <= galleryRight ) {
					break;
				} else {
					start = n;
					count++;
					
					int viewType = mAdapter.getItemViewType( firstPosition + n );
					mRecycleBin.get( viewType ).add( child );

					//if ( firstPosition + n >= mItemCount ) {
					//	mRecyclerInvalidItems.put( firstPosition + n, child );
					//} else {
					//	mRecycler.put( firstPosition + n, child );
					//}
				}
			}
			if ( mIsRtl ) {
				start = 0;
			}
		}

		detachViewsFromParent( start, count );

		if ( toLeft != mIsRtl ) {
			mFirstPosition += count;
		}
	}

	/**
	 * Scrolls the items so that the selected item is in its 'slot' (its center is the gallery's center).
	 */
	@Override
	public void scrollIntoSlots() {
		if ( getChildCount() == 0 || mSelectedChild == null ) return;

		if ( mAutoScrollToCenter ) {
			int selectedCenter = getCenterOfView( mSelectedChild );
			int targetCenter = getCenterOfGallery();

			int scrollAmount = targetCenter - selectedCenter;

			if ( scrollAmount != 0 ) {
				mFlingRunnable.startUsingDistance( 0, -scrollAmount );
				// fireVibration();

			} else {
				onFinishedMovement();
			}
		} else {
			onFinishedMovement();
		}
	}

	/**
	 * Scrolls the items so that the selected item is in its 'slot' (its center is the gallery's center).
	 * 
	 * @return true, if is over scrolled
	 */
	private boolean isOverScrolled() {

		if ( getChildCount() < 2 || mSelectedChild == null ) return false;

		if ( mSelectedPosition == 0 || mSelectedPosition == mItemCount - 1 ) {

			int selectedCenter0 = getCenterOfView( mSelectedChild );
			int targetCenter = getCenterOfGallery();

			if ( mSelectedPosition == 0 && selectedCenter0 > targetCenter ) return true;

			if ( ( mSelectedPosition == mItemCount - 1 ) && selectedCenter0 < targetCenter ) return true;
		}

		return false;
	}

	/**
	 * On finished movement.
	 */
	private void onFinishedMovement() {
		if ( isDown ) return;

		if ( mSuppressSelectionChanged ) {
			mSuppressSelectionChanged = false;

			// We haven't been callbacking during the fling, so do it now
			super.selectionChanged();
		}
		scrollCompleted();
		invalidate();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aviary.android.feather.widget.AdapterView#selectionChanged()
	 */
	@Override
	void selectionChanged() {
		if ( !mSuppressSelectionChanged ) {
			super.selectionChanged();
		}
	}

	/**
	 * Looks for the child that is closest to the center and sets it as the selected child.
	 */
	private void setSelectionToCenterChild() {

		View selView = mSelectedChild;
		if ( mSelectedChild == null ) return;

		int galleryCenter = getCenterOfGallery();

		// Common case where the current selected position is correct
		if ( selView.getLeft() <= galleryCenter && selView.getRight() >= galleryCenter ) {
			return;
		}

		// TODO better search
		int closestEdgeDistance = Integer.MAX_VALUE;
		int newSelectedChildIndex = 0;
		for ( int i = getChildCount() - 1; i >= 0; i-- ) {

			View child = getChildAt( i );

			if ( child.getLeft() <= galleryCenter && child.getRight() >= galleryCenter ) {
				// This child is in the center
				newSelectedChildIndex = i;
				break;
			}

			int childClosestEdgeDistance = Math.min( Math.abs( child.getLeft() - galleryCenter ),
					Math.abs( child.getRight() - galleryCenter ) );
			if ( childClosestEdgeDistance < closestEdgeDistance ) {
				closestEdgeDistance = childClosestEdgeDistance;
				newSelectedChildIndex = i;
			}
		}

		int newPos = mFirstPosition + newSelectedChildIndex;

		if ( newPos != mSelectedPosition ) {

			newPos = Math.min( Math.max( newPos, 0 ), mItemCount - 1 );

			setSelectedPositionInt( newPos, true );
			setNextSelectedPositionInt( newPos );
			checkSelectionChanged();
		}
	}

	/**
	 * Creates and positions all views for this Gallery.
	 * <p>
	 * We layout rarely, most of the time {@link #trackMotionScroll(int)} takes care of repositioning, adding, and removing children.
	 * 
	 * @param delta
	 *           Change in the selected position. +1 means the selection is moving to the right, so views are scrolling to the left.
	 *           -1 means the selection is moving to the left.
	 * @param animate
	 *           the animate
	 */
	@Override
	void layout( int delta, boolean animate, boolean changed ) {

		mIsRtl = false;

		int childrenLeft = mSpinnerPadding.left;
		int childrenWidth = getRight() - getLeft() - mSpinnerPadding.left - mSpinnerPadding.right;

		if ( mDataChanged ) {
			handleDataChanged();
		}

		// Handle an empty gallery by removing all views.
		if ( mItemCount == 0 ) {
			resetList();
			return;
		}

		// Update to the new selected position.
		if ( mNextSelectedPosition >= 0 ) {
			setSelectedPositionInt( mNextSelectedPosition, animate );
		}

		// All views go in recycler while we are in layout
		recycleAllViews();

		// Clear out old views
		// removeAllViewsInLayout();
		detachAllViewsFromParent();

		/*
		 * These will be used to give initial positions to views entering the gallery as we scroll
		 */
		mRightMost = 0;
		mLeftMost = 0;

		// Make selected view and center it

		/*
		 * mFirstPosition will be decreased as we add views to the left later on. The 0 for x will be offset in a couple lines down.
		 */
		mFirstPosition = mSelectedPosition;
		View sel = makeAndAddView( mSelectedPosition, 0, 0, true );

		// Put the selected child in the center
		int selectedOffset = childrenLeft + ( childrenWidth / 2 ) - ( sel.getWidth() / 2 );
		sel.offsetLeftAndRight( selectedOffset );

		fillToGalleryRight();
		fillToGalleryLeft();

		// Flush any cached views that did not get reused above
		//mRecycler.clear();
		//mRecyclerInvalidItems.clear();
		emptySubRecycler();

		invalidate();
		checkSelectionChanged();

		mDataChanged = false;
		mNeedSync = false;
		setNextSelectedPositionInt( mSelectedPosition );

		updateSelectedItemMetadata( animate, changed );
	}

	/**
	 * Fill to gallery left.
	 */
	private void fillToGalleryLeft() {
		if ( mIsRtl ) {
			fillToGalleryLeftRtl();
		} else {
			fillToGalleryLeftLtr();
		}
	}

	/**
	 * Fill to gallery left rtl.
	 */
	private void fillToGalleryLeftRtl() {
		int itemSpacing = mSpacing;
		int galleryLeft = mPaddingLeft;
		int numChildren = getChildCount();

		// Set state for initial iteration
		View prevIterationView = getChildAt( numChildren - 1 );
		int curPosition;
		int curRightEdge;

		if ( prevIterationView != null ) {
			curPosition = mFirstPosition + numChildren;
			curRightEdge = prevIterationView.getLeft() - itemSpacing;
		} else {
			// No children available!
			mFirstPosition = curPosition = mItemCount - 1;
			curRightEdge = getRight() - getLeft() - mPaddingRight;
			mShouldStopFling = true;
		}

		while ( curRightEdge > galleryLeft && curPosition < mItemCount ) {
			prevIterationView = makeAndAddView( curPosition, curPosition - mSelectedPosition, curRightEdge, false );

			// Set state for next iteration
			curRightEdge = prevIterationView.getLeft() - itemSpacing;
			curPosition++;
		}
	}

	/**
	 * Fill to gallery left ltr.
	 */
	private void fillToGalleryLeftLtr() {
		int itemSpacing = mSpacing;
		int galleryLeft = mPaddingLeft;

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
			mShouldStopFling = true;
		}

		while ( curRightEdge > galleryLeft /* && curPosition >= 0 */) {
			prevIterationView = makeAndAddView( curPosition, curPosition - mSelectedPosition, curRightEdge, false );

			// Remember some state
			mFirstPosition = curPosition;

			// Set state for next iteration
			curRightEdge = prevIterationView.getLeft() - itemSpacing;
			curPosition--;
		}
	}

	/**
	 * Fill to gallery right.
	 */
	private void fillToGalleryRight() {
		if ( mIsRtl ) {
			fillToGalleryRightRtl();
		} else {
			fillToGalleryRightLtr();
		}
	}

	/**
	 * Fill to gallery right rtl.
	 */
	private void fillToGalleryRightRtl() {
		int itemSpacing = mSpacing;
		int galleryRight = getRight() - getLeft() - mPaddingRight;

		// Set state for initial iteration
		View prevIterationView = getChildAt( 0 );
		int curPosition;
		int curLeftEdge;

		if ( prevIterationView != null ) {
			curPosition = mFirstPosition - 1;
			curLeftEdge = prevIterationView.getRight() + itemSpacing;
		} else {
			curPosition = 0;
			curLeftEdge = mPaddingLeft;
			mShouldStopFling = true;
		}

		while ( curLeftEdge < galleryRight && curPosition >= 0 ) {
			prevIterationView = makeAndAddView( curPosition, curPosition - mSelectedPosition, curLeftEdge, true );

			// Remember some state
			mFirstPosition = curPosition;

			// Set state for next iteration
			curLeftEdge = prevIterationView.getRight() + itemSpacing;
			curPosition--;
		}
	}

	/**
	 * Fill to gallery right ltr.
	 */
	private void fillToGalleryRightLtr() {
		int itemSpacing = mSpacing;
		int galleryRight = getRight() - getLeft() - mPaddingRight;
		int numChildren = getChildCount();

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
			mShouldStopFling = true;
		}

		while ( curLeftEdge < galleryRight /* && curPosition < numItems */) {
			prevIterationView = makeAndAddView( curPosition, curPosition - mSelectedPosition, curLeftEdge, true );

			// Set state for next iteration
			curLeftEdge = prevIterationView.getRight() + itemSpacing;
			curPosition++;
		}
	}

	/**
	 * Obtain a view, either by pulling an existing view from the recycler or by getting a new one from the adapter. If we are
	 * animating, make sure there is enough information in the view's layout parameters to animate from the old to new positions.
	 * 
	 * @param position
	 *           Position in the gallery for the view to obtain
	 * @param offset
	 *           Offset from the selected position
	 * @param x
	 *           X-coordinate indicating where this view should be placed. This will either be the left or right edge of the view,
	 *           depending on the fromLeft parameter
	 * @param fromLeft
	 *           Are we positioning views based on the left edge? (i.e., building from left to right)?
	 * @return A view that has been added to the gallery
	 */
	private View makeAndAddView( int position, int offset, int x, boolean fromLeft ) {

		View child;
		int viewType = mAdapter.getItemViewType( position );

		if ( !mDataChanged ) {
			
			child = mRecycleBin.get( viewType ).poll();
			
			/*
			if ( valid ) {
				child = mRecycler.get( position );
			} else {
				child = mRecyclerInvalidItems.get( position );
			}
			*/

			if ( child != null ) {
				// Can reuse an existing view
				child = mAdapter.getView( position, child, this );
				int childLeft = child.getLeft();

				// Remember left and right edges of where views have been placed
				mRightMost = Math.max( mRightMost, childLeft + child.getMeasuredWidth() );
				mLeftMost = Math.min( mLeftMost, childLeft );

				// Position the view
				setUpChild( child, offset, x, fromLeft );

				return child;
			}
		}

		// Nothing found in the recycler -- ask the adapter for a view
		child = mAdapter.getView( position, null, this );

		// Position the view
		setUpChild( child, offset, x, fromLeft );

		return child;
	}

	public void invalidateViews() {
		int count = getChildCount();
		for ( int i = 0; i < count; i++ ) {
			View child = getChildAt( i );
			mAdapter.getView( mFirstPosition + i, child, this );
		}
	}

	/**
	 * Helper for makeAndAddView to set the position of a view and fill out its layout parameters.
	 * 
	 * @param child
	 *           The view to position
	 * @param offset
	 *           Offset from the selected position
	 * @param x
	 *           X-coordinate indicating where this view should be placed. This will either be the left or right edge of the view,
	 *           depending on the fromLeft parameter
	 * @param fromLeft
	 *           Are we positioning views based on the left edge? (i.e., building from left to right)?
	 */
	private void setUpChild( View child, int offset, int x, boolean fromLeft ) {

		// Respect layout params that are already in the view. Otherwise
		// make some up...
		Gallery.LayoutParams lp = (Gallery.LayoutParams) child.getLayoutParams();
		if ( lp == null ) {
			lp = (Gallery.LayoutParams) generateDefaultLayoutParams();
		}

		addViewInLayout( child, fromLeft != mIsRtl ? -1 : 0, lp );

		if ( mAutoSelectChild ) child.setSelected( offset == 0 );

		// Get measure specs
		int childHeightSpec = ViewGroup.getChildMeasureSpec( mHeightMeasureSpec, mSpinnerPadding.top + mSpinnerPadding.bottom,
				lp.height );
		int childWidthSpec = ViewGroup
				.getChildMeasureSpec( mWidthMeasureSpec, mSpinnerPadding.left + mSpinnerPadding.right, lp.width );

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
	 * Figure out vertical placement based on mGravity.
	 * 
	 * @param child
	 *           Child to place
	 * @param duringLayout
	 *           the during layout
	 * @return Where the top of the child should be
	 */
	private int calculateTop( View child, boolean duringLayout ) {
		int myHeight = duringLayout ? getMeasuredHeight() : getHeight();
		int childHeight = duringLayout ? child.getMeasuredHeight() : child.getHeight();

		int childTop = 0;

		switch ( mGravity ) {
			case Gravity.TOP:
				childTop = mSpinnerPadding.top;
				break;
			case Gravity.CENTER_VERTICAL:
				int availableSpace = myHeight - mSpinnerPadding.bottom - mSpinnerPadding.top - childHeight;
				childTop = mSpinnerPadding.top + ( availableSpace / 2 );
				break;
			case Gravity.BOTTOM:
				childTop = myHeight - mSpinnerPadding.bottom - childHeight;
				break;
		}
		return childTop;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View#onTouchEvent(android.view.MotionEvent)
	 */
	@Override
	public boolean onTouchEvent( MotionEvent event ) {

		// Give everything to the gesture detector
		boolean retValue = mGestureDetector.onTouchEvent( event );

		int action = event.getAction();
		if ( action == MotionEvent.ACTION_UP ) {
			// Helper method for lifted finger
			onUp();
		} else if ( action == MotionEvent.ACTION_CANCEL ) {
			onCancel();
		}

		return retValue;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.GestureDetector.OnGestureListener#onSingleTapUp(android.view.MotionEvent)
	 */
	@Override
	public boolean onSingleTapUp( MotionEvent e ) {

		if ( mDownTouchPosition >= 0 && mDownTouchPosition < mItemCount ) {

			// An item tap should make it selected, so scroll to this child.

			scrollToChild( mDownTouchPosition - mFirstPosition );

			// Also pass the click so the client knows, if it wants to.
			if ( mShouldCallbackOnUnselectedItemClick || mDownTouchPosition == mSelectedPosition ) {
				performItemClick( mDownTouchView, mDownTouchPosition, mAdapter.getItemId( mDownTouchPosition ) );
			}

			return true;
		}

		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.GestureDetector.OnGestureListener#onFling(android.view.MotionEvent, android.view.MotionEvent, float, float)
	 */
	@Override
	public boolean onFling( MotionEvent e1, MotionEvent e2, float velocityX, float velocityY ) {

		if ( !mShouldCallbackDuringFling ) {
			// We want to suppress selection changes

			// Remove any future code to set mSuppressSelectionChanged = false
			removeCallbacks( mDisableSuppressSelectionChangedRunnable );

			// This will get reset once we scroll into slots
			if ( !mSuppressSelectionChanged ) mSuppressSelectionChanged = true;
		}

		// Fling the gallery!
		int initialVelocity = (int) -velocityX / 2;
		int initialX = initialVelocity < 0 ? Integer.MAX_VALUE : 0;
		mFlingRunnable.startUsingVelocity( initialX, initialVelocity );

		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.GestureDetector.OnGestureListener#onScroll(android.view.MotionEvent, android.view.MotionEvent, float, float)
	 */
	@Override
	public boolean onScroll( MotionEvent e1, MotionEvent e2, float distanceX, float distanceY ) {

		/*
		 * Now's a good time to tell our parent to stop intercepting our events! The user has moved more than the slop amount, since
		 * GestureDetector ensures this before calling this method. Also, if a parent is more interested in this touch's events than
		 * we are, it would have intercepted them by now (for example, we can assume when a Gallery is in the ListView, a vertical
		 * scroll would not end up in this method since a ListView would have intercepted it by now).
		 */
		getParent().requestDisallowInterceptTouchEvent( true );

		// As the user scrolls, we want to callback selection changes so related-
		// info on the screen is up-to-date with the gallery's selection
		if ( !mShouldCallbackDuringFling ) {
			if ( mIsFirstScroll ) {

				if ( !mSuppressSelectionChanged ) mSuppressSelectionChanged = true;
				postDelayed( mDisableSuppressSelectionChangedRunnable, SCROLL_TO_FLING_UNCERTAINTY_TIMEOUT );

				if ( mItemsScrollListener != null ) {
					int selection = this.getSelectedItemPosition();
					if ( selection >= 0 ) {
						View v = getSelectedView();
						mItemsScrollListener.onScrollStarted( this, v, selection, getAdapter().getItemId( selection ) );
					}
				}
			}
		} else {
			if ( mSuppressSelectionChanged ) mSuppressSelectionChanged = false;
		}

		// Track the motion

		if ( mIsFirstScroll ) {
			if ( distanceX > 0 )
				distanceX -= mTouchSlop;
			else
				distanceX += mTouchSlop;
		}

		int delta = -1 * (int) distanceX;
		float limitedDelta = getOverScrollDelta( delta < 0, delta );

		if ( limitedDelta != 0 ) {
			delta = (int) ( delta / Math.max( 1, Math.abs( limitedDelta / 10 ) ) );
		}

		trackMotionScroll( -delta );

		mIsFirstScroll = false;
		return true;
	}

	/** The is down. */
	private boolean isDown;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.GestureDetector.OnGestureListener#onDown(android.view.MotionEvent)
	 */
	@Override
	public boolean onDown( MotionEvent e ) {

		isDown = true;

		// Kill any existing fling/scroll
		mFlingRunnable.stop( false );

		// Get the item's view that was touched
		mDownTouchPosition = pointToPosition( (int) e.getX(), (int) e.getY() );

		if ( mDownTouchPosition >= 0 && mDownTouchPosition < mItemCount ) {
			mDownTouchView = getChildAt( mDownTouchPosition - mFirstPosition );
			mDownTouchView.setPressed( true );
		}

		// Reset the multiple-scroll tracking state
		mIsFirstScroll = true;

		// Must return true to get matching events for this down event.
		return true;
	}

	/**
	 * Called when a touch event's action is MotionEvent.ACTION_UP.
	 */
	void onUp() {
		isDown = false;
		if ( mFlingRunnable.isFinished() ) {
			scrollIntoSlots();
		} else {
			if ( isOverScrolled() ) {
				scrollIntoSlots();
			}
		}

		dispatchUnpress();
	}

	/**
	 * Called when a touch event's action is MotionEvent.ACTION_CANCEL.
	 */
	void onCancel() {
		onUp();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.GestureDetector.OnGestureListener#onLongPress(android.view.MotionEvent)
	 */
	@Override
	public void onLongPress( MotionEvent e ) {

		if ( mDownTouchPosition < 0 || mDownTouchPosition <= mItemCount ) {
			return;
		}

		performHapticFeedback( HapticFeedbackConstants.LONG_PRESS );
		long id = getItemIdAtPosition( mDownTouchPosition );
		dispatchLongPress( mDownTouchView, mDownTouchPosition, id );
	}

	// Unused methods from GestureDetector.OnGestureListener below

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.GestureDetector.OnGestureListener#onShowPress(android.view.MotionEvent)
	 */
	@Override
	public void onShowPress( MotionEvent e ) {}

	// Unused methods from GestureDetector.OnGestureListener above

	/**
	 * Dispatch press.
	 * 
	 * @param child
	 *           the child
	 */
	private void dispatchPress( View child ) {

		if ( child != null ) {
			child.setPressed( true );
		}

		setPressed( true );
	}

	/**
	 * Dispatch unpress.
	 */
	private void dispatchUnpress() {

		for ( int i = getChildCount() - 1; i >= 0; i-- ) {
			getChildAt( i ).setPressed( false );
		}

		setPressed( false );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.ViewGroup#dispatchSetSelected(boolean)
	 */
	@Override
	public void dispatchSetSelected( boolean selected ) {
		/*
		 * We don't want to pass the selected state given from its parent to its children since this widget itself has a selected
		 * state to give to its children.
		 */
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.ViewGroup#dispatchSetPressed(boolean)
	 */
	@Override
	protected void dispatchSetPressed( boolean pressed ) {

		// Show the pressed state on the selected child
		if ( mSelectedChild != null ) {
			mSelectedChild.setPressed( pressed );
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View#getContextMenuInfo()
	 */
	@Override
	protected ContextMenuInfo getContextMenuInfo() {
		return mContextMenuInfo;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.ViewGroup#showContextMenuForChild(android.view.View)
	 */
	@Override
	public boolean showContextMenuForChild( View originalView ) {

		final int longPressPosition = getPositionForView( originalView );
		if ( longPressPosition < 0 ) {
			return false;
		}

		final long longPressId = mAdapter.getItemId( longPressPosition );
		return dispatchLongPress( originalView, longPressPosition, longPressId );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View#showContextMenu()
	 */
	@Override
	public boolean showContextMenu() {

		if ( isPressed() && mSelectedPosition >= 0 ) {
			int index = mSelectedPosition - mFirstPosition;
			View v = getChildAt( index );
			return dispatchLongPress( v, mSelectedPosition, mSelectedRowId );
		}

		return false;
	}

	/**
	 * Dispatch long press.
	 * 
	 * @param view
	 *           the view
	 * @param position
	 *           the position
	 * @param id
	 *           the id
	 * @return true, if successful
	 */
	private boolean dispatchLongPress( View view, int position, long id ) {
		boolean handled = false;

		if ( mOnItemLongClickListener != null ) {
			handled = mOnItemLongClickListener.onItemLongClick( this, mDownTouchView, mDownTouchPosition, id );
		}

		if ( !handled ) {
			mContextMenuInfo = new AdapterContextMenuInfo( view, position, id );
			handled = super.showContextMenuForChild( this );
		}

		if ( handled ) {
			performHapticFeedback( HapticFeedbackConstants.LONG_PRESS );
		}

		return handled;
	}

	/**
	 * Handles left, right, and clicking.
	 * 
	 * @param keyCode
	 *           the key code
	 * @param event
	 *           the event
	 * @return true, if successful
	 * @see android.view.View#onKeyDown
	 */
	@Override
	public boolean onKeyDown( int keyCode, KeyEvent event ) {
		switch ( keyCode ) {

			case KeyEvent.KEYCODE_DPAD_LEFT:
				if ( movePrevious() ) {
					playSoundEffect( SoundEffectConstants.NAVIGATION_LEFT );
				}
				return true;

			case KeyEvent.KEYCODE_DPAD_RIGHT:
				if ( moveNext() ) {
					playSoundEffect( SoundEffectConstants.NAVIGATION_RIGHT );
				}
				return true;

			case KeyEvent.KEYCODE_DPAD_CENTER:
			case KeyEvent.KEYCODE_ENTER:
				mReceivedInvokeKeyDown = true;
				// fallthrough to default handling
		}

		return false;
	}

	@Override
	public boolean dispatchKeyEvent( KeyEvent event ) {
		boolean handled = event.dispatch( this, null, null );
		return handled;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View#onKeyUp(int, android.view.KeyEvent)
	 */
	@Override
	public boolean onKeyUp( int keyCode, KeyEvent event ) {
		switch ( keyCode ) {
			case KeyEvent.KEYCODE_DPAD_CENTER:
			case KeyEvent.KEYCODE_ENTER: {

				if ( mReceivedInvokeKeyDown ) {
					if ( mItemCount > 0 ) {

						dispatchPress( mSelectedChild );
						postDelayed( new Runnable() {

							@Override
							public void run() {
								dispatchUnpress();
							}
						}, ViewConfiguration.getPressedStateDuration() );

						int selectedIndex = mSelectedPosition - mFirstPosition;
						performItemClick( getChildAt( selectedIndex ), mSelectedPosition, mAdapter.getItemId( mSelectedPosition ) );
					}
				}

				// Clear the flag
				mReceivedInvokeKeyDown = false;

				return true;
			}
		}

		return false;
	}

	/**
	 * Move previous.
	 * 
	 * @return true, if successful
	 */
	boolean movePrevious() {
		if ( mItemCount > 0 && mSelectedPosition > 0 ) {
			scrollToChild( mSelectedPosition - mFirstPosition - 1 );
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Move next.
	 * 
	 * @return true, if successful
	 */
	boolean moveNext() {
		if ( mItemCount > 0 && mSelectedPosition < mItemCount - 1 ) {
			scrollToChild( mSelectedPosition - mFirstPosition + 1 );
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Scroll to child.
	 * 
	 * @param childPosition
	 *           the child position
	 * @return true, if successful
	 */
	private boolean scrollToChild( int childPosition ) {
		View child = getChildAt( childPosition );

		if ( child != null ) {

			if ( mItemsScrollListener != null ) {
				int selection = this.getSelectedItemPosition();
				if ( selection >= 0 ) {
					View v = getSelectedView();
					mItemsScrollListener.onScrollStarted( this, v, selection, getAdapter().getItemId( selection ) );
				}
			}

			int distance = getCenterOfGallery() - getCenterOfView( child );
			mFlingRunnable.startUsingDistance( 0, -distance );
			return true;
		}

		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aviary.android.feather.widget.AdapterView#setSelectedPositionInt(int)
	 */
	void setSelectedPositionInt( int position, boolean animate ) {
		super.setSelectedPositionInt( position );
		updateSelectedItemMetadata( animate, false );
	}

	/**
	 * Update selected item metadata.
	 */
	private void updateSelectedItemMetadata( boolean animate, boolean changed ) {

		View oldSelectedChild = mSelectedChild;

		View child = mSelectedChild = getChildAt( mSelectedPosition - mFirstPosition );
		if ( child == null ) {
			return;
		}

		if ( mAutoSelectChild ) child.setSelected( true );

		if ( mItemsScrollListener != null ) {
			mItemsScrollListener.onScroll( this, child, mSelectedPosition, getAdapter().getItemId( mSelectedPosition ) );
		}

		// fire vibration
		if ( mSelectedPosition != mLastMotionValue && animate ) {
			fireVibration();
		}
		mLastMotionValue = mSelectedPosition;

		child.setFocusable( true );

		if ( hasFocus() ) {
			child.requestFocus();
		}

		// We unfocus the old child down here so the above hasFocus check
		// returns true
		if ( oldSelectedChild != null && oldSelectedChild != child ) {

			// Make sure its drawable state doesn't contain 'selected'
			if ( mAutoSelectChild ) oldSelectedChild.setSelected( false );

			// Make sure it is not focusable anymore, since otherwise arrow keys
			// can make this one be focused
			oldSelectedChild.setFocusable( false );

			if ( !animate && changed ) scrollCompleted();
		}
	}

	/**
	 * Fire vibration.
	 */
	private void fireVibration() {
		if ( mVibrationHandler != null ) {
			mVibrationHandler.sendEmptyMessage( MSG_VIBRATE );
		}
	}

	/**
	 * Describes how the child views are aligned.
	 * 
	 * @param gravity
	 *           the new gravity
	 * @attr ref android.R.styleable#Gallery_gravity
	 */
	public void setGravity( int gravity ) {
		if ( mGravity != gravity ) {
			mGravity = gravity;
			requestLayout();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.ViewGroup#getChildDrawingOrder(int, int)
	 */
	@Override
	protected int getChildDrawingOrder( int childCount, int i ) {
		int selectedIndex = mSelectedPosition - mFirstPosition;

		// Just to be safe
		if ( selectedIndex < 0 ) return i;

		if ( i == childCount - 1 ) {
			// Draw the selected child last
			return selectedIndex;
		} else if ( i >= selectedIndex ) {
			// Move the children after the selected child earlier one
			return i + 1;
		} else {
			// Keep the children before the selected child the same
			return i;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View#onFocusChanged(boolean, int, android.graphics.Rect)
	 */
	@Override
	protected void onFocusChanged( boolean gainFocus, int direction, Rect previouslyFocusedRect ) {
		super.onFocusChanged( gainFocus, direction, previouslyFocusedRect );

		/*
		 * The gallery shows focus by focusing the selected item. So, give focus to our selected item instead. We steal keys from our
		 * selected item elsewhere.
		 */
		if ( gainFocus && mSelectedChild != null ) {
			mSelectedChild.requestFocus( direction );
			if ( mAutoSelectChild ) mSelectedChild.setSelected( true );
		}
	}

	/** The m scroll selection notifier. */
	ScrollSelectionNotifier mScrollSelectionNotifier;

	/**
	 * The Class ScrollSelectionNotifier.
	 */
	private class ScrollSelectionNotifier implements Runnable {

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			if ( mDataChanged ) {
				if ( getAdapter() != null ) {
					post( this );
				}
			} else {
				fireOnScrollCompleted();
			}
		}
	}

	/**
	 * Scroll completed.
	 */
	void scrollCompleted() {
		if ( mItemsScrollListener != null ) {
			if ( mInLayout || mBlockLayoutRequests ) {
				// If we are in a layout traversal, defer notification
				// by posting. This ensures that the view tree is
				// in a consistent state and is able to accomodate
				// new layout or invalidate requests.
				if ( mScrollSelectionNotifier == null ) {
					mScrollSelectionNotifier = new ScrollSelectionNotifier();
				}
				post( mScrollSelectionNotifier );
			} else {
				fireOnScrollCompleted();
			}
		}
	}

	/**
	 * Fire on scroll completed.
	 */
	private void fireOnScrollCompleted() {
		if ( mItemsScrollListener == null ) return;

		int selection = this.getSelectedItemPosition();
		if ( selection >= 0 && selection < mItemCount ) {
			View v = getSelectedView();
			mItemsScrollListener.onScrollFinished( this, v, selection, getAdapter().getItemId( selection ) );
		}
	}

	/**
	 * Gallery extends LayoutParams to provide a place to hold current Transformation information along with previous
	 * position/transformation info.
	 */
	public static class LayoutParams extends ViewGroup.LayoutParams {

		/**
		 * Instantiates a new layout params.
		 * 
		 * @param c
		 *           the c
		 * @param attrs
		 *           the attrs
		 */
		public LayoutParams( Context c, AttributeSet attrs ) {
			super( c, attrs );
		}

		/**
		 * Instantiates a new layout params.
		 * 
		 * @param w
		 *           the w
		 * @param h
		 *           the h
		 */
		public LayoutParams( int w, int h ) {
			super( w, h );
		}

		/**
		 * Instantiates a new layout params.
		 * 
		 * @param source
		 *           the source
		 */
		public LayoutParams( ViewGroup.LayoutParams source ) {
			super( source );
		}
	}

	@Override
	public int getMinX() {
		return 0;
	}

	@Override
	public int getMaxX() {
		return Integer.MAX_VALUE;
	}
}
