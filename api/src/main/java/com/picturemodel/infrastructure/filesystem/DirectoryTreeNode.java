/**
 * App: Picture Model
 * Package: com.picturemodel.infrastructure.filesystem
 * File: DirectoryTreeNode.java
 * Version: 0.1.0
 * Turns: 5
 * Author: Bobwares (bobwares@outlook.com)
 * Date: 2026-01-30T02:03:52Z
 * Exports: DirectoryTreeNode
 * Description: class DirectoryTreeNode for DirectoryTreeNode responsibilities. Methods: none declared.
 */

package com.picturemodel.infrastructure.filesystem;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Recursive tree structure representing a directory hierarchy.
 *
 * @author Claude (AI Coding Agent)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DirectoryTreeNode {

    /**
     * Name of the directory
     */
    private String name;

    /**
     * Full path to the directory
     */
    private String path;

    /**
     * Number of images directly in this directory
     */
    @Builder.Default
    private Integer imageCount = 0;

    /**
     * Total number of images in this directory and all subdirectories
     */
    @Builder.Default
    private Integer totalImageCount = 0;

    /**
     * Child directories
     */
    @Builder.Default
    private List<DirectoryTreeNode> children = new ArrayList<>();

    /**
     * Whether this node is expanded in the UI (optional, for client state)
     */
    @Builder.Default
    private Boolean expanded = false;
}
