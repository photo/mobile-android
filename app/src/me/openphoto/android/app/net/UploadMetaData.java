
package me.openphoto.android.app.net;

public class UploadMetaData {
    private String mTitle;
    private String mDescription;
    private String mTags;

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

    // TODO add dateUploaded, dateTaken, latitude, longitude, returnSizes
}
