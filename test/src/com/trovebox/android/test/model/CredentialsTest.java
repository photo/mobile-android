
package com.trovebox.android.test.model;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.test.InstrumentationTestCase;

import com.trovebox.android.common.model.Credentials;
import com.trovebox.android.test.R;
import com.trovebox.android.test.net.JSONUtils;

public class CredentialsTest extends InstrumentationTestCase {
    public void testFromJson() {
        Credentials c;
        try {
            JSONObject json = JSONUtils.getJson(getInstrumentation().getContext(),
                    R.raw.json_credentials);
            c = new Credentials(json);
        } catch (JSONException e) {
            throw new AssertionError("This exception should not be thrown!");
        }

        checkCredentials(c, "hello@trovebox.com");
    }

    public void testCredentialsParcelable() {
        Credentials c;
        try {
            JSONObject json = JSONUtils.getJson(getInstrumentation().getContext(),
                    R.raw.json_credentials);
            c = new Credentials(json);
        } catch (JSONException e) {
            throw new AssertionError("This exception should not be thrown!");
        }

        checkCredentials(c, "hello@trovebox.com");

        Parcel parcel = Parcel.obtain();
        c.writeToParcel(parcel, 0);
        // done writing, now reset parcel for reading
        parcel.setDataPosition(0);
        // finish round trip
        Credentials createFromParcel = Credentials.CREATOR.createFromParcel(parcel);

        checkCredentials(createFromParcel, "hello@trovebox.com");
    }

    public void testCredentialsParcelableV2() throws JSONException {
        Credentials c;
        JSONObject json = JSONUtils.getJson(getInstrumentation().getContext(),
                R.raw.json_credentials_v2);
        c = new Credentials(json);

        checkCredentialsV2(c, "hellox2@trovebox.com", "http://test3.trovebox.com",
                Credentials.GROUP_TYPE);
        ProfileInformationTest.checkProfileInformation2(c.getProfileInformation(), json
                .getJSONObject("profile").getJSONObject("permission")
                .toString());

        Parcel parcel = Parcel.obtain();
        c.writeToParcel(parcel, 0);
        // done writing, now reset parcel for reading
        parcel.setDataPosition(0);
        // finish round trip
        Credentials createFromParcel = Credentials.CREATOR.createFromParcel(parcel);

        checkCredentialsV2(createFromParcel, "hellox2@trovebox.com", "http://test3.trovebox.com",
                Credentials.GROUP_TYPE);
        ProfileInformationTest.checkProfileInformation2(createFromParcel.getProfileInformation(),
                json.getJSONObject("profile").getJSONObject("permission")
                        .toString());
    }

    public static void checkCredentials(Credentials c, String email) {
        assertNotNull(c);
        assertEquals(c.getEmail(), email);
        assertEquals(c.getServer(), "http://apigee.trovebox.com");
        assertEquals(c.getoAuthConsumerKey(), "102230629a6802fbca9825a4617bfe");
        assertEquals(c.getoAuthConsumerSecret(), "0f5d654bca");
        assertEquals(c.getoAuthToken(), "b662440d621f2f71352f8865888fe2");
        assertEquals(c.getoAuthTokenSecret(), "6d1e8fc274");

    }

    public static void checkCredentialsV2(Credentials c, String email, String server, String type) {
        assertNotNull(c);
        assertEquals(c.getEmail(), email);
        assertEquals(c.getServer(), server);
        assertEquals(c.getType(), type);
        assertEquals(c.getoAuthConsumerKey(), "102230629a6802fbca9825a4617bfe");
        assertEquals(c.getoAuthConsumerSecret(), "0f5d654bca");
        assertEquals(c.getoAuthToken(), "b662440d621f2f71352f8865888fe2");
        assertEquals(c.getoAuthTokenSecret(), "6d1e8fc274");
    }
}
