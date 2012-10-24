
package me.openphoto.android.app.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;

import me.openphoto.android.app.BuildConfig;
import me.openphoto.android.app.OpenPhotoApplication;
import me.openphoto.android.app.bitmapfun.util.DiskLruCache;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class ImageWorker {

	private static final int HTTP_CACHE_SIZE = 10 * 1024 * 1024; // 10MB
    public static final String HTTP_CACHE_DIR = "http";
	public static final String TAG = "HSD";
    
    private HashMap<String, Drawable> imageCache;
    private static Drawable DEFAULT_ICON = null;
    private BaseAdapter adapt;
	private final DiskLruCache diskCache;
	private LoadingControl loadingControl;

    static {
        // DEFAULT_ICON =
        // Resources.getSystem().getDrawable(R.drawable.newest_photo_noimage);
    }

	public ImageWorker(Context ctx, LoadingControl loadingControl)
    {
        imageCache = new HashMap<String, Drawable>();
		final File cacheDir = DiskLruCache.getDiskCacheDir(ctx,
				HTTP_CACHE_DIR);
		this.loadingControl = loadingControl;
		diskCache =
				DiskLruCache.openCache(ctx, cacheDir, HTTP_CACHE_SIZE);
    }

	public Drawable loadImage(BaseAdapter adapt, ImageView view)
    {
        this.adapt = adapt;
        String url = (String) view.getTag();
        if (imageCache.containsKey(url))
        {
            return imageCache.get(url);
        }
        else {
            synchronized (this) {
                imageCache.put(url, DEFAULT_ICON);
            }
			new ImageTask().execute(url);
            return DEFAULT_ICON;
        }
    }
    private class ImageTask extends AsyncTask<String, Void, Drawable>
    {
        private String s_url;

		public ImageTask()
		{
		}

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
			if (loadingControl != null)
				loadingControl.startLoading();
        }

        @Override
        protected Drawable doInBackground(String... params) {
			try
			{
				s_url = params[0];

				File cachedFile = getCachedFile(s_url);
				if (cachedFile != null)
				{
//					return Drawable
//							.createFromPath(cachedFile.getAbsolutePath());
					return getDrawable(cachedFile.getAbsolutePath());
				}

				InputStream istr;

				URL url = new URL(s_url);
				istr = url.openStream();

				Drawable result = Drawable.createFromStream(istr, "src");
				if (result instanceof BitmapDrawable)
				{
					diskCache.put(s_url, ((BitmapDrawable) result).getBitmap());
				}
				return result;
			} catch (Exception ex)
			{
				GuiUtils.error(TAG, null, ex);
			} catch (Throwable t)
			{
				GuiUtils.error(TAG, null, new RuntimeException(t));
			}
			return null;

        }

		/**
		 * Out of Memory hack taken from here
		 * http://stackoverflow.com/a/7116158/527759
		 * 
		 * @param path
		 * @return
		 */
		Drawable getDrawable(String path)
		{
			Bitmap bm = null;
			BitmapFactory.Options bfOptions = new BitmapFactory.Options();
			bfOptions.inDither = false; // Disable Dithering mode
			bfOptions.inPurgeable = true; // Tell to gc that whether it needs
											// free memory, the Bitmap can be
											// cleared
			bfOptions.inInputShareable = true; // Which kind of reference will
												// be used to recover the Bitmap
												// data after being clear, when
												// it will be used in the future
			bfOptions.inTempStorage = new byte[32 * 1024];

			File file = new File(path);
			FileInputStream fs = null;
			try
			{
				fs = new FileInputStream(file);
			} catch (FileNotFoundException e)
			{
				// TODO do something intelligent
				e.printStackTrace();
			}

			try
			{
				if (fs != null)
					bm = BitmapFactory.decodeFileDescriptor(fs.getFD(), null,
							bfOptions);
			} catch (IOException e)
			{
				GuiUtils.error(TAG, null, e);
			} finally
			{
				if (fs != null)
				{
					try
					{
						fs.close();
					} catch (IOException e)
					{
						e.printStackTrace();
					}
				}
			}
			return bm == null ? null : new BitmapDrawable(OpenPhotoApplication
					.getContext().getResources(), bm);
		}

        @Override
        protected void onPostExecute(Drawable result) {
            super.onPostExecute(result);
			if (loadingControl != null)
				loadingControl.stopLoading();
			if (result == null)
			{
				return;
			}

            synchronized (this) {
                imageCache.put(s_url, result);
            }
            adapt.notifyDataSetChanged();
        }

		protected File getCachedFile(String urlString)
		{
			final File cacheFile = new File(diskCache.createFilePath(urlString));

			if (diskCache.containsKey(urlString))
			{
				if (BuildConfig.DEBUG)
				{
					CommonUtils.debug(TAG, "downloadBitmap - found in http cache - "
							+ urlString);
				}
				return cacheFile;
			}
			return null;
		}
    }
}
