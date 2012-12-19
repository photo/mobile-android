
package me.openphoto.android.app;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Date;

import me.openphoto.android.app.bitmapfun.util.ImageResizer;
import me.openphoto.android.app.common.CommonActivity;
import me.openphoto.android.app.common.CommonClosableOnRestoreDialogFragment;
import me.openphoto.android.app.common.CommonFragment;
import me.openphoto.android.app.feather.FeatherFragment;
import me.openphoto.android.app.net.UploadMetaData;
import me.openphoto.android.app.provider.PhotoUpload;
import me.openphoto.android.app.provider.UploadsProviderAccessor;
import me.openphoto.android.app.service.UploaderService;
import me.openphoto.android.app.util.CommonUtils;
import me.openphoto.android.app.util.FileUtils;
import me.openphoto.android.app.util.GuiUtils;
import me.openphoto.android.app.util.ImageUtils;
import me.openphoto.android.app.util.TrackerUtils;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.AlertDialog;
import org.holoeverywhere.app.Dialog;
import org.holoeverywhere.app.Fragment;
import org.holoeverywhere.widget.Switch;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

/**
 * This activity handles uploading pictures to OpenPhoto.
 * 
 * @author Patrick Boos
 */
public class UploadActivity extends CommonActivity {
    public static final String TAG = UploadActivity.class.getSimpleName();

    public static final String EXTRA_PENDING_UPLOAD_URI = "pending_upload_uri";

