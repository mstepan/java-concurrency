package com.max.app.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class FileSearchUtil {

    private FileSearchUtil() {
        throw new AssertionError("Static utility class constructor should not be called");
    }

    /**
     *
     * @throws IllegalStateException in case if there is an IOException is thrown during file read.
     */
    public static List<Integer> searchLinesWithWord(String search, Path filePath) {
        try {
            List<String> lines = Files.readAllLines(filePath);

            List<Integer> foundInLines = new ArrayList<>();

            for (int i = 0; i < lines.size(); ++i) {
                String singleLine = lines.get(i);

                if (singleLine.contains(search)) {
                    foundInLines.add(i);
                }
            }

            return foundInLines;
        }
        catch (IOException ioEx) {
            throw new IllegalStateException(ioEx);
        }
    }

}
