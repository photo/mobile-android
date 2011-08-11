
package me.openphoto.android.app.net;

import java.util.ArrayList;
import java.util.List;

import me.openphoto.android.app.model.Photo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PhotosResponse extends PagedResponse {

    private List<Photo> mPhotos;

    public PhotosResponse(JSONObject json) throws JSONException {
        super(json);

        mPhotos = new ArrayList<Photo>();

        JSONArray result = json.getJSONArray("result");

        if (getTotalRows() > 0) {
            for (int i = 0; i < result.length(); i++) {
                mPhotos.add(Photo.fromJson(result.getJSONObject(i)));
            }
        }
    }

    /**
     * @return the retrieved photos
     */
    public List<Photo> getPhotos() {
        return mPhotos;
    }
}
