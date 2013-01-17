
package me.openphoto.android.app.service;

import me.openphoto.android.app.OpenPhotoApplication;
import me.openphoto.android.app.util.CommonUtils;
import me.openphoto.android.app.util.GuiUtils;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

/**
 * @author Eugene Popovich
 */
public class UploaderServiceUtils {
    public static String PHOTO_UPLOADED_ACTION = "me.openphoto.PHOTO_UPLOADED";

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
        OpenPhotoApplication.getContext().sendBroadcast(intent);
    }

    public static interface PhotoUploadedHandler
    {
        void photoUploaded();
    }
}
