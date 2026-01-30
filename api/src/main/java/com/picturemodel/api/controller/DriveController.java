/**
 * App: Picture Model
 * Package: com.picturemodel.api.controller
 * File: DriveController.java
 * Version: 0.1.0
 * Turns: 5
 * Author: Bobwares (bobwares@outlook.com)
 * Date: 2026-01-30T02:03:52Z
 * Exports: DriveController
 * Description: class DriveController for DriveController responsibilities. Methods: createDrive - create drive; getAllDrives - get all drives; getDrive - get drive; deleteDrive - delete drive; connect - connect; disconnect - disconnect; getStatus - get status; testConnection - test connection; parseCredentials - parse credentials.
 */

package com.picturemodel.api.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.picturemodel.api.dto.request.CreateDriveRequest;
import com.picturemodel.api.dto.request.UpdateDriveRequest;
import com.picturemodel.api.dto.response.RemoteFileDriveDto;
import com.picturemodel.api.mapper.DtoMapper;
import com.picturemodel.domain.entity.RemoteFileDrive;
import com.picturemodel.infrastructure.filesystem.ConnectionTestResult;
import com.picturemodel.infrastructure.filesystem.DirectoryTreeNode;
import com.picturemodel.service.DriveService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST controller for drive management operations.
 * Base path: /api/drives
 */
@RestController
@RequestMapping("/api/drives")
@RequiredArgsConstructor
@Slf4j
public class DriveController {

    private final DriveService driveService;
    private final DtoMapper dtoMapper;
    private final ObjectMapper objectMapper;

    /**
     * Create a new drive.
     * POST /api/drives
     */
    @PostMapping
    public ResponseEntity<RemoteFileDriveDto> createDrive(@Valid @RequestBody CreateDriveRequest request) {
        log.info("Creating drive: {}", request.getName());

        RemoteFileDrive drive = dtoMapper.toEntity(request);
        Map<String, String> credentials = parseCredentials(request.getCredentials());

        RemoteFileDrive created = driveService.createDrive(drive, credentials);
        return ResponseEntity.ok(dtoMapper.toDto(created));
    }

    /**
     * Get all drives.
     * GET /api/drives
     */
    @GetMapping
    public ResponseEntity<List<RemoteFileDriveDto>> getAllDrives() {
        log.info("Getting all drives");

        List<RemoteFileDriveDto> drives = driveService.getAllDrives().stream()
                .map(dtoMapper::toDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(drives);
    }

    /**
     * Get a specific drive by ID.
     * GET /api/drives/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<RemoteFileDriveDto> getDrive(@PathVariable UUID id) {
        log.info("Getting drive: {}", id);

        RemoteFileDrive drive = driveService.getDrive(id);
        return ResponseEntity.ok(dtoMapper.toDto(drive));
    }

    /**
     * Update a drive.
     * PUT /api/drives/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<RemoteFileDriveDto> updateDrive(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateDriveRequest request) {
        log.info("Updating drive: {}", id);

        RemoteFileDrive updateData = RemoteFileDrive.builder()
                .name(request.getName())
                .connectionUrl(request.getConnectionUrl())
                .rootPath(request.getRootPath())
                .autoConnect(request.getAutoConnect())
                .autoCrawl(request.getAutoCrawl())
                .build();

        Map<String, String> credentials = parseCredentials(request.getCredentials());

        RemoteFileDrive updated = driveService.updateDrive(id, updateData, credentials);
        return ResponseEntity.ok(dtoMapper.toDto(updated));
    }

    /**
     * Delete a drive.
     * DELETE /api/drives/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDrive(@PathVariable UUID id) {
        log.info("Deleting drive: {}", id);

        driveService.deleteDrive(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Connect to a drive.
     * POST /api/drives/{id}/connect
     */
    @PostMapping("/{id}/connect")
    public ResponseEntity<RemoteFileDriveDto> connect(@PathVariable UUID id) {
        log.info("Connecting to drive: {}", id);

        RemoteFileDrive drive = driveService.connect(id);
        return ResponseEntity.ok(dtoMapper.toDto(drive));
    }

    /**
     * Disconnect from a drive.
     * POST /api/drives/{id}/disconnect
     */
    @PostMapping("/{id}/disconnect")
    public ResponseEntity<RemoteFileDriveDto> disconnect(@PathVariable UUID id) {
        log.info("Disconnecting from drive: {}", id);

        RemoteFileDrive drive = driveService.disconnect(id);
        return ResponseEntity.ok(dtoMapper.toDto(drive));
    }

    /**
     * Get current status of a drive.
     * GET /api/drives/{id}/status
     */
    @GetMapping("/{id}/status")
    public ResponseEntity<RemoteFileDriveDto> getStatus(@PathVariable UUID id) {
        log.info("Getting status for drive: {}", id);

        RemoteFileDrive drive = driveService.getStatus(id);
        return ResponseEntity.ok(dtoMapper.toDto(drive));
    }

    /**
     * Test connection to a drive.
     * POST /api/drives/{id}/test
     */
    @PostMapping("/{id}/test")
    public ResponseEntity<ConnectionTestResult> testConnection(@PathVariable UUID id) {
        log.info("Testing connection for drive: {}", id);

        ConnectionTestResult result = driveService.testConnection(id);
        return ResponseEntity.ok(result);
    }

    /**
     * Get directory tree for a drive.
     * GET /api/drives/{id}/tree
     */
    @GetMapping("/{id}/tree")
    public ResponseEntity<DirectoryTreeNode> getDirectoryTree(
            @PathVariable UUID id,
            @RequestParam(required = false) String path) {
        log.info("Getting directory tree for drive: {}, path: {}", id, path);

        DirectoryTreeNode tree = driveService.getDirectoryTree(id, path);
        return ResponseEntity.ok(tree);
    }

    /**
     * Parse credentials JSON string to Map.
     */
    private Map<String, String> parseCredentials(String credentialsJson) {
        if (credentialsJson == null || credentialsJson.trim().isEmpty()) {
            return null;
        }

        try {
            return objectMapper.readValue(credentialsJson, new TypeReference<Map<String, String>>() {});
        } catch (Exception e) {
            log.error("Error parsing credentials JSON", e);
            throw new IllegalArgumentException("Invalid credentials format. Expected JSON object.", e);
        }
    }
}
