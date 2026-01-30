/**
 * App: Picture Model
 * Package: com.picturemodel
 * File: PictureModelApplication.java
 * Version: 0.1.0
 * Turns: 5
 * Author: Bobwares (bobwares@outlook.com)
 * Date: 2026-01-30T02:03:52Z
 * Exports: for
 * Description: class for for for responsibilities. Methods: main - main.
 */

package com.picturemodel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Picture Model Application - Multi-drive Image Management System
 *
 * Main application class for the Spring Boot backend that supports:
 * - Multiple remote file drives (LOCAL, SMB, SFTP, FTP)
 * - Image indexing and metadata extraction
 * - Tag-based organization
 * - Full-text search capabilities
 * - Real-time crawl progress via WebSocket
 *
 * @author Claude (AI Coding Agent)
 * @version 2.0.0
 */
@SpringBootApplication
@EnableAsync
@EnableScheduling
public class PictureModelApplication {

    public static void main(String[] args) {
        SpringApplication.run(PictureModelApplication.class, args);
    }
}
