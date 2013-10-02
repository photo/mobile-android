
package com.trovebox.android.app.util;


import org.holoeverywhere.app.Activity;
import org.holoeverywhere.app.AlertDialog;
import org.holoeverywhere.app.Dialog;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import com.trovebox.android.app.MainActivity;
import com.trovebox.android.app.R;
import com.trovebox.android.app.common.CommonDialogFragment;
import com.trovebox.android.app.model.Credentials;
import com.trovebox.android.app.net.account.AccountTroveboxResponse;

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
            SelectAccountDialogFragment selectionFragment = SelectAccountDialogFragment
                    .newInstance(new SelectAccountSelectedActionHandler(credentials,
                            fragmentAccessor));
            selectionFragment.show(activity);
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

    public static class SelectAccountSelectedActionHandler implements
            SelectAccountDialogFragment.SelectedActionHandler {

        ObjectAccessor<? extends LoginActionHandler> mLoginActionHandlerAccessor;
        Credentials[] mCredentials;

        public SelectAccountSelectedActionHandler(Credentials[] credentials,
                ObjectAccessor<? extends LoginActionHandler> loginActoinHandlerAccessor) {
            this.mCredentials = credentials;
            this.mLoginActionHandlerAccessor = loginActoinHandlerAccessor;
        }

        @Override
        public void itemSelected(int i) {
            performLogin(mLoginActionHandlerAccessor, getItems()[i]);
        }

        @Override
        public Credentials[] getItems() {
            return mCredentials;
        }

        @Override
        public ObjectAccessor<? extends LoginActionHandler> getLoginActionHandlerAccessor() {
            return mLoginActionHandlerAccessor;
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

    /**
     * The dialog fragment which shows list of retrieved accounts where user can
     * select one to login
     */
    public static class SelectAccountDialogFragment extends CommonDialogFragment {
        public static final String HANDLER_ITEMS = "SelectAccountDialogFragment.handlerItems";
        public static final String LOGIN_HANDLER_ACCESSOR = "SelectAccountDialogFragment.loginHandlerAccessor";

        public static interface SelectedActionHandler {
            void itemSelected(int i);

            Credentials[] getItems();

            ObjectAccessor<? extends LoginActionHandler> getLoginActionHandlerAccessor();
        }

        private SelectedActionHandler handler;

        public static SelectAccountDialogFragment newInstance(SelectedActionHandler handler) {
            SelectAccountDialogFragment frag = new SelectAccountDialogFragment();
            frag.handler = handler;
            return frag;
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
            outState.putParcelableArray(HANDLER_ITEMS, handler.getItems());
            outState.putSerializable(LOGIN_HANDLER_ACCESSOR, handler.getLoginActionHandlerAccessor());
        }

        @SuppressWarnings("unchecked")
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            if (savedInstanceState != null) {
                handler = new SelectAccountSelectedActionHandler(
                        (Credentials[]) savedInstanceState.getParcelableArray(HANDLER_ITEMS),
                        (ObjectAccessor<LoginActionHandler>) savedInstanceState
                                .getSerializable(LOGIN_HANDLER_ACCESSOR));
            }

            final String[] items = new String[handler.getItems().length];
            for (int i = 0; i < items.length; i++) {
                items[i] = handler.getItems()[i].getEmail();
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.select_trovebox_account);
            builder.setItems(items, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int item) {
                    if (handler == null) {
                        return;
                    }
                    handler.itemSelected(item);
                }
            });
            return builder.create();
        }
    }

}
