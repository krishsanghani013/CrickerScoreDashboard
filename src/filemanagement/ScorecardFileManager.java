package filemanagement;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ScorecardFileManager {
    private static final String DEFAULT_DIRECTORY = "data/scorecards";

    public Path saveScorecard(String fileName, String content) throws IOException {
        Path directory = Paths.get(DEFAULT_DIRECTORY);
        Files.createDirectories(directory);

        Path filePath = directory.resolve(normalizeFileName(fileName));
        try (BufferedWriter writer = Files.newBufferedWriter(filePath)) {
            writer.write(content);
        }
        return filePath;
    }

    private String normalizeFileName(String fileName) {
        String normalized = fileName == null ? "" : fileName.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("File name cannot be empty.");
        }

        normalized = normalized.replaceAll("[\\\\/:*?\"<>|]", "_");
        if (!normalized.toLowerCase().endsWith(".txt")) {
            normalized += ".txt";
        }
        return normalized;
    }
}
