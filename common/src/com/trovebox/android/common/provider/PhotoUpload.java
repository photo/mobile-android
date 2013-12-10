
package com.trovebox.android.common.provider;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import com.trovebox.android.common.net.UploadMetaData;

public class PhotoUpload implements Parcelable {
    private final long mId;
    private final Uri mPhotoUri;
    private final UploadMetaData mMetaData;
    private String mError;
    private boolean mIsAutoUpload;
    private boolean mShareOnTwitter;
    private boolean mShareOnFacebook;
    private String mHost;
    private String mToken;
    private String mUserName;
    private long mUploaded;

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

    public boolean isShareOnTwitter() {
        return mShareOnTwitter;
    }

    public void setShareOnTwitter(boolean shareOnTwitter) {
        this.mShareOnTwitter = shareOnTwitter;
    }

    public boolean isShareOnFacebook() {
        return mShareOnFacebook;
    }

    public void setShareOnFacebook(boolean shareOnFacebook) {
        this.mShareOnFacebook = shareOnFacebook;
    }

    public String getHost() {
        return mHost;
    }

    public void setHost(String mHost) {
        this.mHost = mHost;
    }

    public String getToken() {
        return mToken;
    }

    public void setToken(String mToken) {
        this.mToken = mToken;
    }

    public String getUserName() {
        return mUserName;
    }

    public void setUserName(String mUserName) {
        this.mUserName = mUserName;
    }

    public long getUploaded() {
        return mUploaded;
    }

    public void setUploaded(long uploaded) {
        this.mUploaded = uploaded;
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
        out.writeLong(mId);
        out.writeParcelable(mPhotoUri, flags);
        out.writeParcelable(mMetaData, flags);
        out.writeString(mError);
        out.writeByte((byte) (mIsAutoUpload ? 1 : 0));
        out.writeByte((byte) (mShareOnTwitter ? 1 : 0));
        out.writeByte((byte) (mShareOnFacebook ? 1 : 0));
        out.writeString(mHost);
        out.writeString(mToken);
        out.writeString(mUserName);
        out.writeLong(mUploaded);
    }

    public static final Parcelable.Creator<PhotoUpload> CREATOR = new Parcelable.Creator<PhotoUpload>() {
        @Override
        public PhotoUpload createFromParcel(Parcel in) {
            return new PhotoUpload(in);
        }

        @Override
        public PhotoUpload[] newArray(int size) {
            return new PhotoUpload[size];
        }
    };

    private PhotoUpload(Parcel in) {
        mId = in.readLong();
        mPhotoUri = in.readParcelable(getClass().getClassLoader());
        mMetaData = in.readParcelable(getClass().getClassLoader());
        mError = in.readString();
        mIsAutoUpload = in.readByte() == 1;
        mShareOnTwitter = in.readByte() == 1;
        mShareOnFacebook = in.readByte() == 1;
        mHost = in.readString();
        mToken = in.readString();
        mUserName = in.readString();
        mUploaded = in.readLong();
    }
}
