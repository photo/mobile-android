
package me.openphoto.android.test.net;

import java.io.IOException;

import junit.framework.TestCase;
import me.openphoto.android.app.net.ApiBase;
import me.openphoto.android.app.net.ApiRequest;
import me.openphoto.android.app.net.ApiResponse;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;
import org.json.JSONObject;

public class ApiBaseTest extends TestCase {
    public void testConstructorUrl() {
        ApiBase api = new ApiBase(OpenPhotoApiConstants.OPENPHOTO_BASE_URI);
        assertEquals(OpenPhotoApiConstants.OPENPHOTO_BASE_URI, api.getApiUrl());
    }

    public void testUrlEndsWithSlash() {
        ApiBase api = new ApiBase(OpenPhotoApiConstants.OPENPHOTO_BASE_URI + "/");
        assertEquals("Even if given a slash at the end, this should be removed",
                OpenPhotoApiConstants.OPENPHOTO_BASE_URI, api.getApiUrl());
    }

    public void testBasicRequest() throws ClientProtocolException, IOException, JSONException {
        ApiBase api = new ApiBase(OpenPhotoApiConstants.OPENPHOTO_BASE_URI);
        ApiRequest request = new ApiRequest(ApiRequest.GET, "/photos.json");

        ApiResponse response = api.execute(request);
        assertEquals(200, response.getStatusCode());

        String responseString = response.getContentAsString();
        assertNotNull(responseString);
        JSONObject json = new JSONObject(responseString);
        assertTrue(json.has("message"));
    }
}
