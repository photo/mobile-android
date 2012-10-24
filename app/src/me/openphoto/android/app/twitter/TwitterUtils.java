
package me.openphoto.android.app.twitter;

import me.openphoto.android.app.Preferences;
import me.openphoto.android.app.R;
import me.openphoto.android.app.util.GuiUtils;
import me.openphoto.android.app.util.LoadingControl;
import oauth.signpost.OAuthProvider;
import oauth.signpost.basic.DefaultOAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;

/**
 * @author Eugene Popovich
 */
public class TwitterUtils
{
    static final String TAG = TwitterUtils.class.getSimpleName();
    // SharedPreference user logon ID
    static final String PREFS_NAME = "TwitterLogin";
    static final String ACCESS_TOKEN_TOKEN = "accessTokenToken";
    static final String ACCESS_TOKEN_SECRET = "accessTokenSecret";

	static String getCallbackUrl(Context context)
    {
    	return context.getString(R.string.twitter_callback_url);
    }

	static String getConsumerKey(Context context)
	{
		return context.getString(R.string.twitter_api_consumer_key);
	}

	static String getConsumerSecret(Context context)
	{
		return context.getString(R.string.twitter_api_consumer_secret);
	}
    /**
     * This method checks the shared prefs to see if we have persisted a user
     * token/secret if it has then it logs on using them, otherwise return null
     * 
     * @param context
     * @return AccessToken from persisted prefs
     */
    static AccessToken getAccessToken(Context context)
    {
		SharedPreferences settings = Preferences
				.getSharedPreferences(PREFS_NAME);
        String token = settings.getString(ACCESS_TOKEN_TOKEN, "");
        String tokenSecret = settings.getString(ACCESS_TOKEN_SECRET, "");
        if (token != null && tokenSecret != null && !"".equals(tokenSecret)
                && !"".equals(token))
        {
            return new AccessToken(token, tokenSecret);
        }
        return null;
    }

    /**
     * This method persists the Access Token information so that a user is not
     * required to re-login every time the app is used
     * 
     * @param context
     * @param a - the access token
     */
    static void storeAccessToken(Context context, AccessToken a)
    {
		SharedPreferences settings = Preferences
				.getSharedPreferences(PREFS_NAME);
        SharedPreferences.Editor editor = settings.edit();
        if (a == null)
        {
            editor.remove(ACCESS_TOKEN_TOKEN);
            editor.remove(ACCESS_TOKEN_SECRET);
        } else
        {
            editor.putString(ACCESS_TOKEN_TOKEN, a.getToken());
            editor.putString(ACCESS_TOKEN_SECRET, a.getTokenSecret());
        }
        editor.commit();
    }

    /**
     * Open the browser and asks the user to authorize the app. Afterwards, we
     * redirect the user back to activity!
     * 
     * @param activity
     */
    public static void askOAuth(Activity activity)
    {
        new AccessRequestTask((LoadingControl) activity, activity).execute();
    }

    /**
     * Verify received response from the browser
     * 
     * @param activity
     * @param uri
     */
	public static void verifyOAuthResponse(Activity activity, Uri uri,
			Runnable runOnSuccess)
    {
		if (uri != null
				&& uri.toString().startsWith(
						getCallbackUrl(activity)))
        {
            String verifier = uri
                    .getQueryParameter(oauth.signpost.OAuth.OAUTH_VERIFIER);
            new VerifyResponseTask(verifier, (LoadingControl) activity,
					runOnSuccess, activity).execute();
        }
    }

    /**
     * Instantiate the twitter api instance
     * 
     * @param context
     * @return
     */
    static Twitter instantiateTwitter(Context context)
    {
        Twitter twitter = null;
        // Get Access Token and persist it
        AccessToken a = TwitterUtils.getAccessToken(context);
        if (a != null)
        {
            // initialize Twitter4J
            twitter = new TwitterFactory().getInstance();
			twitter.setOAuthConsumer(getConsumerKey(context),
					getConsumerSecret(context));
            twitter.setOAuthAccessToken(a);
        }
        return twitter;
    }

