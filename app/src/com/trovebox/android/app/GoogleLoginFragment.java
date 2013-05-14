
package com.trovebox.android.app;

import java.io.IOException;
import java.lang.ref.WeakReference;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.AlertDialog;
import org.holoeverywhere.app.Dialog;
import org.holoeverywhere.app.ProgressDialog;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.GooglePlayServicesAvailabilityException;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.trovebox.android.app.common.CommonDialogFragment;
import com.trovebox.android.app.common.CommonFragment;
import com.trovebox.android.app.net.TroveboxResponseUtils;
import com.trovebox.android.app.net.account.AccountTroveboxResponse;
import com.trovebox.android.app.net.account.IAccountTroveboxApiFactory;
import com.trovebox.android.app.util.CommonUtils;
import com.trovebox.android.app.util.GuiUtils;
import com.trovebox.android.app.util.LoginUtils;
import com.trovebox.android.app.util.SimpleAsyncTaskEx;
import com.trovebox.android.app.util.TrackerUtils;

/**
 * The fragment which handles login via google action
 * 
 * @author Eugene Popovich
 */
public class GoogleLoginFragment extends CommonFragment {

    private static final String TAG = GoogleLoginFragment.class.getSimpleName();
    public static final String SCOPE =
            "audience:server:client_id:"
                    + CommonUtils.getStringResource(R.string.google_auth_server_client_id);
    LogInUserTask logInTask;
    int requestCode;
    boolean delayedLoggedIn = false;

    static WeakReference<GoogleLoginFragment> currentInstance;

    /**
     * Empty constructor as per the Fragment documentation
     */
    public GoogleLoginFragment() {
    }

