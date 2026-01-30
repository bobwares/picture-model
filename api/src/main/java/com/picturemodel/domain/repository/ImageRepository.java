/**
 * App: Picture Model
 * Package: com.picturemodel.domain.repository
 * File: ImageRepository.java
 * Version: 0.1.0
 * Turns: 5
 * Author: Bobwares (bobwares@outlook.com)
 * Date: 2026-01-30T02:03:52Z
 * Exports: ImageRepository
 * Description: interface ImageRepository for ImageRepository responsibilities. Methods: findByDriveIdAndFilePath - find by drive id and file path; findByFileHash - find by file hash; findByDriveId - find by drive id; countByDriveId - count by drive id; countByDriveIdAndDeletedFalse - count by drive id and deleted false.
 */

package com.picturemodel.domain.repository;

import com.picturemodel.domain.entity.Image;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Image entities with specification support for dynamic queries.
 *
 * @author Claude (AI Coding Agent)
 */
@Repository
public interface ImageRepository extends JpaRepository<Image, UUID>, JpaSpecificationExecutor<Image> {

    /**
     * Find an image by drive ID and file path (unique combination).
     */
    Optional<Image> findByDriveIdAndFilePath(UUID driveId, String filePath);

    /**
     * Find images by file hash (for duplicate detection).
     */
    Optional<Image> findByFileHash(String fileHash);

    /**
     * Find all images for a specific drive with pagination.
     */
    Page<Image> findByDriveId(UUID driveId, Pageable pageable);

    /**
     * Count total images for a specific drive.
     */
    long countByDriveId(UUID driveId);

    /**
     * Count images for a drive that are not deleted.
     */
    long countByDriveIdAndDeletedFalse(UUID driveId);
}
