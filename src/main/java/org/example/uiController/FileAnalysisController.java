package org.example.uiController;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.dto.FileDamageDetailDto;
import org.example.service.FileAnalysisService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/")
public class FileAnalysisController {

    private final FileAnalysisService service;
    private final Logger log = LogManager.getLogger(FileAnalysisController.class);

    public FileAnalysisController(FileAnalysisService service) {
        this.service = service;
    }

    /**
     * Main page with analysis form and results
     */
    @GetMapping("/")
    public String index(Model model,
                        @RequestParam(required = false) String originalDir,
                        @RequestParam(required = false) String damagedDir) {
        model.addAttribute("originalDir", originalDir != null ? originalDir : "/usr/bin");
        model.addAttribute("damagedDir", damagedDir != null ? damagedDir : "/tmp/damaged_bin");
        model.addAttribute("fileStatuses", service.getFileStatuses());
        model.addAttribute("stats", service.getAnalysisStats());
        model.addAttribute("analysisRunning", service.isAnalysisRunning());
        model.addAttribute("successMessage", null);
        return "index";
    }

    /**
     * HTML form analysis endpoint
     */
    @PostMapping("/analyze")
    public String analyze(@RequestParam("originalDir") String originalDir,
                          @RequestParam("damagedDir") String damagedDir,
                          RedirectAttributes redirectAttributes) {
        log.info("Starting analysis: originalDir={}, damagedDir={}", originalDir, damagedDir);
        service.setDirectories(originalDir, damagedDir);

        // Start analysis in background thread
        new Thread(() -> {
            try {
                service.startAnalysis();
                log.info("Analysis completed successfully");
            } catch (Exception e) {
                log.error("Analysis failed", e);
            }
        }).start();

        redirectAttributes.addFlashAttribute("successMessage", " Analysis started!");
        return "redirect:/";
    }

    /**
     * API: Start analysis (used by JavaScript)
     */
    @PostMapping("/api/v1/analyze")
    @ResponseBody
    public ResponseEntity<Map<String, String>> analyzeApi(@RequestBody Map<String, String> request) {
        try {
            String originalDir = request.get("originalDir");
            String damagedDir = request.get("damagedDir");

            if (originalDir == null || damagedDir == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "status", "error",
                        "message", "originalDir and damagedDir are required"
                ));
            }

            service.setDirectories(originalDir, damagedDir);

            // Start analysis in background thread
            new Thread(() -> {
                try {
                    service.startAnalysis();
                } catch (Exception e) {
                    log.error("API analysis failed", e);
                }
            }).start();

            return ResponseEntity.ok(Map.of(
                    "status", "started",
                    "message", "Analysis started successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * API: Get analysis results and status
     */
    @GetMapping("/api/v1/results")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getResults() {
        try {
            return ResponseEntity.ok(Map.of(
                    "status", service.isAnalysisRunning() ? "running" : "ready",
                    "fileStatuses", service.getFileStatuses(), // ✅ Уже содержит damages
                    "stats", service.getAnalysisStats(),
                    "analysisRunning", service.isAnalysisRunning()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * API: Get detailed damages for specific file
     */
    @GetMapping("/api/v1/details/{filename}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getFileDetails(@PathVariable String filename) {
        try {
            List<FileDamageDetailDto> damages = service.getFileDamages(filename);
            if (damages == null || damages.isEmpty()) {
                return ResponseEntity.ok(Map.of(
                        "status", "empty",
                        "fileName", filename,
                        "damages", Collections.emptyList(),
                        "count", 0
                ));
            }
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "fileName", filename,
                    "damages", damages,
                    "count", damages.size()
            ));
        } catch (Exception e) {
            log.error("Error getting details for: {}", filename, e);
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "fileName", filename,
                    "message", e.getMessage()
            ));
        }
    }

}
