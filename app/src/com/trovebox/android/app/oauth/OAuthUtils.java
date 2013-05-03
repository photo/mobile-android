
package com.trovebox.android.app.oauth;

import java.net.URLEncoder;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.basic.DefaultOAuthConsumer;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.trovebox.android.app.Preferences;
import com.trovebox.android.app.R;
import com.trovebox.android.app.net.ITroveboxApi;
import com.trovebox.android.app.util.GuiUtils;
import com.trovebox.android.app.util.LoadingControl;
import com.trovebox.android.app.util.LoginUtils;
import com.trovebox.android.app.util.concurrent.AsyncTaskEx;

public class OAuthUtils
{
    static final String TAG = OAuthUtils.class.getSimpleName();

    static String getCallbackUrl(Context context)
    {
        return context.getString(R.string.trovebox_oauth_callback_url);
    }

    /**
     * Open the browser and asks the user to authorize the app. Afterwards, we
     * redirect the user back to activity!
     * 
     * @param activity
     */
    public static void askOAuth(Activity activity)
    {
        try
        {
            ITroveboxApi mTrovebox = Preferences.getApi(activity);
            String url = mTrovebox.getOAuthUrl(
                    URLEncoder.encode("Trovebox Android App", "UTF-8"),
                    URLEncoder.encode(getCallbackUrl(activity), "UTF-8"),
                    activity);
            activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri
                    .parse(url)));
        } catch (Exception ex)
        {
            GuiUtils.error(TAG, null, ex, activity);
        }
    }

    /**
     * Verify received response from the browser
     * 
     * @param activity
     * @param loadingControl
     * @param uri
     * @param runOnSuccess
     */
    public static void verifyOAuthResponse(Activity activity,
            LoadingControl loadingControl, Uri uri,
            Runnable runOnSuccess)
    {
        if (uri != null && uri.toString().startsWith(getCallbackUrl(activity)))
        {
            if (uri.getQueryParameter("oauth_token") != null)
            {
                new PostTask(uri,
                        loadingControl, activity,
                        runOnSuccess).execute();
            } else
            {
                String errorMsg = uri.getQueryParameter("error");
                if (errorMsg == null)
                {
                    errorMsg = "";
                }
                GuiUtils.alert(String.format(
                        activity.getString(R.string.errorSimple), errorMsg),
                        activity);
            }
        }
    }

    private static class PostTask extends AsyncTaskEx<Void, Void, Boolean>
    {
        private final Uri mUri;
        private OAuthConsumer mUsedConsumer;
        LoadingControl loadingControl;
        Activity activity;
        Runnable runOnSuccess;

        public PostTask(Uri uri, LoadingControl loadingControl,
                Activity activity,
                Runnable runOnSuccess)
        {
            mUri = uri;
            this.loadingControl = loadingControl;
            this.activity = activity;
            this.runOnSuccess = runOnSuccess;
        }

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            if (loadingControl != null)
            {
                loadingControl.startLoading();
            }
        }

        @Override
        protected Boolean doInBackground(Void... params)
        {
            try
            {
                String oAuthConsumerKey = mUri
                        .getQueryParameter("oauth_consumer_key");
                String oAuthConsumerSecret = mUri
                        .getQueryParameter("oauth_consumer_secret");
                String oAuthToken = mUri.getQueryParameter("oauth_token");
                String oAuthTokenSecret = mUri
                        .getQueryParameter("oauth_token_secret");
                String oAuthVerifier = mUri.getQueryParameter("oauth_verifier");

                mUsedConsumer = new DefaultOAuthConsumer(oAuthConsumerKey,
                        oAuthConsumerSecret);
                mUsedConsumer.setTokenWithSecret(oAuthToken, oAuthTokenSecret);

                OAuthProvider provider = Preferences
                        .getOAuthProvider(activity);
                provider.retrieveAccessToken(mUsedConsumer, oAuthVerifier);
                return true;
            } catch (Exception e)
            {
                GuiUtils.error(TAG,
                        R.string.errorWithAuthentication,
                        e, activity);
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result)
        {
            if (loadingControl != null)
            {
                loadingControl.stopLoading();
            }
            if (result.booleanValue())
            {
                Preferences.setLoginInformation(activity,
                        mUsedConsumer);
                LoginUtils.sendLoggedInBroadcast(activity);
                if (runOnSuccess != null)
                {
                    runOnSuccess.run();
                }
            }
        }
    }
}
