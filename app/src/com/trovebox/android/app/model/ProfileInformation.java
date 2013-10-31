
package com.trovebox.android.app.model;

import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Class representing profile information on Trovebox
 * 
 * @author Eugene Popovich
 */
public class ProfileInformation implements Parcelable {

    private String mId;
    private String mEmail;
    private String mName;
    private boolean mOwner;
    private boolean mPaid;
    private String mPhotoUrl;
    private ProfileCounters mCounters;
    private ProfileLimits mLimits;
    private AccessPermissions mPermissions;
    private ProfileInformation mViewer;

    private ProfileInformation() {
    }

    public static ProfileInformation fromJson(JSONObject json) throws JSONException {
        ProfileInformation profileInformation = new ProfileInformation();
        profileInformation.mId = json.optString("id");
        profileInformation.mEmail = json.optString("email");
        profileInformation.mName = json.optString("name");
        profileInformation.mPhotoUrl = json.optString("photoUrl");
        profileInformation.mOwner = json.optBoolean("isOwner");
        profileInformation.mPaid = json.optBoolean("paid");

        JSONObject countersJson = json.optJSONObject("counts");
        if (countersJson != null) {
            profileInformation.mCounters = ProfileCounters.fromJson(countersJson);
        }
        JSONObject limitsJson = json.optJSONObject("limit");
        if (limitsJson != null) {
            profileInformation.mLimits = ProfileLimits.fromJson(limitsJson);
        }
        JSONObject permissionsJson = json.optJSONObject("permission");
        if (permissionsJson != null) {
            profileInformation.mPermissions = AccessPermissions.fromJson(permissionsJson);
        }
        JSONObject viewerJson = json.optJSONObject("viewer");
        if (viewerJson != null) {
            profileInformation.mViewer = ProfileInformation.fromJson(viewerJson);
        }
        return profileInformation;
    }

    public String getId() {
        return mId;
    }

    public String getEmail() {
        return mEmail;
    }

    public String getName() {
        return mName;
    }

    public boolean isOwner() {
        return mOwner;
    }

    public boolean isPaid() {
        return mPaid;
    }

    public String getPhotoUrl() {
        return mPhotoUrl;
    }

    public ProfileCounters getCounters() {
        return mCounters;
    }

    public ProfileLimits getLimits() {
        return mLimits;
    }

    public AccessPermissions getPermissions() {
        return mPermissions;
    }

    public ProfileInformation getViewer() {
        return mViewer;
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
        out.writeString(mEmail);
        out.writeString(mName);
        out.writeByte((byte) (mOwner ? 1 : 0));
        out.writeByte((byte) (mPaid ? 1 : 0));
        out.writeString(mPhotoUrl);
        out.writeParcelable(mCounters, flags);
        out.writeParcelable(mLimits, flags);
        out.writeParcelable(mPermissions, flags);
        out.writeParcelable(mViewer, flags);
    }

    public static final Parcelable.Creator<ProfileInformation> CREATOR = new Parcelable.Creator<ProfileInformation>() {
        @Override
        public ProfileInformation createFromParcel(Parcel in) {
            return new ProfileInformation(in);
        }

        @Override
        public ProfileInformation[] newArray(int size) {
            return new ProfileInformation[size];
        }
    };

    private ProfileInformation(Parcel in) {
        this();
        mId = in.readString();
        mEmail = in.readString();
        mName = in.readString();
        mOwner = in.readByte() == 1;
        mPaid = in.readByte() == 1;
        mPhotoUrl = in.readString();
        mCounters = in.readParcelable(ProfileInformation.class.getClassLoader());
        mLimits = in.readParcelable(ProfileInformation.class.getClassLoader());
        mPermissions = in.readParcelable(ProfileInformation.class.getClassLoader());
        mViewer = in.readParcelable(ProfileInformation.class.getClassLoader());
    }

    /**
     * Class to represent profile response counters
     */
    public static class ProfileCounters implements Parcelable {
        int albums;
        int photos;
        int tags;
        long storage;

        private ProfileCounters() {
        }

        public static ProfileCounters fromJson(JSONObject json) throws JSONException {
            ProfileCounters counters = new ProfileCounters();

            counters.albums = json.optInt("albums");
            counters.photos = json.optInt("photos");
            counters.tags = json.optInt("tags");
            counters.storage = json.optLong("storage");

            return counters;
        }

        public int getAlbums() {
            return albums;
        }

        public int getPhotos() {
            return photos;
        }

        public int getTags() {
            return tags;
        }

        public long getStorage() {
            return storage;
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
            out.writeInt(albums);
            out.writeInt(photos);
            out.writeInt(tags);
            out.writeLong(storage);
        }

