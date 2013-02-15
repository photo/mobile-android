
package com.trovebox.android.app;

import java.util.Date;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.basic.DefaultOAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;

import org.holoeverywhere.preference.PreferenceManager;

import android.content.Context;
import android.content.SharedPreferences;

import com.trovebox.android.app.net.ITroveboxApi;
import com.trovebox.android.app.net.TroveboxApi;
import com.trovebox.android.app.util.CommonUtils;

public class Preferences {
    public final static int PREFERENCES_MODE = Context.MODE_MULTI_PROCESS;
    public final static String PREFERENCES_NAME = "default";
    public final static String LIMITS_PREFERENCES_NAME = "limits";

    public static SharedPreferences getDefaultSharedPreferences(Context context)
    {
        return PreferenceManager.wrap(TroveboxApplication.getContext() == null ? context
                : TroveboxApplication.getContext(),
                PREFERENCES_NAME,
                PREFERENCES_MODE);
    }

    public static SharedPreferences getSharedPreferences(String name)
    {
        return PreferenceManager.wrap(TroveboxApplication.getContext(),
                name,
                PREFERENCES_MODE);
    }

    public static boolean isAutoUploadActive(Context context) {
        return getDefaultSharedPreferences(context)
                .getBoolean(
                        context.getString(R.string.setting_autoupload_on_key),
                        context.getResources().getBoolean(R.bool.setting_autoupload_on_default));
    }

    public static void setAutoUploadActive(Context context, boolean active)
    {
        getDefaultSharedPreferences(context).edit()
                .putBoolean(context.getString(R.string.setting_autoupload_on_key), active).commit();
    }

    public static boolean isWiFiOnlyUploadActive(Context context)
    {
        return getDefaultSharedPreferences(context)
                .getBoolean(
                        context.getString(R.string.setting_wifi_only_upload_on_key),
                        context.getResources().getBoolean(
                                R.bool.setting_wifi_only_upload_on_default));
    }

    public static void setWiFiOnlyUploadActive(Context context, boolean active)
    {
        getDefaultSharedPreferences(context).edit()
                .putBoolean(context.getString(R.string.setting_wifi_only_upload_on_key), active)
                .commit();
    }

    public static String getAutoUploadTag(Context context) {
        return getDefaultSharedPreferences(context)
                .getString(
                        context.getString(R.string.setting_autoupload_tag_key),
                        context.getResources().getString(R.string.setting_autoupload_tag_default));
    }

    public static String getServer(Context context) {
        return getDefaultSharedPreferences(context)
                .getString(
                        context.getString(R.string.setting_account_server_key),
                        context.getString(R.string.setting_account_server_default));
    }

    public static boolean setServer(Context context, String server) {
        if (!server.startsWith("http")) {
            server = "http://" + server;
        }

        if (server.endsWith("/")) {
            server = server.substring(0,
                    server.length() - 1);
        }

        return getDefaultSharedPreferences(context)
                .edit()
                .putString(context.getString(R.string.setting_account_server_key), server)
                .commit();
    }

    /**
     * Is user logged in to application
     * 
     * @return
     */
    public static boolean isLoggedIn() {
        return isLoggedIn(TroveboxApplication.getContext());
    }

    /**
     * Is user logged in to application
     * 
     * @return
     */
    public static boolean isLoggedIn(Context context) {
        return getDefaultSharedPreferences(context)
                .getBoolean(context.getString(R.string.setting_account_loggedin_key), false)
                || CommonUtils.TEST_CASE;
    }

    /**
     * Gets the shared preferences for uploading limit information
     * 
     * @return
     */
    public static SharedPreferences getLimitsSharedPreferences() {
        return getSharedPreferences(LIMITS_PREFERENCES_NAME);
    }

    /**
     * Check whether currently logged in user account type (pro or free)
     * 
     * @return
     */
    public static boolean isProUser() {
        return getLimitsSharedPreferences()
                .getBoolean(CommonUtils.getStringResource(R.string.setting_account_type), false);
    }

    /**
     * Set currently logged in user account type
     * 
     * @param value true if user is a pro, false if user uses free account type
     */
    public static void setProUser(boolean value)
    {
        getLimitsSharedPreferences()
                .edit()
                .putBoolean(CommonUtils.getStringResource(R.string.setting_account_type), value)
                .commit();
    }

