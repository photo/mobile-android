
package me.openphoto.android.app.feather;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import me.openphoto.android.app.OpenPhotoApplication;
import me.openphoto.android.app.util.CommonUtils;
import me.openphoto.android.app.util.GuiUtils;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.v4.app.FragmentManager;
import android.util.DisplayMetrics;
import android.widget.ImageView;

import com.WazaBe.HoloEverywhere.app.Fragment;
import com.WazaBe.HoloEverywhere.app.ProgressDialog;
import com.aviary.android.feather.Constants;
import com.aviary.android.feather.FeatherActivity;
import com.aviary.android.feather.library.media.ExifInterfaceWrapper;
import com.aviary.android.feather.library.moa.MoaHD;
import com.aviary.android.feather.library.moa.MoaHD.Error;
import com.aviary.android.feather.library.providers.FeatherContentProvider;
import com.aviary.android.feather.library.providers.FeatherContentProvider.ActionsDbColumns.Action;
import com.aviary.android.feather.library.utils.DecodeUtils;
import com.aviary.android.feather.library.utils.IOUtils;
import com.aviary.android.feather.library.utils.ImageLoader.ImageSizes;
import com.aviary.android.feather.library.utils.StringUtils;
import com.aviary.android.feather.library.utils.SystemUtils;
import com.facebook.android.R;

/**
 * The feather editor support fragment. It is used as external util to open
 * feather editor.
 * 
 * @author Eugene Popovich
 */
public class FeatherFragment extends Fragment {
    private static final String TAG = FeatherFragment.class.getSimpleName();

    String mOutputFilePath;
    /** session id for the hi-res post processing */
    private String mSessionId;

    private HDAsyncTask hdAsyncTask;
    private DownloadAsync downloadAsyncTask;

    FeatherFragmentParameters parameters;

    /**
     * Empty constructor as per the Fragment documentation
     */
    public FeatherFragment() {
    }

    /**
     * Locate an existing instance of this Fragment or if not found, create and
     * add it using FragmentManager.
     * 
     * @param fm The FragmentManager manager to use.
     * @return The existing instance of the Fragment or the new instance if just
     *         created.
     */
    public static FeatherFragment findOrCreateFeatherFragment(
            FragmentManager fm,
            FeatherFragmentParameters parameters) {
        // Check to see if we have retained the worker fragment.
        FeatherFragment mRetainFragment = (FeatherFragment) fm.findFragmentByTag(TAG);

        // If not retained (or first time running), we need to create and add
        // it.
        if (mRetainFragment == null) {
            mRetainFragment = new FeatherFragment();
            fm.beginTransaction().add(mRetainFragment, TAG).commit();
        }
        mRetainFragment.parameters = parameters;
        return mRetainFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Make sure this Fragment is retained over a configuration change
        setRetainInstance(true);
    }

    /**
     * Should be called remotely in the onActivityResult method for the request
     * code specified in the startFeather method
     * 
     * @param data the intent data provided to the onActivityResult method as
     *            parameter
     */
    public void onFeatherActivitySuccessResult(Intent data) {
        // send a notification to the media scanner
        updateMedia(mOutputFilePath);

        // update the preview with the result
        loadAsync(data.getData());
        onSaveCompleted(mOutputFilePath);
        mOutputFilePath = null;
    }

    /**
     * lo-res process completed, ask the user if wants to process also the
     * hi-res image
     * 
     * @param filepath lo-res file name ( in case we want to delete it )
     */
    private void onSaveCompleted(final String filepath) {

        if (mSessionId != null) {

            processHD(mSessionId, filepath);
            mSessionId = null;
        }
    }

    /**
     * Delete the session and all it's actions. We do not need it anymore.<br />
     * Note that this is optional. All old sessions are automatically removed in
     * Feather.
     * 
     * @param session_id
     */
    private void deleteSession(final String session_id) {
        Uri uri = FeatherContentProvider.SessionsDbColumns.getContentUri(session_id);
        getContentResolver().delete(uri, null, null);
    }

    /**
     * We need to notify the MediaScanner when a new file is created. In this
     * way all the gallery applications will be notified too.
     * 
     * @param file
     */
    @SuppressLint("NewApi")
    private void updateMedia(String filepath) {
        CommonUtils.debug(TAG, "updateMedia: " + filepath);
        MediaScannerConnection.scanFile(OpenPhotoApplication.getContext(),
                new String[] {
                    filepath
                }, null, null);
    }

