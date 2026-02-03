/**
 * App: Picture Model
 * Package: com.picturemodel.api.controller
 * File: ImageController.java
 * Version: 0.1.2
 * Turns: 5,15
 * Author: Bobwares (bobwares@outlook.com)
 * Date: 2026-02-03T04:55:46Z
 * Exports: ImageController
 * Description: REST controller for image search and listing.
 * ImageController - provides search and detail endpoints with DTO responses to avoid lazy-loading serialization errors.
 */

package com.picturemodel.api.controller;

import com.picturemodel.api.dto.response.ImageDto;
import com.picturemodel.api.mapper.DtoMapper;
import com.picturemodel.domain.entity.Image;
import com.picturemodel.service.ReactiveRepositoryWrapper;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST controller for image search and list operations (WebFlux).
 * Base path: /api/images
 */
@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
@Slf4j
public class ImageController {

    private final ReactiveRepositoryWrapper repositoryWrapper;
    private final DtoMapper dtoMapper;

    /**
     * Search/list images with optional filters.
     * GET /api/images
     */
    @GetMapping
    public Mono<ResponseEntity<Map<String, Object>>> searchImages(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) UUID driveId,
            @RequestParam(required = false) List<UUID> tagIds,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            @RequestParam(defaultValue = "date") String sort,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "24") @Min(1) int size) {
        log.info("Searching images: query={}, driveId={}, tagIds={}", query, driveId, tagIds);

        Pageable pageable = PageRequest.of(page, size, resolveSort(sort));
        Specification<Image> spec = buildSpecification(query, driveId, tagIds, fromDate, toDate);

        return repositoryWrapper.findImagesBySpecification(spec, pageable)
                .map(imagesPage -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("content", imagesPage.getContent().stream()
                            .map(dtoMapper::toImageSummaryDto)
                            .toList());
                    response.put("totalElements", imagesPage.getTotalElements());
                    response.put("totalPages", imagesPage.getTotalPages());
                    response.put("currentPage", imagesPage.getNumber());
                    response.put("size", imagesPage.getSize());
                    return ResponseEntity.ok(response);
                });
    }

    /**
     * Get image details by ID.
     * GET /api/images/{id}
     */
    @GetMapping("/{id}")
    public Mono<ResponseEntity<ImageDto>> getImage(@PathVariable UUID id) {
        return repositoryWrapper.findImageDetailById(id)
                .map(dtoMapper::toImageDetailDto)
                .map(ResponseEntity::ok)
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }

    private Specification<Image> buildSpecification(
            String query,
            UUID driveId,
            List<UUID> tagIds,
            String fromDate,
            String toDate
    ) {
        return (root, queryObj, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(builder.isFalse(root.get("deleted")));

            if (driveId != null) {
                predicates.add(builder.equal(root.get("drive").get("id"), driveId));
            }

            if (query != null && !query.isBlank()) {
                String pattern = "%" + query.toLowerCase() + "%";
                Predicate fileNameLike = builder.like(builder.lower(root.get("fileName")), pattern);
                Predicate filePathLike = builder.like(builder.lower(root.get("filePath")), pattern);
                predicates.add(builder.or(fileNameLike, filePathLike));
            }

            if (tagIds != null && !tagIds.isEmpty()) {
                Join<Object, Object> tagJoin = root.join("tags", JoinType.LEFT);
                predicates.add(tagJoin.get("id").in(tagIds));
                queryObj.distinct(true);
            }

            LocalDateTime from = parseDateStart(fromDate);
            if (from != null) {
                predicates.add(builder.greaterThanOrEqualTo(root.get("modifiedDate"), from));
            }

            LocalDateTime to = parseDateEnd(toDate);
            if (to != null) {
                predicates.add(builder.lessThanOrEqualTo(root.get("modifiedDate"), to));
            }

            return builder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private LocalDateTime parseDateStart(String date) {
        if (date == null || date.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(date).atStartOfDay();
        } catch (Exception e) {
            return null;
        }
    }

    private LocalDateTime parseDateEnd(String date) {
        if (date == null || date.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(date).atTime(LocalTime.MAX);
        } catch (Exception e) {
            return null;
        }
    }

    private Sort resolveSort(String sort) {
        if (sort == null) {
            return Sort.by(Sort.Direction.DESC, "modifiedDate");
        }
        switch (sort) {
            case "name":
                return Sort.by(Sort.Direction.ASC, "fileName");
            case "size":
                return Sort.by(Sort.Direction.DESC, "fileSize");
            case "relevance":
            case "date":
            default:
                return Sort.by(Sort.Direction.DESC, "modifiedDate");
        }
    }
}
