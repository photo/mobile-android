
package com.trovebox.android.app;

import java.lang.ref.WeakReference;

import org.holoeverywhere.app.Activity;

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
import com.trovebox.android.app.util.LoginUtils.LoginActionHandler;
import com.trovebox.android.app.util.ObjectAccessor;
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
        addRegisteredReceiver(LoginUtils.getAndRegisterDestroyOnLoginActionBroadcastReceiver(TAG,
                this));
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

        if (!GuiUtils.validateBasicTextData(new String[] {
                username, email, password
        }, new int[] {
                R.string.field_username, R.string.field_email, R.string.field_password
        })) {
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
    public static class NewUserFragment extends CommonRetainedFragmentWithTaskAndProgress implements
            LoginActionHandler {
        private static final String TAG = NewUserFragment.class.getSimpleName();

        static WeakReference<NewUserFragment> sCurrentInstance;
        static ObjectAccessor<NewUserFragment> sCurrentInstanceAccessor = new ObjectAccessor<NewUserFragment>() {
            private static final long serialVersionUID = 1L;

            @Override
            public NewUserFragment run() {
                return sCurrentInstance == null ? null : sCurrentInstance.get();
            }
        };

        boolean mDelayedLoginProcessing = false;
        AccountTroveboxResponse mLastResponse;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            sCurrentInstance = new WeakReference<NewUserFragment>(this);
        }

        @Override
        public void onDestroy() {
            if (sCurrentInstance != null) {
                if (sCurrentInstance.get() == NewUserFragment.this || sCurrentInstance.get() == null) {
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
            return CommonUtils.getStringResource(R.string.signup_message);
        }

        public void createNewUser(String username, String email, String password) {
            startRetainedTask(new NewUserTask(username, email, password));
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
    }
}
