package com.trovebox.android.common.fragment.sync;

import org.holoeverywhere.app.Activity;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.trovebox.android.common.ui.widget.PopupMenuWithIcons;
import com.trovebox.android.common.ui.widget.PopupMenuWithIcons.OnMenuItemClickListener;

public class SyncSelectionMenu implements OnClickListener {
    @SuppressWarnings("unused")
    private static final String TAG = "SelectionMenu";

    private final Activity mActivity;
    private final Button mButton;
    private PopupMenuWithIcons mPopupList;
    private final boolean mHasPopupMenu;

    public SyncSelectionMenu(int popupMenuResource, boolean hasPopupMenu, Activity activity,
            Button button,
            OnMenuItemClickListener listener) {
        mActivity = activity;
        mButton = button;
        mHasPopupMenu = hasPopupMenu;
        if (mHasPopupMenu) {
            mPopupList = new PopupMenuWithIcons(activity, mButton);
            mPopupList.setOnMenuItemClickListener(listener);
            MenuInflater inflater = mActivity.getSupportMenuInflater();
            Menu menu = mPopupList.getMenu();
            inflater.inflate(popupMenuResource, menu);
            mButton.setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View v) {
        if (mHasPopupMenu) {
            mPopupList.show();
        }
    }

    public void setTitle(CharSequence title) {
        mButton.setText(title);
    }
}