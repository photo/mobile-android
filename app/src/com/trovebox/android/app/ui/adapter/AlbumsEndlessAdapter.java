
package com.trovebox.android.app.ui.adapter;

import com.trovebox.android.app.Preferences;
import com.trovebox.android.app.R;
import com.trovebox.android.app.model.Album;
import com.trovebox.android.app.net.AlbumsResponse;
import com.trovebox.android.app.net.ITroveboxApi;
import com.trovebox.android.app.net.Paging;
import com.trovebox.android.app.net.TroveboxResponseUtils;
import com.trovebox.android.app.util.CommonUtils;
import com.trovebox.android.app.util.GuiUtils;
import com.trovebox.android.app.util.LoadingControl;

/**
 * An endless adapter for albums
 * 
 * @author Eugene Popovich
 */
public abstract class AlbumsEndlessAdapter extends EndlessAdapter<Album> {
    public static final int DEFAULT_PAGE_SIZE = 20;
    private final String TAG = AlbumsEndlessAdapter.class.getSimpleName();
    private final ITroveboxApi mTroveboxApi;
    private LoadingControl loadingControl;

    public AlbumsEndlessAdapter(LoadingControl loadingControl)
    {
        this(DEFAULT_PAGE_SIZE, loadingControl);
    }

    public AlbumsEndlessAdapter(int pageSize, LoadingControl loadingControl) {
        super(pageSize);
        this.loadingControl = loadingControl;
        mTroveboxApi = Preferences.getApi();
        loadFirstPage();
    }

    @Override
    public long getItemId(int position)
    {
        return ((Album) getItem(position)).getId().hashCode();
    }

    @Override
    public LoadResponse loadItems(int page)
    {
        if (CommonUtils.checkLoggedInAndOnline())
        {
            try
            {
                AlbumsResponse response = mTroveboxApi.getAlbums(new Paging(page, getPageSize()),
                        true);
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
        if (loadingControl != null)
        {
            loadingControl.startLoading();
        }
    }

    @Override
    protected void onStoppedLoading()
    {
        if (loadingControl != null)
        {
            loadingControl.stopLoading();
        }
    }
}
