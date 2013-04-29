
package com.trovebox.android.test.model;

import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.test.InstrumentationTestCase;

import com.trovebox.android.app.model.Token;
import com.trovebox.android.test.R;
import com.trovebox.android.test.net.JSONUtils;

public class TokenTest extends InstrumentationTestCase {
    public void testFromJson() {
        Token token;
        try {
            JSONObject json = JSONUtils
                    .getJson(getInstrumentation().getContext(), R.raw.json_token);
            token = Token.fromJson(json);
        } catch (JSONException e) {
            throw new AssertionError("This exception should not be thrown!");
        }

        testTokenData(token);
    }

    public void testTokenData(Token token) {
        assertNotNull(token);
        assertEquals("0ef785aeba", token.getId());
        assertEquals("photo", token.getType());
        assertEquals("9t", token.getData());
        assertEquals(new Date(0l), token.getDateExpires());
    }

    public void testTokenParcelable()
    {
        Token token;
        try {
            JSONObject json = JSONUtils
                    .getJson(getInstrumentation().getContext(), R.raw.json_token);
            token = Token.fromJson(json);
        } catch (JSONException e) {
            throw new AssertionError("This exception should not be thrown!");
        }

        testTokenData(token);

        Parcel parcel = Parcel.obtain();
        token.writeToParcel(parcel, 0);
        // done writing, now reset parcel for reading
        parcel.setDataPosition(0);
        // finish round trip
        Token createFromParcel = Token.CREATOR.createFromParcel(parcel);

        testTokenData(createFromParcel);
    }
}
