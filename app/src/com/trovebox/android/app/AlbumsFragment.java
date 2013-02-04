
package com.trovebox.android.app;


import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Activity;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.trovebox.android.app.bitmapfun.util.ImageCache;
import com.trovebox.android.app.bitmapfun.util.ImageFetcher;
import com.trovebox.android.app.common.CommonFrargmentWithImageWorker;
import com.trovebox.android.app.model.Album;
import com.trovebox.android.app.net.AlbumsResponse;
import com.trovebox.android.app.net.ITroveboxApi;
import com.trovebox.android.app.net.Paging;
import com.trovebox.android.app.net.ReturnSizes;
import com.trovebox.android.app.net.TroveboxResponseUtils;
import com.trovebox.android.app.ui.adapter.EndlessAdapter;
import com.trovebox.android.app.util.CommonUtils;
import com.trovebox.android.app.util.GalleryOpenControl;
import com.trovebox.android.app.util.GuiUtils;
import com.trovebox.android.app.util.LoadingControl;
import com.trovebox.android.app.util.TrackerUtils;

/**
 * The fragment which displays albums list
 * 
 * @author Eugene Popovich
 */
public class AlbumsFragment extends CommonFrargmentWithImageWorker implements
        OnItemClickListener
{
    public static final String TAG = AlbumsFragment.class.getSimpleName();

    private LoadingControl loadingControl;
    private GalleryOpenControl galleryOpenControl;

    private AlbumsAdapter mAdapter;

    private ReturnSizes thumbSize;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.fragment_albums, container, false);

        mAdapter = new AlbumsAdapter();
        ListView list = (ListView) v.findViewById(R.id.list_albums);
        list.setAdapter(mAdapter);
        list.setOnItemClickListener(this);

        return v;
    }
    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        loadingControl = ((LoadingControl) activity);
        galleryOpenControl = ((GalleryOpenControl) activity);

    }

    @Override
    protected void initImageWorker() {
        int mImageThumbSize = getResources().getDimensionPixelSize(
                R.dimen.album_item_size);
        thumbSize = new ReturnSizes(mImageThumbSize, mImageThumbSize, true);
        mImageWorker = new ImageFetcher(getActivity(), loadingControl, thumbSize.getWidth(),
                thumbSize.getHeight());
        mImageWorker.setImageCache(ImageCache.findOrCreateCache(getActivity(),
                ImageCache.THUMBS_CACHE_DIR));
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view,
            int position, long id)
    {
        TrackerUtils.trackButtonClickEvent("album_item", AlbumsFragment.this);
        Album album = (Album) mAdapter.getItem(position);
        galleryOpenControl.openGallery(null, album.getId());
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        mAdapter.forceStopLoadingIfNecessary();
    }

    private class AlbumsAdapter extends EndlessAdapter<Album>
    {
        public static final int DEFAULT_PAGE_SIZE = 20;
        private final ITroveboxApi mTroveboxApi;

        public AlbumsAdapter()
        {
            this(DEFAULT_PAGE_SIZE);
        }

        public AlbumsAdapter(int pageSize)
        {
            super(pageSize);
            mTroveboxApi = Preferences.getApi(getActivity());
            loadFirstPage();
        }

        @Override
        public long getItemId(int position)
        {
            // return ((Album) getItem(position)).getAlbum().hashCode();
            return position;
        }

        @Override
        public View getView(Album album, View convertView, ViewGroup parent)
        {
            if (convertView == null)
            {
                final LayoutInflater layoutInflater = (LayoutInflater) getActivity()
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = layoutInflater.inflate(R.layout.list_item_album,
                        null);
            }
            ((TextView) convertView.findViewById(R.id.text_name))
                    .setText(album
                            .getName());
            ((TextView) convertView.findViewById(R.id.text_count))
                    .setText(Integer.toString(album
                            .getCount()));
            ImageView image = (ImageView) convertView.findViewById(R.id.cover);
            if (album.getCover() != null)
            {
                mImageWorker
                        .loadImage(album.getCover().getUrl(thumbSize.toString()), image);
            } else
            {
                image.setImageBitmap(null);
            }
            return convertView;
        }

        @Override
        public LoadResponse loadItems(int page)
        {
            if (CommonUtils.checkLoggedInAndOnline())
            {
                try
                {
                    AlbumsResponse response = mTroveboxApi.getAlbums(new Paging(page,
                            getPageSize()));
                    if (TroveboxResponseUtils.checkResponseValid(response))
                    {
                        return new LoadResponse(response.getAlbums(), response.hasNextPage());
                    }
                } catch (Exception e)
                {
                    GuiUtils.error(
                            TAG,
                            R.string.errorCouldNotLoadNextAlbumsInList,
                            e);
                }
            }
            return new LoadResponse(null, false);
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
    }

}
