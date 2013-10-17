
package com.trovebox.android.app;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Activity;
import org.holoeverywhere.app.Fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.ListView;

import com.trovebox.android.app.NavigationHandlerFragment.TitleChangedHandler;
import com.trovebox.android.app.bitmapfun.util.ImageCache;
import com.trovebox.android.app.bitmapfun.util.ImageFetcher;
import com.trovebox.android.app.common.CommonRefreshableFragmentWithImageWorker;
import com.trovebox.android.app.model.Album;
import com.trovebox.android.app.model.Photo;
import com.trovebox.android.app.model.ProfileInformation;
import com.trovebox.android.app.model.ProfileInformation.AccessPermissions;
import com.trovebox.android.app.model.utils.AlbumUtils;
import com.trovebox.android.app.model.utils.PhotoUtils;
import com.trovebox.android.app.model.utils.PhotoUtils.PhotoDeletedHandler;
import com.trovebox.android.app.model.utils.PhotoUtils.PhotoUpdatedHandler;
import com.trovebox.android.app.net.ProfileResponseUtils;
import com.trovebox.android.app.net.ReturnSizes;
import com.trovebox.android.app.service.UploaderServiceUtils.PhotoUploadedHandler;
import com.trovebox.android.app.ui.adapter.PhotosEndlessAdapter;
import com.trovebox.android.app.util.CommonUtils;
import com.trovebox.android.app.util.GuiUtils;
import com.trovebox.android.app.util.ImageFlowUtils;
import com.trovebox.android.app.util.ImageFlowUtils.FlowObjectToStringWrapper;
import com.trovebox.android.app.util.LoadingControl;
import com.trovebox.android.app.util.RunnableWithParameter;
import com.trovebox.android.app.util.TrackerUtils;
import com.trovebox.android.app.util.Utils;

