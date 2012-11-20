package com.aviary.android.feather.graphics;

import java.lang.ref.WeakReference;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.Log;

public class ExternalFilterPackDrawable extends Drawable {

	Paint mPaint;
	Matrix mMatrix;

	Matrix mDrawMatrix = new Matrix();
	Paint mDrawPaint = new Paint();

	static final int defaultWidth = 161;
	static final int defaultHeight = 250;
	final int bitmapWidth;
	final int bitmapHeight;
	float xRatio;
	float yRatio;

	private String mTitle, mShortTitle;
	private int mNumEffects;
	private int mColor;

	private WeakReference<Bitmap> mShadowBitmap;
	private WeakReference<Bitmap> mEffectBitmap;

	public ExternalFilterPackDrawable( String title, String shortTitle, int numEffects, int color, Typeface typeFace, Bitmap shadow,
			Bitmap effect ) {
		super();
		mMatrix = new Matrix();
		mPaint = new Paint( Paint.ANTI_ALIAS_FLAG );
		mPaint.setSubpixelText( true );
		mPaint.setAntiAlias( true );
		mPaint.setDither( true );
		mPaint.setFilterBitmap( true );

		bitmapWidth = effect.getWidth();
		bitmapHeight = effect.getHeight();

		Log.d( "xxx", "size: " + bitmapWidth + "x" + bitmapHeight );

		xRatio = (float) defaultWidth / bitmapWidth;
		yRatio = (float) defaultHeight / bitmapHeight;

		mShadowBitmap = new WeakReference<Bitmap>( shadow );
		mEffectBitmap = new WeakReference<Bitmap>( effect );

		mTitle = title;
		mShortTitle = shortTitle;
		mNumEffects = numEffects < 0 ? 6 : numEffects;
		mColor = color;

		if ( null != typeFace ) {
			mPaint.setTypeface( typeFace );
		}
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
	}

	@Override
	protected void onBoundsChange( Rect bounds ) {
		super.onBoundsChange( bounds );
	}

	@Override
	public void draw( Canvas canvas ) {
		drawInternal( canvas );
	}

	@Override
	public int getIntrinsicWidth() {
		return bitmapWidth;
	}

	@Override
	public int getIntrinsicHeight() {
		return bitmapHeight;
	}

	private void drawInternal( Canvas canvas ) {

		final int leftTextSize = (int) ( 32 / xRatio );
		final int titleTextSize = (int) ( 26 / yRatio );

		Bitmap bmp;

		// SHADOW
		mMatrix.reset();

		if ( null != mShadowBitmap ) {
			bmp = mShadowBitmap.get();
			if ( null != bmp ) {
				canvas.drawBitmap( bmp, mMatrix, mPaint );
			}
		}

		// colored background
		mPaint.setColor( mColor );
		canvas.drawRect( 17 / xRatio, 37 / yRatio, 145 / xRatio, 225 / yRatio, mPaint );

		int saveCount = canvas.save( Canvas.MATRIX_SAVE_FLAG );
		canvas.rotate( 90 );

		// SHORT TITLE
		mPaint.setColor( Color.BLACK );
		mPaint.setTextSize( leftTextSize );
		canvas.drawText( mShortTitle, 66 / xRatio, -26 / yRatio, mPaint );

		// TITLE
		mPaint.setTextSize( titleTextSize );
		canvas.drawText( mTitle, 69 / xRatio, -88 / yRatio, mPaint );

		// NUM EFFECTS
		mPaint.setARGB( 255, 35, 31, 42 );
		canvas.drawRect( 135 / yRatio, -16 / xRatio, 216 / yRatio, -57 / xRatio, mPaint );
		mPaint.setColor( Color.WHITE );
		mPaint.setTextSize( leftTextSize );
		String text = mNumEffects + "fx";
		int numEffectsLength = String.valueOf( mNumEffects ).length();
		canvas.drawText( text, ( 160 - ( numEffectsLength * 10 ) ) / xRatio, -26 / yRatio, mPaint );

		// restore canvas
		canvas.restoreToCount( saveCount );

		// cannister
		if ( null != mEffectBitmap ) {
			bmp = mEffectBitmap.get();
			if ( null != bmp ) {
				canvas.drawBitmap( bmp, mMatrix, mPaint );
			}
		}
	}

	@Override
	public int getOpacity() {
		return PixelFormat.TRANSLUCENT;
	}

	@Override
	public void setAlpha( int alpha ) {}

	@Override
	public void setColorFilter( ColorFilter cf ) {}

	@Override
	public void setBounds( int left, int top, int right, int bottom ) {
		Log.d( "xxx", "setBounds: " + left + ", " + top + ", " + right + ", " + bottom );
		super.setBounds( left, top, right, bottom );
	}
}
