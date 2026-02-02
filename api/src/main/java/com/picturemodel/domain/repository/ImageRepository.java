/**
 * App: Picture Model
 * Package: com.picturemodel.domain.repository
 * File: ImageRepository.java
 * Version: 0.1.3
 * Turns: 5
 * Author: Bobwares (bobwares@outlook.com)
 * Date: 2026-01-31T07:44:19Z
 * Exports: ImageRepository
 * Description: interface ImageRepository for ImageRepository responsibilities. Methods: findByDrive_IdAndFilePath - find by drive id and file path; findByFileHash - find by file hash; findByDrive_Id - find by drive id; findAllByDrive_Id - find all by drive id; countByDrive_Id - count by drive id; countByDrive_IdAndDeletedFalse - count by drive id and deleted false.
 */

package com.picturemodel.domain.repository;

import com.picturemodel.domain.entity.Image;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
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
    Optional<Image> findByDrive_IdAndFilePath(UUID driveId, String filePath);

    /**
     * Find images by file hash (for duplicate detection).
     */
    Optional<Image> findByFileHash(String fileHash);

    /**
     * Find all images for a specific drive with pagination.
     */
    Page<Image> findByDrive_Id(UUID driveId, Pageable pageable);

    /**
     * Find all images for a specific drive.
     */
    List<Image> findAllByDrive_Id(UUID driveId);

    /**
     * Find images by drive and prefix path, excluding deleted.
     */
    Page<Image> findByDrive_IdAndFilePathStartingWithAndDeletedFalse(UUID driveId, String filePath, Pageable pageable);

    /**
     * Find images in a specific directory only (not subdirectories), excluding deleted.
     * For root directory, use empty string as dirPath.
     * For subdirectories, dirPath should end with '/' (e.g., "photos/").
     *
     * Examples:
     * - dirPath = "" (root): matches "image.jpg" but not "folder/image.jpg"
     * - dirPath = "photos/": matches "photos/image.jpg" but not "photos/vacation/image.jpg"
     */
    @Query("SELECT i FROM Image i WHERE i.drive.id = :driveId " +
           "AND i.deleted = false " +
           "AND (" +
           "  (:dirPath = '' AND i.filePath NOT LIKE '%/%') " +
           "  OR " +
           "  (:dirPath <> '' AND i.filePath LIKE CONCAT(:dirPath, '%') AND i.filePath NOT LIKE CONCAT(:dirPath, '%/%'))" +
           ")")
    Page<Image> findByDrive_IdAndDirectoryAndDeletedFalse(
            @Param("driveId") UUID driveId,
            @Param("dirPath") String dirPath,
            Pageable pageable);

    /**
     * Find images for a drive, excluding deleted.
     */
    Page<Image> findByDrive_IdAndDeletedFalse(UUID driveId, Pageable pageable);

    /**
     * Find images excluding deleted.
     */
    Page<Image> findByDeletedFalse(Pageable pageable);
    /**
     * Count total images for a specific drive.
     */
    long countByDrive_Id(UUID driveId);

    /**
     * Count images for a drive that are not deleted.
     */
    long countByDrive_IdAndDeletedFalse(UUID driveId);
}
