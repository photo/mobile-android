package me.openphoto.android.app.util;

import me.openphoto.android.app.BuildConfig;
import android.util.Log;

public class CommonUtils
{
	public static void debug(String TAG, String message)
	{
		if (BuildConfig.DEBUG)
		{
			Log.d(TAG, message);
		}
	}
}
