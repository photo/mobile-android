
package me.openphoto.android.app.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

import org.apache.http.HttpResponse;

public class ApiResponse {
    private HttpResponse mResponse;

    public ApiResponse(HttpResponse response) {
        mResponse = response;
    }

    public String getContentAsString() throws IllegalStateException, IOException {
        return convertStreamToString(mResponse.getEntity().getContent());
    }

    public boolean hasHeader(String name) {
        return mResponse.getFirstHeader(name) != null;
    }

    public String getHeader(String name) {
        if (!hasHeader(name)) {
            return null;
        }
        return mResponse.getFirstHeader(name).getValue();
    }

    public int getStatusCode() {
        return mResponse.getStatusLine().getStatusCode();
    }

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
