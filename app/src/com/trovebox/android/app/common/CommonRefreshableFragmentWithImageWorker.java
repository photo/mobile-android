
package com.trovebox.android.app.common;

import org.holoeverywhere.LayoutInflater;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.trovebox.android.app.R;
import com.trovebox.android.app.util.CommonUtils;
import com.trovebox.android.app.util.GuiUtils;
import com.trovebox.android.app.util.TrackerUtils;

/**
 * Abstract fragment with the refresh menu available
 * 
 * @author Eugene Popovich
 */
public abstract class CommonRefreshableFragmentWithImageWorker extends
        CommonFragmentWithImageWorker implements
        Refreshable {
    protected boolean refreshOnPageActivated = false;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        refreshOnPageActivated = false;
        return super.onCreateView(inflater, container, savedInstanceState);
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

    @Override
    public void pageActivated() {
        super.pageActivated();
        if (!isVisible())
        {
            return;
        }
        // if refresh is scheduled
        if (refreshOnPageActivated)
        {
            refreshOnPageActivated = false;
            refresh();
        }
    }

    /**
     * Refresh immediately or schedule depend on isActivePage field. Also
     * refresh may be skipped if isVisible is false
     */
    protected void refreshImmediatelyOrScheduleIfNecessary()
    {
        if (!isVisible())
        {
            return;
        }
        if (isActivePage)
        {
            refresh();
        } else
        {
            refreshOnPageActivated = true;
        }
    }

}
