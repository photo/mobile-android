package me.openphoto.android.app.bitmapfun.util;

import java.io.File;

import me.openphoto.android.app.BuildConfig;
import me.openphoto.android.app.util.CommonUtils;
import me.openphoto.android.app.util.LoadingControl;
import android.content.Context;
import android.graphics.Bitmap;

public class ImageFileSystemFetcher extends ImageResizer
{
	private static final String TAG = ImageFileSystemFetcher.class
			.getSimpleName();
	
	/**
	 * Initialize providing a target image width and height for the processing
	 * images.
	 * 
	 * @param context
	 * @param loadingControl
	 * @param imageWidth
	 * @param imageHeight
	 */
	public ImageFileSystemFetcher(Context context,
			LoadingControl loadingControl, int imageWidth,
			int imageHeight)
	{
		super(context, loadingControl, imageWidth, imageHeight);
	}

	/**
	 * Initialize providing a single target image size (used for both width and
	 * height);
	 * 
	 * @param context
	 * @param loadingControl
	 * @param imageSize
	 */
	public ImageFileSystemFetcher(Context context,
			LoadingControl loadingControl, int imageSize)
	{
		super(context, loadingControl, imageSize);
	}

	/**
	 * The main process method, which will be called by the ImageWorker in the
	 * AsyncTask background
	 * thread.
	 * 
	 * @param data
	 *            The data to load the bitmap, in this case, a regular http URL
	 * @return The downloaded and resized bitmap
	 */
	protected Bitmap processBitmap(String data)
	{
		if (BuildConfig.DEBUG)
		{
			CommonUtils.debug(TAG, "processBitmap - " + data);
		}
		if (data == null)
		{
			return null;
		}

		// Download a bitmap, write it to a file
		final File f = new File(data);

		if (f != null && f.exists())
		{
			// Return a sampled down version
			return decodeSampledBitmapFromFile(f.toString(), mImageWidth,
					mImageHeight);
		}

		return null;
	}

	@Override
	protected Bitmap processBitmap(Object data)
	{
		return processBitmap(String.valueOf(data));
	}
}
