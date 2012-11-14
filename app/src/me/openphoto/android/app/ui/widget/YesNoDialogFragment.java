
package me.openphoto.android.app.ui.widget;

import me.openphoto.android.app.R;
import android.content.DialogInterface;
import android.os.Bundle;

import com.WazaBe.HoloEverywhere.app.AlertDialog;
import com.WazaBe.HoloEverywhere.app.Dialog;

/**
 * Basic Yes/No dialog fragment
 * 
 * @author Eugene Popovich
 */
public class YesNoDialogFragment extends ClosableOnRestoreDialogFragment
{
    public static interface YesNoButtonPressedHandler
    {
        void yesButtonPressed(DialogInterface dialog);

        void noButtonPressed(DialogInterface dialog);
    }

    YesNoButtonPressedHandler handler;
    public static YesNoDialogFragment newInstance(
            int message,
            YesNoButtonPressedHandler handler)
    {
        YesNoDialogFragment frag = new YesNoDialogFragment();
        Bundle args = new Bundle();
        args.putInt("message", message);
        frag.handler = handler;
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        int message = getArguments().getInt("message");
        return new AlertDialog.Builder(getActivity())
                .setMessage(message)
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
                )
                .create();
    }
}
