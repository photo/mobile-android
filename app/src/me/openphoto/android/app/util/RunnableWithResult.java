package me.openphoto.android.app.util;

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
