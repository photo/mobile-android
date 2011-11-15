
package me.openphoto.android.app.receiver;

import me.openphoto.android.app.service.UploaderService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootCompletedReceiver extends BroadcastReceiver {

    private static final String TAG = BootCompletedReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Boot completed received");
        context.startService(new Intent(context, UploaderService.class));
    }
}
