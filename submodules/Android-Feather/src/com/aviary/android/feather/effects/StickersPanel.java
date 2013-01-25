package com.aviary.android.feather.effects;

import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver.OnScrollChangedListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;
import com.aviary.android.feather.Constants;
import com.aviary.android.feather.FilterManager.FeatherContext;
import com.aviary.android.feather.R;
import com.aviary.android.feather.async_tasks.AsyncImageManager;
import com.aviary.android.feather.effects.BordersPanel.ViewHolder;
import com.aviary.android.feather.effects.SimpleStatusMachine.OnStatusChangeListener;
import com.aviary.android.feather.graphics.PluginDividerDrawable;
import com.aviary.android.feather.graphics.RepeatableHorizontalDrawable;
import com.aviary.android.feather.library.content.FeatherIntent;
import com.aviary.android.feather.library.content.FeatherIntent.PluginType;
import com.aviary.android.feather.library.filters.StickerFilter;
import com.aviary.android.feather.library.graphics.drawable.FeatherDrawable;
import com.aviary.android.feather.library.graphics.drawable.StickerDrawable;
import com.aviary.android.feather.library.moa.MoaActionFactory;
import com.aviary.android.feather.library.moa.MoaActionList;
import com.aviary.android.feather.library.plugins.FeatherExternalPack;
import com.aviary.android.feather.library.plugins.FeatherInternalPack;
import com.aviary.android.feather.library.plugins.FeatherPack;
import com.aviary.android.feather.library.plugins.PluginManager;
import com.aviary.android.feather.library.plugins.PluginManager.ExternalPlugin;
import com.aviary.android.feather.library.plugins.PluginManager.IPlugin;
import com.aviary.android.feather.library.plugins.PluginManager.InternalPlugin;
import com.aviary.android.feather.library.plugins.UpdateType;
import com.aviary.android.feather.library.services.ConfigService;
import com.aviary.android.feather.library.services.DragControllerService;
import com.aviary.android.feather.library.services.DragControllerService.DragListener;
import com.aviary.android.feather.library.services.DragControllerService.DragSource;
import com.aviary.android.feather.library.services.EffectContext;
import com.aviary.android.feather.library.services.ImageCacheService;
import com.aviary.android.feather.library.services.ImageCacheService.SimpleCachedRemoteBitmap;
import com.aviary.android.feather.library.services.PluginService;
import com.aviary.android.feather.library.services.PluginService.OnUpdateListener;
import com.aviary.android.feather.library.services.PluginService.PluginError;
import com.aviary.android.feather.library.services.PluginService.StickerType;
import com.aviary.android.feather.library.services.PreferenceService;
import com.aviary.android.feather.library.services.drag.DragView;
import com.aviary.android.feather.library.services.drag.DropTarget;
import com.aviary.android.feather.library.services.drag.DropTarget.DropTargetListener;
import com.aviary.android.feather.library.tracking.Tracker;
import com.aviary.android.feather.library.utils.BitmapUtils;
import com.aviary.android.feather.library.utils.IOUtils;
import com.aviary.android.feather.library.utils.ImageLoader;
import com.aviary.android.feather.library.utils.MatrixUtils;
import com.aviary.android.feather.library.utils.PackageManagerUtils;
import com.aviary.android.feather.library.utils.UIConfiguration;
import com.aviary.android.feather.utils.UIUtils;
import com.aviary.android.feather.widget.ArrayAdapterExtended;
import com.aviary.android.feather.widget.DrawableHighlightView;
import com.aviary.android.feather.widget.DrawableHighlightView.OnDeleteClickListener;
import com.aviary.android.feather.widget.EffectThumbLayout;
import com.aviary.android.feather.widget.HorizontalFixedListView.OnItemDragListener;
import com.aviary.android.feather.widget.HorizontalVariableListView;
import com.aviary.android.feather.widget.HorizontalVariableListView.OnItemClickedListener;
import com.aviary.android.feather.widget.IapDialog;
import com.aviary.android.feather.widget.IapDialog.OnCloseListener;
import com.aviary.android.feather.widget.IapNotificationLayout;
import com.aviary.android.feather.widget.ImageViewDrawableOverlay;

public class StickersPanel extends AbstractContentPanel implements OnUpdateListener, OnStatusChangeListener, OnScrollChangedListener, OnItemClickedListener, DragListener, DragSource, DropTargetListener, OnItemSelectedListener {

	private static final int STATUS_NULL = SimpleStatusMachine.INVALID_STATUS;
	private static final int STATUS_PACKS = 1;
	private static final int STATUS_STICKERS = 2;
	private static final int STATUS_IAP = 3;

	private static final int THUMBNAIL_INSET = 8;

	/** panel's status */
	private SimpleStatusMachine mStatus;

	/** This panel is executing some animations */
	private volatile boolean mIsAnimating;

	/** horizontal listview for stickers packs */
	private HorizontalVariableListView mListPacks;

	/** horizontal listview for stickers items */
	private HorizontalVariableListView mListStickers;

	/** view flipper for switching between lists */
	private ViewFlipper mViewFlipper;

	/** external packs availability */
	private boolean mExternalPacksEnabled;

	/** dialog used to alert the user about changes in the installed plugins */
	private AlertDialog mUpdateDialog;

	/** thumbnail cache manager */
	private AsyncImageManager mImageManager;

	/** canvas used to draw stickers */
	private Canvas mCanvas;

	/** default width of rolls elements */
	private int mPacksCellWidth;

	/** installed plugins */
	private List<String> mInstalledPackages;

	/** total number of available plugins */
	private int mAvailablePacks = 0;

	/** required services */
	private PluginService mPluginService;
	private ConfigService mConfigService;
	private PreferenceService mPreferenceService;
	private ImageCacheService mCacheService;
	private DragControllerService mDragControllerService;

	/** should display the iap notification popup? */
	private boolean mShowIapNotificationAndValue;

	/** max number of featured elements to display */
	private int mFeaturedCount;

	/** iap dialog for inline previews */
	private IapDialog mIapDialog;

	/** the notification popup */
	private IapNotificationLayout mIapNotificationPopup;

	/** the iap notification already shown */
	private boolean mIapPopupShown;

	/** hlist scrolled */
	private boolean mScrollChanged;

	/** the current selected sticker pack */
	private IPlugin mPlugin;

	private MoaActionList mActionList;
	private StickerFilter mCurrentFilter;

	/** sticker configurations */
	private int mStickerHvEllipse, mStickerHvStrokeWidth, mStickerHvMinSize;
	private int mStickerHvPadding;;
	private ColorStateList mStickerHvStrokeColorStateList;
	private ColorStateList mStickerHvFillColorStateList;

	/** sticker thumbnail for the horizontal list final size */
	private int mThumbSize;
	
	/** default title for featured divider */
	private String mFeaturedDefaultTitle;
	
	private int mItemsGapPixelSize = 4;

