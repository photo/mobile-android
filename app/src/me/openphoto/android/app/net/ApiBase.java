
package me.openphoto.android.app.net;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.openphoto.android.app.Preferences;
import me.openphoto.android.app.net.ApiRequest.Parameter;
import me.openphoto.android.app.net.HttpEntityWithProgress.ProgressListener;
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
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.protocol.HTTP;

import android.content.Context;
import android.util.Log;

import com.bugsense.trace.BugSenseHandler;

/**
 * ApiBase provides the basic functionality to call RESTful APIs using an
 * ApiRequest.
 * 
 * @author Patrick Boos
 */
public class ApiBase {
    public final static String TAG = ApiBase.class.getSimpleName();

    private Context context;

    public ApiBase(Context context) {
        this.context = context;
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
        return execute(request, null);
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
        DefaultHttpClient httpClient = new DefaultHttpClient();
        httpClient.getParams().setParameter(
                CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
        HttpUriRequest httpRequest = createHttpRequest(request, listener);

        httpRequest.getParams().setBooleanParameter(
                "http.protocol.expect-continue", false);
        httpRequest.setHeader("User-Agent", "OpenPhoto Android");

        OAuthConsumer consumer = Preferences.getOAuthConsumer(context);
        if (consumer != null) {
            try {
                consumer.sign(httpRequest);
            } catch (Exception e) {
                Map<String, String> extraData = new HashMap<String, String>();
                extraData.put("message", "Error signing request");
                BugSenseHandler.log(TAG, extraData, e);
                Log.e(TAG, e.getMessage());
            }
        }
        return new ApiResponse(httpClient.execute(httpRequest));
    }

    /**
     * Create a HttpUriRequest out of a ApiRequest object.
     * 
     * @param request the ApiRequest for which a HttpUriRequest should be
     *            created
     * @param listener Progress Listener with callback on progress
     * @return HttpUriRequest object which will do the request as described in
     *         ApiRequest
     * @throws UnsupportedEncodingException
     */
    private HttpUriRequest createHttpRequest(ApiRequest request,
            ProgressListener listener) throws UnsupportedEncodingException {
        HttpUriRequest httpRequest = null;
        String baseUrl = Preferences.getServer(this.context);

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
                                listener));
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
     */
    private String addParamsToUrl(String url, List<NameValuePair> nameValuePairs) {
        if (nameValuePairs == null || nameValuePairs.isEmpty()) {
            return url;
        }

        final StringBuffer newUrl = new StringBuffer(url);
        newUrl.append(url.contains("?") ? '&' : '?');

        for (NameValuePair param : nameValuePairs) {
            newUrl.append(param.getName() + "="
                    + URLEncoder.encode(param.getValue()) + "&");
        }
        newUrl.deleteCharAt(newUrl.length() - 1);
        return newUrl.toString();
    }
}
