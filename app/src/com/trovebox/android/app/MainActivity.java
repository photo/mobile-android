
package com.trovebox.android.app;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.trovebox.android.app.FacebookFragment.FacebookLoadingControlAccessor;
import com.trovebox.android.app.SyncFragment.SyncHandler;
import com.trovebox.android.app.TwitterFragment.TwitterLoadingControlAccessor;
import com.trovebox.android.app.common.CommonActivity;
import com.trovebox.android.app.common.Refreshable;
import com.trovebox.android.app.facebook.FacebookProvider;
import com.trovebox.android.app.model.Photo;
import com.trovebox.android.app.model.utils.PhotoUtils;
import com.trovebox.android.app.model.utils.PhotoUtils.PhotoDeletedHandler;
import com.trovebox.android.app.model.utils.PhotoUtils.PhotoUpdatedHandler;
import com.trovebox.android.app.net.account.AccountLimitUtils;
import com.trovebox.android.app.provider.UploadsUtils;
import com.trovebox.android.app.provider.UploadsUtils.UploadsClearedHandler;
import com.trovebox.android.app.service.UploaderService;
import com.trovebox.android.app.service.UploaderServiceUtils;
import com.trovebox.android.app.service.UploaderServiceUtils.PhotoUploadedHandler;
import com.trovebox.android.app.twitter.TwitterUtils;
import com.trovebox.android.app.util.BackKeyControl;
import com.trovebox.android.app.util.CommonUtils;
import com.trovebox.android.app.util.GalleryOpenControl;
import com.trovebox.android.app.util.LoadingControl;
import com.trovebox.android.app.util.SyncUtils;
import com.trovebox.android.app.util.SyncUtils.SyncStartedHandler;
import com.trovebox.android.app.util.TrackerUtils;
import com.trovebox.android.app.util.Utils;

