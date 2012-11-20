package com.aviary.android.feather.widget;

import it.sephiroth.android.library.imagezoom.easing.Easing;
import it.sephiroth.android.library.imagezoom.easing.Quad;
import android.graphics.BlurMaskFilter;
import android.graphics.BlurMaskFilter.Blur;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.view.View;
import com.aviary.android.feather.R;
import com.aviary.android.feather.library.utils.ReflectionUtils;
import com.aviary.android.feather.library.utils.ReflectionUtils.ReflectionException;

// TODO: Auto-generated Javadoc
/**
 * The Class HighlightView.
 */
public class HighlightView {

	/** The Constant LOG_TAG. */
	@SuppressWarnings("unused")
	private static final String LOG_TAG = "hv";

	/** The Constant GROW_NONE. */
	static final int GROW_NONE = 1 << 0;

	/** The Constant GROW_LEFT_EDGE. */
	static final int GROW_LEFT_EDGE = 1 << 1;

	/** The Constant GROW_RIGHT_EDGE. */
	static final int GROW_RIGHT_EDGE = 1 << 2;

	/** The Constant GROW_TOP_EDGE. */
	static final int GROW_TOP_EDGE = 1 << 3;

	/** The Constant GROW_BOTTOM_EDGE. */
	static final int GROW_BOTTOM_EDGE = 1 << 4;

	/** The Constant MOVE. */
	static final int MOVE = 1 << 5;

	/** The m hidden. */
	private boolean mHidden;

	/** The m context. */
	private View mContext;

	private static Handler mHandler = new Handler();

	/**
	 * The Enum Mode.
	 */
	enum Mode {

		/** The None. */
		None,
		/** The Move. */
		Move,
		/** The Grow. */
		Grow
	}

	/** The m min size. */
	private int mMinSize = 20;

	/** The m mode. */
	private Mode mMode;

	/** The draw rect. */
	private Rect mDrawRect = new Rect();

	/** The image rect. */
	private RectF mImageRect;

	/** The crop rect. */
	private RectF mCropRect;

	private Matrix mMatrix;

	/** The m maintain aspect ratio. */
	private boolean mMaintainAspectRatio = false;

	/** The m initial aspect ratio. */
	private double mInitialAspectRatio;

	/** The handle knob drawable. */
	private Drawable mResizeDrawable;

	private final Paint mOutlinePaint = new Paint();
	private final Paint mOutlinePaint2 = new Paint();
	private final Paint mOutlineFill = new Paint();
	private Paint mLinesPaintShadow = new Paint();

	/** The highlight_color. */
	private int highlight_color;

	/** The highlight_down_color. */
	private int highlight_down_color;

	/** The highlight_outside_color. */
	private int highlight_outside_color;

	/** The highlight_outside_color_down. */
	private int highlight_outside_color_down;

	/** The internal_stroke_width. */
	private int stroke_width, internal_stroke_width;

	/** internal grid colors */
	private int highlight_internal_color, highlight_internal_color_down;

	/** The d height. */
	private int dWidth, dHeight;

	final int grid_rows = 3;
	final int grid_cols = 3;

	/**
	 * Instantiates a new highlight view.
	 * 
	 * @param ctx
	 *           the ctx
	 */
	public HighlightView( View ctx ) {
		mContext = ctx;
		highlight_color = mContext.getResources().getColor( R.color.feather_crop_highlight );
		highlight_down_color = mContext.getResources().getColor( R.color.feather_crop_highlight_down );
		highlight_outside_color = mContext.getResources().getColor( R.color.feather_crop_highlight_outside );
		highlight_outside_color_down = mContext.getResources().getColor( R.color.feather_crop_highlight_outside_down );
		stroke_width = mContext.getResources().getInteger( R.integer.feather_crop_highlight_stroke_width );
		internal_stroke_width = mContext.getResources().getInteger( R.integer.feather_crop_highlight_internal_stroke_width );
		highlight_internal_color = mContext.getResources().getColor( R.color.feather_crop_highlight_internal );
		highlight_internal_color_down = mContext.getResources().getColor( R.color.feather_crop_highlight_internal_down );
	}

