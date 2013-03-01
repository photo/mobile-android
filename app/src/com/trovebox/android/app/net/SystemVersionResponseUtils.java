package com.trovebox.android.app.net;

import com.trovebox.android.app.Preferences;
import com.trovebox.android.app.R;
import com.trovebox.android.app.TroveboxApplication;
import com.trovebox.android.app.util.CommonUtils;
import com.trovebox.android.app.util.GuiUtils;
import com.trovebox.android.app.util.LoadingControl;
import com.trovebox.android.app.util.SimpleAsyncTaskEx;
import com.trovebox.android.app.util.TrackerUtils;

/**
 * Utils for {@link SystemVersionResponse}
 * 
 * @author Eugene Popovich
 */
public class SystemVersionResponseUtils {
    private static final String SYSTEM_VERSION_CACHE_UPDATE_EVENT = "system_version_cache_update";
    private static final String TAG = SystemVersionResponseUtils.class.getSimpleName();

    /**
     * Run runnable in context of updated system version cache asynchronously
     * 
     * @param runnable to run if cache is already updated or async update
     *            performed successfully
     * @param loadingControl
     */
    public static void tryToUpdateSystemVersionCacheIfNecessaryAndRunInContextAsync(
            final Runnable runnable,
            LoadingControl loadingControl)
    {
        tryToUpdateSystemVersionCacheIfNecessaryAndRunInContextAsync(runnable, null, loadingControl);
    }

    /**
     * Run runnable in context of updated system version cache asynchronously
     * 
     * @param runnable to run if cache is already updated or async update
     *            performed successfully
     * @param runnableOnFailure to run in case cache update failed
     * @param loadingControl
     */
    public static void tryToUpdateSystemVersionCacheIfNecessaryAndRunInContextAsync(
            final Runnable runnable,
            final Runnable runnableOnFailure,
            LoadingControl loadingControl)
    {
        if (Preferences.isSystemVersionInformationCached())
        {
            CommonUtils.debug(TAG, "Update system version information cache skipped");
            TrackerUtils.trackBackgroundEvent(SYSTEM_VERSION_CACHE_UPDATE_EVENT, "skipped");
            runnable.run();
        } else
        {
            new UpdateSystemVersionCacheTask(runnable, runnableOnFailure, loadingControl).execute();
        }
    }
    
    /**
     * Update the system version cache only if it is not yet saved. Should not
     * be run in UI thread
     * 
     * @param silent whether to do not show error messages
     * @return true if system version cache is already updated or was updated
     *         successfully
     */
    public static boolean updateSystemVersionCacheIfNecessary(boolean silent)
    {
        if (Preferences.isSystemVersionInformationCached())
        {
            CommonUtils.debug(TAG, "Update system version information cache skipped");
            TrackerUtils.trackBackgroundEvent(SYSTEM_VERSION_CACHE_UPDATE_EVENT, "skipped");
            return true;
        } else
        {
            CommonUtils.debug(TAG, "Update system version information cache requested");
            TrackerUtils.trackBackgroundEvent(SYSTEM_VERSION_CACHE_UPDATE_EVENT, "requested");
            return updateSystemVersionCache(silent);
        }
    }

    /**
     * Update the system version cache
     * 
     * @param silent whether to do not show error messages
     * @return
     */
    public static boolean updateSystemVersionCache(boolean silent)
    {
        try
        {
            if (CommonUtils.checkLoggedInAndOnline(silent))
            {
                TrackerUtils.trackBackgroundEvent(SYSTEM_VERSION_CACHE_UPDATE_EVENT, "started");
                CommonUtils.debug(TAG, "Update system version information cache started");
                ITroveboxApi api = Preferences.getApi(TroveboxApplication.getContext());
                SystemVersionResponse response = api.getSystemVersion();
                if (silent || TroveboxResponseUtils.checkResponseValid(response))
                {
                    return saveSystemInfoInformationCache(response);
                } else
                {
                    CommonUtils.debug(TAG, "Update system version information cache failed");
                    TrackerUtils.trackBackgroundEvent(SYSTEM_VERSION_CACHE_UPDATE_EVENT, "fail");
                }
            } else
            {
                TrackerUtils.trackBackgroundEvent(SYSTEM_VERSION_CACHE_UPDATE_EVENT,
                        "skipped_not_logged_in_or_not_online");
            }
        } catch (Exception ex)
        {
            GuiUtils.processError(TAG, R.string.errorCouldNotRetrieveSystemVersionInfo, ex, null,
                    !silent);
        }
        return false;
    }

    /**
     * Save system info information to cache
     * 
     * @param response
     * @return
     */
    private static boolean saveSystemInfoInformationCache(SystemVersionResponse response)
    {
        boolean result = false;
        if (response.isSuccess())
        {
            Preferences.setHosted(response.isHosted());
            Preferences.setSystemVersionInformationCached(true);
            TrackerUtils.trackBackgroundEvent(SYSTEM_VERSION_CACHE_UPDATE_EVENT, "success");
            CommonUtils.debug(TAG, "Update system version information cache successful");
            result = true;
        } else
        {
            TrackerUtils.trackBackgroundEvent(SYSTEM_VERSION_CACHE_UPDATE_EVENT, "fail");
            CommonUtils.debug(TAG, "Update system version information cache failed");
        }
        return result;
    }

    /**
     * The async task to update system version cache
     */
    private static class UpdateSystemVersionCacheTask extends SimpleAsyncTaskEx
    {
        Runnable runnable;
        Runnable runnableOnFailure;

        public UpdateSystemVersionCacheTask(Runnable runnable,
                Runnable runnableOnFailure, LoadingControl loadingControl) {
            super(loadingControl);
            this.runnable = runnable;
            this.runnableOnFailure = runnableOnFailure;
        }

        @Override
        protected void onSuccessPostExecute() {
            runnable.run();
        }

        @Override
        protected void onFailedPostExecute() {
            super.onFailedPostExecute();
            if (runnableOnFailure != null)
            {
                runnableOnFailure.run();
            }
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            return updateSystemVersionCache(false);
        }

    }
}
