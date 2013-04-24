
package com.trovebox.android.test.model;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.test.InstrumentationTestCase;

import com.trovebox.android.app.model.Album;
import com.trovebox.android.app.model.Photo;
import com.trovebox.android.test.R;
import com.trovebox.android.test.net.JSONUtils;

public class AlbumTest extends InstrumentationTestCase {
    public void testFromJson() {
        Album album;
        try {
            JSONObject json = JSONUtils
                    .getJson(getInstrumentation().getContext(), R.raw.json_album);
            album = Album.fromJson(json);
        } catch (JSONException e) {
            throw new AssertionError("This exception should not be thrown!");
        }

        testAlbumData(album);
    }

    public void testAlbumData(Album album) {
        assertNotNull(album);
        assertEquals("4", album.getId());
        assertEquals("hello@openphoto.me", album.getOwner());
        assertEquals("Beautiful Scenery", album.getName());
        assertEquals(0, album.getVisible());
        assertEquals(13, album.getCount());
        Photo cover = album.getCover();
        assertNotNull(cover);
        assertEquals("bd", cover.getId());
    }

    public void testAlbumParcelable()
    {
        Album album;
        try {
            JSONObject json = JSONUtils
                    .getJson(getInstrumentation().getContext(), R.raw.json_album);
            album = Album.fromJson(json);
        } catch (JSONException e) {
            throw new AssertionError("This exception should not be thrown!");
        }

        testAlbumData(album);

        Parcel parcel = Parcel.obtain();
        album.writeToParcel(parcel, 0);
        // done writing, now reset parcel for reading
        parcel.setDataPosition(0);
        // finish round trip
        Album createFromParcel = Album.CREATOR.createFromParcel(parcel);

        testAlbumData(createFromParcel);
    }
}