        public static final Parcelable.Creator<ProfileCounters> CREATOR = new Parcelable.Creator<ProfileCounters>() {
            @Override
            public ProfileCounters createFromParcel(Parcel in) {
                return new ProfileCounters(in);
            }

            @Override
            public ProfileCounters[] newArray(int size) {
                return new ProfileCounters[size];
            }
        };

        private ProfileCounters(Parcel in) {
            this();
            albums = in.readInt();
            photos = in.readInt();
            tags = in.readInt();
            storage = in.readLong();
        }
    }

    /**
     * Class to represent profile response limits
     */
    public static class ProfileLimits implements Parcelable {
        int mRemaining;
        int mResetsInDays;
        Date mResetsOn;

        private ProfileLimits() {
        }

        public static ProfileLimits fromJson(JSONObject json) throws JSONException {
            ProfileLimits limits = new ProfileLimits();

            limits.mRemaining = json.optInt("remaining");
            limits.mResetsInDays = json.optInt("resetsInDays");
            long resetsOn = json.optLong("resetsOn");
            if (resetsOn != 0) {
                limits.mResetsOn = new Date(resetsOn);
            }
            return limits;
        }

        public int getRemaining() {
            return mRemaining;
        }

        public int getResetsInDays() {
            return mResetsInDays;
        }

        public Date getResetsOn() {
            return mResetsOn;
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
            out.writeInt(mRemaining);
            out.writeInt(mResetsInDays);
            out.writeLong(mResetsOn == null ? 0l : mResetsOn.getTime());
        }

        public static final Parcelable.Creator<ProfileLimits> CREATOR = new Parcelable.Creator<ProfileLimits>() {
            @Override
            public ProfileLimits createFromParcel(Parcel in) {
                return new ProfileLimits(in);
            }

            @Override
            public ProfileLimits[] newArray(int size) {
                return new ProfileLimits[size];
            }
        };

        private ProfileLimits(Parcel in) {
            this();
            mRemaining = in.readInt();
            mResetsInDays = in.readInt();
            long value = in.readLong();
            mResetsOn = value == 0l ? null : new Date(value);
        }
    }

    public static class AccessPermissions implements Parcelable {
        public static final String CREATE_KEY = "C";
        public static final String READ_KEY = "R";
        public static final String UPDATE_KEY = "U";
        public static final String DELETE_KEY = "D";

        private boolean mFullCreateAccess;
        private String[] mCreateAlbumAccessIds;
        private boolean mFullReadAccess;
        private String[] mReadAlbumAccessIds;
        private boolean mFullUpdateAccess;
        private String[] mUpdateAlbumAccessIds;
        private boolean mFullDeleteAccess;
        private String[] mDeleteAlbumAccessIds;
        private String mJsonString;

        private AccessPermissions() {
        }

        public static AccessPermissions fromJson(JSONObject json) throws JSONException {
            AccessPermissions permissions = new AccessPermissions();
            permissions.mJsonString = json.toString();

            {
                Object obj = json.get(CREATE_KEY);
                if (obj instanceof Boolean) {
                    permissions.mFullCreateAccess = (Boolean) obj;
                } else if (obj instanceof JSONArray) {
                    JSONArray array = (JSONArray) obj;
                    permissions.mFullCreateAccess = false;
                    permissions.mCreateAlbumAccessIds = new String[array.length()];
                    for (int i = 0, size = array.length(); i < size; i++) {
                        permissions.mCreateAlbumAccessIds[i] = array.getString(i);
                    }
                }
            }
            {
                Object obj = json.get(READ_KEY);
                if (obj instanceof Boolean) {
                    permissions.mFullReadAccess = (Boolean) obj;
                } else if (obj instanceof JSONArray) {
                    JSONArray array = (JSONArray) obj;
                    permissions.mFullReadAccess = false;
                    permissions.mReadAlbumAccessIds = new String[array.length()];
                    for (int i = 0, size = array.length(); i < size; i++) {
                        permissions.mReadAlbumAccessIds[i] = array.getString(i);
                    }
                }
            }
            {
                Object obj = json.get(UPDATE_KEY);
                if (obj instanceof Boolean) {
                    permissions.mFullUpdateAccess = (Boolean) obj;
                } else if (obj instanceof JSONArray) {
                    JSONArray array = (JSONArray) obj;
                    permissions.mFullUpdateAccess = false;
                    permissions.mUpdateAlbumAccessIds = new String[array.length()];
                    for (int i = 0, size = array.length(); i < size; i++) {
                        permissions.mUpdateAlbumAccessIds[i] = array.getString(i);
                    }
                }
            }
            {
                Object obj = json.get(DELETE_KEY);
                if (obj instanceof Boolean) {
                    permissions.mFullDeleteAccess = (Boolean) obj;
                } else if (obj instanceof JSONArray) {
                    JSONArray array = (JSONArray) obj;
                    permissions.mFullDeleteAccess = false;
                    permissions.mDeleteAlbumAccessIds = new String[array.length()];
                    for (int i = 0, size = array.length(); i < size; i++) {
                        permissions.mDeleteAlbumAccessIds[i] = array.getString(i);
                    }
                }
            }
            return permissions;
        }

