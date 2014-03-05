
package com.trovebox.android.common.fragment.gallery;

import java.util.List;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Activity;

import uk.co.senab.photoview.VersionedGestureDetector;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.ListView;

import com.trovebox.android.common.CommonConfigurationUtils;
import com.trovebox.android.common.R;
import com.trovebox.android.common.bitmapfun.util.ImageCache;
import com.trovebox.android.common.bitmapfun.util.ImageFetcher;
import com.trovebox.android.common.fragment.common.CommonRefreshableFragmentWithImageWorker;
import com.trovebox.android.common.model.Album;
import com.trovebox.android.common.model.Photo;
import com.trovebox.android.common.model.utils.PhotoUtils;
import com.trovebox.android.common.model.utils.PhotoUtils.PhotoDeletedHandler;
import com.trovebox.android.common.model.utils.PhotoUtils.PhotoUpdatedHandler;
import com.trovebox.android.common.net.ReturnSizes;
import com.trovebox.android.common.service.UploaderServiceUtils;
import com.trovebox.android.common.service.UploaderServiceUtils.PhotoUploadedHandler;
import com.trovebox.android.common.ui.adapter.PhotosEndlessAdapter;
import com.trovebox.android.common.ui.widget.ScalableListView;
import com.trovebox.android.common.util.CommonUtils;
import com.trovebox.android.common.util.GuiUtils;
import com.trovebox.android.common.util.ImageFlowUtils;
import com.trovebox.android.common.util.ImageFlowUtils.FlowObjectToStringWrapper;
import com.trovebox.android.common.util.LoadingControl;
import com.trovebox.android.common.util.RunnableWithParameter;

/**
 * Common gallery fragment
 * 
 * @author Eugene Popovich
 */
