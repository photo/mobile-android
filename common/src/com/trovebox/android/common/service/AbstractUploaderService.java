
package com.trovebox.android.common.service;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.widget.RemoteViews;

import com.trovebox.android.common.CommonConfigurationUtils;
import com.trovebox.android.common.R;
import com.trovebox.android.common.model.Photo;
import com.trovebox.android.common.net.ITroveboxApi;
import com.trovebox.android.common.net.PhotosResponse;
import com.trovebox.android.common.net.UploadMetaData;
import com.trovebox.android.common.net.UploadResponse;
import com.trovebox.android.common.net.HttpEntityWithProgress.ProgressListener;
import com.trovebox.android.common.provider.PhotoUpload;
import com.trovebox.android.common.provider.UploadsProviderAccessor;
import com.trovebox.android.common.service.UploaderServiceUtils.PhotoUploadHandler;
import com.trovebox.android.common.util.CommonUtils;
import com.trovebox.android.common.util.GuiUtils;
import com.trovebox.android.common.util.ImageUtils;
import com.trovebox.android.common.util.SHA1Utils;
import com.trovebox.android.common.util.TrackerUtils;

public abstract class AbstractUploaderService extends Service implements PhotoUploadHandler {
    private static final int NOTIFICATION_UPLOAD_PROGRESS = 1;
    private static final String TAG = AbstractUploaderService.class.getSimpleName();
    private ITroveboxApi mApi;

    private static ConnectivityChangeReceiver sReceiver;

    private volatile Looper mServiceLooper;
    private volatile ServiceHandler mServiceHandler;

    private NotificationManager mNotificationManager;
    private long mNotificationLastUpdateTime;

    private Set<Long> mIdsToSkip = new HashSet<Long>();
    /**
     * According to this http://stackoverflow.com/a/7370448/527759 need so send
     * different request codes each time we put some extra data into intent, or
     * it will not be recreated
     */
    int requestCounter = 0;

    protected boolean mCheckPhotoExistingOnServer = false;

    BroadcastReceiver mUploadRemovedReceiver;

    /**
     * Now it is static and uses weak reference
     * http://stackoverflow.com/a/11408340/527759
     */
    private static final class ServiceHandler extends Handler {
        private final WeakReference<AbstractUploaderService> mService;

        public ServiceHandler(Looper looper, AbstractUploaderService service) {
            super(looper);
            mService = new WeakReference<AbstractUploaderService>(service);
        }

        @Override
        public void handleMessage(Message msg) {
            AbstractUploaderService service = mService.get();
            if (service != null) {
                service.handleIntent((Intent) msg.obj);
            }
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        HandlerThread thread = new HandlerThread(TAG);
        thread.start();
        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper, this);

        mApi = CommonConfigurationUtils.getApi();
        setUpConnectivityWatcher();
        CommonUtils.debug(TAG, "Service created");

        mUploadRemovedReceiver = UploaderServiceUtils
                .getAndRegisterOnPhotoUploadRemovedActionBroadcastReceiver(TAG, this, this);
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(sReceiver);
        CommonUtils.debug(TAG, "Service destroyed");
        super.onDestroy();
        unregisterReceiver(mUploadRemovedReceiver);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        msg.obj = intent;
        mServiceHandler.sendMessage(msg);
        return START_STICKY;
    }