    /**
     * Forget twitter login credentials
     * 
     * @param context
     */
    public static void logout(Context context)
    {
        TwitterProvider provider = TwitterProvider.getInstance();
        if (provider.getTwitter() != null)
        {
            provider.getTwitter().shutdown();
        }
        provider.setTwitter(null);
        provider.setConsumer(null);
        provider.setProvider(null);
        storeAccessToken(context, null);
    }

    private static class VerifyResponseTask
            extends AsyncTask<Void, Void, Boolean>
    {
        private LoadingControl loadingControl;
        private Activity activity;
        String verifier;
        TwitterProvider twitterProvider;
        private CommonsHttpOAuthConsumer consumer;
        private OAuthProvider provider;
		Runnable runOnSuccess;

        public VerifyResponseTask(String verifier,
                LoadingControl loadingControl,
				Runnable runOnSuccess,
                Activity activity)
        {
            this.loadingControl = loadingControl;
            this.activity = activity;
            this.verifier = verifier;
			this.runOnSuccess = runOnSuccess;
            twitterProvider = TwitterProvider.getInstance();
        }

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            loadingControl.startLoading();
        }

        @Override
        protected Boolean doInBackground(Void... params)
        {
            try
            {
                consumer = twitterProvider
                        .getConsumer();
                provider = twitterProvider.getProvider();
                if (consumer == null || provider == null)
                {
                    return false;
                }
                // this will populate token and token_secret in consumer
                provider.retrieveAccessToken(consumer, verifier);
                return true;
            } catch (Exception e)
            {
				GuiUtils.error(TAG,
						R.string.errorVerifyingTheResponse,
						e,
                        activity);
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result)
        {
            loadingControl.stopLoading();
            if (result.booleanValue())
            {
                try
                {
                    // Get Access Token and persist it
                    AccessToken a = new AccessToken(consumer.getToken(),
                            consumer.getTokenSecret());
                    storeAccessToken(activity, a);

                    // initialize Twitter4J
                    Twitter twitter = new TwitterFactory().getInstance();
					twitter.setOAuthConsumer(getConsumerKey(activity),
							getConsumerSecret(activity));
                    twitter.setOAuthAccessToken(a);
                    twitterProvider.setTwitter(twitter);
					GuiUtils.info(
							R.string.share_twitter_success_setup_message);
					if (runOnSuccess != null)
					{
						runOnSuccess.run();
					}
                } catch (Exception ex)
                {
                    GuiUtils.error(TAG, null, ex,
                            activity);
                }
            }
        }
    }

    private static class AccessRequestTask extends
            AsyncTask<Void, Void, Boolean>
    {
        CommonsHttpOAuthConsumer consumer;
        OAuthProvider provider;
        String authUrl;
        private LoadingControl loadingControl;
        private Activity activity;

        public AccessRequestTask(LoadingControl loadingControl,
                Activity activity)
        {
            this.loadingControl = loadingControl;
            this.activity = activity;
        }

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            loadingControl.startLoading();
        }

        @Override
        protected Boolean doInBackground(Void... params)
        {
            try
            {
                consumer = new CommonsHttpOAuthConsumer(
						getConsumerKey(activity),
						getConsumerSecret(activity));
                provider = new DefaultOAuthProvider(
                        "http://twitter.com/oauth/request_token",
                        "http://twitter.com/oauth/access_token",
                        "http://twitter.com/oauth/authorize");
                authUrl = provider.retrieveRequestToken(consumer,
                		getCallbackUrl(activity));
                return true;
            } catch (Exception e)
            {
				GuiUtils.error(TAG, R.string.errorWithTwitterAuthentication, e,
                        activity);
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result)
        {
            loadingControl.stopLoading();
            if (result.booleanValue())
            {
                try
                {
                    GuiUtils.info(
							R.string.share_twitter_authorise_ask);
                    TwitterProvider twitterProvider = TwitterProvider.getInstance();
                    twitterProvider.setConsumer(consumer);
                    twitterProvider.setProvider(provider);
                    activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri
                            .parse(authUrl)));
                } catch (Exception ex)
                {
                    GuiUtils.error(TAG, null, ex,
                            activity);
                }
            }
        }
    }
}
