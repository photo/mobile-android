package com.aviary.android.feather.widget;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.util.concurrent.Future;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory.Options;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import com.aviary.android.feather.Constants;
import com.aviary.android.feather.FeatherActivity;
import com.aviary.android.feather.FilterManager;
import com.aviary.android.feather.R;
import com.aviary.android.feather.library.content.FeatherIntent;
import com.aviary.android.feather.library.plugins.PluginManager.ExternalPlugin;
import com.aviary.android.feather.library.services.EffectContext;
import com.aviary.android.feather.library.services.FutureListener;
import com.aviary.android.feather.library.services.ImageCacheService;
import com.aviary.android.feather.library.services.ImageCacheService.SimpleCachedRemoteBitmap;
import com.aviary.android.feather.library.services.PluginService;
import com.aviary.android.feather.library.services.ThreadPoolService;
import com.aviary.android.feather.library.services.ThreadPoolService.BackgroundCallable;
import com.aviary.android.feather.library.tracking.Tracker;
import com.aviary.android.feather.widget.wp.CellLayout;
import com.aviary.android.feather.widget.wp.CellLayout.CellInfo;
import com.aviary.android.feather.widget.wp.Workspace;
import com.aviary.android.feather.widget.wp.Workspace.OnPageChangeListener;
import com.aviary.android.feather.widget.wp.WorkspaceIndicator;

public class IapDialog extends LinearLayout implements OnPageChangeListener, OnClickListener {

	public interface OnCloseListener {

		void onClose();
	}

	private int mMainLayoutResId = R.layout.feather_iap_workspace_screen_stickers;
	private int mCellResId = R.layout.feather_iap_cell_item_stickers;

	private View mBackground;
	private TextViewCustomFont mTitle, mSubTitle, mDescription;
	private Button mButton;
	private Workspace mWorkspace;
	private WorkspaceIndicator mWorkspaceIndicator;
	private ImageView mIcon;
	private ExternalPlugin mPlugin;
	private int mPluginType;

	private ThreadPoolService mThreadService;
	private ImageCacheService mCacheService;
	private FilterManager mController;

	private boolean mDownloadOnDemand = true;

	private OnCloseListener mCloseListener;

	int mRows;
	int mCols;
	int mItemsPerPage;

	public IapDialog( Context context, AttributeSet attrs ) {
		super( context, attrs );
	}

	@Override
	protected void onAttachedToWindow() {
		Log.i( VIEW_LOG_TAG, "onAttachedFromWindow" );
		super.onAttachedToWindow();

		computeLayoutItems( getResources() );

		mDownloadOnDemand = Constants.getApplicationMaxMemory() < 48;

		mIcon = (ImageView) findViewById( R.id.icon );
		mBackground = findViewById( R.id.main_iap_dialog );
		mButton = (Button) findViewById( R.id.button );
		mTitle = (TextViewCustomFont) findViewById( R.id.title );
		mSubTitle = (TextViewCustomFont) findViewById( R.id.subtitle );
		mDescription = (TextViewCustomFont) findViewById( R.id.description );
		mWorkspace = (Workspace) findViewById( R.id.workspace );
		mWorkspaceIndicator = (WorkspaceIndicator) findViewById( R.id.workspace_indicator );

		mBackground.setOnClickListener( this );
	}

	private void computeLayoutItems( Resources res ) {
		if ( mPluginType == FeatherIntent.PluginType.TYPE_FILTER || mPluginType == FeatherIntent.PluginType.TYPE_BORDER ) {
			mMainLayoutResId = R.layout.feather_iap_workspace_screen_effects;
			mCellResId = R.layout.feather_iap_cell_item_effects;
			mCols = res.getInteger( R.integer.feather_iap_dialog_cols_effects );
			mRows = res.getInteger( R.integer.feather_iap_dialog_rows_effects );
		} else {
			mMainLayoutResId = R.layout.feather_iap_workspace_screen_stickers;
			mCellResId = R.layout.feather_iap_cell_item_stickers;
			mCols = res.getInteger( R.integer.feather_iap_dialog_cols );
			mRows = res.getInteger( R.integer.feather_iap_dialog_rows );
		}
		mItemsPerPage = mRows * mCols;
	}

