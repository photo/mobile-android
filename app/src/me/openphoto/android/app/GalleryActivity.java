/**
 * The photo gallery screen
 */

package me.openphoto.android.app;

import java.util.ArrayList;
import java.util.List;

import me.openphoto.android.app.model.Photo;
import me.openphoto.android.app.net.IOpenPhotoApi;
import me.openphoto.android.app.net.Paging;
import me.openphoto.android.app.net.PhotosResponse;
import me.openphoto.android.app.net.ReturnSize;
import me.openphoto.android.app.ui.adapter.EndlessAdapter;
import me.openphoto.android.app.ui.lib.ImageStorage;
import me.openphoto.android.app.ui.widget.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;

/**
 * The photo gallery screen
 * 
 * @author pas
 */
public class GalleryActivity extends Activity implements OnItemClickListener {
    private static final String TAG = GalleryActivity.class.getSimpleName();

    public static String EXTRA_TAG = "EXTRA_TAG";

    private ActionBar mActionBar;

    private PhotosEndlessAdapter mAdapter;

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

        if (getIntent() != null && getIntent().hasExtra(EXTRA_TAG)) {
            mAdapter = new PhotosEndlessAdapter(getIntent().getStringExtra(EXTRA_TAG));
        } else {
            mAdapter = new PhotosEndlessAdapter();
        }

        GridView photosGrid = (GridView) findViewById(R.id.grid_photos);
        photosGrid.setAdapter(mAdapter);
        photosGrid.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
        Intent intent = new Intent(this, PhotoDetailsActivity.class);
        intent.putExtra(PhotoDetailsActivity.EXTRA_PHOTO, (Photo) mAdapter.getItem(position));
        startActivity(intent);
    }

    private class PhotosEndlessAdapter extends EndlessAdapter<Photo> {
        private final IOpenPhotoApi mOpenPhotoApi;
        private final ImageStorage mStorage = new ImageStorage(GalleryActivity.this);
        private final List<String> mTagFilter;

        public PhotosEndlessAdapter() {
            this(null);
        }

        public PhotosEndlessAdapter(String tagFilter) {
            super(30);
            mOpenPhotoApi = Preferences.getApi(GalleryActivity.this);
            mTagFilter = new ArrayList<String>(1);
            if (tagFilter != null) {
                mTagFilter.add(tagFilter);
            }
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
            mStorage.displayImageFor(image, photo.getUrl("200x200"));
            return convertView;
        }

        @Override
        public LoadResponse loadItems(int page) {
            try {
                PhotosResponse response = mOpenPhotoApi.getPhotos(new ReturnSize(200, 200),
                        mTagFilter, new Paging(page, getPageSize()));
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
