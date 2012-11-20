package com.aviary.android.feather.effects;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;
import com.aviary.android.feather.Constants;
import com.aviary.android.feather.R;
import com.aviary.android.feather.async_tasks.AssetsAsyncDownloadManager;
import com.aviary.android.feather.async_tasks.AssetsAsyncDownloadManager.Thumb;
import com.aviary.android.feather.graphics.StickerBitmapDrawable;
import com.aviary.android.feather.library.content.FeatherIntent;
import com.aviary.android.feather.library.content.FeatherIntent.PluginType;
import com.aviary.android.feather.library.graphics.drawable.FeatherDrawable;
import com.aviary.android.feather.library.graphics.drawable.StickerDrawable;
import com.aviary.android.feather.library.moa.MoaAction;
import com.aviary.android.feather.library.moa.MoaActionFactory;
import com.aviary.android.feather.library.moa.MoaActionList;
import com.aviary.android.feather.library.moa.MoaPointParameter;
import com.aviary.android.feather.library.plugins.FeatherExternalPack;
import com.aviary.android.feather.library.plugins.FeatherInternalPack;
import com.aviary.android.feather.library.plugins.FeatherPack;
import com.aviary.android.feather.library.plugins.PluginManager;
import com.aviary.android.feather.library.plugins.PluginManager.IPlugin;
import com.aviary.android.feather.library.plugins.PluginManager.InternalPlugin;
import com.aviary.android.feather.library.plugins.UpdateType;
import com.aviary.android.feather.library.services.ConfigService;
import com.aviary.android.feather.library.services.DragControllerService;
import com.aviary.android.feather.library.services.DragControllerService.DragListener;
import com.aviary.android.feather.library.services.DragControllerService.DragSource;
import com.aviary.android.feather.library.services.EffectContext;
import com.aviary.android.feather.library.services.PluginService;
import com.aviary.android.feather.library.services.PluginService.OnUpdateListener;
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
import com.aviary.android.feather.utils.TypefaceUtils;
import com.aviary.android.feather.utils.UIUtils;
import com.aviary.android.feather.widget.DrawableHighlightView;
import com.aviary.android.feather.widget.DrawableHighlightView.OnDeleteClickListener;
import com.aviary.android.feather.widget.HorizontalFixedListView;
import com.aviary.android.feather.widget.HorizontalFixedListView.OnItemDragListener;
import com.aviary.android.feather.widget.ImageViewDrawableOverlay;
import com.aviary.android.feather.widget.wp.CellLayout;
import com.aviary.android.feather.widget.wp.CellLayout.CellInfo;
import com.aviary.android.feather.widget.wp.Workspace;
import com.aviary.android.feather.widget.wp.WorkspaceIndicator;

public class StickersPanel extends AbstractContentPanel implements OnUpdateListener, DragListener, DragSource, DropTargetListener {

	private static enum Status {

		Null, // home
		Packs, // pack display
		Stickers, // stickers
	}

	/** The default get more icon. */
	// private Drawable mFolderIcon, mGetMoreIcon, mGetMoreFreeIcon;

	private int mStickerHvEllipse, mStickerHvStrokeWidth, mStickerHvMinSize;
	private int mStickerHvPadding;;
	private ColorStateList mStickerHvStrokeColorStateList;
	private ColorStateList mStickerHvFillColorStateList;

	private Workspace mWorkspace;
	private WorkspaceIndicator mWorkspaceIndicator;
	private HorizontalFixedListView mHList;
	private ViewFlipper mViewFlipper;
	private int mStickerMinSize;
	private Canvas mCanvas;
	private AssetsAsyncDownloadManager mDownloadManager;
	private PluginService mPluginService;
	private int mWorkspaceCols;
	private int mWorkspaceRows;
	private int mWorkspaceItemsPerPage;
	private List<String> mUsedStickers;
	private List<String> mUsedStickersPacks;
	private InternalPlugin mPlugin;
	private ConfigService mConfig;
	private Status mStatus = Status.Null;
	private Status mPrevStatus = Status.Null;
	private List<String> mInstalledPackages;
	private View mLayoutLoader;

	private final MoaActionList mActionList = MoaActionFactory.actionList();
	private MoaAction mCurrentAction;

	private PreferenceService mPrefService;

	private DragControllerService mDragController;

	private boolean mExternalPacksEnabled = true;

