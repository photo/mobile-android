package com.aviary.android.feather;

import it.sephiroth.android.library.imagezoom.ImageViewTouchBase;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.concurrent.Future;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Toast;
import com.aviary.android.feather.effects.AbstractEffectPanel;
import com.aviary.android.feather.effects.AbstractEffectPanel.ContentPanel;
import com.aviary.android.feather.effects.AbstractEffectPanel.OnApplyResultListener;
import com.aviary.android.feather.effects.AbstractEffectPanel.OnContentReadyListener;
import com.aviary.android.feather.effects.AbstractEffectPanel.OnErrorListener;
import com.aviary.android.feather.effects.AbstractEffectPanel.OnPreviewListener;
import com.aviary.android.feather.effects.AbstractEffectPanel.OnProgressListener;
import com.aviary.android.feather.effects.AbstractEffectPanel.OptionPanel;
import com.aviary.android.feather.effects.EffectLoaderService;
import com.aviary.android.feather.library.content.EffectEntry;
import com.aviary.android.feather.library.content.FeatherIntent;
import com.aviary.android.feather.library.log.LoggerFactory;
import com.aviary.android.feather.library.log.LoggerFactory.Logger;
import com.aviary.android.feather.library.log.LoggerFactory.LoggerType;
import com.aviary.android.feather.library.moa.MoaActionList;
import com.aviary.android.feather.library.plugins.ExternalPacksTask;
import com.aviary.android.feather.library.plugins.PluginFetchTask;
import com.aviary.android.feather.library.plugins.PluginUpdaterTask;
import com.aviary.android.feather.library.plugins.UpdateType;
import com.aviary.android.feather.library.services.ConfigService;
import com.aviary.android.feather.library.services.DragControllerService;
import com.aviary.android.feather.library.services.EffectContext;
import com.aviary.android.feather.library.services.EffectContextService;
import com.aviary.android.feather.library.services.FutureListener;
import com.aviary.android.feather.library.services.HiResService;
import com.aviary.android.feather.library.services.LocalDataService;
import com.aviary.android.feather.library.services.PluginService;
import com.aviary.android.feather.library.services.PreferenceService;
import com.aviary.android.feather.library.services.ServiceLoader;
import com.aviary.android.feather.library.services.ThreadPoolService;
import com.aviary.android.feather.library.services.ThreadPoolService.BGCallable;
import com.aviary.android.feather.library.services.drag.DragLayer;
import com.aviary.android.feather.library.tracking.Tracker;
import com.aviary.android.feather.library.utils.BitmapUtils;
import com.aviary.android.feather.receivers.FeatherSystemReceiver;
import com.aviary.android.feather.utils.UIUtils;
import com.aviary.android.feather.widget.BottombarViewFlipper;
import com.aviary.android.feather.widget.BottombarViewFlipper.OnPanelCloseListener;
import com.aviary.android.feather.widget.BottombarViewFlipper.OnPanelOpenListener;
import com.aviary.android.feather.widget.ToolbarView;

// TODO: Auto-generated Javadoc
/**
 * FilterManager is the core of feather. It manages all the tool panels, notifies about new plugins installed, etc
 * 
 * @author alessandro
 * 
 */
