
package me.openphoto.android.test.net;

import java.io.IOException;
import java.io.InputStream;

import me.openphoto.android.app.net.OpenPhotoApi;
import me.openphoto.android.app.net.PhotoResponse;
import me.openphoto.android.app.net.PhotosResponse;
import me.openphoto.android.app.net.UploadMetaData;
import me.openphoto.android.test.R;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;

import android.test.InstrumentationTestCase;

public class OpenPhotoApiTest extends InstrumentationTestCase {

    private OpenPhotoApi mApi;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mApi = new OpenPhotoApi(OpenPhotoApiConstants.OPENPHOTO_BASE_URI);
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

    public void testPhotoUpload() {
        InputStream imageStream = getInstrumentation().getContext().getResources()
                .openRawResource(R.raw.android);

        UploadMetaData settings = new UploadMetaData();
        settings.setTitle("Android");
        settings.setDescription("Nice picture of an android");
        settings.setTags("test");
        try {
            PhotoResponse resp = mApi.uploadPhoto(imageStream, settings);
            assertEquals(202, resp.getCode());
            assertNotNull(resp.getPhoto());
            assertEquals(1, resp.getPhoto().getTags().size());
            assertEquals("test", resp.getPhoto().getTags().get(0));
            assertEquals("Android", resp.getPhoto().getTitle());
            assertEquals("Nice picture of an android", resp.getPhoto().getDescription());
        } catch (Exception e) {
            fail("Exception should not happen: " + e.getClass().getSimpleName() + " - "
                    + e.getMessage());
        }
    }
}
