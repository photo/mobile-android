
package com.trovebox.android.app;


import org.holoeverywhere.app.Activity;
import org.holoeverywhere.widget.TextView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.EditText;

import com.trovebox.android.app.common.CommonActivity;
import com.trovebox.android.app.net.TroveboxResponseUtils;
import com.trovebox.android.app.net.account.AccountTroveboxResponse;
import com.trovebox.android.app.net.account.IAccountTroveboxApi;
import com.trovebox.android.app.net.account.IAccountTroveboxApiFactory;
import com.trovebox.android.app.util.CommonUtils;
import com.trovebox.android.app.util.GuiUtils;
import com.trovebox.android.app.util.LoadingControl;
import com.trovebox.android.app.util.LoginUtils;
import com.trovebox.android.app.util.ProgressDialogLoadingControl;
import com.trovebox.android.app.util.TrackerUtils;
import com.trovebox.android.app.util.concurrent.AsyncTaskEx;

public class AccountLogin extends CommonActivity
{
    private static final String TAG = AccountLogin.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_login);
        init();
    }

    void init()
    {
        TextView signInInstructions = (TextView) findViewById(R.id.sign_in_instructions);
        signInInstructions.setText(Html.fromHtml(getString(R.string.sign_in_instructions)));
    }
    public void loginButtonAction(View view)
    {
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

        new LogInUserTask(new Credentials(email, password),
                new ProgressDialogLoadingControl(this, true, false,
                        getString(R.string.logging_in_message)), this)
                .execute();

    }

    public void accountOwnServerButtonAction(View view)
    {
        CommonUtils.debug(TAG, "Start own server button action");
        TrackerUtils.trackButtonClickEvent("own_server_login_button", AccountLogin.this);
        Intent intent = new Intent(this, SetupActivity.class);
        startActivity(intent);
        finish();
    }

    private class LogInUserTask extends
            AsyncTaskEx<Void, Void, AccountTroveboxResponse>
    {
        private Credentials credentials;
        private Activity activity;
        LoadingControl loadingControl;

        public LogInUserTask(Credentials credentials,
                LoadingControl loadingControl, Activity activity)
        {
            this.credentials = credentials;
            this.activity = activity;
            this.loadingControl = loadingControl;
        }

        @Override
        protected AccountTroveboxResponse doInBackground(Void... params)
        {
            IAccountTroveboxApi api = IAccountTroveboxApiFactory.getApi(
                    this.activity);
            try
            {
                return api.signIn(credentials.getUser(),
                        credentials.getPwd());
            } catch (Exception e)
            {
                GuiUtils.error(TAG,
                        R.string.errorCouldNotLogin,
                        e,
                        this.activity);
            }
            return null;
        }

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            loadingControl.startLoading();
        }

        @Override
        protected void onPostExecute(AccountTroveboxResponse result)
        {
            try
            {
                super.onPostExecute(result);
                loadingControl.stopLoading();

                if (result != null)
                {
                    if (TroveboxResponseUtils.checkResponseValid(result))
                    {
                        // save credentials.
                        result.saveCredentials(this.activity);

                        // start new activity
                        setResult(RESULT_OK);
                        startActivity(new Intent(this.activity,
                                MainActivity.class));
                        LoginUtils.sendLoggedInBroadcast(activity);
                        this.activity.finish();
                    }
                }
            } catch (Exception e)
            {
                GuiUtils.error(TAG, null, e, activity);
            }
        }

    }

    public class Credentials
    {
        private String user;
        private String pwd;

        public Credentials(String user, String pwd)
        {
            this.user = user;
            this.pwd = pwd;
        }

        public String getUser()
        {
            return user;
        }

        public void setUser(String user)
        {
            this.user = user;
        }

        public String getPwd()
        {
            return pwd;
        }

        public void setPwd(String pwd)
        {
            this.pwd = pwd;
        }
    }
}
