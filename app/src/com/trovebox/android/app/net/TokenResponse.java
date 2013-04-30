
package com.trovebox.android.app.net;

import org.json.JSONException;
import org.json.JSONObject;

import com.trovebox.android.app.model.Token;

/**
 * The class which represents token api json response
 * 
 * @author Eugene Popovich
 */
public class TokenResponse extends TroveboxResponse {
    private final Token mToken;

    public TokenResponse(RequestType requestType, JSONObject json) throws JSONException {
        super(requestType, json);
        if (isSuccess() && json.get("result") instanceof JSONObject) {
            mToken = Token.fromJson(json.getJSONObject("result"));
        } else {
            mToken = null;
        }
    }

    /**
     * @return the photo contained in the response from the server
     */
    public Token getToken() {
        return mToken;
    }
}
