
package com.trovebox.android.test.net;

import com.trovebox.android.test.R;

import org.json.JSONException;
import org.json.JSONObject;

import com.trovebox.android.common.net.UploadResponse;

import android.test.InstrumentationTestCase;

public class UploadResponseTest extends InstrumentationTestCase {
    public void testResponse() throws JSONException {
        JSONObject json = JSONUtils.getJson(getInstrumentation().getContext(),
                R.raw.json_photo_upload);
        UploadResponse response = new UploadResponse(json);
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertNotNull(response.getPhoto());
    }
}
