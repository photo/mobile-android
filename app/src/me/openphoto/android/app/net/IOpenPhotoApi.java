
package me.openphoto.android.app.net;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;

public interface IOpenPhotoApi {

    /**
     * @return tags which are used on the server
     * @throws ClientProtocolException
     * @throws IOException
     * @throws IllegalStateException
     * @throws JSONException
     */
    TagsResponse getTags() throws ClientProtocolException, IOException, IllegalStateException,
            JSONException;

    /**
     * Retrieve a single photo.
     * 
     * @param photoId id of the photo
     * @param returnSize which sizes should be returned
     * @return the photo
     * @throws IOException
     * @throws ClientProtocolException
     * @throws JSONException
     * @throws IllegalStateException
     */
    PhotoResponse getPhoto(String photoId, ReturnSize returnSize) throws ClientProtocolException,
            IOException, IllegalStateException, JSONException;

    /**
     * Get photos.
     * 
     * @return the photos
     * @throws ClientProtocolException
     * @throws IOException
     * @throws IllegalStateException
     * @throws JSONException
     */
    PhotosResponse getPhotos()
            throws ClientProtocolException, IllegalStateException, IOException, JSONException;

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
    PhotosResponse getPhotos(ReturnSize resize)
            throws ClientProtocolException, IllegalStateException, IOException, JSONException;

    /**
     * Get photos.
     * 
     * @param resize which sizes should be returned
     * @param paging paging information
     * @return the photos
     * @throws ClientProtocolException
     * @throws IOException
     * @throws IllegalStateException
     * @throws JSONException
     */
    PhotosResponse getPhotos(ReturnSize resize, Paging paging)
            throws ClientProtocolException, IllegalStateException, IOException, JSONException;

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
    PhotosResponse getPhotos(ReturnSize resize, Collection<String> tags)
            throws ClientProtocolException, IllegalStateException, IOException, JSONException;

    /**
     * Get photos.
     * 
     * @param resize which sizes should be returned
     * @param tags filter potos by these tags
     * @param pageing page and pageSize to be retrieved
     * @return the photos
     * @throws ClientProtocolException
     * @throws IOException
     * @throws IllegalStateException
     * @throws JSONException
     */
    PhotosResponse getPhotos(ReturnSize resize, Collection<String> tags, Paging paging)
            throws ClientProtocolException, IOException, IllegalStateException, JSONException;

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
    UploadResponse uploadPhoto(File imageFile, UploadMetaData metaData)
            throws ClientProtocolException, IOException, IllegalStateException, JSONException;
}
