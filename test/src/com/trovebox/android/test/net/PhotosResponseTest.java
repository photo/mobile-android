
package com.trovebox.android.test.net;

import com.trovebox.android.test.R;

import org.json.JSONException;
import org.json.JSONObject;

import com.trovebox.android.app.net.PhotosResponse;

import android.test.InstrumentationTestCase;

public class PhotosResponseTest extends InstrumentationTestCase {
    public void testResponse() throws JSONException {
        JSONObject json = JSONUtils.getJson(getInstrumentation().getContext(),
                R.raw.json_photos_get);
        PhotosResponse response = new PhotosResponse(json);
        assertNotNull(response);
        assertEquals(2, response.getTotalRows());
        assertNotNull(response.getPhotos());
        assertEquals(2, response.getPhotos().size());
        assertEquals("hl", response.getPhotos().get(0).getId());
        assertEquals("ob", response.getPhotos().get(1).getId());
    }
}
