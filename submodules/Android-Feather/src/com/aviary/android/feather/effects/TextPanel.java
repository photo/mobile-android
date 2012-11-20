package com.aviary.android.feather.effects;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import android.R.attr;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.StateListDrawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import com.aviary.android.feather.R;
import com.aviary.android.feather.graphics.DefaultGalleryCheckboxDrawable;
import com.aviary.android.feather.graphics.OverlayGalleryCheckboxDrawable;
import com.aviary.android.feather.library.graphics.drawable.EditableDrawable;
import com.aviary.android.feather.library.graphics.drawable.TextDrawable;
import com.aviary.android.feather.library.moa.MoaAction;
import com.aviary.android.feather.library.moa.MoaActionFactory;
import com.aviary.android.feather.library.moa.MoaActionList;
import com.aviary.android.feather.library.moa.MoaColorParameter;
import com.aviary.android.feather.library.moa.MoaPointParameter;
import com.aviary.android.feather.library.services.ConfigService;
import com.aviary.android.feather.library.services.EffectContext;
import com.aviary.android.feather.library.utils.BitmapUtils;
import com.aviary.android.feather.library.utils.MatrixUtils;
import com.aviary.android.feather.library.utils.UIConfiguration;
import com.aviary.android.feather.utils.UIUtils;
import com.aviary.android.feather.widget.AdapterView;
import com.aviary.android.feather.widget.DrawableHighlightView;
import com.aviary.android.feather.widget.Gallery;
import com.aviary.android.feather.widget.Gallery.OnItemsScrollListener;
import com.aviary.android.feather.widget.ImageViewDrawableOverlay;
import com.aviary.android.feather.widget.ImageViewDrawableOverlay.OnDrawableEventListener;

public class TextPanel extends AbstractContentPanel implements OnDrawableEventListener, OnEditorActionListener {

	abstract class MyTextWatcher implements TextWatcher {

		public DrawableHighlightView view;
	}

	Gallery mGallery;
	View mSelected;
	int mSelectedPosition;

	/** The available text colors. */
	int[] mColors;

	/** The available text stroke colors. */
	int[] mStrokeColors;

	/** The current selected color. */
	private int mColor = 0;

	/** The current selected stroke color. */
	private int mStrokeColor = 0;

	/** The minimum text size. */
	private int minTextSize = 16;

	/** The default text size. */
	private int defaultTextSize = 16;

	/** The text padding. */
	private int textPadding = 10;

	/** The drawing canvas. */
	private Canvas mCanvas;

	/** The android input manager. */
	private InputMethodManager mInputManager;

	/** The current edit text. */
	private EditText mEditText;

	private ConfigService config;

	/** The m highlight stroke color down. */
	private int mHighlightEllipse, mHighlightStrokeWidth;
	private ColorStateList mHighlightFillColorStateList, mHighlightStrokeColorStateList;

	/** The m edit text watcher. */
	private final MyTextWatcher mEditTextWatcher = new MyTextWatcher() {

		@Override
		public void afterTextChanged( final Editable s ) {
			mLogger.info( "afterTextChanged" );
		}

		@Override
		public void beforeTextChanged( final CharSequence s, final int start, final int count, final int after ) {
			mLogger.info( "beforeTextChanged" );
		}

		@Override
		public void onTextChanged( final CharSequence s, final int start, final int before, final int count ) {
			if ( ( view != null ) && ( view.getContent() instanceof EditableDrawable ) ) {
				final EditableDrawable editable = (EditableDrawable) view.getContent();

				if ( !editable.isEditing() ) return;

				editable.setText( s.toString() );
				view.forceUpdate();
			}
		}
	};

	/**
	 * Instantiates a new text panel.
	 * 
	 * @param context
	 *           the context
	 */
	public TextPanel( final EffectContext context ) {
		super( context );
	}

