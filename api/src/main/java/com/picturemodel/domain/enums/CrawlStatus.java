/**
 * App: Picture Model
 * Package: com.picturemodel.domain.enums
 * File: CrawlStatus.java
 * Version: 0.1.0
 * Turns: 5
 * Author: Bobwares (bobwares@outlook.com)
 * Date: 2026-01-30T02:03:52Z
 * Exports: CrawlStatus
 * Description: enum CrawlStatus for CrawlStatus responsibilities. Methods: none (enum constants only).
 */

package com.picturemodel.domain.enums;

/**
 * Status of a crawl job.
 *
 * @author Claude (AI Coding Agent)
 */
public enum CrawlStatus {
    /**
     * Job is pending and has not started yet
     */
    PENDING,

    /**
     * Job is currently running
     */
    IN_PROGRESS,

    /**
     * Job has been paused (can be resumed)
     */
    PAUSED,

    /**
     * Job completed successfully
     */
    COMPLETED,

    /**
     * Job failed due to an error
     */
    FAILED,

    /**
     * Job was cancelled by user
     */
    CANCELLED
}
