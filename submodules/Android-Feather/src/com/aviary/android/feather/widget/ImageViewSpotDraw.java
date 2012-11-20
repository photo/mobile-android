package com.aviary.android.feather.widget;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import com.aviary.android.feather.library.graphics.drawable.IBitmapDrawable;

// TODO: Auto-generated Javadoc
/**
 * The Class ImageViewSpotDraw.
 */
public class ImageViewSpotDraw extends ImageViewTouch {

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
	 * The listener interface for receiving onDraw events. The class that is interested in processing a onDraw event implements this
	 * interface, and the object created with that class is registered with a component using the component's
	 * <code>addOnDrawListener<code> method. When
	 * the onDraw event occurs, that object's appropriate
	 * method is invoked.
	 * 
	 * @see OnDrawEvent
	 */
	public static interface OnDrawListener {

		/**
		 * On draw start.
		 * 
		 * @param points
		 *           the points
		 * @param radius
		 *           the radius
		 */
		void onDrawStart( float points[], int radius );

		/**
		 * On drawing.
		 * 
		 * @param points
		 *           the points
		 * @param radius
		 *           the radius
		 */
		void onDrawing( float points[], int radius );

		/**
		 * On draw end.
		 */
		void onDrawEnd();
	};

	/** The m paint. */
	protected Paint mPaint;

	/** The m current scale. */
	protected float mCurrentScale = 1;

	/** The m brush size. */
	protected float mBrushSize = 30;

	/** The tmp path. */
	protected Path tmpPath = new Path();

	/** The m canvas. */
	protected Canvas mCanvas;

	/** The m touch mode. */
	protected TouchMode mTouchMode = TouchMode.DRAW;

	/** The m y. */
	protected float mX, mY;

	protected float mStartX, mStartY;

	/** The m identity matrix. */
	protected Matrix mIdentityMatrix = new Matrix();

	/** The m inverted matrix. */
	protected Matrix mInvertedMatrix = new Matrix();

	/** The Constant TOUCH_TOLERANCE. */
	protected static final float TOUCH_TOLERANCE = 2;

	/** The m draw listener. */
	private OnDrawListener mDrawListener;

	/** draw restriction **/
	private double mRestiction = 0;

	/**
	 * Instantiates a new image view spot draw.
	 * 
	 * @param context
	 *           the context
	 * @param attrs
	 *           the attrs
	 */
	public ImageViewSpotDraw( Context context, AttributeSet attrs ) {
		super( context, attrs );
	}

	/**
	 * Sets the on draw start listener.
	 * 
	 * @param listener
	 *           the new on draw start listener
	 */
	public void setOnDrawStartListener( OnDrawListener listener ) {
		mDrawListener = listener;
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
		mPaint.setFilterBitmap( false );
		mPaint.setDither( true );
		mPaint.setColor( 0x66FFFFCC );
		mPaint.setStyle( Paint.Style.STROKE );
		mPaint.setStrokeCap( Paint.Cap.ROUND );
		tmpPath = new Path();
	}

	public void setDrawLimit( double value ) {
		mRestiction = value;
	}

	/**
	 * Sets the brush size.
	 * 
	 * @param value
	 *           the new brush size
	 */
	public void setBrushSize( float value ) {
		mBrushSize = value;

		if ( mPaint != null ) {
			mPaint.setStrokeWidth( mBrushSize );
		}
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

			mCurrentScale = getScale();

			mPaint.setStrokeWidth( mBrushSize );
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
		canvas.drawPath( tmpPath, mPaint );
	}

	public RectF getImageRect() {
		if ( getDrawable() != null ) {
			return new RectF( 0, 0, getDrawable().getIntrinsicWidth(), getDrawable().getIntrinsicHeight() );
		} else {
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.sephiroth.android.library.imagezoom.ImageViewTouch#onBitmapChanged(android.graphics.drawable.Drawable)
	 */
	@Override
	protected void onBitmapChanged( Drawable drawable ) {
		super.onBitmapChanged( drawable );

		if ( drawable != null && ( drawable instanceof IBitmapDrawable ) ) {
			mCanvas = new Canvas();
			mCanvas.drawColor( 0 );
			onDrawModeChanged();
		}
	}

	/** The m moved. */
	private boolean mMoved = false;

	/**
	 * Touch_start.
	 * 
	 * @param x
	 *           the x
	 * @param y
	 *           the y
	 */
	private void touch_start( float x, float y ) {

		mMoved = false;

		tmpPath.reset();
		tmpPath.moveTo( x, y );

		mX = x;
		mY = y;
		mStartX = x;
		mStartY = y;

		if ( mDrawListener != null ) {
			float mappedPoints[] = new float[2];
			mappedPoints[0] = x;
			mappedPoints[1] = y;
			mInvertedMatrix.mapPoints( mappedPoints );
			tmpPath.lineTo( x + .1f, y );
			mDrawListener.onDrawStart( mappedPoints, (int) ( mBrushSize / mCurrentScale ) );
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

			if ( !mMoved ) {
				tmpPath.setLastPoint( mX, mY );
			}

			mMoved = true;

			if ( mRestiction > 0 ) {
				double r = Math.sqrt( Math.pow( x - mStartX, 2 ) + Math.pow( y - mStartY, 2 ) );
				double theta = Math.atan2( y - mStartY, x - mStartX );

				final float w = getWidth();
				final float h = getHeight();

				double scale = ( mRestiction / mCurrentScale ) / (double) ( w + h ) / ( mBrushSize / mCurrentScale );
				double rNew = Math.log( r * scale + 1 ) / scale;

				x = (float) ( mStartX + rNew * Math.cos( theta ) );
				y = (float) ( mStartY + rNew * Math.sin( theta ) );
			}

			tmpPath.quadTo( mX, mY, ( x + mX ) / 2, ( y + mY ) / 2 );
			mX = x;
			mY = y;
		}

		if ( mDrawListener != null ) {
			float mappedPoints[] = new float[2];
			mappedPoints[0] = x;
			mappedPoints[1] = y;
			mInvertedMatrix.mapPoints( mappedPoints );
			mDrawListener.onDrawing( mappedPoints, (int) ( mBrushSize / mCurrentScale ) );
		}
	}

	/**
	 * Touch_up.
	 */
	private void touch_up() {

		tmpPath.reset();

		if ( mDrawListener != null ) {
			mDrawListener.onDrawEnd();
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
}
