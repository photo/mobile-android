
package com.trovebox.android.app.service;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import twitter4j.Twitter;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.RemoteViews;

import com.facebook.android.Facebook;
import com.trovebox.android.app.FacebookFragment;
import com.trovebox.android.app.MainActivity;
import com.trovebox.android.app.PhotoDetailsActivity;
import com.trovebox.android.app.Preferences;
import com.trovebox.android.app.R;
import com.trovebox.android.app.TwitterFragment;
import com.trovebox.android.app.UploadActivity;
import com.trovebox.android.app.facebook.FacebookProvider;
import com.trovebox.android.app.model.Photo;
import com.trovebox.android.app.model.utils.PhotoUtils;
import com.trovebox.android.app.net.HttpEntityWithProgress.ProgressListener;
import com.trovebox.android.app.net.ITroveboxApi;
import com.trovebox.android.app.net.PhotosResponse;
import com.trovebox.android.app.net.ReturnSizes;
import com.trovebox.android.app.net.UploadResponse;
import com.trovebox.android.app.net.account.AccountLimitUtils;
import com.trovebox.android.app.provider.PhotoUpload;
import com.trovebox.android.app.provider.UploadsProviderAccessor;
import com.trovebox.android.app.twitter.TwitterProvider;
import com.trovebox.android.app.util.CommonUtils;
import com.trovebox.android.app.util.GuiUtils;
import com.trovebox.android.app.util.ImageUtils;
import com.trovebox.android.app.util.SHA1Utils;
import com.trovebox.android.app.util.TrackerUtils;
import com.trovebox.android.app.util.Utils;

public class UploaderService extends Service {
    private static final int NOTIFICATION_UPLOAD_PROGRESS = 1;
    private static final String TAG = UploaderService.class.getSimpleName();
    private ITroveboxApi mApi;

    private static ArrayList<NewPhotoObserver> sNewPhotoObservers;
    private static ConnectivityChangeReceiver sReceiver;
    private static MediaMountedReceiver mediaMountedReceiver;
    private static Set<String> alreadyObservingPaths;

    private volatile Looper mServiceLooper;
    private volatile ServiceHandler mServiceHandler;

    private NotificationManager mNotificationManager;
    private long mNotificationLastUpdateTime;
    /**
     * According to this http://stackoverflow.com/a/7370448/527759 need so send
     * different request codes each time we put some extra data into intent, or
     * it will not be recreated
     */
    int requestCounter = 0;

    /**
     * Now it is static and uses weak reference
     * http://stackoverflow.com/a/11408340/527759
     */
    private static final class ServiceHandler extends Handler
    {
        private final WeakReference<UploaderService> mService;

        public ServiceHandler(Looper looper, UploaderService service) {
            super(looper);
            mService = new WeakReference<UploaderService>(service);
        }

