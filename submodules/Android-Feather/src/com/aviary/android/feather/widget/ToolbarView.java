package com.aviary.android.feather.widget;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewFlipper;
import android.widget.ViewSwitcher.ViewFactory;
import com.aviary.android.feather.R;

// TODO: Auto-generated Javadoc
/**
 * The Class ToolbarView.
 */
public class ToolbarView extends ViewFlipper implements ViewFactory {

	/**
	 * The listener interface for receiving onToolbarClick events. The class that is interested in processing a onToolbarClick event
	 * implements this interface, and the object created with that class is registered with a component using the component's
	 * <code>addOnToolbarClickListener<code> method. When
	 * the onToolbarClick event occurs, that object's appropriate
	 * method is invoked.
	 * 
	 * @see OnToolbarClickEvent
	 */
	public static interface OnToolbarClickListener {

		/**
		 * On save click.
		 */
		void onSaveClick();

		/**
		 * On apply click.
		 */
		void onApplyClick();

		/**
		 * On cancel click.
		 */
		void onCancelClick();
	};

	/**
	 * The Enum STATE.
	 */
	public static enum STATE {

		/** The STAT e_ save. */
		STATE_SAVE,
		/** The STAT e_ apply. */
		STATE_APPLY,
	};

	/** The m apply button. */
	private Button mApplyButton;

	/** The m save button. */
	private Button mSaveButton;

	/** The m title text. */
	private TextSwitcher mTitleText;

	/** The m aviary logo. */
	private TextView mAviaryLogo;

	/** The is animating. */
	@SuppressWarnings("unused")
	private boolean isAnimating;

	/** The m current state. */
	private STATE mCurrentState;

	/** The m out animation. */
	private Animation mOutAnimation;

	/** The m in animation. */
	private Animation mInAnimation;

	/** The m listener. */
	private OnToolbarClickListener mListener;

	/** The m clickable. */
	private boolean mClickable;

	/** The Constant MSG_SHOW_CHILD. */
	private static final int MSG_SHOW_CHILD = 1;

