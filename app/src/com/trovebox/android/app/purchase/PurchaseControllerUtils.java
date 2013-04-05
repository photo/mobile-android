
package com.trovebox.android.app.purchase;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.trovebox.android.app.TroveboxApplication;
import com.trovebox.android.app.util.CommonUtils;
import com.trovebox.android.app.util.GuiUtils;

/**
 * Utils for purchases
 * 
 * @author Eugene Popovich
 */
public class PurchaseControllerUtils {
    public static String SUBSCRIPTION_PURCHASED_ACTION = "com.trovebox.SUBSCRIPTION_PURCHASED";

    /**
     * Register subscription purchased handler to receive broadcast events
     * 
     * @param TAG
     * @param handler
     * @param activity
     * @return
     */
    public static BroadcastReceiver getAndRegisterOnSubscriptionPurchasedActionBroadcastReceiver(
            final String TAG,
            final SubscriptionPurchasedHandler handler,
            final Activity activity)
    {
        BroadcastReceiver br = new BroadcastReceiver()
        {

            @Override
            public void onReceive(Context context, Intent intent)
            {
                try
                {
                    CommonUtils.debug(TAG,
                            "Received subscription purchased broadcast message");
                    handler.subscriptionPurchased();
                } catch (Exception ex)
                {
                    GuiUtils.error(TAG, ex);
                }
            }
        };
        activity.registerReceiver(br, new IntentFilter(SUBSCRIPTION_PURCHASED_ACTION));
        return br;
    }

    /**
     * Send the broadcast message about subscription purchased
     */
    public static void sendSubscriptionPurchasedBroadcast()
    {
        Intent intent = new Intent(SUBSCRIPTION_PURCHASED_ACTION);
        TroveboxApplication.getContext().sendBroadcast(intent);
    }

    /**
     * Subscription purchased handler for broadcast event
     */
    public static interface SubscriptionPurchasedHandler
    {
        void subscriptionPurchased();
    }

}
