/**
 * App: Picture Model
 * Package: com.picturemodel.infrastructure.filesystem
 * File: SmbFileSystemProvider.java
 * Version: 0.1.3
 * Turns: 5,9,23,27
 * Author: Bobwares (bobwares@outlook.com)
 * Date: 2026-02-02T20:38:20Z
 * Exports: SmbFileSystemProvider
 * Description: class SmbFileSystemProvider for SmbFileSystemProvider responsibilities. Methods: SmbFileSystemProvider - constructor; connect - connect; disconnect - disconnect; isConnected - is connected; listDirectory - list directory; getDirectoryTree - get directory tree; buildDirectoryTree - build directory tree; readFile - read file; getFileMetadata - get file metadata; fileExists - file exists; testConnection - test connection; createFileInfo - create file info; isImageFile - is image file; guessContentType - guess content type.
 */

package com.picturemodel.infrastructure.filesystem;

import jcifs.CIFSContext;
import jcifs.smb.NtlmPasswordAuthenticator;
import jcifs.smb.SmbFile;
import jcifs.config.PropertyConfiguration;
import jcifs.context.BaseContext;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * File system provider for SMB/CIFS network shares using jCIFS-ng.
 *
 * @author Claude (AI Coding Agent)
 */
@Slf4j
public class SmbFileSystemProvider implements FileSystemProvider {

    private final String connectionUrl;
    private final String username;
    private final String password;
    private final String domain;
    private CIFSContext cifsContext;
    private boolean connected;

    public SmbFileSystemProvider(String connectionUrl, String username, String password, String domain) {
        // Ensure connection URL ends with trailing slash for proper path concatenation
        this.connectionUrl = connectionUrl != null && !connectionUrl.endsWith("/")
                ? connectionUrl + "/"
                : connectionUrl;
        this.username = username;
        this.password = password;
        this.domain = domain != null && !domain.isEmpty() ? domain : "";

        log.debug("Initialized SmbFileSystemProvider - URL: {}, username: {}, domain: '{}'",
                this.connectionUrl, username, this.domain);
    }

    @Override
    public void connect() throws Exception {
        log.info("Attempting SMB connection to: {}", connectionUrl);
        log.debug("SMB credentials - username: {}, domain: {}, password: {}",
                username,
                domain,
                password != null && !password.isEmpty() ? "***" : "[empty]");

        // Validate connection URL format
        if (!connectionUrl.startsWith("smb://")) {
            throw new IllegalArgumentException("SMB connection URL must start with 'smb://'");
        }

        if (!connectionUrl.endsWith("/")) {
            log.warn("SMB URL does not end with '/'. This may cause connection issues: {}", connectionUrl);
        }

        // Validate credentials
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("SMB username is required");
        }

        if (password == null) {
            log.warn("SMB password is null. Connection may fail if authentication is required.");
        }

        Properties props = new Properties();
        // More lenient SMB version range for better compatibility
        props.setProperty("jcifs.smb.client.minVersion", "SMB1");
        props.setProperty("jcifs.smb.client.maxVersion", "SMB311");
        props.setProperty("jcifs.smb.client.responseTimeout", "30000");
        props.setProperty("jcifs.smb.client.connTimeout", "10000");

        PropertyConfiguration config = new PropertyConfiguration(props);
        BaseContext baseContext = new BaseContext(config);

        NtlmPasswordAuthenticator auth = new NtlmPasswordAuthenticator(
                domain != null && !domain.isEmpty() ? domain : null,
                username,
                password != null ? password : ""
        );
        cifsContext = baseContext.withCredentials(auth);

