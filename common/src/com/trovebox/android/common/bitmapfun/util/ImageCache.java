/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.trovebox.android.common.bitmapfun.util;

import java.io.File;
import java.util.Map;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.util.LruCache;

import com.trovebox.android.common.BuildConfig;
import com.trovebox.android.common.util.CommonUtils;
import com.trovebox.android.common.util.TrackerUtils;

/**
 * This class holds our bitmap caches (memory and disk).
 */
public class ImageCache {
    private static final String TAG = "ImageCache";

    public static final String THUMBS_CACHE_DIR = "thumbs";
    public static final String LOCAL_THUMBS_CACHE_DIR = "thumbs_local";
    public static final String LARGE_IMAGES_CACHE_DIR = "images";

    // Default memory cache size
    private static final int DEFAULT_MEM_CACHE_SIZE = 1024 * 1024 * 5; // 5MB

    // Default memory cache size
    public static final int DEFAULT_MEM_CACHE_SIZE_RATIO = 8; // 5MB

    // Default disk cache size
    private static final int DEFAULT_DISK_CACHE_SIZE = 1024 * 1024 * 10; // 10MB

    private static final int DEFAULT_DISK_CACHE_MAX_ITEM_SIZE = 64;

    // Compression settings when writing images to disk cache
    private static final CompressFormat DEFAULT_COMPRESS_FORMAT = CompressFormat.JPEG;
    private static final int DEFAULT_COMPRESS_QUALITY = 70;

    // Constants to easily toggle various caches
    private static final boolean DEFAULT_MEM_CACHE_ENABLED = true;
    private static final boolean DEFAULT_DISK_CACHE_ENABLED = false;
    private static final boolean DEFAULT_CLEAR_DISK_CACHE_ON_START = true;

    private DiskLruCache mDiskCache;
    private LruCache<String, Bitmap> mMemoryCache;
    private ImageCacheParams cacheParams;

    /**
     * Creating a new ImageCache object using the specified parameters.
     * 
     * @param context The context to use
     * @param cacheParams The cache parameters to use to initialize the cache
     */
    public ImageCache(Context context, ImageCacheParams cacheParams) {
        init(context, cacheParams);
    }

    /**
     * Creating a new ImageCache object using the default parameters.
     * 
     * @param context The context to use
     * @param uniqueName A unique name that will be appended to the cache
     *            directory
     */
    public ImageCache(Context context, String uniqueName) {
        init(context, new ImageCacheParams(uniqueName));
    }

    /**
     * Find and return an existing ImageCache stored in a {@link RetainFragment}
     * , if not found a new one is created with defaults and saved to a
     * {@link RetainFragment}.
     * 
     * @param activity The calling {@link FragmentActivity}
     * @param uniqueName A unique name to append to the cache directory
     * @return An existing retained ImageCache object or a new one if one did
     *         not exist.
     */
    public static ImageCache findOrCreateCache(final FragmentActivity activity,
            final String uniqueName) {
        return findOrCreateCache(activity, uniqueName, DEFAULT_DISK_CACHE_MAX_ITEM_SIZE,
                DEFAULT_DISK_CACHE_ENABLED, DEFAULT_CLEAR_DISK_CACHE_ON_START,
                DEFAULT_MEM_CACHE_SIZE_RATIO);
    }

    /**
     * Find and return an existing ImageCache stored in a {@link RetainFragment}
     * , if not found a new one is created with defaults and saved to a
     * {@link RetainFragment}.
     * 
     * @param activity The calling {@link FragmentActivity}
     * @param uniqueName A unique name to append to the cache directory
     * @param clearDiskCacheOnStart whether to clear disk cache on start
     * @return An existing retained ImageCache object or a new one if one did
     *         not exist.
     * @return
     */
    public static ImageCache findOrCreateCache(final FragmentActivity activity,
            final String uniqueName, boolean clearDiskCacheOnStart) {
        return findOrCreateCache(activity, uniqueName, clearDiskCacheOnStart,
                DEFAULT_MEM_CACHE_SIZE_RATIO);
    }

    /**
     * Find and return an existing ImageCache stored in a {@link RetainFragment}
     * , if not found a new one is created with defaults and saved to a
     * {@link RetainFragment}.
     * 
     * @param activity The calling {@link FragmentActivity}
     * @param uniqueName A unique name to append to the cache directory
     * @param clearDiskCacheOnStart whether to clear disk cache on start
     * @param memCacheSizeRatio what part of memory will be available for cache
     *            (memory class/memCacheSizeRatio)
     * @return An existing retained ImageCache object or a new one if one did
     *         not exist.
     * @return
     */
    public static ImageCache findOrCreateCache(final FragmentActivity activity,
            final String uniqueName, boolean clearDiskCacheOnStart, int memCacheSizeRatio) {
        return findOrCreateCache(activity, uniqueName, DEFAULT_DISK_CACHE_MAX_ITEM_SIZE,
                DEFAULT_DISK_CACHE_ENABLED, clearDiskCacheOnStart, memCacheSizeRatio);
    }

