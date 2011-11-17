/**
 * The general photo viewing screen
 */

package me.openphoto.android.app;

import me.openphoto.android.app.model.Photo;
import me.openphoto.android.app.net.PhotoResponse;
import me.openphoto.android.app.net.ReturnSize;
import me.openphoto.android.app.ui.lib.ImageStorage;
import me.openphoto.android.app.ui.widget.ActionBar;
import android.app.Activity;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * The general photo viewing screen
 * 
 * @author pboos
 */
public class PhotoDetailsActivity extends Activity implements OnClickListener {
    private static final String TAG = PhotoDetailsActivity.class.getSimpleName();

    public static final String EXTRA_PHOTO = "EXTRA_PHOTO";

    private Photo mPhoto;
    private ImageStorage mStorage;

    private ActionBar mActionBar;
    private ImageView mImageView;
    private TextView mTitleText;
    private TextView mDescriptionText;

    /*
     * (non-Javadoc)
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photo_details);

        mStorage = new ImageStorage(this);
        mPhoto = getIntent().getParcelableExtra(EXTRA_PHOTO);

        mImageView = (ImageView) findViewById(R.id.image);
        mImageView.setOnClickListener(this);
        mTitleText = (TextView) findViewById(R.id.image_title);
        mDescriptionText = (TextView) findViewById(R.id.image_description);
        mActionBar = (ActionBar) findViewById(R.id.actionbar);

        display(mPhoto);
    }

    private void display(Photo photo) {
        new LoadImageTask().execute();
        mTitleText.setText(photo.getTitle());
        mDescriptionText.setText(photo.getDescription());
    }

    @Override
    public void onClick(View view) {
        // TODO
    }

    private class LoadImageTask extends AsyncTask<Void, Void, Bitmap> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mActionBar.startLoading();
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            try {
                if (mPhoto.getUrl("1024x1024") == null) {
                    PhotoResponse response = Preferences.getApi(PhotoDetailsActivity.this)
                            .getPhoto(mPhoto.getId(), new ReturnSize(1024, 1024));
                    mPhoto = response.getPhoto();
                }
                return mStorage.getBitmap(mPhoto.getUrl("1024x1024"));
            } catch (Exception e) {
                Log.e(TAG, "Could not get photo", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            mActionBar.stopLoading();
            if (bitmap != null) {
                mImageView.setImageBitmap(bitmap);
            } else {
                Toast.makeText(PhotoDetailsActivity.this, "Error occured", Toast.LENGTH_LONG)
                        .show();
            }
            super.onPostExecute(bitmap);
        }

    }
}
