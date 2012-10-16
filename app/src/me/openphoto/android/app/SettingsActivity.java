
package me.openphoto.android.app;

import me.openphoto.android.app.facebook.FacebookProvider;
import me.openphoto.android.app.facebook.FacebookUtils;
import me.openphoto.android.app.facebook.FacebookSessionEvents;
import me.openphoto.android.app.facebook.FacebookSessionEvents.LogoutListener;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;

/**
 * The settings screen
 * 
 * @author pas
 * @author Patrick Boos
 */
public class SettingsActivity extends PreferenceActivity implements
		OnPreferenceClickListener
{
    private Preference mLoginPreference;
	private Preference mFacebookLoginPreference;
    private Preference mServerUrl;
	private PreferenceCategory loginCategory;

    /**
     * Called when Settings Activity is first loaded
     * 
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);

		loginCategory = (PreferenceCategory) findPreference(getString(R.string.setting_account_category));
        mLoginPreference = findPreference(getString(R.string.setting_account_loggedin_key));
        mLoginPreference.setOnPreferenceClickListener(this);
		mFacebookLoginPreference = findPreference(getString(R.string.setting_account_facebook_loggedin_key));
		mFacebookLoginPreference.setOnPreferenceClickListener(this);
		if (FacebookProvider.getFacebook() == null
				|| !FacebookProvider.getFacebook().isSessionValid())
		{
			loginCategory.removePreference(mFacebookLoginPreference);
		}
        findPreference(getString(R.string.setting_account_server_key))
                .setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference,
                            Object newValue) {
                        String oldValue = ((EditTextPreference)
                                preference).getText();
                        if (!oldValue.equals(newValue)) {
                            Preferences.logout(SettingsActivity.this);
                            refresh();
                        }
                        return true;
                    }
                });

        // set url
        mServerUrl = findPreference(getString(R.string.setting_account_server_key));
        mServerUrl.setSummary(Preferences.getServer(this));
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }

    private void refresh() {
        mLoginPreference.setTitle(Preferences.isLoggedIn(this) ?
                R.string.setting_account_loggedin_logout : R.string.setting_account_loggedin_login);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (getString(R.string.setting_account_loggedin_key).equals(preference.getKey())) {
            if (Preferences.isLoggedIn(this)) {

                // confirm if user wants to log out
                new AlertDialog.Builder(this)
                        .setTitle("Log out")
                        .setMessage("Are you sure?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes,
                                new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int whichButton)
									{
                                        Preferences.logout(SettingsActivity.this);
                                        refresh();
                                    }
                                })
                        .setNegativeButton(android.R.string.no, null).show();

            } else {
				finish();
            }
		} else if (getString(R.string.setting_account_facebook_loggedin_key)
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
					loginCategory.removePreference(mFacebookLoginPreference);
				}

			};
			mFacebookLoginPreference.setEnabled(false);
			FacebookSessionEvents.addLogoutListener(logoutListener);
			FacebookUtils.logoutRequest(this);
        }
        return false;
    }

}
