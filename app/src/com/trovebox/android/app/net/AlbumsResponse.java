
package com.trovebox.android.app.net;

import java.util.ArrayList;
import java.util.List;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.trovebox.android.app.model.Album;

/**
 * @author Eugene Popovich
 */
public class AlbumsResponse extends TroveboxResponse
{
    private final List<Album> mAlbums;

    public AlbumsResponse(JSONObject json) throws JSONException
    {
        super(json);
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
}
