package me.openphoto.android.app;

import me.openphoto.android.app.util.GuiUtils;
import me.openphoto.android.app.util.Utils;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.WazaBe.HoloEverywhere.LayoutInflater;
import com.WazaBe.HoloEverywhere.app.Activity;
import com.WazaBe.HoloEverywhere.sherlock.SFragment;

/**
 * Common parent fragment. All the tab fragments under MainActivity should to
 * inherit this class
 * 
 * @author Eugene Popovich
 */
public class CommonFragment extends SFragment
{
	static final String TAG = CommonFragment.class.getSimpleName();

	@Override
	public void onAttach(Activity activity)
	{
		super.onAttach(activity);
		Log.d(TAG, "onAttach: " + getClass().getSimpleName());
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate: " + getClass().getSimpleName());
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState)
	{
		Log.d(TAG, "onCreateView: " + getClass().getSimpleName());
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	public void onDetach()
	{
		super.onDetach();
		Log.d(TAG, "onDetach: " + getClass().getSimpleName());
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		Log.d(TAG, "onDestroy: " + getClass().getSimpleName());
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		Log.d(TAG, "onActivityCreated: " + getClass().getSimpleName());
	}

	@Override
	public void onDestroyView()
	{
		super.onDestroyView();
		Log.d(TAG, "onDestroyView: " + getClass().getSimpleName());
	}
	@Override
	public void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		Log.d(TAG, "onSaveInstanceState: " + getClass().getSimpleName());
	}

	@Override
	public void onResume()
	{
		super.onResume();
		Log.d(TAG, "onResume: " + getClass().getSimpleName());
	}

	@Override
	public void onPause()
	{
		super.onPause();
		Log.d(TAG, "onPause: " + getClass().getSimpleName());
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);
		Log.d(TAG, "onViewCreated: " + getClass().getSimpleName());
	}

	@Override
	public void onStart()
	{
		super.onStart();
		Log.d(TAG, "onStart: " + getClass().getSimpleName());
	}

	@Override
	public void onStop()
	{
		super.onStop();
		Log.d(TAG, "onStop: " + getClass().getSimpleName());
	}

	public boolean checkOnline()
	{
		boolean result = Utils.isOnline(getActivity());
		if (!result)
		{
			GuiUtils.alert(R.string.noInternetAccess);
		}
		return result;
	}
}
