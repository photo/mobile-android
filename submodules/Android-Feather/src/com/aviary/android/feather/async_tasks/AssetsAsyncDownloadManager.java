package com.aviary.android.feather.async_tasks;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import com.aviary.android.feather.library.log.LoggerFactory;
import com.aviary.android.feather.library.log.LoggerFactory.Logger;
import com.aviary.android.feather.library.log.LoggerFactory.LoggerType;
import com.aviary.android.feather.library.plugins.PluginManager.InternalPlugin;
import com.aviary.android.feather.library.services.PluginService.StickerType;
import com.aviary.android.feather.library.utils.BitmapUtils;
import com.aviary.android.feather.library.utils.BitmapUtils.FLIP_MODE;
import com.aviary.android.feather.library.utils.BitmapUtils.ROTATION;
import com.aviary.android.feather.library.utils.ImageLoader;
import com.aviary.android.feather.utils.SimpleBitmapCache;

/**
 * Load an internal asset asynchronous.
 * 
 * @author alessandro
 */
public class AssetsAsyncDownloadManager {

	public static final int THUMBNAIL_LOADED = 1;

	@SuppressWarnings("unused")
	private Context mContext;

	private int mThumbSize = -1;

	private Handler mHandler;

	private volatile Boolean mStopped = false;

	private final int nThreads;

	/** thread pool */
	private final PoolWorker[] threads;

	/** The current runnable queue. */
	private final LinkedList<MyRunnable> mQueue;

	private SimpleBitmapCache mBitmapCache;

	private Logger logger = LoggerFactory.getLogger( "AssetAsyncDownloadManager", LoggerType.ConsoleLoggerType );

	/**
	 * Instantiates a new assets async download manager.
	 * 
	 * @param context
	 *           the context
	 * @param handler
	 *           the handler
	 */
	public AssetsAsyncDownloadManager( Context context, Handler handler ) {
		mContext = context;
		mHandler = handler;
		mBitmapCache = new SimpleBitmapCache();

		nThreads = 1;
		mQueue = new LinkedList<MyRunnable>();
		threads = new PoolWorker[nThreads];

		for ( int i = 0; i < nThreads; i++ ) {
			threads[i] = new PoolWorker();
			threads[i].start();
		}
	}

	/**
	 * Gets the thumb size.
	 * 
	 * @return the thumb size
	 */
	public int getThumbSize() {
		return mThumbSize;
	}

	/**
	 * set the default thumbnail size when resizing a bitmap.
	 * 
	 * @param size
	 *           the new thumb size
	 */
	public void setThumbSize( int size ) {
		mThumbSize = size;
	}

	/**
	 * Shut down now.
	 */
	public void shutDownNow() {
		logger.info( "shutDownNow" );

		mStopped = true;

		synchronized ( mQueue ) {
			mQueue.clear();
			mQueue.notify();
		}

		clearCache();
		mContext = null;

		for ( int i = 0; i < nThreads; i++ ) {
			threads[i] = null;
		}
	}

	/**
	 * The Class MyRunnable.
	 */
	private abstract class MyRunnable implements Runnable {

		/** The view. */
		public WeakReference<ImageView> view;

		/**
		 * Instantiates a new my runnable.
		 * 
		 * @param image
		 *           the image
		 */
		public MyRunnable( ImageView image ) {
			this.view = new WeakReference<ImageView>( image );
		}
	};

	/**
	 * Load asset.
	 * 
	 * @param resource
	 *           the resource
	 * @param srcFile
	 *           the src file
	 * @param background
	 *           the background
	 * @param view
	 *           the view
	 */
	public void loadStickerAsset( final InternalPlugin plugin, final String srcFile, final Drawable background, final ImageView view ) {

		if ( mStopped || mThumbSize < 1 ) return;

		mBitmapCache.resetPurgeTimer();

		runTask( new MyRunnable( view ) {

			@Override
			public void run() {
				if ( mStopped ) return;

				Message message = mHandler.obtainMessage();

				Bitmap bitmap = mBitmapCache.getBitmapFromCache( srcFile );
				if ( bitmap != null ) {
					message.what = THUMBNAIL_LOADED;
					message.obj = new Thumb( bitmap, view.get() );
				} else {
					bitmap = downloadBitmap( plugin, srcFile, background, view.get() );
					if ( bitmap != null ) mBitmapCache.addBitmapToCache( srcFile, bitmap );

					ImageView imageView = view.get();

					if ( imageView != null ) {
						MyRunnable bitmapTask = getBitmapTask( imageView );
						if ( this == bitmapTask ) {
							imageView.setTag( null );
							message.what = THUMBNAIL_LOADED;
							message.obj = new Thumb( bitmap, imageView );
						} else {
							logger.error( "image tag is different than current task!" );
						}
					}
				}

				if ( message.what == THUMBNAIL_LOADED ) mHandler.sendMessage( message );
			}
		} );
	}

