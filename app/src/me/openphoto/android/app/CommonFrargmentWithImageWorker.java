package me.openphoto.android.app;

import me.openphoto.android.app.bitmapfun.util.ImageWorker;
import android.os.Bundle;

/**
 * @author Eugene Popovich
 */
public abstract class CommonFrargmentWithImageWorker extends CommonFragment {
    protected ImageWorker mImageWorker;

    protected abstract void initImageWorker();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initImageWorker();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mImageWorker != null && mImageWorker.getImageCache() != null)
        {
            mImageWorker.getImageCache().clearMemoryCache();
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (mImageWorker != null)
        {
            mImageWorker.setExitTasksEarly(false);
        }
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mImageWorker != null)
        {
            mImageWorker.setExitTasksEarly(true);
        }
    }
}
