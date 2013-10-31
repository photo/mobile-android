
package com.trovebox.android.app.model.utils;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.trovebox.android.app.Preferences;
import com.trovebox.android.app.R;
import com.trovebox.android.app.TroveboxApplication;
import com.trovebox.android.app.model.Photo;
import com.trovebox.android.app.net.PhotoResponse;
import com.trovebox.android.app.net.ReturnSizes;
import com.trovebox.android.app.net.TokenResponse;
import com.trovebox.android.app.net.TroveboxResponse;
import com.trovebox.android.app.net.TroveboxResponseUtils;
import com.trovebox.android.app.util.CommonUtils;
import com.trovebox.android.app.util.GuiUtils;
import com.trovebox.android.app.util.ImageUtils;
import com.trovebox.android.app.util.LoadingControl;
import com.trovebox.android.app.util.RunnableWithParameter;
import com.trovebox.android.app.util.SimpleAsyncTaskEx;
import com.trovebox.android.app.util.TrackerUtils;

/**
 * Utils class for the photo object
 * 
 * @author Eugene Popovich
 */
public class PhotoUtils {
    public static final String TAG = PhotoUtils.class.getSimpleName();
    public static String PHOTO_DELETED_ACTION = "com.trovebox.PHOTO_DELETED";
    public static String PHOTO_DELETED = "PHOTO_DELETED";
    public static String PHOTO_UPDATED_ACTION = "com.trovebox.PHOTO_UPDATED";
    public static String PHOTO_UPDATED = "PHOTO_UPDATED";

    /**
     * Validate whether getUrl for the photo size is not null. Runs size
     * retrieval task if it is null
     * 
     * @param photo the photo to check
     * @param photoSize the required photo size
     * @param runnable the runnable which will run with the validated photo
     * @param loadingControl the loading control to display loading indicator
     */
    public static void validateUrlForSizeExistAsyncAndRun(
            Photo photo,
            ReturnSizes photoSize,
            RunnableWithParameter<Photo> runnable,
            LoadingControl loadingControl
            )
    {
        String size = photoSize.toString();
        if (photo.getUrl(size) != null)
        {
            CommonUtils.debug(TAG, "Url for the size " + size + " exists. Running action.");
            runnable.run(photo);
        } else
        {
            CommonUtils.debug(TAG, "Url for the size " + size
                    + " doesn't exist. Running size retrieval task.");
            new RetrieveThumbUrlTask(photo, photoSize, runnable, loadingControl).execute();
        }
    }

    /**
     * Validate whether the photo has url for the required size. If not then
     * retrieve the photo with this size
     * 
     * @param photo
     * @param photoSize
     * @return
     * @throws ClientProtocolException
     * @throws IOException
     * @throws JSONException
     */
    public static Photo validateUrlForSizeExistAndReturn(
            Photo photo,
            ReturnSizes photoSize
            ) throws ClientProtocolException, IOException, JSONException
    {
        String size = photoSize.toString();
        if (photo.getUrl(size) != null)
        {
            CommonUtils.debug(TAG, "Url for the size " + size + " exists");
        } else
        {
            CommonUtils.debug(TAG, "Url for the size " + size
                    + " doesn't exist. Running size retrieval method.");
            Photo photo2 = getThePhotoWithReturnSize(photo, photoSize);
            photo.putUrl(size, photo2.getUrl(size));
        }
        return photo;
    }

    /**
     * Validate whether getShareToken for the photo is not null. Runs share
     * token retrieval task if it is null
     * 
     * @param photo the photo to check
     * @param runnable the runnable which will run with the validated photo
     * @param runnableOnFailure the runnable which will run in case of share
     *            token retrieval failure
     * @param loadingControl the loading control to display loading indicator
     */
    public static void validateShareTokenExistsAsyncAndRunAsync(
            Photo photo,
            RunnableWithParameter<Photo> runnable,
            Runnable runnableOnFailure,
            LoadingControl loadingControl
            )
    {
        if (photo.getShareToken() != null)
        {
            CommonUtils.debug(TAG, "Share token exists. Running action.");
            runnable.run(photo);
        } else
        {
            CommonUtils.debug(TAG, "Share token doesn't exist. Running token retrieval task.");
            new RetrieveShareTokenTask(photo, runnable, runnableOnFailure, loadingControl)
                    .execute();
        }
    }

    /**
     * Validate whether the photo has retrieved share token. If not then
     * retrieve the share token
     * 
     * @param photo
     * @return
     * @throws ClientProtocolException
     * @throws IOException
     * @throws JSONException
     */
    public static Photo validateShareTokenExistsAndReturn(
            Photo photo
            ) throws ClientProtocolException, IOException, JSONException
    {
        if (photo.getShareToken() != null)
        {
            CommonUtils.debug(TAG, "Share token exists");
        } else
        {
            CommonUtils.debug(TAG, "Share token doesn't exist. Running size retrieval method.");
            TokenResponse response = TokenUtils.getPhotoShareTokenResponse(photo);
            photo.setShareToken(response.getToken());
        }
        return photo;
    }

