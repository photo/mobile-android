
package com.trovebox.android.app.model;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Class representing a Album on Trovebox.
 * 
 * @author Eugene Popovich
 */
public class Album implements Parcelable
{
    public static final int VISIBILITY_VISIBLE = 1;
    public static final int VISIBILITY_INVISIBLE = 0;
    protected String mId;
    protected String mOwner;
    protected String mName;
    protected int mVisible;
    protected int mCount;
    protected Photo mCover;

    private Album()
    {

    }

    /**
     * Creates a Album object from json.
     * 
     * @param json JSONObject of the Album as received from the Trovebox API
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

    /*****************************
     * PARCELABLE IMPLEMENTATION *
     *****************************/
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(mId);
        out.writeString(mOwner);
        out.writeString(mName);
        out.writeInt(mVisible);
        out.writeInt(mCount);
        out.writeParcelable(mCover, flags);
    }

    public static final Parcelable.Creator<Album> CREATOR = new Parcelable.Creator<Album>() {
        @Override
        public Album createFromParcel(Parcel in) {
            return new Album(in);
        }

        @Override
        public Album[] newArray(int size) {
            return new Album[size];
        }
    };

    private Album(Parcel in) {
        this();
        mId = in.readString();
        mOwner = in.readString();
        mName = in.readString();
        mVisible = in.readInt();
        mCount = in.readInt();
        mCover = in.readParcelable(Album.class.getClassLoader());
    }
}