    /**
     * Find and return an existing ImageCache stored in a {@link RetainFragment}
     * , if not found a new one is created with defaults and saved to a
     * {@link RetainFragment}.
     * 
     * @param activity The calling {@link FragmentActivity}
     * @param uniqueName A unique name to append to the cache directory
     * @param diskCacheMaxItemSize max item size for the disk cache
     * @param diskCacheEnabled whether to enable first level disk cache
     * @param clearDiskCacheOnStart whether to clear disk cache on start
     * @return An existing retained ImageCache object or a new one if one did
     *         not exist.
     * @return
     */
    public static ImageCache findOrCreateCache(final FragmentActivity activity,
            final String uniqueName, final int diskCacheMaxItemSize, boolean diskCacheEnabled,
            boolean clearDiskCacheOnStart) {
        return findOrCreateCache(activity, uniqueName, diskCacheMaxItemSize, diskCacheEnabled,
                clearDiskCacheOnStart, DEFAULT_MEM_CACHE_SIZE_RATIO);
    }

    /**
     * Find and return an existing ImageCache stored in a {@link RetainFragment}
     * , if not found a new one is created with defaults and saved to a
     * {@link RetainFragment}.
     * 
     * @param activity The calling {@link FragmentActivity}
     * @param uniqueName A unique name to append to the cache directory
     * @param diskCacheMaxItemSize max item size for the disk cache
     * @param diskCacheEnabled whether to enable first level disk cache
     * @param clearDiskCacheOnStart whether to clear disk cache on start
     * @param memCacheSizeRatio what part of memory will be available for cache
     *            (memory class/memCacheSizeRatio)
     * @return An existing retained ImageCache object or a new one if one did
     *         not exist.
     * @return
     */
    public static ImageCache findOrCreateCache(final FragmentActivity activity,
            final String uniqueName, final int diskCacheMaxItemSize, boolean diskCacheEnabled,
            boolean clearDiskCacheOnStart, int memCacheSizeRatio) {
        ImageCacheParams params = new ImageCacheParams(uniqueName);
        // Get memory class of this device, exceeding this amount will throw an
        // OutOfMemory exception.
        final int memClass = ((ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE))
                .getMemoryClass();

        // Use 1/8th of the available memory for this memory cache.
        params.memCacheSize = 1024 * 1024 * memClass / memCacheSizeRatio;
        params.clearDiskCacheOnStart = clearDiskCacheOnStart;
        params.diskCacheMaxItemSize = diskCacheMaxItemSize;
        params.diskCacheEnabled = diskCacheEnabled;
        CommonUtils.debug(TAG, "Calculated memory cache size: " + params.memCacheSize);

        return findOrCreateCache(activity, params);
    }

    /**
     * Find and return an existing ImageCache stored in a {@link RetainFragment}
     * , if not found a new one is created using the supplied params and saved
     * to a {@link RetainFragment}.
     * 
     * @param activity The calling {@link FragmentActivity}
     * @param cacheParams The cache parameters to use if creating the ImageCache
     * @return An existing retained ImageCache object or a new one if one did
     *         not exist
     */
    public static ImageCache findOrCreateCache(final FragmentActivity activity,
            ImageCacheParams cacheParams) {

        // Search for, or create an instance of the non-UI RetainFragment
        final RetainFragment mRetainFragment = RetainFragment.findOrCreateRetainFragment(activity
                .getSupportFragmentManager());

        // See if we already have an ImageCache stored in RetainFragment
        ImageCache imageCache = (ImageCache) mRetainFragment.getObject(cacheParams.uniqueName);

        // No existing ImageCache, create one and store it in RetainFragment
        if (imageCache == null) {
            imageCache = new ImageCache(activity, cacheParams);
            mRetainFragment.setObject(cacheParams.uniqueName, imageCache);
        }

        return imageCache;
    }