	/**
	 * Inits the.
	 */
	private void init() {
		android.content.res.Resources resources = mContext.getResources();
		mResizeDrawable = resources.getDrawable( R.drawable.feather_highlight_crop_handle );

		double w = mResizeDrawable.getIntrinsicWidth();
		double h = mResizeDrawable.getIntrinsicHeight();

		dWidth = (int) Math.ceil( w / 2.0 );
		dHeight = (int) Math.ceil( h / 2.0 );
	}

	/**
	 * Dispose.
	 */
	public void dispose() {
		mContext = null;
	}

	/**
	 * Sets the min size.
	 * 
	 * @param value
	 *           the new min size
	 */
	public void setMinSize( int value ) {
		mMinSize = value;
	}

	/**
	 * Sets the hidden.
	 * 
	 * @param hidden
	 *           the new hidden
	 */
	public void setHidden( boolean hidden ) {
		mHidden = hidden;
	}

	/** The m view drawing rect. */
	private Rect mViewDrawingRect = new Rect();

	/** The m path. */
	private Path mPath = new Path();

	/** The m lines path. */
	private Path mLinesPath = new Path();

	/** The m inverse path. */
	private Path mInversePath = new Path();

	private RectF tmpRect2 = new RectF();
	private Rect tmpRect4 = new Rect();

	private RectF tmpDrawRect2F = new RectF();
	private RectF tmpDrawRectF = new RectF();
	private RectF tmpDisplayRectF = new RectF();
	private Rect tmpRectMotion = new Rect();
	private RectF tmpRectMotionF = new RectF();
	private RectF tempLayoutRectF = new RectF();

	/**
	 * Draw.
	 * 
	 * @param canvas
	 *           the canvas
	 */
	protected void draw( Canvas canvas ) {
		if ( mHidden ) return;

		// canvas.save();

		mPath.reset();
		mInversePath.reset();
		mLinesPath.reset();

		mContext.getDrawingRect( mViewDrawingRect );

		tmpDrawRectF.set( mDrawRect );
		tmpDrawRect2F.set( mViewDrawingRect );

		mInversePath.addRect( tmpDrawRect2F, Path.Direction.CW );
		mInversePath.addRect( tmpDrawRectF, Path.Direction.CCW );

		tmpDrawRectF.set( mDrawRect );
		mPath.addRect( tmpDrawRectF, Path.Direction.CW );

		tmpDrawRect2F.set( mDrawRect );
		mLinesPath.addRect( tmpDrawRect2F, Path.Direction.CW );

		float colStep = (float) mDrawRect.height() / grid_cols;
		float rowStep = (float) mDrawRect.width() / grid_rows;

		for ( int i = 1; i < grid_cols; i++ ) {
			mLinesPath.moveTo( (int) mDrawRect.left, (int) ( mDrawRect.top + colStep * i ) );
			mLinesPath.lineTo( (int) mDrawRect.right, (int) ( mDrawRect.top + colStep * i ) );
		}

		for ( int i = 1; i < grid_rows; i++ ) {
			mLinesPath.moveTo( (int) ( mDrawRect.left + rowStep * i ), (int) mDrawRect.top );
			mLinesPath.lineTo( (int) ( mDrawRect.left + rowStep * i ), (int) mDrawRect.bottom );
		}

		// canvas.restore();
		canvas.drawPath( mInversePath, mOutlineFill );
		// canvas.drawPath( mLinesPath, mLinesPaintShadow );
		canvas.drawPath( mLinesPath, mOutlinePaint2 );
		canvas.drawPath( mPath, mOutlinePaint );

		if ( true /* || mMode == Mode.Grow */) {
			int left = mDrawRect.left + 1;
			int right = mDrawRect.right + 1;
			int top = mDrawRect.top + 4;
			int bottom = mDrawRect.bottom + 3;
			if ( mResizeDrawable != null ) {

				mResizeDrawable.setBounds( left - dWidth, top - dHeight, left + dWidth, top + dHeight );
				mResizeDrawable.draw( canvas );
				mResizeDrawable.setBounds( right - dWidth, top - dHeight, right + dWidth, top + dHeight );
				mResizeDrawable.draw( canvas );
				mResizeDrawable.setBounds( left - dWidth, bottom - dHeight, left + dWidth, bottom + dHeight );
				mResizeDrawable.draw( canvas );
				mResizeDrawable.setBounds( right - dWidth, bottom - dHeight, right + dWidth, bottom + dHeight );
				mResizeDrawable.draw( canvas );
			}
		}
	}

