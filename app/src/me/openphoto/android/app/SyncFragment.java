package me.openphoto.android.app;

import me.openphoto.android.app.SyncImageSelectionFragment.NextStepFlow;
import me.openphoto.android.app.SyncUploadFragment.PreviousStepFlow;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.ViewGroup;

import com.WazaBe.HoloEverywhere.LayoutInflater;

public class SyncFragment extends CommonFragment implements NextStepFlow,
		PreviousStepFlow, Refreshable
{
	Fragment activeFragment;
	static final String FIRST_STEP_TAG = "firstStep";
	static final String SECOND_STEP_TAG = "secondStep";

	SyncImageSelectionFragment firstStepFragment;
	SyncUploadFragment secondStepFragment;

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
		if (activeFragment != null)
		{
			transaction.detach(activeFragment);
		}
		if (attachOnly)
		{
			transaction.attach(fragment);
		} else
		{
			transaction.add(R.id.fragment_container, fragment);
		}
		// transaction.replace(R.id.fragment_container, fragment);
		transaction.commit();
		activeFragment = fragment;
	}
	@Override
	public void onDestroyView()
	{
		super.onDestroyView();
		if (!getActivity().isFinishing())
		{
			FragmentTransaction transaction = getActivity()
					.getSupportFragmentManager().beginTransaction();
			transaction.detach(activeFragment);
			transaction.commit();
		}
	};
	@Override
	public void activatePreviousStep()
	{
		if (firstStepFragment == null)
		{
			firstStepFragment = new SyncImageSelectionFragment();
			firstStepFragment.setNextStepFlow(this);
			selectFragment(firstStepFragment, false);
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
			secondStepFragment = new SyncUploadFragment();
			secondStepFragment.setPreviousStepFlow(this);
			selectFragment(secondStepFragment, false);
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

}