    /**
     * Performs api request to retrieve photo with necessary size
     * 
     * @param photo
     * @param photoSize
     * @return
     * @throws ClientProtocolException
     * @throws IOException
     * @throws JSONException
     */
    public static Photo getThePhotoWithReturnSize(Photo photo, ReturnSizes photoSize)
            throws ClientProtocolException, IOException,
            JSONException {
        TrackerUtils.trackBackgroundEvent("getThePhotoWithReturnSize", TAG);
        long start = System.currentTimeMillis();
        PhotoResponse response = Preferences.getApi(TroveboxApplication.getContext())
                .getPhoto(
                        photo.getId(), photoSize);
        photo = response.getPhoto();
        TrackerUtils.trackDataLoadTiming(System.currentTimeMillis() - start,
                "getThePhotoWithReturnSize", TAG);
        return photo;
    }

    public static void deletePhoto(Photo photo,
            LoadingControl loadingControl)
    {
        new DeletePhotoTask(photo, loadingControl).execute();
    }

    public static void updatePhoto(
            Photo photo, String title,
            String description, Collection<String> tags,
            boolean isPrivate,
            LoadingControl loadingControl)
    {
        updatePhoto(photo, title, description, tags, isPrivate, null, loadingControl);
    }

    public static void updatePhoto(
            Photo photo, String title,
            String description, Collection<String> tags,
            boolean isPrivate,
            Runnable runOnSuccessAction,
            LoadingControl loadingControl)
    {
        new UpdatePhotoTask(photo, title, description, tags, isPrivate, runOnSuccessAction,
                loadingControl).execute();
    }

    /**
     * Get the photo share url
     * 
     * @param photo the photo to get the share url for
     * @return
     */
    public static String getShareUrl(Photo photo) {
        return getShareUrl(photo, true);
    }

    /**
     * Get the photo share url
     * 
     * @param photo the photo to get the share url for
     * @param appendToken whether to append share token at the end of url
     * @return
     */
    public static String getShareUrl(Photo photo, boolean appendToken) {
        String result = photo.getUrl(Photo.URL);
        if (appendToken)
        {
            result += TokenUtils.getTokenUrlSuffix(photo.getShareToken());
        }
        return result;
    }

    private static class RetrieveThumbUrlTask extends SimpleAsyncTaskEx {
        private Photo mPhoto;
        private ReturnSizes photoSize;
        private RunnableWithParameter<Photo> runnable;

        public RetrieveThumbUrlTask(Photo photo,
                ReturnSizes photoSize,
                RunnableWithParameter<Photo> runnable,
                LoadingControl loadingControl) {
            super(loadingControl);
            mPhoto = photo;
            this.photoSize = photoSize;
            this.runnable = runnable;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                Photo mPhoto2 = getThePhotoWithReturnSize(mPhoto, photoSize);
                String size = photoSize.toString();
                mPhoto.putUrl(size, mPhoto2.getUrl(size));
                return true;
            } catch (Exception e) {
                GuiUtils.error(TAG, R.string.errorCouldNotGetPhoto, e);
            }
            return false;
        }

