
package com.trovebox.android.app.common;

import com.trovebox.android.app.R;
import com.trovebox.android.app.util.GuiUtils;

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
        setStyle(STYLE_NORMAL, R.style.Theme_Trovebox_Dialog_Light);
        setDialogType(DialogType.AlertDialog);
    }

}
