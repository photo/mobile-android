
package com.trovebox.android.app.net.account;

import com.trovebox.android.app.Preferences;
import com.trovebox.android.app.R;
import com.trovebox.android.app.TroveboxApplication;
import com.trovebox.android.app.net.TroveboxResponseUtils;
import com.trovebox.android.app.purchase.util.Purchase;
import com.trovebox.android.app.util.CommonUtils;
import com.trovebox.android.app.util.GuiUtils;
import com.trovebox.android.app.util.LoadingControl;
import com.trovebox.android.app.util.SimpleAsyncTaskEx;
import com.trovebox.android.app.util.TrackerUtils;

/**
 * Utils for {@link PaymentVerificationResponse}
 * 
 * @author Eugene Popovich
 */
public class PaymentVerificationResponseUtil {

    private static final String TAG = PaymentVerificationResponseUtil.class.getSimpleName();
    private static final String PURCHASE_VERIFICATION_EVENT = "purchase_verification";

    /**
     * Verify purchase on the server and run async
     * 
     * @param runnable run in case purchase verified successfully
     * @param runnableOnFailure run in case purchase verification failed
     * @param purchase the purchase to verify on the server
     * @param loadingControl
     */
    public static void verifyPurchaseAndRunAsync(
            final Runnable runnable,
            final Runnable runnableOnFailure,
            final Purchase purchase,
            final LoadingControl loadingControl
            )
    {
        if (purchase == null)
        {
            CommonUtils.debug(TAG, "purchase verification failed: null parameter");
            TrackerUtils.trackInAppBillingEvent(PURCHASE_VERIFICATION_EVENT, "null_parameter");
            if (runnableOnFailure != null)
            {
                runnableOnFailure.run();
            }
            return;
        }
        // check whether the purchase was already verified before
        if (Preferences.isPurchaseVerified(purchase))
        {
            CommonUtils.debug(TAG, "Purchase verification skipped: already processed");
            TrackerUtils.trackInAppBillingEvent(PURCHASE_VERIFICATION_EVENT,
                    "skipped_already_verified");
            runnable.run();
        } else
        {
            if (purchase.isInPurchasedState())
            {
                CommonUtils.debug(TAG, "Purchase verification state is purchased");
                TrackerUtils.trackInAppBillingEvent(PURCHASE_VERIFICATION_EVENT,
                        "in_purchase_state");
                if (CommonUtils.checkLoggedInAndOnline(true))
                {
                    CommonUtils.debug(TAG, "Purchase verification requested");
                    TrackerUtils.trackInAppBillingEvent(PURCHASE_VERIFICATION_EVENT,
                            "server_verification_request");
                    new PaymentVerificationTask(purchase, runnable, runnableOnFailure,
                            loadingControl)
                            .execute();
                } else
                {
                    TrackerUtils.trackInAppBillingEvent(PURCHASE_VERIFICATION_EVENT,
                            "failed_not_logged_in_or_not_online");
                    CommonUtils.debug(TAG,
                            "Purchase verification failed: not logged in or not online");
                    if (runnableOnFailure != null)
                    {
                        runnableOnFailure.run();
                    }
                }
            } else
            {
                CommonUtils.debug(TAG, "Purchase verification state is invalid: %1$d",
                        purchase.getPurchaseState());
                TrackerUtils.trackInAppBillingEvent(PURCHASE_VERIFICATION_EVENT,
                        CommonUtils.format("invalid_state: %1$d", purchase.getPurchaseState()));
                if (runnableOnFailure != null)
                {
                    runnableOnFailure.run();
                }
            }
        }
    }

    /**
     * The payment verification async task
     */
    private static class PaymentVerificationTask extends SimpleAsyncTaskEx
    {
        Runnable runnable;
        Runnable runnableOnFailure;
        Purchase purchase;

        public PaymentVerificationTask(
                Purchase purchase,
                Runnable runnable,
                Runnable runnableOnFailure,
                LoadingControl loadingControl) {
            super(loadingControl);
            this.purchase = purchase;
            this.runnable = runnable;
            this.runnableOnFailure = runnableOnFailure;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try
            {
                IAccountTroveboxApi api = IAccountTroveboxApiFactory.getApi(
                        TroveboxApplication.getContext());
                PaymentVerificationResponse response = api.verifyPayment(
                        purchase.getDeveloperPayload(), purchase);
                return TroveboxResponseUtils.checkResponseValid(response);
            } catch (Exception ex)
            {
                GuiUtils.error(TAG,
                        R.string.errorCouldNotVerifyPayment,
                        ex);
            }
            return false;
        }

        @Override
        protected void onSuccessPostExecute()
        {
            CommonUtils.debug(TAG, "Purchase verification successful",
                    purchase.getPurchaseState());
            TrackerUtils.trackInAppBillingEvent(PURCHASE_VERIFICATION_EVENT, "success");
            runnable.run();
            Preferences.setPurchaseVerified(purchase, true);
        }

        @Override
        protected void onFailedPostExecute() {
            super.onFailedPostExecute();
            TrackerUtils.trackInAppBillingEvent(PURCHASE_VERIFICATION_EVENT, "fail");
            if (runnableOnFailure != null)
            {
                runnableOnFailure.run();
            }
        }
    }
}
