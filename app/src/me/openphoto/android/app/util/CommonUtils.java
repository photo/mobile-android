
package me.openphoto.android.app.util;

import me.openphoto.android.app.BuildConfig;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

public class CommonUtils
{
    public static void debug(String TAG, String message)
    {
        if (BuildConfig.DEBUG)
        {
            Log.d(TAG, message);
        }
    }

    /**
     * Write message to the error log
     * 
     * @param TAG
     * @param message
     */
    public static void error(String TAG, String message)
    {
        Log.e(TAG, message);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getSerializableFromBundleIfNotNull(String key, Bundle bundle)
    {
        return (T) (bundle == null ? null : bundle.getSerializable(key));
    }

    /**
     * Check the external storage status
     * 
     * @return
     */
    public static boolean isExternalStorageAvilable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /**
     * Checks whether the running platform version is 4.x or higher
     * 
     * @return
     */
    public static boolean isIceCreamSandwichOrHigher()
    {
        return android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH;
    }

    /**
     * Checks whether the running platform version is 2.2 or higher
     * 
     * @return
     */
    public static boolean isFroyoOrHigher()
    {
        return android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.FROYO;
    }
}
