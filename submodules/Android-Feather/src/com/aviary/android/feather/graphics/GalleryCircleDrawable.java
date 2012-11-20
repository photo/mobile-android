package com.aviary.android.feather.graphics;

import android.graphics.BlurMaskFilter;
import android.graphics.BlurMaskFilter.Blur;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

/**
 * Drawable used as overlay {@link Drawable} in conjunction with {@link OverlayGalleryCheckboxDrawable}.<br />
 * Create a circle drawable using radius and blur_size.
 * 
 * @author alessandro
 * 
 */
public class GalleryCircleDrawable extends Drawable {

	private Paint mPaint;
	private Paint mShadowPaint;
	final int mShadowOffset = 3;
	float mStrokeWidth = 5.0f;
	float mRadius, mOriginalRadius;
	float centerX, centerY;

	/**
	 * Instantiates a new gallery circle drawable.
	 * 
	 * @param radius
	 *           Radius of the circle
	 * @param blur_size
	 *           blur size, if &gt; 0 create a blur mask around the circle
	 */
	public GalleryCircleDrawable( float radius, int blur_size ) {
		super();

		mPaint = new Paint( Paint.ANTI_ALIAS_FLAG );
		mPaint.setStrokeWidth( mStrokeWidth );
		mPaint.setStyle( Paint.Style.STROKE );
		mPaint.setColor( Color.WHITE );

		mShadowPaint = new Paint( mPaint );
		mShadowPaint.setColor( Color.BLACK );

		update( radius, blur_size );
	}

	/**
	 * Sets the stroke width.
	 * 
	 * @param value
	 *           the new stroke width
	 */
	public void setStrokeWidth( float value ) {
		mStrokeWidth = value;
		mPaint.setStrokeWidth( mStrokeWidth );
		invalidateSelf();
	}

	/**
	 * Update.
	 * 
	 * @param radius
	 *           the radius
	 * @param blur_size
	 *           the blur_size
	 */
	public void update( float radius, int blur_size ) {
		mOriginalRadius = radius;
		if ( blur_size > 0 )
			mPaint.setMaskFilter( new BlurMaskFilter( blur_size, Blur.NORMAL ) );
		else
			mPaint.setMaskFilter( null );
		invalidateSelf();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.graphics.drawable.Drawable#draw(android.graphics.Canvas)
	 */
	@Override
	public void draw( Canvas canvas ) {
		canvas.drawCircle( centerX, centerY + mShadowOffset, mRadius, mShadowPaint );
		canvas.drawCircle( centerX, centerY, mRadius, mPaint );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.graphics.drawable.Drawable#onBoundsChange(android.graphics.Rect)
	 */
	@Override
	protected void onBoundsChange( Rect rect ) {
		super.onBoundsChange( rect );
		invalidateSelf();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.graphics.drawable.Drawable#invalidateSelf()
	 */
	@Override
	public void invalidateSelf() {
		super.invalidateSelf();
		invalidate();
	}

	/**
	 * Invalidate.
	 */
	protected void invalidate() {
		Rect rect = getBounds();
		int minSize = Math.max( 1, Math.min( rect.width(), rect.height() ) );
		mRadius = ( (float) minSize * mOriginalRadius ) / 2;
		centerX = rect.centerX();
		centerY = rect.centerY();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.graphics.drawable.Drawable#getOpacity()
	 */
	@Override
	public int getOpacity() {
		return PixelFormat.TRANSLUCENT;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.graphics.drawable.Drawable#setAlpha(int)
	 */
	@Override
	public void setAlpha( int alpha ) {}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.graphics.drawable.Drawable#setColorFilter(android.graphics.ColorFilter)
	 */
	@Override
	public void setColorFilter( ColorFilter cf ) {}
}
