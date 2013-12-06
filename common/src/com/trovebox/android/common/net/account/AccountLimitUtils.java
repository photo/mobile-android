
package com.trovebox.android.common.net.account;

import com.trovebox.android.common.util.LoadingControl;

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
            final LoadingControl loadingControl
            )
    {
        runnable.run();
    }
}
