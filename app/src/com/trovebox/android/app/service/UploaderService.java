
package com.trovebox.android.app.service;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import twitter4j.Twitter;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Environment;

import com.facebook.android.Facebook;
import com.trovebox.android.app.FacebookFragment;
import com.trovebox.android.app.PhotoDetailsActivity;
import com.trovebox.android.app.Preferences;
import com.trovebox.android.app.R;
import com.trovebox.android.app.TwitterFragment;
import com.trovebox.android.app.UploadManagerActivity;
import com.trovebox.android.app.facebook.FacebookProvider;
import com.trovebox.android.app.net.account.AccountLimitUtils2;
import com.trovebox.android.app.twitter.TwitterProvider;
import com.trovebox.android.common.fragment.photo_details.PhotoDetailsFragment;
import com.trovebox.android.common.model.Photo;
import com.trovebox.android.common.model.utils.PhotoUtils;
import com.trovebox.android.common.net.ReturnSizes;
import com.trovebox.android.common.provider.PhotoUpload;
import com.trovebox.android.common.service.AbstractUploaderService;
import com.trovebox.android.common.util.CommonUtils;
import com.trovebox.android.common.util.GuiUtils;

public class UploaderService extends AbstractUploaderService {

    private static ArrayList<NewPhotoObserver> sNewPhotoObservers;
    private static MediaMountedReceiver mediaMountedReceiver;
    private static Set<String> alreadyObservingPaths;

    public UploaderService() {
        setCheckPhotoExistingOnServer(true);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        startFileObserver();
        setUpMediaWatcher();
        CommonUtils.debug(TAG, "Service created overridden");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mediaMountedReceiver);
        for (NewPhotoObserver observer : sNewPhotoObservers) {
            observer.stopWatching();
        }
        CommonUtils.debug(TAG, "Service destroyed overridden");
    }

    @Override
    protected void adjustRemainingUploadingLimit(int shift) {
        super.adjustRemainingUploadingLimit(shift);
        Preferences.adjustRemainingUploadingLimit(-1);
    }

    @Override
    protected void runAfterSuccessfullUploads() {
        super.runAfterSuccessfullUploads();
        AccountLimitUtils2.updateLimitInformationCacheIfNecessary(true);
    }

    @Override
    protected void shareOnFacebook(Photo photo, boolean silent) {
        try {
            Facebook facebook = FacebookProvider.getFacebook();
            if (facebook.isSessionValid()) {
                ReturnSizes thumbSize = FacebookFragment.thumbSize;
                photo = PhotoUtils.validateUrlForSizeExistAndReturn(photo, thumbSize);
                photo = PhotoUtils.validateShareTokenExistsAndReturn(photo);
                FacebookFragment.sharePhoto(null, photo, thumbSize);
            }
        } catch (Exception ex) {
            GuiUtils.processError(TAG, R.string.errorCouldNotSendFacebookPhoto, ex,
                    getApplicationContext(), !silent);
        }
    }

    @Override
    protected void shareOnTwitter(Photo photo, boolean silent) {
        try {
            Twitter twitter = TwitterProvider.getTwitter(getApplicationContext());
            if (twitter != null) {
                photo = PhotoUtils.validateShareTokenExistsAndReturn(photo);
                String message = TwitterFragment.getDefaultTweetMessage(photo, true);
                TwitterFragment.sendTweet(message, twitter);
            }
        } catch (Exception ex) {
            GuiUtils.processError(TAG, R.string.errorCouldNotSendTweet, ex,
                    getApplicationContext(), !silent);
        }
    }

    @Override
    protected int getNotificationIcon() {
        return R.drawable.icon;
    }

    @Override
    protected PendingIntent getStandardPendingIntent() {
        Intent notificationIntent = new Intent(this, UploadManagerActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        return contentIntent;
    }

    @Override
    protected PendingIntent getErrorPendingIntent(ArrayList<PhotoUpload> photoUploads) {
        Intent notificationIntent = new Intent(this, UploadManagerActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        return contentIntent;
    }

    @Override
    protected PendingIntent getSuccessPendingIntent(ArrayList<Photo> photos) {
        Intent notificationIntent;
        notificationIntent = new Intent(this, PhotoDetailsActivity.class);
        notificationIntent.putParcelableArrayListExtra(PhotoDetailsFragment.EXTRA_PHOTOS, photos);
        PendingIntent contentIntent = PendingIntent.getActivity(this, requestCounter++,
                notificationIntent, 0);
        return contentIntent;
    }

    private void setUpMediaWatcher() {
        mediaMountedReceiver = new MediaMountedReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        filter.addDataScheme("file");
        registerReceiver(mediaMountedReceiver, filter);
    }

    private synchronized void startFileObserver() {
        if (sNewPhotoObservers == null) {
            sNewPhotoObservers = new ArrayList<NewPhotoObserver>();
            alreadyObservingPaths = new HashSet<String>();
        }
        Set<String> externalMounts = CommonUtils.getExternalMounts();
        File externalStorage = Environment.getExternalStorageDirectory();
        if (externalStorage != null) {
            externalMounts.add(externalStorage.getAbsolutePath());
        }
        for (String path : externalMounts) {
            File dcim = new File(path, "DCIM");
            if (dcim.isDirectory()) {
                String[] dirNames = dcim.list();
                if (dirNames != null) {
                    for (String dir : dirNames) {
                        if (!dir.startsWith(".")) {
                            dir = dcim.getAbsolutePath() + "/" + dir;
                            if (alreadyObservingPaths.contains(dir)) {
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
            } else {
                CommonUtils.debug(TAG, "Can't find camera storage folder in " + path);
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
