/**
 * App: Picture Model
 * Package: com.picturemodel.infrastructure.filesystem
 * File: FileSystemProviderFactory.java
 * Version: 0.1.0
 * Turns: 5
 * Author: Bobwares (bobwares@outlook.com)
 * Date: 2026-01-30T02:03:52Z
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
                return new LocalFileSystemProvider(connectionUrl);

            case SMB:
                String smbUsername = getStringValue(credentials, "username", "");
                String smbPassword = getStringValue(credentials, "password", "");
                String smbDomain = getStringValue(credentials, "domain", "");
                return new SmbFileSystemProvider(connectionUrl, smbUsername, smbPassword, smbDomain);

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
}
