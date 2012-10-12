
package me.openphoto.android.app.oauth;

import me.openphoto.android.app.Preferences;
import me.openphoto.android.app.net.IOpenPhotoApi;
import me.openphoto.android.app.util.GuiUtils;
import me.openphoto.android.app.util.LoadingControl;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.basic.DefaultOAuthConsumer;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;

public class OAuthUtils
{
    static final String TAG = OAuthUtils.class.getSimpleName();
    private static final String CALLBACK_URL = "openphoto://callback";

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
            IOpenPhotoApi mOpenPhoto = Preferences.getApi(activity);
            String url = mOpenPhoto.getOAuthUrl("OpenPhoto Android App",
                    CALLBACK_URL,
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
        if (uri != null && uri.toString().startsWith(CALLBACK_URL))
        {
            if (uri.getQueryParameter("oauth_token") != null)
            {
                new PostTask(uri,
                        loadingControl, activity,
                        runOnSuccess).execute();
            } else
            {
                GuiUtils.alert("Error: " + uri.getQueryParameter("error"),
                        activity);
            }
        }
    }

    private static class PostTask extends AsyncTask<Void, Void, Boolean>
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
                GuiUtils.error(TAG, "Error with authentication", e, activity);
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
                if (runOnSuccess != null)
                {
                    runOnSuccess.run();
                }
            }
        }
    }
}
