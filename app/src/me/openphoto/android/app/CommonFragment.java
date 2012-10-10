package me.openphoto.android.app;

import me.openphoto.android.app.util.GuiUtils;
import me.openphoto.android.app.util.Utils;
import android.app.Activity;

import com.actionbarsherlock.app.SherlockFragment;

/**
 * Common parent fragment. All the tab fragments under
 * MainActivity should to inherit this class
 * 
 * @author Eugene Popovich
 * @version
 *          10.10.2012
 *          <br>- alert method functionality moved to GuiUtils
 *          <p>
 *          05.10.2012
 *          <br>- removed reference to NetworkAccessControl. Now Utils.isOnline
 *          method is used instead
 *          <p>
 *          03.10.2012
 *          <br>- created
 * 
 */
public class CommonFragment extends SherlockFragment
{
	protected void alert(final String msg)
	{
		GuiUtils.alert(msg, getActivity());
	}

	@Override
	public void onAttach(Activity activity)
	{
		super.onAttach(activity);
	}

	public boolean checkOnline()
	{
		boolean result = Utils.isOnline(getActivity());
		if (!result)
		{
			alert(getString(R.string.noInternetAccess));
		}
		return result;
	}
}
