package com.trovebox.android.app.common;


import org.holoeverywhere.preference.PreferenceActivity;

import android.os.Bundle;

import com.trovebox.android.app.common.lifecycle.LifecycleEventHandler;
import com.trovebox.android.app.common.lifecycle.LifecycleEventHandler.HasLifecycleEventHandler;
import com.trovebox.android.app.util.CommonUtils;
import com.trovebox.android.app.util.TrackerUtils;

/**
 * Common preference activity with lifecycle tracking
 * 
 * @author Eugene Popovich
 */
public class CommonPreferenceActivity extends PreferenceActivity implements
        HasLifecycleEventHandler {
    static final String TAG = CommonPreferenceActivity.class.getSimpleName();
    static final String CATEGORY = "Preference Activity Lifecycle";

    private LifecycleEventHandler lifecycleEventHandler = new LifecycleEventHandler(this);

    void trackLifecycleEvent(String event)
    {
        CommonUtils.debug(TAG, event + ": " + getClass().getSimpleName());
        TrackerUtils.trackEvent(CATEGORY, event, getClass().getSimpleName());
    }
    @Override
    protected void onStart() {
        super.onStart();
        trackLifecycleEvent("onStart");
        TrackerUtils.activityStart(this);
        TrackerUtils.trackView(this);
        lifecycleEventHandler.fireOnStartEvent();
    }

    @Override
    protected void onStop() {
        super.onStop();
        trackLifecycleEvent("onStop");
        TrackerUtils.activityStop(this);
        lifecycleEventHandler.fireOnStartEvent();
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        trackLifecycleEvent("onCreate");
        lifecycleEventHandler.fireOnCreateEvent(savedInstanceState);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        trackLifecycleEvent("onDestroy");
        lifecycleEventHandler.fireOnDestroyEvent();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        trackLifecycleEvent("onSaveInstanceState");
        lifecycleEventHandler.fireOnSaveInstanceStateEvent(outState);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        trackLifecycleEvent("onResume");
        lifecycleEventHandler.fireOnResumeEvent();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        trackLifecycleEvent("onPause");
        lifecycleEventHandler.fireOnPauseEvent();
    }

    @Override
    public LifecycleEventHandler getLifecycleEventHandler() {
        return lifecycleEventHandler;
    }
}
