
package com.trovebox.android.app.facebook;


import org.holoeverywhere.app.Activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;
import com.trovebox.android.app.Preferences;
import com.trovebox.android.app.R;
import com.trovebox.android.app.facebook.FacebookSessionEvents.AuthListener;
import com.trovebox.android.app.facebook.FacebookSessionEvents.LogoutListener;
import com.trovebox.android.common.ui.widget.YesNoDialogFragment;
import com.trovebox.android.common.ui.widget.YesNoDialogFragment.YesNoButtonPressedHandler;
import com.trovebox.android.common.util.GuiUtils;

/**
 * @author Eugene Popovich
 */
public class FacebookUtils
{
    static final String TAG = FacebookUtils.class.getSimpleName();

    private static final String TOKEN = "access_token";
    private static final String EXPIRES = "expires_in";
    private static final String LAST_UPDATE = "last_update";
    private static final String KEY = "facebook-session";

    /*
     * Restore the access token and the expiry date from the shared preferences.
     */
    public static boolean restore(Facebook session, Context context)
    {
        SharedPreferences savedSession = Preferences.getSharedPreferences(KEY);
        session.setTokenFromCache(
                savedSession.getString(TOKEN, null),
                savedSession.getLong(EXPIRES, 0),
                savedSession.getLong(LAST_UPDATE, 0));
        return session.isSessionValid();
    }

    /*
     * Save the access token and expiry date so you don't have to fetch it each
     * time
     */
    public static boolean save(Facebook session, Context context)
    {
        Editor editor = Preferences.getSharedPreferences(KEY)
                .edit();
        editor.putString(TOKEN, session.getAccessToken());
        editor.putLong(EXPIRES, session.getAccessExpires());
        editor.putLong(LAST_UPDATE, session.getLastAccessUpdate());
        return editor.commit();
    }

    /**
     * clears facebook login credentials from the preferences
     * 
     * @param context
     */
    public static void clear(Context context)
    {
        Editor editor = Preferences.getSharedPreferences(KEY)
                .edit();
        editor.clear();
        editor.commit();
    }

    /**
     * Instantiate the facebook session
     * 
     * @param APP_ID
     * @param context
     * @return
     */
    public static Facebook instantiateSession(String APP_ID, Context context)
    {
        Facebook facebook = new Facebook(APP_ID);
        restore(facebook, context);
        SessionListener listener = new SessionListener(context);
        FacebookSessionEvents.addAuthListener(listener);
        FacebookSessionEvents.addLogoutListener(listener);
        return facebook;
    }

    /**
     * Extend the facebook access token if needed
     * 
     * @param context
     */
    public static void extendAceessTokenIfNeeded(Context context)
    {
        Facebook facebook = FacebookProvider.getFacebook();
        if (facebook != null)
        {
            if (facebook.isSessionValid())
            {
                facebook.extendAccessTokenIfNeeded(context, null);
            }
        }
    }

    /**
     * Request the facebook authentication
     * 
     * @param activity
     * @param activityCode the result code which will be handled on the
     *            onActivityResult method
     */
    public static void loginRequest(Activity activity,
            int activityCode
            )
    {
        Facebook mFb = FacebookProvider.getFacebook();
        if (!mFb.isSessionValid())
        {
            mFb.authorize(
                    activity,
                    activity.getResources().getStringArray(
                            R.array.share_facebook_permissions),
                    activityCode,
                    new LoginDialogListener(activity.getApplicationContext()));
        }
    }

    /**
     * Request the logout for the current facebook session
     * 
     * @param context
     */
    public static void logoutRequest(Context context)
    {
        Facebook mFb = FacebookProvider.getFacebook();
        if (mFb.isSessionValid())
        {
            FacebookSessionEvents.onLogoutBegin();
            AsyncFacebookRunner asyncRunner = new AsyncFacebookRunner(mFb);
            asyncRunner.logout(context, new LogoutRequestListener(context));
        }
    }

    public static void runAfterFacebookAuthentication(
            final Activity activity,
            final int activityCode,
            final Runnable runOnSuccessAuthentication)
    {
        runAfterFacebookAuthentication(activity, activityCode, runOnSuccessAuthentication,
                null);
    }

