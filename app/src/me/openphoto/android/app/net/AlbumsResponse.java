
package me.openphoto.android.app.net;

import java.util.ArrayList;
import java.util.List;

import me.openphoto.android.app.model.Album;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Eugene Popovich
 * @version
 *          02.10.2012
 *          <br>-created
 */
public class AlbumsResponse extends OpenPhotoResponse
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
