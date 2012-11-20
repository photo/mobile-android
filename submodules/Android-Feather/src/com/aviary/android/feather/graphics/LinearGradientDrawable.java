package com.aviary.android.feather.graphics;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable.Orientation;

/**
 * Draw a linear gradient.
 * 
 * @author alessandro
 */
public class LinearGradientDrawable extends Drawable {

	private final Paint mFillPaint = new Paint( Paint.ANTI_ALIAS_FLAG );
	private float mCornerRadius = 0;
	private boolean mRectIsDirty = true;
	private int mAlpha = 0xFF; // modified by the caller
	private boolean mDither = true;
	private ColorFilter mColorFilter;
	private Orientation mOrientation = Orientation.LEFT_RIGHT;
	private int[] mColors;
	private float[] mPositions;
	private final RectF mRect = new RectF();

	/**
	 * Instantiates a new linear gradient drawable.
	 * 
	 * @param orientation
	 *           the orientation
	 * @param colors
	 *           the colors
	 * @param positions
	 *           the positions
	 */
	public LinearGradientDrawable( Orientation orientation, int[] colors, float[] positions ) {
		mOrientation = orientation;
		mColors = colors;
		mPositions = positions;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.graphics.drawable.Drawable#draw(android.graphics.Canvas)
	 */
	@Override
	public void draw( Canvas canvas ) {

		if ( !ensureValidRect() ) return;

		mFillPaint.setAlpha( mAlpha );
		mFillPaint.setDither( mDither );
		mFillPaint.setColorFilter( mColorFilter );

		if ( mCornerRadius > 0 ) {
			float rad = mCornerRadius;
			float r = Math.min( mRect.width(), mRect.height() ) * 0.5f;
			if ( rad > r ) {
				rad = r;
			}
			canvas.drawRoundRect( mRect, rad, rad, mFillPaint );
		} else {
			canvas.drawRect( mRect, mFillPaint );
		}

	}

	/**
	 * Ensure valid rect.
	 * 
	 * @return true, if successful
	 */
	private boolean ensureValidRect() {
		if ( mRectIsDirty ) {
			mRectIsDirty = false;

			Rect bounds = getBounds();
			float inset = 0;

			mRect.set( bounds.left + inset, bounds.top + inset, bounds.right - inset, bounds.bottom - inset );

			final int[] colors = mColors;

			if ( colors != null ) {
				RectF r = mRect;
				float x0, x1, y0, y1;

				final float level = 1.0f;
				switch ( mOrientation ) {
					case TOP_BOTTOM:
						x0 = r.left;
						y0 = r.top;
						x1 = x0;
						y1 = level * r.bottom;
						break;
					case TR_BL:
						x0 = r.right;
						y0 = r.top;
						x1 = level * r.left;
						y1 = level * r.bottom;
						break;
					case RIGHT_LEFT:
						x0 = r.right;
						y0 = r.top;
						x1 = level * r.left;
						y1 = y0;
						break;
					case BR_TL:
						x0 = r.right;
						y0 = r.bottom;
						x1 = level * r.left;
						y1 = level * r.top;
						break;
					case BOTTOM_TOP:
						x0 = r.left;
						y0 = r.bottom;
						x1 = x0;
						y1 = level * r.top;
						break;
					case BL_TR:
						x0 = r.left;
						y0 = r.bottom;
						x1 = level * r.right;
						y1 = level * r.top;
						break;
					case LEFT_RIGHT:
						x0 = r.left;
						y0 = r.top;
						x1 = level * r.right;
						y1 = y0;
						break;
					default:/* TL_BR */
						x0 = r.left;
						y0 = r.top;
						x1 = level * r.right;
						y1 = level * r.bottom;
						break;
				}

				mFillPaint.setShader( new LinearGradient( x0, y0, x1, y1, colors, mPositions, Shader.TileMode.CLAMP ) );
			}
		}
		return !mRect.isEmpty();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.graphics.drawable.Drawable#onBoundsChange(android.graphics.Rect)
	 */
	@Override
	protected void onBoundsChange( Rect r ) {
		super.onBoundsChange( r );
		mRectIsDirty = true;
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
	public void setAlpha( int alpha ) {
		if ( alpha != mAlpha ) {
			mAlpha = alpha;
			invalidateSelf();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.graphics.drawable.Drawable#setDither(boolean)
	 */
	@Override
	public void setDither( boolean dither ) {
		if ( dither != mDither ) {
			mDither = dither;
			invalidateSelf();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.graphics.drawable.Drawable#setColorFilter(android.graphics.ColorFilter)
	 */
	@Override
	public void setColorFilter( ColorFilter cf ) {
		if ( cf != mColorFilter ) {
			mColorFilter = cf;
			invalidateSelf();
		}
	}

	/**
	 * Sets the corner radius.
	 * 
	 * @param radius
	 *           the new corner radius
	 */
	public void setCornerRadius( float radius ) {
		mCornerRadius = radius;
		invalidateSelf();
	}

}
