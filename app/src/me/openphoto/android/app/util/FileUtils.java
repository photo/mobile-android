
package me.openphoto.android.app.util;

import java.io.File;
import java.io.IOException;

import android.os.Environment;

public class FileUtils {
    /**
     * @return External storage folder for this application.
     * @throws IOException if external storage can not be accessed or written to
     */
    public static File getStorageFolder() throws IOException {
        if (canWriteToStorage()) {
            File sdCard = Environment.getExternalStorageDirectory();
            File folder = new File(sdCard, "Android/data/me.openphoto.android.app/");
            folder.mkdirs();
            return folder;
        } else {
            throw new IOException("Can not write to external storage.");
        }
    }

    public static File getTempFolder() throws IOException {
        File folder = new File(getStorageFolder(), ".tmp/");
        folder.mkdirs();
        return folder;
    }

    private static boolean canWriteToStorage() {
        boolean mExternalStorageAvailable = false;
        boolean mExternalStorageWriteable = false;
        String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // We can read and write the media
            mExternalStorageAvailable = mExternalStorageWriteable = true;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            // We can only read the media
            mExternalStorageAvailable = true;
            mExternalStorageWriteable = false;
        } else {
            // Something else is wrong. It may be one of many other states, but
            // all we need
            // to know is we can neither read nor write
            mExternalStorageAvailable = mExternalStorageWriteable = false;
        }

        return mExternalStorageWriteable;
    }
}
