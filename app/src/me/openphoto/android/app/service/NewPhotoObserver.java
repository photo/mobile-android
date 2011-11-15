
package me.openphoto.android.app.service;

import java.io.File;

import me.openphoto.android.app.Preferences;
import me.openphoto.android.app.provider.UploadsProvider;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.FileObserver;
import android.util.Log;

public class NewPhotoObserver extends FileObserver {

    private static final String TAG = NewPhotoObserver.class.getSimpleName();
    private final String mPath;
    private final Context mContext;

    public NewPhotoObserver(Context context, String path) {
        super(path, FileObserver.CREATE);
        mPath = path;
        mContext = context;
    }

    @Override
    public void onEvent(int event, String fileName) {
        if (event == FileObserver.CREATE && !fileName.equals(".probe")) {
            File file = new File(mPath + "/" + fileName);
            Log.d(TAG, "File created [" + file.getAbsolutePath() + "]");

            if (!Preferences.isAutoUploadActive(mContext)) {
                return;
            }
            ContentResolver cp = mContext.getContentResolver();
            ContentValues values = new ContentValues();
            values.put(UploadsProvider.KEY_URI, Uri.fromFile(file).toString());
            try {
                JSONObject data = new JSONObject();
                data.put("tag", Preferences.getAutoUploadTag(mContext));
                values.put(UploadsProvider.KEY_METADATA_JSON, data.toString());
            } catch (JSONException e) {
            }
            values.put(UploadsProvider.KEY_UPLOADED, 0);
            cp.insert(UploadsProvider.CONTENT_URI, values);

            mContext.startService(new Intent(mContext, UploaderService.class));
        }
    }

}
