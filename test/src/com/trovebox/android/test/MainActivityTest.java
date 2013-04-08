
package com.trovebox.android.test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import android.app.Instrumentation;
import android.support.v4.app.Fragment;
import android.test.ActivityInstrumentationTestCase2;

import com.actionbarsherlock.app.ActionBar;
import com.trovebox.android.app.AlbumsFragment;
import com.trovebox.android.app.GalleryFragment;
import com.trovebox.android.app.HomeFragment;
import com.trovebox.android.app.MainActivity;
import com.trovebox.android.app.NavigationHandlerFragment;
import com.trovebox.android.app.SyncFragment;
import com.trovebox.android.app.TagsFragment;

public class MainActivityTest extends
        ActivityInstrumentationTestCase2<MainActivity>
{

    private MainActivity activity;
    private Fragment fragment;
    Instrumentation instrumentation;

    public MainActivityTest()
    {
        super(MainActivity.class);
    }

    /**
     * @see android.test.ActivityInstrumentationTestCase2#setUp()
     */
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        activity = this.getActivity();
        instrumentation = getInstrumentation();
    }

    public void testPreconditions() throws InterruptedException
    {
        fragment = activity.getCurrentFragment();
        assertNotNull(fragment);
        assertTrue(fragment instanceof HomeFragment);
    }

    public void testTabSelection() throws InterruptedException
    {

        testSingleTabSelection(NavigationHandlerFragment.GALLERY_INDEX,
                GalleryFragment.class);
        testSingleTabSelection(NavigationHandlerFragment.SYNC_INDEX,
                SyncFragment.class);
        testSingleTabSelection(NavigationHandlerFragment.ALBUMS_INDEX,
                AlbumsFragment.class);
        testSingleTabSelection(NavigationHandlerFragment.TAGS_INDEX,
                TagsFragment.class);
        testSingleTabSelection(NavigationHandlerFragment.HOME_INDEX,
                HomeFragment.class);

    }

    private void testSingleTabSelection(
            final int index, Class<?> fragmentClass)
            throws InterruptedException
    {
        final ActionBar actionBar = activity.getSupportActionBar();
        assertNotNull(actionBar);
        Fragment fragment;
        activity.runOnUiThread(new Runnable()
        {

            @Override
            public void run()
            {
                activity.selectTab(index);
            }
        });
        instrumentation.waitForIdleSync();
        CountDownLatch signal = new CountDownLatch(1);
        signal.await(2, TimeUnit.SECONDS);

        fragment = getActivity().getCurrentFragment();
        assertNotNull(fragment);
        assertTrue(fragmentClass.isInstance(fragment));
    }
}
