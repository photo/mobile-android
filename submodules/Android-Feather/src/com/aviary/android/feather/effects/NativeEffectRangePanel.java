package com.aviary.android.feather.effects;

import org.json.JSONException;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import com.aviary.android.feather.Constants;
import com.aviary.android.feather.R;
import com.aviary.android.feather.library.filters.FilterLoaderFactory;
import com.aviary.android.feather.library.filters.FilterLoaderFactory.Filters;
import com.aviary.android.feather.library.filters.INativeRangeFilter;
import com.aviary.android.feather.library.moa.Moa;
import com.aviary.android.feather.library.moa.MoaActionList;
import com.aviary.android.feather.library.moa.MoaResult;
import com.aviary.android.feather.library.services.EffectContext;
import com.aviary.android.feather.library.utils.BitmapUtils;
import com.aviary.android.feather.library.utils.SystemUtils;
import com.aviary.android.feather.widget.Wheel;

// TODO: Auto-generated Javadoc
/**
 * The Class NativeEffectRangePanel.
 */
public class NativeEffectRangePanel extends ColorMatrixEffectPanel {

	View mDrawingPanel;
	int mLastValue;
	ApplyFilterTask mCurrentTask;
	volatile boolean mIsRendering = false;
	boolean enableFastPreview;
	MoaActionList mActions = null;

