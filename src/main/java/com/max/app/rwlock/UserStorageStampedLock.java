package com.max.app.rwlock;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.StampedLock;

public final class UserStorageStampedLock {

    private final Set<Long> userIds = new HashSet<>();

    private final StampedLock lock = new StampedLock();

    public List<Long> addUserIdAdnGetAll(long userId) {

        long stamp = lock.writeLock();

        try {
            userIds.add(userId);

            long readStamp = lock.tryConvertToReadLock(stamp);

            if (readStamp != 0L) {
                stamp = readStamp;
                return new ArrayList<>(userIds);
            }
            else {
                lock.unlock(stamp);
                stamp = lock.readLock();
                return new ArrayList<>(userIds);
            }
        }
        finally {
            lock.unlock(stamp);
        }
    }

    public void addUserId(long userId) {
        long stamp = lock.writeLock();
        try {
            userIds.add(userId);
        }
        finally {
            lock.unlockWrite(stamp);
        }
    }

    public List<Long> getUserIds() {

        long stamp = lock.tryOptimisticRead();

        final List<Long> data = new ArrayList<>(userIds);

        if (lock.validate(stamp)) {
            return data;
        }

        try {
            stamp = lock.readLock();
            return new ArrayList<>(userIds);
        }
        finally {
            lock.unlockRead(stamp);
        }
    }
}
