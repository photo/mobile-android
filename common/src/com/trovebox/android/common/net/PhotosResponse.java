
package com.trovebox.android.common.net;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.trovebox.android.common.model.Photo;

public class PhotosResponse extends PagedResponse {

    private List<Photo> mPhotos;

    public PhotosResponse(JSONObject json) throws JSONException {
        super(RequestType.PHOTOS, json);

        mPhotos = new ArrayList<Photo>();

        if (json.get("result") instanceof JSONArray)
        {
            JSONArray result = json.getJSONArray("result");

            if (getTotalRows() > 0) {
                for (int i = 0; i < result.length(); i++) {
                    mPhotos.add(Photo.fromJson(result.getJSONObject(i)));
                }
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
