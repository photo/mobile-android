package com.trovebox.android.test.net;

import org.json.JSONException;
import org.json.JSONObject;

import android.test.InstrumentationTestCase;

import com.trovebox.android.common.net.TokenValidationResponse;
import com.trovebox.android.test.R;

public class TokenValidationResponseTest extends InstrumentationTestCase {
    public void testResponse() throws JSONException {
        JSONObject json = JSONUtils.getJson(getInstrumentation().getContext(),
                R.raw.json_token_validation_response);
        TokenValidationResponse response = new TokenValidationResponse(json);
        assertNotNull(response);
        assertEquals("test.trovebox.com", response.getHost());
        assertEquals("8f89059322", response.getId());
        assertEquals("hello+test@openphoto.me", response.getOwner());
        assertEquals("upload", response.getType());
        assertEquals("f", response.getData());
    }
}
