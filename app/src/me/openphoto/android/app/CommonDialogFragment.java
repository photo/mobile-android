
package me.openphoto.android.app;

import me.openphoto.android.app.util.GuiUtils;
import android.os.Bundle;

import com.WazaBe.HoloEverywhere.sherlock.SDialogFragment;

/**
 * Common parent dialog fragment
 * 
 * @author Eugene Popovich
 */
public class CommonDialogFragment extends SDialogFragment
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
