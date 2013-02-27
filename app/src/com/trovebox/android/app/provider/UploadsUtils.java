
package com.trovebox.android.app.provider;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.trovebox.android.app.R;
import com.trovebox.android.app.TroveboxApplication;
import com.trovebox.android.app.util.CommonUtils;
import com.trovebox.android.app.util.GuiUtils;
import com.trovebox.android.app.util.concurrent.AsyncTaskEx;

public class UploadsUtils
{
    public static final String TAG = UploadsUtils.class.getSimpleName();
    public static String BROADCAST_ACTION = "com.trovebox.UPLOADS_CLEARED";

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
                try
                {
                    CommonUtils.debug(TAG,
                            "Received uploads cleared broadcast message");
                    handler.uploadsCleared();
                } catch (Exception ex)
                {
                    GuiUtils.error(TAG, ex);
                }
            }
        };
        activity.registerReceiver(br, new IntentFilter(BROADCAST_ACTION));
        return br;
    }

    public static void sendUploadsClearedBroadcast()
    {
        Intent intent = new Intent(BROADCAST_ACTION);
        TroveboxApplication.getContext().sendBroadcast(intent);
    }

    public static void clearUploadsAsync()
    {
        new ClearUploadsTask().execute();
    }

    public static boolean clearUploads()
    {
        try
        {
            UploadsProviderAccessor uploads = new UploadsProviderAccessor(
                    TroveboxApplication.getContext());
            uploads.deleteAll();
            return true;
        } catch (Exception ex)
        {
            GuiUtils.error(TAG, ex);
        }
        return false;
    }
    public static class ClearUploadsTask extends
            AsyncTaskEx<Void, Void, Boolean>
    {
        @Override
        protected Boolean doInBackground(Void... params)
        {
            return clearUploads();
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
