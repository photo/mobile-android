
package me.openphoto.android.app.common;

import me.openphoto.android.app.R;
import android.os.Bundle;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

/**
 * Abstract fragment with the refresh menu available
 * 
 * @author Eugene Popovich
 */
public abstract class CommonRefreshableFragmentWithImageWorker extends
        CommonFrargmentWithImageWorker implements
        Refreshable {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.refreshable, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        reinitMenu(menu);
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.menu_refresh: {
                refresh();
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected void reinitMenu(Menu menu)
    {
        menu.findItem(R.id.menu_refresh).setVisible(isRefreshMenuVisible());
    }

    protected abstract boolean isRefreshMenuVisible();

}
