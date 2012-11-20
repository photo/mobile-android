package com.aviary.android.feather.widget;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import com.aviary.android.feather.library.graphics.drawable.IBitmapDrawable;

// TODO: Auto-generated Javadoc
/**
 * The Class ImageViewTouchAndDraw.
 */
public class ImageViewTouchAndDraw extends ImageViewTouch {

	/**
	 * The Enum TouchMode.
	 */
	public static enum TouchMode {

		/** The IMAGE. */
		IMAGE,
		/** The DRAW. */
		DRAW
	};

	/**
	 * The listener interface for receiving onDrawStart events. The class that is interested in processing a onDrawStart event
	 * implements this interface, and the object created with that class is registered with a component using the component's
	 * <code>addOnDrawStartListener<code> method. When
	 * the onDrawStart event occurs, that object's appropriate
	 * method is invoked.
	 * 
	 * @see OnDrawStartEvent
	 */
	public static interface OnDrawStartListener {

		/**
		 * On draw start.
		 */
		void onDrawStart();
	};

	public static interface OnDrawPathListener {

		void onStart();

		void onMoveTo( float x, float y );

		void onLineTo( float x, float y );

		void onQuadTo( float x, float y, float x1, float y1 );

		void onEnd();
	}

	/** The m paint. */
	protected Paint mPaint;

	/** The tmp path. */
	protected Path tmpPath = new Path();

	/** The m canvas. */
	protected Canvas mCanvas;

	/** The m touch mode. */
	protected TouchMode mTouchMode = TouchMode.DRAW;

	/** The m y. */
	protected float mX, mY;

	/** The m identity matrix. */
	protected Matrix mIdentityMatrix = new Matrix();

	/** The m inverted matrix. */
	protected Matrix mInvertedMatrix = new Matrix();

	/** The m copy. */
	protected Bitmap mCopy;

	/** The Constant TOUCH_TOLERANCE. */
	protected static final float TOUCH_TOLERANCE = 4;

	/** The m draw listener. */
	private OnDrawStartListener mDrawListener;

	private OnDrawPathListener mDrawPathListener;

	/**
	 * Instantiates a new image view touch and draw.
	 * 
	 * @param context
	 *           the context
	 * @param attrs
	 *           the attrs
	 */
	public ImageViewTouchAndDraw( Context context, AttributeSet attrs ) {
		super( context, attrs );
	}

	/**
	 * Sets the on draw start listener.
	 * 
	 * @param listener
	 *           the new on draw start listener
	 */
	public void setOnDrawStartListener( OnDrawStartListener listener ) {
		mDrawListener = listener;
	}

