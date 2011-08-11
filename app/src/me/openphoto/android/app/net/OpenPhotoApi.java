
package me.openphoto.android.app.net;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * OpenPhotoApi provides access to the acOpenPhoto API.
 * 
 * @author Patrick Boos
 */
public class OpenPhotoApi extends ApiBase {

    /**
     * Constructor
     * 
     * @param baseUrl the base URL of the used OpenPhoto
     */
    public OpenPhotoApi(String baseUrl) {
        super(baseUrl);
    }

    /**
     * Get photos.
     * 
     * @throws IOException
     * @throws ClientProtocolException
     * @throws JSONException
     * @throws IllegalStateException
     */
    public PhotosResponse getPhotos() throws ClientProtocolException, IOException,
            IllegalStateException, JSONException {
        ApiRequest request = new ApiRequest(ApiRequest.GET, "/photos.json");
        ApiResponse response = execute(request);
        return new PhotosResponse(new JSONObject(response.getContentAsString()));
    }

    /**
     * Upload a picture.
     * 
     * @param imageStream InputStream for the picture
     * @param settings Settings which define title, ... of the photo
     * @return // TODO
     * @throws IOException
     * @throws ClientProtocolException
     */
    public String uploadPhoto(InputStream imageStream, PhotoUploadSettings settings)
            throws ClientProtocolException, IOException {
        ApiRequest request = new ApiRequest(ApiRequest.POST, "/photo/upload.json");
        request.setMime(true);
        if (settings.getTags() != null) {
            request.addParameter("tags", settings.getTags());
        }
        if (settings.getTitle() != null) {
            request.addParameter("title", settings.getTitle());
        }
        if (settings.getDescription() != null) {
            request.addParameter("description", settings.getDescription());
        }

        request.addParameter("photo", imageStream);
        ApiResponse response = execute(request);
        return response.getContentAsString();
    }

}
