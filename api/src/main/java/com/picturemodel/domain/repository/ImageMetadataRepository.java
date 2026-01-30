/**
 * App: Picture Model
 * Package: com.picturemodel.domain.repository
 * File: ImageMetadataRepository.java
 * Version: 0.1.0
 * Turns: 5
 * Author: Bobwares (bobwares@outlook.com)
 * Date: 2026-01-30T02:03:52Z
 * Exports: ImageMetadataRepository
 * Description: interface ImageMetadataRepository for ImageMetadataRepository responsibilities. Methods: findByImageId - find by image id; deleteByImageId - delete by image id.
 */

package com.picturemodel.domain.repository;

import com.picturemodel.domain.entity.ImageMetadata;
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
}