public class GalleryFragment extends CommonRefreshableFragmentWithImageWorker
        implements PhotoDeletedHandler, PhotoUpdatedHandler, PhotoUploadedHandler
{
    public static final String TAG = GalleryFragment.class.getSimpleName();

    public static String EXTRA_TAG = "EXTRA_TAG";
    public static String EXTRA_ALBUM = "EXTRA_ALBUM";

    private LoadingControl loadingControl;
    private StartNowHandler startNowHandler;
    private TitleChangedHandler mTitleChangedHandler;
    private GalleryAdapterExt mAdapter;
    private String mTags;
    private Album mAlbum;
    private boolean mSkipPermissionsCheck;
    private CollaboratorAlbumRunnable mCollaboratorAlbumRunnable;

    private ReturnSizes thumbSize;
    private ReturnSizes returnSizes;

    private int mImageThumbSize;
    private int mImageThumbSpacing;
    private int mImageThumbBorder;
    private int pageSize;

    ListView photosGrid;
    ViewTreeObserver.OnGlobalLayoutListener photosGridListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        pageSize = Utils.isTablet(getActivity()) ? 45
                : PhotosEndlessAdapter.DEFAULT_PAGE_SIZE;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(EXTRA_TAG, mTags);
        outState.putParcelable(EXTRA_ALBUM, mAlbum);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState)
    {
        super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.fragment_gallery, container, false);
        if (savedInstanceState != null)
        {
            mTags = savedInstanceState.getString(EXTRA_TAG);
            mAlbum = savedInstanceState.getParcelable(EXTRA_ALBUM);
        } else
        {
            mTags = null;
            mAlbum = null;
            mSkipPermissionsCheck = false;
        }
        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        refresh();
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        loadingControl = ((LoadingControl) activity);
        startNowHandler = ((StartNowHandler) activity);
        mTitleChangedHandler = ((TitleChangedHandler) activity);

    }

    public Album getAlbum() {
        return mAlbum;
    }

    @Override
    protected void initImageWorker() {
        mImageThumbSize = getResources().getDimensionPixelSize(
                R.dimen.gallery_item_size);
        mImageThumbSpacing = getResources().getDimensionPixelSize(
                R.dimen.gallery_item_spacing);
        mImageThumbBorder = getResources().getDimensionPixelSize(
                R.dimen.gallery_item_border);
        thumbSize = new ReturnSizes(mImageThumbSize * 2, mImageThumbSize * 2);
        returnSizes = PhotosEndlessAdapter.getReturnSizes(thumbSize,
                PhotosEndlessAdapter.getDetailsReturnSizes(getActivity()));
        mImageWorker = new CustomImageFetcher(getActivity(), loadingControl,
                thumbSize.getHeight());
        mImageWorker.setImageCache(ImageCache.findOrCreateCache(getActivity(),
                ImageCache.THUMBS_CACHE_DIR));
    }

    @Override
    public void refresh()
    {
        refresh(getView());
    }

    void refresh(View v)
    {
        if (mTags == null && mAlbum == null)
        {
            Intent intent = getActivity().getIntent();
            mTags = intent != null ? intent
                    .getStringExtra(EXTRA_TAG)
                    : null;
            mAlbum = intent != null ? (Album) intent.getParcelableExtra(EXTRA_ALBUM) : null;
        }
        if (mTags != null || mAlbum != null)
        {
            mAdapter = new GalleryAdapterExt(mTags, mAlbum == null ? null : mAlbum.getId());
            removeTagsAndAlbumInformationFromActivityIntent();
        } else
        {
            if (Preferences.isLimitedAccountAccessType() && !mSkipPermissionsCheck) {
                mAdapter = null;
                mCollaboratorAlbumRunnable = new CollaboratorAlbumRunnable();
                ProfileResponseUtils.runWithProfileInformationAsync(true,
                        mCollaboratorAlbumRunnable, null,
                        loadingControl);
            } else {
                mAdapter = new GalleryAdapterExt();
            }
        }

        photosGrid = (ListView) v.findViewById(R.id.list_photos);
        if (photosGridListener != null) {
            GuiUtils.removeGlobalOnLayoutListener(photosGrid, photosGridListener);
        }
        photosGridListener = new ViewTreeObserver.OnGlobalLayoutListener()
        {
            int lastHeight = 0;

            @Override
            public void onGlobalLayout()
            {
                if (mAdapter != null && (mAdapter.imageFlowUtils.getTotalWidth() !=
                        photosGrid.getWidth() || photosGrid.getHeight() != lastHeight))
                {
                    CommonUtils.debug(TAG, "Reinit grid groups");
                    mAdapter.imageFlowUtils.onGroupsStructureModified();
                    mAdapter.imageFlowUtils.buildGroups(photosGrid.getWidth(),
                            mImageThumbSize, photosGrid.getHeight() - 2
                                    * (mImageThumbBorder
                                    + mImageThumbSpacing), mImageThumbBorder
                                    + mImageThumbSpacing);
                    mAdapter.notifyDataSetChanged();
                    lastHeight = photosGrid.getHeight();
                }
            }
        };
        photosGrid.getViewTreeObserver().addOnGlobalLayoutListener(photosGridListener);
        photosGrid.setAdapter(mAdapter);
    }

    public void cleanRefreshIfFiltered()
    {
        if (mTags != null || mAlbum != null)
        {
            mTags = null;
            mAlbum = null;
            refresh();
        }
    }

    private void removeTagsAndAlbumInformationFromActivityIntent() {
        Intent intent = getActivity().getIntent();
        if (intent != null)
        {
            intent.removeExtra(EXTRA_TAG);
            intent.removeExtra(EXTRA_ALBUM);
        }
    }

    void saveCurrentTagAndAlbumInformationToActivityIntent()
    {
        Intent intent = getActivity().getIntent();
        if (intent == null)
        {
            intent = new Intent();
            getActivity().setIntent(intent);
        }
        intent.putExtra(GalleryFragment.EXTRA_TAG, mTags);
        intent.putExtra(GalleryFragment.EXTRA_ALBUM, mAlbum);
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        if (mAdapter != null)
        {
            mAdapter.forceStopLoadingIfNecessary();
        }
        GuiUtils.removeGlobalOnLayoutListener(photosGrid, photosGridListener);
        if (mCollaboratorAlbumRunnable != null) {
            mCollaboratorAlbumRunnable.cancel();
        }
    }

    @Override
    public void photoDeleted(Photo photo)
    {
        if (mAdapter != null)
        {
            mAdapter.photoDeleted(photo);
        }
    }

    @Override
    public void photoUpdated(Photo photo) {
        if (mAdapter != null)
        {
            mAdapter.photoUpdated(photo);
        }
    }

    /**
     * Process all the images preserving aspect ratio and using same height
     */
    private class CustomImageFetcher extends ImageFetcher
    {

        public CustomImageFetcher(Context context, LoadingControl loadingControl, int size) {
            super(context, loadingControl, size);
        }

        @SuppressWarnings("unchecked")
        @Override
        protected Bitmap processBitmap(Object data)
        {
            FlowObjectToStringWrapper<Photo> fo = (FlowObjectToStringWrapper<Photo>) data;
            Photo imageData = fo.getObject();
            double ratio = imageData.getHeight() == 0 ? 1 : (float) imageData.getWidth()
                    / (float) imageData.getHeight();
            int height = imageHeight;
            int width = (int) (height * ratio);
            Bitmap result = null;
            try
            {
                imageData = PhotoUtils.validateUrlForSizeExistAndReturn(imageData, thumbSize);
                result = super.processBitmap(fo.toString(), width, height);
            } catch (Exception e)
            {
                GuiUtils.noAlertError(TAG, e);
            }
            return result;
        }

    }

    /**
     * Extended adapter which uses photo groups as items instead of Photos
     */
    public class GalleryAdapterExt extends
            GalleryAdapter
    {
        ImageFlowUtils<Photo> imageFlowUtils;

        public GalleryAdapterExt()
        {
            super();
            init();
        }

        public GalleryAdapterExt(String tagFilter, String albumFilter)
        {
            super(tagFilter, albumFilter);
            init();
        }

        void init()
        {
            imageFlowUtils = new ImageFlowUtils<Photo>()
            {

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
                public void additionalSingleImageViewInit(View view,
                        final Photo value) {
                    super.additionalSingleImageViewInit(view, value);
                    ImageView imageView = (ImageView) view.findViewById(R.id.image);
                    imageView.setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            TrackerUtils.trackButtonClickEvent("image", GalleryFragment.this);
                            Intent intent = new Intent(getActivity(), PhotoDetailsActivity.class);
                            intent.putExtra(PhotoDetailsActivity.EXTRA_ADAPTER_PHOTOS,
                                    new PhotosEndlessAdapter.ParametersHolder(mAdapter, value));
                            startActivity(intent);
                            clearImageWorkerCaches(true);
                        }
                    });
                }

                @Override
                public void loadImage(final Photo photo, final ImageView imageView) {
                    PhotoUtils.validateUrlForSizeExistAsyncAndRun(photo, thumbSize,
                            new RunnableWithParameter<Photo>() {

                                @Override
                                public void run(Photo photo) {
                                    FlowObjectToStringWrapper<Photo> fo = new FlowObjectToStringWrapper<Photo>(
                                            photo, photo.getUrl(thumbSize.toString()));
                                    mImageWorker
                                            .loadImage(fo,
                                                    imageView);
                                }
                            }, loadingControl);
                }
            };
        }

        public int getSuperCount()
        {
            return super.getCount();
        }

        Photo getSuperItem(int position)
        {
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
                    imageFlowUtils.getSuperItemPositionForGroupPosition(position),
                    getSuperCount());
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (checkNeedToLoadNextPage(position))
            {
                loadNextPage();
            }
            return imageFlowUtils.getView(position, convertView, parent,
                    R.layout.item_gallery_image_line,
                    R.layout.item_gallery_image,
                    R.id.image, getActivity());
        }

        @Override
        public void notifyDataSetChanged() {
            imageFlowUtils.rebuildGroups();
            super.notifyDataSetChanged();
        }

        @Override
        public boolean areAllItemsEnabled()
        {
            return false;
        }

        @Override
        public boolean isEnabled(int position)
        {
            return false;
        }

        @Override
        public void deleteItemAt(int index) {
            imageFlowUtils.onGroupsStructureModified();
            super.deleteItemAt(index);
        }
    }

    private class GalleryAdapter extends PhotosEndlessAdapter
    {
        public GalleryAdapter()
        {
            this(null, null);
        }

        public GalleryAdapter(String tagFilter, String albumFilter)
        {
            super(getActivity(), pageSize, tagFilter, albumFilter, null, returnSizes);
        }

        @Override
        public View getView(Photo photo, View convertView, ViewGroup parent)
        {
            return null;
        }

        @Override
        protected void onStartLoading()
        {
            loadingControl.startLoading();
        }

        @Override
        protected void onStoppedLoading()
        {
            loadingControl.stopLoading();
        }

        @Override
        public LoadResponse loadItems(int page) {
            LoadResponse result = super.loadItems(page);
            // show start now notification in case response returned no items
            // and there are no already loaded items
            GalleryFragment.showStartNowNotification(startNowHandler,
                    GalleryFragment.this, result.items != null && result.items.isEmpty()
                            && getItems().isEmpty() && mTags == null && mAlbum == null);
            return result;
        }
    }

    @Override
    protected boolean isRefreshMenuVisible() {
        return !loadingControl.isLoading();
    }

    @Override
    public void pageActivated() {
        super.pageActivated();
        if (!isVisible())
        {
            return;
        }
        // if filtering is requested
        if (!refreshOnPageActivated)
        {
            Intent intent = getActivity().getIntent();
            if (intent != null)
            {
                if (intent.hasExtra(EXTRA_ALBUM) || intent.hasExtra(EXTRA_TAG))
                {
                    refresh();
                }
            }
        }
    }

    @Override
    public void pageDeactivated() {
        super.pageDeactivated();
        if (mTags != null || mAlbum != null)
        {
            mTags = null;
            mAlbum = null;
            // we need to schedule refresh when the page will be activated in
            // viewpager to clear filters
            refreshOnPageActivated = true;
        }
        if (mCollaboratorAlbumRunnable != null) {
            mCollaboratorAlbumRunnable.cancel();
        }
    };

    @Override
    public void photoUploaded() {
        refreshImmediatelyOrScheduleIfNecessary();
    }

    /**
     * Adjust start now notification visibility state and init it in case it is
     * visible. When user clicked on int startNowHandler.startNow will be
     * executed
     * 
     * @param startNowHandler
     * @param fragment
     * @param show
     */
    public static void showStartNowNotification(final StartNowHandler startNowHandler,
            final Fragment fragment,
            final boolean show)
    {
        GuiUtils.runOnUiThread(
                new Runnable() {
    
                    @Override
                    public void run() {
                        View view = fragment.getView();
                        if (view != null)
                        {
                            view = view.findViewById(R.id.upload_new_images);
                            if (show)
                            {
                                view.setOnClickListener(new OnClickListener() {
    
                                    @Override
                                    public void onClick(View v) {
                                        startNowHandler.startNow();
                                    }
                                });
                            }
                            view.setVisibility(
                                    show ? View.VISIBLE : View.GONE);
                        }
    
                    }
                });
    }

    class CollaboratorAlbumRunnable implements RunnableWithParameter<ProfileInformation> {

        boolean mCancelled = false;

        @Override
        public void run(ProfileInformation parameter) {
            if (mCancelled) {
                return;
            }
            try {
                ProfileInformation viewer = parameter.getViewer();
                AccessPermissions permissions = viewer == null ? null : viewer.getPermissions();
                if (permissions == null || permissions.isFullCreateAccess()
                        || permissions.getCreateAlbumAccessIds() == null
                        || permissions.getCreateAlbumAccessIds().length == 0) {
                    mSkipPermissionsCheck = true;
                    refresh();
                } else {
                    AlbumUtils.getAlbumAndRunAsync(permissions.getCreateAlbumAccessIds()[0],
                            new RunnableWithParameter<Album>() {

                                @Override
                                public void run(Album parameter) {
                                    if (!mCancelled) {
                                        mAlbum = parameter;
                                        mTitleChangedHandler.titleChanged();
                                        refresh();
                                        cancel();
                                    }
                                }
                            }, new Runnable() {

                                @Override
                                public void run() {
                                    cancel();
                                }
                            }, loadingControl);
                }
            } catch (Exception ex) {
                GuiUtils.error(TAG, ex);
            }

        }

        void cancel() {
            mCancelled = true;
            if (mCollaboratorAlbumRunnable == this) {
                mSkipPermissionsCheck = false;
                mCollaboratorAlbumRunnable = null;
            }
        }
    }
    public static interface StartNowHandler
    {
        void startNow();
    }
}
