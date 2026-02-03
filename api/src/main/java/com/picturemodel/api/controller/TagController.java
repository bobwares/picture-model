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
import com.picturemodel.service.ReactiveRepositoryWrapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

/**
 * REST controller for tag CRUD operations (WebFlux).
 * Base path: /api/tags
 */
@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {

    private final ReactiveRepositoryWrapper repositoryWrapper;

    /**
     * List all tags sorted by name.
     * GET /api/tags
     */
    @GetMapping
    public Mono<ResponseEntity<List<Tag>>> getAll() {
        return repositoryWrapper.findAllTagsSorted()
                .collectList()
                .map(ResponseEntity::ok);
    }

    /**
     * Create a tag.
     * POST /api/tags
     */
    @PostMapping
    public Mono<ResponseEntity<Tag>> create(@Valid @RequestBody TagCreateRequest request) {
        String name = request.getName().trim();

        return repositoryWrapper.findTagByNameIgnoreCase(name)
                .flatMap(existing -> Mono.<ResponseEntity<Tag>>just(ResponseEntity.status(HttpStatus.CONFLICT).build()))
                .switchIfEmpty(
                        Mono.defer(() -> {
                            Tag tag = Tag.builder()
                                    .name(name)
                                    .color(request.getColor())
                                    .usageCount(0)
                                    .build();

                            return repositoryWrapper.saveTag(tag)
                                    .map(saved -> ResponseEntity.status(HttpStatus.CREATED).body(saved));
                        })
                );
    }

    /**
     * Update a tag.
     * PUT /api/tags/{id}
     */
    @PutMapping("/{id}")
    public Mono<ResponseEntity<Tag>> update(@PathVariable UUID id, @Valid @RequestBody TagUpdateRequest request) {
        return repositoryWrapper.findTagById(id)
                .flatMap(tag -> {
                    Mono<Tag> updateMono = Mono.just(tag);

                    if (request.getName() != null) {
                        String name = request.getName().trim();
                        if (!name.isEmpty()) {
                            updateMono = repositoryWrapper.findTagByNameIgnoreCase(name)
                                    .flatMap(existing -> {
                                        if (!existing.getId().equals(id)) {
                                            return Mono.error(new IllegalStateException("CONFLICT"));
                                        }
                                        tag.setName(name);
                                        return Mono.just(tag);
                                    })
                                    .switchIfEmpty(Mono.defer(() -> {
                                        tag.setName(name);
                                        return Mono.just(tag);
                                    }));
                        }
                    }

                    return updateMono.flatMap(t -> {
                        if (request.getColor() != null) {
                            t.setColor(request.getColor());
                        }
                        return repositoryWrapper.saveTag(t)
                                .map(ResponseEntity::ok);
                    });
                })
                .onErrorResume(IllegalStateException.class, e ->
                        Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).build()))
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }

    /**
     * Delete a tag.
     * DELETE /api/tags/{id}
     */
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> delete(@PathVariable UUID id) {
        return repositoryWrapper.findTagById(id)
                .flatMap(tag -> {
                    Mono<Void> cleanup;
                    if (!tag.getImages().isEmpty()) {
                        cleanup = Flux.fromIterable(new HashSet<>(tag.getImages()))
                                .flatMap(image -> {
                                    image.getTags().remove(tag);
                                    return repositoryWrapper.saveImage(image);
                                })
                                .then(repositoryWrapper.deleteTag(tag));
                    } else {
                        cleanup = repositoryWrapper.deleteTag(tag);
                    }
                    return cleanup.then(Mono.<ResponseEntity<Void>>just(ResponseEntity.noContent().build()));
                })
                .switchIfEmpty(Mono.<ResponseEntity<Void>>just(ResponseEntity.notFound().build()));
    }
}
