package org.example.service;

import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.dto.AnalysisStats;
import org.example.dto.FileDamageDetailDto;
import org.example.dto.FileStatusDto;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class FileAnalysisService {

    private String originalDir;
    private String damagedDir;
    private final Map<String, FileStatusDto> fileStatuses = new ConcurrentHashMap<>();
    private final Map<String, List<FileDamageDetailDto>> fileDamages = new ConcurrentHashMap<>();

    @Getter
    private volatile boolean analysisRunning = false;
    private final Logger log = LogManager.getLogger(FileAnalysisService.class);

    public void setDirectories(String originalDir, String damagedDir) {
        this.originalDir = originalDir;
        this.damagedDir = damagedDir;
        log.info("Directories set: {} → {}", originalDir, damagedDir);
    }

    /**
     * Start asynchronous file analysis
     */
    public void startAnalysis() {
        if (analysisRunning) {
            throw new IllegalStateException("Analysis already running");
        }
        analysisRunning = true;
        fileStatuses.clear();
        fileDamages.clear();

        CompletableFuture.runAsync(() -> {
            try {
                analyzeDirectories();
            } catch (IOException e) {
                log.error("Error during analysis", e);
            } finally {
                analysisRunning = false;
            }
        });
    }

    /**
     * Walk original directory and analyze each file
     */
    private void analyzeDirectories() throws IOException {
        Path originalPath = Paths.get(originalDir);
        Path damagedPath = Paths.get(damagedDir);

        if (!Files.exists(originalPath)) {
            throw new IOException("Original directory not found: " + originalDir);
        }

        try (Stream<Path> paths = Files.walk(originalPath)) {
            paths.filter(Files::isRegularFile)
                    .forEach(file -> analyzeFile(file, damagedPath));
        }
    }

    /**
     * Compare single original file with damaged counterpart
     */
    private void analyzeFile(Path originalFile, Path damagedRoot) {
        String fileName = originalFile.getFileName().toString();
        Path damagedFile = damagedRoot.resolve(fileName);

        FileStatusDto status = new FileStatusDto();
        status.setFileName(fileName);

        try {
            if (!Files.exists(damagedFile)) {
                status.setStatus("MISSING");
                fileStatuses.put(fileName, status);
                return;
            }

            List<FileDamageDetailDto> damages = compareFilesWithDetails(originalFile, damagedFile);

            if (damages.isEmpty()) {
                status.setStatus("OK");
            } else {
                status.setStatus("DAMAGED");
                status.setDamageCount(Integer.valueOf(damages.size()));
                fileDamages.put(fileName, damages);
            }
        } catch (IOException e) {
            status.setStatus("ERROR");
            log.error("Error analyzing file: {}", fileName, e);
        }

        fileStatuses.put(fileName, status);
    }

    /**
     * Compare byte-by-byte two files and return damage details
     */
    private List<FileDamageDetailDto> compareFilesWithDetails(Path file1, Path file2) throws IOException {
        List<FileDamageDetailDto> damages = new ArrayList<>();

        try (InputStream is1 = Files.newInputStream(file1);
             InputStream is2 = Files.newInputStream(file2)) {

            byte[] buffer1 = new byte[8192];
            byte[] buffer2 = new byte[8192];
            long offset = 0;

            while (true) {
                int bytes1 = is1.read(buffer1);
                int bytes2 = is2.read(buffer2);

                if (bytes1 == -1 || bytes2 == -1) {
                    if (bytes1 != bytes2) {
                        log.warn("Different file lengths detected");
                    }
                    break;
                }

                for (int i = 0; i < Math.min(bytes1, bytes2); i++) {
                    if (buffer1[i] != buffer2[i]) {
                        damages.add(new FileDamageDetailDto(
                                (int) (offset + i),
                                buffer1[i] & 0xFF,
                                buffer2[i] & 0xFF
                        ));
                    }
                }

                offset += Math.min(bytes1, bytes2);
            }
        }
        return damages;
    }

    /**
     * Get analysis statistics
     */
    public AnalysisStats getAnalysisStats() {
        AnalysisStats stats = new AnalysisStats();

        stats.setDamagedFilesCount((int) fileStatuses.values().stream()
                .filter(s -> "DAMAGED".equals(s.getStatus())).count());
        stats.setOkFilesCount((int) fileStatuses.values().stream()
                .filter(s -> "OK".equals(s.getStatus())).count());
        stats.setMissingFilesCount((int) fileStatuses.values().stream()
                .filter(s -> "MISSING".equals(s.getStatus())).count());
        stats.setTotalOriginalFiles(fileStatuses.size());

        return stats;
    }

    public List<FileStatusDto> getFileStatuses() {
        return fileStatuses.values().stream()
                .map(status -> {
                    List<FileDamageDetailDto> damages = getFileDamages(status.getFileName());
                    FileStatusDto dto = new FileStatusDto();
                    dto.setFileName(status.getFileName());
                    dto.setStatus(status.getStatus());
                    dto.setDamageCount(status.getDamageCount());
                    dto.setDamages(damages);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    /**
     * Get detailed damages for specific file (separate API)
     */
    public List<FileDamageDetailDto> getFileDamages(String fileName) {
        return fileDamages.getOrDefault(fileName, Collections.emptyList());
    }

}
