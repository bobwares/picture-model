/**
 * App: Picture Model
 * Package: com.picturemodel.api.controller
 * File: TagController.java
 * Version: 0.1.1
 * Turns: 7
 * Author: Bobwares (bobwares@outlook.com)
 * Date: 2026-01-31T08:41:53Z
 * Exports: TagController
 * Description: REST controller for tag management. Methods: getAll - list tags; create - create tag; update - update tag; delete - delete tag.
 */

package com.picturemodel.api.controller;

import com.picturemodel.api.dto.request.TagCreateRequest;
import com.picturemodel.api.dto.request.TagUpdateRequest;
import com.picturemodel.domain.entity.Image;
import com.picturemodel.domain.entity.Tag;
import com.picturemodel.domain.repository.ImageRepository;
import com.picturemodel.domain.repository.TagRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * REST controller for tag CRUD operations.
 * Base path: /api/tags
 */
@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagRepository tagRepository;
    private final ImageRepository imageRepository;

    /**
     * List all tags sorted by name.
     * GET /api/tags
     */
    @GetMapping
    public ResponseEntity<List<Tag>> getAll() {
        List<Tag> tags = tagRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
        return ResponseEntity.ok(tags);
    }

    /**
     * Create a tag.
     * POST /api/tags
     */
    @PostMapping
    public ResponseEntity<Tag> create(@Valid @RequestBody TagCreateRequest request) {
        String name = request.getName().trim();
        Optional<Tag> existing = tagRepository.findByNameIgnoreCase(name);
        if (existing.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        Tag tag = Tag.builder()
                .name(name)
                .color(request.getColor())
                .usageCount(0)
                .build();

        Tag saved = tagRepository.save(tag);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    /**
     * Update a tag.
     * PUT /api/tags/{id}
     */
    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<Tag> update(@PathVariable UUID id, @Valid @RequestBody TagUpdateRequest request) {
        Optional<Tag> tagOptional = tagRepository.findById(id);
        if (tagOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Tag tag = tagOptional.get();
        if (request.getName() != null) {
            String name = request.getName().trim();
            if (!name.isEmpty()) {
                Optional<Tag> existing = tagRepository.findByNameIgnoreCase(name);
                if (existing.isPresent() && !existing.get().getId().equals(id)) {
                    return ResponseEntity.status(HttpStatus.CONFLICT).build();
                }
                tag.setName(name);
            }
        }
        if (request.getColor() != null) {
            tag.setColor(request.getColor());
        }

        Tag saved = tagRepository.save(tag);
        return ResponseEntity.ok(saved);
    }

    /**
     * Delete a tag.
     * DELETE /api/tags/{id}
     */
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        Optional<Tag> tagOptional = tagRepository.findById(id);
        if (tagOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Tag tag = tagOptional.get();
        if (!tag.getImages().isEmpty()) {
            for (Image image : new HashSet<>(tag.getImages())) {
                image.getTags().remove(tag);
                imageRepository.save(image);
            }
        }

        tagRepository.delete(tag);
        return ResponseEntity.noContent().build();
    }
}