	/**
	 * Sets the mode.
	 * 
	 * @param mode
	 *           the new mode
	 */
	public void setMode( Mode mode ) {
		if ( mode != mMode ) {
			mMode = mode;
			mOutlinePaint.setColor( mMode == Mode.None ? highlight_color : highlight_down_color );
			mOutlinePaint2.setColor( mMode == Mode.None ? highlight_internal_color : highlight_internal_color_down );
			mLinesPaintShadow.setAlpha( mMode == Mode.None ? 102 : 0 );
			mOutlineFill.setColor( mMode == Mode.None ? highlight_outside_color : highlight_outside_color_down );
			mContext.invalidate();
		}
	}

	/** The hysteresis. */
	final float hysteresis = 30F;

	/**
	 * Gets the hit.
	 * 
	 * @param x
	 *           the x
	 * @param y
	 *           the y
	 * @return the hit
	 */
	public int getHit( float x, float y ) {
		Rect r = new Rect();
		computeLayout( false, r );
		int retval = GROW_NONE;
		boolean verticalCheck = ( y >= r.top - hysteresis ) && ( y < r.bottom + hysteresis );
		boolean horizCheck = ( x >= r.left - hysteresis ) && ( x < r.right + hysteresis );
		if ( ( Math.abs( r.left - x ) < hysteresis ) && verticalCheck ) retval |= GROW_LEFT_EDGE;
		if ( ( Math.abs( r.right - x ) < hysteresis ) && verticalCheck ) retval |= GROW_RIGHT_EDGE;
		if ( ( Math.abs( r.top - y ) < hysteresis ) && horizCheck ) retval |= GROW_TOP_EDGE;
		if ( ( Math.abs( r.bottom - y ) < hysteresis ) && horizCheck ) retval |= GROW_BOTTOM_EDGE;
		if ( retval == GROW_NONE && r.contains( (int) x, (int) y ) ) retval = MOVE;
		return retval;
	}

	boolean isLeftEdge( int edge ) {
		return ( GROW_LEFT_EDGE & edge ) == GROW_LEFT_EDGE;
	}

	boolean isRightEdge( int edge ) {
		return ( GROW_RIGHT_EDGE & edge ) == GROW_RIGHT_EDGE;
	}

	boolean isTopEdge( int edge ) {
		return ( GROW_TOP_EDGE & edge ) == GROW_TOP_EDGE;
	}

	boolean isBottomEdge( int edge ) {
		return ( GROW_BOTTOM_EDGE & edge ) == GROW_BOTTOM_EDGE;
	}

	/**
	 * Handle motion.
	 * 
	 * @param edge
	 *           the edge
	 * @param dx
	 *           the dx
	 * @param dy
	 *           the dy
	 */
	void handleMotion( int edge, float dx, float dy ) {
		if ( mRunning ) return;
		computeLayout( false, tmpRect4 );
		if ( edge == GROW_NONE ) {
			return;
		} else if ( edge == MOVE ) {
			moveBy( dx * ( mCropRect.width() / tmpRect4.width() ), dy * ( mCropRect.height() / tmpRect4.height() ) );
		} else {
			if ( ( ( GROW_LEFT_EDGE | GROW_RIGHT_EDGE ) & edge ) == 0 ) dx = 0;
			if ( ( ( GROW_TOP_EDGE | GROW_BOTTOM_EDGE ) & edge ) == 0 ) dy = 0;

			// Convert to image space before sending to growBy().
			double xDelta = Math.round( dx * ( mCropRect.width() / tmpRect4.width() ) );
			double yDelta = Math.round( dy * ( mCropRect.height() / tmpRect4.height() ) );
			if ( mMaintainAspectRatio ) {
				growWithConstantAspectSize( edge, xDelta, yDelta );
			} else {
				growWithoutConstantAspectSize( edge, xDelta, yDelta );
			}
		}
	}

