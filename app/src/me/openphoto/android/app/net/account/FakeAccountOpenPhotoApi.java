
package me.openphoto.android.app.net.account;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import me.openphoto.android.app.net.ApiBase;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

/**
 * @author Patrick Santana <patrick@openphoto.me>
 */
public class FakeAccountOpenPhotoApi extends ApiBase implements
        IAccountOpenPhotoApi {

    public FakeAccountOpenPhotoApi(Context context) {
        super(context);
    }

    @Override
    public AccountOpenPhotoResponse createNewUser(String user, String email,
            String password) throws IllegalStateException, IOException,
            NoSuchAlgorithmException, JSONException {
        return createFakeAccountOpenPhotoResponse();
    }

    @Override
    public AccountOpenPhotoResponse signIn(String email, String password)
            throws IllegalStateException, IOException, NoSuchAlgorithmException, JSONException {
        return createFakeAccountOpenPhotoResponse();
    }

    @Override
    public String recoverPassword(String email) {
        return "Please, look your email";
    }

    private AccountOpenPhotoResponse createFakeAccountOpenPhotoResponse() throws JSONException {

        JSONObject jsonObjFake = new JSONObject("{\"message\" : \"User credentials\"," +
                "\"code\" : 200," +
                "\"result\" :" +
                "{\"host\":\"apigee.openphoto.me\"," +
                "\"id\":\"102230629a6802fbca9825a4617bfe\"," +
                "\"clientSecret\":\"0f5d654bca\"," +
                "\"userToken\":\"b662440d621f2f71352f8865888fe2\"," +
                "\"userSecret\":\"6d1e8fc274\"," +
                "\"owner\":\"hello@openphoto.me\"}}");

        /*
         * For a Fake error, this is the message:
         * {"message":"Invalid username or password.","code":403,"result":0}
         */

        return new AccountOpenPhotoResponse(jsonObjFake);
    }
}
