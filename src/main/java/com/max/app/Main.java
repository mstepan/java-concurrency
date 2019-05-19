package com.max.app;

import com.max.app.rwlock.UserStorageStampedLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

final class Main {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());


    private Main() throws Exception {

        final UserStorageStampedLock storage = new UserStorageStampedLock();

        final int threadGroupCount1 = 10;
        final int threadGroupCount2 = 10;

        final CountDownLatch allStarted = new CountDownLatch(1);
        final CountDownLatch allCompleted = new CountDownLatch(threadGroupCount1 + threadGroupCount2);

        ExecutorService pool = Executors.newFixedThreadPool(threadGroupCount1 + threadGroupCount2);

        for (int i = 0; i < threadGroupCount1; ++i) {
            pool.submit(() -> {
                try {
                    allStarted.await();

                    LOG.info("group1 started, thread id {}", Thread.currentThread().getId());

                    Random rand = new Random();
                    for (int it = 0; it < 100; ++it) {
                        storage.addUserId(rand.nextLong());
                    }
                }
                catch (InterruptedException interEx) {
                    Thread.currentThread().interrupt();
                }
                finally {
                    allCompleted.countDown();
                }
            });
        }

        for (int i = 0; i < threadGroupCount2; ++i) {
            pool.submit(() -> {
                try {
                    allStarted.await();

                    LOG.info("group2 started, thread id {}", Thread.currentThread().getId());

                    Random rand = new Random();

                    for (int it = 0; it < 100; ++it) {
                        storage.addUserIdAdnGetAll(rand.nextInt());
                    }
                }
                catch (InterruptedException interEx) {
                    Thread.currentThread().interrupt();
                }
                finally {
                    allCompleted.countDown();
                }
            });
        }

        long startTime = System.currentTimeMillis();
        allStarted.countDown();

        allCompleted.await();
        long endTime = System.currentTimeMillis();

        pool.shutdownNow();
        pool.awaitTermination(1L, TimeUnit.SECONDS);

        LOG.info("time: {} ms", (endTime - startTime));

        LOG.info("Main done... java-" + System.getProperty("java.version"));
    }

    public static void main(String[] args) {
        try {
            new Main();
        }
        catch (Exception ex) {
            LOG.error("Error occurred", ex);
        }
    }
}
