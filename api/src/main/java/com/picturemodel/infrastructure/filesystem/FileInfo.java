/**
 * App: Picture Model
 * Package: com.picturemodel.infrastructure.filesystem
 * File: FileInfo.java
 * Version: 0.1.0
 * Turns: 5
 * Author: Bobwares (bobwares@outlook.com)
 * Date: 2026-01-30T02:03:52Z
 * Exports: FileInfo
 * Description: class FileInfo for FileInfo responsibilities. Methods: none declared.
 */

package com.picturemodel.infrastructure.filesystem;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO representing information about a file in the file system.
 *
 * @author Claude (AI Coding Agent)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileInfo {

    /**
     * Name of the file or directory
     */
    private String name;

    /**
     * Full path to the file or directory
     */
    private String path;

    /**
     * Size in bytes (0 for directories)
     */
    private Long size;

    /**
     * Whether this is a directory
     */
    private Boolean isDirectory;

    /**
     * Last modified timestamp
     */
    private LocalDateTime lastModified;

    /**
     * MIME type (if applicable and determinable)
     */
    private String mimeType;
}
