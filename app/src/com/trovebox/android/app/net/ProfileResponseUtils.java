package com.trovebox.android.app.net;

import com.trovebox.android.app.Preferences;
import com.trovebox.android.app.R;
import com.trovebox.android.app.TroveboxApplication;
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
     * Run runnable in the context of the ProfileResponse asynchronously
     * 
     * @param runnable to run with the retrieved {@link ProfileResponse}
     * @param loadingControl
     */
    public static void runWithProfileResponseAsync(
            RunnableWithParameter<ProfileResponse> runnable,
            LoadingControl loadingControl)
    {
        runWithProfileResponseAsync(runnable, null, loadingControl);
    }

    /**
     * Run runnable in the context of the ProfileResponse asynchronously. Run
     * runnableOnFailure in case ProfileResponse retrieval failed
     * 
     * @param runnable to run with the retrieved {@link ProfileResponse}
     * @param runnableOnFailure runnable to run in case {@link ProfileResponse}
     *            retrieval failed. Could be null
     * @param loadingControl
     */
    public static void runWithProfileResponseAsync(
            RunnableWithParameter<ProfileResponse> runnable,
            Runnable runnableOnFailure,
            LoadingControl loadingControl)
    {
        if (CommonUtils.checkLoggedInAndOnline())
        {
            new RetrieveProfileInformationTask(runnable, runnableOnFailure, loadingControl)
                    .execute();
        }
    }

    /**
     * ProfileResponse retrieval task
     */
    private static class RetrieveProfileInformationTask extends SimpleAsyncTaskEx {

        RunnableWithParameter<ProfileResponse> runnable;
        Runnable runnableOnFailure;
        ProfileResponse profileResponse;

        /**
         * @param runnable to run after successful ProfileResponse retrieval
         * @param runnableOnFailure to run on failure
         * @param loadingControl
         */
        public RetrieveProfileInformationTask(
                RunnableWithParameter<ProfileResponse> runnable,
                Runnable runnableOnFailure,
                LoadingControl loadingControl) {
            super(loadingControl);
            this.runnable = runnable;
            this.runnableOnFailure = runnableOnFailure;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try
            {
                ITroveboxApi troveboxApi = Preferences.getApi(TroveboxApplication.getContext());
                profileResponse = troveboxApi.getProfile();
                return TroveboxResponseUtils.checkResponseValid(profileResponse);
            } catch (Exception ex)
            {
                GuiUtils.error(TAG,
                        R.string.errorCouldNotRetrieveProfileInfo,
                        ex);
            }
            return false;
        }

        @Override
        protected void onSuccessPostExecute()
        {
            runnable.run(profileResponse);
        }

        @Override
        protected void onFailedPostExecute() {
            super.onFailedPostExecute();
            if (runnableOnFailure != null)
            {
                runnableOnFailure.run();
            }
        }
    }
}
