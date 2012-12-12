
package me.openphoto.android.app.common;

import me.openphoto.android.app.util.CommonUtils;
import me.openphoto.android.app.util.TrackerUtils;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Activity;
import org.holoeverywhere.preference.PreferenceFragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

/**
 * Common preference fragment with lifecycle tracking
 * 
 * @author Eugene Popovich
 */
public class CommonPreferenceFragment extends PreferenceFragment
{
    static final String TAG = CommonPreferenceFragment.class.getSimpleName();
    static final String CATEGORY = "Preference Fragment Lifecycle";

    void trackLifecycleEvent(String event)
    {
        CommonUtils.debug(TAG, event + ": " + getClass().getSimpleName());
        TrackerUtils.trackEvent(CATEGORY, event, getClass().getSimpleName());
    }

    public CommonPreferenceFragment()
    {
        trackLifecycleEvent("Constructor");
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
    public void onViewCreated(View view)
    {
        super.onViewCreated(view);
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
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        trackLifecycleEvent("onActivityResult");
    }
}
