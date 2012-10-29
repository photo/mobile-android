
package me.openphoto.android.app.util;

import java.util.HashMap;
import java.util.Map;

import me.openphoto.android.app.OpenPhotoApplication;
import me.openphoto.android.app.R;
import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.WazaBe.HoloEverywhere.widget.Toast;
import com.bugsense.trace.BugSenseHandler;

/**
 * Contains various gui utils methods
 * 
 * @author Eugene Popovich
 */
public class GuiUtils
{
    static Thread mUiThread;
    static Handler mHandler;

    static String getMessage(int messageId)
    {
        return OpenPhotoApplication.getContext().getString(messageId);
    }

    public static void setup()
    {
        mHandler = new Handler();
        mUiThread = Thread.currentThread();
    }

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

    public static void alert(int messageId)
    {
        alert(getMessage(messageId));
    }

    public static void alert(final String msg)
    {
        alert(msg, null);
    }

    public static void alert(final String msg, final Context context)
    {
        Runnable runnable = new Runnable()
        {
            @Override
            public void run()
            {
                Toast.makeText(
                        context == null ? OpenPhotoApplication.getContext()
                                : context, msg, Toast.LENGTH_LONG).show();
            }
        };
        runOnUiThread(runnable);
    }

    public static void info(int messagId)
    {
        info(getMessage(messagId));
    }

    public static void info(final String msg)
    {
        info(msg, null);
    }

    public static void info(final String msg, final Context context)
    {
        alert(msg, context);
    }

    public static void error(String TAG, int messageId, Exception ex)
    {
        error(TAG, getMessage(messageId), ex);
    }

    public static void error(String TAG, Exception ex)
    {
        error(TAG, null, ex, null);
    }

    public static void error(String TAG, String message, Exception ex)
    {
        error(TAG, message, ex, null);
    }

    public static void error(String TAG, int messageId, Exception ex,
            Context context)
    {
        error(TAG, getMessage(messageId), ex, context);
    }

    public static void error(String TAG, String message, Exception ex,
            Context context)
    {
        processError(TAG, message, ex, context, true);
    }

    public static void noAlertError(String TAG, String message, Exception ex)
    {
        processError(TAG, message, ex, null, false);
    }

    public static void processError(String TAG, int messageId, Exception ex,
            Context context, boolean alertMessage)
    {
        processError(TAG, getMessage(messageId), ex, context, alertMessage);
    }

    public static void processError(String TAG, String message, Exception ex,
            Context context, boolean alertMessage)
    {
        Map<String, String> extraData = new HashMap<String, String>();
        if (message != null)
        {
            extraData.put("message", message);
        }
        BugSenseHandler.log(TAG, extraData, ex);
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