	double calculateDy( double dx, double dy ) {
		double ndy = dy;
		if ( dx != 0 ) {
			ndy = ( dx / mInitialAspectRatio );
			if ( dy != 0 ) {
				if ( dy > 0 ) {
					ndy = Math.abs( ndy );
				} else {
					ndy = Math.abs( ndy ) * -1;
				}
			}
			dy = ndy;
		}
		return ndy;
	}

	double calculateDx( double dy, double dx ) {
		double ndx = dx;
		if ( dy != 0 ) {
			ndx = ( dy * mInitialAspectRatio );
			if ( dx != 0 ) {
				if ( dx > 0 ) {
					ndx = Math.abs( ndx );
				} else {
					ndx = Math.abs( ndx ) * -1;
				}
			}
			dx = ndx;
		}
		return ndx;
	}

	/**
	 * Grow only one handle
	 * 
	 * @param dx
	 * @param dy
	 */
	void growWithConstantAspectSize( int edge, double dx, double dy ) {

		final boolean left = isLeftEdge( edge );
		final boolean right = isRightEdge( edge );
		final boolean top = isTopEdge( edge );
		final boolean bottom = isBottomEdge( edge );
		final boolean horizontal = left || right;
		final boolean vertical = top || bottom;
		final boolean singleSide = !( horizontal && vertical );

		// check minimum size and outset the rectangle as needed
		final double widthCap = (double) mMinSize / getScale();
		double ndx, ndy;

		tmpRectMotionF.set( mCropRect );

		if ( singleSide ) {
			if ( horizontal ) {
				// horizontal only
				ndx = dx;
				ndy = calculateDy( ndx, 0 );

				if ( left ) {
					tmpRectMotionF.left += ndx;
					tmpRectMotionF.inset( 0, (float) ( ndy / 2 ) );
				} else {
					tmpRectMotionF.right += ndx;
					tmpRectMotionF.inset( 0, (float) ( -ndy / 2 ) );
				}

			} else {
				// vertical only
				ndy = dy;
				ndx = calculateDx( ndy, 0 );
				if ( top ) {
					tmpRectMotionF.top += ndy;
					tmpRectMotionF.inset( (float) ( ndx / 2 ), 0 );
				} else if ( bottom ) {
					tmpRectMotionF.bottom += ndy;
					tmpRectMotionF.inset( (float) ( -ndx / 2 ), 0 );
				}
			}

		} else {
			// both horizontal & vertical
			ndx = dx;
			ndy = calculateDy( dx, 0 );

			if ( left && top ) {
				tmpRectMotionF.left += ndx;
				tmpRectMotionF.top += ndy;
			} else if ( left && bottom ) {
				tmpRectMotionF.left += ndx;
				tmpRectMotionF.bottom -= ndy;
			} else if ( right && top ) {
				tmpRectMotionF.right += ndx;
				tmpRectMotionF.top -= ndy;
			} else if ( right && bottom ) {
				tmpRectMotionF.right += ndx;
				tmpRectMotionF.bottom += ndy;
			}
		}

		if ( tmpRectMotionF.width() >= widthCap && tmpRectMotionF.height() >= widthCap && mImageRect.contains( tmpRectMotionF ) ) {
			mCropRect.set( tmpRectMotionF );
		}

		computeLayout( true, mDrawRect );
		mContext.invalidate();
	}