	/**
	 * Instantiates a new native effect range panel.
	 * 
	 * @param context
	 *           the context
	 * @param type
	 *           the type
	 * @param resourcesBaseName
	 *           the resources base name
	 */
	public NativeEffectRangePanel( EffectContext context, Filters type, String resourcesBaseName ) {
		super( context, type, resourcesBaseName );
		mFilter = FilterLoaderFactory.get( type );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aviary.android.feather.effects.ColorMatrixEffectPanel#onCreate(android.graphics.Bitmap)
	 */
	@Override
	public void onCreate( Bitmap bitmap ) {
		super.onCreate( bitmap );

		mWheel.setWheelScaleFactor( 2 );
		mWheelRadio.setTicksNumber( mWheel.getTicks() * 4, mWheel.getWheelScaleFactor() );
		enableFastPreview = Constants.getFastPreviewEnabled();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aviary.android.feather.effects.ColorMatrixEffectPanel#onActivate()
	 */
	@Override
	public void onActivate() {
		super.onActivate();

		disableHapticIsNecessary( mWheel );

		int ticksCount = mWheel.getTicksCount();
		mWheelRadio.setTicksNumber( ticksCount, mWheel.getWheelScaleFactor() );
		mPreview = BitmapUtils.copy( mBitmap, Bitmap.Config.ARGB_8888 );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aviary.android.feather.effects.ColorMatrixEffectPanel#onScrollStarted(com.aviary.android.feather.widget.Wheel, float,
	 * int)
	 */
	@Override
	public void onScrollStarted( Wheel view, float value, int roundValue ) {
		mLogger.info( "onScrollStarted" );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aviary.android.feather.effects.ColorMatrixEffectPanel#onScroll(com.aviary.android.feather.widget.Wheel, float, int)
	 */
	@Override
	public void onScroll( Wheel view, float value, int roundValue ) {
		mWheelRadio.setValue( value );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aviary.android.feather.effects.ColorMatrixEffectPanel#onScrollFinished(com.aviary.android.feather.widget.Wheel,
	 * float, int)
	 */
	@Override
	public void onScrollFinished( Wheel view, float value, int roundValue ) {
		mWheelRadio.setValue( value );

		if ( mLastValue != roundValue ) {
			float realValue = ( mWheelRadio.getValue() );
			mLogger.info( "onScrollFinished: " + value + ", " + realValue );
			applyFilter( realValue * 100 );
		}
		mLastValue = roundValue;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aviary.android.feather.effects.AbstractEffectPanel#onProgressEnd()
	 */
	@Override
	protected void onProgressEnd() {
		if ( !enableFastPreview )
			onProgressModalEnd();
		else
			super.onProgressEnd();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aviary.android.feather.effects.AbstractEffectPanel#onProgressStart()
	 */
	@Override
	protected void onProgressStart() {
		if ( !enableFastPreview )
			onProgressModalStart();
		else
			super.onProgressStart();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aviary.android.feather.effects.AbstractEffectPanel#onDestroy()
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aviary.android.feather.effects.ColorMatrixEffectPanel#onDeactivate()
	 */
	@Override
	public void onDeactivate() {
		onProgressEnd();
		super.onDeactivate();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aviary.android.feather.effects.ColorMatrixEffectPanel#onGenerateResult()
	 */
	@Override
	protected void onGenerateResult() {
		mLogger.info( "onGenerateResult: " + mIsRendering );

		if ( mIsRendering ) {
			GenerateResultTask task = new GenerateResultTask();
			task.execute();
		} else {
			onComplete( mPreview, mActions );
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aviary.android.feather.effects.AbstractEffectPanel#onBackPressed()
	 */
	@Override
	public boolean onBackPressed() {
		mLogger.info( "onBackPressed" );
		killCurrentTask();
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

	/**
	 * Kill current task.
	 * 
	 * @return true, if successful
	 */
	boolean killCurrentTask() {
		if ( mCurrentTask != null ) {
			return mCurrentTask.cancel( true );
		}
		return false;
	}

	/**
	 * Apply a filter.
	 * 
	 * @param value
	 *           the value
	 */
	protected void applyFilter( float value ) {
		killCurrentTask();

		if ( value == 0 ) {
			BitmapUtils.copy( mBitmap, mPreview );
			onPreviewChanged( mPreview, true );
			setIsChanged( false );
			mActions = null;
		} else {
			mCurrentTask = new ApplyFilterTask( value );
			mCurrentTask.execute( mBitmap );
		}
	}

	/**
	 * Generate content view.
	 * 
	 * @param inflater
	 *           the inflater
	 * @return the view
	 */
	protected View generateContentView( LayoutInflater inflater ) {
		return inflater.inflate( R.layout.feather_native_range_effects_content, null );
	}

	/**
	 * The Class ApplyFilterTask.
	 */
	class ApplyFilterTask extends AsyncTask<Bitmap, Void, Bitmap> {

		/** The m result. */
		MoaResult mResult;

		/**
		 * Instantiates a new apply filter task.
		 * 
		 * @param value
		 *           the value
		 */
		public ApplyFilterTask( float value ) {
			( (INativeRangeFilter) mFilter ).setValue( value );
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onPreExecute()
		 */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mLogger.info( this, "onPreExecute" );

			try {
				mResult = ( (INativeRangeFilter) mFilter ).prepare( mBitmap, mPreview, 1, 1 );
			} catch ( JSONException e ) {
				e.printStackTrace();
			}
			onProgressStart();
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			mLogger.info( this, "onCancelled" );
			if ( mResult != null ) {
				mResult.cancel();
			}
			onProgressEnd();
			mIsRendering = false;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#doInBackground(Params[])
		 */
		@Override
		protected Bitmap doInBackground( Bitmap... arg0 ) {

			if ( isCancelled() ) return null;
			mIsRendering = true;

			long t1 = System.currentTimeMillis();
			try {
				mResult.execute();
				mActions = ( (INativeRangeFilter) mFilter ).getActions();
			} catch ( Exception exception ) {
				exception.printStackTrace();
				mLogger.error( exception.getMessage() );
				return null;
			}
			long t2 = System.currentTimeMillis();

			if ( null != mTrackingAttributes ) {
				mTrackingAttributes.put( "renderTime", Long.toString( t2 - t1 ) );
			}

			if ( isCancelled() ) return null;
			return mPreview;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute( Bitmap result ) {
			super.onPostExecute( result );

			if ( !isActive() ) return;

			mLogger.info( this, "onPostExecute" );
			onProgressEnd();

			if ( result != null ) {
				if ( SystemUtils.isHoneyComb() ) {
					Moa.notifyPixelsChanged( mPreview );
				}
				onPreviewChanged( mPreview, true );
			} else {
				BitmapUtils.copy( mBitmap, mPreview );
				onPreviewChanged( mPreview, true );
				setIsChanged( false );
			}
			mIsRendering = false;
			mCurrentTask = null;
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
}
