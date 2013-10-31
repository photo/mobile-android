package com.trovebox.android.app.net;

import com.trovebox.android.app.Preferences;
import com.trovebox.android.app.R;
import com.trovebox.android.app.TroveboxApplication;
import com.trovebox.android.app.model.ProfileInformation;
import com.trovebox.android.app.util.CommonUtils;
import com.trovebox.android.app.util.GuiUtils;
import com.trovebox.android.app.util.LoadingControl;
import com.trovebox.android.app.util.RunnableWithParameter;
import com.trovebox.android.app.util.SimpleAsyncTaskEx;

/**
 * Utils for {@link ProfileResponse}
 * 
 * @author Eugene Popovich
 */
public class ProfileResponseUtils {
    private static final String TAG = ProfileResponseUtils.class.getSimpleName();

    /**
     * Run runnable in the context of the ProfileInformation asynchronously
     * 
     * @param runnable to run with the retrieved {@link ProfileInformation}
     * @param loadingControl
     */
    public static void runWithProfileInformationAsync(
            RunnableWithParameter<ProfileInformation> runnable, LoadingControl loadingControl) {
        runWithProfileInformationAsync(false, runnable, null, loadingControl);
    }

    /**
     * Run runnable in the context of the ProfileInformation asynchronously. Run
     * runnableOnFailure in case ProfileInformation retrieval failed
     * 
     * @param includeViewer whether to include viewer profile information
     * @param runnable to run with the retrieved {@link ProfileInformation}
     * @param runnableOnFailure runnable to run in case
     *            {@link ProfileInformation} retrieval failed. Could be null
     * @param loadingControl
     */
    public static void runWithProfileInformationAsync(boolean includeViewer,
            final RunnableWithParameter<ProfileInformation> runnable, Runnable runnableOnFailure,
            LoadingControl loadingControl) {
        runWithProfileResponseAsync(includeViewer, new RunnableWithParameter<ProfileResponse>() {

            @Override
            public void run(ProfileResponse parameter) {
                runnable.run(parameter.getProfileInformation());
            }
        }, runnableOnFailure, loadingControl);
    }

    /**
     * Run runnable in the context of the ProfileResponse asynchronously
     * 
     * @param runnable to run with the retrieved {@link ProfileResponse}
     * @param loadingControl
     */
    public static void runWithProfileResponseAsync(
            RunnableWithParameter<ProfileResponse> runnable,
            LoadingControl loadingControl)
    {
        runWithProfileResponseAsync(false, runnable, null, loadingControl);
    }

    /**
     * Run runnable in the context of the ProfileResponse asynchronously. Run
     * runnableOnFailure in case ProfileResponse retrieval failed
     * 
     * @param includeViewer whether to include viewer profile information
     * @param runnable to run with the retrieved {@link ProfileResponse}
     * @param runnableOnFailure runnable to run in case {@link ProfileResponse}
     *            retrieval failed. Could be null
     * @param loadingControl
     */
    public static void runWithProfileResponseAsync(boolean includeViewer,
            RunnableWithParameter<ProfileResponse> runnable, Runnable runnableOnFailure,
            LoadingControl loadingControl) {
        if (CommonUtils.checkLoggedInAndOnline()) {
            new RetrieveProfileInformationTask(includeViewer, runnable, runnableOnFailure,
                    loadingControl).execute();
        }
    }

    /**
     * ProfileResponse retrieval task
     */
    private static class RetrieveProfileInformationTask extends SimpleAsyncTaskEx {

        RunnableWithParameter<ProfileResponse> mRunnable;
        Runnable mRunnableOnFailure;
        ProfileResponse mProfileResponse;
        boolean mIncludeViewer;

        /**
         * @param runnable to run after successful ProfileResponse retrieval
         * @param runnableOnFailure to run on failure
         * @param loadingControl
         */
        public RetrieveProfileInformationTask(boolean includeViewer,
                RunnableWithParameter<ProfileResponse> runnable, Runnable runnableOnFailure,
                LoadingControl loadingControl) {
            super(loadingControl);
            this.mIncludeViewer = includeViewer;
            this.mRunnable = runnable;
            this.mRunnableOnFailure = runnableOnFailure;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                ITroveboxApi troveboxApi = Preferences.getApi(TroveboxApplication.getContext());
                mProfileResponse = troveboxApi.getProfile(mIncludeViewer);
                return TroveboxResponseUtils.checkResponseValid(mProfileResponse);
            } catch (Exception ex) {
                GuiUtils.error(TAG, R.string.errorCouldNotRetrieveProfileInfo, ex);
            }
            return false;
        }

        @Override
        protected void onSuccessPostExecute() {
            mRunnable.run(mProfileResponse);
        }

        @Override
        protected void onFailedPostExecute() {
            super.onFailedPostExecute();
            if (mRunnableOnFailure != null) {
                mRunnableOnFailure.run();
            }
        }
    }
}
