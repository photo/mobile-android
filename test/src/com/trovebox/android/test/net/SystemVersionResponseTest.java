
package com.trovebox.android.test.net;

import org.json.JSONException;
import org.json.JSONObject;

import android.test.InstrumentationTestCase;

import com.trovebox.android.common.net.SystemVersionResponse;
import com.trovebox.android.test.R;

public class SystemVersionResponseTest extends InstrumentationTestCase {
    public void testResponse() throws JSONException {
        JSONObject json = JSONUtils.getJson(getInstrumentation().getContext(),
                R.raw.json_system_version_response);
        SystemVersionResponse response = new SystemVersionResponse(json);
        assertNotNull(response);
        assertEquals("System versions", response.getMessage());
        assertNotNull(response.getApi());
        assertEquals("v2", response.getApi());
        assertNotNull(response.getDatabase());
        assertEquals("2.0.0", response.getDatabase());
        assertNotNull(response.getDatabaseType());
        assertTrue(response.getDatabaseType().length == 1);
        assertEquals("mysql", response.getDatabaseType()[0]);
        assertNotNull(response.getFilesystem());
        assertEquals("0.0.0", response.getFilesystem());
        assertNotNull(response.getFilesystemType());
        assertTrue(response.getFilesystemType().length == 1);
        assertEquals("s3", response.getFilesystemType()[0]);
        assertTrue(response.isHosted());
        assertEquals("1.3.3", response.getSystem());
    }

    public void testResponse2() throws JSONException {
        JSONObject json = JSONUtils.getJson(getInstrumentation().getContext(),
                R.raw.json_system_version_response2);
        SystemVersionResponse response = new SystemVersionResponse(json);
        assertNotNull(response);
        assertEquals("System versions", response.getMessage());
        assertNotNull(response.getApi());
        assertEquals("v1", response.getApi());
        assertNotNull(response.getDatabase());
        assertEquals("3.0.8", response.getDatabase());
        assertNotNull(response.getDatabaseType());
        assertTrue(response.getDatabaseType().length == 1);
        assertEquals("mysql", response.getDatabaseType()[0]);
        assertNotNull(response.getFilesystem());
        assertEquals("0.0.0", response.getFilesystem());
        assertNotNull(response.getFilesystemType());
        assertTrue(response.getFilesystemType().length == 1);
        assertEquals("local", response.getFilesystemType()[0]);
        assertFalse(response.isHosted());
        assertEquals("3.0.6", response.getSystem());
    }
}
