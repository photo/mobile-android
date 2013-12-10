
package com.trovebox.android.common.net;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import oauth.signpost.OAuthConsumer;

import org.apache.http.HttpEntity;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import com.trovebox.android.common.CommonConfigurationUtils;
import com.trovebox.android.common.net.ApiRequest.Parameter;
import com.trovebox.android.common.net.HttpEntityWithProgress.ProgressListener;
import com.trovebox.android.common.util.GuiUtils;
import com.trovebox.android.common.util.TrackerUtils;

/**
 * ApiBase provides the basic functionality to call RESTful APIs using an
 * ApiRequest.
 * 
 * @author Patrick Boos
 */
public class ApiBase {
    public final static String TAG = ApiBase.class.getSimpleName();

    public static int NetworkConnectionTimeout_ms = 10000;

    public ApiBase() {
    }

    /**
     * Execute a request to the API.
     * 
     * @param request request to perform
     * @return the response from the API
     * @throws ClientProtocolException
     * @throws IOException
     */
    public ApiResponse execute(ApiRequest request)
            throws ClientProtocolException, IOException {
        return execute(request, CommonConfigurationUtils.getServer());
    }

    /**
     * Execute a request to the API.
     * 
     * @param request request to perform
     * @param baseUrl the base server url
     * @return the response from the API
     * @throws ClientProtocolException
     * @throws IOException
     */
    public ApiResponse execute(ApiRequest request, String baseUrl) throws ClientProtocolException,
            IOException {
        return execute(request, baseUrl, null, NetworkConnectionTimeout_ms);
    }

    /**
     * Execute a request to the API.
     * 
     * @param request request to perform
     * @param listener Progress Listener with callback on progress
     * @return the response from the API
     * @throws ClientProtocolException
     * @throws IOException
     */
    public ApiResponse execute(ApiRequest request, ProgressListener listener)
            throws ClientProtocolException, IOException {
        return execute(request, CommonConfigurationUtils.getServer(),
                CommonConfigurationUtils.getOAuthConsumer(),
                listener);
    }

    /**
     * Execute a request to the API.
     * 
     * @param request request to perform
     * @param listener Progress Listener with callback on progress
     * @param connectionTimeout the connection and socket timeout
     * @return the response from the API
     * @throws ClientProtocolException
     * @throws IOException
     */
    public ApiResponse execute(ApiRequest request, ProgressListener listener, int connectionTimeout)
            throws ClientProtocolException, IOException {
        return execute(request, CommonConfigurationUtils.getServer(),
                listener, connectionTimeout);
    }
    /**
     * Execute a request to the API.
     * 
     * @param request request to perform
     * @param baseUrl the base server url
     * @param listener Progress Listener with callback on progress
     * @param connectionTimeout the connection and socket timeout
     * @return the response from the API
     * @throws ClientProtocolException
     * @throws IOException
     */
    public ApiResponse execute(ApiRequest request, String baseUrl, ProgressListener listener, int connectionTimeout)
            throws ClientProtocolException, IOException {
        return execute(request, baseUrl,
                CommonConfigurationUtils.getOAuthConsumer(),
                listener, connectionTimeout);
    }

    /**
     * Execute a request to the API.
     * 
     * @param request request to perform
     * @param baseUrl the base server url
     * @param consumer the oauth consumer key to sign request
     * @param listener Progress Listener with callback on progress
     * @return the response from the API
     * @throws ClientProtocolException
     * @throws IOException
     */
    public ApiResponse execute(ApiRequest request, String baseUrl, OAuthConsumer consumer,
            ProgressListener listener)
            throws ClientProtocolException, IOException {
        return execute(request, baseUrl, consumer, listener, NetworkConnectionTimeout_ms);
    }

    /**
     * Execute a request to the API.
     * 
     * @param request request to perform
     * @param baseUrl the base server url
     * @param consumer the oauth consumer key to sign request
     * @param listener Progress Listener with callback on progress
     * @param connectionTimeout the connection and socket timeout
     * @return the response from the API
     * @throws ClientProtocolException
     * @throws IOException
     */
    public ApiResponse execute(ApiRequest request, String baseUrl, OAuthConsumer consumer,
            ProgressListener listener, int connectionTimeout)
            throws ClientProtocolException, IOException {
        // PoolingClientConnectionManager();
        HttpParams params = new BasicHttpParams();

        // set params for connection...
        HttpConnectionParams.setStaleCheckingEnabled(
                params, false);
        HttpConnectionParams.setConnectionTimeout(params,
                connectionTimeout);
        HttpConnectionParams.setSoTimeout(params,
                connectionTimeout);
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);

