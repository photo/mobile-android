
package me.openphoto.android.app.ui.adapter;

import java.util.ArrayList;
import java.util.List;

import me.openphoto.android.app.Preferences;
import me.openphoto.android.app.model.Photo;
import me.openphoto.android.app.net.IOpenPhotoApi;
import me.openphoto.android.app.net.Paging;
import me.openphoto.android.app.net.PhotosResponse;
import me.openphoto.android.app.net.ReturnSizes;
import android.content.Context;
import android.util.Log;

public abstract class PhotosEndlessAdapter extends EndlessAdapter<Photo>
{
    private static final String TAG = null;
    private final IOpenPhotoApi mOpenPhotoApi;
    private final List<String> mTagFilter;
    private final String mAlbumFilter;
    private static final ReturnSizes mSizes;
    public static String SIZE_SMALL;
    public static String SIZE_BIG;

    static
    {
        mSizes = new ReturnSizes(200, 200);
        mSizes.add(1024, 1024);
        SIZE_SMALL = mSizes.get(0);
        SIZE_BIG = mSizes.get(1);
    }

    public PhotosEndlessAdapter(Context context)
    {
        this(context, null, null);
    }

    public PhotosEndlessAdapter(Context context, String tagFilter,
            String albumFilter)
    {
        this(context, null, tagFilter, albumFilter);
    }

    public PhotosEndlessAdapter(Context context, ArrayList<Photo> photos)
    {
        this(context, photos, null, null);
    }

    public PhotosEndlessAdapter(Context context, ArrayList<Photo> photos,
            String tagFilter,
            String albumFilter)
    {
        super(30, photos);
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

    @Override
    public long getItemId(int position)
    {
        return ((Photo) getItem(position)).getId().hashCode();
    }

    @Override
    public LoadResponse loadItems(int page)
    {
        try
        {
            PhotosResponse response = mOpenPhotoApi.getPhotos(mSizes,
                    mTagFilter, mAlbumFilter, new Paging(page,
                            getPageSize()));
            boolean hasNextPage = response.getCurrentPage() < response
                    .getTotalPages();
            return new LoadResponse(response.getPhotos(), hasNextPage);
        } catch (Exception e)
        {
            Log.e(TAG, "Could not load next photos in list", e);
        }
        return new LoadResponse(null, false);
    }
}
