
package com.trovebox.android.app.util;

import java.io.File;
import java.io.IOException;

import android.content.Context;
import android.os.Environment;

public class FileUtils {
    public static String STORAGE_FOLDER = null;

    /**
     * @return External storage folder for this application.
     * @throws IOException if external storage can not be accessed or written to
     */
    public static File getStorageFolder(Context context) throws IOException {
        if (canWriteToStorage()) {
            if (STORAGE_FOLDER == null) {
                STORAGE_FOLDER = Environment.getExternalStorageDirectory()
                        + "/Android/data/"
                        + context.getApplicationInfo().packageName + "/";
                File folder = new File(STORAGE_FOLDER);
                folder.mkdirs();
            }

            return new File(STORAGE_FOLDER);
        } else {
            throw new IOException("Can not write to external storage.");
        }
    }

    public static File getTempFolder(Context context) throws IOException {
        File folder = new File(getStorageFolder(context), ".tmp/");
        folder.mkdirs();
        return folder;
    }

    public static File getImageCacheFolder(Context context) throws IOException {
        File folder = new File(getStorageFolder(context), ".img/");
        folder.mkdirs();
        return folder;
    }

    private static boolean canWriteToStorage() {
        boolean mExternalStorageWriteable = false;
        String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // We can read and write the media
            mExternalStorageWriteable = true;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            // We can only read the media
            mExternalStorageWriteable = false;
        } else {
            // Something else is wrong. It may be one of many other states, but
            // all we need
            // to know is we can neither read nor write
            mExternalStorageWriteable = false;
        }

        return mExternalStorageWriteable;
    }
}
