package com.halloapp.util;

import androidx.annotation.NonNull;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Thread pool for doing background work
 *
 * Implementation from
 * https://stackoverflow.com/questions/19528304/how-to-get-the-threadpoolexecutor-to-increase-threads-to-max-before-queueing
 *
 * Which uses a custom queue implementation to prefer increasing threads if core pool can't keep up
 */
public class BgWorkers {

    private static final int CORE_POOL_SIZE = 8;
    private static final int MAXIMUM_POOL_SIZE = 128;

    private final ThreadPoolExecutor threadPool;

    private static BgWorkers instance;

    public static BgWorkers getInstance() {
        if (instance == null) {
            synchronized (BgWorkers.class) {
                if (instance == null) {
                    instance = new BgWorkers();
                }
            }
        }
        return instance;
    }

    private BgWorkers() {
        // Custom queue, as when offer returns false we try to increase the number of threads
        BlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>() {
            @Override
            public boolean offer(Runnable e) {
                if (size() == 0) {
                    // No items in queue, we're keeping up fine
                    return super.offer(e);
                } else {
                    // Queue has some items waiting, increase threads
                    return false;
                }
            }
        };

        threadPool = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, 60, TimeUnit.SECONDS, queue);
        threadPool.setRejectedExecutionHandler((r, executor) -> {
            try {
                // We hit max thread count, queue up the task
                executor.getQueue().put(r);

                if (executor.isShutdown()) {
                    throw new RejectedExecutionException("Task " + r + " rejected from " + executor);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }

    public void execute(@NonNull Runnable runnable) {
        threadPool.execute(runnable);
    }

    public <T> Future<T> submit(@NonNull Callable<T> callable) {
        return threadPool.submit(callable);
    }
}
