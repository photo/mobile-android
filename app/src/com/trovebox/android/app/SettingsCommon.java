
package com.trovebox.android.app;

import org.holoeverywhere.app.Activity;
import org.holoeverywhere.app.AlertDialog;
import org.holoeverywhere.preference.CheckBoxPreference;
import org.holoeverywhere.preference.EditTextPreference;
import org.holoeverywhere.preference.Preference;
import org.holoeverywhere.preference.Preference.OnPreferenceChangeListener;
import org.holoeverywhere.preference.Preference.OnPreferenceClickListener;
import org.holoeverywhere.preference.PreferenceCategory;

import android.content.DialogInterface;
import android.content.Intent;

import com.trovebox.android.app.bitmapfun.util.ImageCacheUtils;
import com.trovebox.android.app.common.CommonFragmentUtils;
import com.trovebox.android.app.common.CommonRetainedFragmentWithTaskAndProgress;
import com.trovebox.android.app.facebook.FacebookProvider;
import com.trovebox.android.app.facebook.FacebookSessionEvents;
import com.trovebox.android.app.facebook.FacebookSessionEvents.LogoutListener;
import com.trovebox.android.app.facebook.FacebookUtils;
import com.trovebox.android.app.provider.UploadsUtils;
import com.trovebox.android.app.twitter.TwitterUtils;
import com.trovebox.android.app.util.GuiUtils;
import com.trovebox.android.app.util.TrackerUtils;

