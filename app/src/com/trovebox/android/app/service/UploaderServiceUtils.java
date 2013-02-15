
package com.trovebox.android.app.service;

import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.trovebox.android.app.TroveboxApplication;
import com.trovebox.android.app.util.CommonUtils;
import com.trovebox.android.app.util.GuiUtils;

/**
 * @author Eugene Popovich
 */
public class UploaderServiceUtils {
    public static String PHOTO_UPLOADED_ACTION = "com.trovebox.PHOTO_UPLOADED";

    public static BroadcastReceiver getAndRegisterOnPhotoUploadedActionBroadcastReceiver(
            final String TAG,
            final PhotoUploadedHandler handler,
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
                            "Received photo uploaded broadcast message");
                    handler.photoUploaded();
                } catch (Exception ex)
                {
                    GuiUtils.error(TAG, ex);
                }
            }
        };
        activity.registerReceiver(br, new IntentFilter(PHOTO_UPLOADED_ACTION));
        return br;
    }

    public static void sendPhotoUploadedBroadcast()
    {
        Intent intent = new Intent(PHOTO_UPLOADED_ACTION);
        TroveboxApplication.getContext().sendBroadcast(intent);
    }

    public static interface PhotoUploadedHandler
    {
        void photoUploaded();
    }

    /**
     * Check whether UploaderService is already running
     * 
     * @return
     */
    public static boolean isServiceRunning() {
        final ActivityManager activityManager = (ActivityManager) TroveboxApplication.getContext()
                .getSystemService(Context.ACTIVITY_SERVICE);
        final List<RunningServiceInfo> services = activityManager
                .getRunningServices(Integer.MAX_VALUE);
        String serviceClassName = UploaderService.class.getName();
        for (RunningServiceInfo runningServiceInfo : services) {
            if (runningServiceInfo.service.getClassName().equals(serviceClassName)) {
                return true;
            }
        }
        return false;
    }
}
