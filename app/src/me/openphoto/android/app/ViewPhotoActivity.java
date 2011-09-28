/**
 * The general photo viewing screen
 */

package me.openphoto.android.app;

import me.openphoto.android.app.model.Photo;
import me.openphoto.android.app.net.OpenPhotoApi;
import me.openphoto.android.app.net.PhotoResponse;
import me.openphoto.android.app.net.ReturnSize;
import me.openphoto.android.app.ui.lib.ImageStorage;
import me.openphoto.android.app.ui.widget.ActionBar;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
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
public class ViewPhotoActivity extends Activity implements OnClickListener {

    public static final String EXTRA_PHOTO = "EXTRA_PHOTO";

    private Photo mPhoto;

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
        setContentView(R.layout.view_photo);

        mPhoto = getIntent().getParcelableExtra(EXTRA_PHOTO);

        mImageView = (ImageView) findViewById(R.id.image);
        mImageView.setOnClickListener(this);
        mTitleText = (TextView) findViewById(R.id.image_title);
        mDescriptionText = (TextView) findViewById(R.id.image_description);
        mActionBar = (ActionBar) findViewById(R.id.actionbar);

        if (mPhoto.getUrl("1024x1024") != null) {
            display(mPhoto);
        } else {
            new LoadPhotoTask().execute();
        }
    }

    private void display(Photo result) {
        new ImageStorage().displayImageFor(mImageView, result.getUrl("1024x1024"), "1024x1024_"
                + result.getId());
        mTitleText.setText(result.getTitle());
        mDescriptionText.setText(result.getDescription());
    }

    @Override
    public void onClick(View view) {
        boolean show = mActionBar.getVisibility() == View.GONE;
        int newVisibility = show ? View.VISIBLE : View.GONE;

        mActionBar.setVisibility(newVisibility);
        mTitleText.setVisibility(newVisibility);
        mDescriptionText.setVisibility(newVisibility);
    }

    private class LoadPhotoTask extends AsyncTask<Void, Void, Photo> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mActionBar.startLoading();
        }

        @Override
        protected Photo doInBackground(Void... params) {
            try {
                PhotoResponse response = OpenPhotoApi.createInstance(
                        Preferences.getServer(ViewPhotoActivity.this)).getPhoto(mPhoto.getId(),
                        new ReturnSize(1024, 1024));
                return response.getPhoto();
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(Photo result) {
            mActionBar.stopLoading();
            if (result != null) {
                display(result);
            } else {
                Toast.makeText(ViewPhotoActivity.this, "Error occured", Toast.LENGTH_LONG).show();
            }
            super.onPostExecute(result);
        }

    }
}
