package com.trovebox.android.app.util;

import java.io.Serializable;

/**
 * The basic serializable object accessor class
 * 
 * @author Eugene Popovic
 * @param <T>
 */
public interface ObjectAccessor<T> extends RunnableWithResult<T>, Serializable
{

}