	/**
	 * Grow only one handle
	 * 
	 * @param dx
	 * @param dy
	 */
	void growWithoutConstantAspectSize( int edge, double dx, double dy ) {

		final boolean left = isLeftEdge( edge );
		final boolean right = isRightEdge( edge );
		final boolean top = isTopEdge( edge );
		final boolean bottom = isBottomEdge( edge );
		final boolean horizontal = left || right;
		final boolean vertical = top || bottom;

		// check minimum size and outset the rectangle as needed
		final double widthCap = (double) mMinSize / getScale();

		tmpRectMotionF.set( mCropRect );

		double ndy = dy;
		double ndx = dx;

		if ( horizontal ) {

			if ( left ) {
				tmpRectMotionF.left += ndx;
				if ( !vertical ) tmpRectMotionF.inset( 0, (float) ( ndy / 2 ) );
			} else if ( right ) {
				tmpRectMotionF.right += ndx;
				if ( !vertical ) tmpRectMotionF.inset( 0, (float) ( -ndy / 2 ) );
			}
		}

		if ( vertical ) {

			if ( top ) {
				tmpRectMotionF.top += ndy;
				if ( !horizontal ) tmpRectMotionF.inset( (float) ( ndx / 2 ), 0 );
			} else if ( bottom ) {
				tmpRectMotionF.bottom += ndy;
				if ( !horizontal ) tmpRectMotionF.inset( (float) ( -ndx / 2 ), 0 );
			}
		}

		if ( tmpRectMotionF.width() >= widthCap && tmpRectMotionF.height() >= widthCap && mImageRect.contains( tmpRectMotionF ) ) {
			mCropRect.set( tmpRectMotionF );
		}

		computeLayout( true, mDrawRect );
		mContext.invalidate();
	}

	/**
	 * Move by.
	 * 
	 * @param dx
	 *           the dx
	 * @param dy
	 *           the dy
	 */
	void moveBy( float dx, float dy ) {

		tmpRectMotion.set( mDrawRect );
		mCropRect.offset( dx, dy );
		mCropRect.offset( Math.max( 0, mImageRect.left - mCropRect.left ), Math.max( 0, mImageRect.top - mCropRect.top ) );
		mCropRect.offset( Math.min( 0, mImageRect.right - mCropRect.right ), Math.min( 0, mImageRect.bottom - mCropRect.bottom ) );

		computeLayout( false, mDrawRect );

		tmpRectMotion.union( mDrawRect );
		tmpRectMotion.inset( -dWidth * 2, -dHeight * 2 );
		mContext.invalidate( tmpRectMotion );
	}

	/**
	 * Gets the scale.
	 * 
	 * @return the scale
	 */
	protected float getScale() {
		float values[] = new float[9];
		mMatrix.getValues( values );
		return values[Matrix.MSCALE_X];
	}

	/**
	 * @deprecated Old grow behavior
	 */
	void growBy( double dx, double dy ) {

		if ( mMaintainAspectRatio ) {
			if ( dx != 0 ) {
				dy = dx / mInitialAspectRatio;
			} else if ( dy != 0 ) {
				dx = dy * mInitialAspectRatio;
			}
		}

		tmpRectMotionF.set( mCropRect );
		if ( dx > 0F && tmpRectMotionF.width() + 2 * dx > mImageRect.width() ) {
			float adjustment = ( mImageRect.width() - tmpRectMotionF.width() ) / 2F;
			dx = adjustment;
			if ( mMaintainAspectRatio ) {
				dy = dx / mInitialAspectRatio;
			}
		}
		if ( dy > 0F && tmpRectMotionF.height() + 2 * dy > mImageRect.height() ) {
			float adjustment = ( mImageRect.height() - tmpRectMotionF.height() ) / 2F;
			dy = adjustment;
			if ( mMaintainAspectRatio ) {
				dx = dy * mInitialAspectRatio;
			}
		}
		tmpRectMotionF.inset( (float) -dx, (float) -dy );

		// check minimum size and outset the rectangle as needed
		final double widthCap = (double) mMinSize / getScale();

		if ( tmpRectMotionF.width() < widthCap ) {
			tmpRectMotionF.inset( (float) ( -( widthCap - tmpRectMotionF.width() ) / 2F ), 0F );
		}

		double heightCap = mMaintainAspectRatio ? ( widthCap / mInitialAspectRatio ) : widthCap;
		if ( tmpRectMotionF.height() < heightCap ) {
			tmpRectMotionF.inset( 0F, (float) ( -( heightCap - tmpRectMotionF.height() ) / 2F ) );
		}

		mCropRect.set( tmpRectMotionF );
		computeLayout( true, mDrawRect );
		mContext.invalidate();
	}

