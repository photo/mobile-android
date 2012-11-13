package me.openphoto.android.app.util;

import android.os.AsyncTask;

/**
 * The simple async task with the loading control
 * 
 * @author Eugene Popovich
 */
public abstract class SimpleAsyncTask extends AsyncTask<Void, Void, Boolean> {
    private final LoadingControl loadingControl;

    public SimpleAsyncTask(
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
