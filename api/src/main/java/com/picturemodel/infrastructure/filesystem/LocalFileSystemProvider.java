/**
 * App: Picture Model
 * Package: com.picturemodel.infrastructure.filesystem
 * File: LocalFileSystemProvider.java
 * Version: 0.1.1
 * Turns: 5
 * Author: Bobwares (bobwares@outlook.com)
 * Date: 2026-01-30T23:05:49Z
 * Exports: LocalFileSystemProvider
 * Description: class LocalFileSystemProvider for LocalFileSystemProvider responsibilities. Methods: LocalFileSystemProvider - constructor; connect - connect; disconnect - disconnect; isConnected - is connected; listDirectory - list directory; getDirectoryTree - get directory tree; buildDirectoryTree - build directory tree; readFile - read file; getFileMetadata - get file metadata; fileExists - file exists; testConnection - test connection; createFileInfo - create file info; probeContentType - probe content type; isImageFile - is image file.
 */

package com.picturemodel.infrastructure.filesystem;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * File system provider for local file system access using Java NIO.2.
 *
 * @author Claude (AI Coding Agent)
 */
@Slf4j
public class LocalFileSystemProvider implements FileSystemProvider {

    private final String rootPath;
    private boolean connected;

    public LocalFileSystemProvider(String rootPath) {
        this.rootPath = rootPath;
        this.connected = false;
    }

    @Override
    public void connect() throws Exception {
        Path root = Paths.get(rootPath);
        if (!Files.exists(root)) {
            throw new IOException("Root path does not exist: " + rootPath);
        }
        if (!Files.isDirectory(root)) {
            throw new IOException("Root path is not a directory: " + rootPath);
        }
        connected = true;
        log.info("Connected to local file system: {}", rootPath);
    }

    @Override
    public void disconnect() {
        connected = false;
        log.info("Disconnected from local file system: {}", rootPath);
    }

    @Override
    public boolean isConnected() {
        return connected && Files.exists(Paths.get(rootPath));
    }

    @Override
    public List<FileInfo> listDirectory(String path) throws Exception {
        if (!isConnected()) {
            throw new IllegalStateException("Not connected to file system");
        }

        String normalizedPath = normalizePath(path);
        Path dirPath = Paths.get(rootPath, normalizedPath);
        List<FileInfo> fileInfos = new ArrayList<>();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dirPath)) {
            for (Path entry : stream) {
                fileInfos.add(createFileInfo(entry));
            }
        }

        return fileInfos;
    }

    @Override
    public DirectoryTreeNode getDirectoryTree(String path) throws Exception {
        if (!isConnected()) {
            throw new IllegalStateException("Not connected to file system");
        }

        String normalizedPath = normalizePath(path);
        Path dirPath = Paths.get(rootPath, normalizedPath);
        return buildDirectoryTree(dirPath, normalizedPath);
    }

    private DirectoryTreeNode buildDirectoryTree(Path dirPath, String relativePath) throws IOException {
        DirectoryTreeNode node = DirectoryTreeNode.builder()
                .name(dirPath.getFileName() != null ? dirPath.getFileName().toString() : rootPath)
                .path(relativePath.isEmpty() ? "/" : relativePath)
                .build();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dirPath)) {
            for (Path entry : stream) {
                if (Files.isDirectory(entry)) {
                    String childRelativePath = relativePath.isEmpty()
                            ? entry.getFileName().toString()
                            : relativePath + "/" + entry.getFileName().toString();
                    DirectoryTreeNode child = buildDirectoryTree(entry, childRelativePath);
                    node.getChildren().add(child);
                    node.setTotalImageCount(node.getTotalImageCount() + child.getTotalImageCount());
                } else if (isImageFile(entry)) {
                    node.setImageCount(node.getImageCount() + 1);
                    node.setTotalImageCount(node.getTotalImageCount() + 1);
                }
            }
        } catch (IOException e) {
            log.warn("Error reading directory: {}", dirPath, e);
        }

        return node;
    }

    @Override
    public InputStream readFile(String path) throws Exception {
        if (!isConnected()) {
            throw new IllegalStateException("Not connected to file system");
        }

        Path filePath = Paths.get(rootPath, path);
        return Files.newInputStream(filePath);
    }

    @Override
    public FileInfo getFileMetadata(String path) throws Exception {
        if (!isConnected()) {
            throw new IllegalStateException("Not connected to file system");
        }

        Path filePath = Paths.get(rootPath, path);
        return createFileInfo(filePath);
    }

    @Override
    public boolean fileExists(String path) {
        Path filePath = Paths.get(rootPath, path);
        return Files.exists(filePath);
    }

    @Override
    public ConnectionTestResult testConnection() {
        long startTime = System.currentTimeMillis();
        try {
            Path root = Paths.get(rootPath);
            if (!Files.exists(root)) {
                return ConnectionTestResult.failure("Root path does not exist: " + rootPath,
                        System.currentTimeMillis() - startTime);
            }
            if (!Files.isReadable(root)) {
                return ConnectionTestResult.failure("Root path is not readable: " + rootPath,
                        System.currentTimeMillis() - startTime);
            }
            return ConnectionTestResult.success("Successfully accessed local path: " + rootPath,
                    System.currentTimeMillis() - startTime);
        } catch (Exception e) {
            return ConnectionTestResult.failure("Connection test failed: " + e.getMessage(),
                    System.currentTimeMillis() - startTime);
        }
    }

    private FileInfo createFileInfo(Path path) throws IOException {
        BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);

        return FileInfo.builder()
                .name(path.getFileName().toString())
                .path(path.toString())
                .size(attrs.size())
                .isDirectory(Files.isDirectory(path))
                .lastModified(LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(attrs.lastModifiedTime().toMillis()),
                        ZoneId.systemDefault()))
                .mimeType(Files.isDirectory(path) ? null : probeContentType(path))
                .build();
    }

    private String probeContentType(Path path) {
        try {
            return Files.probeContentType(path);
        } catch (IOException e) {
            return null;
        }
    }

    private String normalizePath(String path) {
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

    private boolean isImageFile(Path path) {
        String contentType = probeContentType(path);
        return contentType != null && contentType.startsWith("image/");
    }
}
