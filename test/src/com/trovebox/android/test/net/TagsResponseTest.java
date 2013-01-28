
package com.trovebox.android.test.net;

import com.trovebox.android.test.R;

import org.json.JSONException;
import org.json.JSONObject;

import com.trovebox.android.app.net.TagsResponse;

import android.test.InstrumentationTestCase;

public class TagsResponseTest extends InstrumentationTestCase {
    public void testResponse() throws JSONException {
        JSONObject json = JSONUtils.getJson(getInstrumentation().getContext(),
                R.raw.json_tags_list);
        TagsResponse response = new TagsResponse(json);
        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertNotNull(response.getTags());
        assertEquals(4, response.getTags().size());
        assertEquals("anna", response.getTags().get(1).getTag());
        assertEquals(4, response.getTags().get(1).getCount());
        assertEquals("autoupload", response.getTags().get(2).getTag());
        assertEquals(42, response.getTags().get(2).getCount());
    }
}
