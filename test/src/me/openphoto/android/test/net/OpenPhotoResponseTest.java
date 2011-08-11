
package me.openphoto.android.test.net;

import junit.framework.TestCase;
import me.openphoto.android.app.net.OpenPhotoResponse;

import org.json.JSONException;
import org.json.JSONObject;

public class OpenPhotoResponseTest extends TestCase {
    private static final String json = "{'message':'Success', 'code':200, 'result':{ 'data':'data' } }";

    public void testGeneral() throws JSONException {
        OpenPhotoResponse response = new OpenPhotoResponse(new JSONObject(json));
        assertEquals("Success", response.getMessage());
        assertEquals(200, response.getCode());
    }
}
