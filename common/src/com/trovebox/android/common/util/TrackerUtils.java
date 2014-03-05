
package com.trovebox.android.common.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import android.app.Activity;
import android.text.TextUtils;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.ExceptionParser;
import com.google.analytics.tracking.android.ExceptionReporter;
import com.google.analytics.tracking.android.GAServiceManager;
import com.google.analytics.tracking.android.Transaction;
import com.trovebox.android.common.CommonConfigurationUtils;

/**
 * A wrapper class around GoogleAnalytics SDK
 * 
 * @author Eugene Popovich
 */
public class TrackerUtils {
    static final String TAG = TrackerUtils.class.getSimpleName();
    /**
     * Used for tests
     */
    public static boolean SKIP_UNCAUGHT_SETUP = false;
    /**
     * The exception parser used to track exceptions
     */
    static ExceptionParser parser = new ExceptionParser() {
        @Override
        public String getDescription(String threadName, Throwable t) {
            return getStackTrace(t) + getTrackingSuffix(true);
        }

    };

    private static String getStackTrace(Throwable throwable) {
        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);
        throwable.printStackTrace(printWriter);

        return result.toString();
    }

    /**
     * Setup uncaught exception handler
     */
    public static void setupTrackerUncaughtExceptionHandler() {
        EasyTracker.getInstance().setContext(CommonConfigurationUtils.getApplicationContext());
        if (SKIP_UNCAUGHT_SETUP) {
            return;
        }
        ExceptionReporter myHandler = new ExceptionReporter(EasyTracker.getTracker(), // Currently
                                                                                      // used
                                                                                      // Tracker.
                GAServiceManager.getInstance(), // GoogleAnalytics
                Thread.getDefaultUncaughtExceptionHandler()); // Current default
                                                              // uncaught
                                                              // exception
                                                              // handler.
        myHandler.setExceptionParser(parser);

        Thread.setDefaultUncaughtExceptionHandler(myHandler);
    }

    /**
     * Track social event
     * 
     * @param network
     * @param action
     * @param target
     */
    public static void trackSocial(String network, String action, String target) {
        EasyTracker.getTracker().sendSocial(network, action, target);
        trackEvent("social", "action", network);
    }

    /**
     * Track preference change event
     * 
     * @param preferenceName
     * @param preferenceHolder
     */
    public static void trackPreferenceChangeEvent(String preferenceName, Object preferenceHolder) {
        trackPreferenceChangeEvent(preferenceName, null, preferenceHolder);
    }

    /**
     * Track preference change event
     * 
     * @param preferenceName
     * @param preferenceValue
     * @param preferenceHolder
     */
    public static void trackPreferenceChangeEvent(String preferenceName, Object preferenceValue,
            Object preferenceHolder) {
        String preferenceValueString = preferenceValue == null ? null : preferenceValue.toString();
        trackUiEvent(preferenceHolder.getClass().getSimpleName() + ".PreferenceChange",
                preferenceName
                        + (TextUtils.isEmpty(preferenceValueString) ? "" : "."
                                + preferenceValueString));
    }

    /**
     * Track tab selected event
     * 
     * @param tabName
     * @param tabHolder
     */
    public static void trackTabSelectedEvent(String tabName, Object tabHolder) {
        trackUiEvent(tabHolder.getClass().getSimpleName() + ".TabSelected", tabName);
    }

    /**
     * Track tab reselected event
     * 
     * @param tabName
     * @param tabHolder
     */
    public static void trackTabReselectedEvent(String tabName, Object tabHolder) {
        trackUiEvent(tabHolder.getClass().getSimpleName() + ".TabReselected", tabName);
    }

    /**
     * Track navigation item selected event
     * 
     * @param navigationItemName
     * @param navigationItemHolder
     */
    public static void trackNavigationItemSelectedEvent(String navigationItemName,
            Object navigationItemHolder) {
        trackUiEvent(navigationItemHolder.getClass().getSimpleName() + ".NavigationItemSelected",
                navigationItemName);
    }

    /**
     * Track navigation item reselected event
     * 
     * @param navigationItemName
     * @param navigationItemHolder
     */
    public static void trackNavigationItemReselectedEvent(String navigationItemName,
            Object navigationItemHolder) {
        trackUiEvent(navigationItemHolder.getClass().getSimpleName() + ".NavigationItemReselected",
                navigationItemName);
    }

    /**
     * Track button click event
     * 
     * @param buttonName
     * @param buttonHolder
     */
    public static void trackButtonClickEvent(String buttonName, Object buttonHolder) {
        trackUiEvent(buttonHolder.getClass().getSimpleName() + ".ButtonClick", buttonName);
    }

    /**
     * Track options menu clicked event
     * 
     * @param menuName
     * @param menuHolder
     */
    public static void trackOptionsMenuClickEvent(String menuName, Object menuHolder) {
        trackUiEvent(menuHolder.getClass().getSimpleName() + ".OptionsMenuClick", menuName);
    }

    /**
     * Track popup menu clicked event
     * 
     * @param menuName
     * @param menuHolder
     */
    public static void trackPopupMenuClickEvent(String menuName, Object menuHolder) {
        trackUiEvent(menuHolder.getClass().getSimpleName() + ".PopupMenuClick", menuName);
    }

    /**
     * Track context menu click event
     * 
     * @param menuName
     * @param menuHolder
     */
    public static void trackContextMenuClickEvent(String menuName, Object menuHolder) {
        trackUiEvent(menuHolder.getClass().getSimpleName() + ".ContextMenuClick", menuName);
    }

    /**
     * Track ui event
     * 
     * @param action
     * @param label
     */
    public static void trackUiEvent(String action, String label) {
        trackEvent("ui_event", action, label);
    }

    /**
     * Track service event
     * 
     * @param action
     * @param label
     */
    public static void trackServiceEvent(String action, String label) {
        trackEvent("service_event", action, label);
    }

    /**
     * Track error event
     * 
     * @param action
     * @param label
     */
    public static void trackErrorEvent(String action, String label) {
        trackEvent("error_event", action, label);
    }

    /**
     * Track limit event
     * 
     * @param action
     * @param label
     */
    public static void trackLimitEvent(String action, String label) {
        trackEvent("limit_event", action, label);
    }

    /**
     * Track background event
     * 
     * @param action
     * @param label
     */
    public static void trackBackgroundEvent(String action, String label) {
        trackEvent("background_event", action, label);
    }

    /**
     * Track In-app billing event
     * 
     * @param action
     * @param label
     */
    public static void trackInAppBillingEvent(String action, String label) {
        trackEvent("in_app_billing_event", action, label);
    }

    /**
     * Track background event
     * 
     * @param action
     * @param eventHolder
     */
    public static void trackBackgroundEvent(String action, Object eventHolder) {
        trackEvent("background_event", action, eventHolder.getClass().getSimpleName());
    }

    /**
     * Track an event
     * 
     * @param category
     * @param action
     * @param label
     */
    public static void trackEvent(String category, String action, String label) {
        EasyTracker.getTracker().sendEvent(category + getTrackingSuffix(), action, label, null);
    }

    /**
     * Track an event
     * 
     * @param category
     * @param action
     * @param label
     * @param value
     */
    public static void trackEvent(String category, String action, String label, Long value) {
        EasyTracker.getTracker().sendEvent(category + getTrackingSuffix(), action, label, value);
    }

    /**
     * @param inteval
     * @param action
     * @param holder
     */
    public static void trackDataLoadTiming(long inteval, String action, String holder) {
        trackTiming("data_load", inteval, action, holder);
    }

    /**
     * @param inteval
     * @param action
     * @param holder
     */
    public static void trackDataProcessingTiming(long inteval, String action, String holder) {
        trackTiming("data_processing", inteval, action, holder);
    }

    /**
     * Track timing
     * 
     * @param category
     * @param inteval
     * @param name
     * @param label
     */
    public static void trackTiming(String category, long inteval, String name, String label) {
        EasyTracker.getTracker().sendTiming(category + getTrackingSuffix(), inteval, name, label);
    }

    /**
     * Track view
     * 
     * @param view
     */
    public static void trackView(Object view) {
        EasyTracker.getTracker().sendView(view.getClass().getSimpleName() + getTrackingSuffix());
    }

    /**
     * Track throwable
     * 
     * @param t
     */
    public static void trackThrowable(Throwable t) {
        EasyTracker.getInstance().setContext(CommonConfigurationUtils.getApplicationContext());
        EasyTracker.getTracker().setExceptionParser(parser);
        EasyTracker.getTracker().sendException(Thread.currentThread().getName(), t, false);
    }

    /**
     * Track message as exception
     * 
     * @param message
     */
    public static void trackException(String message) {
        EasyTracker.getTracker().sendException(message + getTrackingSuffix(), false);
    }

    /**
     * Send transaction details (Ecommerce tracking)
     * 
     * @param transaction
     */
    public static void sendTransaction(Transaction transaction) {
        EasyTracker.getTracker().sendTransaction(transaction); // Send the
                                                               // transaction.
    }

    /**
     * Called when activity is started
     * 
     * @param activity
     */
    public static void activityStart(Activity activity) {
        EasyTracker.getInstance().activityStart(activity);
    }

    /**
     * Called when activity is stopped
     * 
     * @param activity
     */
    public static void activityStop(Activity activity) {
        EasyTracker.getInstance().activityStop(activity);
    }

    static String getTrackingSuffix() {
        return getTrackingSuffix(false);
    }

    /**
     * Get the tracking suffix to separate self_hosted usage from the hosted
     * 
     * @param trackRetrievalError
     * @return
     */
    static String getTrackingSuffix(boolean trackRetrievalError) {
        try {
            return CommonConfigurationUtils.isLoggedIn() && CommonConfigurationUtils.isSelfHosted() ? " (self_hosted)"
                    : "";
        } catch (Throwable t) {
            CommonUtils.error(TAG, null, t);
            return trackRetrievalError ? CommonUtils.format(" (suffix_retrieval_error: %1$s)",
                    getStackTrace(t)) : " (suffix_retrieval_error)";
        }
    }
}