        @Override
        public void handleMessage(Message msg) {
            UploaderService service = mService.get();
            if (service != null)
            {
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

        mApi = Preferences.getApi(this);
        startFileObserver();
        setUpConnectivityWatcher();
        setUpMediaWatcher();
        CommonUtils.debug(TAG, "Service created");
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(sReceiver);
        unregisterReceiver(mediaMountedReceiver);
        for (NewPhotoObserver observer : sNewPhotoObservers) {
            observer.stopWatching();
        }
        CommonUtils.debug(TAG, "Service destroyed");
        super.onDestroy();
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
        if (!CommonUtils.checkLoggedInAndOnline(true))
        {
            return;
        }
        UploadsProviderAccessor uploads = new UploadsProviderAccessor(this);
        List<PhotoUpload> pendingUploads = uploads.getPendingUploads();
        boolean hasSuccessfulUploads = false;
        for (PhotoUpload photoUpload : pendingUploads) {
            if (!CommonUtils.checkLoggedInAndOnline(true))
            {
                return;
            }
            Log.i(TAG, "Starting upload to Trovebox: " + photoUpload.getPhotoUri());
            String filePath = ImageUtils.getRealPathFromURI(this, photoUpload.getPhotoUri());
            if (filePath == null || !(new File(filePath).exists())) {
                uploads.delete(photoUpload.getId());
                // TODO: Maybe set error, and set as "do not try again"
                CommonUtils.info(TAG, "Upload canceled, because file does not exist anymore.");
                continue;
            }
            boolean wifiOnlyUpload = Preferences
                    .isWiFiOnlyUploadActive(getBaseContext());
            if (wifiOnlyUpload && !Utils.isWiFiActive(getBaseContext()))
            {
                CommonUtils.info(TAG, "Upload canceled because WiFi is not active anymore");
                break;
            }
            File file = new File(filePath);
            stopErrorNotification(file);
            try {
                String hash = SHA1Utils.computeSha1ForFile(filePath);
                PhotosResponse photos = mApi.getPhotos(hash);
                if(!photos.isSuccess())
                {
                    uploads.setError(photoUpload.getId(),
                            photos.getAlertMessage());
                    showErrorNotification(photoUpload, file);
                    continue;
                }
                Photo photo = null;
                boolean skipped = false;
                if (photos.getPhotos().size() > 0)
                {
                    CommonUtils.debug(TAG, "The photo " + filePath
                            + " with hash " + hash
                            + " already found on the server. Skip uploading");
                    skipped = true;
                    photo = photos.getPhotos().get(0);
                } else
                {
                    long start = System.currentTimeMillis();
                    final Notification notification = CommonUtils.isIceCreamSandwichOrHigher() ? null
                            : showUploadNotification(file);
                    final NotificationCompat.Builder builder = CommonUtils
                            .isIceCreamSandwichOrHigher() ? getStandardUploadNotification(file)
                            : null;
                    UploadResponse uploadResponse = mApi.uploadPhoto(file,
                            photoUpload.getMetaData(),
                            new ProgressListener()
                            {
                                private int mLastProgress = -1;

                                @Override
                                public void transferred(long transferedBytes,
                                        long totalBytes)
                                {
                                    int newProgress = (int) (transferedBytes * 100 / totalBytes);
                                    if (mLastProgress < newProgress)
                                    {
                                        mLastProgress = newProgress;
                                        if (builder != null)
                                        {
                                            updateUploadNotification(builder,
                                                    mLastProgress, 100);
                                        } else
                                        {
                                            updateUploadNotification(notification, mLastProgress,
                                                    100);
                                        }
                                    }
                                }
                            });
                    if(uploadResponse.isSuccess())
                    {
                        Log.i(TAG, "Upload to Trovebox completed for: "
                                + photoUpload.getPhotoUri());
                        photo = uploadResponse.getPhoto();
                        TrackerUtils.trackDataLoadTiming(System.currentTimeMillis() - start,
                                "photoUpload", TAG);
                    } else
                    {
                        photoUpload.setError(uploadResponse.getAlertMessage());
                        showErrorNotification(photoUpload, file);
                        continue;
                    }
                }
                Preferences.adjustRemainingUploadingLimit(-1);
                hasSuccessfulUploads = true;
                uploads.setUploaded(photoUpload.getId());
                showSuccessNotification(photoUpload, file,
                        photo, skipped);
                shareIfRequested(photoUpload, photo, true);
                if (!skipped)
                {
                    TrackerUtils.trackServiceEvent("photo_upload", TAG);
                    UploaderServiceUtils.sendPhotoUploadedBroadcast();
                } else
                {
                    TrackerUtils.trackServiceEvent("photo_upload_skip", TAG);
                }
            } catch (Exception e) {
                uploads.setError(photoUpload.getId(),
                        e.getClass().getSimpleName() + ": " + e.getLocalizedMessage());
                showErrorNotification(photoUpload, file);
                GuiUtils.processError(TAG,
                        R.string.errorCouldNotUploadTakenPhoto,
                        e,
                        getApplicationContext(), !photoUpload.isAutoUpload());
            }

            stopUploadNotification();
        }
        // to avoid so many invalid json response errors at unauthorised wi-fi
        // networks we need to
        // update limit information only in case we had successful uploads
        if (hasSuccessfulUploads)
        {
            AccountLimitUtils.updateLimitInformationCacheIfNecessary(true);
        }
    }

    public void shareIfRequested(PhotoUpload photoUpload,
            Photo photo, boolean silent)
    {
        if (photo != null && !photo.isPrivate())
        {
            if (photoUpload.isShareOnTwitter())
            {
                shareOnTwitter(photo, silent);
            }
            if (photoUpload.isShareOnFacebook())
            {
                shareOnFacebook(photo, silent);
            }
        }
    }

    private void shareOnFacebook(Photo photo, boolean silent)
    {
        try
        {
            Facebook facebook = FacebookProvider.getFacebook();
            if (facebook.isSessionValid())
            {
                ReturnSizes thumbSize = FacebookFragment.thumbSize;
                photo = PhotoUtils.validateUrlForSizeExistAndReturn(photo, thumbSize);
                FacebookFragment.sharePhoto(null, photo, thumbSize,
                        getApplicationContext());
            }
        } catch (Exception ex)
        {
            GuiUtils.processError(TAG, R.string.errorCouldNotSendFacebookPhoto,
                    ex,
                    getApplicationContext(),
                    !silent);
        }
    }

    private void shareOnTwitter(Photo photo, boolean silent)
    {
        try
        {
            Twitter twitter = TwitterProvider
                    .getTwitter(getApplicationContext());
            if (twitter != null)
            {
                String message = TwitterFragment.getDefaultTweetMessage(getApplicationContext(),
                        photo);
                TwitterFragment.sendTweet(message, twitter);
            }
        } catch (Exception ex)
        {
            GuiUtils.processError(TAG, R.string.errorCouldNotSendTweet, ex,
                    getApplicationContext(),
                    !silent);
        }
    }

    /**
     * This is used for the devices with android version >= 4.x
     */
    private NotificationCompat.Builder getStandardUploadNotification(File file)
    {
        int icon = R.drawable.icon;
        CharSequence tickerText = getString(R.string.notification_uploading_photo, file.getName());
        long when = System.currentTimeMillis();
        CharSequence contentMessageTitle = getString(R.string.notification_uploading_photo,
                file.getName());

        // TODO adjust this to show the upload manager
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                this);
        builder
                .setTicker(tickerText)
                .setWhen(when)
                .setSmallIcon(icon)
                .setContentTitle(contentMessageTitle)
                .setProgress(100, 0, true)
                .setContentIntent(contentIntent);
        return builder;
    }

