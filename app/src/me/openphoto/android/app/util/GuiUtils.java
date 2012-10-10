
package me.openphoto.android.app.util;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import com.bugsense.trace.BugSenseHandler;

/**
 * Contains various gui utils methods
 * 
 * @author Eugene Popovich
 */
public class GuiUtils
{
    public static void alert(final String msg, final Activity activity)
    {
        activity.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                Toast.makeText(activity, msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    public static void info(final String msg, final Activity activity)
    {
        activity.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                Toast.makeText(activity, msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    public static void error(String TAG, String message, Exception ex,
            Activity activity)
    {
        Map<String, String> extraData = new HashMap<String, String>();
        if (message != null)
        {
            extraData.put("message", message);
        }
        BugSenseHandler.log(TAG, extraData, ex);
        Log.e(TAG, message, ex);
        alert(message == null ? ex.getLocalizedMessage() : message,
                activity);
    }
}
