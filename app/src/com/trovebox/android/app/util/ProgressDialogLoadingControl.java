package com.trovebox.android.app.util;

import java.util.concurrent.atomic.AtomicInteger;

import org.holoeverywhere.app.ProgressDialog;

import android.app.Activity;
import android.content.Context;

/**
 * Simple loading control which shows progress dialog
 * 
 * @author Eugene Popovich
 */
public class ProgressDialogLoadingControl implements LoadingControl
{
    static final String TAG = ProgressDialogLoadingControl.class.getSimpleName();

    private AtomicInteger loaders = new AtomicInteger(0);
    ProgressDialog progress;

    Context context;
    boolean indeterminate;
    int max;
    int currentProgress;
    boolean cancelable;
    String message;

    public ProgressDialogLoadingControl(
            Context context,
            boolean indeterminate,
            boolean cancelable,
            String message) {
        this(context, indeterminate, 100, 0, cancelable, message);
    }

    public ProgressDialogLoadingControl(
            Context context,
            boolean indeterminate,
            int max,
            int currentProgress,
            boolean cancelable,
            String message) {
        super();
        this.context = context;
        this.indeterminate = indeterminate;
        this.max = max;
        this.currentProgress = currentProgress;
        this.cancelable = cancelable;
        this.message = message;
    }

    @Override
    public void startLoading()
    {
        if (loaders.getAndIncrement() == 0)
        {
            showProgress();
        }
    }

    @Override
    public void stopLoading()
    {
        if (loaders.decrementAndGet() == 0)
        {
            dismissProgress();
        }
    }

    @Override
    public boolean isLoading() {
        return loaders.get() > 0;
    }

    public void showProgress() {
        if (context instanceof Activity)
        {
            Activity activity = (Activity) context;
            if (activity.isFinishing())
            {
                return;
            }
        }
        progress = new ProgressDialog(context);
        progress.setCancelable(cancelable);
        updateProgress(message, indeterminate, currentProgress, max);
        progress.show();
    }

    public void updateProgress(String message, boolean indeterminate, int currentProgress, int max)
    {
        this.message = message;
        this.indeterminate = indeterminate;
        this.max = max;
        this.currentProgress = currentProgress;
        if (progress != null)
        {
            progress.setMessage(message);
            progress.setIndeterminate(indeterminate);
            progress.setMax(max);
            progress.setProgress(currentProgress);
        }
    }

    public void dismissProgress() {
        try
        {
            if (progress != null && progress.getWindow() != null && progress.isShowing()) {
                progress.dismiss();
            }
        } catch (Exception ex)
        {
            GuiUtils.noAlertError(TAG, ex);
        }
        progress = null;
    }
}
