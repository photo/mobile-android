package me.openphoto.android.app;

import me.openphoto.android.app.net.account.AccountOpenPhotoResponse;
import me.openphoto.android.app.net.account.FakeAccountOpenPhotoApi;
import me.openphoto.android.app.net.account.IAccountOpenPhotoApi;
import me.openphoto.android.app.util.CommonUtils;
import me.openphoto.android.app.util.GuiUtils;
import me.openphoto.android.app.util.LoadingControl;
import me.openphoto.android.app.util.LoginUtils;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.WazaBe.HoloEverywhere.app.ProgressDialog;
import com.WazaBe.HoloEverywhere.sherlock.SActivity;
import com.actionbarsherlock.view.Menu;

/**
 * Class to create new accounts on OpenPhoto
 * 
 * @author Patrick Santana <patrick@openphoto.me>
 */
public class AccountSignup extends SActivity
		implements LoadingControl
{

	private static final String TAG = AccountSignup.class.getSimpleName();
	ProgressDialog progress;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_account_signup);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getSupportMenuInflater().inflate(R.menu.activity_account_signup, menu);
		return true;
	}

	public void createAccountButtonAction(View view)
	{
		CommonUtils.debug(TAG, "Create an account");

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
		CommonUtils.debug(TAG, "Email = [" + email + "], username = [" + username
				+ "] and pwd = [" + password
				+ "]");

		// clean up login information
		Preferences.logout(this);

		new NewUserTask(username, email, password, this, this).execute();
	}

	@Override
	public void startLoading()
	{
		if (progress == null)
		{
			progress = ProgressDialog.show(this,
					getString(R.string.signup_message), null, true, false);
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

	private class NewUserTask extends
			AsyncTask<Void, Void, AccountOpenPhotoResponse>
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
						result.saveCredentials(this.activity);
						activity.setResult(RESULT_OK);
						LoginUtils.sendLoggedInBroadcast(activity);
						startActivity(new Intent(activity, MainActivity.class));
						activity.finish();
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
}
