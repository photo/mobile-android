
package com.trovebox.android.app.net.account;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;


import org.json.JSONException;
import org.json.JSONObject;

import com.trovebox.android.app.net.ApiBase;

import android.content.Context;

/**
 * @author Patrick Santana <patrick@trovebox.com>
 */
public class FakeAccountTroveboxApi extends ApiBase implements
        IAccountTroveboxApi {

    public FakeAccountTroveboxApi(Context context) {
        super(context);
    }

    @Override
    public AccountTroveboxResponse createNewUser(String user, String email,
            String password) throws IllegalStateException, IOException,
            NoSuchAlgorithmException, JSONException {
        return createFakeAccountTroveboxResponse();
    }

    @Override
    public AccountTroveboxResponse signIn(String email, String password)
            throws IllegalStateException, IOException, NoSuchAlgorithmException, JSONException {
        return createFakeAccountTroveboxResponse();
    }

    @Override
    public String recoverPassword(String email) {
        return "Please, look your email";
    }

    private AccountTroveboxResponse createFakeAccountTroveboxResponse() throws JSONException {

        JSONObject jsonObjFake = new JSONObject(
                "{\"message\" : \"User credentials\"," +
                        "\"code\" : 200," +
                        "\"result\" :" +
                        "{\"host\":\"apigee.trovebox.com\"," +
                        "\"id\":\"102230629a6802fbca9825a4617bfe\"," +
                        "\"clientSecret\":\"0f5d654bca\"," +
                        "\"userToken\":\"b662440d621f2f71352f8865888fe2\"," +
                        "\"userSecret\":\"6d1e8fc274\"," +
                        "\"owner\":\"hello@trovebox.com\"}}");
        // JSONObject jsonObjFake = new JSONObject(
        // "{\"message\":\"Invalid username or password.\",\"code\":403,\"result\":0}");
        // JSONObject jsonObjFake = new JSONObject(
        // "{\"message\":\"Please enter a username with at least 6 characters.\",\"code\":500,\"result\":false}");

        /*
         * For a Fake error, this is the message:
         * {"message":"Invalid username or password.","code":403,"result":0}
         */
        try {
            // emulate network latency
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return new AccountTroveboxResponse(jsonObjFake);
    }
}
