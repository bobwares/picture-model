/**
 * App: Picture Model
 * Package: com.picturemodel.api.controller
 * File: CrawlerController.java
 * Version: 0.1.3
 * Turns: 5,17,24
 * Author: Bobwares (bobwares@outlook.com)
 * Date: 2026-02-02T02:17:16Z
 * Exports: CrawlerController
 * Description: class CrawlerController for CrawlerController responsibilities. Methods: getAllJobs - get all jobs; getDriveJobs - get drive jobs; getJob - get job; startCrawl - start crawl; cancelJob - cancel job; clearDriveJobs - clear drive jobs.
 */

package com.picturemodel.api.controller;

import com.picturemodel.api.dto.request.StartCrawlRequest;
import com.picturemodel.domain.entity.CrawlJob;
import com.picturemodel.domain.repository.CrawlJobRepository;
import com.picturemodel.service.CrawlerService;
import jakarta.validation.Valid;
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
    private final CrawlerService crawlerService;

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
     * Get crawl jobs for a specific drive with pagination.
     * GET /api/crawler/drives/{driveId}/jobs
     */
    @GetMapping("/drives/{driveId}/jobs")
    public ResponseEntity<Map<String, Object>> getDriveJobs(
            @PathVariable UUID driveId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.debug("Getting crawl jobs for drive {} - page: {}, size: {}", driveId, page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<CrawlJob> jobsPage = crawlJobRepository.findByDrive_IdOrderByStartTimeDesc(driveId, pageable);

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
    public ResponseEntity<CrawlJob> startCrawl(@Valid @RequestBody StartCrawlRequest request) {
        log.info("Starting crawl job with request: {}", request);

        CrawlJob job = crawlerService.startCrawl(request);
        return ResponseEntity.accepted().body(job);
    }

    /**
     * Cancel a crawl job.
     * POST /api/crawler/jobs/{id}/cancel
     */
    @PostMapping("/jobs/{id}/cancel")
    public ResponseEntity<CrawlJob> cancelJob(@PathVariable UUID id) {
        log.info("Cancelling crawl job: {}", id);

        CrawlJob job = crawlerService.cancelJob(id);
        return ResponseEntity.ok(job);
    }

    /**
     * Clear crawl job history for a drive.
     * DELETE /api/crawler/drives/{driveId}/jobs
     */
    @DeleteMapping("/drives/{driveId}/jobs")
    public ResponseEntity<Void> clearDriveJobs(@PathVariable UUID driveId) {
        log.info("Clearing crawl jobs for drive: {}", driveId);

        crawlerService.clearDriveHistory(driveId);
        return ResponseEntity.noContent().build();
    }
}
