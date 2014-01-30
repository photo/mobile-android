
package com.trovebox.android.common.net;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;

import android.content.Context;
import android.text.TextUtils;

import com.trovebox.android.common.CommonConfigurationUtils;
import com.trovebox.android.common.net.ApiRequest.ApiVersion;
import com.trovebox.android.common.net.HttpEntityWithProgress.ProgressListener;
import com.trovebox.android.common.net.TroveboxResponse.RequestType;

/**
 * TroveboxApi provides access to the acTrovebox API.
 * 
 * @author Patrick Boos
 */
public class TroveboxApi extends ApiBase implements ITroveboxApi {

    private static ITroveboxApi sMock;
    public static final String NEWEST_PHOTO_SORT_ORDER = "dateUploaded,DESC";

    public static ITroveboxApi createInstance() {
        if (sMock != null) {
            return sMock;
        } else {
            return new TroveboxApi();
        }
    }

    public TroveboxApi() {
        super();
    }

    @Override
    public TagsResponse getTags() throws ClientProtocolException, IOException,
            IllegalStateException, JSONException {
        ApiRequest request = new ApiRequest(ApiRequest.GET, "/tags/list.json");
        ApiResponse response = execute(request);
        return new TagsResponse(response.getJSONObject());
    }

    @Override
    public AlbumsResponse getAlbums() throws ClientProtocolException,
            IOException,
            IllegalStateException, JSONException
    {
        return getAlbums(null, true);
    }

    @Override
    public AlbumsResponse getAlbums(Paging paging, boolean skipEmpty)
            throws ClientProtocolException,
            IOException,
            IllegalStateException, JSONException
    {
        ApiRequest request = new ApiRequest(ApiRequest.GET, "/albums/list.json");
        addPagingRestrictions(paging, request);
        if (skipEmpty) {
            request.addParameter("skipEmpty", "1");
        }
        ApiResponse response = execute(request);
        return new AlbumsResponse(response.getJSONObject());
    }

    @Override
    public AlbumResponse getAlbum(String albumId) throws ClientProtocolException, IOException,
            IllegalStateException, JSONException {
        ApiRequest request = new ApiRequest(ApiRequest.GET, "/album/" + albumId + "/view.json");
        ApiResponse response = execute(request);
        return new AlbumResponse(RequestType.ALBUM, response.getJSONObject());
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
        return new PhotoResponse(RequestType.PHOTO, response.getJSONObject());
    }

    @Override
    public String getOAuthUrl(String name, String callback, Context context) {
        return CommonConfigurationUtils.getServer() + "/v1/oauth/authorize?mobile=1&name="
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
        return getPhotos(resize, null, null, null, null, paging);
    }

    @Override
    public PhotosResponse getPhotos(ReturnSizes resize,
            Collection<String> tags,
            String album)
            throws ClientProtocolException, IllegalStateException, IOException,
            JSONException {
        return getPhotos(resize, tags, album, null, null, null);
    }

    /*
     * (non-Javadoc)
     * @see
     * com.trovebox.android.app.net.ITroveboxApi#getPhotos(com.trovebox.android
     * .app.net.ReturnSize, java.util.Collection,
     * com.trovebox.android.app.net.Paging)
     */
    @Override
    public PhotosResponse getPhotos(ReturnSizes resize,
            Collection<String> tags,
            String album,
            String token,
            String sortBy, Paging paging)
            throws ClientProtocolException, IOException, IllegalStateException,
            JSONException {
        return getPhotos(resize, tags, album, token, null, sortBy, paging);
    }

    public PhotosResponse getPhotos(ReturnSizes resize, Collection<String> tags, String album,
            String token, String hash, String sortBy, Paging paging)
            throws ClientProtocolException, IOException, IllegalStateException, JSONException {
        ApiRequest request;
        StringBuilder path = new StringBuilder();
        if (album != null && album.length() > 0) {
            path.append("/photos/album-" + album);
        } else {
            path.append("/photos");
        }
        if (!TextUtils.isEmpty(token)) {
            path.append("/token-" + token);
        }
        path.append("/list.json");
        request = new ApiRequest(ApiRequest.GET, path.toString());
        if (hash != null) {
            request.addParameter("hash", hash);
        }
        if (sortBy != null) {
            request.addParameter("sortBy", sortBy);
        }
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
        addPagingRestrictions(paging, request);
        ApiResponse response = execute(request);
        return new PhotosResponse(response.getJSONObject());
    }

    /**
     * Add paging restrictions to the request if paging parameter is not null
     * 
     * @param paging
     * @param request
     */
    public void addPagingRestrictions(Paging paging, ApiRequest request) {
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
    }

