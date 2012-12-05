package me.openphoto.android.app.ui.widget;

import org.holoeverywhere.app.DialogFragment;

import android.os.Bundle;

public class ClosableOnRestoreDialogFragment extends DialogFragment {
    boolean isRestore = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isRestore = savedInstanceState != null;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isRestore)
        {
            dismiss();
        }
    }
}
