package com.trovebox.android.app.net;

import android.text.TextUtils;

import com.trovebox.android.app.R;
import com.trovebox.android.app.util.CommonUtils;
import com.trovebox.android.app.util.GuiUtils;
import com.trovebox.android.app.util.TrackerUtils;

/**
 * Utils for the TroveboxResponse class
 * 
 * @author Eugene Popovich
 */
public class TroveboxResponseUtils {
    private static final String TAG = TroveboxResponseUtils.class.getSimpleName();
    
    /**
     * Checks whether response is successful. If not then warn user about the
     * error. Use this in the UI api calls before processing the response
     * 
     * @param response
     * @return true if response is successful
     */
    public static boolean checkResponseValid(TroveboxResponse response)
    {
        boolean result = response.isSuccess();
        if (!result)
        {
            String message = response.getAlertMessage();
            if (!TextUtils.isEmpty(message))
            {
                GuiUtils.alert(message);
            } else
            {
                GuiUtils.alert(R.string.unknown_error_with_code, response.getCode());
            }
        }
        return result;
    }

    /**
     * Track response if it is invalid
     * 
     * @param response
     * @return
     */
    public static boolean trackResponseIfInvalid(
            TroveboxResponse response)
    {
        boolean result = response.isSuccess();
        if (!result)
        {
            String error = CommonUtils
                    .format("Not successful api response. Request Type: %1$s; Code: %2$d; Message: %3$s",
                            response.getRequestType().toString(), response.getCode(),
                            response.getMessage());
            CommonUtils.error(TAG, error);
            TrackerUtils.trackException(error);
            TrackerUtils.trackErrorEvent("invalid_api_response",
                    error);
        }
        return result;
    }
}
