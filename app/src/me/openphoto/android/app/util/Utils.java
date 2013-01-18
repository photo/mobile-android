/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.openphoto.android.app.util;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import me.openphoto.android.app.R;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.actionbarsherlock.internal.ResourcesCompat;

/**
 * Class containing some static utility methods.
 * 
 * @version 05.10.2012 <br>
 *          - added new methods isOnline and isWiFiActive
 */
public class Utils {
    private static final String TAG = Utils.class.getSimpleName();

    /**
     * Check whether the device is connected to any network
     * 
     * @param context
     * @return true if device is connected to any network, otherwise return
     *         false
     */
    public static boolean isOnline(
            Context context)
    {
        boolean result = false;
        try
        {
            ConnectivityManager cm =
                    (ConnectivityManager) context
                            .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            if (netInfo != null && netInfo.isConnectedOrConnecting())
            {
                result = true;
            }
        } catch (Exception ex)
        {
            GuiUtils.noAlertError(TAG, "Error", ex);
        }
        return result;
    }

    /**
     * Check whether the device is connected to WiFi network and it is active
     * connection
     * 
     * @param context
     * @return true if device is connected to WiFi network and it is active,
     *         otherwise return false
     */
    public static boolean isWiFiActive(Context context)
    {
        boolean result = false;
        try
        {
            ConnectivityManager cm =
                    (ConnectivityManager) context
                            .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            if (netInfo != null
                    && netInfo.getType() == ConnectivityManager.TYPE_WIFI
                    && netInfo.isConnectedOrConnecting())
            {
                result = true;
            }
        } catch (Exception ex)
        {
            GuiUtils.noAlertError(TAG, "Error", ex);
        }
        return result;
    }

    public static boolean isTablet(Context context)
    {
        return context.getResources().getBoolean(R.bool.isTablet);
    }

    /**
     * Whether actionbar tabs embedded or in split state
     * 
     * @param context
     * @return
     */
    public static boolean isActionBarTabsEmbeded(Context context)
    {
        return ResourcesCompat.getResources_getBoolean(context,
                R.bool.abs__action_bar_embed_tabs);
    }

    /**
     * Returns possible external sd card path. Solution taken from here
     * http://stackoverflow.com/a/13648873/527759
     * 
     * @return
     */
    public static Set<String> getExternalMounts() {
        final Set<String> out = new HashSet<String>();
        try
        {
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
                                if (!part.toLowerCase(Locale.ENGLISH).contains("vold"))
                                {
                                    CommonUtils.debug(TAG, "Found path: " + part);
                                    out.add(part);
                                }
                        }
                    }
                }
            }
        } catch (Exception ex)
        {
            GuiUtils.noAlertError(TAG, ex);
        }
        return out;
    }
}
