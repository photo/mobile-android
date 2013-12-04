
package com.trovebox.android.common.net;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.trovebox.android.common.CommonConfigurationUtils;
import com.trovebox.android.common.model.Album;

/**
 * @author Eugene Popovich
 */
public class AlbumsResponse extends PagedResponse
{
    private final List<Album> mAlbums;

    public AlbumsResponse(JSONObject json) throws JSONException
    {
        super(RequestType.ALBUMS, json);
        JSONArray data = json.getJSONArray("result");
        mAlbums = new ArrayList<Album>(data.length());
        for (int i = 0; i < data.length(); i++) {
            mAlbums.add(Album.fromJson(data.getJSONObject(i)));
        }
    }

    /**
     * @return the tags retrieved from the server
     */
    public List<Album> getAlbums()
    {
        return mAlbums;
    }

    @Override
    public boolean hasNextPage() {
        if (CommonConfigurationUtils.isV2ApiAvailable()) {
            return super.hasNextPage();
        } else {
            return !mAlbums.isEmpty();
        }
    }
}
