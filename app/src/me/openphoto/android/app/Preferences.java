
package me.openphoto.android.app;

import me.openphoto.android.app.net.IOpenPhotoApi;
import me.openphoto.android.app.net.OpenPhotoApi;
import me.openphoto.android.app.util.CommonUtils;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.basic.DefaultOAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;

import org.holoeverywhere.preference.PreferenceManager;

import android.content.Context;
import android.content.SharedPreferences;

public class Preferences {
    public final static int PREFERENCES_MODE = Context.MODE_MULTI_PROCESS;
    public final static String PREFERENCES_NAME = "default";

    public static SharedPreferences getDefaultSharedPreferences(Context context)
    {
        return PreferenceManager.wrap(OpenPhotoApplication.getContext() == null ? context
                : OpenPhotoApplication.getContext(),
                PREFERENCES_NAME,
                PREFERENCES_MODE);
    }

    public static SharedPreferences getSharedPreferences(String name)
    {
        return PreferenceManager.wrap(OpenPhotoApplication.getContext(),
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

    public static boolean isLoggedIn(Context context) {
        return getDefaultSharedPreferences(context)
                .getBoolean(context.getString(R.string.setting_account_loggedin_key), false)
                || CommonUtils.TEST_CASE;
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

    public static IOpenPhotoApi getApi(Context context) {
        return OpenPhotoApi.createInstance(context);
    }
}
