
package me.openphoto.android.app;

import me.openphoto.android.app.net.account.AccountOpenPhotoResponse;
import me.openphoto.android.app.net.account.FakeAccountOpenPhotoApi;
import me.openphoto.android.app.net.account.IAccountOpenPhotoApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.bugsense.trace.BugSenseHandler;

/**
 * Class to create new accounts on OpenPhoto
 * 
 * @author Patrick Santana <patrick@openphoto.me>
 */
public class AccountSignup extends Activity {

    private static final String TAG = AccountSignup.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_signup);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_account_signup, menu);
        return true;
    }

    public void createAccountButtonAction(View view) {
        Log.d(TAG, "Create an account");

        EditText editText = (EditText) findViewById(R.id.edit_username);
        String username = editText.getText().toString();

        editText = (EditText) findViewById(R.id.edit_email);
        String email = editText.getText().toString();

        editText = (EditText) findViewById(R.id.edit_password);
        String password = editText.getText().toString();

        Log.d(TAG, "Email = [" + email + "], username = [" + username + "] and pwd = [" + password
                + "]");

        // clean up login information
        Preferences.logout(this);

        IAccountOpenPhotoApi api = new FakeAccountOpenPhotoApi(this);
        try {
            AccountOpenPhotoResponse response = api.createNewUser(username, email, password);
            // save credentials.
            response.saveCredentials(this);

            // start new activity
            setResult(RESULT_OK);
            startActivity(new Intent(this, MainActivity.class));
            this.finish();

        } catch (Exception e) {
            Log.e(TAG, "Execption to create user", e);
            Toast.makeText(this,
                    "Error: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
            BugSenseHandler.log(TAG, e);
        }
    }
}