    private Notification showUploadNotification(File file)
    {
        int icon = R.drawable.icon;
        CharSequence tickerText = getString(R.string.notification_uploading_photo, file.getName());
        long when = System.currentTimeMillis();
        CharSequence contentMessageTitle = getString(R.string.notification_uploading_photo,
                file.getName());

        // TODO adjust this to show the upload manager
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.notification_upload);
        contentView.setImageViewResource(R.id.image, icon);
        contentView.setTextViewText(R.id.title, contentMessageTitle);
        contentView.setProgressBar(R.id.progress, 100, 0, true);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                this);
        Notification notification = builder
                .setTicker(tickerText)
                .setWhen(when)
                .setSmallIcon(icon)
                .setContentIntent(contentIntent)
                .setContent(contentView)
                .build();
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
        int icon = R.drawable.icon;
        CharSequence titleText = photoUpload.getError() == null ?
                getString(R.string.notification_upload_failed_title)
                :getString(R.string.notification_upload_failed_title_with_reason, photoUpload.getError());
        long when = System.currentTimeMillis();
        CharSequence contentMessageTitle = getString(R.string.notification_upload_failed_text,
                file.getName());

        Intent notificationIntent = new Intent(this, UploadActivity.class);
        notificationIntent.putExtra(UploadActivity.EXTRA_PENDING_UPLOAD_URI, photoUpload.getUri());
        PendingIntent contentIntent = PendingIntent.getActivity(this,
                requestCounter++, notificationIntent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                this);
        Notification notification = builder
                .setContentTitle(titleText)
                .setContentText(contentMessageTitle)
                .setWhen(when)
                .setSmallIcon(icon)
                .setAutoCancel(true)
                .setContentIntent(contentIntent)
                .build();
        // Notification notification = new Notification(icon, titleText, when);
        // notification.flags |= Notification.FLAG_AUTO_CANCEL;
        // notification.setLatestEventInfo(this, titleText, contentMessageTitle,
        // contentIntent);

