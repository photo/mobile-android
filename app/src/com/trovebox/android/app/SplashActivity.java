
package com.trovebox.android.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.trovebox.android.app.service.UploaderService;
import com.trovebox.android.common.service.UploaderServiceUtils;
import com.trovebox.android.common.util.CommonUtils;
import com.trovebox.android.common.util.TrackerUtils;
import com.trovebox.android.common.util.concurrent.AsyncTaskEx;

/**
 * The splash/loading screen before you get to the Main screen
 * 
 * @author pas
 */
public class SplashActivity extends Activity {
    public static final String TAG = SplashActivity.class.getSimpleName();
    private InitialLoad loadTask;

    /**
     * Called when Splash Activity is first loaded
     * 
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        loadTask = new InitialLoad();
        loadTask.execute((Void) null);
    }

    @Override
    protected void onStop() {
        loadTask.cancel(true);
        super.onPause();
    }

    /**
     * Async task to do whatever loading will be required before going to the
     * Main screen
     */
    private class InitialLoad extends AsyncTaskEx<Void, Void, Void> {
        /**
         * @see com.trovebox.android.common.util.concurrent.AsyncTaskEx#doInBackground(Params[])
         */
        @Override
        protected Void doInBackground(Void... params) {
            if (!UploaderServiceUtils.isServiceRunning(UploaderService.class))
            {
                TrackerUtils.trackBackgroundEvent(
                        "uploader_service_start",
                        "starting_not_running_service_from_main");
                CommonUtils.debug(TAG, "Uploader service is not run. Starting...");
                // To make sure the service is initialized
                startService(new Intent(SplashActivity.this, UploaderService.class));
            }
            return null;
        }

        /**
         * @see com.trovebox.android.common.util.concurrent.AsyncTaskEx#onPostExecute(java.lang.Object)
         */
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            if (!isCancelled()) {
                // Go to Main screen
                Intent i = Preferences.isLoggedIn() ?
                        new Intent(SplashActivity.this, MainActivity.class) :
                        new Intent(SplashActivity.this, IntroActivity.class);
                startActivity(i);
                finish();
            }
        }
    }
}
