
package me.openphoto.android.app;

import java.util.ArrayList;

import me.openphoto.android.app.bitmapfun.util.ImageCache;
import me.openphoto.android.app.bitmapfun.util.ImageFetcher;
import me.openphoto.android.app.bitmapfun.util.ImageWorker;
import me.openphoto.android.app.model.Photo;
import me.openphoto.android.app.net.PhotoResponse;
import me.openphoto.android.app.net.ReturnSizes;
import me.openphoto.android.app.ui.adapter.PhotosEndlessAdapter;
import me.openphoto.android.app.ui.adapter.PhotosEndlessAdapter.ParametersHolder;
import me.openphoto.android.app.ui.widget.HorizontalListView;
import me.openphoto.android.app.ui.widget.ViewPagerWithDisableSupport;
import me.openphoto.android.app.ui.widget.ViewPagerWithDisableSupport.GesturesEnabledHandler;
import me.openphoto.android.app.util.CommonUtils;
import me.openphoto.android.app.util.GuiUtils;
import me.openphoto.android.app.util.LoadingControl;
import me.openphoto.android.app.util.concurrent.AsyncTaskEx;
import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null)
        {
            getSupportFragmentManager().beginTransaction()
                    .replace(android.R.id.content, new UiFragment())
                    .commit();
        }
    }

    public static class UiFragment extends CommonFrargmentWithImageWorker
    {
        private static final String TAG = PhotoDetailsActivity.class.getSimpleName();

        private static final String IMAGE_CACHE_DIR = HomeFragment.IMAGE_CACHE_DIR;
        private static final String IMAGE_CACHE_DIR2 = SyncImageSelectionFragment.IMAGE_CACHE_DIR;

        private ViewPagerWithDisableSupport mViewPager;
        private HorizontalListView thumbnailsList;

        private PhotoDetailPagerAdapter mAdapter;
        private ThumbnailsAdapter thumbnailsAdapter;

        private ImageWorker mImageWorker2;

        private ReturnSizes returnSizes;
        private ReturnSizes returnSizes2;

        private int mImageThumbWithBorderSize;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mImageThumbWithBorderSize = getResources().getDimensionPixelSize(
                    R.dimen.detail_thumbnail_with_border_size);
        }

        @Override
        public View onCreateView(LayoutInflater inflater,
                ViewGroup container, Bundle savedInstanceState) {
            super.onCreateView(inflater, container, savedInstanceState);
            View v = inflater.inflate(R.layout.activity_photo_details, container, false);
            return v;
        }

        @Override
        public void onViewCreated(View view) {
            super.onViewCreated(view);
            init(view);
        }

        void init(View v)
        {
            Intent intent = getActivity().getIntent();
            int position = 0;
            if (intent.hasExtra(EXTRA_PHOTO)) {
                Photo photo = intent.getParcelableExtra(EXTRA_PHOTO);
                ArrayList<Photo> photos = new ArrayList<Photo>();
                photos.add(photo);
                thumbnailsAdapter = new ThumbnailsAdapter(photos);
            } else if (getActivity().getIntent().hasExtra(EXTRA_ADAPTER_PHOTOS)) {
                PhotosEndlessAdapter.ParametersHolder parameters = (ParametersHolder) intent
                        .getParcelableExtra(EXTRA_ADAPTER_PHOTOS);
                position = parameters.getPosition();

                thumbnailsAdapter = new ThumbnailsAdapter(parameters);
            }
            mAdapter = new PhotoDetailPagerAdapter(thumbnailsAdapter);
            thumbnailsList = (HorizontalListView) v.findViewById(R.id.thumbs);
            thumbnailsList.setAdapter(thumbnailsAdapter);
            mViewPager = (ViewPagerWithDisableSupport) v.findViewById(R.id.photos);
            mViewPager.setAdapter(mAdapter);
            mViewPager.setGesturesEnabledHandler(new GesturesEnabledHandler() {

                @Override
                public boolean isEnabled() {
                    return !mAdapter.isCurrentImageZoomed();
                }
            });

            if (position > 0) {
                mViewPager.setCurrentItem(position);
                final int pos = position;
                thumbnailsList.postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        ensureThumbVisible((Photo) mAdapter.mAdapter.getItem(pos));
                    }
                }, 100);
            }
        }

        @Override
        protected void initImageWorker()
        {
            returnSizes = PhotosEndlessAdapter.SIZE_BIG;
            returnSizes2 = PhotosEndlessAdapter.SIZE_SMALL;
            mImageWorker = new ImageFetcher(getActivity(), null, returnSizes.getWidth(),
                    returnSizes.getHeight());
            mImageWorker.setImageCache(ImageCache.findOrCreateCache(getActivity(),
                    IMAGE_CACHE_DIR));
            mImageWorker.setImageFadeIn(false);
            mImageWorker2 = new ImageFetcher(getActivity(), null, returnSizes2.getWidth(),
                    returnSizes2.getHeight());
            mImageWorker2.setImageCache(ImageCache.findOrCreateCache(getActivity(),
                    IMAGE_CACHE_DIR2));
            mImageWorker2.setLoadingImage(R.drawable.empty_photo);
            imageWorkers.add(mImageWorker2);
        }

        /**
         * Ensure photo thumb is visible
         * 
         * @param photo
         */
        void ensureThumbVisible(final Photo photo)
        {
            thumbnailsList.post(new Runnable() {

                @Override
                public void run() {
                    int startX = thumbnailsList.getStartX();
                    int position = -1;
                    int count = 0;
                    for (Photo p : thumbnailsAdapter.getItems())
                    {
                        if (p.getId().equals(photo.getId()))
                        {
                            position = count;
                            break;
                        }
                        count++;
                    }
                    int offset = position * mImageThumbWithBorderSize;
                    int width = thumbnailsList.getWidth();
                    // mImageThumbWithBorderSize, middle);
                    CommonUtils.debug(TAG, "offset: " + offset + "; width: " + width + "; startX: "
                            + startX);
                    if (offset < startX + mImageThumbWithBorderSize)
                    {
                        CommonUtils.debug(TAG, "Thumbnail is on the left, need to scroll left");
                        thumbnailsList.scrollTo(offset
                                - Math.min(mImageThumbWithBorderSize,
                                        (width - mImageThumbWithBorderSize) / 2));
                    } else if (offset > startX + width - 2 * mImageThumbWithBorderSize)
                    {
                        CommonUtils.debug(TAG, "Thumbnail is on the right, need to scroll right");
                        thumbnailsList.scrollTo(offset
                                - width
                                + Math.min(2 * mImageThumbWithBorderSize,
                                        (width + mImageThumbWithBorderSize) / 2));
                    } else
                    {
                        CommonUtils.debug(TAG,
                                "Thumbnail is already visible. Only invalidating view.");
                    }
                    for (int i = 0, size = thumbnailsList.getChildCount(); i < size; i++)
                    {
                        View view = thumbnailsList.getChildAt(i);
                        invalidateSelection(view);
                    }
                }
            });
        }

        void invalidateSelection(View view)
        {
            View border = view.findViewById(R.id.background_container);
            Photo photo = (Photo) border.getTag();
            border.setBackgroundResource(isSelected(photo) ?
                    R.color.detail_thumb_selected_border :
                    R.color.detail_thumb_unselected_border);
        }

        boolean isSelected(Photo photo)
        {
            if (mAdapter != null)
            {
                Photo selectedPhoto = mAdapter.currentPhoto;
                boolean result = selectedPhoto != null
                        && selectedPhoto.getId().equals(photo.getId());
                CommonUtils.debug(TAG, "Is selected: " + result);
                return result;
            }
            return false;
        }

        private class PhotoDetailPagerAdapter extends PagerAdapter {

            private final LayoutInflater mInflator;
            private final ThumbnailsAdapter mAdapter;
            private View mCurrentView;
            private Photo currentPhoto;
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

            public PhotoDetailPagerAdapter(ThumbnailsAdapter adatper) {
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
                // if (getCount() > 1 && position > getCount() - 6) {
                // mAdapter.loadNextPage();
                // }
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
                loadingControl.startLoading();
                String url = photo.getUrl(returnSizes.toString());
                if (url != null) {
                    mImageWorker.loadImage(url, imageView, loadingControl);
                } else {
                    new LoadImageTask(photo, imageView, returnSizes, mImageWorker, loadingControl)
                            .execute();
                }
                loadingControl.stopLoading();

                imageView.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        CommonUtils.debug(TAG, "ImageView on click");
                        if (thumbnailsList.getVisibility() == View.VISIBLE)
                        {
                            thumbnailsList.setVisibility(View.GONE);
                        } else
                        {
                            thumbnailsList.setVisibility(View.VISIBLE);
                        }
                    }
                });

                ((ViewPager) collection).addView(view, 0);

                return view;
            }

            @Override
            public void destroyItem(View collection, int position, Object view) {
                View theView = (View) view;
                ImageView imageView = (ImageView) theView.findViewById(R.id.image);
                ImageWorker.cancelWork(imageView);
                imageView.setImageBitmap(null);
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
                currentPhoto = (Photo) mAdapter.getItem(position);
                ensureThumbVisible(currentPhoto);
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

        private class LoadImageTask extends AsyncTaskEx<Void, Void, Boolean> {
            private Photo mPhoto;
            private final ImageView mImageView;
            private final LoadingControl loadingControl;
            ReturnSizes returnSizes;
            ImageWorker imageWorker;

            public LoadImageTask(Photo photo,
                    ImageView imageView,
                    ReturnSizes returnSizes,
                    ImageWorker imageWorker,
                    LoadingControl loadingControl) {
                mPhoto = photo;
                mImageView = imageView;
                this.loadingControl = loadingControl;
                this.returnSizes = returnSizes;
                this.imageWorker = imageWorker;
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                if (loadingControl != null)
                {
                    loadingControl.startLoading();
                }
            }

            @Override
            protected Boolean doInBackground(Void... params) {
                try {
                    PhotoResponse response = Preferences.getApi(getActivity())
                            .getPhoto(
                                    mPhoto.getId(), returnSizes);
                    Photo mPhoto2 = response.getPhoto();
                    String size = returnSizes.toString();
                    mPhoto.putUrl(size, mPhoto2.getUrl(size));
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
                    imageWorker.loadImage(url, mImageView, loadingControl);
                }
                if (loadingControl != null)
                {
                    loadingControl.stopLoading();
                }
            }

        }

        private class ThumbnailsAdapter extends PhotosEndlessAdapter
        {
            public ThumbnailsAdapter(ArrayList<Photo> photos)
            {
                super(getActivity(), photos);
            }

            public ThumbnailsAdapter(PhotosEndlessAdapter.ParametersHolder parameters)
            {
                super(getActivity(), parameters);
                itemsBeforeLoadNextPage = 5;
            }

            @Override
            public View getView(final Photo photo, View convertView, ViewGroup parent)
            {
                View view;
                if (convertView == null)
                { // if it's not recycled, instantiate and initialize
                    view = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.item_details_thumb_image, null);
                }
                else
                { // Otherwise re-use the converted view
                    view = convertView;
                }

                ImageView imageView = (ImageView) view.findViewById(R.id.image);
                imageView.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        CommonUtils.debug(TAG, "Thumb clicked.");
                        int count = 0;
                        for (Photo p : mAdapter.mAdapter.getItems())
                        {
                            if (p.getId().equals(photo.getId()))
                            {
                                mViewPager.setCurrentItem(count);
                                break;
                            }
                            count++;
                        }
                    }
                });
                View border = view.findViewById(R.id.background_container);
                border.setTag(photo);
                invalidateSelection(view);

                String url = photo.getUrl(returnSizes2.toString());
                // Finally load the image asynchronously into the ImageView,
                // this
                // also takes care of
                // setting a placeholder image while the background thread runs
                if (url != null) {
                    mImageWorker2.loadImage(url, imageView, null);
                } else {
                    new LoadImageTask(photo, imageView, returnSizes2, mImageWorker2, null)
                            .execute();
                }
                return view;
            }

            @Override
            protected void onStartLoading()
            {
                // loadingControl.startLoading();
            }

            @Override
            protected void onStoppedLoading()
            {
                // loadingControl.stopLoading();
            }

            @Override
            public LoadResponse loadItems(
                    int page)
            {
                if (checkLoggedInAndOnline())
                {
                    return super.loadItems(page);
                } else
                {
                    return new LoadResponse(null, false);
                }
            }
        }
    }
}
