
package com.trovebox.android.app.net;

import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * The class to represent API profile json response
 * 
 * @author Eugene Popovich
 */
public class ProfileResponse extends TroveboxResponse {
    private String id;
    private String email;
    private String name;
    private boolean owner;
    private boolean paid;
    private String photoUrl;
    private ProfileCounters counters;
    private ProfileLimits limits;

    public ProfileResponse(JSONObject json) throws JSONException {
        super(RequestType.PROFILE, json);
        json = json.optJSONObject("result");
        if (json != null)
        {
            id = json.optString("id");
            email = json.optString("email");
            name = json.optString("name");
            photoUrl = json.optString("photoUrl");
            owner = json.optBoolean("isOwner");
            paid = json.optBoolean("paid");

            JSONObject countersJson = json.optJSONObject("counts");
            if (countersJson != null)
            {
                counters = ProfileCounters.fromJson(countersJson);
            }
            JSONObject limitsJson = json.optJSONObject("limit");
            if (limitsJson != null)
            {
                limits = ProfileLimits.fromJson(limitsJson);
            }
        }
    }

    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public boolean isOwner() {
        return owner;
    }

    public boolean isPaid() {
        return paid;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public ProfileCounters getCounters() {
        return counters;
    }

    public ProfileLimits getLimits() {
        return limits;
    }

    /**
     * Class to represent profile response counters
     */
    public static class ProfileCounters
    {
        int albums;
        int photos;
        int tags;
        long storage;

        private ProfileCounters() {
        }

        public static ProfileCounters fromJson(JSONObject json)
        {
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
    }

    /**
     * Class to represent profile response limits
     */
    public static class ProfileLimits
    {
        int remaining;
        int resetsInDays;
        Date resetsOn;

        private ProfileLimits() {
        }

        public static ProfileLimits fromJson(JSONObject json)
        {
            ProfileLimits limits = new ProfileLimits();

            limits.remaining = json.optInt("remaining");
            limits.resetsInDays = json.optInt("resetsInDays");
            long resetsOn = json.optLong("resetsOn");
            if (resetsOn != 0)
            {
                limits.resetsOn = new Date(resetsOn);
            }
            return limits;
        }

        public int getRemaining() {
            return remaining;
        }

        public int getResetsInDays() {
            return resetsInDays;
        }

        public Date getResetsOn() {
            return resetsOn;
        }
    }
}
