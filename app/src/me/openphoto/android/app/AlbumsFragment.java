
package me.openphoto.android.app;

import me.openphoto.android.app.bitmapfun.util.ImageCache;
import me.openphoto.android.app.bitmapfun.util.ImageFetcher;
import me.openphoto.android.app.model.Album;
import me.openphoto.android.app.net.AlbumsResponse;
import me.openphoto.android.app.net.IOpenPhotoApi;
import me.openphoto.android.app.net.ReturnSizes;
import me.openphoto.android.app.ui.adapter.EndlessAdapter;
import me.openphoto.android.app.util.GalleryOpenControl;
import me.openphoto.android.app.util.GuiUtils;
import me.openphoto.android.app.util.LoadingControl;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.WazaBe.HoloEverywhere.LayoutInflater;
import com.WazaBe.HoloEverywhere.app.Activity;

/**
 * The fragment which displays albums list
 * 
 * @author Eugene Popovich
 */
public class AlbumsFragment extends CommonFrargmentWithImageWorker implements
        OnItemClickListener
{
    public static final String TAG = AlbumsFragment.class.getSimpleName();

    private static final String IMAGE_CACHE_DIR = SyncImageSelectionFragment.IMAGE_CACHE_DIR;

    private LoadingControl loadingControl;
    private GalleryOpenControl galleryOpenControl;

    private AlbumsAdapter mAdapter;

    private ReturnSizes returnSizes;

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
        int width = 100;
        int height = 100;
        returnSizes = new ReturnSizes(width, height, true);
        mImageWorker = new ImageFetcher(getActivity(), loadingControl, width, height);
        mImageWorker.setImageCache(ImageCache.findOrCreateCache(getActivity(),
                IMAGE_CACHE_DIR));
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view,
            int position, long id)
    {
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
        private final IOpenPhotoApi mOpenPhotoApi;

        public AlbumsAdapter()
        {
            super(Integer.MAX_VALUE);
            mOpenPhotoApi = Preferences.getApi(getActivity());
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
                        .loadImage(album.getCover().getUrl(returnSizes.toString()), image);
            } else
            {
                image.setImageBitmap(null);
            }
            return convertView;
        }

        @Override
        public LoadResponse loadItems(int page)
        {
            if (checkOnline())
            {
                try
                {
                    AlbumsResponse response = mOpenPhotoApi.getAlbums();
                    return new LoadResponse(response.getAlbums(), false);
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
