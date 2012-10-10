package me.openphoto.android.app;

import me.openphoto.android.app.util.GuiUtils;

import com.actionbarsherlock.app.SherlockDialogFragment;

/**
 * Common parent dialog fragment
 * 
 * @author Eugene Popovich
 * @version
 *          10.10.2012
 *          <br>- created
 * 
 */
public class CommonDialogFragment extends SherlockDialogFragment
{
	protected void alert(final String msg)
	{
		GuiUtils.alert(msg, getActivity());
	}
}
