
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
}