        @Override
        protected void onSuccessPostExecute() {
            runnable.run(mPhoto);
        }

    }

    /**
     * Share token retrieval task
     */
    private static class RetrieveShareTokenTask extends SimpleAsyncTaskEx {
        private Photo photo;
        private RunnableWithParameter<Photo> runnable;
        private Runnable runnableOnFailure;

        public RetrieveShareTokenTask(Photo photo,
                RunnableWithParameter<Photo> runnable,
                Runnable runnableOnFailure,
                LoadingControl loadingControl) {
            super(loadingControl);
            this.photo = photo;
            this.runnable = runnable;
            this.runnableOnFailure = runnableOnFailure;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                TokenResponse response = TokenUtils.getPhotoShareTokenResponse(photo);
                if (TroveboxResponseUtils.checkResponseValid(response))
                {
                    photo.setShareToken(response.getToken());
                    return true;
                }
            } catch (Exception e) {
                GuiUtils.error(TAG, R.string.errorCouldNotGetShareToken, e);
            }
            return false;
        }

        @Override
        protected void onSuccessPostExecute() {
            runnable.run(photo);
        }

        @Override
        protected void onFailedPostExecute() {
            super.onFailedPostExecute();
            if (runnableOnFailure != null)
            {
                runnableOnFailure.run();
            }
        }

    }

    private static class DeletePhotoTask extends SimpleAsyncTaskEx {
        private Photo photo;

        public DeletePhotoTask(Photo photo,
                LoadingControl loadingControl) {
            super(loadingControl);
            this.photo = photo;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                if (CommonUtils.checkLoggedInAndOnline())
                {
                    TroveboxResponse response =
                            Preferences.getApi(TroveboxApplication.getContext())
                                    .deletePhoto(photo.getId());
                    return TroveboxResponseUtils.checkResponseValid(response);
                }
            } catch (Exception e) {
                GuiUtils.error(TAG, R.string.errorCouldNotDeletePhoto, e);
            }
            return false;
        }

        @Override
        protected void onSuccessPostExecute() {
            sendPhotoDeletedBroadcast(photo);
        }
    }

    private static class UpdatePhotoTask extends SimpleAsyncTaskEx {
        private Photo photo;
        String title, description;
        boolean isPrivate;
        Collection<String> tags;
        Runnable runOnSuccessAction;

        public UpdatePhotoTask(
                Photo photo, String title,
                String description, Collection<String> tags,
                boolean isPrivate,
                Runnable runOnSuccessAction,
                LoadingControl loadingControl) {
            super(loadingControl);
            this.photo = photo;
            this.title = title;
            this.description = description;
            this.isPrivate = isPrivate;
            this.tags = tags;
            this.runOnSuccessAction = runOnSuccessAction;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                if (CommonUtils.checkLoggedInAndOnline())
                {
                    PhotoResponse response =
                            Preferences.getApi(TroveboxApplication.getContext())
                                    .updatePhotoDetails(
                                            photo.getId(),
                                            title,
                                            description,
                                            tags,
                                            isPrivate ? Photo.PERMISSION_PRIVATE
                                                    : Photo.PERMISSION_PUBLIC);
                    photo = response.getPhoto();
                    return TroveboxResponseUtils.checkResponseValid(response);
                }
            } catch (Exception e) {
                GuiUtils.error(TAG, R.string.errorCouldNotUpdatePhoto, e);
            }
            return false;
        }

        @Override
        protected void onSuccessPostExecute() {
            sendPhotoUpdatedBroadcast(photo);
            if (runOnSuccessAction != null)
            {
                runOnSuccessAction.run();
            }
        }
    }

    /**
     * Get and register the broadcast receiver for the photo removed event
     * 
     * @param TAG
     * @param handler
     * @param activity
     * @return
     */
    public static BroadcastReceiver getAndRegisterOnPhotoDeletedActionBroadcastReceiver(
            final String TAG,
            final PhotoDeletedHandler handler,
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
                            "Received photo deleted broadcast message");
                    Photo photo = intent.getParcelableExtra(PHOTO_DELETED);
                    handler.photoDeleted(photo);
                } catch (Exception ex)
                {
                    GuiUtils.error(TAG, ex);
                }
            }
        };
        activity.registerReceiver(br, new IntentFilter(PHOTO_DELETED_ACTION));
        return br;
    }

    /**
     * Send the photo deleted broadcast
     * 
     * @param photo
     */
    public static void sendPhotoDeletedBroadcast(Photo photo)
    {
        Intent intent = new Intent(PHOTO_DELETED_ACTION);
        intent.putExtra(PHOTO_DELETED, photo);
        TroveboxApplication.getContext().sendBroadcast(intent);
    }

    public static interface PhotoDeletedHandler
    {
        void photoDeleted(Photo photo);
    }

    /**
     * Get and register the broadcast receiver for the photo updated event
     * 
     * @param TAG
     * @param handler
     * @param activity
     * @return
     */
    public static BroadcastReceiver getAndRegisterOnPhotoUpdatedActionBroadcastReceiver(
            final String TAG,
            final PhotoUpdatedHandler handler,
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
                            "Received photo updated broadcast message");
                    Photo photo = intent.getParcelableExtra(PHOTO_UPDATED);
                    handler.photoUpdated(photo);
                } catch (Exception ex)
                {
                    GuiUtils.error(TAG, ex);
                }
            }
        };
        activity.registerReceiver(br, new IntentFilter(PHOTO_UPDATED_ACTION));
        return br;
    }

    /**
     * Send the photo updated broadcast
     * 
     * @param photo
     */
    public static void sendPhotoUpdatedBroadcast(Photo photo)
    {
        Intent intent = new Intent(PHOTO_UPDATED_ACTION);
        intent.putExtra(PHOTO_UPDATED, photo);
        TroveboxApplication.getContext().sendBroadcast(intent);
    }

    public static interface PhotoUpdatedHandler
    {
        void photoUpdated(Photo photo);
    }

    /**
     * Generate photo title based on its date of creation
     * 
     * @param filePath
     * @return
     * @throws IOException
     */
    public static String generatePhotoTitle(String filePath) throws IOException
    {
        long createdDate = ImageUtils.getExifDateTime(filePath);
        if (createdDate == -1)
        {
            CommonUtils.debug(TAG, "generatePhotoTitle: createdDate from exif is missing");
            createdDate = (new File(filePath)).lastModified();
        }
        return CommonUtils.formatDateTime(new Date(createdDate));
    }
}
