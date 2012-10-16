package me.openphoto.android.app;

import android.app.Application;

import com.bugsense.trace.BugSenseHandler;

public class OpenPhotoApplication extends Application
{
	@Override
	public void onCreate()
	{
		super.onCreate();

		String bugSenseApiKey = getString(R.string.bugsense_api_key);
		if (bugSenseApiKey != null && bugSenseApiKey.length() > 0)
		{
			BugSenseHandler.setup(this, bugSenseApiKey);
		}
	}
}
