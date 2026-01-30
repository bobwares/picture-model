/**
 * App: Picture Model
 * Package: com.picturemodel.infrastructure.filesystem
 * File: SmbFileSystemProvider.java
 * Version: 0.1.0
 * Turns: 5
 * Author: Bobwares (bobwares@outlook.com)
 * Date: 2026-01-30T02:03:52Z
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
        this.connectionUrl = connectionUrl;
        this.username = username;
        this.password = password;
        this.domain = domain != null ? domain : "";
    }

    @Override
    public void connect() throws Exception {
        Properties props = new Properties();
        props.setProperty("jcifs.smb.client.minVersion", "SMB202");
        props.setProperty("jcifs.smb.client.maxVersion", "SMB311");

        PropertyConfiguration config = new PropertyConfiguration(props);
        BaseContext baseContext = new BaseContext(config);

        NtlmPasswordAuthenticator auth = new NtlmPasswordAuthenticator(domain, username, password);
        cifsContext = baseContext.withCredentials(auth);

        // Test connection
        SmbFile testFile = new SmbFile(connectionUrl, cifsContext);
        testFile.exists(); // Throws exception if connection fails

        connected = true;
        log.info("Connected to SMB share: {}", connectionUrl);
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

        String fullUrl = connectionUrl + (path.startsWith("/") ? path : "/" + path);
        SmbFile directory = new SmbFile(fullUrl, cifsContext);

        List<FileInfo> fileInfos = new ArrayList<>();
        for (SmbFile file : directory.listFiles()) {
            fileInfos.add(createFileInfo(file));
        }

        return fileInfos;
    }

    @Override
    public DirectoryTreeNode getDirectoryTree(String path) throws Exception {
        if (!isConnected()) {
            throw new IllegalStateException("Not connected to SMB share");
        }

        String fullUrl = connectionUrl + (path.startsWith("/") ? path : "/" + path);
        SmbFile directory = new SmbFile(fullUrl, cifsContext);
        return buildDirectoryTree(directory, path);
    }

    private DirectoryTreeNode buildDirectoryTree(SmbFile directory, String relativePath) {
        DirectoryTreeNode node = DirectoryTreeNode.builder()
                .name(directory.getName().replace("/", ""))
                .path(relativePath)
                .build();

        try {
            for (SmbFile file : directory.listFiles()) {
                if (file.isDirectory()) {
                    String childPath = relativePath + "/" + file.getName().replace("/", "");
                    DirectoryTreeNode child = buildDirectoryTree(file, childPath);
                    node.getChildren().add(child);
                    node.setTotalImageCount(node.getTotalImageCount() + child.getTotalImageCount());
                } else if (isImageFile(file)) {
                    node.setImageCount(node.getImageCount() + 1);
                    node.setTotalImageCount(node.getTotalImageCount() + 1);
                }
            }
        } catch (Exception e) {
            log.warn("Error reading SMB directory: {}", directory.getPath(), e);
        }

        return node;
    }

    @Override
    public InputStream readFile(String path) throws Exception {
        if (!isConnected()) {
            throw new IllegalStateException("Not connected to SMB share");
        }

        String fullUrl = connectionUrl + (path.startsWith("/") ? path : "/" + path);
        SmbFile file = new SmbFile(fullUrl, cifsContext);
        return file.getInputStream();
    }

    @Override
    public FileInfo getFileMetadata(String path) throws Exception {
        if (!isConnected()) {
            throw new IllegalStateException("Not connected to SMB share");
        }

        String fullUrl = connectionUrl + (path.startsWith("/") ? path : "/" + path);
        SmbFile file = new SmbFile(fullUrl, cifsContext);
        return createFileInfo(file);
    }

    @Override
    public boolean fileExists(String path) {
        try {
            String fullUrl = connectionUrl + (path.startsWith("/") ? path : "/" + path);
            SmbFile file = new SmbFile(fullUrl, cifsContext);
            return file.exists();
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public ConnectionTestResult testConnection() {
        long startTime = System.currentTimeMillis();
        try {
            SmbFile testFile = new SmbFile(connectionUrl, cifsContext);
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

    private String guessContentType(String fileName) {
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".gif")) return "image/gif";
        if (lower.endsWith(".bmp")) return "image/bmp";
        if (lower.endsWith(".webp")) return "image/webp";
        if (lower.endsWith(".tiff") || lower.endsWith(".tif")) return "image/tiff";
        return null;
    }
}
