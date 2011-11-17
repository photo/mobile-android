/**
 * The Main screen of OpenPhoto
 */

package me.openphoto.android.app;

import me.openphoto.android.app.model.Photo;
import me.openphoto.android.app.net.IOpenPhotoApi;
import me.openphoto.android.app.net.Paging;
import me.openphoto.android.app.net.ReturnSizes;
import me.openphoto.android.app.service.UploaderService;
import me.openphoto.android.app.ui.lib.ImageStorage;
import me.openphoto.android.app.ui.lib.ImageStorage.OnImageDisplayedCallback;
import me.openphoto.android.app.ui.widget.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.Toast;

/**
 * The Main screen of OpenPhoto
 * 
 * @author pas
 */
public class MainActivity extends Activity implements OnClickListener, OnImageDisplayedCallback {
    public static final String TAG = MainActivity.class.getSimpleName();

    private View tagButton;
    private View uploadButton;
    private View galleryButton;

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

        // Get references to navigation buttons
        tagButton = findViewById(R.id.tags);
        uploadButton = findViewById(R.id.upload);
        galleryButton = findViewById(R.id.photos);
        tagButton.setOnClickListener(this);
        uploadButton.setOnClickListener(this);
        galleryButton.setOnClickListener(this);

        startService(new Intent(this, UploaderService.class));
    }

    @Override
    protected void onResume() {
        super.onResume();
        uploadButton.setEnabled(Preferences.isLoggedIn(this));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.settings:
                Intent i = new Intent(this, SettingsActivity.class);
                startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class LoadImageTask extends AsyncTask<Void, Void, Photo> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mActionBar.startLoading();
        }

        @Override
        protected Photo doInBackground(Void... params) {
            IOpenPhotoApi api = Preferences.getApi(MainActivity.this);
            try {
                return api.getPhotos(new ReturnSizes(600, 600), null, new Paging(1, 1))
                        .getPhotos().get(0);
            } catch (Exception e) {
                Log.w(TAG, "Error while getting image", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(Photo result) {
            mActionBar.stopLoading();
            if (result != null) {
                showImage(result);
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
        if (v.equals(tagButton)) {
            nextScreen = SearchActivity.class;
        } else if (v.equals(uploadButton)) {
            nextScreen = UploadActivity.class;
        } else if (v.equals(galleryButton)) {
            nextScreen = GalleryActivity.class;
        }

        if (nextScreen != null) {
            // Go to the next screen, but keep this Activity in the stack
            Intent i = new Intent(this, nextScreen);
            startActivity(i);
        }
    }

    public void showImage(Photo photo) {
        mActionBar.startLoading();
        ImageView image = (ImageView) findViewById(R.id.image);
        new ImageStorage(MainActivity.this).displayImageFor(image, photo.getUrl("600x600"),
                MainActivity.this);
    }

    @Override
    public void onImageDisplayed(ImageView view) {
        mActionBar.stopLoading();
    }
}
