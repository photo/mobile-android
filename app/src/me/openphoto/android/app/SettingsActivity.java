/**
 * The settings screen
 */

package me.openphoto.android.app;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;

/**
 * The settings screen
 * 
 * @author pas
 * @author Patrick Boos
 */
public class SettingsActivity extends PreferenceActivity implements OnPreferenceClickListener {
    private Preference mLoginPreference;

    /**
     * Called when Settings Activity is first loaded
     * 
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);

        mLoginPreference = findPreference(getString(R.string.setting_account_loggedin_key));
        mLoginPreference.setOnPreferenceClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshLoginPreferenceTitle();
    }

    private void refreshLoginPreferenceTitle() {
        mLoginPreference.setTitle(Preferences.isLoggedIn(this) ?
                R.string.setting_account_loggedin_logout : R.string.setting_account_loggedin_login);
    }

    // TODO when server is changed it should delete the login information

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (getString(R.string.setting_account_loggedin_key).equals(preference.getKey())) {
            if (Preferences.isLoggedIn(this)) {
                // TODO show logout confirmation dialog
                Preferences.logout(this);
                refreshLoginPreferenceTitle();
            } else {
                startActivity(new Intent(this, OAuthActivity.class));
            }
        }
        return false;
    }

}
