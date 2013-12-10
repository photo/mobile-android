
package com.trovebox.android.common.fragment.common;

import java.io.Serializable;
import java.lang.ref.WeakReference;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Activity;
import org.holoeverywhere.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;

import com.trovebox.android.common.util.CommonUtils;
import com.trovebox.android.common.util.ObjectAccessor;
import com.trovebox.android.common.util.RunnableWithResult;
import com.trovebox.android.common.util.TrackerUtils;
import com.trovebox.android.common.utils.lifecycle.ViewPagerHandler;

/**
 * Common parent fragment. All the tab fragments under MainActivity should to
 * inherit this class
 * 
 * @author Eugene Popovich
 */
public class CommonFragment extends Fragment implements ViewPagerHandler {
    static final String TAG = CommonFragment.class.getSimpleName();
    static final String CATEGORY = "Fragment Lifecycle";
    private boolean instanceSaved = false;
    protected boolean isActivePage = false;

    void trackLifecycleEvent(String event) {
        CommonUtils.debug(TAG, event + ": " + getClass().getSimpleName());
        TrackerUtils.trackEvent(CATEGORY, event, getClass().getSimpleName());
    }

    public CommonFragment() {
        trackLifecycleEvent("Constructor");
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        trackLifecycleEvent("onAttach");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        trackLifecycleEvent("onCreate");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        trackLifecycleEvent("onCreateView");
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        trackLifecycleEvent("onDetach");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        trackLifecycleEvent("onDestroy");
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        trackLifecycleEvent("onActivityCreated");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        trackLifecycleEvent("onDestroyView");
        isActivePage = false;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        instanceSaved = true;
        trackLifecycleEvent("onSaveInstanceState");
    }

    @Override
    public void onResume() {
        super.onResume();
        instanceSaved = false;
        trackLifecycleEvent("onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        trackLifecycleEvent("onPause");
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        trackLifecycleEvent("onViewCreated");
    }

    @Override
    public void onStart() {
        super.onStart();
        trackLifecycleEvent("onStart");
        TrackerUtils.trackView(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        trackLifecycleEvent("onStop");
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

    public void onActivityResultUI(int requestCode, int resultCode, Intent data) {
        trackLifecycleEvent("onActivityResultUI");
    }

    public static interface FragmentAccessor<T extends CommonFragment> extends
            RunnableWithResult<T>, Serializable {

    }

    public boolean isInstanceSaved() {
        return instanceSaved;
    }

    @Override
    public void pageActivated() {
        trackLifecycleEvent("pageActivated");
        isActivePage = true;
    }

    @Override
    public void pageDeactivated() {
        trackLifecycleEvent("pageDeactivated");
        isActivePage = false;
    }

    public static abstract class CurrentInstanceManager<T extends CommonFragment> implements
            ObjectAccessor<T> {
        private static final long serialVersionUID = 1L;

        protected abstract void setCurrentInstance(WeakReference<T> instance);

        protected abstract WeakReference<T> getCurrentInstance();

        @Override
        public T run() {
            WeakReference<T> instance = getCurrentInstance();
            return instance == null ? null : instance.get();
        }

        public void onCreate(T item) {
            setCurrentInstance(new WeakReference<T>(item));
        }

        public void onDestroy(T item, String TAG) {
            WeakReference<T> instance = getCurrentInstance();
            if (instance != null) {
                if (instance.get() == item || instance.get() == null) {
                    CommonUtils.debug(TAG, "Nullify current instance");
                    setCurrentInstance(null);
                } else {
                    CommonUtils.debug(TAG,
                            "Skipped nullify of current instance, such as it is not the same");
                }
            }
        }
    }

}
