
package me.openphoto.android.app.net.account;

import me.openphoto.android.app.Preferences;
import me.openphoto.android.app.R;
import me.openphoto.android.app.net.OpenPhotoResponse;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.preference.PreferenceManager;

/**
 * @author Patrick Santana <patrick@openphoto.me>
 */
public class AccountOpenPhotoResponse extends OpenPhotoResponse {

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

    public AccountOpenPhotoResponse(JSONObject json) throws JSONException {
        super(json);
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

        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(context.getString(R.string.setting_account_loggedin_key), true)
                .commit();

        context.getSharedPreferences("oauth", Context.MODE_PRIVATE)
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

}
