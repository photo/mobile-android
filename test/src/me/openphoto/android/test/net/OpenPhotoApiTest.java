
package me.openphoto.android.test.net;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import me.openphoto.android.app.net.OpenPhotoApi;
import me.openphoto.android.app.net.PhotosResponse;
import me.openphoto.android.app.net.UploadMetaData;
import me.openphoto.android.app.net.UploadResponse;
import me.openphoto.android.test.R;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;

import android.os.Environment;
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

    public void testPhotoUpload() throws IOException {
        InputStream imageStream = getInstrumentation().getContext().getResources()
                .openRawResource(R.raw.android);
        String state = Environment.getExternalStorageState();
        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/data/me.openphoto.android");
        if (!dir.exists()) {
            assertTrue(dir.mkdirs());
        }
        File file = new File(dir, "test-android.jpg");
        // if (!file.exists()) {
        // file.createNewFile();
        // }
        writeToFile(imageStream, file);

        UploadMetaData settings = new UploadMetaData();
        settings.setTitle("Android");
        settings.setDescription("Nice picture of an android");
        settings.setTags("test");
        try {
            UploadResponse resp = mApi.uploadPhoto(file, settings);
            assertTrue(resp.isSuccess());
            assertNotNull(resp.getPhoto());
            assertEquals(1, resp.getPhoto().getTags().size());
            assertEquals("test", resp.getPhoto().getTags().get(0));
            assertEquals("Android", resp.getPhoto().getTitle());
            assertEquals("Nice picture of an android", resp.getPhoto().getDescription());
        } catch (Exception e) {
            fail("Exception should not happen: " + e.getClass().getSimpleName() + " - "
                    + e.getMessage());
        }
        file.delete();
    }

    public void writeToFile(InputStream inputStream, File file) {
        try {
            FileOutputStream out = new FileOutputStream(file);
            byte buf[] = new byte[1024];
            int len;
            while ((len = inputStream.read(buf)) > 0)
                out.write(buf, 0, len);
            out.close();
            inputStream.close();
        } catch (IOException e) {
            fail("Could not write image to SD card: (" + e.getClass().getSimpleName() + ")"
                    + e.getMessage());
        }
    }
}
