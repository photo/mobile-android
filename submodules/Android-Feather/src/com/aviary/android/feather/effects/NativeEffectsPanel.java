package com.aviary.android.feather.effects;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import org.json.JSONException;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.TranslateAnimation;
import android.widget.AbsoluteLayout;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher.ViewFactory;
import com.aviary.android.feather.Constants;
import com.aviary.android.feather.R;
import com.aviary.android.feather.graphics.ExternalFilterPackDrawable;
import com.aviary.android.feather.library.content.FeatherIntent;
import com.aviary.android.feather.library.content.FeatherIntent.PluginType;
import com.aviary.android.feather.library.filters.EffectFilter;
import com.aviary.android.feather.library.filters.FilterLoaderFactory;
import com.aviary.android.feather.library.filters.FilterLoaderFactory.Filters;
import com.aviary.android.feather.library.graphics.animation.TransformAnimation;
import com.aviary.android.feather.library.graphics.drawable.FakeBitmapDrawable;
import com.aviary.android.feather.library.moa.Moa;
import com.aviary.android.feather.library.moa.MoaActionList;
import com.aviary.android.feather.library.moa.MoaResult;
import com.aviary.android.feather.library.plugins.FeatherExternalPack;
import com.aviary.android.feather.library.plugins.FeatherInternalPack;
import com.aviary.android.feather.library.plugins.FeatherPack;
import com.aviary.android.feather.library.plugins.PluginManager;
import com.aviary.android.feather.library.plugins.PluginManager.ExternalPlugin;
import com.aviary.android.feather.library.plugins.PluginManager.IPlugin;
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
import com.aviary.android.feather.utils.TypefaceUtils;
import com.aviary.android.feather.utils.UIUtils;
import com.aviary.android.feather.widget.HorizontalFixedListView;
import com.aviary.android.feather.widget.ImageSwitcher;
import com.aviary.android.feather.widget.SwipeView;
import com.aviary.android.feather.widget.SwipeView.OnSwipeListener;
import com.aviary.android.feather.widget.wp.CellLayout;
import com.aviary.android.feather.widget.wp.CellLayout.CellInfo;
import com.aviary.android.feather.widget.wp.Workspace;
import com.aviary.android.feather.widget.wp.WorkspaceIndicator;

/**
 * The Class NativeEffectsPanel.
 */
@SuppressWarnings("deprecation")
public class NativeEffectsPanel extends AbstractContentPanel implements ViewFactory, OnUpdateListener, OnSwipeListener {

	/** The current task. */
	private RenderTask mCurrentTask;

	/** The current selected filter label. */
	private String mSelectedLabel = "undefined";

	/** The current selected filter view. */
	private View mSelectedView;

	/** Panel is rendering. */
	private volatile Boolean mIsRendering = false;

	/** The small preview used for fast rendering. */
	private Bitmap mSmallPreview;

	private static final int PREVIEW_SCALE_FACTOR = 4;

	/** enable/disable fast preview. */
	private boolean enableFastPreview = false;

	private PluginService mPluginService;

	/** The horizontal filter list view. */
	private HorizontalFixedListView mHList;

	/** The main image switcher. */
	private ImageSwitcher mImageSwitcher;

	/** The cannister workspace. */
	private Workspace mWorkspace;

	/** The cannister workspace indicator. */
	private WorkspaceIndicator mWorkspaceIndicator;

	/** The number of workspace cols. */
	private int mWorkspaceCols;

	/** The number of workspace items per page. */
	private int mWorkspaceItemsPerPage;

	private View mWorkspaceContainer;

	/** The big cannister view. */
	private AbsoluteLayout mCannisterView;

	/** panel is animating. */
	private boolean mIsAnimating;

	/** The default animation duration in. */
	private int mAnimationDurationIn = 300;

	/** The animation film duration in. */
	private int mAnimationFilmDurationIn = 200;

	private int mAnimationFilmDurationOut = 200;

	private Interpolator mDecelerateInterpolator;

	private boolean mExternalPacksEnabled = true;

	/** create a reference to the update alert dialog. This to prevent multiple alert messages */
	private AlertDialog mUpdateDialog;

	private MoaActionList mActions = null;

	private PreferenceService mPrefService;

	private int mFilterCellWidth = 80;

	private List<String> mInstalledPackages;
	
	private View mLayoutLoader;

	private static final int toastDuration = Toast.LENGTH_SHORT;

	/* typeface for textviews */
	Typeface mFiltersTypeface;

	private static enum Status {
		Null, // home
		Packs, // pack display
		Filters, // filters
	}

	/** The current panel status. */
	private Status mStatus = Status.Null;

	/** The previous panel status. */
	private Status mPrevStatus = Status.Null;