    public static void runAfterFacebookAuthentication(
            final Activity activity,
            final int activityCode,
            final Runnable runOnSuccessAuthentication,
            final Runnable runOnCancelAuthentication)
    {
        Facebook facebook = FacebookProvider.getFacebook();
        if (facebook.isSessionValid())
        {
            runOnSuccessAuthentication.run();
        } else
        {
            YesNoDialogFragment dialogFragment = YesNoDialogFragment
                    .newInstance(R.string.share_facbook_authorisation_question,
                            new YesNoButtonPressedHandler()
                            {
                                @Override
                                public void yesButtonPressed(
                                        DialogInterface dialog)
                                {
                                    AuthListener listener = new SelfRemovingWithDelayAuthListener(
                                            runOnSuccessAuthentication);
                                    FacebookSessionEvents
                                            .addAuthListener(listener);
                                    FacebookUtils
                                            .loginRequest(
                                                    activity,
                                                    activityCode);
                                }

                                @Override
                                public void noButtonPressed(
                                        DialogInterface dialog)
                                {
                                    if (runOnCancelAuthentication != null)
                                    {
                                        runOnCancelAuthentication.run();
                                    }
                                }
                            });
            dialogFragment.show(activity);
        }
    }

    private static final class LoginDialogListener implements DialogListener
    {
        Context context;

        public LoginDialogListener(Context context)
        {
            this.context = context;
        }

        @Override
        public void onComplete(Bundle values)
        {
            FacebookSessionEvents.onLoginSuccess();
            GuiUtils.info(R.string.share_facebook_success_setup_message);
        }

        @Override
        public void onFacebookError(FacebookError error)
        {
            GuiUtils.error(TAG, null, error, context);
            FacebookSessionEvents.onLoginError(error.getMessage());
        }

        @Override
        public void onError(DialogError error)
        {
            GuiUtils.error(TAG, null, new RuntimeException(error), context);
            FacebookSessionEvents.onLoginError(error.getMessage());
        }

        @Override
        public void onCancel()
        {
            FacebookSessionEvents.onLoginError(context
                    .getString(R.string.share_facbook_action_canceled));
        }

    }

    private static class LogoutRequestListener extends FacebookBaseRequestListener
    {
        public LogoutRequestListener(Context context)
        {
            super(context);
        }

        @Override
        public void onComplete(String response, final Object state)
        {
            /*
             * callback should be run in the original thread, not the background
             * thread
             */
            GuiUtils.runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    FacebookSessionEvents.onLogoutFinish();
                }
            });
        }
    }

    private static class SelfRemovingWithDelayAuthListener implements AuthListener
    {
        Runnable runOnSuccessAuthentication;

        public SelfRemovingWithDelayAuthListener(Runnable runOnSuccessAuthentication)
        {
            this.runOnSuccessAuthentication = runOnSuccessAuthentication;
        }

        @Override
        public void onAuthSucceed()
        {
            Handler handler = new Handler();
            handler.postDelayed(
                    new Runnable() {

                        @Override
                        public void run() {
                            FacebookSessionEvents
                                    .removeAuthListener(SelfRemovingWithDelayAuthListener.this);
                            runOnSuccessAuthentication.run();
                        }
                    },
                    1000);
        }

        @Override
        public void onAuthFail(String error)
        {
            Handler handler = new Handler();
            handler.postDelayed(
                    new Runnable() {

                        @Override
                        public void run() {
                            FacebookSessionEvents
                                    .removeAuthListener(SelfRemovingWithDelayAuthListener.this);
                        }
                    },
                    1000);
        }
    }
    private static class SessionListener implements AuthListener,
            LogoutListener
    {
        Context context;

        public SessionListener(Context context)
        {
            this.context = context;
        }

        @Override
        public void onAuthSucceed()
        {
            save(FacebookProvider.getFacebook(), context);
        }

        @Override
        public void onAuthFail(String error)
        {
            Log.e(TAG, error);
        }

        @Override
        public void onLogoutBegin()
        {
        }

        @Override
        public void onLogoutFinish()
        {
            clear(context);
        }
    }
}
