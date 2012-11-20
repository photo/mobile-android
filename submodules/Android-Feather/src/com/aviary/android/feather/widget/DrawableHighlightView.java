package com.aviary.android.feather.widget;

import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import com.aviary.android.feather.R;
import com.aviary.android.feather.library.graphics.Point2D;
import com.aviary.android.feather.library.graphics.drawable.EditableDrawable;
import com.aviary.android.feather.library.graphics.drawable.FeatherDrawable;

// TODO: Auto-generated Javadoc
/**
 * The Class DrawableHighlightView.
 */
public class DrawableHighlightView {
	
	static final String LOG_TAG = "drawable-view";

	/**
	 * The Enum Mode.
	 */
	enum Mode {

		/** The None. */
		None,
		/** The Move. */
		Move,
		/** The Grow. */
		Grow,
		/** The Rotate. */
		Rotate
	};

	/**
	 * The Enum AlignModeV.
	 */
	public enum AlignModeV {

		/** The Top. */
		Top,
		/** The Bottom. */
		Bottom,
		/** The Center. */
		Center
	};

	/**
	 * The listener interface for receiving onDeleteClick events. The class that is interested in processing a onDeleteClick event
	 * implements this interface, and the object created with that class is registered with a component using the component's
	 * <code>addOnDeleteClickListener<code> method. When
	 * the onDeleteClick event occurs, that object's appropriate
	 * method is invoked.
	 * 
	 * @see OnDeleteClickEvent
	 */
	public interface OnDeleteClickListener {

		/**
		 * On delete click.
		 */
		void onDeleteClick();
	}
	
	private int STATE_NONE = 1 << 0;
	private int STATE_SELECTED = 1 << 1;
	private int STATE_FOCUSED = 1 << 2;

	/** The m delete click listener. */
	private OnDeleteClickListener mDeleteClickListener;

	/** The Constant GROW_NONE. */
	static final int GROW_NONE = 1 << 0; // 1

	/** The Constant GROW_LEFT_EDGE. */
	static final int GROW_LEFT_EDGE = 1 << 1; // 2

	/** The Constant GROW_RIGHT_EDGE. */
	static final int GROW_RIGHT_EDGE = 1 << 2; // 4

	/** The Constant GROW_TOP_EDGE. */
	static final int GROW_TOP_EDGE = 1 << 3; // 8

	/** The Constant GROW_BOTTOM_EDGE. */
	static final int GROW_BOTTOM_EDGE = 1 << 4; // 16

	/** The Constant ROTATE. */
	static final int ROTATE = 1 << 5; // 32

	/** The Constant MOVE. */
	static final int MOVE = 1 << 6; // 64

	// tolerance for buttons hits
	/** The Constant HIT_TOLERANCE. */
	private static final float HIT_TOLERANCE = 40f;

	/** The m hidden. */
	private boolean mHidden;

	/** The m context. */
	private View mContext;

	/** The m mode. */
	private Mode mMode;

	private int mState = STATE_NONE;

	/** The m draw rect. */
	private RectF mDrawRect;

	/** The m crop rect. */
	private RectF mCropRect;

	/** The m matrix. */
	private Matrix mMatrix;

	/** The m content. */
	private final FeatherDrawable mContent;
	
	private final EditableDrawable mEditableContent;
	
	// private Drawable mAnchorResize;
	/** The m anchor rotate. */
	private Drawable mAnchorRotate;

	/** The m anchor delete. */
	private Drawable mAnchorDelete;

	/** The m anchor width. */
	private int mAnchorWidth;

	/** The m anchor height. */
	private int mAnchorHeight;

	/** The m outline stroke color. */
	private int mOutlineStrokeColorNormal;

	/** The m outline stroke color pressed. */
	private int mOutlineStrokeColorPressed;
	
	private int mOutlineStrokeColorUnselected;
	
	/** The m rotate and scale. */
	private boolean mRotateAndScale;

	/** The m show delete button. */
	private boolean mShowDeleteButton = true;

	/** The m rotation. */
	private float mRotation = 0;

