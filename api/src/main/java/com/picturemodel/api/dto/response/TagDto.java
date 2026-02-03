/**
 * App: Picture Model
 * Package: com.picturemodel.api.dto.response
 * File: TagDto.java
 * Version: 0.1.0
 * Turns: 15
 * Author: codex
 * Date: 2026-02-03T04:53:58Z
 * Exports: TagDto
 * Description: Response DTO for tags associated with images.
 */

package com.picturemodel.api.dto.response;

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
public class TagDto {

    private UUID id;

    private String name;

    private String color;

    private Integer usageCount;

    private LocalDateTime createdDate;
}

