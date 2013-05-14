
package com.trovebox.android.test.net.account;

import java.io.IOException;
import java.security.GeneralSecurityException;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.test.ApplicationTestCase;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.trovebox.android.app.R;
import com.trovebox.android.app.TroveboxApplication;
import com.trovebox.android.app.net.account.AccountTroveboxResponse;
import com.trovebox.android.app.net.account.IAccountTroveboxApi;
import com.trovebox.android.app.net.account.IAccountTroveboxApiFactory;
import com.trovebox.android.app.util.CommonUtils;
import com.trovebox.android.app.util.TrackerUtils;

public class AccountTroveboxApiTest
        extends ApplicationTestCase<TroveboxApplication>
{

    public AccountTroveboxApiTest()
    {
        super(TroveboxApplication.class);
    }

    private IAccountTroveboxApi mApi;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        TrackerUtils.SKIP_UNCAUGHT_SETUP = true;
        createApplication();
        mApi = IAccountTroveboxApiFactory.getApi();
    }

    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
        TrackerUtils.SKIP_UNCAUGHT_SETUP = false;
    }

    public void testSignInViaGoogle() throws ClientProtocolException, IllegalStateException,
            IOException,
            JSONException, UserRecoverableAuthException, GoogleAuthException,
            GeneralSecurityException {
        // how to setup environment
        // http://android-developers.blogspot.in/2013/01/verifying-back-end-calls-from-android.html
        String[] names = getAccountNames();
        assertTrue(names != null && names.length > 0);
        String accountName = names[0];
        String audience = CommonUtils.getStringResource(R.string.google_auth_server_client_id);
        String SCOPE =
                "audience:server:client_id:" + audience;

        String tokenString = GoogleAuthUtil.getToken(getContext(), accountName, SCOPE);

        // token verification part, this should be done on server side
        GoogleIdTokenVerifier mVerifier;
        JsonFactory mJFactory;
        NetHttpTransport transport = new NetHttpTransport();
        mJFactory = new GsonFactory();
        mVerifier = new GoogleIdTokenVerifier(transport, mJFactory);

        GoogleIdToken token = GoogleIdToken.parse(mJFactory, tokenString);
        assertTrue(mVerifier.verify(token));
        GoogleIdToken.Payload tempPayload = token.getPayload();
        assertTrue(tempPayload.getAudience().equals(audience));

        assertNotNull(tempPayload.getEmail());
        // end of token verification part

        AccountTroveboxResponse response = mApi.signInViaGoogle(
                tokenString);
        assertNotNull(response);
        assertTrue(response.isSuccess());
        checkoAuthString(response.getoAuthConsumerKey());
        checkoAuthString(response.getoAuthConsumerSecret());
        checkoAuthString(response.getoAuthToken());
        checkoAuthString(response.getoAuthConsumerSecret());

    }

    private void checkoAuthString(String str)
    {
        assertTrue(str != null && str.length() > 0);
    }

    private String[] getAccountNames() {
        AccountManager mAccountManager = AccountManager.get(getApplication());
        Account[] accounts = mAccountManager
                .getAccountsByType(GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
        String[] names = new String[accounts.length];
        for (int i = 0; i < names.length; i++) {
            names[i] = accounts[i].name;
        }
        return names;
    }
}
