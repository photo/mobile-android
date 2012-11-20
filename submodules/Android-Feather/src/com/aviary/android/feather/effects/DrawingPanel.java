package com.aviary.android.feather.effects;

import java.util.ArrayList;
import java.util.Collection;
import android.R.attr;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.BlurMaskFilter.Blur;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.StateListDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import com.aviary.android.feather.R;
import com.aviary.android.feather.graphics.DefaultGalleryCheckboxDrawable;
import com.aviary.android.feather.graphics.GalleryCircleDrawable;
import com.aviary.android.feather.graphics.OverlayGalleryCheckboxDrawable;
import com.aviary.android.feather.graphics.PreviewCircleDrawable;
import com.aviary.android.feather.library.moa.MoaAction;
import com.aviary.android.feather.library.moa.MoaActionFactory;
import com.aviary.android.feather.library.moa.MoaActionList;
import com.aviary.android.feather.library.moa.MoaGraphicsCommandParameter;
import com.aviary.android.feather.library.moa.MoaGraphicsOperationParameter;
import com.aviary.android.feather.library.services.ConfigService;
import com.aviary.android.feather.library.services.EffectContext;
import com.aviary.android.feather.library.utils.BitmapUtils;
import com.aviary.android.feather.library.utils.UIConfiguration;
import com.aviary.android.feather.utils.UIUtils;
import com.aviary.android.feather.widget.AdapterView;
import com.aviary.android.feather.widget.Gallery;
import com.aviary.android.feather.widget.Gallery.OnItemsScrollListener;
import com.aviary.android.feather.widget.IToast;
import com.aviary.android.feather.widget.ImageViewTouchAndDraw;
import com.aviary.android.feather.widget.ImageViewTouchAndDraw.OnDrawPathListener;
import com.aviary.android.feather.widget.ImageViewTouchAndDraw.OnDrawStartListener;
import com.aviary.android.feather.widget.ImageViewTouchAndDraw.TouchMode;

/**
 * The Class DrawingPanel.
 */
public class DrawingPanel extends AbstractContentPanel implements OnDrawStartListener, OnDrawPathListener {

	/**
	 * The Drawin state.
	 */
	private enum DrawinTool {
		Draw, Erase, Zoom,
	};

	protected ImageButton mLensButton;
	protected Gallery mGallerySize;
	protected Gallery mGalleryColor;
	protected View mSelectedSizeView;
	protected View mSelectedColorView;
	protected int mSelectedColorPosition, mSelectedSizePosition = 0;
	int mBrushSizes[];
	int mBrushColors[];
	protected int defaultOption = 0;
	private int mColor = 0;
	private int mSize = 10;
	private int mBlur = 1;
	private Paint mPaint;
	private ConfigService mConfig;
	private DrawinTool mSelectedTool;
	IToast mToast;
	PreviewCircleDrawable mCircleDrawablePreview;

	// width and height of the bitmap
	int mWidth, mHeight;
	MoaActionList mActionList;
	MoaAction mAction;
	Collection<MoaGraphicsOperationParameter> mOperations;
	MoaGraphicsOperationParameter mCurrentOperation;

	/**
	 * Instantiates a new drawing panel.
	 * 
	 * @param context
	 *           the context
	 */
	public DrawingPanel( EffectContext context ) {
		super( context );
	}

	/**
	 * Show toast preview.
	 */
	private void showToastPreview() {
		if ( !isActive() ) return;

		mToast.show();
	}

	/**
	 * Hide toast preview.
	 */
	private void hideToastPreview() {
		if ( !isActive() ) return;
		mToast.hide();
	}

	/**
	 * Update toast preview.
	 * 
	 * @param size
	 *           the size
	 * @param color
	 *           the color
	 * @param blur
	 *           the blur
	 * @param strokeOnly
	 *           the stroke only
	 */
	private void updateToastPreview( int size, int color, int blur, boolean strokeOnly ) {

		if ( !isActive() ) return;

		mCircleDrawablePreview.setRadius( size / 2 );
		mCircleDrawablePreview.setColor( color );
		mCircleDrawablePreview.setBlur( blur );
		mCircleDrawablePreview.setStyle( strokeOnly ? Paint.Style.STROKE : Paint.Style.FILL );

		View v = mToast.getView();
		v.findViewById( R.id.size_preview_image );
		v.invalidate();
	}

