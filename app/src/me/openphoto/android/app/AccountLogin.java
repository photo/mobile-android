
package me.openphoto.android.app;

import me.openphoto.android.app.net.account.AccountOpenPhotoResponse;
import me.openphoto.android.app.net.account.FakeAccountOpenPhotoApi;
import me.openphoto.android.app.net.account.IAccountOpenPhotoApi;
import me.openphoto.android.app.util.CommonUtils;
import me.openphoto.android.app.util.GuiUtils;
import me.openphoto.android.app.util.LoadingControl;
import me.openphoto.android.app.util.LoginUtils;
import me.openphoto.android.app.util.concurrent.AsyncTaskEx;

import org.holoeverywhere.app.Activity;
import org.holoeverywhere.app.ProgressDialog;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class AccountLogin extends Activity implements
        LoadingControl
{
    private static final String TAG = AccountLogin.class.getSimpleName();
    ProgressDialog progress;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_login);
    }

    public void loginButtonAction(View view)
    {
        CommonUtils.debug(TAG, "Login the user");

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

        CommonUtils.debug(TAG, "Email = [" + email + "] and pwd = [" + password + "]");

        // clean up login information
        Preferences.logout(this);

        new LogInUserTask(new Credentials(email, password), this, this)
                .execute();

    }

    @Override
    public void startLoading()
    {
        if (progress == null)
        {
            progress = ProgressDialog.show(this,
                    getString(R.string.logging_in_message), null, true, false);
        }
    }

    @Override
    public void stopLoading()
    {
        if (progress != null && progress.isShowing())
        {
            progress.dismiss();
            progress = null;
        }
    }

    private class LogInUserTask extends
            AsyncTaskEx<Void, Void, AccountOpenPhotoResponse>
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
        protected AccountOpenPhotoResponse doInBackground(Void... params)
        {
            IAccountOpenPhotoApi api = new FakeAccountOpenPhotoApi(
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
        protected void onPostExecute(AccountOpenPhotoResponse result)
        {
            try
            {
                super.onPostExecute(result);
                loadingControl.stopLoading();

                if (result != null)
                {
                    if (result.isSuccess())
                    {
                        // save credentials.
                        result.saveCredentials(this.activity);

                        // start new activity
                        setResult(RESULT_OK);
                        startActivity(new Intent(this.activity,
                                MainActivity.class));
                        LoginUtils.sendLoggedInBroadcast(activity);
                        this.activity.finish();
                    } else if (result.isInvalidCredentials())
                    {
                        GuiUtils.alert(R.string.invalid_credentials);
                    } else if (result.isUnknownError())
                    {
                        if (result.getMessage() != null
                                && result.getMessage().length() > 0)
                        {
                            GuiUtils.alert(result.getMessage(), activity);
                        } else
                        {
                            GuiUtils.alert(R.string.unknown_error);
                        }
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
