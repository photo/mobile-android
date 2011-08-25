
package me.openphoto.android.app;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import me.openphoto.android.app.net.OpenPhotoApi;
import me.openphoto.android.app.net.UploadMetaData;
import me.openphoto.android.app.net.UploadResponse;
import me.openphoto.android.app.util.FileUtils;
import me.openphoto.android.app.util.ImageUtils;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ToggleButton;

/**
 * This activity handles uploading pictures to OpenPhoto.
 * 
 * @author Patrick Boos
 */
public class UploadActivity extends Activity implements OnClickListener {
    private static final String TAG = UploadActivity.class.getSimpleName();

    private static final int REQUEST_GALLERY = 0;
    private static final int REQUEST_CAMERA = 1;

    private static final int DIALOG_SELECT_IMAGE = 0;

    private File mUploadImageFile;

    private ToggleButton mPrivateToggle;

    /**
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photo_upload);
        findViewById(R.id.button_upload).setOnClickListener(this);
        findViewById(R.id.image_upload).setOnClickListener(this);
        showDialog(DIALOG_SELECT_IMAGE);
        mPrivateToggle = (ToggleButton) findViewById(R.id.toggle_private);
        mPrivateToggle.setChecked(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
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
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog;
        switch (id) {
            case DIALOG_SELECT_IMAGE:
                final CharSequence[] items = {
                        "Camera", "Gallery"
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Upload");
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        switch (item) {
                            case 0:
                                try {
                                    mUploadImageFile = new File(FileUtils.getStorageFolder(),
                                            "upload_" + new Date().getTime() + ".jpg");
                                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                    intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT,
                                            Uri.fromFile(mUploadImageFile));
                                    startActivityForResult(intent, REQUEST_CAMERA);
                                } catch (IOException e) {
                                    Toast.makeText(UploadActivity.this,
                                            "Can not find external storage for taking a picture",
                                            Toast.LENGTH_LONG).show();
                                }
                                return;
                            case 1:
                                Intent intent = new Intent(Intent.ACTION_PICK);
                                intent.setType("image/*");
                                startActivityForResult(intent, REQUEST_GALLERY);
                                return;
                        }
                    }
                }).setOnCancelListener(new OnCancelListener() {

                    @Override
                    public void onCancel(DialogInterface dialog) {
                        finish();
                    }
                });
                dialog = builder.create();
                break;
            default:
                dialog = null;
        }
        return dialog;
    }

    private void setSelectedImageUri(Uri imageUri) {
        mUploadImageFile = new File(ImageUtils.getRealPathFromURI(this, imageUri));
        setSelectedImageFile(mUploadImageFile);
    }

    private void setSelectedImageFile(File imageFile) {
        ImageView previewImage = (ImageView) findViewById(R.id.image_upload);
        previewImage
                .setImageBitmap(ImageUtils.decodeFile(mUploadImageFile, previewImage.getWidth()));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_upload:
                if (mUploadImageFile != null) {
                    new UploadTask().execute(mUploadImageFile);
                }
                break;
            case R.id.image_upload:
                Intent intent = new Intent();
                intent.setAction(android.content.Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(mUploadImageFile), "image/png");
                startActivity(intent);
                break;
        }
    }

    private class UploadTask extends AsyncTask<File, Void, UploadResponse> {

        private ProgressDialog mDialog;

        @Override
        protected void onPreExecute() {
            mDialog = ProgressDialog.show(UploadActivity.this, "",
                    "Uploading image to openphoto...", true);
        }

        @Override
        protected UploadResponse doInBackground(File... params) {
            UploadMetaData metaData = new UploadMetaData();
            metaData.setTitle(((EditText) findViewById(R.id.edit_title)).getText().toString());
            metaData.setDescription(((EditText) findViewById(R.id.edit_description)).getText()
                    .toString());
            metaData.setTags(((EditText) findViewById(R.id.edit_tags)).getText().toString());
            metaData.setPrivate(mPrivateToggle.isChecked());
            // TODO add private and effects aviary

            try {
                return OpenPhotoApi.createInstance(Preferences.getServer(UploadActivity.this))
                        .uploadPhoto(params[0], metaData);
            } catch (Exception e) {
                Log.e(TAG, "Error while uploading", e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(UploadResponse result) {
            mDialog.dismiss();

            if (result != null && result.isSuccess()) {
                Toast.makeText(UploadActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();
                finish();
            } else if (result != null) {
                Toast.makeText(UploadActivity.this, "Upload failed: " + result.getMessage(),
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(UploadActivity.this, "Upload failed", Toast.LENGTH_LONG).show();
            }
        }
    }
}
