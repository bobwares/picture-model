/**
 * App: Picture Model
 * Package: com.picturemodel.infrastructure.filesystem
 * File: ConnectionTestResult.java
 * Version: 0.1.0
 * Turns: 5
 * Author: Bobwares (bobwares@outlook.com)
 * Date: 2026-01-30T02:03:52Z
 * Exports: ConnectionTestResult
 * Description: class ConnectionTestResult for ConnectionTestResult responsibilities. Methods: success - success; failure - failure.
 */

package com.picturemodel.infrastructure.filesystem;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Result of a file system connection test.
 *
 * @author Claude (AI Coding Agent)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConnectionTestResult {

    /**
     * Whether the connection test was successful
     */
    private Boolean success;

    /**
     * Error message if connection failed
     */
    private String errorMessage;

    /**
     * Additional details about the connection
     */
    private String details;

    /**
     * Time taken to test the connection in milliseconds
     */
    private Long durationMs;

    /**
     * Create a successful result
     */
    public static ConnectionTestResult success(String details, long durationMs) {
        return ConnectionTestResult.builder()
                .success(true)
                .details(details)
                .durationMs(durationMs)
                .build();
    }

    /**
     * Create a failed result
     */
    public static ConnectionTestResult failure(String errorMessage, long durationMs) {
        return ConnectionTestResult.builder()
                .success(false)
                .errorMessage(errorMessage)
                .durationMs(durationMs)
                .build();
    }
}
