/**
 * App: Picture Model
 * Package: com.picturemodel.api.dto.request
 * File: StartCrawlRequest.java
 * Version: 0.1.0
 * Turns: 5
 * Author: Bobwares (bobwares@outlook.com)
 * Date: 2026-01-30T22:31:58Z
 * Exports: StartCrawlRequest
 * Description: Request DTO for starting a crawl job.
 * StartCrawlRequest - holds crawl options and target drive ID.
 */

package com.picturemodel.api.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Request DTO for starting a crawl job.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StartCrawlRequest {

    @NotNull(message = "Drive ID is required")
    private UUID driveId;

    /**
     * Optional sub-path to crawl. Defaults to drive root.
     */
    private String rootPath;

    /**
     * If true, only process files changed since last crawl.
     */
    private Boolean isIncremental;

    /**
     * Placeholder for EXIF extraction toggle.
     */
    private Boolean extractExif;

    /**
     * Placeholder for thumbnail generation toggle.
     */
    private Boolean generateThumbnails;
}
