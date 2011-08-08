/**
 * The Main screen of OpenPhoto
 */

package me.openphoto.android.app;

import java.net.URL;

import me.openphoto.android.app.model.Photo;
import me.openphoto.android.app.net.OpenPhotoApi;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

/**
 * The Main screen of OpenPhoto
 * 
 * @author pas
 */
public class Main extends Activity {
    /**
     * Called when Main Activity is first loaded
     * 
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        findViewById(R.id.image).setVisibility(View.GONE);
        new LoadImageTask().execute();
    }

    private class LoadImageTask extends AsyncTask<Void, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(Void... params) {
            OpenPhotoApi api = new OpenPhotoApi("http://current.openphoto.me");
            try {
                Photo photo = api.getPhotos().getPhotos().get(0);
                return BitmapFactory.decodeStream(new URL(photo.getUrl("640x960")).openStream());
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            if (result != null) {
                findViewById(R.id.progress).setVisibility(View.GONE);
                ImageView image = (ImageView) findViewById(R.id.image);
                image.setImageBitmap(result);
                image.setVisibility(View.VISIBLE);
            } else {
                Toast.makeText(Main.this, "Could not download image", Toast.LENGTH_LONG).show();
            }
        }

    }
}
