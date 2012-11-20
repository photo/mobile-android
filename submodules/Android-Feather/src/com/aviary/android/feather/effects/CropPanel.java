package com.aviary.android.feather.effects;

import java.util.HashSet;
import org.json.JSONException;
import android.R.attr;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.aviary.android.feather.R;
import com.aviary.android.feather.graphics.CropCheckboxDrawable;
import com.aviary.android.feather.graphics.DefaultGalleryCheckboxDrawable;
import com.aviary.android.feather.library.filters.CropFilter;
import com.aviary.android.feather.library.filters.FilterLoaderFactory;
import com.aviary.android.feather.library.filters.FilterLoaderFactory.Filters;
import com.aviary.android.feather.library.moa.MoaActionList;
import com.aviary.android.feather.library.moa.MoaPointParameter;
import com.aviary.android.feather.library.services.ConfigService;
import com.aviary.android.feather.library.services.EffectContext;
import com.aviary.android.feather.library.utils.ReflectionUtils;
import com.aviary.android.feather.library.utils.ReflectionUtils.ReflectionException;
import com.aviary.android.feather.library.utils.SystemUtils;
import com.aviary.android.feather.utils.UIUtils;
import com.aviary.android.feather.widget.AdapterView;
import com.aviary.android.feather.widget.CropImageView;
import com.aviary.android.feather.widget.Gallery;
import com.aviary.android.feather.widget.Gallery.OnItemsScrollListener;
import com.aviary.android.feather.widget.HighlightView;

// TODO: Auto-generated Javadoc
/**
 * The Class CropPanel.
 */
public class CropPanel extends AbstractContentPanel {

	Gallery mGallery;
	String[] mCropNames, mCropValues;
	View mSelected;
	int mSelectedPosition = 0;
	boolean mIsPortrait = true;
	final static int noImage = 0;
	HashSet<Integer> nonInvertOptions = new HashSet<Integer>();

	/* whether to use inversion and photo size detection */
	boolean strict = false;

	/** Whether or not the proportions are inverted */
	boolean isChecked = false;

	/**
	 * Instantiates a new crop panel.
	 * 
	 * @param context
	 *           the context
	 */
	public CropPanel( EffectContext context ) {
		super( context );
	}

	private void invertRatios( String[] names, String[] values ) {

		for ( int i = 0; i < names.length; i++ ) {

			if ( names[i].contains( ":" ) ) {
				String temp = names[i];
				String[] splitted = temp.split( "[:]" );
				String mNewOptionName = splitted[1] + ":" + splitted[0];
				names[i] = mNewOptionName;
			}

			if ( values[i].contains( ":" ) ) {
				String temp = values[i];
				String[] splitted = temp.split( "[:]" );
				String mNewOptionValue = splitted[1] + ":" + splitted[0];
				values[i] = mNewOptionValue;
			}
		}
	}

