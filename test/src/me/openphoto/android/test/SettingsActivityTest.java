
package me.openphoto.android.test;

import me.openphoto.android.app.Preferences;
import me.openphoto.android.app.R;
import me.openphoto.android.app.SettingsActivity;
import me.openphoto.android.app.SettingsFragment;

import org.holoeverywhere.preference.CheckBoxPreference;
import org.holoeverywhere.preference.EditTextPreference;
import org.holoeverywhere.preference.Preference;
import org.holoeverywhere.preference.PreferenceActivity;

import android.content.SharedPreferences;
import android.test.ActivityInstrumentationTestCase2;

public class SettingsActivityTest extends
        ActivityInstrumentationTestCase2<SettingsActivity>
{

    private SettingsActivity mActivity;
    private SettingsFragment fragment;
    private SharedPreferences mPreferences;

    public SettingsActivityTest() {
        super("me.openphoto.android.app", SettingsActivity.class);
    }

    /**
     * @see android.test.ActivityInstrumentationTestCase2#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();

        // mPreferences.edit().clear().commit();

        mActivity = this.getActivity();
        mPreferences = Preferences
                .getDefaultSharedPreferences(getInstrumentation()
                        .getTargetContext());
        fragment = (SettingsFragment) mActivity.getSupportFragmentManager()
                .findFragmentById(android.R.id.content);
    }

    public void testPreconditions() {
        assertTrue(mActivity instanceof PreferenceActivity);
    }

    /**
     * Tests if the settings for autoupload are present and if they have the
     * correct default values.
     */
    public void testAutoUploadSetting() {
        CheckBoxPreference autouploadOnPreference = (CheckBoxPreference) getPreference(R.string.setting_autoupload_on_key);
        assertNotNull(autouploadOnPreference);
        boolean isAutoUploadOn = mPreferences.getBoolean(
                mActivity.getString(R.string.setting_autoupload_on_key),
                mActivity.getResources().getBoolean(
                        R.bool.setting_autoupload_on_default));
        assertEquals(isAutoUploadOn, autouploadOnPreference.isChecked());

        EditTextPreference autouploadTagPreference = (EditTextPreference) getPreference(R.string.setting_autoupload_tag_key);
        assertEquals(isAutoUploadOn, autouploadTagPreference.isEnabled());
        assertNotNull(autouploadTagPreference);
        String autoUploadTag = mPreferences.getString(
                mActivity.getString(R.string.setting_autoupload_tag_key),
                mActivity.getString(R.string.setting_autoupload_tag_default));
        assertEquals(autoUploadTag,
                autouploadTagPreference.getText());
    }

    public void testServerSetting() {
        Preference serverPreference = getPreference(R.string.setting_account_server_key);
        String server = mPreferences.getString(
                mActivity.getString(R.string.setting_account_server_key),
                mActivity.getString(R.string.setting_account_server_default));
        assertEquals(server,
                serverPreference.getSummary());
    }

    private Preference getPreference(int resId) {
        return fragment.findPreference(mActivity.getString(resId));
    }
}
