package com.trovebox.android.app.model.utils;

import android.content.Context;

import com.trovebox.android.app.Preferences;
import com.trovebox.android.app.R;
import com.trovebox.android.common.model.Credentials;

public class CredentialsUtils {
    /**
     * Save credentials to preferences
     * 
     * @param context
     * @param credentials
     */
    public static void saveCredentials(Context context, Credentials credentials) {
        Preferences.setServer(context, credentials.getServer());

        Preferences.getDefaultSharedPreferences(context).edit()
                .putBoolean(context.getString(R.string.setting_account_loggedin_key), true)
                .commit();

        Preferences.setAccountAccessType(credentials.getType());

        Preferences
                .getSharedPreferences("oauth")
                .edit()
                .putString(context.getString(R.string.setting_oauth_consumer_key),
                        credentials.getoAuthConsumerKey())
                .putString(context.getString(R.string.setting_oauth_consumer_secret),
                        credentials.getoAuthConsumerSecret())
                .putString(context.getString(R.string.setting_oauth_token),
                        credentials.getoAuthToken())
                .putString(context.getString(R.string.setting_oauth_token_secret),
                        credentials.getoAuthTokenSecret()).commit();
    }
}