	/**
	 * Adjust crop rect based on the current image.
	 * 
	 * @param r
	 *           - The {@link Rect} to be adjusted
	 */
	private void adjustCropRect( RectF r ) {
		// Log.i( LOG_TAG, "adjustCropRect: " + r + ", mImageRect: " + mImageRect );

		if ( r.left < mImageRect.left ) {
			r.offset( mImageRect.left - r.left, 0F );
		} else if ( r.right > mImageRect.right ) {
			r.offset( -( r.right - mImageRect.right ), 0 );
		}

		if ( r.top < mImageRect.top ) {
			r.offset( 0F, mImageRect.top - r.top );
		} else if ( r.bottom > mImageRect.bottom ) {
			r.offset( 0F, -( r.bottom - mImageRect.bottom ) );
		}

		double diffx = -1, diffy = -1;

		if ( r.width() > mImageRect.width() ) {

			if ( r.left < mImageRect.left ) {
				diffx = mImageRect.left - r.left;
				r.left += diffx;
			} else if ( r.right > mImageRect.right ) {
				diffx = ( r.right - mImageRect.right );
				r.right += -diffx;
			}

		} else if ( r.height() > mImageRect.height() ) {
			if ( r.top < mImageRect.top ) {
				// top
				diffy = mImageRect.top - r.top;
				r.top += diffy;

			} else if ( r.bottom > mImageRect.bottom ) {
				// bottom
				diffy = ( r.bottom - mImageRect.bottom );
				r.bottom += -diffy;
			}
		}

		if ( mMaintainAspectRatio ) {
			// Log.d( LOG_TAG, "diffx: " + diffx + ", diffy: " + diffy );
			if ( diffy != -1 ) {
				diffx = diffy * mInitialAspectRatio;
				r.inset( (float) ( diffx / 2.0 ), 0 );
			} else if ( diffx != -1 ) {
				diffy = diffx / mInitialAspectRatio;
				r.inset( 0, (float) ( diffy / 2.0 ) );
			}
		}

		r.sort();
	}

	/**
	 * Adjust real crop rect.
	 * 
	 * @param matrix
	 *           the matrix
	 * @param rect
	 *           the rect
	 * @param outsideRect
	 *           the outside rect
	 * @return the rect f
	 */
	private RectF adjustRealCropRect( Matrix matrix, RectF rect, RectF outsideRect ) {
		// Log.i( LOG_TAG, "adjustRealCropRect" );

		boolean adjusted = false;

		tempLayoutRectF.set( rect );
		matrix.mapRect( tempLayoutRectF );

		float[] mvalues = new float[9];
		matrix.getValues( mvalues );
		final float scale = mvalues[Matrix.MSCALE_X];

		if ( tempLayoutRectF.left < outsideRect.left ) {
			adjusted = true;
			rect.offset( ( outsideRect.left - tempLayoutRectF.left ) / scale, 0 );
		} else if ( tempLayoutRectF.right > outsideRect.right ) {
			adjusted = true;
			rect.offset( -( tempLayoutRectF.right - outsideRect.right ) / scale, 0 );
		}

		if ( tempLayoutRectF.top < outsideRect.top ) {
			adjusted = true;
			rect.offset( 0, ( outsideRect.top - tempLayoutRectF.top ) / scale );
		} else if ( tempLayoutRectF.bottom > outsideRect.bottom ) {
			adjusted = true;
			rect.offset( 0, -( tempLayoutRectF.bottom - outsideRect.bottom ) / scale );
		}

		tempLayoutRectF.set( rect );
		matrix.mapRect( tempLayoutRectF );

		if ( tempLayoutRectF.width() > outsideRect.width() ) {
			adjusted = true;
			if ( tempLayoutRectF.left < outsideRect.left ) rect.left += ( outsideRect.left - tempLayoutRectF.left ) / scale;
			if ( tempLayoutRectF.right > outsideRect.right ) rect.right += -( tempLayoutRectF.right - outsideRect.right ) / scale;
		}

		if ( tempLayoutRectF.height() > outsideRect.height() ) {
			adjusted = true;
			if ( tempLayoutRectF.top < outsideRect.top ) rect.top += ( outsideRect.top - tempLayoutRectF.top ) / scale;
			if ( tempLayoutRectF.bottom > outsideRect.bottom )
				rect.bottom += -( tempLayoutRectF.bottom - outsideRect.bottom ) / scale;
		}

		if ( mMaintainAspectRatio && adjusted ) {
			if ( mInitialAspectRatio >= 1 ) { // width > height
				final double dy = rect.width() / mInitialAspectRatio;
				rect.bottom = (float) ( rect.top + dy );
			} else { // height >= width
				final double dx = rect.height() * mInitialAspectRatio;
				rect.right = (float) ( rect.left + dx );
			}
		}

		rect.sort();
		return rect;
	}

