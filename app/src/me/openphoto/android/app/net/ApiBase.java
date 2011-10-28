
package me.openphoto.android.app.net;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.List;

import me.openphoto.android.app.net.ApiRequest.Parameter;
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
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.protocol.HTTP;

import android.util.Log;

/**
 * ApiBase provides the basic functionality to call RESTful APIs using an
 * ApiRequest.
 * 
 * @author Patrick Boos
 */
public class ApiBase {
    private final static String TAG = ApiBase.class.getSimpleName();
    private final String mBaseUrl;
    private OAuthConsumer mOAuthConsumer;

    /**
     * Instantiates a new ApiBase object.
     * 
     * @param baseUrl the base url of the API to use
     */
    public ApiBase(String baseUrl) {
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        mBaseUrl = baseUrl;
    }

    /**
     * Sets the OAuthConsumer when the calls with the server need to be
     * authenticated.
     * 
     * @param oAuthConsumer
     */
    public void setOAuthConsumer(OAuthConsumer oAuthConsumer) {
        mOAuthConsumer = oAuthConsumer;
    }

    /**
     * Gets the base url of the used API.
     * 
     * @return the base url
     */
    public String getBaseUrl() {
        return mBaseUrl;
    }

    /**
     * Execute a request to the API.
     * 
     * @param request request to perform
     * @return the response from the API
     * @throws ClientProtocolException
     * @throws IOException
     */
    public ApiResponse execute(ApiRequest request) throws ClientProtocolException, IOException {
        DefaultHttpClient httpClient = new DefaultHttpClient();
        httpClient.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION,
                HttpVersion.HTTP_1_1);
        HttpUriRequest httpRequest = createHttpRequest(request);

        httpRequest.getParams().setBooleanParameter("http.protocol.expect-continue", false);
        if (mOAuthConsumer != null) {
            try {
                mOAuthConsumer.sign(httpRequest);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return new ApiResponse(httpClient.execute(httpRequest));
    }

    /**
     * Create a HttpUriRequest out of a ApiRequest object.
     * 
     * @param request the ApiRequest for which a HttpUriRequest should be
     *            created
     * @return HttpUriRequest object which will do the request as described in
     *         ApiRequest
     * @throws UnsupportedEncodingException
     */
    private HttpUriRequest createHttpRequest(ApiRequest request)
            throws UnsupportedEncodingException {
        HttpUriRequest httpRequest = null;
        switch (request.getMethod()) {
            case ApiRequest.GET:
                httpRequest = new HttpGet(addParamsToUrl(mBaseUrl + request.getPath(),
                        request.getParameters()));
                break;
            case ApiRequest.POST:
                httpRequest = new HttpPost(mBaseUrl + request.getPath());
                HttpPost httpPost = ((HttpPost) httpRequest);
                if (request.isMime()) {
                    // TODO use the multipart when possible (currently server
                    // handles it wrong)
                    // HttpEntity entity = createMultipartEntity(request);
                    // TODO remove this when doing correct multipart
                    httpRequest = new HttpPost(addParamsToUrl(mBaseUrl + request.getPath(),
                            request.getParameters()));
                    httpPost = ((HttpPost) httpRequest);
                    httpPost.setEntity(createFileOnlyMultipartEntity(request));
                } else {
                    httpPost.setEntity(new UrlEncodedFormEntity(request.getParameters(), HTTP.UTF_8));
                }
                break;
            case ApiRequest.PUT:
                httpRequest = new HttpPut(addParamsToUrl(mBaseUrl + request.getPath(),
                        request.getParameters()));
                break;
            case ApiRequest.DELETE:
                httpRequest = new HttpDelete(addParamsToUrl(mBaseUrl + request.getPath(),
                        request.getParameters()));
                break;
        }

        for (NameValuePair pair : request.getHeaders()) {
            request.addHeader(pair.getName(), pair.getValue());
        }

        return httpRequest;
    }

    private HttpEntity createFileOnlyMultipartEntity(ApiRequest request)
            throws UnsupportedEncodingException {
        Charset stringCharset = Charset.forName("UTF-8");
        MultipartEntity entity = new MultipartEntity();
        for (Parameter<?> parameter : request.getParametersMime()) {
            if (parameter.getValue() instanceof File) {
                File file = (File) parameter.getValue();
                try {
                    entity.addPart(
                            parameter.getName(),
                            new InputStreamBody(new FileInputStream(file), file
                                    .getName()));
                } catch (FileNotFoundException e) {
                    Log.e(TAG,
                            "Could not add file to mime body because file was not found.",
                            e);
                }
            }
        }
        return entity;
    }

    private MultipartEntity createMultipartEntity(ApiRequest request)
            throws UnsupportedEncodingException {
        Charset stringCharset = Charset.forName("UTF-8");
        MultipartEntity entity = new MultipartEntity();
        for (Parameter<?> parameter : request.getParametersMime()) {
            if (parameter.getValue() instanceof String) {
                entity.addPart(parameter.getName(),
                        new StringBody((String) parameter.getValue(), stringCharset));
            } else if (parameter.getValue() instanceof File) {
                File file = (File) parameter.getValue();
                try {
                    entity.addPart(
                            parameter.getName(),
                            new InputStreamBody(new FileInputStream(file), file
                                    .getName()));
                } catch (FileNotFoundException e) {
                    Log.e(TAG,
                            "Could not add file to mime body because file was not found.",
                            e);
                }
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
            newUrl.append(param.getName() + "=" + param.getValue() + "&");
        }
        newUrl.deleteCharAt(newUrl.length() - 1);
        return newUrl.toString();
    }
}