	/** The m handler. */
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage( android.os.Message msg ) {
			switch ( msg.what ) {
				case MSG_SHOW_CHILD:
					setDisplayedChild( msg.arg1 );
					break;
			}
		};
	};

	/**
	 * Instantiates a new toolbar view.
	 * 
	 * @param context
	 *           the context
	 */
	public ToolbarView( Context context ) {
		super( context );
		init( context, null );
	}

	/**
	 * Instantiates a new toolbar view.
	 * 
	 * @param context
	 *           the context
	 * @param attrs
	 *           the attrs
	 */
	public ToolbarView( Context context, AttributeSet attrs ) {
		super( context, attrs );
		init( context, attrs );
	}

	/**
	 * Inits the.
	 * 
	 * @param context
	 *           the context
	 * @param attrs
	 *           the attrs
	 */
	private void init( Context context, AttributeSet attrs ) {
		mCurrentState = STATE.STATE_SAVE;
		setAnimationCacheEnabled( true );
		setAlwaysDrawnWithCacheEnabled( true );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View#setClickable(boolean)
	 */
	@Override
	public void setClickable( boolean clickable ) {
		mClickable = clickable;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View#isClickable()
	 */
	@Override
	public boolean isClickable() {
		return mClickable;
	}

	/**
	 * Gets the in animation time.
	 * 
	 * @return the in animation time
	 */
	public long getInAnimationTime() {
		return mInAnimation.getDuration() + mInAnimation.getStartOffset();
	}

	/**
	 * Gets the out animation time.
	 * 
	 * @return the out animation time
	 */
	public long getOutAnimationTime() {
		return mOutAnimation.getDuration() + mOutAnimation.getStartOffset();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View#onFinishInflate()
	 */
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		mApplyButton = (Button) findViewById( R.id.toolbar_content_panel ).findViewById( R.id.button_apply );
		mSaveButton = (Button) findViewById( R.id.toolbar_main_panel ).findViewById( R.id.button_save );
		mTitleText = (TextSwitcher) findViewById( R.id.toolbar_title );
		mTitleText.setFactory( this );
		mAviaryLogo = (TextView) findViewById( R.id.aviary_logo );
		

		mInAnimation = AnimationUtils.loadAnimation( getContext(), R.anim.feather_push_up_in );
		mInAnimation.setStartOffset( 100 );
		mOutAnimation = AnimationUtils.loadAnimation( getContext(), R.anim.feather_push_up_out );
		mOutAnimation.setStartOffset( 100 );

		mOutAnimation.setAnimationListener( mInAnimationListener );
		mInAnimation.setAnimationListener( mInAnimationListener );

		setInAnimation( mInAnimation );
		setOutAnimation( mOutAnimation );

		mApplyButton.setOnClickListener( new OnClickListener() {

			@Override
			public void onClick( View v ) {
				if ( mListener != null && mCurrentState == STATE.STATE_APPLY && isClickable() ) mListener.onApplyClick();
			}
		} );

		mSaveButton.setOnClickListener( new OnClickListener() {

			@Override
			public void onClick( View v ) {
				if ( mListener != null && mCurrentState == STATE.STATE_SAVE && isClickable() ) mListener.onSaveClick();
			}
		} );
	}

	/**
	 * Change the current toolbar state creating an animation between the current and the new view state.
	 * 
	 * @param state
	 *           the state
	 * @param showMiddle
	 *           the show middle
	 */
	public void setState( STATE state, final boolean showMiddle ) {
		if ( state != mCurrentState ) {
			mCurrentState = state;

			post( new Runnable() {

				@Override
				public void run() {
					switch ( mCurrentState ) {
						case STATE_APPLY:
							showApplyState();
							break;

						case STATE_SAVE:
							showSaveState( showMiddle );
							break;
					}
				}
			} );
		}
	}

	/**
	 * Return the current toolbar state.
	 * 
	 * @return the state
	 * @see #STATE
	 */
	public STATE getState() {
		return mCurrentState;
	}

	/**
	 * Set the toolbar click listener.
	 * 
	 * @param listener
	 *           the new on toolbar click listener
	 * @see OnToolbarClickListener
	 */
	public void setOnToolbarClickListener( OnToolbarClickListener listener ) {
		mListener = listener;
	}

	/**
	 * Sets the apply enabled.
	 * 
	 * @param value
	 *           the new apply enabled
	 */
	public void setApplyEnabled( boolean value ) {
		mApplyButton.setEnabled( value );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View#setSaveEnabled(boolean)
	 */
	@Override
	public void setSaveEnabled( boolean value ) {
		mSaveButton.setEnabled( value );
	}

	/**
	 * Sets the title.
	 * 
	 * @param value
	 *           the new title
	 */
	public void setTitle( CharSequence value ) {
		mTitleText.setText( value );
	}
	
	public void setTitle( CharSequence value, boolean animate ) {
		if( !animate ){
			Animation inAnimation = mTitleText.getInAnimation();
			Animation outAnimation = mTitleText.getOutAnimation();
			mTitleText.setInAnimation( null );
			mTitleText.setOutAnimation( null );
			mTitleText.setText( value );
			mTitleText.setInAnimation( inAnimation );
			mTitleText.setOutAnimation( outAnimation );
		} else {
			setTitle( value );
		}
	}

	/**
	 * Sets the title.
	 * 
	 * @param resourceId
	 *           the new title
	 */
	public void setTitle( int resourceId ) {
		setTitle( getContext().getString( resourceId ) );
	}
	
	public void setTitle( int resourceId, boolean animate ) {
		setTitle( getContext().getString( resourceId ), animate );
	}	

	/**
	 * Show apply state.
	 */
	private void showApplyState() {
		setDisplayedChild( getChildCount() - 1 );
	}

	/**
	 * Show save state.
	 * 
	 * @param showMiddle
	 *           the show middle
	 */
	private void showSaveState( boolean showMiddle ) {
		if ( showMiddle && getChildCount() == 3 )
			setDisplayedChild( 1 );
		else
			setDisplayedChild( 0 );
	}

	/**
	 * Enable children cache.
	 */
	@SuppressWarnings("unused")
	private void enableChildrenCache() {

		setChildrenDrawnWithCacheEnabled( true );
		setChildrenDrawingCacheEnabled( true );

		for ( int i = 0; i < getChildCount(); i++ ) {
			final View child = getChildAt( i );
			child.setDrawingCacheEnabled( true );
			child.buildDrawingCache( true );
		}
	}

	/**
	 * Clear children cache.
	 */
	@SuppressWarnings("unused")
	private void clearChildrenCache() {
		setChildrenDrawnWithCacheEnabled( false );
	}

	/**
	 * Sets the apply enabled.
	 * 
	 * @param applyEnabled
	 *           the apply enabled
	 * @param cancelEnabled
	 *           the cancel enabled
	 */
	public void setApplyEnabled( boolean applyEnabled, boolean cancelEnabled ) {
		mApplyButton.setEnabled( applyEnabled );
	}

	/** The m in animation listener. */
	AnimationListener mInAnimationListener = new AnimationListener() {

		@Override
		public void onAnimationStart( Animation animation ) {
			isAnimating = true;
		}

		@Override
		public void onAnimationRepeat( Animation animation ) {}

		@Override
		public void onAnimationEnd( Animation animation ) {
			isAnimating = false;
			if ( getDisplayedChild() == 1 && getChildCount() > 2 ) {
				Thread t = new Thread( new Runnable() {

					@Override
					public void run() {
						try {
							Thread.sleep( 300 );
						} catch ( InterruptedException e ) {
							e.printStackTrace();
						}

						Message msg = mHandler.obtainMessage( MSG_SHOW_CHILD );
						msg.arg1 = 0;
						mHandler.sendMessage( msg );
					}
				} );
				t.start();
			}
		}
	};

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.ViewSwitcher.ViewFactory#makeView()
	 */
	@Override
	public View makeView() {
		View text = LayoutInflater.from( getContext() ).inflate( R.layout.feather_toolbar_title_text, null );
		return text;
	}
}
