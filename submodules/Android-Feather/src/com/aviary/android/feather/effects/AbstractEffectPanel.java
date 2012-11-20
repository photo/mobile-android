package com.aviary.android.feather.effects;

import java.util.HashMap;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.aviary.android.feather.library.filters.IFilter;
import com.aviary.android.feather.library.log.LoggerFactory;
import com.aviary.android.feather.library.log.LoggerFactory.Logger;
import com.aviary.android.feather.library.log.LoggerFactory.LoggerType;
import com.aviary.android.feather.library.moa.MoaActionList;
import com.aviary.android.feather.library.services.EffectContext;

// TODO: Auto-generated Javadoc
/**
 * Base class for all the feather tools.
 * 
 * @author alessandro
 */
public abstract class AbstractEffectPanel {

	/** The main listener handler. */
	Handler mListenerHandler;

	static final int PREVIEW_BITMAP_CHANGED = 1;
	static final int PREVIEW_FILTER_CHANGED = 2;
	static final int FILTER_SAVE_COMPLETED = 3;
	static final int PROGRESS_START = 4;
	static final int PROGRESS_END = 5;
	static final int PROGRESS_MODAL_START = 6;
	static final int PROGRESS_MODAL_END = 7;

	/**
	 * If the current panel implements {@link #AbstractEffectPanel.ContentPanel} this listener is used by the FilterManager to hide the main
	 * application image when the content panel send the onReady event.
	 * 
	 * @author alessandro
	 * 
	 */
	public static interface OnContentReadyListener {

		/**
		 * On ready. Panel is ready to display its contents
		 * 
		 * @param panel
		 *           the panel
		 */
		void onReady( AbstractEffectPanel panel );
	};

	/**
	 * The listener interface for receiving onProgress events. The class that is interested in processing a onProgress event
	 * implements this interface, and the object created with that class is registered with a component using the component's
	 * <code>addOnProgressListener<code> method. When
	 * the onProgress event occurs, that object's appropriate
	 * method is invoked.
	 * 
	 * @see OnProgressEvent
	 */
	public static interface OnProgressListener {

		/**
		 * On progress start.
		 */
		void onProgressStart();

		/**
		 * On progress end.
		 */
		void onProgressEnd();

		/** a progress modal has been requested */
		void onProgressModalStart();

		/** hide the progress modal */
		void onProgressModalEnd();
	}

	/**
	 * The listener interface for receiving onPreview events. The class that is interested in processing a onPreview event implements
	 * this interface, and the object created with that class is registered with a component using the component's
	 * <code>addOnPreviewListener<code> method. When
	 * the onPreview event occurs, that object's appropriate
	 * method is invoked.
	 * 
	 * @see OnPreviewEvent
	 */
	public static interface OnPreviewListener {

		/**
		 * Some parameters have changed and the effect has generated a new bitmap with the new parameters applied on it.
		 * 
		 * @param result
		 *           the result
		 */
		void onPreviewChange( Bitmap result );

		/**
		 * On preview change.
		 * 
		 * @param colorFilter
		 *           the color filter
		 */
		void onPreviewChange( ColorFilter colorFilter );
	};

	/**
	 * The listener interface for receiving onApplyResult events. The class that is interested in processing a onApplyResult event
	 * implements this interface, and the object created with that class is registered with a component using the component's
	 * <code>addOnApplyResultListener<code> method. When
	 * the onApplyResult event occurs, that object's appropriate
	 * method is invoked.
	 * 
	 * @see OnApplyResultEvent
	 */
	public static interface OnApplyResultListener {

		/**
		 * On complete.
		 * 
		 * @param result
		 *           the result
		 * @param actions
		 *           the actions executed
		 * @param trackingAttributes
		 *           the tracking attributes
		 */
		void onComplete( Bitmap result, MoaActionList actions, HashMap<String, String> trackingAttributes );
	}

	/**
	 * The listener interface for receiving onError events. The class that is interested in processing a onError event implements
	 * this interface, and the object created with that class is registered with a component using the component's
	 * <code>addOnErrorListener<code> method. When
	 * the onError event occurs, that object's appropriate
	 * method is invoked.
	 * 
	 * @see OnErrorEvent
	 */
	public static interface OnErrorListener {

		/**
		 * On error.
		 * 
		 * @param error
		 *           the error
		 */
		void onError( String error );

		void onError( String error, int yesLabel, OnClickListener yesListener, int noLabel, OnClickListener noListener );
	}

	/**
	 * Base interface for all the tools.
	 * 
	 * @author alessandro
	 */
	public static interface OptionPanel {

		/**
		 * Returns a view used to populate the option panel.
		 * 
		 * @param inflater
		 *           the inflater
		 * @param viewGroup
		 *           the view group
		 * @return the option view
		 */
		View getOptionView( LayoutInflater inflater, ViewGroup viewGroup );
	}

