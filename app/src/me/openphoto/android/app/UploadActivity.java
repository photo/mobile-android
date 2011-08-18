
package me.openphoto.android.app;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import me.openphoto.android.app.util.ImageUtils;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

/**
 * This activity handles uploading pictures to OpenPhoto.
 * 
 * @author Patrick Boos
 */
public class UploadActivity extends Activity implements OnClickListener {

    private static final int REQUEST_GALLERY = 1;

    private File mUploadImageFile;

    /**
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photo_upload);
        findViewById(R.id.button_pick).setOnClickListener(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_GALLERY:
                if (resultCode == RESULT_OK && data.getData() != null) {
                    setSelectedImageUri(data.getData());
                }
                break;

            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    private void setSelectedImageUri(Uri imageUri) {
        mUploadImageFile = new File(ImageUtils.getRealPathFromURI(this, imageUri));
        ImageView previewImage = (ImageView) findViewById(R.id.image_upload);
        previewImage.setImageBitmap(decodeFile(mUploadImageFile, previewImage.getWidth()));
    }

    /**
     * decodes image and scales it to reduce memory consumption <br />
     * <br />
     * Source: http://stackoverflow
     * .com/questions/477572/android-strange-out-of-memory-issue/823966#823966
     * 
     * @param file File
     * @param requiredSize size that the image should have
     * @return image in required size
     */
    private Bitmap decodeFile(File file, int requiredSize) {
        try {
            // Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(file), null, o);

            // The new size we want to scale to
            final int REQUIRED_SIZE = requiredSize;

            // Find the correct scale value. It should be the power of 2.
            int width_tmp = o.outWidth, height_tmp = o.outHeight;
            int scale = 1;
            while (true) {
                if (width_tmp / 2 < REQUIRED_SIZE || height_tmp / 2 < REQUIRED_SIZE)
                    break;
                width_tmp /= 2;
                height_tmp /= 2;
                scale *= 2;
            }

            // Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            return BitmapFactory.decodeStream(new FileInputStream(file), null, o2);
        } catch (FileNotFoundException e) {
        }
        return null;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_pick:
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_GALLERY);
                break;

            default:
                break;
        }
    }

}
