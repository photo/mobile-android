
package me.openphoto.android.app.ui.adapter;

import java.util.ArrayList;
import java.util.List;

import me.openphoto.android.app.Preferences;
import me.openphoto.android.app.R;
import me.openphoto.android.app.model.Photo;
import me.openphoto.android.app.net.IOpenPhotoApi;
import me.openphoto.android.app.net.Paging;
import me.openphoto.android.app.net.PhotosResponse;
import me.openphoto.android.app.net.ReturnSizes;
import me.openphoto.android.app.util.GuiUtils;
import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

public abstract class PhotosEndlessAdapter extends EndlessAdapter<Photo>
{
    public static final int DEFAULT_PAGE_SIZE = 30;
    private static final String TAG = null;
    private final IOpenPhotoApi mOpenPhotoApi;
    private final List<String> mTagFilter;
    private final String mAlbumFilter;
    private static final ReturnSizes mSizes;
    public static ReturnSizes SIZE_SMALL = new ReturnSizes(200, 200);
    public static ReturnSizes SIZE_BIG = new ReturnSizes(1024, 1024);

    static
    {
        mSizes = new ReturnSizes(SIZE_SMALL);
        mSizes.add(SIZE_BIG);
    }

    public PhotosEndlessAdapter(Context context)
    {
        this(context, DEFAULT_PAGE_SIZE);
    }

    public PhotosEndlessAdapter(Context context, int pageSize)
    {
        this(context, pageSize, null, null);
    }

    public PhotosEndlessAdapter(Context context, String tagFilter,
            String albumFilter)
    {
        this(context, DEFAULT_PAGE_SIZE, tagFilter, albumFilter);
    }

    public PhotosEndlessAdapter(Context context, int pageSize, String tagFilter,
            String albumFilter)
    {
        this(context, pageSize, null, tagFilter, albumFilter);
    }

    public PhotosEndlessAdapter(Context context, ArrayList<Photo> photos)
    {
        this(context, photos, null, null);
    }

    public PhotosEndlessAdapter(Context context, ArrayList<Photo> photos,
            String tagFilter,
            String albumFilter)
    {
        this(context, DEFAULT_PAGE_SIZE, photos, tagFilter, albumFilter);
    }

    public PhotosEndlessAdapter(Context context,
            int pageSize,
            ArrayList<Photo> photos,
            String tagFilter,
            String albumFilter)
    {
        super(pageSize, photos);
        mOpenPhotoApi = Preferences.getApi(context);
        mTagFilter = new ArrayList<String>(1);
        if (tagFilter != null)
        {
            mTagFilter.add(tagFilter);
        }
        mAlbumFilter = albumFilter;
        if (isEmpty())
        {
            loadFirstPage();
        }
    }

    public PhotosEndlessAdapter(Context context, ParametersHolder holder)
    {
        this(context, holder.pageSize, holder.items, holder.tagFilter, holder.albumFilter);
    }

    @Override
    public long getItemId(int position)
    {
        return getItem(position).hashCode();
    }

    @Override
    public LoadResponse loadItems(int page)
    {
        return loadItemsGeneral(page, getPageSize());
    }

    public LoadResponse loadItemsGeneral(int page, int pageSize) {
        try
        {
            PhotosResponse response = mOpenPhotoApi.getPhotos(mSizes,
                    mTagFilter, mAlbumFilter, new Paging(page,
                            pageSize));
            boolean hasNextPage = response.getCurrentPage() < response
                    .getTotalPages();
            return new LoadResponse(response.getPhotos(), hasNextPage);
        } catch (Exception e)
        {
            GuiUtils.error(
                    TAG,
                    R.string.errorCouldNotLoadNextPhotosInList, e);
        }
        return new LoadResponse(null, false);
    }

    @Override
    public LoadResponse loadOneMoreItem(int index) {
        return loadItemsGeneral(index, 1);
    }

    public String getTagFilter() {
        return mTagFilter.isEmpty() ? null : mTagFilter.get(0);
    }

    public String getAlbumFilter() {
        return mAlbumFilter;
    }

    public static class ParametersHolder implements Parcelable
    {
        int page;
        int pageSize;
        int position;
        String tagFilter;
        String albumFilter;
        ArrayList<Photo> items;

        ParametersHolder() {
        }

        public ParametersHolder(PhotosEndlessAdapter adapter,
                Photo value) {
            items = adapter.getItems();
            pageSize = adapter.getPageSize();
            page = adapter.getCurrentPage();
            position = items.indexOf(value);
            tagFilter = adapter.getTagFilter();
            albumFilter = adapter.getAlbumFilter();
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
            items = new ArrayList<Photo>();
            in.readList(items, getClass().getClassLoader());
        }
    }
}
