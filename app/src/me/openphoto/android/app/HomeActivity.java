/**
 * The search screen
 */

package me.openphoto.android.app;

import me.openphoto.android.app.ui.widget.ActionBar;
import android.app.Activity;
import android.os.Bundle;

/**
 * The home activity
 * 
 * @author Patrick Boos
 */
public class HomeActivity extends Activity {
    public static final String TAG = HomeActivity.class.getSimpleName();

    private ActionBar mActionBar;

    /**
     * Called when Home Activity is first loaded
     * 
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        mActionBar = (ActionBar) getParent().findViewById(R.id.actionbar);
    }

}
