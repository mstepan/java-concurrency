package com.max.app.pinpong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Exchanger;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

final class PingPongMain {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());


    private PingPongMain() throws InterruptedException {

        final int iterationsCount = 10;

        final CountDownLatch exitBarrier = new CountDownLatch(2);

        final ExecutorService pool = Executors.newCachedThreadPool();

        final Exchanger<Integer> exchanger = new Exchanger<>();

        pool.submit(new PingPongTask("ping", 0, iterationsCount, exchanger, exitBarrier));
        pool.submit(new PingPongTask("pong", 1, iterationsCount, exchanger, exitBarrier));

        exitBarrier.await();

        pool.shutdownNow();
        pool.awaitTermination(200L, TimeUnit.MILLISECONDS);

        LOG.info("Main done... java-" + System.getProperty("java.version"));
    }

    private static final class PingPongTask implements Runnable {

        private final String title;
        private final int initialValue;
        private final int iterationsCount;
        private final Exchanger<Integer> exchanger;
        private final CountDownLatch exitBarrier;
        private final String separator;

        PingPongTask(String title, int initialValue, int iterationsCount, Exchanger<Integer> exchanger,
                     CountDownLatch exitBarrier) {
            this.title = title;
            this.initialValue = initialValue;
            this.iterationsCount = iterationsCount;
            this.exchanger = exchanger;
            this.exitBarrier = exitBarrier;
            this.separator = initialValue == 0 ? "*" : "<<";
        }

        @Override
        public void run() {

            LOG.info("[{}] {} started", Thread.currentThread().getId(), title);

            try {

                int value = initialValue;

                for (int i = 0; i < iterationsCount; ++i) {

                    if( value == 0 ){
                        LOG.info("[{}] {} {}", Thread.currentThread().getId(), title, separator);
                    }

                    value = exchanger.exchange(value);
                }
            }
            catch (InterruptedException interEx) {
                Thread.currentThread().interrupt();
                LOG.error(interEx.getMessage(), interEx);
            }
            finally {
                LOG.info("[{}] {} completed", Thread.currentThread().getId(), title);
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
