package com.aviary.android.feather.graphics;

import android.graphics.BlurMaskFilter;
import android.graphics.BlurMaskFilter.Blur;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.widget.Toast;

/**
 * Default drawable used to display the {@link Toast} preview for panels like the RedEye panel, Whiten, etc..
 * 
 * @author alessandro
 * 
 */
public class PreviewCircleDrawable extends Drawable {

	Paint mPaint;
	float mRadius;

	/**
	 * Instantiates a new preview circle drawable.
	 * 
	 * @param radius
	 *           the radius
	 */
	public PreviewCircleDrawable( final float radius ) {

		mPaint = new Paint( Paint.ANTI_ALIAS_FLAG );
		mPaint.setFilterBitmap( false );
		mPaint.setDither( true );
		mPaint.setStrokeWidth( 10.0f );
		mPaint.setStyle( Paint.Style.STROKE );
		mPaint.setColor( Color.WHITE );
		mRadius = radius;
	}

	/**
	 * Sets the paint.
	 * 
	 * @param value
	 *           the new paint
	 */
	public void setPaint( Paint value ) {
		mPaint.set( value );
	}

	/**
	 * Sets the Paint style.
	 * 
	 * @param value
	 *           the new style
	 */
	public void setStyle( Paint.Style value ) {
		mPaint.setStyle( value );
	}

	/**
	 * Sets the radius.
	 * 
	 * @param value
	 *           the new radius
	 */
	public void setRadius( float value ) {
		mRadius = value;
		invalidateSelf();
	}

	/**
	 * Sets the color.
	 * 
	 * @param color
	 *           the new color
	 */
	public void setColor( int color ) {
		mPaint.setColor( color );
	}

	/**
	 * Sets the blur.
	 * 
	 * @param value
	 *           the new blur
	 */
	public void setBlur( int value ) {
		if ( value > 0 ) {
			mPaint.setMaskFilter( new BlurMaskFilter( value, Blur.NORMAL ) );
		} else {
			mPaint.setMaskFilter( null );
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.graphics.drawable.Drawable#draw(android.graphics.Canvas)
	 */
	@Override
	public void draw( final Canvas canvas ) {
		final RectF rect = new RectF( getBounds() );
		canvas.drawCircle( rect.centerX(), rect.centerY(), mRadius, mPaint );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.graphics.drawable.Drawable#getOpacity()
	 */
	@Override
	public int getOpacity() {
		return PixelFormat.OPAQUE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.graphics.drawable.Drawable#setAlpha(int)
	 */
	@Override
	public void setAlpha( final int alpha ) {}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.graphics.drawable.Drawable#setColorFilter(android.graphics.ColorFilter)
	 */
	@Override
	public void setColorFilter( final ColorFilter cf ) {}

};