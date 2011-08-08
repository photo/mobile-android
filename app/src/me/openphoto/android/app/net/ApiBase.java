
package me.openphoto.android.app.net;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;

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

    public ApiResponse execute(ApiRequest request) throws ClientProtocolException, IOException {
        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpUriRequest httpRequest = createHttpRequest(request);

        // This is needed because otherwise photo upload can fail on PHP servers
        httpRequest.getParams().setBooleanParameter("http.protocol.expect-continue", false);

        return new ApiResponse(httpClient.execute(httpRequest));
    }

    private HttpUriRequest createHttpRequest(ApiRequest request)
            throws UnsupportedEncodingException {
        HttpUriRequest httpRequest = null;
        switch (request.getMethod()) {
            case ApiRequest.GET:
                httpRequest = new HttpGet(addGetParams(mBaseUrl + request.getPath(),
                        request.getParameters()));
                break;
            case ApiRequest.POST:
                httpRequest = new HttpPost(mBaseUrl + request.getPath());
                ((HttpPost) httpRequest).setEntity(new UrlEncodedFormEntity(
                        request.getParameters(), HTTP.UTF_8));
                break;
            case ApiRequest.PUT:
                httpRequest = new HttpPut(addGetParams(mBaseUrl + request.getPath(),
                        request.getParameters()));
                break;
            case ApiRequest.DELETE:
                httpRequest = new HttpDelete(addGetParams(mBaseUrl + request.getPath(),
                        request.getParameters()));
                break;
        }

        for (NameValuePair pair : request.getHeaders()) {
            request.addHeader(pair.getName(), pair.getValue());
        }

        return httpRequest;
    }

    private String addGetParams(String url, List<NameValuePair> nameValuePairs) {
        if (nameValuePairs == null || nameValuePairs.isEmpty()) {
            return url;
        }

        final StringBuffer newUrl = new StringBuffer(url);
        newUrl.append(url.contains("?") ? '&' : '?');

        for (NameValuePair param : nameValuePairs) {
            newUrl.append(param.getName() + "=" + param.getValue() + "&");
        }
        newUrl.deleteCharAt(newUrl.length() - 1);
        return newUrl.toString();
    }
}
