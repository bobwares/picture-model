/**
 * App: Picture Model
 * Package: com.picturemodel.api.dto.response
 * File: RemoteFileDriveDto.java
 * Version: 0.1.0
 * Turns: 5
 * Author: Bobwares (bobwares@outlook.com)
 * Date: 2026-01-30T02:03:52Z
 * Exports: RemoteFileDriveDto
 * Description: class RemoteFileDriveDto for RemoteFileDriveDto responsibilities. Methods: none declared.
 */

package com.picturemodel.api.dto.response;

import com.picturemodel.domain.enums.ConnectionStatus;
import com.picturemodel.domain.enums.DriveType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for RemoteFileDrive.
 * Matches the frontend JSON schema for RemoteFileDrive.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RemoteFileDriveDto {

    private UUID id;

    private String name;

    private DriveType type;

    private String connectionUrl;

    private ConnectionStatus status;

    private String rootPath;

    private Boolean autoConnect;

    private Boolean autoCrawl;

    private Integer imageCount;

    private LocalDateTime lastConnected;

    private LocalDateTime lastCrawled;

    private LocalDateTime createdDate;

    private LocalDateTime modifiedDate;
}
