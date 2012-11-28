
package me.openphoto.android.app;

import me.openphoto.android.app.facebook.FacebookProvider;
import me.openphoto.android.app.util.CommonUtils;
import me.openphoto.android.app.util.GuiUtils;
import android.content.Context;

import com.WazaBe.HoloEverywhere.app.Application;
import com.WazaBe.HoloEverywhere.app.Application.Config.PreferenceImpl;
import com.bugsense.trace.BugSenseHandler;
import com.facebook.android.R;

/**
 * @author Eugene Popovich
 */
public class OpenPhotoApplication extends Application
{
    static final String TAG = OpenPhotoApplication.class.getSimpleName();
    private static OpenPhotoApplication instance;

    public OpenPhotoApplication()
    {
        instance = this;
    }

    public static Context getContext()
    {
        return instance;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();

        GuiUtils.setup();
        getConfig().setPreferenceImpl(PreferenceImpl.XML);
        
        String bugSenseApiKey = getString(R.string.bugsense_api_key);
        if (bugSenseApiKey != null && bugSenseApiKey.length() > 0)
        {
            // Log all the messages from the ActivityManager and the Debug and
            // higher of your application.
            BugSenseHandler.initAndStartSession(this, bugSenseApiKey);
        }
        FacebookProvider.init(getString(R.string.facebook_app_id),
                getApplicationContext());
    }

    @Override
    public void onTerminate() {
        CommonUtils.debug(TAG, "Terminating application");
        BugSenseHandler.closeSession(this);
        super.onTerminate();
    }
}
