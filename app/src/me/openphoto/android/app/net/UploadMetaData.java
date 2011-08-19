
package me.openphoto.android.app.net;

import me.openphoto.android.app.model.Photo;

public class UploadMetaData {
    private String mTitle;
    private String mDescription;
    private String mTags;
    private int mPermission;

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

    public void setPrivate(boolean setPrivate) {
        mPermission = setPrivate ? Photo.PERMISSION_PRIVATE : Photo.PERMISSION_PUBLIC;
    }

    public int getPermission() {
        return mPermission;
    }

    // TODO add dateUploaded, dateTaken, latitude, longitude, returnSizes
}