	/**
	 * Base interface for the tools which will provide a content panel.
	 * 
	 * @author alessandro
	 * 
	 */
	public static interface ContentPanel {

		/**
		 * Sets the on ready listener.
		 * 
		 * @param listener
		 *           the new on ready listener
		 */
		void setOnReadyListener( OnContentReadyListener listener );

		/**
		 * Creates and return a new view which will be placed over the original image and its used by the contentpanel to draw its own
		 * preview.
		 * 
		 * @param inflater
		 *           the inflater
		 * @return the content view
		 */
		View getContentView( LayoutInflater inflater );

		/**
		 * Return the current content view.
		 * 
		 * @return the content view
		 */
		View getContentView();

		/**
		 * Returns the current Image display matrix used in the content panel. This is useful when the application leaves the current
		 * tool and the original image needs to be updated using the content panel image. We need to know the content's panel image
		 * matrix in order to present the same image size/position to the user.
		 * 
		 * @return the content display matrix
		 */
		Matrix getContentDisplayMatrix();
	}

	/** If a tool need to store a copy of the input bitmap, use this member which will be automatically recycled. */
	protected Bitmap mPreview;

	/**
	 * This is the input Bitmap passed from the FilterManager class.
	 */
	protected Bitmap mBitmap;

	private boolean mActive;
	private boolean mCreated;
	protected boolean mChanged;
	protected boolean mSaving;
	protected long mRenderTime;
	protected boolean mEnabled;
	protected IFilter mFilter;
	protected HashMap<String, String> mTrackingAttributes;
	protected OnProgressListener mProgressListener;
	protected OnPreviewListener mListener;
	protected OnApplyResultListener mApplyListener;
	protected OnErrorListener mErrorListener;
	private EffectContext mFilterContext;
	protected Logger mLogger;

	/**
	 * Instantiates a new abstract effect panel.
	 * 
	 * @param context
	 *           the context
	 */
	public AbstractEffectPanel( EffectContext context ) {
		mFilterContext = context;
		mActive = false;
		mEnabled = true;
		mTrackingAttributes = new HashMap<String, String>();
		setIsChanged( false );
		mLogger = LoggerFactory.getLogger( this.getClass().getSimpleName(), LoggerType.ConsoleLoggerType );
	}

	public Handler getHandler() {
		return mListenerHandler;
	}

	/**
	 * On progress start.
	 */
	protected void onProgressStart() {
		if ( isActive() ) {
			mListenerHandler.sendEmptyMessage( PROGRESS_START );
		}
	}

	/**
	 * On progress end.
	 */
	protected void onProgressEnd() {
		if ( isActive() ) {
			mListenerHandler.sendEmptyMessage( PROGRESS_END );
		}
	}

	protected void onProgressModalStart() {
		if ( isActive() ) {
			mListenerHandler.sendEmptyMessage( PROGRESS_MODAL_START );
		}
	}

	protected void onProgressModalEnd() {
		if ( isActive() ) {
			mListenerHandler.sendEmptyMessage( PROGRESS_MODAL_END );
		}
	}

	/**
	 * Sets the panel enabled state.
	 * 
	 * @param value
	 *           the new enabled
	 */
	public void setEnabled( boolean value ) {
		mEnabled = value;
	}

	/**
	 * Checks if is enabled.
	 * 
	 * @return true, if is enabled
	 */
	public boolean isEnabled() {
		return mEnabled;
	}

	/**
	 * Return true if current panel state is between the onActivate/onDeactivate states.
	 * 
	 * @return true, if is active
	 */
	public boolean isActive() {
		return mActive && isCreated();
	}

	/**
	 * Return true if current panel state is between onCreate/onDestroy states.
	 * 
	 * @return true, if is created
	 */
	public boolean isCreated() {
		return mCreated;
	}

	/**
	 * Sets the on preview listener.
	 * 
	 * @param listener
	 *           the new on preview listener
	 */
	public void setOnPreviewListener( OnPreviewListener listener ) {
		mListener = listener;
	}

	/**
	 * Sets the on apply result listener.
	 * 
	 * @param listener
	 *           the new on apply result listener
	 */
	public void setOnApplyResultListener( OnApplyResultListener listener ) {
		mApplyListener = listener;
	}

	/**
	 * Sets the on error listener.
	 * 
	 * @param listener
	 *           the new on error listener
	 */
	public void setOnErrorListener( OnErrorListener listener ) {
		mErrorListener = listener;
	}

	/**
	 * Sets the on progress listener.
	 * 
	 * @param listener
	 *           the new on progress listener
	 */
	public void setOnProgressListener( OnProgressListener listener ) {
		mProgressListener = listener;
	}

