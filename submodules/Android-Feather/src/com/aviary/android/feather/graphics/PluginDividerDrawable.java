package com.aviary.android.feather.graphics;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

public class PluginDividerDrawable extends Drawable {

	public static final String LOG_TAG = "Drawable";

	private float mTextSize = 10;
	private FontMetrics mMetrics;
	private String mLabel;
	private final Drawable mDrawable;
	private Paint mTextPaint;
	private Rect mBounds;
	private int mWidth;
	private int mHeight;

	public PluginDividerDrawable( Drawable drawable, final String string ) {
		mDrawable = drawable;
		mLabel = string;

		mWidth = drawable.getIntrinsicWidth();
		mHeight = drawable.getIntrinsicHeight();

		mTextPaint = new Paint( Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG | Paint.DEV_KERN_TEXT_FLAG | Paint.HINTING_ON );
		mTextPaint.setColor( Color.WHITE );
		mBounds = new Rect();
		mMetrics = new FontMetrics();

		onTextBoundsChanged();
	}

	public void setTitle( final String value ) {
		mLabel = value;
		invalidateSelf();
		onTextBoundsChanged();
	}

	public final String getTitle() {
		return mLabel;
	}

	@Override
	public int getOpacity() {
		return mDrawable.getOpacity();
	}

	@Override
	public void setAlpha( int alpha ) {
		mDrawable.setAlpha( alpha );
	}

	@Override
	public void setColorFilter( ColorFilter cf ) {
		mDrawable.setColorFilter( cf );
	}

	@Override
	public void clearColorFilter() {
		mDrawable.clearColorFilter();
	}

	@Override
	public int getIntrinsicHeight() {
		return mDrawable.getIntrinsicHeight();
	}

	@Override
	public int getIntrinsicWidth() {
		return mDrawable.getIntrinsicWidth();
	}

	@Override
	public int getMinimumHeight() {
		return mDrawable.getMinimumHeight();
	}

	@Override
	public int getMinimumWidth() {
		return mDrawable.getMinimumWidth();
	}

	@Override
	public boolean getPadding( Rect padding ) {
		return mDrawable.getPadding( padding );
	}

	@Override
	public void setBounds( int left, int top, int right, int bottom ) {
		mDrawable.setBounds( left, top, right, bottom );
		super.setBounds( left, top, right, bottom );
	}

	@Override
	public void setBounds( Rect bounds ) {
		mDrawable.setBounds( bounds );
		super.setBounds( bounds );
	}

	@Override
	public void setFilterBitmap( boolean filter ) {
		mDrawable.setFilterBitmap( filter );
	}

	@Override
	public boolean setVisible( boolean visible, boolean restart ) {
		return mDrawable.setVisible( visible, restart );
	}

	@Override
	public void setDither( boolean dither ) {
		mDrawable.setDither( dither );
	}

	@Override
	public void setColorFilter( int color, Mode mode ) {
		mDrawable.setColorFilter( color, mode );
	}

	@Override
	public void invalidateSelf() {
		mDrawable.invalidateSelf();
	}

	@Override
	public boolean isStateful() {
		return mDrawable.isStateful();
	}

	@Override
	protected void onBoundsChange( Rect bounds ) {
		super.onBoundsChange( bounds );

		mWidth = bounds.width();
		mHeight = bounds.height();
		
		onTextBoundsChanged();
	}
	
	private int mTextY = 0;
	
	protected final void onTextBoundsChanged() {
		mTextSize = mWidth / 2.0f;
		mTextPaint.setTextSize( mTextSize );
		mTextPaint.getTextBounds( mLabel, 0, mLabel.length(), mBounds );
		mTextPaint.getFontMetrics( mMetrics );
		
		mTextY = (int) ( ( ( mWidth / 2.0f ) + mTextSize/2.0f ) - mMetrics.bottom/2.0f );
		
		if( mBounds.width() >= mHeight ) {
			if( mLabel.length() > 4 ) {
				mLabel = mLabel.substring( 0, mLabel.length() - 4 ) + "..";
				onTextBoundsChanged();
			}
		}
	}

	@Override
	public void draw( Canvas canvas ) {

		mDrawable.draw( canvas );
		
		int saveCount = canvas.save( Canvas.MATRIX_SAVE_FLAG );
		canvas.rotate( -90 );
		canvas.translate( -mHeight, 0 );

		//int y = (mWidth + mMetrics.ascent);
		
		canvas.drawText( mLabel, (mHeight - mBounds.width())/2, mTextY, mTextPaint );
		canvas.restoreToCount( saveCount );
	}

}
