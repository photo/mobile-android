
package com.trovebox.android.app.ui.adapter;

import java.util.HashMap;
import java.util.Map;

import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.trovebox.android.common.model.Album;
import com.trovebox.android.common.util.LoadingControl;

/**
 * @author Eugene Popovich
 */
public abstract class MultiSelectAlbumsAdapter extends AlbumsEndlessAdapter
{
    static final String TAG = MultiSelectAlbumsAdapter.class.getSimpleName();
    protected Map<String, String> checkedAlbums = new HashMap<String, String>();

    public MultiSelectAlbumsAdapter(LoadingControl loadingControl)
    {
        super(loadingControl);
    }

    @Override
    public long getItemId(int position)
    {
        return ((Album) getItem(position)).getId().hashCode();
    }

    /**
     * Init the album checkbox
     * 
     * @param album
     * @param checkBox
     */
    public void initAlbumCheckbox(Album album, CheckBox checkBox) {
        checkBox.setChecked(isChecked(album));
        checkBox.setTag(album);
    }

    /**
     * Should be called when album view is clicked
     * 
     * @param buttonView
     * @param isChecked
     */
    public void onAlbumViewClicked(
            CompoundButton buttonView,
            boolean isChecked) {

        Album album = (Album) buttonView.getTag();
        if (isChecked)
        {
            checkedAlbums.put(album.getId(), album.getName());
        }
        else
        {
            checkedAlbums.remove(album.getId());
        }
        buttonView.setChecked(isChecked);

    }

    protected boolean isChecked(Album album)
    {
        boolean result = false;
        if (album != null)
        {
            return isChecked(album.getId());
        }
        return result;
    }

    protected boolean isChecked(String albumId)
    {
        boolean result = false;
        if (albumId != null)
        {
            result = checkedAlbums.containsKey(albumId);
        }
        return result;
    }
}
