
package me.openphoto.android.app;

import me.openphoto.android.app.util.GuiUtils;

import org.holoeverywhere.app.DialogFragment;

import android.os.Bundle;

/**
 * Common parent dialog fragment
 * 
 * @author Eugene Popovich
 */
public class CommonDialogFragment extends DialogFragment
{
    protected void alert(final String msg)
    {
        GuiUtils.alert(msg, getActivity());
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.Theme_OpenPhoto_Dialog_Light);
    }

}
