/**
 * App: Picture Model
 * Package: com.picturemodel.domain.entity
 * File: Image.java
 * Version: 0.1.0
 * Turns: 5
 * Author: Bobwares (bobwares@outlook.com)
 * Date: 2026-01-30T02:03:52Z
 * Exports: Image
 * Description: class Image for Image responsibilities. Methods: onCreate - on create; getFullPath - get full path.
 */

package com.picturemodel.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Image entity representing an indexed image file.
 *
 * @author Claude (AI Coding Agent)
 */
@Entity
@Table(name = "images", indexes = {
        @Index(name = "idx_image_drive_path", columnList = "drive_id,filePath", unique = true),
        @Index(name = "idx_image_file_hash", columnList = "fileHash"),
        @Index(name = "idx_image_file_name", columnList = "fileName"),
        @Index(name = "idx_image_indexed_date", columnList = "indexedDate"),
        @Index(name = "idx_image_deleted", columnList = "deleted")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "drive_id", nullable = false)
    private RemoteFileDrive drive;

    @Column(nullable = false, length = 500)
    private String fileName;

    @Column(nullable = false, length = 2000)
    private String filePath; // Relative path on the drive

    @Column(nullable = false)
    private Long fileSize; // Size in bytes

    @Column(nullable = false, length = 64)
    private String fileHash; // SHA-256 hash

    @Column(nullable = false, length = 100)
    private String mimeType;

    private Integer width;

    private Integer height;

    @Column(length = 500)
    private String thumbnailPath;

    @Column(nullable = false)
    @Builder.Default
    private Boolean deleted = false;

    private LocalDateTime capturedAt; // From EXIF data

    @Column(nullable = false)
    private LocalDateTime createdDate; // File creation date

    @Column(nullable = false)
    private LocalDateTime modifiedDate; // File modification date

    @Column(nullable = false)
    private LocalDateTime indexedDate; // When indexed by crawler

    @OneToMany(mappedBy = "image", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ImageMetadata> metadata = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "image_tags",
            joinColumns = @JoinColumn(name = "image_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"),
            indexes = {
                    @Index(name = "idx_image_tags_image", columnList = "image_id"),
                    @Index(name = "idx_image_tags_tag", columnList = "tag_id")
            }
    )
    @Builder.Default
    private Set<Tag> tags = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        indexedDate = LocalDateTime.now();
    }

    /**
     * Get the full path (drive root + relative path).
     */
    @Transient
    public String getFullPath() {
        if (drive != null) {
            return drive.getRootPath() + (drive.getRootPath().endsWith("/") ? "" : "/") + filePath;
        }
        return filePath;
    }
}
