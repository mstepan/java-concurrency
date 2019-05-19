package com.max.app.counter;

public class FullySynchCounter {

    private final Object mutex = new Object();

    volatile long value;

    public void increment() {
        synchronized (mutex) {
            ++value;
        }
    }

    public long getValue() {
        synchronized (mutex) {
            return value;
        }
    }

    @Override
    public String toString() {
        return String.valueOf(getValue());
    }
}
