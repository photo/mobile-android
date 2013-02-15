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

package com.trovebox.android.app.bitmapfun.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;

import com.trovebox.android.app.BuildConfig;
import com.trovebox.android.app.util.CommonUtils;
import com.trovebox.android.app.util.GuiUtils;
import com.trovebox.android.app.util.LoadingControl;
import com.trovebox.android.app.util.TrackerUtils;

/**
 * A simple subclass of {@link ImageWorker} that resizes images from resources
 * given a target width and height. Useful for when the input images might be
 * too large to simply load directly into memory.
 */
public class ImageResizer extends ImageWorker {
    private static final String TAG = "ImageWorker";
    protected int mImageWidth;
    protected int mImageHeight;
    protected int cornerRadius;

    /**
     * Initialize providing a single target image size (used for both width and
     * height);
     * 
     * @param context
     * @param loadingControl
     * @param imageWidth
     * @param imageHeight
     */
    public ImageResizer(Context context, LoadingControl loadingControl,
            int imageWidth, int imageHeight)
    {
        this(context, loadingControl, imageWidth, imageHeight, -1);
    }
    
    /**
     * Initialize providing a single target image size (used for both width and
     * height);
     * 
     * @param context
     * @param loadingControl
     * @param imageWidth
     * @param imageHeight
     * @param cornerRadius radius to round image corners. Ignored if <=0
     */
    public ImageResizer(Context context, LoadingControl loadingControl,
            int imageWidth, int imageHeight,
            int cornerRadius)
    {
        super(context, loadingControl);
        setImageSize(imageWidth, imageHeight);
        this.cornerRadius = cornerRadius;
    }

    /**
     * Initialize providing a single target image size (used for both width and
     * height);
     * 
     * @param context
     * @param loadingControl
     * @param imageSize
     */
    public ImageResizer(Context context, LoadingControl loadingControl,
            int imageSize)
    {
        super(context, loadingControl);
        setImageSize(imageSize);
    }

    /**
     * Set the target image width and height.
     * 
     * @param width
     * @param height
     */
    public void setImageSize(int width, int height) {
        mImageWidth = width;
        mImageHeight = height;
    }

    /**
     * Set the target image size (width and height will be the same).
     * 
     * @param size
     */
    public void setImageSize(int size) {
        setImageSize(size, size);
    }

    /**
     * The main processing method. This happens in a background task. In this
     * case we are just sampling down the bitmap and returning it from a
     * resource.
     * 
     * @param resId
     * @return
     */
    private Bitmap processBitmap(int resId) {
        return processBitmap(resId, mImageWidth, mImageHeight);
    }

    /**
     * The main processing method. This happens in a background task. In this
     * case we are just sampling down the bitmap and returning it from a
     * resource.
     * 
     * @param resId
     * @param imageWidth
     * @param imageHeight
     * @return
     */
    protected Bitmap processBitmap(int resId, int imageWidth, int imageHeight) {
        if (BuildConfig.DEBUG) {
            CommonUtils.debug(TAG, "processBitmap - " + resId);
        }
        return decodeSampledBitmapFromResource(
                mContext.getResources(), resId, imageWidth, imageHeight);
    }

    @Override
    protected Bitmap processBitmap(Object data) {
        return processBitmap(Integer.parseInt(String.valueOf(data)));
    }

    /**
     * Decode and sample down a bitmap from resources to the requested width and
     * height.
     * 
     * @param res The resources object containing the image data
     * @param resId The resource id of the image data
     * @param reqWidth The requested width of the resulting bitmap
     * @param reqHeight The requested height of the resulting bitmap
     * @return A bitmap sampled down from the original with the same aspect
     *         ratio and dimensions that are equal to or greater than the
     *         requested width and height
     */
    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
            int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    /**
     * Decode and sample down a bitmap from a file to the requested width and
     * height.
     * 
     * @param filename The full path of the file to decode
     * @param reqWidth The requested width of the resulting bitmap
     * @param reqHeight The requested height of the resulting bitmap
     * @return A bitmap sampled down from the original with the same aspect
     *         ratio and dimensions that are equal to or greater than the
     *         requested width and height
     */
    public static synchronized Bitmap decodeSampledBitmapFromFile(String filename,
            int reqWidth, int reqHeight) {
        return decodeSampledBitmapFromFile(filename, reqWidth, reqHeight, -1);
    }

