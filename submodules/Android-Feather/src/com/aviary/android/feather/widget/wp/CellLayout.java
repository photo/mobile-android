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

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.ContextMenu;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import com.aviary.android.feather.R;
import com.aviary.android.feather.library.log.LoggerFactory;
import com.aviary.android.feather.library.log.LoggerFactory.Logger;
import com.aviary.android.feather.library.log.LoggerFactory.LoggerType;

// TODO: Auto-generated Javadoc
/**
 * The Class CellLayout.
 */
public class CellLayout extends ViewGroup {

	/** The m cell width. */
	private int mCellWidth;

	/** The m cell height. */
	private int mCellHeight;

	/** The m start padding. */
	private int mStartPadding;

	/** The m end padding. */
	private int mEndPadding;

	/** The m top padding. */
	private int mTopPadding;

	/** The m bottom padding. */
	private int mBottomPadding;

	/** The m axis rows. */
	private int mAxisRows;

	/** The m axis cells. */
	private int mAxisCells;

	/** The m width gap. */
	private int mWidthGap;

	/** The m height gap. */
	private int mHeightGap;

	/** The m cell padding h. */
	private int mCellPaddingH;

	/** The m cell padding v. */
	private int mCellPaddingV;

	/** The m cell info. */
	public final CellInfo mCellInfo = new CellInfo();

	/** The m cell xy. */
	int[] mCellXY = new int[2];

	/** The m occupied. */
	boolean[][] mOccupied;

	/** The m last down on occupied cell. */
	private boolean mLastDownOnOccupiedCell = false;

	/** The logger. */
	@SuppressWarnings("unused")
	private static Logger logger;

	/**
	 * Instantiates a new cell layout.
	 * 
	 * @param context
	 *           the context
	 */
	public CellLayout( Context context ) {
		this( context, null );
	}

	/**
	 * Instantiates a new cell layout.
	 * 
	 * @param context
	 *           the context
	 * @param attrs
	 *           the attrs
	 */
	public CellLayout( Context context, AttributeSet attrs ) {
		this( context, attrs, 0 );
	}

	/**
	 * Instantiates a new cell layout.
	 * 
	 * @param context
	 *           the context
	 * @param attrs
	 *           the attrs
	 * @param defStyle
	 *           the def style
	 */
	public CellLayout( Context context, AttributeSet attrs, int defStyle ) {
		super( context, attrs, defStyle );

		logger = LoggerFactory.getLogger( "CellLayout", LoggerType.ConsoleLoggerType );

		TypedArray a = context.obtainStyledAttributes( attrs, R.styleable.CellLayout, defStyle, 0 );

		mCellPaddingH = a.getDimensionPixelSize( R.styleable.CellLayout_horizontalPadding, 0 );
		mCellPaddingV = a.getDimensionPixelSize( R.styleable.CellLayout_horizontalPadding, 0 );

		mStartPadding = a.getDimensionPixelSize( R.styleable.CellLayout_startPadding, 0 );
		mEndPadding = a.getDimensionPixelSize( R.styleable.CellLayout_endPadding, 0 );
		mTopPadding = a.getDimensionPixelSize( R.styleable.CellLayout_topPadding, 0 );
		mBottomPadding = a.getDimensionPixelSize( R.styleable.CellLayout_bottomPadding, 0 );

		// logger.log( "padding", mStartPadding, mEndPadding, mTopPadding, mBottomPadding );

		mAxisCells = a.getInt( R.styleable.CellLayout_cells, 4 );
		mAxisRows = a.getInt( R.styleable.CellLayout_rows, 1 );

		a.recycle();

		//setAlwaysDrawnWithCacheEnabled( false );

		resetCells();
	}

	/**
	 * Reset cells.
	 */
	private void resetCells() {
		mOccupied = new boolean[mAxisCells][mAxisRows];
	}

	/**
	 * Sets the num cols.
	 * 
	 * @param value
	 *           the new num cols
	 */
	public void setNumCols( int value ) {
		if ( mAxisCells != value ) {
			mAxisCells = value;
			resetCells();
		}
	}