public final class FilterManager implements OnPreviewListener, OnApplyResultListener, EffectContext, OnErrorListener,
		OnContentReadyListener, OnProgressListener {

	/**
	 * The Interface FeatherContext.<br />
	 * The activity caller must implement this interface
	 */
	public interface FeatherContext {

		/**
		 * Gets the Activity main image view.
		 * 
		 * @return the main image
		 */
		ImageViewTouchBase getMainImage();

		/**
		 * Gets the Activity bottom bar view.
		 * 
		 * @return the bottom bar
		 */
		BottombarViewFlipper getBottomBar();

		/**
		 * Gets the Activity options panel container view.
		 * 
		 * @return the options panel container
		 */
		ViewGroup getOptionsPanelContainer();

		/**
		 * Gets the Activity drawing image container view.
		 * 
		 * @return the drawing image container
		 */
		ViewGroup getDrawingImageContainer();

		/**
		 * Show tool progress.
		 */
		void showToolProgress();

		/**
		 * Hide tool progress.
		 */
		void hideToolProgress();

		/**
		 * Show a modal progress
		 */
		void showModalProgress();

		/**
		 * Hide the modal progress
		 */
		void hideModalProgress();

		/**
		 * Gets the toolbar.
		 * 
		 * @return the toolbar
		 */
		ToolbarView getToolbar();
	}

	public interface OnHiResListener {

		void OnLoad( Uri uri );

		void OnApplyActions( MoaActionList actionlist );
	}

	public interface OnBitmapChangeListener {

		public void onPreviewChange( ColorFilter filter );

		public void onBitmapChange( Bitmap bitmap, boolean update, Matrix matrix );

		public void onPreviewChange( Bitmap bitmap );

		public void onClearColorFilter();
	}

	/**
	 * The listener interface for receiving onTool events. The class that is interested in processing a onTool event implements this
	 * interface, and the object created with that class is registered with a component using the component's
	 * <code>addOnToolListener<code> method. When
	 * the onTool event occurs, that object's appropriate
	 * method is invoked.
	 * 
	 * @see OnToolEvent
	 */
	public static interface OnToolListener {

		/**
		 * On tool completed.
		 */
		void onToolCompleted();
	}

	/**
	 * All the possible states the filtermanager can use during the feather lifecycle.
	 * 
	 * @author alessandro
	 */
	static enum STATE {
		CLOSED_CANCEL, CLOSED_CONFIRMED, CLOSING, DISABLED, OPENED, OPENING,
	}

	/** The Constant STATE_OPENING. */
	public static final int STATE_OPENING = 0;

	/** The Constant STATE_OPENED. */
	public static final int STATE_OPENED = 1;

	/** The Constant STATE_CLOSING. */
	public static final int STATE_CLOSING = 2;

	/** The Constant STATE_CLOSED. */
	public static final int STATE_CLOSED = 3;

	/** The Constant STATE_DISABLED. */
	public static final int STATE_DISABLED = 4;

	public static final int STATE_CONTENT_READY = 5;
	
	public static final int STATE_READY = 6;

	public static final String LOG_TAG = "filter-manager";

	/** The current bitmap. */
	private Bitmap mBitmap;

	/** The base context. This is the main activity */
	private FeatherContext mContext;

	/** The current active effect. */
	private AbstractEffectPanel mCurrentEffect;

	/** The current active effect entry. */
	private EffectEntry mCurrentEntry;

	/** The current panel state. */
	private STATE mCurrentState;

	/** The main tool listener. */
	private OnToolListener mToolListener;

	/** bitmap change listener */
	private OnBitmapChangeListener mBitmapChangeListener;

	private final Handler mHandler;
	private final ServiceLoader<EffectContextService> mServiceLoader;
	private EffectLoaderService mEffectLoader;
	private Logger logger;

	/** The changed state. If the original image has been modified. */
	private boolean mChanged;

	private Configuration mConfiguration;

	private String mApiKey;
	private String mSessionId;

	private boolean mHiResEnabled = false;
	private OnHiResListener mHiResListener;

	private DragLayer mDragLayer;

	private static Handler mPluingsHandler;

	/**
	 * Instantiates a new filter manager.
	 * 
	 * @param context
	 *           the context
	 * @param handler
	 *           the handler
	 */
	public FilterManager( final FeatherContext context, final Handler handler, final String apiKey ) {
		logger = LoggerFactory.getLogger( "FilterManager", LoggerType.ConsoleLoggerType );
		mContext = context;
		mHandler = handler;
		mApiKey = apiKey;
		mPluingsHandler = new PluginHandler( this );
		mServiceLoader = new ServiceLoader<EffectContextService>( this );
		initServices( context );

		mConfiguration = new Configuration( ((Context)context).getResources().getConfiguration() );

		setCurrentState( STATE.DISABLED );
		mChanged = false;
	}
	
	public void setDragLayer( DragLayer view ) {
		mDragLayer = view;
	}

	private synchronized void initServices( final FeatherContext context ) {
		logger.info( "initServices" );
		mServiceLoader.register( ThreadPoolService.class );
		mServiceLoader.register( ConfigService.class );
		mServiceLoader.register( PluginService.class );
		mServiceLoader.register( EffectLoaderService.class );
		mServiceLoader.register( PreferenceService.class );
		mServiceLoader.register( HiResService.class );
		mServiceLoader.register( DragControllerService.class );
		mServiceLoader.register( LocalDataService.class );
		updateInstalledPlugins( null );
		updateAvailablePlugins();
	}

	private void updateAvailablePlugins() {
		logger.info( "updateAvailablePlugins" );
		ThreadPoolService background = getService( ThreadPoolService.class );
		if ( null != background ) {
			if ( Constants.getExternalPacksEnabled() ) {

				FutureListener<Bundle> listener = new FutureListener<Bundle>() {

					@Override
					public void onFutureDone( Future<Bundle> future ) {
						logger.log( "updateAvailablePlugins::completed" );
						Bundle result = null;
						try {
							result = future.get();
						} catch ( Throwable t ) {
							logger.error( t.getMessage() );
							return;
						}
						mPluingsHandler.post( new ExternalPluginTaskCompletedRunnable( result ) );
					}
				};

				background.submit( new ExternalPacksTask(), listener, null );
			}
		}

	}

	private void updateInstalledPlugins( Bundle extras ) {
		logger.info( "updateInstalledPlugins" );
		ThreadPoolService background = getService( ThreadPoolService.class );
		
		if ( background != null ) {
			final boolean externalItemsEnabled = Constants.getExternalPacksEnabled();


			FutureListener<PluginFetchTask.Result> listener = new FutureListener<PluginFetchTask.Result>() {

				@Override
				public void onFutureDone( Future<PluginFetchTask.Result> future ) {
					PluginFetchTask.Result result;

					try {
						result = future.get();
					} catch ( Throwable t ) {
						logger.error( t.getMessage() );
						return;
					}

					mPluingsHandler.post( new PluginTaskCompletedRunnable( result ) );
				}
			};

			BGCallable<Bundle, PluginFetchTask.Result> task;
			
			if( null == extras ){
				// first time
				task = new PluginFetchTask();
			} else {
				// when a plugin is changed
				task = new PluginUpdaterTask( externalItemsEnabled ? mPluingsHandler : null );
			}
			
			background.submit( task, listener, extras );
			
		} else {
			logger.error( "failed to retrieve ThreadPoolService" );
		}
	}

	/**
	 * Register a default handler to receive hi-res messages
	 * 
	 * @param handler
	 */
	public void setOnHiResListener( OnHiResListener listener ) {
		mHiResListener = listener;
	}

	private void initHiResService() {

		logger.info( "initHiResService" );
		LocalDataService dataService = getService( LocalDataService.class );

		if ( Constants.containsValue( Constants.EXTRA_OUTPUT_HIRES_SESSION_ID ) ) {

			mSessionId = Constants.getValueFromIntent( Constants.EXTRA_OUTPUT_HIRES_SESSION_ID, "" );
			logger.info( "session-id: " + mSessionId + ", length: " + mSessionId.length() );

			if ( mSessionId != null && mSessionId.length() == 64 ) {
				mHiResEnabled = true;

				HiResService service = getService( HiResService.class );
				if ( !service.isRunning() ) {
					service.start();
				}
				service.load( mSessionId, mApiKey, dataService.getSourceImageUri() );
			} else {
				logger.error( "session id is invalid" );
			}
		} else {
			logger.warning( "missing session id" );
		}

		if ( null != mHiResListener ) {
			mHiResListener.OnLoad( dataService.getSourceImageUri() );
		}
	}

	/**
	 * This is the entry point of every feather tools. The main activity catches the tool onClick listener and notify the
	 * filtermanager.
	 * 
	 * @param tag
	 *           the tag
	 */
	public void activateEffect( final EffectEntry tag ) {
		if ( !getEnabled() || !isClosed() || mBitmap == null ) return;

		if ( mCurrentEffect != null ) throw new IllegalStateException( "There is already an active effect. Cannot activate new" );
		if ( mEffectLoader == null ) mEffectLoader = (EffectLoaderService) getService( EffectLoaderService.class );

		final AbstractEffectPanel effect = mEffectLoader.load( tag );

		if ( effect != null ) {
			mCurrentEffect = effect;
			mCurrentEntry = tag;

			setCurrentState( STATE.OPENING );
			prepareEffectPanel( effect, tag );

			Tracker.recordTag( mCurrentEntry.name.name().toLowerCase() + ": opened" );
			mContext.getBottomBar().setOnPanelOpenListener( new OnPanelOpenListener() {

				@Override
				public void onOpened() {
					setCurrentState( STATE.OPENED );
					mContext.getBottomBar().setOnPanelOpenListener( null );
				}

				@Override
				public void onOpening() {
					mCurrentEffect.onOpening();
				}
			} );

			mContext.getBottomBar().open();
		}
	}

	/**
	 * Dispose.
	 */
	public void dispose() {
		// TODO: if a panel is opened the deactivate an destroy it

		if ( mCurrentEffect != null ) {
			logger.log( "Deactivate and destroy current panel" );
			mCurrentEffect.onDeactivate();
			mCurrentEffect.onDestroy();
			mCurrentEffect = null;
		}
		mServiceLoader.dispose();
		mContext = null;
		mToolListener = null;
		mBitmapChangeListener = null;

		System.gc();
	}

	@Override
	public double getApplicationMaxMemory() {
		return Constants.getApplicationMaxMemory();
	}
	
	@Override
	public void getRuntimeMemoryInfo( double[] outValues ) {
		Constants.getMemoryInfo( outValues );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aviary.android.feather.library.services.EffectContext#getBaseContext()
	 */
	@Override
	public Context getBaseContext() {
		return (Activity) mContext;
	}

	@Override
	public Activity getBaseActivity() {
		return (Activity) mContext;
	}

	/**
	 * Return the current bitmap.
	 * 
	 * @return the bitmap
	 */
	public Bitmap getBitmap() {
		return mBitmap;
	}

	/**
	 * Return true if the main image has been modified by any of the feather tools.
	 * 
	 * @return the bitmap is changed
	 */
	public boolean getBitmapIsChanged() {
		return mChanged;
	}

	/**
	 * Return the active tool, null if there is not active tool.
	 * 
	 * @return the current effect
	 */
	@Override
	public EffectEntry getCurrentEffect() {
		return mCurrentEntry;
	}

	/**
	 * Return the current panel associated with the active tool. Null if there's no active tool
	 * 
	 * @return the current panel
	 */
	public AbstractEffectPanel getCurrentPanel() {
		return mCurrentEffect;
	}

	/**
	 * Return the current image transformation matrix. this is useful for those tools which implement ContentPanel and want to
	 * display the preview bitmap with the same zoom level of the main image
	 * 
	 * @return the current image view matrix
	 * @see ContentPanel
	 */
	@Override
	public Matrix getCurrentImageViewMatrix() {
		return mContext.getMainImage().getDisplayMatrix();
	}

	/**
	 * Return true if enabled.
	 * 
	 * @return the enabled
	 */
	public boolean getEnabled() {
		return mCurrentState != STATE.DISABLED;
	}

	/**
	 * Return the service, if previously registered using ServiceLoader.
	 * 
	 * @param <T>
	 *           the generic type
	 * @param cls
	 *           the cls
	 * @return the service
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T> T getService( Class<T> cls ) {
		try {
			return (T) mServiceLoader.getService( (Class<EffectContextService>) cls );
		} catch ( IllegalAccessException e ) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Return true if there's no active tool.
	 * 
	 * @return true, if is closed
	 */
	public boolean isClosed() {
		return ( mCurrentState == STATE.CLOSED_CANCEL ) || ( mCurrentState == STATE.CLOSED_CONFIRMED );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aviary.android.feather.library.services.EffectContext#isConnectedOrConnecting()
	 */
	@Override
	public boolean isConnectedOrConnecting() {
		try {
			final ConnectivityManager conn = (ConnectivityManager) getBaseContext().getSystemService( Context.CONNECTIVITY_SERVICE );
			if ( ( conn.getActiveNetworkInfo() == null ) || !conn.getActiveNetworkInfo().isConnectedOrConnecting() ) return false;
			return true;
		} catch ( final SecurityException e ) {
			// the android.permission.ACCESS_NETWORK_STATE is not set, so we're assuming
			// an internet connection is available
			logger.error( "android.permission.ACCESS_NETWORK_STATE is not defined. Assuming network is fine" );
			return true;
		}
	}

	/**
	 * return true if there's one active tool.
	 * 
	 * @return true, if is opened
	 */
	public boolean isOpened() {
		return mCurrentState == STATE.OPENED;
	}

	/**
	 * On activate.
	 * 
	 * @param bitmap
	 *           the bitmap
	 */
	public void onActivate( final Bitmap bitmap, int[] originalSize ) {
		if ( mCurrentState != STATE.DISABLED ) throw new IllegalStateException( "Cannot activate. Already active!" );

		if ( ( mBitmap != null ) && !mBitmap.isRecycled() ) {
			mBitmap = null;
		}

		mBitmap = bitmap;

		LocalDataService dataService = getService( LocalDataService.class );
		dataService.setSourceImageSize( originalSize );

		mChanged = false;
		setCurrentState( STATE.CLOSED_CONFIRMED );
		initHiResService();
	}

	/**
	 * Current activity is asking to apply the current tool.
	 */
	public void onApply() {
		logger.info( "FilterManager::onapply" );
		if ( !getEnabled() || !isOpened() ) return;

		if ( mCurrentEffect == null ) throw new IllegalStateException( "there is no current effect active in the context" );

		if ( !mCurrentEffect.isEnabled() ) return;

		if ( mCurrentEffect.getIsChanged() ) {
			mCurrentEffect.onSave();
			mChanged = true;
		} else {
			onCancel();
		}
	}

	/**
	 * Parent activity just received a onBackPressed event. If there's one active tool, it will be asked to manage the onBackPressed
	 * event. If the active tool onBackPressed method return a false then try to close it.
	 * 
	 * @return true, if successful
	 */
	public boolean onBackPressed() {
		if ( isClosed() ) return false;
		if ( mCurrentState != STATE.DISABLED ) {
			if ( isOpened() ) {
				if ( !mCurrentEffect.onBackPressed() ) onCancel();
			}
			return true;
		}
		return false;
	}

	/**
	 * Main activity asked to cancel the current operation.
	 */
	public void onCancel() {
		if ( !getEnabled() || !isOpened() ) return;
		if ( mCurrentEffect == null ) throw new IllegalStateException( "there is no current effect active in the context" );
		if ( !mCurrentEffect.onCancel() ) {
			cancel();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aviary.android.feather.library.services.EffectContext#cancel()
	 */
	@Override
	public void cancel() {

		logger.info( "FilterManager::cancel" );

		if ( !getEnabled() || !isOpened() ) return;
		if ( mCurrentEffect == null ) throw new IllegalStateException( "there is no current effect active in the context" );

		Tracker.recordTag( mCurrentEntry.name.name().toLowerCase() + ": cancelled" );

		// send the cancel event to the effect
		mCurrentEffect.onCancelled();

		// check changed image
		if ( mCurrentEffect.getIsChanged() ) {
			// panel is changed, restore the original bitmap

			if ( mCurrentEffect instanceof ContentPanel ) {
				ContentPanel panel = (ContentPanel) mCurrentEffect;
				setNextBitmap( mBitmap, true, panel.getContentDisplayMatrix() );
			} else {
				setNextBitmap( mBitmap, false );
			}

		} else {
			// panel is not changed
			if ( mCurrentEffect instanceof ContentPanel ) {
				ContentPanel panel = (ContentPanel) mCurrentEffect;
				setNextBitmap( mBitmap, true, panel.getContentDisplayMatrix() );
			} else {
				setNextBitmap( mBitmap, false );
			}
		}
		onClose( false );
	}

	/**
	 * On close.
	 * 
	 * @param isConfirmed
	 *           the is confirmed
	 */
	private void onClose( final boolean isConfirmed ) {

		logger.info( "onClose" );

		setCurrentState( STATE.CLOSING );

		mContext.getBottomBar().setOnPanelCloseListener( new OnPanelCloseListener() {

			@Override
			public void onClosed() {
				setCurrentState( isConfirmed ? STATE.CLOSED_CONFIRMED : STATE.CLOSED_CANCEL );
				mContext.getBottomBar().setOnPanelCloseListener( null );
			}

			@Override
			public void onClosing() {
				mCurrentEffect.onClosing();
			}
		} );
		mContext.getBottomBar().close();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aviary.android.feather.effects.AbstractEffectPanel.OnApplyResultListener#onComplete(android.graphics.Bitmap,
	 * java.util.HashMap)
	 */
	@Override
	public void onComplete( final Bitmap result, MoaActionList actions, HashMap<String, String> trackingAttributes ) {
		logger.info( "onComplete: " + android.os.Debug.getNativeHeapAllocatedSize() );
		Tracker.recordTag( mCurrentEntry.name.name().toLowerCase() + ": applied", trackingAttributes );

		if ( result != null ) {
			if ( mCurrentEffect instanceof ContentPanel ) {
				ContentPanel panel = (ContentPanel) mCurrentEffect;
				final boolean changed = BitmapUtils.compareBySize( mBitmap, result );
				setNextBitmap( result, true, changed ? null : panel.getContentDisplayMatrix() );
			} else {
				setNextBitmap( result, false );
			}

		} else {
			logger.error( "Error: returned bitmap is null!" );
			setNextBitmap( mBitmap, true );
		}

		onClose( true );

		if ( mHiResEnabled ) {
			// send the actions...
			if ( null == actions ) logger.error( "WTF actionlist is null!!!!" );

			HiResService service = getService( HiResService.class );
			if ( service.isRunning() ) {
				service.execute( mSessionId, mApiKey, actions );
			}
		}

		if ( null != mHiResListener ) {
			mHiResListener.OnApplyActions( actions );
		}
	}

	/**
	 * Sets the next bitmap.
	 * 
	 * @param bitmap
	 *           the new next bitmap
	 */
	void setNextBitmap( Bitmap bitmap ) {
		setNextBitmap( bitmap, true );
	}

	/**
	 * Sets the next bitmap.
	 * 
	 * @param bitmap
	 *           the bitmap
	 * @param update
	 *           the update
	 */
	void setNextBitmap( Bitmap bitmap, boolean update ) {
		setNextBitmap( bitmap, update, null );
	}

	/**
	 * Sets the next bitmap.
	 * 
	 * @param bitmap
	 *           the bitmap
	 * @param update
	 *           the update
	 * @param matrix
	 *           the matrix
	 */
	void setNextBitmap( Bitmap bitmap, boolean update, Matrix matrix ) {
		logger.log( "setNextBitmap", bitmap, update, matrix );

		if ( null != mBitmapChangeListener ) mBitmapChangeListener.onBitmapChange( bitmap, update, matrix );

		if ( !mBitmap.equals( bitmap ) ) {
			logger.warning( "[recycle] original Bitmap: " + mBitmap );
			mBitmap.recycle();
			mBitmap = null;
		}
		mBitmap = bitmap;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aviary.android.feather.effects.AbstractEffectPanel.OnErrorListener#onError(java.lang.String)
	 */
	@Override
	public void onError( final String error ) {
		new AlertDialog.Builder( (Activity) mContext ).setTitle( R.string.generic_error_title ).setMessage( error )
				.setIcon( android.R.drawable.ic_dialog_alert ).show();
	}

	@Override
	public void onError( final String error, int yesLabel, OnClickListener yesListener, int noLabel, OnClickListener noListener ) {

		new AlertDialog.Builder( (Activity) mContext ).setTitle( R.string.generic_error_title ).setMessage( error )
				.setPositiveButton( yesLabel, yesListener ).setNegativeButton( noLabel, noListener )
				.setIcon( android.R.drawable.ic_dialog_alert ).show();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aviary.android.feather.effects.AbstractEffectPanel.OnPreviewListener#onPreviewChange(android.graphics.Bitmap)
	 */
	@Override
	public void onPreviewChange( final Bitmap result ) {
		if ( !getEnabled() || !isOpened() ) return;
		if ( null != mBitmapChangeListener ) mBitmapChangeListener.onPreviewChange( result );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aviary.android.feather.effects.AbstractEffectPanel.OnPreviewListener#onPreviewChange(android.graphics.ColorFilter)
	 */
	@Override
	public void onPreviewChange( ColorFilter colorFilter ) {
		if ( !getEnabled() || !isOpened() ) return;
		if ( null != mBitmapChangeListener ) mBitmapChangeListener.onPreviewChange( colorFilter );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aviary.android.feather.effects.AbstractEffectPanel.OnContentReadyListener#onReady(com.aviary.android.feather.effects.
	 * AbstractEffectPanel)
	 */
	@Override
	public void onReady( final AbstractEffectPanel panel ) {
		mHandler.sendEmptyMessage( STATE_CONTENT_READY );
		mHandler.sendEmptyMessage( STATE_READY );
	}

	/**
	 * Replace the current bitmap.
	 * 
	 * @param bitmap
	 *           the bitmap
	 */
	public void onReplaceImage( final Bitmap bitmap, int[] originalSize ) {
		if ( !getEnabled() || !isClosed() ) throw new IllegalStateException( "Cannot replace bitmap. Not active nor closed!" );

		LocalDataService dataService = getService( LocalDataService.class );

		if ( ( mBitmap != null ) && !mBitmap.isRecycled() ) {
			logger.warning( "[recycle] original Bitmap: " + mBitmap );
			mBitmap.recycle();
			mBitmap = null;
		}
		mChanged = false;
		mBitmap = bitmap;

		dataService.setSourceImageSize( originalSize );

		HiResService service = getService( HiResService.class );
		if ( mHiResEnabled && service.isRunning() ) {
			service.replace( mSessionId, mApiKey, dataService.getSourceImageUri() );
		}

		if ( null != mHiResListener ) {
			mHiResListener.OnLoad( dataService.getSourceImageUri() );
		}
	}

	/**
	 * On save.
	 */
	public void onSave() {
		if ( !getEnabled() || !isClosed() ) return;
	}

	/**
	 * Prepare effect panel.
	 * 
	 * @param effect
	 *           the effect
	 * @param entry
	 *           the entry
	 */
	private void prepareEffectPanel( final AbstractEffectPanel effect, final EffectEntry entry ) {
		View option_child = null;
		View drawing_child = null;

		if ( effect instanceof OptionPanel ) {
			option_child = ( (OptionPanel) effect ).getOptionView( UIUtils.getLayoutInflater(), mContext.getOptionsPanelContainer() );
			mContext.getOptionsPanelContainer().addView( option_child );
		}

		if ( effect instanceof ContentPanel ) {
			drawing_child = ( (ContentPanel) effect ).getContentView( UIUtils.getLayoutInflater() );
			drawing_child.setLayoutParams( new LayoutParams( LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT ) );
			mContext.getDrawingImageContainer().addView( drawing_child );
		}

		effect.onCreate( mBitmap );
	}

	/**
	 * Run a Runnable on the main UI thread.
	 * 
	 * @param action
	 *           the action
	 */
	@Override
	public void runOnUiThread( final Runnable action ) {
		if ( mContext != null ) ( (Activity) mContext ).runOnUiThread( action );
	}

	/**
	 * Sets the current state.
	 * 
	 * @param newState
	 *           the new current state
	 */
	private void setCurrentState( final STATE newState ) {
		if ( newState != mCurrentState ) {
			final STATE previousState = mCurrentState;
			mCurrentState = newState;

			switch ( newState ) {
				case OPENING:
					mCurrentEffect.setOnPreviewListener( this );
					mCurrentEffect.setOnApplyResultListener( this );
					mCurrentEffect.setOnErrorListener( this );
					mCurrentEffect.setOnProgressListener( this );

					if ( mCurrentEffect instanceof ContentPanel ) ( (ContentPanel) mCurrentEffect ).setOnReadyListener( this );

					mHandler.sendEmptyMessage( FilterManager.STATE_OPENING );
					break;

				case OPENED:
					mCurrentEffect.onActivate();
					mHandler.sendEmptyMessage( FilterManager.STATE_OPENED );
					
					if( !(mCurrentEffect instanceof ContentPanel) ){
						mHandler.sendEmptyMessage( STATE_READY );
					}
					
					break;

				case CLOSING:
					mHandler.sendEmptyMessage( FilterManager.STATE_CLOSING );
					mHandler.post( new Runnable() {

						@Override
						public void run() {
							mCurrentEffect.onDeactivate();
							if ( null != mBitmapChangeListener ) mBitmapChangeListener.onClearColorFilter();
							if ( mCurrentEffect instanceof ContentPanel ) ( (ContentPanel) mCurrentEffect ).setOnReadyListener( null );
							mContext.getDrawingImageContainer().removeAllViews();
						}
					} );
					break;

				case CLOSED_CANCEL:
				case CLOSED_CONFIRMED:

					mContext.getOptionsPanelContainer().removeAllViews();

					if ( previousState != STATE.DISABLED ) {
						mCurrentEffect.onDestroy();
						mCurrentEffect.setOnPreviewListener( null );
						mCurrentEffect.setOnApplyResultListener( null );
						mCurrentEffect.setOnErrorListener( null );
						mCurrentEffect.setOnProgressListener( null );
						mCurrentEffect = null;
						mCurrentEntry = null;
					}

					mHandler.sendEmptyMessage( FilterManager.STATE_CLOSED );

					if ( ( newState == STATE.CLOSED_CONFIRMED ) && ( previousState != STATE.DISABLED ) )
						if ( mToolListener != null ) mToolListener.onToolCompleted();
					System.gc();
					break;

				case DISABLED:
					mHandler.sendEmptyMessage( FilterManager.STATE_DISABLED );
					break;
			}
		}
	}

	/**
	 * Sets the enabled.
	 * 
	 * @param value
	 *           the new enabled
	 */
	public void setEnabled( final boolean value ) {
		if ( !value ) {
			if ( isClosed() ) {
				setCurrentState( STATE.DISABLED );
			} else {
				logger.warning( "FilterManager must be closed to change state" );
			}
		}
	}

	/**
	 * Sets the on tool listener.
	 * 
	 * @param listener
	 *           the new on tool listener
	 */
	public void setOnToolListener( final OnToolListener listener ) {
		mToolListener = listener;
	}

	public void setOnBitmapChangeListener( final OnBitmapChangeListener listener ) {
		mBitmapChangeListener = listener;
	}

	/**
	 * Main Activity configuration changed We want to dispatch the configuration event also to the opened panel.
	 * 
	 * @param newConfig
	 *           the new config
	 * @return true if the event has been handled
	 */
	public boolean onConfigurationChanged( Configuration newConfig ) {

		boolean result = false;
		logger.info( "onConfigurationChanged: " + newConfig.orientation + ", " + mConfiguration.orientation );

		if ( mCurrentEffect != null ) {
			if ( mCurrentEffect.isCreated() ) {
				logger.info( "onConfigurationChanged, sending event to ", mCurrentEffect );
				mCurrentEffect.onConfigurationChanged( newConfig, mConfiguration );
				result = true;
			}
		}

		mConfiguration = new Configuration( newConfig );
		return result;
	}

	/**
	 * A plugin or theme has been installed/removed or replaced Notify the internal pluginservice about the new plugin. All the
	 * classes which have a listener attached to the PluginService will be notified too.
	 * 
	 * @param intent
	 *           the intent
	 * @see FeatherSystemReceiver
	 */
	public void onPluginChanged( Intent intent ) {
		logger.info( "onReceive", intent );
		logger.info( "data", intent.getData() );
		updateInstalledPlugins( intent.getExtras() );
	}

	private static class PluginHandler extends Handler {

		WeakReference<EffectContext> mContext;

		public PluginHandler( EffectContext context ) {
			mContext = new WeakReference<EffectContext>( context );
		}

		@Override
		public void handleMessage( Message msg ) {

			EffectContext effectContext = mContext.get();
			if ( null != effectContext ) {

				Context context = effectContext.getBaseContext();
				UpdateType update = (UpdateType) msg.obj;
				PluginService service = effectContext.getService( PluginService.class );
				
				final String packagename = update.getPackageName();
				final int pluginType = update.getPluginType();
				
				LoggerFactory.log( "PluginHandler::handleMessage. " + msg.what + ", update:" + update.toString() );

				switch ( msg.what ) {
					case PluginUpdaterTask.MSG_PLUING_ADD:
						service.install( context, packagename, pluginType );
						break;

					case PluginUpdaterTask.MSG_PLUING_REMOVE:
						if ( service.installed( packagename ) ) {
							service.uninstall( context, packagename );
						}
						break;

					case PluginUpdaterTask.MSG_PLUING_REPLACE:
						if ( service.uninstall( context, packagename ) ) {
							service.install( context, packagename, pluginType );
						}
						break;
				}

			}

		};
	};

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aviary.android.feather.effects.AbstractEffectPanel.OnProgressListener#onProgressStart()
	 */
	@Override
	public void onProgressStart() {
		mContext.showToolProgress();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aviary.android.feather.effects.AbstractEffectPanel.OnProgressListener#onProgressEnd()
	 */
	@Override
	public void onProgressEnd() {
		mContext.hideToolProgress();
	}

	@Override
	public void onProgressModalStart() {
		mContext.showModalProgress();
	}

	@Override
	public void onProgressModalEnd() {
		mContext.hideModalProgress();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aviary.android.feather.library.services.EffectContext#setToolbarTitle(int)
	 */
	@Override
	public void setToolbarTitle( int resId ) {
		setToolbarTitle( getBaseContext().getString( resId ) );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aviary.android.feather.library.services.EffectContext#setToolbarTitle(java.lang.CharSequence)
	 */
	@Override
	public void setToolbarTitle( CharSequence value ) {
		mContext.getToolbar().setTitle( value );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aviary.android.feather.library.services.EffectContext#restoreToolbarTitle()
	 */
	@Override
	public void restoreToolbarTitle() {
		if ( mCurrentEntry != null ) mContext.getToolbar().setTitle( mCurrentEntry.labelResourceId );
	}

	@Override
	public void downloadPlugin( final String packageName, final int type ) {
		searchOrDownloadPlugin( packageName, type, false );
	}

	@Override
	public void searchPlugin( final int type ) {

		String name = FeatherIntent.PluginType.getName( type );
		String packageName = FeatherIntent.PLUGIN_BASE_PACKAGE;
		if ( null != name ) {
			packageName = packageName + name + ".*";
		} else {
			packageName = packageName + "*";
		}

		searchOrDownloadPlugin( packageName, type, true );
	}

	public void searchOrDownloadPlugin( final String packageName, final int type, final boolean search ) {
		logger.info( "searchOrDownloadPlugin: " + packageName + ", search: " + search );

		Intent intent = new Intent( Intent.ACTION_VIEW );
		if ( search )
			intent.setData( Uri.parse( "market://search?q=" + packageName ) );
		else
			intent.setData( Uri.parse( "market://details?id=" + packageName ) );

		try {

			String name = FeatherIntent.PluginType.getName( type );
			if ( null != name ) {
				HashMap<String, String> attrs = new HashMap<String, String>();
				attrs.put( "assetType", name );
				Tracker.recordTag( "content: addMoreClicked", attrs );
			}
			getBaseContext().startActivity( intent );
		} catch ( ActivityNotFoundException e ) {
			Toast.makeText( getBaseContext(), R.string.feather_activity_not_found, Toast.LENGTH_SHORT ).show();
			e.printStackTrace();
		}
	}

	@Override
	public DragLayer getDragLayer() {
		return mDragLayer;
	}

	private class PluginTaskCompletedRunnable implements Runnable {

		PluginFetchTask.Result mResult;

		public PluginTaskCompletedRunnable( final PluginFetchTask.Result result ) {
			mResult = result;
		}

		@Override
		public void run() {
			PluginService pluginService = getService( PluginService.class );
			if( null != pluginService )
				pluginService.update( mResult.installed, mResult.delta );
		}
	}

	private class ExternalPluginTaskCompletedRunnable implements Runnable {

		Bundle mResult;

		public ExternalPluginTaskCompletedRunnable( final Bundle result ) {
			mResult = result;
		}

		@Override
		public void run() {
			PluginService pluginService = getService( PluginService.class );
			if ( null != pluginService ) {
				pluginService.updateExternalPackages( mResult );
			}
		}
	}
}
