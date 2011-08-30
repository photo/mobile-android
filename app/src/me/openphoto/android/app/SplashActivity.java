/**
 * The splash/loading screen before you get to the Main screen
 */

package me.openphoto.android.app;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

/**
 * The splash/loading screen before you get to the Main screen
 * 
 * @author pas
 */
public class SplashActivity extends Activity {
    private InitialLoad loadTask;

    /**
     * Called when Splash Activity is first loaded
     * 
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);

        loadTask = new InitialLoad();
        loadTask.execute((Void) null);
    }

    @Override
    protected void onPause() {
        loadTask.cancel(true);
        super.onPause();
    }

    /**
     * Async task to do whatever loading will be required before going to the
     * Main screen
     */
    private class InitialLoad extends AsyncTask<Void, Void, Void> {
        /**
         * @see android.os.AsyncTask#doInBackground(Params[])
         */
        @Override
        protected Void doInBackground(Void... params) {
            // TODO Remove this fake work and replace with real work
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return null;
        }

        /**
         * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
         */
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            if (!isCancelled()) {
                // Go to Main screen
                Intent i = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(i);
                finish();
            }
        }
    }
}