	/**
	 * Begin edit and open the android soft keyboard if available
	 * 
	 * @param view
	 *           the view
	 */
	private void beginEdit( final DrawableHighlightView view ) {
		
		if( null != view ){
			view.setFocused( true );
		}
		
		mEditTextWatcher.view = null;
		mEditText.removeTextChangedListener( mEditTextWatcher );
		mEditText.setOnKeyListener( null );

		final EditableDrawable editable = (EditableDrawable) view.getContent();
		final String oldText = editable.isTextHint() ? "" : (String) editable.getText();
		mEditText.setText( oldText );
		mEditText.setSelection( mEditText.length() );
		mEditText.setImeOptions( EditorInfo.IME_ACTION_DONE );
		mEditText.requestFocusFromTouch();
		// mInputManager.showSoftInput( mEditText, InputMethodManager.SHOW_IMPLICIT );
		mInputManager.toggleSoftInput( InputMethodManager.SHOW_FORCED, 0 );

		mEditTextWatcher.view = view;
		mEditText.setOnEditorActionListener( this );
		mEditText.addTextChangedListener( mEditTextWatcher );
		mEditText.setOnKeyListener( new OnKeyListener() {

			@Override
			public boolean onKey( View v, int keyCode, KeyEvent event ) {
				mLogger.log( "onKey: " + event );
				if ( keyCode == KeyEvent.KEYCODE_DEL || keyCode == KeyEvent.KEYCODE_BACK ) {
					if ( editable.isTextHint() && editable.isEditing() ) {
						editable.setText( "" );
						view.forceUpdate();
					}
				}
				return false;
			}
		} );
	}

	/**
	 * Creates the and configure preview.
	 */
	private void createAndConfigurePreview() {

		if ( ( mPreview != null ) && !mPreview.isRecycled() ) {
			mPreview.recycle();
			mPreview = null;
		}

		mPreview = BitmapUtils.copy( mBitmap, mBitmap.getConfig() );
		mCanvas = new Canvas( mPreview );
	}

