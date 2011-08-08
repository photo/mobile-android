
package me.openphoto.android.app.net;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;

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
        return PhotosResponse.fromJson(response.getContentAsString());
    }

}
