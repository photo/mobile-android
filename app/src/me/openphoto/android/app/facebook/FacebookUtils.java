package me.openphoto.android.app.facebook;

import me.openphoto.android.app.R;
import me.openphoto.android.app.facebook.FacebookSessionEvents.AuthListener;
import me.openphoto.android.app.facebook.FacebookSessionEvents.LogoutListener;
import me.openphoto.android.app.util.GuiUtils;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;

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
		SharedPreferences savedSession = context.getSharedPreferences(KEY,
				Context.MODE_PRIVATE);
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
		Editor editor = context.getSharedPreferences(KEY, Context.MODE_PRIVATE)
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
		Editor editor = context.getSharedPreferences(KEY, Context.MODE_PRIVATE)
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
	 * @param activityCode
	 *            the result code which will be handled on the onActivityResult
	 *            method
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
					new LoginDialogListener(activity));
		}
	}

	/**
	 * Request the logout for the current facebook session
	 * 
	 * @param activity
	 */
	public static void logoutRequest(Activity activity)
	{
		Facebook mFb = FacebookProvider.getFacebook();
		if (mFb.isSessionValid())
		{
			FacebookSessionEvents.onLogoutBegin();
			AsyncFacebookRunner asyncRunner = new AsyncFacebookRunner(mFb);
			asyncRunner.logout(activity, new LogoutRequestListener(activity));
		}
	}

	private static final class LoginDialogListener implements DialogListener
	{
		Activity activity;

		public LoginDialogListener(Activity activity)
		{
			this.activity = activity;
		}
		@Override
		public void onComplete(Bundle values)
		{
			FacebookSessionEvents.onLoginSuccess();
			GuiUtils.info(activity.getString(R.string.share_facebook_success_setup_message), activity);
			clearReferences();
		}

		@Override
		public void onFacebookError(FacebookError error)
		{
			GuiUtils.error(TAG, null, error, activity);
			FacebookSessionEvents.onLoginError(error.getMessage());
			clearReferences();
		}

		@Override
		public void onError(DialogError error)
		{
			GuiUtils.error(TAG, null, new RuntimeException(error), activity);
			FacebookSessionEvents.onLoginError(error.getMessage());
			clearReferences();
		}

		@Override
		public void onCancel()
		{
			FacebookSessionEvents.onLoginError(activity
					.getString(R.string.share_facbook_action_canceled));
			clearReferences();
		}

		void clearReferences()
		{
			activity = null;
		}
	}

	private static class LogoutRequestListener extends FacebookBaseRequestListener
	{
		public LogoutRequestListener(Activity activity)
		{
			super(activity);
		}
		@Override
		public void onComplete(String response, final Object state)
		{
			/*
			 * callback should be run in the original thread, not the background
			 * thread
			 */
			activity.runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					FacebookSessionEvents.onLogoutFinish();
				}
			});
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
