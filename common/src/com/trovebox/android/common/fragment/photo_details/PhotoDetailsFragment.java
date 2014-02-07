package com.trovebox.android.common.fragment.photo_details;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Activity;
import org.holoeverywhere.widget.AdapterView;
import org.holoeverywhere.widget.AdapterView.OnItemClickListener;

import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher.OnViewTapListener;
import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.trovebox.android.common.R;
import com.trovebox.android.common.bitmapfun.util.ImageCache;
import com.trovebox.android.common.bitmapfun.util.ImageFetcher;
import com.trovebox.android.common.bitmapfun.util.ImageWorker;
import com.trovebox.android.common.fragment.common.CommonFragmentWithImageWorker;
import com.trovebox.android.common.model.Photo;
import com.trovebox.android.common.model.utils.PhotoUtils;
import com.trovebox.android.common.model.utils.PhotoUtils.PhotoDeletedHandler;
import com.trovebox.android.common.model.utils.PhotoUtils.PhotoUpdatedHandler;
import com.trovebox.android.common.net.ReturnSizes;
import com.trovebox.android.common.ui.adapter.PhotosEndlessAdapter;
import com.trovebox.android.common.ui.adapter.PhotosEndlessAdapter.ParametersHolder;
import com.trovebox.android.common.ui.widget.HorizontalListView;
import com.trovebox.android.common.ui.widget.HorizontalListView.OnDownListener;
import com.trovebox.android.common.ui.widget.PhotoViewHackyViewPager;
import com.trovebox.android.common.util.CommonUtils;
import com.trovebox.android.common.util.GuiUtils;
import com.trovebox.android.common.util.LoadingControl;
import com.trovebox.android.common.util.LoadingControlWithCounter;
import com.trovebox.android.common.util.RunnableWithParameter;
import com.trovebox.android.common.util.TrackerUtils;

