
package com.trovebox.android.app;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Activity;
import org.holoeverywhere.widget.AdapterView;
import org.holoeverywhere.widget.AdapterView.OnItemClickListener;

import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher.OnViewTapListener;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.trovebox.android.app.FacebookFragment.FacebookLoadingControlAccessor;
import com.trovebox.android.app.TwitterFragment.TwitterLoadingControlAccessor;
import com.trovebox.android.app.bitmapfun.util.ImageCache;
import com.trovebox.android.app.bitmapfun.util.ImageCacheUtils;
import com.trovebox.android.app.bitmapfun.util.ImageFetcher;
import com.trovebox.android.app.bitmapfun.util.ImageWorker;
import com.trovebox.android.app.common.CommonActivity;
import com.trovebox.android.app.common.CommonFragmentWithImageWorker;
import com.trovebox.android.app.facebook.FacebookProvider;
import com.trovebox.android.app.facebook.FacebookUtils;
import com.trovebox.android.app.model.Photo;
import com.trovebox.android.app.model.utils.PhotoUtils;
import com.trovebox.android.app.model.utils.PhotoUtils.PhotoDeletedHandler;
import com.trovebox.android.app.model.utils.PhotoUtils.PhotoUpdatedHandler;
import com.trovebox.android.app.net.ReturnSizes;
import com.trovebox.android.app.share.ShareUtils;
import com.trovebox.android.app.share.ShareUtils.TwitterShareRunnable;
import com.trovebox.android.app.twitter.TwitterUtils;
import com.trovebox.android.app.ui.adapter.PhotosEndlessAdapter;
import com.trovebox.android.app.ui.adapter.PhotosEndlessAdapter.DetailsReturnSizes;
import com.trovebox.android.app.ui.adapter.PhotosEndlessAdapter.ParametersHolder;
import com.trovebox.android.app.ui.widget.HorizontalListView;
import com.trovebox.android.app.ui.widget.HorizontalListView.OnDownListener;
import com.trovebox.android.app.ui.widget.PhotoViewHackyViewPager;
import com.trovebox.android.app.ui.widget.YesNoDialogFragment;
import com.trovebox.android.app.ui.widget.YesNoDialogFragment.YesNoButtonPressedHandler;
import com.trovebox.android.app.util.CommonUtils;
import com.trovebox.android.app.util.GuiUtils;
import com.trovebox.android.app.util.LoadingControl;
import com.trovebox.android.app.util.LoadingControlWithCounter;
import com.trovebox.android.app.util.ObjectAccessor;
import com.trovebox.android.app.util.ProgressDialogLoadingControl;
import com.trovebox.android.app.util.RunnableWithParameter;
import com.trovebox.android.app.util.TrackerUtils;

/**
 * The general photo viewing screen
 * 
 * @author pboos
 */
