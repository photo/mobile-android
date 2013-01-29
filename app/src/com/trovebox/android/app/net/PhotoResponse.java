
package com.trovebox.android.app.net;


import org.json.JSONException;
import org.json.JSONObject;

import com.trovebox.android.app.model.Photo;

public class PhotoResponse extends TroveboxResponse {
    private final Photo mPhoto;

    public PhotoResponse(JSONObject json) throws JSONException {
        super(json);
        if (isSuccess() && json.get("result") instanceof JSONObject) {
            mPhoto = Photo.fromJson(json.getJSONObject("result"));
        } else {
            mPhoto = null;
        }
    }

    /**
     * @return the photo contained in the response from the server
     */
    public Photo getPhoto() {
        return mPhoto;
    }
}
