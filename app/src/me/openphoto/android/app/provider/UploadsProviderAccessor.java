
package me.openphoto.android.app.provider;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import me.openphoto.android.app.net.UploadMetaData;
import me.openphoto.android.app.util.CommonUtils;
import me.openphoto.android.app.util.GuiUtils;
import me.openphoto.android.app.util.ImageUtils;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class UploadsProviderAccessor {
    private static final String TAG = UploadsProviderAccessor.class.getSimpleName();

    private static final String JSON_TITLE = "title";
    private static final String JSON_DESCRIPTION = "description";
    private static final String JSON_TAGS = "tags";
    private static final String JSON_PERMISSION = "permission";

    private final Context mContext;

    public UploadsProviderAccessor(Context context) {
        mContext = context;
    }

    private void addPendingUpload(Uri photoUri, UploadMetaData metaData,
            boolean isAutoUpload,
            boolean isShareOnTwitter,
            boolean isShareOnFacebook)
    {
        ContentResolver cp = mContext.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(UploadsProvider.KEY_URI, photoUri.toString());
        try {
            JSONObject data = new JSONObject();
            if (metaData.getTitle() != null) {
                data.put(JSON_TITLE, metaData.getTitle());
            }
            if (metaData.getDescription() != null) {
                data.put(JSON_DESCRIPTION, metaData.getDescription());
            }
            if (metaData.getTags() != null) {
                data.put(JSON_TAGS, metaData.getTags());
            }
            data.put(JSON_PERMISSION, metaData.getPermission());
            values.put(UploadsProvider.KEY_METADATA_JSON, data.toString());
        } catch (JSONException e) {
            GuiUtils.noAlertError(TAG, null, e);
        }
        values.put(UploadsProvider.KEY_UPLOADED, 0);
        values.put(UploadsProvider.KEY_IS_AUTOUPLOAD, isAutoUpload ? 1 : 0);
        values.put(UploadsProvider.KEY_SHARE_ON_FACEBOOK, isShareOnFacebook ? 1
                : 0);
        values.put(UploadsProvider.KEY_SHARE_ON_TWITTER, isShareOnTwitter ? 1
                : 0);
        cp.insert(UploadsProvider.CONTENT_URI, values);
    }

    public void addPendingAutoUpload(Uri photoUri, UploadMetaData metaData) {
        addPendingUpload(photoUri, metaData, true, false, false);
    }

    public void addPendingUpload(Uri photoUri, UploadMetaData metaData,
            boolean isShareOnTwitter,
            boolean isShareOnFacebook)
    {
        addPendingUpload(photoUri, metaData, false, isShareOnTwitter,
                isShareOnFacebook);
    }

    public List<PhotoUpload> getPendingUploads() {
        Cursor cursor = mContext.getContentResolver().query(UploadsProvider.CONTENT_URI, null,
                UploadsProvider.KEY_UPLOADED + "=0", null, null);
        try
        {
            List<PhotoUpload> pendingUploads = new ArrayList<PhotoUpload>(
                    cursor.getCount());

            while (cursor.moveToNext())
            {
                PhotoUpload pendingUpload = extractPhotoUpload(cursor);
                if (pendingUpload != null)
                {
                    pendingUploads.add(pendingUpload);
                }
            }
            return pendingUploads;
        } finally
        {
            closeCursor(cursor);
        }
    }

    public List<String> getUploadedOrPendingPhotosFileNames()
    {
        String[] projection = new String[] {
                UploadsProvider.KEY_URI
        };
        Cursor cursor = mContext.getContentResolver().query(
                UploadsProvider.CONTENT_URI, projection,
                null, null, null);
        try
        {
            List<String> result = new ArrayList<String>(
                    cursor.getCount());

            while (cursor.moveToNext())
            {
                int ind = 0;
                Uri photoUri = Uri.parse(cursor.getString(ind));
                CommonUtils.debug(TAG, "Already uploaded URI: " + photoUri);
                String filePath = ImageUtils.getRealPathFromURI(mContext,
                        photoUri);
                CommonUtils.debug(TAG, "Already uploaded file: " + filePath);
                result.add(filePath);
            }
            return result;
        } finally
        {
            closeCursor(cursor);
        }
    }

    public void closeCursor(Cursor cursor)
    {
        try
        {
            cursor.close();
        } catch (Exception ex)
        {
            GuiUtils.noAlertError(TAG, null, ex);
        }
    }

    public PhotoUpload getPendingUpload(Uri uri) {
        Cursor cursor = mContext.getContentResolver().query(uri, null, null, null, null);
        try
        {
            if (cursor.moveToNext())
            {
                return extractPhotoUpload(cursor);
            } else
            {
                return null;
            }
        } finally
        {
            closeCursor(cursor);
        }
    }

    private PhotoUpload extractPhotoUpload(Cursor cursor) {
        try {
            long id = cursor.getInt(UploadsProvider.ID_COLUMN);
            Uri photoUri = Uri.parse(cursor.getString(UploadsProvider.URI_COLUMN));

            UploadMetaData metaData = new UploadMetaData();
            String metaJson = cursor.getString(UploadsProvider.METADATA_JSON_COLUMN);
            if (metaJson != null) {
                JSONObject jsonMeta = new JSONObject(metaJson);
                if (jsonMeta.has(JSON_TAGS)) {
                    metaData.setTags(jsonMeta.optString(JSON_TAGS));
                }
                if (jsonMeta.has(JSON_TITLE)) {
                    metaData.setTitle(jsonMeta.optString(JSON_TITLE));
                }
                if (jsonMeta.has(JSON_DESCRIPTION)) {
                    metaData.setDescription(jsonMeta.optString(JSON_DESCRIPTION));
                }
                if (jsonMeta.has(JSON_PERMISSION)) {
                    metaData.setPermission(jsonMeta.optInt(JSON_PERMISSION));
                }
                // TODO get other meta data like title, description,
                // location?
            }
            PhotoUpload pendingUpload = new PhotoUpload(id, photoUri, metaData);
            pendingUpload.setError(cursor.getString(UploadsProvider.ERROR_COLUMN));
            pendingUpload
                    .setIsAutoUpload(cursor.getInt(UploadsProvider.IS_AUTOUPLOAD_COLUMN) != 0);
            pendingUpload
                    .setShareOnFacebook(cursor
                            .getInt(UploadsProvider.SHARE_ON_FACEBOOK_COLUMN) != 0);
            pendingUpload
                    .setShareOnTwitter(cursor
                            .getInt(UploadsProvider.SHARE_ON_TWITTER_COLUMN) != 0);
            return pendingUpload;
        } catch (Exception e) {
            GuiUtils.noAlertError(TAG, "Could not get pending upload", e);
            return null;
        }
    }

    public void setUploaded(long id) {
        Uri contentUri = ContentUris.withAppendedId(
                UploadsProvider.CONTENT_URI, id);
        ContentValues values = new ContentValues();
        values.put(UploadsProvider.KEY_UPLOADED, Calendar.getInstance().getTimeInMillis());
        mContext.getContentResolver().update(contentUri, values, null, null);
    }

    public void setError(long id, String error) {
        Uri contentUri = ContentUris.withAppendedId(
                UploadsProvider.CONTENT_URI, id);
        ContentValues values = new ContentValues();
        values.put(UploadsProvider.KEY_ERROR, error);
        mContext.getContentResolver().update(contentUri, values, null, null);
    }

    public void delete(long id) {
        Uri contentUri = ContentUris.withAppendedId(
                UploadsProvider.CONTENT_URI, id);
        mContext.getContentResolver().delete(contentUri, null, null);
    }

    public void deleteAll()
    {
        mContext.getContentResolver().delete(UploadsProvider.CONTENT_URI, null,
                null);
    }
}
