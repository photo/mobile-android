
package com.trovebox.android.common.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

import com.trovebox.android.common.util.CommonUtils;
import com.trovebox.android.common.util.GuiUtils;
import com.trovebox.android.common.util.TrackerUtils;

/**
 * ApiResponse represents the response returned from an API. It gives easier
 * access to a given HttpResponse.
 * 
 * @author Patrick Boos
 */
public class ApiResponse {
    private static final String TAG = ApiResponse.class.getSimpleName();
    private final HttpResponse mResponse;
    private JSONObject jsonObject;
    private boolean jsonParseError = false;
    private String content;
    String requestUrl;

    /**
     * Constructor.
     * 
     * @param requestUrl the url for the request. Used for logging
     * @param response HttpResponse on which this ApiResponse is built.
     */
    public ApiResponse(String requestUrl, HttpResponse response) {
        mResponse = response;
        this.requestUrl = requestUrl;
    }

    /**
     * Gets the content as string.
     * 
     * @return the content as string
     * @throws IllegalStateException
     * @throws IOException
     */
    public String getContentAsString() throws IllegalStateException, IOException {
        if (content == null)
        {
            content = convertStreamToString(mResponse.getEntity().getContent());
        }
        return content;
    }

    /**
     * Returns true if the server sent the given header in the response.
     * 
     * @param name name of the header
     * @return true if the header exists
     */
    public boolean hasHeader(String name) {
        return mResponse.getFirstHeader(name) != null;
    }

    /**
     * Get the value for a header.
     * 
     * @param name name of the header
     * @return value of the header
     */
    public String getHeader(String name) {
        if (!hasHeader(name)) {
            return null;
        }
        return mResponse.getFirstHeader(name).getValue();
    }

    /**
     * Get the status code delivered by the server.
     * 
     * @return status code
     */
    public int getStatusCode() {
        return mResponse.getStatusLine().getStatusCode();
    }

    /**
     * Convert a InputStream into String
     * 
     * @param is inputStream to be converted into a string
     * @return content of InputStream in form of String
     * @throws IOException
     */
    private String convertStreamToString(final InputStream is) throws IOException {
        if (is != null) {
            Writer writer = new StringWriter();

            char[] buffer = new char[1024];
            try {
                Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                int n;
                while ((n = reader.read(buffer)) != -1) {
                    writer.write(buffer, 0, n);
                }
            } finally {
                is.close();
            }
            return writer.toString();
        } else {
            return "";
        }
    }

    /**
     * Checks the http response code whether it is success (200 <= code < 300)
     * 
     * @return true if getStatusCode() is 200 or more and less than 300
     */
    public boolean isSuccess() {
        int statusCode = getStatusCode();
        return statusCode >= HttpStatus.SC_OK && statusCode < 300;
    }

    /**
     * Get the json object parsed from the response content
     * 
     * @return
     * @throws IllegalStateException
     * @throws IOException
     * @throws InvalidApiResponseException if JSONException occurs during
     *             content parse
     */
    public JSONObject getJSONObject() throws IllegalStateException, IOException
    {
        if (jsonObject == null && !jsonParseError)
        {
            try
            {
                jsonObject = new JSONObject(getContentAsString());
            } catch (JSONException ex)
            {
                GuiUtils.noAlertError(TAG, ex);
                jsonParseError = true;
                String error = CommonUtils.format(
                        "Invalid JSON Response. Status code: %1$d; Reason: %2$s; Path: %3$s",
                        getStatusCode(),
                        mResponse.getStatusLine()
                                .getReasonPhrase(),
                        requestUrl);
                CommonUtils.error(TAG, error);
                TrackerUtils.trackErrorEvent("invalid_json_response",
                        error);
                // advanced log to investigate invalid json responses causes
                {
                    String error2 = CommonUtils
                            .format(
                                    "Invalid JSON Response. Status code: %1$d; Reason: %2$s; Path: %3$s; Content:\n%4$s",
                                    getStatusCode(),
                                    mResponse.getStatusLine()
                                            .getReasonPhrase(),
                                    requestUrl,
                                    getContentAsString()
                            );
                    TrackerUtils.trackErrorEvent("invalid_json_response_advanced",
                            error2);
                }
                throw new InvalidApiResponseException(error);
            }
        }
        return jsonObject;
    }

    public static class InvalidApiResponseException extends RuntimeException
    {
        private static final long serialVersionUID = 1L;

        public InvalidApiResponseException(String message)
        {
            super(message);
        }
    }
}
