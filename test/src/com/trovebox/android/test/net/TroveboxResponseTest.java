
package com.trovebox.android.test.net;

import org.json.JSONException;
import org.json.JSONObject;

import android.test.InstrumentationTestCase;

import com.trovebox.android.common.net.TroveboxResponse;
import com.trovebox.android.common.net.TroveboxResponse.RequestType;
import com.trovebox.android.test.R;

public class TroveboxResponseTest extends InstrumentationTestCase {

    public void testGeneral() throws JSONException {
        JSONObject json = JSONUtils.getJson(getInstrumentation().getContext(),
                R.raw.json_basic_response);
        TroveboxResponse response = new TroveboxResponse(RequestType.UNKNOWN, json);
        assertEquals("Success", response.getMessage());
        assertEquals(200, response.getCode());
    }
}
