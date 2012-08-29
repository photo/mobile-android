
package me.openphoto.android.app.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import me.openphoto.android.app.ui.widget.ActionBar;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class ImageWorker {

    private HashMap<String, Drawable> imageCache;
    private static Drawable DEFAULT_ICON = null;
    private BaseAdapter adapt;
    private ActionBar mActionBar;

    static {
        // DEFAULT_ICON =
        // Resources.getSystem().getDrawable(R.drawable.newest_photo_noimage);
    }

    public ImageWorker(Context ctx, ActionBar actionBar)
    {
        imageCache = new HashMap<String, Drawable>();
        mActionBar = actionBar;
    }

    public Drawable loadImage(BaseAdapter adapt, ImageView view)
    {
        this.adapt = adapt;
        String url = (String) view.getTag();
        if (imageCache.containsKey(url))
        {
            return imageCache.get(url);
        }
        else {
            synchronized (this) {
                imageCache.put(url, DEFAULT_ICON);
            }
            new ImageTask().execute(url);
            return DEFAULT_ICON;
        }
    }

    private class ImageTask extends AsyncTask<String, Void, Drawable>
    {
        private String s_url;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (mActionBar != null)
                mActionBar.startLoading();
        }

        @Override
        protected Drawable doInBackground(String... params) {
            s_url = params[0];
            InputStream istr;
            try {
                URL url = new URL(s_url);
                istr = url.openStream();
            } catch (MalformedURLException e) {
                Log.d("HSD", "Malformed: " + e.getMessage());
                throw new RuntimeException(e);
            } catch (IOException e)
            {
                Log.d("HSD", "I/O : " + e.getMessage());
                throw new RuntimeException(e);

            }
            return Drawable.createFromStream(istr, "src");
        }

        @Override
        protected void onPostExecute(Drawable result) {
            super.onPostExecute(result);
            if (mActionBar != null)
                mActionBar.stopLoading();

            synchronized (this) {
                imageCache.put(s_url, result);
            }
            adapt.notifyDataSetChanged();
        }
    }
}
