/**
 * App: Picture Model
 * Package: com.picturemodel.api.dto.response
 * File: ImageDto.java
 * Version: 0.1.0
 * Turns: 15
 * Author: codex
 * Date: 2026-02-03T04:53:58Z
 * Exports: ImageDto
 * Description: Response DTO for images. Supports summary responses (no metadata/tags) and detail responses (with metadata/tags).
 */

package com.picturemodel.api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ImageDto {

    private UUID id;

    private UUID driveId;

    private String driveName;

    private String fileName;

    private String filePath;

    private String fullPath;

    private Long fileSize;

    private String fileHash;

    private String mimeType;

    private Integer width;

    private Integer height;

    private String imageUrl;

    private String thumbnailUrl;

    private LocalDateTime capturedAt;

    private LocalDateTime createdDate;

    private LocalDateTime modifiedDate;

    private LocalDateTime indexedDate;

    private List<ImageMetadataDto> metadata;

    private List<TagDto> tags;
}

