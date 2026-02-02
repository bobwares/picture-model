/**
 * App: Picture Model
 * Package: com.picturemodel.domain.repository
 * File: CrawlJobRepository.java
 * Version: 0.1.2
 * Turns: 5,17
 * Author: Bobwares (bobwares@outlook.com)
 * Date: 2026-02-01T17:12:25Z
 * Exports: CrawlJobRepository
 * Description: interface CrawlJobRepository for CrawlJobRepository responsibilities. Methods: findByDrive_IdOrderByStartTimeDesc - find by drive id order by start time desc; findByStatus - find by status; countByStatus - count by status.
 */

package com.picturemodel.domain.repository;

import com.picturemodel.domain.entity.CrawlJob;
import com.picturemodel.domain.enums.CrawlStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for CrawlJob entities.
 *
 * @author Claude (AI Coding Agent)
 */
@Repository
public interface CrawlJobRepository extends JpaRepository<CrawlJob, UUID> {

    /**
     * Find crawl jobs for a specific drive, ordered by start time (most recent first).
     */
    Page<CrawlJob> findByDrive_IdOrderByStartTimeDesc(UUID driveId, Pageable pageable);

    /**
     * Find all crawl jobs with a specific status.
     */
    List<CrawlJob> findByStatus(CrawlStatus status);

    /**
     * Count crawl jobs with a specific status.
     */
    long countByStatus(CrawlStatus status);

    /**
     * Delete all crawl jobs for a specific drive.
     */
    void deleteByDrive_Id(UUID driveId);
}
