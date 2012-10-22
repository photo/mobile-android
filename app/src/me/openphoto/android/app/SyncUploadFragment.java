package me.openphoto.android.app;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.WazaBe.HoloEverywhere.LayoutInflater;

public class SyncUploadFragment extends CommonFragment
{
	PreviousStepFlow previousStepFlow;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState)
	{
		super.onCreateView(inflater, container, savedInstanceState);
		View v = inflater.inflate(R.layout.fragment_sync_upload_settings,
				container, false);
		Button nextStepBtn = (Button) v.findViewById(R.id.nextBtn);
		nextStepBtn.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if (previousStepFlow != null)
				{
					previousStepFlow.activatePreviousStep();
				}
			}
		});
		return v;
	}
	public PreviousStepFlow getPreviousStepFlow()
	{
		return previousStepFlow;
	}

	public void setPreviousStepFlow(PreviousStepFlow previousStepFlow)
	{
		this.previousStepFlow = previousStepFlow;
	}

	static interface PreviousStepFlow
	{
		void activatePreviousStep();
	}
}