public abstract class GalleryFragment extends CommonRefreshableFragmentWithImageWorker implements
        PhotoDeletedHandler, PhotoUpdatedHandler, PhotoUploadedHandler {
    public static final String TAG = GalleryFragment.class.getSimpleName();

    public static String EXTRA_TAG = CommonConfigurationUtils.getApplicationContext()
            .getPackageName() + ".GALLERY_EXTRA_TAG";
    public static String EXTRA_ALBUM = CommonConfigurationUtils.getApplicationContext()
            .getPackageName() + ".GALLERY_EXTRA_ALBUM";
    public static String EXTRA_TOKEN = CommonConfigurationUtils.getApplicationContext()
            .getPackageName() + ".GALLERY_EXTRA_TOKEN";
    public static String EXTRA_HOST = CommonConfigurationUtils.getApplicationContext()
            .getPackageName() + ".GALLERY_EXTRA_HOST";

    protected LoadingControl loadingControl;
    protected GalleryAdapterExt galleryAdapter;
    protected String currentTags;
    protected Album currentAlbum;
    protected String currentToken;
    protected String currentHost;

    private ReturnSizes thumbSize;
    private ReturnSizes returnSizes;

    private int mImageThumbSize;
    private int mImageThumbSpacing;
    private int mImageThumbBorder;
    private int pageSize;

    ListView photosGrid;
    ViewTreeObserver.OnGlobalLayoutListener photosGridListener;
    PhotosGridOnTouchListener mPhotosGridOnTouchListener;

    boolean mRevalidateRequired = false;

    private int mLayoutId;
    private boolean mCleanCurrentParamsOnDeactivation;
    private String mSortBy;

    /**
     * @param layoutId
     * @param cleanCurrentParamsOnDeactivation
     */
    public GalleryFragment(int layoutId, boolean cleanCurrentParamsOnDeactivation, String sortBy) {
        mLayoutId = layoutId;
        mCleanCurrentParamsOnDeactivation = cleanCurrentParamsOnDeactivation;
        mSortBy = sortBy;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        pageSize = CommonUtils.isTablet(getActivity()) ? 45
                : PhotosEndlessAdapter.DEFAULT_PAGE_SIZE;

        addFragmentLifecycleRegisteredReceiver(UploaderServiceUtils
                .getAndRegisterOnPhotoUploadedActionBroadcastReceiver(TAG, this, getActivity()));
        addFragmentLifecycleRegisteredReceiver(PhotoUtils
                .getAndRegisterOnPhotoDeletedActionBroadcastReceiver(TAG, this, getActivity()));
        addFragmentLifecycleRegisteredReceiver(PhotoUtils
                .getAndRegisterOnPhotoUpdatedActionBroadcastReceiver(TAG, this, getActivity()));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(EXTRA_TAG, currentTags);
        outState.putParcelable(EXTRA_ALBUM, currentAlbum);
        outState.putString(EXTRA_TOKEN, currentToken);
        outState.putString(EXTRA_HOST, currentHost);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(mLayoutId, container, false);
        if (savedInstanceState != null) {
            currentTags = savedInstanceState.getString(EXTRA_TAG);
            currentAlbum = savedInstanceState.getParcelable(EXTRA_ALBUM);
            currentToken = savedInstanceState.getString(EXTRA_TOKEN);
            currentHost = savedInstanceState.getString(EXTRA_HOST);
        }
        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        refresh();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        loadingControl = ((LoadingControl) activity);
    }

    @Override
    public void setImageWorkerExitTaskEarly(boolean exitTaskEarly) {
        super.setImageWorkerExitTaskEarly(exitTaskEarly);
        if (exitTaskEarly) {
            mRevalidateRequired = true;
        } else {
            if (mRevalidateRequired) {
                mRevalidateRequired = false;
                if (galleryAdapter != null) {
                    galleryAdapter.notifyDataSetChanged();
                }
            }
        }
    }

    public Album getAlbum() {
        return currentAlbum;
    }

    @Override
    protected void initImageWorker() {
        mImageThumbSize = getResources().getDimensionPixelSize(R.dimen.gallery_item_size);
        mImageThumbSpacing = getResources().getDimensionPixelSize(R.dimen.gallery_item_spacing);
        mImageThumbBorder = getResources().getDimensionPixelSize(R.dimen.gallery_item_border);
        thumbSize = new ReturnSizes(mImageThumbSize * 2, mImageThumbSize * 2);
        returnSizes = getReturnSizes(thumbSize);
        mImageWorker = new CustomImageFetcher(getActivity(), loadingControl, thumbSize.getHeight());
        mImageWorker.setImageCache(ImageCache.findOrCreateCache(getActivity(),
                ImageCache.THUMBS_CACHE_DIR));
    }

    protected ReturnSizes getReturnSizes(ReturnSizes thumbSize) {
        return thumbSize;
    }

    @Override
    public void refresh() {
        refresh(getView());
    }

    void refresh(View v) {
        mRevalidateRequired = !isResumed();
        if (currentTags != null || currentAlbum != null || currentToken != null
                || currentHost != null) {
            galleryAdapter = new GalleryAdapterExt(currentTags, currentAlbum == null ? null
                    : currentAlbum.getId(), currentToken, mSortBy, currentHost);
        } else {
            galleryAdapter = createGalleryAdapterForNoParams();
        }

        photosGrid = (ListView) v.findViewById(R.id.list_photos);
        if (photosGridListener != null) {
            GuiUtils.removeGlobalOnLayoutListener(photosGrid, photosGridListener);
        }
        photosGridListener = new ViewTreeObserver.OnGlobalLayoutListener() {
            int lastHeight = 0;

            @Override
            public void onGlobalLayout() {
                if (galleryAdapter != null
                        && (galleryAdapter.imageFlowUtils.getTotalWidth() != photosGrid.getWidth() || photosGrid
                                .getHeight() != lastHeight)) {
                    CommonUtils.debug(TAG, "Reinit grid groups");
                    rebuildPhotosGrid();
                    lastHeight = photosGrid.getHeight();
                }
            }
        };
        photosGrid.getViewTreeObserver().addOnGlobalLayoutListener(photosGridListener);
        photosGrid.setAdapter(galleryAdapter);
        mPhotosGridOnTouchListener = new PhotosGridOnTouchListener();
        ((ScalableListView) photosGrid).setVersionedGestureDetector(VersionedGestureDetector
                .newInstance(photosGrid.getContext(), mPhotosGridOnTouchListener));
    }

    protected GalleryAdapterExt createGalleryAdapterForNoParams() {
        return new GalleryAdapterExt();
    }

    public void cleanRefreshIfFiltered() {
        if (currentTags != null || currentAlbum != null || currentToken != null
                || currentHost != null) {
            setCurrentParameters(null, null, null, null);
            refresh();
        }
    }

    public void setCurrentParameters(String tags, Album album, String token, String host) {
        currentTags = tags;
        currentAlbum = album;
        currentToken = token;
        currentHost = host;
    }

    public void reinitFromIntent(Intent intent) {
        setParametersFromIntent(intent);
        refreshIfNewParameters();
    }

    private void setParametersFromIntent(Intent intent) {
        if (intent != null) {
            String tag = intent.getStringExtra(EXTRA_TAG);
            Album album = intent.getParcelableExtra(EXTRA_ALBUM);
            String token = intent.getStringExtra(EXTRA_TOKEN);
            String host = intent.getStringExtra(EXTRA_HOST);
            setCurrentParameters(tag, album, token, host);
        } else {
            setCurrentParameters(null, null, null, null);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (galleryAdapter != null) {
            galleryAdapter.forceStopLoadingIfNecessary();
        }
        GuiUtils.removeGlobalOnLayoutListener(photosGrid, photosGridListener);
    }

    @Override
    public void photoDeleted(Photo photo) {
        if (galleryAdapter != null) {
            galleryAdapter.photoDeleted(photo);
        }
    }

    @Override
    public void photoUpdated(Photo photo) {
        if (galleryAdapter != null) {
            galleryAdapter.photoUpdated(photo);
        }
    }

    @Override
    protected boolean isRefreshMenuVisible() {
        return !loadingControl.isLoading();
    }

    @Override
    public void pageActivated() {
        super.pageActivated();
        if (!isVisible()) {
            return;
        }
        // if filtering is requested
        if (!refreshOnPageActivated) {
            refreshIfNewParameters();
        }
    }

    private void refreshIfNewParameters() {
        if (galleryAdapter != null) {
            if (!(TextUtils.equals(galleryAdapter.getTagFilter(), currentTags)
                    && ((currentAlbum == null && galleryAdapter.getAlbumFilter() == null) || (currentAlbum != null && TextUtils
                            .equals(currentAlbum.getId(), galleryAdapter.getAlbumFilter())))
                    && TextUtils.equals(galleryAdapter.getToken(), currentToken) && TextUtils
                        .equals(galleryAdapter.getHost(), currentHost))) {
                refresh();
            }
        }
    }

    @Override
    public void pageDeactivated() {
        super.pageDeactivated();
        if (mCleanCurrentParamsOnDeactivation
                && (currentTags != null || currentAlbum != null || currentToken != null || currentHost != null)) {
            setCurrentParameters(null, null, null, null);
            // we need to schedule refresh when the page will be activated in
            // viewpager to clear filters
            refreshOnPageActivated = true;
        }
    };

    @Override
    public void photoUploaded() {
        refreshImmediatelyOrScheduleIfNecessary();
    }

    void rebuildPhotosGrid() {
        galleryAdapter.imageFlowUtils.onGroupsStructureModified();
        galleryAdapter.imageFlowUtils.buildGroups(photosGrid.getWidth(),
                (int) (mPhotosGridOnTouchListener.getScaleFactor() * mImageThumbSize),
                photosGrid.getHeight() - 2 * (mImageThumbBorder + mImageThumbSpacing),
                mImageThumbBorder + mImageThumbSpacing);
        galleryAdapter.notifyDataSetChanged();
    }

    protected void additionalSingleImageViewInit(View view, final Photo value) {
    }

    protected void processLoadResponse(List<?> items) {
    }

    /**
     * Process all the images preserving aspect ratio and using same height
     */
    private class CustomImageFetcher extends ImageFetcher {

        public CustomImageFetcher(Context context, LoadingControl loadingControl, int size) {
            super(context, loadingControl, size);
        }

        @SuppressWarnings("unchecked")
        @Override
        protected Bitmap processBitmap(Object data, ProcessingState processingState) {
            FlowObjectToStringWrapper<Photo> fo = (FlowObjectToStringWrapper<Photo>) data;
            Photo imageData = fo.getObject();
            double ratio = imageData.getHeight() == 0 ? 1 : (float) imageData.getWidth()
                    / (float) imageData.getHeight();
            int height = imageHeight;
            int width = (int) (height * ratio);
            Bitmap result = null;
            try {
                imageData = PhotoUtils.validateUrlForSizeExistAndReturn(imageData, thumbSize);
                result = super.processBitmap(fo.toString(), width, height, processingState);
            } catch (Exception e) {
                GuiUtils.noAlertError(TAG, e);
            }
            return result;
        }

    }

    /**
     * Extended adapter which uses photo groups as items instead of Photos
     */
    public class GalleryAdapterExt extends GalleryAdapter {
        ImageFlowUtils<Photo> imageFlowUtils;

        public GalleryAdapterExt() {
            super();
            init();
        }

        public GalleryAdapterExt(String tagFilter, String albumFilter, String token, String sortBy,
                String host) {
            super(tagFilter, albumFilter, token, sortBy, host);
            init();
        }

        void init() {
            imageFlowUtils = new ImageFlowUtils<Photo>() {

                @Override
                public int getHeight(Photo object) {
                    return object.getHeight();
                }

                @Override
                public int getWidth(Photo object) {
                    return object.getWidth();
                }

                @Override
                public int getSuperCount() {
                    return GalleryAdapterExt.this.getSuperCount();
                }

                @Override
                public Photo getSuperItem(int position) {
                    return GalleryAdapterExt.this.getSuperItem(position);
                }

                @Override
                public void additionalSingleImageViewInit(View view, final Photo value) {
                    super.additionalSingleImageViewInit(view, value);
                    GalleryFragment.this.additionalSingleImageViewInit(view, value);
                }

                @Override
                public void loadImage(final Photo photo, final ImageView imageView) {
                    PhotoUtils.validateUrlForSizeExistAsyncAndRun(photo, thumbSize,
                            new RunnableWithParameter<Photo>() {

                                @Override
                                public void run(Photo photo) {
                                    FlowObjectToStringWrapper<Photo> fo = new FlowObjectToStringWrapper<Photo>(
                                            photo, photo.getUrl(thumbSize.toString()));
                                    mImageWorker.loadImage(fo, imageView);
                                }
                            }, new Runnable() {

                                @Override
                                public void run() {
                                    mImageWorker.loadImage(null, imageView);
                                }
                            }, loadingControl);
                }
            };
        }

        public int getSuperCount() {
            return super.getCount();
        }

        Photo getSuperItem(int position) {
            return (Photo) super.getItem(position);
        }

        @Override
        public int getCount() {
            return imageFlowUtils == null ? 0 : imageFlowUtils.getGroupsCount();
        }

        @Override
        public Object getItem(int num) {
            return imageFlowUtils.getGroupItem(num);
        }

        @Override
        public boolean checkNeedToLoadNextPage(int position) {
            // #449 we need to take into account super count and super item
            // position for each group
            return checkNeedToLoadNextPage(
                    imageFlowUtils.getSuperItemPositionForGroupPosition(position), getSuperCount());
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (checkNeedToLoadNextPage(position)) {
                loadNextPage();
            }
            return imageFlowUtils.getView(position, convertView, parent,
                    R.layout.item_gallery_image_line, R.layout.item_gallery_image, R.id.image,
                    getActivity());
        }

        @Override
        public void notifyDataSetChanged() {
            imageFlowUtils.rebuildGroups();
            super.notifyDataSetChanged();
        }

        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }

        @Override
        public boolean isEnabled(int position) {
            return false;
        }

        @Override
        public void deleteItemAt(int index) {
            imageFlowUtils.onGroupsStructureModified();
            super.deleteItemAt(index);
        }
    }

    private class GalleryAdapter extends PhotosEndlessAdapter {
        public GalleryAdapter() {
            this(null, null, null, null, null);
        }

        public GalleryAdapter(String tagFilter, String albumFilter, String token, String sortBy,
                String host) {
            super(getActivity(), pageSize, tagFilter, albumFilter, token, sortBy, returnSizes, host);
        }

        @Override
        public View getView(Photo photo, View convertView, ViewGroup parent) {
            return null;
        }

        @Override
        protected void onStartLoading() {
            loadingControl.startLoading();
        }

        @Override
        protected void onStoppedLoading() {
            loadingControl.stopLoading();
        }

        @Override
        public LoadResponse loadItems(int page) {
            LoadResponse result = super.loadItems(page);
            processLoadResponse(result.items);
            return result;
        }
    }

    class PhotosGridOnTouchListener implements VersionedGestureDetector.OnGestureListener {
        private float mScaleFactor = 1.f;

        @Override
        public void onDrag(float dx, float dy) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onFling(float startX, float startY, float velocityX, float velocityY) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onScale(float scaleFactor, float focusX, float focusY) {
            CommonUtils.debug(TAG,
                    "LibraryListOnTouchListener.onScale: scale: %.2f. fX: %.2f. fY: %.2f",
                    scaleFactor, focusX, focusY);

            final float newScaleFactor = (1.0f + (scaleFactor - 1.0f) / 2) * mScaleFactor;
            CommonUtils.debug(TAG,
                    "LibraryListOnTouchListener.onScale: new scale: %.2f, new height: %d",
                    newScaleFactor, (int) (newScaleFactor * mImageThumbSize));

            if (newScaleFactor >= 0.5f && newScaleFactor <= 5.f && mScaleFactor != newScaleFactor) {
                mScaleFactor = newScaleFactor;
                // Don't let the object get too small or too large.
                rebuildPhotosGrid();
            }
        }

        public float getScaleFactor() {
            return mScaleFactor;
        }
    }
}
