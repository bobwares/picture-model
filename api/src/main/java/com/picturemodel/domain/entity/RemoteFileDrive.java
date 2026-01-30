/**
 * App: Picture Model
 * Package: com.picturemodel.domain.entity
 * File: RemoteFileDrive.java
 * Version: 0.1.0
 * Turns: 5
 * Author: Bobwares (bobwares@outlook.com)
 * Date: 2026-01-30T02:03:52Z
 * Exports: RemoteFileDrive
 * Description: class RemoteFileDrive for RemoteFileDrive responsibilities. Methods: onCreate - on create; onUpdate - on update.
 */

package com.picturemodel.domain.entity;

import com.picturemodel.domain.enums.ConnectionStatus;
import com.picturemodel.domain.enums.DriveType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Remote File Drive entity representing a storage location.
 * Supports LOCAL, SMB, SFTP, and FTP file systems.
 *
 * @author Claude (AI Coding Agent)
 */
@Entity
@Table(name = "remote_file_drives", indexes = {
        @Index(name = "idx_drive_status", columnList = "status"),
        @Index(name = "idx_drive_name", columnList = "name")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RemoteFileDrive {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 200)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DriveType type;

    @Column(nullable = false, length = 1000)
    private String connectionUrl;

    @Column(columnDefinition = "TEXT")
    private String encryptedCredentials; // JSON format, encrypted

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ConnectionStatus status = ConnectionStatus.DISCONNECTED;

    @Column(nullable = false, length = 1000)
    private String rootPath;

    @Column(nullable = false)
    @Builder.Default
    private Boolean autoConnect = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean autoCrawl = false;

    private LocalDateTime lastConnected;

    private LocalDateTime lastCrawled;

    @Column(nullable = false)
    @Builder.Default
    private Integer imageCount = 0;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @Column(nullable = false)
    private LocalDateTime modifiedDate;

    @OneToMany(mappedBy = "drive", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Image> images = new ArrayList<>();

    @OneToMany(mappedBy = "drive", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CrawlJob> crawlJobs = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
        modifiedDate = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        modifiedDate = LocalDateTime.now();
    }
}
