package me.openphoto.android.app;

import me.openphoto.android.app.SyncFragment.SyncHandler;
import me.openphoto.android.app.facebook.FacebookProvider;
import me.openphoto.android.app.provider.UploadsUtils;
import me.openphoto.android.app.provider.UploadsUtils.UploadsClearedHandler;
import me.openphoto.android.app.service.UploaderService;
import me.openphoto.android.app.twitter.TwitterUtils;
import me.openphoto.android.app.util.CommonUtils;
import me.openphoto.android.app.util.GalleryOpenControl;
import me.openphoto.android.app.util.LoadingControl;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;

import com.WazaBe.HoloEverywhere.sherlock.SActivity;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;

public class MainActivity extends SActivity
		implements LoadingControl, GalleryOpenControl, SyncHandler,
		UploadsClearedHandler
{
	private static final int HOME_INDEX = 0;
	private static final int SYNC_INDEX = 4;
	private static final String HOME_TAG = "home";
	private static final String SYNC_TAG = "sync";
	public static final String TAG = MainActivity.class.getSimpleName();
	public static final String ACTIVE_TAB = "ActiveTab";
	public final static int AUTHORIZE_ACTIVITY_RESULT_CODE = 0;

	private ActionBar mActionBar;
	private Menu mMenu;
	private int mLoaders = 0;

	private BroadcastReceiver uploadsClearedReceiver;

	/**
	 * Called when Main Activity is first loaded
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		mActionBar = getSupportActionBar();
		mActionBar.setDisplayUseLogoEnabled(true);
		mActionBar.setDisplayShowTitleEnabled(false);
		// To make sure the service is initialized
		startService(new Intent(this, UploaderService.class));

		// This has to be called before setContentView and you must use the
		// class in com.actionbarsherlock.view and NOT android.view

		setUpTabs(savedInstanceState == null ? 1 : savedInstanceState.getInt(
				ACTIVE_TAB, 1));
		uploadsClearedReceiver = UploadsUtils
				.getAndRegisterOnUploadClearedActionBroadcastReceiver(TAG,
						this, this);
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		unregisterReceiver(uploadsClearedReceiver);
	}
	private void setUpTabs(int activeTab)
	{
		addTab(R.drawable.tab_home_2states,
				R.string.tab_home,
				new TabListener<HomeFragment>(HOME_TAG, HomeFragment.class,
						null));
		addTab(R.drawable.tab_gallery_2states,
				R.string.tab_gallery,
				new TabListener<GalleryFragment>("gallery",
						GalleryFragment.class, null));
		addTab(View.NO_ID,
				R.string.tab_albums,
				new TabListener<AlbumsFragment>("albums",
						AlbumsFragment.class, null));
		addTab(R.drawable.tab_tags_2states,
				R.string.tab_tags,
				new TabListener<TagsFragment>("tags",
						TagsFragment.class, null));
		addTab(View.NO_ID,
				R.string.tab_sync,
				new TabListener<SyncFragment>(SYNC_TAG,
						SyncFragment.class, null));
		mActionBar.selectTab(mActionBar.getTabAt(activeTab));
	}

	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
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
		mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
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
			TwitterUtils.verifyOAuthResponse(this, uri,
					null);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		switch (requestCode)
		{
		/*
		 * if this is the activity result from authorization flow, do a call
		 * back to authorizeCallback Source Tag: login_tag
		 */
			case AUTHORIZE_ACTIVITY_RESULT_CODE:
			{
				FacebookProvider.getFacebook().authorizeCallback(requestCode,
						resultCode,
						data);
				break;
			}
		}
	}
	public void reinitMenu()
	{
		if (mMenu != null)
		{
			mMenu.findItem(R.id.menu_camera).setVisible(
					Preferences.isLoggedIn(this));
			Fragment currentFragment = getCurrentFragment();
			showRefreshAction(currentFragment != null
					&& currentFragment instanceof Refreshable
					&& mLoaders == 0);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		mMenu = menu;
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.main, menu);
		reinitMenu();
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle item selection
		switch (item.getItemId())
		{
			case R.id.menu_settings:
			{
				Intent i = new Intent(this, SettingsActivity.class);
				startActivity(i);
				return true;
			}
			case R.id.menu_refresh:
				((Refreshable) getCurrentFragment()).refresh();
				return true;
			case R.id.menu_camera:
			{
				Intent i = new Intent(this, UploadActivity.class);
				startActivity(i);
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
		mActionBar.selectTab(mActionBar.getTabAt(1));
	}

	@Override
	public void startLoading()
	{
		if (mLoaders++ == 0)
		{
			showLoading(true);
			showRefreshAction(false);
		}
	}

	@Override
	public void stopLoading()
	{
		if (--mLoaders == 0)
		{
			showLoading(false);
			reinitMenu();
		}
	}

	private void showRefreshAction(boolean show)
	{
		if (mMenu != null)
		{
			mMenu.findItem(R.id.menu_refresh).setVisible(show);
		}
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

		public TabListener(String tag, Class<T> clz,
				Bundle args)
		{
			mTag = tag;
			mClass = clz;
			mArgs = args;
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
		}

		@Override
		public void onTabSelected(Tab tab, FragmentTransaction ft)
		{
			CommonUtils.debug(TAG, "onTabSelected");
			if (mFragment == null)
			{
				mFragment = Fragment.instantiate(MainActivity.this,
						mClass.getName(),
						mArgs);
				ft.add(android.R.id.content, mFragment, mTag);
			} else
			{
				ft.attach(mFragment);
			}

		}

		@Override
		public void onTabUnselected(Tab tab, FragmentTransaction ft)
		{
			CommonUtils.debug(TAG, "onTabUnselected");
			if (mFragment != null)
			{
				ft.detach(mFragment);
			}

		}

		@Override
		public void onTabReselected(Tab tab, FragmentTransaction ft)
		{
			CommonUtils.debug(TAG, "onTabReselected");
		}
	}

	@Override
	public void syncStarted()
	{
		if (mActionBar.getSelectedTab() == mActionBar.getTabAt(SYNC_INDEX))
		{
			mActionBar.selectTab(mActionBar.getTabAt(HOME_INDEX));
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

}
