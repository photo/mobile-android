
package com.trovebox.android.app.util;


import org.holoeverywhere.widget.Toast;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.trovebox.android.app.R;
import com.trovebox.android.app.TroveboxApplication;

/**
 * Contains various gui utils methods
 * 
 * @author Eugene Popovich
 */
public class GuiUtils
{
    static Thread mUiThread;
    static Handler mHandler;

    /**
     * Setup application
     */
    public static void setup()
    {
        mHandler = new Handler();
        mUiThread = Thread.currentThread();
        TrackerUtils.setupTrackerUncaughtExceptionHandler();
    }


    /**
     * Run action in UI thread
     * 
     * @param action
     */
    public static final void runOnUiThread(Runnable action)
    {
        if (mHandler == null || mUiThread == null)
        {
            throw new IllegalStateException(
                    "GuiUtils is not configured. Did you forget to call GuiUtils.setup()?");
        }
        if (Thread.currentThread() != mUiThread)
        {
            mHandler.post(action);
        } else
        {
            action.run();
        }
    }

    /**
     * Alert message to user by id
     * 
     * @param messageId
     */
    public static void alert(int messageId)
    {
        alert(CommonUtils.getStringResource(messageId));
    }

    /**
     * Alert message to user by id with parameters
     * 
     * @param messageId
     * @param args
     */
    public static void alert(int messageId, Object... args)
    {
        alert(CommonUtils.getStringResource(messageId, args));
    }

    /**
     * Alert message to user
     * 
     * @param msg
     */
    public static void alert(final String msg)
    {
        alert(msg, null);
    }

    /**
     * Alert message to user
     * 
     * @param msg
     * @param context
     */
    public static void alert(final String msg, final Context context)
    {
        Runnable runnable = new Runnable()
        {
            @Override
            public void run()
            {
                Toast.makeText(
                        context == null ? TroveboxApplication.getContext()
                                : context, msg, Toast.LENGTH_LONG).show();
            }
        };
        runOnUiThread(runnable);
    }

    /**
     * Show info message to user by message id
     * 
     * @param messagId
     */
    public static void info(int messagId)
    {
        info(CommonUtils.getStringResource(messagId));
    }

    /**
     * Show info message to user
     * 
     * @param msg
     */
    public static void info(final String msg)
    {
        info(msg, null);
    }

    /**
     * Show info message to user
     * 
     * @param msg
     * @param context
     */
    public static void info(final String msg, final Context context)
    {
        alert(msg, context);
    }

    /**
     * Process error and show error message to user
     * 
     * @param TAG
     * @param messageId
     * @param ex
     */
    public static void error(String TAG, int messageId, Exception ex)
    {
        error(TAG, CommonUtils.getStringResource(messageId), ex);
    }

    /**
     * Process error and show error message to user
     * 
     * @param TAG
     * @param ex
     */
    public static void error(String TAG, Exception ex)
    {
        error(TAG, null, ex, null);
    }

    /**
     * Process error and show error message to user
     * 
     * @param TAG
     * @param message
     * @param ex
     */
    public static void error(String TAG, String message, Exception ex)
    {
        error(TAG, message, ex, null);
    }

    /**
     * Process error and show error message to user
     * 
     * @param TAG
     * @param messageId
     * @param ex
     * @param context
     */
    public static void error(String TAG, int messageId, Exception ex,
            Context context)
    {
        error(TAG, CommonUtils.getStringResource(messageId), ex, context);
    }

    /**
     * Process error and show error message to user
     * 
     * @param TAG
     * @param message
     * @param ex
     * @param context
     */
    public static void error(String TAG, String message, Exception ex,
            Context context)
    {
        processError(TAG, message, ex, context, true);
    }

    /**
     * Process error but don't show alert to user
     * 
     * @param TAG
     * @param ex
     */
    public static void noAlertError(String TAG, Exception ex)
    {
        noAlertError(TAG, null, ex);
    }

    /**
     * Process error but don't show alert to user
     * 
     * @param TAG
     * @param message
     * @param ex
     */
    public static void noAlertError(String TAG, String message, Exception ex)
    {
        processError(TAG, message, ex, null, false);
    }

    /**
     * Process error
     * 
     * @param TAG
     * @param messageId
     * @param ex
     * @param context
     * @param alertMessage
     */
    public static void processError(String TAG, int messageId, Exception ex,
            Context context, boolean alertMessage)
    {
        processError(TAG, CommonUtils.getStringResource(messageId), ex, context, alertMessage);
    }

    /**
     * Process error
     * 
     * @param TAG
     * @param message
     * @param ex
     * @param context
     * @param alertMessage
     */
    public static void processError(String TAG, String message, Exception ex,
            Context context, boolean alertMessage)
    {
        TrackerUtils.trackThrowable(ex);
        Log.e(TAG, message, ex);
        if (alertMessage)
        {
            alert(message == null ? ex.getLocalizedMessage() : message,
                    context);
        }
    }

    /**
     * Validate basic text data (whether null or empty) and show appropriate
     * "please specify first" message if it is invalid
     * 
     * @param values
     * @param titles
     * @param activity
     * @return false if at least one field is invalid, otherwise return true
     */
    public static boolean validateBasicTextData(
            String[] values,
            String[] titles,
            Activity activity)
    {
        for (int i = 0; i < values.length; i++)
        {
            String value = values[i];
            if (value.length() == 0)
            {
                String pleaseSpecifyFirst = activity
                        .getString(R.string.pleaseSpecifyFirst);
                info(String.format(pleaseSpecifyFirst, titles[i]),
                        activity);
                return false;
            }
        }
        return true;
    }

    /**
     * Validate basic text data (whether null or empty) and show appropriate
     * "please specify first" message if it is invalid
     * 
     * @param values
     * @param titles array of string resource codes
     * @param activity
     * @return false if at least one field is invalid, otherwise return true
     */
    public static boolean validateBasicTextData(
            String[] values,
            int[] titles,
            Activity activity)
    {
        for (int i = 0; i < values.length; i++)
        {
            String value = values[i];
            if (value.length() == 0)
            {
                String pleaseSpecifyFirst = activity
                        .getString(R.string.pleaseSpecifyFirst);
                info(String.format(pleaseSpecifyFirst,
                        activity.getString(titles[i])),
                        activity);
                return false;
            }
        }
        return true;
    }
}
