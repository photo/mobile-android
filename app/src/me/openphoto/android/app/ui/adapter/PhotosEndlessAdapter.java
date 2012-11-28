
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
import android.app.Activity;
import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.DisplayMetrics;

public abstract class PhotosEndlessAdapter extends EndlessAdapter<Photo>
{
    public static final int DEFAULT_PAGE_SIZE = 30;
    private static final String TAG = null;
    private final IOpenPhotoApi mOpenPhotoApi;
    private final List<String> mTagFilter;
    private final String mAlbumFilter;
    private ReturnSizes returnSizes;

    public PhotosEndlessAdapter(Context context,
            ReturnSizes returnSizes)
    {
        this(context, DEFAULT_PAGE_SIZE, returnSizes);
    }

    public PhotosEndlessAdapter(Context context, int pageSize,
            ReturnSizes returnSizes)
    {
        this(context, pageSize, null, null, returnSizes);
    }

    public PhotosEndlessAdapter(Context context, String tagFilter,
            String albumFilter,
            ReturnSizes returnSizes)
    {
        this(context, DEFAULT_PAGE_SIZE, tagFilter, albumFilter, returnSizes);
    }

    public PhotosEndlessAdapter(Context context, int pageSize, String tagFilter,
            String albumFilter,
            ReturnSizes returnSizes)
    {
        this(context, pageSize, null, tagFilter, albumFilter, returnSizes);
    }

    public PhotosEndlessAdapter(Context context, ArrayList<Photo> photos,
            ReturnSizes returnSizes)
    {
        this(context, photos, null, null, returnSizes);
    }

    public PhotosEndlessAdapter(Context context, ArrayList<Photo> photos,
            String tagFilter,
            String albumFilter,
            ReturnSizes returnSizes)
    {
        this(context, DEFAULT_PAGE_SIZE, photos, tagFilter, albumFilter, returnSizes);
    }

    public PhotosEndlessAdapter(Context context,
            int pageSize,
            ArrayList<Photo> photos,
            String tagFilter,
            String albumFilter,
            ReturnSizes returnSizes)
    {
        super(pageSize, photos);
        this.returnSizes = returnSizes;
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

    public PhotosEndlessAdapter(Context context, ParametersHolder holder,
            ReturnSizes returnSizes)
    {
        this(context, holder.pageSize, holder.items, holder.tagFilter, holder.albumFilter,
                returnSizes);
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
            PhotosResponse response = mOpenPhotoApi.getPhotos(returnSizes,
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

    /**
     * Get the return sizes by clonning returnSize and adding additionalSizes as
     * a childs
     * 
     * @param returnSize
     * @param additionalSizes
     * @return
     */
    public static ReturnSizes getReturnSizes(
            ReturnSizes returnSize,
            ReturnSizes... additionalSizes
            )
    {
        ReturnSizes result = new ReturnSizes(returnSize);
        for (ReturnSizes additionalSize : additionalSizes)
        {
            result.add(additionalSize);
        }
        return result;
    }

    /**
     * Get the big image size which depends on the screen dimension
     * 
     * @param activity
     * @return
     */
    public static ReturnSizes getBigImageSize(Activity activity) {
        final DisplayMetrics displaymetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay()
                .getMetrics(displaymetrics);
        final int height = displaymetrics.heightPixels;
        final int width = displaymetrics.widthPixels;
        final int longest = height > width ? height : width;
        ReturnSizes bigSize = new ReturnSizes(longest, longest);
        return bigSize;
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
