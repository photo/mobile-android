package com.trovebox.android.app.bitmapfun.util;

import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.FragmentActivity;

import com.trovebox.android.app.R;
import com.trovebox.android.app.TroveboxApplication;
import com.trovebox.android.app.util.CommonUtils;
import com.trovebox.android.app.util.GuiUtils;
import com.trovebox.android.app.util.LoadingControl;
import com.trovebox.android.app.util.SimpleAsyncTaskEx;

/**
 * Utils for {@link ImageCache}
 * 
 * @author Eugene Popovich
 */
public class ImageCacheUtils {
    public static final String TAG = ImageCacheUtils.class.getSimpleName();
    public static String DISK_CACHE_CLEARED_BROADCAST_ACTION = "com.trovebox.DISK_CACHE_CLEARED";

    /**
     * Get and register broadcast receiver for disk cache cleared event. The
     * receiver calls clearDiskCachesForActivity method
     * 
     * @param TAG
     * @param activity
     * @return
     */
    public static BroadcastReceiver getAndRegisterOnDiskCacheClearedBroadcastReceiver(
            final String TAG,
            final FragmentActivity activity)
    {
        BroadcastReceiver br = new BroadcastReceiver()
        {

            @Override
            public void onReceive(Context context, Intent intent)
            {
                try
                {
                    CommonUtils.debug(TAG,
                            "Received disk cache cleared broadcast message");
                    clearDiskCachesForActivity(activity);
                } catch (Exception ex)
                {
                    GuiUtils.error(TAG, ex);
                }
            }
        };
        activity.registerReceiver(br, new IntentFilter(DISK_CACHE_CLEARED_BROADCAST_ACTION));
        return br;
    }

    /**
     * Send the disk cache cleared broadcast
     */
    public static void sendDiskCacheClearedBroadcast()
    {
        Intent intent = new Intent(DISK_CACHE_CLEARED_BROADCAST_ACTION);
        TroveboxApplication.getContext().sendBroadcast(intent);
    }
    
    /**
     * Clear the disk caches asynchronously and send broadcast event at the end
     * 
     * @param loadingControl
     */
    public static void clearDiskCachesAsync(LoadingControl loadingControl)
    {
        new ClearDiskCachesTask(loadingControl).execute();
    }

    /**
     * Clear disk caches for activity to avoid FileNotFoundExceptions after the
     * global cache is cleared (after clearDiskCachesAsync called)
     * 
     * @param activity
     */
    public static void clearDiskCachesForActivity(final FragmentActivity activity)
    {
        // Search for, or create an instance of the non-UI RetainFragment
        final RetainFragment mRetainFragment = RetainFragment.findOrCreateRetainFragment(
                activity.getSupportFragmentManager());
        List<Object> imageCaches = mRetainFragment.getObjects();
        for (Object obj : imageCaches)
        {
            if (obj != null && obj instanceof ImageCache)
            {
                ImageCache cache = (ImageCache) obj;
                cache.clearDiskCacheIfExists();
            }
        }
    }

    /**
     * Clear disk caches for different disk caches directories
     * 
     * @return
     */
    public static boolean clearDiskCaches()
    {
        try
        {
            DiskLruCache.clearCaches(TroveboxApplication.getContext(),
                    ImageCache.THUMBS_CACHE_DIR, ImageCache.LOCAL_THUMBS_CACHE_DIR,
                    ImageCache.LARGE_IMAGES_CACHE_DIR, ImageFetcher.HTTP_CACHE_DIR);
            return true;
        } catch (Exception ex)
        {
            GuiUtils.error(TAG, ex);
        }
        return false;
    }

    /**
     * The clear disk caches asynchronous task
     */
    private static class ClearDiskCachesTask extends
            SimpleAsyncTaskEx
    {
        public ClearDiskCachesTask(LoadingControl loadingControl) {
            super(loadingControl);
        }

        @Override
        protected Boolean doInBackground(Void... params)
        {
            return clearDiskCaches();
        }

        @Override
        protected void onSuccessPostExecute() {
            GuiUtils.info(R.string.disk_caches_cleared_message);
            sendDiskCacheClearedBroadcast();

        }
    }
}
