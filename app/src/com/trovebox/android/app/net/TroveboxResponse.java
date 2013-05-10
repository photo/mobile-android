
package com.trovebox.android.app.net;

import org.json.JSONException;
import org.json.JSONObject;

public class TroveboxResponse {
    /**
     * Possible request types
     */
    public enum RequestType
    {
        UNKNOWN,
        TAGS,
        PHOTO_UPLOAD, ALBUMS, PHOTOS,
        PHOTO, DELETE_PHOTO, UPDATE_PHOTO,
        CREATE_USER, SIGN_IN, RECOVER_PASSWORD,
        PROFILE, SYSTEM_VERSION,
        PAYMENT_VERIFICATION,
        CREATE_ALBUM,
        CREATE_PHOTO_TOKEN
    }
    private final String mMessage;
    private final int mCode;
    private final RequestType requestType;

    /**
     * @param requestType the request type for this response. Used to track invalid responses
     * @param json
     * @throws JSONException
     */
    public TroveboxResponse(RequestType requestType, JSONObject json) throws JSONException {
        mMessage = json.getString("message");
        mCode = json.getInt("code");
        this.requestType = requestType;

        TroveboxResponseUtils.trackResponseIfInvalid(this);
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

    /**
     * Gets the alert message for the user alert.
     * This can be overrode in the inherited classes
     * to process different status codes and show custom
     * localized messages. 
     * As example see AccountTroveboxResponse.getAlertMessage()
     * 
     * @return message for user alert
     */
    public String getAlertMessage()
    {
        return getMessage();
    }

    /**
     * @return the request type for this response
     */
    public RequestType getRequestType()
    {
        return requestType;
    }
}
