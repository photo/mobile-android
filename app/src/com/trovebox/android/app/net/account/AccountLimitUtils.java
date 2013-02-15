
package com.trovebox.android.app.net.account;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.trovebox.android.app.Preferences;
import com.trovebox.android.app.R;
import com.trovebox.android.app.TroveboxApplication;
import com.trovebox.android.app.net.ITroveboxApi;
import com.trovebox.android.app.net.ProfileResponse;
import com.trovebox.android.app.net.ProfileResponse.ProfileLimits;
import com.trovebox.android.app.net.ProfileResponseUtils;
import com.trovebox.android.app.provider.UploadsProviderAccessor;
import com.trovebox.android.app.util.CommonUtils;
import com.trovebox.android.app.util.GuiUtils;
import com.trovebox.android.app.util.LoadingControl;
import com.trovebox.android.app.util.RunnableWithParameter;
import com.trovebox.android.app.util.TrackerUtils;

/**
 * Various utils for upload limits
 * 
 * @author Eugene Popovich
 */
public class AccountLimitUtils {
    private static String TAG = AccountLimitUtils.class.getSimpleName();

    /**
     * Check whether there is a quota for one upload and run
     * 
     * @param runnable run in case user can upload at least one more photo
     * @param loadingControl
     */
    public static void checkQuotaPerOneUploadAvailableAndRunAsync(
            final Runnable runnable,
            LoadingControl loadingControl
            )
    {
        checkQuotaPerOneUploadAvailableAndRunAsync(runnable, null, loadingControl);
    }

    /**
     * Check whether there is a quota for one upload and run
     * 
     * @param runnable run in case user can upload at least one more photo
     * @param runnableOnFailure run in case quota check failure. Could be null.
     * @param loadingControl
     */
    public static void checkQuotaPerOneUploadAvailableAndRunAsync(
            final Runnable runnable,
            final Runnable runnableOnFailure,
            LoadingControl loadingControl
            )
    {
        checkQuotaPerUploadAvailableAndRunAsync(runnable, runnableOnFailure, 1, loadingControl);
    }

    /**
     * Check whether there is a quota for multiple uploads and run
     * 
     * @param runnable run in case user can upload the number of images
     *            specified in requiredUploadSlotsCount variable
     * @param requiredUploadSlotsCount requested number of images to upload
     * @param loadingControl
     */
    public static void checkQuotaPerUploadAvailableAndRunAsync(
            final Runnable runnable,
            final int requiredUploadSlotsCount,
            LoadingControl loadingControl
            )
    {
        checkQuotaPerUploadAvailableAndRunAsync(runnable, null, requiredUploadSlotsCount,
                loadingControl);
    }

    /**
     * Check whether there is a quota for multiple uploads and run
     * 
     * @param runnable run in case user can upload the number of images
     *            specified in requiredUploadSlotsCount variable
     * @param runnableOnFailure run in case quota check failure. Can be null
     * @param requiredUploadSlotsCount requested number of images to upload
     * @param loadingControl
     */
    public static void checkQuotaPerUploadAvailableAndRunAsync(
            final Runnable runnable,
            final Runnable runnableOnFailure,
            final int requiredUploadSlotsCount,
            LoadingControl loadingControl
            )
    {
        tryToRefreshLimitInformationAndRunInContextAsync(new Runnable() {

            @Override
            public void run() {

                if (Preferences.isProUser())
                {
                    runnable.run();
                } else
                {
                    int remaining = Preferences.getRemainingUploadingLimit();
                    UploadsProviderAccessor uploads = new UploadsProviderAccessor(
                            TroveboxApplication.getContext());
                    int pending = uploads.getPendingUploadsCount();
                    if (remaining - pending >= requiredUploadSlotsCount)
                    {
                        CommonUtils.debug(TAG, "Quota check passed");
                        if (requiredUploadSlotsCount == 1)
                        {
                            TrackerUtils.trackLimitEvent("quota_per_one_image_check", "success");
                        } else
                        {
                            TrackerUtils.trackLimitEvent("quota_per_multiple_images_check",
                                    "success");
                        }
                        runnable.run();
                    } else
                    {
                        CommonUtils.debug(TAG, "Quota check failed");
                        if (requiredUploadSlotsCount == 1)
                        {
                            TrackerUtils.trackLimitEvent("quota_per_one_image_check", "fail");
                            Date resetsOnDate = Preferences.getUploadLimitResetsOnDate();
                            if (resetsOnDate == null)
                            {
                                GuiUtils.alert(R.string.upload_limit_reached_message);
                            } else
                            {
                                GuiUtils.alert(
                                        R.string.upload_limit_reached_with_reset_message,
                                        SimpleDateFormat.getInstance().format(resetsOnDate));
                            }
                        } else
                        {
                            TrackerUtils.trackLimitEvent("quota_per_multiple_images_check", "fail");
                            GuiUtils.alert(
                                    R.string.upload_limit_for_multiple_upload_reached_message,
                                    requiredUploadSlotsCount, remaining - pending);
                        }
                        if (runnableOnFailure != null)
                        {
                            runnableOnFailure.run();
                        }
                    }
                }
            }
        }, runnableOnFailure, loadingControl);
    }

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
        if (CommonUtils.checkLoggedInAndOnline(true))
        {
            CommonUtils.debug(TAG,
                    "Logged in and online. Running actions in ProfileResponse context.€");
            ProfileResponseUtils.runWithProfileResponseAsync(
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
     */
    public static void saveLimitInformationToCache(ProfileResponse response)
    {
        if (response.isSuccess())
        {
            Preferences.setProUser(response.isPaid());
            ProfileLimits limits = response.getLimits();
            if (limits != null)
            {
                Preferences.setRemainingUploadingLimit(limits.getRemaining());
                Preferences.setUploadLimitResetsOnDate(limits.getResetsOn());
            } else
            {
                Preferences.setRemainingUploadingLimit(Integer.MAX_VALUE);
            }
        }
    }

    /**
     * Update the limit information cache. Should not be called in UI thread
     */
    public static void updateLimitInformationCache()
    {
        try
        {
            if (CommonUtils.checkLoggedInAndOnline(true))
            {
                ITroveboxApi api = Preferences.getApi(TroveboxApplication.getContext());
                ProfileResponse response = api.getProfile();
                AccountLimitUtils.saveLimitInformationToCache(response);
            }
        } catch (Exception ex)
        {
            GuiUtils.noAlertError(TAG, ex);
        }
    }
}