	/** The m ratio. */
	private float mRatio = 1f;

	/** The m rotate matrix. */
	private Matrix mRotateMatrix = new Matrix();

	/** The fpoints. */
	private final float fpoints[] = new float[] { 0, 0 };

	/** The m draw outline stroke. */
	private boolean mDrawOutlineStroke = true;

	/** The m draw outline fill. */
	private boolean mDrawOutlineFill = true;

	/** The m outline stroke paint. */
	private Paint mOutlineStrokePaint;
	
	/** The m outline fill paint. */
	private Paint mOutlineFillPaint;

	/** The m outline fill color normal. */
	private int mOutlineFillColorNormal = 0x66000000;

	/** The m outline fill color pressed. */
	private int mOutlineFillColorPressed = 0x66a5a5a5;
	
	private int mOutlineFillColorUnselected = 0;
	
	private int mOutlineFillColorFocused = 0x88FFFFFF;
	
	private int mOutlineStrokeColorFocused = 0x51000000;

	/** The m outline ellipse. */
	private int mOutlineEllipse = 0;

	/** The m padding. */
	private int mPadding = 0;

	/** The m show anchors. */
	private boolean mShowAnchors = true;

	/** The m align vertical mode. */
	private AlignModeV mAlignVerticalMode = AlignModeV.Center;

	/**
	 * Instantiates a new drawable highlight view.
	 * 
	 * @param ctx
	 *           the ctx
	 * @param content
	 *           the content
	 */
	public DrawableHighlightView( final View ctx, final FeatherDrawable content ) {
		mContext = ctx;
		mContent = content;
		
		if( content instanceof EditableDrawable ){
			mEditableContent = (EditableDrawable)content;
		} else {
			mEditableContent = null;
		}
		
		updateRatio();
		setMinSize( 20f );
	}

	/**
	 * Sets the align mode v.
	 * 
	 * @param mode
	 *           the new align mode v
	 */
	public void setAlignModeV( AlignModeV mode ) {
		mAlignVerticalMode = mode;
	}

	/**
	 * Request a layout update.
	 * 
	 * @return the rect f
	 */
	protected RectF computeLayout() {
		return getDisplayRect( mMatrix, mCropRect );
	}

	/**
	 * Dispose.
	 */
	public void dispose() {
		mContext = null;
		mDeleteClickListener = null;
	}

	/** The m outline path. */
	private Path mOutlinePath = new Path();

	// private FontMetrics metrics = new FontMetrics();

	/**
	 * Draw.
	 * 
	 * @param canvas
	 *           the canvas
	 */
	protected void draw( final Canvas canvas ) {
		if ( mHidden ) return;

		RectF drawRectF = new RectF( mDrawRect );
		drawRectF.inset( -mPadding, -mPadding );

		final int saveCount = canvas.save();
		canvas.concat( mRotateMatrix );

		final Rect viewDrawingRect = new Rect();

		mContext.getDrawingRect( viewDrawingRect );

		mOutlinePath.reset();
		mOutlinePath.addRoundRect( drawRectF, mOutlineEllipse, mOutlineEllipse, Path.Direction.CW );

		if ( mDrawOutlineFill ) {
			canvas.drawPath( mOutlinePath, mOutlineFillPaint );
		}
		
		if ( mDrawOutlineStroke ) {
			canvas.drawPath( mOutlinePath, mOutlineStrokePaint );
		}
		
		boolean is_selected = getSelected();
		boolean is_focused = getFocused();

		if ( mEditableContent != null )
			mEditableContent.setBounds( mDrawRect.left, mDrawRect.top, mDrawRect.right, mDrawRect.bottom );
		else
			mContent.setBounds( (int) mDrawRect.left, (int) mDrawRect.top, (int) mDrawRect.right, (int) mDrawRect.bottom );

		mContent.draw( canvas );

		if ( is_selected && !is_focused ) {
			
			if ( mShowAnchors ) {
				final int left = (int) ( drawRectF.left );
				final int right = (int) ( drawRectF.right );
				final int top = (int) ( drawRectF.top );
				final int bottom = (int) ( drawRectF.bottom );

				if ( mAnchorRotate != null ) {
					mAnchorRotate.setBounds( right - mAnchorWidth, bottom - mAnchorHeight, right + mAnchorWidth, bottom + mAnchorHeight );
					mAnchorRotate.draw( canvas );
				}

				if ( ( mAnchorDelete != null ) && mShowDeleteButton ) {
					mAnchorDelete.setBounds( left - mAnchorWidth, top - mAnchorHeight, left + mAnchorWidth, top + mAnchorHeight );
					mAnchorDelete.draw( canvas );
				}
			}
		}

		canvas.restoreToCount( saveCount );

		if ( mEditableContent != null && is_selected ) {
			if ( mEditableContent.isEditing() ) {
				mContext.postInvalidateDelayed( 300 );
			}
		}
	}

