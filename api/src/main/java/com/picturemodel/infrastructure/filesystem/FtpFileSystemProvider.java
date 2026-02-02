/**
 * App: Picture Model
 * Package: com.picturemodel.infrastructure.filesystem
 * File: FtpFileSystemProvider.java
 * Version: 0.1.1
 * Turns: 5,9
 * Author: Bobwares (bobwares@outlook.com)
 * Date: 2026-01-31T21:59:02Z
 * Exports: FtpFileSystemProvider
 * Description: class FtpFileSystemProvider for FtpFileSystemProvider responsibilities. Methods: FtpFileSystemProvider - constructor; connect - connect; disconnect - disconnect; isConnected - is connected; listDirectory - list directory; getDirectoryTree - get directory tree; buildDirectoryTree - build directory tree; readFile - read file; getFileMetadata - get file metadata; fileExists - file exists; testConnection - test connection; createFileInfo - create file info; normalizePath - normalize path; isImageFile - is image file; guessContentType - guess content type.
 */

package com.picturemodel.infrastructure.filesystem;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * File system provider for FTP using Apache Commons Net.
 *
 * @author Claude (AI Coding Agent)
 */
@Slf4j
public class FtpFileSystemProvider implements FileSystemProvider {

    private final String host;
    private final int port;
    private final String username;
    private final String password;
    private final String rootPath;
    private FTPClient ftpClient;
    private boolean connected;

    public FtpFileSystemProvider(String host, int port, String username, String password, String rootPath) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.rootPath = rootPath;
    }

    @Override
    public void connect() throws Exception {
        ftpClient = new FTPClient();
        ftpClient.connect(host, port);
        ftpClient.login(username, password);
        ftpClient.enterLocalPassiveMode();
        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

        connected = true;
        log.info("Connected to FTP server: {}:{}", host, port);
    }

    @Override
    public void disconnect() {
        if (ftpClient != null && ftpClient.isConnected()) {
            try {
                ftpClient.logout();
                ftpClient.disconnect();
            } catch (IOException e) {
                log.error("Error disconnecting from FTP", e);
            }
        }
        connected = false;
        log.info("Disconnected from FTP server: {}:{}", host, port);
    }

    @Override
    public boolean isConnected() {
        return connected && ftpClient != null && ftpClient.isConnected();
    }

    @Override
    public List<FileInfo> listDirectory(String path) throws Exception {
        if (!isConnected()) {
            throw new IllegalStateException("Not connected to FTP server");
        }

        String fullPath = normalizePath(rootPath, path);
        FTPFile[] files = ftpClient.listFiles(fullPath);

        List<FileInfo> fileInfos = new ArrayList<>();
        for (FTPFile file : files) {
            if (".".equals(file.getName()) || "..".equals(file.getName())) {
                continue;
            }
            fileInfos.add(createFileInfo(file, fullPath));
        }

        return fileInfos;
    }

    @Override
    public DirectoryTreeNode getDirectoryTree(String path) throws Exception {
        if (!isConnected()) {
            throw new IllegalStateException("Not connected to FTP server");
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
            FTPFile[] files = ftpClient.listFiles(fullPath);

            for (FTPFile file : files) {
                if (".".equals(file.getName()) || "..".equals(file.getName())) {
                    continue;
                }

                if (file.isDirectory()) {
                    String childPath = relativePath + "/" + file.getName();
                    DirectoryTreeNode child = buildDirectoryTree(fullPath + "/" + file.getName(), childPath);
                    node.getChildren().add(child);
                    node.setTotalImageCount(node.getTotalImageCount() + child.getTotalImageCount());
                } else if (isImageFile(file.getName())) {
                    node.setImageCount(node.getImageCount() + 1);
                    node.setTotalImageCount(node.getTotalImageCount() + 1);
                }
            }
        } catch (IOException e) {
            log.warn("Error reading FTP directory: {}", fullPath, e);
        }

        return node;
    }

    @Override
    public InputStream readFile(String path) throws Exception {
        if (!isConnected()) {
            throw new IllegalStateException("Not connected to FTP server");
        }

        String fullPath = normalizePath(rootPath, path);
        return ftpClient.retrieveFileStream(fullPath);
    }

    @Override
    public FileInfo getFileMetadata(String path) throws Exception {
        if (!isConnected()) {
            throw new IllegalStateException("Not connected to FTP server");
        }

        String fullPath = normalizePath(rootPath, path);
        String parentPath = fullPath.substring(0, fullPath.lastIndexOf('/'));
        String fileName = path.substring(path.lastIndexOf('/') + 1);

        FTPFile[] files = ftpClient.listFiles(parentPath);
        for (FTPFile file : files) {
            if (file.getName().equals(fileName)) {
                return createFileInfo(file, parentPath);
            }
        }

        throw new IOException("File not found: " + path);
    }

    @Override
    public boolean fileExists(String path) {
        try {
            String fullPath = normalizePath(rootPath, path);
            String parentPath = fullPath.substring(0, fullPath.lastIndexOf('/'));
            String fileName = path.substring(path.lastIndexOf('/') + 1);

            FTPFile[] files = ftpClient.listFiles(parentPath);
            for (FTPFile file : files) {
                if (file.getName().equals(fileName)) {
                    return true;
                }
            }
            return false;
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public ConnectionTestResult testConnection() {
        long startTime = System.currentTimeMillis();
        try {
            ftpClient.listFiles(rootPath);
            return ConnectionTestResult.success("Successfully connected to FTP server: " + host + ":" + port,
                    System.currentTimeMillis() - startTime);
        } catch (Exception e) {
            return ConnectionTestResult.failure("FTP connection test failed: " + e.getMessage(),
                    System.currentTimeMillis() - startTime);
        }
    }

    private FileInfo createFileInfo(FTPFile file, String parentPath) {
        return FileInfo.builder()
                .name(file.getName())
                .path(parentPath + "/" + file.getName())
                .size(file.getSize())
                .isDirectory(file.isDirectory())
                .lastModified(LocalDateTime.ofInstant(
                        file.getTimestamp().toInstant(),
                        ZoneId.systemDefault()))
                .mimeType(file.isDirectory() ? null : guessContentType(file.getName()))
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
        if (lower.endsWith(".heic")) return "image/heic";
        if (lower.endsWith(".heif")) return "image/heif";
        return null;
    }
}
