package com.trovebox.android.app.util;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Basic loading control with the counter
 * 
 * @author Eugene Popovich
 */
public abstract class LoadingControlWithCounter implements LoadingControl {
    private AtomicInteger loaders = new AtomicInteger(0);
    
    public LoadingControlWithCounter()
    {
    }
    
    @Override
    public final void startLoading()
    {
        if (loaders.getAndIncrement() == 0)
        {
            startLoadingEx();
        }
    }

    @Override
    public final void stopLoading()
    {
        if (loaders.decrementAndGet() == 0)
        {
            stopLoadingEx();
        }
    }
    
    @Override
    public boolean isLoading()
    {
        return loaders.get() == 0;
    }

    public abstract void startLoadingEx();

    public abstract void stopLoadingEx();
}
