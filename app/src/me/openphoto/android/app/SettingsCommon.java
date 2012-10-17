package me.openphoto.android.app;

import me.openphoto.android.app.facebook.FacebookProvider;
import me.openphoto.android.app.facebook.FacebookSessionEvents;
import me.openphoto.android.app.facebook.FacebookSessionEvents.LogoutListener;
import me.openphoto.android.app.facebook.FacebookUtils;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceCategory;

public class SettingsCommon implements
		OnPreferenceClickListener
{
	Activity activity;
	Preference mLoginPreference;
	Preference mFacebookLoginPreference;
	PreferenceCategory loginCategory;
	Preference mServerUrl;

	public SettingsCommon(Activity activity)
	{
		this.activity = activity;
	}

	@Override
	public boolean onPreferenceClick(Preference preference)
	{
		if (activity.getString(R.string.setting_account_loggedin_key)
				.equals(
						preference.getKey()))
		{
			if (Preferences.isLoggedIn(activity))
			{

				// confirm if user wants to log out
				new AlertDialog.Builder(activity)
						.setTitle("Log out")
						.setMessage("Are you sure?")
						.setIcon(android.R.drawable.ic_dialog_alert)
						.setPositiveButton(R.string.yes,
								new DialogInterface.OnClickListener()
								{

									@Override
									public void onClick(
											DialogInterface dialog,
											int whichButton)
									{
										Preferences
												.logout(activity);
										refresh();
									}
								})
						.setNegativeButton(R.string.no, null)
						.show();

			} else
			{
				activity.finish();
			}
		} else if (activity.getString(
				R.string.setting_account_facebook_loggedin_key)
				.equals(preference.getKey()))
		{
			LogoutListener logoutListener = new LogoutListener()
			{

				@Override
				public void onLogoutBegin()
				{
				}

				@Override
				public void onLogoutFinish()
				{
					FacebookSessionEvents.removeLogoutListener(this);
					loginCategory
							.removePreference(mFacebookLoginPreference);
				}

			};
			mFacebookLoginPreference.setEnabled(false);
			FacebookSessionEvents.addLogoutListener(logoutListener);
			FacebookUtils.logoutRequest(activity);
		}
		return false;
	}

	public void refresh()
	{
		mLoginPreference.setTitle(Preferences.isLoggedIn(activity) ?
				R.string.setting_account_loggedin_logout
				: R.string.setting_account_loggedin_login);
	}

	public Preference getLoginPreference()
	{
		return mLoginPreference;
	}

	public void setLoginPreference(Preference mLoginPreference)
	{
		this.mLoginPreference = mLoginPreference;
		this.mLoginPreference.setOnPreferenceClickListener(this);
	}

	public Preference getFacebookLoginPreference()
	{
		return mFacebookLoginPreference;
	}

	public void setFacebookLoginPreference(
			Preference mFacebookLoginPreference)
	{
		this.mFacebookLoginPreference = mFacebookLoginPreference;
		this.mFacebookLoginPreference.setOnPreferenceClickListener(this);
		if (FacebookProvider.getFacebook() == null
				|| !FacebookProvider.getFacebook().isSessionValid())
		{
			loginCategory.removePreference(mFacebookLoginPreference);
		}
	}

	public PreferenceCategory getLoginCategory()
	{
		return loginCategory;
	}

	public void setLoginCategory(PreferenceCategory loginCategory)
	{
		this.loginCategory = loginCategory;
	}

	public Preference getServerUrl()
	{
		return mServerUrl;
	}

	public void setServerUrl(Preference mServerUrl)
	{
		this.mServerUrl = mServerUrl;
		mServerUrl.setSummary(Preferences.getServer(activity));
		mServerUrl
				.setOnPreferenceChangeListener(new OnPreferenceChangeListener()
				{
					@Override
					public boolean onPreferenceChange(Preference preference,
							Object newValue)
					{
						String oldValue = ((EditTextPreference)
								preference).getText();
						if (!oldValue.equals(newValue))
						{
							Preferences.logout(activity);
							refresh();
						}
						return true;
					}
				});
	}
}
