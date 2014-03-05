
package com.trovebox.android.app;


import oauth.signpost.OAuthConsumer;

import org.holoeverywhere.HoloEverywhere;
import org.holoeverywhere.HoloEverywhere.PreferenceImpl;
import org.holoeverywhere.app.Application;

import android.content.Context;

import com.trovebox.android.app.facebook.FacebookProvider;
import com.trovebox.android.common.CommonConfigurationUtils;
import com.trovebox.android.common.CommonConfigurationUtils.CommonConfiguration;
import com.trovebox.android.common.net.ApiRequest.ApiVersion;
import com.trovebox.android.common.util.CommonUtils;
import com.trovebox.android.common.util.GuiUtils;
import com.trovebox.android.common.util.TrackerUtils;

/**
 * @author Eugene Popovich
 */
public class TroveboxApplication extends Application
{
    static final String TAG = TroveboxApplication.class.getSimpleName();

    public TroveboxApplication()
    {
        CommonConfigurationUtils.setup(new TroveboxConfiguration(), this);
    }

    public static Context getContext()
    {
        return CommonConfigurationUtils.getApplicationContext();
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
        HoloEverywhere.PREFERENCE_IMPL = PreferenceImpl.XML;
        TrackerUtils.setupTrackerUncaughtExceptionHandler();
        GuiUtils.setup();
        
        FacebookProvider.init(getString(R.string.facebook_app_id),
                getApplicationContext());
    }

    @Override
    public void onTerminate() {
        CommonUtils.debug(TAG, "Terminating application");
        super.onTerminate();
        CommonConfigurationUtils.cleanup();
    }

    public static class TroveboxConfiguration implements CommonConfiguration {

        @Override
        public boolean isLoggedIn() {
            return Preferences.isLoggedIn();
        }

        @Override
        public boolean isSelfHosted() {
            return Preferences.isSelfHosted();
        }

        @Override
        public boolean isV2ApiAvailable() {
            return Preferences.isV2ApiAvailable();
        }

        @Override
        public boolean isWiFiOnlyUploadActive() {
            return Preferences.isWiFiOnlyUploadActive(CommonConfigurationUtils
                    .getApplicationContext());
        }

        @Override
        public ApiVersion getCurrentApiVersion() {
            return Preferences.getCurrentApiVersion();
        }

        @Override
        public String getServer() {
            return Preferences.getServer(CommonConfigurationUtils.getApplicationContext());
        }

        @Override
        public OAuthConsumer getOAuthConsumer() {
            return Preferences.getOAuthConsumer(CommonConfigurationUtils.getApplicationContext());
        }

    }
}
