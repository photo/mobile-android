
package com.trovebox.android.app;


import org.holoeverywhere.app.Activity;
import org.holoeverywhere.app.ProgressDialog;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.trovebox.android.app.common.CommonActivity;
import com.trovebox.android.app.net.TroveboxResponseUtils;
import com.trovebox.android.app.net.account.AccountTroveboxResponse;
import com.trovebox.android.app.net.account.FakeAccountTroveboxApi;
import com.trovebox.android.app.net.account.IAccountTroveboxApi;
import com.trovebox.android.app.util.CommonUtils;
import com.trovebox.android.app.util.GuiUtils;
import com.trovebox.android.app.util.LoadingControl;
import com.trovebox.android.app.util.LoginUtils;
import com.trovebox.android.app.util.ProgressDialogLoadingControl;
import com.trovebox.android.app.util.TrackerUtils;
import com.trovebox.android.app.util.concurrent.AsyncTaskEx;

/**
 * Class to create new accounts on Trovebox
 * 
 * @author Patrick Santana <patrick@trovebox.com>
 */
public class AccountSignup extends CommonActivity
{

    private static final String TAG = AccountSignup.class.getSimpleName();
    ProgressDialog progress;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_signup);
    }

    public void createAccountButtonAction(View view)
    {
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

        new NewUserTask(username, email, password, new ProgressDialogLoadingControl(this, true,
                false,
                getString(R.string.signup_message)), this).execute();
    }

    private class NewUserTask extends
            AsyncTaskEx<Void, Void, AccountTroveboxResponse>
    {
        String username, password, email;
        Activity activity;
        LoadingControl loadingControl;

        public NewUserTask(String username, String email, String password,
                LoadingControl loadingControl, Activity activity)
        {
            super();
            this.username = username;
            this.email = email;
            this.password = password;
            this.activity = activity;
            this.loadingControl = loadingControl;
        }

        @Override
        protected AccountTroveboxResponse doInBackground(Void... params)
        {
            IAccountTroveboxApi api = new FakeAccountTroveboxApi(
                    activity);
            try
            {
                return api.createNewUser(username,
                        email, password);
            } catch (Exception e)
            {
                GuiUtils.error(TAG,
                        R.string.errorCouldNotSignup,
                        e,
                        activity);
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
                        result.saveCredentials(this.activity);
                        activity.setResult(RESULT_OK);
                        LoginUtils.sendLoggedInBroadcast(activity);
                        startActivity(new Intent(activity, MainActivity.class));
                        activity.finish();
                    }
                }
            } catch (Exception e)
            {
                GuiUtils.error(TAG, null, e, activity);
            }
        }

    }
}