    /**
     * Get the remaining uploading limit
     * 
     * @return
     */
    public static int getRemainingUploadingLimit() {
        return getLimitsSharedPreferences()
                .getInt(CommonUtils.getStringResource(R.string.setting_remaining_upload_limit),
                        Integer.MAX_VALUE);
    }

    /**
     * Set the remaining uploading limit
     * 
     * @param limit
     */
    public static void setRemainingUploadingLimit(int limit)
    {
        getLimitsSharedPreferences()
                .edit()
                .putInt(CommonUtils.getStringResource(R.string.setting_remaining_upload_limit),
                        limit)
                .commit();
    }

    /**
     * Adjust remaining uploading limit
     * 
     * @param delta value on which to adjust limit
     */
    public static void adjustRemainingUploadingLimit(int delta)
    {
        setRemainingUploadingLimit(getRemainingUploadingLimit() + delta);
    }

    /**
     * Get the uploading limit resets on date
     * 
     * @return
     */
    public static Date getUploadLimitResetsOnDate() {
        long value = getLimitsSharedPreferences()
                .getLong(CommonUtils.getStringResource(R.string.setting_upload_limit_reset_date), 0);
        if (value == 0)
        {
            return null;
        } else
        {
            return new Date(value);
        }
    }

    /**
     * Set the uploading limit resets on date
     * 
     * @param date
     */
    public static void setUploadLimitResetsOnDate(Date date)
    {
        getLimitsSharedPreferences()
                .edit()
                .putLong(CommonUtils.getStringResource(R.string.setting_upload_limit_reset_date),
                        date == null ? 0l : date.getTime())
                .commit();
    }

    public static void logout(Context context) {
        getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(context.getString(R.string.setting_account_loggedin_key), false)
                .commit();
        getDefaultSharedPreferences(context)
                .edit()
                .putString(context.getString(R.string.setting_account_server_key),
                        context.getString(R.string.setting_account_server_default)).commit();

        getSharedPreferences("oauth")
                .edit()
                .clear()
                .commit();
        getLimitsSharedPreferences()
                .edit()
                .clear()
                .commit();
        getDefaultSharedPreferences(context)
                .edit()
                .remove(context.getString(R.string.setting_account_server_key))
                .commit();
    }

    public static void setLoginInformation(Context context, OAuthConsumer consumer) {
        getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(context.getString(R.string.setting_account_loggedin_key), true)
                .commit();
        getSharedPreferences("oauth")
                .edit()
                .putString(context.getString(R.string.setting_oauth_consumer_key),
                        consumer.getConsumerKey())
                .putString(context.getString(R.string.setting_oauth_consumer_secret),
                        consumer.getConsumerSecret())
                .putString(context.getString(R.string.setting_oauth_token),
                        consumer.getToken())
                .putString(context.getString(R.string.setting_oauth_token_secret),
                        consumer.getTokenSecret())
                .commit();
    }

    public static OAuthProvider getOAuthProvider(Context context) {
        String serverUrl = getServer(context);
        OAuthProvider provider = new DefaultOAuthProvider(
                serverUrl + "/v1/oauth/token/request",
                serverUrl + "/v1/oauth/token/access",
                serverUrl + "/v1/oauth/authorize");
        provider.setOAuth10a(true);
        return provider;
    }

    public static OAuthConsumer getOAuthConsumer(Context context) {
        if (!isLoggedIn(context)) {
            return null;
        }

        SharedPreferences prefs = getSharedPreferences("oauth");
        OAuthConsumer consumer = new CommonsHttpOAuthConsumer(
                prefs.getString(context.getString(R.string.setting_oauth_consumer_key), null),
                prefs.getString(context.getString(R.string.setting_oauth_consumer_secret),
                        null));
        consumer.setTokenWithSecret(
                prefs.getString(context.getString(R.string.setting_oauth_token), null),
                prefs.getString(context.getString(R.string.setting_oauth_token_secret), null));
        return consumer;
    }

    public static ITroveboxApi getApi(Context context) {
        return TroveboxApi.createInstance(context);
    }
}
