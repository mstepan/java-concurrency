package com.max.app.barrier;

import com.max.app.util.FileSearchUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class CyclicBarrierSearch {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final int CHUNK_SIZE = 3;

    private CyclicBarrierSearch() throws InterruptedException {

        final List<String> wordsToSearch = Arrays.asList("GNU", "Gosling", "Java",
                                                   "commercial", "similar", "low-level",
                                                   "virtual", "test123");

        final Path mainFile = Paths.get("/Users/mstepan/repo/java-concurrency/src/main/java/com/max/app/barrier/main-text.txt");

        // CountDownLatch used as a one-time exit barrier
        final CountDownLatch allCompleted = new CountDownLatch(1);

        final ExecutorService pool = Executors.newFixedThreadPool(CHUNK_SIZE);

        final AtomicInteger iterationIndex = new AtomicInteger(0);

        final AtomicInteger baseIndex = new AtomicInteger(0);

        final CyclicBarrier barrier = new CyclicBarrier(CHUNK_SIZE, () -> {

            LOG.info("Iteration {} completed\n", iterationIndex.get());
            iterationIndex.incrementAndGet();

            baseIndex.addAndGet(CHUNK_SIZE);

            if (baseIndex.get() >= wordsToSearch.size()) {
                allCompleted.countDown();
            }
        });

        for (int offset = 0; offset < CHUNK_SIZE; ++offset) {
            pool.submit(new SingleWordSearch(wordsToSearch, baseIndex, offset, mainFile, barrier, iterationIndex));
        }

        LOG.info("Search started in chunks, awaiting for last thread");

        allCompleted.await();

        LOG.info("All done");

        pool.shutdownNow();
        pool.awaitTermination(1L, TimeUnit.SECONDS);
    }

    private static final class SingleWordSearch implements Runnable {

        private final List<String> wordsToSearch;
        private final AtomicInteger baseIndex;
        private int offset;
        private final Path mainFile;
        private final CyclicBarrier barrier;
        private final AtomicInteger iterationIndex;

        SingleWordSearch(List<String> wordsToSearch, AtomicInteger baseIndex, int offset, Path mainFile, CyclicBarrier barrier,
                         AtomicInteger iterationIndex) {
            this.wordsToSearch = wordsToSearch;
            this.baseIndex = baseIndex;
            this.offset = offset;
            this.mainFile = mainFile;
            this.barrier = barrier;
            this.iterationIndex = iterationIndex;
        }

        @Override
        public void run() {
            while (baseIndex.get() < wordsToSearch.size()) {
                try {

                    final int indexForWord = baseIndex.get() + offset;

                    /*
                     If wordsToSearch % CHUNK_SIZE != 0, not all search threads will be busy searching, but
                     all should wait on barrier, because CyclicBarrier is a fixed-size barrier.
                     */
                    if (indexForWord < wordsToSearch.size()) {
                        final String search = wordsToSearch.get(baseIndex.get() + offset);

                        LOG.info("Searching '{}', iteration {}", search, iterationIndex.get());
                        TimeUnit.SECONDS.sleep(1);

                        List<Integer> foundInLines = FileSearchUtil.searchLinesWithWord(search, mainFile);

                        if (foundInLines.isEmpty()) {
                            LOG.info("Nothing found '{}'", search, foundInLines);
                        }
                        else {
                            LOG.info("Found '{}', lines {}", search, foundInLines);
                        }
                    }
                }
                catch (InterruptedException interEx) {
                    LOG.error("Thread " + Thread.currentThread().getId() + " interrupted", interEx);
                    Thread.currentThread().interrupt();
                }
                catch (Exception ex) {
                    LOG.error(ex.getMessage(), ex);
                }
                finally {
                    awaitBarrier();
                }
            }
        }

        private void awaitBarrier() {
            try {
                barrier.await();
            }
            catch (BrokenBarrierException brokenBarrierEx) {
                LOG.error(brokenBarrierEx.getMessage(), brokenBarrierEx);
            }
            catch (InterruptedException interEx) {
                Thread.currentThread().interrupt();
                LOG.error(interEx.getMessage(), interEx);
            }
        }
    }

    public static void main(String[] args) {
        try {
            new CyclicBarrierSearch();
        }
        catch (Exception ex) {
            LOG.error("Error occurred", ex);
        }
    }
}
