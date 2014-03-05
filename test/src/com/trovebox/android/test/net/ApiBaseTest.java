
package com.trovebox.android.test.net;

import java.io.IOException;

import junit.framework.TestCase;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;
import org.json.JSONObject;

import com.trovebox.android.common.net.ApiBase;
import com.trovebox.android.common.net.ApiRequest;
import com.trovebox.android.common.net.ApiResponse;

public class ApiBaseTest extends TestCase {
    // public void testConstructorUrl() {
    // ApiBase api = new ApiBase(TroveboxApiConstants.OPENPHOTO_BASE_URI);
    // assertEquals(TroveboxApiConstants.OPENPHOTO_BASE_URI, api.getBaseUrl());
    // }
    //
    // public void testUrlEndsWithSlash() {
    // ApiBase api = new ApiBase(TroveboxApiConstants.OPENPHOTO_BASE_URI +
    // "/");
    // assertEquals("Even if given a slash at the end, this should be removed",
    // TroveboxApiConstants.OPENPHOTO_BASE_URI, api.getBaseUrl());
    // }

    public void testBasicRequest() throws ClientProtocolException, IOException, JSONException {
        ApiBase api = new ApiBase();
        ApiRequest request = new ApiRequest(ApiRequest.GET, "/photos/list.json");

        ApiResponse response = api.execute(request,
                TroveboxApiConstants.OPENPHOTO_BASE_URI, null, null);
        assertEquals(200, response.getStatusCode());

        String responseString = response.getContentAsString();
        assertNotNull(responseString);
        JSONObject json = new JSONObject(responseString);
        assertTrue(json.has("message"));
    }
}
