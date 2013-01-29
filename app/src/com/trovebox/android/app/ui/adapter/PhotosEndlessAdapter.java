
package com.trovebox.android.app.ui.adapter;

import java.util.ArrayList;
import java.util.List;

import com.trovebox.android.app.Preferences;
import com.trovebox.android.app.R;
import com.trovebox.android.app.model.Photo;
import com.trovebox.android.app.net.ITroveboxApi;
import com.trovebox.android.app.net.Paging;
import com.trovebox.android.app.net.PhotosResponse;
import com.trovebox.android.app.net.ReturnSizes;
import com.trovebox.android.app.util.CommonUtils;
import com.trovebox.android.app.util.GuiUtils;
import com.trovebox.android.app.util.TrackerUtils;

import android.app.Activity;
import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.DisplayMetrics;

public abstract class PhotosEndlessAdapter extends EndlessAdapter<Photo>
{
    public static final int DEFAULT_PAGE_SIZE = 30;
    private static final String TAG = PhotosEndlessAdapter.class.getSimpleName();
    private final ITroveboxApi mTroveboxApi;
    private final List<String> mTagFilter;
    private final String mAlbumFilter;
    private final String sortBy;
    private ReturnSizes returnSizes;

    public PhotosEndlessAdapter(Context context,
            ReturnSizes returnSizes)
    {
        this(context, DEFAULT_PAGE_SIZE, returnSizes);
    }

    public PhotosEndlessAdapter(Context context, int pageSize,
            ReturnSizes returnSizes)
    {
        this(context, pageSize, null, null, null, returnSizes);
    }

    public PhotosEndlessAdapter(Context context, String tagFilter,
            String albumFilter,
            String sortBy,
            ReturnSizes returnSizes)
    {
        this(context, DEFAULT_PAGE_SIZE, tagFilter, albumFilter, sortBy, returnSizes);
    }

    public PhotosEndlessAdapter(Context context, int pageSize, String tagFilter,
            String albumFilter,
            String sortBy,
            ReturnSizes returnSizes)
    {
        this(context, pageSize, null, tagFilter, albumFilter, sortBy, returnSizes);
    }

    public PhotosEndlessAdapter(Context context, ArrayList<Photo> photos,
            ReturnSizes returnSizes)
    {
        this(context, photos, null, null, null, returnSizes);
    }

    public PhotosEndlessAdapter(Context context, ArrayList<Photo> photos,
            String tagFilter,
            String albumFilter,
            String sortBy,
            ReturnSizes returnSizes)
    {
        this(context, DEFAULT_PAGE_SIZE, photos, tagFilter, albumFilter, sortBy, returnSizes);
    }

    public PhotosEndlessAdapter(Context context,
            int pageSize,
            ArrayList<Photo> photos,
            String tagFilter,
            String albumFilter,
            String sortBy,
            ReturnSizes returnSizes)
    {
        super(pageSize, photos);
        this.returnSizes = returnSizes;
        mTroveboxApi = Preferences.getApi(context);
        mTagFilter = new ArrayList<String>(1);
        if (tagFilter != null)
        {
            mTagFilter.add(tagFilter);
        }
        mAlbumFilter = albumFilter;
        this.sortBy = sortBy;
        if (isEmpty())
        {
            loadFirstPage();
        }
    }

    public PhotosEndlessAdapter(Context context, ParametersHolder holder,
            ReturnSizes returnSizes)
    {
        this(context, holder.pageSize, holder.items, holder.tagFilter, holder.albumFilter,
                holder.sortBy,
                returnSizes);
    }

    @Override
    public long getItemId(int position)
    {
        return getItem(position).hashCode();
    }

    @Override
    public LoadResponse loadItems(
            int page)
    {
        if (CommonUtils.checkLoggedInAndOnline())
        {
            return loadItemsGeneral(page, getPageSize());
        } else
        {
            return new LoadResponse(null, false);
        }
    }

    public LoadResponse loadItemsGeneral(int page, int pageSize) {
        try
        {
            TrackerUtils.trackBackgroundEvent(
                    CommonUtils.format("loadPhotos: page = %1$d, pageSize = %2$d", page, pageSize),
                    getClass().getSimpleName());
            long start = System.currentTimeMillis();
            PhotosResponse response = mTroveboxApi.getPhotos(returnSizes,
                    mTagFilter, mAlbumFilter, sortBy, new Paging(page,
                            pageSize));
            TrackerUtils.trackDataLoadTiming(System.currentTimeMillis() - start,
                    CommonUtils.format("loadPhotos: page = %1$d, pageSize = %2$d", page, pageSize),
                    getClass().getSimpleName());
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
     * Get the return sizes by clonning returnSize and adding detailsReturnSizes
     * fields as a childs
     * 
     * @param returnSize
     * @param detailsReturnSizes
     * @return
     */
    public static ReturnSizes getReturnSizes(
            ReturnSizes returnSize,
            DetailsReturnSizes detailsReturnSizes
            )
    {
        return getReturnSizes(returnSize, detailsReturnSizes.detailsBigPhotoSize,
                detailsReturnSizes.detailsThumbSize);
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

    /**
     * Get the return sizes for the gallery activity
     * 
     * @param activity
     * @return
     */
    public static DetailsReturnSizes getDetailsReturnSizes(Activity activity)
    {
        DetailsReturnSizes result = new DetailsReturnSizes();
        
        int detailsThumbnailSize = activity.getResources().getDimensionPixelSize(
                R.dimen.detail_thumbnail_size);
        result.detailsThumbSize = new ReturnSizes(
                detailsThumbnailSize, detailsThumbnailSize, true);
        result.detailsBigPhotoSize = getBigImageSize(activity);
        
        return result;
    }

    public static class DetailsReturnSizes
    {
        public ReturnSizes detailsThumbSize;
        public ReturnSizes detailsBigPhotoSize;
    }
    public static class ParametersHolder implements Parcelable
    {
        int page;
        int pageSize;
        int position;
        String tagFilter;
        String albumFilter;
        String sortBy;
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
            sortBy = in.readString();
            items = new ArrayList<Photo>();
            in.readList(items, getClass().getClassLoader());
        }
    }
    /**
     * Should be called when processing photo removal event
     * @param photo
     */
    public void photoDeleted(Photo photo)
    {
        int index = photoIndex(photo);
        if (index != -1)
        {
            deleteItemAtAndLoadOneMoreItem(index);
        }
    }

    /**
     * Should be called when processing photo updated event
     * 
     * @param photo
     */
    public void photoUpdated(Photo photo)
    {
        int index = photoIndex(photo);
        if (index != -1)
        {
            updateItemAt(index, photo);
        }
    }

    public int photoIndex(Photo photo) {
        int index = -1;
        List<Photo> photos = getItems();
        for (int i = 0, size = photos.size(); i < size; i++)
        {
            Photo photo2 = photos.get(i);
            if (photo2.getId().equals(photo.getId()))
            {
                index = i;
                break;
            }
        }
        return index;
    }
}