	/**
	 * Show anchors.
	 * 
	 * @param value
	 *           the value
	 */
	public void showAnchors( boolean value ) {
		mShowAnchors = value;
	}

	/**
	 * Draw.
	 * 
	 * @param canvas
	 *           the canvas
	 * @param source
	 *           the source
	 */
	public void draw( final Canvas canvas, final Matrix source ) {

		final Matrix matrix = new Matrix( source );
		matrix.invert( matrix );

		final int saveCount = canvas.save();
		canvas.concat( matrix );
		canvas.concat( mRotateMatrix );

		mContent.setBounds( (int) mDrawRect.left, (int) mDrawRect.top, (int) mDrawRect.right, (int) mDrawRect.bottom );
		mContent.draw( canvas );

		canvas.restoreToCount( saveCount );
	}

	/**
	 * Returns the cropping rectangle in image space.
	 * 
	 * @return the crop rect
	 */
	public Rect getCropRect() {
		return new Rect( (int) mCropRect.left, (int) mCropRect.top, (int) mCropRect.right, (int) mCropRect.bottom );
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
	 * Gets the crop rotation matrix.
	 * 
	 * @return the crop rotation matrix
	 */
	public Matrix getCropRotationMatrix() {
		final Matrix m = new Matrix();
		m.postTranslate( -mCropRect.centerX(), -mCropRect.centerY() );
		m.postRotate( mRotation );
		m.postTranslate( mCropRect.centerX(), mCropRect.centerY() );
		return m;
	}

	/**
	 * Gets the display rect.
	 * 
	 * @param m
	 *           the m
	 * @param supportRect
	 *           the support rect
	 * @return the display rect
	 */
	protected RectF getDisplayRect( final Matrix m, final RectF supportRect ) {
		final RectF r = new RectF( supportRect );
		m.mapRect( r );
		return r;
	}

	/**
	 * Gets the display rect f.
	 * 
	 * @return the display rect f
	 */
	public RectF getDisplayRectF() {
		final RectF r = new RectF( mDrawRect );
		mRotateMatrix.mapRect( r );
		return r;
	}

	/**
	 * Gets the draw rect.
	 * 
	 * @return the draw rect
	 */
	public RectF getDrawRect() {
		return mDrawRect;
	}

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

		final RectF rect = new RectF( mDrawRect );
		rect.inset( -mPadding, -mPadding );

		final float pts[] = new float[] { x, y };

		final Matrix rotateMatrix = new Matrix();
		rotateMatrix.postTranslate( -rect.centerX(), -rect.centerY() );
		rotateMatrix.postRotate( -mRotation );
		rotateMatrix.postTranslate( rect.centerX(), rect.centerY() );
		rotateMatrix.mapPoints( pts );

		x = pts[0];
		y = pts[1];

		mContext.invalidate();

		int retval = DrawableHighlightView.GROW_NONE;
		final boolean verticalCheck = ( y >= ( rect.top - DrawableHighlightView.HIT_TOLERANCE ) )
				&& ( y < ( rect.bottom + DrawableHighlightView.HIT_TOLERANCE ) );
		final boolean horizCheck = ( x >= ( rect.left - DrawableHighlightView.HIT_TOLERANCE ) )
				&& ( x < ( rect.right + DrawableHighlightView.HIT_TOLERANCE ) );
		
		// if horizontal and vertical checks are good then
		// at least the move edge is selected
		if( verticalCheck && horizCheck ){
			retval = DrawableHighlightView.MOVE;
		}

		if ( !mRotateAndScale ) {
			if ( ( Math.abs( rect.left - x ) < DrawableHighlightView.HIT_TOLERANCE ) && verticalCheck )
				retval |= DrawableHighlightView.GROW_LEFT_EDGE;
			if ( ( Math.abs( rect.right - x ) < DrawableHighlightView.HIT_TOLERANCE ) && verticalCheck )
				retval |= DrawableHighlightView.GROW_RIGHT_EDGE;
			if ( ( Math.abs( rect.top - y ) < DrawableHighlightView.HIT_TOLERANCE ) && horizCheck )
				retval |= DrawableHighlightView.GROW_TOP_EDGE;
			if ( ( Math.abs( rect.bottom - y ) < DrawableHighlightView.HIT_TOLERANCE ) && horizCheck )
				retval |= DrawableHighlightView.GROW_BOTTOM_EDGE;
		}

		if ( ( Math.abs( rect.right - x ) < DrawableHighlightView.HIT_TOLERANCE )
				&& ( Math.abs( rect.bottom - y ) < DrawableHighlightView.HIT_TOLERANCE ) && verticalCheck && horizCheck )
			retval = DrawableHighlightView.ROTATE;

		if ( ( retval == DrawableHighlightView.GROW_NONE ) && rect.contains( (int) x, (int) y ) )
			retval = DrawableHighlightView.MOVE;
		return retval;
	}

