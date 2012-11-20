package com.aviary.android.feather.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ViewFlipper;
import com.aviary.android.feather.R;
import com.aviary.android.feather.library.graphics.animation.VoidAnimation;
import com.aviary.android.feather.library.log.LoggerFactory;
import com.aviary.android.feather.library.log.LoggerFactory.Logger;
import com.aviary.android.feather.library.log.LoggerFactory.LoggerType;

// TODO: Auto-generated Javadoc
/**
 * The Class BottombarViewFlipper.
 */
public class BottombarViewFlipper extends ViewFlipper {

	/** The logger. */
	Logger logger = LoggerFactory.getLogger( "bottombar", LoggerType.ConsoleLoggerType );

	/**
	 * The listener interface for receiving onPanelOpen events. The class that is interested in processing a onPanelOpen event
	 * implements this interface, and the object created with that class is registered with a component using the component's
	 * <code>addOnPanelOpenListener<code> method. When
	 * the onPanelOpen event occurs, that object's appropriate
	 * method is invoked.
	 * 
	 * @see OnPanelOpenEvent
	 */
	public static interface OnPanelOpenListener {

		/**
		 * On opening.
		 */
		void onOpening();

		/**
		 * On opened.
		 */
		void onOpened();
	};

	/**
	 * The listener interface for receiving onPanelClose events. The class that is interested in processing a onPanelClose event
	 * implements this interface, and the object created with that class is registered with a component using the component's
	 * <code>addOnPanelCloseListener<code> method. When
	 * the onPanelClose event occurs, that object's appropriate
	 * method is invoked.
	 * 
	 * @see OnPanelCloseEvent
	 */
	public static interface OnPanelCloseListener {

		/**
		 * On closing.
		 */
		void onClosing();

		/**
		 * On closed.
		 */
		void onClosed();
	};

	/** The m open animation listener. */
	private AnimationListener mOpenAnimationListener = new AnimationListener() {

		@Override
		public void onAnimationStart( Animation animation ) {
			if ( mOpenListener != null ) mOpenListener.onOpening();
		}

		@Override
		public void onAnimationRepeat( Animation animation ) {}

		@Override
		public void onAnimationEnd( Animation animation ) {
			if ( mOpenListener != null ) mOpenListener.onOpened();
			animation.setAnimationListener( null );
		}
	};

	/** The m close animation listener. */
	private AnimationListener mCloseAnimationListener = new AnimationListener() {

		@Override
		public void onAnimationStart( Animation animation ) {
			if ( mCloseListener != null ) mCloseListener.onClosing();
		}

		@Override
		public void onAnimationRepeat( Animation animation ) {}

		@Override
		public void onAnimationEnd( Animation animation ) {
			if ( mCloseListener != null ) mCloseListener.onClosed();
			animation.setAnimationListener( null );
		}
	};

	/** The m open listener. */
	private OnPanelOpenListener mOpenListener;

	/** The m close listener. */
	private OnPanelCloseListener mCloseListener;

	/** The m animation duration. */
	private int mAnimationDuration = 500;

	/** The m animation in. */
	private Animation mAnimationIn;

	/** The m animation out. */
	private Animation mAnimationOut;

	private int mAnimationOpenStartOffset = 100;
	private int mAnimationCloseStartOffset = 100;

	/**
	 * Instantiates a new bottombar view flipper.
	 * 
	 * @param context
	 *           the context
	 */
	public BottombarViewFlipper( Context context ) {
		this( context, null );
		init( context );
	}

	/**
	 * Instantiates a new bottombar view flipper.
	 * 
	 * @param context
	 *           the context
	 * @param attrs
	 *           the attrs
	 */
	public BottombarViewFlipper( Context context, AttributeSet attrs ) {
		super( context, attrs );
		init( context );
	}

	/**
	 * Inits the.
	 * 
	 * @param context
	 *           the context
	 */
	private void init( Context context ) {
		setAnimationCacheEnabled( true );
		// setDrawingCacheEnabled( true );
		// setAlwaysDrawnWithCacheEnabled( false );
		// setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_LOW);

		mAnimationDuration = context.getResources().getInteger( R.integer.feather_config_bottom_animTime );
		mAnimationCloseStartOffset = context.getResources().getInteger( R.integer.feather_bottombar_close_offset );
		mAnimationOpenStartOffset = context.getResources().getInteger( R.integer.feather_bottombar_open_offset );
	}

	/**
	 * Return the view which will contain all the options panel.
	 * 
	 * @return the content
	 */
	public ViewGroup getContent() {
		return (ViewGroup) getChildAt( mContentPanelIndex );
	}

