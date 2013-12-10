
package com.trovebox.android.app.net.account;

import com.trovebox.android.app.Preferences;
import com.trovebox.android.app.R;
import com.trovebox.android.app.TroveboxApplication;
import com.trovebox.android.app.net.ProfileResponseUtils;
import com.trovebox.android.app.net.SystemVersionResponseUtils;
import com.trovebox.android.common.model.ProfileInformation;
import com.trovebox.android.common.model.ProfileInformation.ProfileLimits;
import com.trovebox.android.common.net.ITroveboxApi;
import com.trovebox.android.common.net.ProfileResponse;
import com.trovebox.android.common.net.TroveboxResponseUtils;
import com.trovebox.android.common.util.CommonUtils;
import com.trovebox.android.common.util.GuiUtils;
import com.trovebox.android.common.util.LoadingControl;
import com.trovebox.android.common.util.RunnableWithParameter;
import com.trovebox.android.common.util.SimpleAsyncTaskEx;
import com.trovebox.android.common.util.TrackerUtils;

/**
 * Various utils for upload limits
 * 
 * @author Eugene Popovich
 */
public class AccountLimitUtils2 {
    private static final String LIMIT_INFORMATION_CACHE_UPDATE_EVENT = "limit_information_cache_update";
    private static String TAG = AccountLimitUtils2.class.getSimpleName();

    /**
     * Try to refresh upload limit information cache and run action which may
     * access cached values
     * 
     * @param runnable run in case successful cache update or in case user is
     *            logged in but not online
     * @param runnableOnFailure run in case of cache update failure or in case
     *            user is not logged in
     * @param loadingControl
     */
    public static void tryToRefreshLimitInformationAndRunInContextAsync(
            final Runnable runnable,
            final Runnable runnableOnFailure,
            LoadingControl loadingControl)
    {
        if (GuiUtils.checkLoggedInAndOnline(true))
        {
            CommonUtils.debug(TAG,
                    "Logged in and online. Running actions in ProfileResponse context.");
            ProfileResponseUtils.runWithProfileResponseAsync(true,
                    new RunnableWithParameter<ProfileResponse>() {

                        @Override
                        public void run(ProfileResponse parameter) {
                            saveLimitInformationToCache(parameter);
                            TrackerUtils.trackLimitEvent("limit_information_context_run",
                                    "refreshed");
                            runnable.run();
                        }
                    }, runnableOnFailure, loadingControl);
        } else
        {
            if (Preferences.isLoggedIn())
            {
                TrackerUtils.trackLimitEvent("limit_information_context_run", "cached");
                CommonUtils.debug(TAG,
                        "Logged in but not online. Running action in cached context.");
                runnable.run();
            } else
            {
                TrackerUtils.trackLimitEvent("limit_information_context_run", "failure");
                CommonUtils.debug(TAG, "Not logged in and not online. Running on failure action.");
                if (runnableOnFailure != null)
                {
                    runnableOnFailure.run();
                }
            }
        }
    }

    /**
     * Save the limit information to the cache. In case limits section is missed
     * store the maximum possible limit value
     * 
     * @param response
     * @return true if response is successful and information was saved to
     *         cache. Othwerwise returns false
     */
    public static boolean saveLimitInformationToCache(ProfileResponse response)
    {
        boolean result = false;
        if (response.isSuccess())
        {
            ProfileInformation profileInformation = response.getProfileInformation();
            Preferences.setProUser(profileInformation.isPaid());
            ProfileLimits limits = profileInformation.getLimits();
            if (limits != null)
            {
                Preferences.setRemainingUploadingLimit(limits.getRemaining());
                Preferences.setUploadLimitResetsOnDate(limits.getResetsOn());
            } else
            {
                Preferences.setRemainingUploadingLimit(Integer.MAX_VALUE);
            }
            if (profileInformation.getViewer() != null) {
                Preferences.setAccessPermissions(profileInformation.getViewer().getPermissions());
            }
            TrackerUtils.trackBackgroundEvent(LIMIT_INFORMATION_CACHE_UPDATE_EVENT, "success");
            result = true;
        } else
        {
            TrackerUtils.trackBackgroundEvent(LIMIT_INFORMATION_CACHE_UPDATE_EVENT, "fail");
        }
        return result;
    }

    /**
     * Update the limit information cache asynchronously. Operation will be done
     * in separate async task thread
     * 
     * @param loadingControl
     */
    public static void updateLimitInformationCacheAsync(LoadingControl loadingControl)
    {
        CommonUtils.debug(TAG, "Async update limit information cache request");
        new UpdateLimitInformationCacheTask(loadingControl);
    }

    /**
     * Update the limit information cache if necessary. Should not be called in
     * UI thread. The method first tries to perofrm system version
     * information cache retrieval and updates limit information
     * only in case installation is hosted
     * 
     * @param silent whether to do not notify user about an errors
     */
    public static boolean updateLimitInformationCacheIfNecessary(boolean silent)
    {
        if (!SystemVersionResponseUtils.updateSystemVersionCacheIfNecessary(silent))
        {
            return false;
        }
        if (Preferences.isSelfHosted())
        {
            return true;
        } else
        {
            return updateLimitInformationCache(silent);
        }
    }

    /**
     * Update the limit information cache. Should not be called in UI thread
     * @param silent whether to do not notify user about an errors
     * @return
     */
    public static boolean updateLimitInformationCache(boolean silent)
    {
        try
        {
            if (GuiUtils.checkLoggedInAndOnline(silent))
            {
                TrackerUtils.trackBackgroundEvent(LIMIT_INFORMATION_CACHE_UPDATE_EVENT, "started");
                CommonUtils.debug(TAG, "Update limit information cache request");
                ITroveboxApi api = Preferences.getApi(TroveboxApplication.getContext());
                ProfileResponse response = api.getProfile(false);
                if (silent || TroveboxResponseUtils.checkResponseValid(response))
                {
                    return AccountLimitUtils2.saveLimitInformationToCache(response);
                } else
                {
                    TrackerUtils.trackBackgroundEvent(LIMIT_INFORMATION_CACHE_UPDATE_EVENT, "fail");
                }
            } else
            {
                TrackerUtils.trackBackgroundEvent(LIMIT_INFORMATION_CACHE_UPDATE_EVENT,
                        "skipped_not_logged_in_or_not_online");
            }
        } catch (Exception ex)
        {
            GuiUtils.processError(TAG, R.string.errorCouldNotRetrieveProfileInfo, ex, null,
                    !silent);
        }
        return false;
    }

    private static class UpdateLimitInformationCacheTask extends SimpleAsyncTaskEx
    {
        public UpdateLimitInformationCacheTask(LoadingControl loadingControl) {
            super(loadingControl);
        }

        @Override
        protected void onSuccessPostExecute() {
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try
            {
                updateLimitInformationCacheIfNecessary(true);
                return true;
            } catch (Exception ex)
            {
                GuiUtils.noAlertError(TAG, ex);
            }
            return false;
        }
    }
}
