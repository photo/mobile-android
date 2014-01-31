
package com.trovebox.android.app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.holoeverywhere.LayoutInflater;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.trovebox.android.app.model.utils.AlbumUtils;
import com.trovebox.android.app.model.utils.AlbumUtils.AlbumCreatedHandler;
import com.trovebox.android.app.model.utils.AlbumUtils.AlbumsByIdComparator;
import com.trovebox.android.app.ui.adapter.MultiSelectAlbumsAdapter;
import com.trovebox.android.common.activity.CommonActivity;
import com.trovebox.android.common.bitmapfun.util.ImageCache;
import com.trovebox.android.common.bitmapfun.util.ImageFetcher;
import com.trovebox.android.common.fragment.common.CommonFragmentWithImageWorker;
import com.trovebox.android.common.model.Album;
import com.trovebox.android.common.model.Photo;
import com.trovebox.android.common.model.utils.PhotoUtils;
import com.trovebox.android.common.net.ReturnSizes;
import com.trovebox.android.common.util.CommonUtils;
import com.trovebox.android.common.util.LoadingControl;
import com.trovebox.android.common.util.RunnableWithParameter;
import com.trovebox.android.common.util.TrackerUtils;
import com.trovebox.android.common.util.data.StringMapParcelableWrapper;

/**
 * The activity which allows to select albums
 * 
 * @author Eugene Popovich
 */