	public void setOnDrawPathListener( OnDrawPathListener listener ) {
		mDrawPathListener = listener;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.sephiroth.android.library.imagezoom.ImageViewTouch#init()
	 */
	@Override
	protected void init() {
		super.init();
		mPaint = new Paint( Paint.ANTI_ALIAS_FLAG );
		mPaint.setDither( true );
		mPaint.setFilterBitmap( false );
		mPaint.setColor( 0xFFFF0000 );
		mPaint.setStyle( Paint.Style.STROKE );
		mPaint.setStrokeJoin( Paint.Join.ROUND );
		mPaint.setStrokeCap( Paint.Cap.ROUND );
		mPaint.setStrokeWidth( 10.0f );

		tmpPath = new Path();
	}

	/**
	 * Gets the draw mode.
	 * 
	 * @return the draw mode
	 */
	public TouchMode getDrawMode() {
		return mTouchMode;
	}

	/**
	 * Sets the draw mode.
	 * 
	 * @param mode
	 *           the new draw mode
	 */
	public void setDrawMode( TouchMode mode ) {
		if ( mode != mTouchMode ) {
			mTouchMode = mode;
			onDrawModeChanged();
		}
	}

	/**
	 * On draw mode changed.
	 */
	protected void onDrawModeChanged() {
		if ( mTouchMode == TouchMode.DRAW ) {

			Matrix m1 = new Matrix( getImageMatrix() );
			mInvertedMatrix.reset();

			float[] v1 = getMatrixValues( m1 );
			m1.invert( m1 );
			float[] v2 = getMatrixValues( m1 );

			mInvertedMatrix.postTranslate( -v1[Matrix.MTRANS_X], -v1[Matrix.MTRANS_Y] );
			mInvertedMatrix.postScale( v2[Matrix.MSCALE_X], v2[Matrix.MSCALE_Y] );
			mCanvas.setMatrix( mInvertedMatrix );
		}
	}

	/**
	 * Gets the paint.
	 * 
	 * @return the paint
	 */
	public Paint getPaint() {
		return mPaint;
	}

	/**
	 * Sets the paint.
	 * 
	 * @param paint
	 *           the new paint
	 */
	public void setPaint( Paint paint ) {
		mPaint.set( paint );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.ImageView#onDraw(android.graphics.Canvas)
	 */
	@Override
	protected void onDraw( Canvas canvas ) {
		super.onDraw( canvas );

		// canvas.drawPath( tmpPath, mPaint );

		if ( mCopy != null ) {
			final int saveCount = canvas.getSaveCount();
			canvas.save();
			canvas.drawBitmap( mCopy, getImageMatrix(), null );
			canvas.restoreToCount( saveCount );
		}
	}

	/**
	 * Commit.
	 * 
	 * @param canvas
	 *           the canvas
	 */
	public void commit( Canvas canvas ) {
		canvas.drawBitmap( ( (IBitmapDrawable) getDrawable() ).getBitmap(), new Matrix(), null );
		canvas.drawBitmap( mCopy, new Matrix(), null );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.sephiroth.android.library.imagezoom.ImageViewTouch#onBitmapChanged(android.graphics.drawable.Drawable)
	 */
	@Override
	protected void onBitmapChanged( Drawable drawable ) {
		super.onBitmapChanged( drawable );

		if ( mCopy != null ) {
			mCopy.recycle();
			mCopy = null;
		}

		if ( drawable != null && ( drawable instanceof IBitmapDrawable ) ) {
			final Bitmap bitmap = ( (IBitmapDrawable) drawable ).getBitmap();
			mCopy = Bitmap.createBitmap( bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888 );
			mCanvas = new Canvas( mCopy );
			mCanvas.drawColor( 0 );
			onDrawModeChanged();
		}
	}

	/**
	 * Touch_start.
	 * 
	 * @param x
	 *           the x
	 * @param y
	 *           the y
	 */
	private void touch_start( float x, float y ) {
		tmpPath.reset();
		tmpPath.moveTo( x, y );

		mX = x;
		mY = y;

		if ( mDrawListener != null ) mDrawListener.onDrawStart();
		if ( mDrawPathListener != null ) {

			mDrawPathListener.onStart();

			float[] pts = new float[] { x, y };
			mInvertedMatrix.mapPoints( pts );
			mDrawPathListener.onMoveTo( pts[0], pts[1] );
		}
	}

	/**
	 * Touch_move.
	 * 
	 * @param x
	 *           the x
	 * @param y
	 *           the y
	 */
	private void touch_move( float x, float y ) {
		float dx = Math.abs( x - mX );
		float dy = Math.abs( y - mY );
		if ( dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE ) {

			float x1 = ( x + mX ) / 2;
			float y1 = ( y + mY ) / 2;

			tmpPath.quadTo( mX, mY, x1, y1 );

			mCanvas.drawPath( tmpPath, mPaint );
			tmpPath.reset();
			tmpPath.moveTo( x1, y1 );

			if ( mDrawPathListener != null ) {

				float[] pts = new float[] { mX, mY, x1, y1 };
				mInvertedMatrix.mapPoints( pts );
				mDrawPathListener.onQuadTo( pts[0], pts[1], pts[2], pts[3] );
			}

			mX = x;
			mY = y;
		}
	}

	/**
	 * Touch_up.
	 */
	private void touch_up() {

		// mCanvas.drawPath( tmpPath, mPaint );

		tmpPath.reset();

		if ( mDrawPathListener != null ) {
			mDrawPathListener.onEnd();
		}
	}

	/**
	 * Gets the matrix values.
	 * 
	 * @param m
	 *           the m
	 * @return the matrix values
	 */
	public static float[] getMatrixValues( Matrix m ) {
		float[] values = new float[9];
		m.getValues( values );
		return values;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.sephiroth.android.library.imagezoom.ImageViewTouch#onTouchEvent(android.view.MotionEvent)
	 */
	@Override
	public boolean onTouchEvent( MotionEvent event ) {
		if ( mTouchMode == TouchMode.DRAW && event.getPointerCount() == 1 ) {
			float x = event.getX();
			float y = event.getY();

			switch ( event.getAction() ) {
				case MotionEvent.ACTION_DOWN:
					touch_start( x, y );
					invalidate();
					break;
				case MotionEvent.ACTION_MOVE:
					touch_move( x, y );
					invalidate();
					break;
				case MotionEvent.ACTION_UP:
					touch_up();
					invalidate();
					break;
			}
			return true;
		} else {
			if ( mTouchMode == TouchMode.IMAGE )
				return super.onTouchEvent( event );
			else
				return false;
		}
	}

	/**
	 * Gets the overlay bitmap.
	 * 
	 * @return the overlay bitmap
	 */
	public Bitmap getOverlayBitmap() {
		return mCopy;
	}

}
