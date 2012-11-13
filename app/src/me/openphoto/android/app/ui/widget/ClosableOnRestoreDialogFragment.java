package me.openphoto.android.app.ui.widget;

import android.os.Bundle;

import com.WazaBe.HoloEverywhere.sherlock.SDialogFragment;

public class ClosableOnRestoreDialogFragment extends SDialogFragment {
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
