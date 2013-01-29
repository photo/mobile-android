
package com.trovebox.android.app.provider;

import com.trovebox.android.app.net.UploadMetaData;

import android.net.Uri;

public class PhotoUpload {
    private final long mId;
    private final Uri mPhotoUri;
    private final UploadMetaData mMetaData;
    private String mError;
    private boolean mIsAutoUpload;
    private boolean shareOnTwitter;
    private boolean shareOnFacebook;

    public PhotoUpload(long id, Uri photoUri, UploadMetaData metaData) {
        mId = id;
        mPhotoUri = photoUri;
        mMetaData = metaData;
    }

    public long getId() {
        return mId;
    }

    public Uri getUri() {
        return Uri.withAppendedPath(UploadsProvider.CONTENT_URI, Long.toString(mId));
    }

    public UploadMetaData getMetaData() {
        return mMetaData;
    }

    public Uri getPhotoUri() {
        return mPhotoUri;
    }

    public String getError() {
        return mError;
    }

    public void setError(String error) {
        mError = error;
    }

    public boolean isAutoUpload() {
        return mIsAutoUpload;
    }

    public void setIsAutoUpload(boolean isAutoUpload) {
        mIsAutoUpload = isAutoUpload;
    }

    public boolean isShareOnTwitter()
    {
        return shareOnTwitter;
    }

    public void setShareOnTwitter(boolean shareOnTwitter)
    {
        this.shareOnTwitter = shareOnTwitter;
    }

    public boolean isShareOnFacebook()
    {
        return shareOnFacebook;
    }

    public void setShareOnFacebook(boolean shareOnFacebook)
    {
        this.shareOnFacebook = shareOnFacebook;
    }

}