	/**
	 * On single tap confirmed.
	 * 
	 * @param x
	 *           the x
	 * @param y
	 *           the y
	 */
	public void onSingleTapConfirmed( float x, float y ) {

		final RectF rect = new RectF( mDrawRect );
		rect.inset( -mPadding, -mPadding );

		final float pts[] = new float[] { x, y };

		final Matrix rotateMatrix = new Matrix();
		rotateMatrix.postTranslate( -rect.centerX(), -rect.centerY() );
		rotateMatrix.postRotate( -mRotation );
		rotateMatrix.postTranslate( rect.centerX(), rect.centerY() );
		rotateMatrix.mapPoints( pts );

		x = pts[0];
		y = pts[1];

		mContext.invalidate();

		final boolean verticalCheck = ( y >= ( rect.top - DrawableHighlightView.HIT_TOLERANCE ) )
				&& ( y < ( rect.bottom + DrawableHighlightView.HIT_TOLERANCE ) );
		final boolean horizCheck = ( x >= ( rect.left - DrawableHighlightView.HIT_TOLERANCE ) )
				&& ( x < ( rect.right + DrawableHighlightView.HIT_TOLERANCE ) );

		if ( mShowDeleteButton )
			if ( ( Math.abs( rect.left - x ) < DrawableHighlightView.HIT_TOLERANCE )
					&& ( Math.abs( rect.top - y ) < DrawableHighlightView.HIT_TOLERANCE ) && verticalCheck && horizCheck )
				if ( mDeleteClickListener != null ) {
					mDeleteClickListener.onDeleteClick();
				}
	}

