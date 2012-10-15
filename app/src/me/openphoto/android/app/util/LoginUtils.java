package me.openphoto.android.app.util;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

public class LoginUtils
{
	public static String LOGIN_ACTION = "me.openphoto.ACTION_LOGIN";

	public static BroadcastReceiver getAndRegisterDestroyOnLoginActionBroadcastReceiver(
			final String TAG, final Activity activity)
	{
		BroadcastReceiver br = new BroadcastReceiver()
		{
	
			@Override
			public void onReceive(Context context, Intent intent)
			{
				Log.d(TAG, "Received login broadcast message");
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

}
