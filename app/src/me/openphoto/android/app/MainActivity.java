/**
 * The Main screen of OpenPhoto
 */

package me.openphoto.android.app;

import me.openphoto.android.app.service.UploaderService;
import me.openphoto.android.app.ui.widget.ActionBar;
import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TabHost.TabSpec;

/**
 * The Main screen of OpenPhoto
 * 
 * @author pas, pboos
 */
public class MainActivity extends TabActivity implements OnClickListener {
    public static final String TAG = MainActivity.class.getSimpleName();

    private View tagButton;
    private View uploadButton;
    private View galleryButton;

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

        // To make sure the service is initialized
        startService(new Intent(this, UploaderService.class));

        setUpTabs();
    }

    private void setUpTabs() {
        Resources res = getResources();

        TabSpec tabSpec = getTabHost().newTabSpec("home")
                .setIndicator(newTabIndicator(R.drawable.tab_home))
                .setContent(new Intent(this, HomeActivity.class));
        getTabHost().addTab(tabSpec);

        tabSpec = getTabHost().newTabSpec("gallery")
                .setIndicator(newTabIndicator(R.drawable.tab_gallery))
                .setContent(new Intent(this, GalleryActivity.class));
        getTabHost().addTab(tabSpec);

        tabSpec = getTabHost().newTabSpec("tags")
                .setIndicator(newTabIndicator(R.drawable.tab_tags))
                .setContent(new Intent(this, TagsActivity.class));
        getTabHost().addTab(tabSpec);
    }

    private View newTabIndicator(int drawableResId) {
        View view = getLayoutInflater().inflate(R.layout.tab, null);
        ((ImageView) view.findViewById(R.id.image)).setImageResource(drawableResId);
        return view;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // uploadButton.setEnabled(Preferences.isLoggedIn(this));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.settings:
                Intent i = new Intent(this, SettingsActivity.class);
                startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Handle clicks on the navigation buttons
     */
    @Override
    public void onClick(View v) {
        Class<?> nextScreen = null;
        if (v.equals(tagButton)) {
            nextScreen = TagsActivity.class;
        } else if (v.equals(uploadButton)) {
            nextScreen = UploadActivity.class;
        } else if (v.equals(galleryButton)) {
            nextScreen = GalleryActivity.class;
        }

        if (nextScreen != null) {
            // Go to the next screen, but keep this Activity in the stack
            Intent i = new Intent(this, nextScreen);
            startActivity(i);
        }
    }
}
