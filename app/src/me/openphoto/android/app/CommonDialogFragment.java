
package me.openphoto.android.app;

import me.openphoto.android.app.util.GuiUtils;

import com.actionbarsherlock.app.SherlockDialogFragment;

/**
 * Common parent dialog fragment
 * 
 * @author Eugene Popovich
 */
public class CommonDialogFragment extends SherlockDialogFragment
{
    protected void alert(final String msg)
    {
        GuiUtils.alert(msg, getActivity());
    }
}
