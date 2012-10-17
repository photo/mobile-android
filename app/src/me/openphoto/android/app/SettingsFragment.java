package me.openphoto.android.app;

import android.annotation.TargetApi;
import android.os.Bundle;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;

@TargetApi(11)
public class SettingsFragment extends PreferenceFragment
{
	private SettingsCommon settingsCommon;
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);

		settingsCommon = new SettingsCommon(getActivity());

		settingsCommon
				.setLoginCategory((PreferenceCategory) findPreference(getString(R.string.setting_account_category)));
		settingsCommon
				.setLoginPreference(findPreference(getString(R.string.setting_account_loggedin_key)));
		settingsCommon
				.setFacebookLoginPreference(findPreference(getString(R.string.setting_account_facebook_loggedin_key)));
		settingsCommon
				.setServerUrl(findPreference(getString(R.string.setting_account_server_key)));
	}

	@Override
	public void onResume()
	{
		super.onResume();
		settingsCommon.refresh();
	}
}
