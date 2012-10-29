
package me.openphoto.android.app;

import java.util.ArrayList;

import me.openphoto.android.app.bitmapfun.util.ImageCache;
import me.openphoto.android.app.bitmapfun.util.ImageFetcher;
import me.openphoto.android.app.bitmapfun.util.ImageWorker;
import me.openphoto.android.app.model.Photo;
import me.openphoto.android.app.net.PhotoResponse;
import me.openphoto.android.app.net.ReturnSizes;
import me.openphoto.android.app.ui.adapter.PhotosEndlessAdapter;
import me.openphoto.android.app.ui.widget.ViewPagerWithDisableSupport;
import me.openphoto.android.app.ui.widget.ViewPagerWithDisableSupport.GesturesEnabledHandler;
import me.openphoto.android.app.util.GuiUtils;
import me.openphoto.android.app.util.LoadingControl;
import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.WazaBe.HoloEverywhere.LayoutInflater;
import com.WazaBe.HoloEverywhere.sherlock.SActivity;
import com.polites.android.GestureImageView;

/**
 * The general photo viewing screen
 * 
 * @author pboos
 * @version 05.10.2012 <br>
 *          - removed action bar reference <br>
 *          - removed custom onClick listener from the ImageView
 *          <p>
 *          03.10.2012 <br>
 *          - added initial support for album photos filter
 */
public class PhotoDetailsActivity extends SActivity {

    public static final String EXTRA_PHOTO = "EXTRA_PHOTO";

