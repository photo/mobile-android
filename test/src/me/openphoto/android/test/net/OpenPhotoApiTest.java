
package me.openphoto.android.test.net;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import me.openphoto.android.app.net.IOpenPhotoApi;
import me.openphoto.android.app.net.OpenPhotoApi;
import me.openphoto.android.app.net.PhotosResponse;
import me.openphoto.android.app.net.TagsResponse;
import me.openphoto.android.app.net.UploadMetaData;
import me.openphoto.android.app.net.UploadResponse;
import me.openphoto.android.test.R;
import me.openphoto.android.test.util.FileUtils;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;

import android.os.Environment;
import android.test.InstrumentationTestCase;

public class OpenPhotoApiTest extends InstrumentationTestCase {

    private IOpenPhotoApi mApi;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mApi = OpenPhotoApi.createInstance(OpenPhotoApiConstants.OPENPHOTO_BASE_URI);
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

    public void testPhotoUpload() throws IOException {
        InputStream imageStream = getInstrumentation().getContext().getResources()
                .openRawResource(R.raw.android);
        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/data/me.openphoto.android");
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
        settings.setPrivate(false);
        try {
            UploadResponse resp = mApi.uploadPhoto(file, settings);
            assertTrue(resp.isSuccess());
            assertNotNull(resp.getPhoto());
            assertEquals(1, resp.getPhoto().getTags().size());
            assertEquals("test", resp.getPhoto().getTags().get(0));
            assertEquals("Android", resp.getPhoto().getTitle());
            assertEquals("Nice picture of an android", resp.getPhoto().getDescription());
            assertFalse(resp.getPhoto().isPrivate());
        } catch (Exception e) {
            fail("Exception should not happen: " + e.getClass().getSimpleName() + " - "
                    + e.getMessage());
        }
        file.delete();
    }
}
