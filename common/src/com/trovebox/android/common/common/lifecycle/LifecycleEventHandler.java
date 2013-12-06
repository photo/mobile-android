
package com.trovebox.android.common.common.lifecycle;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import android.content.Intent;
import android.os.Bundle;

import com.trovebox.android.common.util.CommonUtils;
import com.trovebox.android.common.util.RunnableWithParameter;

/**
 * Lifecycle event handler. Stores event listener and can fire events to them
 * 
 * @author Eugene Popovich
 */
/**
 * @author Eugene
 */
public class LifecycleEventHandler {
    private static final String TAG = LifecycleEventHandler.class.getSimpleName();
    String holderClassName;

    /**
     * the registered listeners
     */
    private List<LifecycleListener> listeners = new ArrayList<LifecycleListener>();
    /**
     * the pending listeners to remove list
     */
    private List<LifecycleListener> listenersToRemove = new ArrayList<LifecycleListener>();

    /**
     * the work indicator
     */
    private AtomicInteger workers = new AtomicInteger(0);

    void trackEvent(String event) {
        CommonUtils.debug(TAG, event + ": " + holderClassName);
    }

    public LifecycleEventHandler(Object holder) {
        if (holder == null) {
            throw new IllegalArgumentException("holder argument should not be null");
        }
        this.holderClassName = holder.getClass().getSimpleName();
    }

    /**
     * Add the lifecycle listener
     * 
     * @param listener
     */
    public void addLifecycleListener(LifecycleListener listener) {
        trackEvent("addLifecycleListener");
        synchronized (listeners) {
            trackEvent("addLifecycleListener.Synchronized");
            listeners.add(listener);
        }
    }

    /**
     * Remove the lifecycle listener
     * 
     * @param listener
     */
    public void removeLifecycleListener(LifecycleListener listener) {
        trackEvent("removeLifecycleListener");
        try {
            if (workers.getAndIncrement() == 0) {
                trackEvent("removeLifecycleListener. No pending work, removing listener");
                synchronized (listeners) {
                    trackEvent("removeLifecycleListener. No pending work, removing listener. Synchronized");
                    listeners.remove(listener);
                }
            } else {
                trackEvent("removeLifecycleListener. Where is pending work, adding listener to delayed removal");
                synchronized (listenersToRemove) {
                    CommonUtils
                            .debug(TAG,
                                    "removeLifecycleListener. Where is pending work, adding listener to delayed removal. Synchronized");
                    listenersToRemove.add(listener);
                }
            }
        } finally {
            workers.decrementAndGet();
        }
    }

    /**
     * Fire event occurred
     * 
     * @param runnable
     */
    private void fireEventOccurred(RunnableWithParameter<LifecycleListener> runnable) {
        workers.getAndIncrement();
        try {
            for (LifecycleListener listener : listeners) {
                if (!listenersToRemove.contains(listener)) {
                    runnable.run(listener);
                }
            }
        } finally {
            workers.decrementAndGet();
        }
        performListenersRemoval();
    }

    /**
     * Process listeners waiting for removal
     */
    private void performListenersRemoval() {
        try {
            if (workers.getAndIncrement() == 0) {
                trackEvent("performListenersRemoval. Performing delayed listeners removal");
                synchronized (listeners) {
                    trackEvent("performListenersRemoval. Performing delayed listeners removal. Synchronized listeners");
                    synchronized (listenersToRemove) {
                        CommonUtils
                                .debug(TAG,
                                        "performListenersRemoval. Performing delayed listeners removal. Synchronized listenersToRemove");
                        trackEvent("performListenersRemoval. Synchronized lists for removal");
                        if (listenersToRemove.isEmpty()) {
                            CommonUtils
                                    .debug(TAG,
                                            "performListenersRemoval. Listeners to remove are empty. Skipping");
                        } else {
                            for (LifecycleListener listener : listenersToRemove) {
                                trackEvent("performListenersRemoval. Removing listener");
                                listeners.remove(listener);
                            }
                            trackEvent("performListenersRemoval. Clearing listenersToRemove");
                            listenersToRemove.clear();
                        }
                    }
                }
            }
        } finally {
            workers.decrementAndGet();
        }
    }

    /**
     * Should be called on the holder onStart() method. This will fire onStart
     * event to all registered event listeners
     */
    public void fireOnStartEvent() {
        trackEvent("fireOnStartEvent");
        fireEventOccurred(onStartRunnable);
    }

    /**
     * Should be called on the holder onStop() method. This will fire onStop
     * event to all registered event listeners
     */
    public void fireOnStopEvent() {
        trackEvent("fireOnStopEvent");
        fireEventOccurred(onStopRunnable);
    }

    /**
     * Should be called on the holder onSaveInstanceState() method. This will
     * fire onSaveInstanceState event to all registered event listeners
     */
    public void fireOnSaveInstanceStateEvent(final Bundle outState) {
        trackEvent("fireOnSaveInstanceStateEvent");
        onSaveInstanceStateRunnable.outState = outState;
        fireEventOccurred(onSaveInstanceStateRunnable);
    }

    /**
     * Should be called on the holder onCreate() method. This will fire onCreate
     * event to all registered event listeners
     */
    public void fireOnCreateEvent(final Bundle savedInstanceState) {
        trackEvent("fireOnCreateEvent");
        onCreateRunnable.savedInstanceState = savedInstanceState;
        fireEventOccurred(onCreateRunnable);
    }