	@Override
	protected void onDetachedFromWindow() {
		Log.i( VIEW_LOG_TAG, "onDetachedFromWindow" );
		super.onDetachedFromWindow();
		mButton.setOnClickListener( null );
		mWorkspace.setAdapter( null );
		mWorkspace.setOnPageChangeListener( null );
		mBackground.setOnClickListener( null );
		mCloseListener = null;
		mController = null;
		mThreadService = null;
		mCacheService = null;
		mPlugin = null;
	}

	@Override
	public void onClick( View v ) {
		if ( v.equals( mBackground ) ) {
			if ( mCloseListener != null ) {
				mCloseListener.onClose();
			}
		}
	}

	private void initWorkspace( ExternalPlugin plugin ) {
		if ( null != plugin && null != plugin.getItems() ) {

			String[] items = plugin.getItems();
			String folder = getRemoteFolder( plugin );
			mWorkspace.setAdapter( new WorkspaceAdapter( getContext(), folder, mMainLayoutResId, -1, items ) );
			mWorkspace.setOnPageChangeListener( this );

			if ( plugin.getItems().length <= mItemsPerPage ) {
				mWorkspaceIndicator.setVisibility( View.INVISIBLE );
			} else {
				mWorkspaceIndicator.setVisibility( View.VISIBLE );
			}
		} else {
			mWorkspace.setAdapter( null );
			mWorkspace.setOnPageChangeListener( null );
		}
	}

	private String getRemoteFolder( ExternalPlugin plugin ) {

		if ( null != plugin && null != plugin.getPackageName() ) {
			String[] pkg = plugin.getPackageName().split( "\\." );
			String folder = null;
			if ( pkg.length >= 2 ) {
				folder = PluginService.CONTENT_DEFAULT_URL + "/" + pkg[pkg.length - 2] + "/" + pkg[pkg.length - 1];
				return folder;
			}
		}
		return "";
	}

	public void setOnCloseListener( OnCloseListener listener ) {
		mCloseListener = listener;
	}

	public ExternalPlugin getPlugin() {
		return mPlugin;
	}

	public int getPluginType() {
		return mPluginType;
	}

	public void setPlugin( ExternalPlugin value, int type, Context context ) {
		mPlugin = value;
		mPluginType = type;

		computeLayoutItems( getResources() );

		Log.d( VIEW_LOG_TAG, "cols: " + mCols + ", rows: " + mRows );

		if ( null != context && null == mController ) {
			if ( context instanceof FeatherActivity ) {
				mController = ( (FeatherActivity) context ).getController();

				if ( null != mController ) {
					mThreadService = mController.getService( ThreadPoolService.class );
					mCacheService = mController.getService( ImageCacheService.class );
				}
			}
		}

		mTitle.setText( value.getLabel( mPluginType ) );
		mSubTitle.setText( "(" + value.getNumItems( mPluginType ) + ")" );
		mDescription.setText( value.getDescription() != null ? value.getDescription() : "" );

		if ( null != mPlugin.getPackageName() ) {

			mWorkspace.setIndicator( mWorkspaceIndicator );
			initWorkspace( mPlugin );
			downloadPackIcon( mPlugin );

			mButton.setOnClickListener( new OnClickListener() {

				@Override
				public void onClick( View v ) {
					if ( null != mController ) {
						Tracker.recordTag( "Unpurchased(" + mPlugin.getLabel( mPluginType ) + ") : StoreButtonClicked" );
						mController.downloadPlugin( mPlugin.getPackageName(), mPluginType );

						postDelayed( new Runnable() {

							@Override
							public void run() {
								if ( mCloseListener != null ) {
									mCloseListener.onClose();
								}
							}
						}, 500 );

					}
				}
			} );
		}
	}

	/**
	 * Fetch the current pack icon
	 * 
	 * @param plugin
	 */
	private void downloadPackIcon( ExternalPlugin plugin ) {
		if ( null != plugin ) {
			if ( null != mThreadService && null != mIcon ) {
				final String url = PluginService.CONTENT_DEFAULT_URL + "/" + plugin.getIconUrl();
				BackgroundImageLoader callable = new BackgroundImageLoader( mCacheService, false );
				BackgroundImageLoaderListener listener = new BackgroundImageLoaderListener( mIcon, url );
				mThreadService.submit( callable, listener, url );
			}
		}
	}

