
package me.openphoto.android.app;

import me.openphoto.android.app.bitmapfun.util.ImageCache;
import me.openphoto.android.app.bitmapfun.util.ImageFetcher;
import me.openphoto.android.app.model.Photo;
import me.openphoto.android.app.net.ReturnSizes;
import me.openphoto.android.app.ui.adapter.PhotosEndlessAdapter;
import me.openphoto.android.app.util.LoadingControl;
import me.openphoto.android.app.util.Utils;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;

import com.WazaBe.HoloEverywhere.LayoutInflater;
import com.WazaBe.HoloEverywhere.app.Activity;

public class GalleryFragment extends CommonFrargmentWithImageWorker implements Refreshable,
        OnItemClickListener
{
    public static final String TAG = GalleryFragment.class.getSimpleName();

    private static final String IMAGE_CACHE_DIR = SyncImageSelectionFragment.IMAGE_CACHE_DIR;

    public static String EXTRA_TAG = "EXTRA_TAG";
    public static String EXTRA_ALBUM = "EXTRA_ALBUM";

    private LoadingControl loadingControl;
    private GalleryAdapter mAdapter;
    private String mTags;
    private String mAlbum;

    private ReturnSizes returnSizes;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.fragment_gallery, container, false);
        mTags = null;
        mAlbum = null;
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
        mImageWorker = new ImageFetcher(getActivity(), loadingControl, returnSizes.getWidth(),
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
            if (mTags != null || mAlbum != null)
            {
                mAdapter = new GalleryAdapter(mTags, mAlbum);
                getActivity().getIntent().removeExtra(EXTRA_TAG);
                getActivity().getIntent().removeExtra(EXTRA_ALBUM);
            } else
            {
                mAdapter = new GalleryAdapter();
            }
        }

        GridView photosGrid = (GridView) v.findViewById(R.id.grid_photos);
        photosGrid.setAdapter(mAdapter);
        photosGrid.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int position,
            long arg3)
    {
        Intent intent = new Intent(getActivity(), PhotoDetailsActivity.class);
        intent.putParcelableArrayListExtra(
                PhotoDetailsActivity.EXTRA_ADAPTER_PHOTOS,
                mAdapter.getItems());
        intent.putExtra(PhotoDetailsActivity.EXTRA_ADAPTER_POSITION, position);
        intent.putExtra(PhotoDetailsActivity.EXTRA_ADAPTER_TAGS, mTags);
        startActivity(intent);
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        mAdapter.forceStopLoadingIfNecessary();
    }

    private class GalleryAdapter extends PhotosEndlessAdapter
    {
        public GalleryAdapter()
        {
            this(null, null);
        }

        public GalleryAdapter(String tagFilter, String albumFilter)
        {
            super(getActivity(), Utils.isTablet(getActivity()) ? 45
                    : PhotosEndlessAdapter.DEFAULT_PAGE_SIZE, tagFilter, albumFilter);
        }

        @Override
        public View getView(Photo photo, View convertView, ViewGroup parent)
        {
            if (convertView == null)
            {
                final LayoutInflater layoutInflater = (LayoutInflater) getActivity()
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = layoutInflater.inflate(
                        R.layout.item_gallery_image, null);
            }
            ImageView image = (ImageView) convertView.findViewById(R.id.image);
            mImageWorker
                    .loadImage(photo.getUrl(returnSizes.toString()), image);
            return convertView;
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
            if (Preferences.isLoggedIn(getActivity()) && checkOnline())
            {
                return super.loadItems(page);
            } else
            {
                return new LoadResponse(null, false);
            }
        }
    }

}
