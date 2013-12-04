
package com.trovebox.android.common.service;

import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;

import com.trovebox.android.common.CommonConfigurationUtils;
import com.trovebox.android.common.provider.PhotoUpload;
import com.trovebox.android.common.util.CommonUtils;
import com.trovebox.android.common.util.GuiUtils;

/**
 * @author Eugene Popovich
 */
public class UploaderServiceUtils {
    public static String PHOTO_UPLOADED_ACTION = CommonConfigurationUtils.getApplicationContext()
            .getPackageName() + ".PHOTO_UPLOADED";
    public static String PHOTO_UPLOAD_UPDATED_ACTION = CommonConfigurationUtils
            .getApplicationContext().getPackageName() + ".PHOTO_UPLOAD_UPDATED";
    public static String PHOTO_UPLOAD_REMOVED_ACTION = CommonConfigurationUtils
            .getApplicationContext().getPackageName() + ".PHOTO_UPLOAD_REMOVED";
    public static String PHOTO_UPLOAD = CommonConfigurationUtils.getApplicationContext()
            .getPackageName() + ".PHOTO_UPLOAD";
    public static String PHOTO_UPLOAD_PROGRESS = CommonConfigurationUtils.getApplicationContext()
            .getPackageName() + ".PHOTO_UPLOAD_PROGRESS";

    public static BroadcastReceiver getAndRegisterOnPhotoUploadedActionBroadcastReceiver(
            final String TAG, final PhotoUploadedHandler handler, final Activity activity) {
        BroadcastReceiver br = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                try {
                    CommonUtils.debug(TAG, "Received photo uploaded broadcast message");
                    handler.photoUploaded();
                } catch (Exception ex) {
                    GuiUtils.error(TAG, ex);
                }
            }
        };
        activity.registerReceiver(br, new IntentFilter(PHOTO_UPLOADED_ACTION));
        return br;
    }

    public static BroadcastReceiver getAndRegisterOnPhotoUploadUpdatedActionBroadcastReceiver(
            final String TAG, final PhotoUploadHandler handler, final Activity activity) {
        BroadcastReceiver br = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                try {
                    CommonUtils.debug(TAG, "Received photo upload updated broadcast message");
                    PhotoUpload photoUpload = intent.getParcelableExtra(PHOTO_UPLOAD);
                    int progress = intent.getIntExtra(PHOTO_UPLOAD_PROGRESS, -1);
                    handler.photoUploadUpdated(photoUpload, progress);
                } catch (Exception ex) {
                    GuiUtils.error(TAG, ex);
                }
            }
        };
        activity.registerReceiver(br, new IntentFilter(PHOTO_UPLOAD_UPDATED_ACTION));
        return br;
    }

    public static BroadcastReceiver getAndRegisterOnPhotoUploadRemovedActionBroadcastReceiver(
            final String TAG, final PhotoUploadHandler handler, final ContextWrapper context) {
        BroadcastReceiver br = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                try {
                    CommonUtils.debug(TAG, "Received photo upload deleted broadcast message");
                    PhotoUpload photoUpload = intent.getParcelableExtra(PHOTO_UPLOAD);
                    handler.photoUploadRemoved(photoUpload);
                } catch (Exception ex) {
                    GuiUtils.error(TAG, ex);
                }
            }
        };
        context.registerReceiver(br, new IntentFilter(PHOTO_UPLOAD_REMOVED_ACTION));
        return br;
    }

    public static void sendPhotoUploadedBroadcast() {
        Intent intent = new Intent(PHOTO_UPLOADED_ACTION);
        CommonConfigurationUtils.getApplicationContext().sendBroadcast(intent);
    }

    public static void sendPhotoUploadUpdatedBroadcast(PhotoUpload photoUpload) {
        sendPhotoUploadUpdatedBroadcast(photoUpload, -1);
    }

    public static void sendPhotoUploadUpdatedBroadcast(PhotoUpload photoUpload, int progress) {
        Intent intent = new Intent(PHOTO_UPLOAD_UPDATED_ACTION);
        intent.putExtra(PHOTO_UPLOAD, photoUpload);
        intent.putExtra(PHOTO_UPLOAD_PROGRESS, progress);
        CommonConfigurationUtils.getApplicationContext().sendBroadcast(intent);
    }

    public static void sendPhotoUploadRemovedBroadcast(PhotoUpload photoUpload) {
        Intent intent = new Intent(PHOTO_UPLOAD_REMOVED_ACTION);
        intent.putExtra(PHOTO_UPLOAD, photoUpload);
        CommonConfigurationUtils.getApplicationContext().sendBroadcast(intent);
    }

    public static interface PhotoUploadedHandler {
        void photoUploaded();
    }

    public static interface PhotoUploadHandler {
        void photoUploadUpdated(PhotoUpload photoUpload, int progress);

        void photoUploadRemoved(PhotoUpload photoUpload);
    }

    /**
     * Check whether UploaderService is already running
     * 
     * @return
     */
    public static boolean isServiceRunning(
            Class<? extends AbstractUploaderService> uploaderServiceClass) {
        final ActivityManager activityManager = (ActivityManager) CommonConfigurationUtils
                .getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        final List<RunningServiceInfo> services = activityManager
                .getRunningServices(Integer.MAX_VALUE);
        String serviceClassName = uploaderServiceClass.getName();
        for (RunningServiceInfo runningServiceInfo : services) {
            if (runningServiceInfo.service.getClassName().equals(serviceClassName)) {
                return true;
            }
        }
        return false;
    }
}
