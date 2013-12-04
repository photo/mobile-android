package com.trovebox.android.common;

import oauth.signpost.OAuthConsumer;
import android.content.Context;

import com.trovebox.android.common.net.ITroveboxApi;
import com.trovebox.android.common.net.TroveboxApi;
import com.trovebox.android.common.net.ApiRequest.ApiVersion;

/**
 * The common configuration utils
 * 
 * @author Eugene Popovich
 */
public class CommonConfigurationUtils {

    private static CommonConfiguration sInstance;
    private static Context sApplicationContext;

    /**
     * Get the current application context
     * 
     * @return
     */
    public static Context getApplicationContext() {
        if (sApplicationContext == null) {
            throw new IllegalStateException(
                    "Context is empty. Don't forget to call setContext in the Application.onCreate() method");
        }
        return sApplicationContext;
    }

    /**
     * Get the current application configuration
     * 
     * @return
     */
    public static CommonConfiguration getConfig() {
        if (sInstance == null) {
            throw new IllegalStateException(
                    "Config is empty. Don't forget to call setup in the Application.onCreate() method");
        }
        return sInstance;
    }

    /**
     * Setup configuration utils. Should be called in the android application
     * constructor
     * 
     * @param config
     * @param context
     */
    public static void setup(CommonConfiguration config, Context context) {
        sInstance = config;
        sApplicationContext = context;
    }

    /**
     * Call this method in the Application.onTerminate() method
     */
    public static void cleanup() {
        sInstance = null;
        sApplicationContext = null;
    }

    public static boolean isV2ApiAvailable() {
        return getConfig().isV2ApiAvailable();
    }

    public static boolean isLoggedIn() {
        return getConfig().isLoggedIn();
    }

    public static boolean isSelfHosted() {
        return getConfig().isSelfHosted();
    }

    public static boolean isWiFiOnlyUploadActive() {
        return getConfig().isWiFiOnlyUploadActive();
    }

    public static String getServer() {
        return getConfig().getServer();
    }

    public static OAuthConsumer getOAuthConsumer() {
        return getConfig().getOAuthConsumer();
    }

    public static ApiVersion getCurrentApiVersion() {
        return getConfig().getCurrentApiVersion();
    }

    public static interface CommonConfiguration {
    
        boolean isLoggedIn();
    
        boolean isSelfHosted();
    
        boolean isV2ApiAvailable();

        boolean isWiFiOnlyUploadActive();
    
        ApiVersion getCurrentApiVersion();
    
        String getServer();
    
        OAuthConsumer getOAuthConsumer();
    }

    /**
     * Get trovebox api instance
     * 
     * @return
     */
    public static ITroveboxApi getApi() {
        return TroveboxApi.createInstance();
    }
}
