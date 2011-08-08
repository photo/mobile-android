
package me.openphoto.android.test.net;

import junit.framework.TestCase;
import me.openphoto.android.app.net.ApiRequest;

public class ApiRequestTest extends TestCase {
    public void testBasicConstruction() {
        ApiRequest request = new ApiRequest(ApiRequest.GET, "/");
        assertEquals(ApiRequest.GET, request.getMethod());
        assertEquals("/", request.getPath());

        request = new ApiRequest(ApiRequest.DELETE, "/photos");
        assertEquals(ApiRequest.DELETE, request.getMethod());
        assertEquals("/photos", request.getPath());
    }

    public void testMustStartWithSlash() {
        boolean exceptionThrown = false;
        try {
            new ApiRequest(ApiRequest.GET, "photos");
        } catch (IllegalArgumentException e) {
            exceptionThrown = true;
        }
        assertTrue("IllegalArgumentException mus be thrown if not starting with a '/'",
                exceptionThrown);
    }
}
