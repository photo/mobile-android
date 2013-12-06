
package com.trovebox.android.common.util;

/**
 * Simple runnable interface which run method returns a value of the defined
 * type
 * 
 * @author Eugene Popovich
 * @param <T>
 */
public interface RunnableWithResult<T> {
    T run();
}