    /**
     * Initialize the cache, providing all parameters.
     * 
     * @param context The context to use
     * @param cacheParams The cache parameters to initialize the cache
     */
    private void init(Context context, ImageCacheParams cacheParams) {
        final File diskCacheDir = DiskLruCache.getDiskCacheDir(context, cacheParams.uniqueName);
        this.cacheParams = cacheParams;
        // Set up disk cache
        if (cacheParams.diskCacheEnabled) {
            mDiskCache = DiskLruCache.openCache(context, diskCacheDir, cacheParams.diskCacheSize,
                    cacheParams.diskCacheMaxItemSize);
            // Issue #259 fix. Sometimes previous step returns null
            if (mDiskCache != null) {
                mDiskCache.setCompressParams(cacheParams.compressFormat,
                        cacheParams.compressQuality);
                if (cacheParams.clearDiskCacheOnStart) {
                    mDiskCache.clearCache();
                }
            } else {
                CommonUtils.debug(TAG, "Couldn't create disk cache");
                TrackerUtils.trackBackgroundEvent("unsuccessfullDiskCacheCreation",
                        diskCacheDir.getAbsolutePath());
            }
        }

        // Set up memory cache
        if (cacheParams.memoryCacheEnabled) {
            mMemoryCache = new LruCache<String, Bitmap>(cacheParams.memCacheSize) {
                /**
                 * Measure item size in bytes rather than units which is more
                 * practical for a bitmap cache
                 */
                @Override
                protected int sizeOf(String key, Bitmap bitmap) {
                    return Utils.getBitmapSize(bitmap);
                }
            };
        }
    }

    public void addBitmapToCache(String data, Bitmap bitmap) {
        if (data == null || bitmap == null) {
            return;
        }

        // Add to memory cache
        if (mMemoryCache != null && mMemoryCache.get(data) == null) {
            mMemoryCache.put(data, bitmap);
        }

        // Add to disk cache
        if (mDiskCache != null && !mDiskCache.containsKey(data)) {
            mDiskCache.put(data, bitmap);
        }
    }

    /**
     * Get from memory cache.
     * 
     * @param data Unique identifier for which item to get
     * @return The bitmap if found in cache, null otherwise
     */
    public Bitmap getBitmapFromMemCache(String data) {
        if (mMemoryCache != null) {
            final Bitmap memBitmap = mMemoryCache.get(data);
            if (memBitmap != null) {
                if (BuildConfig.DEBUG) {
                    CommonUtils.debug(TAG, "Memory cache hit");
                }
                return memBitmap;
            }
        }
        return null;
    }

    /**
     * Get from disk cache.
     * 
     * @param data Unique identifier for which item to get
     * @return The bitmap if found in cache, null otherwise
     */
    public Bitmap getBitmapFromDiskCache(String data) {
        if (mDiskCache != null) {
            return mDiskCache.get(data);
        }
        return null;
    }

    public void clearCaches(boolean memoryOnly) {
        if (!memoryOnly) {
            clearDiskCacheIfNeeded();
        }
        clearMemoryCache();
    }

    public void clearDiskCacheIfNeeded() {
        if (mDiskCache != null && cacheParams.clearDiskCacheOnStart) {
            mDiskCache.clearCache();
        }
    }

    public void clearDiskCacheIfExists() {
        if (mDiskCache != null) {
            mDiskCache.clearCache();
        }
    }

    public void clearMemoryCache() {
        if (mMemoryCache != null) {
            CommonUtils.debug(TAG, "Requested memory cache cleaning");
            mMemoryCache.evictAll();
        }
    }

    /**
     * Check whether bitmap is present in memory cache
     * 
     * @param bitmap
     * @return
     */
    public boolean hasInMemoryCache(Bitmap bitmap) {
        boolean result = false;
        if (mMemoryCache != null) {
            Map<String, Bitmap> snapshot = mMemoryCache.snapshot();
            for (Bitmap b : snapshot.values()) {
                if (b == bitmap) {
                    result = true;
                    break;
                }
            }
        }
        CommonUtils.debug(TAG, "hasInMemoryCache: %1$b", result);
        return result;
    }

    /**
     * A holder class that contains cache parameters.
     */
    public static class ImageCacheParams {
        public String uniqueName;
        public int memCacheSize = DEFAULT_MEM_CACHE_SIZE;
        public int diskCacheSize = DEFAULT_DISK_CACHE_SIZE;
        public int diskCacheMaxItemSize = DEFAULT_DISK_CACHE_MAX_ITEM_SIZE;
        public CompressFormat compressFormat = DEFAULT_COMPRESS_FORMAT;
        public int compressQuality = DEFAULT_COMPRESS_QUALITY;
        public boolean memoryCacheEnabled = DEFAULT_MEM_CACHE_ENABLED;
        public boolean diskCacheEnabled = DEFAULT_DISK_CACHE_ENABLED;
        public boolean clearDiskCacheOnStart = DEFAULT_CLEAR_DISK_CACHE_ON_START;

        public ImageCacheParams(String uniqueName) {
            this.uniqueName = uniqueName;
        }
    }
}
