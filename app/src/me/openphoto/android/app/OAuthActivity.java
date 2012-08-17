package me.openphoto.android.app;

import java.util.HashMap;
import java.util.Map;

import me.openphoto.android.app.net.IOpenPhotoApi;
import me.openphoto.android.app.ui.widget.ActionBar;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.basic.DefaultOAuthConsumer;
import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.bugsense.trace.BugSenseHandler;

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
		setContentView(R.layout.activity_oauth);
		mActionBar = (ActionBar) findViewById(R.id.actionbar);

		mWebView = (WebView) findViewById(R.id.webview);
		WebSettings webSettings = mWebView.getSettings();
		webSettings.setJavaScriptEnabled(true);
		mWebView.setWebChromeClient(mWebChromeClient);
		mWebView.setWebViewClient(mWebViewClient);

		mOpenPhoto = Preferences.getApi(this);
		String url = mOpenPhoto.getOAuthUrl("OpenPhoto Android App", CALLBACK);
		mWebView.loadUrl(url);
	}

	private final WebChromeClient mWebChromeClient = new WebChromeClient() {

		@Override
		public void onConsoleMessage(String message, int lineNumber,
				String sourceID) {
			Log.e(TAG, "Error: " + message);
			super.onConsoleMessage(message, lineNumber, sourceID);
		}

	};

	private final WebViewClient mWebViewClient = new WebViewClient() {

		@Override
		public void onPageFinished(WebView view, String url) {
			super.onPageFinished(view, url);
			mActionBar.stopLoading();
		}

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			super.onPageStarted(view, url, favicon);
			mActionBar.startLoading();
		}

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			boolean result = true;
			if ((url != null) && (url.startsWith(CALLBACK))) {
				Uri uri = Uri.parse(url);
				if (uri.getQueryParameter("oauth_token") != null) {
					new PostTask(uri).execute();
				} else {
					Toast.makeText(OAuthActivity.this,
							"Error: " + uri.getQueryParameter("error"),
							Toast.LENGTH_LONG).show();
					finish();
				}
			} else {
				result = super.shouldOverrideUrlLoading(view, url);
			}
			return result;
		}
	};

	private class PostTask extends AsyncTask<Void, Void, Boolean> {
		private final Uri mUri;
		private OAuthConsumer mUsedConsumer;

		public PostTask(Uri uri) {
			mUri = uri;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mActionBar.startLoading();
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			try {
				String oAuthConsumerKey = mUri
						.getQueryParameter("oauth_consumer_key");
				String oAuthConsumerSecret = mUri
						.getQueryParameter("oauth_consumer_secret");
				String oAuthToken = mUri.getQueryParameter("oauth_token");
				String oAuthTokenSecret = mUri
						.getQueryParameter("oauth_token_secret");
				String oAuthVerifier = mUri.getQueryParameter("oauth_verifier");

				mUsedConsumer = new DefaultOAuthConsumer(oAuthConsumerKey,
						oAuthConsumerSecret);
				mUsedConsumer.setTokenWithSecret(oAuthToken, oAuthTokenSecret);

				OAuthProvider provider = Preferences
						.getOAuthProvider(OAuthActivity.this);
				provider.retrieveAccessToken(mUsedConsumer, oAuthVerifier);
				return true;
			} catch (Exception e) {
				Map<String, String> extraData = new HashMap<String, String>();
				extraData.put("message", "Error with authentication");
				BugSenseHandler.log(TAG, extraData, e);
				return false;
			}
		}

		@Override
		protected void onPostExecute(Boolean result) {
			mActionBar.stopLoading();
			if (result.booleanValue()) {
				Preferences.setLoginInformation(OAuthActivity.this,
						mUsedConsumer);
				setResult(RESULT_OK);
				finish();
			}
		}
	}
}
