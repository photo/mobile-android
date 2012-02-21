
package me.openphoto.android.app.net;

import me.openphoto.android.app.model.Photo;

import org.json.JSONException;
import org.json.JSONObject;

public class PhotoResponse extends OpenPhotoResponse {
    private final Photo mPhoto;

    public PhotoResponse(JSONObject json) throws JSONException {
        super(json);
        if (isSuccess()) {
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
