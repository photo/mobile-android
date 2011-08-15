
package me.openphoto.android.test;

import me.openphoto.android.app.R;
import me.openphoto.android.app.SettingsActivity;
import android.content.SharedPreferences;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.test.ActivityInstrumentationTestCase2;

public class SettingsActivityTest extends ActivityInstrumentationTestCase2<SettingsActivity> {

    private SettingsActivity mActivity;
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

        mPreferences = PreferenceManager.getDefaultSharedPreferences(getInstrumentation()
                .getTargetContext());
        mPreferences.edit().clear().commit();

        mActivity = this.getActivity();
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
        boolean isAutoUploadOnDefault = mActivity.getResources().getBoolean(
                R.bool.setting_autoupload_on_default);
        assertEquals(isAutoUploadOnDefault, autouploadOnPreference.isChecked());

        EditTextPreference autouploadTagPreference = (EditTextPreference) getPreference(R.string.setting_autoupload_tag_key);
        assertEquals(isAutoUploadOnDefault, autouploadTagPreference.isEnabled());
        assertNotNull(autouploadTagPreference);
        assertEquals(mActivity.getString(R.string.setting_autoupload_tag_default).toString(),
                autouploadTagPreference.getText());
    }

    private Preference getPreference(int resId) {
        return mActivity.findPreference(mActivity.getString(resId));
    }
}
