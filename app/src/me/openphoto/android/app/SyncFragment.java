
package me.openphoto.android.app;

import java.util.List;

import me.openphoto.android.app.SyncImageSelectionFragment.NextStepFlow;
import me.openphoto.android.app.SyncUploadFragment.PreviousStepFlow;
import me.openphoto.android.app.provider.UploadsUtils.UploadsClearedHandler;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.ViewGroup;

import com.WazaBe.HoloEverywhere.LayoutInflater;
import com.WazaBe.HoloEverywhere.app.Activity;

public class SyncFragment extends CommonFragment implements NextStepFlow,
        PreviousStepFlow, Refreshable, UploadsClearedHandler
{
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
            secondStepFragment.setPreviousStepFlow(this);
        }
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
    public void refresh()
    {
        if (activeFragment == firstStepFragment)
        {
            firstStepFragment.refresh();
        }
    }

    @Override
    public List<String> getSelectedFileNames()
    {
        return firstStepFragment.getSelectedFileNames();
    }

    @Override
    public void uploadStarted(List<String> processedFileNames)
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
}