public class SelectAlbumsActivity
        extends CommonActivity implements AlbumCreatedHandler {

    public static final String TAG =
            SelectAlbumsActivity.class.getSimpleName();
    public static final String SELECTED_ALBUMS = "SELECTED_ALBUMS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null)
        {
            getSupportFragmentManager().beginTransaction()
                    .replace(android.R.id.content, new SelectAlbumsUiFragment())
                    .commit();
        }
        addRegisteredReceiver(AlbumUtils.getAndRegisterOnAlbumCreatedActionBroadcastReceiver(TAG,
                this, this));
    }

    @Override
    public void albumCreated(Album album) {
        getContentFragment().albumCreated(album);
    }

    /**
     * Get the current content fragment of the SelectAlbumsUiFragment type
     * 
     * @return
     */
    SelectAlbumsUiFragment getContentFragment()
    {
        return (SelectAlbumsUiFragment) getSupportFragmentManager().findFragmentById(
                android.R.id.content);
    }

    public static class SelectAlbumsUiFragment extends CommonFragmentWithImageWorker
            implements LoadingControl, OnItemClickListener, AlbumCreatedHandler
    {
        private int mLoaders = 0;

        private AlbumsAdapter mAdapter;
        private ListView list;

        private ReturnSizes thumbSize;

        @Override
        protected void initImageWorker() {
            int mImageThumbSize = getResources().getDimensionPixelSize(
                    R.dimen.album_item_size);
            thumbSize = new ReturnSizes(mImageThumbSize, mImageThumbSize, true);
            mImageWorker = new ImageFetcher(getActivity(), this, thumbSize.getWidth(),
                    thumbSize.getHeight());
            mImageWorker.setLoadingImage(R.drawable.empty_photo);
            mImageWorker.setImageCache(ImageCache.findOrCreateCache(getActivity(),
                    ImageCache.THUMBS_CACHE_DIR, false));
        }

        @Override
        public View onCreateView(LayoutInflater inflater,
                ViewGroup container, Bundle savedInstanceState)
        {
            super.onCreateView(inflater, container, savedInstanceState);
            View v = inflater.inflate(R.layout.activity_select_albums, container,
                    false);
            return v;
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            init(view);
        }

        void init(View v)
        {
            StringMapParcelableWrapper albumsWrapper = getActivity().getIntent()
                    .getParcelableExtra(SELECTED_ALBUMS);
            mAdapter = new AlbumsAdapter(albumsWrapper == null ? null : albumsWrapper.getMap());
            list = (ListView) v.findViewById(R.id.list_select_albums);

            list.setAdapter(mAdapter);
            list.setOnItemClickListener(this);

            Button createAlbumBtn = (Button) v.findViewById(R.id.createBtn);
            createAlbumBtn.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    TrackerUtils.trackButtonClickEvent("createAlbumBtn",
                            SelectAlbumsUiFragment.this);
                    createAlbumClicked(v);
                }
            });
            Button finishBtn = (Button) v.findViewById(R.id.finishBtn);
            finishBtn.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    TrackerUtils.trackButtonClickEvent("finishBtn",
                            SelectAlbumsUiFragment.this);
                    finishedClicked(v);
                }
            });
        }

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view,
                int position, long id)
        {
            TrackerUtils.trackButtonClickEvent("album_item", SelectAlbumsUiFragment.this);
            ViewHolder vh = mAdapter.getViewHolder(view);
            mAdapter.onAlbumViewClicked(vh.checkBox, !vh.checkBox.isChecked());
        }

        /**
         * Return selected Albums to the upload activity
         * 
         * @param v
         */
        public void finishedClicked(View v) {

            Intent data = new Intent();
            data.putExtra(SELECTED_ALBUMS, mAdapter.getSelectedAlbumsParceble());
            getActivity().setResult(RESULT_OK, data);
            getActivity().finish();

        }

        /**
         * Open create new album dialog
         * 
         * @param v
         */
        public void createAlbumClicked(View v) {
            AlbumCreateFragment albumCreateFragment = new AlbumCreateFragment();
            albumCreateFragment.show(getSupportActivity());
        }

        @Override
        public void albumCreated(Album album) {
            mAdapter.addItem(album);
        }

        private class AlbumsAdapter extends MultiSelectAlbumsAdapter {
            boolean needToFilter = false;
            /**
             * This stores already filtered items
             */
            List<Album> sortedItems;
            /**
             * The comparator to sort sortedItems list
             */
            AlbumsByIdComparator comparator;

            public AlbumsAdapter(Map<String, String> alreadySelectedAlbums) {
                super(SelectAlbumsUiFragment.this);
                if (alreadySelectedAlbums != null)
                {
                    checkedAlbums.putAll(alreadySelectedAlbums);
                }
            }

            @Override
            public View getView(Album album, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    final LayoutInflater layoutInflater = (LayoutInflater) getActivity()
                            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = layoutInflater.inflate(
                            R.layout.list_item_album_checkbox, null);
                }
                final ViewHolder vh = getViewHolder(convertView);
                initAlbumCheckbox(album, vh.checkBox);
                vh.name.setText(album.getName());
                if (album.getCover() != null)
                {
                    PhotoUtils.validateUrlForSizeExistAsyncAndRun(album.getCover(), thumbSize,
                            new RunnableWithParameter<Photo>() {

                                @Override
                                public void run(Photo photo) {
                                    mImageWorker
                                            .loadImage(photo.getUrl(thumbSize.toString()), vh.cover);

                                }
                            }, SelectAlbumsUiFragment.this);
                } else
                {
                    mImageWorker
                            .loadImage(null, vh.cover);
                }

                return convertView;
            }

            ViewHolder getViewHolder(View view)
            {
                ViewHolder viewHolder = (ViewHolder) view.getTag();
                if (viewHolder == null)
                {
                    viewHolder = new ViewHolder();
                    viewHolder.checkBox = (CheckBox) view
                            .findViewById(R.id.album_checkbox);
                    viewHolder.cover = (ImageView) view.findViewById(R.id.cover);
                    viewHolder.name = (TextView) view.findViewById(R.id.text_name);
                }
                return viewHolder;
            }

            public StringMapParcelableWrapper getSelectedAlbumsParceble() {
                return new StringMapParcelableWrapper(checkedAlbums);
            }

            @Override
            public void addItem(Album item) {
                needToFilter = true;
                super.addItem(item);
            }

            @Override
            public Album filterItemBeforeAdd(Album item) {
                if (needToFilter)
                {
                    return super.filterItemBeforeAdd(item);
                } else
                {
                    return item;
                }
            }

            @Override
            public List<Album> filterItemsBeforeAdd(List<Album> items) {
                try
                {
                    if (needToFilter)
                    {
                        if (sortedItems == null)
                        {
                            comparator = new AlbumsByIdComparator();
                            sortedItems = new ArrayList<Album>(getItems());
                            Collections.sort(sortedItems, comparator);
                        }
                        List<Album> filteredItems = new ArrayList<Album>();
                        for (Album album : items)
                        {
                            int ix = Collections.binarySearch(sortedItems, album, comparator);
                            // if item is absent in sortedItems list we need to
                            // add it there and to the filteredItems
                            if (ix < 0)
                            {
                                filteredItems.add(album);
                                sortedItems.add(-ix - 1, album);
                            }

                        }
                        return filteredItems;
                    }
                } catch (Exception ex)
                {
                    CommonUtils.error(TAG, null, ex);
                }
                return super.filterItemsBeforeAdd(items);
            }
        }

        public static class ViewHolder
        {
            CheckBox checkBox;
            ImageView cover;
            TextView name;

        }

        @Override
        public void startLoading()
        {
            if (mLoaders++ == 0)
            {
                showLoading(true);
            }
        }

        @Override
        public void stopLoading()
        {
            if (--mLoaders == 0)
            {
                showLoading(false);
            }
        }

        @Override
        public boolean isLoading() {
            return mLoaders > 0;
        }

        private void showLoading(boolean show)
        {
            if (getView() != null)
            {
                getView().findViewById(R.id.loading).setVisibility(show ? View.VISIBLE :
                        View.GONE);
            }
        }

    }
}