	class WorkspaceAdapter extends ArrayAdapter<String> {

		LayoutInflater mLayoutInflater;
		int mResId;
		String mUrlPrefix;

		public WorkspaceAdapter( Context context, String urlPrefix, int resource, int textResourceId, String[] objects ) {
			super( context, resource, textResourceId, objects );
			mUrlPrefix = urlPrefix;
			mResId = resource;
			mLayoutInflater = LayoutInflater.from( getContext() );
		}

		public String getUrlPrefix() {
			return mUrlPrefix;
		}

		@Override
		public int getCount() {
			return (int) Math.ceil( (double) super.getCount() / mItemsPerPage );
		}

		/**
		 * Gets the real num of items.
		 * 
		 * @return the real count
		 */
		public int getRealCount() {
			return super.getCount();
		}

		@Override
		public View getView( int position, View convertView, ViewGroup parent ) {

			Log.i( VIEW_LOG_TAG, "getView: " + position + ", convertView: " + convertView );

			if ( convertView == null ) {
				convertView = mLayoutInflater.inflate( mResId, mWorkspace, false );
			}

			CellLayout cell = (CellLayout) convertView;
			cell.setNumCols( mCols );
			cell.setNumRows( mRows );

			for ( int i = 0; i < mItemsPerPage; i++ ) {
				View toolView;
				CellInfo cellInfo = cell.findVacantCell();

				if ( cellInfo == null ) {
					toolView = cell.getChildAt( i );
				} else {
					toolView = mLayoutInflater.inflate( mCellResId, parent, false );
					CellLayout.LayoutParams lp = new CellLayout.LayoutParams( cellInfo.cellX, cellInfo.cellY, cellInfo.spanH, cellInfo.spanV );
					cell.addView( toolView, -1, lp );
				}

				final int index = ( position * mItemsPerPage ) + i;
				final ImageView imageView = (ImageView) toolView.findViewById( R.id.image );
				final View progress = toolView.findViewById( R.id.progress );

				if ( index < getRealCount() ) {
					final String url = getUrlPrefix() + "/" + getItem( index ) + ".png";
					final String tag = (String) imageView.getTag();

					if ( mDownloadOnDemand ) {
						// if on demand we can clean up the bitmap
						if ( tag == null || !url.equals( tag ) ) {
							imageView.setImageBitmap( null );
							imageView.setTag( null );
						}
					} else {
						// download the image immediately
						if ( null != mThreadService ) {

							if ( null != progress ) {
								progress.setVisibility( View.VISIBLE );
							}

							imageView.setImageBitmap( null );
							imageView.setTag( null );
							BackgroundImageLoader callable = new BackgroundImageLoader( mCacheService, true );
							BackgroundImageLoaderListener listener = new BackgroundImageLoaderListener( imageView, url );
							mThreadService.submit( callable, listener, url );
						}
					}
				} else {
					if ( null != progress ) {
						progress.setVisibility( View.GONE );
					}
					imageView.setImageBitmap( null );
					imageView.setTag( null );
				}
			}

			convertView.requestLayout();
			return convertView;
		}
	}

	@Override
	public void onPageChanged( int which, int old ) {

		if ( !mDownloadOnDemand ) return;

		Log.i( VIEW_LOG_TAG, "onPageChanged: " + which + " from " + old );

		if ( null != mWorkspace && null != getContext() ) {
			WorkspaceAdapter adapter = (WorkspaceAdapter) mWorkspace.getAdapter();

			int index = which * mItemsPerPage;
			int endIndex = index + mItemsPerPage;
			int total = adapter.getRealCount();

			for ( int i = index; i < endIndex; i++ ) {
				CellLayout cellLayout = (CellLayout) mWorkspace.getScreenAt( which );
				View toolView = cellLayout.getChildAt( i - index );
				if ( i < total ) {
					final String url = adapter.getUrlPrefix() + "/" + adapter.getItem( i ) + ".png";
					final ImageView imageView = (ImageView) toolView.findViewById( R.id.image );
					final String tag = (String) imageView.getTag();

					if ( tag == null || !url.equals( tag ) ) {
						if ( null != mThreadService ) {

							Log.d( VIEW_LOG_TAG, "fetching image: " + url );
							BackgroundImageLoader callable = new BackgroundImageLoader( mCacheService, true );
							BackgroundImageLoaderListener listener = new BackgroundImageLoaderListener( imageView, url );
							mThreadService.submit( callable, listener, url );

						}
					} else {
						Log.w( VIEW_LOG_TAG, "image already loaded?" );
					}
				}
			}
		}
	}