        public String toJsonString() {
            return mJsonString;
        }
        
        public boolean isFullCreateAccess() {
            return mFullCreateAccess;
        }

        public String[] getCreateAlbumAccessIds() {
            return mCreateAlbumAccessIds;
        }

        public boolean isFullReadAccess() {
            return mFullReadAccess;
        }

        public String[] getReadAlbumAccessIds() {
            return mReadAlbumAccessIds;
        }

        public boolean isFullUpdateAccess() {
            return mFullUpdateAccess;
        }

        public String[] getUpdateAlbumAccessIds() {
            return mUpdateAlbumAccessIds;
        }

        public boolean isFullDeleteAccess() {
            return mFullDeleteAccess;
        }

        public String[] getDeleteAlbumAccessIds() {
            return mDeleteAlbumAccessIds;
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
            out.writeByte((byte) (mFullCreateAccess ? 1 : 0));
            out.writeInt(mCreateAlbumAccessIds == null ? -1 : mCreateAlbumAccessIds.length);
            if (mCreateAlbumAccessIds != null) {
                out.writeStringArray(mCreateAlbumAccessIds);
            }
            out.writeByte((byte) (mFullReadAccess ? 1 : 0));
            out.writeInt(mReadAlbumAccessIds == null ? -1 : mReadAlbumAccessIds.length);
            if (mReadAlbumAccessIds != null) {
                out.writeStringArray(mReadAlbumAccessIds);
            }
            out.writeByte((byte) (mFullUpdateAccess ? 1 : 0));
            out.writeInt(mUpdateAlbumAccessIds == null ? -1 : mUpdateAlbumAccessIds.length);
            if (mUpdateAlbumAccessIds != null) {
                out.writeStringArray(mUpdateAlbumAccessIds);
            }
            out.writeByte((byte) (mFullDeleteAccess ? 1 : 0));
            out.writeInt(mDeleteAlbumAccessIds == null ? -1 : mDeleteAlbumAccessIds.length);
            if (mDeleteAlbumAccessIds != null) {
                out.writeStringArray(mDeleteAlbumAccessIds);
            }
            out.writeString(mJsonString);
        }

        public static final Parcelable.Creator<AccessPermissions> CREATOR = new Parcelable.Creator<AccessPermissions>() {
            @Override
            public AccessPermissions createFromParcel(Parcel in) {
                return new AccessPermissions(in);
            }

            @Override
            public AccessPermissions[] newArray(int size) {
                return new AccessPermissions[size];
            }
        };

        private AccessPermissions(Parcel in) {
            this();
            mFullCreateAccess = in.readByte() == 1;
            int mCreateAlbumAccessIdsSize = in.readInt();
            if (mCreateAlbumAccessIdsSize != -1) {
                mCreateAlbumAccessIds = new String[mCreateAlbumAccessIdsSize];
                in.readStringArray(mCreateAlbumAccessIds);
            }
            mFullReadAccess = in.readByte() == 1;
            int mReadAlbumAccessIdsSize = in.readInt();
            if (mReadAlbumAccessIdsSize != -1) {
                mReadAlbumAccessIds = new String[mReadAlbumAccessIdsSize];
                in.readStringArray(mReadAlbumAccessIds);
            }
            mFullUpdateAccess = in.readByte() == 1;
            int mUpdateAlbumAccessIdsSize = in.readInt();
            if (mUpdateAlbumAccessIdsSize != -1) {
                mUpdateAlbumAccessIds = new String[mUpdateAlbumAccessIdsSize];
                in.readStringArray(mUpdateAlbumAccessIds);
            }
            mFullDeleteAccess = in.readByte() == 1;
            int mDeleteAlbumAccessIdsSize = in.readInt();
            if (mDeleteAlbumAccessIdsSize != -1) {
                mDeleteAlbumAccessIds = new String[mDeleteAlbumAccessIdsSize];
                in.readStringArray(mDeleteAlbumAccessIds);
            }
            mJsonString = in.readString();
        }
    }
}
