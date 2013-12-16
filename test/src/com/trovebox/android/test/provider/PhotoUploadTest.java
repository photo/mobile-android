
package com.trovebox.android.test.provider;

import android.net.Uri;
import android.os.Parcel;
import android.test.InstrumentationTestCase;

import com.trovebox.android.common.net.UploadMetaData;
import com.trovebox.android.common.provider.PhotoUpload;
import com.trovebox.android.test.net.UploadMetaDataTest;

public class PhotoUploadTest extends InstrumentationTestCase {
    private static long sId = 100;
    private static Uri sPhotoUri = Uri.fromParts("test", "ssp", "frag");
    private static UploadMetaData sMetaData = UploadMetaDataTest.getTestData();
    private static String sError = "error";
    private static boolean sIsAutoUpload = false;
    private static boolean sShareOnTwitter = false;
    private static boolean sShareOnFacebook = true;
    private static String sHost = "host";
    private static String sToken = "token";
    private static String sName = "name";
    private static long sUploaded = 1000l;

    public static PhotoUpload getTestData() {
        PhotoUpload res = new PhotoUpload(sId, sPhotoUri, sMetaData);
        res.setError(sError);
        res.setIsAutoUpload(sIsAutoUpload);
        res.setShareOnTwitter(sShareOnTwitter);
        res.setShareOnFacebook(sShareOnFacebook);
        res.setHost(sHost);
        res.setToken(sToken);
        res.setUserName(sName);
        res.setUploaded(sUploaded);
        return res;
    }

    public void testPhotoUploadData(PhotoUpload data) {
        assertNotNull(data);
        assertEquals(data.getId(), sId);
        assertEquals(data.getPhotoUri(), sPhotoUri);
        UploadMetaDataTest.testUploadMetaData(data.getMetaData());
        assertEquals(data.getError(), sError);
        assertEquals(data.isAutoUpload(), sIsAutoUpload);
        assertEquals(data.isShareOnTwitter(), sShareOnTwitter);
        assertEquals(data.isShareOnFacebook(), sShareOnFacebook);
        assertEquals(data.getHost(), sHost);
        assertEquals(data.getToken(), sToken);
        assertEquals(data.getUserName(), sName);
        assertEquals(data.getUploaded(), sUploaded);
    }

    public void testPhotoUploadParcelable() {
        PhotoUpload data = getTestData();

        testPhotoUploadData(data);

        Parcel parcel = Parcel.obtain();
        data.writeToParcel(parcel, 0);
        // done writing, now reset parcel for reading
        parcel.setDataPosition(0);
        // finish round trip
        PhotoUpload createFromParcel = PhotoUpload.CREATOR.createFromParcel(parcel);

        testPhotoUploadData(createFromParcel);
    }
}