        DefaultHttpClient httpClient = new DefaultHttpClient(params);

        HttpUriRequest httpRequest = createHttpRequest(request, baseUrl, listener);

        httpRequest.getParams().setBooleanParameter(
                "http.protocol.expect-continue", false);
        httpRequest.setHeader("User-Agent", "android");
        httpRequest.setHeader("source", "android");

        if (consumer != null) {
            try {
                consumer.sign(httpRequest);
            } catch (Exception e) {
                GuiUtils.noAlertError(TAG, "Error signing request", e);
            }
        } else
        {
            TrackerUtils.trackBackgroundEvent("not_signed_request", baseUrl + request.getPath());
        }
        return new ApiResponse(baseUrl + request.getPath(), httpClient.execute(httpRequest));
    }

    /**
     * Create a HttpUriRequest out of a ApiRequest object.
     * 
     * @param request the ApiRequest for which a HttpUriRequest should be
     *            created
     * @param baseUrl the base server url
     * @param listener Progress Listener with callback on progress
     * @return HttpUriRequest object which will do the request as described in
     *         ApiRequest
     * @throws UnsupportedEncodingException
     */
    private HttpUriRequest createHttpRequest(ApiRequest request,
            String baseUrl,
            ProgressListener listener) throws UnsupportedEncodingException {
        HttpUriRequest httpRequest = null;
        switch (request.getMethod()) {
            case ApiRequest.GET:
                httpRequest = new HttpGet(addParamsToUrl(
                        baseUrl + request.getPath(), request.getParameters()));
                break;
            case ApiRequest.POST:
                httpRequest = new HttpPost(baseUrl + request.getPath());
                HttpPost httpPost = ((HttpPost) httpRequest);
                if (request.isMime()) {
                    // TODO use the multipart when possible (currently server
                    // handles it wrong)
                    // HttpEntity entity = createMultipartEntity(request);
                    // TODO remove this when doing correct multipart
                    httpRequest = new HttpPost(addParamsToUrl(
                            baseUrl + request.getPath(), request.getParameters()));
                    httpPost = ((HttpPost) httpRequest);
                    HttpEntity entity = createFileOnlyMultipartEntity(request);
                    if (listener != null) {
                        httpPost.setEntity(new HttpEntityWithProgress(entity,
                                listener, httpPost));
                    } else {
                        httpPost.setEntity(entity);
                    }
                } else {
                    httpPost.setEntity(new UrlEncodedFormEntity(request
                            .getParameters(), HTTP.UTF_8));
                }
                break;
            case ApiRequest.PUT:
                httpRequest = new HttpPut(addParamsToUrl(
                        baseUrl + request.getPath(), request.getParameters()));
                break;
            case ApiRequest.DELETE:
                httpRequest = new HttpDelete(addParamsToUrl(
                        baseUrl + request.getPath(), request.getParameters()));
                break;
        }

        for (NameValuePair pair : request.getHeaders()) {
            request.addHeader(pair.getName(), pair.getValue());
        }

        return httpRequest;
    }

    private HttpEntity createFileOnlyMultipartEntity(ApiRequest request)
            throws UnsupportedEncodingException {
        MultipartEntity entity = new MultipartEntity();
        for (Parameter<?> parameter : request.getParametersMime()) {
            if (parameter.getValue() instanceof File) {
                File file = (File) parameter.getValue();
                entity.addPart(parameter.getName(), new FileBody(file));
            }
        }
        return entity;
    }

    /**
     * Adds the parameters to the given url.
     * 
     * @param url the url
     * @param nameValuePairs the name value pairs to be added to the url
     * @return the url with added parameters
     * @throws UnsupportedEncodingException
     */
    private String addParamsToUrl(String url, List<NameValuePair> nameValuePairs)
            throws UnsupportedEncodingException {
        if (nameValuePairs == null || nameValuePairs.isEmpty()) {
            return url;
        }

        final StringBuffer newUrl = new StringBuffer(url);
        newUrl.append(url.contains("?") ? '&' : '?');

        for (NameValuePair param : nameValuePairs) {
            newUrl.append(param.getName() + "="
                    + URLEncoder.encode(param.getValue(), "UTF-8") + "&");
        }
        newUrl.deleteCharAt(newUrl.length() - 1);
        return newUrl.toString();
    }
}
