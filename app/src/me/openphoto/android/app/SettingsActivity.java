
package me.openphoto.android.app;

import org.holoeverywhere.preference.PreferenceActivity;

import android.os.Bundle;

/**
 * The settings screen
 * 
 * @author pas
 * @author Patrick Boos
 * @author Eugene Popovich
 */
public class SettingsActivity extends PreferenceActivity
{
    /**
     * Called when Settings Activity is first loaded
     * 
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        initFragment();
    }

    private void initFragment()
    {
        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content,
                        new SettingsFragment()).commit();
    }
}
