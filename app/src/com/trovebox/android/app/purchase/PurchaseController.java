
package com.trovebox.android.app.purchase;

import org.holoeverywhere.app.Activity;

import android.content.Context;
import android.content.Intent;

import com.google.analytics.tracking.android.Transaction;
import com.google.analytics.tracking.android.Transaction.Item;
import com.trovebox.android.app.Preferences;
import com.trovebox.android.app.R;
import com.trovebox.android.app.model.ProfileInformation;
import com.trovebox.android.app.net.ProfileResponseUtils;
import com.trovebox.android.app.net.account.AccountLimitUtils;
import com.trovebox.android.app.net.account.PaymentVerificationResponseUtil;
import com.trovebox.android.app.purchase.util.IabHelper;
import com.trovebox.android.app.purchase.util.IabResult;
import com.trovebox.android.app.purchase.util.Inventory;
import com.trovebox.android.app.purchase.util.Purchase;
import com.trovebox.android.app.purchase.util.StringXORer;
import com.trovebox.android.app.util.CommonUtils;
import com.trovebox.android.app.util.GuiUtils;
import com.trovebox.android.app.util.LoadingControl;
import com.trovebox.android.app.util.ObjectAccessor;
import com.trovebox.android.app.util.RunnableWithParameter;
import com.trovebox.android.app.util.TrackerUtils;

/**
 * The purchase controller class which is used to setup billing and process
 * purchase requests
 * 
 * @author Eugene Popovich
 */
public class PurchaseController {
    private static final String TAG = PurchaseController.class.getSimpleName();

    // SKU for subscription (monthly)
    static final String SKU_MONTHLY_SUBSCRIPTION = "monthly_subscription";
    /**
     * The crypt key for the encoded application public key value
     */
    public static String cryptKey = "123ffNNNbblm";

    /**
     * Static OnIabPurchaseFinishedListener reference which will be reused in
     * case activity was recreated because of orientation change
     */
    static IabHelper.OnIabPurchaseFinishedListener useOncePurchaseListener;
    /**
     * Static reference to the last purchasing item type. To avoid lost of
     * information in case orientation is changed during purchase
     */
    static String lastPurchasingItemType;
    /**
     * The helper object
     */
    IabHelper mHelper;

    /*
     */
    /**
     * Get the encrypted application public key <br>
     * base64EncodedPublicKey should be YOUR APPLICATION'S PUBLIC KEY (that you
     * got from the Google Play developer console). This is not your developer
     * public key, it's the *app-specific* public key. Instead of just storing
     * the entire literal string here embedded in the program, construct the key
     * at runtime from pieces or use bit manipulation (for example, XOR with
     * some other string) to hide the actual key. The key itself is not secret
     * information, but we don't want to make it easy for an attacker to replace
     * the public key with one of their own and then fake messages from the
     * server.
     * 
     * @param context
     * @return
     */
    public static String getBase64EncodedPublicKeyCrypted(Context context)
    {
        return context.getString(R.string.application_public_key);
    }

    /**
     * Get the already configured instance of {@link PurchaseController}
     * 
     * @param context
     * @param loadingControl
     * @return
     */
    public static PurchaseController getAndSetup(Context context, LoadingControl loadingControl)
    {
        PurchaseController controller = new PurchaseController(context);
        controller.setup(loadingControl);
        return controller;
    }

    /**
     * @param context
     */
    public PurchaseController(Context context)
    {
        CommonUtils.debug(TAG, "Creating IAB helper.");
        String base64EncodedPublicKey = getBase64EncodedPublicKeyCrypted(context);
        base64EncodedPublicKey = StringXORer.decode(base64EncodedPublicKey, cryptKey);
        mHelper = new IabHelper(context, base64EncodedPublicKey);
    }

