package com.aviary.android.feather.graphics;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import com.aviary.android.feather.R;
import com.aviary.android.feather.widget.Gallery;

/**
 * Default Drawable used for all the Views created inside a {@link Gallery}.<br/ >
 * This drawable will only draw the default background based on the pressed state.<br />
 * Note that this {@link Drawable} should be used inside a {@link StateListDrawable} instance.
 * 
 * @author alessandro
 * 
 */
public class DefaultGalleryCheckboxDrawable extends Drawable {

	private Paint mPaint;
	protected Rect mRect;
	private int backgroundColor; // 0xFF2e2e2e - 0xFF404040
	private int borderColor; // 0xff535353 - 0xFF626262

	/**
	 * Instantiates a new default gallery checkbox drawable.
	 * 
	 * @param res
	 *           {@link Context} resource manager
	 * @param pressed
	 *           pressed state. it will affect the background colors
	 */
	public DefaultGalleryCheckboxDrawable( Resources res, boolean pressed ) {
		super();
		mPaint = new Paint( Paint.ANTI_ALIAS_FLAG );
		mRect = new Rect();

		if ( pressed ) {
			backgroundColor = res.getColor( R.color.feather_crop_adapter_background_selected );
			borderColor = res.getColor( R.color.feather_crop_adapter_border_selected );
		} else {
			backgroundColor = res.getColor( R.color.feather_crop_adapter_background_normal );
			borderColor = res.getColor( R.color.feather_crop_adapter_border_normal );
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.graphics.drawable.Drawable#draw(android.graphics.Canvas)
	 */
	@Override
	public void draw( Canvas canvas ) {
		copyBounds( mRect );

		mPaint.setColor( backgroundColor );
		canvas.drawPaint( mPaint );

		mPaint.setColor( Color.BLACK );
		canvas.drawRect( 0, 0, 1, mRect.bottom, mPaint );
		canvas.drawRect( mRect.right - 1, 0, mRect.right, mRect.bottom, mPaint );

		mPaint.setColor( borderColor );
		canvas.drawRect( 1, 0, 3, mRect.bottom, mPaint );
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
