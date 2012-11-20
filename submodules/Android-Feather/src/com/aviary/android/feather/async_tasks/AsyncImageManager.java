package com.aviary.android.feather.async_tasks;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.concurrent.Callable;
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
import com.aviary.android.feather.utils.SimpleBitmapCache;

/**
 * Load an internal asset asynchronous.
 * 
 * @author alessandro
 */
public class AsyncImageManager {

	public static interface OnImageLoadListener {

		public void onLoadComplete( ImageView view, Bitmap bitmap );
	}

	private static final int THUMBNAIL_LOADED = 1;

	private volatile Boolean mStopped = false;

	private final int nThreads;

	/** thread pool */
	private final PoolWorker[] threads;

	/** The current runnable queue. */
	private final LinkedList<MyRunnable> mQueue;

	private SimpleBitmapCache mBitmapCache;

	private OnImageLoadListener mListener;

	private static Handler mHandler;

	private Logger logger = LoggerFactory.getLogger( "AsyncImageManager", LoggerType.ConsoleLoggerType );

	/**
	 * Instantiates a new assets async download manager.
	 * 
	 * @param context
	 *           the context
	 * @param handler
	 *           the handler
	 */
	public AsyncImageManager() {
		mBitmapCache = new SimpleBitmapCache();

		nThreads = 1;
		mQueue = new LinkedList<MyRunnable>();
		threads = new PoolWorker[nThreads];
		mHandler = new MyHandler( this );

		for ( int i = 0; i < nThreads; i++ ) {
			threads[i] = new PoolWorker();
			threads[i].start();
		}
		mListener = null;
	}

	public void setOnLoadCompleteListener( OnImageLoadListener listener ) {
		mListener = listener;
	}

	private static class MyHandler extends Handler {

		WeakReference<AsyncImageManager> mParent;

		public MyHandler( AsyncImageManager parent ) {
			mParent = new WeakReference<AsyncImageManager>( parent );
		}

		@Override
		public void handleMessage( Message msg ) {

			switch ( msg.what ) {
				case AsyncImageManager.THUMBNAIL_LOADED:

					Thumb thumb = (Thumb) msg.obj;

					if ( thumb.image != null && thumb.bitmap != null ) {
						if ( thumb.image.get() != null && thumb.bitmap.get() != null ) {

							if ( mParent != null && mParent.get() != null ) {
								AsyncImageManager parent = mParent.get();

								if ( parent.mListener != null ) {
									parent.mListener.onLoadComplete( thumb.image.get(), thumb.bitmap.get() );
								} else {
									thumb.image.get().setImageBitmap( thumb.bitmap.get() );
								}
							}
						}
					}
					break;
			}
		}
	}

	/**
	 * Shut down now.
	 */
	public void shutDownNow() {
		logger.info( "shutDownNow" );

		mStopped = true;
		mHandler = null;

		synchronized ( mQueue ) {
			mQueue.clear();
			mQueue.notify();
		}

		clearCache();

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
	 * @param hash
	 *           the src file
	 * @param background
	 *           the background
	 * @param view
	 *           the view
	 */
	public void execute( final MyCallable executor, final String hash, final ImageView view ) {

		if ( mStopped ) return;

		mBitmapCache.resetPurgeTimer();

		runTask( new MyRunnable( view ) {

			@Override
			public void run() {
				if ( mStopped ) return;

				Message message = mHandler.obtainMessage();

				Bitmap bitmap = mBitmapCache.getBitmapFromCache( hash );
				if ( bitmap != null ) {
					message.what = THUMBNAIL_LOADED;
					message.obj = new Thumb( bitmap, view.get() );
				} else {
					try {
						bitmap = executor.call();
					} catch ( Exception e ) {
						e.printStackTrace();
					}
					if ( bitmap != null ) mBitmapCache.addBitmapToCache( hash, bitmap );

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
				} else {
					if ( image.equals( task.view.get() ) ) {
						current.view.get().setTag( null );
						iterator.remove();
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
	static class Thumb {

		public WeakReference<Bitmap> bitmap;
		public WeakReference<ImageView> image;

		public Thumb( Bitmap bmp, ImageView img ) {
			image = new WeakReference<ImageView>( img );
			bitmap = new WeakReference<Bitmap>( bmp );
		}
	}

	public static class MyCallable implements Callable<Bitmap> {

		@Override
		public Bitmap call() throws Exception {
			return null;
		}

	}
}
