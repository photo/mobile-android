package com.aviary.android.feather.widget;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;

// TODO: Auto-generated Javadoc
/**
 * The Class ImageViewTouchBrush.
 */
public class ImageViewTouchBrush extends ImageViewTouch {

	/** The m brush highlight. */
	private BrushHighlight mBrushHighlight;

	/** The m single tap confirmed listener. */
	private OnSingleTapConfirmedListener mSingleTapConfirmedListener;

	/**
	 * The listener interface for receiving onSingleTapConfirmed events. The class that is interested in processing a
	 * onSingleTapConfirmed event implements this interface, and the object created with that class is registered with a component
	 * using the component's <code>addOnSingleTapConfirmedListener<code> method. When
	 * the onSingleTapConfirmed event occurs, that object's appropriate
	 * method is invoked.
	 * 
	 * @see OnSingleTapConfirmedEvent
	 */
	public static interface OnSingleTapConfirmedListener {

		/**
		 * On single tap.
		 * 
		 * @param x
		 *           the x
		 * @param y
		 *           the y
		 */
		void onSingleTap( float x, float y );
	}

	/**
	 * Instantiates a new image view touch brush.
	 * 
	 * @param context
	 *           the context
	 * @param attrs
	 *           the attrs
	 */
	public ImageViewTouchBrush( Context context, AttributeSet attrs ) {
		super( context, attrs );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.sephiroth.android.library.imagezoom.ImageViewTouch#init()
	 */
	@Override
	protected void init() {
		super.init();
		mBrushHighlight = new BrushHighlight( this );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.ImageView#onDraw(android.graphics.Canvas)
	 */
	@Override
	protected void onDraw( Canvas canvas ) {
		super.onDraw( canvas );
		mBrushHighlight.draw( canvas );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.sephiroth.android.library.imagezoom.ImageViewTouch#getGestureListener()
	 */
	@Override
	protected OnGestureListener getGestureListener() {
		return new GestureListenerNoDoubleTap();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View#onDetachedFromWindow()
	 */
	@Override
	protected void onDetachedFromWindow() {
		mBrushHighlight.clear();
		mBrushHighlight = null;
		super.onDetachedFromWindow();
	}

	/**
	 * Sets the on single tap confirmed listener.
	 * 
	 * @param listener
	 *           the new on single tap confirmed listener
	 */
	public void setOnSingleTapConfirmedListener( OnSingleTapConfirmedListener listener ) {
		mSingleTapConfirmedListener = listener;
	}

	/**
	 * Do some stuff.
	 * 
	 * @param x
	 *           the x
	 * @param y
	 *           the y
	 */
	private void doSomeStuff( float x, float y ) {

		if ( mSingleTapConfirmedListener != null ) {
			mSingleTapConfirmedListener.onSingleTap( x, y );
		}

		playSoundEffect( SoundEffectConstants.CLICK );
		mBrushHighlight.addTouch( x, y, mBrushDuration, mBrushEndSize );

	}

	/**
	 * The Class GestureListenerNoDoubleTap.
	 */
	class GestureListenerNoDoubleTap extends GestureListener {

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.view.GestureDetector.SimpleOnGestureListener#onSingleTapConfirmed(android.view.MotionEvent)
		 */
		@Override
		public boolean onSingleTapConfirmed( MotionEvent e ) {
			doSomeStuff( e.getX(), e.getY() );
			return super.onSingleTapConfirmed( e );
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see it.sephiroth.android.library.imagezoom.ImageViewTouch.GestureListener#onDoubleTap(android.view.MotionEvent)
		 */
		@Override
		public boolean onDoubleTap( MotionEvent e ) {
			doSomeStuff( e.getX(), e.getY() );
			return false;
		}
	}

	/** The m brush end size. */
	private float mBrushEndSize = 10.f;

	/** The m brush duration. */
	private long mBrushDuration = 400;

	/**
	 * Sets the tap radius.
	 * 
	 * @param f
	 *           the new tap radius
	 */
	public void setTapRadius( float f ) {
		mBrushEndSize = f;
	}

	/**
	 * Sets the brush duration.
	 * 
	 * @param duration
	 *           the new brush duration
	 */
	public void setBrushDuration( long duration ) {
		mBrushDuration = duration;
	}
}
