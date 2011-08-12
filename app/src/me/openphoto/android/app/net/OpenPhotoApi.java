
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
     * @param metaData MetaData which define title, ... of the photo
     * @return The response with which contains info about the uploaded photo
     * @throws IOException
     * @throws ClientProtocolException
     * @throws JSONException
     * @throws IllegalStateException
     */
    public PhotoResponse uploadPhoto(InputStream imageStream, UploadMetaData metaData)
            throws ClientProtocolException, IOException, IllegalStateException, JSONException {
        ApiRequest request = new ApiRequest(ApiRequest.POST, "/photo/upload.json");
        request.setMime(true);
        if (metaData.getTags() != null) {
            request.addParameter("tags", metaData.getTags());
        }
        if (metaData.getTitle() != null) {
            request.addParameter("title", metaData.getTitle());
        }
        if (metaData.getDescription() != null) {
            request.addParameter("description", metaData.getDescription());
        }

        request.addParameter("photo", imageStream);
        ApiResponse response = execute(request);
        return new PhotoResponse(new JSONObject(response.getContentAsString()));
    }
}
