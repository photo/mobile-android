
package com.trovebox.android.app;

import java.lang.ref.WeakReference;

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
import com.trovebox.android.app.util.LoginUtils.LoginActionHandler;
import com.trovebox.android.app.util.ObjectAccessor;
import com.trovebox.android.app.util.TrackerUtils;

public class AccountLogin extends CommonActivity {
    private static final String TAG = AccountLogin.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_login);
        init();
        addRegisteredReceiver(LoginUtils.getAndRegisterDestroyOnLoginActionBroadcastReceiver(TAG,
                this));
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

        if (!GuiUtils.validateBasicTextData(new String[] {
                email, password
        }, new int[] {
                R.string.field_email, R.string.field_password
        })) {
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

        if (!GuiUtils.validateBasicTextData(new String[] {
            email
        }, new int[] {
            R.string.field_email,
        })) {
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
    public static class LogInFragment extends CommonRetainedFragmentWithTaskAndProgress implements
            LoginActionHandler {
        private static final String TAG = LogInFragment.class.getSimpleName();

        static WeakReference<LogInFragment> sCurrentInstance;
        static ObjectAccessor<LogInFragment> sCurrentInstanceAccessor = new ObjectAccessor<LogInFragment>() {
            private static final long serialVersionUID = 1L;

            @Override
            public LogInFragment run() {
                return sCurrentInstance == null ? null : sCurrentInstance.get();
            }
        };

        boolean mDelayedLoginProcessing = false;
        AccountTroveboxResponse mLastResponse;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            sCurrentInstance = new WeakReference<LogInFragment>(this);
        }

        @Override
        public void onDestroy() {
            if (sCurrentInstance != null) {
                if (sCurrentInstance.get() == LogInFragment.this
                        || sCurrentInstance.get() == null) {
                    CommonUtils.debug(TAG, "Nullify current instance");
                    sCurrentInstance = null;
                } else {
                    CommonUtils.debug(TAG,
                            "Skipped nullify of current instance, such as it is not the same");
                }
            }
            super.onDestroy();
        }

        @Override
        public String getLoadingMessage() {
            return CommonUtils.getStringResource(R.string.logging_in_message);
        }

        public void doLogin(String user, String pwd) {
            startRetainedTask(new LogInUserTask(new Credentials(user, pwd)));
        }

        @Override
        public void processLoginCredentials(com.trovebox.android.app.model.Credentials credentials) {
            Activity activity = getSupportActivity();
            credentials.saveCredentials(activity);
            LoginUtils.onLoggedIn(activity, false);
        }

        void processLoginResonse(Activity activity) {
            LoginUtils.processSuccessfulLoginResult(mLastResponse, sCurrentInstanceAccessor,
                    activity);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            if (mDelayedLoginProcessing) {
                mDelayedLoginProcessing = false;
                processLoginResonse(getSupportActivity());
            }
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
                    mLastResponse = result;
                    Activity activity = getSupportActivity();
                    if (activity != null) {
                        processLoginResonse(activity);
                    } else {
                        TrackerUtils.trackErrorEvent("activity_null", TAG);
                        mDelayedLoginProcessing = true;
                    }
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