    private void handleIntent(Intent intent) {
        if (!GuiUtils.checkLoggedInAndOnline(true)) {
            return;
        }
        UploadsProviderAccessor uploads = new UploadsProviderAccessor(this);
        List<PhotoUpload> pendingUploads;
        synchronized (mIdsToSkip) {
            mIdsToSkip.clear();
            pendingUploads = uploads.getPendingUploads();
        }
        boolean hasSuccessfulUploads = false;
        NotificationCompat.Builder successNotification = getStandardSuccessNotification();
        int uploadedCount = 0;
        int skippedCount = 0;
        int successNotificationId = requestCounter++;
        ArrayList<Photo> uploadedPhotos = new ArrayList<Photo>();
        List<PhotoUploadDetails> uploadDetails = new ArrayList<PhotoUploadDetails>();
        for (final PhotoUpload photoUpload : pendingUploads) {
            if (!GuiUtils.checkLoggedInAndOnline(true)) {
                return;
            }
            Log.i(TAG, "Starting upload to Trovebox: " + photoUpload.getPhotoUri());
            if (isUploadRemoved(photoUpload)) {
                CommonUtils.info(TAG, "Upload skipped, because it was canceled externally");
                continue;
            }
            String filePath = ImageUtils.getRealPathFromURI(this, photoUpload.getPhotoUri());
            if (filePath == null || !(new File(filePath).exists())) {
                uploads.delete(photoUpload.getId());
                // TODO: Maybe set error, and set as "do not try again"
                CommonUtils.info(TAG, "Upload canceled, because file does not exist anymore.");
                UploaderServiceUtils.sendPhotoUploadRemovedBroadcast(photoUpload);
                continue;
            }
            boolean wifiOnlyUpload = CommonConfigurationUtils.isWiFiOnlyUploadActive();
            if (wifiOnlyUpload && !CommonUtils.isWiFiActive()) {
                CommonUtils.info(TAG, "Upload canceled because WiFi is not active anymore");
                break;
            }
            File file = new File(filePath);
            stopErrorNotification(file);
            try {
                boolean skipped = false;
                Photo photo = null;
                if (mCheckPhotoExistingOnServer) {
                    String hash = SHA1Utils.computeSha1ForFile(filePath);
                    PhotosResponse photos = mApi.getPhotos(hash);
                    if (!photos.isSuccess()) {
                        uploads.setError(photoUpload.getId(), photos.getAlertMessage());
                        photoUpload.setError(photos.getAlertMessage());
                        showErrorNotification(photoUpload, file);
                        if (!isUploadRemoved(photoUpload)) {
                            UploaderServiceUtils.sendPhotoUploadUpdatedBroadcast(photoUpload);
                        }
                        continue;
                    }
                    if (photos.getPhotos().size() > 0) {
                        CommonUtils.debug(TAG, "The photo " + filePath + " with hash " + hash
                                + " already found on the server. Skip uploading");
                        skipped = true;
                        photo = photos.getPhotos().get(0);
                    }
                }
                if (isUploadRemoved(photoUpload)) {
                    CommonUtils.info(TAG, "Upload skipped, because it was canceled externally");
                    continue;
                }
                if (photo == null) {
                    long start = System.currentTimeMillis();
                    final Notification notification = CommonUtils.isIceCreamSandwichOrHigher() ? null
                            : showUploadNotification(file);
                    final NotificationCompat.Builder builder = CommonUtils
                            .isIceCreamSandwichOrHigher() ? getStandardUploadNotification(file)
                            : null;
                    UploadMetaData metaData = photoUpload.getMetaData();
                    if (!isUploadRemoved(photoUpload)) {
                        UploaderServiceUtils.sendPhotoUploadUpdatedBroadcast(photoUpload, 0);
                    }
                    UploadResponse uploadResponse = mApi.uploadPhoto(file, metaData,
                            photoUpload.getToken(), photoUpload.getHost(), new ProgressListener() {
                                private int mLastProgress = -1;
                                private boolean mCancelled;

                                @Override
                                public void transferred(long transferedBytes, long totalBytes) {
                                    int newProgress = (int) (transferedBytes * 100 / totalBytes);
                                    if (mLastProgress < newProgress) {
                                        mLastProgress = newProgress;
                                        if (mCancelled || isUploadRemoved(photoUpload)) {
                                            if (!mCancelled) {
                                                CommonUtils
                                                        .info(TAG,
                                                                "Upload interrupted, because it was canceled externally");
                                                mCancelled = true;
                                                // stopUploadNotification();
                                            }
                                        }
                                        // else
                                        {
                                            if (!mCancelled) {
                                                UploaderServiceUtils
                                                        .sendPhotoUploadUpdatedBroadcast(
                                                                photoUpload, mLastProgress);
                                            }
                                            if (builder != null) {
                                                updateUploadNotification(builder, mLastProgress,
                                                        100);
                                            } else {
                                                updateUploadNotification(notification,
                                                        mLastProgress, 100);
                                            }
                                        }
                                    }
                                }

                                @Override
                                public boolean isCancelled() {
                                    // TODO need to return mCancelled here, but
                                    // looks like abort causes
                                    // java.lang.IllegalStateException: Adapter
                                    // is detached.
                                    return false;
                                }
                            });
                    // if (isUploadRemoved(photoUpload)) {
                    // stopUploadNotification();
                    // CommonUtils.info(TAG,
                    // "Upload skipped, because it was canceled externally");
                    // continue;
                    // }
                    if (uploadResponse.isSuccess()) {
                        Log.i(TAG, "Upload to Trovebox completed for: " + photoUpload.getPhotoUri());
                        photo = uploadResponse.getPhoto();
                        TrackerUtils.trackDataLoadTiming(System.currentTimeMillis() - start,
                                "photoUpload", TAG);
                    } else {
                        uploads.setError(photoUpload.getId(), uploadResponse.getAlertMessage());
                        photoUpload.setError(uploadResponse.getAlertMessage());
                        showErrorNotification(photoUpload, file);
                        stopUploadNotification();
                        if (!isUploadRemoved(photoUpload)) {
                            UploaderServiceUtils.sendPhotoUploadUpdatedBroadcast(photoUpload);
                        }
                        continue;
                    }
                }
                adjustRemainingUploadingLimit(-1);
                hasSuccessfulUploads = true;
                long uploaded = uploads.setUploaded(photoUpload.getId());
                photoUpload.setUploaded(uploaded);
                if (!isUploadRemoved(photoUpload)) {
                    UploaderServiceUtils.sendPhotoUploadUpdatedBroadcast(photoUpload);
                }
                if (skipped) {
                    skippedCount++;
                } else {
                    uploadedCount++;
                }
                uploadedPhotos.add(photo);
                uploadDetails.add(new PhotoUploadDetails(photoUpload, skipped, file));
                updateSuccessNotification(successNotification, uploadedCount, skippedCount,
                        uploadedPhotos, uploadDetails, successNotificationId);
                shareIfRequested(photoUpload, photo, true);
                if (!skipped) {
                    TrackerUtils.trackServiceEvent("photo_upload", TAG);
                    if (!isUploadRemoved(photoUpload)) {
                        UploaderServiceUtils.sendPhotoUploadedBroadcast();
                    }
                } else {
                    TrackerUtils.trackServiceEvent("photo_upload_skip", TAG);
                }
            } catch (Exception e) {
                CommonUtils.error(TAG, e);
                uploads.setError(photoUpload.getId(),
                        e.getClass().getSimpleName() + ": " + e.getLocalizedMessage());
                photoUpload.setError(e.getClass().getSimpleName() + ": " + e.getLocalizedMessage());
                showErrorNotification(photoUpload, file);
                if (!isUploadRemoved(photoUpload)) {
                    UploaderServiceUtils.sendPhotoUploadUpdatedBroadcast(photoUpload);
                }
            }

            stopUploadNotification();
        }
        // to avoid so many invalid json response errors at unauthorised wi-fi
        // networks we need to
        // update limit information only in case we had successful uploads
        if (hasSuccessfulUploads) {
            runAfterSuccessfullUploads();
        }
    }

