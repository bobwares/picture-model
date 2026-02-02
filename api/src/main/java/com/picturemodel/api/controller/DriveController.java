/**
 * App: Picture Model
 * Package: com.picturemodel.api.controller
 * File: DriveController.java
 * Version: 0.1.3
 * Turns: 5,15
 * Author: Bobwares (bobwares@outlook.com)
 * Date: 2026-02-02T19:01:20Z
 * Exports: DriveController
 * Description: class DriveController for DriveController responsibilities. Methods: createDrive - create drive; getAllDrives - get all drives; getDrive - get drive; deleteDrive - delete drive; connect - connect; disconnect - disconnect; getStatus - get status; testConnection - test connection; parseCredentials - parse credentials; resolveSort - resolve sort param.
 */

package com.picturemodel.api.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.picturemodel.api.dto.request.CreateDriveRequest;
import com.picturemodel.api.dto.request.UpdateDriveRequest;
import com.picturemodel.api.dto.response.RemoteFileDriveDto;
import com.picturemodel.api.mapper.DtoMapper;
import com.picturemodel.domain.entity.Image;
import com.picturemodel.domain.entity.RemoteFileDrive;
import com.picturemodel.domain.repository.ImageRepository;
import com.picturemodel.infrastructure.filesystem.ConnectionTestResult;
import com.picturemodel.infrastructure.filesystem.DirectoryTreeNode;
import com.picturemodel.service.DriveService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
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
    private final ImageRepository imageRepository;
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
     * Get images for a drive and optional path filter.
     * GET /api/drives/{id}/images
     */
    @GetMapping("/{id}/images")
    public ResponseEntity<Map<String, Object>> getDriveImages(
            @PathVariable UUID id,
            @RequestParam(required = false) String path,
            @RequestParam(defaultValue = "date") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "24") int size) {
        log.info("Getting images for drive: {}, path: {}, sort: {}", id, path, sort);

        Pageable pageable = PageRequest.of(page, size, resolveSort(sort));
        String normalizedPath = normalizePath(path);

        // Use new query that only returns images in the specific directory (not subdirectories)
        // normalizedPath is null for root directory, so we use empty string
        String directoryPath = normalizedPath != null ? normalizedPath : "";
        log.debug("Querying images in directory: '{}' (root={})", directoryPath, normalizedPath == null);

        Page<Image> imagesPage = imageRepository.findByDrive_IdAndDirectoryAndDeletedFalse(
                id,
                directoryPath,
                pageable
        );

        Map<String, Object> response = new HashMap<>();
        response.put("content", imagesPage.getContent());
        response.put("totalElements", imagesPage.getTotalElements());
        response.put("totalPages", imagesPage.getTotalPages());
        response.put("currentPage", imagesPage.getNumber());
        response.put("size", imagesPage.getSize());
        response.put("last", imagesPage.isLast());

        return ResponseEntity.ok(response);
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

    private Sort resolveSort(String sort) {
        if (sort == null) {
            return Sort.by(Sort.Direction.DESC, "modifiedDate");
        }
        String trimmed = sort.trim();
        String[] parts = trimmed.split("[,:]", 2);
        String sortField = parts[0].trim();
        String directionToken = parts.length > 1 ? parts[1].trim() : "";

        Sort.Direction direction = null;
        if (!directionToken.isEmpty()) {
            try {
                direction = Sort.Direction.fromString(directionToken);
            } catch (IllegalArgumentException ignored) {
                direction = null;
            }
        }

        switch (sortField) {
            case "name":
                return Sort.by(direction != null ? direction : Sort.Direction.ASC, "fileName");
            case "size":
                return Sort.by(direction != null ? direction : Sort.Direction.DESC, "fileSize");
            case "date":
            default:
                return Sort.by(direction != null ? direction : Sort.Direction.DESC, "modifiedDate");
        }
    }

    private String normalizePath(String path) {
        if (path == null) {
            return null;
        }
        String trimmed = path.trim();
        if (trimmed.isEmpty() || "/".equals(trimmed)) {
            return null;
        }
        if (trimmed.startsWith("/")) {
            trimmed = trimmed.substring(1);
        }
        if (!trimmed.endsWith("/")) {
            trimmed = trimmed + "/";
        }
        return trimmed;
    }
}
