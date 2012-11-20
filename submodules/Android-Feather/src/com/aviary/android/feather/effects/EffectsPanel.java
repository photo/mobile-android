package com.aviary.android.feather.effects;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import org.json.JSONException;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewSwitcher.ViewFactory;
import com.aviary.android.feather.Constants;
import com.aviary.android.feather.R;
import com.aviary.android.feather.async_tasks.AsyncImageManager;
import com.aviary.android.feather.async_tasks.AsyncImageManager.OnImageLoadListener;
import com.aviary.android.feather.graphics.RepeatableHorizontalDrawable;
import com.aviary.android.feather.library.content.FeatherIntent;
import com.aviary.android.feather.library.content.FeatherIntent.PluginType;
import com.aviary.android.feather.library.filters.EffectFilter;
import com.aviary.android.feather.library.filters.FilterLoaderFactory;
import com.aviary.android.feather.library.filters.FilterLoaderFactory.Filters;
import com.aviary.android.feather.library.filters.INativeFiler;
import com.aviary.android.feather.library.filters.NativeFilter;
import com.aviary.android.feather.library.graphics.drawable.FakeBitmapDrawable;
import com.aviary.android.feather.library.moa.Moa;
import com.aviary.android.feather.library.moa.MoaAction;
import com.aviary.android.feather.library.moa.MoaActionFactory;
import com.aviary.android.feather.library.moa.MoaActionList;
import com.aviary.android.feather.library.moa.MoaColorParameter;
import com.aviary.android.feather.library.moa.MoaResult;
import com.aviary.android.feather.library.plugins.FeatherExternalPack;
import com.aviary.android.feather.library.plugins.FeatherInternalPack;
import com.aviary.android.feather.library.plugins.FeatherPack;
import com.aviary.android.feather.library.plugins.PluginManager;
import com.aviary.android.feather.library.plugins.PluginManager.InternalPlugin;
import com.aviary.android.feather.library.plugins.UpdateType;
import com.aviary.android.feather.library.services.ConfigService;
import com.aviary.android.feather.library.services.EffectContext;
import com.aviary.android.feather.library.services.PluginService;
import com.aviary.android.feather.library.services.PluginService.OnUpdateListener;
import com.aviary.android.feather.library.services.PluginService.PluginError;
import com.aviary.android.feather.library.services.PreferenceService;
import com.aviary.android.feather.library.tracking.Tracker;
import com.aviary.android.feather.library.utils.BitmapUtils;
import com.aviary.android.feather.library.utils.SystemUtils;
import com.aviary.android.feather.library.utils.UserTask;
import com.aviary.android.feather.utils.UIUtils;
import com.aviary.android.feather.widget.ArrayAdapterExtended;
import com.aviary.android.feather.widget.EffectThumbLayout;
import com.aviary.android.feather.widget.HorizontalFixedListView;
import com.aviary.android.feather.widget.ImageSwitcher;
import com.aviary.android.feather.widget.SwipeView;
import com.aviary.android.feather.widget.SwipeView.OnSwipeListener;