    public void shareIfRequested(PhotoUpload photoUpload, Photo photo, boolean silent) {
        if (photo != null && !photo.isPrivate()) {
            if (photoUpload.isShareOnTwitter()) {
                shareOnTwitter(photo, silent);
            }
            if (photoUpload.isShareOnFacebook()) {
                shareOnFacebook(photo, silent);
            }
        }
    }

    protected void shareOnFacebook(Photo photo, boolean silent) {
        throw new IllegalStateException("Not implemented");
    }

    protected void shareOnTwitter(Photo photo, boolean silent) {
        throw new IllegalStateException("Not implemented");
    }

    protected void adjustRemainingUploadingLimit(int shift) {

    }

    protected void runAfterSuccessfullUploads() {

    }

    protected abstract int getNotificationIcon();

    protected abstract PendingIntent getStandardPendingIntent();

    protected abstract PendingIntent getErrorPendingIntent(PhotoUpload photoUpload);

    protected abstract PendingIntent getSuccessPendingIntent(ArrayList<Photo> photos);

    /**
     * This is used for the devices with android version >= 4.x
     */
    private NotificationCompat.Builder getStandardUploadNotification(File file) {
        int icon = getNotificationIcon();
        CharSequence tickerText = getString(R.string.notification_uploading_photo, file.getName());
        long when = System.currentTimeMillis();
        CharSequence contentMessageTitle = getString(R.string.notification_uploading_photo,
                file.getName());

        PendingIntent contentIntent = getStandardPendingIntent();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setTicker(tickerText).setWhen(when).setSmallIcon(icon)
                .setContentTitle(contentMessageTitle).setProgress(100, 0, true);
        if (contentIntent != null) {
            builder.setContentIntent(contentIntent);
        }
        return builder;
    }

