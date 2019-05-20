package com.max.app.barrier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Phaser;
import java.util.concurrent.TimeUnit;

public class PhaserMain {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private PhaserMain() throws InterruptedException {

        final ExecutorService pool = Executors.newCachedThreadPool();

        final Phaser phaser = new Phaser();
        phaser.register();

        for (int i = 0; i < 3; ++i) {
            pool.submit(new Task(phaser));
        }

        final int prevPhase = phaser.getPhase();
        LOG.info("main phase {}", prevPhase);

        phaser.arriveAndAwaitAdvance();

        LOG.info("main completed phase {}", prevPhase);

        phaser.arriveAndDeregister();

        pool.shutdownNow();
        pool.awaitTermination(1L, TimeUnit.SECONDS);

        LOG.info("main completed");
    }

    private static final class Task implements Runnable {

        private final Phaser phaser;

        Task(Phaser phaser) {
            this.phaser = phaser;
            this.phaser.register();
        }

        @Override
        public void run() {
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
                phaser.arriveAndDeregister();
            }
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