	/**
	 * Called first when the panel has been created and it is ready to be shown.
	 * 
	 * @param bitmap
	 *           the bitmap
	 */
	public void onCreate( Bitmap bitmap ) {
		mLogger.info( "onCreate" );
		mBitmap = bitmap;
		mCreated = true;
	}

	/**
	 * panel is being shown.
	 */
	public void onOpening() {
		mLogger.info( "onOpening" );
	}

	/**
	 * panel is being closed.
	 */
	public void onClosing() {
		mLogger.info( "onClosing" );
	}

	/**
	 * Return true if you want the back event handled by the current panel otherwise return false and the back button will be handled
	 * by the system.
	 * 
	 * @return true, if successful
	 */
	public boolean onBackPressed() {
		return false;
	}

	/**
	 * Device configuration changed.
	 * 
	 * @param newConfig
	 *           the new config
	 */
	public void onConfigurationChanged( Configuration newConfig, Configuration oldConfig ) {

	}

	/**
	 * The main context requests to apply the current status of the filter.
	 */
	public void onSave() {
		mLogger.info( "onSave" );

		if ( mSaving == false ) {
			mSaving = true;
			mRenderTime = System.currentTimeMillis();
			onGenerateResult();
		}
	}

	/**
	 * Manager is asking to cancel the current tool. Return false if no further user interaction is necessary and you agree to close
	 * this panel. Return true otherwise and the next call to this panel will be onCancelled. If you want to manage this event you
	 * can then cancel the panel by calling {@link EffectContext#cancel()} on the current context
	 * 
	 * onCancel -> onCancelled -> onDeactivate -> onDestroy
	 * 
	 * @return true, if successful
	 */
	public boolean onCancel() {
		mLogger.info( "onCancel" );
		return false;
	}

	/*
	 * Panel is being closed without applying the result. Either because the user clicked on the cancel button or because a back
	 * event has been fired.
	 */
	/**
	 * On cancelled.
	 */
	public void onCancelled() {
		mLogger.info( "onCancelled" );
		setEnabled( false );
	}

	/**
	 * Check if the current panel has pending changes.
	 * 
	 * @return the checks if is changed
	 */
	public boolean getIsChanged() {
		return mChanged;
	}

	/**
	 * Sets the 'changed' status of the current panel.
	 * 
	 * @param value
	 *           the new checks if is changed
	 */
	protected void setIsChanged( boolean value ) {
		mChanged = value;
	}

	/**
	 * Panel is now hidden and it need to be disposed.
	 */
	public void onDestroy() {
		mLogger.info( "onDestroy" );
		mCreated = false;
		onDispose();
	}

	/**
	 * Called after onCreate as soon as the panel it's ready to receive user interactions
	 * 
	 * panel lifecycle: 1. onCreate 2. onActivate 3. ( user interactions.. ) 3.1 onCancel/onBackPressed 4. onSave|onCancelled 5.
	 * onDeactivate 6. onDestroy
	 */
	public void onActivate() {
		mLogger.info( "onActivate" );
		mActive = true;

		mListenerHandler = new Handler() {

			@Override
			public void handleMessage( Message msg ) {
				super.handleMessage( msg );

				switch ( msg.what ) {
					case PREVIEW_FILTER_CHANGED:
						if ( mListener != null && isActive() ) {
							mListener.onPreviewChange( (ColorFilter) msg.obj );
						}
						break;

					case PREVIEW_BITMAP_CHANGED:
						if ( mListener != null && isActive() ) {
							mListener.onPreviewChange( (Bitmap) msg.obj );
						}
						break;

					case PROGRESS_START:
						if ( mProgressListener != null && isCreated() ) {
							mProgressListener.onProgressStart();
						}
						break;

					case PROGRESS_END:
						if ( mProgressListener != null && isCreated() ) {
							mProgressListener.onProgressEnd();
						}
						break;

					case PROGRESS_MODAL_START:
						if ( mProgressListener != null && isCreated() ) {
							mProgressListener.onProgressModalStart();
						}
						break;

					case PROGRESS_MODAL_END:
						if ( mProgressListener != null && isCreated() ) {
							mProgressListener.onProgressModalEnd();
						}
						break;

					default:
						break;
				}

			}
		};
	}

	/**
	 * Called just before start hiding the panel No user interactions should be accepted anymore after this point.
	 */
	public void onDeactivate() {
		mLogger.info( "onDeactivate" );
		setEnabled( false );
		mActive = false;
		mListenerHandler = null;
	}

	/**
	 * Return the current Effect Context.
	 * 
	 * @return the context
	 */
	public EffectContext getContext() {
		return mFilterContext;
	}

