/**
 * App: Picture Model
 * Package: com.picturemodel.service
 * File: ConnectionManager.java
 * Version: 0.1.1
 * Turns: 5,24
 * Author: Bobwares (bobwares@outlook.com)
 * Date: 2026-02-02T19:53:10Z
 * Exports: ConnectionManager
 * Description:
 * class ConnectionManager for ConnectionManager responsibilities.
 * Methods:
 * connect - connect;
 * disconnect - disconnect; getProvider - get provider;
 * isConnected - is connected;
 * performHealthCheck - perform health check; shutdown -
 * shutdown; getActiveConnectionCount - get active connection count.
 */

package com.picturemodel.service;

import com.picturemodel.domain.entity.RemoteFileDrive;
import com.picturemodel.domain.enums.ConnectionStatus;
import com.picturemodel.domain.repository.RemoteFileDriveRepository;
import com.picturemodel.infrastructure.filesystem.FileSystemProvider;
import com.picturemodel.infrastructure.filesystem.FileSystemProviderFactory;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.net.URI;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages file system provider connections with health checks and connection pooling.
 *
 * @author Claude (AI Coding Agent)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ConnectionManager {

    private final FileSystemProviderFactory providerFactory;
    private final RemoteFileDriveRepository driveRepository;

    // Cache of active file system providers
    private final Map<UUID, FileSystemProvider> providerCache = new ConcurrentHashMap<>();

    /**
     * Connect to a drive and cache the provider.
     *
     * @param drive the RemoteFileDrive to connect to
     * @return connected FileSystemProvider
     * @throws Exception if connection fails
     */
    public FileSystemProvider connect(RemoteFileDrive drive) throws Exception {
        UUID driveId = drive.getId();

        // Check if already connected
        if (providerCache.containsKey(driveId)) {
            FileSystemProvider existing = providerCache.get(driveId);
            if (existing.isConnected()) {
                log.debug("Using existing connection for drive: {}", drive.getName());
                return existing;
            } else {
                // Remove stale connection
                providerCache.remove(driveId);
            }
        }

        log.info(
                "Connecting to drive: {} ({}) at {}",
                drive.getName(),
                drive.getType(),
                sanitizeConnectionUrl(drive.getConnectionUrl())
        );

        // Update status to CONNECTING
        drive.setStatus(ConnectionStatus.CONNECTING);
        driveRepository.save(drive);

        try {
            // Create and connect provider
            FileSystemProvider provider = providerFactory.createProvider(drive);
            provider.connect();

            // Cache the provider
            providerCache.put(driveId, provider);

            // Update drive status
            drive.setStatus(ConnectionStatus.CONNECTED);
            drive.setLastConnected(LocalDateTime.now());
            driveRepository.save(drive);

            log.info("Successfully connected to drive: {}", drive.getName());
            return provider;

        } catch (Exception e) {
            log.error("Failed to connect to drive: {}", drive.getName(), e);
            drive.setStatus(ConnectionStatus.ERROR);
            driveRepository.save(drive);
            throw e;
        }
    }

    /**
     * Disconnect from a drive.
     *
     * @param driveId the drive ID to disconnect
     */
    public void disconnect(UUID driveId) {
        FileSystemProvider provider = providerCache.remove(driveId);
        if (provider != null) {
            try {
                provider.disconnect();
                log.info("Disconnected from drive: {}", driveId);

                // Update drive status
                driveRepository.findById(driveId).ifPresent(drive -> {
                    drive.setStatus(ConnectionStatus.DISCONNECTED);
                    driveRepository.save(drive);
                });
            } catch (Exception e) {
                log.error("Error disconnecting from drive: {}", driveId, e);
            }
        }
    }

    /**
     * Get the provider for a drive, connecting if necessary.
     *
     * @param driveId the drive ID
     * @return FileSystemProvider instance
     * @throws Exception if connection fails
     */
    public FileSystemProvider getProvider(UUID driveId) throws Exception {
        FileSystemProvider provider = providerCache.get(driveId);

        if (provider == null || !provider.isConnected()) {
            RemoteFileDrive drive = driveRepository.findById(driveId)
                    .orElseThrow(() -> new IllegalArgumentException("Drive not found: " + driveId));
            return connect(drive);
        }

        return provider;
    }

    /**
     * Check if a drive is currently connected.
     *
     * @param driveId the drive ID
     * @return true if connected, false otherwise
     */
    public boolean isConnected(UUID driveId) {
        FileSystemProvider provider = providerCache.get(driveId);
        return provider != null && provider.isConnected();
    }

    /**
     * Scheduled health check to verify all connections are still active.
     * Runs every 5 minutes.
     */
    @Scheduled(fixedRate = 300000) // 5 minutes
    public void performHealthCheck() {
        log.debug("Performing health check on {} active connections", providerCache.size());

        providerCache.entrySet().removeIf(entry -> {
            UUID driveId = entry.getKey();
            FileSystemProvider provider = entry.getValue();

            if (!provider.isConnected()) {
                log.warn("Drive {} is no longer connected, removing from cache", driveId);

                // Update drive status
                driveRepository.findById(driveId).ifPresent(drive -> {
                    drive.setStatus(ConnectionStatus.DISCONNECTED);
                    driveRepository.save(drive);
                });

                return true; // Remove from cache
            }

            return false; // Keep in cache
        });
    }

    /**
     * Disconnect all drives on application shutdown.
     */
    @PreDestroy
    public void shutdown() {
        log.info("Shutting down ConnectionManager, disconnecting {} drives", providerCache.size());

        providerCache.forEach((driveId, provider) -> {
            try {
                provider.disconnect();
            } catch (Exception e) {
                log.error("Error disconnecting drive {} during shutdown", driveId, e);
            }
        });

        providerCache.clear();
    }

    /**
     * Get the number of active connections.
     *
     * @return count of active connections
     */
    public int getActiveConnectionCount() {
        return providerCache.size();
    }

    private String sanitizeConnectionUrl(String url) {
        if (url == null || url.isBlank()) {
            return "[missing]";
        }
        try {
            URI uri = new URI(url);
            String userInfo = uri.getUserInfo();
            if (userInfo == null || userInfo.isBlank()) {
                return url;
            }
            String maskedUserInfo = userInfo.replaceFirst(":(.*)$", ":***");
            URI sanitized = new URI(
                    uri.getScheme(),
                    maskedUserInfo,
                    uri.getHost(),
                    uri.getPort(),
                    uri.getPath(),
                    uri.getQuery(),
                    uri.getFragment()
            );
            return sanitized.toString();
        } catch (Exception e) {
            return url.replaceAll("//([^/@:]+):([^@]+)@", "//$1:***@");
        }
    }
}