        // Test connection with detailed error handling
        try {
            log.debug("Creating SmbFile object for: {}", connectionUrl);
            SmbFile testFile = new SmbFile(connectionUrl, cifsContext);

            log.debug("Testing SMB connection with exists() call...");
            boolean exists = testFile.exists();
            log.info("SMB connection test successful. Path exists: {}", exists);

            connected = true;
            log.info("Successfully connected to SMB share: {}", connectionUrl);

        } catch (jcifs.smb.SmbAuthException e) {
            log.error("SMB authentication failed for user '{}' on domain '{}': {}",
                    username, domain, e.getMessage());
            throw new Exception("SMB authentication failed. Check username, password, and domain.", e);

        } catch (jcifs.smb.SmbException e) {
            log.error("SMB protocol error (code: {}): {}", e.getNtStatus(), e.getMessage(), e);
            throw new Exception("SMB connection failed: " + e.getMessage() +
                    " (Error code: 0x" + Integer.toHexString(e.getNtStatus()) + ")", e);

        } catch (Exception e) {
            log.error("Unexpected error connecting to SMB share", e);
            throw new Exception("SMB connection failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void disconnect() {
        cifsContext = null;
        connected = false;
        log.info("Disconnected from SMB share: {}", connectionUrl);
    }

    @Override
    public boolean isConnected() {
        return connected && cifsContext != null;
    }

    @Override
    public List<FileInfo> listDirectory(String path) throws Exception {
        if (!isConnected()) {
            throw new IllegalStateException("Not connected to SMB share");
        }

        String url = buildUrl(path);
        log.debug("SMB listDirectory path '{}' -> {}", path, sanitizeUrl(url));

        try {
            SmbFile directory = new SmbFile(url, cifsContext);

            // Check if directory exists and is accessible
            if (!directory.exists()) {
                log.warn("SMB directory does not exist: {}", sanitizeUrl(url));
                return new ArrayList<>();
            }

            if (!directory.isDirectory()) {
                log.warn("SMB path is not a directory: {}", sanitizeUrl(url));
                return new ArrayList<>();
            }

            SmbFile[] files = directory.listFiles();
            if (files == null) {
                log.debug("No files in SMB directory: {}", sanitizeUrl(url));
                return new ArrayList<>();
            }

            List<FileInfo> fileInfos = new ArrayList<>();
            for (SmbFile file : files) {
                try {
                    fileInfos.add(createFileInfo(file));
                } catch (Exception e) {
                    log.debug("Error getting info for file '{}': {} - skipping", file.getName(), e.getMessage());
                }
            }

            return fileInfos;

        } catch (jcifs.smb.SmbAuthException e) {
            log.error("Access denied to SMB directory '{}': {}", sanitizeUrl(url), e.getMessage());
            throw new Exception("Access denied to directory: " + path, e);

        } catch (jcifs.smb.SmbException e) {
            log.error("SMB error accessing directory '{}' (error 0x{}): {}",
                    sanitizeUrl(url),
                    Integer.toHexString(e.getNtStatus()),
                    e.getMessage());
            throw new Exception("Cannot access directory: " + path + " (Error: " + e.getMessage() + ")", e);
        }
    }

    @Override
    public DirectoryTreeNode getDirectoryTree(String path) throws Exception {
        if (!isConnected()) {
            throw new IllegalStateException("Not connected to SMB share");
        }

        SmbFile directory = new SmbFile(buildUrl(path), cifsContext);
        return buildDirectoryTree(directory, path);
    }

    private DirectoryTreeNode buildDirectoryTree(SmbFile directory, String relativePath) {
        String dirName = directory.getName().replace("/", "");
        DirectoryTreeNode node = DirectoryTreeNode.builder()
                .name(dirName)
                .path(relativePath)
                .build();

        try {
            // Skip protected system directories
            if (isProtectedDirectory(dirName)) {
                log.debug("Skipping protected system directory: {}", dirName);
                return node;
            }

            SmbFile[] files;
            try {
                files = directory.listFiles();
            } catch (jcifs.smb.SmbAuthException e) {
                log.warn("Access denied to SMB directory '{}': {}", sanitizeUrl(directory.getPath()), e.getMessage());
                return node;
            } catch (jcifs.smb.SmbException e) {
                log.warn("Cannot access SMB directory '{}' (error 0x{}): {}",
                        sanitizeUrl(directory.getPath()),
                        Integer.toHexString(e.getNtStatus()),
                        e.getMessage());
                return node;
            }

            if (files == null) {
                log.debug("No files in directory: {}", sanitizeUrl(directory.getPath()));
                return node;
            }

            for (SmbFile file : files) {
                try {
                    if (file.isDirectory()) {
                        String childName = file.getName().replace("/", "");
                        String childPath = relativePath + "/" + childName;

                        // Skip protected directories at any level
                        if (isProtectedDirectory(childName)) {
                            log.debug("Skipping protected subdirectory: {}", childName);
                            continue;
                        }

                        DirectoryTreeNode child = buildDirectoryTree(file, childPath);
                        node.getChildren().add(child);
                        node.setTotalImageCount(node.getTotalImageCount() + child.getTotalImageCount());
                    } else if (isImageFile(file)) {
                        node.setImageCount(node.getImageCount() + 1);
                        node.setTotalImageCount(node.getTotalImageCount() + 1);
                    }
                } catch (jcifs.smb.SmbAuthException e) {
                    log.debug("Access denied to file '{}': skipping", file.getName());
                } catch (Exception e) {
                    log.debug("Error processing file '{}': {} - skipping", file.getName(), e.getMessage());
                }
            }
        } catch (Exception e) {
            log.warn("Error reading SMB directory '{}': {}", sanitizeUrl(directory.getPath()), e.getMessage());
        }

        return node;
    }

    @Override
    public InputStream readFile(String path) throws Exception {
        if (!isConnected()) {
            throw new IllegalStateException("Not connected to SMB share");
        }

        SmbFile file = new SmbFile(buildUrl(path), cifsContext);
        return file.getInputStream();
    }

    @Override
    public FileInfo getFileMetadata(String path) throws Exception {
        if (!isConnected()) {
            throw new IllegalStateException("Not connected to SMB share");
        }

        SmbFile file = new SmbFile(buildUrl(path), cifsContext);
        return createFileInfo(file);
    }

    @Override
    public boolean fileExists(String path) {
        try {
            SmbFile file = new SmbFile(buildUrl(path), cifsContext);
            return file.exists();
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public ConnectionTestResult testConnection() {
        long startTime = System.currentTimeMillis();
        try {
            SmbFile testFile = new SmbFile(buildUrl(""), cifsContext);
            testFile.exists();
            return ConnectionTestResult.success("Successfully connected to SMB share: " + connectionUrl,
                    System.currentTimeMillis() - startTime);
        } catch (Exception e) {
            return ConnectionTestResult.failure("SMB connection test failed: " + e.getMessage(),
                    System.currentTimeMillis() - startTime);
        }
    }

    private FileInfo createFileInfo(SmbFile file) throws Exception {
        return FileInfo.builder()
                .name(file.getName().replace("/", ""))
                .path(file.getPath())
                .size(file.length())
                .isDirectory(file.isDirectory())
                .lastModified(LocalDateTime.ofInstant(
                        java.time.Instant.ofEpochMilli(file.getLastModified()),
                        ZoneId.systemDefault()))
                .mimeType(file.isDirectory() ? null : guessContentType(file.getName()))
                .build();
    }

    private boolean isImageFile(SmbFile file) throws Exception {
        String contentType = guessContentType(file.getName());
        return contentType != null && contentType.startsWith("image/");
    }

    private String buildUrl(String path) {
        if (path == null) {
            return connectionUrl;
        }
        String trimmed = path.trim();
        if (trimmed.isEmpty() || "/".equals(trimmed) || "\\".equals(trimmed)) {
            return connectionUrl;
        }
        String normalized = trimmed.replace('\\', '/');
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        return connectionUrl + normalized;
    }

    private String sanitizeUrl(String url) {
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

    /**
     * Check if a directory name is a protected Windows system directory that should be skipped.
     */
    private boolean isProtectedDirectory(String dirName) {
        if (dirName == null || dirName.isEmpty()) {
            return false;
        }

        String lower = dirName.toLowerCase();
        return lower.equals("system volume information") ||
               lower.equals("$recycle.bin") ||
               lower.equals("recycler") ||
               lower.equals("$windows.~bt") ||
               lower.equals("$windows.~ws") ||
               lower.equals("recovery") ||
               lower.equals("msocache") ||
               lower.equals("perflogs") ||
               lower.startsWith("~$") ||
               lower.startsWith(".");
    }

    private String guessContentType(String fileName) {
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".gif")) return "image/gif";
        if (lower.endsWith(".bmp")) return "image/bmp";
        if (lower.endsWith(".webp")) return "image/webp";
        if (lower.endsWith(".tiff") || lower.endsWith(".tif")) return "image/tiff";
        if (lower.endsWith(".heic")) return "image/heic";
        if (lower.endsWith(".heif")) return "image/heif";
        return null;
    }
}
