/**
 * App: Picture Model
 * Package: com.picturemodel.domain.repository
 * File: ImageMetadataRepository.java
 * Version: 0.1.1
 * Turns: 5,10
 * Author: Bobwares (bobwares@outlook.com)
 * Date: 2026-02-01T09:51:47Z
 * Exports: ImageMetadataRepository
 * Description: interface ImageMetadataRepository for ImageMetadataRepository responsibilities. Methods: findByImageId - find by image id; deleteByImageId - delete by image id.
 */

package com.picturemodel.domain.repository;

import com.picturemodel.domain.entity.ImageMetadata;
import com.picturemodel.domain.enums.MetadataSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for ImageMetadata entities.
 *
 * @author Claude (AI Coding Agent)
 */
@Repository
public interface ImageMetadataRepository extends JpaRepository<ImageMetadata, UUID> {

    /**
     * Find all metadata entries for a specific image.
     */
    List<ImageMetadata> findByImageId(UUID imageId);

    /**
     * Delete all metadata entries for a specific image.
     */
    void deleteByImageId(UUID imageId);

    /**
     * Delete metadata entries for a specific image and source.
     */
    void deleteByImageIdAndSource(UUID imageId, MetadataSource source);

    /**
     * Check if metadata exists for a specific image and source.
     */
    boolean existsByImageIdAndSource(UUID imageId, MetadataSource source);
}