	/**
	 * Compute and adjust the current crop layout
	 * 
	 * @param adjust
	 *           - If true tries to adjust the crop rect
	 * @param outRect
	 *           - The result will be stored in this {@link Rect}
	 */
	public void computeLayout( boolean adjust, Rect outRect ) {
		if ( adjust ) {
			adjustCropRect( mCropRect );
			tmpRect2.set( 0, 0, mContext.getWidth(), mContext.getHeight() );
			mCropRect = adjustRealCropRect( mMatrix, mCropRect, tmpRect2 );
		}

		getDisplayRect( mMatrix, mCropRect, outRect );
	}

	public void getDisplayRect( Matrix m, RectF supportRect, Rect outRect ) {
		tmpDisplayRectF.set( supportRect.left, supportRect.top, supportRect.right, supportRect.bottom );
		m.mapRect( tmpDisplayRectF );
		outRect.set( Math.round( tmpDisplayRectF.left ), Math.round( tmpDisplayRectF.top ), Math.round( tmpDisplayRectF.right ),
				Math.round( tmpDisplayRectF.bottom ) );
	}

	/**
	 * Invalidate.
	 */
	public void invalidate() {
		if ( !mRunning ) {
			computeLayout( true, mDrawRect );
		}
	}

	/** true while the view is animating */
	protected volatile boolean mRunning = false;

	/** animation duration in ms */
	protected int animationDurationMs = 300;

	/** {@link Easing} used to animate the view */
	protected Easing mEasing = new Quad();

	/**
	 * Return true if the view is currently running
	 * 
	 * @return
	 */
	public boolean isRunning() {
		return mRunning;
	}

	public void animateTo( Matrix m, Rect imageRect, RectF cropRect, final boolean maintainAspectRatio ) {

		if ( !mRunning ) {
			mRunning = true;
			setMode( Mode.None );

			mMatrix = new Matrix( m );
			mCropRect = cropRect;
			mImageRect = new RectF( imageRect );
			mMaintainAspectRatio = false;

			double ratio = (double) mCropRect.width() / (double) mCropRect.height();
			mInitialAspectRatio = Math.round( ratio * 1000.0 ) / 1000.0;
			// Log.i( LOG_TAG, "aspect ratio: " + mInitialAspectRatio );

			final Rect oldRect = mDrawRect;
			final Rect newRect = new Rect();
			computeLayout( false, newRect );

			final float[] topLeft = { oldRect.left, oldRect.top };
			final float[] bottomRight = { oldRect.right, oldRect.bottom };

			final double pt1 = newRect.left - oldRect.left;
			final double pt2 = newRect.right - oldRect.right;
			final double pt3 = newRect.top - oldRect.top;
			final double pt4 = newRect.bottom - oldRect.bottom;

			final long startTime = System.currentTimeMillis();

			mHandler.post( new Runnable() {

				@Override
				public void run() {

					if ( mContext == null ) return;

					long now = System.currentTimeMillis();
					double currentMs = Math.min( animationDurationMs, now - startTime );

					double value1 = mEasing.easeOut( currentMs, 0, pt1, animationDurationMs );
					double value2 = mEasing.easeOut( currentMs, 0, pt2, animationDurationMs );
					double value3 = mEasing.easeOut( currentMs, 0, pt3, animationDurationMs );
					double value4 = mEasing.easeOut( currentMs, 0, pt4, animationDurationMs );

					mDrawRect.left = (int) ( topLeft[0] + value1 );
					mDrawRect.right = (int) ( bottomRight[0] + value2 );
					mDrawRect.top = (int) ( topLeft[1] + value3 );
					mDrawRect.bottom = (int) ( bottomRight[1] + value4 );
					mContext.invalidate();

					if ( currentMs < animationDurationMs ) {
						mHandler.post( this );
					} else {
						mMaintainAspectRatio = maintainAspectRatio;
						mRunning = false;
						invalidate();
						mContext.invalidate();
					}
				}

			} );

		}
	}