    private static final int REQUEST_GALLERY = 0;
    private static final int REQUEST_CAMERA = 1;
    private static final int REQUEST_TAGS = 2;
    private static final int ACTION_REQUEST_FEATHER = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null)
        {
            getSupportFragmentManager().beginTransaction()
                    .replace(android.R.id.content, new UploadUiFragment())
                    .commit();
        }
    }

    public static class UploadUiFragment extends CommonFragment
            implements OnClickListener
    {
        static final String UPLOAD_IMAGE_FILE = "UploadActivityFile";
        static final String UPLOAD_IMAGE_FILE_URI = "UploadActivityFileUri";

        private File mUploadImageFile;
        private Uri fileUri;

        private Switch mPrivateToggle;
        FeatherFragment featherFragment;
        EditText tagsText;
        private SelectImageDialogFragment imageSelectionFragment;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            if (savedInstanceState != null)
            {
                mUploadImageFile = CommonUtils.getSerializableFromBundleIfNotNull(
                        UPLOAD_IMAGE_FILE, savedInstanceState);
                String fileUriString = savedInstanceState.getString(UPLOAD_IMAGE_FILE_URI);
                if (fileUriString != null)
                {
                    fileUri = Uri.parse(fileUriString);
                }
            }
        }

        FeatherFragment getFeatherFragment()
        {
            if (featherFragment == null)
            {
                featherFragment = FeatherFragment.findOrCreateFeatherFragment(getSupportActivity()
                        .getSupportFragmentManager(),
                        new CustomFeatherFragmentParameters());
            }
            return featherFragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater,
                ViewGroup container, Bundle savedInstanceState) {
            super.onCreateView(inflater, container, savedInstanceState);
            View v = inflater.inflate(R.layout.activity_upload, container, false);
            getFeatherFragment().onCallingViewCreated();
            return v;
        }

        @Override
        public void onViewCreated(View view) {
            super.onViewCreated(view);
            init(view);
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
            outState.putSerializable(UPLOAD_IMAGE_FILE, mUploadImageFile);
            if (fileUri != null)
            {
                outState.putString(UPLOAD_IMAGE_FILE_URI, fileUri.toString());
            }
        }

        void init(View v)
        {
            v.findViewById(R.id.button_upload).setOnClickListener(this);
            v.findViewById(R.id.select_tags).setOnClickListener(this);
            v.findViewById(R.id.image_upload).setOnClickListener(this);
            v.findViewById(R.id.button_edit).setOnClickListener(this);
            tagsText = ((EditText) v.findViewById(R.id.edit_tags));

            mPrivateToggle = (Switch) v.findViewById(R.id.private_switch);
            mPrivateToggle.setChecked(true);

            Intent intent = getActivity().getIntent();
            boolean showOptions = true;
            if (intent != null)
            {
                if (Intent.ACTION_SEND.equals(intent.getAction())
                        && intent.getExtras() != null
                        && intent.getExtras().containsKey(Intent.EXTRA_STREAM)) {
                    Bundle extras = intent.getExtras();
                    setSelectedImageUri((Uri) extras.getParcelable(Intent.EXTRA_STREAM));
                    showOptions = false;
                } else if (intent.hasExtra(EXTRA_PENDING_UPLOAD_URI)) {
                    Uri uri = intent.getParcelableExtra(EXTRA_PENDING_UPLOAD_URI);
                    PhotoUpload pendingUpload = new UploadsProviderAccessor(getActivity())
                            .getPendingUpload(uri);
                    if (pendingUpload == null)
                    {
                        GuiUtils.alert(R.string.errorCantFindPendingUploadInformation);
                        getActivity().finish();
                        return;
                    }
                    new UploadsProviderAccessor(getActivity()).delete(pendingUpload.getId());
                    setSelectedImageUri(pendingUpload.getPhotoUri());
                    ((EditText) v.findViewById(R.id.edit_title)).setText(pendingUpload
                            .getMetaData()
                            .getTitle());
                    ((EditText) v.findViewById(R.id.edit_description)).setText(pendingUpload
                            .getMetaData()
                            .getDescription());
                    ((EditText) v.findViewById(R.id.edit_tags))
                            .setText(pendingUpload.getMetaData().getTags());
                    mPrivateToggle.setChecked(pendingUpload.getMetaData().isPrivate());

                    showOptions = false;
                }
            }
            if (mUploadImageFile != null)
            {
                setSelectedImageFile(mUploadImageFile);
                showOptions = false;
            }
            if (showOptions)
            {
                showSelectionDialog();
            }
        }

        @Override
        public void onActivityResultDelayed(int requestCode, int resultCode, Intent data) {
            super.onActivityResultDelayed(requestCode, resultCode, data);
            if (resultCode != RESULT_OK && (requestCode == REQUEST_GALLERY
                    || requestCode == REQUEST_CAMERA)) {
                showSelectionDialog();
                if (requestCode == REQUEST_CAMERA)
                {
                    removeGalleryEntryForCurrentFile();
                }
                return;
            }
            switch (requestCode) {
                case REQUEST_TAGS:
                    if (resultCode == RESULT_OK && data.getExtras() != null) {
                        String selectedTags = data.getExtras().getString(
                                SelectTagsActivity.SELECTED_TAGS);
                        tagsText.setText(selectedTags);
                    }
                    break;
                case REQUEST_GALLERY:
                    if (resultCode == RESULT_OK && data.getData() != null) {
                        setSelectedImageUri(data.getData());
                    }
                    break;
                case REQUEST_CAMERA:
                    if (resultCode == RESULT_OK) {
                        updateIngGalleryPictureSize();
                        setSelectedImageFile(mUploadImageFile);
                    } else {
                        mUploadImageFile = null;
                    }
                    break;
                case ACTION_REQUEST_FEATHER:
                    if (resultCode == RESULT_OK)
                    {
                        getFeatherFragment().onFeatherActivitySuccessResult(data);
                    }
                default:
                    break;
            }
            // this is necessary because onActivityResultDelayed is called
            // after the onCreateView method so the dialog may appear there if
            // view were recreated and here need to be closed
            if (imageSelectionFragment != null && !imageSelectionFragment.isDetached())
            {
                imageSelectionFragment.dismissAllowingStateLoss();
                imageSelectionFragment = null;
            }
        }

        void removeGalleryEntryForCurrentFile()
        {
            CommonUtils.debug(TAG, "Removing empty gallery entry: " + fileUri);
            int rowsDeleted = getActivity().getContentResolver().delete(fileUri, null, null);

            CommonUtils.debug(TAG, "Rows deleted:" + rowsDeleted);
        }

        void updateIngGalleryPictureSize()
        {
            int sdk = android.os.Build.VERSION.SDK_INT;
            if (sdk < android.os.Build.VERSION_CODES.HONEYCOMB)
            {
                return;
            }

            CommonUtils.debug(TAG, "Updating gallery entry: " + fileUri);
            BitmapFactory.Options options = ImageResizer.calculateImageSize(mUploadImageFile
                    .getAbsolutePath());
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.WIDTH, options.outWidth);
            values.put(MediaStore.Images.Media.HEIGHT, options.outHeight);
            int rowsUpdated = getActivity().getContentResolver()
                    .update(fileUri, values, null, null);
            CommonUtils.debug(TAG, "Rows updated:" + rowsUpdated);
        }

        File getNextFileName(String prefix) throws IOException
        {
            return new File(FileUtils
                    .getStorageFolder(getActivity()),
                    prefix + new Date().getTime() + ".jpg");
        }

        void showSelectionDialog()
        {
            if (imageSelectionFragment != null)
            {
                return;
            }
            imageSelectionFragment = SelectImageDialogFragment
                    .newInstance(new SelectImageDialogFragment.SelectedActionHandler() {

                        private static final long serialVersionUID = 1L;

                        @Override
                        public void cameraOptionSelected() {
                            try {
                                mUploadImageFile = getNextFileName("upload_");
                                // this is a hack for some
                                // devices taken from here
                                // http://thanksmister.com/2012/03/16/android_null_data_camera_intent/
                                ContentValues values = new ContentValues();
                                values.put(MediaStore.Images.Media.TITLE,
                                        mUploadImageFile.getName());
                                values.put(MediaStore.Images.Media.DATA,
                                        mUploadImageFile.getAbsolutePath());

                                Intent intent = new Intent(
                                        MediaStore.ACTION_IMAGE_CAPTURE);

                                fileUri = getActivity()
                                        .getContentResolver()
                                        .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                                values);
                                intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

                                startActivityForResult(intent, REQUEST_CAMERA);

                            } catch (IOException e) {
                                GuiUtils.error(
                                        TAG,
                                        R.string.errorCanNotFindExternalStorageForTakingPicture,
                                        e);
                            }
                        }

                        @Override
                        public void galleryOptionSelected() {
                            Intent intent = new Intent(Intent.ACTION_PICK);
                            intent.setType("image/*");
                            startActivityForResult(intent, REQUEST_GALLERY);
                        }
                    });
            imageSelectionFragment.show(getSupportActivity());
        }

        private void setSelectedImageUri(Uri imageUri) {
            mUploadImageFile = new File(ImageUtils.getRealPathFromURI(getActivity(), imageUri));
            setSelectedImageFile(mUploadImageFile);
        }

        private void setSelectedImageFile(File imageFile) {
            ImageView previewImage = (ImageView) getView().findViewById(R.id.image_upload);
            previewImage.setImageBitmap(ImageUtils.decodeFile(mUploadImageFile, 200));
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.select_tags:
                    TrackerUtils.trackButtonClickEvent("select_tags", getActivity());
                    Intent i = new Intent(getActivity(), SelectTagsActivity.class);
                    i.putExtra(SelectTagsActivity.SELECTED_TAGS, tagsText.getText().toString());
                    startActivityForResult(i, REQUEST_TAGS);
                    break;
                case R.id.button_upload:
                    TrackerUtils.trackButtonClickEvent("button_upload", getActivity());
                    if (mUploadImageFile != null) {
                        startUpload(mUploadImageFile);
                    } else
                    {
                        GuiUtils.alert(R.string.upload_pick_photo_first);
                        showSelectionDialog();
                    }
                    break;
                case R.id.button_edit:
                    TrackerUtils.trackButtonClickEvent("button_edit", getActivity());

                    getFeatherFragment().startFeather(mUploadImageFile,
                            ACTION_REQUEST_FEATHER);
                    break;
                case R.id.image_upload:
                    TrackerUtils.trackButtonClickEvent("image_upload", getActivity());
                    if (mUploadImageFile != null)
                    {
                        Intent intent = new Intent();
                        intent.setAction(android.content.Intent.ACTION_VIEW);
                        intent.setDataAndType(Uri.fromFile(mUploadImageFile), "image/png");
                        startActivity(intent);
                    } else
                    {
                        showSelectionDialog();
                    }
                    break;
            }
        }

        private void startUpload(File uploadFile) {
            UploadsProviderAccessor uploads = new UploadsProviderAccessor(getActivity());
            UploadMetaData metaData = new UploadMetaData();

            metaData.setTitle(((EditText) getView().findViewById(R.id.edit_title)).getText()
                    .toString());
            metaData.setDescription(((EditText) getView().findViewById(R.id.edit_description))
                    .getText()
                    .toString());
            metaData.setTags(((EditText) getView().findViewById(R.id.edit_tags)).getText()
                    .toString());
            metaData.setPrivate(mPrivateToggle.isChecked());

            uploads.addPendingUpload(Uri.fromFile(uploadFile), metaData, false,
                    false);
            getActivity().startService(new Intent(getActivity(), UploaderService.class));
            GuiUtils.info(R.string.uploading_in_background);
            getActivity().finish();
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            getFeatherFragment().onCallingViewDestroyed();
        }

        class CustomFeatherFragmentParameters implements FeatherFragment.FeatherFragmentParameters
        {

            @Override
            public File getNextFileName(String prefix) throws IOException {
                return UploadUiFragment.this.getNextFileName(prefix);
            }

            @Override
            public void setImageUri(Uri uri) {
                mUploadImageFile = new File(ImageUtils.getRealPathFromURI(getActivity(), uri));
            }

            @Override
            public void setHDFile(String path) {
                mUploadImageFile = new File(path);
            }

            @Override
            public void setImageBitmap(Bitmap result) {
                ImageView previewImage = getImageView();
                if (previewImage != null)
                {
                    previewImage.setImageBitmap(result);
                }
            }

            @Override
            public ImageView getImageView() {
                if (getView() != null)
                {
                    return (ImageView) getView().findViewById(R.id.image_upload);
                }
                return null;
            }

            @Override
            public Fragment getCallingFragment() {
                return UploadUiFragment.this;
            }

        }

        public static class SelectImageDialogFragment extends CommonClosableOnRestoreDialogFragment
        {
            public static interface SelectedActionHandler extends Serializable
            {
                void cameraOptionSelected();

                void galleryOptionSelected();
            }

            private SelectedActionHandler handler;

            public static SelectImageDialogFragment newInstance(
                    SelectedActionHandler handler)
            {
                SelectImageDialogFragment frag = new SelectImageDialogFragment();
                frag.handler = handler;
                return frag;
            }

            @Override
            public void onCancel(DialogInterface dialog) {
                super.onCancel(dialog);
                getActivity().finish();
            }

            @Override
            public Dialog onCreateDialog(Bundle savedInstanceState) {
                final CharSequence[] items = {
                        getString(R.string.upload_camera_option),
                        getString(R.string.upload_gallery_option)
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(R.string.upload_title);
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        if (handler == null)
                        {
                            return;
                        }
                        switch (item) {
                            case 0:
                                TrackerUtils.trackContextMenuClickEvent("menu_camera",
                                        SelectImageDialogFragment.this);
                                handler.cameraOptionSelected();
                                return;
                            case 1:
                                TrackerUtils.trackContextMenuClickEvent("menu_gallery",
                                        SelectImageDialogFragment.this);
                                handler.galleryOptionSelected();
                                return;
                        }
                    }
                });
                return builder.create();
            }
        }
    }
}
