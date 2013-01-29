package com.trovebox.android.app.util;

import com.trovebox.android.app.util.concurrent.AsyncTaskEx;

/**
 * The simple async task with the loading control
 * 
 * @author Eugene Popovich
 */
public abstract class SimpleAsyncTaskEx extends AsyncTaskEx<Void, Void, Boolean> {
    private final LoadingControl loadingControl;

    public SimpleAsyncTaskEx(
            LoadingControl loadingControl) {
        this.loadingControl = loadingControl;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (loadingControl != null)
        {
            loadingControl.startLoading();
        }
    }
    @Override
    protected void onCancelled() {
        super.onCancelled();
        if (loadingControl != null)
        {
            loadingControl.stopLoading();
        }

    }

    @Override
    protected final void onPostExecute(Boolean result) {
        super.onPostExecute(result);
        if (loadingControl != null)
        {
            loadingControl.stopLoading();
        }
        if (result.booleanValue())
        {
            onSuccessPostExecute();
        } else
        {
            onFailedPostExecute();
        }
    }

    protected abstract void onSuccessPostExecute();

    protected void onFailedPostExecute() {
    }

}
