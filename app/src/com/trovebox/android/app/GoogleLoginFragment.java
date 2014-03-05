
package com.trovebox.android.app;

import java.io.IOException;
import java.lang.ref.WeakReference;

import org.holoeverywhere.app.Activity;
import org.holoeverywhere.app.AlertDialog;
import org.holoeverywhere.app.Dialog;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.GooglePlayServicesAvailabilityException;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.trovebox.android.app.model.utils.CredentialsUtils;
import com.trovebox.android.app.net.account.AccountTroveboxResponse;
import com.trovebox.android.app.net.account.IAccountTroveboxApiFactory;
import com.trovebox.android.app.util.LoginUtils;
import com.trovebox.android.app.util.LoginUtils.LoginActionHandler;
import com.trovebox.android.common.fragment.common.CommonDialogFragment;
import com.trovebox.android.common.fragment.common.CommonRetainedFragmentWithTaskAndProgress;
import com.trovebox.android.common.net.TroveboxResponseUtils;
import com.trovebox.android.common.util.CommonUtils;
import com.trovebox.android.common.util.GuiUtils;
import com.trovebox.android.common.util.ObjectAccessor;
import com.trovebox.android.common.util.TrackerUtils;

/**
 * The fragment which handles login via google action
 * 
 * @author Eugene Popovich
 */
public class GoogleLoginFragment extends CommonRetainedFragmentWithTaskAndProgress implements
        LoginActionHandler {

    private static final String TAG = GoogleLoginFragment.class.getSimpleName();
    public static final String SCOPE =
            "audience:server:client_id:"
                    + CommonUtils.getStringResource(R.string.google_auth_server_client_id);
    static WeakReference<GoogleLoginFragment> sCurrentInstance;
    static ObjectAccessor<GoogleLoginFragment> sCurrentInstanceAccessor = new ObjectAccessor<GoogleLoginFragment>() {
        private static final long serialVersionUID = 1L;

        @Override
        public GoogleLoginFragment run() {
            return sCurrentInstance == null ? null : sCurrentInstance.get();
        }
    };

    int mRequestCode;
    boolean mDelayedLoginProcessing = false;
    AccountTroveboxResponse mLastResponse;

    /**
     * Empty constructor as per the Fragment documentation
     */
    public GoogleLoginFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sCurrentInstance = new WeakReference<GoogleLoginFragment>(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (sCurrentInstance != null)
        {
            if (sCurrentInstance.get() == GoogleLoginFragment.this
                    || sCurrentInstance.get() == null)
            {
                CommonUtils.debug(TAG, "Nullify current instance");
                sCurrentInstance = null;
            } else
            {
                CommonUtils.debug(TAG,
                        "Skipped nullify of current instance, such as it is not the same");
            }
        }
    }

    public void doLogin(int requestCode) {
        this.mRequestCode = requestCode;
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
                    mRequestCode);
            errorDialog.show();
        } catch (Exception ex)
        {
            GuiUtils.error(TAG, R.string.errorGooglePlayServicesNotAvailable, ex);
        }
    }

    void checkAccountNamesAndPerformLoginAction() {
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

    void showAccountSelectionDialog(final String[] accountNames) {
        SelectAccountDialogFragment fragment = SelectAccountDialogFragment
                .newInstance(new SelectAccountSelectedActionHandler(accountNames));
        fragment.show(getSupportActivity());
    }

    void performLoginAction(String accountName) {
        startRetainedTask(new LogInUserTask(accountName));
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

    @Override
    public String getLoadingMessage() {
        return CommonUtils.getStringResource(R.string.logging_in_message);
    }

    @Override
    public void processLoginCredentials(com.trovebox.android.common.model.Credentials credentials) {
        Activity activity = getSupportActivity();
        CredentialsUtils.saveCredentials(activity, credentials);
        LoginUtils.onLoggedIn(activity, true);
    }

    void processLoginResonse(Activity activity) {
        LoginUtils.processSuccessfulLoginResult(mLastResponse, sCurrentInstanceAccessor, activity);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (mDelayedLoginProcessing) {
            mDelayedLoginProcessing = false;
            processLoginResonse(getSupportActivity());
        }
    }
    private class LogInUserTask extends
            RetainedTask {
        AccountTroveboxResponse result;
        String accountName;

        public LogInUserTask(String accountName) {
            this.accountName = accountName;
        }

        @Override
        protected void onSuccessPostExecuteAdditional() {
            try {
                mLastResponse = result;
                Activity activity = getSupportActivity();
                if (activity != null) {
                    processLoginResonse(activity);
                } else {
                    TrackerUtils.trackErrorEvent("activity_null", TAG);
                    mDelayedLoginProcessing = true;
                }
            } catch (Exception e) {
                GuiUtils.error(TAG, e);
            }
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                if (GuiUtils.checkOnline())
                {
                    String token = fetchToken();
                    if (token != null)
                    {
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
                Activity activity = getSupportActivity();
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
                startActivityForResult(userRecoverableException.getIntent(), mRequestCode);
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

        public SelectAccountSelectedActionHandler(String[] accountNames) {
            this.accountNames = accountNames;
        }

        @Override
        public void itemSelected(int i) {
            sCurrentInstance.get().performLoginAction(getItems()[i]);
        }

        @Override
        public String[] getItems() {
            return accountNames;
        }
    }

    public static class SelectAccountDialogFragment extends CommonDialogFragment {
        public static final String HANDLER_ITEMS = "SelectAccountDialogFragment.handlerItems";

        public static interface SelectedActionHandler {
            void itemSelected(int i);

            String[] getItems();
        }

        private SelectedActionHandler handler;

        public static SelectAccountDialogFragment newInstance(
                SelectedActionHandler handler) {
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
            if (savedInstanceState != null)
            {
                handler = new SelectAccountSelectedActionHandler(
                        savedInstanceState.getStringArray(HANDLER_ITEMS));
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
