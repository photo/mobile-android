
package com.trovebox.android.common.model;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Class representing login credentials for Trovebox.
 * 
 * @author Eugene Popovich
 */
public class Credentials implements Parcelable {
    public static final String OWNER_TYPE = "owner";
    public static final String ADMIN_TYPE = "admin";
    public static final String GROUP_TYPE = "group";

    private String mHost;
    private String mOAuthConsumerKey;
    private String mOAuthConsumerSecret;
    private String mOAuthToken;
    private String mOAuthTokenSecret;
    private String mEmail;
    private String mType;
    private ProfileInformation mProfileInformation;

    private Credentials() {
    }

    public Credentials(JSONObject json) throws JSONException {
        mHost = json.getString("host");
        mOAuthConsumerKey = json.getString("id");
        mOAuthConsumerSecret = json.getString("clientSecret");
        mOAuthToken = json.getString("userToken");
        mOAuthTokenSecret = json.getString("userSecret");
        mEmail = json.getString("owner");
        mType = json.optString("_type", OWNER_TYPE);
        JSONObject profileJson = json.optJSONObject("profile");
        if (profileJson != null) {
            mProfileInformation = ProfileInformation.fromJson(profileJson);
        }
    }

    public String getHost() {
        return mHost;
    }

    public String getServer() {
        return "http://" + mHost;
    }

    public String getoAuthConsumerKey() {
        return mOAuthConsumerKey;
    }

    public String getoAuthConsumerSecret() {
        return mOAuthConsumerSecret;
    }

    public String getoAuthToken() {
        return mOAuthToken;
    }

    public String getoAuthTokenSecret() {
        return mOAuthTokenSecret;
    }

    public String getEmail() {
        return mEmail;
    }

    public String getType() {
        return mType;
    }

    public ProfileInformation getProfileInformation() {
        return mProfileInformation;
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
        out.writeString(mHost);
        out.writeString(mEmail);
        out.writeString(mType);
        out.writeString(mOAuthConsumerKey);
        out.writeString(mOAuthConsumerSecret);
        out.writeString(mOAuthToken);
        out.writeString(mOAuthTokenSecret);
        out.writeParcelable(mProfileInformation, flags);
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
        mHost = in.readString();
        mEmail = in.readString();
        mType = in.readString();
        mOAuthConsumerKey = in.readString();
        mOAuthConsumerSecret = in.readString();
        mOAuthToken = in.readString();
        mOAuthTokenSecret = in.readString();
        mProfileInformation = in.readParcelable(Credentials.class.getClassLoader());
    }
}
