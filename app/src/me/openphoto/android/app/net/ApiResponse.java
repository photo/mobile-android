
package me.openphoto.android.app.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

import org.apache.http.HttpResponse;

/**
 * ApiResponse represents the response returned from an API. It gives easier
 * access to a given HttpResponse.
 * 
 * @author Patrick Boos
 */
public class ApiResponse {
    private HttpResponse mResponse;

    /**
     * Constructor.
     * 
     * @param response HttpResponse on which this ApiResponse is built.
     */
    public ApiResponse(HttpResponse response) {
        mResponse = response;
    }

    /**
     * Gets the content as string.
     * 
     * @return the content as string
     * @throws IllegalStateException
     * @throws IOException
     */
    public String getContentAsString() throws IllegalStateException, IOException {
        return convertStreamToString(mResponse.getEntity().getContent());
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
}