    /**
     * Should be called on the holder onDestroy() method. This will fire
     * onDestroy event to all registered event listeners
     */
    public void fireOnDestroyEvent() {
        trackEvent("fireOnDestroyEvent");
        fireEventOccurred(onDestroyRunnable);
    }

    /**
     * Should be called on the holder onResume() method. This will fire onResume
     * event to all registered event listeners
     */
    public void fireOnResumeEvent() {
        trackEvent("fireOnResumeEvent");
        fireEventOccurred(onResumeRunnable);
    }

    /**
     * Should be called on the holder onPause() method. This will fire onPause
     * event to all registered event listeners
     */
    public void fireOnPauseEvent() {
        trackEvent("fireOnPauseEvent");
        fireEventOccurred(onPauseRunnable);
    }

    /**
     * Should be called on the holder onActivityResult() method. This will fire
     * onActivityResult event to all registered event listeners
     */
    public void fireOnActivityResultEvent(final int requestCode, final int resultCode,
            final Intent data) {
        trackEvent("fireOnActivityResultEvent");
        onActivityResultRunnable.requestCode = requestCode;
        onActivityResultRunnable.resultCode = resultCode;
        onActivityResultRunnable.data = data;
        fireEventOccurred(onActivityResultRunnable);
    }

    /**
     * On start runnable which is used on fire method
     */
    RunnableWithParameter<LifecycleListener> onStartRunnable = new RunnableWithParameter<LifecycleListener>() {

        @Override
        public void run(LifecycleListener listener) {
            trackEvent("onStartRunnable.run");
            listener.onStart();
        }
    };
    /**
     * On stop runnable which is used on fire method
     */
    RunnableWithParameter<LifecycleListener> onStopRunnable = new RunnableWithParameter<LifecycleListener>() {

        @Override
        public void run(LifecycleListener listener) {
            trackEvent("onStopRunnable.run");
            listener.onStop();
        }
    };
    /**
     * On save instance state runnable which is used on fire method
     */
    OnSaveInstanceStateRunnable onSaveInstanceStateRunnable = new OnSaveInstanceStateRunnable() {
        @Override
        public void run(LifecycleListener listener) {
            trackEvent("onSaveInstanceStateRunnable.run");
            super.run(listener);
        }
    };
    /**
     * On create runnable which is used on fire method
     */
    OnCreateRunnable onCreateRunnable = new OnCreateRunnable() {
        @Override
        public void run(LifecycleListener listener) {
            trackEvent("onCreateRunnable.run");
            super.run(listener);
        }
    };
    /**
     * On destroy runnable which is used on fire method
     */
    RunnableWithParameter<LifecycleListener> onDestroyRunnable = new RunnableWithParameter<LifecycleListener>() {

        @Override
        public void run(LifecycleListener listener) {
            trackEvent("onDestroyRunnable.run");
            listener.onDestroy();
        }
    };
    /**
     * On resume runnable which is used on fire method
     */
    RunnableWithParameter<LifecycleListener> onResumeRunnable = new RunnableWithParameter<LifecycleListener>() {

        @Override
        public void run(LifecycleListener listener) {
            trackEvent("onResumeRunnable.run");
            listener.onResume();
        }
    };
    /**
     * On pause runnable which is used on fire method
     */
    RunnableWithParameter<LifecycleListener> onPauseRunnable = new RunnableWithParameter<LifecycleListener>() {

        @Override
        public void run(LifecycleListener listener) {
            trackEvent("onPauseRunnable.run");
            listener.onPause();
        }
    };
    /**
     * On activity result runnable which is used on fire method
     */
    OnActivityResultRunnable onActivityResultRunnable = new OnActivityResultRunnable() {
        @Override
        public void run(LifecycleListener listener) {
            trackEvent("onActivityResultRunnable.run");
            super.run(listener);
        }
    };

    /**
     * Basic on save instance state runnable. Need separate class because it
     * holds the parameter
     */
    private class OnSaveInstanceStateRunnable implements RunnableWithParameter<LifecycleListener> {
        Bundle outState;

        @Override
        public void run(LifecycleListener listener) {
            listener.onSaveInstanceState(outState);
        }
    }

    /**
     * Basic on create runnable. Need separate class because it holds the
     * parameter
     */
    private class OnCreateRunnable implements RunnableWithParameter<LifecycleListener> {
        Bundle savedInstanceState;

        @Override
        public void run(LifecycleListener listener) {
            listener.onCreate(savedInstanceState);
        }
    }

    /**
     * Basic on activity result runnable. Need separate class because it holds
     * the parameters
     */
    private class OnActivityResultRunnable implements RunnableWithParameter<LifecycleListener> {
        int requestCode;
        int resultCode;
        Intent data;

        @Override
        public void run(LifecycleListener listener) {
            listener.onActivityResult(requestCode, resultCode, data);
        }
    }

    /**
     * Objects containing {@link LifecycleEventHandler} may implement this
     * interface
     */
    public static interface HasLifecycleEventHandler {
        /**
         * Get the lifecycle event handler
         * 
         * @return
         */
        LifecycleEventHandler getLifecycleEventHandler();
    }
}
