
package com.trovebox.android.common.utils.lifecycle;

/**
 * Basic handler for fragments inside viewpager to know when page is
 * activated/deactivated
 * 
 * @author Eugene Popovich
 */
public interface ViewPagerHandler {
    /**
     * Should be called when page become active. Handle this manually in the
     * viewpager adapter
     */
    void pageActivated();

    /**
     * Should be called when page become inactive. Handle this manually in the
     * viewpager adapter
     */
    void pageDeactivated();
}
