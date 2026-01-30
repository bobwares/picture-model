/**
 * App: Picture Model
 * Package: com.picturemodel.domain.entity
 * File: Tag.java
 * Version: 0.1.0
 * Turns: 5
 * Author: Bobwares (bobwares@outlook.com)
 * Date: 2026-01-30T02:03:52Z
 * Exports: Tag
 * Description: class Tag for Tag responsibilities. Methods: onCreate - on create.
 */

package com.picturemodel.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Tag entity for organizing images.
 *
 * @author Claude (AI Coding Agent)
 */
@Entity
@Table(name = "tags", indexes = {
        @Index(name = "idx_tag_name", columnList = "name", unique = true),
        @Index(name = "idx_tag_usage_count", columnList = "usageCount")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(length = 7)
    private String color; // Hex color code, e.g., "#FF5733"

    @Column(nullable = false)
    @Builder.Default
    private Integer usageCount = 0;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @ManyToMany(mappedBy = "tags")
    @Builder.Default
    private Set<Image> images = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
    }
}
