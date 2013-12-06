
package com.trovebox.android.common.util;

import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.text.format.DateUtils;
import android.util.Log;

import com.actionbarsherlock.internal.ResourcesCompat;
import com.trovebox.android.common.BuildConfig;
import com.trovebox.android.common.CommonConfigurationUtils;
import com.trovebox.android.common.R;

public class CommonUtils {
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
    public static void debug(String TAG, String message, Object... params) {
        try {
            if (BuildConfig.DEBUG) {
                if (params == null || params.length == 0) {
                    Log.d(TAG, message);
                } else {
                    Log.d(TAG, format(message, params));
                }
            }
        } catch (Exception ex) {
            error(TAG, ex);
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
        try {
            return String.format(Locale.ENGLISH, message, params);
        } catch (Exception ex) {
            error(TAG, ex);
        }
        return null;
    }

    /**
     * Format the number
     * 
     * @param number
     * @return
     */
    public static String format(Number number) {
        return DecimalFormat.getInstance().format(number);
    }

    /**
     * Format date time accordingly to specified user locale
     * 
     * @param date
     * @return
     */
    public static String formatDateTime(Date date) {
        if (date == null) {
            return null;
        }
        return DateUtils.formatDateTime(CommonConfigurationUtils.getApplicationContext(), date.getTime(),
                DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_SHOW_DATE
                        | DateUtils.FORMAT_SHOW_TIME);
    }

    /**
     * Write message to the verbose log
     * 
     * @param TAG
     * @param message
     * @param params
     */
    public static void verbose(String TAG, String message, Object... params) {
        try {
            if (BuildConfig.DEBUG) {
                if (params == null || params.length == 0) {
                    Log.v(TAG, message);
                } else {
                    Log.v(TAG, format(message, params));
                }
            }
        } catch (Exception ex) {
            error(TAG, ex);
        }
    }

    /**
     * Write message to the error log
     * 
     * @param TAG
     * @param message
     */
    public static void error(String TAG, String message) {
        Log.e(TAG, message);
    }

    /**
     * Write message to the info log
     * 
     * @param TAG
     * @param message
     */
    public static void info(String TAG, String message) {
        Log.i(TAG, message);
    }

    /**
     * Write throwable to the error log and track it
     * 
     * @param TAG
     * @param tr
     */
    public static void error(String TAG, Throwable tr) {
        error(TAG, null, tr);
    }

    /**
     * Write message and throwable to the error log and track it
     * 
     * @param TAG
     * @param message
     * @param tr
     */
    public static void error(String TAG, String message, Throwable tr) {
        error(TAG, message, tr, true);
    }

    /**
     * Write message to the error log and track it depend on trackThrowable
     * parameter
     * 
     * @param TAG
     * @param message
     * @param tr
     * @param trackThrowable - whether to track the throwable via TrackerUtils
     */
    public static void error(String TAG, String message, Throwable tr, boolean trackThrowable) {
        if (trackThrowable) {
            TrackerUtils.trackThrowable(tr);
        }
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
    public static <T> T getSerializableFromBundleIfNotNull(String key, Bundle bundle) {
        return (T) (bundle == null ? null : bundle.getSerializable(key));
    }

    /**
     * Get parcelable object from bundle if it is not null
     * 
     * @param key
     * @param bundle
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T getParcelableFromBundleIfNotNull(String key, Bundle bundle) {
        return (T) (bundle == null ? null : bundle.getParcelable(key));
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
    public static boolean isIceCreamSandwichOrHigher() {
        return android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH;
    }

    /**
     * Checks whether the running platform version is 4.1 or higher
     * 
     * @return
     */
    public static boolean isJellyBeanOrHigher() {
        return android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN;
    }

    /**
     * Checks whether the running platform version is 2.2 or higher
     * 
     * @return
     */
    public static boolean isFroyoOrHigher() {
        return android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.FROYO;
    }

    /**
     * Get string resource by id
     * 
     * @param resourceId
     * @return
     */
    public static String getStringResource(int resourceId) {
        return CommonConfigurationUtils.getApplicationContext().getString(resourceId);
    }

    /**
     * Get string resource by id with parameters
     * 
     * @param resourceId
     * @param args
     * @return
     */
    public static String getStringResource(int resourceId, Object... args) {
        return CommonConfigurationUtils.getApplicationContext().getString(resourceId, args);
    }

    /**
     * Get string resource by id
     * 
     * @param resourceId
     * @return
     */
    public static String getQuantityStringResource(int resourceId, int quantity) {
        return CommonConfigurationUtils.getApplicationContext().getResources()
                .getQuantityString(resourceId, quantity);
    }

    /**
     * Get string resource by id with parameters
     * 
     * @param resourceId
     * @param args
     * @return
     */
    public static String getQuantityStringResource(int resourceId, int quantity, Object... args) {
        return CommonConfigurationUtils.getApplicationContext().getResources()
                .getQuantityString(resourceId, quantity, args);
    }

    /**
     * Check whether the device is connected to any network
     * 
     * @return true if device is connected to any network, otherwise return
     *         false
     */
    public static boolean isOnline() {
        return isOnline(CommonConfigurationUtils.getApplicationContext());
    }

    /**
     * Check whether the device is connected to any network
     * 
     * @param context
     * @return true if device is connected to any network, otherwise return
     *         false
     */
    public static boolean isOnline(Context context) {
        boolean result = false;
        try {
            ConnectivityManager cm = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            if (netInfo != null && netInfo.isConnectedOrConnecting()) {
                result = true;
            }
        } catch (Exception ex) {
            error(TAG, "Error", ex);
        }
        return result;
    }

    /**
     * Check whether the device is connected to WiFi network and it is active
     * connection
     * 
     * @return true if device is connected to WiFi network and it is active,
     *         otherwise return false
     */
    public static boolean isWiFiActive() {
        boolean result = false;
        try {
            ConnectivityManager cm = (ConnectivityManager) CommonConfigurationUtils.getApplicationContext()
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            if (netInfo != null && netInfo.getType() == ConnectivityManager.TYPE_WIFI
                    && netInfo.isConnectedOrConnecting()) {
                result = true;
            }
        } catch (Exception ex) {
            error(TAG, "Error", ex);
        }
        return result;
    }

    public static boolean isTablet(Context context) {
        return context.getResources().getBoolean(R.bool.isTablet);
    }

    /**
     * Whether actionbar tabs embedded or in split state
     * 
     * @param context
     * @return
     */
    public static boolean isActionBarTabsEmbeded(Context context) {
        return ResourcesCompat.getResources_getBoolean(context, R.bool.abs__action_bar_embed_tabs);
    }

    /**
     * Returns possible external sd card path. Solution taken from here
     * http://stackoverflow.com/a/13648873/527759
     * 
     * @return
     */
    public static Set<String> getExternalMounts() {
        final Set<String> out = new HashSet<String>();
        try {
            String reg = ".*vold.*(vfat|ntfs|exfat|fat32|ext3|ext4).*rw.*";
            StringBuilder sb = new StringBuilder();
            try {
                final Process process = new ProcessBuilder().command("mount")
                        .redirectErrorStream(true).start();
                process.waitFor();
                final InputStream is = process.getInputStream();
                final byte[] buffer = new byte[1024];
                while (is.read(buffer) != -1) {
                    sb.append(new String(buffer));
                }
                is.close();
            } catch (final Exception e) {
                e.printStackTrace();
            }
            // parse output
            final String[] lines = sb.toString().split("\n");
            for (String line : lines) {
                if (!line.toLowerCase(Locale.ENGLISH).contains("asec")) {
                    if (line.matches(reg)) {
                        String[] parts = line.split(" ");
                        for (String part : parts) {
                            if (part.startsWith("/"))
                                if (!part.toLowerCase(Locale.ENGLISH).contains("vold")) {
                                    CommonUtils.debug(TAG, "Found path: " + part);
                                    out.add(part);
                                }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            error(TAG, ex);
        }
        return out;
    }
}
