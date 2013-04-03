
package com.trovebox.android.app;

import java.util.ArrayList;
import java.util.List;

import org.holoeverywhere.FontLoader;
import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.addon.AddonSlider.AddonSliderA;
import org.holoeverywhere.widget.LinearLayout;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.trovebox.android.app.common.lifecycle.ViewPagerHandler;
import com.trovebox.android.app.net.SystemVersionResponseUtils;
import com.trovebox.android.app.ui.adapter.FragmentPagerAdapter;
import com.trovebox.android.app.ui.widget.SliderNavigationItem;
import com.trovebox.android.app.util.CommonUtils;
import com.trovebox.android.app.util.LoadingControl;
import com.trovebox.android.app.util.TrackerUtils;

/**
 * The fragment which handles navigation in the MainActivity. Left slider and
 * viewpager is there
 * 
 * @author Eugene Popovich
 */
public class NavigationHandlerFragment extends org.holoeverywhere.app.Fragment {
    static final String TAG = NavigationHandlerFragment.class.getSimpleName();
    public static final int HOME_INDEX = 0;
    public static final int GALLERY_INDEX = 1;
    public static final int ALBUMS_INDEX = 2;
    public static final int TAGS_INDEX = 3;
    public static final int SYNC_INDEX = 4;
    public static final int ACCOUNT_INDEX = 5;

    public static interface OnMenuClickListener {
        public void onMenuClick(int position);
    }

    /**
     * The fragment wrapper. Contains necessary information to construct
     * fragment
     */
    private final class FragmentWrapper<T extends Fragment> implements OnClickListener {
        private Class<? extends Fragment> mClass;
        private int mTitleId;
        private int mPosition;
        private final Bundle mArgs;
        private Fragment mFragment;
        private Runnable runOnReselect;

        public FragmentWrapper(int title, Class<T> clz,
                Bundle args,
                Runnable runOnReselect, int position) {
            mTitleId = title;
            mClass = clz;
            mPosition = position;
            mClass = clz;
            mArgs = args;
            this.runOnReselect = runOnReselect;
        }

        @Override
        public void onClick(View v) {
            if (mCurrentPage != mPosition || getSupportFragmentManager()
                    .getBackStackEntryCount() > 0) {
                CommonUtils.debug(TAG, "onNavigationItemSelected");
                TrackerUtils.trackNavigationItemSelectedEvent(TAG,
                        NavigationHandlerFragment.this);
                if (mOnMenuClickListener != null) {
                    mOnMenuClickListener.onMenuClick(mPosition);
                }
                selectFragment();
                AddonSliderA slider = getSupportActivity().addonSlider();
                if (slider.getSliderView() != null) {
                    slider.showContentDelayed();
                }
            } else
            {
                TrackerUtils.trackNavigationItemReselectedEvent(TAG,
                        NavigationHandlerFragment.this);
                CommonUtils.debug(TAG, "onNavigationItemReselected");
                if (mCurrentPage == mPosition && runOnReselect != null)
                {
                    runOnReselect.run();
                }
            }
        }

        public Fragment getFragment() {
            if (mFragment == null)
            {
                mFragment = Fragment.instantiate(getSupportActivity(),
                        mClass.getName(),
                        mArgs);
            }
            return mFragment;
        }

        void selectFragment()
        {
            selectTab(mPosition);
        }
    }

    private static final String KEY_PAGE = "page";
    private int mCurrentPage = 0;
    private LinearLayout mMenuList;
    private OnMenuClickListener mOnMenuClickListener;
    private FragmentAdapter adapter;
    private Fragment activeFragment;
    private ViewPager pager;