public class PhotoDetailsFragment extends CommonFragmentWithImageWorker implements
        PhotoDeletedHandler, PhotoUpdatedHandler {

    public static final String TAG = PhotoDetailsFragment.class.getSimpleName();

    public static final String EXTRA_PHOTO = "EXTRA_PHOTO";

    public static final String EXTRA_PHOTOS = "EXTRA_PHOTOS";

    public static final String EXTRA_ADAPTER_PHOTOS = "EXTRA_ADAPTER_PHOTOS";

    private PhotoViewHackyViewPager mViewPager;
    private HorizontalListView thumbnailsList;

    private PhotoDetailPagerAdapter mAdapter;
    private ThumbnailsAdapter thumbnailsAdapter;

    private ImageWorker mImageWorker2;

    private ReturnSizes bigPhotoSize;
    private ReturnSizes thumbSize;
    private ReturnSizes returnSizes;

    private int mImageThumbWithBorderSize;

    TextView titleText;
    TextView dateText;
    ImageView privateBtn;
    View detailsView;
    protected boolean detailsVisible;
    AtomicBoolean nextPageLoaded = new AtomicBoolean(false);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mImageThumbWithBorderSize = getResources().getDimensionPixelSize(
                R.dimen.detail_thumbnail_border)
                + getResources().getDimensionPixelSize(R.dimen.detail_thumbnail_spacing);
        mImageThumbWithBorderSize = 2 * mImageThumbWithBorderSize
                + getResources().getDimensionPixelSize(R.dimen.detail_thumbnail_size);
        addFragmentLifecycleRegisteredReceiver(PhotoUtils
                .getAndRegisterOnPhotoDeletedActionBroadcastReceiver(TAG, this, getActivity()));
        addFragmentLifecycleRegisteredReceiver(PhotoUtils
                .getAndRegisterOnPhotoUpdatedActionBroadcastReceiver(TAG, this, getActivity()));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.fragment_photo_details, container, false);
        init(v, savedInstanceState);
        return v;
    }

    void init(View v, Bundle savedInstanceState) {
        titleText = (TextView) v.findViewById(R.id.image_title);
        dateText = (TextView) v.findViewById(R.id.image_date);
        privateBtn = (ImageView) v.findViewById(R.id.button_private);
        detailsView = v.findViewById(R.id.image_details);
        int position = 0;
        if (savedInstanceState != null) {
            PhotosEndlessAdapter.ParametersHolder parameters = (ParametersHolder) savedInstanceState
                    .getParcelable(EXTRA_ADAPTER_PHOTOS);
            position = parameters.getPosition();

            thumbnailsAdapter = new ThumbnailsAdapter(parameters);
        } else {
            position = initFromIntent(getActivity().getIntent());
        }
        initImageViewers(v, position);
    }

    public void reinitFromIntent(Intent intent) {
        int position = initFromIntent(intent);
        if (position != -1) {
            initImageViewers(getView(), position);
        }
    }

    public int initFromIntent(Intent intent) {
        int position = -1;
        if (intent.hasExtra(EXTRA_PHOTO)) {
            Photo photo = intent.getParcelableExtra(EXTRA_PHOTO);
            ArrayList<Photo> photos = new ArrayList<Photo>();
            photos.add(photo);
            thumbnailsAdapter = new ThumbnailsAdapter(photos);
            position = 0;
        } else if (intent.hasExtra(EXTRA_PHOTOS)) {
            ArrayList<Photo> photos = intent.getParcelableArrayListExtra(EXTRA_PHOTOS);
            thumbnailsAdapter = new ThumbnailsAdapter(photos);
            position = 0;
        } else if (intent.hasExtra(EXTRA_ADAPTER_PHOTOS)) {
            PhotosEndlessAdapter.ParametersHolder parameters = (ParametersHolder) intent
                    .getParcelableExtra(EXTRA_ADAPTER_PHOTOS);
            position = parameters.getPosition();

            thumbnailsAdapter = new ThumbnailsAdapter(parameters);
        }
        return position;
    }

    public void initImageViewers(View v, int position) {
        mAdapter = new PhotoDetailPagerAdapter(thumbnailsAdapter);
        initThumbnailsList(v);
        mViewPager = (PhotoViewHackyViewPager) v.findViewById(R.id.photos);
        mViewPager.setAdapter(mAdapter);

        if (position > 0) {
            mViewPager.setCurrentItem(position);
        }
        mViewPager.postDelayed(new Runnable() {

            @Override
            public void run() {
                if (!detailsVisible) {
                    adjustDetailsVisibility(false);
                }
            }
        }, 4000);
    }

    public void initThumbnailsList(View v) {
        thumbnailsList = (HorizontalListView) v.findViewById(R.id.thumbs);
        LayoutParams params = thumbnailsList.getLayoutParams();
        params.height = mImageThumbWithBorderSize;
        thumbnailsList.setLayoutParams(params);
        thumbnailsList.setAdapter(thumbnailsAdapter);
        thumbnailsList.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TrackerUtils.trackButtonClickEvent("thumb", PhotoDetailsFragment.this);
                CommonUtils.debug(TAG, "Thumb clicked.");
                detailsVisible = true;
                mViewPager.setCurrentItem(position);
            }
        });
        thumbnailsList.setOnDownListener(new OnDownListener() {
            @Override
            public void onDown(MotionEvent e) {
                CommonUtils.debug(TAG, "Thumbnails List onDown");
                detailsVisible = true;
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(EXTRA_ADAPTER_PHOTOS, new ParametersHolder(thumbnailsAdapter,
                mAdapter.currentPhoto));
    }

    @Override
    protected void initImageWorker() {
        DetailsReturnSizes detailReturnSizes = getDetailsReturnSizes(getActivity());
        bigPhotoSize = detailReturnSizes.detailsBigPhotoSize;
        thumbSize = detailReturnSizes.detailsThumbSize;
        returnSizes = PhotosEndlessAdapter.getReturnSizes(thumbSize, bigPhotoSize);

        mImageWorker = new ImageFetcher(getActivity(), null, bigPhotoSize.getWidth(),
                bigPhotoSize.getHeight());
        mImageWorker.setImageCache(ImageCache.findOrCreateCache(getActivity(),
                ImageCache.LARGE_IMAGES_CACHE_DIR, false,
                ImageCache.DEFAULT_MEM_CACHE_SIZE_RATIO * 2));
        mImageWorker.setImageFadeIn(false);
        mImageWorker2 = new ImageFetcher(getActivity(), null, thumbSize.getWidth(),
                thumbSize.getHeight());
        mImageWorker2.setImageCache(ImageCache.findOrCreateCache(getActivity(),
                ImageCache.THUMBS_CACHE_DIR, false, ImageCache.DEFAULT_MEM_CACHE_SIZE_RATIO * 2));
        mImageWorker2.setLoadingImage(R.drawable.empty_photo);
        imageWorkers.add(mImageWorker2);
    }

    void photoSelected(final Photo photo) {
        ActionBar actionBar = ((Activity) getSupportActivity()).getSupportActionBar();
        String title = photo.getTitle();
        if (TextUtils.isEmpty(title)) {
            title = photo.getFilenameOriginal();
        }
        actionBar.setTitle(getString(R.string.details_title_and_date_header, title,
                CommonUtils.formatDateTime(photo.getDateTaken())));

        titleText.setText(title);

        dateText.setText(CommonUtils.formatDateTime(photo.getDateTaken()));

        privateBtn.setVisibility(photo.isPrivate() ? View.VISIBLE : View.GONE);

        ensureThumbVisible(photo);
    }

    /**
     * Ensure photo thumb is visible
     * 
     * @param photo
     */
    public void ensureThumbVisible(final Photo photo) {
        thumbnailsList.post(new Runnable() {

            @Override
            public void run() {
                int startX = thumbnailsList.getStartX();
                int position = -1;
                int count = 0;
                for (Photo p : thumbnailsAdapter.getItems()) {
                    if (p.getId().equals(photo.getId())) {
                        position = count;
                        break;
                    }
                    count++;
                }
                int offset = position * mImageThumbWithBorderSize;
                int width = thumbnailsList.getWidth();
                // mImageThumbWithBorderSize, middle);
                CommonUtils
                        .debug(TAG,
                                "ensureThumbVisible: offset: %1$d; position %2$d; thumbWithBorderSize: %3$d; width: %4$d; startX: %5$d",
                                offset, position, mImageThumbWithBorderSize, width, startX);
                if (offset < startX + mImageThumbWithBorderSize) {
                    CommonUtils.debug(TAG,
                            "ensureThumbVisible: Thumbnail is on the left, need to scroll left");
                    thumbnailsList.scrollTo(offset
                            - Math.min(mImageThumbWithBorderSize,
                                    (width - mImageThumbWithBorderSize) / 2));
                } else if (offset > startX + width - 2 * mImageThumbWithBorderSize) {
                    CommonUtils.debug(TAG,
                            "ensureThumbVisible: Thumbnail is on the right, need to scroll right");
                    thumbnailsList.scrollTo(offset
                            - width
                            + Math.min(2 * mImageThumbWithBorderSize,
                                    (width + mImageThumbWithBorderSize) / 2));
                } else {
                    CommonUtils
                            .debug(TAG,
                                    "ensureThumbVisible: Thumbnail is already visible. Only invalidating view.");
                }
                for (int i = 0, size = thumbnailsList.getChildCount(); i < size; i++) {
                    View view = thumbnailsList.getChildAt(i);
                    invalidateSelection(view);
                }
            }
        });
    }

    void invalidateSelection(View view) {
        View border = view.findViewById(R.id.background_container);
        Photo photo = (Photo) border.getTag();
        border.setBackgroundResource(isSelected(photo) ? R.color.detail_thumb_selected_border
                : R.color.detail_thumb_unselected_border);
    }

    boolean isSelected(Photo photo) {
        if (mAdapter != null) {
            Photo selectedPhoto = mAdapter.currentPhoto;
            boolean result = selectedPhoto != null && selectedPhoto.getId().equals(photo.getId());
            CommonUtils.debug(TAG, "Is selected: " + result);
            return result;
        }
        return false;
    }

    @Override
    public void photoDeleted(Photo photo) {
        if (thumbnailsAdapter != null) {
            thumbnailsAdapter.photoDeleted(photo);
            if (thumbnailsAdapter.getCount() == 0) {
                getActivity().finish();
            }
        }
    }

    @Override
    public void photoUpdated(Photo photo) {
        if (thumbnailsAdapter != null) {
            thumbnailsAdapter.photoUpdated(photo);
        }
    }

    protected Photo getActivePhoto() {
        return mAdapter.currentPhoto;
    }

    void adjustDetailsVisibility(final boolean visible) {
        detailsVisible = visible;
        if (getActivity() == null) {
            return;
        }
        Animation animation = AnimationUtils.loadAnimation(getActivity(),
                visible ? android.R.anim.fade_in : android.R.anim.fade_out);
        long animationDuration = 500;
        animation.setDuration(animationDuration);
        thumbnailsList.startAnimation(animation);
        detailsView.startAnimation(animation);
        thumbnailsList.postDelayed(new Runnable() {
            @Override
            public void run() {
                detailsVisible = visible;
                thumbnailsList.setVisibility(detailsVisible ? View.VISIBLE : View.GONE);
                detailsView.setVisibility(detailsVisible ? View.VISIBLE : View.GONE);
                if (detailsVisible && nextPageLoaded.getAndSet(false)) {
                    ensureThumbVisible(getActivePhoto());
                }
            }
        }, animationDuration);
        ActionBar actionBar = ((Activity) getSupportActivity()).getSupportActionBar();
        if (visible) {
            actionBar.show();
        } else {
            actionBar.hide();
        }
    }

    /**
     * Get the return sizes by clonning returnSize and adding detailsReturnSizes
     * fields as a childs
     * 
     * @param returnSize
     * @param detailsReturnSizes
     * @return
     */
    public static ReturnSizes getReturnSizes(ReturnSizes returnSize,
            DetailsReturnSizes detailsReturnSizes) {
        return PhotosEndlessAdapter.getReturnSizes(returnSize,
                detailsReturnSizes.detailsBigPhotoSize, detailsReturnSizes.detailsThumbSize);
    }

    /**
     * Get the return sizes for the gallery activity
     * 
     * @param activity
     * @return
     */
    public static DetailsReturnSizes getDetailsReturnSizes(android.app.Activity activity) {
        DetailsReturnSizes result = new DetailsReturnSizes();

        int detailsThumbnailSize = activity.getResources().getDimensionPixelSize(
                R.dimen.detail_thumbnail_size);
        result.detailsThumbSize = new ReturnSizes(detailsThumbnailSize, detailsThumbnailSize, true);
        result.detailsBigPhotoSize = getBigImageSize(activity);

        return result;
    }

    /**
     * Get the big image size which depends on the screen dimension
     * 
     * @param activity
     * @return
     */
    public static ReturnSizes getBigImageSize(android.app.Activity activity) {
        final DisplayMetrics displaymetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        final int height = displaymetrics.heightPixels;
        final int width = displaymetrics.widthPixels;
        final int longest = height > width ? height : width;
        ReturnSizes bigSize = new ReturnSizes(longest, longest);
        return bigSize;
    }

    public static class DetailsReturnSizes {
        public ReturnSizes detailsThumbSize;
        public ReturnSizes detailsBigPhotoSize;
    }

    private class PhotoDetailPagerAdapter extends PagerAdapter {

        private final LayoutInflater mInflator;
        private final ThumbnailsAdapter mAdapter;
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
            mInflator = (LayoutInflater) getActivity().getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return mAdapter.getCount();
        }

        @Override
        public Object instantiateItem(View collection, int position) {
            if (mAdapter.checkNeedToLoadNextPage(position)) {
                mAdapter.loadNextPage();
                nextPageLoaded.set(true);
            }
            Photo photo = (Photo) mAdapter.getItem(position);

            final View view = mInflator.inflate(R.layout.item_photo_detail,
                    ((ViewPager) collection), false);
            final PhotoView imageView = (PhotoView) view.findViewById(R.id.image);

            final LoadingControl loadingControl = new LoadingControlWithCounter() {

                @Override
                public void stopLoadingEx() {
                    try {
                        view.findViewById(R.id.loading).setVisibility(View.GONE);
                    } catch (Exception ex) {
                        GuiUtils.noAlertError(TAG, null, ex);
                    }
                }

                @Override
                public void startLoadingEx() {
                    try {
                        view.findViewById(R.id.loading).setVisibility(View.VISIBLE);
                    } catch (Exception ex) {
                        GuiUtils.noAlertError(TAG, null, ex);
                    }
                }
            };
            loadingControl.startLoading();
            // Finally load the image asynchronously into the ImageView,
            // this
            // also takes care of
            // setting a placeholder image while the background thread runs
            PhotoUtils.validateUrlForSizeExistAsyncAndRun(photo, bigPhotoSize,
                    new RunnableWithParameter<Photo>() {

                        @Override
                        public void run(Photo photo) {
                            String url = photo.getUrl(bigPhotoSize.toString());
                            // #417 workaround.
                            // TODO remove try/catch if exception will not
                            // appear anymore
                            try {
                                if (getView() != null && getView().getWindowToken() != null) {
                                    mImageWorker.loadImage(url, imageView, loadingControl);
                                }
                            } catch (Exception ex) {
                                GuiUtils.noAlertError(TAG, ex);
                            }
                        }
                    }, loadingControl);

            loadingControl.stopLoading();

            imageView.setOnViewTapListener(new OnViewTapListener() {

                @Override
                public void onViewTap(View view, float x, float y) {
                    TrackerUtils.trackButtonClickEvent("image", PhotoDetailsFragment.this);
                    adjustDetailsVisibility(!detailsVisible);
                }
            });
            // imageView.setOnClickListener(new OnClickListener() {
            //
            // @Override
            // public void onClick(View v) {
            // adjustDetailsVisibility(!detailsVisible);
            // }
            // });

            ((ViewPager) collection).addView(view, 0);

            return view;
        }

        @Override
        public void destroyItem(View collection, int position, Object view) {
            View theView = (View) view;
            ImageView imageView = (ImageView) theView.findViewById(R.id.image);
            ImageWorker.cancelWork(imageView);
            if (isAdded()) {
                imageView.setImageBitmap(null);
            }
            ((ViewPager) collection).removeView(theView);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == ((View) object);
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            super.setPrimaryItem(container, position, object);
            if (position < mAdapter.getCount()) {
                Photo photo = (Photo) mAdapter.getItem(position);
                if (photo != currentPhoto) {
                    currentPhoto = photo;
                    photoSelected(currentPhoto);
                }
            }
        }

        /**
         * Hack to refresh ViewPager when data set notification event is
         * received http://stackoverflow.com/a/7287121/527759
         */
        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }
    }

    private class ThumbnailsAdapter extends PhotosEndlessAdapter {
        public ThumbnailsAdapter(ArrayList<Photo> photos) {
            super(getActivity(), photos, returnSizes);
        }

        public ThumbnailsAdapter(PhotosEndlessAdapter.ParametersHolder parameters) {
            super(getActivity(), parameters, returnSizes);
        }

        @Override
        public View getView(final Photo photo, View convertView, ViewGroup parent) {
            View view;
            if (convertView == null) { // if it's not recycled, instantiate and
                                       // initialize
                view = LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.item_details_thumb_image, null);
            } else { // Otherwise re-use the converted view
                view = convertView;
            }

            final ImageView imageView = (ImageView) view.findViewById(R.id.image);
            View border = view.findViewById(R.id.background_container);
            border.setTag(photo);
            invalidateSelection(view);

            // Finally load the image asynchronously into the ImageView,
            // this
            // also takes care of
            // setting a placeholder image while the background thread runs
            PhotoUtils.validateUrlForSizeExistAsyncAndRun(photo, thumbSize,
                    new RunnableWithParameter<Photo>() {

                        @Override
                        public void run(Photo photo) {
                            String url = photo.getUrl(thumbSize.toString());
                            mImageWorker2.loadImage(url, imageView);
                        }
                    }, null);
            return view;
        }

        @Override
        protected void onStartLoading() {
            // loadingControl.startLoading();
        }

        @Override
        protected void onStoppedLoading() {
            // loadingControl.stopLoading();
        }

    }
}
