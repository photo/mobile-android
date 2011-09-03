package me.openphoto.android.app.ui.lib;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

import android.graphics.Bitmap;
import android.os.Environment;
import android.text.TextUtils;
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

    /** The Constant FOLDER_DOMO. */
    public static final String APP_FOLDER = Environment.getExternalStorageDirectory()
            + "/data/ch.pboos.flickr/";

    /** The Constant FOLDER_TEMP. */
    public static final String FOLDER_TEMP = APP_FOLDER + ".tmp/";
    private static final String FOLDER_IMAGES = APP_FOLDER + ".img/";

    private ImageDownloader mDownloader;
    private ImageFromDiskReader mDiskReader;

    public ImageStorage() {
        mDiskReader = new ImageFromDiskReader();
        mDownloader = new ImageDownloader();
    }

    /**
     * Display image for the given path or downloadUrl. If image is already
     * available, show immediatly. Otherwise download and show it after
     * download.
     * 
     * @param imageView the ImageView in which the image should be displayed.
     * @param imageUrl the image url
     * @param uniqueName the unique name (without .png)
     */
    public void displayImageFor(ImageView imageView, String imageUrl, String uniqueName) {
        String path = getPath(uniqueName);

        mDownloader.stopTasksFor(imageView);
        mDiskReader.stop(imageView);

        if (fileExistsOnSdCard(path)) {
            mDiskReader.displayImage(path, imageView);
        } else {
            if (!TextUtils.isEmpty(imageUrl)) {
                mDownloader.runInQueue(imageView, path, imageUrl);
            }
        }
    }

    private String getPath(String uniqueName) {
        String path = FOLDER_IMAGES + uniqueName + ".png";
        return path;
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
    public Bitmap getBitmap(String imageUrl, String uniqueName) throws MalformedURLException,
            FileNotFoundException, IOException {
        String path = getPath(uniqueName);

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

}
