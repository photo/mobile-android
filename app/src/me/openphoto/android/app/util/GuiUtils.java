
package me.openphoto.android.app.util;

import java.util.HashMap;
import java.util.Map;

import me.openphoto.android.app.R;
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

	/**
	 * Validate basic text data (whether null or empty) and show
	 * appropriate "please specify first" message if it is invalid
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
	 * Validate basic text data (whether null or empty) and show
	 * appropriate "please specify first" message if it is invalid
	 * 
	 * @param values
	 * @param titles
	 *            array of string resource codes
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