	private void populateInvertOptions( HashSet<Integer> options, String[] cropValues ) {
		for ( int i = 0; i < cropValues.length; i++ ) {
			String value = cropValues[i];
			String[] values = value.split( ":" );
			int x = Integer.parseInt( values[0] );
			int y = Integer.parseInt( values[1] );

			if ( x == y ) {
				options.add( i );
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aviary.android.feather.effects.AbstractEffectPanel#onCreate(android.graphics.Bitmap)
	 */
	@Override
	public void onCreate( Bitmap bitmap ) {
		super.onCreate( bitmap );

		ConfigService config = getContext().getService( ConfigService.class );
		mFilter = FilterLoaderFactory.get( Filters.CROP );

		mCropNames = config.getStringArray( R.array.feather_crop_names );
		mCropValues = config.getStringArray( R.array.feather_crop_values );
		strict = config.getBoolean( R.integer.feather_crop_invert_policy );

		if ( !strict ) {
			if ( bitmap.getHeight() > bitmap.getWidth() ) {
				mIsPortrait = true;
			} else {
				mIsPortrait = false;
			}

			// configure options that will not invert
			populateInvertOptions( nonInvertOptions, mCropValues );

			if ( mIsPortrait ) {
				invertRatios( mCropNames, mCropValues );
			}
		}

		mSelectedPosition = config.getInteger( R.integer.feather_crop_selected_value );

		mImageView = (CropImageView) getContentView().findViewById( R.id.crop_image_view );
		mImageView.setDoubleTapEnabled( false );

		int minAreaSize = config.getInteger( R.integer.feather_crop_min_size );
		( (CropImageView) mImageView ).setMinCropSize( minAreaSize );

		mGallery = (Gallery) getOptionView().findViewById( R.id.gallery );
		mGallery.setCallbackDuringFling( false );
		mGallery.setSpacing( 0 );
		mGallery.setAdapter( new GalleryAdapter( getContext().getBaseContext(), mCropNames ) );
		mGallery.setSelection( mSelectedPosition, false, true );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aviary.android.feather.effects.AbstractEffectPanel#onActivate()
	 */
	@Override
	public void onActivate() {
		super.onActivate();

		int position = mGallery.getSelectedItemPosition();
		final double ratio = calculateAspectRatio( position, false );

		disableHapticIsNecessary( mGallery );

		setIsChanged( true );
		contentReady();
		
		mGallery.setOnItemsScrollListener( new OnItemsScrollListener() {

			@Override
			public void onScrollFinished( AdapterView<?> parent, View view, int position, long id ) {

				if ( !isActive() ) return;

				if ( position == mSelectedPosition ) {

					if ( !strict && !nonInvertOptions.contains( position ) ) {
						isChecked = !isChecked;
						CropImageView cview = (CropImageView) mImageView;

						double currentAspectRatio = cview.getAspectRatio();

						HighlightView hv = cview.getHighlightView();
						if ( !cview.getAspectRatioIsFixed() && hv != null ) {
							currentAspectRatio = (double) hv.getDrawRect().width() / (double) hv.getDrawRect().height();
						}

						double invertedAspectRatio = 1 / currentAspectRatio;

						cview.setAspectRatio( invertedAspectRatio, cview.getAspectRatioIsFixed() );
						invertRatios( mCropNames, mCropValues );
						mGallery.invalidateViews();
					}
				} else {
					double ratio = calculateAspectRatio( position, false );
					setCustomRatio( ratio, ratio != 0 );
				}
				updateSelection( view, position );

			}

			@Override
			public void onScrollStarted( AdapterView<?> parent, View view, int position, long id ) {}

			@Override
			public void onScroll( AdapterView<?> parent, View view, int position, long id ) {}
		} );

		getOptionView().getHandler().post( new Runnable() {

			@Override
			public void run() {
				createCropView( ratio, ratio != 0 );
				updateSelection( (View) mGallery.getSelectedView(), mGallery.getSelectedItemPosition() );
			}
		} );
	}

	/**
	 * Calculate aspect ratio.
	 * 
	 * @param position
	 *           the position
	 * @param inverted
	 *           the inverted
	 * @return the double
	 */
	private double calculateAspectRatio( int position, boolean inverted ) {

		String value = mCropValues[position];
		String[] values = value.split( ":" );

		if ( values.length == 2 ) {
			int aspectx = Integer.parseInt( inverted ? values[1] : values[0] );
			int aspecty = Integer.parseInt( inverted ? values[0] : values[1] );

			if ( aspectx == -1 ) {
				aspectx = inverted ? mBitmap.getHeight() : mBitmap.getWidth();
			}

			if ( aspecty == -1 ) {
				aspecty = inverted ? mBitmap.getWidth() : mBitmap.getHeight();
			}

			double ratio = 0;

			if ( aspectx != 0 && aspecty != 0 ) {
				ratio = (double) aspectx / (double) aspecty;
			}
			return ratio;
		}
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aviary.android.feather.effects.AbstractEffectPanel#onDestroy()
	 */
	@Override
	public void onDestroy() {
		mImageView.clear();
		( (CropImageView) mImageView ).setOnHighlightSingleTapUpConfirmedListener( null );
		super.onDestroy();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aviary.android.feather.effects.AbstractEffectPanel#onDeactivate()
	 */
	@Override
	public void onDeactivate() {
		super.onDeactivate();
	}

	/**
	 * Creates the crop view.
	 * 
	 * @param aspectRatio
	 *           the aspect ratio
	 */
	private void createCropView( double aspectRatio, boolean isFixed ) {
		( (CropImageView) mImageView ).setImageBitmap( mBitmap, aspectRatio, isFixed );
	}

	/**
	 * Sets the custom ratio.
	 * 
	 * @param aspectRatio
	 *           the aspect ratio
	 * @param isFixed
	 *           the is fixed
	 */
	private void setCustomRatio( double aspectRatio, boolean isFixed ) {
		( (CropImageView) mImageView ).setAspectRatio( aspectRatio, isFixed );
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

			String label = (String) mSelected.getTag();
			if ( label != null ) {
				View textview = mSelected.findViewById( R.id.text );
				if ( null != textview ) {
					( (TextView) textview ).setText( getString( label ) );
				}

				View arrow = mSelected.findViewById( R.id.invertCropArrow );
				if ( null != arrow ) {
					arrow.setVisibility( View.INVISIBLE );
				}
			}

			mSelected.setSelected( false );
		}

		mSelected = newSelection;
		mSelectedPosition = position;

		if ( mSelected != null ) {
			mSelected = newSelection;
			mSelected.setSelected( true );

			View arrow = mSelected.findViewById( R.id.invertCropArrow );
			if ( null != arrow && !nonInvertOptions.contains( position ) && !strict ) {
				arrow.setVisibility( View.VISIBLE );
				arrow.setSelected( isChecked );
			} else {
				arrow.setVisibility( View.INVISIBLE );
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aviary.android.feather.effects.AbstractEffectPanel#onGenerateResult()
	 */
	@Override
	protected void onGenerateResult() {
		Rect crop_rect = ( (CropImageView) mImageView ).getHighlightView().getCropRect();
		GenerateResultTask task = new GenerateResultTask( crop_rect );
		task.execute( mBitmap );
	}

	/**
	 * Generate bitmap.
	 * 
	 * @param bitmap
	 *           the bitmap
	 * @param cropRect
	 *           the crop rect
	 * @return the bitmap
	 */
	@SuppressWarnings("unused")
	private Bitmap generateBitmap( Bitmap bitmap, Rect cropRect ) {
		Bitmap croppedImage;

		int width = cropRect.width();
		int height = cropRect.height();

		croppedImage = Bitmap.createBitmap( width, height, Bitmap.Config.RGB_565 );
		Canvas canvas = new Canvas( croppedImage );
		Rect dstRect = new Rect( 0, 0, width, height );
		canvas.drawBitmap( mBitmap, cropRect, dstRect, null );
		return croppedImage;
	}

	/**
	 * The Class GenerateResultTask.
	 */
	class GenerateResultTask extends AsyncTask<Bitmap, Void, Bitmap> {

		/** The m crop rect. */
		Rect mCropRect;
		MoaActionList mActionList;

		/**
		 * Instantiates a new generate result task.
		 * 
		 * @param rect
		 *           the rect
		 */
		public GenerateResultTask( Rect rect ) {
			mCropRect = rect;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onPreExecute()
		 */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			onProgressModalStart();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#doInBackground(Params[])
		 */
		@Override
		protected Bitmap doInBackground( Bitmap... arg0 ) {

			final Bitmap bitmap = arg0[0];

			MoaPointParameter topLeft = new MoaPointParameter();
			topLeft.setValue( (double) mCropRect.left / bitmap.getWidth(), (double) mCropRect.top / bitmap.getHeight() );

			MoaPointParameter size = new MoaPointParameter();
			size.setValue( (double) mCropRect.width() / bitmap.getWidth(), (double) mCropRect.height() / bitmap.getHeight() );

			( (CropFilter) mFilter ).setTopLeft( topLeft );
			( (CropFilter) mFilter ).setSize( size );

			mActionList = (MoaActionList) ( (CropFilter) mFilter ).getActions().clone();

			try {
				return ( (CropFilter) mFilter ).execute( arg0[0], null, 1, 1 );
			} catch ( JSONException e ) {
				e.printStackTrace();
			}
			return arg0[0];
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute( Bitmap result ) {
			super.onPostExecute( result );

			onProgressModalEnd();

			( (CropImageView) mImageView ).setImageBitmap( result, ( (CropImageView) mImageView ).getAspectRatio(),
					( (CropImageView) mImageView ).getAspectRatioIsFixed() );
			( (CropImageView) mImageView ).setHighlightView( null );

			onComplete( result, mActionList );
		}
	}

	@Override
	protected View generateContentView( LayoutInflater inflater ) {
		View view = inflater.inflate( R.layout.feather_crop_content, null );

		if ( SystemUtils.isHoneyComb() ) {
			// Honeycomb bug with canvas clip
			try {
				ReflectionUtils.invokeMethod( view, "setLayerType", new Class[] { int.class, Paint.class }, 1, null );
			} catch ( ReflectionException e ) {}
		}

		return view;
	}

	@Override
	protected ViewGroup generateOptionView( LayoutInflater inflater, ViewGroup parent ) {
		return (ViewGroup) inflater.inflate( R.layout.feather_crop_panel, parent, false );
	}

	@Override
	public Matrix getContentDisplayMatrix() {
		return mImageView.getDisplayMatrix();
	}

	@Override
	public void onConfigurationChanged( Configuration newConfig, Configuration oldConfig ) {
		super.onConfigurationChanged( newConfig, oldConfig );
	}

	private String getString( String input ) {
		int id = getContext().getBaseContext().getResources()
				.getIdentifier( input, "string", getContext().getBaseContext().getPackageName() );
		if ( id > 0 ) {
			return getContext().getBaseContext().getResources().getString( id );
		}
		return input;
	}

	class GalleryAdapter extends BaseAdapter {

		private String[] mStrings;
		LayoutInflater mLayoutInflater;
		Resources mRes;
		
		private static final int VALID_POSITION = 0;
		private static final int INVALID_POSITION = 1;

		/**
		 * Instantiates a new gallery adapter.
		 * 
		 * @param context
		 *           the context
		 * @param values
		 *           the values
		 */
		public GalleryAdapter( Context context, String[] values ) {
			mLayoutInflater = UIUtils.getLayoutInflater();
			mStrings = values;
			mRes = getContext().getBaseContext().getResources();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.widget.Adapter#getCount()
		 */
		@Override
		public int getCount() {
			return mStrings.length;
		}

		public void updateStrings() {

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.widget.Adapter#getItem(int)
		 */
		@Override
		public Object getItem( int position ) {
			return mStrings[position];
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
		 * @see android.widget.BaseAdapter#getViewTypeCount()
		 */
		@Override
		public int getViewTypeCount() {
			return 2;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.widget.BaseAdapter#getItemViewType(int)
		 */
		@Override
		public int getItemViewType( int position ) {
			final boolean valid = position >= 0 && position < getCount();
			return valid ? VALID_POSITION : INVALID_POSITION;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
		 */
		@SuppressWarnings("deprecation")
		@Override
		public View getView( final int position, View convertView, ViewGroup parent ) {

			int type = getItemViewType( position );
			final View view;
			if ( convertView == null ) {
				if ( type == VALID_POSITION ) {
					// use the default crop checkbox view
					view = mLayoutInflater.inflate( R.layout.feather_crop_button, mGallery, false );

					StateListDrawable st = new StateListDrawable();
					Drawable unselectedBackground = new CropCheckboxDrawable( mRes, false, null, 1.0f, 0, 0 );
					Drawable selectedBackground = new CropCheckboxDrawable( mRes, true, null, 1.0f, 0, 0 );
					st.addState( new int[] { -attr.state_selected }, unselectedBackground );
					st.addState( new int[] { attr.state_selected }, selectedBackground );
					view.setBackgroundDrawable( st );

				} else {
					// use the blank view
					view = mLayoutInflater.inflate( R.layout.feather_checkbox_button, mGallery, false );
					Drawable unselectedBackground = new DefaultGalleryCheckboxDrawable( mRes, false );
					view.setBackgroundDrawable( unselectedBackground );
				}
			} else {
				view = convertView;
			}

			view.setSelected( mSelectedPosition == position );

			if ( type == VALID_POSITION ) {
				Object item = getItem( position );
				view.setTag( item );
				if ( null != item ) {

					TextView text = (TextView) view.findViewById( R.id.text );
					String textValue = "";
					if ( position >= 0 && position < mStrings.length ) textValue = mStrings[position];

					if ( null != text ) text.setText( getString( textValue ) );
					View arrow = view.findViewById( R.id.invertCropArrow );
					int targetVisibility;

					if ( mSelectedPosition == position && !strict ) {
						mSelected = view;
						if ( null != arrow && !nonInvertOptions.contains( position ) ) {
							targetVisibility = View.VISIBLE;
						} else {
							targetVisibility = View.INVISIBLE;
						}
					} else {
						targetVisibility = View.INVISIBLE;
					}
					
					if( null != arrow ){
						arrow.setVisibility( targetVisibility );
						arrow.setSelected( isChecked );
					}
				}

			}
			return view;
		}
	}
}
