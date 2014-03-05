package com.trovebox.android.common.fragment.sync;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Activity;
import org.holoeverywhere.widget.TextView;

import android.content.res.Resources;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.trovebox.android.common.R;
import com.trovebox.android.common.ui.widget.PopupMenuWithIcons;
import com.trovebox.android.common.util.CommonUtils;

public class SyncActionModeHandler implements ActionMode.Callback,
        PopupMenuWithIcons.OnMenuItemClickListener {

    private final Activity mActivity;
    private final SyncSelectionManager mSelectionManager;
    private SyncSelectionMenu mSelectionMenu;
    private ActionModeListener mListener;
    private ActionMode mActionMode;
    private OnClickListener mDoneButtonOnClickListener;
    private int mActionModeLayout;
    private boolean mHasPopupMenu;
    private int mActionButtonTextResource;

    public SyncActionModeHandler(int actionModeLayout, boolean hasPopupMenu,
            int actionButtonTextResource, Activity activity, SyncSelectionManager selectionManager,
            OnClickListener doneButtonOnClickListener) {
        mActivity = activity;
        mSelectionManager = selectionManager;
        mDoneButtonOnClickListener = doneButtonOnClickListener;
        mActionModeLayout = actionModeLayout;
        mHasPopupMenu = hasPopupMenu;
        mActionButtonTextResource = actionButtonTextResource;
    }

    public void destroy() {

    }

    public void pause() {

    }

    public void startActionMode() {
        Activity a = mActivity;
        mActionMode = a.startActionMode(this);
        View customView = LayoutInflater.from(a).inflate(mActionModeLayout, null);
        mActionMode.setCustomView(customView);
        mSelectionMenu = new SyncSelectionMenu(R.menu.sync_image_selection2, mHasPopupMenu, a,
                (Button) customView.findViewById(R.id.selection_menu), this);
        updateSelectionMenu();
        customizeActionModeCloseButton();
    }

    private void customizeActionModeCloseButton() {
        int buttonId = Resources.getSystem().getIdentifier("action_mode_close_button", "id",
                "android");
        View v = mActivity.findViewById(buttonId);
        if (v == null) {
            buttonId = R.id.abs__action_mode_close_button;
            v = mActivity.findViewById(buttonId);
        }
        if (v == null)
            return;
        ViewGroup ll = (ViewGroup) v;
        if (ll.getChildCount() == 1) {
            LayoutInflater.from(mActivity).inflate(R.layout.action_mode_close_text, ll, true);
        }
        if (ll.getChildCount() > 1 && ll.getChildAt(1) != null) {
            TextView tv = (TextView) ll.getChildAt(1);
            tv.setText(mActionButtonTextResource);
            TypedValue value = new TypedValue();
            mActivity.getTheme().resolveAttribute(R.attr.actionMenuTextColor, value, true);
            tv.setTextColor(value.data);
        }
        if (mDoneButtonOnClickListener != null) {
            ll.setOnClickListener(mDoneButtonOnClickListener);
        }
    }

    public void finishActionMode() {
        mActionMode.finish();
    }

    public void setTitle(String title) {
        mSelectionMenu.setTitle(title);
    }

    public void setActionModeListener(ActionModeListener listener) {
        mListener = listener;
    }

    @Override
    public boolean onMenuItemClick(com.actionbarsherlock.view.MenuItem item) {
        if (item.getItemId() == R.id.menu_select_all) {
            updateSupportedOperation();
            mSelectionManager.selectAll();
        } else if (item.getItemId() == R.id.menu_select_none) {
            updateSupportedOperation();
            mSelectionManager.selectNone();
        }
        return true;
    }

    public void updateSelectionMenu() {
        // update title
        int count = mSelectionManager.getSelectedCount();
        String format = CommonUtils.getQuantityStringResource(
                R.plurals.number_of_items_selected, count);
        setTitle(String.format(format, count));
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        mSelectionManager.leaveSelectionMode();
    }

    public void updateSupportedOperation() {
        updateSelectionMenu();
    }

    public void resume() {
        if (mSelectionManager.inSelectionMode()) {
            updateSupportedOperation();
        } else {
            if (mSelectionManager.getSelectedCount() > 0) {
                mSelectionManager.enterSelectionMode();
            }
        }
    }

    @Override
    public boolean onActionItemClicked(ActionMode actionMode,
            com.actionbarsherlock.view.MenuItem item) {
        if (mListener != null) {
            boolean result = mListener.onActionItemClicked(item);
            if (result) {
                mSelectionManager.leaveSelectionMode();
                return result;
            }
        }
        return true;
    }

    public static interface ActionModeListener {
        public boolean onActionItemClicked(com.actionbarsherlock.view.MenuItem item);
    }
}