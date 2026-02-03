/**
 * App: Picture Model
 * Package: com.picturemodel.api.dto.response
 * File: ImageMetadataDto.java
 * Version: 0.1.0
 * Turns: 15
 * Author: codex
 * Date: 2026-02-03T04:53:58Z
 * Exports: ImageMetadataDto
 * Description: Response DTO for image metadata key/value entries.
 */

package com.picturemodel.api.dto.response;

import com.picturemodel.domain.enums.MetadataSource;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImageMetadataDto {

    private UUID id;

    private String key;

    private String value;

    private MetadataSource source;

    private LocalDateTime lastModified;
}

