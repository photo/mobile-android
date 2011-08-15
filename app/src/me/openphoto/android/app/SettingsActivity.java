/**
 * The settings screen
 */

package me.openphoto.android.app;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * The settings screen
 * 
 * @author pas
 * @author Patrick Boos
 */
public class SettingsActivity extends PreferenceActivity {
    /**
     * Called when Settings Activity is first loaded
     * 
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
    }

}
