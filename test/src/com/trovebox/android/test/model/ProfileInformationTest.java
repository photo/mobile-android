
package com.trovebox.android.test.model;

import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.test.InstrumentationTestCase;

import com.trovebox.android.app.model.ProfileInformation;
import com.trovebox.android.app.model.ProfileInformation.AccessPermissions;
import com.trovebox.android.app.model.ProfileInformation.ProfileCounters;
import com.trovebox.android.app.model.ProfileInformation.ProfileLimits;
import com.trovebox.android.test.R;
import com.trovebox.android.test.net.JSONUtils;

public class ProfileInformationTest extends InstrumentationTestCase {
    public void testFromJson() throws JSONException {
        ProfileInformation pi;
        JSONObject json = JSONUtils.getJson(getInstrumentation().getContext(),
                R.raw.json_profile_information);
        pi = ProfileInformation.fromJson(json);
        checkProfileInformation1(pi, json.getJSONObject("permission").toString());
    }

    public void testFromJson2() throws JSONException {
        ProfileInformation pi;
        JSONObject json = JSONUtils.getJson(getInstrumentation().getContext(),
                R.raw.json_profile_information2);
        pi = ProfileInformation.fromJson(json);
        checkProfileInformation2(pi, json.getJSONObject("permission").toString());
    }

    public void testProfileInformationParcelable() throws JSONException {
        ProfileInformation c;
        JSONObject json = JSONUtils.getJson(getInstrumentation().getContext(),
                R.raw.json_profile_information);
        c = ProfileInformation.fromJson(json);

        checkProfileInformation1(c, json.getJSONObject("permission").toString());

        Parcel parcel = Parcel.obtain();
        c.writeToParcel(parcel, 0);
        // done writing, now reset parcel for reading
        parcel.setDataPosition(0);
        // finish round trip
        ProfileInformation createFromParcel = ProfileInformation.CREATOR.createFromParcel(parcel);

        checkProfileInformation1(createFromParcel, json.getJSONObject("permission").toString());
    }

    public void testProfileInformation2Parcelable() throws JSONException {
        ProfileInformation c;
        JSONObject json = JSONUtils.getJson(getInstrumentation().getContext(),
                R.raw.json_profile_information2);
        c = ProfileInformation.fromJson(json);

        checkProfileInformation2(c, json.getJSONObject("permission").toString());

        Parcel parcel = Parcel.obtain();
        c.writeToParcel(parcel, 0);
        // done writing, now reset parcel for reading
        parcel.setDataPosition(0);
        // finish round trip
        ProfileInformation createFromParcel = ProfileInformation.CREATOR.createFromParcel(parcel);

        checkProfileInformation2(createFromParcel, json.getJSONObject("permission").toString());
    }

    public void testProfileInformation3Parcelable() throws JSONException {
        ProfileInformation c;
        JSONObject json = JSONUtils.getJson(getInstrumentation().getContext(),
                R.raw.json_profile_response_with_viewer);
        c = ProfileInformation.fromJson(json.getJSONObject("result"));

        checkProfileInformation3(c, json.getJSONObject("result").getJSONObject("viewer")
                .getJSONObject("permission").toString());

        Parcel parcel = Parcel.obtain();
        c.writeToParcel(parcel, 0);
        // done writing, now reset parcel for reading
        parcel.setDataPosition(0);
        // finish round trip
        ProfileInformation createFromParcel = ProfileInformation.CREATOR.createFromParcel(parcel);

        checkProfileInformation3(createFromParcel,
                json.getJSONObject("result").getJSONObject("viewer").getJSONObject("permission")
                        .toString());
    }

    public static void checkProfileInformation1(ProfileInformation pi, String permissionsJson) {
        assertNotNull(pi);
        assertEquals(pi.getEmail(), "hello+test@openphoto.me");
        assertEquals(pi.getId(), "test.trovebox.com");
        assertTrue(pi.isOwner());
        assertEquals(pi.getName(), "Trovebox Test Name");
        assertTrue(pi.isPaid());
        assertEquals(pi.getPhotoUrl(),
                "http://d1odebs29o9vbg.cloudfront.net/custom/196912/27005_original-526fb8_100x100xCR.jpg");

        ProfileCounters counters = pi.getCounters();
        assertNotNull(counters);
        assertEquals(counters.getAlbums(), 27);
        assertEquals(counters.getPhotos(), 397);
        assertEquals(counters.getTags(), 141);
        assertEquals(counters.getStorage(), 316116992);

        ProfileLimits limits = pi.getLimits();
        assertNotNull(limits);
        assertEquals(limits.getRemaining(), 2147483647);
        assertEquals(limits.getResetsInDays(), 25);
        assertEquals(limits.getResetsOn(), new Date(1383289200));

        AccessPermissions permissions = pi.getPermissions();
        assertNotNull(permissions);
        assertTrue(permissions.isFullCreateAccess());
        assertNull(permissions.getCreateAlbumAccessIds());
        assertTrue(permissions.isFullReadAccess());
        assertNull(permissions.getReadAlbumAccessIds());
        assertTrue(permissions.isFullUpdateAccess());
        assertNull(permissions.getUpdateAlbumAccessIds());
        assertTrue(permissions.isFullDeleteAccess());
        assertNull(permissions.getDeleteAlbumAccessIds());
        assertEquals(permissions.toJsonString(), permissionsJson);
    }

