package com.aviary.android.feather.graphics;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import com.aviary.android.feather.library.graphics.drawable.IBitmapDrawable;

/**
 * The Class StickerBitmapDrawable.
 */
public class StickerBitmapDrawable extends Drawable implements IBitmapDrawable {

	protected Bitmap mBitmap;
	protected Paint mPaint;
	protected Paint mShadowPaint;
	protected Rect srcRect;
	protected Rect dstRect;
	protected ColorFilter blackColorFilter, whiteColorFilter;
	protected int mInset;

	/**
	 * Instantiates a new sticker bitmap drawable.
	 * 
	 * @param b
	 *           the b
	 * @param inset
	 *           the inset
	 */
	public StickerBitmapDrawable( Bitmap b, int inset ) {
		mBitmap = b;
		mPaint = new Paint();
		mPaint.setAntiAlias( true );
		mPaint.setDither( true );
		mPaint.setFilterBitmap( false );
		mInset = inset;

		mShadowPaint = new Paint( mPaint );
		mShadowPaint.setAntiAlias( true );

		srcRect = new Rect( 0, 0, b.getWidth(), b.getHeight() );
		dstRect = new Rect();

		blackColorFilter = new ColorMatrixColorFilter( new float[] { 1, 0, 0, -255, 0, 0, 1, 0, -255, 0, 0, 0, 1, -255, 0, 0, 0, 0, 0.3f, 0, } );

		whiteColorFilter = new ColorMatrixColorFilter( new float[] { 1, 0, 0, 0, 255, 0, 1, 0, 0, 255, 0, 0, 1, 0, 255, 0, 0, 0, 1, 0, } );

		mShadowPaint.setColorFilter( blackColorFilter );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.graphics.drawable.Drawable#draw(android.graphics.Canvas)
	 */
	@Override
	public void draw( Canvas canvas ) {

		dstRect.set( srcRect );
		dstRect.inset( -mInset / 2, -mInset / 2 );
		dstRect.offset( mInset / 2 + 1, mInset / 2 + 1 );

		canvas.drawBitmap( mBitmap, srcRect, dstRect, mShadowPaint );

		mPaint.setColorFilter( whiteColorFilter );
		dstRect.offset( -1, -1 );
		canvas.drawBitmap( mBitmap, srcRect, dstRect, mPaint );

		mPaint.setColorFilter( null );
		canvas.drawBitmap( mBitmap, mInset / 2, mInset / 2, mPaint );

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
	public void setColorFilter( ColorFilter cf ) {
		mPaint.setColorFilter( cf );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.graphics.drawable.Drawable#getIntrinsicWidth()
	 */
	@Override
	public int getIntrinsicWidth() {
		return mBitmap.getWidth() + ( mInset * 2 );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.graphics.drawable.Drawable#getIntrinsicHeight()
	 */
	@Override
	public int getIntrinsicHeight() {
		return mBitmap.getHeight() + ( mInset * 2 );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.graphics.drawable.Drawable#getMinimumWidth()
	 */
	@Override
	public int getMinimumWidth() {
		return mBitmap.getWidth() + ( mInset * 2 );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.graphics.drawable.Drawable#getMinimumHeight()
	 */
	@Override
	public int getMinimumHeight() {
		return mBitmap.getHeight() + ( mInset * 2 );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aviary.android.feather.library.graphics.drawable.IBitmapDrawable#getBitmap()
	 */
	@Override
	public Bitmap getBitmap() {
		return mBitmap;
	}
}