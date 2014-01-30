
package com.trovebox.android.common.net;

import org.json.JSONException;
import org.json.JSONObject;

import com.trovebox.android.common.R;
import com.trovebox.android.common.util.CommonUtils;

/**
 * The class to represent API token validation json response
 * 
 * @author Eugene Popovich
 */
public class TokenValidationResponse extends TroveboxResponse {
    public static int SUCCESSFUL_CODE = 200;
    public static int EXPIRED_TOKEN_CODE = 410;
    public static int INVALID_TOKEN_CODE = 500;

    private String mId;
    private String mHost;
    private String mOwner;
    private String mType;
    private String mData;

    public TokenValidationResponse(JSONObject json) throws JSONException {
        super(RequestType.VALIDATE_UPLOAD_TOKEN, json);
        json = json.optJSONObject("result");
        if (json != null) {
            mId = json.optString("id");
            mHost = json.optString("host");
            mOwner = json.optString("owner");
            mType = json.optString("type");
            mData = json.optString("data");
        }
    }

    public String getId() {
        return mId;
    }

    public String getHost() {
        return mHost;
    }

    public String getOwner() {
        return mOwner;
    }

    public String getType() {
        return mType;
    }

    public String getData() {
        return mData;
    }

    @Override
    public boolean isSuccess() {
        return getCode() == SUCCESSFUL_CODE;
    }

    public boolean isExpiredToken() {
        return getCode() == EXPIRED_TOKEN_CODE;
    }

    public boolean isInvalidToken() {
        return getCode() == INVALID_TOKEN_CODE;
    }

    @Override
    public String getAlertMessage() {
        if (isExpiredToken()) {
            return CommonUtils.getStringResource(R.string.api_expired_token);
        } else if (isInvalidToken()) {
            return CommonUtils.getStringResource(R.string.api_invalid_token);
        } else {
            return super.getAlertMessage();
        }
    }
}
