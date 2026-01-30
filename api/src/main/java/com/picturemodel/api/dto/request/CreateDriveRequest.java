/**
 * App: Picture Model
 * Package: com.picturemodel.api.dto.request
 * File: CreateDriveRequest.java
 * Version: 0.1.0
 * Turns: 5
 * Author: Bobwares (bobwares@outlook.com)
 * Date: 2026-01-30T02:03:52Z
 * Exports: CreateDriveRequest
 * Description: class CreateDriveRequest for CreateDriveRequest responsibilities. Methods: none declared.
 */

package com.picturemodel.api.dto.request;

import com.picturemodel.domain.enums.DriveType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating a new RemoteFileDrive.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateDriveRequest {

    @NotBlank(message = "Drive name is required")
    @Size(max = 200, message = "Drive name must not exceed 200 characters")
    private String name;

    @NotNull(message = "Drive type is required")
    private DriveType type;

    @NotBlank(message = "Connection URL is required")
    @Size(max = 500, message = "Connection URL must not exceed 500 characters")
    private String connectionUrl;

    /**
     * JSON string containing credentials (username, password, etc.)
     * Will be encrypted before storage.
     */
    private String credentials;

    @Size(max = 500, message = "Root path must not exceed 500 characters")
    private String rootPath;

    private Boolean autoConnect;

    private Boolean autoCrawl;
}
