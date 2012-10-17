package me.openphoto.android.app;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;

/**
 * The settings screen
 * 
 * @author pas
 * @author Patrick Boos
 */
public class SettingsActivity extends PreferenceActivity
{
	private SettingsCommon settingsCommon;

	/**
	 * Called when Settings Activity is first loaded
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
		{
			initActivity();
		} else
		{
			initFragment();
		}
	}

	public void initActivity()
	{
		addPreferencesFromResource(R.xml.settings);

		settingsCommon = new SettingsCommon(this);

		settingsCommon
				.setLoginCategory((PreferenceCategory) findPreference(getString(R.string.setting_account_category)));
		settingsCommon
				.setLoginPreference(findPreference(getString(R.string.setting_account_loggedin_key)));
		settingsCommon
				.setFacebookLoginPreference(findPreference(getString(R.string.setting_account_facebook_loggedin_key)));
		settingsCommon
				.setServerUrl(findPreference(getString(R.string.setting_account_server_key)));
	}

	@TargetApi(11)
	private void initFragment()
	{
		getFragmentManager().beginTransaction().replace(android.R.id.content,
				new SettingsFragment()).commit();
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		if (settingsCommon != null)
		{
			settingsCommon.refresh();
		}
	}
}
