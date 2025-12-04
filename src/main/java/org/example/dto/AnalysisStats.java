package org.example.dto;

import lombok.Data;

@Data
public class AnalysisStats {
    private long totalOriginalFiles;
    private long totalDamagedFiles;
    private int damagedFilesCount;
    private int okFilesCount;
    private int missingFilesCount;

}
