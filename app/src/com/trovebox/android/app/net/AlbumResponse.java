
package com.trovebox.android.app.net;


import org.json.JSONException;
import org.json.JSONObject;

import com.trovebox.android.app.model.Album;

public class AlbumResponse extends TroveboxResponse {
    private final Album mAlbum;

    public AlbumResponse(RequestType requestType, JSONObject json) throws JSONException {
        super(requestType, json);
        if (isSuccess() && json.get("result") instanceof JSONObject) {
            mAlbum = Album.fromJson(json.getJSONObject("result"));
        } else {
            mAlbum = null;
        }
    }

    /**
     * @return the album contained in the response from the server
     */
    public Album getAlbum() {
        return mAlbum;
    }

    /**
     * @return true if the upload was successful
     */
    @Override
    public boolean isSuccess() {
        return getCode() == 201;
    }
}
