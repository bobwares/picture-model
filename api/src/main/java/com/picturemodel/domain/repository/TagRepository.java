/**
 * App: Picture Model
 * Package: com.picturemodel.domain.repository
 * File: TagRepository.java
 * Version: 0.1.0
 * Turns: 5
 * Author: Bobwares (bobwares@outlook.com)
 * Date: 2026-01-30T02:03:52Z
 * Exports: TagRepository
 * Description: interface TagRepository for TagRepository responsibilities. Methods: findByNameIgnoreCase - find by name ignore case; findByName - find by name.
 */

package com.picturemodel.domain.repository;

import com.picturemodel.domain.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Tag entities.
 *
 * @author Claude (AI Coding Agent)
 */
@Repository
public interface TagRepository extends JpaRepository<Tag, UUID> {

    /**
     * Find a tag by name (case-insensitive).
     */
    Optional<Tag> findByNameIgnoreCase(String name);

    /**
     * Find a tag by exact name.
     */
    Optional<Tag> findByName(String name);
}
