package com.aviary.android.feather.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.aviary.android.feather.R;
import com.aviary.android.feather.library.graphics.animation.CustomAlphaAnimation;

public class IapNotificationLayout extends LinearLayout {

	/** show/hide delay */
	private int mIapShowDuration, mIapHideDuration;
	private int mIapHideDelay, mIapShowDelay;

	public IapNotificationLayout( Context context ) {
		super( context );
		init( context, null, 0 );
	}

	public IapNotificationLayout( Context context, AttributeSet attrs ) {
		super( context, attrs );
		init( context, attrs, 0 );
	}

	@SuppressLint("NewApi")
	public IapNotificationLayout( Context context, AttributeSet attrs, int defStyle ) {
		super( context, attrs, defStyle );
		init( context, attrs, defStyle );
	}

	private void init( Context context, AttributeSet attrs, int defStyle ) {

		TypedArray a = context.obtainStyledAttributes( attrs, R.styleable.IapNotification, defStyle, 0 );

		mIapShowDuration = a.getInteger( R.styleable.IapNotification_showDuration, 1 );
		mIapHideDuration = a.getInteger( R.styleable.IapNotification_hideDuration, 3 );

		mIapHideDelay = a.getInteger( R.styleable.IapNotification_hideDelay, 30 );
		mIapShowDelay = a.getInteger( R.styleable.IapNotification_showDelay, 3 );

		a.recycle();
	}

	public void setIcon( int resId ) {
		if ( getResources() != null ) {
			ImageView image = (ImageView) findViewById( R.id.iap_image );
			if ( null != image ) {
				image.setImageResource( resId );
			}
		}
	}

	public void setText( CharSequence value ) {
		if ( getResources() != null ) {
			TextView text = (TextView) findViewById( R.id.iap_text );
			if ( null != text ) {
				text.setText( value );
			}
		}
	}

	public void show() {
		show( mIapShowDelay );
	}

	public void show( long delayMillis ) {

		AnimationListener listener = new AnimationListener() {

			@Override
			public void onAnimationStart( Animation animation ) {
				setVisibility( View.VISIBLE );
			}

			@Override
			public void onAnimationRepeat( Animation animation ) {
			}

			@Override
			public void onAnimationEnd( Animation animation ) {
				hide();
			}
		};

		Animation animation = new CustomAlphaAnimation( 0.0f, 1.0f );
		animation.setStartOffset( delayMillis );
		animation.setInterpolator( new DecelerateInterpolator( 1.0f ) );
		animation.setDuration( mIapShowDuration );
		animation.setAnimationListener( listener );
		startAnimation( animation );
	}

	public void hide() {
		hide( mIapHideDelay );
	}

	public void hide( long delayMillis ) {

		if ( getHandler() == null ) return;
		if ( getParent() == null ) return;
		if ( getVisibility() == View.GONE ) return;

		AnimationListener listener = new AnimationListener() {

			@Override
			public void onAnimationStart( Animation animation ) {
			}

			@Override
			public void onAnimationRepeat( Animation animation ) {}

			@Override
			public void onAnimationEnd( Animation animation ) {
				setVisibility( View.GONE );
			}
		};

		float currentAlpha = 1.0f;
		final Animation animation = getAnimation();

		if ( animation != null ) {
			if ( animation instanceof CustomAlphaAnimation ) {
				currentAlpha = ( (CustomAlphaAnimation) animation ).getAlpha();
			}

			getAnimation().setAnimationListener( null );
			clearAnimation();
		}

		Animation newAnimation = new AlphaAnimation( currentAlpha, 0 );
		newAnimation.setDuration( mIapHideDuration );
		newAnimation.setStartOffset( delayMillis );
		newAnimation.setAnimationListener( listener );
		startAnimation( newAnimation );
	}

}