	public StickersPanel( EffectContext context ) {
		super( context );
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onCreate( Bitmap bitmap ) {
		super.onCreate( bitmap );

		mStatus = new SimpleStatusMachine();

		// determine if the external packs are enabled
		mExternalPacksEnabled = Constants.getExternalStickersEnabled();

		// init layout components
		mListPacks = (HorizontalVariableListView) getOptionView().findViewById( R.id.list_packs );
		mListStickers = (HorizontalVariableListView) getOptionView().findViewById( R.id.list_stickers );
		mViewFlipper = (ViewFlipper) getOptionView().findViewById( R.id.flipper );
		mImageView = (ImageViewDrawableOverlay) getContentView().findViewById( R.id.overlay );

		// init services
		mPluginService = getContext().getService( PluginService.class );
		mConfigService = getContext().getService( ConfigService.class );
		mPreferenceService = getContext().getService( PreferenceService.class );
		mCacheService = getContext().getService( ImageCacheService.class );

		// TODO: only for testing
		// mCacheService.deleteCache();

		// setup the main horizontal listview
		mListPacks.setGravity( Gravity.BOTTOM );
		mListPacks.setOverScrollMode( View.OVER_SCROLL_ALWAYS );
		mListPacks.setEdgeGravityY( Gravity.BOTTOM );

		// setup the stickers listview
		mListStickers.setGravity( Gravity.BOTTOM );
		mListStickers.setOverScrollMode( View.OVER_SCROLL_ALWAYS );
		mListStickers.setEdgeGravityY( Gravity.BOTTOM );

		// setup the main imageview
		( (ImageViewDrawableOverlay) mImageView ).setForceSingleSelection( false );
		( (ImageViewDrawableOverlay) mImageView ).setDropTargetListener( this );
		( (ImageViewDrawableOverlay) mImageView ).setScaleWithContent( true );

		// create the default action list
		mActionList = MoaActionFactory.actionList();

		// load the configuration for the sticker drawable
		mStickerHvEllipse = mConfigService.getInteger( R.integer.feather_sticker_highlight_ellipse );
		mStickerHvStrokeWidth = mConfigService.getInteger( R.integer.feather_sticker_highlight_stroke_width );

		mStickerHvStrokeColorStateList = mConfigService.getColorStateList( R.color.feather_sticker_color_stroke_selector );
		mStickerHvFillColorStateList = mConfigService.getColorStateList( R.color.feather_sticker_color_fill_selector );

		mStickerHvMinSize = mConfigService.getInteger( R.integer.feather_sticker_highlight_minsize );
		mStickerHvPadding = mConfigService.getInteger( R.integer.feather_sticker_highlight_padding );

		mFeaturedCount = mConfigService.getInteger( R.integer.feather_featured_count );
		mFeaturedDefaultTitle = mConfigService.getString( R.string.feather_featured );

		// update the background drawable
		View content = getOptionView().findViewById( R.id.background );
		content.setBackgroundDrawable( RepeatableHorizontalDrawable.createFromView( content ) );

		mImageManager = new AsyncImageManager();

		// create the preview for the main imageview
		createAndConfigurePreview();

		if ( android.os.Build.VERSION.SDK_INT > 8 ) {
			DragControllerService dragger = getContext().getService( DragControllerService.class );
			dragger.addDropTarget( (DropTarget) mImageView );
			dragger.setMoveTarget( mImageView );
			dragger.setDragListener( this );
			// TODO: remember to activate this!
			// dragger.activate();
			setDragController( dragger );
		}
	}

	@Override
	public void onActivate() {
		super.onActivate();

		mImageView.setImageBitmap( mPreview, true, getContext().getCurrentImageViewMatrix(), UIConfiguration.IMAGE_VIEW_MAX_ZOOM );

		//mPacksCellWidth = mConfigService.getDimensionPixelSize( R.dimen.feather_sticker_pack_cell_width );
		//mPacksCellWidth = (int) ( ( Constants.SCREEN_WIDTH / UIUtils.getScreenOptimalColumnsPixels( mPacksCellWidth ) ) );
		
		mPacksCellWidth = (int) ( ( getOptionView().findViewById( R.id.background ).getHeight() - getOptionView().findViewById( R.id.bottom_background_overlay ).getHeight() ) * 0.8 );
		
		mItemsGapPixelSize = mConfigService.getDimensionPixelSize( R.dimen.feather_stickers_panel_items_gap );

		mInstalledPackages = Collections.synchronizedList( new ArrayList<String>() );

		mListPacks.setOnScrollListener( this );
		mListPacks.setOnItemClickedListener( this );
		mListPacks.setOnItemSelectedListener( this );

		// register to status change
		mStatus.setOnStatusChangeListener( this );

		if ( mExternalPacksEnabled ) {
			mPluginService.registerOnUpdateListener( this );
			mStatus.setStatus( STATUS_PACKS );
		} else {
			updateInstalledPacks( true );
		}

		getContentView().setVisibility( View.VISIBLE );
		contentReady();
	}

	@Override
	public boolean onBackPressed() {

		mLogger.info( "onBackPressed" );

		if ( mIsAnimating ) return true;

		if ( mStatus.getCurrentStatus() == STATUS_IAP ) {
			mStatus.setStatus( STATUS_PACKS );
			mListPacks.setSelectedPosition( HorizontalVariableListView.INVALID_POSITION, true );
			return true;
		}

		// we're in the packs status
		if ( mStatus.getCurrentStatus() == STATUS_PACKS ) {
			if ( stickersOnScreen() ) {
				askToLeaveWithoutApply();
				return true;
			}
			return false;
		}

		// we're in the stickers status
		if ( mStatus.getCurrentStatus() == STATUS_STICKERS ) {
			if ( mExternalPacksEnabled ) {
				mStatus.setStatus( STATUS_PACKS );
				if ( null != mPlugin ) {
					Tracker.recordTag( mPlugin.getLabel( FeatherIntent.PluginType.TYPE_STICKER ) + ": Cancelled" );
				}
				return true;
			} else {
				// ok we still have a sticker in there
				if ( stickersOnScreen() ) {
					askToLeaveWithoutApply();
					return true;
				}
				return false;
			}
		}

		return super.onBackPressed();
	}

	@Override
	public boolean onCancel() {

		mLogger.info( "onCancel" );

		// if there's an active sticker on screen
		// then ask if we really want to exit this panel
		// and discard changes
		if ( stickersOnScreen() ) {
			askToLeaveWithoutApply();
			return true;
		}

		return super.onCancel();
	}

	@Override
	public void onDeactivate() {
		super.onDeactivate();

		// disable the drag controller
		if ( null != getDragController() ) {
			getDragController().deactivate();
			getDragController().removeDropTarget( (DropTarget) mImageView );
			getDragController().setDragListener( null );
		}
		setDragController( null );

		mPluginService.removeOnUpdateListener( this );
		mStatus.setOnStatusChangeListener( null );
		mListPacks.setOnScrollListener( null );
		mListPacks.setOnItemClickedListener( null );
		mListPacks.setOnItemSelectedListener( null );
		mListStickers.setOnItemClickedListener( null );
		mListStickers.setOnItemDragListener( null );
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		mCurrentFilter = null;
		mActionList = null;
	}

	@Override
	protected void onDispose() {
		super.onDispose();

		if ( null != mImageManager ) {
			mImageManager.clearCache();
			mImageManager.shutDownNow();
		}

		if ( null != mInstalledPackages ) {
			mInstalledPackages.clear();
		}

		mPlugin = null;
		mCacheService = null;
		mCanvas = null;
	}

	@Override
	protected void onGenerateResult() {
		onApplyCurrent();
		super.onGenerateResult( mActionList );
	}

	@Override
	public void onConfigurationChanged( Configuration newConfig, Configuration oldConfig ) {

		// TODO: To be verified

		super.onConfigurationChanged( newConfig, oldConfig );

		mImageManager.clearCache();

		if ( mStatus.getCurrentStatus() == STATUS_NULL || mStatus.getCurrentStatus() == STATUS_PACKS ) {
			updateInstalledPacks( false );
		} else if ( mStatus.getCurrentStatus() == STATUS_STICKERS ) {
			loadStickers();
		} else if( mStatus.getCurrentStatus() == STATUS_IAP ) {
			if ( mIapDialog != null ) {
				
				ViewGroup parent = (ViewGroup) mIapDialog.getParent();
				
				if( null != parent ) {
					ExternalPlugin currentPlugin = mIapDialog.getPlugin();
					
					int index = parent.indexOfChild( mIapDialog );
					parent.removeView( mIapDialog );
					mIapDialog = (IapDialog) UIUtils.getLayoutInflater().inflate( R.layout.feather_iap_dialog, parent, false );
					mIapDialog.setLayoutAnimation( null );
					parent.addView( mIapDialog, index );
					mIapDialog.setPlugin( currentPlugin, FeatherIntent.PluginType.TYPE_STICKER, getContext().getBaseContext() );
				}
			}
			updateInstalledPacks( false );
		}
	}

	@Override
	protected View generateContentView( LayoutInflater inflater ) {
		return inflater.inflate( R.layout.feather_stickers_content, null );
	}

	@Override
	protected ViewGroup generateOptionView( LayoutInflater inflater, ViewGroup parent ) {
		return (ViewGroup) inflater.inflate( R.layout.feather_stickers2_panel, null );
	}

	// /////////////////////////
	// OnStatusChangeListener //
	// /////////////////////////
	@Override
	public void OnStatusChanged( int oldStatus, int newStatus ) {
		mLogger.info( "OnStatusChange: " + oldStatus + " >> " + newStatus );

		switch ( newStatus ) {
			case STATUS_PACKS:

				// deactivate listeners for the stickers list
				mListStickers.setOnItemClickedListener( null );
				mListStickers.setOnItemDragListener( null );

				if ( oldStatus == STATUS_NULL ) {
					updateInstalledPacks( true );
				} else if ( oldStatus == STATUS_STICKERS ) {
					mViewFlipper.setDisplayedChild( 1 );
					restoreToolbarTitle();

					if ( getDragController() != null ) {
						getDragController().deactivate();
					}

				} else if ( oldStatus == STATUS_IAP ) {
					// only using back button
					mPlugin = null;
					removeIapDialog();
					setApplyEnabled( true );

				}
				break;

			case STATUS_STICKERS:
				if ( oldStatus == STATUS_PACKS ) {
					loadStickers();
				} else if ( oldStatus == STATUS_IAP ) {
					removeIapDialog();
					loadStickers();
					setApplyEnabled( true );
				} else if( oldStatus == STATUS_NULL ) {
					loadStickers();
				}

				setToolbarTitle( mPlugin.getLabel( FeatherIntent.PluginType.TYPE_STICKER ) );

				if ( getDragController() != null ) {
					getDragController().activate();
				}
				break;

			case STATUS_IAP:
				mIapDialog = createIapDialog();
				updateIapDialog();
				setApplyEnabled( false );
				break;

			default:
				mLogger.error( "unmanaged status change: " + oldStatus + " >> " + newStatus );
				onGenericError( "unmanaged status change: " + oldStatus + " >> " + newStatus );
				break;
		}
	}

	@Override
	public void OnStatusUpdated( int status ) {
		mLogger.info( "OnStatusUpdated: " + status );
		switch ( status ) {
			case STATUS_IAP:
				updateIapDialog();
				break;
		}
	}

	// ///////////////////////////
	// OnUpdateListener methods //
	// ///////////////////////////

	@Override
	public void onUpdate( Bundle delta ) {
		mLogger.info( "onUpdate" );

		if ( !isActive() || !mExternalPacksEnabled ) return;

		if ( !validDelta( delta ) ) {
			mLogger.log( "Suppress the alert, no stickers in the delta bundle" );
			return;
		}

		if ( mUpdateDialog != null && mUpdateDialog.isShowing() ) {
			mLogger.log( "dialog is already there, skip new alerts" );
			return;
		}

		final int status = mStatus.getCurrentStatus();
		AlertDialog dialog = null;

		if ( status == STATUS_NULL || status == STATUS_PACKS ) {
			// PACKS
			dialog = new AlertDialog.Builder( getContext().getBaseContext() ).setMessage( R.string.sticker_pack_updated_1 ).setPositiveButton( android.R.string.ok, new DialogInterface.OnClickListener() {

				@Override
				public void onClick( DialogInterface dialog, int which ) {
					updateInstalledPacks( false );
				}
			} ).create();

		} else if ( status == STATUS_STICKERS ) {
			// STICKERS

			if ( stickersOnScreen() ) {

				dialog = new AlertDialog.Builder( getContext().getBaseContext() ).setMessage( R.string.sticker_pack_updated_3 ).setPositiveButton( android.R.string.yes, new DialogInterface.OnClickListener() {

					@Override
					public void onClick( DialogInterface dialog, int which ) {
						onApplyCurrent();
						mStatus.setStatus( STATUS_PACKS );
						updateInstalledPacks( false );
					}
				} ).setNegativeButton( android.R.string.no, new DialogInterface.OnClickListener() {

					@Override
					public void onClick( DialogInterface dialog, int which ) {
						onClearCurrent( true );
						mStatus.setStatus( STATUS_PACKS );
						updateInstalledPacks( false );
					}
				} ).create();

			} else {

				dialog = new AlertDialog.Builder( getContext().getBaseContext() ).setMessage( R.string.sticker_pack_updated_2 ).setPositiveButton( android.R.string.ok, new DialogInterface.OnClickListener() {

					@Override
					public void onClick( DialogInterface dialog, int which ) {
						mStatus.setStatus( STATUS_PACKS );
						updateInstalledPacks( false );
					}
				} ).create();
			}

		} else if ( status == STATUS_IAP ) {
			// IAP
			dialog = new AlertDialog.Builder( getContext().getBaseContext() ).setMessage( R.string.sticker_pack_updated_2 ).setPositiveButton( android.R.string.ok, new DialogInterface.OnClickListener() {

				@Override
				public void onClick( DialogInterface dialog, int which ) {
					mStatus.setStatus( STATUS_PACKS );
					mListPacks.setSelectedPosition( HorizontalVariableListView.INVALID_POSITION, true );
					updateInstalledPacks( false );
				}
			} ).create();
		}

		if ( dialog != null ) {
			mUpdateDialog = dialog;
			mUpdateDialog.setCancelable( false );
			mUpdateDialog.show();
		}
	}

	// //////////////////////////
	// OnScrollChangedListener //

	@Override
	public void onScrollChanged() {
		mListPacks.setOnScrollListener( null );
		mScrollChanged = true;

		if ( mExternalPacksEnabled ) {
			hideIapPopup( 0 );
		}
	}

	// ///////////////////////////
	// Iap Notification methods //
	// ///////////////////////////

	/**
	 * Based on various condition show the IAP notification popup
	 */
	private void showIapPopup() {

		// available packs must be > 0
		// external packs must be enabled
		// popup never shown before

		if ( !mShowIapNotificationAndValue ) return;
		if ( mIapPopupShown ) return;
		mIapPopupShown = true;

		if ( mScrollChanged ) return;
		if ( !isActive() || getContext() == null || getHandler() == null ) return;

		if ( mIapNotificationPopup == null ) {
			if ( null != getContext().getBaseContext() ) {
				ViewGroup container = ( (FeatherContext) getContext().getBaseContext() ).activatePopupContainer();
				UIUtils.getLayoutInflater().inflate( R.layout.feather_iap_notification_popup, container, true );
				mIapNotificationPopup = (IapNotificationLayout) container.findViewById( R.id.iap_popup );
			}
		}

		if ( mIapNotificationPopup == null ) return;

		Rect r = new Rect( 0, 0, 0, 200 );
		Point offset = new Point();

		try {
			getOptionView().findViewById( R.id.flipper ).getGlobalVisibleRect( r, offset );
		} catch ( Throwable t ) {
			t.printStackTrace();
		}

		mIapNotificationPopup.setPadding( 0, 0, 0, r.height() );

		mIapNotificationPopup.setIcon( R.drawable.feather_stickers_popup_icon );
		mIapNotificationPopup.setText( String.valueOf( mAvailablePacks ) );

		// then finally post a delayed execution of the animation
		mIapNotificationPopup.show();
	}

	/**
	 * Hide the IAP notification popup
	 * 
	 * @param view
	 * @param delayMillis
	 */
	private void hideIapPopup( final long delayMillis ) {
		mLogger.info( "hideIapPopup: " + delayMillis );

		if ( !isActive() || getHandler() == null ) return;
		if ( mIapNotificationPopup == null || mIapNotificationPopup.getParent() == null ) return;
		if ( mIapNotificationPopup.getVisibility() == View.GONE ) return;

		mIapNotificationPopup.hide( delayMillis );
	}

	// //////////////////////
	// OnItemClickListener //
	// //////////////////////

	@Override
	public boolean onItemClick( AdapterView<?> parent, View view, int position, long id ) {

		Log.i( "stickers", "onItemClick: " + position );

		if ( !isActive() ) return false;

		if ( mStatus.getCurrentStatus() == STATUS_PACKS || mStatus.getCurrentStatus() == STATUS_IAP ) {

			StickerEffectPack item = (StickerEffectPack) mListPacks.getAdapter().getItem( position );

			// "get more" button
			if ( null == item ) {

				if ( position == 0 ) {
					Tracker.recordTag( "LeftGetMoreStickers : Selected" );
				} else {
					Tracker.recordTag( "RightGetMoreStickers : Selected" );
				}

				getContext().searchPlugin( FeatherIntent.PluginType.TYPE_STICKER );
				return false;
			}

			if ( null != item ) {

				if ( item.isDivider ) {
					return false;
				} else if ( item.isExternal ) {

					// open the IAP Dialog only if current build is > froyo and app memory is >= 32
					if ( android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.FROYO && Constants.getApplicationMaxMemory() >= 32 ) {
						mPlugin = (ExternalPlugin) item.mPluginRef;
						mStatus.setStatus( STATUS_IAP );
						Tracker.recordTag( "Unpurchased(" + mPlugin.getLabel( FeatherIntent.PluginType.TYPE_STICKER ) + ") : Opened" );
						return true;

					} else {
						// external plugin - download from the play store
						Tracker.recordTag( "Unpurchased(" + item.mTitle + ") : StoreButtonClicked" );
						getContext().downloadPlugin( item.mPackageName.toString(), FeatherIntent.PluginType.TYPE_STICKER );
						return false;
					}
				} else {
					// internal plugin
					mPlugin = (InternalPlugin) item.mPluginRef;
					if ( null != mPlugin ) {
						mStatus.setStatus( STATUS_STICKERS );
						Tracker.recordTag( mPlugin.getLabel( FeatherIntent.PluginType.TYPE_STICKER ) + ": Opened" );
					}
					return true;
				}
			}
		}
		
		return false;
	}

	// /////////////////////////
	// OnItemSelectedListener //
	// /////////////////////////

	@Override
	public void onItemSelected( AdapterView<?> parent, View view, int position, long id ) {
		mLogger.info( "onItemSelected: " + position );
	}

	@Override
	public void onNothingSelected( AdapterView<?> parent ) {
		mLogger.info( "onNothingSelected" );

		if ( mStatus.getCurrentStatus() == STATUS_IAP ) {
			mStatus.setStatus( STATUS_PACKS );
		}
	}

	// ////////////////////////
	// Drag and Drop methods //
	// ////////////////////////

	/**
	 * Starts the drag and drop operation
	 * 
	 * @param parent
	 *           - the parent list
	 * @param view
	 *           - the current view clicked
	 * @param position
	 *           - the position in the list
	 * @param id
	 *           - the item id
	 * @param nativeClick
	 *           - it's a native click
	 * @return
	 */
	private boolean startDrag( AdapterView<?> parent, View view, int position, long id, boolean animate ) {

		mLogger.info( "startDrag" );

		if ( android.os.Build.VERSION.SDK_INT < 9 ) return false;

		if ( parent == null || view == null || parent.getAdapter() == null ) {
			return false;
		}

		if ( mStatus.getCurrentStatus() != STATUS_STICKERS ) return false;
		if ( mPlugin == null || !( mPlugin instanceof InternalPlugin ) ) return false;

		if ( null != view ) {
			View image = view.findViewById( R.id.image );
			if ( null != image ) {
				final String dragInfo = (String) parent.getAdapter().getItem( position );

				int size = mThumbSize;
				Bitmap bitmap;
				try {
					bitmap = ImageLoader.getPluginItemBitmap( (InternalPlugin) mPlugin, dragInfo, FeatherIntent.PluginType.TYPE_STICKER, StickerType.Small, size, size );
					int offsetx = Math.abs( image.getWidth() - bitmap.getWidth() ) / 2;
					int offsety = Math.abs( image.getHeight() - bitmap.getHeight() ) / 2;
					return getDragController().startDrag( image, bitmap, offsetx, offsety, StickersPanel.this, dragInfo, DragControllerService.DRAG_ACTION_MOVE, animate );
				} catch ( Exception e ) {
					e.printStackTrace();
				}

				return getDragController().startDrag( image, StickersPanel.this, dragInfo, DragControllerService.DRAG_ACTION_MOVE, animate );
			}
		}
		return false;
	}

	@Override
	public void setDragController( DragControllerService controller ) {
		mDragControllerService = controller;
	}

	@Override
	public DragControllerService getDragController() {
		return mDragControllerService;
	}

	@Override
	public void onDropCompleted( View arg0, boolean arg1 ) {
		mLogger.info( "onDropCompleted" );
		mListStickers.setIsDragging( false );
	}

	@Override
	public boolean onDragEnd() {
		mLogger.info( "onDragEnd" );
		mListStickers.setIsDragging( false );
		return false;
	}

	@Override
	public void onDragStart( DragSource arg0, Object arg1, int arg2 ) {
		mLogger.info( "onDragStart" );
		mListStickers.setIsDragging( true );
	}

	@Override
	public boolean acceptDrop( DragSource source, int x, int y, int xOffset, int yOffset, DragView dragView, Object dragInfo ) {
		return source == this;
	}

	@Override
	public void onDrop( DragSource source, int x, int y, int xOffset, int yOffset, DragView dragView, Object dragInfo ) {

		mLogger.info( "onDrop. source=" + source + ", dragInfo=" + dragInfo );

		if ( dragInfo != null && dragInfo instanceof String ) {
			String sticker = (String) dragInfo;
			onApplyCurrent();

			float scaleFactor = dragView.getScaleFactor();

			float w = dragView.getWidth();
			float h = dragView.getHeight();

			int width = (int) ( w / scaleFactor );
			int height = (int) ( h / scaleFactor );

			int targetX = (int) ( x - xOffset );
			int targetY = (int) ( y - yOffset );

			RectF rect = new RectF( targetX, targetY, targetX + width, targetY + height );
			addSticker( sticker, rect );
		}
	}

	// /////////////////////////
	// Stickers panel methods //
	// /////////////////////////

	/**
	 * bundle contains a list of all updates applications. if one meets the criteria ( is a sticker apk ) then return true
	 * 
	 * @param bundle
	 *           - the bundle delta
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

							if ( FeatherIntent.PluginType.isSticker( update.getPluginType() ) ) {
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

	/**
	 * Ask to leave without apply changes.
	 * 
	 */
	void askToLeaveWithoutApply() {
		new AlertDialog.Builder( getContext().getBaseContext() ).setTitle( R.string.attention ).setMessage( R.string.tool_leave_question ).setPositiveButton( android.R.string.yes, new DialogInterface.OnClickListener() {

			@Override
			public void onClick( DialogInterface dialog, int which ) {
				getContext().cancel();
			}
		} ).setNegativeButton( android.R.string.no, null ).show();
	}

	/**
	 * Initialize the preview bitmap and canvas.
	 */
	private void createAndConfigurePreview() {

		if ( mPreview != null && !mPreview.isRecycled() ) {
			mPreview.recycle();
			mPreview = null;
		}

		mPreview = BitmapUtils.copy( mBitmap, Bitmap.Config.ARGB_8888 );
		mCanvas = new Canvas( mPreview );
	}

	/**
	 * Update the installed plugins
	 */
	protected void updateInstalledPacks( boolean firstTime ) {
		mIsAnimating = true;

		if ( mViewFlipper.getDisplayedChild() != 0 ) {
			mViewFlipper.setDisplayedChild( 0 );
		}
		new PluginInstallTask().execute();
	}

	private IapDialog createIapDialog() {
		ViewGroup container = ( (FeatherContext) getContext().getBaseContext() ).activatePopupContainer();
		IapDialog dialog = (IapDialog) container.findViewById( R.id.main_iap_dialog );
		if ( dialog == null ) {
			UIUtils.getLayoutInflater().inflate( R.layout.feather_iap_dialog, container, true );
			dialog = (IapDialog) container.findViewById( R.id.main_iap_dialog );
			dialog.setFocusable( true );
			dialog.setOnCloseListener( new OnCloseListener() {

				@Override
				public void onClose() {
					mStatus.setStatus( STATUS_PACKS );
					mListPacks.setSelectedPosition( HorizontalVariableListView.INVALID_POSITION, true );
				}
			} );

		}
		return dialog;
	}

	private void updateIapDialog() {
		final ExternalPlugin plugin = (ExternalPlugin) mPlugin;

		if ( null != mIapDialog && null != plugin ) {
			mIapDialog.setPlugin( plugin, FeatherIntent.PluginType.TYPE_STICKER, getContext().getBaseContext() );
		}
	}

	private boolean removeIapDialog() {
		if ( null != mIapDialog ) {
			mIapDialog.setOnCloseListener( null );
			mIapDialog.hide();
			mIapDialog = null;
			return true;
		}
		return false;
	}

	/**
	 * Loads the list of available stickers for the current selected pack
	 */
	protected void loadStickers() {

		mLogger.info( "loadStickers" );

		if ( mExternalPacksEnabled ) {
			hideIapPopup( 0 );
		}

		if ( mViewFlipper.getDisplayedChild() != 2 ) {
			mViewFlipper.setDisplayedChild( 2 );
		}

		if ( mPlugin != null || !( mPlugin instanceof InternalPlugin ) ) {

			String[] list = ( (InternalPlugin) mPlugin ).listStickers();
			getOptionView().post( new LoadStickersRunner( list ) );

		} else {
			onGenericError( "Sorry, there was an error opening the pack" );
		}
	}

	/**
	 * Add a new sticker to the canvas.
	 * 
	 * @param drawable
	 *           - the drawable name
	 */
	private void addSticker( String drawable, RectF position ) {

		if ( mPlugin == null || !( mPlugin instanceof InternalPlugin ) ) {
			return;
		}

		final InternalPlugin plugin = (InternalPlugin) mPlugin;

		onApplyCurrent();

		final boolean rotateAndResize = true;
		InputStream stream = null;

		try {
			stream = plugin.getStickerStream( drawable, StickerType.Small );
		} catch ( Exception e ) {
			e.printStackTrace();
			onGenericError( "Failed to load the selected sticker" );
			return;
		}

		if ( stream != null ) {
			StickerDrawable d = new StickerDrawable( plugin.getResources(), stream, drawable, plugin.getLabel( FeatherIntent.PluginType.TYPE_STICKER ).toString() );
			d.setAntiAlias( true );

			IOUtils.closeSilently( stream );

			// adding the required action
			ApplicationInfo info = PackageManagerUtils.getApplicationInfo( getContext().getBaseContext(), mPlugin.getPackageName() );
			if ( info != null ) {
				String sourceDir = plugin.getSourceDir( PluginType.TYPE_STICKER );

				if ( null == sourceDir ) {
					sourceDir = "";
					mLogger.error( "Cannot find the source dir" );
				}

				mCurrentFilter = new StickerFilter( sourceDir, drawable );
				mCurrentFilter.setSize( d.getBitmapWidth(), d.getBitmapHeight() );
				mCurrentFilter.setExternal( 0 );

				Tracker.recordTag( drawable + ": Selected" );

				addSticker( d, rotateAndResize, position );

			} else {
				onGenericError( "Sorry I'm not able to load the selected sticker" );
			}
		}
	}

	/**
	 * Adds the sticker.
	 * 
	 * @param drawable
	 *           - the drawable
	 * @param rotateAndResize
	 *           - allow rotate and resize
	 */
	private void addSticker( FeatherDrawable drawable, boolean rotateAndResize, RectF positionRect ) {

		mLogger.info( "addSticker: " + drawable + ", rotate: " + rotateAndResize + ", position: " + positionRect );

		setIsChanged( true );

		DrawableHighlightView hv = new DrawableHighlightView( mImageView, drawable );

		hv.setOnDeleteClickListener( new OnDeleteClickListener() {

			@Override
			public void onDeleteClick() {
				onClearCurrent( true );
			}
		} );

		Matrix mImageMatrix = mImageView.getImageViewMatrix();

		int cropWidth, cropHeight;
		int x, y;

		final int width = mImageView.getWidth();
		final int height = mImageView.getHeight();

		// width/height of the sticker
		if ( positionRect != null ) {
			cropWidth = (int) positionRect.width();
			cropHeight = (int) positionRect.height();
		} else {
			cropWidth = drawable.getIntrinsicWidth();
			cropHeight = drawable.getIntrinsicHeight();
		}

		final int cropSize = Math.max( cropWidth, cropHeight );
		final int screenSize = Math.min( mImageView.getWidth(), mImageView.getHeight() );

		if ( cropSize > screenSize ) {
			float ratio;
			float widthRatio = (float) mImageView.getWidth() / cropWidth;
			float heightRatio = (float) mImageView.getHeight() / cropHeight;

			if ( widthRatio < heightRatio ) {
				ratio = widthRatio;
			} else {
				ratio = heightRatio;
			}

			cropWidth = (int) ( (float) cropWidth * ( ratio / 2 ) );
			cropHeight = (int) ( (float) cropHeight * ( ratio / 2 ) );

			if ( positionRect == null ) {
				int w = mImageView.getWidth();
				int h = mImageView.getHeight();
				positionRect = new RectF( w / 2 - cropWidth / 2, h / 2 - cropHeight / 2, w / 2 + cropWidth / 2, h / 2 + cropHeight / 2 );
			}

			positionRect.inset( ( positionRect.width() - cropWidth ) / 2, ( positionRect.height() - cropHeight ) / 2 );
		}

		if ( positionRect != null ) {
			x = (int) positionRect.left;
			y = (int) positionRect.top;
		} else {
			x = ( width - cropWidth ) / 2;
			y = ( height - cropHeight ) / 2;
		}

		Matrix matrix = new Matrix( mImageMatrix );
		matrix.invert( matrix );

		float[] pts = new float[] { x, y, x + cropWidth, y + cropHeight };
		MatrixUtils.mapPoints( matrix, pts );

		RectF cropRect = new RectF( pts[0], pts[1], pts[2], pts[3] );
		Rect imageRect = new Rect( 0, 0, width, height );

		hv.setRotateAndScale( rotateAndResize );
		hv.setup( mImageMatrix, imageRect, cropRect, false );

		hv.drawOutlineFill( true );
		hv.drawOutlineStroke( true );
		hv.setPadding( mStickerHvPadding );

		hv.setOutlineStrokeColor( mStickerHvStrokeColorStateList );
		hv.setOutlineFillColor( mStickerHvFillColorStateList );

		hv.setOutlineEllipse( mStickerHvEllipse );
		hv.setMinSize( mStickerHvMinSize );

		Paint stroke = hv.getOutlineStrokePaint();
		stroke.setStrokeWidth( mStickerHvStrokeWidth );

		hv.getOutlineFillPaint().setXfermode( new PorterDuffXfermode( android.graphics.PorterDuff.Mode.SRC_ATOP ) );

		( (ImageViewDrawableOverlay) mImageView ).addHighlightView( hv );
		( (ImageViewDrawableOverlay) mImageView ).setSelectedHighlightView( hv );
	}

	/**
	 * Flatten the current sticker within the preview bitmap. No more changes will be possible on this sticker.
	 */
	private void onApplyCurrent() {

		mLogger.info( "onApplyCurrent" );

		if ( !stickersOnScreen() ) return;

		final DrawableHighlightView hv = ( (ImageViewDrawableOverlay) mImageView ).getHighlightViewAt( 0 );

		if ( hv != null ) {

			final StickerDrawable stickerDrawable = ( (StickerDrawable) hv.getContent() );

			RectF cropRect = hv.getCropRectF();
			Rect rect = new Rect( (int) cropRect.left, (int) cropRect.top, (int) cropRect.right, (int) cropRect.bottom );

			Matrix rotateMatrix = hv.getCropRotationMatrix();
			Matrix matrix = new Matrix( mImageView.getImageMatrix() );
			if ( !matrix.invert( matrix ) ) {}

			int saveCount = mCanvas.save( Canvas.MATRIX_SAVE_FLAG );
			mCanvas.concat( rotateMatrix );

			stickerDrawable.setDropShadow( false );
			hv.getContent().setBounds( rect );
			hv.getContent().draw( mCanvas );
			mCanvas.restoreToCount( saveCount );
			mImageView.invalidate();

			if ( mCurrentFilter != null ) {
				final int w = mBitmap.getWidth();
				final int h = mBitmap.getHeight();

				mCurrentFilter.setTopLeft( cropRect.left / w, cropRect.top / h );
				mCurrentFilter.setBottomRight( cropRect.right / w, cropRect.bottom / h );
				mCurrentFilter.setRotation( Math.toRadians( hv.getRotation() ) );

				int dw = stickerDrawable.getBitmapWidth();
				int dh = stickerDrawable.getBitmapHeight();
				float scalew = cropRect.width() / dw;
				float scaleh = cropRect.height() / dh;

				mCurrentFilter.setCenter( cropRect.centerX() / w, cropRect.centerY() / h );
				mCurrentFilter.setScale( scalew, scaleh );

				mActionList.add( mCurrentFilter.getActions().get( 0 ) );

				Tracker.recordTag( stickerDrawable.getPackLabel() + ": Applied" );

				mCurrentFilter = null;
			}
		}

		onClearCurrent( false );
		onPreviewChanged( mPreview, false );
	}

	/**
	 * Remove the current sticker.
	 * 
	 * @param removed
	 *           - true if the current sticker is being removed, otherwise it was flattened
	 */
	private void onClearCurrent( boolean removed ) {
		mLogger.info( "onClearCurrent. removed=" + removed );

		if ( stickersOnScreen() ) {
			final ImageViewDrawableOverlay image = (ImageViewDrawableOverlay) mImageView;
			final DrawableHighlightView hv = image.getHighlightViewAt( 0 );
			onClearCurrent( hv, removed );
		}
	}

	/**
	 * Removes the current active sticker.
	 * 
	 * @param hv
	 *           - the {@link DrawableHighlightView} of the active sticker
	 * @param removed
	 *           - current sticker is removed
	 */
	private void onClearCurrent( DrawableHighlightView hv, boolean removed ) {

		mLogger.info( "onClearCurrent. hv=" + hv + ", removed=" + removed );

		if ( mCurrentFilter != null ) {
			mCurrentFilter = null;
		}

		if ( null != hv ) {
			FeatherDrawable content = hv.getContent();

			if ( removed ) {
				if ( content instanceof StickerDrawable ) {
					String name = ( (StickerDrawable) content ).getStickerName();
					String packname = ( (StickerDrawable) content ).getPackLabel();

					Tracker.recordTag( name + ": Cancelled" );
					Tracker.recordTag( packname + ": Cancelled" );

				}
			}
		}

		hv.setOnDeleteClickListener( null );
		( (ImageViewDrawableOverlay) mImageView ).removeHightlightView( hv );
		( (ImageViewDrawableOverlay) mImageView ).invalidate();
	}

	/**
	 * Return true if there's at least one active sticker on screen.
	 * 
	 * @return true, if successful
	 */
	private boolean stickersOnScreen() {
		final ImageViewDrawableOverlay image = (ImageViewDrawableOverlay) mImageView;
		return image.getHighlightCount() > 0;
	}

	/**
	 * The PluginInstallTask is completed
	 * 
	 * @param result
	 */
	private void onStickersPackListUpdated( List<StickerEffectPack> result ) {
		mLogger.info( "onStickersPackListUpdated: " + result.size() );

		if ( mExternalPacksEnabled ) {

			StickerPacksAdapter adapter = new StickerPacksAdapter( getContext().getBaseContext(), R.layout.feather_sticker_pack2, R.layout.feather_sticker_pack2_external, R.layout.feather_stickers_pack_divider_empty,
					R.layout.feather_getmore_stickers_thumb, R.layout.feather_getmore_stickers_thumb_inverted, result );
			mListPacks.setAdapter( adapter );

			if ( mViewFlipper.getDisplayedChild() != 1 ) {
				mViewFlipper.setDisplayedChild( 1 );
			}
			showIapPopup();

			if ( mInstalledPackages.size() < 1 && mExternalPacksEnabled ) {
				// show the dialog popup

				if ( !mPreferenceService.containsValue( this.getClass().getSimpleName() + "-install-first-time" ) ) {

					OnClickListener listener = new OnClickListener() {

						@Override
						public void onClick( DialogInterface dialog, int which ) {
							getContext().downloadPlugin( PluginService.FREE_STICKERS_PACKAGENAME, FeatherIntent.PluginType.TYPE_STICKER );
							dialog.dismiss();
						}
					};

					AlertDialog dialog = new AlertDialog.Builder( getContext().getBaseContext() ).setMessage( R.string.feather_stickers_dialog_first_time ).setPositiveButton( android.R.string.ok, listener )
							.setNegativeButton( android.R.string.cancel, null ).create();

					mPreferenceService.putBoolean( this.getClass().getSimpleName() + "-install-first-time", true );

					dialog.show();
				}
			}
		} else {
			if( result.size() > 0 ) {
				mPlugin = (InternalPlugin) result.get( 0 ).mPluginRef;
				mStatus.setStatus( STATUS_STICKERS );
			}
		}
	}

	class PluginInstallTask extends AsyncTask<Void, Void, List<StickerEffectPack>> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mImageManager.clearCache();
		}