	/**
	 * Instantiates a new native effects panel.
	 * 
	 * @param context
	 *           the context
	 */
	public NativeEffectsPanel( EffectContext context ) {
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

		mPluginService = getContext().getService( PluginService.class );
		mPluginService.registerOnUpdateListener( this );

		mPrefService = getContext().getService( PreferenceService.class );

		mWorkspaceIndicator = (WorkspaceIndicator) mOptionView.findViewById( R.id.workspace_indicator );
		mWorkspace = (Workspace) mOptionView.findViewById( R.id.workspace );
		mWorkspace.setHapticFeedbackEnabled( false );
		mWorkspace.setIndicator( mWorkspaceIndicator );
		
		mLayoutLoader = mOptionView.findViewById( R.id.layout_loader );

		mHList = (HorizontalFixedListView) getOptionView().findViewById( R.id.gallery );
		mWorkspaceContainer = mOptionView.findViewById( R.id.workspace_container );
		mCannisterView = (AbsoluteLayout) mOptionView.findViewById( R.id.cannister_container );
		initWorkspace();

		enableFastPreview = Constants.getFastPreviewEnabled();

		mExternalPacksEnabled = Constants.getValueFromIntent( Constants.EXTRA_EFFECTS_ENABLE_EXTERNAL_PACKS, true );

		mImageSwitcher = (ImageSwitcher) getContentView().findViewById( R.id.switcher );
		mImageSwitcher.setSwitchEnabled( enableFastPreview );
		mImageSwitcher.setFactory( this );

		ConfigService config = getContext().getService( ConfigService.class );
		String fontPack = config.getString( R.string.feather_effect_pack_font );
		if ( null != fontPack && fontPack.length() > 1 ) {
			try {
				mFiltersTypeface = TypefaceUtils.createFromAsset( getContext().getBaseContext().getAssets(), fontPack );
			} catch ( Throwable t ) {}
		}

		mDecelerateInterpolator = AnimationUtils.loadInterpolator( getContext().getBaseContext(), android.R.anim.decelerate_interpolator );

		if ( enableFastPreview ) {
			try {
				mSmallPreview = Bitmap.createBitmap( mBitmap.getWidth() / PREVIEW_SCALE_FACTOR, mBitmap.getHeight() / PREVIEW_SCALE_FACTOR, Config.ARGB_8888 );
				mImageSwitcher.setImageBitmap( mBitmap, true, null, Float.MAX_VALUE );
				mImageSwitcher.setInAnimation( AnimationUtils.loadAnimation( getContext().getBaseContext(), android.R.anim.fade_in ) );
				mImageSwitcher.setOutAnimation( AnimationUtils.loadAnimation( getContext().getBaseContext(), android.R.anim.fade_out ) );
			} catch ( OutOfMemoryError e ) {
				enableFastPreview = false;
				mImageSwitcher.setImageBitmap( mBitmap, true, getContext().getCurrentImageViewMatrix(), Float.MAX_VALUE );
			}
		} else {
			mImageSwitcher.setImageBitmap( mBitmap, true, getContext().getCurrentImageViewMatrix(), Float.MAX_VALUE );
		}
		mImageSwitcher.setAnimateFirstView( false );

		mPreview = BitmapUtils.copy( mBitmap, Bitmap.Config.ARGB_8888 );

		if ( mExternalPacksEnabled ) {
			createFirstAnimation();
		} else {
			mLayoutLoader.setVisibility( View.GONE );
			mWorkspaceContainer.setVisibility( View.GONE );
		}

		SwipeView mSwipeView = (SwipeView) getContentView().findViewById( R.id.swipeview ); // add an overlaying view that detects for
		mSwipeView.setOnSwipeListener( this );

	}

