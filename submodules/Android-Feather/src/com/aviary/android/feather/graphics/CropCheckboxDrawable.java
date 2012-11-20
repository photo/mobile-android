package com.aviary.android.feather.graphics;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import com.aviary.android.feather.R;
import com.aviary.android.feather.widget.Gallery;

/**
 * Default checkbox drawable for {@link Gallery} views.<br />
 * Draw a checkbox drawable in order to simulate a checkbox component.<br/>
 * 
 * @author alessandro
 * 
 */
public class CropCheckboxDrawable extends OverlayGalleryCheckboxDrawable {

	/** The default drawable. */
	protected Drawable mCropDrawable;

	/**
	 * Instantiates a new crop checkbox drawable.
	 * 
	 * @param res
	 *           the res
	 * @param pressed
	 *           the pressed
	 * @param resId
	 *           the res id
	 * @param bottomOffset
	 *           the bottom offset
	 * @param padding
	 *           the padding
	 */
	public CropCheckboxDrawable( Resources res, boolean pressed, int resId, float bottomOffset, float paddingW, float paddingH ) {
		this( res, pressed, res.getDrawable( resId ), bottomOffset, paddingW, paddingH );
	}

	/**
	 * Instantiates a new crop checkbox drawable.
	 * 
	 * @param res
	 *           the res
	 * @param pressed
	 *           the pressed
	 * @param drawable
	 *           the drawable
	 * @param bottomOffset
	 *           the bottom offset
	 * @param padding
	 *           the padding
	 */
	public CropCheckboxDrawable( Resources res, boolean pressed, Drawable drawable, float bottomOffset, float paddingW,
			float paddingH ) {
		super( res, pressed, drawable, bottomOffset, paddingW, paddingH );
		mCropDrawable = res.getDrawable( pressed ? R.drawable.feather_crop_checkbox_selected
				: R.drawable.feather_crop_checkbox_unselected );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aviary.android.feather.graphics.OverlayGalleryCheckboxDrawable#draw(android.graphics.Canvas)
	 */
	@Override
	public void draw( Canvas canvas ) {
		super.draw( canvas );
		mCropDrawable.draw( canvas );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aviary.android.feather.graphics.OverlayGalleryCheckboxDrawable#onBoundsChange(android.graphics.Rect)
	 */
	@Override
	protected void onBoundsChange( Rect rect ) {
		super.onBoundsChange( rect );

		int left = (int) ( rect.width() * 0.2831 );
		int top = (int) ( rect.height() * 0.6708 );
		int right = (int) ( rect.width() * 0.7433 );
		int bottom = (int) ( rect.height() * 0.8037 );

		mCropDrawable.setBounds( left, top, right, bottom );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aviary.android.feather.graphics.DefaultGalleryCheckboxDrawable#getOpacity()
	 */
	@Override
	public int getOpacity() {
		return PixelFormat.TRANSLUCENT;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aviary.android.feather.graphics.DefaultGalleryCheckboxDrawable#setAlpha(int)
	 */
	@Override
	public void setAlpha( int alpha ) {}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aviary.android.feather.graphics.DefaultGalleryCheckboxDrawable#setColorFilter(android.graphics.ColorFilter)
	 */
	@Override
	public void setColorFilter( ColorFilter cf ) {}

}
