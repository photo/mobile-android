
package me.openphoto.android.app.net;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import me.openphoto.android.app.Preferences;
import me.openphoto.android.app.net.HttpEntityWithProgress.ProgressListener;
import me.openphoto.android.app.util.CommonUtils;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

/**
 * OpenPhotoApi provides access to the acOpenPhoto API.
 * 
 * @author Patrick Boos
 */
public class OpenPhotoApi extends ApiBase implements IOpenPhotoApi {

    private static IOpenPhotoApi sMock;
    public static final String NEWEST_PHOTO_SORT_ORDER = "dateUploaded,DESC";

    public static IOpenPhotoApi createInstance(Context context) {
        if (sMock != null) {
            return sMock;
        } else {
            return new OpenPhotoApi(context);
        }
    }

    public OpenPhotoApi(Context context) {
        super(context);
    }

    @Override
    public TagsResponse getTags() throws ClientProtocolException, IOException,
            IllegalStateException, JSONException {
        ApiRequest request = new ApiRequest(ApiRequest.GET, "/tags/list.json");
        ApiResponse response = execute(request);
        return new TagsResponse(new JSONObject(response.getContentAsString()));
    }

    @Override
    public AlbumsResponse getAlbums() throws ClientProtocolException,
            IOException,
            IllegalStateException, JSONException
    {
        ApiRequest request = new ApiRequest(ApiRequest.GET, "/albums/list.json");
        ApiResponse response = execute(request);
        String content = response.getContentAsString();
        return new AlbumsResponse(new JSONObject(content));
    }

    @Override
    public PhotoResponse getPhoto(String photoId, ReturnSizes returnSize)
            throws ClientProtocolException, IOException, IllegalStateException,
            JSONException {
        ApiRequest request = new ApiRequest(ApiRequest.GET, "/photo/" + photoId
                + "/view.json");
        if (returnSize != null) {
            request.addParameter("returnSizes", returnSize.toString());
        }
        ApiResponse response = execute(request);
        return new PhotoResponse(new JSONObject(response.getContentAsString()));
    }

    @Override
    public String getOAuthUrl(String name, String callback, Context context) {
        return Preferences.getServer(context) + "/v1/oauth/authorize?mobile=1&name="
                + name
                + "&oauth_callback=" + callback;
    }

    @Override
    public PhotosResponse getPhotos() throws ClientProtocolException,
            IllegalStateException, IOException, JSONException {
        return getPhotos(null, null);
    }

    @Override
    public PhotosResponse getPhotos(ReturnSizes resize)
            throws ClientProtocolException, IllegalStateException, IOException,
            JSONException {
        return getPhotos(resize, null);
    }

    @Override
    public PhotosResponse getPhotos(ReturnSizes resize, Paging paging)
            throws ClientProtocolException, IllegalStateException, IOException,
            JSONException {
        return getPhotos(resize, null, null, null, paging);
    }

    @Override
    public PhotosResponse getPhotos(ReturnSizes resize,
            Collection<String> tags,
            String album)
            throws ClientProtocolException, IllegalStateException, IOException,
            JSONException {
        return getPhotos(resize, tags, album, null, null);
    }

    /*
     * (non-Javadoc)
     * @see
     * me.openphoto.android.app.net.IOpenPhotoApi#getPhotos(me.openphoto.android
     * .app.net.ReturnSize, java.util.Collection,
     * me.openphoto.android.app.net.Paging)
     */
    @Override
    public PhotosResponse getPhotos(ReturnSizes resize,
            Collection<String> tags,
            String album,
            String sortBy, Paging paging)
            throws ClientProtocolException, IOException, IllegalStateException,
            JSONException {
        return getPhotos(resize, tags, album, null, sortBy, paging);
    }

    public PhotosResponse getPhotos(ReturnSizes resize,
            Collection<String> tags,
            String album,
            String hash,
            String sortBy,
            Paging paging)
            throws ClientProtocolException, IOException, IllegalStateException,
            JSONException
    {
        ApiRequest request;
        if (album != null && album.length() > 0)
        {
            request = new ApiRequest(ApiRequest.GET, "/photos/album-" + album
                    + "/list.json");
        } else
        {
            request = new ApiRequest(ApiRequest.GET, "/photos/list.json");
        }
        if (hash != null)
        {
            request.addParameter("hash", hash);
        }
        if (sortBy != null)
        {
            request.addParameter("sortBy", sortBy);
        }
        if (resize != null)
        {
            request.addParameter("returnSizes", resize.toString());
        }
        if (tags != null && !tags.isEmpty())
        {
            Iterator<String> it = tags.iterator();
            StringBuilder sb = new StringBuilder(it.next());
            while (it.hasNext())
            {
                sb.append("," + it.next());
            }
            request.addParameter("tags", sb.toString());
        }
        if (paging != null)
        {
            if (paging.hasPage())
            {
                request.addParameter("page", Integer.toString(paging.getPage()));
            }
            if (paging.hasPageSize())
            {
                request.addParameter("pageSize",
                        Integer.toString(paging.getPageSize()));
            }
        }
        ApiResponse response = execute(request);
        // TODO: Fix null pointer exception at this place.
        return new PhotosResponse(new JSONObject(response.getContentAsString()));
    }

    /*
     * (non-Javadoc)
     * @see me.openphoto.android.app.net.IOpenPhotoApi#uploadPhoto(java.io.File,
     * me.openphoto.android.app.net.UploadMetaData)
     */
    @Override
    public UploadResponse uploadPhoto(File imageFile, UploadMetaData metaData,
            ProgressListener progressListener) throws ClientProtocolException,
            IOException, IllegalStateException, JSONException {
        ApiRequest request = new ApiRequest(ApiRequest.POST,
                "/photo/upload.json");
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
        request.addParameter("permission",
                Integer.toString(metaData.getPermission()));

        request.addFileParameter("photo", imageFile);
        ApiResponse response = execute(request, progressListener);
        String responseString = response.getContentAsString();
        return new UploadResponse(new JSONObject(responseString));
    }

    @Override
    public PhotosResponse getNewestPhotos(Paging paging) throws ClientProtocolException,
            IOException,
            IllegalStateException, JSONException {
        return getNewestPhotos(null, paging);
    }

    @Override
    public PhotosResponse getNewestPhotos(ReturnSizes returnSize, Paging paging)
            throws ClientProtocolException, IOException, IllegalStateException,
            JSONException
    {
        return getPhotos(returnSize, null, null, NEWEST_PHOTO_SORT_ORDER, paging);
    }

    /**
     * ONLY FOR TESTING! Will make the class return the given Mock when
     * accessing createInstance(..)
     * 
     * @param mock Mock to be used for OpenPhotoApi
     */
    public static void injectMock(IOpenPhotoApi mock) {
        OpenPhotoApi.sMock = mock;
    }

    @Override
    public PhotosResponse getPhotos(String hash)
            throws ClientProtocolException,
            IOException, IllegalStateException,
            JSONException
    {
        return getPhotos(null, null, null, hash, null);
    }

    @Override
    public OpenPhotoResponse deletePhoto(String photoId) throws ClientProtocolException,
            IOException, IllegalStateException, JSONException
    {
        ApiRequest request = new ApiRequest(ApiRequest.POST, "/photo/" + photoId
                + "/delete.json");
        ApiResponse response = execute(request);
        String content = response.getContentAsString();
        CommonUtils.debug(TAG, content);
        return new OpenPhotoResponse(new JSONObject(content));
    }
}
