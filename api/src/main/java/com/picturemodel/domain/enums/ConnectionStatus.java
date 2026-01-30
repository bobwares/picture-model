/**
 * App: Picture Model
 * Package: com.picturemodel.domain.enums
 * File: ConnectionStatus.java
 * Version: 0.1.0
 * Turns: 5
 * Author: Bobwares (bobwares@outlook.com)
 * Date: 2026-01-30T02:03:52Z
 * Exports: ConnectionStatus
 * Description: enum ConnectionStatus for ConnectionStatus responsibilities. Methods: none (enum constants only).
 */

package com.picturemodel.domain.enums;

/**
 * Connection status for a remote file drive.
 *
 * @author Claude (AI Coding Agent)
 */
public enum ConnectionStatus {
    /**
     * Drive is not connected
     */
    DISCONNECTED,

    /**
     * Connection is in progress
     */
    CONNECTING,

    /**
     * Drive is connected and accessible
     */
    CONNECTED,

    /**
     * Connection failed or encountered an error
     */
    ERROR
}