    public static void checkProfileInformation2(ProfileInformation pi, String permissionsJson) {
        assertNotNull(pi);
        assertEquals(pi.getEmail(), "");
        assertEquals(pi.getId(), "test3.trovebox.com");
        assertFalse(pi.isOwner());
        assertEquals(pi.getName(), " Demo User");
        assertFalse(pi.isPaid());
        assertEquals(pi.getPhotoUrl(),
                "http://awesomeness.openphoto.me/custom/201203/62f010-Boracay-Philippines-033_100x100xCR.jpg");

        ProfileCounters counters = pi.getCounters();
        assertNotNull(counters);
        assertEquals(counters.getAlbums(), 5);
        assertEquals(counters.getPhotos(), 363);
        assertEquals(counters.getTags(), 37);
        assertEquals(counters.getStorage(), 0);

        ProfileLimits limits = pi.getLimits();
        assertNull(limits);

        AccessPermissions permissions = pi.getPermissions();
        assertNotNull(permissions);
        assertFalse(permissions.isFullCreateAccess());
        assertNotNull(permissions.getCreateAlbumAccessIds());
        assertTrue(permissions.getCreateAlbumAccessIds().length == 1);
        assertEquals(permissions.getCreateAlbumAccessIds()[0], "4");
        assertFalse(permissions.isFullReadAccess());
        assertNotNull(permissions.getReadAlbumAccessIds());
        assertTrue(permissions.getReadAlbumAccessIds().length == 2);
        assertEquals(permissions.getReadAlbumAccessIds()[0], "5");
        assertEquals(permissions.getReadAlbumAccessIds()[1], "4");
        assertFalse(permissions.isFullUpdateAccess());
        assertNull(permissions.getUpdateAlbumAccessIds());
        assertFalse(permissions.isFullDeleteAccess());
        assertNull(permissions.getDeleteAlbumAccessIds());
        assertEquals(permissions.toJsonString(), permissionsJson);

    }

    public static void checkProfileInformation3(ProfileInformation pi, String permissionsJson) {
        assertNotNull(pi);
        assertEquals(pi.getEmail(), "");
        assertEquals(pi.getId(), "current.trovebox.com");
        assertFalse(pi.isOwner());
        assertEquals(pi.getName(), " Demo User");
        assertFalse(pi.isPaid());
        assertEquals(
                pi.getPhotoUrl(),
                "http://awesomeness.openphoto.me/custom/201203/62f010-Boracay-Philippines-033_100x100xCR.jpg");

        ProfileCounters counters = pi.getCounters();
        assertNotNull(counters);
        assertEquals(counters.getAlbums(), 5);
        assertEquals(counters.getPhotos(), 363);
        assertEquals(counters.getTags(), 37);
        assertEquals(counters.getStorage(), 0);

        ProfileLimits limits = pi.getLimits();
        assertNull(limits);

        AccessPermissions permissions = pi.getPermissions();
        assertNull(permissions);

        ProfileInformation viewer = pi.getViewer();
        assertNotNull(viewer);
        permissions = viewer.getPermissions();
        assertNotNull(permissions);
        assertFalse(permissions.isFullCreateAccess());
        assertNotNull(permissions.getCreateAlbumAccessIds());
        assertTrue(permissions.getCreateAlbumAccessIds().length == 1);
        assertEquals(permissions.getCreateAlbumAccessIds()[0], "4");
        assertFalse(permissions.isFullReadAccess());
        assertNotNull(permissions.getReadAlbumAccessIds());
        assertTrue(permissions.getReadAlbumAccessIds().length == 1);
        assertEquals(permissions.getReadAlbumAccessIds()[0], "4");
        assertFalse(permissions.isFullUpdateAccess());
        assertNotNull(permissions.getUpdateAlbumAccessIds());
        assertTrue(permissions.getUpdateAlbumAccessIds().length == 0);
        assertFalse(permissions.isFullDeleteAccess());
        assertNotNull(permissions.getDeleteAlbumAccessIds());
        assertTrue(permissions.getDeleteAlbumAccessIds().length == 0);
        assertEquals(permissions.toJsonString(), permissionsJson);

        assertEquals(viewer.getEmail(), "");
        assertEquals(viewer.getId(), "hello+test@openphoto.me");
        assertFalse(viewer.isOwner());
        assertEquals(viewer.getName(), "Trovebox Test Name");
        assertFalse(viewer.isPaid());
        assertEquals(
                viewer.getPhotoUrl(),
                "http://d1odebs29o9vbg.cloudfront.net/custom/196912/27005_original-526fb8_100x100xCR.jpg");

        counters = viewer.getCounters();
        assertNull(counters);

        limits = viewer.getLimits();
        assertNull(limits);

    }
}