	/**
	 * Load asset icon.
	 * 
	 * @param info
	 *           the info
	 * @param pm
	 *           the pm
	 * @param view
	 *           the view
	 */
	public void loadAssetIcon( final ApplicationInfo info, final PackageManager pm, final ImageView view ) {

		if ( mStopped || mThumbSize < 1 ) return;

		mBitmapCache.resetPurgeTimer();

		runTask( new MyRunnable( view ) {

			@Override
			public void run() {
				if ( mStopped ) return;

				Message message = mHandler.obtainMessage();

				Bitmap bitmap = mBitmapCache.getBitmapFromCache( info.packageName );
				if ( bitmap != null ) {
					message.what = THUMBNAIL_LOADED;
					message.obj = new Thumb( bitmap, view.get() );
				} else {
					bitmap = downloadIcon( info, pm, view.get() );
					if ( bitmap != null ) mBitmapCache.addBitmapToCache( info.packageName, bitmap );

					ImageView imageView = view.get();

					if ( imageView != null ) {
						MyRunnable bitmapTask = getBitmapTask( imageView );
						if ( this == bitmapTask ) {
							imageView.setTag( null );
							message.what = THUMBNAIL_LOADED;
							message.obj = new Thumb( bitmap, imageView );
						} else {
							logger.error( "image tag is different than current task!" );
						}
					}
				}

				if ( message.what == THUMBNAIL_LOADED ) mHandler.sendMessage( message );
			}
		} );
	}

	/**
	 * Run task.
	 * 
	 * @param task
	 *           the task
	 */
	private void runTask( MyRunnable task ) {
		synchronized ( mQueue ) {

			Iterator<MyRunnable> iterator = mQueue.iterator();
			while ( iterator.hasNext() ) {
				MyRunnable current = iterator.next();
				ImageView image = current.view.get();

				if ( image == null ) {
					iterator.remove();
					// mQueue.remove( current );
				} else {
					if ( image.equals( task.view.get() ) ) {
						current.view.get().setTag( null );
						iterator.remove();
						// mQueue.remove( current );
						break;
					}
				}
			}

			task.view.get().setTag( new CustomTag( task ) );

			mQueue.add( task );
			mQueue.notify();
		}
	}

	/**
	 * Download bitmap.
	 * 
	 * @param resource
	 *           the resource
	 * @param url
	 *           the url
	 * @param background
	 *           the background
	 * @param view
	 *           the view
	 * @return the bitmap
	 */
	Bitmap downloadBitmap( InternalPlugin plugin, String url, Drawable background, View view ) {

		if ( view == null ) return null;

		try {
			Bitmap bitmap;
			Bitmap result;

			bitmap = ImageLoader.loadStickerBitmap( plugin, url, StickerType.Small, mThumbSize, mThumbSize );

			if ( background != null ) {
				result = BitmapUtils.createThumbnail( bitmap, mThumbSize, mThumbSize, ROTATION.ROTATE_NULL, FLIP_MODE.None, null, background, 20, 10 );
			} else {
				result = bitmap;
			}

			if( result != bitmap ){
				bitmap.recycle();
			}
			return result;
		} catch ( Exception e ) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Download icon.
	 * 
	 * @param info
	 *           the info
	 * @param pm
	 *           the pm
	 * @param view
	 *           the view
	 * @return the bitmap
	 */
	Bitmap downloadIcon( ApplicationInfo info, PackageManager pm, View view ) {
		if ( view == null ) return null;

		Drawable d = info.loadIcon( pm );
		if ( d instanceof BitmapDrawable ) {
			Bitmap bitmap = ( (BitmapDrawable) d ).getBitmap();
			return bitmap;
		}

		return null;

	}

	/**
	 * The Class PoolWorker.
	 */
	private class PoolWorker extends Thread {

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Thread#run()
		 */
		@Override
		public void run() {
			Runnable r;

			while ( mStopped != true ) {
				synchronized ( mQueue ) {
					while ( mQueue.isEmpty() ) {
						if ( mStopped ) break;
						try {
							mQueue.wait();
						} catch ( InterruptedException ignored ) {}
					}

					try {
						r = (Runnable) mQueue.removeFirst();
					} catch ( NoSuchElementException e ) {
						// queue is empty
						break;
					}
				}

				try {
					r.run();
				} catch ( RuntimeException e ) {
					logger.error( e.getMessage() );
				}
			}
		}
	}

	/**
	 * The Class CustomTag.
	 */
	static class CustomTag {

		/** The task reference. */
		private final WeakReference<MyRunnable> taskReference;

		/**
		 * Instantiates a new custom tag.
		 * 
		 * @param task
		 *           the task
		 */
		public CustomTag( MyRunnable task ) {
			super();
			taskReference = new WeakReference<MyRunnable>( task );
		}

		/**
		 * Gets the downloader task.
		 * 
		 * @return the downloader task
		 */
		public MyRunnable getDownloaderTask() {
			return taskReference.get();
		}
	}

	/**
	 * Gets the bitmap task.
	 * 
	 * @param imageView
	 *           the image view
	 * @return the bitmap task
	 */
	private static MyRunnable getBitmapTask( ImageView imageView ) {
		if ( imageView != null ) {
			Object tag = imageView.getTag();
			if ( tag instanceof CustomTag ) {
				CustomTag runnableTag = (CustomTag) tag;
				return runnableTag.getDownloaderTask();
			}
		}
		return null;
	}

	/**
	 * Clears the image cache used internally to improve performance. Note that for memory efficiency reasons, the cache will
	 * automatically be cleared after a certain inactivity delay.
	 */
	public void clearCache() {
		mBitmapCache.clearCache();
	}

	/**
	 * The Class Thumb.
	 */
	public static class Thumb {

		/** The bitmap. */
		public Bitmap bitmap;

		/** The image. */
		public ImageView image;

		/**
		 * Instantiates a new thumb.
		 * 
		 * @param bmp
		 *           the bmp
		 * @param img
		 *           the img
		 */
		public Thumb( Bitmap bmp, ImageView img ) {
			image = img;
			bitmap = bmp;
		}
	}
}
