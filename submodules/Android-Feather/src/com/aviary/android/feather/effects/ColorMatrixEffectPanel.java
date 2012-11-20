package com.aviary.android.feather.effects;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.ColorMatrixColorFilter;
import android.media.ThumbnailUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.aviary.android.feather.R;
import com.aviary.android.feather.library.filters.AbstractColorMatrixFilter;
import com.aviary.android.feather.library.filters.FilterLoaderFactory;
import com.aviary.android.feather.library.filters.FilterLoaderFactory.Filters;
import com.aviary.android.feather.library.services.ConfigService;
import com.aviary.android.feather.library.services.EffectContext;
import com.aviary.android.feather.library.utils.BitmapUtils;
import com.aviary.android.feather.widget.Wheel;
import com.aviary.android.feather.widget.Wheel.OnScrollListener;
import com.aviary.android.feather.widget.WheelRadio;

// TODO: Auto-generated Javadoc
/**
 * The Class ColorMatrixEffectPanel.
 */
public class ColorMatrixEffectPanel extends AbstractOptionPanel implements OnScrollListener {

	Wheel mWheel;
	WheelRadio mWheelRadio;
	String mResourceName;

	/**
	 * Instantiates a new color matrix effect panel.
	 * 
	 * @param context
	 *           the context
	 * @param type
	 *           the type
	 * @param resourcesBaseName
	 *           the resources base name
	 */
	public ColorMatrixEffectPanel( EffectContext context, Filters type, String resourcesBaseName ) {
		super( context );

		mFilter = FilterLoaderFactory.get( type );

		if ( mFilter instanceof AbstractColorMatrixFilter ) {
			mMinValue = ( (AbstractColorMatrixFilter) mFilter ).getMinValue();
			mMaxValue = ( (AbstractColorMatrixFilter) mFilter ).getMaxValue();
		}
		mResourceName = resourcesBaseName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aviary.android.feather.effects.AbstractEffectPanel#onCreate(android.graphics.Bitmap)
	 */
	@Override
	public void onCreate( Bitmap bitmap ) {
		super.onCreate( bitmap );
		mWheel = (Wheel) getOptionView().findViewById( R.id.wheel );
		mWheelRadio = (WheelRadio) getOptionView().findViewById( R.id.wheel_radio );
		ConfigService config = getContext().getService( ConfigService.class );
		mLivePreview = config.getBoolean( R.integer.feather_brightness_live_preview );
		onCreateIcons();
	}

	protected void onCreateIcons() {
		ImageView icon_small = (ImageView) getOptionView().findViewById( R.id.icon_small );
		ImageView icon_big = (ImageView) getOptionView().findViewById( R.id.icon_big );
		Resources res = getContext().getBaseContext().getResources();

		if ( null != res ) {
			int id = res.getIdentifier( "feather_tool_icon_" + mResourceName, "drawable", getContext().getBaseContext().getPackageName() );
			if ( id > 0 ) {

				Bitmap big, small;

				try {
					Bitmap bmp = BitmapFactory.decodeResource( res, id );
					big = ThumbnailUtils.extractThumbnail( bmp, (int) ( bmp.getWidth() / 1.5 ), (int) ( bmp.getHeight() / 1.5 ) );
					bmp.recycle();
					small = ThumbnailUtils.extractThumbnail( big, (int) ( big.getWidth() / 1.5 ), (int) ( big.getHeight() / 1.5 ) );
				} catch ( OutOfMemoryError e ) {
					e.printStackTrace();
					return;
				}
				icon_big.setImageBitmap( big );
				icon_small.setImageBitmap( small );
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aviary.android.feather.effects.AbstractEffectPanel#onActivate()
	 */
	@Override
	public void onActivate() {
		super.onActivate();

		disableHapticIsNecessary( mWheel );

		int ticksCount = mWheel.getTicksCount();
		mWheelRadio.setTicksNumber( ticksCount / 2, mWheel.getWheelScaleFactor() );
		mWheel.setOnScrollListener( this );

		// we intercept the touch event from the whole option panel
		// and send it to the wheel, so it's like the wheel component
		// interacts with the entire option view
		getOptionView().setOnTouchListener( new OnTouchListener() {

			@Override
			public boolean onTouch( View v, MotionEvent event ) {
				return mWheel.onTouchEvent( event );
			}
		} );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aviary.android.feather.effects.AbstractEffectPanel#onDeactivate()
	 */
	@Override
	public void onDeactivate() {

		getOptionView().setOnTouchListener( null );

		super.onDeactivate();
		mWheel.setOnScrollListener( null );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aviary.android.feather.effects.AbstractOptionPanel#generateOptionView(android.view.LayoutInflater,
	 * android.view.ViewGroup)
	 */
	@Override
	protected ViewGroup generateOptionView( LayoutInflater inflater, ViewGroup parent ) {
		return (ViewGroup) inflater.inflate( R.layout.feather_wheel_panel, parent, false );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aviary.android.feather.widget.Wheel.OnScrollListener#onScrollStarted(com.aviary.android.feather.widget.Wheel, float,
	 * int)
	 */
	@Override
	public void onScrollStarted( Wheel view, float value, int roundValue ) {}

	/** The m last value. */
	int mLastValue;

	/** The m current real value. */
	float mCurrentRealValue;

	/** The m min value. */
	float mMinValue = 0;

	/** The m max value. */
	float mMaxValue = 1;

	boolean mLivePreview = false;

	/**
	 * On apply value.
	 * 
	 * @param value
	 *           the value
	 */
	private void onApplyValue( float value ) {
		float realValue = 1f + value;
		float range = mMaxValue - mMinValue;
		float perc = mMinValue + ( ( realValue / 2.0F ) * range );
		mCurrentRealValue = perc;
		final ColorMatrixColorFilter c = ( (AbstractColorMatrixFilter) mFilter ).apply( perc );
		onPreviewChanged( c, true );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aviary.android.feather.widget.Wheel.OnScrollListener#onScroll(com.aviary.android.feather.widget.Wheel, float, int)
	 */
	@Override
	public void onScroll( Wheel view, float value, int roundValue ) {
		mWheelRadio.setValue( value );

		if ( mLivePreview ) { // we don't really want to update every frame...
			if ( mLastValue != roundValue ) {
				onApplyValue( mWheelRadio.getValue() );
			}
			mLastValue = roundValue;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aviary.android.feather.widget.Wheel.OnScrollListener#onScrollFinished(com.aviary.android.feather.widget.Wheel, float,
	 * int)
	 */
	@Override
	public void onScrollFinished( Wheel view, float value, int roundValue ) {
		mWheelRadio.setValue( value );
		onApplyValue( mWheelRadio.getValue() );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aviary.android.feather.effects.AbstractEffectPanel#onGenerateResult()
	 */
	@Override
	protected void onGenerateResult() {
		mPreview = BitmapUtils.copy( mBitmap, Config.ARGB_8888 );

		AbstractColorMatrixFilter filter = (AbstractColorMatrixFilter) mFilter;

		filter.execute( mBitmap, mPreview, mCurrentRealValue );
		onComplete( mPreview, filter.getActions() );
	}
}
