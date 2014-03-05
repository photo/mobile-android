package com.trovebox.android.app;

import com.trovebox.android.common.util.CommonUtils;
import com.trovebox.android.common.util.GuiUtils;
import com.trovebox.android.common.util.TrackerUtils;

public class SyncImageSelectionFragment extends
        com.trovebox.android.common.fragment.sync.SyncImageSelectionFragment {

    NextStepFlow nextStepFlow;

    public SyncImageSelectionFragment() {
        super(R.layout.action_mode_sync, true, R.string.sync_next_button, true);
    }


    @Override
    protected void actionButtonPressed() {
        super.actionButtonPressed();
        TrackerUtils.trackButtonClickEvent("nextBtn", SyncImageSelectionFragment.this);
        if (isDataLoaded()) {
            if (getSelectedCount() > 0) {
                int selectedCount = getSelectedCount();
                com.trovebox.android.common.net.account.AccountLimitUtils
                        .checkQuotaPerUploadAvailableAndRunAsync(new Runnable() {
                            @Override
                            public void run() {
                                CommonUtils.debug(TAG, "Upload limit check passed");
                                TrackerUtils.trackLimitEvent("sync_move_to_second_step", "success");
                                if (nextStepFlow != null) {
                                    nextStepFlow.activateNextStep();
                                }
                            }
                        }, new Runnable() {

                            @Override
                            public void run() {
                                CommonUtils.debug(TAG, "Upload limit check failed");
                                TrackerUtils.trackLimitEvent("sync_move_to_second_step", "fail");
                            }
                        }, selectedCount, loadingControl);
            } else {
                GuiUtils.alert(R.string.sync_please_pick_at_least_one_photo);
            }
        }
    }

    @Override
    public void pageDeactivated() {
        super.pageDeactivated();
        leaveSelectionMode();
    }

    public NextStepFlow getNextStepFlow() {
        return nextStepFlow;
    }

    public void setNextStepFlow(NextStepFlow nextStepFlow) {
        this.nextStepFlow = nextStepFlow;
    }

    static interface NextStepFlow {
        void activateNextStep();
    }
}
