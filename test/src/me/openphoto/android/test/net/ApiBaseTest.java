
package me.openphoto.android.test.net;

import junit.framework.TestCase;
import me.openphoto.android.app.net.ApiBase;

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
}
