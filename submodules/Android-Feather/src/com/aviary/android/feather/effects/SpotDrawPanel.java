package com.aviary.android.feather.effects;

import android.R.attr;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import com.aviary.android.feather.R;
import com.aviary.android.feather.graphics.CropCheckboxDrawable;
import com.aviary.android.feather.graphics.DefaultGalleryCheckboxDrawable;
import com.aviary.android.feather.graphics.GalleryCircleDrawable;
import com.aviary.android.feather.graphics.PreviewCircleDrawable;
import com.aviary.android.feather.library.filters.FilterLoaderFactory;
import com.aviary.android.feather.library.filters.FilterLoaderFactory.Filters;
import com.aviary.android.feather.library.filters.IFilter;
import com.aviary.android.feather.library.filters.SpotBrushFilter;
import com.aviary.android.feather.library.graphics.FlattenPath;
import com.aviary.android.feather.library.moa.Moa;
import com.aviary.android.feather.library.moa.MoaAction;
import com.aviary.android.feather.library.moa.MoaActionFactory;
import com.aviary.android.feather.library.moa.MoaActionList;
import com.aviary.android.feather.library.services.ConfigService;
import com.aviary.android.feather.library.services.EffectContext;
import com.aviary.android.feather.library.utils.BitmapUtils;
import com.aviary.android.feather.library.utils.SystemUtils;
import com.aviary.android.feather.library.utils.UIConfiguration;
import com.aviary.android.feather.utils.UIUtils;
import com.aviary.android.feather.widget.AdapterView;
import com.aviary.android.feather.widget.Gallery;
import com.aviary.android.feather.widget.Gallery.OnItemsScrollListener;
import com.aviary.android.feather.widget.IToast;
import com.aviary.android.feather.widget.ImageViewSpotDraw;
import com.aviary.android.feather.widget.ImageViewSpotDraw.OnDrawListener;
import com.aviary.android.feather.widget.ImageViewSpotDraw.TouchMode;

/**
 * The Class SpotDrawPanel.
 */
public class SpotDrawPanel extends AbstractContentPanel implements OnDrawListener {

	/** The brush size. */
	protected int mBrushSize;

	/** The filter type. */
	protected Filters mFilterType;

	protected Gallery mGallery;

	/** The brush sizes. */
	protected int[] mBrushSizes;

	/** The current selected view. */
	protected View mSelected;

	/** The current selected position. */
	protected int mSelectedPosition = 0;

	protected ImageButton mLensButton;

	/** The background draw thread. */
	private MyHandlerThread mBackgroundDrawThread;

	IToast mToast;

	PreviewCircleDrawable mCircleDrawablePreview;

	MoaActionList mActions = MoaActionFactory.actionList();

	private int mPreviewWidth, mPreviewHeight;

	/**
	 * Show size preview.
	 * 
	 * @param size
	 *           the size
	 */
	private void showSizePreview( int size ) {
		if ( !isActive() ) return;

		mToast.show();
		updateSizePreview( size );
	}

	/**
	 * Hide size preview.
	 */
	private void hideSizePreview() {
		if ( !isActive() ) return;
		mToast.hide();
	}

	/**
	 * Update size preview.
	 * 
	 * @param size
	 *           the size
	 */
	private void updateSizePreview( int size ) {

		if ( !isActive() ) return;

		mCircleDrawablePreview.setRadius( size );

		View v = mToast.getView();
		v.findViewById( R.id.size_preview_image );
		v.invalidate();
	}

