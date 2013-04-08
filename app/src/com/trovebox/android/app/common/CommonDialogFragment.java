
package com.trovebox.android.app.common;


import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Activity;
import org.holoeverywhere.app.Dialog;
import org.holoeverywhere.app.DialogFragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;

import com.trovebox.android.app.util.CommonUtils;
import com.trovebox.android.app.util.TrackerUtils;

/**
 * Common parent dialog fragment
 * 
 * @author Eugene Popovich
 */
public class CommonDialogFragment extends DialogFragment
{
    static final String TAG = CommonFragment.class.getSimpleName();
    static final String CATEGORY = "Dialog Fragment Lifecycle";

    protected void trackLifecycleEvent(String event)
    {
        CommonUtils.debug(TAG, event + ": " + getClass().getSimpleName());
        TrackerUtils.trackEvent(CATEGORY, event, getClass().getSimpleName());
    }

    public CommonDialogFragment()
    {
        trackLifecycleEvent("Constructor");
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        trackLifecycleEvent("onCancel");
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        trackLifecycleEvent("onCreateDialog");
        return super.onCreateDialog(savedInstanceState);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        trackLifecycleEvent("onDismiss");
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        trackLifecycleEvent("onAttach");
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        trackLifecycleEvent("onCreate");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState)
    {
        trackLifecycleEvent("onCreateView");
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        trackLifecycleEvent("onDetach");
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        trackLifecycleEvent("onDestroy");
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        trackLifecycleEvent("onActivityCreated");
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        trackLifecycleEvent("onDestroyView");
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        trackLifecycleEvent("onSaveInstanceState");
    }

    @Override
    public void onResume()
    {
        super.onResume();
        trackLifecycleEvent("onResume");
    }

    @Override
    public void onPause()
    {
        super.onPause();
        trackLifecycleEvent("onPause");
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        trackLifecycleEvent("onViewCreated");
    }

    @Override
    public void onStart()
    {
        super.onStart();
        trackLifecycleEvent("onStart");
        TrackerUtils.trackView(this);
    }

    @Override
    public void onStop()
    {
        super.onStop();
        trackLifecycleEvent("onStop");
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        trackLifecycleEvent("onConfigurationChanged");
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        trackLifecycleEvent("onActivityResult");
        Handler handler = new Handler();
        handler.post(new Runnable() {

            @Override
            public void run() {
                onActivityResultUI(requestCode, resultCode, data);
            }

        });
    }

    public void onActivityResultUI(int requestCode, int resultCode, Intent data)
    {
        trackLifecycleEvent("onActivityResultUI");
    }
}
