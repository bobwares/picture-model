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

import com.picturemodel.domain.enums.ConnectionStatus;
import com.picturemodel.domain.enums.CrawlStatus;
import com.picturemodel.service.ReactiveRepositoryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * REST controller for system status operations (WebFlux).
 * Base path: /api/system
 */
@RestController
@RequestMapping("/api/system")
@RequiredArgsConstructor
@Slf4j
public class SystemController {

    private final ReactiveRepositoryWrapper repositoryWrapper;

    /**
     * Get system status with statistics.
     * GET /api/system/status
     */
    @GetMapping("/status")
    public Mono<ResponseEntity<Map<String, Object>>> getSystemStatus() {
        log.debug("Getting system status");

        return Mono.zip(
                repositoryWrapper.countDrives(),
                repositoryWrapper.countDrivesByStatus(ConnectionStatus.CONNECTED),
                repositoryWrapper.countAllImages(),
                repositoryWrapper.countAllTags(),
                repositoryWrapper.countCrawlJobsByStatus(CrawlStatus.IN_PROGRESS)
        ).map(tuple -> {
            Map<String, Object> status = new HashMap<>();
            status.put("totalDrives", tuple.getT1());
            status.put("connectedDrives", tuple.getT2());
            status.put("totalImages", tuple.getT3());
            status.put("totalTags", tuple.getT4());
            status.put("activeCrawls", tuple.getT5());
            return ResponseEntity.ok(status);
        });
    }

    /**
     * Health check endpoint.
     * GET /api/system/health
     */
    @GetMapping("/health")
    public Mono<ResponseEntity<Map<String, String>>> healthCheck() {
        Map<String, String> health = new HashMap<>();
        health.put("status", "UP");
        health.put("application", "Picture Model");
        return Mono.just(ResponseEntity.ok(health));
    }
}
