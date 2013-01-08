
package me.openphoto.android.app;

import me.openphoto.android.app.facebook.FacebookProvider;
import me.openphoto.android.app.util.CommonUtils;
import me.openphoto.android.app.util.GuiUtils;

import org.holoeverywhere.app.Application;
import org.holoeverywhere.app.Application.Config.PreferenceImpl;

import android.content.Context;

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
        
        FacebookProvider.init(getString(R.string.facebook_app_id),
                getApplicationContext());
    }

    @Override
    public void onTerminate() {
        CommonUtils.debug(TAG, "Terminating application");
        super.onTerminate();
    }
}
