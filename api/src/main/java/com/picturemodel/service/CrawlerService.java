/**
 * App: Picture Model
 * Package: com.picturemodel.service
 * File: CrawlerService.java
 * Version: 0.1.3
 * Turns: 5,10,24
 * Author: Bobwares (bobwares@outlook.com)
 * Date: 2026-02-02T02:17:16Z
 * Exports: CrawlerService
 * Description: Service for managing crawl job lifecycle and scheduling.
 * CrawlerService - creates crawl jobs, starts execution, and handles cancellation.
 */

package com.picturemodel.service;

import com.picturemodel.api.dto.request.StartCrawlRequest;
import com.picturemodel.domain.entity.CrawlJob;
import com.picturemodel.domain.entity.RemoteFileDrive;
import com.picturemodel.domain.enums.CrawlStatus;
import com.picturemodel.domain.repository.CrawlJobRepository;
import com.picturemodel.domain.repository.RemoteFileDriveRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CrawlerService {

    private final CrawlJobRepository crawlJobRepository;
    private final RemoteFileDriveRepository driveRepository;
    private final CrawlerJobRunner crawlerJobRunner;

    public CrawlJob startCrawl(StartCrawlRequest request) {
        RemoteFileDrive drive = driveRepository.findById(request.getDriveId())
                .orElseThrow(() -> new IllegalArgumentException("Drive not found: " + request.getDriveId()));

        String rootPath = request.getRootPath();
        if (rootPath == null || rootPath.isBlank()) {
            rootPath = "";
        }

        CrawlJob job = CrawlJob.builder()
                .drive(drive)
                .rootPath(rootPath)
                .status(CrawlStatus.PENDING)
                .startTime(LocalDateTime.now())
                .isIncremental(Boolean.TRUE.equals(request.getIsIncremental()))
                .build();

        CrawlJob saved = crawlJobRepository.save(job);
        boolean extractExif = Boolean.TRUE.equals(request.getExtractExif());
        crawlerJobRunner.runJob(saved.getId(), extractExif);
        return saved;
    }

    public CrawlJob cancelJob(UUID jobId) {
        CrawlJob job = crawlJobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Crawl job not found: " + jobId));

        if (job.getStatus() == CrawlStatus.COMPLETED || job.getStatus() == CrawlStatus.FAILED) {
            return job;
        }

        job.setStatus(CrawlStatus.CANCELLED);
        job.setEndTime(LocalDateTime.now());
        crawlJobRepository.save(job);
        crawlerJobRunner.requestCancel(jobId);
        return job;
    }

    @Transactional
    public void clearDriveHistory(UUID driveId) {
        crawlJobRepository.deleteByDrive_Id(driveId);
    }
}
