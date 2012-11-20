package com.aviary.android.feather.graphics;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Drawable.Callback;
import com.aviary.android.feather.widget.Gallery;

/**
 * Default {@link Gallery} drawable which accept a {@link Drawable} as overlay.
 * 
 * @author alessandro
 * @see DefaultGalleryCheckboxDrawable
 */
public class OverlayGalleryCheckboxDrawable extends DefaultGalleryCheckboxDrawable implements Callback {

	protected Drawable mOverlayDrawable;
	protected float mBottomOffset;
	protected float mPaddingW;
	protected float mPaddingH;

	/**
	 * Instantiates a new overlay gallery checkbox drawable.
	 * 
	 * @param res
	 *           {@link Context} resource manager
	 * @param pressed
	 *           pressed state
	 * @param drawable
	 *           drawable used as overlay
	 * @param bottomOffset
	 *           value from 0.0 to 1.0. It's the maximum height of the overlay. According to this value the size and the center of
	 *           the overlay drawable will change.
	 * @param padding
	 *           padding of the overlay drawable
	 */
	public OverlayGalleryCheckboxDrawable( Resources res, boolean pressed, Drawable drawable, float bottomOffset, float padding ) {
		this( res, pressed, drawable, bottomOffset, padding, padding );
	}

	public OverlayGalleryCheckboxDrawable( Resources res, boolean pressed, Drawable drawable, float bottomOffset, float paddingW,
			float paddingH ) {
		super( res, pressed );
		mOverlayDrawable = drawable;
		mBottomOffset = bottomOffset;
		mPaddingW = paddingW;
		mPaddingH = paddingH;

		if ( mOverlayDrawable != null ) mOverlayDrawable.setCallback( this );
	}

	@Override
	protected void onBoundsChange( Rect bounds ) {
		super.onBoundsChange( bounds );

		if ( mOverlayDrawable != null ) {
			final float maxHeight = bounds.height() * mBottomOffset;
			final int paddingW = (int) ( mPaddingW * bounds.width() );
			final int paddingH = (int) ( mPaddingH * maxHeight );

			Rect mBoundsRect = new Rect( paddingW, paddingH, bounds.width() - paddingW, (int) maxHeight - paddingH );

			mOverlayDrawable.setBounds( mBoundsRect );
		}
	}

	@Override
	public void draw( Canvas canvas ) {
		super.draw( canvas );

		if ( mOverlayDrawable != null ) {
			mOverlayDrawable.draw( canvas );
		}
	}

	@Override
	public void invalidateDrawable( Drawable arg0 ) {
		invalidateSelf();
	}

	@Override
	public void scheduleDrawable( Drawable arg0, Runnable arg1, long arg2 ) {

	}

	@Override
	public void unscheduleDrawable( Drawable arg0, Runnable arg1 ) {}
}