    @Override
    public MainActivity getSupportActivity() {
        return (MainActivity) super.getSupportActivity();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mCurrentPage = savedInstanceState.getInt(KEY_PAGE, 0);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.menu, container, false);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_PAGE, mCurrentPage);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mMenuList = (LinearLayout) view.findViewById(R.id.menuList);
        adapter = new FragmentAdapter(getSupportFragmentManager());
        pager = (ViewPager) getActivity().findViewById(R.id.pager);
        pager.setAdapter(adapter);
        initPager();
    }

    /**
     * Select the fragment by index
     * 
     * @param index
     */
    public void selectTab(int index)
    {
        if (getSupportActivity() == null ||
                getSupportActivity().isFinishing())
        {
            return;
        }
        pager.setCurrentItem(index, false);
    }

    /**
     * Refresh the left view
     */
    private void refreshLeftView() {
        for (int position = 0, size = mMenuList.getChildCount(); position < size; position++)
        {
            SliderNavigationItem view = (SliderNavigationItem) mMenuList.getChildAt(position);
            view.setSelectionHandlerVisiblity(position == mCurrentPage);
        }
    }

    private void rebuildLeftView() {
        mMenuList.removeAllViews();
        for (int position = 0, size = adapter.getCount(); position < size; position++)
        {
            FragmentWrapper<?> wrapper = adapter.wrappers.get(position);
            SliderNavigationItem view = FontLoader.apply(new SliderNavigationItem(getSupportActivity()));
            view.setLabel(wrapper.mTitleId);
            view.setOnClickListener(wrapper);
            view.setSelectionHandlerVisiblity(position == mCurrentPage);
            mMenuList.addView(view);
        }
    }

    /**
     * Init the pager for fragments
     */
    void initPager()
    {
        adapter.add(
                R.string.tab_home,
                HomeFragment.class,
                null);
        adapter.add(
                R.string.tab_gallery,
                GalleryFragment.class, null,
                new Runnable() {
                    @Override
                    public void run() {
                        GalleryFragment gf = getGalleryFragment();
                        if (gf != null)
                        {
                            gf.cleanRefreshIfFiltered();
                        }
                    }
                });
        adapter.add(
                R.string.tab_albums,
                AlbumsFragment.class, null);
        adapter.add(
                R.string.tab_tags,
                TagsFragment.class, null);
        adapter.add(
                R.string.tab_sync,
                SyncFragment.class, null);
        adapter.add(
                R.string.tab_preferences,
                SettingsFragment.class, null);
        adapter.notifyDataSetChanged();

        // the account tab should appear only for hosted installation such
        // as profile api is absent on self-hosted
        if (CommonUtils.checkLoggedIn(true))
        {
            SystemVersionResponseUtils
                    .tryToUpdateSystemVersionCacheIfNecessaryAndRunInContextAsync(
                            new Runnable() {

                                @Override
                                public void run() {
                                    if (getActivity() != null && !getActivity().isFinishing())
                                    {
                                        if (Preferences.isHosted())
                                        {
                                            adapter.add(R.string.tab_account,
                                                    AccountFragment.class, null, null,
                                                    ACCOUNT_INDEX);
                                            for (int position = ACCOUNT_INDEX + 1, size = adapter
                                                    .getCount(); position < size; position++)
                                            {
                                                FragmentWrapper<?> wrapper = adapter.wrappers
                                                        .get(position);
                                                wrapper.mPosition = position;
                                            }
                                        }
                                        rebuildLeftView();
                                        if (mCurrentPage >= ACCOUNT_INDEX)
                                        {
                                            selectTab(mCurrentPage);
                                        }
                                    }
                                }
                            }, (LoadingControl) getActivity());
        }
        rebuildLeftView();
        // such as account tab may be absent at this step
        // we need to exclute tab selection in case actibeTab
        // is account
        if (mCurrentPage < ACCOUNT_INDEX)
        {
            selectTab(mCurrentPage);
        }
    }

    public void setOnMenuClickListener(OnMenuClickListener onMenuClickListener) {
        mOnMenuClickListener = onMenuClickListener;
    }

    /**
     * Get the currently selected page index
     * 
     * @return
     */
    public int getSelectedNavigationIndex()
    {
        return mCurrentPage;
    }

    /**
     * Get fragment by index
     * 
     * @param index
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T extends Fragment> T getFragment(int index)
    {
        return (T) adapter.getItem(index);
    }

    /**
     * Get the currently active fragment
     * 
     * @return
     */
    public Fragment getCurrentFragment()
    {
        return activeFragment;
    }

    /**
     * Get gallery fragment
     * 
     * @return
     */
    public GalleryFragment getGalleryFragment()
    {
        return getFragment(GALLERY_INDEX);
    }

    /**
     * Get sync fragment
     * 
     * @return
     */
    public SyncFragment getSyncFragment()
    {
        return getFragment(SYNC_INDEX);
    }

    /**
     * Get home fragment
     * 
     * @return
     */
    public HomeFragment getHomeFragment()
    {
        return getFragment(HOME_INDEX);
    }

    /**
     * Custom fragment pager adapter
     */
    public class FragmentAdapter extends FragmentPagerAdapter {
        List<FragmentWrapper<? extends Fragment>> wrappers = new ArrayList<FragmentWrapper<? extends Fragment>>();

        public FragmentAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return wrappers.size();
        }

        @Override
        public Fragment getItem(int position) {
            return wrappers.get(position).getFragment();
        }

        public <T extends Fragment> void add(int title, Class<T> clz,
                Bundle args) {
            add(title, clz, args, null);
        }

        public <T extends Fragment> void add(int title, Class<T> clz,
                Bundle args,
                Runnable runOnReselect) {
            final int position = getCount();
            add(title, clz, args, runOnReselect, position);
        }

        public <T extends Fragment> void add(int title, Class<T> clz, Bundle args,
                Runnable runOnReselect, final int position) {
            FragmentWrapper<?> listener = new FragmentWrapper<T>(title, clz, args,
                    runOnReselect, position);
            wrappers.add(position, listener);
        }

        void superSetPrimaryItem(ViewGroup container, final int position, Object object) {
            super.setPrimaryItem(container, position, object);
        }

        @Override
        public long getItemId(int position) {
            return wrappers.get(position).mTitleId;
        }

        @Override
        public void setPrimaryItem(final ViewGroup container, final int position,
                final Object object) {
            Fragment newFragment = getItem(position);
            if (activeFragment != null && newFragment != activeFragment)
            {
                if (activeFragment instanceof ViewPagerHandler)
                {
                    ((ViewPagerHandler) activeFragment).pageDeactivated();
                }
                if (newFragment instanceof ViewPagerHandler)
                {
                    ((ViewPagerHandler) newFragment).pageActivated();
                }
            }
            activeFragment = newFragment;
            mCurrentPage = position;
            super.setPrimaryItem(container, position, object);
            pager.post(new Runnable() {

                @Override
                public void run() {
                    if (getSupportActivity() != null && !getSupportActivity().isFinishing())
                    {
                        getSupportActivity().setActionBarTitle(": "
                                + CommonUtils.getStringResource(wrappers.get(position).mTitleId));
                        refreshLeftView();
                    }
                }
            });
        }
    }

}
