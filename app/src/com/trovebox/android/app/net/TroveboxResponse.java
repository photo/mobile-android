
package com.trovebox.android.app.net;

import org.json.JSONException;
import org.json.JSONObject;

public class TroveboxResponse {
    private final String mMessage;
    private final int mCode;

    public TroveboxResponse(JSONObject json) throws JSONException {
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
