
package com.trovebox.android.common.utils.lifecycle;

import android.content.Intent;
import android.os.Bundle;

import com.trovebox.android.common.activity.CommonActivity;

/**
 * Lifecycle listener. This can be used to listen for Activity or Fragment
 * lifecycle events. Note that activity or fragment should use instance of
 * {@link LifecycleEventHandler} and call fire events method manually. See
 * {@link CommonActivity}
 * 
 * @author Eugene Popovich
 */
public interface LifecycleListener {
    /**
     * Executed when onStart event fires
     */
    void onStart();

    /**
     * Executed when onStop event fires
     */
    void onStop();

    /**
     * Executed when onSaveInstanceState event fires
     */
    void onSaveInstanceState(Bundle outState);

    /**
     * Executed when onCreate event fires
     */
    void onCreate(Bundle savedInstanceState);

    /**
     * Executed when onDestroy event fires
     */
    void onDestroy();

    /**
     * Executed when onResume event fires
     */
    void onResume();

    /**
     * Executed when onPause event fires
     */
    void onPause();

    /**
     * Executed when onActivityResult event fires
     */
    void onActivityResult(int requestCode, int resultCode, Intent data);
}
