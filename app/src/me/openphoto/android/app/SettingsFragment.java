
package me.openphoto.android.app;

import android.os.Bundle;

import com.WazaBe.HoloEverywhere.preference.CheckBoxPreference;
import com.WazaBe.HoloEverywhere.preference.PreferenceCategory;
import com.WazaBe.HoloEverywhere.sherlock.SPreferenceFragment;

/**
 * @author Eugene Popovich
 */
public class SettingsFragment extends SPreferenceFragment
{
    private SettingsCommon settingsCommon;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName(Preferences.PREFERENCES_NAME);
        getPreferenceManager().setSharedPreferencesMode(Preferences.PREFERENCES_MODE);
        addPreferencesFromResource(R.xml.settings);

        settingsCommon = new SettingsCommon(getActivity());

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
                .setAutoUploadActive((CheckBoxPreference) findPreference(getString(R.string.setting_autoupload_on_key)));
        settingsCommon
                .setWiFiOnlyUpload((CheckBoxPreference) findPreference(getString(R.string.setting_wifi_only_upload_on_key)));
    }

    @Override
    public void onResume()
    {
        super.onResume();
        settingsCommon.refresh();
    }
}
