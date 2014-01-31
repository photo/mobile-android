
package com.trovebox.android.common.ui.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.trovebox.android.common.CommonConfigurationUtils;
import com.trovebox.android.common.R;
import com.trovebox.android.common.model.Photo;
import com.trovebox.android.common.net.ITroveboxApi;
import com.trovebox.android.common.net.Paging;
import com.trovebox.android.common.net.PhotosResponse;
import com.trovebox.android.common.net.ReturnSizes;
import com.trovebox.android.common.net.TroveboxResponseUtils;
import com.trovebox.android.common.util.CommonUtils;
import com.trovebox.android.common.util.GuiUtils;
import com.trovebox.android.common.util.TrackerUtils;

public abstract class PhotosEndlessAdapter extends EndlessAdapter<Photo> {
    public static final int DEFAULT_PAGE_SIZE = 30;
    private static final String TAG = PhotosEndlessAdapter.class.getSimpleName();
    private final ITroveboxApi mTroveboxApi;
    private final List<String> mTagFilter;
    private final String mAlbumFilter;
    private final String mToken;
    private final String mHost;
    private final String sortBy;
    private ReturnSizes returnSizes;

    public PhotosEndlessAdapter(Context context, ReturnSizes returnSizes) {
        this(context, DEFAULT_PAGE_SIZE, returnSizes);
    }

    public PhotosEndlessAdapter(Context context, int pageSize, ReturnSizes returnSizes) {
        this(context, pageSize, null, null, null, null, returnSizes, null);
    }

    public PhotosEndlessAdapter(Context context, String tagFilter, String albumFilter,
            String token, String sortBy, ReturnSizes returnSizes) {
        this(context, DEFAULT_PAGE_SIZE, tagFilter, albumFilter, token, sortBy, returnSizes, null);
    }

    public PhotosEndlessAdapter(Context context, int pageSize, String tagFilter,
            String albumFilter, String token, String sortBy, ReturnSizes returnSizes, String host) {
        this(context, pageSize, null, tagFilter, albumFilter, token, sortBy, returnSizes, host);
    }

    public PhotosEndlessAdapter(Context context, ArrayList<Photo> photos, ReturnSizes returnSizes) {
        this(context, photos, null, null, null, null, returnSizes);
    }

    public PhotosEndlessAdapter(Context context, ArrayList<Photo> photos, String tagFilter,
            String albumFilter, String token, String sortBy, ReturnSizes returnSizes) {
        this(context, DEFAULT_PAGE_SIZE, photos, tagFilter, albumFilter, token, sortBy,
                returnSizes, null);
    }

    public PhotosEndlessAdapter(Context context, int pageSize, ArrayList<Photo> photos,
            String tagFilter, String albumFilter, String token, String sortBy,
            ReturnSizes returnSizes, String host) {
        super(pageSize, photos);
        this.returnSizes = returnSizes;
        mTroveboxApi = CommonConfigurationUtils.getApi();
        mTagFilter = new ArrayList<String>(1);
        if (tagFilter != null) {
            mTagFilter.add(tagFilter);
        }
        mAlbumFilter = albumFilter;
        mToken = token;
        mHost = host;
        this.sortBy = sortBy;
        if (isEmpty()) {
            loadFirstPage();
        }
    }