        mNotificationManager.notify(file.hashCode(), notification);
    }

    private void stopErrorNotification(File file) {
        mNotificationManager.cancel(file.hashCode());
    }

    private void showSuccessNotification(PhotoUpload photoUpload, File file,
            Photo photo,
            boolean skipped)
    {
        int icon = R.drawable.icon;
        CharSequence titleText = getString(
                skipped ?
                        R.string.notification_upload_skipped_title :
                        R.string.notification_upload_success_title);
        long when = System.currentTimeMillis();
        String imageName = file.getName();
        if (!TextUtils.isEmpty(photoUpload.getMetaData().getTitle())) {
            imageName = photoUpload.getMetaData().getTitle();
        }
        CharSequence contentMessageTitle = getString(R.string.notification_upload_success_text,
                imageName);

        Intent notificationIntent;
        if (photo != null)
        {
            notificationIntent = new Intent(this, PhotoDetailsActivity.class);
            notificationIntent
                    .putExtra(PhotoDetailsActivity.EXTRA_PHOTO, photo);
        } else {
            notificationIntent = new Intent(this, MainActivity.class);
        }
        PendingIntent contentIntent = PendingIntent.getActivity(this,
                requestCounter++, notificationIntent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                this);
        Notification notification = builder
                .setContentTitle(titleText)
                .setContentText(contentMessageTitle)
                .setWhen(when)
                .setSmallIcon(icon)
                .setAutoCancel(true)
                .setContentIntent(contentIntent)
                .build();
        // Notification notification = new Notification(icon, titleText, when);
        // notification.flags |= Notification.FLAG_AUTO_CANCEL;
        // notification.setLatestEventInfo(this, titleText, contentMessageTitle,
        // contentIntent);

        mNotificationManager.notify(file.hashCode(), notification);
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

    private void setUpMediaWatcher() {
        mediaMountedReceiver = new MediaMountedReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        filter.addDataScheme("file");
        registerReceiver(mediaMountedReceiver, filter);
    }

    private synchronized void startFileObserver() {
        if (sNewPhotoObservers == null)
        {
            sNewPhotoObservers = new ArrayList<NewPhotoObserver>();
            alreadyObservingPaths = new HashSet<String>();
        }
        Set<String> externalMounts = Utils.getExternalMounts();
        File externalStorage = Environment.getExternalStorageDirectory();
        if (externalStorage != null)
        {
            externalMounts.add(externalStorage.getAbsolutePath());
        }
        for (String path : externalMounts)
        {
            File dcim = new File(path, "DCIM");
            if (dcim.isDirectory()) {
                String[] dirNames = dcim.list();
                if (dirNames != null)
                {
                    for (String dir : dirNames) {
                        if (!dir.startsWith(".")) {
                            dir = dcim.getAbsolutePath() + "/" + dir;
                            if (alreadyObservingPaths.contains(dir))
                            {
                                CommonUtils.debug(TAG, "Directory " + dir
                                        + " is already observing, skipping");
                                continue;
                            }
                            alreadyObservingPaths.add(dir);
                            NewPhotoObserver observer = new NewPhotoObserver(this, dir);
                            sNewPhotoObservers.add(observer);
                            observer.startWatching();
                            CommonUtils.debug(TAG, "Started watching " + dir);
                        }
                    }
                }
            } else
            {
                CommonUtils.debug(TAG, "Can't find camera storage folder in " + path);
            }
        }
    }

    private class ConnectivityChangeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            boolean online = Utils.isOnline(context);
            boolean wifiOnlyUpload = Preferences
                    .isWiFiOnlyUploadActive(getBaseContext());
            CommonUtils.debug(TAG, "Connectivity changed to " + (online ? "online" : "offline"));
            if (online
                    && (!wifiOnlyUpload || (wifiOnlyUpload && Utils
                            .isWiFiActive(context))))
            {
                context.startService(new Intent(context, UploaderService.class));
            }
        }
    }

    private class MediaMountedReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            CommonUtils.debug(TAG, "Received media mounted intent");
            startFileObserver();
        }
    }
}
