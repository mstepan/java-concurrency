package com.max.app.executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ExecutorCompletionMain {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private ExecutorCompletionMain() throws InterruptedException, ExecutionException {

        final ExecutorService pool = Executors.newFixedThreadPool(2);

        final ExecutorCompletionService<String> completionService = new ExecutorCompletionService<>(pool);

        List<String> files = Arrays.asList(
                "/Users/mstepan/repo/java-concurrency/src/main/java/com/max/app/executor/file1.txt",
                "/Users/mstepan/repo/java-concurrency/src/main/java/com/max/app/executor/file2.txt",
                "/Users/mstepan/repo/java-concurrency/src/main/java/com/max/app/executor/file3.txt",
                "/Users/mstepan/repo/java-concurrency/src/main/java/com/max/app/executor/file4.txt",
                "/Users/mstepan/repo/java-concurrency/src/main/java/com/max/app/executor/file5.txt"
        );

        for (String singlePath : files) {
            completionService.submit(new ChecksumCalculatorTask(singlePath));
        }

        for (int i = 0; i < files.size(); ++i) {
            String hash = completionService.take().get();
            LOG.info(hash);
        }

        pool.shutdownNow();
        pool.awaitTermination(1L, TimeUnit.SECONDS);

        LOG.info("main completed");
    }

    private static final class ChecksumCalculatorTask implements Callable<String> {

        private static final int BUF_READ_SIZE_IN_BYTES = 4096;

        private final String filePath;

        ChecksumCalculatorTask(String filePath) {
            this.filePath = filePath;
        }

        @Override
        public String call() throws Exception {

            final MessageDigest digest = MessageDigest.getInstance("SHA-256");

            final byte[] buf = new byte[BUF_READ_SIZE_IN_BYTES];

            try (InputStream in = Files.newInputStream(Paths.get(filePath));
                 BufferedInputStream bufIn = new BufferedInputStream(in)) {

                int readedBytes;

                while ((readedBytes = bufIn.read(buf)) != -1) {
                    digest.update(buf, 0, readedBytes);
                }

                return filePath + " : " + toHexString(digest.digest());
            }
        }

        private static String toHexString(byte[] data) {
            StringBuilder buf = new StringBuilder(2 * data.length);

            String singleHex;

            for (byte singleByte : data) {
                singleHex = Integer.toHexString(singleByte & 0xFF);

                if (singleHex.length() == 1) {
                    buf.append('0');
                }

                buf.append(singleHex);
            }

            return buf.toString();
        }
    }

    public static void main(String[] args) {
        try {
            new ExecutorCompletionMain();
        }
        catch (Exception ex) {
            LOG.error("Error occurred", ex);
        }
    }
}
