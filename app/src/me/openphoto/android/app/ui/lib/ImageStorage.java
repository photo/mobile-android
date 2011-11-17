
package me.openphoto.android.app.ui.lib;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

import me.openphoto.android.app.util.FileUtils;
import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;

/**
 * This class provides easy access to images. Giving the model object it will
 * return either the defined image in the app, or already downloaded image. If
 * the image is not yet downloaded, it will be downloaded and set to the
 * ImageView after download. It will consider, that the same ImageView can be
 * recycled to be used with another object and won't set the picture if that is
 * the case.
 */
public class ImageStorage {

    private static final String TAG = ImageStorage.class.getSimpleName();

    private final ImageDownloader mDownloader;
    private final ImageFromDiskReader mDiskReader;
    private final Context mContext;

    public ImageStorage(Context context) {
        mDiskReader = new ImageFromDiskReader();
        mDownloader = new ImageDownloader();
        mContext = context;
    }

    /**
     * Display image for the given path or downloadUrl. If image is already
     * available, show immediatly. Otherwise download and show it after
     * download.
     * 
     * @param imageView the ImageView in which the image should be displayed.
     * @param imageUrl the image url
     */
    public void displayImageFor(ImageView imageView, String imageUrl) {
        displayImageFor(imageView, imageUrl, null);
    }

    /**
     * Display image for the given path or downloadUrl. If image is already
     * available, show immediatly. Otherwise download and show it after
     * download.
     * 
     * @param imageView the ImageView in which the image should be displayed.
     * @param imageUrl the image url
     * @param listener callback when image is displayed
     */
    public void displayImageFor(ImageView imageView, String imageUrl,
            OnImageDisplayedCallback listener) {
        String path = getPath(imageUrl);

        mDownloader.stopTasksFor(imageView);
        mDiskReader.stop(imageView);

        if (fileExistsOnSdCard(path)) {
            mDiskReader.displayImage(path, imageView, listener);
        } else {
            if (!TextUtils.isEmpty(imageUrl)) {
                mDownloader.runInQueue(imageView, path, imageUrl, listener);
            }
        }
    }

    private String getPath(String url) {
        url = url.replace("://", "/");
        url = url.replace("/", "-");
        try {
            return FileUtils.getImageCacheFolder(mContext) + "/" + url;
        } catch (IOException e) {
            Log.e(TAG, "Can not get storage path", e);
            return null;
        }
    }

    /**
     * This will download the image or return the image already stored on
     * sdcard.
     * 
     * @param imageUrl the image url
     * @param uniqueName the unique name (without .png)
     * @throws IOException
     * @throws FileNotFoundException
     * @throws MalformedURLException
     */
    public Bitmap getBitmap(String imageUrl) throws MalformedURLException, FileNotFoundException,
            IOException {
        String path = getPath(imageUrl);

        if (fileExistsOnSdCard(path)) {
            return mDiskReader.getBitmap(path);
        } else {
            if (!TextUtils.isEmpty(imageUrl)) {
                return mDownloader.downloadImage(imageUrl, path);
            }
        }
        return null;
    }

    /**
     * Checks if the file exists.
     * 
     * @param path the path to the file
     * @return true, if the file exists
     */
    private boolean fileExistsOnSdCard(String path) {
        String filePath = path;
        return new File(filePath).exists();
    }

    public interface OnImageDisplayedCallback {
        void onImageDisplayed(ImageView view);
    }
}
