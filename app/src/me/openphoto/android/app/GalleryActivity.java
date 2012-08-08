package me.openphoto.android.app;

import me.openphoto.android.app.model.Photo;
import me.openphoto.android.app.ui.adapter.PhotosEndlessAdapter;
import me.openphoto.android.app.ui.lib.ImageStorage;
import me.openphoto.android.app.ui.widget.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
public class GalleryActivity extends Activity implements OnItemClickListener, Refreshable {
    public static final String TAG = GalleryActivity.class.getSimpleName();

    public static String EXTRA_TAG = "EXTRA_TAG";

    private ActionBar mActionBar;
    private GalleryAdapter mAdapter;
    private String mTags;

    /**
     * Called when Gallery Activity is first loaded
     * 
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        if (getParent() != null) {
            mActionBar = (ActionBar) getParent().findViewById(R.id.actionbar);
            findViewById(R.id.actionbar).setVisibility(View.GONE);
        } else {
            mActionBar = (ActionBar) findViewById(R.id.actionbar);
        }

        refresh();
    }

    @Override
    public void refresh() {
        mTags = getIntent() != null ? getIntent().getStringExtra(EXTRA_TAG) : null;
        if (mTags != null) {
            mAdapter = new GalleryAdapter(mTags);
        } else {
            mAdapter = new GalleryAdapter();
        }

        GridView photosGrid = (GridView) findViewById(R.id.grid_photos);
        photosGrid.setAdapter(mAdapter);
        photosGrid.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
        Intent intent = new Intent(this, PhotoDetailsActivity.class);
        intent.putParcelableArrayListExtra(PhotoDetailsActivity.EXTRA_ADAPTER_PHOTOS,
                mAdapter.getItems());
        intent.putExtra(PhotoDetailsActivity.EXTRA_ADAPTER_POSITION, position);
        intent.putExtra(PhotoDetailsActivity.EXTRA_ADAPTER_TAGS, mTags);
        startActivity(intent);
    }

    private class GalleryAdapter extends PhotosEndlessAdapter {
        private final ImageStorage mStorage = new ImageStorage(GalleryActivity.this);

        public GalleryAdapter() {
            super(GalleryActivity.this);
        }

        public GalleryAdapter(String tagFilter) {
            super(GalleryActivity.this, tagFilter);
        }

        @Override
        public View getView(Photo photo, View convertView) {
            if (convertView == null) {
                final LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = layoutInflater.inflate(R.layout.item_gallery_image, null);
            }
            ImageView image = (ImageView) convertView.findViewById(R.id.image);
            image.setImageBitmap(null); // TODO maybe a loading image
            mStorage.displayImageFor(image, photo.getUrl(SIZE_SMALL));
            return convertView;
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