public class PhotoDetailsActivity extends CommonActivity implements TwitterLoadingControlAccessor,
        FacebookLoadingControlAccessor, PhotoDeletedHandler, PhotoUpdatedHandler {

    private static final String TAG = PhotoDetailsActivity.class.getSimpleName();

    public static final String EXTRA_PHOTO = "EXTRA_PHOTO";

    public static final String EXTRA_ADAPTER_PHOTOS = "EXTRA_ADAPTER_PHOTOS";

    public final static int AUTHORIZE_ACTIVITY_REQUEST_CODE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set up activity to go full screen
        getWindow().addFlags(LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        if (savedInstanceState == null)
        {
            getSupportFragmentManager().beginTransaction()
                    .replace(android.R.id.content, new PhotoDetailsUiFragment())
                    .commit();
        }
        addRegisteredReceiver(PhotoUtils.getAndRegisterOnPhotoDeletedActionBroadcastReceiver(
                TAG, this, this));
        addRegisteredReceiver(PhotoUtils.getAndRegisterOnPhotoUpdatedActionBroadcastReceiver(
                TAG, this, this));
        addRegisteredReceiver(ImageCacheUtils.getAndRegisterOnDiskCacheClearedBroadcastReceiver(
                TAG, this));
    }

    PhotoDetailsUiFragment getContentFragment()
    {
        return (PhotoDetailsUiFragment) getSupportFragmentManager().findFragmentById(
                android.R.id.content);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent != null)
        {
            if (intent.getData() != null)
            {
                Uri uri = intent.getData();
                TwitterUtils.verifyOAuthResponse(
                        new ProgressDialogLoadingControl(this, true,
                                false, getString(R.string.share_twitter_verifying_authentication)),
                        this,
                        uri,
                        TwitterUtils.getPhotoDetailsCallbackUrl(this),
                        null);
            }
            getContentFragment().reinitFromIntent(intent);
        }
    }

    @Override
    public LoadingControl getTwitterLoadingControl() {
        return new ProgressDialogLoadingControl(this, true, false, getString(R.string.loading));
    }

    @Override
    public LoadingControl getFacebookLoadingControl() {
        return new ProgressDialogLoadingControl(this, true, false, getString(R.string.loading));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode)
        {
        /*
         * if this is the activity result from authorization flow, do a call
         * back to authorizeCallback Source Tag: login_tag
         */
            case AUTHORIZE_ACTIVITY_REQUEST_CODE: {
                FacebookProvider.getFacebook().authorizeCallback(requestCode,
                        resultCode,
                        data);
                break;
            }
        }
    }

    @Override
    public void photoDeleted(Photo photo) {
        getContentFragment().photoDeleted(photo);
    }

    @Override
    public void photoUpdated(Photo photo) {
        getContentFragment().photoUpdated(photo);
    }

    public static class PhotoDetailsUiFragment extends CommonFragmentWithImageWorker
    {
        static WeakReference<PhotoDetailsUiFragment> currentInstance;
        static ObjectAccessor<PhotoDetailsUiFragment> currentInstanceAccessor = new ObjectAccessor<PhotoDetailsUiFragment>() {
            private static final long serialVersionUID = 1L;

            @Override
            public PhotoDetailsUiFragment run() {
                return currentInstance == null ? null : currentInstance.get();
            }
        };

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
        boolean detailsVisible;
        AtomicBoolean nextPageLoaded = new AtomicBoolean(false);

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            currentInstance = new WeakReference<PhotoDetailsActivity.PhotoDetailsUiFragment>(this);
            setHasOptionsMenu(true);
            mImageThumbWithBorderSize = getResources().getDimensionPixelSize(
                    R.dimen.detail_thumbnail_with_border_size);
        }

        @Override
        public void onDestroy() {
            if (currentInstance != null)
            {
                if (currentInstance.get() == PhotoDetailsUiFragment.this
                        || currentInstance.get() == null)
                {
                    CommonUtils.debug(TAG, "Nullify current instance");
                    currentInstance = null;
                } else
                {
                    CommonUtils.debug(TAG,
                            "Skipped nullify of current instance, such as it is not the same");
                }
            }
            super.onDestroy();
        }

        @Override
        public void onResume() {
            super.onResume();
            FacebookUtils.extendAceessTokenIfNeeded(getActivity());
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            super.onCreateOptionsMenu(menu, inflater);
            inflater.inflate(R.menu.photo_details, menu);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            detailsVisible = true;
            boolean result = true;
            switch (item.getItemId())
            {
                case R.id.menu_delete:
                    TrackerUtils.trackOptionsMenuClickEvent("menu_delete", getSupportActivity());
                    deleteCurrentPhoto();
                    break;
                case R.id.menu_share:
                    TrackerUtils.trackOptionsMenuClickEvent("menu_share", getSupportActivity());
                    break;
                case R.id.menu_share_email:
                    TrackerUtils.trackOptionsMenuClickEvent("menu_share_email",
                            getSupportActivity());
                    confirmPrivatePhotoSharingAndRun(new Runnable() {

                        @Override
                        public void run() {
                            shareActivePhotoViaEMail();
                        }
                    });
                    break;
                case R.id.menu_share_system:
                    TrackerUtils.trackOptionsMenuClickEvent("menu_share_system",
                            getSupportActivity());
                    confirmPrivatePhotoSharingAndRun(new Runnable() {

                        @Override
                        public void run() {
                            shareActivePhotoViaSystem();
                        }
                    });
                    break;
                case R.id.menu_share_twitter:
                    TrackerUtils.trackOptionsMenuClickEvent("menu_share_twitter",
                            getSupportActivity());
                    confirmPrivatePhotoSharingAndRun(new Runnable() {

                        @Override
                        public void run() {
                            shareActivePhotoViaTwitter();
                        }
                    });
                    break;
                case R.id.menu_share_facebook:
                    TrackerUtils.trackOptionsMenuClickEvent("menu_share_facebook",
                            getSupportActivity());
                    confirmPrivatePhotoSharingAndRun(new Runnable() {

                        @Override
                        public void run() {
                            shareActivePhotoViaFacebook();
                        }
                    });
                    break;
                case R.id.menu_edit:
                    TrackerUtils.trackOptionsMenuClickEvent("menu_edit", getSupportActivity());
                    PhotoDetailsEditFragment detailsFragment = new PhotoDetailsEditFragment();
                    detailsFragment.setPhoto(getActivePhoto());
                    detailsFragment.show(getSupportActivity());
                    break;
                default:
                    result = super.onOptionsItemSelected(item);
            }
            return result;
        }

        public void shareActivePhotoViaFacebook() {
            Photo photo = getActivePhoto();
            if (photo != null)
            {
                FacebookUtils.runAfterFacebookAuthentication(getSupportActivity(),
                        AUTHORIZE_ACTIVITY_REQUEST_CODE,
                        new ShareUtils.FacebookShareRunnable(
                                photo, currentInstanceAccessor));
            }
        }

        public void shareActivePhotoViaTwitter() {
            Photo photo = getActivePhoto();
            if (photo != null)
            {
                TwitterUtils.runAfterTwitterAuthentication(
                        new ProgressDialogLoadingControl(getSupportActivity(), true, false,
                                getString(R.string.share_twitter_requesting_authentication)),
                        getSupportActivity(),
                        TwitterUtils.getPhotoDetailsCallbackUrl(getActivity()),
                        new TwitterShareRunnable(photo, currentInstanceAccessor));
            }
        }

        public void shareActivePhotoViaEMail() {
            Photo photo = getActivePhoto();
            if (photo != null)
            {
                ShareUtils.shareViaEMail(photo, getActivity(),
                        new ProgressDialogLoadingControl(
                                getSupportActivity(), true, false,
                                getString(R.string.loading)));
            }
        }

        public void shareActivePhotoViaSystem() {
            Photo photo = getActivePhoto();
            if (photo != null)
            {
                ShareUtils.shareViaSystem(photo, getActivity(),
                        new ProgressDialogLoadingControl(
                                getSupportActivity(), true, false,
                                getString(R.string.loading)));
            }
        }

        Photo getActivePhoto()
        {
            return mAdapter.currentPhoto;
        }

        public void confirmPrivatePhotoSharingAndRun(final Runnable runnable)
        {
            ShareUtils.confirmPrivatePhotoSharingAndRun(getActivePhoto(), runnable,
                    getSupportActivity());
        }

        @Override
        public View onCreateView(LayoutInflater inflater,
                ViewGroup container, Bundle savedInstanceState) {
            super.onCreateView(inflater, container, savedInstanceState);
            View v = inflater.inflate(R.layout.activity_photo_details, container, false);
            init(v, savedInstanceState);
            return v;
        }

        void init(View v, Bundle savedInstanceState)
        {
            titleText = (TextView) v.findViewById(R.id.image_title);
            dateText = (TextView) v.findViewById(R.id.image_date);
            privateBtn = (ImageView) v.findViewById(R.id.button_private);
            detailsView = v.findViewById(R.id.image_details);
            int position = 0;
            if (savedInstanceState != null)
            {
                PhotosEndlessAdapter.ParametersHolder parameters = (ParametersHolder) savedInstanceState
                        .getParcelable(EXTRA_ADAPTER_PHOTOS);
                position = parameters.getPosition();

                thumbnailsAdapter = new ThumbnailsAdapter(parameters);
            } else
            {
                position = initFromIntent(getActivity().getIntent());
            }
            initImageViewers(v, position);
        }

        public void reinitFromIntent(Intent intent)
        {
            int position = initFromIntent(intent);
            if (position != -1)
            {
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
                    if (!detailsVisible)
                    {
                        adjustDetailsVisibility(false);
                    }
                }
            }, 4000);
        }

        public void initThumbnailsList(View v) {
            thumbnailsList = (HorizontalListView) v.findViewById(R.id.thumbs);
            thumbnailsList.setAdapter(thumbnailsAdapter);
            thumbnailsList.setOnItemClickListener(new OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    TrackerUtils.trackButtonClickEvent("thumb", PhotoDetailsUiFragment.this);
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
        protected void initImageWorker()
        {
            DetailsReturnSizes detailReturnSizes = PhotosEndlessAdapter
                    .getDetailsReturnSizes(getActivity());
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
                    ImageCache.THUMBS_CACHE_DIR, false,
                    ImageCache.DEFAULT_MEM_CACHE_SIZE_RATIO * 2));
            mImageWorker2.setLoadingImage(R.drawable.empty_photo);
            imageWorkers.add(mImageWorker2);
        }

        void photoSelected(final Photo photo)
        {
            ActionBar actionBar = ((Activity) getSupportActivity())
                    .getSupportActionBar();
            String title = photo.getTitle();
            if (TextUtils.isEmpty(title))
            {
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

        void deleteCurrentPhoto()
        {
            final Photo photo = mAdapter.currentPhoto;
            YesNoDialogFragment dialogFragment = YesNoDialogFragment
                    .newInstance(R.string.delete_photo_confirmation_question,
                            new YesNoButtonPressedHandler()
                            {
                                @Override
                                public void yesButtonPressed(
                                        DialogInterface dialog)
                                {
                                    final ProgressDialogLoadingControl loadingControl = new ProgressDialogLoadingControl(
                                            getActivity(), true, false,
                                            getString(R.string.deleting_photo_message)
                                            );
                                    PhotoUtils.deletePhoto(photo,
                                            loadingControl);
                                }

                                @Override
                                public void noButtonPressed(
                                        DialogInterface dialog)
                                {
                                    // DO NOTHING
                                }
                            });
            dialogFragment.show(getSupportActivity());
        }

        void photoDeleted(Photo photo)
        {
            if (thumbnailsAdapter != null)
            {
                thumbnailsAdapter.photoDeleted(photo);
                if (thumbnailsAdapter.getCount() == 0)
                {
                    getActivity().finish();
                }
            }
        }

        void photoUpdated(Photo photo)
        {
            if (thumbnailsAdapter != null)
            {
                thumbnailsAdapter.photoUpdated(photo);
            }
        }

        void adjustDetailsVisibility(final boolean visible)
        {
            detailsVisible = visible;
            if (getActivity() == null)
            {
                return;
            }
            Animation animation = AnimationUtils
                    .loadAnimation(
                            getActivity(),
                            visible ? android.R.anim.fade_in : android.R.anim.fade_out);
            long animationDuration = 500;
            animation
                    .setDuration(animationDuration);
            thumbnailsList.startAnimation(animation);
            detailsView.startAnimation(animation);
            thumbnailsList.postDelayed(new Runnable()
            {
                @Override
                public void run()
                {
                    detailsVisible = visible;
                    thumbnailsList.setVisibility(detailsVisible ? View.VISIBLE : View.GONE);
                    detailsView.setVisibility(detailsVisible ? View.VISIBLE : View.GONE);
                    if (detailsVisible && nextPageLoaded.getAndSet(false))
                    {
                        ensureThumbVisible(getActivePhoto());
                    }
                }
            }, animationDuration);
            ActionBar actionBar = ((Activity) getSupportActivity())
                    .getSupportActionBar();
            if (visible)
            {
                actionBar.show();
            } else
            {
                actionBar.hide();
            }
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
                mInflator = (LayoutInflater) getActivity()
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            }

            @Override
            public int getCount() {
                return mAdapter.getCount();
            }

            @Override
            public Object instantiateItem(View collection, int position) {
                if (mAdapter.checkNeedToLoadNextPage(position))
                {
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
                        try
                        {
                            view.findViewById(R.id.loading).setVisibility(View.GONE);
                        } catch (Exception ex)
                        {
                            GuiUtils.noAlertError(TAG, null, ex);
                        }
                    }

                    @Override
                    public void startLoadingEx() {
                        try
                        {
                            view.findViewById(R.id.loading).setVisibility(View.VISIBLE);
                        } catch (Exception ex)
                        {
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
                                try
                                {
                                    if (getView() != null
                                            && getView().getWindowToken() != null)
                                    {
                                        mImageWorker.loadImage(url, imageView, loadingControl);
                                    }
                                } catch (Exception ex)
                                {
                                    GuiUtils.noAlertError(TAG, ex);
                                }
                            }
                        }, loadingControl);

                loadingControl.stopLoading();

                imageView.setOnViewTapListener(new OnViewTapListener() {

                    @Override
                    public void onViewTap(View view, float x, float y) {
                        TrackerUtils.trackButtonClickEvent("image", PhotoDetailsUiFragment.this);
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
                if (isAdded())
                {
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
                if (position < mAdapter.getCount())
                {
                    Photo photo = (Photo) mAdapter.getItem(position);
                    if (photo != currentPhoto)
                    {
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

        private class ThumbnailsAdapter extends PhotosEndlessAdapter
        {
            public ThumbnailsAdapter(ArrayList<Photo> photos)
            {
                super(getActivity(), photos, returnSizes);
            }

            public ThumbnailsAdapter(PhotosEndlessAdapter.ParametersHolder parameters)
            {
                super(getActivity(), parameters, returnSizes);
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
            protected void onStartLoading()
            {
                // loadingControl.startLoading();
            }

            @Override
            protected void onStoppedLoading()
            {
                // loadingControl.stopLoading();
            }

        }
    }

}
