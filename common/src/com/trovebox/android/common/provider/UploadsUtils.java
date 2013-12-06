
package com.trovebox.android.common.provider;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.trovebox.android.common.CommonConfigurationUtils;
import com.trovebox.android.common.R;
import com.trovebox.android.common.util.CommonUtils;
import com.trovebox.android.common.util.GuiUtils;
import com.trovebox.android.common.util.concurrent.AsyncTaskEx;

public class UploadsUtils
{
    public static final String TAG = UploadsUtils.class.getSimpleName();
    public static String UPLOADS_CLEARED_ACTION = CommonConfigurationUtils.getApplicationContext().getPackageName()
            + ".UPLOADS_CLEARED";

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
        activity.registerReceiver(br, new IntentFilter(UPLOADS_CLEARED_ACTION));
        return br;
    }

    public static void sendUploadsClearedBroadcast()
    {
        Intent intent = new Intent(UPLOADS_CLEARED_ACTION);
        CommonConfigurationUtils.getApplicationContext().sendBroadcast(intent);
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
                    CommonConfigurationUtils.getApplicationContext());
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
