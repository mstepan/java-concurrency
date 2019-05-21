package com.max.app.thread;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.Random;
import java.util.concurrent.TimeUnit;

final class DaemonVsUserThreadsMain {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private DaemonVsUserThreadsMain(boolean gcdThreadDaemon) throws Exception {

        Thread th = new Thread(new GcdTask());

        th.setDaemon(gcdThreadDaemon);
        th.start();

        LOG.info("[main] waiting for completion");

        TimeUnit.SECONDS.sleep(2);

        LOG.info("[main] done... java-" + System.getProperty("java.version"));
    }

    private static final class GcdTask implements Runnable {
        @Override
        public void run() {
            try {
                LOG.info("{} started", getThreadLabel());

                Random rand = new Random();

                final int itCount = 100_000_000;
                int coprimesCount = 0;

                for (int i = 0; i < itCount; ++i) {

                    int a = rand.nextInt(Integer.MAX_VALUE);
                    int b = rand.nextInt(Integer.MAX_VALUE);
                    int res = gcd(a, b);

                    if (res == 1) {
                        ++coprimesCount;
                    }
                }

                double coPrimesPercentage = (coprimesCount * 100.0) / itCount;

                LOG.info("{} co-primes percentage: {} %", getThreadLabel(), coPrimesPercentage);
            }
            finally {
                LOG.info("{} completed", getThreadLabel());
            }
        }

        private int gcd(int a, int b) {
            if (b == 0) {
                return a;
            }

            return gcd(b, a % b);
        }

        private String getThreadLabel() {
            return String.format("[thread-%d, %s]", Thread.currentThread().getId(),
                                 Thread.currentThread().isDaemon() ? "demon" : "user");
        }
    }

    public static void main(String[] args) {
        try {
            new DaemonVsUserThreadsMain(args.length > 0);
        }
        catch (Exception ex) {
            LOG.error("Error occurred", ex);
        }
    }
}
