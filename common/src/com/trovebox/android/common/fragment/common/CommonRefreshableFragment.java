
package com.trovebox.android.common.fragment.common;

import android.os.Bundle;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.trovebox.android.common.R;
import com.trovebox.android.common.util.CommonUtils;
import com.trovebox.android.common.util.GuiUtils;
import com.trovebox.android.common.util.TrackerUtils;

/**
 * Abstract fragment with the refresh menu available
 * 
 * @author Eugene Popovich
 */
public abstract class CommonRefreshableFragment extends
        CommonFragment implements
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
        if (item.getItemId() == R.id.menu_refresh) {
            refresh();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    protected void reinitMenu(Menu menu)
    {
        try
        {
            MenuItem refreshItem = menu.findItem(R.id.menu_refresh);
            refreshItem.setVisible(isRefreshMenuVisible());
        } catch (Exception ex)
        {
            GuiUtils.noAlertError(TAG, ex);
            try
            {
                TrackerUtils.trackErrorEvent("#409 situation",
                        CommonUtils.format(
                                "isAdded: %1$b; isDetached: %2$b; " +
                                        "isHidden: %3$b; isRemoving: %4$b; " +
                                        "isVisible: %1$b",
                                isAdded(),
                                isDetached(),
                                isHidden(),
                                isRemoving(),
                                isVisible()
                                )
                        );
            } catch (Exception ex2)
            {
                GuiUtils.noAlertError(TAG, ex2);
                TrackerUtils.trackErrorEvent("#409 situation",
                        "additinal details error");
            }
        }
    }

    protected abstract boolean isRefreshMenuVisible();

}
