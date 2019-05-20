package com.max.app.barrier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Phaser;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class PhaserMain {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private PhaserMain() throws InterruptedException {

        final ExecutorService pool = Executors.newCachedThreadPool();

        final AtomicBoolean completed = new AtomicBoolean(false);

        final Phaser phaser = new Phaser();
        phaser.register();


        for (int i = 0; i < 3; ++i) {
            pool.submit(new Task(phaser, completed));
        }

        LOG.info("main phase {}", phaser.getPhase());
        phaser.arriveAndAwaitAdvance();

        LOG.info("main phase {}", phaser.getPhase());
        phaser.arriveAndAwaitAdvance();

        completed.set(true);

        LOG.info("main phase {}", phaser.getPhase());
        phaser.arriveAndAwaitAdvance();

        phaser.arriveAndDeregister();

        pool.shutdownNow();
        pool.awaitTermination(1L, TimeUnit.SECONDS);

        LOG.info("main completed");
    }

    private static final class Task implements Runnable {

        private final Phaser phaser;
        private final AtomicBoolean completed;

        Task(Phaser phaser, AtomicBoolean completed) {
            this.phaser = phaser;
            this.completed = completed;
            this.phaser.register();
        }

        @Override
        public void run() {
            while (!(completed.get() || phaser.isTerminated())) {
                try {
                    TimeUnit.SECONDS.sleep(1);

                    LOG.info("start thread {} phase {}", Thread.currentThread().getId(), phaser.getPhase());

                    TimeUnit.SECONDS.sleep(1);
                }
                catch (InterruptedException interEx) {
                    Thread.currentThread().interrupt();
                    LOG.info(interEx.getMessage(), interEx);
                }
                finally {
                    LOG.info("end thread {} phase {}", Thread.currentThread().getId(), phaser.getPhase());
                    phaser.arriveAndAwaitAdvance();
                }
            }

            phaser.arriveAndDeregister();
        }
    }

    public static void main(String[] args) {
        try {
            new PhaserMain();
        }
        catch (Exception ex) {
            LOG.error("Error occurred", ex);
        }
    }
}
