package com.aviary.android.feather.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;
import com.aviary.android.feather.R;

public class ImageViewWithShadow extends ImageView {

	private Drawable mShadowDrawable;
	private boolean mShadowEnabled = false;
	private int mShadowOffsetX = 10;
	private int mShadowOffsetY = 10;

	public ImageViewWithShadow( Context context, AttributeSet attrs ) {
		super( context, attrs );
		init( context, attrs, 0 );
	}

	private void init( Context context, AttributeSet attrs, int defStyle ) {

		TypedArray a = context.obtainStyledAttributes( attrs, R.styleable.ImageViewWithShadow, defStyle, 0 );

		mShadowDrawable = a.getDrawable( R.styleable.ImageViewWithShadow_shadowDrawable );
		if ( null == mShadowDrawable ) {
			mShadowEnabled = false;
		} else {
			mShadowEnabled = true;
		}

		mShadowOffsetX = a.getInteger( R.styleable.ImageViewWithShadow_shadowOffsetX, 5 );
		mShadowOffsetY = a.getInteger( R.styleable.ImageViewWithShadow_shadowOffsetY, 5 );

		a.recycle();
	}

	public void setShadowEnabled( boolean value ) {
		mShadowEnabled = value;
	}

	@Override
	protected void onDraw( Canvas canvas ) {

		Drawable drawable = getDrawable();
		Matrix matrix = getImageMatrix();

		if ( null != drawable && mShadowEnabled ) {

			int saveCount = canvas.getSaveCount();
			int paddingLeft = getPaddingLeft();
			int paddingTop = getPaddingTop();

			int dwidth = drawable.getIntrinsicWidth();
			int dheight = drawable.getIntrinsicHeight();
			mShadowDrawable.setBounds( 0, 0, dwidth + mShadowOffsetX, dheight + mShadowOffsetY );

			canvas.save();
			canvas.translate( paddingLeft, paddingTop );
			if ( null != matrix ) {
				// canvas.concat( matrix );
			}

			mShadowDrawable.draw( canvas );
			canvas.restoreToCount( saveCount );

		}
		super.onDraw( canvas );

	}

}