    private Notification showUploadNotification(File file) {
        int icon = getNotificationIcon();
        CharSequence tickerText = getString(R.string.notification_uploading_photo, file.getName());
        long when = System.currentTimeMillis();
        CharSequence contentMessageTitle = getString(R.string.notification_uploading_photo,
                file.getName());

        PendingIntent contentIntent = getStandardPendingIntent();
        RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.notification_upload);
        contentView.setImageViewResource(R.id.image, icon);
        contentView.setTextViewText(R.id.title, contentMessageTitle);
        contentView.setProgressBar(R.id.progress, 100, 0, true);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        if (contentIntent != null) {
            builder.setContentIntent(contentIntent);
        }
        Notification notification = builder.setTicker(tickerText).setWhen(when).setSmallIcon(icon)
                .setContent(contentView).build();
        CommonUtils.debug(TAG, "Is notification content view null: "
                + (notification.contentView == null));
        // need to explicitly set contentView again because of bug in compat
        // library. Solution found here
        // http://stackoverflow.com/a/12574534/527759
        notification.contentView = contentView;

        mNotificationManager.notify(NOTIFICATION_UPLOAD_PROGRESS, notification);

        return notification;
    }

    protected void updateUploadNotification(final Notification notification, int progress, int max) {
        if (progress < max) {
            long now = System.currentTimeMillis();
            if (now - mNotificationLastUpdateTime < 500) {
                return;
            }
            mNotificationLastUpdateTime = now;

            notification.contentView.setProgressBar(R.id.progress, max, progress, false);
        } else {
            notification.contentView.setProgressBar(R.id.progress, 0, 0, true);
        }
        mNotificationManager.notify(NOTIFICATION_UPLOAD_PROGRESS, notification);
    }

    /**
     * This is used to update progress in the notification message for the
     * platform version >= 4.x
     */
    protected void updateUploadNotification(final NotificationCompat.Builder builder, int progress,
            int max) {
        if (progress < max) {
            long now = System.currentTimeMillis();
            if (now - mNotificationLastUpdateTime < 500) {
                return;
            }
            mNotificationLastUpdateTime = now;

            builder.setProgress(max, progress, false);
        } else {
            builder.setProgress(0, 0, true);
        }
        mNotificationManager.notify(NOTIFICATION_UPLOAD_PROGRESS, builder.build());
    }

    private void stopUploadNotification() {
        mNotificationManager.cancel(NOTIFICATION_UPLOAD_PROGRESS);
    }

    private void showErrorNotification(PhotoUpload photoUpload, File file) {
        int icon = getNotificationIcon();
        CharSequence titleText = photoUpload.getError() == null ? getString(R.string.notification_upload_failed_title)
                : getString(R.string.notification_upload_failed_title_with_reason,
                        photoUpload.getError());
        long when = System.currentTimeMillis();
        CharSequence contentMessageTitle = getString(R.string.notification_upload_failed_text,
                file.getName());

        PendingIntent contentIntent = getErrorPendingIntent(photoUpload);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        if (contentIntent != null) {
            builder.setContentIntent(contentIntent);
        }
        Notification notification = builder.setContentTitle(titleText)
                .setContentText(contentMessageTitle).setWhen(when).setSmallIcon(icon)
                .setAutoCancel(true).build();
        // Notification notification = new Notification(icon, titleText, when);
        // notification.flags |= Notification.FLAG_AUTO_CANCEL;
        // notification.setLatestEventInfo(this, titleText, contentMessageTitle,
        // contentIntent);

        mNotificationManager.notify(file.hashCode(), notification);
    }

    private void stopErrorNotification(File file) {
        mNotificationManager.cancel(file.hashCode());
    }

    private NotificationCompat.Builder getStandardSuccessNotification() {
        int icon = getNotificationIcon();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(icon).setAutoCancel(true);
        return builder;
    }

    private void updateSuccessNotification(NotificationCompat.Builder builder, int uploaded,
            int skipped, ArrayList<Photo> photos, List<PhotoUploadDetails> uploadDetails,
            int notificationId) {
        CharSequence contentMessageTitle;
        CharSequence titleText;
        if (photos.size() == 1) {
            PhotoUploadDetails pud = uploadDetails.get(0);
            titleText = getString(pud.skipped ? R.string.notification_upload_skipped_title
                    : R.string.notification_upload_success_title);
            contentMessageTitle = getUploadTitle(pud);
        } else {
            contentMessageTitle = getString(R.string.notification_upload_multiple_text, uploaded,
                    skipped);
            titleText = getString(R.string.notification_upload_multiple_title);

            NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
            // Sets a title for the Inbox style big view
            inboxStyle.setBigContentTitle(getString(R.string.notification_upload_multiple_details));
            // Moves events into the big view
            for (int i = 0; i < uploadDetails.size(); i++) {

                PhotoUploadDetails pud = uploadDetails.get(i);
                String title = getUploadTitle(pud);
                String line = getString(
                        pud.skipped ? R.string.notification_upload_multiple_detail_skipped
                                : R.string.notification_upload_multiple_detail_done, title);
                inboxStyle.addLine(Html.fromHtml(line));
            }
            // Moves the big view style object into the notification object.
            builder.setStyle(inboxStyle);
        }
        long when = System.currentTimeMillis();
        PendingIntent contentIntent = getSuccessPendingIntent(photos);
        if (contentIntent != null) {
            builder.setContentIntent(contentIntent);
        }
        builder.setContentTitle(titleText).setContentText(contentMessageTitle).setWhen(when);
        mNotificationManager.notify(notificationId, builder.build());
    }

    private String getUploadTitle(PhotoUploadDetails pud) {
        String contentMessageTitle;
        String imageName = pud.file.getName();
        if (!TextUtils.isEmpty(pud.photoUpload.getMetaData().getTitle())) {
            imageName = pud.photoUpload.getMetaData().getTitle();
        }
        contentMessageTitle = getString(R.string.notification_upload_success_text, imageName);
        return contentMessageTitle;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void setUpConnectivityWatcher() {
        sReceiver = new ConnectivityChangeReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(sReceiver, filter);
    }

    public void setCheckPhotoExistingOnServer(boolean mCheckPhotoExistingOnServer) {
        this.mCheckPhotoExistingOnServer = mCheckPhotoExistingOnServer;
    }

    @Override
    public void photoUploadUpdated(PhotoUpload photoUpload, int progress) {
        // do nothing
    }

    @Override
    public void photoUploadRemoved(PhotoUpload photoUpload) {
        synchronized (mIdsToSkip) {
            mIdsToSkip.add(photoUpload.getId());
        }
    }

    boolean isUploadRemoved(PhotoUpload upload) {
        return mIdsToSkip.contains(upload.getId());
    }

    private class PhotoUploadDetails {
        PhotoUpload photoUpload;
        boolean skipped;
        File file;

        public PhotoUploadDetails(PhotoUpload photoUpload, boolean skipped, File file) {
            super();
            this.photoUpload = photoUpload;
            this.skipped = skipped;
            this.file = file;
        }
    }

    private class ConnectivityChangeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            boolean online = CommonUtils.isOnline();
            boolean wifiOnlyUpload = CommonConfigurationUtils.isWiFiOnlyUploadActive();
            CommonUtils.debug(TAG, "Connectivity changed to " + (online ? "online" : "offline"));
            if (online && (!wifiOnlyUpload || (wifiOnlyUpload && CommonUtils.isWiFiActive()))) {
                context.startService(new Intent(context, AbstractUploaderService.class));
            }
        }
    }
}
