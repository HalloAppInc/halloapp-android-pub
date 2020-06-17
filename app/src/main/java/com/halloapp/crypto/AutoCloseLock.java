package com.halloapp.crypto;

import com.halloapp.util.Log;

import java.util.concurrent.Semaphore;

public class AutoCloseLock implements AutoCloseable {
    private final Semaphore lock = new Semaphore(1);

    public AutoCloseLock lock() throws InterruptedException {
        this.lock.acquire();
        return this;
    }

    public void close() {
        if (this.lock.availablePermits() == 0) {
            this.lock.release();
        }
    }
}
