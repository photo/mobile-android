
package me.openphoto.android.app.net;

import org.json.JSONException;
import org.json.JSONObject;

public class OpenPhotoResponse {
    private final String mMessage;
    private final int mCode;

    public OpenPhotoResponse(JSONObject json) throws JSONException {
        mMessage = json.getString("message");
        mCode = json.getInt("code");
    }

    public String getMessage() {
        return mMessage;
    }

    public int getCode() {
        return mCode;
    }

    public boolean isSuccess() {
        return mCode >= 200 && mCode < 300;
    }
}
