
package com.trovebox.android.app;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.holoeverywhere.addon.AddonSlider;
import org.holoeverywhere.addon.AddonSlider.AddonSliderA;
import org.holoeverywhere.app.Activity;
import org.holoeverywhere.app.Activity.Addons;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.trovebox.android.app.FacebookFragment.FacebookLoadingControlAccessor;
import com.trovebox.android.app.NavigationHandlerFragment.TitleChangedHandler;
import com.trovebox.android.app.SyncFragment.SyncHandler;
import com.trovebox.android.app.TwitterFragment.TwitterLoadingControlAccessor;
import com.trovebox.android.app.bitmapfun.util.ImageCacheUtils;
import com.trovebox.android.app.facebook.FacebookProvider;
import com.trovebox.android.app.net.account.AccountLimitUtils2;
import com.trovebox.android.app.twitter.TwitterUtils;
import com.trovebox.android.common.activity.CommonActivity;
import com.trovebox.android.common.model.Album;
import com.trovebox.android.common.provider.UploadsUtils;
import com.trovebox.android.common.provider.UploadsUtils.UploadsClearedHandler;
import com.trovebox.android.common.util.BackKeyControl;
import com.trovebox.android.common.util.CommonUtils;
import com.trovebox.android.common.util.GalleryOpenControl;
import com.trovebox.android.common.util.GuiUtils;
import com.trovebox.android.common.util.LoadingControl;
import com.trovebox.android.common.util.ObjectAccessor;
import com.trovebox.android.common.util.SyncUtils;
import com.trovebox.android.common.util.SyncUtils.SyncStartedHandler;
import com.trovebox.android.common.util.TrackerUtils;

