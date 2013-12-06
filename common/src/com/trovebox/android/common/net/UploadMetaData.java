
package com.trovebox.android.common.net;

import java.util.Map;

import android.os.Parcel;
import android.os.Parcelable;

import com.trovebox.android.common.model.Photo;

public class UploadMetaData implements Parcelable {
    private String mTitle;
    private String mDescription;
    private String mTags;
    private int mPermission;
    private Map<String, String> mAlbums;

    public UploadMetaData() {
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setDescription(String description) {
        mDescription = description;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setTags(String tags) {
        mTags = tags;
    }

    public String getTags() {
        return mTags;
    }

    public int getPermission() {
        return mPermission;
    }

    public void setPermission(int permission) {
        mPermission = permission;
    }

    public void setPrivate(boolean setPrivate) {
        setPermission(setPrivate ? Photo.PERMISSION_PRIVATE : Photo.PERMISSION_PUBLIC);
    }

    public boolean isPrivate() {
        return getPermission() == Photo.PERMISSION_PRIVATE;
    }

    public Map<String, String> getAlbums() {
        return mAlbums;
    }

    public void setAlbums(Map<String, String> albums) {
        this.mAlbums = albums;
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
        out.writeString(mTitle);
        out.writeString(mDescription);
        out.writeString(mTags);
        out.writeInt(mPermission);
        out.writeMap(mAlbums);
    }

    public static final Parcelable.Creator<UploadMetaData> CREATOR = new Parcelable.Creator<UploadMetaData>() {
        @Override
        public UploadMetaData createFromParcel(Parcel in) {
            return new UploadMetaData(in);
        }

        @Override
        public UploadMetaData[] newArray(int size) {
            return new UploadMetaData[size];
        }
    };

    @SuppressWarnings("unchecked")
    private UploadMetaData(Parcel in) {
        mTitle = in.readString();
        mDescription = in.readString();
        mTags = in.readString();
        mPermission = in.readInt();
        mAlbums = in.readHashMap(getClass().getClassLoader());
    }
}
