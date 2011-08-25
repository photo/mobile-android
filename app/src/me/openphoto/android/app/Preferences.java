
package me.openphoto.android.app;

import android.content.Context;
import android.preference.PreferenceManager;

public class Preferences {
    public static boolean isAutoUploadActive(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
                context.getString(R.string.setting_autoupload_on_key),
                context.getResources().getBoolean(R.bool.setting_autoupload_on_default));
    }

    public static String getAutoUploadTag(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(
                context.getString(R.string.setting_autoupload_tag_key),
                context.getResources().getString(R.string.setting_autoupload_tag_default));
    }

    public static String getServer(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(
                context.getString(R.string.setting_account_server_key),
                context.getString(R.string.setting_account_server_default));
    }
}