	/**
	 * Instantiates a new spot draw panel.
	 * 
	 * @param context
	 *           the context
	 * @param filter_type
	 *           the filter_type
	 */
	public SpotDrawPanel( EffectContext context, Filters filter_type ) {
		super( context );
		mFilterType = filter_type;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aviary.android.feather.effects.AbstractEffectPanel#onCreate(android.graphics.Bitmap)
	 */
	@Override
	public void onCreate( Bitmap bitmap ) {
		super.onCreate( bitmap );

		mFilter = createFilter();

		ConfigService config = getContext().getService( ConfigService.class );

		mBrushSizes = config.getSizeArray( R.array.feather_spot_brush_sizes );
		mBrushSize = mBrushSizes[0];

		mLensButton = (ImageButton) getContentView().findViewById( R.id.lens_button );

		mImageView = (ImageViewSpotDraw) getContentView().findViewById( R.id.image );
		( (ImageViewSpotDraw) mImageView ).setBrushSize( (float) mBrushSize );

		mPreview = BitmapUtils.copy( mBitmap, Config.ARGB_8888 );

		mPreviewWidth = mPreview.getWidth();
		mPreviewHeight = mPreview.getHeight();

		mImageView.setImageBitmap( mPreview, true, getContext().getCurrentImageViewMatrix(), UIConfiguration.IMAGE_VIEW_MAX_ZOOM );

		int defaultOption = config.getInteger( R.integer.feather_spot_brush_selected_size_index );
		defaultOption = Math.min( Math.max( defaultOption, 0 ), mBrushSizes.length - 1 );

		mGallery = (Gallery) getOptionView().findViewById( R.id.gallery );
		mGallery.setCallbackDuringFling( false );
		mGallery.setSpacing( 0 );
		mGallery.setOnItemsScrollListener( new OnItemsScrollListener() {

			@Override
			public void onScrollFinished( AdapterView<?> parent, View view, int position, long id ) {
				mLogger.info( "onScrollFinished: " + position );
				mBrushSize = mBrushSizes[position];
				( (ImageViewSpotDraw) mImageView ).setBrushSize( (float) mBrushSize );

				setSelectedTool( TouchMode.DRAW );

				updateSelection( view, position );
				hideSizePreview();
			}

			@Override
			public void onScrollStarted( AdapterView<?> parent, View view, int position, long id ) {
				showSizePreview( mBrushSizes[position] );
				setSelectedTool( TouchMode.DRAW );
			}

			@Override
			public void onScroll( AdapterView<?> parent, View view, int position, long id ) {
				updateSizePreview( mBrushSizes[position] );
			}
		} );

		mBackgroundDrawThread = new MyHandlerThread( "filter-thread", Thread.MIN_PRIORITY );
		initAdapter();
	}

	/**
	 * Inits the adapter.
	 */
	private void initAdapter() {
		int height = mGallery.getHeight();
		if ( height < 1 ) {
			mGallery.getHandler().post( new Runnable() {

				@Override
				public void run() {
					initAdapter();
				}
			} );
			return;
		}

		mGallery.setSelection( 2, false, true );
		mGallery.setAdapter( new GalleryAdapter( getContext().getBaseContext(), mBrushSizes ) );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aviary.android.feather.effects.AbstractEffectPanel#onActivate()
	 */
	@Override
	public void onActivate() {
		super.onActivate();

		( (ImageViewSpotDraw) mImageView ).setOnDrawStartListener( this );
		mBackgroundDrawThread.start();
		mBackgroundDrawThread.setRadius( (float) Math.max( 1, mBrushSizes[0] ), mPreviewWidth );

		updateSelection( (View) mGallery.getSelectedView(), mGallery.getSelectedItemPosition() );

		mToast = IToast.make( getContext().getBaseContext(), -1 );
		mCircleDrawablePreview = new PreviewCircleDrawable( 0 );
		ImageView image = (ImageView) mToast.getView().findViewById( R.id.size_preview_image );
		image.setImageDrawable( mCircleDrawablePreview );

		mLensButton.setOnClickListener( new OnClickListener() {

			@Override
			public void onClick( View arg0 ) {
				// boolean selected = arg0.isSelected();
				setSelectedTool( ( (ImageViewSpotDraw) mImageView ).getDrawMode() == TouchMode.DRAW ? TouchMode.IMAGE : TouchMode.DRAW );
			}
		} );
		mLensButton.setVisibility( View.VISIBLE );

		contentReady();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aviary.android.feather.effects.AbstractContentPanel#onDispose()
	 */
	@Override
	protected void onDispose() {
		mContentReadyListener = null;
		super.onDispose();
	}

	/**
	 * Update selection.
	 * 
	 * @param newSelection
	 *           the new selection
	 * @param position
	 *           the position
	 */
	protected void updateSelection( View newSelection, int position ) {
		if ( mSelected != null ) {
			mSelected.setSelected( false );
		}

		mSelected = newSelection;
		mSelectedPosition = position;

		if ( mSelected != null ) {
			mSelected = newSelection;
			mSelected.setSelected( true );
		}
	}

	/**
	 * Sets the selected tool.
	 * 
	 * @param which
	 *           the new selected tool
	 */
	private void setSelectedTool( TouchMode which ) {
		( (ImageViewSpotDraw) mImageView ).setDrawMode( which );
		mLensButton.setSelected( which == TouchMode.IMAGE );
		setPanelEnabled( which != TouchMode.IMAGE );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aviary.android.feather.effects.AbstractEffectPanel#onDeactivate()
	 */
	@Override
	public void onDeactivate() {
		( (ImageViewSpotDraw) mImageView ).setOnDrawStartListener( null );

		if ( mBackgroundDrawThread != null ) {
			if ( mBackgroundDrawThread.isAlive() ) {
				mBackgroundDrawThread.quit();
				while ( mBackgroundDrawThread.isAlive() ) {
					// wait...
				}
			}
		}
		onProgressEnd();
		super.onDeactivate();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aviary.android.feather.effects.AbstractEffectPanel#onDestroy()
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
		mBackgroundDrawThread = null;
		mImageView.clear();
		mToast.hide();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aviary.android.feather.effects.AbstractEffectPanel#onCancelled()
	 */
	@Override
	public void onCancelled() {
		super.onCancelled();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aviary.android.feather.widget.ImageViewSpotDraw.OnDrawListener#onDrawStart(float[], int)
	 */
	@Override
	public void onDrawStart( float[] points, int radius ) {
		radius = Math.max( 1, radius );
		mLogger.info( "onDrawStart. radius: " + radius );
		mBackgroundDrawThread.setRadius( (float) radius, mPreviewWidth );
		mBackgroundDrawThread.moveTo( points );
		mBackgroundDrawThread.lineTo( points );

		setIsChanged( true );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aviary.android.feather.widget.ImageViewSpotDraw.OnDrawListener#onDrawing(float[], int)
	 */
	@Override
	public void onDrawing( float[] points, int radius ) {
		mBackgroundDrawThread.quadTo( points );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aviary.android.feather.widget.ImageViewSpotDraw.OnDrawListener#onDrawEnd()
	 */
	@Override
	public void onDrawEnd() {
		// TODO: empty
		mLogger.info( "onDrawEnd" );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aviary.android.feather.effects.AbstractEffectPanel#onGenerateResult()
	 */
	@Override
	protected void onGenerateResult() {
		mLogger.info( "onGenerateResult: " + mBackgroundDrawThread.isCompleted() + ", " + mBackgroundDrawThread.isAlive() );

		if ( !mBackgroundDrawThread.isCompleted() && mBackgroundDrawThread.isAlive() ) {
			GenerateResultTask task = new GenerateResultTask();
			task.execute();
		} else {
			onComplete( mPreview, mActions );
		}
	}

	/**
	 * Sets the panel enabled.
	 * 
	 * @param value
	 *           the new panel enabled
	 */
	public void setPanelEnabled( boolean value ) {

		if ( mOptionView != null ) {
			if ( value != mOptionView.isEnabled() ) {
				mOptionView.setEnabled( value );

				if ( value ) {
					getContext().restoreToolbarTitle();
				} else {
					getContext().setToolbarTitle( R.string.zoom_mode );
				}

				mOptionView.findViewById( R.id.disable_status ).setVisibility( value ? View.INVISIBLE : View.VISIBLE );
			}
		}
	}

	/**
	 * Prints the rect.
	 * 
	 * @param rect
	 *           the rect
	 * @return the string
	 */
	@SuppressWarnings("unused")
	private String printRect( Rect rect ) {
		return "( left=" + rect.left + ", top=" + rect.top + ", width=" + rect.width() + ", height=" + rect.height() + ")";
	}

	/**
	 * Creates the filter.
	 * 
	 * @return the i filter
	 */
	protected IFilter createFilter() {
		return FilterLoaderFactory.get( mFilterType );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aviary.android.feather.effects.AbstractContentPanel#generateContentView(android.view.LayoutInflater)
	 */
	@Override
	protected View generateContentView( LayoutInflater inflater ) {
		return inflater.inflate( R.layout.feather_spotdraw_content, null );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aviary.android.feather.effects.AbstractOptionPanel#generateOptionView(android.view.LayoutInflater,
	 * android.view.ViewGroup)
	 */
	@Override
	protected ViewGroup generateOptionView( LayoutInflater inflater, ViewGroup parent ) {
		return (ViewGroup) inflater.inflate( R.layout.feather_pixelbrush_panel, parent, false );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aviary.android.feather.effects.AbstractContentPanel#getContentDisplayMatrix()
	 */
	@Override
	public Matrix getContentDisplayMatrix() {
		return mImageView.getDisplayMatrix();
	}

	/**
	 * background draw thread
	 */
	class MyHandlerThread extends Thread {

		/** The started. */
		boolean started;

		/** The running. */
		volatile boolean running;

		/** The paused. */
		boolean paused;

		/** The m x. */
		float mX = 0;

		/** The m y. */
		float mY = 0;

		/** The m flatten path. */
		FlattenPath mFlattenPath;

		/**
		 * Instantiates a new my handler thread.
		 * 
		 * @param name
		 *           the name
		 * @param priority
		 *           the priority
		 */
		public MyHandlerThread( String name, int priority ) {
			super( name );
			setPriority( priority );
			init();
		}

		/**
		 * Inits the.
		 */
		void init() {
			mFlattenPath = new FlattenPath( 0.1 );
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Thread#start()
		 */
		@Override
		synchronized public void start() {
			started = true;
			running = true;
			super.start();
		}

		/**
		 * Quit.
		 */
		synchronized public void quit() {
			running = false;
			pause();
			interrupt();
		};

		/**
		 * Pause.
		 */
		public void pause() {
			if ( !started ) throw new IllegalAccessError( "thread not started" );
			paused = true;

			boolean stopped = ( (SpotBrushFilter) mFilter ).stop();
			mLogger.log( "pause. filter stopped: " + stopped );
		}

		/**
		 * Unpause.
		 */
		public void unpause() {
			if ( !started ) throw new IllegalAccessError( "thread not started" );
			paused = false;
		}

		/** The m radius. */
		float mRadius = 10;

		/**
		 * Sets the radius.
		 * 
		 * @param radius
		 *           the new radius
		 */
		public void setRadius( float radius, int bitmapWidth ) {
			( (SpotBrushFilter) mFilter ).setRadius( radius, bitmapWidth );
			mRadius = radius;
		}

		/**
		 * Move to.
		 * 
		 * @param values
		 *           the values
		 */
		public void moveTo( float values[] ) {
			mFlattenPath.moveTo( values[0], values[1] );
			mX = values[0];
			mY = values[1];
		}

		/**
		 * Line to.
		 * 
		 * @param values
		 *           the values
		 */
		public void lineTo( float values[] ) {
			mFlattenPath.lineTo( values[0], values[1] );
			mX = values[0];
			mY = values[1];
		}

		/**
		 * Quad to.
		 * 
		 * @param values
		 *           the values
		 */
		public void quadTo( float values[] ) {
			mFlattenPath.quadTo( mX, mY, ( values[0] + mX ) / 2, ( values[1] + mY ) / 2 );
			mX = values[0];
			mY = values[1];
		}

		/**
		 * Checks if is completed.
		 * 
		 * @return true, if is completed
		 */
		public boolean isCompleted() {
			return mFlattenPath.size() == 0;
		}

		/**
		 * Queue size.
		 * 
		 * @return the int
		 */
		public int queueSize() {
			return mFlattenPath.size();
		}

		/**
		 * Gets the lerp.
		 * 
		 * @param pt1
		 *           the pt1
		 * @param pt2
		 *           the pt2
		 * @param t
		 *           the t
		 * @return the lerp
		 */
		public PointF getLerp( PointF pt1, PointF pt2, float t ) {
			return new PointF( pt1.x + ( pt2.x - pt1.x ) * t, pt1.y + ( pt2.y - pt1.y ) * t );
		}

		/** The m last point. */
		PointF mLastPoint;

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Thread#run()
		 */
		@Override
		public void run() {

			while ( !started ) {}

			boolean s = false;

			mLogger.log( "thread.start!" );

			while ( running ) {

				if ( paused ) {
					continue;
				}

				int currentSize;

				currentSize = mFlattenPath.size();

				if ( currentSize > 0 && !isInterrupted() ) {

					if ( !s ) {
						mLogger.log( "start: " + currentSize );
						s = true;
						onProgressStart();
					}

					PointF firstPoint;

					firstPoint = mFlattenPath.remove();

					if ( mLastPoint == null ) {
						mLastPoint = firstPoint;
						continue;
					}

					if ( firstPoint == null ) {
						mLastPoint = null;
						continue;
					}

					float currentPosition = 0;

					// float length = mLastPoint.length( firstPoint.x, firstPoint.y );

					float x = Math.abs( firstPoint.x - mLastPoint.x );
					float y = Math.abs( firstPoint.y - mLastPoint.y );
					float length = (float) Math.sqrt( x * x + y * y );
					float lerp;

					if ( length == 0 ) {
						( (SpotBrushFilter) mFilter ).draw( firstPoint.x / mPreviewWidth, firstPoint.y / mPreviewHeight, mPreview );
						try {
							mActions.add( (MoaAction) ( (SpotBrushFilter) mFilter ).getActions().get( 0 ).clone() );
						} catch ( CloneNotSupportedException e ) {
							e.printStackTrace();
						}
					} else {
						while ( currentPosition < length ) {
							lerp = currentPosition / length;
							PointF point = getLerp( mLastPoint, firstPoint, lerp );
							currentPosition += mRadius;
							( (SpotBrushFilter) mFilter ).draw( point.x / mPreviewWidth, point.y / mPreviewHeight, mPreview );
							try {
								mActions.add( (MoaAction) ( (SpotBrushFilter) mFilter ).getActions().get( 0 ).clone() );
							} catch ( CloneNotSupportedException e ) {
								e.printStackTrace();
							}

							if ( SystemUtils.isHoneyComb() ) {
								// There's a bug in Honeycomb which prevent the bitmap to be updated on a glcanvas
								// so we need to force it
								Moa.notifyPixelsChanged( mPreview );
							}
						}
					}
					mLastPoint = firstPoint;

					mImageView.postInvalidate();
				} else {
					if ( s ) {
						mLogger.log( "end: " + currentSize );
						onProgressEnd();
						s = false;
					}
				}
			}
			onProgressEnd();
			mLogger.log( "thread.end" );
		};
	};

	/**
	 * Bottom Gallery adapter.
	 * 
	 * @author alessandro
	 */
	class GalleryAdapter extends BaseAdapter {

		private int[] sizes;
		LayoutInflater mLayoutInflater;
		Drawable checkbox_unselected, checkbox_selected;
		Resources mRes;

		/**
		 * Instantiates a new gallery adapter.
		 * 
		 * @param context
		 *           the context
		 * @param values
		 *           the values
		 */
		public GalleryAdapter( Context context, int[] values ) {
			mLayoutInflater = UIUtils.getLayoutInflater();
			sizes = values;

			mRes = getContext().getBaseContext().getResources();
			checkbox_selected = mRes.getDrawable( R.drawable.feather_crop_checkbox_selected );
			checkbox_unselected = mRes.getDrawable( R.drawable.feather_crop_checkbox_unselected );
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.widget.Adapter#getCount()
		 */
		@Override
		public int getCount() {
			return sizes.length;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.widget.Adapter#getItem(int)
		 */
		@Override
		public Object getItem( int position ) {
			return sizes[position];
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.widget.Adapter#getItemId(int)
		 */
		@Override
		public long getItemId( int position ) {
			return 0;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
		 */
		@Override
		public View getView( int position, View convertView, ViewGroup parent ) {

			final boolean valid = position >= 0 && position < getCount();

			GalleryCircleDrawable mCircleDrawable = null;
			int biggest = sizes[sizes.length - 1];
			int size = 1;

			View view;
			if ( convertView == null ) {
				if ( valid ) {
					mCircleDrawable = new GalleryCircleDrawable( 1, 0 );
					view = mLayoutInflater.inflate( R.layout.feather_checkbox_button, mGallery, false );
					StateListDrawable st = new StateListDrawable();
					Drawable d1 = new CropCheckboxDrawable( mRes, false, mCircleDrawable, 0.67088f, 0.4f, 0.0f );
					Drawable d2 = new CropCheckboxDrawable( mRes, true, mCircleDrawable, 0.67088f, 0.4f, 0.0f );
					st.addState( new int[] { -attr.state_selected }, d1 );
					st.addState( new int[] { attr.state_selected }, d2 );
					view.setBackgroundDrawable( st );
					view.setTag( mCircleDrawable );
				} else {
					// use the blank view
					view = mLayoutInflater.inflate( R.layout.feather_checkbox_button, mGallery, false );
					Drawable unselectedBackground = new DefaultGalleryCheckboxDrawable( mRes, false );
					view.setBackgroundDrawable( unselectedBackground );
				}
			} else {
				view = convertView;
				if ( valid ) {
					mCircleDrawable = (GalleryCircleDrawable) view.getTag();
				}
			}

			if ( mCircleDrawable != null && valid ) {
				size = sizes[position];
				float value = (float) size / biggest;
				mCircleDrawable.update( value, 0 );
			}

			view.setSelected( mSelectedPosition == position );

			return view;
		}
	}

	/**
	 * GenerateResultTask is used when the background draw operation is still running. Just wait until the draw operation completed.
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

			if ( mBackgroundDrawThread != null ) {

				mLogger.info( "GenerateResultTask::doInBackground", mBackgroundDrawThread.isCompleted() );

				while ( mBackgroundDrawThread != null && !mBackgroundDrawThread.isCompleted() ) {
					mLogger.log( "waiting.... " + mBackgroundDrawThread.queueSize() );
					try {
						Thread.sleep( 100 );
					} catch ( InterruptedException e ) {
						e.printStackTrace();
					}
				}
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

			if ( mProgress.isShowing() ) {
				try {
					mProgress.dismiss();
				} catch ( IllegalArgumentException e ) {}
			}

			onComplete( mPreview, mActions );
		}
	}
}
