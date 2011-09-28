/**
 * The photo gallery screen
 */

package me.openphoto.android.app;

import me.openphoto.android.app.model.Photo;
import me.openphoto.android.app.net.IOpenPhotoApi;
import me.openphoto.android.app.net.OpenPhotoApi;
import me.openphoto.android.app.net.Paging;
import me.openphoto.android.app.net.PhotosResponse;
import me.openphoto.android.app.net.ReturnSize;
import me.openphoto.android.app.ui.adapter.EndlessAdapter;
import me.openphoto.android.app.ui.lib.ImageStorage;
import me.openphoto.android.app.ui.widget.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;

/**
 * The photo gallery screen
 * 
 * @author pas
 */
public class GalleryActivity extends Activity {
    private static final String TAG = GalleryActivity.class.getSimpleName();

    private ActionBar mActionBar;

    /**
     * Called when Gallery Activity is first loaded
     * 
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gallery);
        mActionBar = (ActionBar) findViewById(R.id.actionbar);
        GridView photosGrid = (GridView) findViewById(R.id.grid_photos);
        photosGrid.setAdapter(new PhotosEndlessAdapter());
    }

    private class PhotosEndlessAdapter extends EndlessAdapter<Photo> {
        private final IOpenPhotoApi mOpenPhotoApi;
        private final ImageStorage mStorage = new ImageStorage();

        public PhotosEndlessAdapter() {
            super();
            mOpenPhotoApi = OpenPhotoApi
                    .createInstance(Preferences.getServer(GalleryActivity.this));
        }

        @Override
        public long getItemId(int position) {
            return ((Photo) getItem(position)).getId().hashCode();
        }

        @Override
        public View getView(Photo photo, View convertView) {
            if (convertView == null) {
                final LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = layoutInflater.inflate(R.layout.gallery_image, null);
            }
            ImageView image = (ImageView) convertView.findViewById(R.id.image);
            image.setImageBitmap(null); // TODO maybe a loading image
            mStorage.displayImageFor(image, photo.getUrl("200x200"), photo.getId() + "_200x200");
            return convertView;
        }

        @Override
        public LoadResponse loadItems(int page) {
            try {
                PhotosResponse response = mOpenPhotoApi.getPhotos(new ReturnSize(200, 200),
                        new Paging(page, 30));
                boolean hasNextPage = response.getCurrentPage() < response.getTotalPages();
                return new LoadResponse(response.getPhotos(), hasNextPage);
            } catch (Exception e) {
                Log.e(TAG, "Could not load next photos in list", e);
            }
            return new LoadResponse(null, false);
        }

        @Override
        protected void onStartLoading() {
            mActionBar.startLoading();
        }

        @Override
        protected void onStoppedLoading() {
            mActionBar.stopLoading();
        }
    }
}
