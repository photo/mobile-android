
package com.trovebox.android.app.util;

import java.util.concurrent.atomic.AtomicInteger;

import android.view.View;

/**
 * Simple loading control which shows/hides view on start/stop loading
 * 
 * @author Eugene Popovich
 */
public class SimpleViewLoadingControl implements LoadingControl {

    static final String TAG = SimpleViewLoadingControl.class.getSimpleName();

    private AtomicInteger mLoaders = new AtomicInteger(0);
    private View mView;

    public SimpleViewLoadingControl(View view) {
        this.mView = view;
    }

    @Override
    public void startLoading() {
        if (mLoaders.getAndIncrement() == 0) {
            setViewVisibile(true);
        }
    }

    @Override
    public void stopLoading() {
        if (mLoaders.decrementAndGet() == 0) {
            setViewVisibile(false);
        }
    }

    void setViewVisibile(boolean visible) {
        try {
            mView.setVisibility(visible ? View.VISIBLE : View.GONE);
        } catch (Exception ex) {
            GuiUtils.noAlertError(TAG, ex);
        }
    }

    @Override
    public boolean isLoading() {
        return mLoaders.get() > 0;
    }
}
