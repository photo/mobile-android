
package me.openphoto.android.app.util;

import me.openphoto.android.app.BuildConfig;
import android.os.Bundle;
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

    @SuppressWarnings("unchecked")
    public static <T> T getSerializableFromBundleIfNotNull(String key, Bundle bundle)
    {
        return (T) (bundle == null ? null : bundle.getSerializable(key));
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
}