    /*
     * (non-Javadoc)
     * @see com.trovebox.android.app.net.ITroveboxApi#uploadPhoto(java.io.File,
     * com.trovebox.android.app.net.UploadMetaData,
     * com.trovebox.android.app.net.HttpEntityWithProgress.ProgressListener)
     */
    @Override
    public UploadResponse uploadPhoto(File imageFile, UploadMetaData metaData,
            ProgressListener progressListener) throws ClientProtocolException, IOException,
            IllegalStateException, JSONException {
        return uploadPhoto(imageFile, metaData, null, null, progressListener);
    }
    /*
     * (non-Javadoc)
     * @see com.trovebox.android.app.net.ITroveboxApi#uploadPhoto(java.io.File,
     * com.trovebox.android.app.net.UploadMetaData)
     */
    @Override
    public UploadResponse uploadPhoto(File imageFile, UploadMetaData metaData, String token,
            String host, ProgressListener progressListener) throws ClientProtocolException,
            IOException, IllegalStateException, JSONException {
        ApiRequest request = new ApiRequest(ApiRequest.POST, "/photo/upload.json");
        request.setMime(true);
        if (!TextUtils.isEmpty(token)) {
            request.addParameter("token", token);
        }
        if (metaData.getTags() != null) {
            request.addParameter("tags", metaData.getTags());
        }
        if (metaData.getAlbums() != null)
        {
            request.addParameter("albums", UploadMetaDataUtils.getAlbumIds(metaData));
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
        ApiResponse response = TextUtils.isEmpty(host) ? 
                execute(request, progressListener, NetworkConnectionTimeout_ms * 12): 
                execute(request, host, progressListener, NetworkConnectionTimeout_ms * 12);
        return new UploadResponse(response.getJSONObject());
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
        return getPhotos(returnSize, null, null, null, NEWEST_PHOTO_SORT_ORDER, paging);
    }

    /**
     * ONLY FOR TESTING! Will make the class return the given Mock when
     * accessing createInstance(..)
     * 
     * @param mock Mock to be used for TroveboxApi
     */
    public static void injectMock(ITroveboxApi mock) {
        TroveboxApi.sMock = mock;
    }

    @Override
    public PhotosResponse getPhotos(String hash)
            throws ClientProtocolException,
            IOException, IllegalStateException,
            JSONException
    {
        return getPhotos(null, null, null, null, hash, null, null);
    }

    @Override
    public TroveboxResponse deletePhoto(String photoId) throws ClientProtocolException,
            IOException, IllegalStateException, JSONException
    {
        ApiRequest request = new ApiRequest(ApiRequest.POST, "/photo/" + photoId
                + "/delete.json");
        ApiResponse response = execute(request);
        return new TroveboxResponse(RequestType.DELETE_PHOTO, response.getJSONObject());
    }

    @Override
    public PhotoResponse updatePhotoDetails(
            String photoId, String title, String description,
            Collection<String> tags, Integer permission) throws ClientProtocolException,
            IOException, IllegalStateException, JSONException {
        ApiRequest request = new ApiRequest(ApiRequest.POST, "/photo/" + photoId
                + "/update.json");
        if (title != null) {
            request.addParameter("title", title);
        }
        if (description != null) {
            request.addParameter("description", description);
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
        if (permission != null)
        {
            request.addParameter("permission",
                    Integer.toString(permission));
        }
        ApiResponse response = execute(request);
        return new PhotoResponse(RequestType.UPDATE_PHOTO, response.getJSONObject());
    }

    @Override
    public ProfileResponse getProfile(boolean includeViewer) throws ClientProtocolException,
            IOException,
            IllegalStateException, JSONException {
        ApiRequest request = new ApiRequest(ApiRequest.GET, "/user/profile.json");
        if (includeViewer) {
            request.addParameter("includeViewer", "1");
        }
        ApiResponse response = execute(request);
        return new ProfileResponse(response.getJSONObject());
    }

    @Override
    public SystemVersionResponse getSystemVersion() throws ClientProtocolException, IOException,
            IllegalStateException, JSONException {
        ApiRequest request = new ApiRequest(ApiRequest.GET,
                "/system/version.json");
        ApiResponse response = execute(request);
        return new SystemVersionResponse(response.getJSONObject());
    }

    @Override
    public AlbumResponse createAlbum(String name) throws ClientProtocolException, IOException,
            IllegalStateException, JSONException {
        ApiRequest request = new ApiRequest(ApiRequest.POST,
                "/album/create.json");
        request.addParameter("name", name);
        ApiResponse response = execute(request);
        return new AlbumResponse(RequestType.CREATE_ALBUM, response.getJSONObject());
    }

    @Override
    public TokenResponse createTokenForPhoto(String photoId) throws ClientProtocolException,
            IOException, IllegalStateException, JSONException {

        ApiRequest request = new ApiRequest(ApiRequest.POST,
                "/token/photo/" + photoId + "/create.json");
        ApiResponse response = execute(request);
        return new TokenResponse(RequestType.CREATE_PHOTO_TOKEN,
                response.getJSONObject());
    }

    @Override
    public TokenValidationResponse validateUploadToken(String token)
            throws ClientProtocolException, IOException, IllegalStateException, JSONException {
        ApiRequest request = new ApiRequest(ApiRequest.GET, "/c/" + token + ".json",
                ApiVersion.NO_VERSION);
        ApiResponse response = execute(request);
        return new TokenValidationResponse(response.getJSONObject());
    }

    @Override
    public TroveboxResponse notifyUploadFinished(String token, String host, String uploader,
            int count) throws ClientProtocolException, IOException, IllegalStateException,
            JSONException {
        ApiRequest request = new ApiRequest(ApiRequest.POST, "/photos/upload/" + token
                + "/notify.json");
        if (!TextUtils.isEmpty(uploader)) {
            request.addParameter("uploader", uploader);
        }
        request.addParameter("count", Integer.toString(count));
        ApiResponse response = TextUtils.isEmpty(host) ? execute(request) : execute(request, host);
        return new TroveboxResponse(RequestType.NOTIFY_UPLOAD_DONE, response.getJSONObject());
    }
}
