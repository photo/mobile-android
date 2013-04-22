
package com.trovebox.android.test.net;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import android.test.ApplicationTestCase;

import com.trovebox.android.app.TroveboxApplication;
import com.trovebox.android.app.model.Album;
import com.trovebox.android.app.model.Photo;
import com.trovebox.android.app.net.AlbumResponse;
import com.trovebox.android.app.net.ITroveboxApi;
import com.trovebox.android.app.net.PhotoResponse;
import com.trovebox.android.app.net.PhotosResponse;
import com.trovebox.android.app.net.TagsResponse;
import com.trovebox.android.app.net.TroveboxApi;
import com.trovebox.android.app.net.UploadMetaData;
import com.trovebox.android.app.net.UploadResponse;
import com.trovebox.android.app.util.TrackerUtils;
import com.trovebox.android.test.util.FileUtils;

public class TroveboxApiTest extends ApplicationTestCase<TroveboxApplication>
{

    public TroveboxApiTest()
    {
        super(TroveboxApplication.class);
    }

    private ITroveboxApi mApi;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        TrackerUtils.SKIP_UNCAUGHT_SETUP = true;
        createApplication();
        mApi = TroveboxApi.createInstance(TroveboxApplication.getContext());
    }

    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
        TrackerUtils.SKIP_UNCAUGHT_SETUP = false;
    }

    public void testTags() throws ClientProtocolException, IllegalStateException, IOException,
            JSONException {
        TagsResponse resp = mApi.getTags();
        assertNotNull(resp);
        assertEquals(200, resp.getCode());
        assertNotNull(resp.getTags());
        assertNotSame(0, resp.getTags().size());
    }

    public void testPhotos() throws ClientProtocolException, IllegalStateException, IOException,
            JSONException {
        PhotosResponse resp = mApi.getPhotos();
        assertNotNull(resp);
        assertEquals(1, resp.getCurrentPage());
        assertNotSame(0, resp.getTotalRows());
        assertNotNull(resp.getPhotos());
        assertNotSame(0, resp.getPhotos().size());
    }

    public void testPhotoUpload() throws Exception
    {
        AssetManager assetMgr = getTestContext().getAssets();
        InputStream imageStream = assetMgr.open("android.jpg");
        // InputStream imageStream = TroveboxApplication.getContext()
        // .getResources()
        // .openRawResource(R.raw.android);
        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/data/com.trovebox.android");
        if (!dir.exists()) {
            assertTrue(dir.mkdirs());
        }
        File file = new File(dir, "test-android.jpg");
        // if (!file.exists()) {
        // file.createNewFile();
        // }
        FileUtils.writeToFile(imageStream, file);

        UploadMetaData settings = new UploadMetaData();
        settings.setTitle("Android");
        settings.setDescription("Nice picture of an android");
        settings.setTags("test");
        Map<String, String> albums = new HashMap<String, String>();
        albums.put("1", "test");
        albums.put("2", "test2");
        settings.setAlbums(albums);
        settings.setPrivate(false);
        try {
            UploadResponse resp = mApi.uploadPhoto(file, settings, null);
            assertTrue(resp.isSuccess());
            assertNotNull(resp.getPhoto());
            try
            {

                assertTrue(resp.getPhoto().getTags().size() >= 1);
                // assertEquals("test", resp.getPhoto().getTags().get(0));
                assertEquals("Android", resp.getPhoto().getTitle());
                assertEquals("Nice picture of an android", resp.getPhoto()
                        .getDescription());
                assertFalse(resp.getPhoto().isPrivate());
            } finally
            {
                // remove uploaded photo
                mApi.deletePhoto(resp.getPhoto().getId());
            }
        } catch (Exception e) {
            fail("Exception should not happen: " + e.getClass().getSimpleName() + " - "
                    + e.getMessage());
        }
        file.delete();
    }

    public void testPhotoUploadAndDetailsEdit() throws Exception
    {
        AssetManager assetMgr = getTestContext().getAssets();
        InputStream imageStream = assetMgr.open("android.jpg");
        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/data/com.trovebox.android");
        if (!dir.exists()) {
            assertTrue(dir.mkdirs());
        }
        File file = new File(dir, "test-android.jpg");
        FileUtils.writeToFile(imageStream, file);

        UploadMetaData settings = new UploadMetaData();
        String title = "Android";
        String description = "Nice picture of an android";
        String tags = "test";
        boolean priv = false;
        settings.setTitle(title);
        settings.setDescription(description);
        settings.setTags(tags);
        settings.setPrivate(priv);
        try {
            UploadResponse resp = mApi.uploadPhoto(file, settings, null);
            assertTrue(resp.isSuccess());
            assertNotNull(resp.getPhoto());
            try
            {
                Photo photo = resp.getPhoto();
                assertNotNull(photo);
                assertTrue(photo.getTags().size() >= 1);
                // assertEquals("test", resp.getPhoto().getTags().get(0));
                assertEquals(title, photo.getTitle());
                assertEquals(description, photo
                        .getDescription());
                assertFalse(photo.isPrivate());

                title = "Android (Edited)";
                description = "Nice picture of an android (Edited)";
                tags = "edited";
                Collection<String> tagsCollection = new ArrayList<String>();
                tagsCollection.add("edited");

                priv = true;

                PhotoResponse photoResp = mApi.updatePhotoDetails(photo.getId(), title,
                        description, tagsCollection, Photo.PERMISSION_PRIVATE);

                photo = photoResp.getPhoto();
                assertTrue(photoResp.isSuccess());
                assertNotNull(photo);
                assertTrue(photo.getTags().size() == 1);
                assertEquals(tags, photo.getTags().get(0));
                assertEquals(title, photo.getTitle());
                assertEquals(description, photo
                        .getDescription());
                assertTrue(photo.isPrivate() == priv);

            } finally
            {
                // remove uploaded photo
                mApi.deletePhoto(resp.getPhoto().getId());
            }
        } catch (Exception e) {
            fail("Exception should not happen: " + e.getClass().getSimpleName() + " - "
                    + e.getMessage());
        }
        file.delete();
    }

    public void testAlbumCreate() throws ClientProtocolException, IllegalStateException,
            IOException, JSONException
    {
        String albumName = "api_test";
        AlbumResponse resp = mApi.createAlbum(albumName);
        assertNotNull(resp);
        Album album = resp.getAlbum();
        assertNotNull(album);
        assertEquals(album.getName(), albumName);
        assertTrue(album.getId() != null && !album.getId().isEmpty());
        assertTrue(resp.isSuccess());
        assertEquals(201, resp.getCode());
    }
    private Context getTestContext() throws Exception
    {
        return (Context) getClass().getMethod("getTestContext").invoke(this);
    }
}
