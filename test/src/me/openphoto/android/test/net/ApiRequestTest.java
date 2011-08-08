
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

    public void testParametersNeverNull() {
        ApiRequest request = new ApiRequest(ApiRequest.GET, "/");
        assertNotNull(request.getParameters());
    }

    public void testAddParameter() {
        ApiRequest request = new ApiRequest(ApiRequest.GET, "/");
        request.addParameter("parameterName", "parameterValue");
        assertEquals(request.getParameters().size(), 1);
        assertEquals(request.getParameters().get(0).getName(), "parameterName");
        assertEquals(request.getParameters().get(0).getValue(), "parameterValue");
    }

    public void testHeadersNeverNull() {
        ApiRequest request = new ApiRequest(ApiRequest.GET, "/");
        assertNotNull(request.getHeaders());
    }

    public void testAddHeader() {
        ApiRequest request = new ApiRequest(ApiRequest.GET, "/");
        request.addHeader("headerName", "headerValue");
        assertEquals(request.getHeaders().size(), 1);
        assertEquals(request.getHeaders().get(0).getName(), "headerName");
        assertEquals(request.getHeaders().get(0).getValue(), "headerValue");
    }
}