    /**
     * Decode and sample down a bitmap from a file to the requested width and
     * height.
     * 
     * @param filename The full path of the file to decode
     * @param reqWidth The requested width of the resulting bitmap
     * @param reqHeight The requested height of the resulting bitmap
     * @param cornerRadius
     * @return A bitmap sampled down from the original with the same aspect
     *         ratio and dimensions that are equal to or greater than the
     *         requested width and height
     */
    public static synchronized Bitmap decodeSampledBitmapFromFile(String filename,
            int reqWidth, int reqHeight,
            int cornerRadius) {
        long start = System.currentTimeMillis();
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = calculateImageSize(filename);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        Bitmap result = decodeBitmap(filename, options);
        TrackerUtils.trackDataProcessingTiming(System.currentTimeMillis() - start,
                "decodeSampledBitmapFromFile", TAG);
        if (cornerRadius > 0)
        {
            result = getRoundedCornerBitmap(result, cornerRadius);
        }
        return result;
    }

    /**
     * Out of Memory hack taken from here
     * http://stackoverflow.com/a/7116158/527759
     * 
     * @param path
     * @param bfOptions
     * @return
     */
    public static Bitmap decodeBitmap(String path, BitmapFactory.Options bfOptions) {
        Bitmap bm = null;
        File file = new File(path);
        FileInputStream fs = null;
        try
        {
            fs = new FileInputStream(file);
        } catch (FileNotFoundException e)
        {
            GuiUtils.noAlertError(TAG, e);
        }

        try
        {
            if (fs != null)
                bm = BitmapFactory.decodeFileDescriptor(fs.getFD(), null,
                        bfOptions);
        } catch (IOException e)
        {
            GuiUtils.error(TAG, e);
        } finally
        {
            if (fs != null)
            {
                try
                {
                    fs.close();
                } catch (IOException e)
                {
                    GuiUtils.noAlertError(TAG, e);
                }
            }
        }
        return bm;
    }

    /**
     * Calculate the image size for the given file name.
     * 
     * @param filename
     * @return
     */
    public static BitmapFactory.Options calculateImageSize(String filename) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filename, options);
        return options;
    }

    /**
     * Calculate an inSampleSize for use in a {@link BitmapFactory.Options}
     * object when decoding bitmaps using the decode* methods from
     * {@link BitmapFactory}. This implementation calculates the closest
     * inSampleSize that will result in the final decoded bitmap having a width
     * and height equal to or larger than the requested width and height. This
     * implementation does not ensure a power of 2 is returned for inSampleSize
     * which can be faster when decoding but results in a larger bitmap which
     * isn't as useful for caching purposes.
     * 
     * @param options An options object with out* params already populated (run
     *            through a decode* method with inJustDecodeBounds==true
     * @param reqWidth The requested width of the resulting bitmap
     * @param reqHeight The requested height of the resulting bitmap
     * @return The value to be used for inSampleSize
     */
    public static int calculateInSampleSize(BitmapFactory.Options options,
            int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            if (width > height) {
                inSampleSize = Math.round((float) height / (float) reqHeight);
            } else {
                inSampleSize = Math.round((float) width / (float) reqWidth);
            }

            // This offers some additional logic in case the image has a strange
            // aspect ratio. For example, a panorama may have a much larger
            // width than height. In these cases the total pixels might still
            // end up being too large to fit comfortably in memory, so we should
            // be more aggressive with sample down the image (=larger
            // inSampleSize).

            final float totalPixels = width * height;

            // Anything more than 2x the requested pixels we'll sample down
            // further.
            final float totalReqPixelsCap = reqWidth * reqHeight * 2;

            while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
                inSampleSize++;
            }
        }
        return inSampleSize;
    }

    /**
     * Get the bitmap with rounded corners.
     * Taken from here http://stackoverflow.com/a/3292810/527759
     * 
     * @param bitmap - bitmap to add corners to
     * @param pixels - corner radius
     * @return
     */
    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int pixels) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap
                .getHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = pixels;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }
}
