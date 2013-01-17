
package me.openphoto.android.test.net;

import java.io.File;

import junit.framework.TestCase;
import me.openphoto.android.app.net.ApiRequest;
import android.os.Environment;

public class ApiRequestTest extends TestCase {
    public void testBasicConstruction() {
        ApiRequest request = new ApiRequest(ApiRequest.GET, "/");
        assertEquals(ApiRequest.GET, request.getMethod());
        assertEquals(request.getApiVersion().correctPathWithPrefix("/"), request.getPath());

        request = new ApiRequest(ApiRequest.DELETE, "/photos");
        assertEquals(ApiRequest.DELETE, request.getMethod());
        assertEquals(request.getApiVersion().correctPathWithPrefix("/photos"), request.getPath());
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

    public void testMime() {
        ApiRequest request = new ApiRequest(ApiRequest.GET, "/photo");
        boolean hadException = false;
        try {
            request.setMime(true);
        } catch (Exception e) {
            hadException = true;
        }
        assertTrue("Should have had exception because MIME not possible with GET", hadException);

        request = new ApiRequest(ApiRequest.POST, "/photo");
        assertFalse("Mime should be off by default", request.isMime());
        request.setMime(true);
        assertTrue(request.isMime());

        request.addParameter("mime1", "value1");
        try {
            request.addFileParameter("mime2", Environment.getDataDirectory());
        } catch (Exception e) {
            fail("Exception should not happen: " + e.getClass().getSimpleName() + " - "
                    + e.getMessage());
        }

        assertEquals(1, request.getParameters().size());
        assertEquals("value1", request.getParameters().get(0).getValue());

        assertEquals(2, request.getParametersMime().size());
        assertEquals("value1", (String) request.getParametersMime().get(0).getValue());
        assertTrue(request.getParametersMime().get(1).getValue() instanceof File);
    }
}
