
package me.openphoto.android.app.net;

import me.openphoto.android.app.model.Photo;

import org.json.JSONException;
import org.json.JSONObject;

public class PhotoResponse extends OpenPhotoResponse {
    private Photo mPhoto;

    public PhotoResponse(JSONObject json) throws JSONException {
        super(json);
        mPhoto = Photo.fromJson(json.getJSONObject("result"));
    }

    public Photo getPhoto() {
        return mPhoto;
    }
}
