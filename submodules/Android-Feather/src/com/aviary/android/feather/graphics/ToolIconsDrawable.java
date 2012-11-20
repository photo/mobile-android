package com.aviary.android.feather.graphics;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;

/**
 * The Class ToolIconsDrawable.
 */
public class ToolIconsDrawable extends StateListDrawable {

	Drawable mDrawable;
	final static int state_pressed = android.R.attr.state_pressed;

	/** The white color filter. */
	ColorMatrixColorFilter whiteColorFilter = new ColorMatrixColorFilter( new float[] {
		1, 0, 0, 0, 255, 0, 1, 0, 0, 255, 0, 0, 1, 0, 255, 0, 0, 0, 1, 0, } );

	/**
	 * Instantiates a new tool icons drawable.
	 * 
	 * @param res
	 *           the res
	 * @param resId
	 *           the res id
	 */
	public ToolIconsDrawable( Resources res, int resId ) {
		super();
		mDrawable = res.getDrawable( resId );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.graphics.drawable.DrawableContainer#onBoundsChange(android.graphics.Rect)
	 */
	@Override
	protected void onBoundsChange( Rect bounds ) {
		super.onBoundsChange( bounds );
		mDrawable.setBounds( bounds );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.graphics.drawable.DrawableContainer#getIntrinsicHeight()
	 */
	@Override
	public int getIntrinsicHeight() {
		if ( mDrawable != null ) return mDrawable.getIntrinsicHeight();
		return super.getIntrinsicHeight();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.graphics.drawable.DrawableContainer#getIntrinsicWidth()
	 */
	@Override
	public int getIntrinsicWidth() {
		if ( mDrawable != null ) return mDrawable.getIntrinsicWidth();
		return super.getIntrinsicWidth();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.graphics.drawable.DrawableContainer#draw(android.graphics.Canvas)
	 */
	@Override
	public void draw( Canvas canvas ) {
		if ( mDrawable != null ) {
			mDrawable.draw( canvas );
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.graphics.drawable.DrawableContainer#getOpacity()
	 */
	@Override
	public int getOpacity() {
		return PixelFormat.TRANSLUCENT;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.graphics.drawable.DrawableContainer#setAlpha(int)
	 */
	@Override
	public void setAlpha( int alpha ) {
		if ( mDrawable != null ) mDrawable.setAlpha( alpha );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.graphics.drawable.DrawableContainer#setColorFilter(android.graphics.ColorFilter)
	 */
	@Override
	public void setColorFilter( ColorFilter cf ) {
		if ( mDrawable != null ) {
			mDrawable.setColorFilter( cf );
		}
		invalidateSelf();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.graphics.drawable.StateListDrawable#onStateChange(int[])
	 */
	@Override
	protected boolean onStateChange( int[] state ) {

		boolean pressed = false;

		if ( state != null && state.length > 0 ) {
			for ( int i = 0; i < state.length; i++ ) {
				if ( state[i] == state_pressed ) {
					pressed = true;
					break;
				}
			}
		}

		boolean result = pressed != mPressed;
		setPressed( pressed );
		return result;
	}

	/** The m pressed. */
	private boolean mPressed;

	/**
	 * Sets the pressed.
	 * 
	 * @param value
	 *           the new pressed
	 */
	public void setPressed( boolean value ) {
		if ( value ) {
			setColorFilter( whiteColorFilter );
		} else {
			setColorFilter( null );
		}
		mPressed = value;
	}
}
