/**
 * App: Picture Model
 * Package: com.picturemodel.domain.enums
 * File: DriveType.java
 * Version: 0.1.0
 * Turns: 5
 * Author: Bobwares (bobwares@outlook.com)
 * Date: 2026-01-30T02:03:52Z
 * Exports: DriveType
 * Description: enum DriveType for DriveType responsibilities. Methods: none (enum constants only).
 */

package com.picturemodel.domain.enums;

/**
 * Type of remote file drive supported by the system.
 *
 * @author Claude (AI Coding Agent)
 */
public enum DriveType {
    /**
     * Local file system access (direct disk access)
     */
    LOCAL,

    /**
     * SMB/CIFS network share (Windows file sharing protocol)
     */
    SMB,

    /**
     * SFTP - SSH File Transfer Protocol (secure file transfer over SSH)
     */
    SFTP,

    /**
     * FTP - File Transfer Protocol (legacy, less secure)
     */
    FTP
}
