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

/**
 * Class to create new accounts on OpenPhoto
 * 
 * @author Patrick Santana <patrick@openphoto.me>
 */
public class AccountSignup extends Activity
{

	private static final String TAG = AccountSignup.class.getSimpleName();

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_account_signup);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.activity_account_signup, menu);
		return true;
	}

	public void createAccountButtonAction(View view)
	{
		Log.d(TAG, "Create an account");

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
		Log.d(TAG, "Email = [" + email + "], username = [" + username
				+ "] and pwd = [" + password
				+ "]");

		// clean up login information
		Preferences.logout(this);

		new NewUserTask(username, email, password, this).execute();
	}

	private class NewUserTask extends
			AsyncTask<Void, Void, AccountOpenPhotoResponse>
	{
		String username, password, email;
		Activity activity;

		public NewUserTask(String username, String email, String password,
				Activity activity)
		{
			super();
			this.username = username;
			this.email = email;
			this.password = password;
			this.activity = activity;
		}

		@Override
		protected AccountOpenPhotoResponse doInBackground(Void... params)
		{
			IAccountOpenPhotoApi api = new FakeAccountOpenPhotoApi(
					activity);
			try
			{
				return api.createNewUser(username,
						email, password);
			} catch (Exception e)
			{
				GuiUtils.error(TAG, "Could not signup",
						e,
						activity);
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
						activity.setResult(RESULT_OK);
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
