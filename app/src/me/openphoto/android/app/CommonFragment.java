
package me.openphoto.android.app;

import me.openphoto.android.app.util.CommonUtils;
import me.openphoto.android.app.util.GuiUtils;
import me.openphoto.android.app.util.Utils;
import android.os.Bundle;
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
        CommonUtils.debug(TAG, "onAttach: " + getClass().getSimpleName());
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        CommonUtils.debug(TAG, "onCreate: " + getClass().getSimpleName());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState)
    {
        CommonUtils.debug(TAG, "onCreateView: " + getClass().getSimpleName());
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        CommonUtils.debug(TAG, "onDetach: " + getClass().getSimpleName());
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        CommonUtils.debug(TAG, "onDestroy: " + getClass().getSimpleName());
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        CommonUtils.debug(TAG, "onActivityCreated: " + getClass().getSimpleName());
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        CommonUtils.debug(TAG, "onDestroyView: " + getClass().getSimpleName());
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        CommonUtils.debug(TAG, "onSaveInstanceState: " + getClass().getSimpleName());
    }

    @Override
    public void onResume()
    {
        super.onResume();
        CommonUtils.debug(TAG, "onResume: " + getClass().getSimpleName());
    }

    @Override
    public void onPause()
    {
        super.onPause();
        CommonUtils.debug(TAG, "onPause: " + getClass().getSimpleName());
    }

    @Override
    public void onViewCreated(View view)
    {
        super.onViewCreated(view);
        CommonUtils.debug(TAG, "onViewCreated: " + getClass().getSimpleName());
    }

    @Override
    public void onStart()
    {
        super.onStart();
        CommonUtils.debug(TAG, "onStart: " + getClass().getSimpleName());
    }

    @Override
    public void onStop()
    {
        super.onStop();
        CommonUtils.debug(TAG, "onStop: " + getClass().getSimpleName());
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
