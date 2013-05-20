
package com.trovebox.android.app;

import org.holoeverywhere.app.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.trovebox.android.app.common.CommonActivity;
import com.trovebox.android.app.common.CommonFragmentUtils;
import com.trovebox.android.app.common.CommonRetainedFragmentWithTaskAndProgress;
import com.trovebox.android.app.net.TroveboxResponseUtils;
import com.trovebox.android.app.net.account.AccountTroveboxResponse;
import com.trovebox.android.app.net.account.IAccountTroveboxApiFactory;
import com.trovebox.android.app.util.CommonUtils;
import com.trovebox.android.app.util.GuiUtils;
import com.trovebox.android.app.util.LoginUtils;
import com.trovebox.android.app.util.TrackerUtils;

/**
 * Class to create new accounts on Trovebox
 * 
 * @author Patrick Santana <patrick@trovebox.com>
 */
public class AccountSignup extends CommonActivity {

    private static final String TAG = AccountSignup.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_signup);
        getNewUserFragment();
    }

    public void createAccountButtonAction(View view) {
        CommonUtils.debug(TAG, "Create an account");
        TrackerUtils.trackButtonClickEvent("create_account_button", AccountSignup.this);

        EditText editText = (EditText) findViewById(R.id.edit_username);
        String username = editText.getText().toString();

        editText = (EditText) findViewById(R.id.edit_email);
        String email = editText.getText().toString();

        editText = (EditText) findViewById(R.id.edit_password);
        String password = editText.getText().toString();

        if (!GuiUtils.validateBasicTextData(
                new String[]
                {
                        username, email, password
                }, new int[]
                {
                        R.string.field_username,
                        R.string.field_email,
                        R.string.field_password
                }, this))
        {
            return;
        }

        // clean up login information
        Preferences.logout(this);

        getNewUserFragment().createNewUser(username, email, password);
    }

    /**
     * Get the NewUserFragment.
     * 
     * @return
     */
    NewUserFragment getNewUserFragment() {
        return CommonFragmentUtils.findOrCreateFragment(
                NewUserFragment.class,
                getSupportFragmentManager());
    }

    /**
     * The create new user fragment with the retained instance across
     * configuration change
     */
    public static class NewUserFragment extends CommonRetainedFragmentWithTaskAndProgress {
        private static final String TAG = NewUserFragment.class.getSimpleName();

        @Override
        public String getLoadingMessage() {
            return CommonUtils.getStringResource(R.string.signup_message);
        }

        public void createNewUser(String username, String email, String password) {
            startRetainedTask(new NewUserTask(username, email, password));
        }

        class NewUserTask extends RetainedTask {
            String username, password, email;
            AccountTroveboxResponse result;

            public NewUserTask(String username, String email, String password) {
                this.username = username;
                this.email = email;
                this.password = password;
            }

            @Override
            protected Boolean doInBackground(Void... params) {
                try {
                    if (CommonUtils.checkOnline())
                    {
                        result = IAccountTroveboxApiFactory.getApi()
                                .createNewUser(username, email, password);
                        return TroveboxResponseUtils.checkResponseValid(result);
                    }
                } catch (Exception e) {
                    GuiUtils.error(TAG, R.string.errorCouldNotSignup, e);
                }
                return false;
            }

            @Override
            protected void onSuccessPostExecuteAdditional() {
                try
                {
                    Activity activity = getSupportActivity();
                    // save credentials.
                    result.saveCredentials(activity);

                    // start new activity
                    startActivity(new Intent(activity,
                            MainActivity.class));
                    LoginUtils.sendLoggedInBroadcast(activity);
                    activity.finish();
                } catch (Exception e)
                {
                    GuiUtils.error(TAG, e);
                }
            }
        }
    }
}
