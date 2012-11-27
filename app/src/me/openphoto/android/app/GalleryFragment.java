
package me.openphoto.android.app;

import me.openphoto.android.app.bitmapfun.util.ImageCache;
import me.openphoto.android.app.bitmapfun.util.ImageFetcher;
import me.openphoto.android.app.model.Photo;
import me.openphoto.android.app.net.ReturnSizes;
import me.openphoto.android.app.ui.adapter.PhotosEndlessAdapter;
import me.openphoto.android.app.util.CommonUtils;
import me.openphoto.android.app.util.ImageFlowUtils;
import me.openphoto.android.app.util.LoadingControl;
import me.openphoto.android.app.util.Utils;
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

import com.WazaBe.HoloEverywhere.LayoutInflater;
import com.WazaBe.HoloEverywhere.app.Activity;

public class GalleryFragment extends CommonFrargmentWithImageWorker implements Refreshable
{
    public static final String TAG = GalleryFragment.class.getSimpleName();

    private static final String IMAGE_CACHE_DIR = SyncImageSelectionFragment.IMAGE_CACHE_DIR;

    public static String EXTRA_TAG = "EXTRA_TAG";
    public static String EXTRA_ALBUM = "EXTRA_ALBUM";

    private LoadingControl loadingControl;
    private GalleryAdapterExt mAdapter;
    private String mTags;
    private String mAlbum;

    private ReturnSizes returnSizes;

    private int mImageThumbSize;
    private int mImageThumbSpacing;
    private int mImageThumbBorder;
    private int pageSize;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mImageThumbSize = getResources().getDimensionPixelSize(
                R.dimen.gallery_item_size);
        mImageThumbSpacing = getResources().getDimensionPixelSize(
                R.dimen.gallery_item_spacing);
        mImageThumbBorder = getResources().getDimensionPixelSize(
                R.dimen.gallery_item_border);
        pageSize = Utils.isTablet(getActivity()) ? 45
                : PhotosEndlessAdapter.DEFAULT_PAGE_SIZE;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(EXTRA_TAG, mTags);
        outState.putString(EXTRA_ALBUM, mAlbum);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.fragment_gallery, container, false);
        if (savedInstanceState != null)
        {
            mTags = savedInstanceState.getString(EXTRA_TAG);
            mAlbum = savedInstanceState.getString(EXTRA_ALBUM);
        } else
        {
            mTags = null;
            mAlbum = null;
        }
        refresh(v);
        return v;
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        loadingControl = ((LoadingControl) activity);

    }

    @Override
    protected void initImageWorker() {
        returnSizes = PhotosEndlessAdapter.SIZE_SMALL;
        mImageWorker = new CustomImageFetcher(getActivity(), loadingControl,
                returnSizes.getHeight());
        mImageWorker.setImageCache(ImageCache.findOrCreateCache(getActivity(),
                IMAGE_CACHE_DIR));
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
            mAlbum = intent != null ? intent.getStringExtra(EXTRA_ALBUM) : null;
        }
        if (mTags != null || mAlbum != null)
        {
            mAdapter = new GalleryAdapterExt(mTags, mAlbum);
            removeTagsAndAlbumInformationFromActivityIntent();
        } else
        {
            mAdapter = new GalleryAdapterExt();
        }

        final ListView photosGrid = (ListView) v.findViewById(R.id.list_photos);
        photosGrid.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener()
                {
                    int lastHeight = 0;
                    @Override
                    public void onGlobalLayout()
                    {
                        if (mAdapter != null && (mAdapter.imageFlowUtils.getTotalWidth() !=
                                photosGrid.getWidth() || photosGrid.getHeight() != lastHeight))
                        {
                            CommonUtils.debug(TAG, "Reinit grid groups");
                            mAdapter.imageFlowUtils.buildGroups(photosGrid.getWidth(),
                                    mImageThumbSize, photosGrid.getHeight() - 2
                                            * (mImageThumbBorder
                                            + mImageThumbSpacing), mImageThumbBorder
                                            + mImageThumbSpacing);
                            mAdapter.notifyDataSetChanged();
                            lastHeight = photosGrid.getHeight();
                        }
                    }

                });
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
        mAdapter.forceStopLoadingIfNecessary();
    }

    /**
     * Process all the images preserving aspect ratio and using same height
     */
    private class CustomImageFetcher extends ImageFetcher
    {

        public CustomImageFetcher(Context context, LoadingControl loadingControl, int size) {
            super(context, loadingControl, size);
        }

        @Override
        protected Bitmap processBitmap(Object data)
        {
            Photo imageData = (Photo) data;
            double ratio = imageData.getHeight() == 0 ? 1 : (float) imageData.getWidth()
                    / (float) imageData.getHeight();
            int height = mImageHeight;
            int width = (int) (height * ratio);
            return super.processBitmap(imageData.getUrl(returnSizes.toString()), width, height);
        }

    }

    /**
     * Extended adapter which uses photo groups as items instead of Photos
     */
    private class GalleryAdapterExt extends
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
                            Intent intent = new Intent(getActivity(), PhotoDetailsActivity.class);
                            intent.putExtra(PhotoDetailsActivity.EXTRA_ADAPTER_PHOTOS,
                                    new PhotosEndlessAdapter.ParametersHolder(mAdapter, value));
                            startActivity(intent);
                        }
                    });
                }
            };
        }

        int getSuperCount()
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
        public View getView(int position, View convertView, ViewGroup parent) {
            if (position == getCount() - 1) {
                loadNextPage();
            }
            return imageFlowUtils.getView(position, convertView, parent,
                    R.layout.item_gallery_image_line,
                    R.layout.item_gallery_image,
                    R.id.image, mImageWorker, getActivity());
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
    }

    private class GalleryAdapter extends PhotosEndlessAdapter
    {
        public GalleryAdapter()
        {
            this(null, null);
        }

        public GalleryAdapter(String tagFilter, String albumFilter)
        {
            super(getActivity(), pageSize, tagFilter, albumFilter);
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
