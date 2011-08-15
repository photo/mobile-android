
package me.openphoto.android.app.receiver;

import me.openphoto.android.app.Preferences;
import me.openphoto.android.app.provider.UploadsProvider;
import me.openphoto.android.app.service.UploaderService;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

public class NewPhotoReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!Preferences.isAutoUploadActive(context)) {
            return;
        }

        if (intent.getData() != null) {
            ContentResolver cp = context.getContentResolver();
            ContentValues values = new ContentValues();
            values.put(UploadsProvider.KEY_URI, intent.getData().toString());
            values.put(UploadsProvider.KEY_UPLOADED, 0);
            cp.insert(UploadsProvider.CONTENT_URI, values);
        }

        if (isOnline(context)) {
            context.startService(new Intent(context, UploaderService.class));
        }
    }

    public boolean isOnline(Context context) {
        try {
            ConnectivityManager cm = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            return cm.getActiveNetworkInfo().isConnectedOrConnecting();
        } catch (Exception e) {
            return false;
        }
    }
}
