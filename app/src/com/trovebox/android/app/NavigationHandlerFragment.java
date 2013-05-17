
package com.trovebox.android.app;

import java.util.ArrayList;
import java.util.List;

import org.holoeverywhere.FontLoader;
import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.addon.AddonSlider.AddonSliderA;
import org.holoeverywhere.widget.LinearLayout;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import com.trovebox.android.app.common.CommonFragment;
import com.trovebox.android.app.common.lifecycle.ViewPagerHandler;
import com.trovebox.android.app.net.SystemVersionResponseUtils;
import com.trovebox.android.app.ui.adapter.FragmentPagerAdapter;
import com.trovebox.android.app.ui.widget.SliderCategorySeparator;
import com.trovebox.android.app.ui.widget.SliderNavigationItem;
import com.trovebox.android.app.util.CommonUtils;
import com.trovebox.android.app.util.GuiUtils;
import com.trovebox.android.app.util.LoadingControl;
import com.trovebox.android.app.util.TrackerUtils;

/**
 * The fragment which handles navigation in the MainActivity. Left slider and
 * viewpager is there
 * 
 * @author Eugene Popovich
 */
public class NavigationHandlerFragment extends CommonFragment {
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
        private int mSeparatorTitleId = View.NO_ID;
        private int mPosition;
        private int mIconId;
        private final Bundle mArgs;
        private Fragment mFragment;
        private Runnable runOnReselect;

        public FragmentWrapper(int title, int icon, Class<T> clz,
                Bundle args,
                Runnable runOnReselect, int position) {
            mTitleId = title;
            mIconId = icon;
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

        public Fragment getInstantiatedFragment() {
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
        super.onCreateView(inflater, container, savedInstanceState);
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
    @SuppressWarnings("unchecked")
    private void refreshLeftView() {
        List<SliderNavigationItem> navigationItems = (List<SliderNavigationItem>) mMenuList
                .getTag();
        if (navigationItems != null)
        {
            for (int position = 0, size = navigationItems.size(); position < size; position++)
            {
                SliderNavigationItem view = navigationItems.get(position);
                view.setSelected(position == mCurrentPage);
            }
        }
    }

    private void rebuildLeftView() {
        mMenuList.removeAllViews();
        List<SliderNavigationItem> navigationItems = new ArrayList<SliderNavigationItem>();
        for (int position = 0, size = adapter.getCount(); position < size; position++)
        {
            FragmentWrapper<?> wrapper = adapter.wrappers.get(position);
            if (wrapper.mSeparatorTitleId != View.NO_ID)
            {
                SliderCategorySeparator view = FontLoader.apply(new SliderCategorySeparator(
                        getSupportActivity()));
                view.setLabel(wrapper.mSeparatorTitleId);
                mMenuList.addView(view);
            }
            SliderNavigationItem view = FontLoader.apply(new SliderNavigationItem(
                    getSupportActivity()));
            view.setLabel(wrapper.mTitleId);
            if (wrapper.mIconId != View.NO_ID)
            {
                view.setIcon(getResources().getDrawable(wrapper.mIconId));
            }
            view.setOnClickListener(wrapper);
            view.setSelected(position == mCurrentPage);
            mMenuList.addView(view);
            navigationItems.add(view);
        }
        mMenuList.setTag(navigationItems);
    }

    /**
     * Init the pager for fragments
     */
    void initPager()
    {
        adapter.add(
                R.string.tab_home,
                R.drawable.menu_latest_2states,
                HomeFragment.class,
                null);
        adapter.add(
                R.string.tab_gallery,
                R.drawable.menu_gallery_2states,
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
                R.drawable.menu_album_2states,
                AlbumsFragment.class, null);
        adapter.add(
                R.string.tab_tags,
                R.drawable.menu_tags_2states,
                TagsFragment.class, null);
        adapter.add(
                R.string.tab_sync,
                R.drawable.menu_upload_2states,
                SyncFragment.class, null);
        FragmentWrapper<?> wrapper = adapter.add(
                R.string.tab_settings,
                R.drawable.menu_settings_2states,
                SettingsFragment.class, null);
        wrapper.mSeparatorTitleId = R.string.tab_preferences;

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
                                            FragmentWrapper<?> wrapper;

                                            wrapper = adapter.add(R.string.tab_account,
                                                    R.drawable.menu_profile_2states,
                                                    AccountFragment.class, null, null,
                                                    ACCOUNT_INDEX);
                                            wrapper.mSeparatorTitleId = R.string.tab_preferences;
                                            for (int position = ACCOUNT_INDEX + 1, size = adapter
                                                    .getCount(); position < size; position++)
                                            {
                                                wrapper = adapter.wrappers
                                                        .get(position);
                                                wrapper.mPosition = position;
                                                wrapper.mSeparatorTitleId = View.NO_ID;
                                            }
                                            adapter.notifyDataSetChanged();
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
        T result = null;
        if (isAdded() && getSupportFragmentManager() != null)
        {
            result = (T) adapter.getActiveFragment(pager, getSupportFragmentManager(), index);
        }
        return result;
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
     * Get the account fragment if present
     * 
     * @return
     */
    public AccountFragment getAccountFragment()
    {
        Fragment result = getFragment(ACCOUNT_INDEX);
        if (!(result instanceof AccountFragment))
        {
            result = null;
        }
        return (AccountFragment) result;
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
            return wrappers.get(position).getInstantiatedFragment();
        }

        public <T extends Fragment> FragmentWrapper<?> add(int title, int icon, Class<T> clz,
                Bundle args) {
            return add(title, icon, clz, args, null);
        }

        public <T extends Fragment> FragmentWrapper<?> add(int title, int icon, Class<T> clz,
                Bundle args,
                Runnable runOnReselect) {
            final int position = getCount();
            return add(title, icon, clz, args, runOnReselect, position);
        }

        public <T extends Fragment> FragmentWrapper<?> add(int title, int icon, Class<T> clz,
                Bundle args,
                Runnable runOnReselect, final int position) {
            FragmentWrapper<?> listener = new FragmentWrapper<T>(title, icon, clz, args,
                    runOnReselect, position);
            wrappers.add(position, listener);
            return listener;
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
            Fragment newFragment = (Fragment) object;
            if (newFragment != activeFragment)
            {
                if (activeFragment != null && activeFragment instanceof ViewPagerHandler)
                {
                    ((ViewPagerHandler) activeFragment).pageDeactivated();
                    // #244
                    hideSoftKeyboardForActiveFragment();
                }
                if (newFragment != null && newFragment instanceof ViewPagerHandler)
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

        /**
         * Hhide soft keyboard from window for active fragment
         */
        public void hideSoftKeyboardForActiveFragment() {
            try
            {
                View target = activeFragment.getView().findFocus();

                if (target != null)
                {
                    InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    mgr.hideSoftInputFromWindow(target.getWindowToken(), 0);
                }
            } catch (Exception ex)
            {
                GuiUtils.error(TAG, ex);
            }
        }

        /**
         * http://stackoverflow.com/a/9293207/527759
         * 
         * @param container
         * @param fragmentManager
         * @param position
         * @return
         */
        public Fragment getActiveFragment(ViewPager container, FragmentManager fragmentManager,
                int position) {
            String name = makeFragmentName(container.getId(), getItemId(position));
            return fragmentManager.findFragmentByTag(name);
        }

        /**
         * Copy of makeFragmentName from parent FragmentPagerAdapter class
         * 
         * @param viewId
         * @param id
         * @return
         */
        private String makeFragmentName(int viewId, long id) {
            return "android:switcher:" + viewId + ":" + id;
        }
    }

}