	/**
	 * Instantiates a new stickers panel.
	 * 
	 * @param context
	 *           the context
	 */
	public StickersPanel( EffectContext context ) {
		super( context );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aviary.android.feather.effects.AbstractEffectPanel#onCreate(android.graphics.Bitmap)
	 */
	@Override
	public void onCreate( Bitmap bitmap ) {
		super.onCreate( bitmap );

		// Resolve layout resources
		mWorkspaceIndicator = (WorkspaceIndicator) mOptionView.findViewById( R.id.workspace_indicator );
		mWorkspace = (Workspace) mOptionView.findViewById( R.id.workspace );
		mViewFlipper = (ViewFlipper) mOptionView.findViewById( R.id.flipper );
		mHList = (HorizontalFixedListView) mOptionView.findViewById( R.id.gallery );
		mImageView = (ImageViewDrawableOverlay) mDrawingPanel.findViewById( R.id.overlay );
		mLayoutLoader = mOptionView.findViewById( R.id.layout_loader );

		// retrive the used services
		mConfig = getContext().getService( ConfigService.class );
		mPluginService = getContext().getService( PluginService.class );
		mPrefService = getContext().getService( PreferenceService.class );

		// Load all the configurations
		mStickerHvEllipse = mConfig.getInteger( R.integer.feather_sticker_highlight_ellipse );
		mStickerHvStrokeWidth = mConfig.getInteger( R.integer.feather_sticker_highlight_stroke_width );

		mStickerHvStrokeColorStateList = mConfig.getColorStateList( R.color.feather_sticker_color_stroke_selector );
		mStickerHvFillColorStateList = mConfig.getColorStateList( R.color.feather_sticker_color_fill_selector );

		mStickerHvMinSize = mConfig.getInteger( R.integer.feather_sticker_highlight_minsize );
		mStickerHvPadding = mConfig.getInteger( R.integer.feather_sticker_highlight_padding );

		// External packs enabled ?
		mExternalPacksEnabled = Constants.getValueFromIntent( Constants.EXTRA_STICKERS_ENABLE_EXTERNAL_PACKS, true );

		// Remember which stickers we used
		mUsedStickers = new ArrayList<String>();
		mUsedStickersPacks = new ArrayList<String>();

		// Initialize the asset download manager
		mDownloadManager = new AssetsAsyncDownloadManager( this.getContext().getBaseContext(), mHandler );

		// setup the main imageview
		( (ImageViewDrawableOverlay) mImageView ).setForceSingleSelection( false );
		( (ImageViewDrawableOverlay) mImageView ).setDropTargetListener( this );
		( (ImageViewDrawableOverlay) mImageView ).setScaleWithContent( true );

		// setup the horizontal list
		mHList.setOverScrollMode( View.OVER_SCROLL_ALWAYS );
		mHList.setHideLastChild( true );
		mHList.setInverted( true );

		// setup the workspace
		mWorkspace.setHapticFeedbackEnabled( false );
		mWorkspace.setIndicator( mWorkspaceIndicator );

		// resource manager used to load external stickers
		mPlugin = null;

		// Set the current view status
		mPrevStatus = mStatus = Status.Null;

		// initialize the content bitmap
		createAndConfigurePreview();

		if ( android.os.Build.VERSION.SDK_INT > 8 ) {
			DragControllerService dragger = getContext().getService( DragControllerService.class );
			dragger.addDropTarget( (DropTarget) mImageView );
			dragger.setMoveTarget( mImageView );
			dragger.setDragListener( this );
			dragger.activate();
			setDragController( dragger );
		}

		// If external packs not enabled then skip to the stickers status
		if ( !mExternalPacksEnabled ) {
			mLayoutLoader.setVisibility( View.GONE );
			mViewFlipper.setInAnimation( null );
			mViewFlipper.setOutAnimation( null );
			mWorkspace.setVisibility( View.GONE );
			setCurrentPack( FeatherInternalPack.getDefault( getContext().getBaseContext() ) );
		}
	}

	@Override
	protected void onDispose() {
		super.onDispose();
		mWorkspace.setAdapter( null );
		mHList.setAdapter( null );
	}

	/**
	 * Screen configuration has changed.
	 * 
	 * @param newConfig
	 *           the new config
	 */

	@Override
	public void onConfigurationChanged( Configuration newConfig, Configuration oldConfig ) {
		super.onConfigurationChanged( newConfig, oldConfig );

		// we need to reload the current adapter...
		if ( mExternalPacksEnabled ) {
			initWorkspace();
		}
		mDownloadManager.clearCache();

		if ( mStatus == Status.Null || mStatus == Status.Packs ) {
			loadPacks( false );
		} else {
			loadStickers();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aviary.android.feather.effects.AbstractEffectPanel#onDeactivate()
	 */
	@Override
	public void onDeactivate() {
		super.onDeactivate();

		if ( mExternalPacksEnabled ) {
			mPluginService.removeOnUpdateListener( this );
		}

		( (ImageViewDrawableOverlay) mImageView ).setDropTargetListener( null );

		if ( null != getDragController() ) {
			getDragController().deactivate();
			getDragController().removeDropTarget( (DropTarget) mImageView );
			getDragController().setDragListener( null );
		}
		setDragController( null );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aviary.android.feather.effects.AbstractEffectPanel#onActivate()
	 */
	@Override
	public void onActivate() {
		super.onActivate();

		// getting the packs installed
		mInstalledPackages = Collections.synchronizedList( new ArrayList<String>() );

		// initialize the workspace
		if ( mExternalPacksEnabled ) {
			initWorkspace();
		}

		mImageView.setImageBitmap( mPreview, true, getContext().getCurrentImageViewMatrix(), UIConfiguration.IMAGE_VIEW_MAX_ZOOM );

		if ( mExternalPacksEnabled ) {
			mPluginService.registerOnUpdateListener( this );
			mWorkspace.setCacheEnabled( true );
			mWorkspace.enableChildrenCache( 0, 1 );

			setStatus( Status.Packs );
		} else {
			getContentView().setVisibility( View.VISIBLE );
			contentReady();
		}
	}

	private void startFirstAnimation() {
		Animation animation = new TranslateAnimation( TranslateAnimation.RELATIVE_TO_SELF, 0, TranslateAnimation.RELATIVE_TO_SELF, 0, TranslateAnimation.RELATIVE_TO_SELF, 1, TranslateAnimation.RELATIVE_TO_SELF, 0 );

		animation.setDuration( 300 );
		animation.setStartOffset( 100 );
		animation.setInterpolator( AnimationUtils.loadInterpolator( getContext().getBaseContext(), android.R.anim.decelerate_interpolator ) );
		animation.setFillEnabled( true );
		animation.setAnimationListener( new AnimationListener() {

			@Override
			public void onAnimationStart( Animation animation ) {
				mWorkspace.setVisibility( View.VISIBLE );
			}

			@Override
			public void onAnimationRepeat( Animation animation ) {}

			@Override
			public void onAnimationEnd( Animation animation ) {
				getContentView().setVisibility( View.VISIBLE );
				contentReady();

				mWorkspace.clearChildrenCache();
				mWorkspace.setCacheEnabled( false );
				mWorkspace.requestLayout();
				mWorkspace.postInvalidate();
			}
		} );

		mWorkspace.startAnimation( animation );
	}

	/**
	 * Initialize the preview bitmap and canvas.
	 */
	private void createAndConfigurePreview() {

		if ( mPreview != null && !mPreview.isRecycled() ) {
			mPreview.recycle();
			mPreview = null;
		}

		mPreview = BitmapUtils.copy( mBitmap, mBitmap.getConfig() );
		mCanvas = new Canvas( mPreview );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aviary.android.feather.effects.AbstractEffectPanel#onDestroy()
	 */
	@Override
	public void onDestroy() {
		if ( mDownloadManager != null ) {
			mDownloadManager.clearCache();
			mDownloadManager.shutDownNow();
		}

		if( null != mPlugin ){
			mPlugin.dispose();
		}
		
		mPlugin = null;
		mCanvas = null;
		super.onDestroy();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aviary.android.feather.effects.AbstractEffectPanel#onGenerateResult()
	 */
	@Override
	protected void onGenerateResult() {
		onApplyCurrent( false );
		super.onGenerateResult( mActionList );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aviary.android.feather.effects.AbstractEffectPanel#onBackPressed()
	 */
	@Override
	public boolean onBackPressed() {
		if ( backHandled() ) return true;
		return false;
	}

	/**
	 * Manager asked to cancel this panel Before leave ask user if he really want to leave and lose all stickers.
	 * 
	 * @return true, if successful
	 */
	@Override
	public boolean onCancel() {
		if ( stickersOnScreen() ) {
			askToLeaveWithoutApply();
			return true;
		}

		return false;
	}

	/**
	 * Set the current pack as active and display its content.
	 * 
	 * @param info
	 *           the new current pack
	 */
	private void setCurrentPack( FeatherPack info ) {
		
		if ( info == null ) {
			getContext().downloadPlugin( FeatherIntent.PLUGIN_BASE_PACKAGE + "*", FeatherIntent.PluginType.TYPE_STICKER );
			return;
		}
		if ( info instanceof FeatherExternalPack ) {
			getContext().downloadPlugin( ( (FeatherExternalPack) info ).getPackageName(), FeatherIntent.PluginType.TYPE_STICKER );
			return;
		}
		
		if( info.getStickerVersion() < mPluginService.getMinStickersVersion() || info.getStickerVersion() > mPluginService.getMaxStickersVersion() ) {
			onGenericError( "The version of this plugin is not supported!" );
			mLogger.error( "sticker version: " + info.getStickerVersion() );
			return;
		}

		if( null != mPlugin ){
			mPlugin.dispose();
		}
		
		mPlugin = (InternalPlugin) PluginManager.create( getContext().getBaseContext(), info );

		/**
		 * send the event to localytics only once
		 */
		if ( !mPrefService.containsValue( "stickers." + info.getPackageName() ) ) {
			if ( !getContext().getBaseContext().getPackageName().equals( info.getPackageName() ) ) {
				mPrefService.putString( "stickers." + info.getPackageName(), info.getPackageName() );
				HashMap<String, String> map = new HashMap<String, String>();
				map.put( "assetType", "stickers" );
				map.put( "assetID", info.getPackageName() );
				Tracker.recordTag( "content: purchased", map );
			}
		}

		setStatus( Status.Stickers );
	}

	/**
	 * Load all the available stickers packs.
	 */
	private void loadPacks( boolean animate ) {
		updateInstalledPacks( animate );
		if ( mViewFlipper.getDisplayedChild() != 0 ) {
			mViewFlipper.setDisplayedChild( 0 );
		}
	}

	/**
	 * Reload the installed packs and reload the workspace adapter.
	 */
	private void updateInstalledPacks( boolean animate ) {

		if ( !isActive() ) return;
		if ( getContext().getBaseContext() != null ) {
			UpdateInstalledPacksTask task = new UpdateInstalledPacksTask( animate );
			task.execute();
		}
	}

	/**
	 * Load all the available stickers for the selected pack.
	 */
	private void loadStickers() {
		String[] list = mPlugin.listStickers();

		if ( list != null ) {

			String[] listcopy = new String[list.length + 2];
			System.arraycopy( list, 0, listcopy, 1, list.length );
			mViewFlipper.setDisplayedChild( 1 );

			getOptionView().post( new LoadStickersRunner( listcopy ) );
		}
	}

	/**
	 * The Class LoadStickersRunner.
	 */
	private class LoadStickersRunner implements Runnable {

		/** The mlist. */
		String[] mlist;

		/**
		 * Instantiates a new load stickers runner.
		 * 
		 * @param list
		 *           the list
		 */
		LoadStickersRunner( String[] list ) {
			mlist = list;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {

			if ( mHList.getHeight() == 0 ) {
				mOptionView.post( this );
				return;
			}

			StickersAdapter adapter = new StickersAdapter( getContext().getBaseContext(), R.layout.feather_sticker_thumb, mlist );
			mHList.setAdapter( adapter );

			// setting the drag tolerance to the list view height
			mHList.setDragTolerance( mHList.getHeight() );

			// activate drag and drop only for android 2.3+
			if ( android.os.Build.VERSION.SDK_INT > 8 ) {
				mHList.setDragScrollEnabled( true );

				mHList.setOnItemDragListener( new OnItemDragListener() {

					@Override
					public boolean onItemStartDrag( AdapterView<?> parent, View view, int position, long id ) {
						return startDrag( parent, view, position, id, false );
					}
				} );

				mHList.setLongClickable( true );
				mHList.setOnItemLongClickListener( new AdapterView.OnItemLongClickListener() {

					@Override
					public boolean onItemLongClick( AdapterView<?> parent, View view, int position, long id ) {
						return startDrag( parent, view, position, id, true );
					}
				} );
			} else {
				mHList.setLongClickable( false );
			}

			mHList.setOnItemClickListener( new OnItemClickListener() {

				@Override
				public void onItemClick( AdapterView<?> parent, View view, int position, long id ) {
					final Object obj = parent.getAdapter().getItem( position );
					final String sticker = (String) obj;
					mLogger.log( view.getWidth() + ", " + view.getHeight() );
					addSticker( sticker, null );
				}
			} );
			mlist = null;
		}
	}

	private boolean startDrag( AdapterView<?> parent, View view, int position, long id, boolean nativeClick ) {

		if ( android.os.Build.VERSION.SDK_INT < 9 ) return false;

		if ( parent == null || view == null || parent.getAdapter() == null ) {
			return false;
		}

		if ( position == 0 || position >= parent.getAdapter().getCount() - 1 ) {
			return false;
		}

		if ( null != view ) {
			View image = view.findViewById( R.id.image );
			if ( null != image ) {
				final String dragInfo = (String) parent.getAdapter().getItem( position );

				int size = mDownloadManager.getThumbSize();
				Bitmap bitmap;
				try {
					bitmap = ImageLoader.loadStickerBitmap( mPlugin, dragInfo, StickerType.Small, size, size );
					int offsetx = Math.abs( image.getWidth() - bitmap.getWidth() ) / 2;
					int offsety = Math.abs( image.getHeight() - bitmap.getHeight() ) / 2;
					return getDragController().startDrag( image, bitmap, offsetx, offsety, StickersPanel.this, dragInfo, DragControllerService.DRAG_ACTION_MOVE, nativeClick );
				} catch ( Exception e ) {
					e.printStackTrace();
				}

				return getDragController().startDrag( image, StickersPanel.this, dragInfo, DragControllerService.DRAG_ACTION_MOVE, nativeClick );
			}
		}
		return false;
	}

	/**
	 * Inits the workspace.
	 */
	private void initWorkspace() {

		ConfigService config = getContext().getService( ConfigService.class );
		if ( config != null ) {
			mWorkspaceRows = Math.max( config.getInteger( R.integer.feather_config_portraitRows ), 1 );
		} else {
			mWorkspaceRows = 1;
		}
		mWorkspaceCols = getContext().getBaseContext().getResources().getInteger( R.integer.featherStickerPacksCount );
		mWorkspaceItemsPerPage = mWorkspaceRows * mWorkspaceCols;
	}

	/**
	 * Flatten the current sticker within the preview bitmap no more changes will be possible on this sticker.
	 * 
	 * @param updateStatus
	 *           the update status
	 */
	private void onApplyCurrent( boolean updateStatus ) {
		final ImageViewDrawableOverlay image = (ImageViewDrawableOverlay) mImageView;
		if ( image.getHighlightCount() < 1 ) return;

		final DrawableHighlightView hv = ( (ImageViewDrawableOverlay) mImageView ).getHighlightViewAt( 0 );

		if ( hv != null ) {

			RectF cropRect = hv.getCropRectF();
			Rect rect = new Rect( (int) cropRect.left, (int) cropRect.top, (int) cropRect.right, (int) cropRect.bottom );

			Matrix rotateMatrix = hv.getCropRotationMatrix();
			Matrix matrix = new Matrix( mImageView.getImageMatrix() );
			if ( !matrix.invert( matrix ) ) {}

			int saveCount = mCanvas.save( Canvas.MATRIX_SAVE_FLAG );
			mCanvas.concat( rotateMatrix );

			( (StickerDrawable) hv.getContent() ).setDropShadow( false );
			hv.getContent().setBounds( rect );
			hv.getContent().draw( mCanvas );
			mCanvas.restoreToCount( saveCount );
			mImageView.invalidate();

			if ( mCurrentAction != null ) {
				final int w = mBitmap.getWidth();
				final int h = mBitmap.getHeight();
				mCurrentAction.setValue( "topleft", new MoaPointParameter( cropRect.left / w, cropRect.top / h ) );
				mCurrentAction.setValue( "bottomright", new MoaPointParameter( cropRect.right / w, cropRect.bottom / h ) );
				mCurrentAction.setValue( "rotation", Math.toRadians( hv.getRotation() ) );

				int dw = ( (StickerDrawable) hv.getContent() ).getBitmapWidth();
				int dh = ( (StickerDrawable) hv.getContent() ).getBitmapHeight();
				float scalew = cropRect.width() / dw;
				float scaleh = cropRect.height() / dh;

				// version 2
				mCurrentAction.setValue( "center", new MoaPointParameter( cropRect.centerX() / w, cropRect.centerY() / h ) );
				mCurrentAction.setValue( "scale", new MoaPointParameter( scalew, scaleh ) );

				mActionList.add( mCurrentAction );
				mCurrentAction = null;

			}

		}
		onClearCurrent( true, updateStatus );
		onPreviewChanged( mPreview, false );
	}

	/**
	 * Remove the current sticker.
	 * 
	 * @param isApplying
	 *           if true is passed it means we're currently in the "applying" status
	 * @param updateStatus
	 *           if true will update the internal status
	 */
	private void onClearCurrent( boolean isApplying, boolean updateStatus ) {
		final ImageViewDrawableOverlay image = (ImageViewDrawableOverlay) mImageView;

		if ( image.getHighlightCount() > 0 ) {
			final DrawableHighlightView hv = image.getHighlightViewAt( 0 );
			onClearCurrent( hv, isApplying, updateStatus );
		}
	}

	/**
	 * removes the current active sticker.
	 * 
	 * @param hv
	 *           the hv
	 * @param isApplying
	 *           if panel is in the onGenerateResult state
	 * @param updateStatus
	 *           update the panel status
	 */
	private void onClearCurrent( DrawableHighlightView hv, boolean isApplying, boolean updateStatus ) {

		if ( mCurrentAction != null ) {
			mCurrentAction = null;
		}

		if ( !isApplying ) {

			FeatherDrawable content = hv.getContent();
			String name;
			String packagename;
			if ( content instanceof StickerDrawable ) {
				name = ( (StickerDrawable) content ).getName();
				packagename = ( (StickerDrawable) content ).getPackageName();

				if ( mUsedStickers.size() > 0 ) mUsedStickers.remove( name );
				if ( mUsedStickersPacks.size() > 0 ) mUsedStickersPacks.remove( packagename );
			} else {

				if ( mUsedStickers.size() > 0 ) mUsedStickers.remove( mUsedStickers.size() - 1 );
				if ( mUsedStickersPacks.size() > 0 ) mUsedStickersPacks.remove( mUsedStickersPacks.size() - 1 );
			}
		}

		hv.setOnDeleteClickListener( null );
		( (ImageViewDrawableOverlay) mImageView ).removeHightlightView( hv );
		( (ImageViewDrawableOverlay) mImageView ).invalidate();

		if ( updateStatus ) setStatus( Status.Stickers );
	}
	
	/**
	 * Add a new sticker to the canvas.
	 * 
	 * @param drawable
	 *           the drawable
	 */
	private void addSticker( String drawable, RectF position ) {

		onApplyCurrent( false );

		mLogger.info( "addSticker: " + drawable );
		final boolean rotateAndResize = true;
		InputStream stream = null;

		try {
			stream = mPlugin.getStickerStream( drawable, StickerType.Small );
		} catch ( Exception e ) {
			e.printStackTrace();
			onGenericError( "Failed to load the selected sticker" );
			return;
		}

		if ( stream != null ) {
			StickerDrawable d = new StickerDrawable( mPlugin.getResources(), stream, mPlugin.getPackageName(), drawable );
			d.setAntiAlias( true );
			mUsedStickers.add( drawable );
			mUsedStickersPacks.add( mPlugin.getPackageName() );
			addSticker( d, rotateAndResize, position );
			IOUtils.closeSilently( stream );

			// adding the required action
			ApplicationInfo info = PackageManagerUtils.getApplicationInfo( getContext().getBaseContext(), mPlugin.getPackageName() );
			if ( info != null ) {
				mCurrentAction = MoaActionFactory.action( "addsticker" );
				String sourceDir = mPlugin.getSourceDir( PluginType.TYPE_STICKER );
				
				if( null != sourceDir ) {
					mCurrentAction.setValue( "source", sourceDir );
					mLogger.log( "source-dir: " + sourceDir );
				} else {
					mLogger.error("Cannot find the source dir");
				}
				
				mCurrentAction.setValue( "url", drawable );

				// version 2
				mCurrentAction.setValue( "size", new MoaPointParameter( d.getBitmapWidth(), d.getBitmapHeight() ) );
				mCurrentAction.setValue( "external", 0 );
			}
		}
	}

	/**
	 * Adds the sticker.
	 * 
	 * @param drawable
	 *           the drawable
	 * @param rotateAndResize
	 *           the rotate and resize
	 */
	private void addSticker( FeatherDrawable drawable, boolean rotateAndResize, RectF positionRect ) {
		setIsChanged( true );

		DrawableHighlightView hv = new DrawableHighlightView( mImageView, drawable );
		hv.setMinSize( mStickerMinSize );
		hv.setOnDeleteClickListener( new OnDeleteClickListener() {

			@Override
			public void onDeleteClick() {
				onClearCurrent( false, true );
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
			
			if( positionRect == null ) {
				int w = mImageView.getWidth();
				int h = mImageView.getHeight();
				positionRect = new RectF( w/2-cropWidth/2, h/2-cropHeight/2, w/2+cropWidth/2, h/2+cropHeight/2 );
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
	 * The Class StickersPacksAdapter.
	 */
	class StickersPacksAdapter extends ArrayAdapter<FeatherPack> {

		int screenId, cellId;
		LayoutInflater mLayoutInflater;
		long mCurrentDate;
		boolean mInFirstLayout = true;
		String mGetMoreLabel;
		Drawable mFolderIcon, mGetMoreIcon, mGetMoreFreeIcon;
		Typeface mPackTypeface;

		/**
		 * Instantiates a new stickers packs adapter.
		 * 
		 * @param context
		 *           the context
		 * @param resource
		 *           the resource
		 * @param textViewResourceId
		 *           the text view resource id
		 * @param objects
		 *           the objects
		 */
		public StickersPacksAdapter( Context context, int resource, int textViewResourceId, FeatherPack objects[] ) {
			super( context, resource, textViewResourceId, objects );
			screenId = resource;
			cellId = textViewResourceId;
			mLayoutInflater = UIUtils.getLayoutInflater();
			mCurrentDate = System.currentTimeMillis();
			mGetMoreLabel = context.getString( R.string.get_more );
			mFolderIcon = context.getResources().getDrawable( R.drawable.feather_sticker_pack_background );
			mGetMoreIcon = context.getResources().getDrawable( R.drawable.feather_sticker_pack_background_more );
			mGetMoreFreeIcon = context.getResources().getDrawable( R.drawable.feather_sticker_pack_background_free_more );

			String packFont = context.getString( R.string.feather_sticker_pack_font );
			if ( null != packFont && packFont.length() > 1 ) {
				try {
					mPackTypeface = TypefaceUtils.createFromAsset( context.getAssets(), packFont );
				} catch ( Throwable t ) {
					t.printStackTrace();
				}
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.widget.ArrayAdapter#getCount()
		 */
		@Override
		public int getCount() {
			return (int) Math.ceil( (double) ( super.getCount() ) / mWorkspaceItemsPerPage );
		}

		/**
		 * Gets the real count.
		 * 
		 * @return the real count
		 */
		public int getRealCount() {
			return super.getCount();
		}

		@Override
		public View getView( int position, View convertView, ViewGroup parent ) {

			CellLayout view;

			if ( convertView == null ) {
				view = (CellLayout) mLayoutInflater.inflate( screenId, mWorkspace, false );
				view.setNumCols( mWorkspaceCols );
			} else {
				view = (CellLayout) convertView;
			}

			int index = position * mWorkspaceItemsPerPage;
			int count = getRealCount();

			for ( int i = 0; i < mWorkspaceItemsPerPage; i++ ) {
				View itemView = null;
				CellInfo cellInfo = view.findVacantCell( 1, 1 );
				if ( cellInfo == null ) {
					itemView = view.getChildAt( i );
				} else {
					itemView = mLayoutInflater.inflate( cellId, parent, false );
					CellLayout.LayoutParams lp = new CellLayout.LayoutParams( cellInfo.cellX, cellInfo.cellY, cellInfo.spanH, cellInfo.spanV );
					view.addView( itemView, -1, lp );
				}

				if ( ( index + i ) < count ) {

					final FeatherPack appInfo = getItem( index + i );
					final IPlugin plugin = PluginManager.create( getContext(), appInfo );
					CharSequence label;
					Drawable icon;

					if ( appInfo == null ) {
						label = mGetMoreLabel;
						icon = mGetMoreIcon;
					} else {

						label = plugin.getLabel( PluginType.TYPE_STICKER );

						if ( plugin.isLocal() ) {
							icon = plugin.getIcon( PluginType.TYPE_STICKER );
							icon = UIUtils.drawFolderIcon( mFolderIcon, icon, null );
						} else {
							if ( plugin.isFree() ) {
								icon = mGetMoreFreeIcon;
							} else {
								icon = mGetMoreIcon;
							}
						}
					}

					ImageView image = (ImageView) itemView.findViewById( R.id.image );
					TextView text = (TextView) itemView.findViewById( R.id.text );

					if ( null != mPackTypeface ) {
						text.setTypeface( mPackTypeface );
					}

					image.setImageDrawable( icon );
					text.setText( label );
					itemView.setTag( appInfo );
					itemView.setOnClickListener( new OnClickListener() {

						@Override
						public void onClick( View v ) {
							setCurrentPack( appInfo );
						}
					} );
					itemView.setVisibility( View.VISIBLE );
				} else {
					itemView.setVisibility( View.INVISIBLE );
				}
			}

			mInFirstLayout = false;

			view.setSelected( false );
			return view;
		}
	}

	class StickersAdapter extends ArrayAdapter<String> {

		private LayoutInflater mLayoutInflater;
		private int mStickerResourceId;
		private int mFinalSize;
		private int mContainerHeight;

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
			mContainerHeight = mHList.getHeight() - ( mHList.getPaddingBottom() + mHList.getPaddingTop() );
			mFinalSize = (int) ( (float) mContainerHeight * ( 4.0 / 5.0 ) );

			mLogger.log( "gallery height: " + mContainerHeight );
			mLogger.log( "final size: " + mFinalSize );

			mDownloadManager.setThumbSize( mFinalSize - 10 );
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.widget.ArrayAdapter#getCount()
		 */
		@Override
		public int getCount() {
			return super.getCount();
		}

		@Override
		public View getView( int position, View convertView, ViewGroup parent ) {

			View retval = mLayoutInflater.inflate( mStickerResourceId, null );
			ImageView image = (ImageView) retval.findViewById( R.id.image );
			ImageView background = (ImageView) retval.findViewById( R.id.background );

			retval.setLayoutParams( new LinearLayout.LayoutParams( mContainerHeight, LayoutParams.MATCH_PARENT ) );

			if ( position == 0 ) {
				image.setVisibility( View.INVISIBLE );
				background.setImageResource( R.drawable.feather_sticker_paper_left_edge );
			} else if ( position >= getCount() - 1 ) {
				background.setImageResource( R.drawable.feather_sticker_paper_center_1 );
			} else {
				if ( position % 2 == 0 ) {
					background.setImageResource( R.drawable.feather_sticker_paper_center_1 );
				} else {
					background.setImageResource( R.drawable.feather_sticker_paper_center_2 );
				}
				loadStickerForImage( position, image );
			}
			return retval;
		}

		/**
		 * Load sticker for image.
		 * 
		 * @param position
		 *           the position
		 * @param view
		 *           the view
		 */
		private void loadStickerForImage( int position, ImageView view ) {
			final String sticker = getItem( position );
			mDownloadManager.loadStickerAsset( mPlugin, sticker, null, view );
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aviary.android.feather.effects.AbstractContentPanel#generateContentView(android.view.LayoutInflater)
	 */
	@Override
	protected View generateContentView( LayoutInflater inflater ) {
		return inflater.inflate( R.layout.feather_stickers_content, null );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aviary.android.feather.effects.AbstractOptionPanel#generateOptionView(android.view.LayoutInflater,
	 * android.view.ViewGroup)
	 */
	@Override
	protected ViewGroup generateOptionView( LayoutInflater inflater, ViewGroup parent ) {
		ViewGroup view = (ViewGroup) inflater.inflate( R.layout.feather_stickers_panel, parent, false );
		return view;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aviary.android.feather.effects.AbstractEffectPanel#onComplete(android.graphics.Bitmap)
	 */
	@Override
	protected void onComplete( Bitmap bitmap, MoaActionList actionlist ) {
		mTrackingAttributes.put( "stickerCount", Integer.toString( mUsedStickers.size() ) );
		mTrackingAttributes.put( "stickerNames", getUsedStickersNames().toString() );
		mTrackingAttributes.put( "packNames", getUsedPacksNames().toString() );
		super.onComplete( bitmap, actionlist );
	}

	/**
	 * Gets the used stickers names.
	 * 
	 * @return the used stickers names
	 */
	StringBuilder getUsedStickersNames() {
		StringBuilder sb = new StringBuilder();
		for ( String s : mUsedStickers ) {
			sb.append( s );
			sb.append( "," );
		}

		mLogger.log( "used stickers: " + sb.toString() );

		return sb;
	}

	/**
	 * Gets the used packs names.
	 * 
	 * @return the used packs names
	 */
	StringBuilder getUsedPacksNames() {

		SortedSet<String> map = new TreeSet<String>();

		StringBuilder sb = new StringBuilder();
		for ( String s : mUsedStickersPacks ) {
			map.add( s );
		}

		for ( String s : map ) {
			sb.append( s );
			sb.append( "," );
		}

		mLogger.log( "packs: " + sb.toString() );
		return sb;
	}

	/** The m update dialog. */
	private AlertDialog mUpdateDialog;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aviary.android.feather.library.services.PluginService.OnUpdateListener#onUpdate(android.os.Bundle)
	 */
	@Override
	public void onUpdate( Bundle delta ) {

		mLogger.info( "onUpdate: " + delta );

		if ( isActive() ) {

			if ( !validDelta( delta ) ) {
				mLogger.log( "Suppress the alert, no stickers in the delta bundle" );
				return;
			}

			if ( mUpdateDialog != null && mUpdateDialog.isShowing() ) {
				mLogger.log( "dialog is already there, skip new alerts" );
				return;
			}

			// update the available packs...
			AlertDialog dialog = null;

			switch ( mStatus ) {

				case Null:
				case Packs:

					dialog = new AlertDialog.Builder( getContext().getBaseContext() ).setMessage( R.string.sticker_pack_updated_1 ).setPositiveButton( android.R.string.ok, new DialogInterface.OnClickListener() {

						@Override
						public void onClick( DialogInterface dialog, int which ) {
							loadPacks( false );
						}
					} ).create();

					break;

				case Stickers:

					if ( stickersOnScreen() ) {

						dialog = new AlertDialog.Builder( getContext().getBaseContext() ).setMessage( R.string.sticker_pack_updated_3 ).setPositiveButton( android.R.string.yes, new DialogInterface.OnClickListener() {

							@Override
							public void onClick( DialogInterface dialog, int which ) {
								onApplyCurrent( false );
								setStatus( Status.Packs );
								updateInstalledPacks( false );
							}
						} ).setNegativeButton( android.R.string.no, new DialogInterface.OnClickListener() {

							@Override
							public void onClick( DialogInterface dialog, int which ) {
								onClearCurrent( false, false );
								setStatus( Status.Packs );
								updateInstalledPacks( false );
							}
						} ).create();

					} else {

						dialog = new AlertDialog.Builder( getContext().getBaseContext() ).setMessage( R.string.sticker_pack_updated_2 ).setPositiveButton( android.R.string.ok, new DialogInterface.OnClickListener() {

							@Override
							public void onClick( DialogInterface dialog, int which ) {
								setStatus( Status.Packs );
								updateInstalledPacks( false );
							}
						} ).create();
					}
					break;
			}

			if ( dialog != null ) {
				mUpdateDialog = dialog;
				mUpdateDialog.setCancelable( false );
				mUpdateDialog.show();
			}
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

	/** The m handler. */
	private static final Handler mHandler = new Handler() {

		@Override
		public void handleMessage( Message msg ) {

			switch ( msg.what ) {
				case AssetsAsyncDownloadManager.THUMBNAIL_LOADED:
					Thumb thumb = (Thumb) msg.obj;

					if ( thumb.image != null && thumb.bitmap != null ) {
						thumb.image.setImageDrawable( new StickerBitmapDrawable( thumb.bitmap, 10 ) );
					}
					break;
			}
		}
	};

	//
	// STATUS
	//

	/** The is animating. */
	boolean isAnimating = false;

	/**
	 * Back Button is pressed. Handle the event if we're not in the top folder list, otherwise always handle it
	 * 
	 * @return true if the event has been handled
	 */
	boolean backHandled() {

		mLogger.error( "onBackPressed: " + mStatus + " ( is_animating? " + isAnimating + " )" );

		if ( isAnimating ) return true;

		switch ( mStatus ) {
			case Null:
			case Packs:
				// we're in the root folder, so we dont need
				// to handle the back button anymore ( exit the current panel )

				if ( stickersOnScreen() ) {
					askToLeaveWithoutApply();
					return true;
				}

				return false;

			case Stickers:
				if ( mExternalPacksEnabled ) {
					// if we wont allow more stickers or if there is only
					// one pack installed then we wanna exit the current panel
					setStatus( Status.Packs );
					return true;
				} else {
					if ( stickersOnScreen() ) {
						askToLeaveWithoutApply();
						return true;
					}
				}
				return false;
		}

		return false;
	}

	/**
	 * Ask to leave without apply.
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
	 * Sets the status.
	 * 
	 * @param status
	 *           the new status
	 */
	void setStatus( Status status ) {

		mLogger.error( "setStatus: " + mStatus + " >> " + status + " ( is animating? " + isAnimating + " )" );

		if ( status != mStatus ) {

			mPrevStatus = mStatus;
			mStatus = status;

			switch ( mStatus ) {

				case Null:
					// we never want to go to this status!
					break;

				case Packs: {
					// move to the packs list view
					if ( mPrevStatus == Status.Null ) {
						loadPacks( true );
					} else if ( mPrevStatus == Status.Stickers ) {
						mViewFlipper.setDisplayedChild( 0 );
					}
				}
					break;

				case Stickers: {
					if ( mPrevStatus == Status.Null || mPrevStatus == Status.Packs ) {
						loadStickers();
					}

				}
					break;
			}
		}
	}

	/**
	 * Stickers on screen.
	 * 
	 * @return true, if successful
	 */
	private boolean stickersOnScreen() {
		final ImageViewDrawableOverlay image = (ImageViewDrawableOverlay) mImageView;
		mLogger.info( "stickers on screen?", mStatus, image.getHighlightCount() );
		return image.getHighlightCount() > 0;
	}

	@Override
	public void onDragStart( DragSource source, Object info, int dragAction ) {
		mLogger.info( "onDragStart" );
		mHList.setIsDragging( true );
	}

	@Override
	public void onDragEnd() {
		mLogger.info( "onDragEnd" );
		mHList.setIsDragging( false );
	}

	@Override
	public void onDropCompleted( View target, boolean success ) {
		mLogger.info( "onDropCompleted: " + target + ", success: " + success );
		mHList.setIsDragging( false );
	}

	@Override
	public void setDragController( DragControllerService controller ) {
		mDragController = controller;
	}

	@Override
	public DragControllerService getDragController() {
		return mDragController;
	}

	@Override
	public boolean acceptDrop( DragSource source, int x, int y, int xOffset, int yOffset, DragView dragView, Object dragInfo ) {
		return source == this;
	}

	@Override
	public void onDrop( DragSource source, int x, int y, int xOffset, int yOffset, DragView dragView, Object dragInfo ) {

		if ( dragInfo != null && dragInfo instanceof String ) {
			String sticker = (String) dragInfo;
			onApplyCurrent( true );

			float scaleFactor = dragView.getScaleFactor();

			float w = dragView.getWidth();
			float h = dragView.getHeight();

			int width = (int) ( w / scaleFactor );
			int height = (int) ( h / scaleFactor );

			mLogger.log( "sticker.size: " + width + "x" + height );

			int targetX = (int) ( x - xOffset );
			int targetY = (int) ( y - yOffset );

			RectF rect = new RectF( targetX, targetY, targetX + width, targetY + height );
			addSticker( sticker, rect );
		}
	}

	// updated installed package names
	private class UpdateInstalledPacksTask extends AsyncTask<Void, Void, FeatherPack[]> {

		private boolean mPostAnimate;

		UpdateInstalledPacksTask( boolean postAnimate ) {
			mPostAnimate = postAnimate;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mLayoutLoader.setVisibility( View.VISIBLE );
			mWorkspace.setVisibility( View.INVISIBLE );
		}

		@Override
		protected FeatherPack[] doInBackground( Void... params ) {

			if ( getContext().getBaseContext() != null ) {

				PluginService service = getContext().getService( PluginService.class );
				if ( null != service ) {
					while ( !service.isUpdated() ) {
						try {
							Thread.sleep( 50 );
						} catch ( InterruptedException e ) {
							e.printStackTrace();
						}
					}
				}

				FeatherPack[] packs = service.getInstalled( getContext().getBaseContext(), FeatherIntent.PluginType.TYPE_STICKER );
				FeatherPack[] packs2 = service.getAvailable( FeatherIntent.PluginType.TYPE_STICKER );
				int newlen = 0;

				if ( null != packs && null != packs2 ) {
					newlen = packs.length + packs2.length;
				}

				FeatherPack[] packs3 = new FeatherPack[newlen];

				if ( null != packs ) System.arraycopy( packs, 0, packs3, 0, packs.length );
				if ( null != packs2 ) System.arraycopy( packs2, 0, packs3, packs.length, packs2.length );

				mInstalledPackages.clear();
				if ( null != packs ) {
					for ( FeatherPack pack : packs ) {
						if ( !mInstalledPackages.contains( pack ) ) mInstalledPackages.add( pack.getPackageName() );
					}
				}
				return packs3;
			}
			return new FeatherPack[0];
		}

		@Override
		protected void onPostExecute( FeatherPack[] result ) {
			super.onPostExecute( result );

			StickersPacksAdapter adapter = new StickersPacksAdapter( getContext().getBaseContext(), R.layout.feather_workspace_screen, R.layout.feather_sticker_pack, result );
			mWorkspace.setAdapter( adapter );
			mWorkspaceIndicator.setVisibility( mWorkspace.getTotalPages() > 1 ? View.VISIBLE : View.INVISIBLE );
			mDownloadManager.clearCache();
			mLayoutLoader.setVisibility( View.GONE );

			if ( mPostAnimate ) {
				startFirstAnimation();
			} else {
				mWorkspace.setVisibility( View.VISIBLE );
			}
		}

	}
}
