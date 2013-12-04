
package com.trovebox.android.common.net;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.text.TextUtils;

import com.trovebox.android.common.CommonConfigurationUtils;

/**
 * ApiRequest holds the information for a call to the API.
 * 
 * @author Patrick Boos
 */
public class ApiRequest {
    public enum ApiVersion {
        NO_VERSION, V1("v1", "/v1"), V2("v2", "/v2");

        private String prefix;
        private String name;

        public String getPrefix() {
            return prefix;
        }

        public String getName() {
            return name;
        }

        public String correctPathWithPrefix(String path) {
            return prefix == null ? path : prefix + path;
        }

        ApiVersion() {
            this(null, null);
        }

        ApiVersion(String name, String prefix) {
            this.name = name;
            this.prefix = prefix;
        }

        public static ApiVersion getApiVersionByName(String name) {
            ApiVersion result = null;
            for (ApiVersion v : values()) {
                if (TextUtils.equals(name, v.name)) {
                    result = v;
                    break;
                }
            }
            if (result == null) {
                result = NO_VERSION;
            }
            return result;
        }
    }
    public static final int GET = 1;
    public static final int POST = 2;
    public static final int PUT = 3;
    public static final int DELETE = 4;

    private final int mMethod;
    private final String mPath;
    private final List<Parameter<?>> mParameters;
    private final List<NameValuePair> mHeaders;
    private boolean mIsMime;
    private ApiVersion apiVersion;

    /**
     * Creates a basic ApiRequest
     * 
     * @param requestMethod Method of the HTTP request. Must be either GET,
     *            POST, PUT or DELETE.
     * @param path Path of the url. Must start with '/'.
     */
    public ApiRequest(int requestMethod, String path) {
        this(requestMethod, path, CommonConfigurationUtils.getCurrentApiVersion());
    }

    /**
     * Creates a basic ApiRequest
     * 
     * @param requestMethod Method of the HTTP request. Must be either GET,
     *            POST, PUT or DELETE.
     * @param path Path of the url. Must start with '/'.
     * @param apiVersion the api version for this request. This may change the
     *            path in the getPath method
     */
    public ApiRequest(int requestMethod, String path, ApiVersion apiVersion) {
        if (!path.startsWith("/")) {
            throw new IllegalArgumentException("Parameter 'path' must start with '/'.");
        }
        if (requestMethod != GET && requestMethod != POST && requestMethod != PUT
                && requestMethod != DELETE) {
            throw new IllegalArgumentException(
                    "Parameter 'requestMethod' must be either GET, POST, PUT or DELETE.");
        }
        this.apiVersion = apiVersion;
        mMethod = requestMethod;
        mPath = path;
        mParameters = new ArrayList<Parameter<?>>();
        mHeaders = new ArrayList<NameValuePair>();
        mIsMime = false;
    }

    /**
     * @return Path of the request suitable for the api version
     */
    public String getPath() {
        return apiVersion.correctPathWithPrefix(mPath);
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
     * Add a file for a POST MIME upload request.<br />
     * This will throw an exception if isMime() != true or this is not a POST
     * request.
     * 
     * @param name the name
     * @param file the file to be added
     */
    public void addFileParameter(String name, File file) {
        if (!isMime() || getMethod() != POST) {
            throw new IllegalStateException("Only possible if POST and MIME is used.");
        }
        mParameters.add(new Parameter<File>(name, file));
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

    public ApiVersion getApiVersion() {
        return apiVersion;
    }
}
