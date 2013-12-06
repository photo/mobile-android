
package com.trovebox.android.common.common.lifecycle;

import android.content.Intent;
import android.os.Bundle;

import com.trovebox.android.common.util.CommonUtils;

/**
 * Basic implementation of {@link LifecycleListener}
 * 
 * @author Eugene Popovich
 */
public class LifecycleAdapter implements LifecycleListener {
    static final String TAG = LifecycleAdapter.class.getSimpleName();
    String holderClassName;

    void trackLifecycleEvent(String event) {
        CommonUtils.debug(TAG, event + ": " + holderClassName);
    }

    /**
     * @param holder - used for logs
     */
    public LifecycleAdapter(Object holder) {
        if (holder == null) {
            throw new IllegalArgumentException("holder argument should not be null");
        }
        this.holderClassName = holder.getClass().getSimpleName();
    }

    @Override
    public void onStart() {
        trackLifecycleEvent("onStart");
    }

    @Override
    public void onStop() {
        trackLifecycleEvent("onStop");

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        trackLifecycleEvent("onSaveInstanceState");

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        trackLifecycleEvent("onCreate");

    }

    @Override
    public void onDestroy() {
        trackLifecycleEvent("onDestroy");

    }

    @Override
    public void onResume() {
        trackLifecycleEvent("onResume");

    }

    @Override
    public void onPause() {
        trackLifecycleEvent("onPause");

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        trackLifecycleEvent("onActivityResult");

    }

}