	/**
	 * Inits the toast.
	 */
	private void initToast() {
		mToast = IToast.make( getContext().getBaseContext(), -1 );
		mCircleDrawablePreview = new PreviewCircleDrawable( 0 );
		mCircleDrawablePreview.setStyle( Paint.Style.FILL );
		ImageView image = (ImageView) mToast.getView().findViewById( R.id.size_preview_image );
		image.setImageDrawable( mCircleDrawablePreview );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aviary.android.feather.effects.AbstractEffectPanel#onCreate(android.graphics.Bitmap)
	 */
	@Override
	public void onCreate( Bitmap bitmap ) {
		super.onCreate( bitmap );

		mConfig = getContext().getService( ConfigService.class );

		mBrushSizes = mConfig.getSizeArray( R.array.feather_brush_sizes );

		int colors[] = mConfig.getIntArray( R.array.feather_default_colors );

		mBrushColors = new int[colors.length + 2];
		mBrushColors[0] = 0;
		System.arraycopy( colors, 0, mBrushColors, 1, colors.length );

		if ( mConfig != null ) {
			mSize = mBrushSizes[0];
			mColor = mBrushColors[1];
			mBlur = mConfig.getInteger( R.integer.feather_brush_softValue );
		}

		mLensButton = (ImageButton) getContentView().findViewById( R.id.lens_button );

		mGallerySize = (Gallery) getOptionView().findViewById( R.id.gallery );
		mGallerySize.setCallbackDuringFling( false );
		mGallerySize.setSpacing( 0 );

		mGalleryColor = (Gallery) getOptionView().findViewById( R.id.gallery_color );
		mGalleryColor.setCallbackDuringFling( false );
		mGalleryColor.setSpacing( 0 );

		mImageView = (ImageViewTouchAndDraw) getContentView().findViewById( R.id.image );

		mWidth = mBitmap.getWidth();
		mHeight = mBitmap.getHeight();

		resetBitmap();

		mSelectedColorPosition = 1;
		mSelectedSizePosition = 0;

		// init the actionlist
		mActionList = MoaActionFactory.actionList( "draw" );
		mAction = mActionList.get( 0 );
		mOperations = new ArrayList<MoaGraphicsOperationParameter>();
		mCurrentOperation = null;
		mAction.setValue( "commands", mOperations );

		initAdapter( mGallerySize, new GallerySizeAdapter( getContext().getBaseContext(), mBrushSizes ), 0 );
		initAdapter( mGalleryColor, new GalleryColorAdapter( getContext().getBaseContext(), mBrushColors ), 1 );
		initPaint();
	}

	/**
	 * Inits the adapter.
	 * 
	 * @param gallery
	 *           the gallery
	 * @param adapter
	 *           the adapter
	 * @param selectedPosition
	 *           the selected position
	 */
	private void initAdapter( final Gallery gallery, final BaseAdapter adapter, final int selectedPosition ) {
		int height = gallery.getHeight();
		if ( height < 1 ) {
			gallery.getHandler().post( new Runnable() {

				@Override
				public void run() {
					initAdapter( gallery, adapter, selectedPosition );
				}
			} );
			return;
		}

		gallery.setAdapter( adapter );
		gallery.setSelection( selectedPosition, false, true );
	}

	/**
	 * Reset bitmap.
	 */
	private void resetBitmap() {
		mImageView.setImageBitmap( mBitmap, true, getContext().getCurrentImageViewMatrix(), UIConfiguration.IMAGE_VIEW_MAX_ZOOM );
		( (ImageViewTouchAndDraw) mImageView ).setDrawMode( TouchMode.DRAW );

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aviary.android.feather.effects.AbstractEffectPanel#onActivate()
	 */
	@Override
	public void onActivate() {
		super.onActivate();

		disableHapticIsNecessary( mGalleryColor, mGallerySize );
		initToast();

		mGallerySize.setOnItemsScrollListener( new OnItemsScrollListener() {

			@Override
			public void onScrollFinished( AdapterView<?> parent, View view, int position, long id ) {
				mSize = (Integer) mGallerySize.getAdapter().getItem( position );
				final boolean soft = ( (GallerySizeAdapter) mGallerySize.getAdapter() ).getIsSoft( position );

				if ( soft )
					mPaint.setMaskFilter( new BlurMaskFilter( mBlur, Blur.NORMAL ) );
				else
					mPaint.setMaskFilter( null );

				updatePaint();
				updateSelectedSize( view, position );
				hideToastPreview();
			}

			@Override
			public void onScrollStarted( AdapterView<?> parent, View view, int position, long id ) {
				showToastPreview();

				if ( getSelectedTool() == DrawinTool.Zoom ) {
					setSelectedTool( DrawinTool.Draw );
				}
			}

			@Override
			public void onScroll( AdapterView<?> parent, View view, int position, long id ) {
				GallerySizeAdapter adapter = (GallerySizeAdapter) parent.getAdapter();
				int size = (Integer) adapter.getItem( position );
				int blur = adapter.getIsSoft( position ) ? mBlur : 0;
				boolean is_eraser = mGalleryColor.getSelectedItemPosition() == 0
						|| mGalleryColor.getSelectedItemPosition() == mGalleryColor.getAdapter().getCount() - 1;
				if ( is_eraser ) {
					updateToastPreview( size, Color.WHITE, blur, true );
				} else {
					updateToastPreview( size, mColor, blur, false );
				}
			}
		} );

		mGalleryColor.setOnItemsScrollListener( new OnItemsScrollListener() {

			@Override
			public void onScrollFinished( AdapterView<?> parent, View view, int position, long id ) {
				mColor = (Integer) parent.getAdapter().getItem( position );

				final boolean is_eraser = position == 0 || ( position == parent.getAdapter().getCount() - 1 );

				if ( is_eraser ) {
					mColor = 0;
				}

				mPaint.setColor( mColor );

				if ( getSelectedTool() == DrawinTool.Zoom ) {
					if ( is_eraser )
						setSelectedTool( DrawinTool.Erase );
					else
						setSelectedTool( DrawinTool.Draw );
				} else {
					if ( is_eraser && getSelectedTool() != DrawinTool.Erase )
						setSelectedTool( DrawinTool.Erase );
					else if ( !is_eraser && getSelectedTool() != DrawinTool.Draw ) setSelectedTool( DrawinTool.Draw );
				}

				updatePaint();
				updateSelectedColor( view, position );
				hideToastPreview();
			}

			@Override
			public void onScrollStarted( AdapterView<?> parent, View view, int position, long id ) {
				showToastPreview();
				if ( getSelectedTool() == DrawinTool.Zoom ) {
					setSelectedTool( DrawinTool.Draw );
				}
			}

			@Override
			public void onScroll( AdapterView<?> parent, View view, int position, long id ) {

				final boolean is_eraser = position == 0 || ( position == parent.getAdapter().getCount() - 1 );

				if ( is_eraser ) {
					updateToastPreview( mSize, Color.WHITE, mBlur, true );
				} else {
					updateToastPreview( mSize, mBrushColors[position], mBlur, false );
				}
			}
		} );

		mLensButton.setOnClickListener( new OnClickListener() {

			@Override
			public void onClick( View arg0 ) {

				boolean selected = arg0.isSelected();
				arg0.setSelected( !selected );

				if ( arg0.isSelected() ) {
					setSelectedTool( DrawinTool.Zoom );
				} else {
					if ( mGalleryColor.getSelectedItemPosition() == 0 ) {
						setSelectedTool( DrawinTool.Erase );
					} else {
						setSelectedTool( DrawinTool.Draw );
					}
					updatePaint();
				}
			}
		} );

		setSelectedTool( DrawinTool.Draw );
		updatePaint();
		updateSelectedSize( (View) mGallerySize.getSelectedView(), mGallerySize.getSelectedItemPosition() );
		updateSelectedColor( (View) mGalleryColor.getSelectedView(), mGalleryColor.getSelectedItemPosition() );

		( (ImageViewTouchAndDraw) mImageView ).setOnDrawStartListener( this );
		( (ImageViewTouchAndDraw) mImageView ).setOnDrawPathListener( this );
		mLensButton.setVisibility( View.VISIBLE );

		getContentView().setVisibility( View.VISIBLE );
		contentReady();
	}

	/**
	 * Update selected size.
	 * 
	 * @param newSelection
	 *           the new selection
	 * @param position
	 *           the position
	 */
	protected void updateSelectedSize( View newSelection, int position ) {
		if ( mSelectedSizeView != null ) {
			mSelectedSizeView.setSelected( false );
		}

		mSelectedSizeView = newSelection;
		mSelectedSizePosition = position;

		if ( mSelectedSizeView != null ) {
			mSelectedSizeView = newSelection;
			mSelectedSizeView.setSelected( true );
		}
	}

	/**
	 * Update selected color.
	 * 
	 * @param newSelection
	 *           the new selection
	 * @param position
	 *           the position
	 */
	protected void updateSelectedColor( View newSelection, int position ) {
		if ( mSelectedColorView != null ) {
			mSelectedColorView.setSelected( false );
		}

		mSelectedColorView = newSelection;
		mSelectedColorPosition = position;

		if ( mSelectedColorView != null ) {
			mSelectedColorView = newSelection;
			mSelectedColorView.setSelected( true );
		}
	}

	/**
	 * Sets the selected tool.
	 * 
	 * @param which
	 *           the new selected tool
	 */
	private void setSelectedTool( DrawinTool which ) {

		switch ( which ) {
			case Draw:
				( (ImageViewTouchAndDraw) mImageView ).setDrawMode( TouchMode.DRAW );
				mPaint.setAlpha( 255 );
				mPaint.setXfermode( null );
				updatePaint();
				break;

			case Erase:
				( (ImageViewTouchAndDraw) mImageView ).setDrawMode( TouchMode.DRAW );
				mPaint.setXfermode( new PorterDuffXfermode( PorterDuff.Mode.CLEAR ) );
				mPaint.setAlpha( 0 );
				updatePaint();
				break;

			case Zoom:
				( (ImageViewTouchAndDraw) mImageView ).setDrawMode( TouchMode.IMAGE );
				break;
		}

		mLensButton.setSelected( which == DrawinTool.Zoom );
		setPanelEnabled( which != DrawinTool.Zoom );
		mSelectedTool = which;
	}

	/**
	 * Sets the panel enabled.
	 * 
	 * @param value
	 *           the new panel enabled
	 */
	public void setPanelEnabled( boolean value ) {
		
		if( !isActive() ) return;

		if ( value ) {
			getContext().restoreToolbarTitle();
		} else {
			getContext().setToolbarTitle( R.string.zoom_mode );
		}

		mOptionView.findViewById( R.id.disable_status ).setVisibility( value ? View.INVISIBLE : View.VISIBLE );
	}

	/**
	 * Gets the selected tool.
	 * 
	 * @return the selected tool
	 */
	private DrawinTool getSelectedTool() {
		return mSelectedTool;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aviary.android.feather.effects.AbstractEffectPanel#onDeactivate()
	 */
	@Override
	public void onDeactivate() {
		( (ImageViewTouchAndDraw) mImageView ).setOnDrawStartListener( null );
		( (ImageViewTouchAndDraw) mImageView ).setOnDrawPathListener( null );
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
		mImageView.clear();
		mToast.hide();
	}

	/**
	 * Inits the paint.
	 */
	private void initPaint() {
		mPaint = new Paint( Paint.ANTI_ALIAS_FLAG );
		mPaint.setFilterBitmap( false );
		mPaint.setDither( true );
		mPaint.setColor( mColor );
		mPaint.setStrokeWidth( mSize );
		mPaint.setStyle( Paint.Style.STROKE );
		mPaint.setStrokeJoin( Paint.Join.ROUND );
		mPaint.setStrokeCap( Paint.Cap.ROUND );
	}

	/**
	 * Update paint.
	 */
	private void updatePaint() {
		mPaint.setStrokeWidth( mSize );
		( (ImageViewTouchAndDraw) mImageView ).setPaint( mPaint );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aviary.android.feather.effects.AbstractContentPanel#generateContentView(android.view.LayoutInflater)
	 */
	@Override
	protected View generateContentView( LayoutInflater inflater ) {
		return inflater.inflate( R.layout.feather_drawing_content, null );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aviary.android.feather.effects.AbstractOptionPanel#generateOptionView(android.view.LayoutInflater,
	 * android.view.ViewGroup)
	 */
	@Override
	protected ViewGroup generateOptionView( LayoutInflater inflater, ViewGroup parent ) {
		return (ViewGroup) inflater.inflate( R.layout.feather_drawing_panel, parent, false );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aviary.android.feather.effects.AbstractEffectPanel#onGenerateResult()
	 */
	@Override
	protected void onGenerateResult() {

		Bitmap bitmap = null;

		if ( !mBitmap.isMutable() ) {
			bitmap = BitmapUtils.copy( mBitmap, mBitmap.getConfig() );
		} else {
			bitmap = mBitmap;
		}

		Canvas canvas = new Canvas( bitmap );
		( (ImageViewTouchAndDraw) mImageView ).commit( canvas );
		( (ImageViewTouchAndDraw) mImageView ).setImageBitmap( bitmap, false );
		onComplete( bitmap, mActionList );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aviary.android.feather.widget.ImageViewTouchAndDraw.OnDrawStartListener#onDrawStart()
	 */
	@Override
	public void onDrawStart() {
		setIsChanged( true );
	}

	/**
	 * The Class GallerySizeAdapter.
	 */
	class GallerySizeAdapter extends BaseAdapter {
		
		private static final int VALID_POSITION = 0;
		private static final int INVALID_POSITION = 1;

		/** The sizes. */
		private int[] sizes;

		/** The m layout inflater. */
		LayoutInflater mLayoutInflater;

		/** The m res. */
		Resources mRes;

		/** The m biggest. */
		int mBiggest;

		/**
		 * Instantiates a new gallery size adapter.
		 * 
		 * @param context
		 *           the context
		 * @param values
		 *           the values
		 */
		public GallerySizeAdapter( Context context, int[] values ) {
			mLayoutInflater = UIUtils.getLayoutInflater();
			sizes = values;
			mRes = getContext().getBaseContext().getResources();
			mBiggest = sizes[sizes.length - 1];
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

		/**
		 * Gets the checks if is soft.
		 * 
		 * @param position
		 *           the position
		 * @return the checks if is soft
		 */
		public boolean getIsSoft( int position ) {
			return true;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.widget.Adapter#getItemId(int)
		 */
		@Override
		public long getItemId( int position ) {
			return position;
		}
		
		@Override
		public int getItemViewType( int position ) {
			final boolean valid = position >= 0 && position < getCount();
			return valid ? VALID_POSITION : INVALID_POSITION;
		}
		
		@Override
		public int getViewTypeCount() {
			return 2;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
		 */
		@SuppressWarnings("deprecation")
		@Override
		public View getView( int position, View convertView, ViewGroup parent ) {

			final int type = getItemViewType( position );
			GalleryCircleDrawable mCircleDrawable = null;

			View view;
			if ( convertView == null ) {
				if ( type == VALID_POSITION ) {
					view = mLayoutInflater.inflate( R.layout.feather_checkbox_button, mGallerySize, false );
					mCircleDrawable = new GalleryCircleDrawable( 1, 0 );
					Drawable unselectedBackground = new OverlayGalleryCheckboxDrawable( mRes, false, mCircleDrawable, 1.0f, 0.4f );
					Drawable selectedBackground = new OverlayGalleryCheckboxDrawable( mRes, true, mCircleDrawable, 1.0f, 0.4f );
					StateListDrawable st = new StateListDrawable();
					st.addState( new int[] { -attr.state_selected }, unselectedBackground );
					st.addState( new int[] { attr.state_selected }, selectedBackground );
					view.setBackgroundDrawable( st );
					view.setTag( mCircleDrawable );
				} else {
					// use the blank view
					view = mLayoutInflater.inflate( R.layout.feather_default_blank_gallery_item, mGallerySize, false );
					Drawable unselectedBackground = new DefaultGalleryCheckboxDrawable( mRes, false );
					view.setBackgroundDrawable( unselectedBackground );
				}
			} else {
				view = convertView;
				if ( type == VALID_POSITION ) {
					mCircleDrawable = (GalleryCircleDrawable) view.getTag();
				}
			}

			if ( type == VALID_POSITION && mCircleDrawable != null ) {
				int size = (Integer) getItem( position );
				float value = (float) size / mBiggest;
				mCircleDrawable.update( value, 0 );
			}
			view.setSelected( position == mSelectedSizePosition );
			return view;
		}
	}

	/**
	 * The Class GalleryColorAdapter.
	 */
	class GalleryColorAdapter extends BaseAdapter {
		
		private static final int VALID_POSITION = 0;
		private static final int INVALID_POSITION = 1;

		/** The colors. */
		private int[] colors;

		/** The m layout inflater. */
		LayoutInflater mLayoutInflater;

		/** The m res. */
		Resources mRes;

		/**
		 * Instantiates a new gallery color adapter.
		 * 
		 * @param context
		 *           the context
		 * @param values
		 *           the values
		 */
		public GalleryColorAdapter( Context context, int[] values ) {
			mLayoutInflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
			colors = values;
			mRes = getContext().getBaseContext().getResources();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.widget.Adapter#getCount()
		 */
		@Override
		public int getCount() {
			return colors.length;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.widget.Adapter#getItem(int)
		 */
		@Override
		public Object getItem( int position ) {
			return colors[position];
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
		
		@Override
		public int getViewTypeCount() {
			return 2;
		}
		
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
		public View getView( int position, View convertView, ViewGroup parent ) {

			ImageView mask = null;
			View rubber = null;
			View view;

			final int type = getItemViewType( position );

			if ( convertView == null ) {
				if ( type == VALID_POSITION ) {
					view = mLayoutInflater.inflate( R.layout.feather_color_button, mGalleryColor, false );
					Drawable unselectedBackground = new OverlayGalleryCheckboxDrawable( mRes, false, null, 1.0f, 20 );
					Drawable selectedBackground = new OverlayGalleryCheckboxDrawable( mRes, true, null, 1.0f, 20 );
					StateListDrawable st = new StateListDrawable();
					st.addState( new int[] { -attr.state_selected }, unselectedBackground );
					st.addState( new int[] { attr.state_selected }, selectedBackground );

					rubber = view.findViewById( R.id.rubber );
					mask = (ImageView) view.findViewById( R.id.color_mask );

					view.setBackgroundDrawable( st );
				} else {
					// use the blank view
					view = mLayoutInflater.inflate( R.layout.feather_checkbox_button, mGalleryColor, false );
					Drawable unselectedBackground = new DefaultGalleryCheckboxDrawable( mRes, false );
					view.setBackgroundDrawable( unselectedBackground );
				}

			} else {
				view = convertView;
				if ( type == VALID_POSITION ) {
					rubber = view.findViewById( R.id.rubber );
					mask = (ImageView) view.findViewById( R.id.color_mask );
				}
			}

			if ( type == VALID_POSITION ) {
				final int color = (Integer) getItem( position );
				final boolean is_eraser = position == 0 || position == getCount() - 1;

				view.setSelected( position == mSelectedColorPosition );

				if ( !is_eraser ) {

					LayerDrawable layer = (LayerDrawable) mask.getDrawable();
					GradientDrawable shape = (GradientDrawable) layer.findDrawableByLayerId( R.id.masked );
					shape.setColor( color );

					mask.setVisibility( View.VISIBLE );
					rubber.setVisibility( View.GONE );
				} else {
					mask.setVisibility( View.GONE );
					rubber.setVisibility( View.VISIBLE );
				}
			}

			return view;
		}
	}

	@Override
	public void onStart() {
		final float scale = mImageView.getScale();
		mCurrentOperation = new MoaGraphicsOperationParameter( mBlur, ( (float) mSize / scale ) / mWidth, mColor,
				getSelectedTool() == DrawinTool.Erase ? 1 : 0 );
	}

	@Override
	public void onMoveTo( float x, float y ) {
		mCurrentOperation.addCommand( new MoaGraphicsCommandParameter( MoaGraphicsCommandParameter.COMMAND_MOVETO, x / mWidth, y
				/ mHeight ) );
	}

	@Override
	public void onLineTo( float x, float y ) {
		mCurrentOperation.addCommand( new MoaGraphicsCommandParameter( MoaGraphicsCommandParameter.COMMAND_LINETO, x / mWidth, y
				/ mHeight ) );
	}

	@Override
	public void onQuadTo( float x, float y, float x1, float y1 ) {
		mCurrentOperation.addCommand( new MoaGraphicsCommandParameter( MoaGraphicsCommandParameter.COMMAND_QUADTO, x / mWidth, y
				/ mHeight, x1 / mWidth, y1 / mHeight ) );
	}

	@Override
	public void onEnd() {
		mOperations.add( mCurrentOperation );
	}
}
