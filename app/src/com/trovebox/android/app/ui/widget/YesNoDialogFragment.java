
package com.trovebox.android.app.ui.widget;


import org.holoeverywhere.app.AlertDialog;
import org.holoeverywhere.app.Dialog;

import com.trovebox.android.app.R;
import com.trovebox.android.app.common.CommonClosableOnRestoreDialogFragment;

import android.content.DialogInterface;
import android.os.Bundle;

/**
 * Basic Yes/No dialog fragment
 * 
 * @author Eugene Popovich
 */
public class YesNoDialogFragment extends CommonClosableOnRestoreDialogFragment
{
    public static interface YesNoButtonPressedHandler
    {
        void yesButtonPressed(DialogInterface dialog);

        void noButtonPressed(DialogInterface dialog);
    }

    YesNoButtonPressedHandler handler;
    boolean cancelable;
    int message;

    /**
     * @param message
     * @param handler
     * @return
     */
    public static YesNoDialogFragment newInstance(
            int message,
            YesNoButtonPressedHandler handler)
    {
        return newInstance(message, true, handler);
    }

    /**
     * @param message
     * @param cancelable whether dialog can be cancelled by the back button
     * @param handler
     * @return
     */
    public static YesNoDialogFragment newInstance(
            int message,
            boolean cancelable,
            YesNoButtonPressedHandler handler)
    {
        YesNoDialogFragment frag = new YesNoDialogFragment();
        frag.handler = handler;
        frag.message = message;
        frag.cancelable = cancelable;
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(),
                R.style.Theme_Trovebox_Dialog_Light)
                .setCancelable(cancelable)
                .setPositiveButton(R.string.yes,
                        new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog,
                                    int whichButton)
                            {
                                if (handler != null)
                                {
                                    handler.yesButtonPressed(dialog);
                                }
                            }
                        }
                )
                .setNegativeButton(R.string.no,
                        new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog,
                                    int whichButton)
                            {
                                if (handler != null)
                                {
                                    handler.noButtonPressed(dialog);
                                }
                            }
                        }
                );
        if(message != 0)
        {
            builder.setMessage(message);
        }
        return builder.create();
    }
}
