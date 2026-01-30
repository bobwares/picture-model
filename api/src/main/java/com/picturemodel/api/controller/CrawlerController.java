/**
 * App: Picture Model
 * Package: com.picturemodel.api.controller
 * File: CrawlerController.java
 * Version: 0.1.0
 * Turns: 5
 * Author: Bobwares (bobwares@outlook.com)
 * Date: 2026-01-30T02:03:52Z
 * Exports: CrawlerController
 * Description: class CrawlerController for CrawlerController responsibilities. Methods: getJob - get job; startCrawl - start crawl; cancelJob - cancel job.
 */

package com.picturemodel.api.controller;

import com.picturemodel.domain.entity.CrawlJob;
import com.picturemodel.domain.repository.CrawlJobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * REST controller for crawler/crawl job operations.
 * Base path: /api/crawler
 */
@RestController
@RequestMapping("/api/crawler")
@RequiredArgsConstructor
@Slf4j
public class CrawlerController {

    private final CrawlJobRepository crawlJobRepository;

    /**
     * Get all crawl jobs with pagination.
     * GET /api/crawler/jobs
     */
    @GetMapping("/jobs")
    public ResponseEntity<Map<String, Object>> getAllJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.debug("Getting all crawl jobs - page: {}, size: {}", page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("startTime").descending());
        Page<CrawlJob> jobsPage = crawlJobRepository.findAll(pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("content", jobsPage.getContent());
        response.put("totalElements", jobsPage.getTotalElements());
        response.put("totalPages", jobsPage.getTotalPages());
        response.put("currentPage", jobsPage.getNumber());
        response.put("size", jobsPage.getSize());

        return ResponseEntity.ok(response);
    }

    /**
     * Get a specific crawl job by ID.
     * GET /api/crawler/jobs/{id}
     */
    @GetMapping("/jobs/{id}")
    public ResponseEntity<CrawlJob> getJob(@PathVariable UUID id) {
        log.debug("Getting crawl job: {}", id);

        return crawlJobRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Start a new crawl job.
     * POST /api/crawler/start
     */
    @PostMapping("/start")
    public ResponseEntity<Map<String, String>> startCrawl(@RequestBody Map<String, Object> request) {
        log.info("Starting crawl job with request: {}", request);

        // TODO: Implement actual crawl job starting logic
        // For now, return a placeholder response
        Map<String, String> response = new HashMap<>();
        response.put("message", "Crawl job start endpoint - implementation pending");
        response.put("status", "NOT_IMPLEMENTED");

        return ResponseEntity.accepted().body(response);
    }

    /**
     * Cancel a crawl job.
     * POST /api/crawler/jobs/{id}/cancel
     */
    @PostMapping("/jobs/{id}/cancel")
    public ResponseEntity<Map<String, String>> cancelJob(@PathVariable UUID id) {
        log.info("Cancelling crawl job: {}", id);

        // TODO: Implement actual crawl job cancellation logic
        Map<String, String> response = new HashMap<>();
        response.put("message", "Crawl job cancellation - implementation pending");
        response.put("status", "NOT_IMPLEMENTED");

        return ResponseEntity.accepted().body(response);
    }
}