public class EffectsPanel extends AbstractContentPanel implements ViewFactory, OnUpdateListener, OnSwipeListener,
		OnImageLoadListener {

	/** Plugin type handled in this panel */
	private final int mType;

	/** thumbnail listview */
	private HorizontalFixedListView mHList;

	/** Panel is rendering. */
	private volatile Boolean mIsRendering = false;

	private Boolean mIsAnimating;

	/** The current rendering task. */
	private RenderTask mCurrentTask;

	/** The small preview used for fast rendering. */
	private Bitmap mSmallPreview;

	private static final int PREVIEW_SCALE_FACTOR = 4;

	/** enable/disable fast preview. */
	private boolean mEnableFastPreview = false;

	private PluginService mPluginService;

	private PreferenceService mPrefService;

	/** The main image switcher. */
	private ImageSwitcher mImageSwitcher;

	private boolean mExternalPacksEnabled = true;

	/**
	 * A reference to the effect applied
	 */
	private MoaActionList mActions = null;

	/**
	 * create a reference to the update alert dialog. This to prevent multiple alert messages
	 */
	private AlertDialog mUpdateDialog;

	/** default width of each effect thumbnail */
	private int mFilterCellWidth = 80;

	private List<String> mInstalledPackages;

	// thumbnail cache manager
	private AsyncImageManager mImageManager;

	// thumbnail for effects
	private Bitmap mThumbBitmap;

	// current selected thumbnail
	private View mSelectedEffectView = null;

	// current selected position
	private int mSelectedPosition = 1;

	// first position allowed in selection
	private static int FIRST_POSITION = 1;

	@SuppressWarnings("unused")
	private SwipeView mSwipeView;

	/** the effects list adapter */
	private EffectsAdapter mListAdapter;

	private int mAvailablePacks = 0;
	
	private boolean mEnableEffectAnimation = false;

	private Bitmap updateArrowBitmap;

	// thumbnail properties
	private static int mRoundedBordersPixelSize = 16;
	private static int mShadowRadiusPixelSize = 4;
	private static int mShadowOffsetPixelSize = 2;
	private static int mRoundedBordersPaddingPixelSize = 5;
	private static int mRoundedBordersStrokePixelSize = 3;

	// don't display the error dialog more than once
	private static boolean mUpdateErrorHandled = false;

	public EffectsPanel( EffectContext context, int type ) {
		super( context );
		mType = type;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onCreate( Bitmap bitmap ) {
		super.onCreate( bitmap );

		mImageManager = new AsyncImageManager();

		mPluginService = getContext().getService( PluginService.class );
		mPrefService = getContext().getService( PreferenceService.class );

		mExternalPacksEnabled = Constants.getValueFromIntent( Constants.EXTRA_EFFECTS_ENABLE_EXTERNAL_PACKS, true );
		mSelectedPosition = mExternalPacksEnabled ? 1 : 0;
		FIRST_POSITION = mExternalPacksEnabled ? 1 : 0;

		mHList = (HorizontalFixedListView) getOptionView().findViewById( R.id.list );
		mHList.setOverScrollMode( View.OVER_SCROLL_ALWAYS );

		mImageSwitcher = (ImageSwitcher) getContentView().findViewById( R.id.switcher );
		mImageSwitcher.setSwitchEnabled( mEnableFastPreview );
		mImageSwitcher.setFactory( this );

		mSwipeView = (SwipeView) getContentView().findViewById( R.id.swipeview );

		mPreview = BitmapUtils.copy( mBitmap, Bitmap.Config.ARGB_8888 );

		// setup the main imageview based on the current configuration
		if ( mEnableFastPreview ) {
			try {
				mSmallPreview = Bitmap.createBitmap( mBitmap.getWidth() / PREVIEW_SCALE_FACTOR, mBitmap.getHeight()
						/ PREVIEW_SCALE_FACTOR, Config.ARGB_8888 );
				mImageSwitcher.setImageBitmap( mBitmap, true, null, Float.MAX_VALUE );
				mImageSwitcher.setInAnimation( AnimationUtils.loadAnimation( getContext().getBaseContext(), android.R.anim.fade_in ) );
				mImageSwitcher.setOutAnimation( AnimationUtils.loadAnimation( getContext().getBaseContext(), android.R.anim.fade_out ) );
			} catch ( OutOfMemoryError e ) {
				mEnableFastPreview = false;
			}
		} else {
			mImageSwitcher.setImageBitmap( mBitmap, true, getContext().getCurrentImageViewMatrix(), Float.MAX_VALUE );
		}

		mImageSwitcher.setAnimateFirstView( false );

		mHList.setOnItemClickListener( new android.widget.AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick( android.widget.AdapterView<?> parent, View view, int position, long id ) {
				if ( !view.isSelected() && isActive() ) {
					int viewType = mHList.getAdapter().getItemViewType( position );

					if ( viewType == EffectsAdapter.TYPE_NORMAL ) {

						EffectPack item = (EffectPack) mHList.getAdapter().getItem( position );

						if ( item.mStatus == PluginError.NoError ) {
							setSelectedEffect( view, position );
						} else {
							showUpdateAlert( item.mPackageName, item.mStatus, true );
						}
					} else if ( viewType == EffectsAdapter.TYPE_GET_MORE_FIRST || viewType == EffectsAdapter.TYPE_GET_MORE_LAST ) {
						EffectsPanel.this.getContext().searchPlugin( mType );
					}
				}
			}
		} );

		View content = getOptionView().findViewById( R.id.background );
		content.setBackgroundDrawable( RepeatableHorizontalDrawable.createFromView( content ) );

		try {
			updateArrowBitmap = BitmapFactory.decodeResource( getContext().getBaseContext().getResources(),
					R.drawable.feather_update_arrow );
		} catch ( Throwable t ) {}
		
		
		mEnableEffectAnimation = Constants.ANDROID_SDK > android.os.Build.VERSION_CODES.GINGERBREAD && SystemUtils.getCpuMhz() > 800; 
	}

	private void showUpdateAlert( final CharSequence packageName, final PluginError error, boolean fromUseClick ) {
		if ( error != PluginError.NoError ) {
			
			String errorString;
			
			if( fromUseClick )
				errorString = getError( error );
			else
				errorString = getErrors( error );

			if ( error == PluginError.PluginTooOldError ) {

				OnClickListener yesListener = new OnClickListener() {

					@Override
					public void onClick( DialogInterface dialog, int which ) {
						getContext().downloadPlugin( (String) packageName, mType );
					}
				};
				onGenericError( errorString, R.string.feather_update, yesListener, android.R.string.cancel, null );

			} else if ( error == PluginError.PluginTooNewError ) {
				OnClickListener yesListener = new OnClickListener() {

					@Override
					public void onClick( DialogInterface dialog, int which ) {
						String pname = getContext().getBaseContext().getPackageName();
						getContext().downloadPlugin( pname, mType );
					}
				};
				onGenericError( errorString, R.string.feather_update, yesListener, android.R.string.cancel, null );
			} else {
				onGenericError( errorString );
			}
			return;
		}
	}

	/**
	 * Create a popup alert dialog when multiple plugins need to be updated
	 * 
	 * @param error
	 */
	private void showUpdateAlertMultiplePlugins( final PluginError error, boolean fromUserClick  ) {

		if ( error != PluginError.NoError ) {
			final String errorString = getErrors( error );

			if ( error == PluginError.PluginTooOldError ) {
				OnClickListener yesListener = new OnClickListener() {

					@Override
					public void onClick( DialogInterface dialog, int which ) {
						getContext().searchPlugin( mType );
					}
				};
				onGenericError( errorString, R.string.feather_update, yesListener, android.R.string.cancel, null );

			} else if ( error == PluginError.PluginTooNewError ) {
				OnClickListener yesListener = new OnClickListener() {

					@Override
					public void onClick( DialogInterface dialog, int which ) {
						String pname = getContext().getBaseContext().getPackageName();
						getContext().downloadPlugin( pname, mType );
					}
				};
				onGenericError( errorString, R.string.feather_update, yesListener, android.R.string.cancel, null );
			} else {
				onGenericError( errorString );
			}
		}
	}
	
	/**
	 * 
	 * @param set
	 */
	private void showUpdateAlertMultipleItems( final String pkgname, Set<PluginError> set ) {
		if( null != set ) {
			final String errorString = getContext().getBaseContext().getResources().getString( R.string.feather_effects_error_update_multiple );
			
			OnClickListener yesListener = new OnClickListener() {

				@Override
				public void onClick( DialogInterface dialog, int which ) {
					getContext().searchOrDownloadPlugin( pkgname, mType, true );
				}
			};
			onGenericError( errorString, R.string.feather_update, yesListener, android.R.string.cancel, null );
		}
	}

	protected String getError( PluginError error ) {

		int resId = R.string.feather_effects_error_loading_pack;

		switch ( error ) {
			case UnknownError:
				resId = R.string.feather_effects_unknown_error;
				break;

			case PluginTooOldError:
				resId = R.string.feather_effects_error_update_pack;
				break;

			case PluginTooNewError:
				resId = R.string.feather_effects_error_update_editor;
				break;

			case PluginNotLoadedError:
				break;

			case PluginLoadError:
				break;

			case MethodNotFoundError:
				break;

			default:
				break;
		}

		return getContext().getBaseContext().getString( resId );
	}

	protected String getErrors( PluginError error ) {

		int resId = R.string.feather_effects_error_loading_packs;

		switch ( error ) {
			case UnknownError:
				resId = R.string.feather_effects_unknown_errors;
				break;

			case PluginTooOldError:
				resId = R.string.feather_effects_error_update_packs;
				break;

			case PluginTooNewError:
				resId = R.string.feather_effects_error_update_editors;
				break;

			case PluginNotLoadedError:
			case PluginLoadError:
			case MethodNotFoundError:
			default:
				break;
		}

		return getContext().getBaseContext().getString( resId );
	}

	@Override
	public void onActivate() {
		super.onActivate();

		ConfigService config = getContext().getService( ConfigService.class );

		mFilterCellWidth = config.getDimensionPixelSize( R.dimen.feather_effects_cell_width );
		mFilterCellWidth = (int) ( ( Constants.SCREEN_WIDTH / UIUtils.getScreenOptimalColumnsPixels( mFilterCellWidth ) ) );

		mThumbBitmap = generateThumbnail( mBitmap, (int) ( mFilterCellWidth * 0.9 ), (int) ( mFilterCellWidth * 0.9 ) );

		mInstalledPackages = Collections.synchronizedList( new ArrayList<String>() );

		List<EffectPack> data = Collections.synchronizedList( new ArrayList<EffectPack>() );

		mListAdapter = new EffectsAdapter( getContext().getBaseContext(), R.layout.feather_effect_thumb,
				R.layout.feather_getmore_thumb, R.layout.feather_getmore_thumb_inverted, data );
		mHList.setAdapter( mListAdapter );

		mLogger.info( "[plugin] onActivate" );
		// register for plugins updates
		mPluginService.registerOnUpdateListener( this );
		updateInstalledPacks( true );

		// register for swipe gestures
		// mSwipeView.setOnSwipeListener( this );

		mRoundedBordersPixelSize = config.getDimensionPixelSize( R.dimen.feather_effects_panel_thumb_rounded_border );
		mRoundedBordersPaddingPixelSize = config.getDimensionPixelSize( R.dimen.feather_effects_panel_thumb_padding );
		mShadowOffsetPixelSize = config.getDimensionPixelSize( R.dimen.feather_effects_panel_thumb_shadow_offset );
		mShadowRadiusPixelSize = config.getDimensionPixelSize( R.dimen.feather_effects_panel_thumb_shadow_radius );
		mRoundedBordersStrokePixelSize = config.getDimensionPixelSize( R.dimen.feather_effects_panel_thumb_stroke_size );

		mHList.setEdgeHeight( config.getDimensionPixelSize( R.dimen.feather_effects_panel_top_bg_height ) );
		mHList.setEdgeGravityY( Gravity.BOTTOM );

		mImageManager.setOnLoadCompleteListener( this );

		getContentView().setVisibility( View.VISIBLE );
		contentReady();
	}

	@Override
	public void onDeactivate() {
		onProgressEnd();
		mPluginService.removeOnUpdateListener( this );
		mImageManager.setOnLoadCompleteListener( null );
		// mSwipeView.setOnSwipeListener( null );
		super.onDeactivate();
	}

	@Override
	public void onConfigurationChanged( Configuration newConfig, Configuration oldConfig ) {
		mImageManager.clearCache();
		super.onConfigurationChanged( newConfig, oldConfig );
	}

	@Override
	protected void onDispose() {

		if ( null != mImageManager ) {
			mImageManager.clearCache();
			mImageManager.shutDownNow();
		}

		if ( mThumbBitmap != null && !mThumbBitmap.isRecycled() ) {
			mThumbBitmap.recycle();
		}
		mThumbBitmap = null;

		if ( mSmallPreview != null && !mSmallPreview.isRecycled() ) {
			mSmallPreview.recycle();
		}
		mSmallPreview = null;

		if ( null != updateArrowBitmap ) {
			updateArrowBitmap.recycle();
		}
		updateArrowBitmap = null;

		super.onDispose();
	}

	@Override
	protected void onGenerateResult() {
		if ( mIsRendering ) {
			GenerateResultTask task = new GenerateResultTask();
			task.execute();
		} else {
			onComplete( mPreview, mActions );
		}
	}

	@Override
	protected void onProgressEnd() {
		if ( !mEnableFastPreview ) {
			super.onProgressModalEnd();
		} else {
			super.onProgressEnd();
		}
	}

	@Override
	protected void onProgressStart() {
		if ( !mEnableFastPreview ) {
			super.onProgressModalStart();
		} else {
			super.onProgressStart();
		}
	}

	@Override
	public boolean onBackPressed() {
		if ( backHandled() ) return true;
		return super.onBackPressed();
	}

	@Override
	public void onCancelled() {
		killCurrentTask();
		mIsRendering = false;
		super.onCancelled();
	}

	@Override
	public boolean getIsChanged() {
		return super.getIsChanged() || mIsRendering == true;
	}

	@Override
	public void onSwipe( boolean leftToRight ) {
		// TODO: implement this
	}

	@Override
	public void onUpdate( Bundle delta ) {
		if ( isActive() && mExternalPacksEnabled ) {
			if ( mUpdateDialog != null && mUpdateDialog.isShowing() ) {
				// another update alert is showing, skip new alerts
				return;
			}

			if ( validDelta( delta ) ) {

				DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {

					@Override
					public void onClick( DialogInterface dialog, int which ) {
						updateInstalledPacks( true );
					}
				};

				mUpdateDialog = new AlertDialog.Builder( getContext().getBaseContext() ).setMessage( R.string.filter_pack_updated )
						.setNeutralButton( android.R.string.ok, listener ).setCancelable( false ).create();

				mUpdateDialog.show();

			}
		}
	}

	@Override
	public void onLoadComplete( final ImageView view, Bitmap bitmap ) {
		if( !isActive() ) return;
		
		view.setImageBitmap( bitmap );
		
		if ( null != view && view.getParent() != null ) {
			
			View parent = (View)view.getParent();
			
			if( mEnableEffectAnimation ) {
				if( mHList.getScrollX() == 0 ) {
					if( parent.getLeft() < mHList.getRight() ){
						ScaleAnimation anim = new ScaleAnimation( 0, 1, 0, 1, Animation.RELATIVE_TO_SELF, (float) 0.5, Animation.RELATIVE_TO_SELF, (float) 0.5 );
						anim.setAnimationListener( new AnimationListener() {
							
							@Override
							public void onAnimationStart( Animation animation ) {
								view.setVisibility( View.VISIBLE );
							}
							
							@Override
							public void onAnimationRepeat( Animation animation ) {
							}
							
							@Override
							public void onAnimationEnd( Animation animation ) {
							}
						} );
						
						anim.setDuration( 100 );
						anim.setStartOffset( mHList.getScreenPositionForView( view ) * 100 );
						view.startAnimation( anim );
						view.setVisibility( View.INVISIBLE );
						return;
					}
				}
			}
			
			view.setVisibility( View.VISIBLE );
		}
	}

	/**
	 * bundle contains a list of all updates applications. if one meets the criteria ( is a filter apk ) then return true
	 * 
	 * @param bundle
	 *           the bundle
	 * @return true if bundle contains a valid filter package
	 */
	private boolean validDelta( Bundle bundle ) {
		if ( null != bundle ) {
			if ( bundle.containsKey( "delta" ) ) {
				try {
					@SuppressWarnings("unchecked")
					ArrayList<UpdateType> updates = (ArrayList<UpdateType>) bundle.getSerializable( "delta" );
					if ( null != updates ) {
						for ( UpdateType update : updates ) {
							if ( FeatherIntent.PluginType.isFilter( update.getPluginType() ) ) {
								return true;
							}

							if ( FeatherIntent.ACTION_PLUGIN_REMOVED.equals( update.getAction() ) ) {
								// if it's removed check against current listed packs
								if ( mInstalledPackages.contains( update.getPackageName() ) ) {
									return true;
								}
							}
						}
						return false;
					}
				} catch ( ClassCastException e ) {
					return true;
				}
			}
		}
		return true;
	}

	@Override
	public View makeView() {
		ImageViewTouch view = new ImageViewTouch( getContext().getBaseContext(), null );
		view.setBackgroundColor( 0x00000000 );
		view.setDoubleTapEnabled( false );

		if ( mEnableFastPreview ) {
			view.setScrollEnabled( false );
			view.setScaleEnabled( false );
		}
		view.setLayoutParams( new ImageSwitcher.LayoutParams( LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT ) );
		return view;
	}

	@Override
	protected View generateContentView( LayoutInflater inflater ) {
		mEnableFastPreview = Constants.getFastPreviewEnabled();
		return inflater.inflate( R.layout.feather_native_effects_content, null );
	}

	@Override
	protected ViewGroup generateOptionView( LayoutInflater inflater, ViewGroup parent ) {
		return (ViewGroup) inflater.inflate( R.layout.feather_effects_panel, parent, false );
	}

	@Override
	public Matrix getContentDisplayMatrix() {
		return null;
	}

	protected Bitmap generateThumbnail( Bitmap input, final int width, final int height ) {
		return ThumbnailUtils.extractThumbnail( input, width, height );
	}

	/**
	 * Update the installed plugins
	 */
	private void updateInstalledPacks( boolean invalidateList ) {
		mIsAnimating = true;
		// List of installed plugins available on the device
		FeatherInternalPack installedPacks[];
		FeatherPack availablePacks[];

		if ( mExternalPacksEnabled ) {
			installedPacks = mPluginService.getInstalled( getContext().getBaseContext(), mType );
			availablePacks = mPluginService.getAvailable( mType );
		} else {
			installedPacks = new FeatherInternalPack[] { FeatherInternalPack.getDefault( getContext().getBaseContext() ) };
			availablePacks = new FeatherExternalPack[] {};
		}

		// List of the available plugins online
		mAvailablePacks = availablePacks.length;

		// now try to install every plugin...
		if ( invalidateList ) {
			mListAdapter.clear();
			mHList.setVisibility( View.INVISIBLE );
			getOptionView().findViewById( R.id.layout_loader ).setVisibility( View.VISIBLE );
		}
		new PluginInstallTask().execute( installedPacks );
	}

	private void onEffectListUpdated( List<EffectPack> result, List<EffectPackError> mErrors ) {

		// we had errors during installation
		if ( null != mErrors && mErrors.size() > 0 ) {
			if ( !mUpdateErrorHandled ) {
				handleErrors( mErrors );
			}
		}

		if ( mSelectedPosition != FIRST_POSITION ) {
			setSelectedEffect( mHList.getItemAt( FIRST_POSITION ), FIRST_POSITION );
		}

		mHList.setVisibility( View.VISIBLE );
		getOptionView().findViewById( R.id.layout_loader ).setVisibility( View.GONE );
	}

	private void handleErrors( List<EffectPackError> mErrors ) {

		if ( mErrors == null || mErrors.size() < 1 ) return;

		// first get the total number of errors

		HashMap<PluginError, String> hash = new HashMap<PluginError, String>();

		for ( EffectPackError item : mErrors ) {
			PluginError error = item.mError;
			hash.put( error, (String) item.mPackageName );
		}

		// now manage the different cases
		// 1. just one type of error
		if ( hash.size() == 1 ) {

			// get the first error
			EffectPackError item = mErrors.get( 0 );

			if ( mErrors.size() == 1 ) {
				showUpdateAlert( item.mPackageName, item.mError, false );
			} else {
				showUpdateAlertMultiplePlugins( item.mError, false );
			}
		} else {
			// 2. here we must handle different errors type
			showUpdateAlertMultipleItems( getContext().getBaseContext().getPackageName(), hash.keySet() );
		}

		mUpdateErrorHandled = true;
	}


	void setSelectedEffect( View view, int position ) {
		String label = "original";
		mSelectedPosition = position;

		if ( mSelectedEffectView != null && mSelectedEffectView.isSelected() && !mSelectedEffectView.equals( view ) ) {
			mSelectedEffectView.setSelected( false );
		}
		mSelectedEffectView = null;

		if ( null != view ) {
			mSelectedEffectView = view;
			mSelectedEffectView.setSelected( true );
		}

		if ( mHList.getAdapter() != null ) {
			EffectPack item = ( (EffectsAdapter) mHList.getAdapter() ).getItem( position );
			if ( null != item && item.mStatus == PluginError.NoError ) {
				label = (String) item.getItemAt( position );
				renderEffect( label );
			}
		}
		mHList.requestLayout();
	}

	private void renderEffect( String label ) {
		killCurrentTask();
		mCurrentTask = new RenderTask( label );
		mCurrentTask.execute();
	}

	boolean killCurrentTask() {
		if ( mCurrentTask != null ) {
			onProgressEnd();
			return mCurrentTask.cancel( true );
		}
		return false;
	}

	protected INativeFiler loadNativeFilter( String label ) {
		switch ( mType ) {
			case FeatherIntent.PluginType.TYPE_FILTER:
				EffectFilter filter = (EffectFilter) FilterLoaderFactory.get( Filters.EFFECTS );
				filter.setEffectName( label );
				return filter;

				// case FeatherIntent.PluginType.TYPE_BORDER:
				// BorderFilter filter = (BorderFilter) mFilterService.load( Filters.BORDERS );
				// filter.setBorderName( label );
				// return filter;

			default:
				return null;

		}
	}

	protected void trackPackage( String packageName ) {

		if ( !mPrefService.containsValue( "plugin." + mType + "." + packageName ) ) {
			if ( !getContext().getBaseContext().getPackageName().equals( packageName ) ) {

				mPrefService.putString( "plugin." + mType + "." + packageName, packageName );
				HashMap<String, String> map = new HashMap<String, String>();

				if ( mType == FeatherIntent.PluginType.TYPE_FILTER ) {
					map.put( "assetType", "effects" );
				} else if ( mType == FeatherIntent.PluginType.TYPE_BORDER ) {
					map.put( "assetType", "borders" );
				} else if ( mType == FeatherIntent.PluginType.TYPE_STICKER ) {
					map.put( "assetType", "stickers" );
				} else {
					map.put( "assetType", "tools" );
				}
				map.put( "assetID", packageName );

				Tracker.recordTag( "content: purchased", map );
			}
		}

		mTrackingAttributes.put( "packName", packageName );
	}

	boolean backHandled() {
		if ( mIsAnimating ) return true;
		killCurrentTask();
		return false;
	}

	static class ViewHolder {

		TextView text;
		ImageView image;
	}

	class EffectsAdapter extends ArrayAdapterExtended<EffectPack> {

		private int mLayoutResId;
		private int mAltLayoutResId;
		private int mAltLayout2ResId;
		private int mCount = -1;
		private List<EffectPack> mData;
		private LayoutInflater mLayoutInflater;

		static final int TYPE_GET_MORE_FIRST = 0;
		static final int TYPE_GET_MORE_LAST = 1;
		static final int TYPE_NORMAL = 2;

		public EffectsAdapter( Context context, int textViewResourceId, int altViewResourceId, int altViewResource2Id,
				List<EffectPack> objects ) {
			super( context, textViewResourceId, objects );
			mLayoutResId = textViewResourceId;
			mAltLayoutResId = altViewResourceId;
			mAltLayout2ResId = altViewResource2Id;
			mData = objects;
			mLayoutInflater = UIUtils.getLayoutInflater();
		}

		@Override
		public int getCount() {
			if ( mCount == -1 ) {
				int total = 0; // first get more
				for ( EffectPack pack : mData ) {
					if ( null == pack ) {
						total++;
						continue;
					}
					pack.setIndex( total );
					total += pack.size;
				}
				// return total;
				mCount = total;
			}
			return mCount;
		}

		@Override
		public void notifyDataSetChanged() {
			mCount = -1;
			super.notifyDataSetChanged();
		}

		@Override
		public void notifyDataSetAdded() {
			mCount = -1;
			super.notifyDataSetAdded();
		}

		@Override
		public void notifyDataSetRemoved() {
			mCount = -1;
			super.notifyDataSetRemoved();
		}

		@Override
		public void notifyDataSetInvalidated() {
			mCount = -1;
			super.notifyDataSetInvalidated();
		}

		@Override
		public int getViewTypeCount() {
			return 3;
		}

		@Override
		public int getItemViewType( int position ) {

			if ( !mExternalPacksEnabled ) return TYPE_NORMAL;

			EffectPack item = getItem( position );
			if ( null == item ) {
				if ( position == 0 )
					return TYPE_GET_MORE_FIRST;
				else
					return TYPE_GET_MORE_LAST;
			}
			return TYPE_NORMAL;
		}

		@Override
		public EffectPack getItem( int position ) {
			for ( int i = 0; i < mData.size(); i++ ) {
				EffectPack pack = mData.get( i );
				if ( null == pack ) continue;

				if ( position >= pack.index && position < pack.index + pack.size ) {
					return pack;
				}
			}
			return null;
		}

		@Override
		public View getView( final int position, final View convertView, final ViewGroup parent ) {

			View view;
			ViewHolder holder = null;
			int type = getItemViewType( position );

			if ( convertView == null ) {

				if ( type == TYPE_GET_MORE_FIRST ) {
					view = mLayoutInflater.inflate( mAltLayoutResId, parent, false );
				} else if ( type == TYPE_GET_MORE_LAST ) {
					view = mLayoutInflater.inflate( mAltLayout2ResId, parent, false );
				} else {
					view = mLayoutInflater.inflate( mLayoutResId, parent, false );
					holder = new ViewHolder();
					holder.text = (TextView) view.findViewById( R.id.text );
					holder.image = (ImageView) view.findViewById( R.id.image );
					view.setTag( holder );
				}
				view.setLayoutParams( new EffectThumbLayout.LayoutParams( mFilterCellWidth, EffectThumbLayout.LayoutParams.MATCH_PARENT ) );
			} else {
				view = convertView;
				holder = (ViewHolder) view.getTag();

			}

			if ( type == TYPE_NORMAL ) {
				EffectPack item = getItem( position );
				holder.text.setText( item.getLabelAt( position ) );
				holder.image.setImageBitmap( mThumbBitmap );

				final String effectName = (String) item.getItemAt( position );
				boolean selected = mSelectedPosition == position;

				ThumbnailCallable executor = new ThumbnailCallable( (EffectFilter) loadNativeFilter( effectName ), effectName,
						mThumbBitmap, item.mStatus == PluginError.NoError, updateArrowBitmap );
				mImageManager.execute( executor, item.index + "/" + effectName, holder.image );
				// holder.image.setImageResource( R.drawable.test_thumb );

				view.setSelected( selected );
				if ( selected ) {
					mSelectedEffectView = view;
				}
			} else {
				// get more
				TextView totalText = (TextView) view.findViewById( R.id.text01 );
				totalText.setText( String.valueOf( mAvailablePacks ) );
				( (ViewGroup) totalText.getParent() ).setVisibility( mAvailablePacks > 0 ? View.VISIBLE : View.INVISIBLE );
			}

			return view;
		}
	}

	/**
	 * Render the passed effect in a thumbnail
	 * 
	 * @author alessandro
	 * 
	 */
	static class ThumbnailCallable extends AsyncImageManager.MyCallable {

		EffectFilter mFilter;
		String mEffectName;
		Bitmap srcBitmap;
		Bitmap invalidBitmap;
		boolean isValid;

		public ThumbnailCallable( EffectFilter filter, String effectName, Bitmap bitmap, boolean valid, Bitmap invalid_bitmap ) {
			mEffectName = effectName;
			mFilter = filter;
			srcBitmap = bitmap;
			isValid = valid;
			invalidBitmap = invalid_bitmap;
		}

		@Override
		public Bitmap call() throws Exception {

			mFilter.setBorders( false );

			MoaAction action = MoaActionFactory.action( "ext-roundedborders" );
			action.setValue( "padding", mRoundedBordersPaddingPixelSize );
			action.setValue( "roundPx", mRoundedBordersPixelSize );
			action.setValue( "strokeColor", new MoaColorParameter( 0xffa6a6a6 ) );
			action.setValue( "strokeWeight", mRoundedBordersStrokePixelSize );

			if ( !isValid ) {
				action.setValue( "overlaycolor", new MoaColorParameter( 0x99000000 ) );
			}

			mFilter.getActions().add( action );

			// shadow
			action = MoaActionFactory.action( "ext-roundedshadow" );
			action.setValue( "color", 0x99000000 );
			action.setValue( "radius", mShadowRadiusPixelSize );
			action.setValue( "roundPx", mRoundedBordersPixelSize );
			action.setValue( "offsetx", mShadowOffsetPixelSize );
			action.setValue( "offsety", mShadowOffsetPixelSize );
			action.setValue( "padding", mRoundedBordersPaddingPixelSize );
			mFilter.getActions().add( action );

			Bitmap result = mFilter.execute( srcBitmap, null, 1, 1 );

			if ( !isValid ) {
				addUpdateArrow( result );
			}
			return result;
		}

		void addUpdateArrow( Bitmap bitmap ) {

			if ( null != invalidBitmap && !invalidBitmap.isRecycled() ) {

				final double w = Math.floor( bitmap.getWidth() * 0.75 );
				final double h = Math.floor( bitmap.getHeight() * 0.75 );

				final int paddingx = (int) ( bitmap.getWidth() - w ) / 2;
				final int paddingy = (int) ( bitmap.getHeight() - h ) / 2;

				Paint paint = new Paint( Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG );
				Rect src = new Rect( 0, 0, invalidBitmap.getWidth(), invalidBitmap.getHeight() );
				Rect dst = new Rect( paddingx, paddingy, paddingx + (int) w, paddingy + (int) h );
				Canvas canvas = new Canvas( bitmap );
				canvas.drawBitmap( invalidBitmap, src, dst, paint );
			}
		}
	}

	/**
	 * Install all the
	 * 
	 * @author alessandro
	 * 
	 */
	class PluginInstallTask extends AsyncTask<FeatherInternalPack[], Void, List<EffectPack>> {

		List<EffectPackError> mErrors;
		private PluginService mEffectsService;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			mEffectsService = getContext().getService( PluginService.class );
			mErrors = Collections.synchronizedList( new ArrayList<EffectPackError>() );
			mImageManager.clearCache();
		}

		@Override
		protected List<EffectPack> doInBackground( FeatherInternalPack[]... params ) {

			List<EffectPack> result = Collections.synchronizedList( new ArrayList<EffectPack>() );
			mInstalledPackages.clear();

			if ( params[0] != null && params[0].length > 0 ) {
				if ( mExternalPacksEnabled ) {
					addItemToList( null );
				}
			}

			for ( FeatherPack pack : params[0] ) {
				if ( pack instanceof FeatherInternalPack ) {
					InternalPlugin plugin = (InternalPlugin) PluginManager.create( getContext().getBaseContext(), pack );
					PluginError status;
						if ( plugin.isExternal() ) {
							status = installPlugin( plugin.getPackageName(), plugin.getPluginType() );

							if ( status != PluginError.NoError ) {
								EffectPackError error = new EffectPackError( plugin.getPackageName(), plugin.getLabel( mType ), status );
								mErrors.add( error );
								// continue;
							}
						} else {
							status = PluginError.NoError;
						}

						CharSequence[] filters = listPackItems( plugin );
						CharSequence[] labels = listPackLabels( plugin, filters );
						CharSequence title = plugin.getLabel( mType );

						final EffectPack effectPack = new EffectPack( plugin.getPackageName(), title, filters, labels, status );
						
						if( plugin.isExternal() ){
							trackPackage( plugin.getPackageName() );
						}
						mInstalledPackages.add( plugin.getPackageName() );

						if ( isActive() ) {
							addItemToList( effectPack );
							result.add( effectPack );
						}
					}
			}

			if ( params[0] != null && params[0].length > 0 ) {

				if ( mExternalPacksEnabled ) {
					addItemToList( null );
				}
			}

			return result;
		}

		private void addItemToList( final EffectPack pack ) {
			if ( isActive() ) {
				getHandler().post( new Runnable() {

					@Override
					public void run() {
						mListAdapter.add( pack );
					}
				} );
			}
		}

		@Override
		protected void onPostExecute( List<EffectPack> result ) {
			super.onPostExecute( result );

			onEffectListUpdated( result, mErrors );
			mIsAnimating = false;
		}

		private CharSequence[] listPackItems( InternalPlugin plugin ) {
			if ( mType == FeatherIntent.PluginType.TYPE_FILTER ) {
				return plugin.listFilters();
			} else if ( mType == FeatherIntent.PluginType.TYPE_BORDER ) {
				return plugin.listBorders();
			}
			return null;
		}

		private CharSequence[] listPackLabels( InternalPlugin plugin, CharSequence[] items ) {
			CharSequence[] labels = new String[items.length];
			for ( int i = 0; i < items.length; i++ ) {
				if ( mType == FeatherIntent.PluginType.TYPE_FILTER ) {
					labels[i] = plugin.getFilterLabel( items[i] );
				} else if ( mType == FeatherIntent.PluginType.TYPE_BORDER ) {
					labels[i] = plugin.getBorderLabel( items[i] );
				}
			}
			return labels;
		}

		private PluginError installPlugin( final String packagename, final int pluginType ) {
			if ( mEffectsService.installed( packagename ) ) {
				return PluginError.NoError;
			}
			return mEffectsService.install( getContext().getBaseContext(), packagename, pluginType );
		}
	}

	class EffectPackError {

		CharSequence mPackageName;
		CharSequence mLabel;
		PluginError mError;

		public EffectPackError( CharSequence packagename, CharSequence label, PluginError error ) {
			mPackageName = packagename;
			mLabel = label;
			mError = error;
		}
	}

	class EffectPack {

		CharSequence mPackageName;
		CharSequence[] mValues;
		CharSequence[] mLabels;
		CharSequence mTitle;
		PluginError mStatus;
		int size = 0;
		int index = 0;

		public EffectPack( CharSequence packageName, CharSequence pakageTitle, CharSequence[] filters, CharSequence[] labels,
				PluginError status ) {
			mPackageName = packageName;
			mValues = filters;
			mLabels = labels;
			mStatus = status;
			mTitle = pakageTitle;

			if ( null != filters ) {
				size = filters.length;
			}
		}

		public int getCount() {
			return size;
		}

		public int getIndex() {
			return index;
		}

		public void setIndex( int value ) {
			index = value;
		}

		public CharSequence getItemAt( int position ) {
			return mValues[position - index];
		}

		public CharSequence getLabelAt( int position ) {
			return mLabels[position - index];
		}
	}

	/**
	 * Render the selected effect
	 */
	private class RenderTask extends UserTask<Void, Bitmap, Bitmap> implements OnCancelListener {

		String mError;
		String mEffect;
		MoaResult mNativeResult;
		MoaResult mSmallNativeResult;

		/**
		 * Instantiates a new render task.
		 * 
		 * @param tag
		 *           the tag
		 */
		public RenderTask( final String tag ) {
			mEffect = tag;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.aviary.android.feather.library.utils.UserTask#onPreExecute()
		 */
		@Override
		public void onPreExecute() {
			super.onPreExecute();

			final NativeFilter filter = (NativeFilter) loadNativeFilter( mEffect );

			if ( mType == FeatherIntent.PluginType.TYPE_FILTER ) {
				// activate borders ?
				((EffectFilter) filter ).setBorders( Constants.getValueFromIntent( Constants.EXTRA_EFFECTS_BORDERS_ENABLED, true ) );
			}

			try {
				mNativeResult = filter.prepare( mBitmap, mPreview, 1, 1 );
				mActions = (MoaActionList) filter.getActions().clone();
			} catch ( JSONException e ) {
				mLogger.error( e.toString() );
				e.printStackTrace();
				mNativeResult = null;
				return;
			}

			if ( mNativeResult == null ) return;

			onProgressStart();

			if ( !mEnableFastPreview ) {
				// use the standard system modal progress dialog
				// to render the effect
			} else {
				try {
					mSmallNativeResult = filter.prepare( mBitmap, mSmallPreview, mSmallPreview.getWidth(), mSmallPreview.getHeight() );
				} catch ( JSONException e ) {
					e.printStackTrace();
				}
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.aviary.android.feather.library.utils.UserTask#doInBackground(Params[])
		 */
		@Override
		public Bitmap doInBackground( final Void... params ) {

			if ( isCancelled() ) return null;
			if ( mNativeResult == null ) return null;

			mIsRendering = true;

			// rendering the small preview
			if ( mEnableFastPreview && mSmallNativeResult != null ) {
				mSmallNativeResult.execute();
				if ( mSmallNativeResult.active > 0 ) {
					publishProgress( mSmallNativeResult.outputBitmap );
				}
			}

			if ( isCancelled() ) return null;

			long t1, t2;

			// rendering the full preview
			try {
				t1 = System.currentTimeMillis();
				mNativeResult.execute();
				t2 = System.currentTimeMillis();
			} catch ( Exception exception ) {
				mLogger.error( exception.getMessage() );
				mError = exception.getMessage();
				exception.printStackTrace();
				return null;
			}

			if ( null != mTrackingAttributes ) {
				mTrackingAttributes.put( "filterName", mEffect );
				mTrackingAttributes.put( "renderTime", Long.toString( t2 - t1 ) );
			}

			mLogger.log( "	complete. isCancelled? " + isCancelled(), mEffect );

			if ( !isCancelled() ) {
				return mNativeResult.outputBitmap;
			} else {
				return null;
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.aviary.android.feather.library.utils.UserTask#onProgressUpdate(Progress[])
		 */
		@Override
		public void onProgressUpdate( Bitmap... values ) {
			super.onProgressUpdate( values );

			// we're using a FakeBitmapDrawable just to upscale the small bitmap
			// to be rendered the same way as the full image
			final FakeBitmapDrawable drawable = new FakeBitmapDrawable( values[0], mBitmap.getWidth(), mBitmap.getHeight() );
			mImageSwitcher.setImageDrawable( drawable, true, null, Float.MAX_VALUE );
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.aviary.android.feather.library.utils.UserTask#onPostExecute(java.lang.Object)
		 */
		@Override
		public void onPostExecute( final Bitmap result ) {
			super.onPostExecute( result );

			if ( !isActive() ) return;

			mPreview = result;

			if ( result == null || mNativeResult == null || mNativeResult.active == 0 ) {
				// restore the original bitmap...
				mImageSwitcher.setImageBitmap( mBitmap, false, null, Float.MAX_VALUE );

				if ( mError != null ) {
					onGenericError( mError );
				}
				setIsChanged( false );
				mActions = null;

			} else {
				if ( SystemUtils.isHoneyComb() ) {
					Moa.notifyPixelsChanged( result );
				}
				mImageSwitcher.setImageBitmap( result, true, null, Float.MAX_VALUE );
				setIsChanged( true );
			}

			onProgressEnd();

			mIsRendering = false;
			mCurrentTask = null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.aviary.android.feather.library.utils.UserTask#onCancelled()
		 */
		@Override
		public void onCancelled() {
			super.onCancelled();

			if ( mNativeResult != null ) {
				mNativeResult.cancel();
			}

			if ( mSmallNativeResult != null ) {
				mSmallNativeResult.cancel();
			}

			mIsRendering = false;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.content.DialogInterface.OnCancelListener#onCancel(android.content.DialogInterface)
		 */
		@Override
		public void onCancel( DialogInterface dialog ) {
			cancel( true );
		}
	}

	/**
	 * Used to generate the Bitmap result. If user clicks on the "Apply" button when an effect is still rendering, then starts this
	 * task.
	 */
	class GenerateResultTask extends AsyncTask<Void, Void, Void> {

		ProgressDialog mProgress = new ProgressDialog( getContext().getBaseContext() );

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mProgress.setTitle( getContext().getBaseContext().getString( R.string.feather_loading_title ) );
			mProgress.setMessage( getContext().getBaseContext().getString( R.string.effect_loading_message ) );
			mProgress.setIndeterminate( true );
			mProgress.setCancelable( false );
			mProgress.show();
		}

		@Override
		protected Void doInBackground( Void... params ) {

			mLogger.info( "GenerateResultTask::doInBackground", mIsRendering );

			while ( mIsRendering ) {
				// mLogger.log( "waiting...." );
			}

			return null;
		}

		@Override
		protected void onPostExecute( Void result ) {
			super.onPostExecute( result );

			mLogger.info( "GenerateResultTask::onPostExecute" );

			if ( getContext().getBaseActivity().isFinishing() ) return;
			if ( mProgress.isShowing() ) mProgress.dismiss();

			onComplete( mPreview, mActions );
		}
	}

}
