
package me.openphoto.android.app.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;

public class ImageUtils {
	static final String TAG = ImageUtils.class.getSimpleName();
    /**
     * decodes image and scales it to reduce memory consumption <br />
     * <br />
     * Source: http://stackoverflow
     * .com/questions/477572/android-strange-out-of-memory-issue/823966#823966
     * 
     * @param file File
     * @param requiredSize size that the image should have
     * @return image in required size
     */
    public static Bitmap decodeFile(File file, int requiredSize) {
        try {
            // Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(file), null, o);

            // The new size we want to scale to
            final int REQUIRED_SIZE = requiredSize;

            // Find the correct scale value. It should be the power of 2.
            int width_tmp = o.outWidth, height_tmp = o.outHeight;
            int scale = 1;
            while (true) {
                if (width_tmp / 2 < REQUIRED_SIZE || height_tmp / 2 < REQUIRED_SIZE)
                    break;
                width_tmp /= 2;
                height_tmp /= 2;
                scale *= 2;
            }

            // Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            return BitmapFactory.decodeStream(new FileInputStream(file), null, o2);
        } catch (FileNotFoundException e) {
			GuiUtils.noAlertError(TAG, null, e);
        }
        return null;
    }

    /**
     * @param context the context
     * @param imageUri Uri of the image for which the file path should be
     *            returned
     * @return file path of the given imageUri
     */
    public static String getRealPathFromURI(Context context, Uri imageUri) {
        if (imageUri.getScheme().equals("file")) {
            return imageUri.getPath();
        }
        String[] proj = {
                MediaStore.Images.Media.DATA
        };
        Cursor cursor = context.getContentResolver().query(imageUri, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        if (cursor.moveToFirst()) {
            String path = cursor.getString(column_index);
            cursor.close();
            return path;
        } else {
            return null;
        }
    }
}
