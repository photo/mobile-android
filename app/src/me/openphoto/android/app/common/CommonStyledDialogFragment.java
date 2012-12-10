
package me.openphoto.android.app.common;

import me.openphoto.android.app.R;
import me.openphoto.android.app.util.GuiUtils;
import android.os.Bundle;

/**
 * Common styled dialog fragment
 * 
 * @author Eugene Popovich
 */
public class CommonStyledDialogFragment extends CommonDialogFragment
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
        setDialogType(DialogType.AlertDialog);
    }

}
