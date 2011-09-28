
package me.openphoto.android.app;

import java.net.URL;

import me.openphoto.android.app.model.Photo;
import me.openphoto.android.app.net.IOpenPhotoApi;
import me.openphoto.android.app.net.OpenPhotoApi;
import me.openphoto.android.app.net.Paging;
import me.openphoto.android.app.net.ReturnSize;
import me.openphoto.android.app.ui.widget.ActionBar;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.Toast;

/**
 * The Main screen of OpenPhoto
 * 
 * @author pas
 */
public class OAuthActivity extends Activity {
    public static final String TAG = OAuthActivity.class.getSimpleName();

    private static final String CALLBACK = "openphoto://callback";

    private ActionBar mActionBar;
    private WebView mWebView;

    private IOpenPhotoApi mOpenPhoto;

    /**
     * Called when Main Activity is first loaded
     * 
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.oauth);
        mActionBar = (ActionBar) findViewById(R.id.actionbar);

        mWebView = (WebView) findViewById(R.id.webview);
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setSavePassword(false);
        webSettings.setJavaScriptEnabled(true);
        mWebView.setWebChromeClient(mWebChromeClient);
        mWebView.setWebViewClient(mWebViewClient);

        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(false);

        mOpenPhoto = OpenPhotoApi.createInstance(Preferences.getServer(this));
        String url = mOpenPhoto.getOAuthUrl(CALLBACK);
        mWebView.loadUrl(url);
    }

    private final WebChromeClient mWebChromeClient = new WebChromeClient() {
        private boolean mIsLoading = false;

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
            if (newProgress < 100) {
                if (!mIsLoading) {
                    mIsLoading = true;
                    mActionBar.startLoading();
                }
            } else if (mIsLoading) {
                mActionBar.stopLoading();
                mIsLoading = false;
            }
        }

    };

    private final WebViewClient mWebViewClient = new WebViewClient() {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            boolean result = true;
            if ((url != null) && (url.startsWith(CALLBACK))) {
                Uri uri = Uri.parse(url);
                if (uri.getQueryParameter("token") != null) {
                    new PostTask().execute();
                } else {
                    Toast.makeText(OAuthActivity.this, "Error: " + uri.getQueryParameter("error"),
                            Toast.LENGTH_LONG).show();
                    finish();
                }
            } else {
                result = super.shouldOverrideUrlLoading(view, url);
            }
            return result;
        }

    };

    private class PostTask extends AsyncTask<Void, Void, Bitmap> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mActionBar.startLoading();
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            IOpenPhotoApi api = OpenPhotoApi
                    .createInstance(Preferences.getServer(OAuthActivity.this));
            try {
                Photo photo = api.getPhotos(new ReturnSize(600, 600), null, new Paging(1, 1))
                        .getPhotos().get(0);
                // TODO do not use base, make getPhotos actually use a
                // returnSize parameter that should be used then.
                return BitmapFactory.decodeStream(new URL(photo
                        .getUrl("600x600")).openStream());
            } catch (Exception e) {
                Log.w(TAG, "Error while getting image", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            mActionBar.stopLoading();
            if (result != null) {
                ImageView image = (ImageView) findViewById(R.id.image);
                image.setImageBitmap(result);
                image.setVisibility(View.VISIBLE);
            } else {
                Toast.makeText(OAuthActivity.this, "Could not download image",
                        Toast.LENGTH_LONG).show();
            }
        }

    }
}
