
package com.trovebox.android.common.model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Class representing a Tag on Trovebox.
 * 
 * @author Patrick Boos
 */
public class Tag {
    private String mTag;
    private int mCount;

    /**
     * Constructor which should not be used externally. All object creation
     * should work through fromJson().
     */
    private Tag() {
    }

    /**
     * Creates a Tag object from json.
     * 
     * @param json JSONObject of the Tag as received from the Trovebox API
     * @return Tag as represented in the given json
     * @throws JSONException
     */
    public static Tag fromJson(JSONObject json) throws JSONException {
        Tag tag = new Tag();
        tag.mTag = json.getString("id");
        tag.mCount = json.getInt("count");
        return tag;
    }

    /**
     * Creates a Tag object for tag name
     * 
     * @param tag the tag name
     * @return
     */
    public static Tag fromTagName(String tag) {
        Tag result = new Tag();
        result.mTag = tag;
        return result;
    }

    /**
     * @return The tag string
     */
    public String getTag() {
        return mTag;
    }

    /**
     * @return The amount of pictures that have this tag.
     */
    public int getCount() {
        return mCount;
    }

    @Override
    public String toString() {
        return mTag;
    }
}
