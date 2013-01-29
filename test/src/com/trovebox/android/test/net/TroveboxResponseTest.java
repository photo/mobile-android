
package com.trovebox.android.test.net;

import com.trovebox.android.test.R;

import org.json.JSONException;
import org.json.JSONObject;

import com.trovebox.android.app.net.TroveboxResponse;

import android.test.InstrumentationTestCase;

public class TroveboxResponseTest extends InstrumentationTestCase {

    public void testGeneral() throws JSONException {
        JSONObject json = JSONUtils.getJson(getInstrumentation().getContext(),
                R.raw.json_basic_response);
        TroveboxResponse response = new TroveboxResponse(json);
        assertEquals("Success", response.getMessage());
        assertEquals(200, response.getCode());
    }
}
