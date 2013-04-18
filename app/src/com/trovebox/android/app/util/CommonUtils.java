
package com.trovebox.android.app.util;

import java.text.DecimalFormat;
import java.util.Locale;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import com.trovebox.android.app.BuildConfig;
import com.trovebox.android.app.Preferences;
import com.trovebox.android.app.R;
import com.trovebox.android.app.TroveboxApplication;

public class CommonUtils
{
    /**
     * This variable is used in the test project to skip some checks
     */
    public static boolean TEST_CASE = false;
    
    public static final String TAG = CommonUtils.class.getSimpleName();

    /**
     * Write message to the debug log
     * 
     * @param TAG
     * @param message
     * @param params
     */
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

    /**
     * Format string with params
     * 
     * @param message
     * @param params
     * @return
     */
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

    /**
     * Format the number
     * @param number
     * @return
     */
    public static String format(Number number)
    {
        return DecimalFormat.getInstance().format(number);
    }
    /**
     * Write message to the verbose log
     * 
     * @param TAG
     * @param message
     * @param params
     */
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

    /**
     * Write message to the info log
     * 
     * @param TAG
     * @param message
     */
    public static void info(String TAG, String message)
    {
        Log.i(TAG, message);
    }

    /**
     * Write message to the error log
     * 
     * @param TAG
     * @param message
     * @param tr
     */
    public static void error(String TAG, String message, Throwable tr)
    {
        Log.e(TAG, message, tr);
    }

    /**
     * Get serializable object from bundle if it is not null
     * 
     * @param key
     * @param bundle
     * @return
     */
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
     * Checks whether the running platform version is 4.1 or higher
     * 
     * @return
     */
    public static boolean isJellyBeanOrHigher()
    {
        return android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN;
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
        return checkLoggedInAndOnline(false);
    }

    /**
     * Checks whether user is logged in and internet is available
     * 
     * @param silent whether or not to do not show message in case check failure
     * @return
     */
    public static boolean checkLoggedInAndOnline(boolean silent)
    {
        return checkLoggedIn(silent) && checkOnline(silent);
    }

    /**
     * Checks whether network connection is available. Otherwise shows warning
     * message
     * 
     * @return
     */
    public static boolean checkOnline()
    {
        return checkOnline(false);
    }

    /**
     * Checks whether network connection is available. Otherwise shows warning
     * message
     * 
     * @param silent whether or not to do not show message in case check failure
     * @return
     */
    public static boolean checkOnline(boolean silent)
    {
        boolean result = Utils.isOnline(TroveboxApplication.getContext()) || TEST_CASE;
        if (!result && !silent)
        {
            GuiUtils.alert(R.string.noInternetAccess);
        }
        return result;
    }

    /**
     * Checks whether user is logged in
     * 
     * @return
     */
    public static boolean checkLoggedIn()
    {
        return checkLoggedIn(false);
    }

    /**
     * Checks whether user is logged in
     * 
     * @param silent
     * @return
     */
    public static boolean checkLoggedIn(boolean silent)
    {
        boolean result = Preferences.isLoggedIn();
        if (!result && !silent)
        {
            GuiUtils.alert(R.string.errorNotLoggedIn);
        }
        return result;
    }

    /**
     * Get string resource by id
     * 
     * @param resourceId
     * @return
     */
    public static String getStringResource(int resourceId)
    {
        return TroveboxApplication.getContext().getString(resourceId);
    }

    /**
     * Get string resource by id with parameters
     * 
     * @param resourceId
     * @param args
     * @return
     */
    public static String getStringResource(int resourceId, Object... args)
    {
        return TroveboxApplication.getContext().getString(resourceId, args);
    }
}
