
package com.trovebox.android.test.net;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import android.os.Parcel;
import android.test.InstrumentationTestCase;

import com.trovebox.android.common.net.UploadMetaData;

public class UploadMetaDataTest extends InstrumentationTestCase {
    private static String sTitle = "test";
    private static String sDescription = "desc";
    private static String sTags = "1,2,tag";
    private static int sPermission = 1;
    private static Map<String, String> sAlbums = new HashMap<String, String>();
    static {
        sAlbums.put("1", "album1");
        sAlbums.put("2", "album2");
        sAlbums.put("3", "album3");
    }

    public static UploadMetaData getTestData() {
        UploadMetaData umd = new UploadMetaData();
        umd.setTitle(sTitle);
        umd.setDescription(sDescription);
        umd.setTags(sTags);
        umd.setPermission(sPermission);
        umd.setAlbums(sAlbums);
        return umd;
    }

    public static void testUploadMetaData(UploadMetaData data) {
        assertNotNull(data);
        assertEquals(data.getTitle(), sTitle);
        assertEquals(data.getDescription(), sDescription);
        assertEquals(data.getTags(), sTags);
        assertEquals(data.getPermission(), sPermission);
        assertNotNull(data.getAlbums());
        assertEquals(data.getAlbums().size(), sAlbums.size());
        for (Entry<String, String> entry : sAlbums.entrySet()) {
            assertTrue(data.getAlbums().containsKey(entry.getKey()));
            String value = data.getAlbums().get(entry.getKey());
            assertEquals(value, entry.getValue());
        }
    }

    public void testUploadMetaDataParcelable() {
        UploadMetaData data = getTestData();

        testUploadMetaData(data);

        Parcel parcel = Parcel.obtain();
        data.writeToParcel(parcel, 0);
        // done writing, now reset parcel for reading
        parcel.setDataPosition(0);
        // finish round trip
        UploadMetaData createFromParcel = UploadMetaData.CREATOR.createFromParcel(parcel);

        testUploadMetaData(createFromParcel);
    }
}
