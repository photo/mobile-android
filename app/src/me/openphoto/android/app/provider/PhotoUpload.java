
package me.openphoto.android.app.provider;

import me.openphoto.android.app.net.UploadMetaData;
import android.net.Uri;

public class PhotoUpload {
    private final long mId;
    private final Uri mPhotoUri;
    private final UploadMetaData mMetaData;
    private String mError;
    private boolean mIsAutoUpload;

    public PhotoUpload(long id, Uri photoUri, UploadMetaData metaData) {
        mId = id;
        mPhotoUri = photoUri;
        mMetaData = metaData;
    }

    public long getId() {
        return mId;
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
}
