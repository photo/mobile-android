
package me.openphoto.android.app.model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Class representing a Album on OpenPhoto.
 * 
 * @author Eugene Popovich
 */
public class Album
{
    public static final int VISIBILITY_VISIBLE = 1;
    public static final int VISIBILITY_INVISIBLE = 0;
    protected String mId;
    protected String mOwner;
    protected String mName;
    protected int mVisible;
    protected int mCount;
    protected Photo mCover;

    /**
     * Creates a Album object from json.
     * 
     * @param json JSONObject of the Album as received from the OpenPhoto API
     * @return Album as represented in the given json
     * @throws JSONException
     */
    public static Album fromJson(JSONObject json) throws JSONException
    {
        Album album = new Album();
        album.mId = json.optString("id");
        album.mOwner = json.optString("owner");
        album.mName = json.optString("name");
        album.mVisible = json.optInt("visible");
        album.mCount = json.optInt("count");
        if (!json.isNull("cover"))
        {
            album.mCover = Photo.fromJson(json.optJSONObject("cover"));
        }
        return album;
    }

    public String getId()
    {
        return mId;
    }

    public String getOwner()
    {
        return mOwner;
    }

    public String getName()
    {
        return mName;
    }

    public int getVisible()
    {
        return mVisible;
    }

    public int getCount()
    {
        return mCount;
    }

    public Photo getCover()
    {
        return mCover;
    }
}