    /**
     * Setup the purchase controller
     * 
     * @param loadingControl
     */
    public void setup(final LoadingControl loadingControl)
    {
        // enable debug logging (for a production application, you should set
        // this to false).
        mHelper.enableDebugLogging(false);

        // Start setup. This is asynchronous and the specified listener
        // will be called once setup completes.
        CommonUtils.debug(TAG, "Starting setup.");
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            @Override
            public void onIabSetupFinished(IabResult result) {
                CommonUtils.debug(TAG, "Setup finished.");

                TrackerUtils.trackInAppBillingEvent("setup_result", result.toString());
                if (!result.isSuccess()) {
                    // Oh noes, there was a problem.
                    errorWithTracking("Problem setting up in-app billing: " + result);
                    return;
                }

                // Hooray, IAB is fully set up. Now, let's get an inventory of
                // stuff we own.
                CommonUtils.debug(TAG, "Setup successful. Querying inventory.");
                // need to check whether mHelper is not null. It may be null in
                // case parent activity was destroyed such as action is async.
                // Issue #382
                if (mHelper != null)
                {
                    mHelper.queryInventoryAsync(new GotInventoryListener(loadingControl),
                            loadingControl);
                } else
                {
                    TrackerUtils.trackInAppBillingEvent("setup_result",
                            "fail: helper is null");
                }
            }
        });
    }

    /**
     * call this at Activity.onDestroy method
     */
    public void dispose()
    {
        CommonUtils.debug(TAG, "Destroying helper.");
        if (mHelper != null)
            mHelper.dispose();
        mHelper = null;
    }

    /**
     * Handle activity result
     * 
     * @param resultCode the activity result code
     * @param data the intent data passed to the onActivityResult method
     */
    public void handleActivityResult(int resultCode, Intent data) {
        // we need to set purchase listener and purchase item type to the
        // mHelper for a case it was recreated (orientation of the calling
        // activity changed)
        if (useOncePurchaseListener != null)
        {
            mHelper.setPurchaseListener(useOncePurchaseListener);
            useOncePurchaseListener = null;
        }
        if (lastPurchasingItemType != null)
        {
            mHelper.setPurchasingItemType(lastPurchasingItemType);
        }
        mHelper.handleActivityResult(resultCode, data);
    }

    /**
     * Write to logcat error message and track it via TrackerUtils
     * 
     * @param error
     */
    private static void errorWithTracking(String error)
    {
        CommonUtils.error(TAG, error);
        TrackerUtils.trackException(TAG + ":" + error);
        TrackerUtils.trackErrorEvent("in_app_billing_error", error);
    }

    /** Verifies the developer payload of a purchase. */
    static boolean verifyDeveloperPayload(Purchase p) {
        // String payload = p.getDeveloperPayload();

        /*
         * verify that the developer payload of the purchase is correct. It will
         * be the same one that you sent when initiating the purchase. WARNING:
         * Locally generating a random string when starting a purchase and
         * verifying it here might seem like a good approach, but this will fail
         * in the case where the user purchases an item on one device and then
         * uses your app on a different device, because on the other device you
         * will not have access to the random string you originally generated.
         * So a good developer payload has these characteristics: 1. If two
         * different users purchase an item, the payload is different between
         * them, so that one user's purchase can't be replayed to another user.
         * 2. The payload must be such that you can verify it even when the app
         * wasn't the one who initiated the purchase flow (so that items
         * purchased by the user on one device work on other devices owned by
         * the user). Using your own server to store and verify developer
         * payloads across app installations is recommended.
         */

        return true;
    }

    /**
     * Start purchase monthly subscription flow
     * 
     * @param activity calling activity
     * @param requestCode the request code for calling activity. It will be used
     *            in the onActivityResult method
     * @param loadingControlAccessor the serializable accessor for
     *            LoadingControl
     */
    public void purchaseMonthlySubscription(
            final Activity activity,
            final int requestCode,
            final ObjectAccessor<? extends LoadingControl> loadingControlAccessor)
    {
        CommonUtils.debug(TAG, "Requesting purchase for monthly subscription");
        TrackerUtils.trackInAppBillingEvent("purchase_request", "monthly_subscription");
        if (!mHelper.isSetupDone())
        {
            CommonUtils.debug(TAG,
                    "Purchase request cancelled: billing setup was not properly done");
            TrackerUtils.trackInAppBillingEvent("purchase_request",
                    "fail: unsuccessful setup");
            GuiUtils.alert(R.string.errorIabNotSupported);
            return;
        }
        if (!mHelper.subscriptionsSupported()) {
            CommonUtils.debug(TAG, "Purchase request cancelled: subscriptions are not supported");
            TrackerUtils.trackInAppBillingEvent("purchase_request",
                    "fail: subscriptions are not supported");
            GuiUtils.alert(R.string.errorSubscriptionNotSupported);
            return;
        }

        ProfileResponseUtils.runWithProfileInformationAsync(
                new RunnableWithParameter<ProfileInformation>() {

                    @Override
                    public void run(ProfileInformation parameter) {
                        CommonUtils
                                .debug(TAG,
                                        "Running purchase request for monthly subscription in profile response context");
                        TrackerUtils.trackInAppBillingEvent("purchase_request",
                                "monthly_subscription_run_in_context");
                        if (parameter.isPaid())
                        {
                            TrackerUtils.trackInAppBillingEvent("purchase_request",
                                    "skipped_already_pro");
                            GuiUtils.alert(R.string.iabNoNeedToUpgrade);
                            return;
                        }
                        /*
                         * for security, generate your payload here for
                         * verification. See the comments on
                         * verifyDeveloperPayload() for more info. Since this is
                         * a SAMPLE, we just use an empty string, but on a
                         * production app you should carefully generate this.
                         */
                        String payload = parameter.getEmail();

                        CommonUtils.debug(TAG,
                                "Launching purchase flow for premium subscription.");
                        if (mHelper != null)
                        {
                            useOncePurchaseListener = new PurchaseFinishedListener(
                                    loadingControlAccessor);
                            lastPurchasingItemType =
                                    IabHelper.ITEM_TYPE_SUBS;
                            mHelper.launchPurchaseFlow(activity,
                                    SKU_MONTHLY_SUBSCRIPTION, lastPurchasingItemType,
                                    requestCode, useOncePurchaseListener, payload);
                        } else
                        {
                            TrackerUtils.trackInAppBillingEvent("purchase_request",
                                    "fail: helper is null");
                        }
                    }
                }, loadingControlAccessor.run());
    }

    /**
     * Ecommerce GA tracking for subscription purchase
     * 
     * @param purchase
     */
    static void trackMonthlySubscriptionPurchaseIfNecessary(Purchase purchase)
    {
        try
        {
            if (purchase != null && purchase.isInPurchasedState()
                    && !Preferences.isPurchaseVerified(purchase))
            {
                Transaction myTrans = new Transaction.Builder(
                        purchase.getOrderId(), // (String) Transaction Id,
                                               // should be unique.
                        (long) (2.99 * 1000000)) // (long) Order total (in
                                                 // micros)
                        .setAffiliation("In-App Store") // (String) Affiliation
                        .setTotalTaxInMicros((long) (0.0 * 1000000)) // (long)
                                                                     // Total
                                                                     // tax (in
                                                                     // micros)
                        .setShippingCostInMicros(0) // (long) Total shipping
                                                    // cost (in micros)
                        .build();

                myTrans.addItem(new Item.Builder(
                        purchase.getSku(), // (String) Product SKU
                        "Monthly subscription to Pro", // (String) Product name
                        (long) (2.99 * 1000000), // (long) Product price (in
                                                 // micros)
                        (long) 1) // (long) Product quantity
                        .setProductCategory("Subscriptions") // (String) Product
                                                             // category
                        .build());
                TrackerUtils.sendTransaction(myTrans);
            }
        } catch (Exception ex)
        {
            GuiUtils.noAlertError(TAG, ex);
        }
    }

    /**
     * Listener for the query inventory result
     */
    static class GotInventoryListener implements IabHelper.QueryInventoryFinishedListener {
        LoadingControl loadingControl;

        GotInventoryListener(LoadingControl loadingControl)
        {
            this.loadingControl = loadingControl;
        }

        @Override
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            CommonUtils.debug(TAG, "Query inventory finished.");
            TrackerUtils.trackInAppBillingEvent("query_result", result.toString());
            if (result.isFailure()) {
                errorWithTracking("Failed to query inventory: " + result);
                return;
            }
            CommonUtils.debug(TAG, "Query inventory was successful.");

            /*
             * Check for items we own. Notice that for each purchase, we check
             * the developer payload to see if it's correct! See
             * verifyDeveloperPayload().
             */

            // Do we have the monthly subscription?
            final Purchase monthlySubscriptionPurchase = inventory
                    .getPurchase(SKU_MONTHLY_SUBSCRIPTION);
            boolean mSubscribedToMonthlyPremium = (
                    monthlySubscriptionPurchase != null &&
                            monthlySubscriptionPurchase.isInPurchasedState() &&
                    verifyDeveloperPayload(monthlySubscriptionPurchase));
            CommonUtils.debug(TAG, "User "
                    + (mSubscribedToMonthlyPremium ? "HAS" : "DOES NOT HAVE")
                    + " in-app monthly subscription.");
            if (monthlySubscriptionPurchase != null
                    && monthlySubscriptionPurchase.isInPurchasedState())
            {
                trackMonthlySubscriptionPurchaseIfNecessary(monthlySubscriptionPurchase);
                CommonUtils.debug(TAG, "Monthly subscription inventory found.");
                PaymentVerificationResponseUtil.verifyPurchaseAndRunAsync(new Runnable() {

                    @Override
                    public void run() {
                        if (!Preferences.isProUser())
                        {
                            AccountLimitUtils.updateLimitInformationCacheAsync(getLoadingControl());
                        }
                    }
                }, null, monthlySubscriptionPurchase, getLoadingControl());
            }
        }

        LoadingControl getLoadingControl()
        {
            return loadingControl;
        }
    }

    /**
     * Called when the purchase is finished
     */
    static class PurchaseFinishedListener implements IabHelper.OnIabPurchaseFinishedListener
    {

        ObjectAccessor<? extends LoadingControl> loadingControlAccessor;

        PurchaseFinishedListener(ObjectAccessor<? extends LoadingControl> loadingControlAccessor)
        {
            this.loadingControlAccessor = loadingControlAccessor;
        }

        @Override
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            useOncePurchaseListener = null;
            CommonUtils.debug(TAG, "Purchase finished: " + result + ", purchase: " + purchase);
            TrackerUtils.trackInAppBillingEvent("purchase_result", result.toString());
            if (result.isFailure()) {
                errorWithTracking("Error purchasing: " + result);
                GuiUtils.alert(R.string.errorPurchasing, result.getMessage());
                return;
            }
            if (!verifyDeveloperPayload(purchase)) {
                errorWithTracking("Error purchasing. Authenticity verification failed.");
                GuiUtils.alert(R.string.errorPurchasingAuthVerificationFailed);
                return;
            }
            CommonUtils.debug(TAG, "Purchase successful.");

            if (purchase.getSku().equals(SKU_MONTHLY_SUBSCRIPTION)) {
                // bought the monthly subscription
                CommonUtils.debug(TAG, "Monthly subscription purchased.");
                trackMonthlySubscriptionPurchaseIfNecessary(purchase);
                PaymentVerificationResponseUtil.verifyPurchaseAndRunAsync(new Runnable() {

                    @Override
                    public void run() {
                        TrackerUtils.trackInAppBillingEvent("purchase_request_monthly", "verified");
                        GuiUtils.alert(R.string.iabMonthlySubscriptionSuccess);
                        if (!Preferences.isProUser())
                        {
                            AccountLimitUtils.updateLimitInformationCacheAsync(getLoadingControl());
                        }
                        PurchaseControllerUtils.sendSubscriptionPurchasedBroadcast();
                    }
                }, new Runnable() {

                    @Override
                    public void run() {
                        TrackerUtils.trackInAppBillingEvent("purchase_request_monthly",
                                "verification failed");
                        GuiUtils.alert(R.string.errorCouldNotVerifyPayment);
                    }
                }, purchase, getLoadingControl());
            }
        }

        public LoadingControl getLoadingControl() {
            return loadingControlAccessor == null ? null : loadingControlAccessor.run();
        }
    }

    /**
     * The purchase handler interface.
     */
    public interface PurchaseHandler
    {
        /**
         * Call the purchase monthly subscription flow
         */
        void purchaseMonthlySubscription();
    }
}