	/**
	 * Gets the invalidation rect.
	 * 
	 * @return the invalidation rect
	 */
	protected Rect getInvalidationRect() {
		final RectF r = new RectF( mDrawRect );
		r.inset( -mPadding, -mPadding );
		mRotateMatrix.mapRect( r );

		final Rect rect = new Rect( (int) r.left, (int) r.top, (int) r.right, (int) r.bottom );
		rect.inset( -mAnchorWidth * 2, -mAnchorHeight * 2 );
		return rect;
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
	 * Gets the mode.
	 * 
	 * @return the mode
	 */
	public Mode getMode() {
		return mMode;
	}

	/**
	 * Gets the rotation.
	 * 
	 * @return the rotation
	 */
	public float getRotation() {
		return mRotation;
	}

	public Matrix getRotationMatrix() {
		return mRotateMatrix;
	}

	/**
	 * Increase the size of the View.
	 * 
	 * @param dx
	 *           the dx
	 */
	protected void growBy( final float dx ) {
		growBy( dx, dx / mRatio, true );
	}

	/**
	 * Increase the size of the View.
	 * 
	 * @param dx
	 *           the dx
	 * @param dy
	 *           the dy
	 * @param checkMinSize
	 *           the check min size
	 */
	protected void growBy( final float dx, final float dy, boolean checkMinSize ) {
		final RectF r = new RectF( mCropRect );

		if ( mAlignVerticalMode == AlignModeV.Center ) {
			r.inset( -dx, -dy );
		} else if ( mAlignVerticalMode == AlignModeV.Top ) {
			r.inset( -dx, 0 );
			r.bottom += dy * 2;
		} else {
			r.inset( -dx, 0 );
			r.top -= dy * 2;
		}

		RectF testRect = getDisplayRect( mMatrix, r );
		Log.d( LOG_TAG, "growBy: " + testRect.width() + "x" + testRect.height() );
		
		if ( !mContent.validateSize( testRect ) && checkMinSize ) {
			return;
		}

		mCropRect.set( r );
		invalidate();
		mContext.invalidate();
	}

	/**
	 * On mouse move.
	 * 
	 * @param edge
	 *           the edge
	 * @param event2
	 *           the event2
	 * @param dx
	 *           the dx
	 * @param dy
	 *           the dy
	 */
	void onMouseMove( int edge, MotionEvent event2, float dx, float dy ) {
		if ( edge == GROW_NONE ) {
			return;
		}

		fpoints[0] = dx;
		fpoints[1] = dy;

		float xDelta;
		@SuppressWarnings("unused")
		float yDelta;

		if ( edge == MOVE ) {
			moveBy( dx * ( mCropRect.width() / mDrawRect.width() ), dy * ( mCropRect.height() / mDrawRect.height() ) );
		} else if ( edge == ROTATE ) {
			dx = fpoints[0];
			dy = fpoints[1];
			xDelta = dx * ( mCropRect.width() / mDrawRect.width() );
			yDelta = dy * ( mCropRect.height() / mDrawRect.height() );
			rotateBy( event2.getX(), event2.getY(), dx, dy );

			invalidate();
			mContext.invalidate( getInvalidationRect() );
		} else {

			Matrix rotateMatrix = new Matrix();
			rotateMatrix.postRotate( -mRotation );
			rotateMatrix.mapPoints( fpoints );
			dx = fpoints[0];
			dy = fpoints[1];

			if ( ( ( GROW_LEFT_EDGE | GROW_RIGHT_EDGE ) & edge ) == 0 ) dx = 0;
			if ( ( ( GROW_TOP_EDGE | GROW_BOTTOM_EDGE ) & edge ) == 0 ) dy = 0;

			xDelta = dx * ( mCropRect.width() / mDrawRect.width() );
			yDelta = dy * ( mCropRect.height() / mDrawRect.height() );
			growBy( ( ( ( edge & GROW_LEFT_EDGE ) != 0 ) ? -1 : 1 ) * xDelta );

			invalidate();
			mContext.invalidate( getInvalidationRect() );
		}
	}
	
	void onMove( float dx, float dy ) {
		moveBy( dx * ( mCropRect.width() / mDrawRect.width() ), dy * ( mCropRect.height() / mDrawRect.height() ) );
	}
	
	/**
	 * Inits the.
	 */
	private void init() {

		final android.content.res.Resources resources = mContext.getResources();
		// mAnchorResize = resources.getDrawable( R.drawable.camera_crop_width );
		mAnchorRotate = resources.getDrawable( R.drawable.feather_resize_knob );
		mAnchorDelete = resources.getDrawable( R.drawable.feather_highlight_delete_button );

		mAnchorWidth = mAnchorRotate.getIntrinsicWidth() / 2;
		mAnchorHeight = mAnchorRotate.getIntrinsicHeight() / 2;

		mOutlineStrokeColorNormal = mContext.getResources().getColor( R.color.feather_drawable_highlight_focus );
		mOutlineStrokeColorPressed = mContext.getResources().getColor( R.color.feather_drawable_highlight_down );
		mOutlineStrokeColorUnselected = 0;

		mOutlineStrokePaint = new Paint( Paint.ANTI_ALIAS_FLAG );
		mOutlineStrokePaint.setStrokeWidth( 2.0f );
		mOutlineStrokePaint.setStyle( Paint.Style.STROKE );
		mOutlineStrokePaint.setColor( mOutlineStrokeColorNormal );
		
		mOutlineFillPaint = new Paint( Paint.ANTI_ALIAS_FLAG );
		mOutlineFillPaint.setStyle( Paint.Style.FILL );
		mOutlineFillPaint.setColor( mOutlineFillColorNormal );

		setMode( Mode.None );
	}

	/**
	 * Invalidate.
	 */
	public void invalidate() {
		mDrawRect = computeLayout(); // true

		mRotateMatrix.reset();
		mRotateMatrix.postTranslate( -mDrawRect.centerX(), -mDrawRect.centerY() );
		mRotateMatrix.postRotate( mRotation );
		mRotateMatrix.postTranslate( mDrawRect.centerX(), mDrawRect.centerY() );
	}

	/**
	 * Move by.
	 * 
	 * @param dx
	 *           the dx
	 * @param dy
	 *           the dy
	 */
	void moveBy( final float dx, final float dy ) {
		mCropRect.offset( dx, dy );
		invalidate();
		mContext.invalidate();
	}

	/**
	 * Rotate by.
	 * 
	 * @param dx
	 *           the dx
	 * @param dy
	 *           the dy
	 * @param diffx
	 *           the diffx
	 * @param diffy
	 *           the diffy
	 */
	void rotateBy( final float dx, final float dy, float diffx, float diffy ) {
		final float pt1[] = new float[] { mDrawRect.centerX(), mDrawRect.centerY() };
		final float pt2[] = new float[] { mDrawRect.right, mDrawRect.bottom };
		final float pt3[] = new float[] { dx, dy };

		final double angle1 = Point2D.angleBetweenPoints( pt2, pt1 );
		final double angle2 = Point2D.angleBetweenPoints( pt3, pt1 );

		if ( !mRotateAndScale ) mRotation = -(float) ( angle2 - angle1 );
		final Matrix rotateMatrix = new Matrix();
		rotateMatrix.postRotate( -mRotation );

		if ( mRotateAndScale ) {
			final float points[] = new float[] { diffx, diffy };

			rotateMatrix.mapPoints( points );
			diffx = points[0];
			diffy = points[1];

			final float xDelta = diffx * ( mCropRect.width() / mDrawRect.width() );
			final float yDelta = diffy * ( mCropRect.height() / mDrawRect.height() );

			final float pt4[] = new float[] { mDrawRect.right + xDelta, mDrawRect.bottom + yDelta };
			final double distance1 = Point2D.distance( pt1, pt2 );
			final double distance2 = Point2D.distance( pt1, pt4 );
			final float distance = (float) ( distance2 - distance1 );

			// float ratio = mDrawRect.width() / mDrawRect.height();

			mRotation = -(float) ( angle2 - angle1 );
			growBy( distance );
		}
	}
	
	void onRotateAndGrow( double angle, float scaleFactor ) {
		
		if ( !mRotateAndScale ) mRotation -= (float) ( angle );

		if ( mRotateAndScale ) {
			mRotation -= (float) ( angle );
			growBy( scaleFactor * ( mCropRect.width() / mDrawRect.width() ) );
		}	
		
		invalidate();
		mContext.invalidate( getInvalidationRect() );
	}	

	/**
	 * Toggle visibility to the current View.
	 * 
	 * @param hidden
	 *           the new hidden
	 */
	public void setHidden( final boolean hidden ) {
		mHidden = hidden;
	}

	/**
	 * Sets the min size.
	 * 
	 * @param size
	 *           the new min size
	 */
	public void setMinSize( final float size ) {
		if ( mRatio >= 1 ) {
			mContent.setMinSize( size, size / mRatio );
		} else {
			mContent.setMinSize( size * mRatio, size );
		}
	}
	
	/**
	 * Sets the mode.
	 * 
	 * @param mode
	 *           the new mode
	 */
	public void setMode( final Mode mode ) {
		if ( mode != mMode ) {
			mMode = mode;

			invalidateColors();
			mContext.invalidate();
		}
	}
	
	protected void invalidateColors() {
		
		boolean is_selected = getSelected();
		boolean is_focused = getFocused();
		
		if( is_selected ){
			if( mMode == Mode.None ){
				if( is_focused ){
					mOutlineFillPaint.setColor( mOutlineFillColorFocused );
					mOutlineStrokePaint.setColor( mOutlineStrokeColorFocused );
				} else {
					mOutlineFillPaint.setColor( mOutlineFillColorNormal );
					mOutlineStrokePaint.setColor( mOutlineStrokeColorNormal );
				}
			} else {
				mOutlineFillPaint.setColor( mOutlineFillColorPressed );
				mOutlineStrokePaint.setColor( mOutlineStrokeColorPressed );
			}
			
			
		} else {
			mOutlineFillPaint.setColor( mOutlineFillColorUnselected );
			mOutlineStrokePaint.setColor( mOutlineStrokeColorUnselected );
		}
	}

	/**
	 * Sets the on delete click listener.
	 * 
	 * @param listener
	 *           the new on delete click listener
	 */
	public void setOnDeleteClickListener( final OnDeleteClickListener listener ) {
		mDeleteClickListener = listener;
	}

	/**
	 * Sets the rotate and scale.
	 * 
	 * @param value
	 *           the new rotate and scale
	 */
	public void setRotateAndScale( final boolean value ) {
		mRotateAndScale = value;
	}

	/**
	 * Show delete.
	 * 
	 * @param value
	 *           the value
	 */
	public void showDelete( boolean value ) {
		mShowDeleteButton = value;
	}

	/**
	 * Sets the selected.
	 * 
	 * @param selected
	 *           the new selected
	 */
	public void setSelected( final boolean selected ) {
		boolean is_selected = getSelected();
		if ( is_selected != selected ) {
			mState ^= STATE_SELECTED;
			invalidateColors();
		}
		mContext.invalidate();
	}
	
	public boolean getSelected() {
		return ( mState & STATE_SELECTED ) == STATE_SELECTED;
	}
	
	public void setFocused( final boolean value ) {
		boolean is_focused = getFocused();
		if ( is_focused != value ) {
			mState ^= STATE_FOCUSED;
			
			if( null != mEditableContent ){
				if( value ){
					mEditableContent.beginEdit();
				} else {
					mEditableContent.endEdit();
				}
			}
			
			invalidateColors();
		}
		
		mContext.invalidate();		
	}
	
	public boolean getFocused() {
		return ( mState & STATE_FOCUSED ) == STATE_FOCUSED;
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
	public void setup( final Matrix m, final Rect imageRect, final RectF cropRect, final boolean maintainAspectRatio ) {
		init();
		mMatrix = new Matrix( m );
		mRotation = 0;
		mRotateMatrix = new Matrix();
		mCropRect = cropRect;
		invalidate();
	}

	/**
	 * Update.
	 * 
	 * @param imageMatrix
	 *           the image matrix
	 * @param imageRect
	 *           the image rect
	 */
	public void update( final Matrix imageMatrix, final Rect imageRect ) {
		setMode( Mode.None );
		mMatrix = new Matrix( imageMatrix );
		mRotation = 0;
		mRotateMatrix = new Matrix();
		invalidate();
	}

	/**
	 * Draw outline stroke.
	 * 
	 * @param value
	 *           the value
	 */
	public void drawOutlineStroke( boolean value ) {
		mDrawOutlineStroke = value;
	}

	/**
	 * Draw outline fill.
	 * 
	 * @param value
	 *           the value
	 */
	public void drawOutlineFill( boolean value ) {
		mDrawOutlineFill = value;
	}

	/**
	 * Gets the outline stroke paint.
	 * 
	 * @return the outline stroke paint
	 */
	public Paint getOutlineStrokePaint() {
		return mOutlineStrokePaint;
	}

	/**
	 * Gets the outline fill paint.
	 * 
	 * @return the outline fill paint
	 */
	public Paint getOutlineFillPaint() {
		return mOutlineFillPaint;
	}

	
	public void setOutlineFillColor( ColorStateList colors ){
		mOutlineFillColorNormal  = colors.getColorForState( new int[]{ android.R.attr.state_selected }, 0 );
		mOutlineFillColorFocused = colors.getColorForState( new int[]{ android.R.attr.state_focused }, 0 );
		mOutlineFillColorPressed = colors.getColorForState( new int[]{ android.R.attr.state_pressed }, 0 );
		mOutlineFillColorUnselected = colors.getColorForState( new int[]{ android.R.attr.state_active }, 0 );
		invalidateColors();
		invalidate();
		mContext.invalidate();
	}
	
	public void setOutlineStrokeColor( ColorStateList colors ){
		mOutlineStrokeColorNormal  = colors.getColorForState( new int[]{ android.R.attr.state_selected }, 0 );
		mOutlineStrokeColorFocused = colors.getColorForState( new int[]{ android.R.attr.state_focused }, 0 );
		mOutlineStrokeColorPressed = colors.getColorForState( new int[]{ android.R.attr.state_pressed }, 0 );
		mOutlineStrokeColorUnselected = colors.getColorForState( new int[]{ android.R.attr.state_active }, 0 );
		invalidateColors();
		invalidate();
		mContext.invalidate();
	}	

	/**
	 * Sets the outline ellipse.
	 * 
	 * @param value
	 *           the new outline ellipse
	 */
	public void setOutlineEllipse( int value ) {
		mOutlineEllipse = value;
		invalidate();
		mContext.invalidate();
	}

	/**
	 * Gets the content.
	 * 
	 * @return the content
	 */
	public FeatherDrawable getContent() {
		return mContent;
	}

	/**
	 * Update ratio.
	 */
	private void updateRatio() {
		final int w = mContent.getIntrinsicWidth();
		final int h = mContent.getIntrinsicHeight();
		mRatio = (float) w / (float) h;
	}

	/**
	 * Force update.
	 */
	public void forceUpdate() {
		Log.i( LOG_TAG, "forceUpdate" );
		RectF cropRect = getCropRectF();
		RectF drawRect = getDrawRect();

		if ( mEditableContent != null ) {

			final int textWidth = mContent.getIntrinsicWidth();
			final int textHeight = mContent.getIntrinsicHeight();
			Log.d( LOG_TAG, "text.size: " + textWidth + "x" + textHeight );

			updateRatio();

			RectF textRect = new RectF( cropRect );
			getMatrix().mapRect( textRect );

			float dx = textWidth - textRect.width();
			float dy = textHeight - textRect.height();

			float[] fpoints = new float[] { dx, dy };

			Matrix rotateMatrix = new Matrix();
			rotateMatrix.postRotate( -mRotation );
			// rotateMatrix.mapPoints( fpoints );

			dx = fpoints[0];
			dy = fpoints[1];

			float xDelta = dx * ( cropRect.width() / drawRect.width() );
			float yDelta = dy * ( cropRect.height() / drawRect.height() );

			if ( xDelta != 0 || yDelta != 0 ) {
				growBy( xDelta / 2, yDelta / 2, false );
			}

			invalidate();
			mContext.invalidate( getInvalidationRect() );
		}
	}

	/**
	 * Sets the padding.
	 * 
	 * @param value
	 *           the new padding
	 */
	public void setPadding( int value ) {
		mPadding = value;
	}
}
