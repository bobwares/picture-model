/**
 * App: Picture Model
 * Package: com.picturemodel.service
 * File: DriveService.java
 * Version: 0.1.1
 * Turns: 5
 * Author: Bobwares (bobwares@outlook.com)
 * Date: 2026-01-30T23:05:49Z
 * Exports: DriveService
 * Description: class DriveService for DriveService responsibilities. Methods: createDrive - create drive; getAllDrives - get all drives; getDrive - get drive; updateDrive - update drive; deleteDrive - delete drive; connect - connect; disconnect - disconnect; testConnection - test connection; getDirectoryTree - get directory tree; getStatus - get status.
 */

package com.picturemodel.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.picturemodel.domain.entity.RemoteFileDrive;
import com.picturemodel.domain.enums.ConnectionStatus;
import com.picturemodel.domain.repository.RemoteFileDriveRepository;
import com.picturemodel.infrastructure.filesystem.ConnectionTestResult;
import com.picturemodel.infrastructure.filesystem.DirectoryTreeNode;
import com.picturemodel.infrastructure.filesystem.FileSystemProvider;
import com.picturemodel.infrastructure.security.CredentialEncryptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service for managing RemoteFileDrive entities and connections.
 *
 * @author Claude (AI Coding Agent)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DriveService {

    private final RemoteFileDriveRepository driveRepository;
    private final ConnectionManager connectionManager;
    private final CredentialEncryptionService encryptionService;
    private final ObjectMapper objectMapper;

    /**
     * Create a new remote file drive.
     *
     * @param drive the drive to create
     * @param credentials optional credentials map
     * @return created drive
     */
    @Transactional
    public RemoteFileDrive createDrive(RemoteFileDrive drive, Map<String, String> credentials) {
        log.info("Creating new drive: {} ({})", drive.getName(), drive.getType());

        // Encrypt credentials if provided
        if (credentials != null && !credentials.isEmpty()) {
            try {
                String credentialsJson = objectMapper.writeValueAsString(credentials);
                String encrypted = encryptionService.encrypt(credentialsJson);
                drive.setEncryptedCredentials(encrypted);
            } catch (Exception e) {
                log.error("Error encrypting credentials", e);
                throw new RuntimeException("Failed to encrypt credentials", e);
            }
        }

        drive.setStatus(ConnectionStatus.DISCONNECTED);
        drive.setImageCount(0);

        return driveRepository.save(drive);
    }

    /**
     * Get all drives.
     *
     * @return list of all drives
     */
    public List<RemoteFileDrive> getAllDrives() {
        return driveRepository.findAll();
    }

    /**
     * Get a drive by ID.
     *
     * @param id the drive ID
     * @return the drive
     */
    public RemoteFileDrive getDrive(UUID id) {
        return driveRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Drive not found: " + id));
    }

    /**
     * Update an existing drive.
     *
     * @param id the drive ID
     * @param updateData the updated drive data
     * @param credentials optional updated credentials
     * @return updated drive
     */
    @Transactional
    public RemoteFileDrive updateDrive(UUID id, RemoteFileDrive updateData, Map<String, String> credentials) {
        log.info("Updating drive: {}", id);

        RemoteFileDrive drive = getDrive(id);

        // Update basic fields
        if (updateData.getName() != null) {
            drive.setName(updateData.getName());
        }
        if (updateData.getConnectionUrl() != null) {
            drive.setConnectionUrl(updateData.getConnectionUrl());
        }
        if (updateData.getRootPath() != null) {
            drive.setRootPath(updateData.getRootPath());
        }
        if (updateData.getAutoConnect() != null) {
            drive.setAutoConnect(updateData.getAutoConnect());
        }
        if (updateData.getAutoCrawl() != null) {
            drive.setAutoCrawl(updateData.getAutoCrawl());
        }

        // Update credentials if provided
        if (credentials != null && !credentials.isEmpty()) {
            try {
                String credentialsJson = objectMapper.writeValueAsString(credentials);
                String encrypted = encryptionService.encrypt(credentialsJson);
                drive.setEncryptedCredentials(encrypted);
            } catch (Exception e) {
                log.error("Error encrypting credentials", e);
                throw new RuntimeException("Failed to encrypt credentials", e);
            }
        }

        // If connection parameters changed, disconnect
        if (connectionManager.isConnected(id)) {
            log.info("Drive configuration changed, disconnecting: {}", drive.getName());
            connectionManager.disconnect(id);
        }

        return driveRepository.save(drive);
    }

    /**
     * Delete a drive.
     *
     * @param id the drive ID
     */
    @Transactional
    public void deleteDrive(UUID id) {
        log.info("Deleting drive: {}", id);

        // Disconnect if connected
        if (connectionManager.isConnected(id)) {
            connectionManager.disconnect(id);
        }

        driveRepository.deleteById(id);
    }

    /**
     * Connect to a drive.
     *
     * @param id the drive ID
     * @return the connected drive
     */
    @Transactional
    public RemoteFileDrive connect(UUID id) {
        log.info("Connecting to drive: {}", id);

        RemoteFileDrive drive = getDrive(id);

        try {
            connectionManager.connect(drive);
            drive.setStatus(ConnectionStatus.CONNECTED);
            drive.setLastConnected(LocalDateTime.now());
        } catch (Exception e) {
            log.error("Failed to connect to drive: {}", drive.getName(), e);
            drive.setStatus(ConnectionStatus.ERROR);
            throw new RuntimeException("Connection failed: " + e.getMessage(), e);
        }

        return driveRepository.save(drive);
    }

    /**
     * Disconnect from a drive.
     *
     * @param id the drive ID
     * @return the disconnected drive
     */
    @Transactional
    public RemoteFileDrive disconnect(UUID id) {
        log.info("Disconnecting from drive: {}", id);

        RemoteFileDrive drive = getDrive(id);
        connectionManager.disconnect(id);

        drive.setStatus(ConnectionStatus.DISCONNECTED);
        return driveRepository.save(drive);
    }

    /**
     * Test connection to a drive.
     *
     * @param id the drive ID
     * @return connection test result
     */
    public ConnectionTestResult testConnection(UUID id) {
        log.info("Testing connection to drive: {}", id);

        RemoteFileDrive drive = getDrive(id);

        try {
            FileSystemProvider provider = connectionManager.getProvider(id);
            return provider.testConnection();
        } catch (Exception e) {
            log.error("Connection test failed for drive: {}", drive.getName(), e);
            return ConnectionTestResult.failure("Connection test failed: " + e.getMessage(), 0);
        }
    }

    /**
     * Get the directory tree for a drive.
     *
     * @param id the drive ID
     * @param path the path to start from (optional, defaults to root)
     * @return directory tree
     */
    public DirectoryTreeNode getDirectoryTree(UUID id, String path) {
        log.info("Getting directory tree for drive: {}, path: {}", id, path);

        try {
            FileSystemProvider provider = connectionManager.getProvider(id);
            String searchPath = normalizeTreePath(path);
            DirectoryTreeNode tree = provider.getDirectoryTree(searchPath);
            if (tree != null && (tree.getPath() == null || tree.getPath().isEmpty())) {
                tree.setPath("/");
            }
            return tree;
        } catch (Exception e) {
            log.error("Failed to get directory tree", e);
            throw new RuntimeException("Failed to get directory tree: " + e.getMessage(), e);
        }
    }

    private String normalizeTreePath(String path) {
        if (path == null) {
            return "";
        }
        String trimmed = path.trim();
        if (trimmed.isEmpty() || "/".equals(trimmed)) {
            return "";
        }
        if (trimmed.startsWith("/")) {
            return trimmed.substring(1);
        }
        return trimmed;
    }

    /**
     * Get the current status of a drive.
     *
     * @param id the drive ID
     * @return drive with current status
     */
    public RemoteFileDrive getStatus(UUID id) {
        RemoteFileDrive drive = getDrive(id);

        // Update status based on actual connection state
        boolean isConnected = connectionManager.isConnected(id);
        if (isConnected && drive.getStatus() != ConnectionStatus.CONNECTED) {
            drive.setStatus(ConnectionStatus.CONNECTED);
            driveRepository.save(drive);
        } else if (!isConnected && drive.getStatus() == ConnectionStatus.CONNECTED) {
            drive.setStatus(ConnectionStatus.DISCONNECTED);
            driveRepository.save(drive);
        }

        return drive;
    }
}
