package com.trovebox.android.app.model;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.trovebox.android.app.Preferences;
import com.trovebox.android.app.R;

/**
 * Class representing login credentials for Trovebox.
 * 
 * @author Eugene Popovich
 */
public class Credentials implements Parcelable {
    private String mServer;
    private String mOAuthConsumerKey;
    private String mOAuthConsumerSecret;
    private String mOAuthToken;
    private String mOAuthTokenSecret;
    private String mEmail;

    private Credentials() {
    }

    public Credentials(JSONObject json) throws JSONException {
        mServer = "http://" + json.getString("host");
        mOAuthConsumerKey = json.getString("id");
        mOAuthConsumerSecret = json.getString("clientSecret");
        mOAuthToken = json.getString("userToken");
        mOAuthTokenSecret = json.getString("userSecret");
        mEmail = json.getString("owner");
    }

    public String getServer() {
        return mServer;
    }

    public void setServer(String server) {
        this.mServer = server;
    }

    public String getoAuthConsumerKey() {
        return mOAuthConsumerKey;
    }

    public void setoAuthConsumerKey(String oAuthConsumerKey) {
        this.mOAuthConsumerKey = oAuthConsumerKey;
    }

    public String getoAuthConsumerSecret() {
        return mOAuthConsumerSecret;
    }

    public void setoAuthConsumerSecret(String oAuthConsumerSecret) {
        this.mOAuthConsumerSecret = oAuthConsumerSecret;
    }

    public String getoAuthToken() {
        return mOAuthToken;
    }

    public void setoAuthToken(String oAuthToken) {
        this.mOAuthToken = oAuthToken;
    }

    public String getoAuthTokenSecret() {
        return mOAuthTokenSecret;
    }

    public void setoAuthTokenSecret(String oAuthTokenSecret) {
        this.mOAuthTokenSecret = oAuthTokenSecret;
    }

    public String getEmail() {
        return mEmail;
    }

    public void setEmail(String email) {
        this.mEmail = email;
    }

    public void saveCredentials(Context context) {
        Preferences.setServer(context, this.getServer());

        Preferences.getDefaultSharedPreferences(context).edit()
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
                        this.getoAuthTokenSecret()).commit();
    }

    /*****************************
     * PARCELABLE IMPLEMENTATION *
     *****************************/
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(mServer);
        out.writeString(mEmail);
        out.writeString(mOAuthConsumerKey);
        out.writeString(mOAuthConsumerSecret);
        out.writeString(mOAuthToken);
        out.writeString(mOAuthTokenSecret);
    }

    public static final Parcelable.Creator<Credentials> CREATOR = new Parcelable.Creator<Credentials>() {
        @Override
        public Credentials createFromParcel(Parcel in) {
            return new Credentials(in);
        }

        @Override
        public Credentials[] newArray(int size) {
            return new Credentials[size];
        }
    };

    private Credentials(Parcel in) {
        this();
        mServer = in.readString();
        mEmail = in.readString();
        mOAuthConsumerKey = in.readString();
        mOAuthConsumerSecret = in.readString();
        mOAuthToken = in.readString();
        mOAuthTokenSecret = in.readString();
    }
}