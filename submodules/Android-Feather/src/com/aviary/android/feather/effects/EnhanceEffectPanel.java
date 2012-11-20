package com.aviary.android.feather.effects;

import org.json.JSONException;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.aviary.android.feather.R;
import com.aviary.android.feather.library.filters.EnhanceFilter;
import com.aviary.android.feather.library.filters.EnhanceFilter.Types;
import com.aviary.android.feather.library.filters.FilterLoaderFactory;
import com.aviary.android.feather.library.filters.FilterLoaderFactory.Filters;
import com.aviary.android.feather.library.moa.Moa;
import com.aviary.android.feather.library.moa.MoaActionList;
import com.aviary.android.feather.library.services.EffectContext;
import com.aviary.android.feather.library.utils.BitmapUtils;
import com.aviary.android.feather.library.utils.SystemUtils;
import com.aviary.android.feather.widget.ImageButtonRadioGroup;
import com.aviary.android.feather.widget.ImageButtonRadioGroup.OnCheckedChangeListener;

// TODO: Auto-generated Javadoc
/**
 * The Class EnhanceEffectPanel.
 */
public class EnhanceEffectPanel extends AbstractOptionPanel implements OnCheckedChangeListener {

	/** current rendering task */
	private RenderTask mCurrentTask;
	private Filters mFilterType;
	volatile boolean mIsRendering = false;
	boolean enableFastPreview = false;
	MoaActionList mActions = null;

	/**
	 * Instantiates a new enhance effect panel.
	 * 
	 * @param context
	 *           the context
	 * @param type
	 *           the type
	 */
	public EnhanceEffectPanel( EffectContext context, Filters type ) {
		super( context );
		mFilterType = type;
	}

	@Override
	public void onCreate( Bitmap bitmap ) {
		super.onCreate( bitmap );

		// well, it's better to have the big progress here
		// enableFastPreview = Constants.getFastPreviewEnabled();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aviary.android.feather.effects.AbstractEffectPanel#onActivate()
	 */
	@Override
	public void onActivate() {
		super.onActivate();
		mPreview = BitmapUtils.copy( mBitmap, Config.ARGB_8888 );
		ImageButtonRadioGroup radio = (ImageButtonRadioGroup) getOptionView().findViewById( R.id.radio );
		radio.setOnCheckedChangeListener( this );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aviary.android.feather.effects.AbstractOptionPanel#generateOptionView(android.view.LayoutInflater,
	 * android.view.ViewGroup)
	 */
	@Override
	protected ViewGroup generateOptionView( LayoutInflater inflater, ViewGroup parent ) {
		return (ViewGroup) inflater.inflate( R.layout.feather_enhance_panel, parent, false );
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
	 * @see com.aviary.android.feather.effects.AbstractEffectPanel#onCancel()
	 */
	@Override
	public boolean onCancel() {
		killCurrentTask();
		return super.onCancel();
	}

	/**
	 * Kill current task.
	 */
	private void killCurrentTask() {
		if ( mCurrentTask != null ) {
			synchronized ( mCurrentTask ) {
				mCurrentTask.cancel( true );
				mCurrentTask.renderFilter.stop();
				onProgressEnd();
			}
			mIsRendering = false;
			mCurrentTask = null;
		}
	}

	@Override
	protected void onProgressEnd() {
		if ( !enableFastPreview ) {
			super.onProgressModalEnd();
		} else {
			super.onProgressEnd();
		}
	}

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
	 * @see com.aviary.android.feather.effects.AbstractEffectPanel#getIsChanged()
	 */
	@Override
	public boolean getIsChanged() {
		return super.getIsChanged() || mIsRendering == true;
	}

	/**
	 * The Class RenderTask.
	 */
	class RenderTask extends AsyncTask<Types, Void, Bitmap> {

		/** The m error. */
		String mError;

		/** The render filter. */
		volatile EnhanceFilter renderFilter;

		/**
		 * Instantiates a new render task.
		 */
		public RenderTask() {
			renderFilter = (EnhanceFilter) FilterLoaderFactory.get( mFilterType );
			mError = null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onPreExecute()
		 */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			onProgressStart();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#doInBackground(Params[])
		 */
		@Override
		protected Bitmap doInBackground( Types... params ) {
			if ( isCancelled() ) return null;

			Bitmap result = null;
			mIsRendering = true;
			Types type = params[0];
			renderFilter.setType( type );

			try {
				result = renderFilter.execute( mBitmap, mPreview, 1, 1 );
				mActions = renderFilter.getActions();
			} catch ( JSONException e ) {
				e.printStackTrace();
				mError = e.getMessage();
				return null;
			}

			if ( isCancelled() ) return null;
			return result;
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

			onProgressEnd();

			if ( isCancelled() ) return;

			if ( result != null ) {

				if ( SystemUtils.isHoneyComb() ) {
					Moa.notifyPixelsChanged( mPreview );
				}

				onPreviewChanged( mPreview, true );
			} else {
				if ( mError != null ) {
					onGenericError( mError );
				}
			}

			mIsRendering = false;
			mCurrentTask = null;
		}

		@Override
		protected void onCancelled() {
			renderFilter.stop();
			super.onCancelled();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.aviary.android.feather.widget.ImageButtonRadioGroup.OnCheckedChangeListener#onCheckedChanged(com.aviary.android.feather
	 * .widget.ImageButtonRadioGroup, int, boolean)
	 */
	@Override
	public void onCheckedChanged( ImageButtonRadioGroup group, int checkedId, boolean isChecked ) {
		mLogger.info( "onCheckedChange: " + checkedId );

		if ( !isActive() || !isEnabled() ) return;

		Types type = null;

		killCurrentTask();

		if ( checkedId == R.id.button1 ) {
			type = Types.AUTOENHANCE;
		} else if ( checkedId == R.id.button2 ) {
			type = Types.NIGHTENHANCE;
		} else if ( checkedId == R.id.button3 ) {
			type = Types.BACKLIGHT;
		} else if ( checkedId == R.id.button4 ) {
			type = Types.LABCORRECT;
		}

		if ( !isChecked ) {
			// restore the original image
			BitmapUtils.copy( mBitmap, mPreview );
			onPreviewChanged( mPreview, true );
			setIsChanged( false );
			mActions = null;
		} else {
			if ( type != null ) {
				mCurrentTask = new RenderTask();
				mCurrentTask.execute( type );
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

		if ( mIsRendering ) {
			GenerateResultTask task = new GenerateResultTask();
			task.execute();
		} else {
			onComplete( mPreview, mActions );
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