	@Override
	protected void onDispose() {
		super.onDispose();
		mWorkspace.setAdapter( null );
		mHList.setAdapter( null );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aviary.android.feather.effects.AbstractEffectPanel#onActivate()
	 */
	@Override
	public void onActivate() {
		super.onActivate();

		mInstalledPackages = Collections.synchronizedList( new ArrayList<String>() );

		ConfigService config = getContext().getService( ConfigService.class );
		mAnimationDurationIn = config.getInteger( R.integer.feather_config_mediumAnimTime ) + 100;
		mAnimationFilmDurationIn = config.getInteger( R.integer.feather_config_shortAnimTime ) + 100;
		mAnimationFilmDurationOut = config.getInteger( R.integer.feather_config_shortAnimTime );
		mFilterCellWidth = config.getDimensionPixelSize( R.dimen.feather_effects_cell_width );

		if ( mExternalPacksEnabled ) {
			setStatus( Status.Packs );
		} else {
			startDefaultAnimation();
		}
	}

	private void startDefaultAnimation() {
		FeatherInternalPack thisPack = FeatherInternalPack.getDefault( getContext().getBaseContext() );
		InternalPlugin plugin = (InternalPlugin) PluginManager.create( getContext().getBaseContext(), thisPack );

		Drawable icon = plugin.getIcon( PluginType.TYPE_FILTER );
		ImageView newView = new ImageView( getContext().getBaseContext() );
		
		float destW, destH;
		float iconW = icon.getIntrinsicWidth();
		float iconH = icon.getIntrinsicHeight();
		float iconR = iconW / iconH;

		if ( getOptionView().findViewById( R.id.workspace_container ) != null ) {
			destH = getOptionView().findViewById( R.id.workspace_container ).getHeight();
		} else {
			destH = iconH;
		}
		destH = Math.max( iconH, destH );
		destW = destH * iconR;

		Rect r = new Rect();
		Point offset = new Point();
		mOptionView.getChildVisibleRect( mOptionView.findViewById( R.id.RelativeLayout01 ), r, offset );
		
		Resources res = getContext().getBaseContext().getResources();
		final float shadow_offset = res.getDimensionPixelSize( R.dimen.feather_options_panel_height_shadow );

		AbsoluteLayout.LayoutParams params = new AbsoluteLayout.LayoutParams( (int) destW, (int) destH, 0, 0 );
		newView.setLayoutParams( params );
		newView.setScaleType( ImageView.ScaleType.FIT_XY );
		newView.setImageDrawable( icon );

		final float startX = Constants.SCREEN_WIDTH;
		final float endX = Constants.SCREEN_WIDTH - ( destW / 2 );
		
		final float startY = -r.top + offset.y - shadow_offset;
		final float endY = startY;

		Animation animation = new TranslateAnimation( TranslateAnimation.ABSOLUTE, startX, TranslateAnimation.ABSOLUTE, endX, TranslateAnimation.ABSOLUTE, startY, TranslateAnimation.ABSOLUTE, endY );
		animation.setInterpolator( mDecelerateInterpolator );
		animation.setDuration( mAnimationDurationIn / 2 );
		animation.setFillEnabled( true );
		animation.setFillBefore( true );
		animation.setFillAfter( true );
		startCannisterAnimation( newView, animation, plugin );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aviary.android.feather.effects.AbstractEffectPanel#onProgressEnd()
	 */
	@Override
	protected void onProgressEnd() {
		if ( !enableFastPreview ) {
			super.onProgressModalEnd();
		} else {
			super.onProgressEnd();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aviary.android.feather.effects.AbstractEffectPanel#onProgressStart()
	 */
	@Override
	protected void onProgressStart() {
		if ( !enableFastPreview ) {
			super.onProgressModalStart();
		} else {
			super.onProgressStart();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aviary.android.feather.effects.AbstractEffectPanel#onGenerateResult()
	 */
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
	public void onConfigurationChanged( Configuration newConfig, Configuration oldConfig ) {
		super.onConfigurationChanged( newConfig, oldConfig );

		mLogger.info( "onConfigurationChanged: " + newConfig.orientation + ", " + oldConfig.orientation );

		if ( newConfig.orientation != oldConfig.orientation ) {
			reloadCurrentStatus();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aviary.android.feather.effects.AbstractOptionPanel#generateOptionView(android.view.LayoutInflater,
	 * android.view.ViewGroup)
	 */
	@Override
	protected ViewGroup generateOptionView( LayoutInflater inflater, ViewGroup parent ) {
		ViewGroup view = (ViewGroup) inflater.inflate( R.layout.feather_native_effects_panel, parent, false );
		return view;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.ViewSwitcher.ViewFactory#makeView()
	 */
	@Override
	public View makeView() {

		ImageViewTouch view = new ImageViewTouch( getContext().getBaseContext(), null );
		view.setBackgroundColor( 0x00000000 );
		view.setDoubleTapEnabled( false );

		if ( enableFastPreview ) {
			view.setScrollEnabled( false );
			view.setScaleEnabled( false );
		}

		view.setLayoutParams( new ImageSwitcher.LayoutParams( LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT ) );
		return view;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aviary.android.feather.effects.AbstractEffectPanel#onDestroy()
	 */
	@Override
	public void onDestroy() {
		if ( mSmallPreview != null && !mSmallPreview.isRecycled() ) mSmallPreview.recycle();
		mSmallPreview = null;
		super.onDestroy();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aviary.android.feather.effects.AbstractEffectPanel#onDeactivate()
	 */
	@Override
	public void onDeactivate() {
		onProgressEnd();
		mPluginService.removeOnUpdateListener( this );
		super.onDeactivate();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aviary.android.feather.effects.AbstractContentPanel#getContentDisplayMatrix()
	 */
	@Override
	public Matrix getContentDisplayMatrix() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aviary.android.feather.effects.AbstractEffectPanel#onBackPressed()
	 */
	@Override
	public boolean onBackPressed() {
		if ( backHandled() ) return true;
		return super.onBackPressed();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aviary.android.feather.effects.AbstractEffectPanel#onCancelled()
	 */
	@Override
	public void onCancelled() {
		killCurrentTask();
		mIsRendering = false;
		super.onCancelled();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aviary.android.feather.effects.AbstractEffectPanel#getIsChanged()
	 */
	@Override
	public boolean getIsChanged() {
		return super.getIsChanged() || mIsRendering == true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aviary.android.feather.effects.AbstractContentPanel#generateContentView(android.view.LayoutInflater)
	 */
	@Override
	protected View generateContentView( LayoutInflater inflater ) {
		return inflater.inflate( R.layout.feather_native_effects_content, null );
	}

	/**
	 * Kill current task.
	 * 
	 * @return true, if successful
	 */
	boolean killCurrentTask() {
		if ( mCurrentTask != null ) {
			onProgressEnd();
			return mCurrentTask.cancel( true );
		}
		return false;
	}

	/**
	 * Load effects.
	 */
	private void loadEffects( final InternalPlugin plugin ) {
		String[] filters = plugin.listFilters();

		if ( filters != null ) {

			String[] listcopy = new String[filters.length + 2];
			System.arraycopy( filters, 0, listcopy, 1, filters.length );

			mSelectedLabel = "undefined";
			mSelectedView = null;

			FiltersAdapter adapter = new FiltersAdapter( getContext().getBaseContext(), R.layout.feather_filter_thumb, plugin, listcopy );
			mFiltersAdapter = adapter;

			mHList.setHideLastChild( true );
			mHList.setAdapter( adapter );
			mHList.setOnItemClickListener( new OnItemClickListener() {

				@Override
				public void onItemClick( AdapterView<?> parent, View view, int position, long id ) {
					if ( !view.isSelected() ) {
						setSelected( view, position, (String) parent.getAdapter().getItem( position ) );
					}
				}
			} );
		}
	}

	FiltersAdapter mFiltersAdapter;

	/**
	 * Render the current effect.
	 * 
	 * @param tag
	 *           the tag
	 */
	void renderEffect( String tag ) {
		mLogger.log( "tag: " + tag );
		killCurrentTask();
		mCurrentTask = new RenderTask( tag );
		mCurrentTask.execute();
	}

	int mSelectedPosition = 1;

	/**
	 * Sets the selected.
	 * 
	 * @param view
	 *           the view
	 * @param position
	 *           the position
	 * @param label
	 *           the label
	 */
	void setSelected( View view, int position, String label ) {
		mLogger.info( "setSelected: " + view + "," + position + "," + label );

		mSelectedPosition = position;

		if ( mSelectedView != null ) {
			mSelectedView.setSelected( false );
			ViewHolder holder = (ViewHolder) mSelectedView.getTag();
			if ( null != holder ) {
				holder.image.setAlpha( 127 );
			}
		}

		// mSelectedIndex = position;
		mSelectedLabel = label;
		mSelectedView = view;

		if ( view != null ) {

			view.setSelected( true );

			ViewHolder holder = (ViewHolder) view.getTag();
			if ( null != holder ) {
				holder.image.setAlpha( 255 );
			}
		}
		// String tag = (String) mHList.getAdapter().getItem( position );
		renderEffect( label );
	}

	/**
	 * The Class RenderTask.
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
			mLogger.info( "RenderTask::ctor", tag );
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.aviary.android.feather.library.utils.UserTask#onPreExecute()
		 */
		@Override
		public void onPreExecute() {
			super.onPreExecute();

			EffectFilter filter = (EffectFilter) FilterLoaderFactory.get( Filters.EFFECTS );
			filter.setEffectName( mEffect );
			filter.setBorders( Constants.getValueFromIntent( Constants.EXTRA_EFFECTS_BORDERS_ENABLED, true ) );

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

			if ( !enableFastPreview ) {
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

			if ( enableFastPreview && mSmallNativeResult != null ) {
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

			mLogger.warning( "onCancelled", mEffect );
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
	 * The Class GenerateResultTask.
	 */
	class GenerateResultTask extends AsyncTask<Void, Void, Void> {

		/** The m progress. */
		ProgressDialog mProgress = new ProgressDialog( getContext().getBaseContext() );

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onPreExecute()
		 */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mProgress.setTitle( getContext().getBaseContext().getString( R.string.feather_loading_title ) );
			mProgress.setMessage( getContext().getBaseContext().getString( R.string.effect_loading_message ) );
			mProgress.setIndeterminate( true );
			mProgress.setCancelable( false );
			mProgress.show();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#doInBackground(Params[])
		 */
		@Override
		protected Void doInBackground( Void... params ) {

			mLogger.info( "GenerateResultTask::doInBackground", mIsRendering );

			while ( mIsRendering ) {
				// mLogger.log( "waiting...." );
			}

			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute( Void result ) {
			super.onPostExecute( result );

			mLogger.info( "GenerateResultTask::onPostExecute" );

			if ( getContext().getBaseActivity().isFinishing() ) return;
			if ( mProgress.isShowing() ) mProgress.dismiss();

			onComplete( mPreview, mActions );
		}
	}

	class ViewHolder {

		ImageView image;
		TextView text;
		View container;
	};

	/**
	 * The main Adapter for the film horizontal list view.
	 */
	class FiltersAdapter extends ArrayAdapter<String> {

		private LayoutInflater mLayoutInflater;
		private int mFilterResourceId;
		private int mCellWidth;
		private WeakReference<InternalPlugin> mPlugin;

		/**
		 * Instantiates a new filters adapter.
		 * 
		 * @param context
		 *           the context
		 * @param textViewResourceId
		 *           the text view resource id
		 * @param objects
		 *           the objects
		 */
		public FiltersAdapter( Context context, int textViewResourceId, final InternalPlugin plugin, String[] objects ) {
			super( context, textViewResourceId, objects );
			mFilterResourceId = textViewResourceId;
			mPlugin = new WeakReference<InternalPlugin>( plugin );
			mLayoutInflater = UIUtils.getLayoutInflater();
			mCellWidth = Constants.SCREEN_WIDTH / UIUtils.getScreenOptimalColumns( mFilterCellWidth );
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

		public CharSequence getFilterName( int position ) {
			String item = getItem( position );
			if ( null != mPlugin.get() ) {
				CharSequence text = mPlugin.get().getFilterLabel( item );
				return text;
			} else {
				return "";
			}
		}

		@Override
		public View getView( int position, View convertView, ViewGroup parent ) {

			View view;
			ViewHolder holder;
			boolean selected = false;

			if ( convertView != null ) {
				view = convertView;
				holder = (ViewHolder) convertView.getTag();
			} else {
				view = mLayoutInflater.inflate( mFilterResourceId, parent, false );
				holder = new ViewHolder();
				holder.image = (ImageView) view.findViewById( R.id.image );
				holder.text = (TextView) view.findViewById( R.id.text );
				holder.container = view.findViewById( R.id.container );

				view.setTag( holder );
				view.setLayoutParams( new LinearLayout.LayoutParams( mCellWidth, LinearLayout.LayoutParams.MATCH_PARENT ) );
			}

			if ( position == 0 ) {
				holder.container.setVisibility( View.INVISIBLE );
				view.setBackgroundResource( R.drawable.feather_film_left );
			} else if ( position > getCount() - 2 ) {
				holder.container.setVisibility( View.INVISIBLE );
				view.setBackgroundResource( R.drawable.feather_film_center );
			} else {
				holder.container.setVisibility( View.VISIBLE );

				String item = getItem( position );

				Drawable icon;
				CharSequence text;

				if ( null != mPlugin.get() ) {
					icon = mPlugin.get().getFilterDrawable( item );
					text = mPlugin.get().getFilterLabel( item );
				} else {
					icon = null;
					text = "";
				}

				selected = item.equals( mSelectedLabel );

				if ( icon != null )
					holder.image.setImageDrawable( icon );
				else
					holder.image.setImageResource( R.drawable.feather_plugin_filter_undefined_thumb );

				view.setBackgroundResource( R.drawable.feather_film_center );
				holder.text.setText( text );

				if ( null != mFiltersTypeface ) {
					holder.text.setTypeface( mFiltersTypeface );
				}

			}

			view.setSelected( selected );
			holder.image.setAlpha( selected ? 255 : 127 );

			if ( mSelectedView == null && selected ) {
				mSelectedView = view;
			}

			return view;
		}
	}

	/** The m cannister on click listener. */
	private View.OnClickListener mCannisterOnClickListener = new View.OnClickListener() {

		@Override
		public void onClick( View clickView ) {

			Object tag = clickView.getTag();
			if ( tag == null ) {
				getContext().downloadPlugin( FeatherIntent.PLUGIN_BASE_PACKAGE + "*", FeatherIntent.PluginType.TYPE_FILTER );
				return;
			}

			if ( tag instanceof FeatherExternalPack ) {
				getContext().downloadPlugin( ( (FeatherExternalPack) tag ).getPackageName(), FeatherIntent.PluginType.TYPE_FILTER );
				return;
			}

			if ( !( tag instanceof FeatherInternalPack ) ) {
				mLogger.warning( "invalid view.tag!" );
				return;
			}

			final FeatherInternalPack featherPack = (FeatherInternalPack) tag;
			final InternalPlugin plugin = (InternalPlugin) PluginManager.create( getContext().getBaseContext(), featherPack );

			final ImageView image = (ImageView) clickView.findViewById( R.id.image );
			final Drawable vIcon = image.getDrawable();

			if ( plugin == null ) {
				onGenericError( R.string.feather_effects_error_loading_pack );
				return;
			}

			// then be sure the pack selected is valid
			boolean loaded = plugin.listFilters().length > 0;
			if ( !loaded ) {
				onGenericError( R.string.feather_effects_error_loading_pack );
				return;
			}

			// and finally verify the pack can be installed
			// TODO: Move install external effects to a separate thread
			PluginError error;
			if ( plugin.isExternal() ) {
				error = installPlugin( featherPack.getPackageName(), featherPack.getPluginType() );
			} else {
				error = PluginError.NoError;
			}

			if ( error != PluginError.NoError ) {
				final String errorString = getError( error );

				if ( error == PluginError.PluginTooOldError ) {

					OnClickListener yesListener = new OnClickListener() {

						@Override
						public void onClick( DialogInterface dialog, int which ) {
							getContext().downloadPlugin( featherPack.getPackageName(), PluginType.TYPE_FILTER );
						}
					};
					onGenericError( errorString, R.string.feather_update, yesListener, android.R.string.cancel, null );
				} else {
					onGenericError( errorString );
				}
				return;
			}

			trackPackage( featherPack.getPackageName() );

			float destW, destH;
			float iconW = vIcon.getIntrinsicWidth();
			float iconH = vIcon.getIntrinsicHeight();
			float iconR = iconW / iconH;

			if ( getOptionView().findViewById( R.id.workspace_container ) != null ) {
				destH = getOptionView().findViewById( R.id.workspace_container ).getHeight();
			} else {
				destH = iconH;
			}
			destH = Math.max( iconH, destH );
			destW = destH * iconR;

			final float scalex = destW / image.getWidth();
			final float scaley = destH / image.getHeight();
			final float scale = Math.max( scalex, scaley );

			Rect r = new Rect();
			Point offset = new Point();
			CellLayout cell = (CellLayout) clickView.getParent();

			( (ViewGroup) mOptionView ).getChildVisibleRect( clickView, r, offset );

			int top = -r.top;
			top += cell.getTopPadding();

			clickView.getGlobalVisibleRect( r );
			ImageView newView = new ImageView( getContext().getBaseContext() );
			newView.setScaleType( image.getScaleType() );
			newView.setImageDrawable( vIcon );

			AbsoluteLayout.LayoutParams params = new AbsoluteLayout.LayoutParams( image.getWidth(), image.getHeight(), 0, 0 );
			newView.setLayoutParams( params );

			final float startX = r.left;
			final float endX = Constants.SCREEN_WIDTH - ( (float) image.getWidth() * scale ) / 2;

			final float startY = r.top + top;
			final float endY = startY;

			Animation animation = new TransformAnimation( TranslateAnimation.ABSOLUTE, startX, TranslateAnimation.ABSOLUTE, endX, TranslateAnimation.ABSOLUTE, startY, TranslateAnimation.ABSOLUTE, endY, 1, scale, 1, scale );
			animation.setInterpolator( mDecelerateInterpolator );
			animation.setDuration( mAnimationDurationIn );
			animation.setFillEnabled( true );
			animation.setFillBefore( true );
			animation.setFillAfter( true );
			startCannisterAnimation( newView, animation, plugin );
		}
	};

	/**
	 * The main Adapter for the cannister workspace view.
	 */
	class FiltersPacksAdapter extends ArrayAdapter<FeatherPack> {

		int screenId, cellId;
		LayoutInflater mLayoutInflater;
		boolean mInFirstLayout = true;

		/** The default get more icon. */
		Bitmap mShadow, mEffect, mEffectFree;
		Typeface mTypeface;

		/** The default get more label. */
		String mGetMoreLabel;

		/**
		 * Instantiates a new filters packs adapter.
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
		public FiltersPacksAdapter( Context context, int resource, int textViewResourceId, FeatherPack objects[] ) {
			super( context, resource, textViewResourceId, objects );
			screenId = resource;
			cellId = textViewResourceId;
			mLayoutInflater = UIUtils.getLayoutInflater();
			mGetMoreLabel = context.getString( R.string.get_more );
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

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.widget.ArrayAdapter#getItem(int)
		 */
		@Override
		public FeatherPack getItem( int position ) {
			if ( position < super.getCount() ) {
				return super.getItem( position );
			} else {
				return null;
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.widget.ArrayAdapter#getItemId(int)
		 */
		@Override
		public long getItemId( int position ) {
			return super.getItemId( position );
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
			int count = super.getCount();

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

				if ( index < ( count ) ) {

					final FeatherPack featherPack = getItem( index );

					Drawable icon;
					CharSequence label = "";

					ensureBitmapTemplate();

					if ( featherPack == null ) {
						label = mGetMoreLabel;
						icon = new ExternalFilterPackDrawable( "Get More", "AV", 6, 0xFF5fcbef, mTypeface, mShadow, mEffect );
					} else {

						final IPlugin plugin = PluginManager.create( getContext(), featherPack );

						if ( plugin.isLocal() ) {
							label = plugin.getLabel( PluginType.TYPE_FILTER );
							icon = plugin.getIcon( PluginType.TYPE_FILTER );
						} else {

							label = plugin.getLabel( PluginType.TYPE_FILTER );
							ExternalPlugin externalPlugin = (ExternalPlugin) plugin;

							if ( externalPlugin.isFree() ) {
								ensureBitmapTemplateFree();
								icon = new ExternalFilterPackDrawable( label.toString(), externalPlugin.getShortTitle(), externalPlugin.getNumFilters(), externalPlugin.getDisplayColor(), mTypeface, mShadow, mEffectFree );
							} else {
								icon = new ExternalFilterPackDrawable( label.toString(), externalPlugin.getShortTitle(), externalPlugin.getNumFilters(), externalPlugin.getDisplayColor(), mTypeface, mShadow, mEffect );
							}
						}
					}

					final ImageView image = (ImageView) itemView.findViewById( R.id.image );
					final TextView text = (TextView) itemView.findViewById( R.id.text );

					if ( null != mFiltersTypeface ) {
						text.setTypeface( mFiltersTypeface );
					}

					image.setImageDrawable( icon );
					text.setText( label );
					itemView.setTag( featherPack );

					itemView.setOnClickListener( mCannisterOnClickListener );
					itemView.setVisibility( View.VISIBLE );
				} else {
					itemView.setVisibility( View.INVISIBLE );
				}

				index++;
			}

			mInFirstLayout = false;

			view.setSelected( false );
			return view;
		}

		private void ensureBitmapTemplate() {
			if ( null == mShadow ) {
				mShadow = ( (BitmapDrawable) getContext().getResources().getDrawable( R.drawable.feather_external_filters_template_shadow ) ).getBitmap();
				mEffect = ( (BitmapDrawable) getContext().getResources().getDrawable( R.drawable.feather_external_filters_template ) ).getBitmap();
			}
			if ( null == mTypeface ) {
				try {
					mTypeface = TypefaceUtils.createFromAsset( getContext().getAssets(), "fonts/HelveticaBold.ttf" );
				} catch ( Exception e ) {
					mTypeface = Typeface.DEFAULT_BOLD;
				}
			}
		}

		private void ensureBitmapTemplateFree() {
			if ( null == mEffectFree ) {
				mEffectFree = ( (BitmapDrawable) getContext().getResources().getDrawable( R.drawable.feather_external_filters_template_free ) ).getBitmap();
			}
		}
	}

	private void initWorkspace() {
		mWorkspaceCols = getContext().getBaseContext().getResources().getInteger( R.integer.featherfilterPacksCount );
		mWorkspaceItemsPerPage = mWorkspaceCols;
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

	/**
	 * Track only the first time the package is started
	 * 
	 * @param packageName
	 */
	protected void trackPackage( String packageName ) {

		if ( !mPrefService.containsValue( "effects." + packageName ) ) {
			if ( !getContext().getBaseContext().getPackageName().equals( packageName ) ) {
				mPrefService.putString( "effects." + packageName, packageName );
				HashMap<String, String> map = new HashMap<String, String>();
				map.put( "assetType", "effects" );
				map.put( "assetID", packageName );
				Tracker.recordTag( "content: purchased", map );
			}
		}

		mTrackingAttributes.put( "packName", packageName );
	}


	/**
	 * Update installed packs.
	 */
	private void updateInstalledPacks( boolean animate ) {
		mWorkspace.setAdapter( null );
		UpdateInstalledPacksTask task = new UpdateInstalledPacksTask( animate );
		task.execute();
	}

	/**
	 * Gets the installed packs.
	 * 
	 * @return the installed packs
	 */
	private FeatherInternalPack[] getInstalledPacks() {
		return mPluginService.getInstalled( getContext().getBaseContext(), FeatherIntent.PluginType.TYPE_FILTER );
	}

	/**
	 * Gets the list of all the packs available on the market
	 * 
	 * @param type
	 * @return
	 */
	private FeatherExternalPack[] getAvailablePacks( final int type ) {
		return mPluginService.getAvailable( type );
	}

	/**
	 * Back handled.
	 * 
	 * @return true, if successful
	 */
	boolean backHandled() {
		if ( mIsAnimating ) return true;
		if ( !mExternalPacksEnabled ) return false;

		killCurrentTask();

		switch ( mStatus ) {
			case Null:
			case Packs:
				return false;

			case Filters:
				setStatus( Status.Packs );
				return true;
		}

		return false;
	}

	private void reloadCurrentStatus() {
		mLogger.info( "reloadCurrentStatus" );
		initWorkspace();

		if ( mStatus == Status.Packs ) {
			updateInstalledPacks( false );
		} else if ( mStatus == Status.Filters ) {

			View view = mCannisterView.getChildAt( 0 );
			if ( null != view && view instanceof ImageView ) {
				ImageView newView = (ImageView) view;
				newView.clearAnimation();

				Drawable icon = newView.getDrawable();
				if ( null != icon ) {
					Resources res = getContext().getBaseContext().getResources();
					final float height = res.getDimension( R.dimen.feather_options_panel_height_with_shadow ) + 25;
					final float offset = res.getDimension( R.dimen.feather_options_panel_height_shadow );
					final float ratio = (float) icon.getIntrinsicWidth() / (float) icon.getIntrinsicHeight();
					final float width = height * ratio;

					final float endX = Constants.SCREEN_WIDTH - ( width / 2 );
					final float endY = offset * 2;

					AbsoluteLayout.LayoutParams params = new AbsoluteLayout.LayoutParams( (int) width, (int) height, (int) endX, (int) endY );
					newView.setLayoutParams( params );
				}
			}
		}
	}

	/**
	 * Set the new status for this panel
	 * 
	 * @param status
	 */
	void setStatus( Status status ) {
		setStatus( status, null );
	}

	/**
	 * Change the status passing a custom object data.
	 * 
	 * @param status
	 *           the new status
	 * @param object
	 *           a custom user object
	 */
	void setStatus( Status status, InternalPlugin plugin ) {
		mLogger.error( "setStatus: " + mStatus + " >> " + status );

		if ( status != mStatus ) {

			mPrevStatus = mStatus;
			mStatus = status;

			switch ( mStatus ) {

				case Null:
					break;

				case Packs: {
					if ( mPrevStatus == Status.Null ) {
						updateInstalledPacks( true );
					} else if ( mPrevStatus == Status.Filters ) {
						// going back, just switch visibility...
						restorePacksAnimation();
					}
				}
					break;

				case Filters: {

					if ( null == plugin ) {
						mLogger.error( "plugin instance is null!" );
						return;
					}

					if ( mPrevStatus == Status.Packs ) {
						loadEffects( plugin );
						startEffectsSliderAnimation( plugin.getLabel( PluginType.TYPE_FILTER ) );
						mSelectedPosition = 0;

					} else if ( mPrevStatus == Status.Null ) {
						loadEffects( plugin );
						startEffectsSliderAnimation( plugin.getLabel( PluginType.TYPE_FILTER ) );
					}
				}
					break;
			}
		}
	}

	Animation mCannisterAnimationIn;

	/**
	 * Firt animation when panel is loaded and it's ready to display the effects packs.
	 */
	private void startFirstAnimation() {
		getHandler().postDelayed( new Runnable() {

			@Override
			public void run() {
				postStartFirstAnimation();
			}
		}, 200 );
		return;
	}

	private void postStartFirstAnimation() {
		mIsAnimating = true;

		if ( mWorkspace.getChildCount() < 1 ) {
			getHandler().postDelayed( new Runnable() {

				@Override
				public void run() {
					startFirstAnimation();
				}
			}, 10 );

			return;
		}

		mWorkspace.setVisibility( View.VISIBLE );
		mWorkspace.setCacheEnabled( true );
		mWorkspace.enableChildrenCache( 0, 1 );
		mWorkspace.startAnimation( mCannisterAnimationIn );
	}

	private void createFirstAnimation() {
		mCannisterAnimationIn = AnimationUtils.loadAnimation( getContext().getBaseContext(), R.anim.feather_push_up_cannister );
		mCannisterAnimationIn.setInterpolator( new DecelerateInterpolator( 0.4f ) );
		mCannisterAnimationIn.setAnimationListener( new AnimationListener() {

			@Override
			public void onAnimationStart( Animation animation ) {}

			@Override
			public void onAnimationRepeat( Animation animation ) {}

			@Override
			public void onAnimationEnd( Animation animation ) {
				mIsAnimating = false;
				getContentView().setVisibility( View.VISIBLE );
				contentReady();

				mWorkspace.clearChildrenCache();
				mWorkspace.setCacheEnabled( false );
				mWorkspace.requestLayout();
				mWorkspace.postInvalidate();
			}
		} );
	}

	private void startCannisterAnimation( View view, Animation animation, final InternalPlugin plugin ) {
		mLogger.info( "startCannisterAnimation" );
		mIsAnimating = true;

		animation.setAnimationListener( new AnimationListener() {

			@Override
			public void onAnimationStart( Animation animation ) {}

			@Override
			public void onAnimationRepeat( Animation animation ) {}

			@Override
			public void onAnimationEnd( Animation animation ) {
				setStatus( Status.Filters, plugin );
			}
		} );

		mWorkspaceContainer.setVisibility( View.INVISIBLE );
		mCannisterView.removeAllViews();
		mCannisterView.addView( view );
		view.startAnimation( animation );
	}

	/** The slide in left animation. */
	private Animation mSlideInLeftAnimation;

	/** The slide out right animation. */
	private Animation mSlideRightAnimation;

	/**
	 * Restore the view of the effects packs with an animation
	 */
	private void restorePacksAnimation() {

		mIsAnimating = true;

		if ( mSlideInLeftAnimation == null ) {
			mSlideInLeftAnimation = AnimationUtils.loadAnimation( getContext().getBaseContext(), R.anim.feather_slide_in_left );
			mSlideInLeftAnimation.setDuration( mAnimationFilmDurationOut );

			mSlideInLeftAnimation.setAnimationListener( new AnimationListener() {

				@Override
				public void onAnimationStart( Animation animation ) {
					mWorkspaceContainer.setVisibility( View.VISIBLE );
				}

				@Override
				public void onAnimationRepeat( Animation animation ) {}

				@Override
				public void onAnimationEnd( Animation animation ) {
					mIsAnimating = false;
				}
			} );
		}

		mWorkspaceContainer.startAnimation( mSlideInLeftAnimation );

		if ( mSlideRightAnimation == null ) {
			// hide effects
			mSlideRightAnimation = AnimationUtils.loadAnimation( getContext().getBaseContext(), R.anim.feather_slide_out_right );
			mSlideRightAnimation.setDuration( mAnimationFilmDurationOut );
			mSlideRightAnimation.setAnimationListener( new AnimationListener() {

				@Override
				public void onAnimationStart( Animation animation ) {}

				@Override
				public void onAnimationRepeat( Animation animation ) {}

				@Override
				public void onAnimationEnd( Animation animation ) {
					mCannisterView.removeAllViews();
					mHList.setVisibility( View.INVISIBLE );
					mHList.setAdapter( null );
					getContext().setToolbarTitle( getContext().getCurrentEffect().labelResourceId );

					// ok restore the original filter too...
					mSelectedLabel = "undefined";
					mSelectedView = null;
					mImageSwitcher.setImageBitmap( mBitmap, false, null, Float.MAX_VALUE );
					setIsChanged( false );
				}
			} );
		}

		mCannisterView.startAnimation( mSlideRightAnimation );
		mHList.startAnimation( mSlideRightAnimation );
	}

	/**
	 * The effect list is loaded, animate it.
	 */
	private void startEffectsSliderAnimation( final CharSequence title ) {

		mIsAnimating = true;

		Animation animation = new TranslateAnimation( TranslateAnimation.RELATIVE_TO_SELF, 1, TranslateAnimation.RELATIVE_TO_SELF, 0, TranslateAnimation.RELATIVE_TO_SELF, 0, TranslateAnimation.RELATIVE_TO_SELF, 0 );
		animation.setDuration( mAnimationFilmDurationIn );
		animation.setAnimationListener( new AnimationListener() {

			@Override
			public void onAnimationStart( Animation animation ) {
				mHList.setVisibility( View.VISIBLE );
			}

			@Override
			public void onAnimationRepeat( Animation animation ) {}

			@Override
			public void onAnimationEnd( Animation animation ) {
				mIsAnimating = false;

				// set the new toolbar title
				if ( mExternalPacksEnabled ) {
					getContext().setToolbarTitle( title );
				} else {
					getContentView().setVisibility( View.VISIBLE );
					contentReady();
				}
			}
		} );
		mHList.startAnimation( animation );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aviary.android.feather.library.services.PluginService.OnUpdateListener#onUpdate(android.os.Bundle)
	 */
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
						setStatus( Status.Packs );
						updateInstalledPacks( false );
					}
				};

				mUpdateDialog = new AlertDialog.Builder( getContext().getBaseContext() ).setMessage( R.string.filter_pack_updated ).setNeutralButton( android.R.string.ok, listener ).setCancelable( false ).create();

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

	/**
	 * Try to install the selected pack, >only if the passed resource manager contains an external app (not the current one)
	 */
	private PluginError installPlugin( String packagename, int pluginType ) {
		if ( mPluginService.installed( packagename ) ) {
			return PluginError.NoError;
		}
		return mPluginService.install( getContext().getBaseContext(), packagename, pluginType );
	}

	// updated installed package names
	private class UpdateInstalledPacksTask extends AsyncTask<Void, Void, FeatherPack[]> {
		
		private boolean mPostAnimate;
		
		public UpdateInstalledPacksTask( boolean postAnimate ) {
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

			PluginService service = getContext().getService( PluginService.class );
			if ( null != service ) {
				while ( !service.isUpdated() ) {
					try {
						Thread.sleep( 50 );
					} catch ( InterruptedException e ) {
						e.printStackTrace();
					}
				}

				FeatherPack packs[] = getInstalledPacks();
				FeatherPack packs2[] = getAvailablePacks( FeatherIntent.PluginType.TYPE_FILTER );
				int newLength = packs.length + packs2.length;
				FeatherPack packs3[] = new FeatherPack[newLength];

				System.arraycopy( packs, 0, packs3, 0, packs.length );
				System.arraycopy( packs2, 0, packs3, packs.length, packs2.length );

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
			
			mLogger.log( "total packs: " + result.length );
			
			FiltersPacksAdapter adapter = new FiltersPacksAdapter( getContext().getBaseContext(), R.layout.feather_workspace_screen, R.layout.feather_filter_pack, result );
			mWorkspace.setAdapter( adapter );
			mWorkspaceIndicator.setVisibility( mWorkspace.getTotalPages() > 1 ? View.VISIBLE : View.INVISIBLE );
			mLayoutLoader.setVisibility( View.GONE );
			
			if( mPostAnimate ){
				startFirstAnimation();
			} else {
				mWorkspace.setVisibility( View.VISIBLE );
			}
		}

	}

	public void onSwipe( boolean leftToRight ) {

		if ( mStatus.equals( Status.Filters ) ) {

			Context context = getContext().getBaseContext();

			int position = mSelectedPosition;
			if ( position == 0 ) position = 1;
			position = leftToRight ? position - 1 : position + 1;

			if ( position > 0 && position < mHList.getAdapter().getCount() - 1 ) {
				View view = mHList.getItemAt( position );
				setSelected( view, position, (String) mHList.getAdapter().getItem( position ) );
				CharSequence text = mFiltersAdapter.getFilterName( position );
				Toast.makeText( context, text, toastDuration ).show();
			} else {

				int errorText;
				if ( position < 1 ) {
					errorText = R.string.feather_effects_beginning_of_list;
					position += 1;
				} else {
					errorText = R.string.feather_effects_end_of_list;
					position -= 1;
				}

				Toast.makeText( context, errorText, toastDuration ).show();
			}
		}
	}
}
