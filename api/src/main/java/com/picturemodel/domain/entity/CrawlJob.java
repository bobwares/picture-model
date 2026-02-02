/**
 * App: Picture Model
 * Package: com.picturemodel.domain.entity
 * File: CrawlJob.java
 * Version: 0.1.3
 * Turns: 5,16,17
 * Author: Bobwares (bobwares@outlook.com)
 * Date: 2026-02-01T17:13:10Z
 * Exports: CrawlJob
 * Description: class CrawlJob for CrawlJob responsibilities. Methods: onCreate - on create; getDurationSeconds - get duration seconds; getProgressPercentage - get progress percentage.
 */

package com.picturemodel.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.picturemodel.domain.enums.CrawlStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * CrawlJob entity representing a file system crawl operation.
 *
 * @author Claude (AI Coding Agent)
 */
@Entity
@Table(name = "crawl_jobs", indexes = {
        @Index(name = "idx_crawl_drive_id", columnList = "drive_id"),
        @Index(name = "idx_crawl_status", columnList = "status"),
        @Index(name = "idx_crawl_start_time", columnList = "startTime")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrawlJob {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "drive_id", nullable = false)
    @JsonIgnore
    private RemoteFileDrive drive;

    @Column(nullable = false, length = 2000)
    private String rootPath;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private CrawlStatus status = CrawlStatus.PENDING;

    @Column(nullable = false)
    private LocalDateTime startTime;

    private LocalDateTime endTime;

    @Column(nullable = false)
    @Builder.Default
    private Integer filesProcessed = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer filesAdded = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer filesUpdated = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer filesDeleted = 0;

    @Column(length = 2000)
    private String currentPathValue;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isIncremental = false;

    @Column(columnDefinition = "TEXT")
    private String errors; // JSON array of error messages

    @PrePersist
    protected void onCreate() {
        if (startTime == null) {
            startTime = LocalDateTime.now();
        }
    }

    /**
     * Calculate the duration of the crawl job in seconds.
     */
    @Transient
    public Long getDurationSeconds() {
        if (startTime == null) {
            return 0L;
        }
        LocalDateTime end = endTime != null ? endTime : LocalDateTime.now();
        return java.time.Duration.between(startTime, end).getSeconds();
    }

    /**
     * Calculate progress percentage (0-100).
     */
    @Transient
    public Double getProgressPercentage() {
        // This is a simple calculation; can be enhanced with estimated total files
        if (filesProcessed == 0) {
            return 0.0;
        }
        // For now, return a simple metric based on processed files
        // In real implementation, would need total files estimate
        return Math.min(100.0, (filesProcessed / 10.0)); // Placeholder logic
    }

    /**
     * Expose drive ID without serializing the full drive entity.
     */
    @Transient
    public UUID getDriveId() {
        return drive != null ? drive.getId() : null;
    }

    /**
     * Expose drive name without serializing the full drive entity.
     */
    @Transient
    public String getDriveName() {
        return drive != null ? drive.getName() : null;
    }

    /**
     * Expose current path for API clients using a stable field name.
     */
    @Transient
    public String getCurrentPath() {
        return currentPathValue;
    }
}
