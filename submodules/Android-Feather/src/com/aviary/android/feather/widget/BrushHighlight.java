package com.aviary.android.feather.widget;

import it.sephiroth.android.library.imagezoom.easing.Cubic;
import it.sephiroth.android.library.imagezoom.easing.Easing;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

// TODO: Auto-generated Javadoc
/**
 * The Class BrushHighlight.
 */
class BrushHighlight {

	/** The m context. */
	private View mContext;

	/** The Constant LOG_TAG. */
	static final String LOG_TAG = ImageViewTouchBrush.LOG_TAG;

	/** The m brushes. */
	List<Brush> mBrushes = Collections.synchronizedList( new ArrayList<Brush>() );

	/**
	 * Instantiates a new brush highlight.
	 * 
	 * @param context
	 *           the context
	 */
	BrushHighlight( View context ) {
		mContext = context;
	}

	/**
	 * Adds the touch.
	 * 
	 * @param x
	 *           the x
	 * @param y
	 *           the y
	 * @param duration
	 *           the duration
	 * @param endSize
	 *           the end size
	 */
	public void addTouch( final float x, final float y, final long duration, final float endSize ) {
		if ( mContext != null ) {

			Brush brush = new Brush( x, y, duration, endSize );

			synchronized ( mBrushes ) {
				mBrushes.add( brush );
			}

			mContext.invalidate();
		}
	}

	/**
	 * Clear.
	 */
	public void clear() {
		mContext = null;
		mBrushes.clear();
	}

	/**
	 * Draw.
	 * 
	 * @param canvas
	 *           the canvas
	 */
	protected void draw( Canvas canvas ) {

		boolean shouldInvalidate = false;

		synchronized ( mBrushes ) {

			if ( mBrushes.size() > 0 ) {
				shouldInvalidate = true;
				int i = mBrushes.size() - 1;
				while ( i >= 0 ) {
					Brush brush = mBrushes.get( i );
					if ( brush.mActive ) {
						brush.draw( canvas );
					} else {
						mBrushes.remove( i );
					}
					i--;
				}
			}
		}

		if ( shouldInvalidate ) mContext.invalidate();
	}

	/**
	 * The Class Brush.
	 */
	class Brush {

		/** The m paint. */
		private Paint mPaint;

		/** The m start time. */
		private long mStartTime;

		/** The m duration ms. */
		private long mDurationMs;

		/** The m active. */
		private boolean mActive;

		/** The m y. */
		private double mX, mY;

		/** The m end size. */
		private double mEndSize;

		/** The m easing. */
		private Easing mEasing = new Cubic();

		/**
		 * Instantiates a new brush.
		 * 
		 * @param x
		 *           the x
		 * @param y
		 *           the y
		 * @param duration
		 *           the duration
		 * @param endSize
		 *           the end size
		 */
		public Brush( double x, double y, long duration, double endSize ) {
			mX = x;
			mY = y;
			mDurationMs = duration;
			mEndSize = endSize;
			mStartTime = System.currentTimeMillis();
			mActive = true;
			mPaint = new Paint( Paint.ANTI_ALIAS_FLAG );
			mPaint.setColor( Color.BLACK );
		}

		/**
		 * Draw.
		 * 
		 * @param canvas
		 *           the canvas
		 */
		protected void draw( Canvas canvas ) {

			if ( mActive ) {
				final long now = System.currentTimeMillis();
				final float currentMs = Math.min( mDurationMs, now - mStartTime );

				final double radius = mEndSize;
				final double alpha = mEasing.easeOut( currentMs, 0.0f, 255.0f, mDurationMs );

				if ( ( now - mStartTime ) > mDurationMs ) {
					mActive = false;
					return;
				}

				mPaint.setAlpha( 255 - (int) alpha );
				canvas.drawCircle( (float) mX, (float) mY, (float) radius, mPaint );
			}
		}

	};
}