	/**
	 * On dispose.
	 */
	protected void onDispose() {
		mLogger.info( "onDispose" );
		internalDispose();
	}

	/**
	 * Internal dispose.
	 */
	private void internalDispose() {
		recyclePreview();
		mPreview = null;
		mBitmap = null;
		mListener = null;
		mErrorListener = null;
		mApplyListener = null;
		mFilterContext = null;
		mFilter = null;
	}

	/**
	 * Recycle and free the preview bitmap.
	 */
	protected void recyclePreview() {
		if ( mPreview != null && !mPreview.isRecycled() && !mPreview.equals( mBitmap ) ) {
			mLogger.warning( "[recycle] preview Bitmap: " + mPreview );
			mPreview.recycle();
		}
	}

	/**
	 * On preview changed.
	 * 
	 * @param bitmap
	 *           the bitmap
	 */
	protected void onPreviewChanged( Bitmap bitmap ) {
		onPreviewChanged( bitmap, true );
	}

	/**
	 * On preview changed.
	 * 
	 * @param colorFilter
	 *           the color filter
	 * @param notify
	 *           the notify
	 */
	protected void onPreviewChanged( ColorFilter colorFilter, boolean notify ) {
		setIsChanged( colorFilter != null );

		if ( notify && isActive() ) {
			mListenerHandler.removeMessages( PREVIEW_FILTER_CHANGED );
			Message msg = mListenerHandler.obtainMessage( PREVIEW_FILTER_CHANGED );
			msg.obj = colorFilter;
			mListenerHandler.sendMessage( msg );
		}

		// if ( mListener != null && notify && isActive() ) mListener.onPreviewChange( colorFilter );
	}

	/**
	 * On preview changed.
	 * 
	 * @param bitmap
	 *           the bitmap
	 * @param notify
	 *           the notify
	 */
	protected void onPreviewChanged( Bitmap bitmap, boolean notify ) {
		setIsChanged( bitmap != null );

		if ( bitmap == null || !bitmap.equals( mPreview ) ) {
			recyclePreview();
		}

		mPreview = bitmap;

		if ( notify && isActive() ) {
			Message msg = mListenerHandler.obtainMessage( PREVIEW_BITMAP_CHANGED );
			msg.obj = bitmap;
			mListenerHandler.sendMessage( msg );
		}

		// if ( mListener != null && notify && isActive() ) mListener.onPreviewChange( bitmap );
	}

	/**
	 * Called when the current effect panel has completed the generation of the final bitmap.
	 * 
	 * @param bitmap
	 *           the bitmap
	 * @param actions
	 *           list of the current applied actions
	 */
	protected void onComplete( Bitmap bitmap, MoaActionList actions ) {
		mLogger.info( "onComplete" );
		long t = System.currentTimeMillis();
		if ( mApplyListener != null && isActive() ) {

			if ( !mTrackingAttributes.containsKey( "renderTime" ) )
				mTrackingAttributes.put( "renderTime", Long.toString( t - mRenderTime ) );
			mApplyListener.onComplete( bitmap, actions, mTrackingAttributes );

		}
		mPreview = null;
		mSaving = false;
	}

	/**
	 * On generic error.
	 * 
	 * @param error
	 *           the error
	 */
	protected void onGenericError( String error ) {
		if ( mErrorListener != null && isActive() ) mErrorListener.onError( error );
	}

	protected void onGenericError( int resId ) {
		if ( mErrorListener != null && isActive() ) {
			String label = getContext().getBaseContext().getString( resId );
			mErrorListener.onError( label );
		}
	}

	protected void onGenericError( int resId, int yesLabel, OnClickListener yesListener, int noLabel, OnClickListener noListener ) {
		if ( mErrorListener != null && isActive() ) {
			String message = getContext().getBaseContext().getString( resId );
			onGenericError( message, yesLabel, yesListener, noLabel, noListener );
		}
	}

	protected void onGenericError( String message, int yesLabel, OnClickListener yesListener, int noLabel, OnClickListener noListener ) {
		if ( mErrorListener != null && isActive() ) {
			mErrorListener.onError( message, yesLabel, yesListener, noLabel, noListener );
		}
	}

	/**
	 * On generic error.
	 * 
	 * @param e
	 *           the e
	 */
	protected void onGenericError( Exception e ) {
		onGenericError( e.getMessage() );
	}

	/**
	 * This methods is called by the {@link #onSave()} method. Here the implementation of the current option panel should generate
	 * the result bitmap, even asyncronously, and when completed it must call the {@link #onComplete(Bitmap)} event.
	 */
	protected void onGenerateResult() {
		onGenerateResult( null );
	}

	protected void onGenerateResult( MoaActionList actions ) {
		onComplete( mPreview, actions );
	}
}
