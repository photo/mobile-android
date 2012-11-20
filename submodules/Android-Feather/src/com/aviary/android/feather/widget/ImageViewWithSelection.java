package com.aviary.android.feather.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;
import com.aviary.android.feather.R;

public class ImageViewWithSelection extends ImageView {

	private Drawable mSelectionDrawable;
	private Rect mSelectionDrawablePadding;
	private RectF mRect;
	private boolean mSelected;
	private int mPaddingLeft = 0;
	private int mPaddingTop = 0;

	public ImageViewWithSelection( Context context, AttributeSet attrs ) {
		super( context, attrs );
		init( context, attrs, -1 );
	}

	public ImageViewWithSelection( Context context, AttributeSet attrs, int defStyle ) {
		super( context, attrs, defStyle );
		init( context, attrs, defStyle );
	}

	private void init( Context context, AttributeSet attrs, int defStyle ) {
		TypedArray array = context.obtainStyledAttributes( attrs, R.styleable.ImageViewWithSelection, defStyle, 0 );
		mSelectionDrawable = array.getDrawable( R.styleable.ImageViewWithSelection_selectionSrc );
		int pleft = array.getDimensionPixelSize( R.styleable.ImageViewWithSelection_selectionPaddingLeft, 0 );
		int ptop = array.getDimensionPixelSize( R.styleable.ImageViewWithSelection_selectionPaddingTop, 0 );
		int pright = array.getDimensionPixelSize( R.styleable.ImageViewWithSelection_selectionPaddingRight, 0 );
		int pbottom = array.getDimensionPixelSize( R.styleable.ImageViewWithSelection_selectionPaddingBottom, 0 );
		mSelectionDrawablePadding = new Rect( pleft, ptop, pright, pbottom );

		array.recycle();

		mRect = new RectF();
	}

	@Override
	public void setSelected( boolean value ) {
		super.setSelected( value );
		mSelected = value;
		invalidate();
	}

	public boolean getSelected() {
		return mSelected;
	}

	@Override
	protected void onLayout( boolean changed, int left, int top, int right, int bottom ) {
		super.onLayout( changed, left, top, right, bottom );
		mPaddingLeft = getPaddingLeft();
		mPaddingTop = getPaddingTop();
	}

	@Override
	public void setPadding( int left, int top, int right, int bottom ) {
		mPaddingLeft = left;
		mPaddingTop = top;
		super.setPadding( left, top, right, bottom );
	}

	@Override
	protected void onDraw( Canvas canvas ) {

		Drawable drawable = getDrawable();
		if ( null == drawable ) return;

		Rect bounds = drawable.getBounds();
		mRect.set( bounds );

		if ( getImageMatrix() != null ) {
			getImageMatrix().mapRect( mRect );
			mRect.offset( mPaddingLeft, mPaddingTop );
			mRect.inset( -( mSelectionDrawablePadding.left + mSelectionDrawablePadding.right ),
					-( mSelectionDrawablePadding.top + mSelectionDrawablePadding.bottom ) );
		}

		if ( mSelectionDrawable != null && mSelected ) {
			mSelectionDrawable.setBounds( (int) mRect.left, (int) mRect.top, (int) mRect.right, (int) mRect.bottom );
			mSelectionDrawable.draw( canvas );
		}
		super.onDraw( canvas );
	}

}
