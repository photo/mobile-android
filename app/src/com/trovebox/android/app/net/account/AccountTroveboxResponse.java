
package com.trovebox.android.app.net.account;


import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.trovebox.android.app.Preferences;
import com.trovebox.android.app.R;
import com.trovebox.android.app.TroveboxApplication;
import com.trovebox.android.app.net.TroveboxResponse;

/**
 * @author Patrick Santana <patrick@trovebox.com>
 */
public class AccountTroveboxResponse extends TroveboxResponse {
    public static int SUCCESSFUL_CODE = 200;
    public static int INVALID_CREDENTIALS_CODE = 403;
    public static int UNKNOWN_ERROR_CODE = 500;
    private String server;
    private String oAuthConsumerKey;
    private String oAuthConsumerSecret;
    private String oAuthToken;
    private String oAuthTokenSecret;
    private String email;

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public String getoAuthConsumerKey() {
        return oAuthConsumerKey;
    }

    public void setoAuthConsumerKey(String oAuthConsumerKey) {
        this.oAuthConsumerKey = oAuthConsumerKey;
    }

    public String getoAuthConsumerSecret() {
        return oAuthConsumerSecret;
    }

    public void setoAuthConsumerSecret(String oAuthConsumerSecret) {
        this.oAuthConsumerSecret = oAuthConsumerSecret;
    }

    public String getoAuthToken() {
        return oAuthToken;
    }

    public void setoAuthToken(String oAuthToken) {
        this.oAuthToken = oAuthToken;
    }

    public String getoAuthTokenSecret() {
        return oAuthTokenSecret;
    }

    public void setoAuthTokenSecret(String oAuthTokenSecret) {
        this.oAuthTokenSecret = oAuthTokenSecret;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public AccountTroveboxResponse(RequestType requestType, JSONObject json) throws JSONException {
        super(requestType, json);
        if (isSuccess() && json.get("result") instanceof JSONObject) {
            JSONObject result = json.getJSONObject("result");
            server = "http://" + result.getString("host");
            oAuthConsumerKey = result.getString("id");
            oAuthConsumerSecret = result.getString("clientSecret");
            oAuthToken = result.getString("userToken");
            oAuthTokenSecret = result.getString("userSecret");
            email = result.getString("owner");
        }
    }

    public void saveCredentials(Context context) {
        Preferences.setServer(context, this.getServer());

        Preferences
                .getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(context.getString(R.string.setting_account_loggedin_key), true)
                .commit();

        Preferences
                .getSharedPreferences("oauth")
                .edit()
                .putString(context.getString(R.string.setting_oauth_consumer_key),
                        this.getoAuthConsumerKey())
                .putString(context.getString(R.string.setting_oauth_consumer_secret),
                        this.getoAuthConsumerSecret())
                .putString(context.getString(R.string.setting_oauth_token),
                        this.getoAuthToken())
                .putString(context.getString(R.string.setting_oauth_token_secret),
                        this.getoAuthTokenSecret())
                .commit();
    }

    @Override
    public boolean isSuccess()
    {
        return getCode() == SUCCESSFUL_CODE;
    }

    public boolean isInvalidCredentials()
    {
        return getCode() == INVALID_CREDENTIALS_CODE;
    }

    public boolean isUnknownError()
    {
        return getCode() == UNKNOWN_ERROR_CODE;
    }

    @Override
    public String getAlertMessage() {
        if (isInvalidCredentials())
        {
            return TroveboxApplication.getContext().getString(R.string.invalid_credentials);
        } else
        {
            return super.getAlertMessage();
        }
    }
}