	/**
	 * Hide the current view and remove from its parent once the hide animation is completed
	 */
	public void hide() {

		if ( null != mPlugin ) {
			Tracker.recordTag( "Unpurchased(" + mPlugin.getLabel( mPluginType ) + ") : Cancelled" );
		}

		if ( null != getHandler() ) {
			getHandler().post( mHide );
		}
	}

	private Runnable mHide = new Runnable() {

		@Override
		public void run() {
			handleHide();
		}
	};

	private void handleHide() {
		Animation animation = AnimationUtils.loadAnimation( getContext(), R.anim.feather_iap_close_animation );
		AnimationListener listener = new AnimationListener() {

			@Override
			public void onAnimationStart( Animation animation ) {}

			@Override
			public void onAnimationRepeat( Animation animation ) {}

			@Override
			public void onAnimationEnd( Animation animation ) {
				ViewGroup parent = (ViewGroup) getParent();
				if ( null != parent ) {
					parent.removeView( IapDialog.this );
				}
			}
		};
		animation.setAnimationListener( listener );
		startAnimation( animation );
	}

	class BackgroundImageLoaderListener implements FutureListener<Bitmap> {

		WeakReference<ImageView> mImageView;
		String mUrl;

		public BackgroundImageLoaderListener( final ImageView view, final String url ) {
			mImageView = new WeakReference<ImageView>( view );
			mUrl = url;
		}

		@Override
		public void onFutureDone( Future<Bitmap> future ) {

			final ImageView image = mImageView.get();

			if ( null != image ) {
				try {
					final Bitmap bitmap = future.get();
					if ( null != getHandler() ) {
						getHandler().post( new Runnable() {

							@SuppressWarnings("deprecation")
							@Override
							public void run() {

								if ( null == bitmap ) {
									image.setScaleType( ScaleType.CENTER );
									image.setImageResource( R.drawable.feather_iap_dialog_image_na );
								} else {
									image.setScaleType( ScaleType.FIT_CENTER );
									image.setImageDrawable( new BitmapDrawable( bitmap ) );
								}

								View parent = (View) image.getParent();
								if ( null != parent ) {
									View progress = parent.findViewById( R.id.progress );
									if ( null != progress ) {
										progress.setVisibility( View.INVISIBLE );
									}
								}

								Animation anim = AnimationUtils.loadAnimation( getContext(), android.R.anim.fade_in );
								image.startAnimation( anim );
								image.setTag( mUrl );
							}
						} );
					}
				} catch ( Throwable e ) {
					e.printStackTrace();
				}
			} else {
				Log.w( VIEW_LOG_TAG, "imageView is null" );
			}
		}
	}

	/**
	 * Background loader for the remote images
	 * 
	 * @author alessandro
	 * 
	 */
	static class BackgroundImageLoader extends BackgroundCallable<String, Bitmap> {

		WeakReference<ImageCacheService> mImageCache;
		boolean mSaveToCache;

		public BackgroundImageLoader( ImageCacheService service, boolean saveToCache ) {
			mImageCache = new WeakReference<ImageCacheService>( service );
			mSaveToCache = saveToCache;
		}

		@Override
		public Bitmap call( EffectContext context, String url ) {

			if ( null != url ) {

				Options options = new Options();
				options.inPreferredConfig = Bitmap.Config.RGB_565;
				options.inInputShareable = true;
				options.inPurgeable = true;

				ImageCacheService service = mImageCache.get();
				if ( null != service ) {

					SimpleCachedRemoteBitmap remoteRequest;

					try {
						remoteRequest = service.requestRemoteBitmap( url );
					} catch ( MalformedURLException e ) {
						e.printStackTrace();
						return null;
					}

					try {
						return remoteRequest.getBitmap( options );
					} catch ( IOException e ) {
						e.printStackTrace();
						return null;
					}
				}
			}
			return null;
		}
	}

}