	/**
	 * Sets the num rows.
	 * 
	 * @param value
	 *           the new num rows
	 */
	public void setNumRows( int value ) {
		if ( value != mAxisRows ) {
			mAxisRows = value;
			resetCells();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.ViewGroup#removeAllViews()
	 */
	@Override
	public void removeAllViews() {
		super.removeAllViews();
		mOccupied = new boolean[mAxisCells][mAxisRows];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View#cancelLongPress()
	 */
	@Override
	public void cancelLongPress() {
		super.cancelLongPress();

		// Cancel long press for all children
		final int count = getChildCount();
		for ( int i = 0; i < count; i++ ) {
			final View child = getChildAt( i );
			child.cancelLongPress();
		}
	}

	/**
	 * Gets the count x.
	 * 
	 * @return the count x
	 */
	int getCountX() {
		return mAxisCells;
	}

	/**
	 * Gets the count y.
	 * 
	 * @return the count y
	 */
	int getCountY() {
		return mAxisRows;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.ViewGroup#addView(android.view.View, int, android.view.ViewGroup.LayoutParams)
	 */
	@Override
	public void addView( View child, int index, ViewGroup.LayoutParams params ) {
		final LayoutParams cellParams = (LayoutParams) params;
		// logger.info( "addView: (cellX=" + cellParams.cellX + ", cellY=" + cellParams.cellY + ", spanH=" + cellParams.cellHSpan +
		// ", spanV=" + cellParams.cellVSpan + ")" );
		cellParams.regenerateId = true;

		mOccupied[cellParams.cellX][cellParams.cellY] = true;

		super.addView( child, index, params );
	}

	/**
	 * Find vacant cell.
	 * 
	 * @return the cell info
	 */
	public CellInfo findVacantCell() {
		return findVacantCell( 1, 1 );
	}

	/**
	 * Find vacant cell.
	 * 
	 * @param spanH
	 *           the span h
	 * @param spanV
	 *           the span v
	 * @return the cell info
	 */
	public CellInfo findVacantCell( int spanH, int spanV ) {
		if ( spanH > mAxisCells ) {
			return null;
		}
		if ( spanV > mAxisRows ) {
			return null;
		}

		for ( int x = 0; x < mOccupied.length; x++ ) {
			for ( int y = 0; y < mOccupied[x].length; y++ ) {
				if ( findVacantCell( x, y, spanH, spanV ) ) {
					CellInfo info = new CellInfo();
					info.cellX = x;
					info.cellY = y;
					info.spanH = spanH;
					info.spanV = spanV;
					info.screen = mCellInfo.screen;
					return info;
				}
			}
		}

		return null;
	}

	/**
	 * Find vacant cell.
	 * 
	 * @param x
	 *           the x
	 * @param y
	 *           the y
	 * @param spanH
	 *           the span h
	 * @param spanV
	 *           the span v
	 * @return true, if successful
	 */
	private boolean findVacantCell( int x, int y, int spanH, int spanV ) {

		if ( x < mOccupied.length ) {
			if ( y < mOccupied[x].length ) {

				if ( !mOccupied[x][y] ) {

					boolean result = true;

					if ( spanH > 1 ) {
						result = findVacantCell( x + 1, y, spanH - 1, 1 );
					} else {
						result = true;
					}

					if ( spanV > 1 ) {
						result &= findVacantCell( x, y + 1, 1, spanV - 1 );
					} else {
						result &= true;
					}

					if ( result ) return true;

				}
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.ViewGroup#requestChildFocus(android.view.View, android.view.View)
	 */
	@Override
	public void requestChildFocus( View child, View focused ) {
		super.requestChildFocus( child, focused );
		if ( child != null ) {
			Rect r = new Rect();
			child.getDrawingRect( r );
			requestRectangleOnScreen( r );
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
		mCellInfo.screen = ( (ViewGroup) getParent() ).indexOfChild( this );
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.ViewGroup#requestFocus(int, android.graphics.Rect)
	 */
	@Override
	public boolean requestFocus( int direction, Rect previouslyFocusedRect ) {
		return super.requestFocus( direction, previouslyFocusedRect );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View#onTouchEvent(android.view.MotionEvent)
	 */
	@Override
	public boolean onTouchEvent( MotionEvent event ) {
		return true;
	}

	/**
	 * Given a point, return the cell that strictly encloses that point.
	 * 
	 * @param x
	 *           X coordinate of the point
	 * @param y
	 *           Y coordinate of the point
	 * @param result
	 *           Array of 2 ints to hold the x and y coordinate of the cell
	 */
	void pointToCellExact( int x, int y, int[] result ) {

		final int hStartPadding = mStartPadding;
		final int vStartPadding = mTopPadding;

		result[0] = ( x - hStartPadding ) / ( mCellWidth + mWidthGap );
		result[1] = ( y - vStartPadding ) / ( mCellHeight + mHeightGap );

		final int xAxis = mAxisCells;
		final int yAxis = mAxisRows;

		if ( result[0] < 0 ) result[0] = 0;
		if ( result[0] >= xAxis ) result[0] = xAxis - 1;
		if ( result[1] < 0 ) result[1] = 0;
		if ( result[1] >= yAxis ) result[1] = yAxis - 1;
	}

	/**
	 * Given a point, return the cell that most closely encloses that point.
	 * 
	 * @param x
	 *           X coordinate of the point
	 * @param y
	 *           Y coordinate of the point
	 * @param result
	 *           Array of 2 ints to hold the x and y coordinate of the cell
	 */
	void pointToCellRounded( int x, int y, int[] result ) {
		pointToCellExact( x + ( mCellWidth / 2 ), y + ( mCellHeight / 2 ), result );
	}

	/**
	 * Given a cell coordinate, return the point that represents the upper left corner of that cell.
	 * 
	 * @param cellX
	 *           X coordinate of the cell
	 * @param cellY
	 *           Y coordinate of the cell
	 * @param result
	 *           Array of 2 ints to hold the x and y coordinate of the point
	 */
	void cellToPoint( int cellX, int cellY, int[] result ) {
		final int hStartPadding = mStartPadding;
		final int vStartPadding = mTopPadding;
		result[0] = hStartPadding + cellX * ( mCellWidth + mWidthGap );
		result[1] = vStartPadding + cellY * ( mCellHeight + mHeightGap );
	}

	/**
	 * Gets the cell width.
	 * 
	 * @return the cell width
	 */
	public int getCellWidth() {
		return mCellWidth;
	}

	/**
	 * Gets the cell height.
	 * 
	 * @return the cell height
	 */
	public int getCellHeight() {
		return mCellHeight;
	}

	/**
	 * Gets the left padding.
	 * 
	 * @return the left padding
	 */
	public int getLeftPadding() {
		return mStartPadding;
	}

	/**
	 * Gets the top padding.
	 * 
	 * @return the top padding
	 */
	public int getTopPadding() {
		return mTopPadding;
	}

	/**
	 * Gets the right padding.
	 * 
	 * @return the right padding
	 */
	public int getRightPadding() {
		return mEndPadding;
	}

	/**
	 * Gets the bottom padding.
	 * 
	 * @return the bottom padding
	 */
	public int getBottomPadding() {
		return mBottomPadding;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View#onMeasure(int, int)
	 */
	@Override
	protected void onMeasure( int widthMeasureSpec, int heightMeasureSpec ) {
		int widthSpecMode = MeasureSpec.getMode( widthMeasureSpec );
		int width = MeasureSpec.getSize( widthMeasureSpec );

		int heightSpecMode = MeasureSpec.getMode( heightMeasureSpec );
		int height = MeasureSpec.getSize( heightMeasureSpec );

		if ( widthSpecMode == MeasureSpec.UNSPECIFIED || heightSpecMode == MeasureSpec.UNSPECIFIED ) {
			throw new RuntimeException( "CellLayout cannot have UNSPECIFIED dimensions" );
		}

		int numHGaps = mAxisCells - 1;
		int numVGaps = mAxisRows - 1;

		int totalHGap = numHGaps * mCellPaddingH;
		int totalVGap = numVGaps * mCellPaddingV;

		int availableWidth = width - ( mStartPadding + mEndPadding );
		int availableHeight = height - ( mTopPadding + mBottomPadding );

		mCellWidth = ( ( availableWidth - totalHGap ) / mAxisCells );
		mCellHeight = ( ( availableHeight - totalVGap ) / mAxisRows );

		mHeightGap = 0;
		mWidthGap = 0;

		int vTotalSpace = availableHeight - ( mCellHeight * mAxisRows );
		try {
			mHeightGap = vTotalSpace / numVGaps;
		} catch ( ArithmeticException e ) {}

		int hTotalSpace = availableWidth - ( mCellWidth * mAxisCells );
		if ( numHGaps > 0 ) {
			mWidthGap = hTotalSpace / numHGaps;
		}

		int count = getChildCount();

		// logger.log( "childCount: " + count );

		for ( int i = 0; i < count; i++ ) {
			View child = getChildAt( i );
			LayoutParams lp = (LayoutParams) child.getLayoutParams();

			lp.setup( mCellWidth, mCellHeight, mWidthGap, mHeightGap, mStartPadding, mTopPadding );

			if ( lp.regenerateId ) {
				child.setId( ( ( getId() & 0xFF ) << 16 ) | ( lp.cellX & 0xFF ) << 8 | ( lp.cellY & 0xFF ) );
				lp.regenerateId = false;
			}

			int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec( lp.width, MeasureSpec.EXACTLY );
			int childheightMeasureSpec = MeasureSpec.makeMeasureSpec( lp.height, MeasureSpec.EXACTLY );
			child.measure( childWidthMeasureSpec, childheightMeasureSpec );
		}
		setMeasuredDimension( width, height );
		// logger.log( "size: ", width, height );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.ViewGroup#onLayout(boolean, int, int, int, int)
	 */
	@Override
	protected void onLayout( boolean changed, int l, int t, int r, int b ) {
		int count = getChildCount();

		for ( int i = 0; i < count; i++ ) {
			View child = getChildAt( i );
			if ( child.getVisibility() != GONE ) {

				CellLayout.LayoutParams lp = (CellLayout.LayoutParams) child.getLayoutParams();
				int childLeft = lp.x;
				int childTop = lp.y;
				child.layout( childLeft, childTop, childLeft + lp.width, childTop + lp.height );
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.ViewGroup#setChildrenDrawingCacheEnabled(boolean)
	 */
	@Override
	public void setChildrenDrawingCacheEnabled( boolean enabled ) {
		logger.info( "setChildrenDrawingCacheEnabled: " + enabled );
		setDrawingCacheEnabled( enabled );
		final int count = getChildCount();
		for ( int i = 0; i < count; i++ ) {
			final View view = getChildAt( i );
			view.setDrawingCacheEnabled( enabled );
			view.buildDrawingCache( true );
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.ViewGroup#setChildrenDrawnWithCacheEnabled(boolean)
	 */
	@Override
	public void setChildrenDrawnWithCacheEnabled( boolean enabled ) {
		super.setChildrenDrawnWithCacheEnabled( enabled );
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

	/**
	 * Computes a bounding rectangle for a range of cells.
	 * 
	 * @param cellX
	 *           X coordinate of upper left corner expressed as a cell position
	 * @param cellY
	 *           Y coordinate of upper left corner expressed as a cell position
	 * @param cellHSpan
	 *           Width in cells
	 * @param cellVSpan
	 *           Height in cells
	 * @param dragRect
	 *           Rectnagle into which to put the results
	 */
	public void cellToRect( int cellX, int cellY, int cellHSpan, int cellVSpan, RectF dragRect ) {
		final int cellWidth = mCellWidth;
		final int cellHeight = mCellHeight;
		final int widthGap = mWidthGap;
		final int heightGap = mHeightGap;
		final int hStartPadding = mStartPadding;
		final int vStartPadding = mTopPadding;

		int width = cellHSpan * cellWidth + ( ( cellHSpan - 1 ) * widthGap );
		int height = cellVSpan * cellHeight + ( ( cellVSpan - 1 ) * heightGap );

		int x = hStartPadding + cellX * ( cellWidth + widthGap );
		int y = vStartPadding + cellY * ( cellHeight + heightGap );

		dragRect.set( x, y, x + width, y + height );
	}

	/**
	 * Find the first vacant cell, if there is one.
	 * 
	 * @param vacant
	 *           Holds the x and y coordinate of the vacant cell
	 * @param spanX
	 *           Horizontal cell span.
	 * @param spanY
	 *           Vertical cell span.
	 * 
	 * @return True if a vacant cell was found
	 */
	public boolean getVacantCell( int[] vacant, int spanX, int spanY ) {
		final int xCount = mAxisCells;
		final int yCount = mAxisRows;
		final boolean[][] occupied = mOccupied;
		findOccupiedCells( xCount, yCount, occupied, null );
		return findVacantCell( vacant, spanX, spanY, xCount, yCount, occupied );
	}

	/**
	 * Find vacant cell.
	 * 
	 * @param vacant
	 *           the vacant
	 * @param spanX
	 *           the span x
	 * @param spanY
	 *           the span y
	 * @param xCount
	 *           the x count
	 * @param yCount
	 *           the y count
	 * @param occupied
	 *           the occupied
	 * @return true, if successful
	 */
	static boolean findVacantCell( int[] vacant, int spanX, int spanY, int xCount, int yCount, boolean[][] occupied ) {

		for ( int x = 0; x < xCount; x++ ) {
			for ( int y = 0; y < yCount; y++ ) {
				boolean available = !occupied[x][y];
				out:
				for ( int i = x; i < x + spanX - 1 && x < xCount; i++ ) {
					for ( int j = y; j < y + spanY - 1 && y < yCount; j++ ) {
						available = available && !occupied[i][j];
						if ( !available ) break out;
					}
				}

				if ( available ) {
					vacant[0] = x;
					vacant[1] = y;
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * Gets the occupied cells.
	 * 
	 * @return the occupied cells
	 */
	boolean[] getOccupiedCells() {
		final int xCount = mAxisCells;
		final int yCount = mAxisRows;
		final boolean[][] occupied = mOccupied;
		findOccupiedCells( xCount, yCount, occupied, null );
		final boolean[] flat = new boolean[xCount * yCount];
		for ( int y = 0; y < yCount; y++ ) {
			for ( int x = 0; x < xCount; x++ ) {
				flat[y * xCount + x] = occupied[x][y];
			}
		}
		return flat;
	}

	/**
	 * Find occupied cells.
	 * 
	 * @param xCount
	 *           the x count
	 * @param yCount
	 *           the y count
	 * @param occupied
	 *           the occupied
	 * @param ignoreView
	 *           the ignore view
	 */
	private void findOccupiedCells( int xCount, int yCount, boolean[][] occupied, View ignoreView ) {
		for ( int x = 0; x < xCount; x++ ) {
			for ( int y = 0; y < yCount; y++ ) {
				occupied[x][y] = false;
			}
		}

		int count = getChildCount();
		for ( int i = 0; i < count; i++ ) {
			View child = getChildAt( i );
			if ( child.equals( ignoreView ) ) {
				continue;
			}
			LayoutParams lp = (LayoutParams) child.getLayoutParams();

			for ( int x = lp.cellX; x < lp.cellX + lp.cellHSpan && x < xCount; x++ ) {
				for ( int y = lp.cellY; y < lp.cellY + lp.cellVSpan && y < yCount; y++ ) {
					occupied[x][y] = true;
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.ViewGroup#generateLayoutParams(android.util.AttributeSet)
	 */
	@Override
	public ViewGroup.LayoutParams generateLayoutParams( AttributeSet attrs ) {
		return new CellLayout.LayoutParams( getContext(), attrs );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.ViewGroup#checkLayoutParams(android.view.ViewGroup.LayoutParams)
	 */
	@Override
	protected boolean checkLayoutParams( ViewGroup.LayoutParams p ) {
		return p instanceof CellLayout.LayoutParams;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.ViewGroup#generateLayoutParams(android.view.ViewGroup.LayoutParams)
	 */
	@Override
	protected ViewGroup.LayoutParams generateLayoutParams( ViewGroup.LayoutParams p ) {
		return new CellLayout.LayoutParams( p );
	}

	/**
	 * The Class LayoutParams.
	 */
	public static class LayoutParams extends ViewGroup.MarginLayoutParams {

		/**
		 * Horizontal location of the item in the grid.
		 */
		public int cellX;

		/**
		 * Vertical location of the item in the grid.
		 */
		public int cellY;

		/**
		 * Number of cells spanned horizontally by the item.
		 */
		public int cellHSpan;

		/**
		 * Number of cells spanned vertically by the item.
		 */
		public int cellVSpan;

		// X coordinate of the view in the layout.
		/** The x. */
		int x;

		// Y coordinate of the view in the layout.
		/** The y. */
		int y;

		/** The regenerate id. */
		boolean regenerateId;

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
			cellHSpan = 1;
			cellVSpan = 1;
			cellX = -1;
			cellY = -1;
		}

		/**
		 * Instantiates a new layout params.
		 * 
		 * @param source
		 *           the source
		 */
		public LayoutParams( ViewGroup.LayoutParams source ) {
			super( source );
			cellHSpan = 1;
			cellVSpan = 1;
			cellX = -1;
			cellY = -1;
		}

		/**
		 * Instantiates a new layout params.
		 * 
		 * @param cellX
		 *           the cell x
		 * @param cellY
		 *           the cell y
		 * @param cellHSpan
		 *           the cell h span
		 * @param cellVSpan
		 *           the cell v span
		 */
		public LayoutParams( int cellX, int cellY, int cellHSpan, int cellVSpan ) {
			super( LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT );
			this.cellX = cellX;
			this.cellY = cellY;
			this.cellHSpan = cellHSpan;
			this.cellVSpan = cellVSpan;
		}

		/**
		 * Setup.
		 * 
		 * @param cellWidth
		 *           the cell width
		 * @param cellHeight
		 *           the cell height
		 * @param widthGap
		 *           the width gap
		 * @param heightGap
		 *           the height gap
		 * @param hStartPadding
		 *           the h start padding
		 * @param vStartPadding
		 *           the v start padding
		 */
		public void setup( int cellWidth, int cellHeight, int widthGap, int heightGap, int hStartPadding, int vStartPadding ) {
			final int myCellHSpan = cellHSpan;
			final int myCellVSpan = cellVSpan;
			final int myCellX = cellX;
			final int myCellY = cellY;

			width = myCellHSpan * cellWidth + ( ( myCellHSpan - 1 ) * widthGap ) - leftMargin - rightMargin;
			height = myCellVSpan * cellHeight + ( ( myCellVSpan - 1 ) * heightGap ) - topMargin - bottomMargin;

			x = hStartPadding + myCellX * ( cellWidth + widthGap ) + leftMargin;
			y = vStartPadding + myCellY * ( cellHeight + heightGap ) + topMargin;

			// logger.info( "setup. position: " + x + "x" + y + ", size: " + width + "x" + height + " gap: " + widthGap + "x" +
			// heightGap );
		}
	}

	/**
	 * The Class CellInfo.
	 */
	static public final class CellInfo implements ContextMenu.ContextMenuInfo {

		/** The cell. */
		View cell;

		/** The cell x. */
		public int cellX;

		/** The cell y. */
		public int cellY;

		/** The span h. */
		public int spanH;

		/** The span v. */
		public int spanV;

		/** The screen. */
		public int screen;

		/** The valid. */
		boolean valid;

		/** The current. */
		final Rect current = new Rect();

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "Cell[view=" + ( cell == null ? "null" : cell.getClass() ) + ", x=" + cellX + "]";
		}

		/**
		 * Clear vacant cells.
		 */
		public void clearVacantCells() {
			// TODO: implement this method
		}
	}

	/**
	 * Last down on occupied cell.
	 * 
	 * @return true, if successful
	 */
	public boolean lastDownOnOccupiedCell() {
		return mLastDownOnOccupiedCell;
	}
}
