package com.max.app.barrier;

import com.max.app.util.FileSearchUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class CountDownSearch {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private CountDownSearch() throws InterruptedException {
        List<String> wordsToSearch = Arrays.asList("GNU", "Gosling", "Java");

        final Path mainFile = Paths.get("/Users/mstepan/repo/java-concurrency/src/main/java/com/max/app/barrier/main-text.txt");

        ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() - 1);

        // CountDownLatch used as a one-time exit barrier
        CountDownLatch allCompleted = new CountDownLatch(wordsToSearch.size());

        for (String search : wordsToSearch) {
            pool.submit(new SingleWordSearch(search, mainFile, allCompleted));
        }

        LOG.info("Waiting for all searches to complete...");

        allCompleted.await();

        LOG.info("All completed");

        pool.shutdownNow();
        pool.awaitTermination(1L, TimeUnit.SECONDS);
    }

    private static final class SingleWordSearch implements Runnable {

        private final String search;
        private final Path mainFile;
        private final CountDownLatch allCompleted;

        SingleWordSearch(String search, Path mainFile, CountDownLatch allCompleted) {
            this.search = search;
            this.mainFile = mainFile;
            this.allCompleted = allCompleted;
        }

        @Override
        public void run() {
            try {

                LOG.info("Searching '{}'", search);
                TimeUnit.SECONDS.sleep(2);

                List<Integer> foundInLines = FileSearchUtil.searchLinesWithWord(search, mainFile);

                if (!foundInLines.isEmpty()) {
                    LOG.info("Found '{}', lines {}", search, foundInLines);
                }
            }
            catch (InterruptedException interEx) {
                LOG.error("Thread " + Thread.currentThread().getId() + " interrupted", interEx);
                Thread.currentThread().interrupt();
            }
            catch(Exception ex){
                LOG.error(ex.getMessage(), ex);
            }
            finally {
                allCompleted.countDown();
            }
        }
    }

    public static void main(String[] args) {
        try {
            new CountDownSearch();
        }
        catch (Exception ex) {
            LOG.error("Error occurred", ex);
        }
    }
}
