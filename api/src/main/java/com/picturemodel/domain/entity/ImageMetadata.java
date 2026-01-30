/**
 * App: Picture Model
 * Package: com.picturemodel.domain.entity
 * File: ImageMetadata.java
 * Version: 0.1.0
 * Turns: 5
 * Author: Bobwares (bobwares@outlook.com)
 * Date: 2026-01-30T02:03:52Z
 * Exports: ImageMetadata
 * Description: class ImageMetadata for ImageMetadata responsibilities. Methods: onSave - on save.
 */

package com.picturemodel.domain.entity;

import com.picturemodel.domain.enums.MetadataSource;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ImageMetadata entity for storing key-value metadata associated with images.
 *
 * @author Claude (AI Coding Agent)
 */
@Entity
@Table(name = "image_metadata", indexes = {
        @Index(name = "idx_metadata_image_id", columnList = "image_id"),
        @Index(name = "idx_metadata_key", columnList = "metadataKey")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImageMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "image_id", nullable = false)
    private Image image;

    @Column(name = "metadataKey", nullable = false, length = 255)
    private String key;

    @Column(columnDefinition = "TEXT")
    private String value_entry;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MetadataSource source;

    @Column(nullable = false)
    private LocalDateTime lastModified;

    @PrePersist
    @PreUpdate
    protected void onSave() {
        lastModified = LocalDateTime.now();
        // Normalize key to lowercase for consistent searching
        if (key != null) {
            key = key.toLowerCase().trim();
        }
    }
}
