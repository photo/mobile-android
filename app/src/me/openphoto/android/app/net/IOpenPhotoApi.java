
package me.openphoto.android.app.net;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import me.openphoto.android.app.net.HttpEntityWithProgress.ProgressListener;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;

import android.content.Context;

public interface IOpenPhotoApi {

    /**
     * @return tags which are used on the server
     * @throws ClientProtocolException
     * @throws IOException
     * @throws IllegalStateException
     * @throws JSONException
     */
    TagsResponse getTags() throws ClientProtocolException, IOException,
            IllegalStateException, JSONException;

    /**
     * @return albums which are used on the server
     * @throws ClientProtocolException
     * @throws IOException
     * @throws IllegalStateException
     * @throws JSONException
     */
    AlbumsResponse getAlbums() throws ClientProtocolException, IOException,
            IllegalStateException, JSONException;

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
    PhotoResponse getPhoto(String photoId, ReturnSizes returnSize)
            throws ClientProtocolException, IOException, IllegalStateException,
            JSONException;

    /**
     * Will return the URL to which the user has to start the OAuth
     * authorization process.
     * 
     * @param name Name of the app for which authorization is requested
     * @param callback Where the user should be forwarded after authorizing the
     *            app.
     * @param context
     * @return Url to which the user should be pointed in a WebView.
     */
    String getOAuthUrl(String name, String callback, Context context);

    /**
     * Get photos.
     * 
     * @return the photos
     * @throws ClientProtocolException
     * @throws IOException
     * @throws IllegalStateException
     * @throws JSONException
     */
    PhotosResponse getPhotos() throws ClientProtocolException,
            IllegalStateException, IOException, JSONException;

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
    PhotosResponse getPhotos(ReturnSizes resize)
            throws ClientProtocolException, IllegalStateException, IOException,
            JSONException;

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
    PhotosResponse getPhotos(ReturnSizes resize, Paging paging)
            throws ClientProtocolException, IllegalStateException, IOException,
            JSONException;

    /**
     * Get photos.
     * 
     * @param resize which sizes should be returned
     * @param tags filter potos by these tags
     * @param album filter potos by this album
     * @return the photos
     * @throws ClientProtocolException
     * @throws IOException
     * @throws IllegalStateException
     * @throws JSONException
     */
    PhotosResponse getPhotos(ReturnSizes resize, Collection<String> tags,
            String album)
            throws ClientProtocolException, IllegalStateException, IOException,
            JSONException;

    /**
     * Get photos.
     * 
     * @param resize which sizes should be returned
     * @param tags filter potos by these tags
     * @param album filter potos by this album
     * @param sortBy sort photos condition
     * @param pageing page and pageSize to be retrieved
     * @return the photos
     * @throws ClientProtocolException
     * @throws IOException
     * @throws IllegalStateException
     * @throws JSONException
     */
    PhotosResponse getPhotos(ReturnSizes resize, Collection<String> tags,
            String album,
            String sortBy,
            Paging paging) throws ClientProtocolException, IOException,
            IllegalStateException, JSONException;

    /**
     * Upload a picture.
     * 
     * @param imageFile the image file
     * @param metaData MetaData which define title, ... of the photo
     * @param progressListener Listener that will be called on progress
     * @return The response with which contains info about the uploaded photo
     * @throws IOException
     * @throws ClientProtocolException
     * @throws JSONException
     * @throws IllegalStateException
     */
    UploadResponse uploadPhoto(File imageFile, UploadMetaData metaData,
            ProgressListener progressListener) throws ClientProtocolException,
            IOException, IllegalStateException, JSONException;

    /**
     * Return Newest Photos to be used in the Home Screen
     * 
     * @param paging paging for the newest hone screen
     * @return a list of photos to be displayed inthe home screen
     * @throws ClientProtocolException
     * @throws IOException
     * @throws IllegalStateException
     * @throws JSONException
     */
    PhotosResponse getNewestPhotos(Paging paging) throws ClientProtocolException, IOException,
            IllegalStateException, JSONException;

    /**
     * Return Newest Photos to be used in the Home Screen
     * 
     * @param resize which sizes should be returned
     * @param paging paging for the newest hone screen
     * @return a list of photos to be displayed inthe home screen
     * @throws ClientProtocolException
     * @throws IOException
     * @throws IllegalStateException
     * @throws JSONException
     */
    PhotosResponse getNewestPhotos(ReturnSizes resize, Paging paging)
            throws ClientProtocolException, IOException,
            IllegalStateException, JSONException;

    /**
     * Return the photos by given SHA-1 hash
     * 
     * @param hash
     * @return a list of photos with the specified hash
     * @throws ClientProtocolException
     * @throws IOException
     * @throws IllegalStateException
     * @throws JSONException
     */
    PhotosResponse getPhotos(String hash) throws ClientProtocolException,
            IOException, IllegalStateException,
            JSONException;

    /**
     * Delete the photo from the server by id
     * 
     * @param photoId id of the photo
     * @return
     * @throws ClientProtocolException
     * @throws IOException
     * @throws IllegalStateException
     * @throws JSONException
     */
    public OpenPhotoResponse deletePhoto(String photoId) throws ClientProtocolException,
            IOException, IllegalStateException, JSONException;
}
