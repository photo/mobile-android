
package me.openphoto.android.app.net;

/**
 * ApiRequest holds the information for a call to the API.
 * 
 * @author Patrick Boos
 */
public class ApiRequest {
    public static final int GET = 1;
    public static final int POST = 2;
    public static final int PUT = 3;
    public static final int DELETE = 4;

    private final int mMethod;
    private final String mPath;

    /**
     * Creates a basic ApiRequest
     * 
     * @param requestMethod Method of the HTTP request. Must be either GET,
     *            POST, PUT or DELETE.
     * @param path Path of the url. Must start with '/'.
     */
    public ApiRequest(int requestMethod, String path) {
        if (!path.startsWith("/")) {
            throw new IllegalArgumentException("Parameter 'path' must start with '/'.");
        }
        if (requestMethod != GET && requestMethod != POST && requestMethod != PUT
                && requestMethod != DELETE) {
            throw new IllegalArgumentException(
                    "Parameter 'requestMethod' must be either GET, POST, PUT or DELETE.");
        }
        mMethod = requestMethod;
        mPath = path;
    }

    /**
     * @return Path of the request
     */
    public String getPath() {
        return mPath;
    }

    /**
     * @return Method of the request
     */
    public int getMethod() {
        return mMethod;
    }
}
