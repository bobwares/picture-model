/**
 * App: Picture Model
 * Package: com.picturemodel.infrastructure.filesystem
 * File: FileSystemProviderFactory.java
 * Version: 0.1.2
 * Turns: 6,20
 * Author: Bobwares (bobwares@outlook.com)
 * Date: 2026-02-03T08:07:08Z
 * Exports: FileSystemProviderFactory
 * Description: class FileSystemProviderFactory for FileSystemProviderFactory responsibilities. Methods: createProvider - create provider; getStringValue - get string value; getIntValue - get int value; extractHostFromUrl - extract host from url.
 */

package com.picturemodel.infrastructure.filesystem;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.picturemodel.domain.entity.RemoteFileDrive;
import com.picturemodel.domain.enums.DriveType;
import com.picturemodel.infrastructure.security.CredentialEncryptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URI;

/**
 * Factory for creating FileSystemProvider instances based on drive type.
 * Handles credential decryption and provider instantiation.
 *
 * @author Claude (AI Coding Agent)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class FileSystemProviderFactory {

    private final CredentialEncryptionService encryptionService;
    private final ObjectMapper objectMapper;

    /**
     * Create a FileSystemProvider for the given drive.
     *
     * @param drive the RemoteFileDrive entity
     * @return configured FileSystemProvider instance
     * @throws Exception if provider creation fails
     */
    public FileSystemProvider createProvider(RemoteFileDrive drive) throws Exception {
        DriveType type = drive.getType();
        String connectionUrl = drive.getConnectionUrl();
        String rootPath = drive.getRootPath();

        // Decrypt and parse credentials
        JsonNode credentials = null;
        if (drive.getEncryptedCredentials() != null && !drive.getEncryptedCredentials().isEmpty()) {
            String decryptedJson = encryptionService.decrypt(drive.getEncryptedCredentials());
            credentials = objectMapper.readTree(decryptedJson);
        }

        switch (type) {
            case LOCAL:
                String localRoot = rootPath != null && !rootPath.isBlank() ? rootPath : connectionUrl;
                return new LocalFileSystemProvider(localRoot);

            case SMB:
                String smbUsername = getStringValue(credentials, "username", "");
                String smbPassword = getStringValue(credentials, "password", "");
                String smbDomain = getStringValue(credentials, "domain", "");
                String smbUrl = buildSmbUrl(connectionUrl, rootPath);
                return new SmbFileSystemProvider(smbUrl, smbUsername, smbPassword, smbDomain);

            case SFTP:
                String sftpHost = getStringValue(credentials, "host", extractHostFromUrl(connectionUrl));
                int sftpPort = getIntValue(credentials, "port", 22);
                String sftpUsername = getStringValue(credentials, "username", "");
                String sftpPassword = getStringValue(credentials, "password", "");
                return new SftpFileSystemProvider(sftpHost, sftpPort, sftpUsername, sftpPassword, rootPath);

            case FTP:
                String ftpHost = getStringValue(credentials, "host", extractHostFromUrl(connectionUrl));
                int ftpPort = getIntValue(credentials, "port", 21);
                String ftpUsername = getStringValue(credentials, "username", "anonymous");
                String ftpPassword = getStringValue(credentials, "password", "");
                return new FtpFileSystemProvider(ftpHost, ftpPort, ftpUsername, ftpPassword, rootPath);

            default:
                throw new IllegalArgumentException("Unsupported drive type: " + type);
        }
    }

    private String getStringValue(JsonNode credentials, String field, String defaultValue) {
        if (credentials != null && credentials.has(field)) {
            return credentials.get(field).asText();
        }
        return defaultValue;
    }

    private int getIntValue(JsonNode credentials, String field, int defaultValue) {
        if (credentials != null && credentials.has(field)) {
            return credentials.get(field).asInt();
        }
        return defaultValue;
    }

    private String extractHostFromUrl(String url) {
        // Simple URL parsing for sftp://host:port/path or ftp://host:port/path
        try {
            if (url.contains("://")) {
                String[] parts = url.split("://")[1].split("/")[0].split(":");
                return parts[0];
            }
        } catch (Exception e) {
            log.warn("Failed to extract host from URL: {}", url, e);
        }
        return "localhost";
    }

    private String buildSmbUrl(String connectionUrl, String rootPath) {
        if (connectionUrl == null) {
            return null;
        }
        String baseUrl = connectionUrl.endsWith("/") ? connectionUrl : connectionUrl + "/";
        if (rootPath == null || rootPath.isBlank()) {
            return baseUrl;
        }
        String normalizedRoot = trimSeparators(rootPath);
        if (normalizedRoot.isEmpty()) {
            return baseUrl;
        }

        try {
            URI uri = new URI(baseUrl);
            String path = uri.getPath();
            String normalizedPath = trimSeparators(path);
            if (!normalizedPath.isEmpty()) {
                String lowerPath = normalizedPath.toLowerCase();
                String lowerRoot = normalizedRoot.toLowerCase();
                if (lowerPath.equals(lowerRoot) || lowerPath.endsWith("/" + lowerRoot)) {
                    return baseUrl;
                }
            }
        } catch (Exception e) {
            log.warn("Failed to parse SMB URL for root path merge: {}", connectionUrl, e);
        }

        return baseUrl + normalizedRoot;
    }

    private String trimSeparators(String path) {
        if (path == null || path.isBlank()) {
            return "";
        }
        String normalized = path.trim().replace('\\', '/');
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }
}
