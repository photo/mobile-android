
package com.trovebox.android.app.util;


import java.util.ArrayList;
import java.util.Arrays;

import org.holoeverywhere.app.Activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.trovebox.android.app.MainActivity;
import com.trovebox.android.app.SelectAccountActivity;
import com.trovebox.android.app.net.account.AccountTroveboxResponse;
import com.trovebox.android.common.model.Credentials;
import com.trovebox.android.common.util.CommonUtils;
import com.trovebox.android.common.util.ObjectAccessor;
import com.trovebox.android.common.util.TrackerUtils;

public class LoginUtils
{
    public static final String TAG = LoginUtils.class.getSimpleName();

    public static String LOGIN_ACTION = "com.trovebox.ACTION_LOGIN";

    public static BroadcastReceiver getAndRegisterDestroyOnLoginActionBroadcastReceiver(
            final String TAG, final Activity activity)
    {
        BroadcastReceiver br = new BroadcastReceiver()
        {

            @Override
            public void onReceive(Context context, Intent intent)
            {
                CommonUtils.debug(TAG, "Received login broadcast message");
                activity.finish();
            }
        };
        activity.registerReceiver(br, new IntentFilter(LOGIN_ACTION));
        return br;
    }

    public static void sendLoggedInBroadcast(Activity activity)
    {
        Intent intent = new Intent(LOGIN_ACTION);
        activity.sendBroadcast(intent);
    }

    /**
     * Should be called by external activities/fragments after the logged in
     * action completed
     * 
     * @param activity
     * @param finishActivity whether to finish activity after the MainActivity
     *            started
     */
    public static void onLoggedIn(Activity activity, boolean finishActivity) {
        // start new activity
        activity.startActivity(new Intent(activity, MainActivity.class));
        LoginUtils.sendLoggedInBroadcast(activity);
        if (finishActivity) {
            activity.finish();
        }
    }

    /**
     * Common successful AccountTroveboxResult processor
     * 
     * @param result
     * @param fragmentAccessor
     * @param activity
     */
    public static void processSuccessfulLoginResult(AccountTroveboxResponse result,
            ObjectAccessor<? extends LoginActionHandler> fragmentAccessor, Activity activity) {
        Credentials[] credentials = result.getCredentials();
        if (credentials.length == 1) {
            CommonUtils.debug(TAG, "processSuccessfulLoginResult: found one login credentials");
            performLogin(fragmentAccessor, credentials[0]);
        } else {
            CommonUtils
                    .debug(TAG, "processSuccessfulLoginResult: found multiple login credentials");
            Intent intent = new Intent(activity, SelectAccountActivity.class);
            intent.putParcelableArrayListExtra(SelectAccountActivity.CREDENTIALS,
                    new ArrayList<Credentials>(Arrays.asList(credentials)));
            activity.startActivity(intent);
        }
    }

    private static void performLogin(
            ObjectAccessor<? extends LoginActionHandler> loginActionHandlerAccessor,
            Credentials credentials) {
        LoginActionHandler handler = loginActionHandlerAccessor.run();
        if (handler != null) {
            handler.processLoginCredentials(credentials);
        } else {
            String error = "Current instance accessor returned null";
            CommonUtils.error(TAG, error);
            TrackerUtils.trackException(error);
        }
    }

    /**
     * The interface the login/signup fragments should implement to use
     * processSuccessfulLoginResult method
     */
    public static interface LoginActionHandler
    {
        void processLoginCredentials(Credentials credentials);
    }
}
