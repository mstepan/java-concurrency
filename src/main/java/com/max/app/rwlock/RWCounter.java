package com.max.app.rwlock;

public class RWCounter {

    private volatile long value;

    public RWCounter(long value) {
        this.value = value;
    }

    public long getValue() {
        return value;
    }

    public long incrementAndGet() {
        synchronized (this) {
            ++value;
            return value;
        }
    }
}
