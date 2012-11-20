package com.aviary.android.feather.widget;

import it.sephiroth.android.library.imagezoom.easing.Easing;
import it.sephiroth.android.library.imagezoom.easing.Linear;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import com.aviary.android.feather.R;
import com.aviary.android.feather.library.log.LoggerFactory;
import com.aviary.android.feather.library.log.LoggerFactory.Logger;
import com.aviary.android.feather.library.log.LoggerFactory.LoggerType;

public class EffectThumbLayout extends RelativeLayout {

	private boolean opened;
	int mThumbSelectionHeight = 20;
	int mThumbAnimationDuration = 200;
	View mHiddenView;
	View mImageView;
	Logger logger = LoggerFactory.getLogger( "effect-thumb", LoggerType.ConsoleLoggerType );

	public EffectThumbLayout( Context context, AttributeSet attrs ) {
		super( context, attrs );
		init( context, attrs, 0 );
		opened = false;
	}

	private void init( Context context, AttributeSet attrs, int defStyle ) {
		TypedArray a = context.obtainStyledAttributes( attrs, R.styleable.EffectThumbLayout, defStyle, 0 );

		mThumbSelectionHeight = a.getDimensionPixelSize( R.styleable.EffectThumbLayout_selectedHeight, 20 );
		mThumbAnimationDuration = a.getInteger( R.styleable.EffectThumbLayout_selectionAnimationDuration, 200 );
		a.recycle();
	}

	@Override
	public void setSelected( boolean selected ) {

		boolean animate = isSelected() != selected;

		super.setSelected( selected );

		if ( null != mHiddenView && animate ) {
			mHiddenView.setVisibility( View.VISIBLE );
			if ( selected ) {
				open();
			} else {
				close();
			}
		} else {
			opened = selected;
		}
	}

	void open() {
		animateView( mThumbAnimationDuration, false );
	}

	void close() {
		animateView( mThumbAnimationDuration, true );
	}

	void setIsOpened( boolean value ) {
		if ( null != mHiddenView ) {
			opened = value;
			postSetIsOpened();
		} else {
			opened = value;
		}
	}
	
	protected void postSetIsOpened() {
		if( null != getHandler() ){
			getHandler().postDelayed( new Runnable() {
				
				@Override
				public void run() {
					if( mImageView != null && mHiddenView != null ){
						if( mHiddenView.getHeight() == 0 ){
							postSetIsOpened();
							return;
						}
						mHiddenView.setVisibility( opened ? View.VISIBLE : View.INVISIBLE );
						mImageView.setPadding( 0, 0, 0, opened ? mHiddenView.getHeight() : 0 );
					}
				}
			}, 10 );
		}
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		mHiddenView = null;
		mImageView = null;
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		mHiddenView = findViewById( R.id.hidden );
		mImageView = findViewById( R.id.image );
		setIsOpened( opened );
	}
	
	protected void postAnimateView( final int durationMs, final boolean isClosing ) {
		if( null != getHandler() ){
			getHandler().post( new Runnable() {
				
				@Override
				public void run() {
					animateView( durationMs, isClosing );
				}
			} );
		}
	}
	
	protected void animateView( final int durationMs, final boolean isClosing ) {

		boolean is_valid = mHiddenView != null && mImageView != null;

		if ( !is_valid ) return;
		
		if( mHiddenView.getHeight() == 0 ){
			postAnimateView( durationMs, isClosing );
		}

		final long startTime = System.currentTimeMillis();
		final float startHeight = 0;
		final float endHeight = isClosing ? mImageView.getPaddingBottom() : mHiddenView.getHeight();
		
		final Easing easing = new Linear();

		if ( null != mHiddenView && null != getParent() && null != getHandler() ) {
			
			getHandler().post( new Runnable() {

				@Override
				public void run() {
					
					if ( null != mHiddenView ) {
						long now = System.currentTimeMillis();

						float currentMs = Math.min( durationMs, now - startTime );
						float newHeight = (float) easing.easeOut( currentMs, startHeight, endHeight, durationMs );

						int height = isClosing ? (int) ( endHeight - newHeight ) : (int) newHeight;
						mImageView.setPadding( 0, 0, 0, height );

						if ( currentMs < durationMs ) {
							if ( null != getHandler() ) {
								getHandler().post( this );
							}
						} else {
							opened = !isClosing;
							if ( null != getParent() ) {
								requestLayout();
							}
						}
					}
				}
			} );
		}
	}

}
