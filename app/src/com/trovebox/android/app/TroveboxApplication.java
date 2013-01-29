
package com.trovebox.android.app;


import org.holoeverywhere.app.Application;
import org.holoeverywhere.app.Application.Config.PreferenceImpl;

import com.trovebox.android.app.R;
import com.trovebox.android.app.facebook.FacebookProvider;
import com.trovebox.android.app.util.CommonUtils;
import com.trovebox.android.app.util.GuiUtils;

import android.content.Context;

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
