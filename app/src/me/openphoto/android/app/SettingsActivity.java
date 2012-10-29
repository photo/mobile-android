
package me.openphoto.android.app;

import android.os.Bundle;

import com.WazaBe.HoloEverywhere.sherlock.SPreferenceActivity;

/**
 * The settings screen
 * 
 * @author pas
 * @author Patrick Boos
 * @author Eugene Popovich
 */
public class SettingsActivity extends SPreferenceActivity
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
