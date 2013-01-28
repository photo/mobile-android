
package com.trovebox.android.app.util;

import java.util.ArrayList;
import java.util.List;


import org.holoeverywhere.app.Activity;

import com.trovebox.android.app.TroveboxApplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

/**
 * Sync action utils
 * 
 * @author Eugene Popovich
 */
public class SyncUtils {
    public static String SYNC_STARTED_ACTION = "com.trovebox.SYNC_STARTED";
    public static String SYNC_FILE_NAMES = "SYNC_FILE_NAMES";

    /**
     * Get and register the sync started broadcast receiver
     * 
     * @param TAG
     * @param handler
     * @param activity
     * @return
     */
    public static BroadcastReceiver getAndRegisterOnSyncStartedActionBroadcastReceiver(
            final String TAG,
            final SyncStartedHandler handler,
            final Activity activity)
    {
        BroadcastReceiver br = new BroadcastReceiver()
        {

            @Override
            public void onReceive(Context context, Intent intent)
            {
                try
                {
                    CommonUtils.debug(TAG,
                            "Received sync started broadcast message");
                    handler.syncStarted(intent.getStringArrayListExtra(SYNC_FILE_NAMES));
                } catch (Exception ex)
                {
                    GuiUtils.error(TAG, ex);
                }
            }
        };
        activity.registerReceiver(br, new IntentFilter(SYNC_STARTED_ACTION));
        return br;
    }

    /**
     * Send the sync started broadcast message with the processedFileNames as
     * the parameter
     * 
     * @param processedFileNames
     */
    public static void sendSyncStartedBroadcast(ArrayList<String> processedFileNames)
    {
        Intent intent = new Intent(SYNC_STARTED_ACTION);
        intent.putStringArrayListExtra(SYNC_FILE_NAMES, processedFileNames);
        TroveboxApplication.getContext().sendBroadcast(intent);
    }

    /**
     * The sync starter handler interface
     */
    public static interface SyncStartedHandler
    {
        void syncStarted(List<String> processedFileNames);
    }
}
