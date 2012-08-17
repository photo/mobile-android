
package me.openphoto.android.app.ui.lib;

import java.io.File;
import java.io.FileOutputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.util.TypedValue;

/**
 * The Class ImageTools which provides different functions for image related
 * processing.
 */
public class ImageTools {
    private static final String TAG = ImageTools.class.getSimpleName();

    /**
     * Gets the pixels for dip.
     * 
     * @param context the context
     * @param dip the dip
     * @return the pixels for dip
     */
    public static int getPixelsForDip(Context context, int dip) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) dip, context
                .getResources().getDisplayMetrics());
    }

    /**
     * Save bitmap to file.
     * 
     * @param bitmap the bitmap
     * @param saveLocation the save location
     * @return true, if successful
     */
    public static boolean saveBitmap(Bitmap bitmap, String saveLocation) {
        try {
            createDirsOfFile(saveLocation);
            FileOutputStream out = new FileOutputStream(saveLocation);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
            return true;
        } catch (Exception e) {
            Log.w(TAG, "Error while saving Bitmap (" + saveLocation + ": " + e.getMessage());
            new File(saveLocation).delete();
        }
        return false;

    }

    /**
     * Creates the dirs for a file if they do not yet exist.
     * 
     * @param fileLocation the location for which the directories should be
     *            created
     */
    protected static void createDirsOfFile(String fileLocation) {
        File file = new File(fileLocation);
        file.getParentFile().mkdirs();
    }
}
