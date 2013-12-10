
package com.trovebox.android.test.model;

import com.trovebox.android.test.R;

import org.json.JSONException;
import org.json.JSONObject;

import com.trovebox.android.common.model.Photo;
import com.trovebox.android.test.net.JSONUtils;

import android.test.InstrumentationTestCase;

public class PhotoTest extends InstrumentationTestCase {
    public void testFromJson() {
        Photo photo;
        try {
            JSONObject json = JSONUtils
                    .getJson(getInstrumentation().getContext(), R.raw.json_photo);
            photo = Photo.fromJson(json);
        } catch (JSONException e) {
            throw new AssertionError("This exception should not be thrown!");
        }

        assertNotNull(photo);
        assertEquals("4t", photo.getId());
        assertNotNull(photo.getTags());
        assertTrue(photo.isPrivate());
        assertEquals(2, photo.getTags().size());
        assertEquals("sunnyvale", photo.getTags().get(0));
        assertEquals("tavin", photo.getTags().get(1));
        assertEquals("Tavin riding the horsey at Murphy Park in Sunnyvale",
                photo.getTitle());
        assertEquals("Nice description", photo.getDescription());
        assertEquals("current.trovebox.com", photo.getAppId());
        assertEquals(
                "http://opmecurrent.s3.amazonaws.com/base/201108/1312348300-IMAG0015.jpg",
                photo.getUrl("base"));
        assertEquals(
                "http://opmecurrent.s3.amazonaws.com/original/201108/1312348300-IMAG0015.jpg",
                photo.getUrl("original"));
        assertEquals(
                "http://opmecurrent.s3.amazonaws.com/custom/201108/1312348300-IMAG0015_960x960.jpg",
                photo.getUrl("960x960"));
        assertEquals(
                "http://opmecurrent.s3.amazonaws.com/custom/201108/1312348300-IMAG0015_50x50xCR.jpg",
                photo.getUrl("50x50xCR"));
    }
}
