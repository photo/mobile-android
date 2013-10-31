
package com.trovebox.android.app.net;

import org.json.JSONException;
import org.json.JSONObject;

import com.trovebox.android.app.model.ProfileInformation;

/**
 * The class to represent API profile json response
 * 
 * @author Eugene Popovich
 */
public class ProfileResponse extends TroveboxResponse {
    private ProfileInformation mProfileInformation;

    public ProfileResponse(JSONObject json) throws JSONException {
        super(RequestType.PROFILE, json);
        json = json.optJSONObject("result");
        if (json != null)
        {
            mProfileInformation = ProfileInformation.fromJson(json);
        }
    }

    public ProfileInformation getProfileInformation()
    {
        return mProfileInformation;
    }
}
