package com.max.app.lock;

import com.max.app.util.UnsafeUtils;
import sun.misc.Unsafe;

public final class SpinningLock {

    private static final int FREE_STATE = 0;
    private static final int BUSY_STATE = 1;

    private static final Unsafe unsafe = UnsafeUtils.getUnsafe();

    private static final long valueOffset;

    static {
        try {
            valueOffset = unsafe.objectFieldOffset(SpinningLock.class.getDeclaredField("value"));
        }
        catch (NoSuchFieldException ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    private int value;

    public void lock() {
        while (!unsafe.compareAndSwapInt(this, valueOffset, FREE_STATE, BUSY_STATE)) {
        }
    }

    public void unlock() {
        if (!unsafe.compareAndSwapInt(this, valueOffset, BUSY_STATE, FREE_STATE)) {
            throw new IllegalStateException("Lock wasn't locked");
        }
    }
}