    /**
     * Remove the media entry for the specified filepath
     * 
     * @param filePath
     */
    private void removeMedia(String filePath)
    {
        CommonUtils.debug(TAG, "Remove media request for file: " + filePath);
        String[] projection = {
                MediaStore.Images.Media._ID
        };
        Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection, MediaStore.Images.Media.DATA + " = ?", new String[] {
                    filePath
                }, null);
        if (cursor != null)
        {
            try
            {
                while (cursor.moveToNext())
                {
                    long mediaId = cursor.getLong(0);
                    CommonUtils.debug(TAG, "Found media id " + mediaId);
                    Uri uriToRemove = ContentUris.withAppendedId(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, mediaId);
                    CommonUtils.debug(TAG, "Removing content on uri " + uriToRemove);
                    getContentResolver().delete(uriToRemove, null, null);
                }
            } finally
            {
                cursor.close();
            }
        }
    }

    /**
     * Start the hi-res image processing.
     * 
     * @param session_name
     * @param lowResPath the low res path which will be removed once hi res task
     *            complete
     */
    private void processHD(final String session_name, String lowResPath) {

        try
        {
            CommonUtils.debug(TAG, "processHD: " + session_name);

            // get a new file for the hi-res file
            File destination = getNextFileName("upload_edited_hires_");

            destination.createNewFile();

            // Now we need to fetch the session information from the
            // content
            // provider
            FeatherContentProvider.SessionsDbColumns.Session session = null;

            Uri sessionUri = FeatherContentProvider.SessionsDbColumns
                    .getContentUri(session_name);

            // this query will return a cursor with the informations
            // about
            // the given session
            Cursor cursor = getContentResolver()
                    .query(sessionUri, null,
                            null, null, null);

            if (null != cursor) {
                session = FeatherContentProvider.SessionsDbColumns.Session.Create(cursor);
                cursor.close();
            }

            if (null != session) {
                // Print out the session informations
                CommonUtils.debug(TAG, "session.id: " + session.id); // session
                                                                     // _id
                CommonUtils.debug(TAG, "session.name: " + session.session); // session
                // name
                CommonUtils.debug(TAG, "session.ctime: " + session.ctime); // creation
                // time
                CommonUtils.debug(TAG, "session.file_name: " + session.file_name); // original
                // file, it is the same you passed in the startActivityForResult
                // Intent

                // Now, based on the session information we need to
                // retrieve
                // the list of actions to apply to the hi-res image
                Uri actionsUri = FeatherContentProvider.ActionsDbColumns
                        .getContentUri(session.session);

                // this query will return the list of actions performed
                // on
                // the original file, during the FeatherActivity
                // session.
                // Now you can apply each action to the hi-res image to
                // replicate the same result on the bigger image
                cursor = getContentResolver()
                        .query(actionsUri, null, null,
                                null, null);

                if (null != cursor) {
                    // If the cursor is valid we will start a new
                    // asynctask
                    // process to query the cursor
                    // and apply all the actions in a queue
                    HDAsyncTask task = new HDAsyncTask(Uri.parse(session.file_name),
                            destination.getAbsolutePath(), session_name, lowResPath);
                    task.execute(cursor);
                } else {
                    throw new IllegalStateException(
                            OpenPhotoApplication.getContext().getString(
                                    R.string.feather_failed_to_retrieve_the_list_of_acitions));
                }
            } else {
                throw new IllegalStateException(
                        OpenPhotoApplication.getContext().getString(
                                R.string.feather_failed_to_retrieve_the_session_information));
            }

        } catch (Exception ex)
        {
            GuiUtils.error(TAG, ex);
        }
    }

    /**
     * Once you've chosen an image you can start the feather activity
     * 
     * @param file
     * @param requestCode
     */
    public void startFeather(File file, int requestCode) {

        try
        {
            Uri uri = Uri.fromFile(file);
            CommonUtils.debug(TAG, "Start feather for uri: " + uri);

            // first check the external storage availability
            if (!CommonUtils.isExternalStorageAvilable()) {
                GuiUtils.alert(R.string.external_storage_na_message);
                return;
            }
            if (!CommonUtils.isFroyoOrHigher())
            {
                GuiUtils.alert(R.string.action_not_supported_on_os_version);
                return;
            }

            // create a temporary file where to store the resulting
            // image
            file = getNextFileName("upload_edited_");
            mOutputFilePath = file.getAbsolutePath();
            // Create the intent needed to start feather
            Intent newIntent = new Intent(getCallingFragment().getActivity(), FeatherActivity.class);

            // set the source image uri
            newIntent.setData(uri);

            String API_KEY = OpenPhotoApplication.getContext().getString(R.string.feather_api_key);
            // pass the required api_key and secret ( see
            // http://developers.aviary.com/ )
            newIntent.putExtra("API_KEY", API_KEY);

            // pass the uri of the destination image file (optional)
            // This will be the same uri you will receive in the
            // onActivityResult
            newIntent.putExtra("output", Uri.fromFile(file));

            // format of the destination image (optional)
            newIntent
                    .putExtra(Constants.EXTRA_OUTPUT_FORMAT,
                            Bitmap.CompressFormat.JPEG.name());

            // output format quality (optional)
            newIntent.putExtra(Constants.EXTRA_OUTPUT_QUALITY, 90);

            // If you want to disable the external effects
            // newIntent.putExtra(
            // Constants.EXTRA_EFFECTS_ENABLE_EXTERNAL_PACKS, false );

            // If you want to disable the external effects
            // newIntent.putExtra(
            // Constants.EXTRA_STICKERS_ENABLE_EXTERNAL_PACKS, false );

            // enable fast rendering preview
            newIntent.putExtra(Constants.EXTRA_EFFECTS_ENABLE_FAST_PREVIEW,
                    true);

            // you can force feather to display only a certain ( see
            // FilterLoaderFactory#Filters )
            // you can omit this if you just wanto to display the
            // default
            // tools

            /*
             * newIntent.putExtra( "tools-list", new String[] {
             * FilterLoaderFactory.Filters.ENHANCE.name(),
             * FilterLoaderFactory.Filters.EFFECTS.name(),
             * FilterLoaderFactory.Filters.STICKERS.name(),
             * FilterLoaderFactory.Filters.ADJUST.name(),
             * FilterLoaderFactory.Filters.CROP.name(),
             * FilterLoaderFactory.Filters.BRIGHTNESS.name(),
             * FilterLoaderFactory.Filters.CONTRAST.name(),
             * FilterLoaderFactory.Filters.SATURATION.name(),
             * FilterLoaderFactory.Filters.SHARPNESS.name(),
             * FilterLoaderFactory.Filters.DRAWING.name(),
             * FilterLoaderFactory.Filters.TEXT.name(),
             * FilterLoaderFactory.Filters.MEME.name(),
             * FilterLoaderFactory.Filters.RED_EYE.name(),
             * FilterLoaderFactory.Filters.WHITEN.name(),
             * FilterLoaderFactory.Filters.BLEMISH.name(),
             * FilterLoaderFactory.Filters.COLORTEMP.name(), } );
             */

            // you want the result bitmap inline. (optional)
            // newIntent.putExtra( Constants.EXTRA_RETURN_DATA, true );

            // you want to hide the exit alert dialog shown when back is
            // pressed
            // without saving image first
            // newIntent.putExtra(
            // Constants.EXTRA_HIDE_EXIT_UNSAVE_CONFIRMATION, true );

            // -- VIBRATION --
            // Some aviary tools use the device vibration in order to
            // give a
            // better experience
            // to the final user. But if you want to disable this
            // feature,
            // just
            // pass
            // any value with the key "tools-vibration-disabled" in the
            // calling
            // intent.
            // This option has been added to version 2.1.5 of the Aviary
            // SDK
            newIntent.putExtra(Constants.EXTRA_TOOLS_DISABLE_VIBRATION, true);

            final DisplayMetrics metrics = new DisplayMetrics();
            getCallingFragment().getActivity().getWindowManager().getDefaultDisplay()
                    .getMetrics(metrics);
            int max_size = Math.min(metrics.widthPixels, metrics.heightPixels);

            // you can pass the maximum allowed image size, otherwise
            // feather
            // will determine
            // the max size based on the device memory
            // Here we're passing the current display size as max image
            // size
            // because after
            // the execution of Aviary we're saving the HI-RES image so
            // we
            // don't
            // need a big
            // image for the preview
            max_size = (int) ((double) max_size / 0.8);
            CommonUtils.debug(TAG, "max-image-size: " + max_size);
            newIntent.putExtra("max-image-size", max_size);

            // Enable/disable the default borders for the effects
            newIntent.putExtra("effect-enable-borders", true);

            // You need to generate a new session id key to pass to
            // Aviary
            // feather
            // this is the key used to operate with the hi-res image (
            // and
            // must
            // be unique for every new instance of Feather )
            // The session-id key must be 64 char length

            mSessionId = StringUtils.getSha256(System.currentTimeMillis() + API_KEY);
            CommonUtils.debug(TAG,
                    "session: " + mSessionId + ", size: " + mSessionId.length());
            newIntent.putExtra("output-hires-session-id", mSessionId);

            // ..and start feather
            getCallingFragment().startActivityForResult(newIntent, requestCode);
        } catch (Exception ex)
        {
            GuiUtils.error(TAG, ex);
        }
    }

    File getNextFileName(String prefix) throws IOException
    {
        return parameters.getNextFileName(prefix);
    }

    /**
     * Load the incoming Image
     * 
     * @param uri
     */
    private void loadAsync(final Uri uri) {
        CommonUtils.debug(TAG, "loadAsync: " + uri);

        ImageView imageView = parameters.getImageView();
        if (imageView != null)
        {
            Drawable toRecycle = imageView.getDrawable();
            if (toRecycle != null && toRecycle instanceof BitmapDrawable) {
                if (((BitmapDrawable) imageView.getDrawable()).getBitmap() != null)
                    ((BitmapDrawable) imageView.getDrawable()).getBitmap().recycle();
            }
            imageView.setImageDrawable(null);
        }
        setImageUri(uri);

        DownloadAsync task = new DownloadAsync();
        task.execute(uri);
    }

    private void setImageUri(Uri uri) {
        parameters.setImageUri(uri);
    }

    void setHDFile(String path)
    {
        parameters.setHDFile(path);
    }

    public void setImageBitmap(Bitmap result) {
        parameters.setImageBitmap(result);
    }

    Fragment getCallingFragment()
    {
        return parameters.getCallingFragment();
    }

    ContentResolver getContentResolver()
    {
        return OpenPhotoApplication.getContext().getContentResolver();
    }

    /**
     * This should be called by the parent fragment when its view is destroyed
     */
    public void onCallingViewDestroyed()
    {
        try
        {
            if (hdAsyncTask != null)
            {
                hdAsyncTask.dismissProgress();
            }
            if (downloadAsyncTask != null)
            {
                downloadAsyncTask.dismissProgress();
            }
        } catch (Exception ex)
        {
            GuiUtils.noAlertError(TAG, ex);
        }
    }

    /**
     * This should be called by the parent fragment when its view is created
     */
    public void onCallingViewCreated()
    {
        try
        {
            if (hdAsyncTask != null && !hdAsyncTask.finished)
            {
                hdAsyncTask.showProgress();
            }
        } catch (Exception ex)
        {
            GuiUtils.noAlertError(TAG, ex);
        }
    }

    /**
     * The common parameters for the feather fragment
     */
    public static interface FeatherFragmentParameters
    {
        File getNextFileName(String prefix) throws IOException;

        void setImageUri(Uri uri);

        void setHDFile(String path);

        void setImageBitmap(Bitmap result);

        ImageView getImageView();

        Fragment getCallingFragment();
    }

    class DownloadAsync extends AsyncTask<Uri, Void, Bitmap> implements OnCancelListener {

        ProgressDialog progress;
        private Uri mUri;
        int imageWidth, imageHeight;
        boolean finished = false;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            downloadAsyncTask = this;

            showProgress();
            DisplayMetrics metrics = getResources().getDisplayMetrics();
            imageWidth = (int) ((float) metrics.widthPixels / 1.5);
            imageHeight = (int) ((float) metrics.heightPixels / 1.5);
        }

        public void showProgress() {
            progress = new ProgressDialog(getCallingFragment().getActivity());
            progress.setIndeterminate(true);
            progress.setCancelable(true);
            progress.setMessage(OpenPhotoApplication.getContext().getString(
                    R.string.feather_loading_image));
            progress.setOnCancelListener(this);
            progress.show();
        }

        @Override
        protected Bitmap doInBackground(Uri... params) {
            mUri = params[0];
            Bitmap bitmap = null;

            // while (mImageContainer.getWidth() < 1) {
            // try {
            // Thread.sleep(1);
            // } catch (InterruptedException e) {
            // e.printStackTrace();
            // }
            // }
            //
            // final int w = mImageContainer.getWidth();
            // CommonUtils.debug(TAG, "width: " + w);
            ImageSizes sizes = new ImageSizes();
            bitmap = DecodeUtils.decode(OpenPhotoApplication.getContext(), mUri, imageWidth,
                    imageHeight, sizes);
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            try
            {

                finished = true;
                dismissProgress();

                if (result != null) {
                    setImageBitmap(result);
                } else {
                    GuiUtils.alert(OpenPhotoApplication.getContext().getString(
                            R.string.feather_failed_to_load_image, mUri.toString()));
                }
            } finally
            {
                downloadAsyncTask = null;
            }
        }

        public void dismissProgress() {
            try
            {
                if (progress != null && progress.getWindow() != null && progress.isShowing()) {
                    progress.dismiss();
                }
            } catch (Exception ex)
            {
                GuiUtils.noAlertError(TAG, ex);
            }
            progress = null;
        }

        @Override
        public void onCancel(DialogInterface dialog) {
            CommonUtils.debug(TAG, "onProgressCancel");
            this.cancel(true);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            CommonUtils.debug(TAG, "onCancelled");
        }

    }

    /**
     * AsyncTask for Hi-Res image processing
     * 
     * @author alessandro
     */
    private class HDAsyncTask extends AsyncTask<Cursor, Integer, MoaHD.Error> {

        Uri uri_;
        String dstPath_;
        String lowResPath;
        ProgressDialog progress_;
        String session_;
        ExifInterfaceWrapper exif_;
        int index = -1;
        int total = -1;
        boolean finished = false;

        /**
         * Initialize the HiRes async task
         * 
         * @param source - source image file
         * @param destination - destination image file
         * @param session_id - the session id used to retrieve the list of
         *            actions
         * @param lowResPath - the low res image path which will be removed
         *            after hd saved
         */
        public HDAsyncTask(Uri source, String destination, String session_id,
                String lowResPath) {
            uri_ = source;
            dstPath_ = destination;
            session_ = session_id;
            this.lowResPath = lowResPath;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            hdAsyncTask = this;
            showProgress();
        }

        public void showProgress() {
            progress_ = new ProgressDialog(getCallingFragment().getActivity());
            progress_.setIndeterminate(true);
            progress_.setTitle(R.string.feather_processing_hi_res_image);
            progress_.setMessage(OpenPhotoApplication.getContext().getString(
                    R.string.feather_loading_image));
            progress_.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progress_.setCancelable(false);
            progress_.show();
            onProgressUpdate(index, total);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

            index = values[0];
            total = values[1];
            String message = "";

            if (index == -1)
                message = OpenPhotoApplication.getContext()
                        .getString(R.string.feather_saving_image);
            else
                message = OpenPhotoApplication.getContext().getString(
                        R.string.feather_applying_action, Integer.toString(index + 1),
                        Integer.toString(total));

            if (progress_ != null)
            {
                progress_.setMessage(message);
            }

            CommonUtils.debug(TAG, index + "/" + total + ", message: " + message);
        }

        @Override
        protected Error doInBackground(Cursor... params) {
            Cursor cursor = params[0];

            MoaHD.Error result = Error.UnknownError;

            if (null != cursor) {

                // Initialize the class to perform HD operations
                MoaHD moa = new MoaHD();

                // try to load the source image
                result = loadImage(moa);
                CommonUtils.debug(TAG, "moa.load: " + result.name());

                // if image is loaded
                if (result == Error.NoError) {

                    final int total_actions = cursor.getCount();

                    CommonUtils.debug(TAG, "total actions: " + total_actions);

                    if (cursor.moveToFirst()) {

                        // get the total number of actions in the queue
                        // we're adding also the 'load' and the 'save' action to
                        // the total count

                        // now for each action in the given cursor, apply the
                        // action to
                        // the MoaHD instance
                        do {
                            if (isCancelled())
                            {
                                return result;
                            }
                            // send a progress notification to the progressbar
                            // dialog
                            publishProgress(cursor.getPosition(), total_actions);

                            // load the action from the current cursor
                            Action action = Action.Create(cursor);
                            if (null != action) {
                                CommonUtils.debug(TAG, "executing: " + action.id + "("
                                        + action.session_id
                                        + " on " + action.ctime + ") = "
                                        + action.getActions());

                                // apply a list of actions to the current image
                                moa.applyActions(action.getActions());
                            } else {
                                CommonUtils.error(TAG,
                                        "Woa, something went wrong! Invalid action returned");
                            }

                            // move the cursor to next position
                        } while (cursor.moveToNext());
                    }
                    if (isCancelled())
                    {
                        return result;
                    }
                    // at the end of all the operations we need to save
                    // the modified image to a new file
                    publishProgress(-1, -1);
                    result = moa.save(dstPath_);
                    CommonUtils.debug(TAG, "moa.save: " + result.name());

                    if (result != Error.NoError) {
                        CommonUtils.debug(TAG, "failed to save the image to " + dstPath_);
                    }

                    // ok, now we can save the source image EXIF tags
                    // to the new image
                    if (null != exif_) {
                        saveExif(exif_, dstPath_);
                    }

                } else {
                    CommonUtils.error(TAG, "Failed to load file");
                }
                cursor.close();

                // and unload the current bitmap. Note that you *MUST* call this
                // method to free the memory allocated with the load
                // method
                result = moa.unload();
                CommonUtils.debug(TAG, "moa.unload: " + result.name());

                // finally dispose the moahd instance
                moa.dispose();
            }

            return result;
        }

        /**
         * Save the Exif tags to the new image
         * 
         * @param originalExif
         * @param filename
         */
        private void saveExif(ExifInterfaceWrapper originalExif, String filename) {
            // ok, now we can save back the EXIF tags
            // to the new file
            ExifInterfaceWrapper newExif = null;
            try {
                newExif = new ExifInterfaceWrapper(dstPath_);
            } catch (IOException e) {
                GuiUtils.noAlertError(TAG, e);
            }
            if (null != newExif && null != originalExif) {
                originalExif.copyTo(newExif);
                // this should be changed because the editor already rotate the
                // image
                newExif.setAttribute(ExifInterfaceWrapper.TAG_ORIENTATION, "0");
                // let's update the software tag too
                newExif.setAttribute(ExifInterfaceWrapper.TAG_SOFTWARE, "Aviary "
                        + FeatherActivity.SDK_VERSION);
                // ...and the modification date
                newExif.setAttribute(ExifInterfaceWrapper.TAG_DATETIME,
                        ExifInterfaceWrapper.formatDate(new Date()));
                try {
                    newExif.saveAttributes();
                } catch (IOException e) {
                    GuiUtils.noAlertError(TAG, e);
                }
            }
        }

        @Override
        protected void onPostExecute(MoaHD.Error result) {
            super.onPostExecute(result);
            try
            {

                finished = true;
                dismissProgress();

                // in case we had an error...
                if (result != Error.NoError) {
                    GuiUtils.alert(OpenPhotoApplication.getContext().getString(
                            R.string.feather_error_occurred, result.name()));
                    return;
                }

                // finally notify the MediaScanner of the new generated file
                updateMedia(dstPath_);

                // we don't need the session anymore, now we can delete it.
                CommonUtils.debug(TAG, "delete session: " + session_);
                deleteSession(session_);

                setHDFile(dstPath_);
                removeMedia(lowResPath);
            } finally
            {
                hdAsyncTask = null;
            }
        }

        public void dismissProgress() {
            try
            {
                if (progress_ != null && progress_.getWindow() != null) {
                    progress_.dismiss();
                }
            } catch (Exception ex)
            {
                GuiUtils.noAlertError(TAG, ex);
            }
            progress_ = null;
        }

        private Error loadImage(MoaHD moa) {
            MoaHD.Error result = Error.UnknownError;
            final String srcPath = IOUtils.getRealFilePath(OpenPhotoApplication.getContext(), uri_);
            if (srcPath != null) {

                // Let's try to load the EXIF tags from
                // the source image
                try {
                    exif_ = new ExifInterfaceWrapper(srcPath);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                result = moa.load(srcPath);
            } else {

                if (SystemUtils.isHoneyComb()) {
                    InputStream stream = null;
                    try {
                        stream = getContentResolver()
                                .openInputStream(uri_);
                    } catch (Exception e) {
                        result = Error.FileNotFoundError;
                        GuiUtils.noAlertError(TAG, e);
                    }
                    if (stream != null) {
                        try {
                            result = moa.load(stream);
                        } catch (Exception e) {
                            result = Error.DecodeError;
                            GuiUtils.noAlertError(TAG, e);
                        }
                    }
                } else {
                    ParcelFileDescriptor fd = null;
                    try {
                        fd = getContentResolver()
                                .openFileDescriptor(uri_, "r");
                    } catch (FileNotFoundException e) {
                        GuiUtils.noAlertError(TAG, e);
                        result = Error.FileNotFoundError;
                    }

                    if (null != fd) {
                        result = moa.load(fd.getFileDescriptor());
                    }
                }
            }
            return result;
        }
    }
}
