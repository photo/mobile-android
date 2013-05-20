
package com.trovebox.android.app.common;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.ProgressDialog;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import com.trovebox.android.app.R;
import com.trovebox.android.app.util.CommonUtils;
import com.trovebox.android.app.util.GuiUtils;
import com.trovebox.android.app.util.LoadingControl;
import com.trovebox.android.app.util.SimpleAsyncTaskEx;

/**
 * Common fragment which retain its instance and performed task accross
 * configuration change
 * 
 * @author Eugene Popovich
 */
public abstract class CommonRetainedFragmentWithTaskAndProgress extends CommonFragment implements
        LoadingControl {
    RetainedTask retainedTask;
    ProgressDialog progress;

    /**
     * Empty constructor as per the Fragment documentation
     */
    public CommonRetainedFragmentWithTaskAndProgress() {
    }

    @Override
    public void startLoading() {
        progress = new ProgressDialog(getActivity());
        progress.setIndeterminate(true);
        progress.setMessage(getLoadingMessage());
        progress.setCancelable(false);
        progress.show();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Make sure this Fragment is retained over a configuration change
        setRetainInstance(true);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        try {
            // if view is destroyed we need to hide progress dialog
            if (retainedTask != null) {
                retainedTask.stopLoading();
            }
        } catch (Exception ex) {
            GuiUtils.noAlertError(TAG, ex);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View result = super.onCreateView(inflater, container, savedInstanceState);
        try {
            // if login task still working we need to show progress dialog
            if (isLoading()) {
                retainedTask.startLoading();
            }
        } catch (Exception ex) {
            GuiUtils.noAlertError(TAG, ex);
        }
        return result;
    }

    /**
     * This should be called to support retained task
     * 
     * @param task
     */
    public void startRetainedTask(RetainedTask task) {
        retainedTask = task;
        task.execute();
    }

    @Override
    public void stopLoading() {
        try {
            if (progress != null && progress.getWindow() != null) {
                progress.dismiss();
            }
        } catch (Exception ex) {
            GuiUtils.noAlertError(TAG, ex);
        }
        progress = null;
    }

    @Override
    public boolean isLoading() {
        return retainedTask != null && !retainedTask.isFinished();
    }

    /**
     * Get the loading message for indeterminate progress. Can be overridden
     * 
     * @return
     */
    public String getLoadingMessage() {
        return CommonUtils.getStringResource(R.string.loading);
    }

    /**
     * The retained accross configuration changes task
     */
    public abstract class RetainedTask extends SimpleAsyncTaskEx {
        boolean finished = false;

        public RetainedTask() {
            super(null);
        }

        @Override
        public void startLoading() {
            super.startLoading();
            CommonRetainedFragmentWithTaskAndProgress.this.startLoading();
        }

        @Override
        public void stopLoading() {
            super.stopLoading();
            CommonRetainedFragmentWithTaskAndProgress.this.stopLoading();
        }

        @Override
        protected final void onSuccessPostExecute() {
            finished = true;
            try {
                onSuccessPostExecuteAdditional();
            } finally {
                retainedTask = null;
            }
        }

        /**
         * Executed on success post execute
         */
        protected abstract void onSuccessPostExecuteAdditional();

        @Override
        protected void onFailedPostExecute() {
            super.onFailedPostExecute();
            finished = true;
            retainedTask = null;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            finished = true;
            retainedTask = null;
        }

        boolean isFinished() {
            return finished;
        }
    }
}
