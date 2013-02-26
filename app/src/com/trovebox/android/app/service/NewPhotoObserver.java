
package com.trovebox.android.app.service;

import java.io.File;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.FileObserver;
import android.support.v4.app.NotificationCompat;
import android.webkit.MimeTypeMap;

import com.trovebox.android.app.Preferences;
import com.trovebox.android.app.R;
import com.trovebox.android.app.TroveboxApplication;
import com.trovebox.android.app.net.UploadMetaData;
import com.trovebox.android.app.net.account.AccountLimitUtils;
import com.trovebox.android.app.provider.UploadsProviderAccessor;
import com.trovebox.android.app.util.CommonUtils;
import com.trovebox.android.app.util.TrackerUtils;

public class NewPhotoObserver extends FileObserver {

    private static final String TAG = NewPhotoObserver.class.getSimpleName();
    private final String mPath;
    private final Context mContext;

    public NewPhotoObserver(Context context, String path) {
        super(path, FileObserver.CREATE);
        mPath = path;
        mContext = context;
    }

    @Override
    public void onEvent(int event, String fileName) {
        if (event == FileObserver.CREATE && !fileName.equals(".probe")) {
            File file = new File(mPath + "/" + fileName);
            CommonUtils.debug(TAG, "File created [" + file.getAbsolutePath() + "]");
            // fix for the issue #309
            String type = getMimeType(file);
            if (type != null && type.toLowerCase().startsWith("image/"))
            {
                TrackerUtils.trackBackgroundEvent("autoupload_observer", CommonUtils.format("Processed for Mime-Type: %1$s", type));
                if (!Preferences.isAutoUploadActive(mContext) || !CommonUtils.checkLoggedIn(true)) {
                    return;
                }
                if (checkLimits())
                {
                    TrackerUtils.trackLimitEvent("auto_upload_limit_check", "success");
                    CommonUtils.debug(TAG, "Adding new autoupload to queue for file: " + fileName);
                    UploadsProviderAccessor uploads = new UploadsProviderAccessor(mContext);
                    UploadMetaData metaData = new UploadMetaData();
                    metaData.setTags(Preferences.getAutoUploadTag(mContext));
                    uploads.addPendingAutoUpload(Uri.fromFile(file), metaData);
                    mContext.startService(new Intent(mContext, UploaderService.class));
                } else
                {
                    TrackerUtils.trackLimitEvent("autoupload_observer", "fail");
                    showAutouploadIgnoredNotification(file);
                }
            } else
            {
                TrackerUtils.trackBackgroundEvent("autoupload_observer",
                        CommonUtils.format("Skipped for Mime-Type: %1$s", type == null ? "null"
                                : type));
            }
        }
    }

    /**
     * Check upload limits to determine whether autoupload could be done
     * @return
     */
    private boolean checkLimits()
    {
        AccountLimitUtils.updateLimitInformationCache();
        if (Preferences.isProUser())
        {
            return true;
        } else
        {
            int remaining = Preferences.getRemainingUploadingLimit();
            UploadsProviderAccessor uploads = new UploadsProviderAccessor(
                    TroveboxApplication.getContext());
            int pending = uploads.getPendingUploadsCount();
            boolean result = remaining - pending >= 1;
            return result;
        }
    }

    /**
     * Show the autoupload ignored notification
     * 
     * @param file
     */
    private void showAutouploadIgnoredNotification(File file) {
        NotificationManager notificationManager = (NotificationManager) mContext
                .getSystemService(Context.NOTIFICATION_SERVICE);
        int icon = R.drawable.icon;
        long when = System.currentTimeMillis();
        String contentMessageTitle;
        contentMessageTitle = CommonUtils
                .getStringResource(R.string.upload_limit_reached_message);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                mContext);
        Notification notification = builder
                .setContentTitle(CommonUtils.getStringResource(R.string.autoupload_ignored))
                .setContentText(contentMessageTitle)
                .setWhen(when)
                .setSmallIcon(icon)
                .setAutoCancel(true)
                .build();

        notificationManager.notify(file.hashCode(), notification);
    }

    /**
     * Get the mime type for the file
     * 
     * @param file
     * @return
     */
    public static String getMimeType(File file)
    {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(file).getPath());
        if (extension != null) {
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            type = mime.getMimeTypeFromExtension(extension);
        }
        CommonUtils.debug(TAG, "File: %1$s; extension %2$s; MimeType: %3$s",
                file.getAbsolutePath(), extension, type);
        return type;
    }
}
