
package me.openphoto.android.app;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import me.openphoto.android.app.FacebookFragment.FacebookLoadingControlAccessor;
import me.openphoto.android.app.TwitterFragment.TwitterLoadingControlAccessor;
import me.openphoto.android.app.bitmapfun.util.ImageCache;
import me.openphoto.android.app.bitmapfun.util.ImageFetcher;
import me.openphoto.android.app.bitmapfun.util.ImageWorker;
import me.openphoto.android.app.facebook.FacebookProvider;
import me.openphoto.android.app.facebook.FacebookUtils;
import me.openphoto.android.app.model.Photo;
import me.openphoto.android.app.model.utils.PhotoUtils;
import me.openphoto.android.app.net.ReturnSizes;
import me.openphoto.android.app.share.ShareUtils;
import me.openphoto.android.app.share.ShareUtils.TwitterShareRunnable;
import me.openphoto.android.app.twitter.TwitterUtils;
import me.openphoto.android.app.ui.adapter.PhotosEndlessAdapter;
import me.openphoto.android.app.ui.adapter.PhotosEndlessAdapter.ParametersHolder;
import me.openphoto.android.app.ui.widget.HorizontalListView;
import me.openphoto.android.app.ui.widget.ViewPagerWithDisableSupport;
import me.openphoto.android.app.ui.widget.ViewPagerWithDisableSupport.GesturesEnabledHandler;
import me.openphoto.android.app.ui.widget.YesNoDialogFragment;
import me.openphoto.android.app.ui.widget.YesNoDialogFragment.YesNoButtonPressedHandler;
import me.openphoto.android.app.util.CommonUtils;
import me.openphoto.android.app.util.GuiUtils;
import me.openphoto.android.app.util.LoadingControl;
import me.openphoto.android.app.util.ProgressDialogLoadingControl;
import me.openphoto.android.app.util.RunnableWithParameter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.WazaBe.HoloEverywhere.LayoutInflater;
import com.WazaBe.HoloEverywhere.sherlock.SActivity;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.facebook.android.R;
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
public class PhotoDetailsActivity extends SActivity implements TwitterLoadingControlAccessor,
        FacebookLoadingControlAccessor {

    public static final String EXTRA_PHOTO = "EXTRA_PHOTO";

    public static final String EXTRA_ADAPTER_PHOTOS = "EXTRA_ADAPTER_PHOTOS";

    public final static int AUTHORIZE_ACTIVITY_RESULT_CODE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set up activity to go full screen
        getWindow().addFlags(LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        if (savedInstanceState == null)
        {
            getSupportFragmentManager().beginTransaction()
                    .replace(android.R.id.content, new UiFragment())
                    .commit();
        }
    }

    UiFragment getContentFragment()
    {
        return (UiFragment) getSupportFragmentManager().findFragmentById(android.R.id.content);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.photo_details, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        UiFragment fragment = getContentFragment();
        fragment.detailsVisible = true;
        boolean result = true;
        switch (item.getItemId())
        {
            case R.id.menu_delete:
                fragment.deleteCurrentPhoto();
                break;
            case R.id.menu_share:
                Photo photo = fragment.getActivePhoto();
                boolean isPrivate = photo == null || photo.isPrivate();
                item.getSubMenu().setGroupVisible(R.id.share_group, !isPrivate);
                if (isPrivate)
                {
                    GuiUtils.alert(R.string.share_private_photo_forbidden);
                    result = false;
                }
                break;
            case R.id.menu_share_email:
                fragment.shareActivePhotoViaEMail();
                break;
            case R.id.menu_share_twitter:
                fragment.shareActivePhotoViaTwitter();
                break;
            case R.id.menu_share_facebook:
                fragment.shareActivePhotoViaFacebook();
                break;
            default:
                result = super.onOptionsItemSelected(item);
        }
        return result;
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
                        TwitterUtils.getSecondCallbackUrl(this),
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
        switch (requestCode)
        {
        /*
         * if this is the activity result from authorization flow, do a call
         * back to authorizeCallback Source Tag: login_tag
         */
            case AUTHORIZE_ACTIVITY_RESULT_CODE: {
                FacebookProvider.getFacebook().authorizeCallback(requestCode,
                        resultCode,
                        data);
                break;
            }
        }
    }

    public static class UiFragment extends CommonFrargmentWithImageWorker
    {
        private static final String TAG = PhotoDetailsActivity.class.getSimpleName();

        private static final String IMAGE_CACHE_DIR = HomeFragment.IMAGE_CACHE_DIR;
        private static final String IMAGE_CACHE_DIR2 = SyncImageSelectionFragment.IMAGE_CACHE_DIR;

        static UiFragment currentInstance;
        static FragmentAccessor<UiFragment> currentInstanceAccessor = new FragmentAccessor<UiFragment>() {
            private static final long serialVersionUID = 1L;

            @Override
            public UiFragment run() {
                return currentInstance;
            }
        };

        private ViewPagerWithDisableSupport mViewPager;
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

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            currentInstance = this;
            mImageThumbWithBorderSize = getResources().getDimensionPixelSize(
                    R.dimen.detail_thumbnail_with_border_size);
        }

        @Override
        public void onDestroy() {
            currentInstance = null;
            super.onDestroy();
        }

        @Override
        public void onResume() {
            super.onResume();
            FacebookUtils.extendAceessTokenIfNeeded(getActivity());
        }

        public void shareActivePhotoViaFacebook() {
            Photo photo = getActivePhoto();
            if (photo != null)
            {
                FacebookUtils.runAfterFacebookAuthentication(getActivity(),
                        new ShareUtils.FacebookShareRunnable(
                                photo, currentInstanceAccessor));
            }
        }

        public void shareActivePhotoViaTwitter() {
            Photo photo = getActivePhoto();
            if (photo != null)
            {
                TwitterUtils.runAfterTwitterAuthentication(
                        new ProgressDialogLoadingControl(getActivity(), true, false,
                                getString(R.string.share_twitter_requesting_authentication)),
                        getActivity(),
                        TwitterUtils.getSecondCallbackUrl(getActivity()),
                        new TwitterShareRunnable(photo, currentInstanceAccessor));
            }
        }

        public void shareActivePhotoViaEMail() {
            Photo photo = getActivePhoto();
            if (photo != null)
            {
                ShareUtils.shareViaEMail(photo, getActivity());
            }
        }

        Photo getActivePhoto()
        {
            return mAdapter.currentPhoto;
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

        @Override
        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
            outState.putParcelable(EXTRA_ADAPTER_PHOTOS, new ParametersHolder(thumbnailsAdapter,
                    mAdapter.currentPhoto));
        }

        @Override
        protected void initImageWorker()
        {
            int imageThumbnailSize = getResources().getDimensionPixelSize(
                    R.dimen.detail_thumbnail_size);
            bigPhotoSize = PhotosEndlessAdapter.getBigImageSize(getActivity());
            thumbSize = new ReturnSizes(imageThumbnailSize, imageThumbnailSize, true);
            returnSizes = PhotosEndlessAdapter.getReturnSizes(thumbSize, bigPhotoSize);
            final DisplayMetrics displaymetrics = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay()
                    .getMetrics(displaymetrics);
            final int height = displaymetrics.heightPixels;
            final int width = displaymetrics.widthPixels;
            final int longest = height > width ? height : width;
            mImageWorker = new ImageFetcher(getActivity(), null, longest);
            mImageWorker.setImageCache(ImageCache.findOrCreateCache(getActivity(),
                    IMAGE_CACHE_DIR));
            mImageWorker.setImageFadeIn(false);
            mImageWorker2 = new ImageFetcher(getActivity(), null, thumbSize.getWidth(),
                    thumbSize.getHeight());
            mImageWorker2.setImageCache(ImageCache.findOrCreateCache(getActivity(),
                    IMAGE_CACHE_DIR2));
            mImageWorker2.setLoadingImage(R.drawable.empty_photo);
            imageWorkers.add(mImageWorker2);
        }

        void photoSelected(final Photo photo)
        {
            ActionBar actionBar = ((SActivity) getSupportActivity())
                    .getSupportActionBar();
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            String title = photo.getTitle();
            if (TextUtils.isEmpty(title))
            {
                title = photo.getFilenameOriginal();
            }
            actionBar.setTitle(getString(R.string.details_title_and_date_header, title,
                    df.format(photo.getDateTaken())));

            titleText.setText(title);

            dateText.setText(df.format(photo.getDateTaken()));

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
                                            new RunnableWithParameter<Boolean>() {

                                                @Override
                                                public void run(Boolean parameter) {
                                                    if (parameter.booleanValue())
                                                    {
                                                        if (thumbnailsAdapter.getCount() == 1)
                                                        {
                                                            getActivity().finish();
                                                        } else
                                                        {
                                                            int index = thumbnailsAdapter
                                                                    .itemIndex(photo);
                                                            if (index != -1)
                                                            {
                                                                thumbnailsAdapter
                                                                        .deleteItemAtAndLoadOneMoreItem(index);
                                                            }
                                                        }
                                                    } else
                                                    {
                                                        // DO NOTHING
                                                    }
                                                }
                                            }, loadingControl);
                                }

                                @Override
                                public void noButtonPressed(
                                        DialogInterface dialog)
                                {
                                    // DO NOTHING
                                }
                            });
            dialogFragment.replace(getSupportFragmentManager());
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
                    thumbnailsList.setVisibility(visible ? View.VISIBLE : View.GONE);
                    detailsView.setVisibility(visible ? View.VISIBLE : View.GONE);
                }
            }, animationDuration);
            ActionBar actionBar = ((SActivity) getSupportActivity())
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

                final LoadingControl loadingControl = new LoadingControl() {
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
                // Finally load the image asynchronously into the ImageView,
                // this
                // also takes care of
                // setting a placeholder image while the background thread runs
                PhotoUtils.validateUrlForSizeExistAsyncAndRun(photo, bigPhotoSize,
                        new RunnableWithParameter<Photo>() {

                            @Override
                            public void run(Photo photo) {
                                String url = photo.getUrl(bigPhotoSize.toString());
                                mImageWorker.loadImage(url, imageView, loadingControl);
                            }
                        }, loadingControl);

                loadingControl.stopLoading();

                imageView.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        CommonUtils.debug(TAG, "ImageView on click");
                        adjustDetailsVisibility(!detailsVisible);
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

                final ImageView imageView = (ImageView) view.findViewById(R.id.image);
                imageView.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        CommonUtils.debug(TAG, "Thumb clicked.");
                        detailsVisible = true;
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

                // Finally load the image asynchronously into the ImageView,
                // this
                // also takes care of
                // setting a placeholder image while the background thread runs
                PhotoUtils.validateUrlForSizeExistAsyncAndRun(photo, thumbSize,
                        new RunnableWithParameter<Photo>() {

                            @Override
                            public void run(Photo photo) {
                                String url = photo.getUrl(thumbSize.toString());
                                mImageWorker2.loadImage(url, imageView, null);
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

            @Override
            public LoadResponse loadItems(
                    int page)
            {
                if (CommonUtils.checkLoggedInAndOnline())
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
