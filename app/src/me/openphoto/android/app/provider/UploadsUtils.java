package me.openphoto.android.app.provider;

import me.openphoto.android.app.OpenPhotoApplication;
import me.openphoto.android.app.R;
import me.openphoto.android.app.util.CommonUtils;
import me.openphoto.android.app.util.GuiUtils;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;

public class UploadsUtils
{
	public static final String TAG = UploadsUtils.class.getSimpleName();
	public static String BROADCAST_ACTION = "me.openphoto.UPLOADS_CLEARED";

	public static BroadcastReceiver getAndRegisterOnUploadClearedActionBroadcastReceiver(
			final String TAG,
			final UploadsClearedHandler handler,
			final Activity activity)
	{
		BroadcastReceiver br = new BroadcastReceiver()
		{

			@Override
			public void onReceive(Context context, Intent intent)
			{
				CommonUtils.debug(TAG,
						"Received uploads cleared broadcast message");
				handler.uploadsCleared();
			}
		};
		activity.registerReceiver(br, new IntentFilter(BROADCAST_ACTION));
		return br;
	}

	public static void sendUploadsClearedBroadcast()
	{
		Intent intent = new Intent(BROADCAST_ACTION);
		OpenPhotoApplication.getContext().sendBroadcast(intent);
	}

	public static void clearUploads()
	{
		new ClearUploadsTask().execute();
	}

	public static class ClearUploadsTask extends
			AsyncTask<Void, Void, Boolean>
	{
		@Override
		protected Boolean doInBackground(Void... params)
		{
			try
			{
				UploadsProviderAccessor uploads = new UploadsProviderAccessor(
						OpenPhotoApplication.getContext());
				uploads.deleteAll();
				return true;
			} catch (Exception ex)
			{
				GuiUtils.error(TAG, ex);
			}
			return false;
		}

		@Override
		protected void onPostExecute(Boolean result)
		{
			super.onPostExecute(result);
			if (result.booleanValue())
			{
				GuiUtils.info(R.string.sync_cleared_message);
				sendUploadsClearedBroadcast();
			}
		}
	}

	public static interface UploadsClearedHandler
	{
		void uploadsCleared();
	}
}
