
package me.openphoto.android.app;

import me.openphoto.android.app.bitmapfun.util.ImageCache;
import me.openphoto.android.app.facebook.FacebookProvider;
import me.openphoto.android.app.facebook.FacebookSessionEvents;
import me.openphoto.android.app.facebook.FacebookSessionEvents.LogoutListener;
import me.openphoto.android.app.facebook.FacebookUtils;
import me.openphoto.android.app.provider.UploadsUtils;
import me.openphoto.android.app.util.GuiUtils;
import me.openphoto.android.app.util.ProgressDialogLoadingControl;
import me.openphoto.android.app.util.SimpleAsyncTaskEx;
import android.app.Activity;
import android.content.DialogInterface;

import com.WazaBe.HoloEverywhere.app.AlertDialog;
import com.WazaBe.HoloEverywhere.preference.EditTextPreference;
import com.WazaBe.HoloEverywhere.preference.Preference;
import com.WazaBe.HoloEverywhere.preference.Preference.OnPreferenceChangeListener;
import com.WazaBe.HoloEverywhere.preference.Preference.OnPreferenceClickListener;
import com.WazaBe.HoloEverywhere.preference.PreferenceCategory;

public class SettingsCommon implements
        OnPreferenceClickListener
{
    static final String TAG = SettingsCommon.class.getSimpleName();
    Activity activity;
    Preference mLoginPreference;
    Preference mFacebookLoginPreference;
    Preference mSyncClearPreference;
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
                        .setTitle(R.string.logOut)
                        .setMessage(R.string.areYouSureQuestion)
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
                                        new ClearCachesTask().execute();
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

    public void setSyncClearPreference(Preference mSyncClearPreference)
    {
        this.mSyncClearPreference = mSyncClearPreference;
        mSyncClearPreference
                .setOnPreferenceClickListener(new OnPreferenceClickListener()
                {

                    @Override
                    public boolean onPreferenceClick(Preference preference)
                    {
                        // confirm if user wants to clear sync information
                        new AlertDialog.Builder(activity)
                                .setTitle(R.string.sync_clear)
                                .setMessage(R.string.areYouSureQuestion)
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setPositiveButton(R.string.yes,
                                        new DialogInterface.OnClickListener()
                                        {

                                            @Override
                                            public void onClick(
                                                    DialogInterface dialog,
                                                    int whichButton)
                                            {
                                                UploadsUtils.clearUploads();
                                            }
                                        })
                                .setNegativeButton(R.string.no, null)
                                .show();

                        return true;
                    }
                });
    }

    public class ClearCachesTask extends SimpleAsyncTaskEx
    {

        public ClearCachesTask() {
            super(new ProgressDialogLoadingControl(activity, true, false,
                    activity.getString(R.string.loading)));
        }

        @Override
        protected void onSuccessPostExecute() {
            if (activity != null)
            {
                activity.finish();
            }
        }

        @Override
        protected void onFailedPostExecute() {
            if (activity != null)
            {
                activity.finish();
            }
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try
            {
                ImageCache.clearDiskCaches();
                return true;
            } catch (Exception ex)
            {
                GuiUtils.noAlertError(TAG, ex);
            }
            return false;
        }

    }
}
