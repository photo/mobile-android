/**
 * The Main screen of OpenPhoto
 */

package me.openphoto.android.app;

import java.net.URL;

import me.openphoto.android.app.model.Photo;
import me.openphoto.android.app.net.IOpenPhotoApi;
import me.openphoto.android.app.net.OpenPhotoApi;
import me.openphoto.android.app.net.Paging;
import me.openphoto.android.app.net.ReturnSize;
import me.openphoto.android.app.ui.widget.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

/**
 * The Main screen of OpenPhoto
 * 
 * @author pas
 */
public class MainActivity extends Activity implements OnClickListener {
    public static final String TAG = MainActivity.class.getSimpleName();

    private ImageButton searchBtn;
    private ImageButton cameraBtn;
    private ImageButton galleryBtn;
    private ImageButton settingsBtn;

    private ActionBar mActionBar;

    /**
     * Called when Main Activity is first loaded
     * 
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mActionBar = (ActionBar) findViewById(R.id.actionbar);
        new LoadImageTask().execute();

        // Get refereneces to navigation buttons
        searchBtn = (ImageButton) findViewById(R.id.main_search_btn);
        cameraBtn = (ImageButton) findViewById(R.id.main_camera_btn);
        galleryBtn = (ImageButton) findViewById(R.id.main_gallery_btn);
        settingsBtn = (ImageButton) findViewById(R.id.main_settings_btn);
        searchBtn.setOnClickListener(this);
        cameraBtn.setOnClickListener(this);
        galleryBtn.setOnClickListener(this);
        settingsBtn.setOnClickListener(this);
    }

    private class LoadImageTask extends AsyncTask<Void, Void, Bitmap> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mActionBar.startLoading();
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            IOpenPhotoApi api = OpenPhotoApi
                    .createInstance(Preferences.getServer(MainActivity.this));
            try {
                Photo photo = api.getPhotos(new ReturnSize(600, 600), null, new Paging(1, 1))
                        .getPhotos().get(0);
                // TODO do not use base, make getPhotos actually use a
                // returnSize parameter that should be used then.
                return BitmapFactory.decodeStream(new URL(photo
                        .getUrl("600x600")).openStream());
            } catch (Exception e) {
                Log.w(TAG, "Error while getting image", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            mActionBar.stopLoading();
            if (result != null) {
                ImageView image = (ImageView) findViewById(R.id.image);
                image.setImageBitmap(result);
                image.setVisibility(View.VISIBLE);
            } else {
                Toast.makeText(MainActivity.this, "Could not download image",
                        Toast.LENGTH_LONG).show();
            }
        }

    }

    /**
     * Handle clicks on the navigation buttons
     */
    @Override
    public void onClick(View v) {
        Class<?> nextScreen = null;
        if (v.equals(searchBtn)) {
            nextScreen = SearchActivity.class;
        } else if (v.equals(cameraBtn)) {
            nextScreen = UploadActivity.class;
        } else if (v.equals(galleryBtn)) {
            nextScreen = GalleryActivity.class;
        } else if (v.equals(settingsBtn)) {
            nextScreen = SettingsActivity.class;
        }

        if (nextScreen != null) {
            // Go to the next screen, but keep this Activity in the stack
            Intent i = new Intent(this, nextScreen);
            startActivity(i);
        }
    }
}
