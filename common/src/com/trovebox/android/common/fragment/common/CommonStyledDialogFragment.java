
package com.trovebox.android.common.fragment.common;

import android.os.Bundle;

import com.trovebox.android.common.R;
import com.trovebox.android.common.util.GuiUtils;

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
