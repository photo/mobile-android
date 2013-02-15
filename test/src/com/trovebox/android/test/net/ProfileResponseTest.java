
package com.trovebox.android.test.net;

import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import android.test.InstrumentationTestCase;

import com.trovebox.android.app.net.ProfileResponse;
import com.trovebox.android.app.net.ProfileResponse.ProfileCounters;
import com.trovebox.android.app.net.ProfileResponse.ProfileLimits;
import com.trovebox.android.test.R;

public class ProfileResponseTest extends InstrumentationTestCase {
    public void testResponse() throws JSONException {
        JSONObject json = JSONUtils.getJson(getInstrumentation().getContext(),
                R.raw.json_profile_response);
        ProfileResponse response = new ProfileResponse(json);
        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertNotNull(response.getId());
        assertEquals("current.trovebox.com", response.getId());
        assertNotNull(response.getEmail());
        assertEquals("test@trovebox.com", response.getEmail());
        assertNotNull(response.getName());
        assertEquals("Trovebox User", response.getName());
        assertNotNull(response.getPhotoUrl());
        assertEquals("http://www.gravatar.com/avatar/12f2ed3922ab37da6e4cf082b31e63b6?s=100",
                response.getPhotoUrl());
        assertTrue(response.isOwner());
        assertFalse(response.isPaid());
        ProfileCounters counters = response.getCounters();
        assertNotNull(counters);
        assertEquals(7, counters.getAlbums());
        assertEquals(134, counters.getPhotos());
        assertEquals(26, counters.getTags());
        assertEquals(24720384, counters.getStorage());
        ProfileLimits limits = response.getLimits();
        assertNotNull(limits);
        assertEquals(62, limits.getRemaining());
        assertEquals(17, limits.getResetsInDays());
        assertEquals(new Date(1362124800l), limits.getResetsOn());
    }
}