		@Override
		protected List<StickerEffectPack> doInBackground( Void... params ) {

			long sharedUpdateTime = 0, lastUpdateTime = 0;
			FeatherInternalPack installedPacks[] = null;
			FeatherPack availablePacks[] = null;

			if ( getContext() == null ) {
				return null;
			}

			if ( null != mPreferenceService && mExternalPacksEnabled ) sharedUpdateTime = mPreferenceService.getLong( this.getClass().getName() + "-plugins-update-date", 0 );
			if ( null != mPluginService ) lastUpdateTime = mPluginService.getLastUpdateTime();

			final Context context = getContext().getBaseContext();
			List<StickerEffectPack> result = Collections.synchronizedList( new ArrayList<StickerEffectPack>() );

			if ( null != context ) {

				if ( mExternalPacksEnabled ) {
					while ( !mPluginService.isUpdated() ) {
						try {
							Thread.sleep( 50 );
						} catch ( InterruptedException e ) {
							e.printStackTrace();
						}
						mLogger.log( "waiting for plugin service..." );
					}

					installedPacks = mPluginService.getInstalled( context, FeatherIntent.PluginType.TYPE_STICKER );
					availablePacks = mPluginService.getAvailable( FeatherIntent.PluginType.TYPE_STICKER );
				} else {
					installedPacks = new FeatherInternalPack[] { FeatherInternalPack.getDefault( getContext().getBaseContext() ) };
					availablePacks = new FeatherExternalPack[] {};
				}

				mAvailablePacks = availablePacks.length;
			}

			// List of the available plugins online
			mInstalledPackages.clear();

			if ( mExternalPacksEnabled ) {
				result.add( null );
			}

			// cycle the installed "internal" packages
			if ( null != context && installedPacks != null ) {
				for ( FeatherPack pack : installedPacks ) {
					if ( pack instanceof FeatherInternalPack ) {
						InternalPlugin plugin = (InternalPlugin) PluginManager.create( getContext().getBaseContext(), pack );
						final CharSequence packagename = plugin.getPackageName();
						final CharSequence label = plugin.getLabel( FeatherIntent.PluginType.TYPE_STICKER );

						final StickerEffectPack effectPack = new StickerEffectPack( packagename, label, PluginError.NoError, plugin, false );

						mInstalledPackages.add( packagename.toString() );

						if ( isActive() ) {
							result.add( effectPack );
						}
					}
				}
			}

			// cycle the available "external" packs
			if ( mExternalPacksEnabled && context != null ) {
				// Add divider if necessary
				if ( installedPacks != null && availablePacks != null ) {
					if ( availablePacks.length > 0 && installedPacks.length > 0 ) {
						result.add( new StickerEffectPack( mFeaturedDefaultTitle ) );
					}
				}

				if ( availablePacks != null ) {
					int index = 0;
					for ( FeatherPack pack : availablePacks ) {
						if ( index >= mFeaturedCount ) break;
						ExternalPlugin plugin = (ExternalPlugin) PluginManager.create( context, pack );
						final CharSequence packagename = plugin.getPackageName();
						final CharSequence label = plugin.getLabel( FeatherIntent.PluginType.TYPE_STICKER );

						final StickerEffectPack effectPack = new StickerEffectPack( packagename, label, PluginError.NoError, plugin, true );

						if ( isActive() ) {
							result.add( effectPack );
						}

						index++;
					}
				}
			}

			// add ending "get more" if necessary
			if ( mInstalledPackages != null && mInstalledPackages.size() > 0 && mExternalPacksEnabled ) {
				if ( mExternalPacksEnabled ) {
					result.add( null );
				}
			}

			// display the notification popup?
			if ( mExternalPacksEnabled && context != null ) {

				mLogger.log( "shared update time: " + sharedUpdateTime );
				mLogger.log( "last update time: " + lastUpdateTime );

				if ( sharedUpdateTime != lastUpdateTime ) {
					if ( mPreferenceService != null ) mPreferenceService.putLong( this.getClass().getName() + "-plugins-update-date", lastUpdateTime );
					mShowIapNotificationAndValue = mAvailablePacks > 0;
				} else {
					mShowIapNotificationAndValue = false;
				}
				mLogger.log( "mShowIapNotificationAndValue: " + mShowIapNotificationAndValue );
			}
			return result;
		}

