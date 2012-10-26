
package me.openphoto.android.app.service;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import me.openphoto.android.app.FacebookFragment;
import me.openphoto.android.app.MainActivity;
import me.openphoto.android.app.PhotoDetailsActivity;
import me.openphoto.android.app.Preferences;
import me.openphoto.android.app.R;
import me.openphoto.android.app.TwitterFragment;
import me.openphoto.android.app.UploadActivity;
import me.openphoto.android.app.facebook.FacebookProvider;
import me.openphoto.android.app.model.Photo;
import me.openphoto.android.app.net.HttpEntityWithProgress.ProgressListener;
import me.openphoto.android.app.net.IOpenPhotoApi;
import me.openphoto.android.app.net.PhotosResponse;
import me.openphoto.android.app.net.UploadResponse;
import me.openphoto.android.app.provider.PhotoUpload;
import me.openphoto.android.app.provider.UploadsProviderAccessor;
import me.openphoto.android.app.twitter.TwitterProvider;
import me.openphoto.android.app.util.CommonUtils;
import me.openphoto.android.app.util.GuiUtils;
import me.openphoto.android.app.util.ImageUtils;
import me.openphoto.android.app.util.SHA1Utils;
import me.openphoto.android.app.util.Utils;
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

public class UploaderService extends Service {
    private static final int NOTIFICATION_UPLOAD_PROGRESS = 1;
    private static final String TAG = UploaderService.class.getSimpleName();
    private IOpenPhotoApi mApi;

    private static ArrayList<NewPhotoObserver> sNewPhotoObservers;
    private static ConnectivityChangeReceiver sReceiver;

    private volatile Looper mServiceLooper;
	private volatile ServiceHandler mServiceHandler;

    private NotificationManager mNotificationManager;
    private long mNotificationLastUpdateTime;
	/**
	 * According to this http://stackoverflow.com/a/7370448/527759
	 * need so send different request codes each time we put some extra data
	 * into
	 * intent, or it will not be recreated
	 */
	int requestCounter = 0;

	/**
	 * Now it is static and uses weak reference
	 * http://stackoverflow.com/a/11408340/527759
	 * 
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
        CommonUtils.debug(TAG, "Service created");
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(sReceiver);
        for (NewPhotoObserver observer : sNewPhotoObservers) {
            observer.stopWatching();
        }
        CommonUtils.debug(TAG, "Service destroyed");
        super.onDestroy();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        msg.obj = intent;
        mServiceHandler.sendMessage(msg);
    }

    private void handleIntent(Intent intent) {
        if (!Utils.isOnline(getBaseContext()))
        {
            return;
        }
        boolean wifiOnlyUpload = Preferences
                .isWiFiOnlyUploadActive(getBaseContext());
        UploadsProviderAccessor uploads = new UploadsProviderAccessor(this);
        List<PhotoUpload> pendingUploads = uploads.getPendingUploads();
        for (PhotoUpload photoUpload : pendingUploads) {
            Log.i(TAG, "Starting upload to OpenPhoto: " + photoUpload.getPhotoUri());
            String filePath = ImageUtils.getRealPathFromURI(this, photoUpload.getPhotoUri());
            if (filePath == null || !(new File(filePath).exists())) {
                uploads.delete(photoUpload.getId());
                // TODO: Maybe set error, and set as "do not try again"
                Log.i(TAG, "Upload canceled, because file does not exist anymore.");
                continue;
            }
            if (wifiOnlyUpload && !Utils.isWiFiActive(getBaseContext()))
            {
                Log.i(TAG, "Upload canceled because WiFi is not active anymore");
                break;
            }
			File file = new File(filePath);
			stopErrorNotification(file);
            try {
				String hash = SHA1Utils.computeSha1ForFile(filePath);
				PhotosResponse photos = mApi.getPhotos(hash);
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
					final Notification notification = showUploadNotification(file);
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
										updateUploadNotification(notification,
												mLastProgress, 100);
									}
								}
							});
					Log.i(TAG, "Upload to OpenPhoto completed for: "
							+ photoUpload.getPhotoUri());
					photo = uploadResponse.getPhoto();
				}
				uploads.setUploaded(photoUpload.getId());
				if (!photoUpload.isAutoUpload())
				{
					showSuccessNotification(photoUpload, file,
							photo, skipped);
				}
				shareIfRequested(photoUpload, photo, true);
                if (!skipped)
                {
                    UploaderServiceUtils.sendPhotoUploadedBroadcast();
                }
            } catch (Exception e) {
                if (!photoUpload.isAutoUpload()) {
                    uploads.setError(photoUpload.getId(),
                            e.getClass().getSimpleName() + ": " + e.getMessage());
                    showErrorNotification(photoUpload, file);
                }
				GuiUtils.processError(TAG,
						R.string.errorCouldNotUploadTakenPhoto,
						e,
						getApplicationContext(), !photoUpload.isAutoUpload());
            }

            stopUploadNotification();
        }
    }

	public void shareIfRequested(PhotoUpload photoUpload,
			Photo photo, boolean silent)
	{
		if (photo != null)
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
				FacebookFragment.sharePhoto(null, photo,
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
				TwitterFragment.sendTweet(String.format(
						getString(R.string.share_twitter_default_msg),
						photo.getUrl(Photo.PATH_ORIGINAL)), twitter);
			}
		} catch (Exception ex)
		{
			GuiUtils.processError(TAG, R.string.errorCouldNotSendTweet, ex,
					getApplicationContext(),
					!silent);
		}
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

    private void stopUploadNotification() {
        mNotificationManager.cancel(NOTIFICATION_UPLOAD_PROGRESS);
    }

    private void showErrorNotification(PhotoUpload photoUpload, File file) {
        int icon = R.drawable.icon;
        CharSequence titleText = getString(R.string.notification_upload_failed_title);
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

    private void startFileObserver() {
        sNewPhotoObservers = new ArrayList<NewPhotoObserver>();
        File dcim = new File(Environment.getExternalStorageDirectory(), "DCIM");
        if (dcim.isDirectory()) {
            for (String dir : dcim.list()) {
                if (!dir.startsWith(".")) {
                    dir = dcim.getAbsolutePath() + "/" + dir;
                    NewPhotoObserver observer = new NewPhotoObserver(this, dir);
                    sNewPhotoObservers.add(observer);
                    observer.startWatching();
                    CommonUtils.debug(TAG, "Started watching " + dir);
                }
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
}
