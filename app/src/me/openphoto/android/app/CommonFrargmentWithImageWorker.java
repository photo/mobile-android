
package me.openphoto.android.app;

import java.util.ArrayList;
import java.util.List;

import me.openphoto.android.app.bitmapfun.util.ImageWorker;
import android.os.Bundle;

/**
 * @author Eugene Popovich
 */
public abstract class CommonFrargmentWithImageWorker extends CommonFragment {
    protected ImageWorker mImageWorker;
    protected List<ImageWorker> imageWorkers = new ArrayList<ImageWorker>();

    protected abstract void initImageWorker();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initImageWorker();
        imageWorkers.add(mImageWorker);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        for (ImageWorker mImageWorker : imageWorkers)
        {
            if (mImageWorker != null && mImageWorker.getImageCache() != null)
            {
                mImageWorker.getImageCache().clearMemoryCache();
            }
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        for (ImageWorker mImageWorker : imageWorkers)
        {
            if (mImageWorker != null)
            {
                mImageWorker.setExitTasksEarly(false);
            }
        }
    }

    @Override
    public void onPause()
    {
        super.onPause();
        for (ImageWorker mImageWorker : imageWorkers)
        {
            if (mImageWorker != null)
            {
                mImageWorker.setExitTasksEarly(true);
            }
        }
    }
}
