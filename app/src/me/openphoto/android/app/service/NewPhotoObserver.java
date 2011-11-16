
package me.openphoto.android.app.service;

import java.io.File;

import me.openphoto.android.app.Preferences;
import me.openphoto.android.app.net.UploadMetaData;
import me.openphoto.android.app.provider.UploadsProviderAccessor;
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
            UploadsProviderAccessor uploads = new UploadsProviderAccessor(mContext);
            UploadMetaData metaData = new UploadMetaData();
            metaData.setTags(Preferences.getAutoUploadTag(mContext));
            uploads.addPendingAutoUpload(Uri.fromFile(file), metaData);
            mContext.startService(new Intent(mContext, UploaderService.class));
        }
    }

}
