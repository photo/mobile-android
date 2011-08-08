
package me.openphoto.android.app.net;

/**
 * ApiBase provides the basic functionality to call RESTful APIs using an
 * ApiRequest.
 */
public class ApiBase {
    private final String mBaseUrl;

    public ApiBase(String baseUrl) {
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        mBaseUrl = baseUrl;
    }

    public String getApiUrl() {
        return mBaseUrl;
    }
}
