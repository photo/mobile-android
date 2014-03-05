
package com.trovebox.android.common.model.utils;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;

import com.trovebox.android.common.CommonConfigurationUtils;
import com.trovebox.android.common.model.Photo;
import com.trovebox.android.common.model.Token;
import com.trovebox.android.common.net.TokenResponse;
import com.trovebox.android.common.util.TrackerUtils;

/**
 * Utils class for the token object
 * 
 * @author Eugene Popovich
 */
public class TokenUtils {
    private static final String TAG = TokenUtils.class.getSimpleName();

    /**
     * Performs api request to retrieve share token for the photo
     * 
     * @param photo
     * @return
     * @throws ClientProtocolException
     * @throws IOException
     * @throws JSONException
     */
    public static TokenResponse getPhotoShareTokenResponse(Photo photo)
            throws ClientProtocolException, IOException, JSONException {
        TrackerUtils.trackBackgroundEvent("getPhotoShareToken", TAG);
        long start = System.currentTimeMillis();
        TokenResponse response = CommonConfigurationUtils.getApi().createTokenForPhoto(
                photo.getId());
        TrackerUtils.trackDataLoadTiming(System.currentTimeMillis() - start, "getPhotoShareToken",
                TAG);
        return response;
    }

    /**
     * Get token suffix for share url
     * 
     * @param token
     * @return
     */
    public static String getTokenUrlSuffix(Token token) {
        return "/token-" + token.getId();
    }
}
