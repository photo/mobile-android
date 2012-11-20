/*
 * Copyright (C) 2006 The Android Open Source Project
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;

// TODO: Auto-generated Javadoc
/**
 * An abstract base class for spinner widgets. SDK users will probably not need to use this class.
 * 
 * @attr ref android.R.styleable#AbsSpinner_entries
 */
public abstract class AbsSpinner extends AdapterView<Adapter> {

	/** The m adapter. */
	Adapter mAdapter;

	/** The m height measure spec. */
	int mHeightMeasureSpec;

	/** The m width measure spec. */
	int mWidthMeasureSpec;

	/** The m block layout requests. */
	boolean mBlockLayoutRequests;

	/** The m selection left padding. */
	int mSelectionLeftPadding = 0;

	/** The m selection top padding. */
	int mSelectionTopPadding = 0;

	/** The m selection right padding. */
	int mSelectionRightPadding = 0;

	/** The m selection bottom padding. */
	int mSelectionBottomPadding = 0;

	/** The m spinner padding. */
	final Rect mSpinnerPadding = new Rect();

	/** The m padding left. */
	int mPaddingLeft;

	/** The m padding right. */
	int mPaddingRight;

	/** The m padding top. */
	int mPaddingTop;

	/** The m padding bottom. */
	int mPaddingBottom;

	/** The m recycler. */
	protected final List<Queue<View>> mRecycleBin;

	/** The m data set observer. */
	private DataSetObserver mDataSetObserver;

	/** Temporary frame to hold a child View's frame rectangle. */
	private Rect mTouchFrame;

	/**
	 * Instantiates a new abs spinner.
	 * 
	 * @param context
	 *           the context
	 */
	public AbsSpinner( Context context ) {
		super( context );
		mRecycleBin = Collections.synchronizedList( new ArrayList<Queue<View>>() );
		initAbsSpinner();
	}

	/**
	 * Instantiates a new abs spinner.
	 * 
	 * @param context
	 *           the context
	 * @param attrs
	 *           the attrs
	 */
	public AbsSpinner( Context context, AttributeSet attrs ) {
		this( context, attrs, 0 );
	}

