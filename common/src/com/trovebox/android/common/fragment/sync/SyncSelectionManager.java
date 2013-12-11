package com.trovebox.android.common.fragment.sync;

public abstract class SyncSelectionManager {

    public static final int ENTER_SELECTION_MODE = 1;
    public static final int LEAVE_SELECTION_MODE = 2;
    public static final int SELECT_ALL_MODE = 3;

    private boolean mInSelectionMode;
    private SyncSelectionListener mListener;

    abstract int getSelectedCount();

    abstract void selectAll();

    abstract void selectNone();

    public void setSelectionListener(SyncSelectionListener listener) {
        mListener = listener;
    }

    public boolean inSelectionMode() {
        return mInSelectionMode;
    }

    public void enterSelectionMode() {
        if (mInSelectionMode)
            return;

        mInSelectionMode = true;
        if (mListener != null)
            mListener.onSelectionModeChange(ENTER_SELECTION_MODE);
    }

    public void leaveSelectionMode() {
        if (!mInSelectionMode)
            return;

        mInSelectionMode = false;
        if (mListener != null)
            mListener.onSelectionModeChange(LEAVE_SELECTION_MODE);
    }

}