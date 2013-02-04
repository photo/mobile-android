
package com.trovebox.android.test.net;

import org.json.JSONException;
import org.json.JSONObject;

import android.test.InstrumentationTestCase;

import com.trovebox.android.app.net.PagedResponse;
import com.trovebox.android.app.net.TroveboxResponse.RequestType;
import com.trovebox.android.test.R;

public class PagedResponseTest extends InstrumentationTestCase {
    public void testFromJson() throws JSONException {
        JSONObject json = JSONUtils.getJson(getInstrumentation().getContext(),
                R.raw.json_paged_response);
        PagedResponse response = new PagedResponse(RequestType.UNKNOWN, json);

        assertEquals(200, response.getCode());
        assertEquals(24, response.getTotalRows());
        assertEquals(16, response.getPageSize());
        assertEquals(1, response.getCurrentPage());
        assertEquals(2, response.getTotalPages());
    }
}
