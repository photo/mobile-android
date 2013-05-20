
package com.trovebox.android.app;

import org.holoeverywhere.app.Activity;
import org.holoeverywhere.widget.TextView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.EditText;

import com.trovebox.android.app.common.CommonActivity;
import com.trovebox.android.app.common.CommonFragmentUtils;
import com.trovebox.android.app.common.CommonRetainedFragmentWithTaskAndProgress;
import com.trovebox.android.app.net.TroveboxResponse;
import com.trovebox.android.app.net.TroveboxResponseUtils;
import com.trovebox.android.app.net.account.AccountTroveboxResponse;
import com.trovebox.android.app.net.account.IAccountTroveboxApiFactory;
import com.trovebox.android.app.util.CommonUtils;
import com.trovebox.android.app.util.GuiUtils;
import com.trovebox.android.app.util.LoginUtils;
import com.trovebox.android.app.util.TrackerUtils;

public class AccountLogin extends CommonActivity {
    private static final String TAG = AccountLogin.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_login);
        init();
    }

    void init() {
        getLoginFragment();
        getRecoverPasswordFragment();
        TextView signInInstructions = (TextView) findViewById(R.id.sign_in_instructions);
        signInInstructions.setText(Html.fromHtml(getString(R.string.sign_in_instructions)));
    }

    public void loginButtonAction(View view) {
        CommonUtils.debug(TAG, "Login the user");
        TrackerUtils.trackButtonClickEvent("login_button", AccountLogin.this);

        EditText editText = (EditText) findViewById(R.id.edit_email);
        String email = editText.getText().toString();

        editText = (EditText) findViewById(R.id.edit_password);
        String password = editText.getText().toString();

        if (!GuiUtils.validateBasicTextData(
                new String[]
                {
                        email, password
                }, new int[]
                {
                        R.string.field_email,
                        R.string.field_password
                }, this))
        {
            return;
        }

        // clean up login information
        Preferences.logout(this);

        getLoginFragment().doLogin(email, password);

    }

    public void accountOwnServerButtonAction(View view) {
        CommonUtils.debug(TAG, "Start own server button action");
        TrackerUtils.trackButtonClickEvent("own_server_login_button", AccountLogin.this);
        Intent intent = new Intent(this, SetupActivity.class);
        startActivity(intent);
        finish();
    }

    public void forgotPasswordButtonAction(View view) {
        CommonUtils.debug(TAG, "Recover user password");
        TrackerUtils.trackButtonClickEvent("forgot_password_button", AccountLogin.this);

        EditText editText = (EditText) findViewById(R.id.edit_email);
        String email = editText.getText().toString();

        if (!GuiUtils.validateBasicTextData(
                new String[]
                {
                    email
                }, new int[]
                {
                    R.string.field_email,
                }, this))
        {
            return;
        }
        getRecoverPasswordFragment().recoverPassword(email);
    }

    /**
     * Get the login fragment
     * 
     * @return
     */
    public LogInFragment getLoginFragment() {
        return CommonFragmentUtils
                .findOrCreateFragment(LogInFragment.class, getSupportFragmentManager());
    }

    /**
     * Get the recover password fragment
     * 
     * @return
     */
    public RecoverPasswordFragment getRecoverPasswordFragment() {
        return CommonFragmentUtils
                .findOrCreateFragment(RecoverPasswordFragment.class, getSupportFragmentManager());
    }

    /**
     * The log in fragment with the retained instance across configuration
     * change
     */
    public static class LogInFragment extends CommonRetainedFragmentWithTaskAndProgress {
        private static final String TAG = LogInFragment.class.getSimpleName();

        @Override
        public String getLoadingMessage() {
            return CommonUtils.getStringResource(R.string.logging_in_message);
        }

        public void doLogin(String user, String pwd) {
            startRetainedTask(new LogInUserTask(new Credentials(user, pwd)));
        }

        class LogInUserTask extends RetainedTask {
            private Credentials credentials;
            AccountTroveboxResponse result;

            public LogInUserTask(Credentials credentials) {
                this.credentials = credentials;
            }

            @Override
            protected Boolean doInBackground(Void... params) {
                try {
                    if (CommonUtils.checkOnline())
                    {
                        result = IAccountTroveboxApiFactory.getApi()
                                .signIn(credentials.getUser(), credentials.getPwd());
                        return TroveboxResponseUtils.checkResponseValid(result);
                    }
                } catch (Exception e) {
                    GuiUtils.error(TAG, R.string.errorCouldNotLogin, e);
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

        public class Credentials {
            private String user;
            private String pwd;

            public Credentials(String user, String pwd) {
                this.user = user;
                this.pwd = pwd;
            }

            public String getUser() {
                return user;
            }

            public void setUser(String user) {
                this.user = user;
            }

            public String getPwd() {
                return pwd;
            }

            public void setPwd(String pwd) {
                this.pwd = pwd;
            }
        }
    }

    /**
     * The recover password fragment with the retained instance across
     * configuration change
     */
    public static class RecoverPasswordFragment extends CommonRetainedFragmentWithTaskAndProgress {
        private static final String TAG = RecoverPasswordFragment.class.getSimpleName();

        public void recoverPassword(String email) {
            startRetainedTask(new RecoverPasswordUserTask(email));
        }

        class RecoverPasswordUserTask extends RetainedTask {
            String email;
            String message;
            AccountTroveboxResponse result;

            public RecoverPasswordUserTask(String email) {
                this.email = email;
            }

            @Override
            protected Boolean doInBackground(Void... params) {
                try {
                    if (CommonUtils.checkOnline())
                    {
                        TroveboxResponse response = IAccountTroveboxApiFactory.getApi()
                                .recoverPassword(email);
                        message = response.getAlertMessage();
                        return TroveboxResponseUtils.checkResponseValid(response);
                    }
                } catch (Exception e) {
                    GuiUtils.error(TAG, R.string.errorCouldNotRecoverPassword, e);
                }
                return false;
            }

            @Override
            protected void onSuccessPostExecuteAdditional() {
                GuiUtils.alert(message);
            }
        }
    }
}
