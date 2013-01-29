
package com.trovebox.android.app.util;

import java.util.Locale;

import com.trovebox.android.app.BuildConfig;
import com.trovebox.android.app.TroveboxApplication;
import com.trovebox.android.app.Preferences;
import com.trovebox.android.app.R;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

public class CommonUtils
{
    /**
     * This variable is used in the test project to skip some checks
     */
    public static boolean TEST_CASE = false;
    
    public static final String TAG = CommonUtils.class.getSimpleName();
    public static void debug(String TAG, String message, Object... params)
    {
        try
        {
            if (BuildConfig.DEBUG)
            {
                if (params == null || params.length == 0)
                {
                    Log.d(TAG, message);
                } else
                {
                    Log.d(TAG, format(message, params));
                }
            }
        } catch (Exception ex)
        {
            GuiUtils.noAlertError(TAG, ex);
        }
    }

    public static String format(String message, Object... params) {
        try
        {
            return String.format(Locale.ENGLISH, message, params);
        } catch (Exception ex)
        {
            GuiUtils.noAlertError(TAG, ex);
        }
        return null;
    }

    public static void verbose(String TAG, String message, Object... params)
    {
        try
        {
            if (BuildConfig.DEBUG)
            {
                if (params == null || params.length == 0)
                {
                    Log.v(TAG, message);
                } else
                {
                    Log.v(TAG, format(message, params));
                }
            }
        } catch (Exception ex)
        {
            GuiUtils.noAlertError(TAG, ex);
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

    /**
     * Checks whether user is logged in and internet is available
     * 
     * @return
     */
    public static boolean checkLoggedInAndOnline()
    {
        return Preferences.isLoggedIn(TroveboxApplication.getContext()) && checkOnline();
    }

    /**
     * Checks whether network connection is available. Otherwise shows warning
     * message
     * 
     * @return
     */
    public static boolean checkOnline()
    {
        boolean result = Utils.isOnline(TroveboxApplication.getContext()) || TEST_CASE;
        if (!result)
        {
            GuiUtils.alert(R.string.noInternetAccess);
        }
        return result;
    }
}