	/**
	 * Setup.
	 * 
	 * @param m
	 *           the m
	 * @param imageRect
	 *           the image rect
	 * @param cropRect
	 *           the crop rect
	 * @param maintainAspectRatio
	 *           the maintain aspect ratio
	 */
	public void setup( Matrix m, Rect imageRect, RectF cropRect, boolean maintainAspectRatio ) {

		mMatrix = new Matrix( m );
		mCropRect = cropRect;
		mImageRect = new RectF( imageRect );
		mMaintainAspectRatio = maintainAspectRatio;

		double ratio = (double) mCropRect.width() / (double) mCropRect.height();
		mInitialAspectRatio = Math.round( ratio * 1000.0 ) / 1000.0;
		// Log.i( LOG_TAG, "aspect ratio: " + mInitialAspectRatio );

		computeLayout( true, mDrawRect );

		mOutlinePaint.setStrokeWidth( stroke_width );
		mOutlinePaint.setStyle( Paint.Style.STROKE );
		mOutlinePaint.setAntiAlias( false );
		try {
			ReflectionUtils.invokeMethod( mOutlinePaint, "setHinting", new Class<?>[] { int.class }, 0 );
		} catch ( ReflectionException e ) {}

		mOutlinePaint2.setStrokeWidth( internal_stroke_width );
		mOutlinePaint2.setStyle( Paint.Style.STROKE );
		mOutlinePaint2.setAntiAlias( false );
		mOutlinePaint2.setColor( highlight_internal_color );
		try {
			ReflectionUtils.invokeMethod( mOutlinePaint2, "setHinting", new Class<?>[] { int.class }, 0 );
		} catch ( ReflectionException e ) {}

		mOutlineFill.setColor( highlight_outside_color );
		mOutlineFill.setStyle( Paint.Style.FILL );
		mOutlineFill.setAntiAlias( false );
		try {
			ReflectionUtils.invokeMethod( mOutlineFill, "setHinting", new Class<?>[] { int.class }, 0 );
		} catch ( ReflectionException e ) {}

		mLinesPaintShadow.setStrokeWidth( internal_stroke_width );
		mLinesPaintShadow.setAntiAlias( true );
		mLinesPaintShadow.setColor( Color.BLACK );
		mLinesPaintShadow.setStyle( Paint.Style.STROKE );
		mLinesPaintShadow.setMaskFilter( new BlurMaskFilter( 2, Blur.NORMAL ) );

		setMode( Mode.None );
		init();
	}

	/**
	 * Sets the aspect ratio.
	 * 
	 * @param value
	 *           the new aspect ratio
	 */
	public void setAspectRatio( float value ) {
		mInitialAspectRatio = value;
	}

	/**
	 * Sets the maintain aspect ratio.
	 * 
	 * @param value
	 *           the new maintain aspect ratio
	 */
	public void setMaintainAspectRatio( boolean value ) {
		mMaintainAspectRatio = value;
	}

	/**
	 * Update.
	 * 
	 * @param imageMatrix
	 *           the image matrix
	 * @param imageRect
	 *           the image rect
	 */
	public void update( Matrix imageMatrix, Rect imageRect ) {
		mMatrix = new Matrix( imageMatrix );
		mImageRect = new RectF( imageRect );
		computeLayout( true, mDrawRect );
		mContext.invalidate();
	}

	/**
	 * Gets the matrix.
	 * 
	 * @return the matrix
	 */
	public Matrix getMatrix() {
		return mMatrix;
	}

	/**
	 * Gets the draw rect.
	 * 
	 * @return the draw rect
	 */
	public Rect getDrawRect() {
		return mDrawRect;
	}

	/**
	 * Gets the crop rect f.
	 * 
	 * @return the crop rect f
	 */
	public RectF getCropRectF() {
		return mCropRect;
	}

	/**
	 * Returns the cropping rectangle in image space.
	 * 
	 * @return the crop rect
	 */
	public Rect getCropRect() {
		return new Rect( (int) mCropRect.left, (int) mCropRect.top, (int) mCropRect.right, (int) mCropRect.bottom );
	}

}
