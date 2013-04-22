
package com.trovebox.android.app.model.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.trovebox.android.app.Preferences;
import com.trovebox.android.app.R;
import com.trovebox.android.app.TroveboxApplication;
import com.trovebox.android.app.model.Album;
import com.trovebox.android.app.net.AlbumResponse;
import com.trovebox.android.app.net.TroveboxResponseUtils;
import com.trovebox.android.app.net.UploadMetaDataUtils;
import com.trovebox.android.app.util.CommonUtils;
import com.trovebox.android.app.util.GuiUtils;
import com.trovebox.android.app.util.LoadingControl;
import com.trovebox.android.app.util.SimpleAsyncTaskEx;
import com.trovebox.android.app.util.compare.ToStringComparator;

/**
 * @author Eugene Popovich
 */
public class AlbumUtils {
    private static final String TAG = AlbumUtils.class.getSimpleName();
    public static String ALBUM_CREATED_ACTION = "com.trovebox.ALBUM_CREATED";
    public static String ALBUM_CREATED = "ALBUM_CREATED";

    /**
     * Get the album names string from the albums map
     * 
     * @param albums
     * @return
     */
    public static String getAlbumsString(Map<String, String> albums)
    {
        Collection<String> values = albums == null ? null : albums.values();
        if (values != null)
        {
            values = new ArrayList<String>(values);
            Collections.sort((List<String>) values, new ToStringComparator());
        }
        return UploadMetaDataUtils.getCommaSeparatedString(values);
    }

    /**
     * Create the album
     * 
     * @param title the new album title
     * @param loadingControl the loading indicator control
     */
    public static void createAlbum(
            String title, LoadingControl loadingControl)
    {
        createAlbum(title, null, loadingControl);
    }

    /**
     * Create the album
     * 
     * @param title the new album title
     * @param runOnSuccessAction this will be executed in case of successful
     *            album creation
     * @param loadingControl the loading indicator control
     */
    public static void createAlbum(
            String title,
            Runnable runOnSuccessAction,
            LoadingControl loadingControl)
    {
        new CreateAlbumTask(title, runOnSuccessAction,
                loadingControl).execute();
    }

    /**
     * Get and register the broadcast receiver for the album created event
     * 
     * @param TAG
     * @param handler
     * @param activity
     * @return
     */
    public static BroadcastReceiver getAndRegisterOnAlbumCreatedActionBroadcastReceiver(
            final String TAG,
            final AlbumCreatedHandler handler,
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
                            "Received album created broadcast message");
                    Album album = intent.getParcelableExtra(ALBUM_CREATED);
                    handler.albumCreated(album);
                } catch (Exception ex)
                {
                    GuiUtils.error(TAG, ex);
                }
            }
        };
        activity.registerReceiver(br, new IntentFilter(ALBUM_CREATED_ACTION));
        return br;
    }

    /**
     * Send the album created broadcast
     * 
     * @param album
     */
    public static void sendAlbumCreatedBroadcast(Album album)
    {
        Intent intent = new Intent(ALBUM_CREATED_ACTION);
        intent.putExtra(ALBUM_CREATED, album);
        TroveboxApplication.getContext().sendBroadcast(intent);
    }

    /**
     * Get the album index in the list
     * 
     * @param album
     * @param albums
     * @return index of album in the list if found. Otherwise returns -1
     */
    public int albumIndex(Album album, List<Album> albums) {
        int index = -1;
        for (int i = 0, size = albums.size(); i < size; i++)
        {
            Album album2 = albums.get(i);
            if (album2.getId().equals(album.getId()))
            {
                index = i;
                break;
            }
        }
        return index;
    }

    /**
     * The async task to create album
     */
    private static class CreateAlbumTask extends SimpleAsyncTaskEx {
        private Album album;
        String title;
        Runnable runOnSuccessAction;

        public CreateAlbumTask(
                String title,
                Runnable runOnSuccessAction,
                LoadingControl loadingControl) {
            super(loadingControl);
            this.title = title;
            this.runOnSuccessAction = runOnSuccessAction;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                if (CommonUtils.checkLoggedInAndOnline())
                {
                    AlbumResponse response =
                            Preferences.getApi()
                                    .createAlbum(title);
                    album = response.getAlbum();
                    return TroveboxResponseUtils.checkResponseValid(response);
                }
            } catch (Exception e) {
                GuiUtils.error(TAG, R.string.errorCouldNotCreateAlbum, e);
            }
            return false;
        }

        @Override
        protected void onSuccessPostExecute() {
            sendAlbumCreatedBroadcast(album);
            if (runOnSuccessAction != null)
            {
                runOnSuccessAction.run();
            }
        }
    }

    /**
     * The album created handler interface
     */
    public static interface AlbumCreatedHandler
    {
        void albumCreated(Album album);
    }

    /**
     * Comparator for albums by album id
     */
    public static class AlbumsByIdComparator implements Comparator<Album>
    {
        @Override
        public int compare(Album lhs, Album rhs) {
            return lhs.getId().compareTo(rhs.getId());
        }

    }
}
