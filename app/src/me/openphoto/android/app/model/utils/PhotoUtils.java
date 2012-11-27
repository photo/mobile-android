package me.openphoto.android.app.model.utils;

import java.io.IOException;

import me.openphoto.android.app.OpenPhotoApplication;
import me.openphoto.android.app.Preferences;
import me.openphoto.android.app.model.Photo;
import me.openphoto.android.app.net.OpenPhotoResponse;
import me.openphoto.android.app.net.PhotoResponse;
import me.openphoto.android.app.net.ReturnSizes;
import me.openphoto.android.app.util.CommonUtils;
import me.openphoto.android.app.util.GuiUtils;
import me.openphoto.android.app.util.LoadingControl;
import me.openphoto.android.app.util.RunnableWithParameter;
import me.openphoto.android.app.util.SimpleAsyncTaskEx;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;

import com.facebook.android.R;

/**
 * Utils class for the photo object
 * 
 * @author Eugene Popovich
 */
public class PhotoUtils {
    public static final String TAG = PhotoUtils.class.getSimpleName();

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
            CommonUtils.debug(TAG, "Url for the size " + size + " exists. Running action.");
        } else
        {
            CommonUtils.debug(TAG, "Url for the size " + size
                    + " doesn't exist. Running size retrieval method.");
            photo = getThePhotoWithReturnSize(photo, photoSize);
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
        PhotoResponse response = Preferences.getApi(OpenPhotoApplication.getContext())
                .getPhoto(
                        photo.getId(), photoSize);
        photo = response.getPhoto();
        return photo;
    }

    public static void deletePhoto(Photo photo,
            RunnableWithParameter<Boolean> runnable,
            LoadingControl loadingControl)
    {
        new DeletePhotoTask(photo, runnable, loadingControl).execute();
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

    private static class DeletePhotoTask extends SimpleAsyncTaskEx {
        private Photo photo;
        private RunnableWithParameter<Boolean> runnable;

        public DeletePhotoTask(Photo photo,
                RunnableWithParameter<Boolean> runnable,
                LoadingControl loadingControl) {
            super(loadingControl);
            this.photo = photo;
            this.runnable = runnable;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                OpenPhotoResponse response =
                        Preferences.getApi(OpenPhotoApplication.getContext())
                                .deletePhoto(photo.getId());
                return response.isSuccess();
            } catch (Exception e) {
                GuiUtils.error(TAG, R.string.errorCouldNotDeletePhoto, e);
            }
            return false;
        }

        @Override
        protected void onSuccessPostExecute() {
            runnable.run(true);
        }

        @Override
        protected void onFailedPostExecute() {
            runnable.run(false);
        }
    }
}
