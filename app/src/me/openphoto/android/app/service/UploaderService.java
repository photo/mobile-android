
package me.openphoto.android.app.service;

import java.io.File;
import java.util.Calendar;

import me.openphoto.android.app.Preferences;
import me.openphoto.android.app.net.OpenPhotoApi;
import me.openphoto.android.app.net.UploadMetaData;
import me.openphoto.android.app.provider.UploadsProvider;
import me.openphoto.android.app.util.ImageUtils;
import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class UploaderService extends IntentService {
    private static final String TAG = UploaderService.class.getSimpleName();
    private final OpenPhotoApi mApi;

    public UploaderService() {
        super(TAG);
        mApi = new OpenPhotoApi(Preferences.getServer(this));
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Cursor cursor = getContentResolver().query(UploadsProvider.CONTENT_URI, null,
                UploadsProvider.KEY_UPLOADED + "=0", null, null);
        while (cursor.moveToNext()) {
            try {
                Uri uri = Uri.parse(cursor.getString(UploadsProvider.URI_COLUMN));
                Log.i(TAG, "Starting upload to OpenPhoto: " + uri);
                File file = new File(ImageUtils.getRealPathFromURI(this, uri));

                UploadMetaData metaData = new UploadMetaData();
                metaData.setTags(Preferences.getAutoUploadTag(this));

                mApi.uploadPhoto(file, metaData);
                Log.i(TAG, "Upload to OpenPhoto completed for: " + uri);

                Uri contentUri = Uri.withAppendedPath(UploadsProvider.CONTENT_URI,
                        "" + cursor.getInt(UploadsProvider.ID_COLUMN));
                ContentValues values = new ContentValues();
                values.put(UploadsProvider.KEY_UPLOADED, Calendar.getInstance().getTimeInMillis());
                getContentResolver().update(contentUri, values, null, null);
            } catch (Exception e) {
                Log.e(TAG, "Could not upload the photo taken", e);
                continue;
            }
        }
    }
}
