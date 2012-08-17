
package me.openphoto.android.app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import me.openphoto.android.app.model.Photo;
import me.openphoto.android.app.net.PhotoResponse;
import me.openphoto.android.app.net.ReturnSizes;
import me.openphoto.android.app.ui.adapter.PhotosEndlessAdapter;
import me.openphoto.android.app.ui.lib.ImageStorage;
import me.openphoto.android.app.ui.lib.ImageStorage.OnImageDisplayedCallback;
import me.openphoto.android.app.ui.widget.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;
import android.widget.Toast;

import com.bugsense.trace.BugSenseHandler;

/**
 * The general photo viewing screen
 * 
 * @author pboos
 */
public class PhotoDetailsActivity extends Activity {
    private static final String TAG = PhotoDetailsActivity.class.getSimpleName();

    public static final String EXTRA_PHOTO = "EXTRA_PHOTO";

    public static final String EXTRA_ADAPTER_PHOTOS = "EXTRA_ADAPTER_PHOTOS";
    public static final String EXTRA_ADAPTER_POSITION = "EXTRA_ADAPTER_POSITION";
    public static final String EXTRA_ADAPTER_TAGS = "EXTRA_ADAPTER_TAGS";

    private ImageStorage mStorage;

    private ActionBar mActionBar;
    private ViewPager mViewPager;

    private PhotoDetailPagerAdapter mAdapter;

