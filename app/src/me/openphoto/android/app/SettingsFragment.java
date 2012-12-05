
package me.openphoto.android.app;

import org.holoeverywhere.preference.PreferenceCategory;
import org.holoeverywhere.preference.PreferenceFragment;

import android.os.Bundle;

/**
 * @author Eugene Popovich
 */
public class SettingsFragment extends PreferenceFragment
{
    private SettingsCommon settingsCommon;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName(Preferences.PREFERENCES_NAME);
        getPreferenceManager().setSharedPreferencesMode(Preferences.PREFERENCES_MODE);
        addPreferencesFromResource(R.xml.settings);

        settingsCommon = new SettingsCommon(getSupportActivity());

        settingsCommon
                .setLoginCategory((PreferenceCategory) findPreference(getString(R.string.setting_account_category)));
        settingsCommon
                .setLoginPreference(findPreference(getString(R.string.setting_account_loggedin_key)));
        // settingsCommon
        // .setFacebookLoginPreference(findPreference(getString(R.string.setting_account_facebook_loggedin_key)));
        settingsCommon
                .setServerUrl(findPreference(getString(R.string.setting_account_server_key)));
        settingsCommon
                .setSyncClearPreference(findPreference(getString(R.string.setting_sync_clear_key)));
    }

    @Override
    public void onResume()
    {
        super.onResume();
        settingsCommon.refresh();
    }
}
