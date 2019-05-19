package com.max.app.rwlock;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public final class UserStorage {

    private final Set<Long> userIds = new HashSet<>();

    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final Lock writeLock = rwLock.writeLock();
    private final Lock readLock = rwLock.readLock();

    public List<Long> addUserIdAdnGetAll(long userId) {

        Lock lock = rwLock.writeLock();

        try {
            userIds.add(userId);

            // downgrade to read lock
            Lock tempReadLock = rwLock.readLock();
            tempReadLock.lock();

            try {
                lock.unlock();
                return new ArrayList<>(userIds);
            }
            finally {
                lock = tempReadLock;
            }
        }
        finally {
            lock.unlock();
        }
    }

    public void addUserId(long userId) {
        writeLock.lock();
        try {
            userIds.add(userId);
        }
        finally {
            writeLock.unlock();
        }
    }

    public List<Long> getUserIds() {
        readLock.lock();
        try {
            return new ArrayList<>(userIds);
        }
        finally {
            readLock.unlock();
        }
    }
}
