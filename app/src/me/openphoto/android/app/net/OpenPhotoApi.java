
package me.openphoto.android.app.net;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

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
     * @return the photos
     * @throws ClientProtocolException
     * @throws IOException
     * @throws IllegalStateException
     * @throws JSONException
     */
    public PhotosResponse getPhotos()
            throws ClientProtocolException, IllegalStateException, IOException, JSONException {
        return getPhotos(null, null, 0);
    }

    /**
     * Get photos.
     * 
     * @param resize which sizes should be returned
     * @return the photos
     * @throws ClientProtocolException
     * @throws IOException
     * @throws IllegalStateException
     * @throws JSONException
     */
    public PhotosResponse getPhotos(ReturnSize resize)
            throws ClientProtocolException, IllegalStateException, IOException, JSONException {
        return getPhotos(resize, null, 0);
    }

    /**
     * Get photos.
     * 
     * @param resize which sizes should be returned
     * @param page page to be retrieved
     * @return the photos
     * @throws ClientProtocolException
     * @throws IOException
     * @throws IllegalStateException
     * @throws JSONException
     */
    public PhotosResponse getPhotos(ReturnSize resize, int page)
            throws ClientProtocolException, IllegalStateException, IOException, JSONException {
        return getPhotos(resize, null, page);
    }

    /**
     * Get photos.
     * 
     * @param resize which sizes should be returned
     * @param tags filter potos by these tags
     * @return the photos
     * @throws ClientProtocolException
     * @throws IOException
     * @throws IllegalStateException
     * @throws JSONException
     */
    public PhotosResponse getPhotos(ReturnSize resize, Collection<String> tags)
            throws ClientProtocolException, IllegalStateException, IOException, JSONException {
        return getPhotos(resize, tags, 0);
    }

    /**
     * Get photos.
     * 
     * @param resize which sizes should be returned
     * @param tags filter potos by these tags
     * @param page page to be retrieved
     * @return the photos
     * @throws ClientProtocolException
     * @throws IOException
     * @throws IllegalStateException
     * @throws JSONException
     */
    public PhotosResponse getPhotos(ReturnSize resize, Collection<String> tags, int page)
            throws ClientProtocolException, IOException, IllegalStateException, JSONException {
        ApiRequest request = new ApiRequest(ApiRequest.GET, "/photos.json");
        if (resize != null) {
            request.addParameter("returnSizes", resize.toString());
        }
        if (tags != null && !tags.isEmpty()) {
            Iterator<String> it = tags.iterator();
            StringBuilder sb = new StringBuilder(it.next());
            while (it.hasNext()) {
                sb.append("," + it.next());
            }
            request.addParameter("tags", sb.toString());
        }
        if (page > 0) {
            request.addParameter("page", Integer.toString(page));
        }
        ApiResponse response = execute(request);
        return new PhotosResponse(new JSONObject(response.getContentAsString()));
    }

    /**
     * Upload a picture.
     * 
     * @param imageFile the image file
     * @param metaData MetaData which define title, ... of the photo
     * @return The response with which contains info about the uploaded photo
     * @throws IOException
     * @throws ClientProtocolException
     * @throws JSONException
     * @throws IllegalStateException
     */
    public UploadResponse uploadPhoto(File imageFile, UploadMetaData metaData)
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
        if (metaData.getDescription() != null) {
            request.addParameter("description", metaData.getDescription());
        }
        request.addParameter("permission", Integer.toString(metaData.getPermission()));

        request.addFileParameter("photo", imageFile);
        ApiResponse response = execute(request);
        String responseString = response.getContentAsString();
        return new UploadResponse(new JSONObject(responseString));
    }
}
