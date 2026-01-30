/**
 * App: Picture Model
 * Package: com.picturemodel.api.controller
 * File: SystemController.java
 * Version: 0.1.0
 * Turns: 5
 * Author: Bobwares (bobwares@outlook.com)
 * Date: 2026-01-30T02:03:52Z
 * Exports: SystemController
 * Description: class SystemController for SystemController responsibilities. Methods: getSystemStatus - get system status; healthCheck - health check.
 */

package com.picturemodel.api.controller;

import com.picturemodel.domain.repository.CrawlJobRepository;
import com.picturemodel.domain.repository.ImageRepository;
import com.picturemodel.domain.repository.RemoteFileDriveRepository;
import com.picturemodel.domain.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * REST controller for system status operations.
 * Base path: /api/system
 */
@RestController
@RequestMapping("/api/system")
@RequiredArgsConstructor
@Slf4j
public class SystemController {

    private final RemoteFileDriveRepository driveRepository;
    private final ImageRepository imageRepository;
    private final TagRepository tagRepository;
    private final CrawlJobRepository crawlJobRepository;

    /**
     * Get system status with statistics.
     * GET /api/system/status
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getSystemStatus() {
        log.debug("Getting system status");

        Map<String, Object> status = new HashMap<>();

        // Count drives
        long totalDrives = driveRepository.count();
        long connectedDrives = driveRepository.countByStatus(
            com.picturemodel.domain.enums.ConnectionStatus.CONNECTED
        );

        // Count images
        long totalImages = imageRepository.count();

        // Count tags
        long totalTags = tagRepository.count();

        // Count active crawl jobs
        long activeCrawls = crawlJobRepository.countByStatus(
            com.picturemodel.domain.enums.CrawlStatus.IN_PROGRESS
        );

        status.put("totalDrives", totalDrives);
        status.put("connectedDrives", connectedDrives);
        status.put("totalImages", totalImages);
        status.put("totalTags", totalTags);
        status.put("activeCrawls", activeCrawls);

        return ResponseEntity.ok(status);
    }

    /**
     * Health check endpoint.
     * GET /api/system/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> health = new HashMap<>();
        health.put("status", "UP");
        health.put("application", "Picture Model");
        return ResponseEntity.ok(health);
    }
}
