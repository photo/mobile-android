
package com.trovebox.android.app.net;

import org.json.JSONException;
import org.json.JSONObject;

import com.trovebox.android.app.R;
import com.trovebox.android.app.TroveboxApplication;

public class UploadResponse extends PhotoResponse {
    private static final int LIMIT_REACHED_CODE = 402;

    public UploadResponse(JSONObject json) throws JSONException {
        super(RequestType.PHOTO_UPLOAD, json);
    }

    /**
     * @return true if the upload was successful
     */
    @Override
    public boolean isSuccess() {
        return getCode() == 201;
    }

    @Override
    public String getAlertMessage() {
        switch (getCode())
        {
            case LIMIT_REACHED_CODE:
                return TroveboxApplication.getContext().getString(
                        R.string.upload_limit_reached_message);
            default:
                return super.getAlertMessage();
        }
    }
}