public class MainActivity extends CommonActivity
        implements LoadingControl, GalleryOpenControl, SyncHandler,
        UploadsClearedHandler, PhotoUploadedHandler, TwitterLoadingControlAccessor,
        FacebookLoadingControlAccessor, SyncStartedHandler,
        PhotoDeletedHandler, PhotoUpdatedHandler
{
    public static final int HOME_INDEX = 0;
    public static final int GALLERY_INDEX = 1;
    public static final int SYNC_INDEX = 2;
    public static final int ALBUMS_INDEX = 3;
    public static final int TAGS_INDEX = 4;
    public static final int ACCOUNT_INDEX = 4;
    private static final String HOME_TAG = "home";
    private static final String GALLERY_TAG = "gallery";
    private static final String SYNC_TAG = "sync";
    private static final String ACCOUNT_TAG = "account";
    public static final String TAG = MainActivity.class.getSimpleName();
    public static final String ACTIVE_TAB = "ActiveTab";
    public final static int AUTHORIZE_ACTIVITY_REQUEST_CODE = 0;

    private ActionBar mActionBar;
    private AtomicInteger loaders = new AtomicInteger(0);
    private AtomicBoolean cameraActionProcessing = new AtomicBoolean(false);

    private List<BroadcastReceiver> receivers = new ArrayList<BroadcastReceiver>();
    boolean instanceSaved = false;
    boolean actionbBarNavigationModeInitiated = false;

    /**
     * Called when Main Activity is first loaded
     * 
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        // we need to use different theme for embed tabs because of where
        // are no over way to use different tab text colors for embed and
        // split states
        if (Utils.isActionBarTabsEmbeded(TroveboxApplication.getContext()))
        {
            setTheme(R.style.Theme_Trovebox_Light_Stacked);
        }
        super.onCreate(savedInstanceState);
        instanceSaved = false;
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        mActionBar = getSupportActionBar();
        mActionBar.setDisplayUseLogoEnabled(true);
        mActionBar.setDisplayShowTitleEnabled(false);
        if (!UploaderServiceUtils.isServiceRunning())
        {
            TrackerUtils.trackBackgroundEvent(
                    "uploader_service_start",
                    "starting_not_running_service_from_main");
            CommonUtils.debug(TAG, "Uploader service is not run. Starting...");
            // To make sure the service is initialized
            startService(new Intent(this, UploaderService.class));
        }

        // This has to be called before setContentView and you must use the
        // class in com.actionbarsherlock.view and NOT android.view

        setUpTabs(savedInstanceState == null ? 0 : savedInstanceState.getInt(
                ACTIVE_TAB, 1), savedInstanceState);
        receivers.add(UploadsUtils
                .getAndRegisterOnUploadClearedActionBroadcastReceiver(TAG,
                        this, this));
        receivers.add(UploaderServiceUtils.getAndRegisterOnPhotoUploadedActionBroadcastReceiver(
                TAG, this, this));
        receivers.add(SyncUtils.getAndRegisterOnSyncStartedActionBroadcastReceiver(
                TAG, this, this));
        receivers.add(PhotoUtils.getAndRegisterOnPhotoDeletedActionBroadcastReceiver(
                TAG, this, this));
        receivers.add(PhotoUtils.getAndRegisterOnPhotoUpdatedActionBroadcastReceiver(
                TAG, this, this));
        if (CommonUtils.checkLoggedIn(true))
        {
            AccountLimitUtils.updateLimitInformationCache();
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        for (BroadcastReceiver br : receivers)
        {
            unregisterReceiver(br);
        }
    }

    private void setUpTabs(int activeTab, Bundle savedInstanceState)
    {
        addTab(R.drawable.tab_home_2states,
                R.string.tab_home,
                new TabListener<HomeFragment>(HOME_TAG, HomeFragment.class,
                        null));
        addTab(R.drawable.tab_gallery_2states,
                R.string.tab_gallery,
                new TabListener<GalleryFragment>(GALLERY_TAG,
                        GalleryFragment.class, null, false,
                        new Runnable() {
                            @Override
                            public void run() {
                                GalleryFragment gf = (GalleryFragment) getSupportFragmentManager()
                                        .findFragmentByTag(GALLERY_TAG);
                                if (gf != null)
                                {
                                    gf.cleanRefreshIfFiltered();
                                }
                            }
                        }));
        addTab(View.NO_ID,
                R.string.tab_sync,
                new TabListener<SyncFragment>(SYNC_TAG,
                        SyncFragment.class, null));
        addTab(View.NO_ID,
                R.string.tab_albums,
                new TabListener<AlbumsFragment>("albums",
                        AlbumsFragment.class, null));
        addTab(R.drawable.tab_tags_2states,
                R.string.tab_tags,
                new TabListener<TagsFragment>("tags",
                        TagsFragment.class, null));
        addTab(View.NO_ID,
                R.string.tab_account,
                new TabListener<AccountFragment>(ACCOUNT_TAG,
                        AccountFragment.class, null));

        mActionBar.selectTab(mActionBar.getTabAt(activeTab));
        // hack which refreshes indeterminate progress state on
        // orientation change
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {

            @Override
            public void run() {
                startLoading();
                stopLoading();
            }
        }, 100);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        instanceSaved = true;
        outState.putInt(ACTIVE_TAB, mActionBar.getSelectedNavigationIndex());
    }

    private <T extends Fragment> void addTab(int drawableResId,
            int textResId,
            TabListener<T> tabListener)
    {
        Tab tab = mActionBar
                .newTab()
                .setText(textResId)
                // .setIcon(drawableResId)
                .setTabListener(tabListener);
        mActionBar.addTab(tab);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if (!actionbBarNavigationModeInitiated)
        {
            mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
            actionbBarNavigationModeInitiated = true;
        }
        instanceSaved = false;
        reinitMenu();

        if (!Preferences.isLoggedIn(this))
        {
            // startActivity(new Intent(this, SetupActivity.class));
            startActivity(new Intent(this, AccountActivity.class));
            finish();
        }
    }

    @Override
    protected void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);
        if (intent != null && intent.getData() != null)
        {
            Uri uri = intent.getData();
            TwitterUtils.verifyOAuthResponse(this, this, uri,
                    TwitterUtils.getDefaultCallbackUrl(this),
                    null);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode)
        {
        /*
         * if this is the activity result from authorization flow, do a call
         * back to authorizeCallback Source Tag: login_tag
         */
            case AUTHORIZE_ACTIVITY_REQUEST_CODE: {
                FacebookProvider.getFacebook().authorizeCallback(requestCode,
                        resultCode,
                        data);
                break;
            }
        }
    }

    public void reinitMenu()
    {
        Handler handler = new Handler();
        handler.post(new Runnable() {

            @Override
            public void run() {
                invalidateOptionsMenu();
            }
        });
    }

    public void reinitMenu(Menu menu)
    {
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        reinitMenu(menu);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item)
    {
        // Handle item selection
        switch (item.getItemId())
        {
            case R.id.menu_settings: {
                TrackerUtils.trackOptionsMenuClickEvent("menu_settings", MainActivity.this);
                Intent i = new Intent(this, SettingsActivity.class);
                startActivity(i);
                return true;
            }
            case R.id.menu_camera: {
                TrackerUtils.trackOptionsMenuClickEvent("menu_camera", MainActivity.this);
                if (!cameraActionProcessing.getAndSet(true))
                {
                    AccountLimitUtils.checkQuotaPerOneUploadAvailableAndRunAsync(
                            new Runnable() {

                                @Override
                                public void run() {
                                    CommonUtils.debug(TAG, "Upload limit check passed");
                                    TrackerUtils.trackLimitEvent("upload_activity_open", "success");
                                    Intent i = new Intent(MainActivity.this, UploadActivity.class);
                                    startActivity(i);
                                    cameraActionProcessing.set(false);
                                }
                            },
                            new Runnable() {

                                @Override
                                public void run() {
                                    CommonUtils.debug(TAG, "Upload limit check failed");
                                    TrackerUtils.trackLimitEvent("upload_activity_open", "fail");
                                    cameraActionProcessing.set(false);
                                }
                            },
                            this);

                }
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    Fragment getCurrentFragment()
    {
        return getSupportFragmentManager().findFragmentById(
                android.R.id.content);
    }

    @Override
    public void openGallery(String tag, String album)
    {
        Intent intent = getIntent();
        if (intent == null)
        {
            intent = new Intent();
            setIntent(intent);
        }
        intent.putExtra(GalleryFragment.EXTRA_TAG, tag);
        intent.putExtra(GalleryFragment.EXTRA_ALBUM, album);
        mActionBar.selectTab(mActionBar.getTabAt(GALLERY_INDEX));
    }

    @Override
    public void startLoading()
    {
        if (loaders.getAndIncrement() == 0)
        {
            reinitMenu();
            showLoading(true);
        }
    }

    @Override
    public void stopLoading()
    {
        if (loaders.decrementAndGet() == 0)
        {
            showLoading(false);
            reinitMenu();
        }
    }

    @Override
    public boolean isLoading() {
        return loaders.get() > 0;
    }

    private void showLoading(boolean show)
    {
        setSupportProgressBarIndeterminateVisibility(show);
    }

    public class TabListener<T extends Fragment> implements
            ActionBar.TabListener
    {
        private final String mTag;
        private final Class<T> mClass;
        private final Bundle mArgs;
        private Fragment mFragment;
        private Runnable runOnReselect;

        public TabListener(String tag, Class<T> clz,
                Bundle args)
        {
            this(tag, clz, args, false, null);
        }

        public TabListener(String tag, Class<T> clz,
                Bundle args, boolean removeIfExists,
                Runnable runOnReselect)
        {
            mTag = tag;
            mClass = clz;
            mArgs = args;
            this.runOnReselect = runOnReselect;
            FragmentTransaction ft = getSupportFragmentManager()
                    .beginTransaction();

            // Check to see if we already have a fragment for this tab, probably
            // from a previously saved state. If so, deactivate it, because our
            // initial state is that a tab isn't shown.
            mFragment = getSupportFragmentManager()
                    .findFragmentByTag(mTag);
            if (mFragment != null && !mFragment.isDetached())
            {
                ft.detach(mFragment);
            }
            if (removeIfExists && mFragment != null)
            {
                ft.remove(mFragment);
            }
        }

        @Override
        public void onTabSelected(Tab tab, FragmentTransaction ft)
        {
            CommonUtils.debug(TAG, "onTabSelected");
            TrackerUtils.trackTabSelectedEvent(mTag, MainActivity.this);
            if (mFragment == null)
            {
                mFragment = Fragment.instantiate(MainActivity.this,
                        mClass.getName(),
                        mArgs);
                ft.replace(android.R.id.content, mFragment, mTag);
            } else
            {
                ft.attach(mFragment);
            }
            reinitMenu();
        }

        @Override
        public void onTabUnselected(Tab tab, FragmentTransaction ft)
        {
            CommonUtils.debug(TAG, "onTabUnselected");
            if (mFragment != null)
            {
                if (mFragment.getView() != null)
                {
                    View target = mFragment.getView().findFocus();

                    if (target != null)
                    {
                        InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        mgr.hideSoftInputFromWindow(target.getWindowToken(), 0);
                    }
                }
                ft.detach(mFragment);
            }

        }

        @Override
        public void onTabReselected(Tab tab, FragmentTransaction ft)
        {
            TrackerUtils.trackTabReselectedEvent(mTag, MainActivity.this);
            CommonUtils.debug(TAG, "onTabReselected");
            if (runOnReselect != null)
            {
                runOnReselect.run();
            }
        }
    }

    @Override
    public void syncStarted()
    {
        CommonUtils.debug(TAG, "Sync started");
        if (mActionBar.getSelectedNavigationIndex() == SYNC_INDEX)
        {
            if (!instanceSaved)
            {
                mActionBar.selectTab(mActionBar.getTabAt(HOME_INDEX));
            }
        }
    }

    @Override
    public void uploadsCleared()
    {
        SyncFragment fragment = (SyncFragment) getSupportFragmentManager()
                .findFragmentByTag(SYNC_TAG);
        if (fragment != null)
        {
            fragment.uploadsCleared();
        }
    }

    @Override
    public void photoUploaded() {
        switch (mActionBar.getSelectedNavigationIndex())
        {
            case HOME_INDEX:
            case GALLERY_INDEX:
                Fragment fragment = getCurrentFragment();
                if (fragment != null)
                {
                    ((Refreshable) fragment).refresh();
                }
                break;
        }
    }

    @Override
    public LoadingControl getTwitterLoadingControl() {
        return this;
    }

    @Override
    public LoadingControl getFacebookLoadingControl() {
        return this;
    }

    @Override
    public void onBackPressed() {
        Fragment fragment = getCurrentFragment();
        boolean proceed = true;
        if (fragment != null && fragment instanceof BackKeyControl)
        {
            proceed &= !((BackKeyControl) fragment).isBackKeyOverrode();
        }
        if (proceed)
        {
            super.onBackPressed();
        }
    }

    @Override
    public void syncStarted(List<String> processedFileNames) {
        CommonUtils.debug(TAG, "Sync started call");
        SyncFragment syncFragment = (SyncFragment) getSupportFragmentManager().findFragmentByTag(
                SYNC_TAG);
        if (syncFragment != null)
        {
            syncFragment.syncStarted(processedFileNames);
        }
    }

    @Override
    public void photoDeleted(Photo photo)
    {
        HomeFragment homeFragment = (HomeFragment) getSupportFragmentManager().findFragmentByTag(
                HOME_TAG);
        if (homeFragment != null)
        {
            homeFragment.photoDeleted(photo);
        }

        GalleryFragment galleryFragment = (GalleryFragment) getSupportFragmentManager()
                .findFragmentByTag(GALLERY_TAG);
        if (galleryFragment != null)
        {
            galleryFragment.photoDeleted(photo);
        }
    }

    @Override
    public void photoUpdated(Photo photo)
    {
        HomeFragment homeFragment = (HomeFragment) getSupportFragmentManager().findFragmentByTag(
                HOME_TAG);
        if (homeFragment != null)
        {
            homeFragment.photoUpdated(photo);
        }

        GalleryFragment galleryFragment = (GalleryFragment) getSupportFragmentManager()
                .findFragmentByTag(GALLERY_TAG);
        if (galleryFragment != null)
        {
            galleryFragment.photoUpdated(photo);
        }
    }
}