	/**
	 * Instantiates a new abs spinner.
	 * 
	 * @param context
	 *           the context
	 * @param attrs
	 *           the attrs
	 * @param defStyle
	 *           the def style
	 */
	public AbsSpinner( Context context, AttributeSet attrs, int defStyle ) {
		super( context, attrs, defStyle );
		mRecycleBin = Collections.synchronizedList( new ArrayList<Queue<View>>() );
		initAbsSpinner();
		/*
		 * 
		 * TypedArray a = context.obtainStyledAttributes(attrs, com.android.internal.R.styleable.AbsSpinner, defStyle, 0);
		 * 
		 * CharSequence[] entries = a.getTextArray(R.styleable.AbsSpinner_entries); if (entries != null) { ArrayAdapter<CharSequence>
		 * adapter = new ArrayAdapter<CharSequence>(context, R.layout.simple_spinner_item, entries);
		 * adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item); setAdapter(adapter); } a.recycle();
		 */
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.ViewGroup#setPadding(int, int, int, int)
	 */
	@Override
	public void setPadding( int left, int top, int right, int bottom ) {
		super.setPadding( left, top, right, bottom );
		mPaddingLeft = left;
		mPaddingBottom = bottom;
		mPaddingTop = top;
		mPaddingRight = right;
	}

	/**
	 * Common code for different constructor flavors.
	 */
	private void initAbsSpinner() {
		setFocusable( true );
		setWillNotDraw( false );
	}

	/**
	 * The Adapter is used to provide the data which backs this Spinner. It also provides methods to transform spinner items based on
	 * their position relative to the selected item.
	 * 
	 * @param adapter
	 *           The SpinnerAdapter to use for this Spinner
	 */
	@Override
	public void setAdapter( Adapter adapter ) {
		if ( null != mAdapter ) {
			mAdapter.unregisterDataSetObserver( mDataSetObserver );
			emptyRecycler();
			resetList();
		}

		mAdapter = adapter;

		mOldSelectedPosition = INVALID_POSITION;
		mOldSelectedRowId = INVALID_ROW_ID;

		if ( mAdapter != null ) {
			mOldItemCount = mItemCount;
			mItemCount = mAdapter.getCount();
			checkFocus();

			mDataSetObserver = new AdapterDataSetObserver();
			mAdapter.registerDataSetObserver( mDataSetObserver );

			int position = mItemCount > 0 ? 0 : INVALID_POSITION;

			int total = mAdapter.getViewTypeCount();
			for ( int i = 0; i < total; i++ ) {
				mRecycleBin.add( new LinkedList<View>() );
			}

			setSelectedPositionInt( position );
			setNextSelectedPositionInt( position );

			if ( mItemCount == 0 ) {
				// Nothing selected
				checkSelectionChanged();
			}

		} else {
			checkFocus();
			resetList();
			// Nothing selected
			checkSelectionChanged();
		}

		requestLayout();
	}

	private void emptyRecycler() {
		emptySubRecycler();
		if ( null != mRecycleBin ) {
			mRecycleBin.clear();
		}
	}
	
	protected void emptySubRecycler() {
		if ( null != mRecycleBin ) {
			for( int i = 0; i < mRecycleBin.size(); i++ ){
				mRecycleBin.get( i ).clear();
			}
		}
	}

	/**
	 * Clear out all children from the list.
	 */
	void resetList() {
		mDataChanged = false;
		mNeedSync = false;

		removeAllViewsInLayout();
		mOldSelectedPosition = INVALID_POSITION;
		mOldSelectedRowId = INVALID_ROW_ID;

		setSelectedPositionInt( INVALID_POSITION );
		setNextSelectedPositionInt( INVALID_POSITION );
		invalidate();
	}

	/**
	 * On measure.
	 * 
	 * @param widthMeasureSpec
	 *           the width measure spec
	 * @param heightMeasureSpec
	 *           the height measure spec
	 * @see android.view.View#measure(int, int)
	 * 
	 *      Figure out the dimensions of this Spinner. The width comes from the widthMeasureSpec as Spinnners can't have their width
	 *      set to UNSPECIFIED. The height is based on the height of the selected item plus padding.
	 */
	@Override
	protected void onMeasure( int widthMeasureSpec, int heightMeasureSpec ) {
		int widthMode = MeasureSpec.getMode( widthMeasureSpec );
		int widthSize;
		int heightSize;

		mSpinnerPadding.left = mPaddingLeft > mSelectionLeftPadding ? mPaddingLeft : mSelectionLeftPadding;
		mSpinnerPadding.top = mPaddingTop > mSelectionTopPadding ? mPaddingTop : mSelectionTopPadding;
		mSpinnerPadding.right = mPaddingRight > mSelectionRightPadding ? mPaddingRight : mSelectionRightPadding;
		mSpinnerPadding.bottom = mPaddingBottom > mSelectionBottomPadding ? mPaddingBottom : mSelectionBottomPadding;

		if ( mDataChanged ) {
			handleDataChanged();
		}

		int preferredHeight = 0;
		int preferredWidth = 0;
		boolean needsMeasuring = true;

		int selectedPosition = getSelectedItemPosition();
		if ( selectedPosition >= 0 && mAdapter != null && selectedPosition < mAdapter.getCount() ) {
			// Try looking in the recycler. (Maybe we were measured once already)

			int viewType = mAdapter.getItemViewType( selectedPosition );
			View view = mRecycleBin.get( viewType ).poll();
			if ( view == null ) {
				// Make a new one
				view = mAdapter.getView( selectedPosition, null, this );
			}

			if ( view != null ) {
				// Put in recycler for re-measuring and/or layout
				mRecycleBin.get( viewType ).offer( view );
			}

			if ( view != null ) {
				if ( view.getLayoutParams() == null ) {
					mBlockLayoutRequests = true;
					view.setLayoutParams( generateDefaultLayoutParams() );
					mBlockLayoutRequests = false;
				}
				measureChild( view, widthMeasureSpec, heightMeasureSpec );

				preferredHeight = getChildHeight( view ) + mSpinnerPadding.top + mSpinnerPadding.bottom;
				preferredWidth = getChildWidth( view ) + mSpinnerPadding.left + mSpinnerPadding.right;

				needsMeasuring = false;
			}
		}

		if ( needsMeasuring ) {
			// No views -- just use padding
			preferredHeight = mSpinnerPadding.top + mSpinnerPadding.bottom;
			if ( widthMode == MeasureSpec.UNSPECIFIED ) {
				preferredWidth = mSpinnerPadding.left + mSpinnerPadding.right;
			}
		}

		preferredHeight = Math.max( preferredHeight, getSuggestedMinimumHeight() );
		preferredWidth = Math.max( preferredWidth, getSuggestedMinimumWidth() );

		heightSize = resolveSize( preferredHeight, heightMeasureSpec );
		widthSize = resolveSize( preferredWidth, widthMeasureSpec );

		setMeasuredDimension( widthSize, heightSize );
		mHeightMeasureSpec = heightMeasureSpec;
		mWidthMeasureSpec = widthMeasureSpec;
	}

	/**
	 * Gets the child height.
	 * 
	 * @param child
	 *           the child
	 * @return the child height
	 */
	int getChildHeight( View child ) {
		return child.getMeasuredHeight();
	}

	/**
	 * Gets the child width.
	 * 
	 * @param child
	 *           the child
	 * @return the child width
	 */
	int getChildWidth( View child ) {
		return child.getMeasuredWidth();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.ViewGroup#generateDefaultLayoutParams()
	 */
	@Override
	protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
		return new ViewGroup.LayoutParams( ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT );
	}

	/**
	 * Recycle all views.
	 */
	void recycleAllViews() {

		final int childCount = getChildCount();
		final int position = mFirstPosition;

		// All views go in recycler
		for ( int i = 0; i < childCount; i++ ) {
			View v = getChildAt( i );
			int index = position + i;
			int viewType = mAdapter.getItemViewType( index );
			mRecycleBin.get( viewType ).offer( v );

			// if ( position + i < 0 ) {
			// recycleBin2.put( index, v );
			// } else {
			// recycleBin.put( index, v );
			// }
		}
		// recycleBin2.clear();
	}

	/**
	 * Jump directly to a specific item in the adapter data.
	 * 
	 * @param position
	 *           the position
	 * @param animate
	 *           the animate
	 */
	public void setSelection( int position, boolean animate, boolean changed ) {
		// Animate only if requested position is already on screen somewhere
		boolean shouldAnimate = animate && mFirstPosition <= position && position <= mFirstPosition + getChildCount() - 1;
		setSelectionInt( position, shouldAnimate, changed );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aviary.android.feather.widget.AdapterView#setSelection(int)
	 */
	@Override
	public void setSelection( int position ) {
		setNextSelectedPositionInt( position );
		requestLayout();
		invalidate();
	}

	/**
	 * Makes the item at the supplied position selected.
	 * 
	 * @param position
	 *           Position to select
	 * @param animate
	 *           Should the transition be animated
	 * 
	 */
	void setSelectionInt( int position, boolean animate, boolean changed ) {
		if ( position != mOldSelectedPosition ) {
			mBlockLayoutRequests = true;
			int delta = position - mSelectedPosition;
			setNextSelectedPositionInt( position );
			layout( delta, animate, changed );
			mBlockLayoutRequests = false;
		}
	}

	/**
	 * Layout.
	 * 
	 * @param delta
	 *           the delta
	 * @param animate
	 *           the animate
	 */
	abstract void layout( int delta, boolean animate, boolean changed );

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aviary.android.feather.widget.AdapterView#getSelectedView()
	 */
	@Override
	public View getSelectedView() {
		if ( mItemCount > 0 && mSelectedPosition >= 0 ) {
			return getChildAt( mSelectedPosition - mFirstPosition );
		} else {
			return null;
		}
	}

	/**
	 * Override to prevent spamming ourselves with layout requests as we place views.
	 * 
	 * @see android.view.View#requestLayout()
	 */
	@Override
	public void requestLayout() {
		if ( !mBlockLayoutRequests ) {
			super.requestLayout();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aviary.android.feather.widget.AdapterView#getAdapter()
	 */
	@Override
	public Adapter getAdapter() {
		return mAdapter;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aviary.android.feather.widget.AdapterView#getCount()
	 */
	@Override
	public int getCount() {
		return mItemCount;
	}

	/**
	 * Maps a point to a position in the list.
	 * 
	 * @param x
	 *           X in local coordinate
	 * @param y
	 *           Y in local coordinate
	 * @return The position of the item which contains the specified point, or {@link #INVALID_POSITION} if the point does not
	 *         intersect an item.
	 */
	public int pointToPosition( int x, int y ) {
		Rect frame = mTouchFrame;
		if ( frame == null ) {
			mTouchFrame = new Rect();
			frame = mTouchFrame;
		}

		final int count = getChildCount();
		for ( int i = count - 1; i >= 0; i-- ) {
			View child = getChildAt( i );
			if ( child.getVisibility() == View.VISIBLE ) {
				child.getHitRect( frame );
				if ( frame.contains( x, y ) ) {
					return mFirstPosition + i;
				}
			}
		}
		return INVALID_POSITION;
	}

	/**
	 * The Class SavedState.
	 */
	static class SavedState extends BaseSavedState {

		/** The selected id. */
		long selectedId;

		/** The position. */
		int position;

		/**
		 * Constructor called from {@link AbsSpinner#onSaveInstanceState()}.
		 * 
		 * @param superState
		 *           the super state
		 */
		SavedState( Parcelable superState ) {
			super( superState );
		}

		/**
		 * Constructor called from {@link #CREATOR}.
		 * 
		 * @param in
		 *           the in
		 */
		private SavedState( Parcel in ) {
			super( in );
			selectedId = in.readLong();
			position = in.readInt();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.view.AbsSavedState#writeToParcel(android.os.Parcel, int)
		 */
		@Override
		public void writeToParcel( Parcel out, int flags ) {
			super.writeToParcel( out, flags );
			out.writeLong( selectedId );
			out.writeInt( position );
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "AbsSpinner.SavedState{" + Integer.toHexString( System.identityHashCode( this ) ) + " selectedId=" + selectedId
					+ " position=" + position + "}";
		}

		/** The Constant CREATOR. */
		public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {

			public SavedState createFromParcel( Parcel in ) {
				return new SavedState( in );
			}

			public SavedState[] newArray( int size ) {
				return new SavedState[size];
			}
		};
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View#onSaveInstanceState()
	 */
	@Override
	public Parcelable onSaveInstanceState() {
		Parcelable superState = super.onSaveInstanceState();
		SavedState ss = new SavedState( superState );
		ss.selectedId = getSelectedItemId();
		if ( ss.selectedId >= 0 ) {
			ss.position = getSelectedItemPosition();
		} else {
			ss.position = INVALID_POSITION;
		}
		return ss;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View#onRestoreInstanceState(android.os.Parcelable)
	 */
	@Override
	public void onRestoreInstanceState( Parcelable state ) {
		SavedState ss = (SavedState) state;

		super.onRestoreInstanceState( ss.getSuperState() );

		if ( ss.selectedId >= 0 ) {
			mDataChanged = true;
			mNeedSync = true;
			mSyncRowId = ss.selectedId;
			mSyncPosition = ss.position;
			mSyncMode = SYNC_SELECTED_POSITION;
			requestLayout();
		}
	}

	/**
	 * The Class RecycleBin.
	 */
	class RecycleBin {

		/** The m scrap heap. */
		@SuppressWarnings("unused")
		private final SparseArray<View> mScrapHeap = new SparseArray<View>();

		/** The m heap. */
		private final ArrayList<View> mHeap = new ArrayList<View>( 100 );

		/**
		 * Put.
		 * 
		 * @param position
		 *           the position
		 * @param v
		 *           the v
		 */
		public void put( int position, View v ) {
			mHeap.add( v );
			// mScrapHeap.put( position, v );
		}

		/**
		 * Gets the.
		 * 
		 * @param position
		 *           the position
		 * @return the view
		 */
		View get( int position ) {
			if ( mHeap.size() < 1 ) return null;
			View result = mHeap.remove( 0 );
			return result;

			/*
			 * View result = mScrapHeap.get( position ); if ( result != null ) { mScrapHeap.delete( position ); } else { } return
			 * result;
			 */
		}

		/**
		 * Clear.
		 */
		void clear() {
			/*
			 * final SparseArray<View> scrapHeap = mScrapHeap; final int count = scrapHeap.size(); for ( int i = 0; i < count; i++ ) {
			 * final View view = scrapHeap.valueAt( i ); if ( view != null ) { removeDetachedView( view, true ); } } scrapHeap.clear();
			 */

			final int count = mHeap.size();
			for ( int i = 0; i < count; i++ ) {
				final View view = mHeap.remove( 0 );
				if ( view != null ) {
					removeDetachedView( view, true );
				}
			}
			mHeap.clear();
		}
	}
}
