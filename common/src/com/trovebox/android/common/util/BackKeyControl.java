
package com.trovebox.android.common.util;

/**
 * If fragment provides custom back button pressed behaviour it can implement
 * this interface
 * 
 * @author Eugene Popovich
 */
public interface BackKeyControl {
    /**
     * @return true if back key action was overrode, otherwise returns false
     */
    boolean isBackKeyOverrode();
}