    public static final String EXTRA_ADAPTER_PHOTOS = "EXTRA_ADAPTER_PHOTOS";
    public static final String EXTRA_ADAPTER_POSITION = "EXTRA_ADAPTER_POSITION";
    public static final String EXTRA_ADAPTER_TAGS = "EXTRA_ADAPTER_TAGS";
    public static final String EXTRA_ADAPTER_ALBUM = "EXTRA_ADAPTER_ALBUM";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, new UiFragment())
                .commit();
    }

    public static class UiFragment extends CommonFrargmentWithImageWorker
    {
        private static final String TAG = PhotoDetailsActivity.class.getSimpleName();

        private static final String IMAGE_CACHE_DIR = HomeFragment.IMAGE_CACHE_DIR;

        private ViewPagerWithDisableSupport mViewPager;

        private PhotoDetailPagerAdapter mAdapter;

        private ImageWorker mImageWorker;

        private ReturnSizes returnSizes;

        @Override
        public View onCreateView(LayoutInflater inflater,
                ViewGroup container, Bundle savedInstanceState) {
            super.onCreateView(inflater, container, savedInstanceState);
            View v = inflater.inflate(R.layout.activity_photo_details, container, false);
            init(v);
            return v;
        }

        void init(View v)
        {
            initImageWorker();

            Intent intent = getActivity().getIntent();
            if (intent.hasExtra(EXTRA_PHOTO)) {
                Photo photo = intent.getParcelableExtra(EXTRA_PHOTO);
                ArrayList<Photo> photos = new ArrayList<Photo>();
                photos.add(photo);
                mAdapter = new PhotoDetailPagerAdapter(new PhotosAdapter(getActivity(), photos));
            } else if (getActivity().getIntent().hasExtra(EXTRA_ADAPTER_PHOTOS)) {
                ArrayList<Photo> photos = intent.getParcelableArrayListExtra(
                        EXTRA_ADAPTER_PHOTOS);
                String tags = intent.getStringExtra(EXTRA_ADAPTER_TAGS);
                String album = intent.getStringExtra(EXTRA_ADAPTER_ALBUM);
                mAdapter = new PhotoDetailPagerAdapter(new PhotosAdapter(getActivity(),
                        photos, tags, album));
            }

            mViewPager = (ViewPagerWithDisableSupport) v.findViewById(R.id.photos);
            mViewPager.setAdapter(mAdapter);
            mViewPager.setGesturesEnabledHandler(new GesturesEnabledHandler() {

                @Override
                public boolean isEnabled() {
                    return !mAdapter.isCurrentImageZoomed();
                }
            });

            int position = intent.getIntExtra(EXTRA_ADAPTER_POSITION, 0);
            if (position > 0) {
                mViewPager.setCurrentItem(position);
            }
        }

        @Override
        protected void initImageWorker()
        {
            returnSizes = PhotosEndlessAdapter.SIZE_BIG;
            mImageWorker = new ImageFetcher(getActivity(), null, returnSizes.getWidth(),
                    returnSizes.getHeight());
            mImageWorker.setImageCache(ImageCache.findOrCreateCache(getActivity(),
                    IMAGE_CACHE_DIR));
            mImageWorker.setImageFadeIn(false);
        }

        private class PhotoDetailPagerAdapter extends PagerAdapter {

            private final LayoutInflater mInflator;
            private final PhotosAdapter mAdapter;
            private View mCurrentView;
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
                mInflator = (LayoutInflater) getActivity()
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

                LoadingControl loadingControl = new LoadingControl() {
                    private int mLoaders = 0;

                    @Override
                    public void stopLoading() {
                        if (--mLoaders == 0)
                        {
                            try
                            {
                                view.findViewById(R.id.loading).setVisibility(View.GONE);
                            } catch (Exception ex)
                            {
                                GuiUtils.noAlertError(TAG, null, ex);
                            }
                        }
                    }

                    @Override
                    public void startLoading() {
                        if (mLoaders++ == 0)
                        {
                            try
                            {
                                view.findViewById(R.id.loading).setVisibility(View.VISIBLE);
                            } catch (Exception ex)
                            {
                                GuiUtils.noAlertError(TAG, null, ex);
                            }
                        }
                    }
                };
                String url = photo.getUrl(returnSizes.toString());
                if (url != null) {
                    mImageWorker.loadImage(url, imageView, loadingControl);
                } else {
                    new LoadImageTask(photo, imageView, loadingControl).execute();
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

            @Override
            public void setPrimaryItem(ViewGroup container, int position, Object object) {
                super.setPrimaryItem(container, position, object);
                mCurrentView = (View) object;
            }

            boolean isCurrentImageZoomed()
            {
                if (mCurrentView != null)
                {
                    GestureImageView giv = (GestureImageView)
                            mCurrentView.findViewById(R.id.image);
                    return giv.isZoomed();
                }
                return false;
            }
        }

        private class PhotosAdapter extends PhotosEndlessAdapter {

            public PhotosAdapter(Context context, ArrayList<Photo> photos) {
                super(context, photos);
            }

            public PhotosAdapter(Context context, ArrayList<Photo> photos,
                    String tags,
                    String album)
            {
                super(context, photos, tags, album);
            }

            @Override
            protected void onStartLoading() {
            }

            @Override
            protected void onStoppedLoading() {
            }

            @Override
            public View getView(Photo item, View convertView, ViewGroup parent) {
                return null;
            }

        }

        private class LoadImageTask extends AsyncTask<Void, Void, Boolean> {
            private Photo mPhoto;
            private final ImageView mImageView;
            private final LoadingControl loadingControl;

            public LoadImageTask(Photo photo, ImageView imageView, LoadingControl loadingControl) {
                mPhoto = photo;
                mImageView = imageView;
                this.loadingControl = loadingControl;
            }
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loadingControl.startLoading();
            }

            @Override
            protected Boolean doInBackground(Void... params) {
                try {
                    PhotoResponse response = Preferences.getApi(getActivity())
                            .getPhoto(
                                    mPhoto.getId(), returnSizes);
                    mPhoto = response.getPhoto();
                    return true;
                } catch (Exception e) {
                    GuiUtils.error(TAG, R.string.errorCouldNotGetPhoto, e);
                }
                return false;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                super.onPostExecute(result);
                if (result.booleanValue())
                {
                    String url = mPhoto.getUrl(returnSizes.toString());
                    mImageWorker.loadImage(url, mImageView, loadingControl);
                }
                loadingControl.stopLoading();
            }

        }

    }
}
