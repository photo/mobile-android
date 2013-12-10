
package com.trovebox.android.app.net.account;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.trovebox.android.app.R;
import com.trovebox.android.app.TroveboxApplication;
import com.trovebox.android.common.model.Credentials;
import com.trovebox.android.common.net.TroveboxResponse;

/**
 * @author Patrick Santana <patrick@trovebox.com>
 */
public class AccountTroveboxResponse extends TroveboxResponse {
    public static int SUCCESSFUL_CODE = 200;
    public static int INVALID_CREDENTIALS_CODE = 403;
    public static int UNKNOWN_ERROR_CODE = 500;
    private Credentials[] mCredentials;

    public AccountTroveboxResponse(RequestType requestType, JSONObject json) throws JSONException {
        super(requestType, json);
        if (isSuccess()) {
            if (json.get("result") instanceof JSONArray) {
                JSONArray array = json.getJSONArray("result");
                mCredentials = new Credentials[array.length()];
                for (int i = 0; i < mCredentials.length; i++) {
                    mCredentials[i] = new Credentials(array.getJSONObject(i));
                }
            } else if (json.get("result") instanceof JSONObject) {
                JSONObject result = json.getJSONObject("result");
                mCredentials = new Credentials[1];
                mCredentials[0] = new Credentials(result);
            }
        }
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

    public Credentials[] getCredentials() {
        return mCredentials;
    }
}
