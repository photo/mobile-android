
package me.openphoto.android.app;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Date;

import me.openphoto.android.app.net.UploadMetaData;
import me.openphoto.android.app.provider.PhotoUpload;
import me.openphoto.android.app.provider.UploadsProviderAccessor;
import me.openphoto.android.app.service.UploaderService;
import me.openphoto.android.app.util.FileUtils;
import me.openphoto.android.app.util.GuiUtils;
import me.openphoto.android.app.util.ImageUtils;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import com.WazaBe.HoloEverywhere.LayoutInflater;
import com.WazaBe.HoloEverywhere.app.AlertDialog;
import com.WazaBe.HoloEverywhere.app.Dialog;
import com.WazaBe.HoloEverywhere.sherlock.SActivity;
import com.WazaBe.HoloEverywhere.widget.Switch;
import com.facebook.android.R;

/**
 * This activity handles uploading pictures to OpenPhoto.
 * 
 * @author Patrick Boos
 */
public class UploadActivity extends SActivity {
    public static final String TAG = UploadActivity.class.getSimpleName();

    public static final String EXTRA_PENDING_UPLOAD_URI = "pending_upload_uri";

    private static final int REQUEST_GALLERY = 0;
    private static final int REQUEST_CAMERA = 1;
    private static final int REQUEST_TAGS = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null)
        {
        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, new UiFragment())
                .commit();
        }
    }

    public static class UiFragment extends CommonFragment
            implements OnClickListener
    {
        private File mUploadImageFile;

        private Switch mPrivateToggle;

        @Override
        public View onCreateView(LayoutInflater inflater,
                ViewGroup container, Bundle savedInstanceState) {
            super.onCreateView(inflater, container, savedInstanceState);
            View v = inflater.inflate(R.layout.activity_upload, container, false);
            init(v);
            return v;
        }

        void init(View v)
        {
            v.findViewById(R.id.button_upload).setOnClickListener(this);
            v.findViewById(R.id.select_tags).setOnClickListener(this);
            v.findViewById(R.id.image_upload).setOnClickListener(this);
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
            if (showOptions)
            {
                showSelectionDialog();
            }
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if (resultCode != RESULT_OK && (requestCode == REQUEST_GALLERY
                    || requestCode == REQUEST_CAMERA)) {
                showSelectionDialog();
                return;
            }

            switch (requestCode) {
                case REQUEST_TAGS:
                    if (resultCode == RESULT_OK && data.getExtras() != null) {
                        String selectedTags = data.getExtras().getString(
                                "SELECTED_TAGS");
                        ((EditText) getView().findViewById(R.id.edit_tags)).setText(selectedTags);
                    }
                    break;
                case REQUEST_GALLERY:
                    if (resultCode == RESULT_OK && data.getData() != null) {
                        setSelectedImageUri(data.getData());
                    }
                    break;
                case REQUEST_CAMERA:
                    if (resultCode == RESULT_OK) {
                        setSelectedImageFile(mUploadImageFile);
                    } else {
                        mUploadImageFile = null;
                    }
                    break;
                default:
                    break;
            }
        }

        void showSelectionDialog()
        {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {

                @Override
                public void run() {
                    SelectImageDialogFragment imageSelectionFragment =
                            SelectImageDialogFragment
                                    .newInstance(new SelectImageDialogFragment.SelectedActionHandler() {

                                        private static final long serialVersionUID = 1L;

                                        @Override
                                        public void cameraOptionSelected() {
                                            try {
                                                mUploadImageFile = new File(FileUtils
                                                        .getStorageFolder(getActivity()),
                                                        "upload_" + new Date().getTime() + ".jpg");
                                                Intent intent = new Intent(
                                                        MediaStore.ACTION_IMAGE_CAPTURE);
                                                intent.putExtra(
                                                        android.provider.MediaStore.EXTRA_OUTPUT,
                                                        Uri.fromFile(mUploadImageFile));
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
                    imageSelectionFragment.replace(getActivity().getSupportFragmentManager());
                }
            }, 100);
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
                    Intent i = new Intent(getActivity(), SelectTagsActivity.class);
                    startActivityForResult(i, REQUEST_TAGS);
                    break;
                case R.id.button_upload:
                    if (mUploadImageFile != null) {
                        startUpload(mUploadImageFile);
                    } else
                    {
                        GuiUtils.alert(R.string.upload_pick_photo_first);
                        showSelectionDialog();
                    }
                    break;
                case R.id.image_upload:
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
    }

    public static class SelectImageDialogFragment extends CommonDialogFragment
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
                            handler.cameraOptionSelected();
                            return;
                        case 1:
                            handler.galleryOptionSelected();
                            return;
                    }
                }
            });
            return builder.create();
        }
    }
}
