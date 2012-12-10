
package me.openphoto.android.app;

import me.openphoto.android.app.common.CommonPreferenceFragment;

import org.holoeverywhere.preference.PreferenceCategory;

import android.os.Bundle;

/**
 * @author Eugene Popovich
 */
public class SettingsFragment extends CommonPreferenceFragment
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
        settingsCommon
                .setAutoUploadTagPreference(findPreference(getString(R.string.setting_autoupload_tag_key)));
        settingsCommon
                .setAutoUploadPreference(findPreference(getString(R.string.setting_autoupload_on_key)));
        settingsCommon
                .setWiFiOnlyUploadPreference(findPreference(getString(R.string.setting_wifi_only_upload_on_key)));
    }

    @Override
    public void onResume()
    {
        super.onResume();
        settingsCommon.refresh();
    }
}