public class SettingsCommon implements
        OnPreferenceClickListener {
    static final String TAG = SettingsCommon.class.getSimpleName();
    Activity activity;
    Preference mLoginPreference;
    Preference mFacebookLoginPreference;
    Preference mSyncClearPreference;
    Preference diskCacheClearPreference;
    PreferenceCategory loginCategory;
    Preference mServerUrl;
    Preference autoUploadTagPreference;

    public SettingsCommon(Activity activity) {
        this.activity = activity;
        getLogoutFragment();
        getClearDiskCachesFragment();
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (activity.getString(R.string.setting_account_loggedin_key)
                .equals(
                        preference.getKey()))
        {
            if (Preferences.isLoggedIn(activity))
            {

                // confirm if user wants to log out
                new AlertDialog.Builder(activity, R.style.Theme_Trovebox_Dialog_Light)
                        .setTitle(R.string.logOut)
                        .setMessage(R.string.areYouSureQuestion)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(R.string.yes,
                                new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(
                                            DialogInterface dialog,
                                            int whichButton) {
                                        TrackerUtils.trackButtonClickEvent("setting_logout",
                                                activity);
                                        getLogoutFragment().doLogout();
                                    }
                                })
                        .setNegativeButton(R.string.no, null)
                        .show();

            } else
            {
                getLogoutFragment().finishActivity();
            }
        } else if (activity.getString(
                R.string.setting_account_facebook_loggedin_key)
                .equals(preference.getKey()))
        {
            LogoutListener logoutListener = new LogoutListener() {

                @Override
                public void onLogoutBegin() {
                }

                @Override
                public void onLogoutFinish() {
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

    public void refresh() {
        mLoginPreference.setTitle(Preferences.isLoggedIn(activity) ?
                R.string.setting_account_loggedin_logout
                : R.string.setting_account_loggedin_login);
    }

    public Preference getLoginPreference() {
        return mLoginPreference;
    }

    public void setLoginPreference(Preference mLoginPreference) {
        this.mLoginPreference = mLoginPreference;
        this.mLoginPreference.setOnPreferenceClickListener(this);
    }

    public Preference getFacebookLoginPreference() {
        return mFacebookLoginPreference;
    }

    public void setFacebookLoginPreference(
            Preference mFacebookLoginPreference) {
        this.mFacebookLoginPreference = mFacebookLoginPreference;
        this.mFacebookLoginPreference.setOnPreferenceClickListener(this);
        if (FacebookProvider.getFacebook() == null
                || !FacebookProvider.getFacebook().isSessionValid())
        {
            loginCategory.removePreference(mFacebookLoginPreference);
        }
    }

    public void setAutoUploadTagPreference(
            Preference autoUploadTagPreference) {
        this.autoUploadTagPreference = autoUploadTagPreference;
        this.autoUploadTagPreference
                .setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        TrackerUtils.trackPreferenceChangeEvent("settings_auto_upload_tag",
                                activity);
                        SettingsCommon.this.autoUploadTagPreference.setSummary(newValue.toString());
                        return true;
                    }
                });
        autoUploadTagPreference.setSummary(Preferences.getAutoUploadTag(activity));
    }

    public void setWiFiOnlyUploadPreference(
            Preference wiFiOnlyUploadPreference) {
        wiFiOnlyUploadPreference
                .setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        TrackerUtils.trackPreferenceChangeEvent("settings_wifi_only_upload_on",
                                newValue,
                                activity);
                        return true;
                    }
                });
    }

    public void setAutoUploadPreference(
            Preference autoUploadPreference) {
        autoUploadPreference
                .setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        TrackerUtils.trackPreferenceChangeEvent("settings_autoupload_on", newValue,
                                activity);
                        return true;
                    }
                });
        if (Preferences.isLimitedAccountAccessType()) {
            autoUploadPreference.setEnabled(false);
            ((CheckBoxPreference) autoUploadPreference).setChecked(false);
        }
    }

    public PreferenceCategory getLoginCategory() {
        return loginCategory;
    }

    public void setLoginCategory(PreferenceCategory loginCategory) {
        this.loginCategory = loginCategory;
    }

    public Preference getServerUrl() {
        return mServerUrl;
    }

    public void setServerUrl(Preference mServerUrl) {
        this.mServerUrl = mServerUrl;
        mServerUrl.setSummary(Preferences.getServer(activity));
        mServerUrl
                .setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference,
                            Object newValue) {
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

    public void setSyncClearPreference(Preference mSyncClearPreference) {
        this.mSyncClearPreference = mSyncClearPreference;
        mSyncClearPreference
                .setOnPreferenceClickListener(new OnPreferenceClickListener() {

                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        // confirm if user wants to clear sync information
                        new AlertDialog.Builder(activity, R.style.Theme_Trovebox_Dialog_Light)
                                .setTitle(R.string.sync_clear)
                                .setMessage(R.string.areYouSureQuestion)
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setPositiveButton(R.string.yes,
                                        new DialogInterface.OnClickListener() {

                                            @Override
                                            public void onClick(
                                                    DialogInterface dialog,
                                                    int whichButton) {
                                                TrackerUtils.trackButtonClickEvent(
                                                        "setting_sync_clear", activity);
                                                UploadsUtils.clearUploadsAsync();
                                            }
                                        })
                                .setNegativeButton(R.string.no, null)
                                .show();

                        return true;
                    }
                });
    }

    public void setDiskCachClearPreference(Preference diskCacheClearPreference) {
        this.diskCacheClearPreference = diskCacheClearPreference;
        diskCacheClearPreference
                .setOnPreferenceClickListener(new OnPreferenceClickListener() {

                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        // confirm if user wants to clear sync information
                        new AlertDialog.Builder(activity, R.style.Theme_Trovebox_Dialog_Light)
                                .setTitle(R.string.disk_cache_clear)
                                .setMessage(R.string.areYouSureQuestion)
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setPositiveButton(R.string.yes,
                                        new DialogInterface.OnClickListener() {

                                            @Override
                                            public void onClick(
                                                    DialogInterface dialog,
                                                    int whichButton) {
                                                TrackerUtils.trackButtonClickEvent(
                                                        "setting_disk_cache_clear", activity);
                                                getClearDiskCachesFragment().clearDiskCaches();
                                            }
                                        })
                                .setNegativeButton(R.string.no, null)
                                .show();

                        return true;
                    }
                });
    }

    /**
     * Get the logout fragment. Create it if it is null
     * 
     * @return
     */
    LogoutFragment getLogoutFragment() {
        return CommonFragmentUtils.findOrCreateFragment(LogoutFragment.class,
                activity.getSupportFragmentManager());
    }

    /**
     * Get the clear disk caches fragment. Create it if it is null
     * 
     * @return
     */
    ClearDiskCachesFragment getClearDiskCachesFragment() {
        return CommonFragmentUtils.findOrCreateFragment(ClearDiskCachesFragment.class,
                activity.getSupportFragmentManager());
    }

    public static class LogoutFragment extends CommonRetainedFragmentWithTaskAndProgress {
        private static final String TAG = LogoutFragment.class.getSimpleName();

        public void doLogout() {
            startRetainedTask(new LogoutUserTask());
        }

        public void finishActivity() {
            Activity activity = getSupportActivity();
            if (activity != null)
            {
                activity.startActivity(new Intent(activity, AccountActivity.class));
                activity.finish();
            } else
            {
                TrackerUtils.trackErrorEvent("activity_null", TAG);
            }
        }

        class LogoutUserTask extends RetainedTask {
            @Override
            protected Boolean doInBackground(Void... params) {
                try
                {
                    UploadsUtils.clearUploads();
                    FacebookUtils.logoutRequest(TroveboxApplication.getContext());
                    TwitterUtils.logout(TroveboxApplication.getContext());
                    ImageCacheUtils.clearDiskCaches();
                    return true;
                } catch (Exception ex)
                {
                    GuiUtils.noAlertError(TAG, ex);
                }
                return false;
            }

            @Override
            protected void onSuccessPostExecuteAdditional() {
                try
                {
                    Preferences
                            .logout(getSupportActivity());
                    finishActivity();
                } catch (Exception e)
                {
                    GuiUtils.error(TAG, e);
                }
            }
        }
    }

    public static class ClearDiskCachesFragment extends CommonRetainedFragmentWithTaskAndProgress {

        public void clearDiskCaches() {
            startRetainedTask(new ClearDiskCachesTask());
        }

        class ClearDiskCachesTask extends RetainedTask {
            @Override
            protected Boolean doInBackground(Void... params) {
                return ImageCacheUtils.clearDiskCaches();
            }

            @Override
            protected void onSuccessPostExecuteAdditional() {
                GuiUtils.info(R.string.disk_caches_cleared_message);
                ImageCacheUtils.sendDiskCacheClearedBroadcast();
            }
        }
    }
}