	/**
	 * End edit text and closes the android keyboard
	 * 
	 * @param view
	 *           the view
	 */
	private void endEdit( final DrawableHighlightView view ) {
		if( null != view ){
			view.setFocused( false );
			view.forceUpdate();
		}
		mEditTextWatcher.view = null;
		mEditText.removeTextChangedListener( mEditTextWatcher );
		mEditText.setOnKeyListener( null );
		if ( mInputManager.isActive( mEditText ) ) mInputManager.hideSoftInputFromWindow( mEditText.getWindowToken(), 0 );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aviary.android.feather.effects.AbstractContentPanel#generateContentView(android.view.LayoutInflater)
	 */
	@Override
	protected View generateContentView( final LayoutInflater inflater ) {
		return inflater.inflate( R.layout.feather_text_content, null );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aviary.android.feather.effects.AbstractOptionPanel#generateOptionView(android.view.LayoutInflater,
	 * android.view.ViewGroup)
	 */
	@Override
	protected ViewGroup generateOptionView( final LayoutInflater inflater, ViewGroup parent ) {
		return (ViewGroup) inflater.inflate( R.layout.feather_text_panel, parent, false );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aviary.android.feather.effects.AbstractEffectPanel#getIsChanged()
	 */
	@Override
	public boolean getIsChanged() {
		return super.getIsChanged() || ( ( (ImageViewDrawableOverlay) mImageView ).getHighlightCount() > 0 );
	}

	/**
	 * Add a new editable text on the screen.
	 */
	private void onAddNewText() {
		final ImageViewDrawableOverlay image = (ImageViewDrawableOverlay) mImageView;

		if ( image.getHighlightCount() > 0 ) onApplyCurrent( image.getHighlightViewAt( 0 ) );

		final TextDrawable text = new TextDrawable( "", defaultTextSize );
		text.setTextColor( mColor );
		text.setTextHint( getContext().getBaseContext().getString( R.string.enter_text_here ) );

		final DrawableHighlightView hv = new DrawableHighlightView( mImageView, text );

		final Matrix mImageMatrix = mImageView.getImageViewMatrix();

		final int width = mImageView.getWidth();
		final int height = mImageView.getHeight();
		final int imageSize = Math.max( width, height );

		// width/height of the sticker
		int cropWidth = text.getIntrinsicWidth();
		int cropHeight = text.getIntrinsicHeight();

		final int cropSize = Math.max( cropWidth, cropHeight );

		if ( cropSize > imageSize ) {
			cropWidth = width / 2;
			cropHeight = height / 2;
		}

		final int x = ( width - cropWidth ) / 2;
		final int y = ( height - cropHeight ) / 2;

		final Matrix matrix = new Matrix( mImageMatrix );
		matrix.invert( matrix );

		final float[] pts = new float[] { x, y, x + cropWidth, y + cropHeight };
		MatrixUtils.mapPoints( matrix, pts );

		final RectF cropRect = new RectF( pts[0], pts[1], pts[2], pts[3] );
		final Rect imageRect = new Rect( 0, 0, width, height );

		hv.setRotateAndScale( true );
		hv.showDelete( false );

		hv.setup( mImageMatrix, imageRect, cropRect, false );
		hv.drawOutlineFill( true );
		hv.drawOutlineStroke( true );
		hv.setPadding( textPadding );
		hv.setMinSize( minTextSize );
		hv.setOutlineEllipse( mHighlightEllipse );
		
		hv.setOutlineFillColor( mHighlightFillColorStateList );
		hv.setOutlineStrokeColor( mHighlightStrokeColorStateList );

		Paint stroke = hv.getOutlineStrokePaint();
		stroke.setStrokeWidth( mHighlightStrokeWidth );

		image.addHighlightView( hv );
		// image.setSelectedHighlightView( hv );

		onClick( hv );
	}

	/**
	 * apply the current text and flatten it over the image.
	 */
	private MoaActionList onApplyCurrent() {
		final MoaActionList nullActions = MoaActionFactory.actionList();
		final ImageViewDrawableOverlay image = (ImageViewDrawableOverlay) mImageView;
		if ( image.getHighlightCount() < 1 ) return nullActions;
		final DrawableHighlightView hv = ( (ImageViewDrawableOverlay) mImageView ).getHighlightViewAt( 0 );

		if ( hv != null ) {

			if ( hv.getContent() instanceof EditableDrawable ) {
				EditableDrawable editable = (EditableDrawable) hv.getContent();
				if ( editable != null ) {
					if ( editable.isTextHint() ) {
						setIsChanged( false );
						return nullActions;
					}
				}
			}
			return onApplyCurrent( hv );
		}
		return nullActions;
	}

	/**
	 * Flatten the view on the current image
	 * 
	 * @param hv
	 *           the hv
	 */
	private MoaActionList onApplyCurrent( final DrawableHighlightView hv ) {

		MoaActionList actionList = MoaActionFactory.actionList();

		if ( hv != null ) {
			setIsChanged( true );

			final RectF cropRect = hv.getCropRectF();
			final Rect rect = new Rect( (int) cropRect.left, (int) cropRect.top, (int) cropRect.right, (int) cropRect.bottom );
			final Matrix rotateMatrix = hv.getCropRotationMatrix();
			final int w = mBitmap.getWidth();
			final int h = mBitmap.getHeight();
			final float left = cropRect.left / w;
			final float top = cropRect.top / h;
			final float right = cropRect.right / w;
			final float bottom = cropRect.bottom / h;

			final Matrix matrix = new Matrix( mImageView.getImageMatrix() );
			if ( !matrix.invert( matrix ) ) mLogger.error( "unable to invert matrix" );

			EditableDrawable editable = (EditableDrawable) hv.getContent();
			editable.endEdit();
			mImageView.invalidate();

			MoaAction action = MoaActionFactory.action( "addtext" );
			action.setValue( "text", (String) editable.getText() );
			action.setValue( "fillcolor", new MoaColorParameter( mColor ) );
			action.setValue( "outlinecolor", new MoaColorParameter( mStrokeColor ) );
			action.setValue( "rotation", hv.getRotation() );
			action.setValue( "topleft", new MoaPointParameter( left, top ) );
			action.setValue( "bottomright", new MoaPointParameter( right, bottom ) );
			actionList.add( action );

			final int saveCount = mCanvas.save( Canvas.MATRIX_SAVE_FLAG );
			mCanvas.concat( rotateMatrix );
			hv.getContent().setBounds( rect );

			hv.getContent().draw( mCanvas );
			mCanvas.restoreToCount( saveCount );
			mImageView.invalidate();
			onClearCurrent( hv );
		}

		onPreviewChanged( mPreview, false );
		return actionList;
	}

	/**
	 * Removes the current text
	 * 
	 * @param hv
	 *           the hv
	 */
	private void onClearCurrent( final DrawableHighlightView hv ) {
		hv.setOnDeleteClickListener( null );
		( (ImageViewDrawableOverlay) mImageView ).removeHightlightView( hv );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.aviary.android.feather.widget.ImageViewDrawableOverlay.OnDrawableEventListener#onClick(com.aviary.android.feather.widget
	 * .DrawableHighlightView)
	 */
	@Override
	public void onClick( final DrawableHighlightView view ) {
		if ( view != null ) if ( view.getContent() instanceof EditableDrawable ) {
			
			if( !view.getFocused() ){
				beginEdit( view );
			}
			
			/*
			final EditableDrawable text = (EditableDrawable) view.getContent();
			if ( !text.isEditing() ) {
				text.beginEdit();
				beginEdit( view );
			}
			*/
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aviary.android.feather.effects.AbstractEffectPanel#onCreate(android.graphics.Bitmap)
	 */
	@Override
	public void onCreate( final Bitmap bitmap ) {
		super.onCreate( bitmap );

		config = getContext().getService( ConfigService.class );

		mColors = config.getIntArray( R.array.feather_text_fill_colors );
		mStrokeColors = config.getIntArray( R.array.feather_text_stroke_colors );

		mSelectedPosition = config.getInteger( R.integer.feather_text_selected_color );
		mColor = mColors[mSelectedPosition];
		mStrokeColor = mStrokeColors[mSelectedPosition];

		mGallery = (Gallery) getOptionView().findViewById( R.id.gallery );
		mGallery.setCallbackDuringFling( false );
		mGallery.setSpacing( 0 );
		mGallery.setAdapter( new GalleryAdapter( getContext().getBaseContext(), mColors ) );
		mGallery.setSelection( mSelectedPosition, false, true );
		mGallery.setOnItemsScrollListener( new OnItemsScrollListener() {

			@Override
			public void onScrollFinished( AdapterView<?> parent, View view, int position, long id ) {
				updateSelection( view, position );

				final int color = mColors[position];
				mColor = color;
				mStrokeColor = mStrokeColors[position];

				updateCurrentHighlightColor();
				updateColorButtonBitmap();
			}

			@Override
			public void onScrollStarted( AdapterView<?> parent, View view, int position, long id ) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onScroll( AdapterView<?> parent, View view, int position, long id ) {
				// TODO Auto-generated method stub

			}
		} );

		mEditText = (EditText) getContentView().findViewById( R.id.invisible_text );
		mImageView = (ImageViewTouch) getContentView().findViewById( R.id.overlay );
		mImageView.setDoubleTapEnabled( false );

		createAndConfigurePreview();
		updateColorButtonBitmap();

		mImageView.setImageBitmap( mPreview, true, getContext().getCurrentImageViewMatrix(), UIConfiguration.IMAGE_VIEW_MAX_ZOOM );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aviary.android.feather.effects.AbstractEffectPanel#onActivate()
	 */
	@Override
	public void onActivate() {
		super.onActivate();

		disableHapticIsNecessary( mGallery );

		mHighlightFillColorStateList = config.getColorStateList( R.color.feather_effect_text_color_fill_selector );
		mHighlightStrokeColorStateList = config.getColorStateList( R.color.feather_effect_text_color_stroke_selector );
		
		mHighlightEllipse = config.getInteger( R.integer.feather_text_highlight_ellipse );
		mHighlightStrokeWidth = config.getInteger( R.integer.feather_text_highlight_stroke_width );
		
		minTextSize = config.getDimensionPixelSize( R.dimen.feather_text_drawable_min_size );
		defaultTextSize = config.getDimensionPixelSize( R.dimen.feather_text_default_size );
		defaultTextSize = Math.max( minTextSize, defaultTextSize );
		
		textPadding = config.getInteger( R.integer.feather_text_padding );

		( (ImageViewDrawableOverlay) mImageView ).setOnDrawableEventListener( this );
		( (ImageViewDrawableOverlay) mImageView ).setScaleWithContent( true );
		( (ImageViewDrawableOverlay) mImageView ).setForceSingleSelection( false );
		
		mInputManager = (InputMethodManager) getContext().getBaseContext().getSystemService( Context.INPUT_METHOD_SERVICE );

		mImageView.requestLayout();

		updateSelection( (View) mGallery.getSelectedView(), mGallery.getSelectedItemPosition() );
		contentReady();
		onAddNewText();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aviary.android.feather.effects.AbstractEffectPanel#onDeactivate()
	 */
	@Override
	public void onDeactivate() {
		( (ImageViewDrawableOverlay) mImageView ).setOnDrawableEventListener( null );
		endEdit( null );
		super.onDeactivate();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aviary.android.feather.effects.AbstractEffectPanel#onDestroy()
	 */
	@Override
	public void onDestroy() {
		mCanvas = null;
		mInputManager = null;
		super.onDestroy();
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.aviary.android.feather.widget.ImageViewDrawableOverlay.OnDrawableEventListener#onDown(com.aviary.android.feather.widget
	 * .DrawableHighlightView)
	 */
	@Override
	public void onDown( final DrawableHighlightView view ) {}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.aviary.android.feather.widget.ImageViewDrawableOverlay.OnDrawableEventListener#onFocusChange(com.aviary.android.feather
	 * .widget.DrawableHighlightView, com.aviary.android.feather.widget.DrawableHighlightView)
	 */
	@Override
	public void onFocusChange( final DrawableHighlightView newFocus, final DrawableHighlightView oldFocus ) {
		EditableDrawable text;

		if ( oldFocus != null ) if ( oldFocus.getContent() instanceof EditableDrawable ) {
			text = (EditableDrawable) oldFocus.getContent();
			if ( text.isEditing() ) {
				//text.endEdit();
				endEdit( oldFocus );
			}

			// if ( !oldFocus.equals( newFocus ) )
			// if ( text.getText().length() == 0 )
			// onClearCurrent( oldFocus );
		}

		if ( newFocus != null ) if ( newFocus.getContent() instanceof EditableDrawable ) {
			text = (EditableDrawable) newFocus.getContent();
			mColor = text.getTextColor();
			mStrokeColor = text.getTextStrokeColor();
			updateColorButtonBitmap();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aviary.android.feather.effects.AbstractEffectPanel#onGenerateResult()
	 */
	@Override
	protected void onGenerateResult() {
		MoaActionList actions = onApplyCurrent();
		super.onGenerateResult( actions );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.aviary.android.feather.widget.ImageViewDrawableOverlay.OnDrawableEventListener#onMove(com.aviary.android.feather.widget
	 * .DrawableHighlightView)
	 */
	@Override
	public void onMove( final DrawableHighlightView view ) {
		if ( view.getContent() instanceof EditableDrawable ) if ( ( (EditableDrawable) view.getContent() ).isEditing() ) {
			//( (EditableDrawable) view.getContent() ).endEdit();
			endEdit( view );
		}
	}

	/**
	 * Update color button bitmap.
	 */
	private void updateColorButtonBitmap() {}

	/**
	 * Update current highlight color.
	 */
	private void updateCurrentHighlightColor() {
		final ImageViewDrawableOverlay image = (ImageViewDrawableOverlay) mImageView;
		if ( image.getSelectedHighlightView() != null ) {
			final DrawableHighlightView hv = image.getSelectedHighlightView();
			if ( hv.getContent() instanceof EditableDrawable ) {
				( (EditableDrawable) hv.getContent() ).setTextColor( mColor );
				( (EditableDrawable) hv.getContent() ).setTextStrokeColor( mStrokeColor );
				mImageView.postInvalidate();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.TextView.OnEditorActionListener#onEditorAction(android.widget.TextView, int, android.view.KeyEvent)
	 */
	@Override
	public boolean onEditorAction( TextView v, int actionId, KeyEvent event ) {

		if ( mEditText != null ) {
			if ( mEditText.equals( v ) ) {
				if ( actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_UNSPECIFIED ) {
					final ImageViewDrawableOverlay image = (ImageViewDrawableOverlay) mImageView;
					if ( image.getSelectedHighlightView() != null ) {
						DrawableHighlightView d = image.getSelectedHighlightView();
						if ( d.getContent() instanceof EditableDrawable ) {
							EditableDrawable text = (EditableDrawable) d.getContent();
							if ( text.isEditing() ) {
								//text.endEdit();
								endEdit( d );
							}
						}
					}
				}
			}
		}

		return false;
	}

	class GalleryAdapter extends BaseAdapter {

		private final int VALID_POSITION = 0;
		private final int INVALID_POSITION = 1;
		private int[] colors;
		Resources mRes;
		LayoutInflater mLayoutInflater;

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

			final int type = getItemViewType( position );

			View view;
			if ( convertView == null ) {
				if ( type == VALID_POSITION ) {
					view = mLayoutInflater.inflate( R.layout.feather_color_button, mGallery, false );
					Drawable unselectedBackground = new OverlayGalleryCheckboxDrawable( mRes, false, null, 1.0f, 20 );
					Drawable selectedBackground = new OverlayGalleryCheckboxDrawable( mRes, true, null, 1.0f, 20 );
					StateListDrawable st = new StateListDrawable();
					st.addState( new int[] { -attr.state_selected }, unselectedBackground );
					st.addState( new int[] { attr.state_selected }, selectedBackground );
					view.setBackgroundDrawable( st );
				} else {
					// use the blank view
					view = mLayoutInflater.inflate( R.layout.feather_default_blank_gallery_item, mGallery, false );
					Drawable unselectedBackground = new DefaultGalleryCheckboxDrawable( mRes, false );
					view.setBackgroundDrawable( unselectedBackground );
				}
			} else {
				view = convertView;
			}

			if ( type == VALID_POSITION ) {
				ImageView masked = (ImageView) view.findViewById( R.id.color_mask );
				final int color = (Integer) getItem( position );
				LayerDrawable layer = (LayerDrawable) masked.getDrawable();
				GradientDrawable shape = (GradientDrawable) layer.findDrawableByLayerId( R.id.masked );
				shape.setColor( color );

				view.setSelected( position == mSelectedPosition );
			}

			return view;
		}
	}
}
