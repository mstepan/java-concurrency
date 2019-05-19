package com.max.app.queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public final class SimpleBlockingQueue<T> implements BlockingQueue<T> {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final int capacity;
    private final List<T> data;

    private final ReentrantLock monitor = new ReentrantLock();

    private final Condition notEmpty = monitor.newCondition();
    private final Condition notFull = monitor.newCondition();

    public SimpleBlockingQueue(int capacity) {
        this.capacity = capacity;
        this.data = new ArrayList<>(capacity);
    }

    @Override
    public T take() throws InterruptedException {

        final ReentrantLock lock = this.monitor;
        lock.lockInterruptibly();

        try {
            while (data.isEmpty()) {
                notEmpty.await();
            }

            T retValue = data.remove(0);
            notFull.signal();
            return retValue;
        }
        finally {
            lock.unlock();
        }
    }

    @Override
    public void put(T value) throws InterruptedException {

        final ReentrantLock lock = this.monitor;
        lock.lockInterruptibly();

        try {
            while (data.size() == capacity) {
                notFull.await();
            }

            data.add(value);
            notEmpty.signal();
        }
        finally {
            lock.unlock();
        }
    }

    @Override
    public boolean add(T t) {
        return false;
    }

    @Override
    public boolean offer(T t) {
        return false;
    }

    @Override
    public boolean offer(T t, long timeout, TimeUnit unit) throws InterruptedException {
        return false;
    }

    @Override
    public T poll(long timeout, TimeUnit unit) throws InterruptedException {
        return null;
    }

    @Override
    public int remainingCapacity() {
        return 0;
    }

    @Override
    public boolean remove(Object o) {
        return false;
    }

    @Override
    public boolean contains(Object o) {
        return false;
    }

    @Override
    public int drainTo(Collection<? super T> c) {
        return 0;
    }

    @Override
    public int drainTo(Collection<? super T> c, int maxElements) {
        return 0;
    }

    @Override
    public T remove() {
        return null;
    }

    @Override
    public T poll() {
        return null;
    }

    @Override
    public T element() {
        return null;
    }

    @Override
    public T peek() {
        return null;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public Iterator<T> iterator() {
        return null;
    }

    @Override
    public Object[] toArray() {
        return new Object[0];
    }

    @Override
    public <T1> T1[] toArray(T1[] a) {
        return null;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return false;
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        return false;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return false;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return false;
    }

    @Override
    public void clear() {

    }
}
