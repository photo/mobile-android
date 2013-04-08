
package com.trovebox.android.app;

import java.util.Date;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.basic.DefaultOAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;

import org.holoeverywhere.preference.PreferenceManagerHelper;

import android.content.Context;
import android.content.SharedPreferences;

import com.trovebox.android.app.net.ITroveboxApi;
import com.trovebox.android.app.net.TroveboxApi;
import com.trovebox.android.app.purchase.util.Purchase;
import com.trovebox.android.app.util.CommonUtils;

public class Preferences {
    public final static int PREFERENCES_MODE = Context.MODE_MULTI_PROCESS;
    public final static String PREFERENCES_NAME = "default";
    public final static String LIMITS_PREFERENCES_NAME = "limits";
    public final static String SYSTEM_VERSION_PREFERENCES_NAME = "system_version";
    public final static String VERIFIED_PAYMENTS_PREFERENCES_NAME = "verified_payments";

    public static SharedPreferences getDefaultSharedPreferences(Context context)
    {
        return PreferenceManagerHelper.wrap(TroveboxApplication.getContext() == null ? context
                : TroveboxApplication.getContext(),
                PREFERENCES_NAME,
                PREFERENCES_MODE);
    }

    public static SharedPreferences getSharedPreferences(String name)
    {
        return PreferenceManagerHelper.wrap(TroveboxApplication.getContext(),
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
     * Gets the shared preferences for system version information
     * 
     * @return
     */
    public static SharedPreferences getSystemVersionPreferences() {
        return getSharedPreferences(SYSTEM_VERSION_PREFERENCES_NAME);
    }

    /**
     * Gets the shared preferences for verified payments cache information
     * 
     * @return
     */
    public static SharedPreferences getVerifiedPaymentsPreferences() {
        return getSharedPreferences(VERIFIED_PAYMENTS_PREFERENCES_NAME);
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

    /**
     * Check whether currently used server is self-hosted
     * @return true if server is self-hosted, otherwise return false
     */
    public static boolean isSelfHosted()
    {
        return !isHosted();
    }
    /**
     * Check whether currently used server is hosted or self-hosted
     * 
     * @return true if server is hosted, otherwise return false
     */
    public static boolean isHosted() {
        return getSystemVersionPreferences()
                .getBoolean(CommonUtils.getStringResource(R.string.setting_system_version_hosted),
                        false);
    }

    /**
     * Set used server type (hosted or serlf-hosted)
     * 
     * @param value true if server is hosted (*.trovebox.com), false if user
     *            uses own self-hosted installation
     */
    public static void setHosted(boolean value)
    {
        getSystemVersionPreferences()
                .edit()
                .putBoolean(CommonUtils.getStringResource(R.string.setting_system_version_hosted),
                        value)
                .commit();
    }

    /**
     * Check whether the purchase was already verified by the application
     * @param purchase purchase to verify
     * @return true if purchase was successfully verified
     */
    public static boolean isPurchaseVerified(Purchase purchase)
    {
        return getVerifiedPaymentsPreferences().getBoolean(
                purchase.getDeveloperPayload() + ":" + purchase.getToken(), false);
    }

    /**
     * Set purchase verified state to the cache
     * @param purchase related purchase
     * @param verified whether the verification was successful
     */
    public static void setPurchaseVerified(Purchase purchase, boolean verified)
    {
        getVerifiedPaymentsPreferences().edit().putBoolean(
                purchase.getDeveloperPayload() + ":" + purchase.getToken(), verified);
    }
    /**
     * Check whether necessary system version information already retrieved and
     * stored in the cache
     * 
     * @return
     */
    public static boolean isSystemVersionInformationCached() {
        return getSystemVersionPreferences()
                .getBoolean(
                        CommonUtils.getStringResource(R.string.setting_system_version_info_updated),
                        false);
    }

    /**
     * Set whether necessary system version information retrieved
     * and cached properly
     * 
     * @param value true if information retrieved and saved, false othwerwise
     */
    public static void setSystemVersionInformationCached(boolean value)
    {
        getSystemVersionPreferences()
                .edit()
                .putBoolean(
                        CommonUtils.getStringResource(R.string.setting_system_version_info_updated),
                        value)
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
        getSystemVersionPreferences()
                .edit()
                .clear()
                .commit();
        getVerifiedPaymentsPreferences()
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
