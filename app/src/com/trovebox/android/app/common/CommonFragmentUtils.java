
package com.trovebox.android.app.common;

import android.support.v4.app.FragmentManager;

import com.trovebox.android.app.util.GuiUtils;

/**
 * The class containing different utils method for the fragments
 * 
 * @author Eugene Popovich
 */
public class CommonFragmentUtils {
    /**
     * Locate an existing instance of this Fragment or if not found, create and
     * add it using FragmentManager.
     * 
     * @param clazz the fragment class
     * @param fm The FragmentManager manager to use.
     * @return The existing instance of the Fragment or the new instance if just
     *         created.
     */
    public static <T extends CommonFragment> T findOrCreateFragment(Class<T> clazz,
            FragmentManager fm) {
        return findOrCreateFragment(clazz, fm, clazz.getSimpleName());
    }

    /**
     * Locate an existing instance of this Fragment or if not found, create and
     * add it using FragmentManager.
     * 
     * @param clazz the fragment class
     * @param fm The FragmentManager manager to use.
     * @param TAG the tag to search fragment by
     * @return The existing instance of the Fragment or the new instance if just
     *         created.
     */
    @SuppressWarnings("unchecked")
    public static <T extends CommonFragment> T findOrCreateFragment(Class<T> clazz,
            FragmentManager fm, String TAG) {
        T mRetainFragment = null;
        try {
            // Check to see if we have retained the worker fragment.
            mRetainFragment = (T) fm.findFragmentByTag(TAG);

            // If not retained (or first time running), we need to create and
            // add
            // it.
            if (mRetainFragment == null) {
                mRetainFragment = clazz.newInstance();
                fm.beginTransaction().add(mRetainFragment, TAG).commit();
            }
        } catch (Exception ex) {
            GuiUtils.error(TAG, ex);
        }
        return mRetainFragment;
    }
}
