package com.trovebox.android.test.net.account;

import org.json.JSONException;
import org.json.JSONObject;

import android.test.InstrumentationTestCase;

import com.trovebox.android.app.model.Credentials;
import com.trovebox.android.app.net.TroveboxResponse.RequestType;
import com.trovebox.android.app.net.account.AccountTroveboxResponse;
import com.trovebox.android.test.R;
import com.trovebox.android.test.model.CredentialsTest;
import com.trovebox.android.test.net.JSONUtils;

public class AccountTroveboxResponseTest extends InstrumentationTestCase {

    public void testSimpleResponse() throws JSONException {
        JSONObject json = JSONUtils.getJson(getInstrumentation().getContext(),
                R.raw.json_login_simple);
        AccountTroveboxResponse response = new AccountTroveboxResponse(RequestType.UNKNOWN, json);
        assertNotNull(response);
        assertEquals(200, response.getCode());
        Credentials[] credentials = response.getCredentials();
        assertNotNull(credentials);
        assertTrue(credentials.length == 1);
        CredentialsTest.checkCredentials(credentials[0], "hello@trovebox.com");
    }

    public void testMultiResponse() throws JSONException {
        JSONObject json = JSONUtils.getJson(getInstrumentation().getContext(),
                R.raw.json_login_multiple);
        AccountTroveboxResponse response = new AccountTroveboxResponse(RequestType.UNKNOWN, json);
        assertNotNull(response);
        assertEquals(200, response.getCode());
        Credentials[] credentials = response.getCredentials();
        assertNotNull(credentials);
        assertTrue(credentials.length == 2);
        CredentialsTest.checkCredentials(credentials[0], "hello@trovebox.com");
        CredentialsTest.checkCredentials(credentials[1], "hello2@trovebox.com");
    }
}