    /**
     * Locate an existing instance of this Fragment or if not found, create and
     * add it using FragmentManager.
     * 
     * @param fm The FragmentManager manager to use.
     * @return The existing instance of the Fragment or the new instance if just
     *         created.
     */
    public static GoogleLoginFragment findOrCreateFeatherFragment(
            FragmentManager fm) {
        // Check to see if we have retained the worker fragment.
        GoogleLoginFragment mRetainFragment = (GoogleLoginFragment) fm.findFragmentByTag(TAG);

        // If not retained (or first time running), we need to create and add
        // it.
        if (mRetainFragment == null) {
            mRetainFragment = new GoogleLoginFragment();
            fm.beginTransaction().add(mRetainFragment, TAG).commit();
        }
        return mRetainFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Make sure this Fragment is retained over a configuration change
        setRetainInstance(true);

        currentInstance = new WeakReference<GoogleLoginFragment>(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        try
        {
            // if view is destroyed we need to hide progress dialog
            if (logInTask != null)
            {
                logInTask.stopLoading();
            }
        } catch (Exception ex)
        {
            GuiUtils.noAlertError(TAG, ex);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        currentInstance = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View result = super.onCreateView(inflater, container, savedInstanceState);
        try
        {
            // if login task still working we need to show progress dialog
            if (logInTask != null && !logInTask.finished)
            {
                logInTask.startLoading();
            }
        } catch (Exception ex)
        {
            GuiUtils.noAlertError(TAG, ex);
        }
        return result;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (delayedLoggedIn)
        {
            delayedLoggedIn = false;
            onLoggedIn(getActivity());
        }
    }

    void onLoggedIn(Activity activity)
    {
        // start new activity
        startActivity(new Intent(activity,
                MainActivity.class));
        LoginUtils.sendLoggedInBroadcast(activity);
    }

    public void doLogin(int requestCode)
    {
        this.requestCode = requestCode;
        int availabilityResult = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(getActivity());
        if (availabilityResult == ConnectionResult.SUCCESS)
        {
            CommonUtils
                    .debug(TAG, "accountGoogleLoginButtonAction: Google play services available");
            TrackerUtils.trackBackgroundEvent("google_play_services_availability", "available");
            checkAccountNamesAndPerformLoginAction();

        } else if (availabilityResult == ConnectionResult.SERVICE_DISABLED
                || availabilityResult == ConnectionResult.SERVICE_MISSING ||
                availabilityResult == ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED)
        {
            CommonUtils
                    .debug(TAG,
                            "accountGoogleLoginButtonAction: Google play services not available but can be fixed");
            TrackerUtils.trackBackgroundEvent("google_play_services_availability",
                    "not available, can be fixed: " + availabilityResult);
            showGooglePlayErrorDialog(availabilityResult);

        } else
        {
            CommonUtils.debug(TAG,
                    "accountGoogleLoginButtonAction: Google play services not available");
            TrackerUtils.trackBackgroundEvent("google_play_services_availability",
                    "not available: " + availabilityResult);
            GuiUtils.alert(R.string.errorGooglePlayServicesNotAvailable);

        }
    }

    public void showGooglePlayErrorDialog(int availabilityResult) {
        try
        {
            android.app.Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
                    availabilityResult,
                    getActivity(),
                    requestCode);
            errorDialog.show();
        } catch (Exception ex)
        {
            GuiUtils.error(TAG, R.string.errorGooglePlayServicesNotAvailable, ex);
        }
    }

    void checkAccountNamesAndPerformLoginAction()
    {
        String[] accountNames = getAccountNames();
        if (accountNames == null || accountNames.length == 0)
        {
            CommonUtils.debug(TAG,
                    "checkAccountNamesAndPerformLoginAction: didn't find any google accounts");
            GuiUtils.alert(R.string.errorGoogleAccountsNotFound);
        } else if (accountNames.length == 1)
        {
            CommonUtils.debug(TAG,
                    "checkAccountNamesAndPerformLoginAction: found one google account");
            performLoginAction(accountNames[0]);
        } else
        {
            CommonUtils.debug(TAG,
                    "checkAccountNamesAndPerformLoginAction: found multiple google accounts");
            showAccountSelectionDialog(accountNames);
        }
    }

    void showAccountSelectionDialog(final String[] accountNames)
    {
        SelectAccountDialogFragment fragment = SelectAccountDialogFragment
                .newInstance(new SelectAccountSelectedActionHandler(accountNames));
        fragment.show(getSupportActivity());
    }

    void performLoginAction(String accountName)
    {
        logInTask = new LogInUserTask(accountName);
        logInTask.execute();
    }

    private String[] getAccountNames() {
        AccountManager mAccountManager = AccountManager.get(getActivity());
        Account[] accounts = mAccountManager
                .getAccountsByType(GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
        String[] names = new String[accounts.length];
        for (int i = 0; i < names.length; i++) {
            names[i] = accounts[i].name;
        }
        return names;
    }

    private class LogInUserTask extends
            SimpleAsyncTaskEx
    {
        AccountTroveboxResponse result;
        ProgressDialog progress;
        boolean finished = false;
        String accountName;

        public LogInUserTask(String accountName)
        {
            super(null);
            CommonUtils.debug(TAG, "accountName: " + accountName);
            this.accountName = accountName;
        }

        @Override
        public void startLoading() {
            super.startLoading();
            progress = new ProgressDialog(getActivity());
            progress.setIndeterminate(true);
            progress.setMessage(CommonUtils.getStringResource(R.string.logging_in_message));
            progress.setCancelable(false);
            progress.show();
        }

        @Override
        public void stopLoading() {
            super.stopLoading();
            try
            {
                if (progress != null && progress.getWindow() != null) {
                    progress.dismiss();
                }
            } catch (Exception ex)
            {
                GuiUtils.noAlertError(TAG, ex);
            }
            progress = null;
        }

        @Override
        protected void onSuccessPostExecute() {
            finished = true;
            try
            {
                // save credentials.
                result.saveCredentials(TroveboxApplication.getContext());

                Activity activity = getActivity();
                if (activity != null)
                {
                    onLoggedIn(activity);
                } else
                {
                    delayedLoggedIn = true;
                }
            } finally
            {
                logInTask = null;
            }
        }

        @Override
        protected void onFailedPostExecute() {
            super.onFailedPostExecute();
            finished = true;
            logInTask = null;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            finished = true;
            logInTask = null;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                if (CommonUtils.checkOnline())
                {
                    String token = fetchToken();
                    if (token != null)
                    {
                        CommonUtils.debug(TAG, "Token: " + token);

                        result = IAccountTroveboxApiFactory.getApi().signInViaGoogle(token);
                        return TroveboxResponseUtils.checkResponseValid(result);
                    }
                }
            } catch (Exception e) {
                GuiUtils.error(TAG, R.string.errorCouldNotLogin, e);
            }
            return false;
        }

        /**
         * Get a authentication token if one is not available. If the error is
         * not recoverable then it displays the error message on parent activity
         * right away.
         */
        protected String fetchToken() throws IOException {
            try {
                Activity activity = getActivity();
                if (activity == null || activity.isFinishing())
                {
                    return null;
                }
                return GoogleAuthUtil.getToken(activity, accountName, SCOPE);
            } catch (final GooglePlayServicesAvailabilityException playEx) {
                GuiUtils.noAlertError(TAG, playEx);
                // GooglePlayServices.apk is either old, disabled, or not
                // present.
                GuiUtils.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        showGooglePlayErrorDialog(playEx.getConnectionStatusCode());
                    }
                });
            } catch (UserRecoverableAuthException userRecoverableException) {
                // Unable to authenticate, but the user can fix this.
                // Forward the user to the appropriate activity.
                startActivityForResult(userRecoverableException.getIntent(), requestCode);
            } catch (GoogleAuthException fatalException) {
                GuiUtils.error(TAG, CommonUtils.getStringResource(
                        R.string.errorCouldNotFetchGoogleAccountToken,
                        fatalException.getLocalizedMessage()), fatalException);
            }
            return null;
        }
    }

    public static class SelectAccountSelectedActionHandler implements
            SelectAccountDialogFragment.SelectedActionHandler {

        String[] accountNames;
        public SelectAccountSelectedActionHandler(String[] accountNames)
        {
            this.accountNames = accountNames;
        }
        @Override
        public void itemSelected(int i) {
            currentInstance.get().performLoginAction(getItems()[i]);
        }

        @Override
        public String[] getItems() {
            return accountNames;
        }
    }
    public static class SelectAccountDialogFragment extends CommonDialogFragment
    {
        public static final String HANDLER_ITEMS = "SelectAccountDialogFragment.handlerItems";

        public static interface SelectedActionHandler
        {
            void itemSelected(int i);

            String[] getItems();
        }

        private SelectedActionHandler handler;

        public static SelectAccountDialogFragment newInstance(
                SelectedActionHandler handler)
        {
            SelectAccountDialogFragment frag = new SelectAccountDialogFragment();
            frag.handler = handler;
            return frag;
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
            outState.putStringArray(HANDLER_ITEMS, handler.getItems());
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            if(savedInstanceState != null)
            {
                handler = new SelectAccountSelectedActionHandler(savedInstanceState.getStringArray(HANDLER_ITEMS));
            }

            final CharSequence[] items = handler.getItems();

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.select_google_account);
            builder.setItems(items, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int item) {
                    if (handler == null)
                    {
                        return;
                    }
                    handler.itemSelected(item);
                }
            });
            return builder.create();
        }
    }
}
