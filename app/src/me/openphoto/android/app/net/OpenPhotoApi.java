
package me.openphoto.android.app.net;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import me.openphoto.android.app.net.HttpEntityWithProgress.ProgressListener;
import oauth.signpost.OAuthConsumer;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * OpenPhotoApi provides access to the acOpenPhoto API.
 * 
 * @author Patrick Boos
 */
public class OpenPhotoApi extends ApiBase implements IOpenPhotoApi {

    private static IOpenPhotoApi sMock;

    /**
     * Constructor
     * 
     * @param baseUrl the base URL of the used OpenPhoto
     */
    protected OpenPhotoApi(String baseUrl) {
        super(baseUrl);
    }

    public static IOpenPhotoApi createInstance(String baseUrl) {
        if (sMock != null) {
            return sMock;
        } else {
            return new OpenPhotoApi(baseUrl);
        }
    }

    /*
     * (non-Javadoc)
     * @see
     * me.openphoto.android.app.net.IOpenPhotoApi#setOAuthConsumer(oauth.signpost
     * .OAuthConsumer)
     */
    @Override
    public void setOAuthConsumer(OAuthConsumer oAuthConsumer) {
        super.setOAuthConsumer(oAuthConsumer);
    }

    /*
     * (non-Javadoc)
     * @see me.openphoto.android.app.net.IOpenPhotoApi#getTags()
     */
    @Override
    public TagsResponse getTags() throws ClientProtocolException, IOException,
            IllegalStateException, JSONException {
        ApiRequest request = new ApiRequest(ApiRequest.GET, "/tags/list.json");
        ApiResponse response = execute(request);
        return new TagsResponse(new JSONObject(response.getContentAsString()));
    }

    /*
     * (non-Javadoc)
     * @see
     * me.openphoto.android.app.net.IOpenPhotoApi#getPhoto(java.lang.String,
     * me.openphoto.android.app.net.ReturnSize)
     */
    @Override
    public PhotoResponse getPhoto(String photoId, ReturnSize returnSize)
            throws ClientProtocolException, IOException, IllegalStateException, JSONException {
        ApiRequest request = new ApiRequest(ApiRequest.GET, "/photo/" + photoId + "/view.json");
        if (returnSize != null) {
            request.addParameter("returnSizes", returnSize.toString());
        }
        ApiResponse response = execute(request);
        return new PhotoResponse(new JSONObject(response.getContentAsString()));
    }

    /*
     * (non-Javadoc)
     * @see
     * me.openphoto.android.app.net.IOpenPhotoApi#getOAuthUrl(java.lang.String)
     */
    @Override
    public String getOAuthUrl(String name, String callback) {
        return getBaseUrl() + "/v1/oauth/authorize?mobile=1&name=" + name
                + "&oauth_callback=" + callback;
    }

    /*
     * (non-Javadoc)
     * @see me.openphoto.android.app.net.IOpenPhotoApi#getPhotos()
     */
    @Override
    public PhotosResponse getPhotos()
            throws ClientProtocolException, IllegalStateException, IOException, JSONException {
        return getPhotos(null, null, null);
    }

    /*
     * (non-Javadoc)
     * @see
     * me.openphoto.android.app.net.IOpenPhotoApi#getPhotos(me.openphoto.android
     * .app.net.ReturnSize)
     */
    @Override
    public PhotosResponse getPhotos(ReturnSize resize)
            throws ClientProtocolException, IllegalStateException, IOException, JSONException {
        return getPhotos(resize, null, null);
    }

    /*
     * (non-Javadoc)
     * @see
     * me.openphoto.android.app.net.IOpenPhotoApi#getPhotos(me.openphoto.android
     * .app.net.ReturnSize, int)
     */
    @Override
    public PhotosResponse getPhotos(ReturnSize resize, Paging paging)
            throws ClientProtocolException, IllegalStateException, IOException, JSONException {
        return getPhotos(resize, null, paging);
    }

    /*
     * (non-Javadoc)
     * @see
     * me.openphoto.android.app.net.IOpenPhotoApi#getPhotos(me.openphoto.android
     * .app.net.ReturnSize, java.util.Collection)
     */
    @Override
    public PhotosResponse getPhotos(ReturnSize resize, Collection<String> tags)
            throws ClientProtocolException, IllegalStateException, IOException, JSONException {
        return getPhotos(resize, tags, null);
    }

    /*
     * (non-Javadoc)
     * @see
     * me.openphoto.android.app.net.IOpenPhotoApi#getPhotos(me.openphoto.android
     * .app.net.ReturnSize, java.util.Collection,
     * me.openphoto.android.app.net.Paging)
     */
    @Override
    public PhotosResponse getPhotos(ReturnSize resize, Collection<String> tags, Paging paging)
            throws ClientProtocolException, IOException, IllegalStateException, JSONException {
        ApiRequest request = new ApiRequest(ApiRequest.GET, "/photos/list.json");
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
        if (paging != null) {
            if (paging.hasPage()) {
                request.addParameter("page", Integer.toString(paging.getPage()));
            }
            if (paging.hasPageSize()) {
                request.addParameter("pageSize", Integer.toString(paging.getPageSize()));
            }
        }
        ApiResponse response = execute(request);
        return new PhotosResponse(new JSONObject(response.getContentAsString()));
    }

    /*
     * (non-Javadoc)
     * @see me.openphoto.android.app.net.IOpenPhotoApi#uploadPhoto(java.io.File,
     * me.openphoto.android.app.net.UploadMetaData)
     */
    @Override
    public UploadResponse uploadPhoto(File imageFile, UploadMetaData metaData,
            ProgressListener progressListener)
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
        request.addParameter("permission", Integer.toString(metaData.getPermission()));

        request.addFileParameter("photo", imageFile);
        ApiResponse response = execute(request, progressListener);
        String responseString = response.getContentAsString();
        return new UploadResponse(new JSONObject(responseString));
    }

    /**
     * ONLY FOR TESTING! Will make the class return the given Mock when
     * accessing createInstance(..)
     * 
     * @param mock Mock to be used for OpenPhotoApi
     */
    private static void injectMock(IOpenPhotoApi mock) {
        OpenPhotoApi.sMock = mock;
    }
}
