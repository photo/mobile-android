package com.aviary.android.feather.widget;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

// TODO: Auto-generated Javadoc
/**
 * The Class IToast.
 */
public class IToast {

	/** The LO g_ tag. */
	final String LOG_TAG = "toast";

	/** The m context. */
	Context mContext;

	/** The m window manager. */
	WindowManager mWindowManager;

	/** The m next view. */
	View mNextView;

	/** The m view. */
	View mView;

	/** The m duration. */
	int mDuration;

	/** The m gravity. */
	int mGravity = Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL;

	/** The m y. */
	int mX, mY;

	/** The m tn. */
	final TN mTN;

	/** The m horizontal margin. */
	float mHorizontalMargin;

	/** The m vertical margin. */
	float mVerticalMargin;

	/** The m handler. */
	final Handler mHandler = new Handler();

	public static interface LayoutListener {

		public void onShown( View currentView );

		public void onHidden();
	}

	private LayoutListener mLayoutListener;

	/**
	 * Instantiates a new i toast.
	 * 
	 * @param context
	 *           the context
	 */
	public IToast( Context context ) {
		mContext = context;
		mWindowManager = (WindowManager) context.getSystemService( Context.WINDOW_SERVICE );

		mTN = new TN();
		mTN.mWm = mWindowManager;
		mY = 0;
		mX = 0;
	}

	public void setLayoutListener( LayoutListener listener ) {
		mLayoutListener = listener;
	}

	/**
	 * Sets the view.
	 * 
	 * @param v
	 *           the new view
	 */
	public void setView( View v ) {
		mNextView = v;
	}

	/**
	 * Gets the view.
	 * 
	 * @return the view
	 */
	public View getView() {
		return mNextView;
	}

	/**
	 * Show.
	 */
	public void show() {
		if ( mNextView == null ) throw new RuntimeException( "setView must be called first" );
		mTN.show();
	}

	/**
	 * Hide.
	 */
	public void hide() {
		mTN.hide();
	}

	/**
	 * Update.
	 */
	public void update() {

	}

	/**
	 * Make.
	 * 
	 * @param context
	 *           the context
	 * @param duration
	 *           the duration
	 * @return the i toast
	 */
	public static IToast make( Context context, int duration ) {
		IToast result = new IToast( context );
		LayoutInflater inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
		View v = inflater.inflate( com.aviary.android.feather.R.layout.feather_itoast_layout, null );
		result.mNextView = v;
		result.mDuration = duration;
		return result;
	}

	/**
	 * The Class TN.
	 */
	private class TN {

		/** The m show. */
		final Runnable mShow = new Runnable() {

			@Override
			public void run() {
				handleShow();
			}
		};

		/** The m hide. */
		final Runnable mHide = new Runnable() {

			@Override
			public void run() {
				handleHide();
			}
		};

		/** The m params. */
		WindowManager.LayoutParams mParams = new WindowManager.LayoutParams();

		/** The m wm. */
		WindowManager mWm;

		/**
		 * Instantiates a new tN.
		 */
		TN() {
			final WindowManager.LayoutParams params = mParams;
			params.height = WindowManager.LayoutParams.WRAP_CONTENT;
			params.width = WindowManager.LayoutParams.WRAP_CONTENT;
			params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
					| WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
			params.format = PixelFormat.TRANSLUCENT;
			params.type = WindowManager.LayoutParams.TYPE_TOAST;
			params.windowAnimations = com.aviary.android.feather.R.style.Animations_iToast;
			params.setTitle( "Toast" );
		}

		/**
		 * Show.
		 */
		public void show() {
			mHandler.post( mShow );
		}

		/**
		 * Hide.
		 */
		public void hide() {
			mHandler.post( mHide );
		}

		/**
		 * Handle show.
		 */
		public void handleShow() {

			if ( mView != mNextView ) {
				handleHide();
				mView = mNextView;
				final int gravity = mGravity;
				mParams.gravity = gravity;
				if ( ( gravity & Gravity.HORIZONTAL_GRAVITY_MASK ) == Gravity.FILL_HORIZONTAL ) {
					mParams.horizontalWeight = 1.0f;
				}
				if ( ( gravity & Gravity.VERTICAL_GRAVITY_MASK ) == Gravity.FILL_VERTICAL ) {
					mParams.verticalWeight = 1.0f;
				}
				mParams.x = mX;
				mParams.y = mY;
				mParams.verticalMargin = mVerticalMargin;
				mParams.horizontalMargin = mHorizontalMargin;

				if ( mView.getParent() != null ) {
					mView.setVisibility( View.GONE );
					mWm.removeView( mView );
				}

				mWm.addView( mView, mParams );
				mView.setVisibility( View.VISIBLE );

				if ( mLayoutListener != null ) {
					mLayoutListener.onShown( mView );
				}
			}
		}

		/**
		 * Handle hide.
		 */
		public void handleHide() {
			removeView();
			if ( mLayoutListener != null ) {
				mLayoutListener.onHidden();
			}
		}

		/**
		 * Removes the view.
		 */
		void removeView() {
			if ( mView != null ) {
				if ( mView.getParent() != null ) {
					mView.setVisibility( View.GONE );
					mWm.removeView( mView );
				}
				mView = null;
			}
		}
	};
}
