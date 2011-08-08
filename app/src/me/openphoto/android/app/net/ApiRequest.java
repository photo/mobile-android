
package me.openphoto.android.app.net;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

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
    private final List<NameValuePair> mParameters;
    private final List<NameValuePair> mHeaders;

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
        mParameters = new ArrayList<NameValuePair>();
        mHeaders = new ArrayList<NameValuePair>();
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

    /**
     * Adds a request parameter.
     * 
     * @param name the name
     * @param value the value
     */
    public void addParameter(String name, String value) {
        mParameters.add(new BasicNameValuePair(name, value));
    }

    /**
     * @return the parameters specified in the ApiRequest object
     */
    public List<NameValuePair> getParameters() {
        return mParameters;
    }

    /**
     * Add a header to be sent with the request
     * 
     * @param name the header name (without :)
     * @param value the value
     */
    public void addHeader(String name, String value) {
        mHeaders.add(new BasicNameValuePair(name, value));

    }

    /**
     * @return the headers specified in the ApiRequest object
     */
    public List<NameValuePair> getHeaders() {
        return mHeaders;
    }
}
