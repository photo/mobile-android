
package com.trovebox.android.common.ui.widget;

import org.holoeverywhere.app.AlertDialog;
import org.holoeverywhere.app.Dialog;

import android.content.DialogInterface;
import android.os.Bundle;

import com.trovebox.android.common.R;
import com.trovebox.android.common.fragment.common.CommonClosableOnRestoreDialogFragment;
import com.trovebox.android.common.util.CommonUtils;

/**
 * Basic Yes/No dialog fragment
 * 
 * @author Eugene Popovich
 */
public class YesNoDialogFragment extends CommonClosableOnRestoreDialogFragment {
    public static interface YesNoButtonPressedHandler {
        void yesButtonPressed(DialogInterface dialog);

        void noButtonPressed(DialogInterface dialog);
    }

    YesNoButtonPressedHandler handler;
    boolean cancelable;
    String message;

    /**
     * @param message
     * @param handler
     * @return
     */
    public static YesNoDialogFragment newInstance(int message, YesNoButtonPressedHandler handler) {
        return newInstance(message == 0 ? null : CommonUtils.getStringResource(message), true,
                handler);
    }

    /**
     * @param message
     * @param cancelable whether dialog can be cancelled by the back button
     * @param handler
     * @return
     */
    public static YesNoDialogFragment newInstance(String message, boolean cancelable,
            YesNoButtonPressedHandler handler) {
        YesNoDialogFragment frag = new YesNoDialogFragment();
        frag.handler = handler;
        frag.message = message;
        frag.cancelable = cancelable;
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(),
                R.style.Theme_Trovebox_Dialog_Light).setCancelable(cancelable)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                        if (handler != null) {
                            handler.yesButtonPressed(dialog);
                        }
                    }
                }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                        if (handler != null) {
                            handler.noButtonPressed(dialog);
                        }
                    }
                });
        if (message != null) {
            builder.setMessage(message);
        }
        return builder.create();
    }
}
