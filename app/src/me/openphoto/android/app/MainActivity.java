package me.openphoto.android.app;

import me.openphoto.android.app.service.UploaderService;
import me.openphoto.android.app.ui.widget.ActionBar;
import me.openphoto.android.app.ui.widget.ActionBar.ActionClickListener;
import android.app.Activity;
import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

import com.bugsense.trace.BugSenseHandler;

/**
 * The Main screen of OpenPhoto
 * 
 * @author pas, pboos
 */
public class MainActivity extends TabActivity implements ActionClickListener {
	public static final String TAG = MainActivity.class.getSimpleName();

	private static final String BUG_SENSE_API_KEY = null;

	private ActionBar mActionBar;

	/**
	 * Called when Main Activity is first loaded
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mActionBar = (ActionBar) findViewById(R.id.actionbar);
		mActionBar.setOnActionClickListener(this);

		// To make sure the service is initialized
		startService(new Intent(this, UploaderService.class));

		setUpTabs();

		if (BUG_SENSE_API_KEY != null) {
			BugSenseHandler.setup(this, BUG_SENSE_API_KEY);
		}
	}

	private void setUpTabs() {
		TabSpec tabSpec = getTabHost()
				.newTabSpec("home")
				.setIndicator(
						newTabIndicator(R.drawable.tab_home, R.string.tab_home))
				.setContent(new Intent(this, HomeActivity.class));
		getTabHost().addTab(tabSpec);

		tabSpec = getTabHost()
				.newTabSpec("gallery")
				.setIndicator(
						newTabIndicator(R.drawable.tab_gallery,
								R.string.tab_gallery))
				.setContent(new Intent(this, GalleryActivity.class));
		getTabHost().addTab(tabSpec);

		tabSpec = getTabHost()
				.newTabSpec("tags")
				.setIndicator(
						newTabIndicator(R.drawable.tab_tags, R.string.tab_tags))
				.setContent(new Intent(this, TagsActivity.class));
		getTabHost().addTab(tabSpec);

		getTabHost().setCurrentTabByTag("gallery");
	}

	private View newTabIndicator(int drawableResId, int textResId) {
		View view = getLayoutInflater().inflate(R.layout.tab, null);
		((ImageView) view.findViewById(R.id.image))
				.setImageResource(drawableResId);
		((TextView) view.findViewById(R.id.text)).setText(textResId);
		return view;
	}

	private Activity getCurrentTabActivity() {
		String currentTab = getTabHost().getCurrentTabTag();
		Activity tabActivity = getLocalActivityManager()
				.getActivity(currentTab);
		return tabActivity;
	}

	@Override
	protected void onResume() {
		super.onResume();
		mActionBar.removeAction(R.id.action_add);
		if (Preferences.isLoggedIn(this)) {
			mActionBar.addAction(R.drawable.action_add, 0, R.id.action_add);
		}

		if (!Preferences.isLoggedIn(this)) {
			startActivity(new Intent(this, SetupActivity.class));
			finish();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		Activity tabActivity = getCurrentTabActivity();
		boolean showRefresh = tabActivity instanceof Refreshable;
		menu.findItem(R.id.menu_refresh).setVisible(showRefresh);
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.menu_settings:
			Intent i = new Intent(this, SettingsActivity.class);
			startActivity(i);
			return true;
		case R.id.menu_refresh:
			((Refreshable) getCurrentActivity()).refresh();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onActionClick(int id) {
		Intent i = new Intent(this, UploadActivity.class);
		startActivity(i);
	}
}
