
package me.openphoto.android.app.net;

import java.util.ArrayList;
import java.util.List;

import me.openphoto.android.app.model.Tag;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TagsResponse extends OpenPhotoResponse {
    private final List<Tag> mTags;

    public TagsResponse(JSONObject json) throws JSONException {
        super(json);
        JSONArray data = json.getJSONArray("result");
        mTags = new ArrayList<Tag>(data.length());
        for (int i = 0; i < data.length(); i++) {
            mTags.add(Tag.fromJson(data.getJSONObject(i)));
        }
    }

    /**
     * @return the tags retrieved from the server
     */
    public List<Tag> getTags() {
        return mTags;
    }
}
