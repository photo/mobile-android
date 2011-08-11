
package me.openphoto.android.app.net;

import java.io.InputStream;
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
    private final List<Parameter<?>> mParameters;
    private final List<NameValuePair> mHeaders;
    private boolean mIsMime;

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
        mParameters = new ArrayList<Parameter<?>>();
        mHeaders = new ArrayList<NameValuePair>();
        mIsMime = false;
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
        mParameters.add(new Parameter<String>(name, value));
    }

    /**
     * Add a inputStream for a POST MIME upload request.<br />
     * This will throw an exception if isMime() != true or this is not a POST
     * request.
     * 
     * @param name the name
     * @param inputStream the inputStream which is the file/value
     */
    public void addParameter(String name, InputStream inputStream) {
        if (!isMime() || getMethod() != POST) {
            throw new IllegalStateException("Only possible if POST and MIME is used.");
        }
        mParameters.add(new Parameter<InputStream>(name, inputStream));
    }

    /**
     * Will return the parameters for a non MIME request.<br />
     * Note that if this is a MIME request with InputStream parameters, those
     * will not be returned herewith.
     * 
     * @return the string parameters
     */
    public List<NameValuePair> getParameters() {
        List<NameValuePair> result = new ArrayList<NameValuePair>(mParameters.size());
        for (Parameter<?> parameter : mParameters) {
            if (parameter.getValue() instanceof String) {
                result.add(new BasicNameValuePair(parameter.getName(), (String) parameter
                        .getValue()));
            }
        }
        return result;
    }

    /**
     * @return all the parameters
     */
    public List<Parameter<?>> getParametersMime() {
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

    /**
     * @return if this request will use MIME
     */
    public boolean isMime() {
        return mIsMime;
    }

    /**
     * Set if this request should use MIME.
     * 
     * @param isMime true if should use MIME
     */
    public void setMime(boolean isMime) {
        if (isMime && getMethod() != POST) {
            throw new IllegalStateException("Needs to be a POST request to be MIME!");
        }
        mIsMime = isMime;
    }

    /**
     * A parameter for a request.
     * 
     * @author Patrick Boos
     * @param <T> Type of the Value
     */
    public class Parameter<T> {
        private String mName;
        private T mValue;

        public Parameter(String name, T value) {
            mName = name;
            mValue = value;
        }

        /**
         * @return Name of the parameter
         */
        public String getName() {
            return mName;
        }

        /**
         * @return Value of the parameter
         */
        public T getValue() {
            return mValue;
        }
    }
}