    /*
     * (non-Javadoc)
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_details);

        mStorage = new ImageStorage(this);

        if (getIntent().hasExtra(EXTRA_PHOTO)) {
            Photo photo = getIntent().getParcelableExtra(EXTRA_PHOTO);
            ArrayList<Photo> photos = new ArrayList<Photo>();
            photos.add(photo);
            mAdapter = new PhotoDetailPagerAdapter(new PhotosAdapter(this, photos));
        } else if (getIntent().hasExtra(EXTRA_ADAPTER_PHOTOS)) {
            ArrayList<Photo> photos = getIntent().getParcelableArrayListExtra(EXTRA_ADAPTER_PHOTOS);
            String tags = getIntent().getStringExtra(EXTRA_ADAPTER_TAGS);
            mAdapter = new PhotoDetailPagerAdapter(new PhotosAdapter(this, photos, tags));
        }

        mActionBar = (ActionBar) findViewById(R.id.actionbar);
        mActionBar.setVisibility(View.GONE);
        mViewPager = (ViewPager) findViewById(R.id.photos);
        mViewPager.setAdapter(mAdapter);

        int position = getIntent().getIntExtra(EXTRA_ADAPTER_POSITION, 0);
        if (position > 0) {
            mViewPager.setCurrentItem(position);
        }
    }

    private class PhotoDetailPagerAdapter extends PagerAdapter {

        private final LayoutInflater mInflator;
        private final PhotosAdapter mAdapter;
        private final DataSetObserver mObserver = new DataSetObserver() {

            @Override
            public void onChanged() {
                super.onChanged();
                notifyDataSetChanged();
            }

            @Override
            public void onInvalidated() {
                super.onInvalidated();
                notifyDataSetChanged();
            }
        };

        public PhotoDetailPagerAdapter(PhotosAdapter adatper) {
            mAdapter = adatper;
            mAdapter.registerDataSetObserver(mObserver);
            mInflator = (LayoutInflater) PhotoDetailsActivity.this
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return mAdapter.getCount();
        }

        @Override
        public Object instantiateItem(View collection, int position) {
            if (getCount() > 1 && position > getCount() - 6) {
                mAdapter.loadNextPage();
            }
            Photo photo = (Photo) mAdapter.getItem(position);

            final View view = mInflator.inflate(R.layout.item_photo_detail,
                    ((ViewPager) collection), false);
            final ImageView imageView = (ImageView) view.findViewById(R.id.image);
            view.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    ScaleType newScaleType = imageView.getScaleType() == ScaleType.CENTER_CROP ?
                            ScaleType.FIT_CENTER : ScaleType.CENTER_CROP;
                    imageView.setScaleType(newScaleType);
                }
            });

            TextView titleText = (TextView) view.findViewById(R.id.image_title);
            TextView descriptionText = (TextView) view.findViewById(R.id.image_description);
            titleText.setText(photo.getTitle());
            descriptionText.setText(photo.getDescription());
            if (TextUtils.isEmpty(photo.getTitle())) {
                titleText.setVisibility(View.GONE);
                if (TextUtils.isEmpty(photo.getDescription())) {
                    view.findViewById(R.id.image_details).setVisibility(View.GONE);
                }
            }
            if (TextUtils.isEmpty(photo.getDescription())) {
                descriptionText.setVisibility(View.GONE);
            }

            OnImageDisplayedCallback callback = new OnImageDisplayedCallback() {

                @Override
                public void onImageDisplayed(ImageView imageView) {
                    view.findViewById(R.id.loading).setVisibility(View.GONE);
                }
            };
            if (photo.getUrl(PhotosEndlessAdapter.SIZE_BIG) != null) {
                mStorage.displayImageFor(imageView, photo.getUrl(PhotosEndlessAdapter.SIZE_BIG),
                        callback);
            } else {
                new LoadImageTask(photo, imageView, callback).execute();
            }

            ((ViewPager) collection).addView(view, 0);

            return view;
        }

        @Override
        public void destroyItem(View collection, int position, Object view) {
            View theView = (View) view;
            ((ImageView) theView.findViewById(R.id.image)).setImageBitmap(null);
            ((ViewPager) collection).removeView(theView);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == ((View) object);
        }

        @Override
        public void finishUpdate(View arg0) {
        }

        @Override
        public void restoreState(Parcelable arg0, ClassLoader arg1) {
        }

        @Override
        public Parcelable saveState() {
            return null;
        }

        @Override
        public void startUpdate(View arg0) {
        }

    }

    private class PhotosAdapter extends PhotosEndlessAdapter {

        public PhotosAdapter(Context context, ArrayList<Photo> photos) {
            super(context, photos);
        }

        public PhotosAdapter(Context context, ArrayList<Photo> photos, String tags) {
            super(context, photos, tags);
        }

        @Override
        protected void onStartLoading() {
            mActionBar.startLoading();
        }

        @Override
        protected void onStoppedLoading() {
            mActionBar.stopLoading();
        }

        @Override
        public View getView(Photo item, View convertView) {
            return null;
        }

    }

    private class LoadImageTask extends AsyncTask<Void, Void, Bitmap> {
        private Photo mPhoto;
        private final ImageView mImageView;
        private final OnImageDisplayedCallback mCallback;

        public LoadImageTask(Photo photo, ImageView imageView, OnImageDisplayedCallback callback) {
            mPhoto = photo;
            mImageView = imageView;
            mCallback = callback;
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            try {
                PhotoResponse response = Preferences.getApi(PhotoDetailsActivity.this).getPhoto(
                        mPhoto.getId(), new ReturnSizes(PhotosEndlessAdapter.SIZE_BIG));
                mPhoto = response.getPhoto();
                return mStorage.getBitmap(mPhoto.getUrl(PhotosEndlessAdapter.SIZE_BIG));
            } catch (Exception e) {
                Log.e(TAG, "Could not get photo", e);
                Map<String, String> extraData = new HashMap<String, String>();
                extraData.put("message", "Error with load photos");
                BugSenseHandler.log(TAG, extraData, e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap != null) {
                mImageView.setImageBitmap(bitmap);
                if (mCallback != null) {
                    mCallback.onImageDisplayed(mImageView);
                }
            } else {
                Toast.makeText(PhotoDetailsActivity.this, "Error occured", Toast.LENGTH_LONG)
                        .show();
            }
            super.onPostExecute(bitmap);
        }

    }
}
