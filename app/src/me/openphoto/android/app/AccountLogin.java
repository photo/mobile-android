package me.openphoto.android.app;

import me.openphoto.android.app.net.account.AccountOpenPhotoResponse;
import me.openphoto.android.app.net.account.FakeAccountOpenPhotoApi;
import me.openphoto.android.app.net.account.IAccountOpenPhotoApi;
import me.openphoto.android.app.util.GuiUtils;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;

public class AccountLogin extends Activity
{

	private static final String TAG = AccountLogin.class.getSimpleName();

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_account_login);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.activity_account_login, menu);
		return true;
	}

	public void loginButtonAction(View view)
	{
		Log.d(TAG, "Login the user");

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

		Log.d(TAG, "Email = [" + email + "] and pwd = [" + password + "]");

		// clean up login information
		Preferences.logout(this);

		new LogInUserTask(new Credentials(email, password), this).execute();

	}

	private class LogInUserTask extends
			AsyncTask<Void, Void, AccountOpenPhotoResponse>
	{
		private Credentials credentials;
		private Activity activity;

		public LogInUserTask(Credentials credentials, Activity activity)
		{
			this.credentials = credentials;
			this.activity = activity;
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
				GuiUtils.error(TAG, "Could not login",
						e,
						this.activity);
			}
			return null;
		}

		@Override
		protected void onPreExecute()
		{
			super.onPreExecute();
		}

		@Override
		protected void onPostExecute(AccountOpenPhotoResponse result)
		{
			try
			{
				super.onPostExecute(result);

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
						this.activity.finish();
					} else if (result.isInvalidCredentials())
					{
						GuiUtils.alert(getString(R.string.invalid_credentials),
								activity);
					} else if (result.isUnknownError())
					{
						if (result.getMessage() != null
								&& result.getMessage().length() > 0)
						{
							GuiUtils.alert(result.getMessage(), activity);
						} else
						{
							GuiUtils.alert(getString(R.string.unknown_error),
									activity);
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