	/**
	 * Gets the tool panel.
	 * 
	 * @return the tool panel
	 */
	public ViewGroup getToolPanel() {
		return (ViewGroup) getChildAt( mToolPanelIndex );
	}

	/** The m tool panel index. */
	final int mToolPanelIndex = 1;

	/** The m content panel index. */
	final int mContentPanelIndex = 0;

	/**
	 * Close the option panel and return to the tools list view.
	 */
	public void close() {

		int height = getContent().getHeight();
		Animation animationOut = createVoidAnimation( mAnimationDuration, mAnimationCloseStartOffset );
		animationOut.setAnimationListener( mCloseAnimationListener );

		height = getToolPanel().getHeight();
		Animation animationIn = createInAnimation( TranslateAnimation.ABSOLUTE, height, mAnimationDuration,
				mAnimationCloseStartOffset );
		setInAnimation( animationIn );
		setOutAnimation( animationOut );
		setDisplayedChild( mToolPanelIndex );
	}

	/**
	 * Display the option panel while hiding the bottom tools.
	 */
	public void open() {

		// first we must check the height of the content
		// panel to use within the in animation
		int height;
		// getContent().setVisibility( View.INVISIBLE );

		height = getContent().getMeasuredHeight();

		if ( height == 0 ) {
			getHandler().post( new Runnable() {

				@Override
				public void run() {
					try {
						Thread.sleep( 10 );
					} catch ( InterruptedException e ) {
						e.printStackTrace();
					}
					open();
				}
			} );
			return;
		}

		Animation animationIn = createVoidAnimation( mAnimationDuration, mAnimationOpenStartOffset );

		height = getToolPanel().getHeight();
		Animation animationOut = createOutAnimation( TranslateAnimation.ABSOLUTE, height, mAnimationDuration,
				mAnimationOpenStartOffset );
		animationIn.setAnimationListener( mOpenAnimationListener );

		setInAnimation( animationIn );
		setOutAnimation( animationOut );

		setDisplayedChild( mContentPanelIndex );
	}

	/**
	 * Creates the void animation.
	 * 
	 * @param durationMillis
	 *           the duration millis
	 * @param startOffset
	 *           the start offset
	 * @return the animation
	 */
	private Animation createVoidAnimation( int durationMillis, int startOffset ) {
		Animation animation = new VoidAnimation();
		animation.setDuration( durationMillis );
		animation.setStartOffset( startOffset );
		return animation;
	}

	/**
	 * Creates the out animation.
	 * 
	 * @param deltaType
	 *           the delta type
	 * @param height
	 *           the height
	 * @param durationMillis
	 *           the duration millis
	 * @param startOffset
	 *           the start offset
	 * @return the animation
	 */
	private Animation createOutAnimation( int deltaType, int height, int durationMillis, int startOffset ) {

		if ( mAnimationOut == null ) {
			mAnimationOut = new TranslateAnimation( deltaType, 0, deltaType, 0, deltaType, 0, deltaType, height );
			mAnimationOut.setInterpolator( new DecelerateInterpolator( 0.4f ) );
			mAnimationOut.setDuration( durationMillis );
			mAnimationOut.setStartOffset( startOffset );
		}
		return mAnimationOut;
	}

	/**
	 * Creates the in animation.
	 * 
	 * @param deltaType
	 *           the delta type
	 * @param height
	 *           the height
	 * @param durationMillis
	 *           the duration millis
	 * @param startOffset
	 *           the start offset
	 * @return the animation
	 */
	private Animation createInAnimation( int deltaType, int height, int durationMillis, int startOffset ) {

		if ( mAnimationIn == null ) {
			mAnimationIn = new TranslateAnimation( deltaType, 0, deltaType, 0, deltaType, height, deltaType, 0 );
			mAnimationIn.setDuration( durationMillis );
			mAnimationIn.setStartOffset( startOffset );
			mAnimationIn.setInterpolator( new AccelerateInterpolator( 0.5f ) );
		}
		return mAnimationIn;

		// return animation;
	}

	/**
	 * Sets the on panel open listener.
	 * 
	 * @param listener
	 *           the new on panel open listener
	 */
	public void setOnPanelOpenListener( OnPanelOpenListener listener ) {
		mOpenListener = listener;
	}

	/**
	 * Sets the on panel close listener.
	 * 
	 * @param listener
	 *           the new on panel close listener
	 */
	public void setOnPanelCloseListener( OnPanelCloseListener listener ) {
		mCloseListener = listener;
	}
}
