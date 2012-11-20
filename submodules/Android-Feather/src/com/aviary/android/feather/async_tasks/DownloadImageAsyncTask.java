package com.aviary.android.feather.async_tasks;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import com.aviary.android.feather.Constants;
import com.aviary.android.feather.library.log.LoggerFactory;
import com.aviary.android.feather.library.log.LoggerFactory.Logger;
import com.aviary.android.feather.library.log.LoggerFactory.LoggerType;
import com.aviary.android.feather.library.utils.DecodeUtils;
import com.aviary.android.feather.library.utils.ImageLoader;
import com.aviary.android.feather.library.utils.ImageLoader.ImageSizes;

// TODO: Auto-generated Javadoc
/**
 * Load an Image bitmap asynchronous.
 * 
 * @author alessandro
 */
public class DownloadImageAsyncTask extends AsyncTask<Context, Void, Bitmap> {

	/**
	 * The listener interface for receiving onImageDownload events. The class that is interested in processing a onImageDownload
	 * event implements this interface, and the object created with that class is registered with a component using the component's
	 * <code>addOnImageDownloadListener<code> method. When
	 * the onImageDownload event occurs, that object's appropriate
	 * method is invoked.
	 * 
	 * @see OnImageDownloadEvent
	 */
	public static interface OnImageDownloadListener {

		/**
		 * On download start.
		 */
		void onDownloadStart();

		/**
		 * On download complete.
		 * 
		 * @param result
		 *           the result
		 */
		void onDownloadComplete( Bitmap result, ImageSizes sizes );

		/**
		 * On download error.
		 * 
		 * @param error
		 *           the error
		 */
		void onDownloadError( String error );
	};

	private OnImageDownloadListener mListener;
	private Uri mUri;
	private String error;
	private ImageLoader.ImageSizes mImageSize;

	/**
	 * Instantiates a new download image async task.
	 * 
	 * @param uri
	 *           the uri
	 */
	public DownloadImageAsyncTask( Uri uri ) {
		super();
		mUri = uri;
	}

	/**
	 * Sets the on load listener.
	 * 
	 * @param listener
	 *           the new on load listener
	 */
	public void setOnLoadListener( OnImageDownloadListener listener ) {
		mListener = listener;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.AsyncTask#onPreExecute()
	 */
	@Override
	protected void onPreExecute() {
		super.onPreExecute();

		if ( mListener != null ) mListener.onDownloadStart();
		mImageSize = new ImageLoader.ImageSizes();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.AsyncTask#doInBackground(Params[])
	 */
	@Override
	protected Bitmap doInBackground( Context... params ) {
		Context context = params[0];

		try {
			final int max_size = Constants.getManagedMaxImageSize();
			return DecodeUtils.decode( context, mUri, max_size, max_size, mImageSize );
		} catch ( Exception e ) {
			Logger logger = LoggerFactory.getLogger( "DownloadImageTask", LoggerType.ConsoleLoggerType );
			logger.error( "error", e.getMessage() );
			error = e.getMessage();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
	 */
	@Override
	protected void onPostExecute( Bitmap result ) {
		super.onPostExecute( result );

		if ( mListener != null ) {
			if ( result != null ) {
				mListener.onDownloadComplete( result, mImageSize );
			} else {
				mListener.onDownloadError( error );
			}
		}

		if ( mImageSize.getOriginalSize() == null ) {
			mImageSize.setOriginalSize( mImageSize.getNewSize() );
		}

		mListener = null;
		mUri = null;
		error = null;
	}
}
