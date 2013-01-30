
package com.trovebox.android.app.common;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;

import com.trovebox.android.app.bitmapfun.util.ImageWorker;

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
        clearImageWorkerCaches(true);
    }

    public void clearImageWorkerCaches(boolean memoryOnly) {
        for (ImageWorker mImageWorker : imageWorkers)
        {
            if (mImageWorker != null && mImageWorker.getImageCache() != null)
            {
                mImageWorker.getImageCache().clearCaches(memoryOnly);
            }
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        setImageWorkerExitTaskEarly(false);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        setImageWorkerExitTaskEarly(true);
    }

    public void setImageWorkerExitTaskEarly(boolean exitTaskEarly) {
        for (ImageWorker mImageWorker : imageWorkers)
        {
            if (mImageWorker != null)
            {
                mImageWorker.setExitTasksEarly(exitTaskEarly);
            }
        }
    }
}
