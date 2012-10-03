package me.openphoto.android.app;

import me.openphoto.android.app.util.NetworkAccessControl;
import android.app.Activity;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;

/**
 * Common parent fragment. All the tab fragments under
 * MainActivity should to inherit this class
 * 
 * @author Eugene Popovich
 * @version
 *          03.10.2012
 *          <br>- created
 * 
 */
public class CommonFragment extends SherlockFragment
{
	protected NetworkAccessControl networkAccessControl;
	protected void alert(final String msg)
	{
		getActivity().runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
			}
		});
	}

	@Override
	public void onAttach(Activity activity)
	{
		super.onAttach(activity);
		networkAccessControl = ((NetworkAccessControl) activity);
	}

	public boolean checkOnline()
	{
		boolean result = networkAccessControl.isOnline();
		if (!result)
		{
			alert(getString(R.string.noInternetAccess));
		}
		return result;
	}
}