		@Override
		protected void onPostExecute( List<StickerEffectPack> result ) {
			super.onPostExecute( result );
			mIsAnimating = false;
			onStickersPackListUpdated( result );
		}
	}

	/**
	 * Sticker pack listview adapter class
	 * 
	 * @author alessandro
	 * 
	 */
	class StickerPacksAdapter extends ArrayAdapterExtended<StickerEffectPack> {

		static final int TYPE_GET_MORE_FIRST = 0;
		static final int TYPE_GET_MORE_LAST = 1;
		static final int TYPE_NORMAL = 2;
		static final int TYPE_EXTERNAL = 3;
		static final int TYPE_DIVIDER = 4;

		private int mLayoutResId;
		private int mExternalLayoutResId;
		private int mAltLayoutResId;
		private int mAltLayout2ResId;
		private int mDividerLayoutResId;
		private int mDefaultHeight;
		private LayoutInflater mLayoutInflater;
		private BitmapDrawable mFolderIcon;
		private BitmapDrawable mExternalFolderIcon;

		public StickerPacksAdapter( Context context, int mainResId, int externalResId, int dividerResId, int altResId, int altResId2, List<StickerEffectPack> objects ) {
			super( context, mainResId, objects );
			mLayoutResId = mainResId;
			mExternalLayoutResId = externalResId;
			mAltLayoutResId = altResId;
			mAltLayout2ResId = altResId2;
			mDividerLayoutResId = dividerResId;
			mLayoutInflater = UIUtils.getLayoutInflater();
			mFolderIcon = (BitmapDrawable) context.getResources().getDrawable( R.drawable.feather_sticker_pack_background );
			mExternalFolderIcon = (BitmapDrawable) context.getResources().getDrawable( R.drawable.feather_sticker_pack_background );
			mDefaultHeight = getOptionView().findViewById( R.id.background ).getHeight() - getOptionView().findViewById( R.id.bottom_background_overlay ).getHeight();
		}

		@Override
		public int getViewTypeCount() {
			return 5;
		}

		@Override
		public int getItemViewType( int position ) {

			if ( !mExternalPacksEnabled ) return TYPE_NORMAL;

			StickerEffectPack item = getItem( position );
			if ( null == item ) {
				if ( position == 0 )
					return TYPE_GET_MORE_FIRST;
				else
					return TYPE_GET_MORE_LAST;
			}

			if ( item.isDivider ) return TYPE_DIVIDER;
			if ( item.isExternal ) return TYPE_EXTERNAL;
			return TYPE_NORMAL;
		}

		@Override
		public View getView( final int position, final View convertView, final ViewGroup parent ) {

			mLogger.log( "getView: " + position );

			View view;
			ViewHolder holder = null;
			int type = getItemViewType( position );
			int layoutWidth = mPacksCellWidth;
			int layoutHeight = LayoutParams.MATCH_PARENT;

			if ( convertView == null ) {
				holder = new ViewHolder();
				if ( type == TYPE_GET_MORE_FIRST ) {
					view = mLayoutInflater.inflate( mAltLayoutResId, parent, false );
					layoutHeight = mDefaultHeight;
				} else if ( type == TYPE_GET_MORE_LAST ) {
					view = mLayoutInflater.inflate( mAltLayout2ResId, parent, false );
					layoutHeight = mDefaultHeight;

					// hide the last "get more" button if there's no need
					View lastChild = parent.getChildAt( parent.getChildCount() - 1 );
					if ( null != lastChild ) {
						if ( lastChild.getRight() < parent.getRight() ) {
							layoutWidth = 0;
						}
					}
				} else if ( type == TYPE_NORMAL ) {
					// INSTALLED packs
					view = mLayoutInflater.inflate( mLayoutResId, parent, false );
					holder.text = (TextView) view.findViewById( R.id.text );
					holder.image = (ImageView) view.findViewById( R.id.image );
					
					LayoutParams params = holder.image.getLayoutParams();
					params.width = params.height = mPacksCellWidth;
					holder.image.setLayoutParams( params );	
					holder.image.requestLayout();
					
					view.setTag( holder );
					layoutHeight = LayoutParams.WRAP_CONTENT;
					layoutWidth = mPacksCellWidth + mItemsGapPixelSize;
					
				} else if ( type == TYPE_EXTERNAL ) {
					// EXTERNAL PACKS
					view = mLayoutInflater.inflate( mExternalLayoutResId, parent, false );
					holder.text = (TextView) view.findViewById( R.id.text );
					holder.image = (ImageView) view.findViewById( R.id.image );
					view.setTag( holder );
					
					LayoutParams params = holder.image.getLayoutParams();
					params.width = params.height = mPacksCellWidth;
					holder.image.setLayoutParams( params );	
					holder.image.requestLayout();
					
					layoutWidth = mPacksCellWidth + mItemsGapPixelSize;
					layoutHeight = LayoutParams.WRAP_CONTENT;
				} else {
					// TYPE_DIVIDER
					view = mLayoutInflater.inflate( mDividerLayoutResId, parent, false );
					
					ImageView image = (ImageView) view.findViewById( R.id.image );
					Drawable drawable = image.getDrawable();

					if( null != drawable ) {
						PluginDividerDrawable d = new PluginDividerDrawable( drawable, mFeaturedDefaultTitle );
						image.setImageDrawable( d );
					}
					
					layoutWidth = EffectThumbLayout.LayoutParams.WRAP_CONTENT;
					layoutHeight = mDefaultHeight;
				}
				view.setLayoutParams( new EffectThumbLayout.LayoutParams( layoutWidth, layoutHeight ) );
			} else {
				view = convertView;
				holder = (ViewHolder) view.getTag();
			}

			if ( type == TYPE_NORMAL ) {
				StickerEffectPack item = getItem( position );
				holder.text.setText( item.mTitle );

				InternalPlugin plugin = (InternalPlugin) item.mPluginRef;
				StickerPackThumbnailCallable executor = new StickerPackThumbnailCallable( plugin, mFolderIcon );
				mImageManager.execute( executor, plugin.getPackageName(), holder.image, STATUS_PACKS );

			} else if ( type == TYPE_EXTERNAL ) {
				StickerEffectPack item = getItem( position );
				holder.text.setText( item.mTitle );
				ExternalPlugin plugin = (ExternalPlugin) item.mPluginRef;

				mLogger.log( item.mTitle + " is free? " + plugin.isFree() );

				ExternalThumbnailCallable executor = new ExternalThumbnailCallable( plugin.getIconUrl(), mCacheService, mExternalFolderIcon, this.getContext().getResources(), R.drawable.feather_iap_dialog_image_na );
				mImageManager.execute( executor, plugin.getPackageName(), holder.image, STATUS_PACKS );

			} else if ( type == TYPE_DIVIDER ) {
				// do nothing...
			} else {
				// get more
				if ( mShowIapNotificationAndValue ) {
					TextView totalText = (TextView) view.findViewById( R.id.text01 );
					totalText.setText( String.valueOf( mAvailablePacks ) );
				}
			}
			return view;
		}
	}

	/**
	 * Retrieve and draw the internal plugin Icon
	 * 
	 * @author alessandro
	 * 
	 */
	static class StickerPackThumbnailCallable implements Callable<Bitmap> {

		InternalPlugin mPlugin;
		BitmapDrawable mFolder;

		public StickerPackThumbnailCallable( InternalPlugin plugin, BitmapDrawable drawable ) {
			mPlugin = plugin;
			mFolder = drawable;
		}

		@Override
		public Bitmap call() throws Exception {
			Drawable icon = mPlugin.getIcon( FeatherIntent.PluginType.TYPE_STICKER );
			if ( null != icon ) {
				return ( (BitmapDrawable) UIUtils.drawFolderIcon( mFolder, icon, 1.7f ) ).getBitmap();
			} else {
				return mFolder.getBitmap();
			}
		}
	}

	/**
	 * Download the remote icon or re-use the one from the current cache
	 * 
	 * @author alessandro
	 * 
	 */
	static class ExternalThumbnailCallable implements Callable<Bitmap> {

		String mUri;
		BitmapDrawable mFolder;
		SoftReference<ImageCacheService> cacheServiceRef;
		SoftReference<Resources> resourcesRef;
		int mDefaultIconResId;

		public ExternalThumbnailCallable( final String uri, ImageCacheService cacheService, final BitmapDrawable folderBackground, Resources resources, int defaultIconResId ) {
			mUri = uri;
			mFolder = folderBackground;
			cacheServiceRef = new SoftReference<ImageCacheService>( cacheService );
			resourcesRef = new SoftReference<Resources>( resources );
			mDefaultIconResId = defaultIconResId;
		}

		@SuppressWarnings("deprecation")
		@Override
		public Bitmap call() throws Exception {
			
			if( null == mUri || mUri.length() < 1 ) {
				return mFolder.getBitmap();
			}
			
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inPreferredConfig = Config.RGB_565;
			
			Bitmap bitmap = null;
			ImageCacheService cache = cacheServiceRef.get();
			
			if( null == cache ) {
				return mFolder.getBitmap();
			}
			
			SimpleCachedRemoteBitmap request;
			
			try {
				request = cache.requestRemoteBitmap( PluginService.CONTENT_DEFAULT_URL + "/" + mUri );
				bitmap = request.getBitmap( options );
			} catch( Exception e ){}


			// fallback icon
			if( null == bitmap ) {
				if( null != resourcesRef.get() ) {
					try {
						bitmap = BitmapFactory.decodeResource( resourcesRef.get(), mDefaultIconResId );
					} catch( Throwable t ) {}
				}
			}
			
			if ( null != bitmap ) {
				try {
					Bitmap result = UIUtils.drawFolderBitmap( mFolder, new BitmapDrawable( bitmap ), 1.7f );
					bitmap.recycle();
					bitmap = null;
					return result;
				} catch ( Throwable e ) {
					return mFolder.getBitmap();
				}
			} else {
				return mFolder.getBitmap();
			}
		}
	}

	/**
	 * Sticker pack element
	 * 
	 * @author alessandro
	 * 
	 */
	static class StickerEffectPack {

		CharSequence mPackageName;
		CharSequence mTitle;
		PluginError mPluginStatus;
		IPlugin mPluginRef;
		boolean isExternal;
		boolean isDivider;

		public StickerEffectPack( final String label ) {
			isDivider = true;
			mPluginStatus = PluginError.NoError;
			mTitle = label;
		}

		public StickerEffectPack( CharSequence packageName, CharSequence title, PluginError status, IPlugin plugin, boolean external ) {
			mPackageName = packageName;
			mPluginStatus = status;
			mPluginRef = plugin;
			mTitle = title;
			isExternal = external;
			isDivider = false;
		}

		@Override
		protected void finalize() throws Throwable {
			mPluginRef = null;
			super.finalize();
		}
	}

	//
	// Stickers list adapter
	//

	class StickersAdapter extends ArrayAdapter<String> {

		private LayoutInflater mLayoutInflater;
		private int mStickerResourceId;
		private int mDefaultHeight;

		/**
		 * Instantiates a new stickers adapter.
		 * 
		 * @param context
		 *           the context
		 * @param textViewResourceId
		 *           the text view resource id
		 * @param objects
		 *           the objects
		 */
		public StickersAdapter( Context context, int textViewResourceId, String[] objects ) {
			super( context, textViewResourceId, objects );

			mLogger.info( "StickersAdapter. size: " + objects.length );

			mStickerResourceId = textViewResourceId;
			mLayoutInflater = UIUtils.getLayoutInflater();
			mDefaultHeight = getOptionView().findViewById( R.id.background ).getHeight() - getOptionView().findViewById( R.id.bottom_background_overlay ).getHeight();
			mThumbSize = mDefaultHeight - ( THUMBNAIL_INSET * 2 );

			mLogger.log( "default height: " + mDefaultHeight );
			mLogger.log( "thumb size: " + mThumbSize );
		}

		@Override
		public View getView( int position, View convertView, ViewGroup parent ) {

			View view;

			if ( null == convertView ) {
				view = mLayoutInflater.inflate( mStickerResourceId, null );
				LayoutParams params = new LayoutParams( mDefaultHeight, mDefaultHeight );
				LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams( mThumbSize, mThumbSize );

				view.findViewById( R.id.image ).setLayoutParams( params2 );
				view.setLayoutParams( params );
			} else {
				view = convertView;
			}

			ImageView image = (ImageView) view.findViewById( R.id.image );

			final String sticker = getItem( position );

			StickerThumbnailCallable executor = new StickerThumbnailCallable( (InternalPlugin) mPlugin, sticker, mThumbSize );
			mImageManager.execute( executor, sticker, image, STATUS_STICKERS );

			return view;
		}
	}

	/**
	 * Downloads and renders the sticker thumbnail
	 * 
	 * @author alessandro
	 * 
	 */
	static class StickerThumbnailCallable implements Callable<Bitmap> {

		InternalPlugin mPlugin;
		int mFinalSize;
		String mUrl;

		public StickerThumbnailCallable( final InternalPlugin plugin, final String srcUrl, final int size ) {
			mPlugin = plugin;
			mFinalSize = size;
			mUrl = srcUrl;
		}

		@Override
		public Bitmap call() throws Exception {
			try {
				return ImageLoader.getPluginItemBitmap( mPlugin, mUrl, FeatherIntent.PluginType.TYPE_STICKER, StickerType.Preview, mFinalSize, mFinalSize );
			} catch ( NameNotFoundException e ) {
				return ImageLoader.getPluginItemBitmap( mPlugin, mUrl, FeatherIntent.PluginType.TYPE_STICKER, StickerType.Small, mFinalSize, mFinalSize );
			} catch ( Exception e ) {
				e.printStackTrace();
				return null;
			}
		}

	}

	//
	// Runnable for loading all the stickers from a pack
	//

	private class LoadStickersRunner implements Runnable {

		String[] mlist;

		LoadStickersRunner( String[] list ) {
			mlist = list;
		}

		@Override
		public void run() {

			mIsAnimating = true;

			if ( mListStickers.getHeight() == 0 ) {
				mOptionView.post( this );
				return;
			}

			StickersAdapter adapter = new StickersAdapter( getContext().getBaseContext(), R.layout.feather_sticker_thumb, mlist );
			mListStickers.setAdapter( adapter );

			// setting the drag tolerance to the list view height
			mListStickers.setDragTolerance( mListStickers.getHeight() );

			// activate drag and drop only for android 2.3+
			if ( android.os.Build.VERSION.SDK_INT > 8 ) {
				mListStickers.setDragScrollEnabled( true );
				mListStickers.setOnItemDragListener( new OnItemDragListener() {

					@Override
					public boolean onItemStartDrag( AdapterView<?> parent, View view, int position, long id ) {
						return startDrag( parent, view, position, id, false );
					}
				} );
			}
			mListStickers.setLongClickable( false );

			mListStickers.setOnItemClickedListener( new OnItemClickedListener() {

				@Override
				public boolean onItemClick( AdapterView<?> parent, View view, int position, long id ) {
					final Object obj = parent.getAdapter().getItem( position );
					final String sticker = (String) obj;
					addSticker( sticker, null );
					return true;
				}
			} );

			mIsAnimating = false;
			mlist = null;
		}
	}
}
