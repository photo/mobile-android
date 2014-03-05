
package com.trovebox.android.test.net;

import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import android.test.InstrumentationTestCase;

import com.trovebox.android.common.model.ProfileInformation;
import com.trovebox.android.common.model.ProfileInformation.ProfileCounters;
import com.trovebox.android.common.model.ProfileInformation.ProfileLimits;
import com.trovebox.android.common.net.ProfileResponse;
import com.trovebox.android.test.R;
import com.trovebox.android.test.model.ProfileInformationTest;

public class ProfileResponseTest extends InstrumentationTestCase {
    public void testResponse() throws JSONException {
        JSONObject json = JSONUtils.getJson(getInstrumentation().getContext(),
                R.raw.json_profile_response);
        ProfileResponse response = new ProfileResponse(json);
        assertNotNull(response);
        assertEquals(200, response.getCode());
        ProfileInformation pi = response.getProfileInformation();
        assertNotNull(pi);
        assertNotNull(pi.getId());
        assertEquals("current.trovebox.com", pi.getId());
        assertNotNull(pi.getEmail());
        assertEquals("test@trovebox.com", pi.getEmail());
        assertNotNull(pi.getName());
        assertEquals("Trovebox User", pi.getName());
        assertNotNull(pi.getPhotoUrl());
        assertEquals("http://www.gravatar.com/avatar/12f2ed3922ab37da6e4cf082b31e63b6?s=100",
                pi.getPhotoUrl());
        assertTrue(pi.isOwner());
        assertFalse(pi.isPaid());
        ProfileCounters counters = pi.getCounters();
        assertNotNull(counters);
        assertEquals(7, counters.getAlbums());
        assertEquals(134, counters.getPhotos());
        assertEquals(26, counters.getTags());
        assertEquals(24720384, counters.getStorage());
        ProfileLimits limits = pi.getLimits();
        assertNotNull(limits);
        assertEquals(62, limits.getRemaining());
        assertEquals(17, limits.getResetsInDays());
        assertEquals(new Date(1362124800l), limits.getResetsOn());
    }

    public void testResponseWithViewer() throws JSONException {
        JSONObject json = JSONUtils.getJson(getInstrumentation().getContext(),
                R.raw.json_profile_response_with_viewer);
        ProfileResponse response = new ProfileResponse(json);
        assertNotNull(response);
        assertEquals(200, response.getCode());
        ProfileInformation pi = response.getProfileInformation();
        ProfileInformationTest.checkProfileInformation3(pi, json.getJSONObject("result")
                .getJSONObject("viewer").getJSONObject("permission").toString());

    }
}
