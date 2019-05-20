package com.max.app.pinpong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

final class PingPongMain {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());


    private PingPongMain() throws Exception {

        final int iterationsCount = 10;

        final CountDownLatch exitBarrier = new CountDownLatch(2);

        final ExecutorService pool = Executors.newCachedThreadPool();

        final BlockingQueue<Integer> exchangeQueue = new SynchronousQueue<>();

        pool.submit(new PingTask(iterationsCount, exchangeQueue, exitBarrier));
        pool.submit(new PongTask(iterationsCount, exchangeQueue, exitBarrier));

        exitBarrier.await();

        pool.shutdownNow();
        pool.awaitTermination(200L, TimeUnit.MILLISECONDS);

        LOG.info("Main done... java-" + System.getProperty("java.version"));
    }

    private static final class PingTask implements Runnable {

        private final int iterationsCount;
        private final BlockingQueue<Integer> exchangeQueue;
        private final CountDownLatch exitBarrier;

        PingTask(int iterationsCount, BlockingQueue<Integer> exchangeQueue, CountDownLatch exitBarrier) {
            this.iterationsCount = iterationsCount;
            this.exchangeQueue = exchangeQueue;
            this.exitBarrier = exitBarrier;
        }

        @Override
        public void run() {

            LOG.info("Ping started");

            try {
                for (int i = 0; i < iterationsCount; ++i) {
                    LOG.info("Ping *");
                    exchangeQueue.put(i);
                    exchangeQueue.take();
                }
            }
            catch (InterruptedException interEx) {
                Thread.currentThread().interrupt();
                LOG.error(interEx.getMessage(), interEx);
            }
            finally {
                LOG.info("Ping completed");
                exitBarrier.countDown();
            }
        }
    }

    private static final class PongTask implements Runnable {

        private final int iterationsCount;
        private final BlockingQueue<Integer> exchangeQueue;
        private final CountDownLatch exitBarrier;

        PongTask(int iterationsCount, BlockingQueue<Integer> exchangeQueue, CountDownLatch exitBarrier) {
            this.iterationsCount = iterationsCount;
            this.exchangeQueue = exchangeQueue;
            this.exitBarrier = exitBarrier;
        }

        @Override
        public void run() {

            LOG.info("Pong started");

            try {
                for (int i = 0; i < iterationsCount; ++i) {
                    exchangeQueue.take();
                    LOG.info("Pong <<");
                    exchangeQueue.put(i);
                }
            }
            catch (InterruptedException interEx) {
                Thread.currentThread().interrupt();
                LOG.error(interEx.getMessage(), interEx);
            }
            finally {
                LOG.info("Pong completed");
                exitBarrier.countDown();
            }
        }
    }


    public static void main(String[] args) {
        try {
            new PingPongMain();
        }
        catch (Exception ex) {
            LOG.error("Error occurred", ex);
        }
    }
}
