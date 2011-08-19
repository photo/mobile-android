
package me.openphoto.android.test.net;

import me.openphoto.android.app.net.OpenPhotoResponse;
import me.openphoto.android.test.R;

import org.json.JSONException;
import org.json.JSONObject;

import android.test.InstrumentationTestCase;

public class OpenPhotoResponseTest extends InstrumentationTestCase {

    public void testGeneral() throws JSONException {
        JSONObject json = JSONUtils.getJson(getInstrumentation().getContext(),
                R.raw.json_basic_response);
        OpenPhotoResponse response = new OpenPhotoResponse(json);
        assertEquals("Success", response.getMessage());
        assertEquals(200, response.getCode());
    }
}
