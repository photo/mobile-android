
package com.trovebox.android.app;


import org.holoeverywhere.app.Application;
import org.holoeverywhere.app.Application.Config.PreferenceImpl;

import android.content.Context;

import com.trovebox.android.app.facebook.FacebookProvider;
import com.trovebox.android.app.util.CommonUtils;
import com.trovebox.android.app.util.GuiUtils;

/**
 * @author Eugene Popovich
 */
public class TroveboxApplication extends Application
{
    static final String TAG = TroveboxApplication.class.getSimpleName();
    private static TroveboxApplication instance;

    public TroveboxApplication()
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
        // temp hack to lowercase all the server paths to
        // overcome oauth issue
        String server = Preferences.getServer(this);
        if (server != null)
        {
            String lowerCaseServerName = server.toLowerCase();
            if (!server.equals(lowerCaseServerName))
            {
                Preferences.setServer(this, lowerCaseServerName);
            }
        }
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
