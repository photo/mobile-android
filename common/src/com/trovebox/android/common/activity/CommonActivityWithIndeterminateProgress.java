package com.trovebox.android.common.activity;

import java.util.concurrent.atomic.AtomicInteger;

import android.os.Bundle;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Window;
import com.trovebox.android.common.util.GuiUtils;
import com.trovebox.android.common.util.LoadingControl;

/**
 * Common activity with indeterminate progress indicator feature
 * 
 * @author Eugene Popovich
 */
public class CommonActivityWithIndeterminateProgress extends CommonActivity implements
        LoadingControl {
    private AtomicInteger mLoaders = new AtomicInteger(0);
    protected ActionBar actionBar;
    protected boolean instanceSaved = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instanceSaved = false;
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        actionBar = getSupportActionBar();
        actionBar.setDisplayUseLogoEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);
        if (mLoaders.get() == 0) {
            setSupportProgressBarIndeterminateVisibility(false);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        instanceSaved = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        instanceSaved = false;
        reinitMenu();
    }

    @Override
    public void startLoading() {
        if (mLoaders.getAndIncrement() == 0) {
            reinitMenu();
            showLoading(true);
        }
    }

    @Override
    public void stopLoading() {
        if (mLoaders.decrementAndGet() == 0) {
            showLoading(false);
            reinitMenu();
        }
    }

    private void showLoading(final boolean show) {
        GuiUtils.post(new Runnable() {

            @Override
            public void run() {
                setSupportProgressBarIndeterminateVisibility(show);
            }
        });
    }

    public void reinitMenu() {
        GuiUtils.post(new Runnable() {

            @Override
            public void run() {
                invalidateOptionsMenu();
            }
        });
    }

    @Override
    public boolean isLoading() {
        return mLoaders.get() > 0;
    }

}
