/**
 * App: Picture Model
 * Package: com.picturemodel.infrastructure.filesystem
 * File: SftpFileSystemProvider.java
 * Version: 0.1.0
 * Turns: 5
 * Author: Bobwares (bobwares@outlook.com)
 * Date: 2026-01-30T02:03:52Z
 * Exports: SftpFileSystemProvider
 * Description: class SftpFileSystemProvider for SftpFileSystemProvider responsibilities. Methods: SftpFileSystemProvider - constructor; connect - connect; disconnect - disconnect; isConnected - is connected; listDirectory - list directory; getDirectoryTree - get directory tree; buildDirectoryTree - build directory tree; readFile - read file; getFileMetadata - get file metadata; fileExists - file exists; testConnection - test connection; createFileInfo - create file info; normalizePath - normalize path; isImageFile - is image file; guessContentType - guess content type.
 */

package com.picturemodel.infrastructure.filesystem;

import com.jcraft.jsch.*;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * File system provider for SFTP using JSch library.
 *
 * @author Claude (AI Coding Agent)
 */
@Slf4j
public class SftpFileSystemProvider implements FileSystemProvider {

    private final String host;
    private final int port;
    private final String username;
    private final String password;
    private final String rootPath;
    private Session session;
    private ChannelSftp sftpChannel;
    private boolean connected;

    public SftpFileSystemProvider(String host, int port, String username, String password, String rootPath) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.rootPath = rootPath;
    }

    @Override
    public void connect() throws Exception {
        JSch jsch = new JSch();
        session = jsch.getSession(username, host, port);
        session.setPassword(password);

        // Disable strict host key checking for convenience (not recommended for production)
        java.util.Properties config = new java.util.Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.setTimeout(30000);

        session.connect();
        sftpChannel = (ChannelSftp) session.openChannel("sftp");
        sftpChannel.connect();

        connected = true;
        log.info("Connected to SFTP server: {}:{}", host, port);
    }

    @Override
    public void disconnect() {
        if (sftpChannel != null && sftpChannel.isConnected()) {
            sftpChannel.disconnect();
        }
        if (session != null && session.isConnected()) {
            session.disconnect();
        }
        connected = false;
        log.info("Disconnected from SFTP server: {}:{}", host, port);
    }

    @Override
    public boolean isConnected() {
        return connected && sftpChannel != null && sftpChannel.isConnected();
    }

    @Override
    public List<FileInfo> listDirectory(String path) throws Exception {
        if (!isConnected()) {
            throw new IllegalStateException("Not connected to SFTP server");
        }

        String fullPath = normalizePath(rootPath, path);
        @SuppressWarnings("unchecked")
        Vector<ChannelSftp.LsEntry> files = sftpChannel.ls(fullPath);

        List<FileInfo> fileInfos = new ArrayList<>();
        for (ChannelSftp.LsEntry entry : files) {
            if (".".equals(entry.getFilename()) || "..".equals(entry.getFilename())) {
                continue;
            }
            fileInfos.add(createFileInfo(entry, fullPath));
        }

        return fileInfos;
    }

    @Override
    public DirectoryTreeNode getDirectoryTree(String path) throws Exception {
        if (!isConnected()) {
            throw new IllegalStateException("Not connected to SFTP server");
        }

        String fullPath = normalizePath(rootPath, path);
        return buildDirectoryTree(fullPath, path);
    }

    private DirectoryTreeNode buildDirectoryTree(String fullPath, String relativePath) {
        DirectoryTreeNode node = DirectoryTreeNode.builder()
                .name(fullPath.substring(fullPath.lastIndexOf('/') + 1))
                .path(relativePath)
                .build();

        try {
            @SuppressWarnings("unchecked")
            Vector<ChannelSftp.LsEntry> files = sftpChannel.ls(fullPath);

            for (ChannelSftp.LsEntry entry : files) {
                if (".".equals(entry.getFilename()) || "..".equals(entry.getFilename())) {
                    continue;
                }

                if (entry.getAttrs().isDir()) {
                    String childPath = relativePath + "/" + entry.getFilename();
                    DirectoryTreeNode child = buildDirectoryTree(fullPath + "/" + entry.getFilename(), childPath);
                    node.getChildren().add(child);
                    node.setTotalImageCount(node.getTotalImageCount() + child.getTotalImageCount());
                } else if (isImageFile(entry.getFilename())) {
                    node.setImageCount(node.getImageCount() + 1);
                    node.setTotalImageCount(node.getTotalImageCount() + 1);
                }
            }
        } catch (SftpException e) {
            log.warn("Error reading SFTP directory: {}", fullPath, e);
        }

        return node;
    }

    @Override
    public InputStream readFile(String path) throws Exception {
        if (!isConnected()) {
            throw new IllegalStateException("Not connected to SFTP server");
        }

        String fullPath = normalizePath(rootPath, path);
        return sftpChannel.get(fullPath);
    }

    @Override
    public FileInfo getFileMetadata(String path) throws Exception {
        if (!isConnected()) {
            throw new IllegalStateException("Not connected to SFTP server");
        }

        String fullPath = normalizePath(rootPath, path);
        SftpATTRS attrs = sftpChannel.stat(fullPath);
        String fileName = path.substring(path.lastIndexOf('/') + 1);

        return FileInfo.builder()
                .name(fileName)
                .path(fullPath)
                .size(attrs.getSize())
                .isDirectory(attrs.isDir())
                .lastModified(LocalDateTime.ofInstant(
                        java.time.Instant.ofEpochMilli(attrs.getMTime() * 1000L),
                        ZoneId.systemDefault()))
                .mimeType(attrs.isDir() ? null : guessContentType(fileName))
                .build();
    }

    @Override
    public boolean fileExists(String path) {
        try {
            String fullPath = normalizePath(rootPath, path);
            sftpChannel.stat(fullPath);
            return true;
        } catch (SftpException e) {
            return false;
        }
    }

    @Override
    public ConnectionTestResult testConnection() {
        long startTime = System.currentTimeMillis();
        try {
            sftpChannel.stat(rootPath);
            return ConnectionTestResult.success("Successfully connected to SFTP server: " + host + ":" + port,
                    System.currentTimeMillis() - startTime);
        } catch (Exception e) {
            return ConnectionTestResult.failure("SFTP connection test failed: " + e.getMessage(),
                    System.currentTimeMillis() - startTime);
        }
    }

    private FileInfo createFileInfo(ChannelSftp.LsEntry entry, String parentPath) {
        return FileInfo.builder()
                .name(entry.getFilename())
                .path(parentPath + "/" + entry.getFilename())
                .size(entry.getAttrs().getSize())
                .isDirectory(entry.getAttrs().isDir())
                .lastModified(LocalDateTime.ofInstant(
                        java.time.Instant.ofEpochMilli(entry.getAttrs().getMTime() * 1000L),
                        ZoneId.systemDefault()))
                .mimeType(entry.getAttrs().isDir() ? null : guessContentType(entry.getFilename()))
                .build();
    }

    private String normalizePath(String root, String path) {
        if (path == null || path.isEmpty() || "/".equals(path)) {
            return root;
        }
        return root + (path.startsWith("/") ? path : "/" + path);
    }

    private boolean isImageFile(String fileName) {
        String contentType = guessContentType(fileName);
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