    public PhotosEndlessAdapter(Context context, ParametersHolder holder, ReturnSizes returnSizes) {
        this(context, holder.pageSize, holder.items, holder.tagFilter, holder.albumFilter,
                holder.token, holder.sortBy, returnSizes, holder.host);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).hashCode();
    }

    @Override
    public LoadResponse loadItems(int page) {
        if (GuiUtils.checkLoggedInAndOnline()) {
            return loadItemsGeneral(page, getPageSize());
        } else {
            return new LoadResponse(null, false);
        }
    }

    public LoadResponse loadItemsGeneral(int page, int pageSize) {
        try {
            CommonUtils.debug(TAG, "loadPhotos: page = %1$d, pageSize = %2$d", page, pageSize);
            TrackerUtils.trackBackgroundEvent(
                    CommonUtils.format("loadPhotos: page = %1$d, pageSize = %2$d", page, pageSize),
                    getClass().getSimpleName());
            long start = System.currentTimeMillis();
            PhotosResponse response = mTroveboxApi.getPhotos(returnSizes, mTagFilter, mAlbumFilter,
                    mToken, sortBy, new Paging(page, pageSize), mHost);
            if (TroveboxResponseUtils.checkResponseValid(response)) {
                TrackerUtils.trackDataLoadTiming(System.currentTimeMillis() - start, CommonUtils
                        .format("loadPhotos: page = %1$d, pageSize = %2$d", page, pageSize),
                        getClass().getSimpleName());
                boolean hasNextPage = response.getCurrentPage() < response.getTotalPages();
                return new LoadResponse(response.getPhotos(), hasNextPage);
            }
        } catch (Exception e) {
            GuiUtils.error(TAG, R.string.errorCouldNotLoadNextPhotosInList, e);
        }
        return new LoadResponse(null, false);
    }

    @Override
    public LoadResponse loadOneMoreItem(int index) {
        if (GuiUtils.checkLoggedInAndOnline()) {
            return loadItemsGeneral(index, 1);
        } else {
            return new LoadResponse(null, false);
        }
    }

    public String getTagFilter() {
        return mTagFilter.isEmpty() ? null : mTagFilter.get(0);
    }

    public String getAlbumFilter() {
        return mAlbumFilter;
    }

    public String getToken() {
        return mToken;
    }

    public String getHost() {
        return mHost;
    }

    public String getSortBy() {
        return sortBy;
    }

    /**
     * Get the return sizes by clonning returnSize and adding additionalSizes as
     * a childs
     * 
     * @param returnSize
     * @param additionalSizes
     * @return
     */
    public static ReturnSizes getReturnSizes(ReturnSizes returnSize, ReturnSizes... additionalSizes) {
        ReturnSizes result = new ReturnSizes(returnSize);
        for (ReturnSizes additionalSize : additionalSizes) {
            result.add(additionalSize);
        }
        return result;
    }

    /**
     * Should be called when processing photo removal event
     * 
     * @param photo
     */
    public void photoDeleted(Photo photo) {
        int index = photoIndex(photo);
        if (index != -1) {
            deleteItemAtAndLoadOneMoreItem(index);
        }
    }

    /**
     * Should be called when processing photo updated event
     * 
     * @param photo
     */
    public void photoUpdated(Photo photo) {
        int index = photoIndex(photo);
        if (index != -1) {
            updateItemAt(index, photo);
        }
    }

    public int photoIndex(Photo photo) {
        int index = -1;
        List<Photo> photos = getItems();
        for (int i = 0, size = photos.size(); i < size; i++) {
            Photo photo2 = photos.get(i);
            if (photo2.getId().equals(photo.getId())) {
                index = i;
                break;
            }
        }
        return index;
    }

    public static class ParametersHolder implements Parcelable {
        int page;
        int pageSize;
        int position;
        String tagFilter;
        String albumFilter;
        String token;
        String host;
        String sortBy;
        ArrayList<Photo> items;

        ParametersHolder() {
        }

        public ParametersHolder(PhotosEndlessAdapter adapter, Photo value) {
            items = adapter.getItems();
            pageSize = adapter.getPageSize();
            page = adapter.getCurrentPage();
            position = items.indexOf(value);
            tagFilter = adapter.getTagFilter();
            albumFilter = adapter.getAlbumFilter();
            token = adapter.getToken();
            host = adapter.getHost();
            sortBy = adapter.getSortBy();
        }

        public int getPage() {
            return page;
        }

        public int getPageSize() {
            return pageSize;
        }

        public int getPosition() {
            return position;
        }

        public ArrayList<Photo> getItems() {
            return items;
        }

        /*****************************
         * PARCELABLE IMPLEMENTATION *
         *****************************/

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            out.writeInt(page);
            out.writeInt(pageSize);
            out.writeInt(position);
            out.writeString(tagFilter);
            out.writeString(albumFilter);
            out.writeString(token);
            out.writeString(host);
            out.writeString(sortBy);
            out.writeList(items);
        }

        public static final Parcelable.Creator<ParametersHolder> CREATOR = new Parcelable.Creator<ParametersHolder>() {
            @Override
            public ParametersHolder createFromParcel(Parcel in) {
                return new ParametersHolder(in);
            }

            @Override
            public ParametersHolder[] newArray(int size) {
                return new ParametersHolder[size];
            }
        };

        private ParametersHolder(Parcel in) {
            this();
            page = in.readInt();
            pageSize = in.readInt();
            position = in.readInt();
            tagFilter = in.readString();
            albumFilter = in.readString();
            token = in.readString();
            host = in.readString();
            sortBy = in.readString();
            items = new ArrayList<Photo>();
            in.readList(items, getClass().getClassLoader());
        }
    }
}
