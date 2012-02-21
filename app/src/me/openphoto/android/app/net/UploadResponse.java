
package me.openphoto.android.app.net;

import org.json.JSONException;
import org.json.JSONObject;

public class UploadResponse extends PhotoResponse {

    public UploadResponse(JSONObject json) throws JSONException {
        super(json);
    }

    /**
     * @return true if the upload was successful
     */
    @Override
    public boolean isSuccess() {
        return getCode() == 201;
    }
}
