
package com.trovebox.android.app;

import java.util.ArrayList;
import java.util.List;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.ViewGroup;

import com.trovebox.android.app.SyncImageSelectionFragment.NextStepFlow;
import com.trovebox.android.app.SyncUploadFragment.PreviousStepFlow;
import com.trovebox.android.common.fragment.common.CommonFragment;
import com.trovebox.android.common.provider.UploadsUtils.UploadsClearedHandler;
import com.trovebox.android.common.util.BackKeyControl;
import com.trovebox.android.common.util.CommonUtils;
import com.trovebox.android.common.utils.lifecycle.ViewPagerHandler;

public class SyncFragment extends CommonFragment implements NextStepFlow,
        PreviousStepFlow, UploadsClearedHandler, BackKeyControl
{
    static final String TAG = SyncFragment.class.getSimpleName();
    static final String FIRST_STEP_TAG = "firstStepSync";
    static final String SECOND_STEP_TAG = "secondStepSync";
    static final String ACTIVE_STEP = "SyncFragmentActiveStep";

    Fragment activeFragment;
    SyncImageSelectionFragment firstStepFragment;
    SyncUploadFragment secondStepFragment;
    SyncHandler syncHandler;
    boolean instanceSaved = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initInnerFragments(savedInstanceState);
    }

    public void initInnerFragments(Bundle savedInstanceState) {
        String activeStep = savedInstanceState == null ? FIRST_STEP_TAG :
                savedInstanceState.getString(ACTIVE_STEP);
        firstStepFragment = (SyncImageSelectionFragment) getActivity()
                .getSupportFragmentManager().findFragmentByTag(FIRST_STEP_TAG);

        if (!activeStep.equals(FIRST_STEP_TAG))
        {
            detachFragmentIfNecessary(firstStepFragment);
        }
        if (firstStepFragment != null)
        {
            CommonUtils.debug(TAG, "First step fragment is not null. Setting next step flow...");
            firstStepFragment.setNextStepFlow(this);
        }

        secondStepFragment = (SyncUploadFragment)
                getActivity().getSupportFragmentManager()
                        .findFragmentByTag(SECOND_STEP_TAG);
        if (!activeStep.equals(SECOND_STEP_TAG))
        {
            detachFragmentIfNecessary(secondStepFragment);
        }
        if (secondStepFragment != null)
        {
            CommonUtils.debug(TAG,
                    "Second step fragment is not null. Setting previous step flow...");
            secondStepFragment.setPreviousStepFlow(this);
        }
        CommonUtils.debug(TAG, "Active step: " + activeStep);
        if (activeStep.equals(FIRST_STEP_TAG))
        {
            activeFragment = firstStepFragment;
        } else
        {
            activeFragment = secondStepFragment;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    boolean mMenuVisible = true;
    @Override
    public void setMenuVisibility(boolean menuVisible) {
        mMenuVisible = menuVisible;
        super.setMenuVisibility(menuVisible);
        if (activeFragment != null)
        {
            activeFragment.setMenuVisibility(menuVisible);
        }
    }

    boolean mUserVisibleHint = true;
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        mUserVisibleHint = isVisibleToUser;
        super.setUserVisibleHint(isVisibleToUser);
        if (activeFragment != null)
        {
            activeFragment.setUserVisibleHint(isVisibleToUser);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (activeFragment != null)
        {
            outState.putString(ACTIVE_STEP, getTagForFragment(activeFragment));
        }
        instanceSaved = true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState)
    {
        super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.fragment_sync_switch, container,
                false);
        init(v);
        return v;
    }
    @Override
    public void onResume() {
        super.onResume();
        // additional fix for the issue #216
        instanceSaved = false;
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        syncHandler = (SyncHandler) activity;
    }

    void init(View view)
    {
        if (activeFragment != null)
        {
            selectFragment(activeFragment, true);
        } else
        {
            activatePreviousStep();
        }
    }

    void selectFragment(Fragment fragment, boolean attachOnly)
    {
        CommonUtils.debug(TAG, "Selecting fragment: " + fragment + "; attachOnly: " + attachOnly);
        if (getActivity() == null)
        {
            CommonUtils.debug(TAG, "Fragment is no more attached to activity.");
            return;
        }
        FragmentTransaction transaction = getActivity()
                .getSupportFragmentManager().beginTransaction();
        if (activeFragment != null && !activeFragment.isDetached())
        {
            transaction.detach(activeFragment);
        }
        if (attachOnly)
        {
            transaction.attach(fragment);
        } else
        {
            transaction.replace(R.id.fragment_container, fragment, getTagForFragment(fragment));
        }
        transaction.commit();
        activeFragment = fragment;
        activeFragment.setMenuVisibility(mMenuVisible);
        activeFragment.setUserVisibleHint(mUserVisibleHint);
    }

    String getTagForFragment(Fragment fragment)
    {
        if (fragment == firstStepFragment)
        {
            return FIRST_STEP_TAG;
        } else
        {
            return SECOND_STEP_TAG;
        }
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        detachActiveFragment();
    }

    public void detachActiveFragment()
    {
        if (activeFragment != null && !instanceSaved && !getActivity().isFinishing())
        {
            FragmentTransaction transaction = getActivity()
                    .getSupportFragmentManager().beginTransaction();
            transaction.detach(activeFragment);
            transaction.commit();
            // activeFragment = null;
        }
    };

    public void detachFragmentIfNecessary(Fragment fragment) {
        if (fragment != null && !fragment.isDetached())
        {
            CommonUtils.debug(TAG, "Detaching fragment: " + fragment);
            getActivity().getSupportFragmentManager().beginTransaction()
                    .detach(fragment)
                    .commit();
        }
    }

    @Override
    public void activatePreviousStep()
    {
        if (getActivity() == null || getActivity().isFinishing() || instanceSaved)
        {
            CommonUtils
                    .debug(TAG,
                            "Skipping previous step activation because of finishing activity or saved instance state.");
            return;
        }
        if (firstStepFragment == null)
        {
            firstStepFragment = (SyncImageSelectionFragment) getActivity()
                    .getSupportFragmentManager().findFragmentByTag(FIRST_STEP_TAG);
            detachFragmentIfNecessary(firstStepFragment);
            boolean attachOnly = true;
            if (firstStepFragment == null)
            {
                firstStepFragment = new SyncImageSelectionFragment();
                attachOnly = false;
            }
            firstStepFragment.setNextStepFlow(this);
            selectFragment(firstStepFragment, attachOnly);
        } else
        {
            selectFragment(firstStepFragment, true);
        }
    }

    @Override
    public void activateNextStep()
    {
        if (getActivity() == null)
        {
            CommonUtils.debug(TAG, "Fragment is no more attached to activity.");
            return;
        }
        if (secondStepFragment == null)
        {
            secondStepFragment = (SyncUploadFragment) getActivity().getSupportFragmentManager()
                    .findFragmentByTag(SECOND_STEP_TAG);
            detachFragmentIfNecessary(secondStepFragment);
            boolean attachOnly = true;
            if (secondStepFragment == null)
            {
                secondStepFragment = new SyncUploadFragment();
                attachOnly = false;
            }
            secondStepFragment.setPreviousStepFlow(this);
            selectFragment(secondStepFragment, attachOnly);
        } else
        {
            selectFragment(secondStepFragment, true);
        }
    }

    @Override
    public ArrayList<String> getSelectedFileNames()
    {
        return firstStepFragment.getSelectedFileNames();
    }

    @Override
    public int getSelectedCount() {
        return firstStepFragment.getSelectedCount();
    }

    public void syncStarted(List<String> processedFileNames)
    {
        // detachActiveFragment();
        firstStepFragment.clear();
        firstStepFragment.addProcessedValues(processedFileNames);
        secondStepFragment.clear();
        activatePreviousStep();
        if (syncHandler != null)
        {
            syncHandler.syncStarted();
        }
    }

    static interface SyncHandler
    {
        void syncStarted();
    }

    @Override
    public void uploadsCleared()
    {
        firstStepFragment.uploadsCleared();
    }

    @Override
    public boolean isBackKeyOverrode() {
        if (activeFragment != null && activeFragment == secondStepFragment)
        {
            activatePreviousStep();
            return true;
        }
        return false;
    }

    @Override
    public void pageActivated() {
        if (activeFragment != null && activeFragment instanceof ViewPagerHandler)
        {
            ((ViewPagerHandler) activeFragment).pageActivated();
        }
    }

    @Override
    public void pageDeactivated() {
        if (activeFragment != null && activeFragment instanceof ViewPagerHandler)
        {
            ((ViewPagerHandler) activeFragment).pageDeactivated();
        }
    }
}
