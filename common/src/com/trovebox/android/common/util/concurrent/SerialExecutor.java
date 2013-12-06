
package com.trovebox.android.common.util.concurrent;

import java.util.concurrent.Executor;

public class SerialExecutor implements Executor {
    final ArrayDeque<Runnable> mTasks = new ArrayDeque<Runnable>();
    Runnable mActive;
    Executor threadPoolExecutor;

    public SerialExecutor(Executor threadPoolExecutor) {
        this.threadPoolExecutor = threadPoolExecutor;
    }

    @Override
    public synchronized void execute(final Runnable r) {
        mTasks.offer(new Runnable() {
            @Override
            public void run() {
                try {
                    r.run();
                } finally {
                    scheduleNext();
                }
            }
        });
        if (mActive == null) {
            scheduleNext();
        }
    }

    protected synchronized void scheduleNext() {
        if ((mActive = mTasks.poll()) != null) {
            threadPoolExecutor.execute(mActive);
        }
    }
}
