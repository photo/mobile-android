
package me.openphoto.android.app.net;

import java.util.ArrayList;
import java.util.List;

import me.openphoto.android.app.model.Photo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PhotosResponse extends PagedResponse {

    private List<Photo> mPhotos;

    private PhotosResponse() {
        mPhotos = new ArrayList<Photo>();
    }

    /**
     * This will create a PhotosResponse object by giving the response as
     * received from an OpenPhoto API.
     * 
     * @param jsonString
     * @return PhotosResponse object represented in the jsonString
     * @throws JSONException
     */
    public static PhotosResponse fromJson(String jsonString) throws JSONException {
        PhotosResponse response = new PhotosResponse();

        JSONObject json = new JSONObject(jsonString);
        JSONArray result = json.getJSONArray("result");
        response.setPaging(result.getJSONObject(0));
        if (response.getTotalRows() > 0) {
            for (int i = 0; i < result.length(); i++) {
                response.mPhotos.add(Photo.fromJson(result.getJSONObject(i)));
            }
        }

        return response;
    }

    /**
     * @return the retrieved photos
     */
    public List<Photo> getPhotos() {
        return mPhotos;
    }
}
