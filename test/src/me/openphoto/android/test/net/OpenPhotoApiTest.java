
package me.openphoto.android.test.net;

import java.io.IOException;

import junit.framework.TestCase;
import me.openphoto.android.app.net.OpenPhotoApi;
import me.openphoto.android.app.net.PhotosResponse;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;

public class OpenPhotoApiTest extends TestCase {
    public void testPhotos() throws ClientProtocolException, IllegalStateException, IOException,
            JSONException {
        OpenPhotoApi api = new OpenPhotoApi(OpenPhotoApiConstants.OPENPHOTO_BASE_URI);
        PhotosResponse resp = api.getPhotos();
        assertNotNull(resp);
        assertEquals(1, resp.getCurrentPage());
        assertNotSame(0, resp.getTotalRows());
        assertNotNull(resp.getPhotos());
        assertNotSame(0, resp.getPhotos().size());
    }

}
