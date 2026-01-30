/**
 * App: Picture Model
 * Package: com.picturemodel.infrastructure.filesystem
 * File: FileSystemProvider.java
 * Version: 0.1.0
 * Turns: 5
 * Author: Bobwares (bobwares@outlook.com)
 * Date: 2026-01-30T02:03:52Z
 * Exports: for
 * Description: interface for for for responsibilities. Methods: connect - connect; disconnect - disconnect; isConnected - is connected; listDirectory - list directory; getDirectoryTree - get directory tree; readFile - read file; getFileMetadata - get file metadata; fileExists - file exists; testConnection - test connection.
 */

package com.picturemodel.infrastructure.filesystem;

import java.io.InputStream;
import java.util.List;

/**
 * Abstract interface for file system access.
 * Provides a unified API for accessing different types of file systems (LOCAL, SMB, SFTP, FTP).
 *
 * @author Claude (AI Coding Agent)
 */
public interface FileSystemProvider {

    /**
     * Establish connection to the file system.
     *
     * @throws Exception if connection fails
     */
    void connect() throws Exception;

    /**
     * Disconnect from the file system and release resources.
     */
    void disconnect();

    /**
     * Check if currently connected to the file system.
     *
     * @return true if connected, false otherwise
     */
    boolean isConnected();

    /**
     * List all files and directories in the specified path.
     *
     * @param path the directory path to list
     * @return list of FileInfo objects
     * @throws Exception if listing fails
     */
    List<FileInfo> listDirectory(String path) throws Exception;

    /**
     * Get the complete directory tree structure starting from the specified path.
     *
     * @param path the root path to start building the tree
     * @return root DirectoryTreeNode
     * @throws Exception if tree building fails
     */
    DirectoryTreeNode getDirectoryTree(String path) throws Exception;

    /**
     * Read a file and return its contents as an InputStream.
     *
     * @param path the file path to read
     * @return InputStream of the file contents
     * @throws Exception if reading fails
     */
    InputStream readFile(String path) throws Exception;

    /**
     * Get metadata about a specific file.
     *
     * @param path the file path
     * @return FileInfo with file metadata
     * @throws Exception if metadata retrieval fails
     */
    FileInfo getFileMetadata(String path) throws Exception;

    /**
     * Check if a file exists at the specified path.
     *
     * @param path the file path to check
     * @return true if file exists, false otherwise
     */
    boolean fileExists(String path);

    /**
     * Test the connection to verify it's working properly.
     *
     * @return ConnectionTestResult with test outcome
     */
    ConnectionTestResult testConnection();
}