@Addons(Activity.ADDON_SLIDER)
public class MainActivity extends CommonActivity implements LoadingControl, GalleryOpenControl,
        SyncHandler, UploadsClearedHandler, TwitterLoadingControlAccessor,
        FacebookLoadingControlAccessor, SyncStartedHandler, GalleryFragment.StartNowHandler,
        TitleChangedHandler {
    private static final String NAVIGATION_HANDLER_FRAGMENT_TAG = "NavigationHandlerFragment";

    public static final String TAG = MainActivity.class.getSimpleName();

    public final static int AUTHORIZE_ACTIVITY_REQUEST_CODE = 0;
    public static final int REQUEST_ALBUMS = 2;

    private ActionBar mActionBar;
    private AtomicInteger loaders = new AtomicInteger(0);
    private AtomicBoolean cameraActionProcessing = new AtomicBoolean(false);

    boolean instanceSaved = false;

    final Handler handler = new Handler();

    static WeakReference<MainActivity> currentInstance;

    static ObjectAccessor<MainActivity> currentInstanceAccessor = new ObjectAccessor<MainActivity>() {
        private static final long serialVersionUID = 1L;

        @Override
        public MainActivity run() {
            return currentInstance == null ? null : currentInstance.get();
        }
    };

    /**
     * Called when Main Activity is first loaded
     * 
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        currentInstance = new WeakReference<MainActivity>(this);
        instanceSaved = false;
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        mActionBar = getSupportActionBar();
        mActionBar.setDisplayUseLogoEnabled(true);
        mActionBar.setDisplayShowTitleEnabled(true);
        if (loaders.get() == 0)
        {
            setSupportProgressBarIndeterminateVisibility(false);
        }

        setContentView(R.layout.content);

        addRegisteredReceiver(UploadsUtils
                .getAndRegisterOnUploadClearedActionBroadcastReceiver(TAG,
                        this, this));
        addRegisteredReceiver(SyncUtils.getAndRegisterOnSyncStartedActionBroadcastReceiver(
                TAG, this, this));
        addRegisteredReceiver(ImageCacheUtils.getAndRegisterOnDiskCacheClearedBroadcastReceiver(
                TAG,
                this));
    }

    @Override
    protected void onPostCreate(Bundle sSavedInstanceState) {
        super.onPostCreate(sSavedInstanceState);
        // //TODO hack which refreshes indeterminate progress state on
        // // orientation change
        // handler.postDelayed(new Runnable() {
        //
        // @Override
        // public void run() {
        // startLoading();
        // stopLoading();
        // }
        // }, 100);

        final AddonSliderA slider = addonSlider();
        mActionBar.setDisplayHomeAsUpEnabled(true);
        slider.setLeftViewWidth(computeMenuWidth());
        slider.setDragWithActionBar(true);
        navigationHandlerFragment = (NavigationHandlerFragment) getSupportFragmentManager()
                .findFragmentByTag(NAVIGATION_HANDLER_FRAGMENT_TAG);
        if (navigationHandlerFragment == null)
        {
            navigationHandlerFragment = (NavigationHandlerFragment) Fragment.instantiate(
                    MainActivity.this,
                    NavigationHandlerFragment.class.getName());
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.leftView, navigationHandlerFragment,
                            NAVIGATION_HANDLER_FRAGMENT_TAG).commit();
        }
        if (GuiUtils.checkLoggedIn(true))
        {
            AccountLimitUtils2.updateLimitInformationCacheAsync(this);
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        if (currentInstance != null)
        {
            if (currentInstance.get() == MainActivity.this
                    || currentInstance.get() == null)
            {
                CommonUtils.debug(TAG, "Nullify current instance");
                currentInstance = null;
            } else
            {
                CommonUtils.debug(TAG,
                        "Skipped nullify of current instance, such as it is not the same");
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        instanceSaved = true;
    }
  
    @Override
    protected void onResume()
    {
        super.onResume();
        instanceSaved = false;
        reinitMenu();

        if (!Preferences.isLoggedIn(this))
        {
            // startActivity(new Intent(this, SetupActivity.class));
            // startActivity(new Intent(this, AccountActivity.class));
            if(Preferences.isSkipIntro() == false)
                startActivity(new Intent(this, IntroActivity.class));
            else
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
            }
                break;
        }
    }

    public void reinitMenu()
    {
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
            case android.R.id.home:
                TrackerUtils.trackOptionsMenuClickEvent("menu_home", MainActivity.this);
                if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                    addonSlider().toggle();
                } else {
                    onBackPressed();
                }
                return true;
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
                    com.trovebox.android.common.net.account.AccountLimitUtils.checkQuotaPerOneUploadAvailableAndRunAsync(
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

    /**
     * Get the currently selected fragment
     * 
     * @return
     */
    public Fragment getCurrentFragment()
    {
        return navigationHandlerFragment.getCurrentFragment();
    }

    @Override
    public void openGallery(String tag, Album album)
    {
        Intent intent = getIntent();
        if (intent == null)
        {
            intent = new Intent();
            setIntent(intent);
        }
        intent.putExtra(GalleryFragment.EXTRA_TAG, tag);
        intent.putExtra(GalleryFragment.EXTRA_ALBUM, album);
        selectTab(navigationHandlerFragment.getGalleryIndex());
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

    private void showLoading(final boolean show)
    {
        handler.post(new Runnable() {

            @Override
            public void run() {
                setSupportProgressBarIndeterminateVisibility(show);
            }
        });
    }

    @Override
    public void syncStarted()
    {
        CommonUtils.debug(TAG, "Sync started");
        if (navigationHandlerFragment.getSelectedNavigationIndex() == navigationHandlerFragment
                .getSyncIndex())
        {
            if (!instanceSaved)
            {
                selectTab(navigationHandlerFragment.getGalleryIndex());
            }
        }
    }

    @Override
    public void uploadsCleared()
    {
        SyncFragment fragment = navigationHandlerFragment.getFragment(navigationHandlerFragment
                .getSyncIndex());
        if (fragment != null)
        {
            fragment.uploadsCleared();
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
    public boolean dispatchKeyEvent(android.view.KeyEvent event) {
        final int keyCode = event.getKeyCode();

        boolean proceed = true;
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Fragment fragment = getCurrentFragment();
            if (fragment != null && fragment instanceof BackKeyControl) {
                proceed &= !((BackKeyControl) fragment).isBackKeyOverrode();
            }
        }
        if (proceed) {
            return super.dispatchKeyEvent(event);
        }
        return proceed;
    }

    @Override
    public void syncStarted(List<String> processedFileNames) {
        CommonUtils.debug(TAG, "Sync started call");
        SyncFragment syncFragment = navigationHandlerFragment.getSyncFragment();
        if (syncFragment != null)
        {
            syncFragment.syncStarted(processedFileNames);
        }
    }

    @Override
    public void startNow() {
        CommonUtils.debug(TAG, "Start now");
        if (navigationHandlerFragment.getSelectedNavigationIndex() != navigationHandlerFragment
                .getSyncIndex())
        {
            if (!instanceSaved)
            {
                selectTab(navigationHandlerFragment.getSyncIndex());
            }
        }
    }

    private NavigationHandlerFragment navigationHandlerFragment;

    /**
     * Get the slider addon
     * 
     * @return
     */
    public AddonSliderA addonSlider() {
        return addon(AddonSlider.class);
    }

    private int computeMenuWidth() {
        return (int) getResources().getDimensionPixelSize(R.dimen.slider_menu_width);
    }

    /**
     * Set the action bar title
     * 
     * @param title
     */
    public void setActionBarTitle(String title)
    {
        mActionBar.setTitle(title);
    }

    public void selectTab(int index)
    {
        navigationHandlerFragment.selectTab(index);
    }

    @Override
    public void titleChanged() {
        navigationHandlerFragment.refreshActionBarTitle();
    }
}
