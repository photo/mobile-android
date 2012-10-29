
package me.openphoto.android.app.facebook;

import android.content.Context;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.Facebook;

/**
 * @author Eugene Popovich
 */
public class FacebookProvider
{
    private Facebook mFacebook;
    private AsyncFacebookRunner mAsyncRunner;
    static FacebookProvider instance = new FacebookProvider();

    static FacebookProvider getInstance()
    {
        return instance;
    }

    public static Facebook getFacebook()
    {
        return getInstance().mFacebook;
    }

    public static AsyncFacebookRunner getAsyncRunner()
    {
        return getInstance().mAsyncRunner;
    }

    public static void init(String APP_ID, Context context)
    {
        FacebookProvider provider = getInstance();
        if (provider.mFacebook == null)
        {
            // Create the Facebook Object using the app id.
            Facebook facebook = new Facebook(APP_ID);
            // Instantiate the asynrunner object for asynchronous api calls.
            AsyncFacebookRunner asyncRunner = new AsyncFacebookRunner(facebook);
            provider.mFacebook = facebook;
            provider.mAsyncRunner = asyncRunner;
        }
    }

}
