/**
 * App: Picture Model
 * Package: com.picturemodel.api.controller
 * File: FileController.java
 * Version: 0.1.0
 * Turns: 9
 * Author: Bobwares (bobwares@outlook.com)
 * Date: 2026-01-31T03:30:00Z
 * Exports: FileController
 * Description: REST controller for streaming image files and on-the-fly thumbnails from connected drives.
 */

package com.picturemodel.api.controller;

import com.picturemodel.domain.entity.Image;
import com.picturemodel.infrastructure.filesystem.FileSystemProvider;
import com.picturemodel.service.ConnectionManager;
import com.picturemodel.service.ReactiveRepositoryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.UUID;

/**
 * Serves image files and thumbnails by reading from the connected drive
 * via the cached FileSystemProvider (WebFlux).
 * Thumbnails are generated on the fly using Thumbnailator.
 */
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Slf4j
public class FileController {

    private final ReactiveRepositoryWrapper repositoryWrapper;
    private final ConnectionManager connectionManager;

    private static final Map<String, Integer> THUMBNAIL_SIZES = Map.of(
            "small",  150,
            "medium", 250,
            "large",  350
    );

    /**
     * Stream the full-resolution image (reactive, blocking I/O on boundedElastic).
     * GET /api/files/{imageId}
     */
    @GetMapping("/{imageId}")
    public Mono<ResponseEntity<byte[]>> getImage(@PathVariable UUID imageId) {
        return loadImage(imageId)
                .flatMap(image -> Mono.fromCallable(() -> {
                    FileSystemProvider provider = connectionManager.getProvider(image.getDrive().getId());
                    try (InputStream in = provider.readFile(image.getFilePath())) {
                        byte[] bytes = in.readAllBytes();
                        return ResponseEntity.ok()
                                .contentType(MediaType.parseMediaType(image.getMimeType()))
                                .contentLength(bytes.length)
                                .body(bytes);
                    }
                }).subscribeOn(Schedulers.boundedElastic()))
                .onErrorResume(IllegalArgumentException.class, e -> Mono.error(e))
                .onErrorResume(Exception.class, e -> {
                    log.error("Failed to read image {}", imageId, e);
                    return Mono.error(new RuntimeException("Failed to read image: " + e.getMessage(), e));
                });
    }

    /**
     * Stream a resized thumbnail (reactive, blocking I/O on boundedElastic).
     * The original image is read from the drive and resized in memory via Thumbnailator.
     * Output format matches the source mime type; TIFF and BMP are downgraded to JPEG.
     * GET /api/files/{imageId}/thumbnail?size=small|medium|large
     */
    @GetMapping("/{imageId}/thumbnail")
    public Mono<ResponseEntity<byte[]>> getThumbnail(
            @PathVariable UUID imageId,
            @RequestParam(defaultValue = "medium") String size) {

        return loadImage(imageId)
                .flatMap(image -> {
                    int targetSize = THUMBNAIL_SIZES.getOrDefault(size, 250);
                    String outputFormat = outputFormat(image.getMimeType());
                    String contentType = outputContentType(image.getMimeType());

                    return Mono.fromCallable(() -> {
                        FileSystemProvider provider = connectionManager.getProvider(image.getDrive().getId());
                        try (InputStream in = provider.readFile(image.getFilePath())) {
                            ByteArrayOutputStream out = new ByteArrayOutputStream();
                            Thumbnails.of(in)
                                    .size(targetSize, targetSize)
                                    .keepAspectRatio(true)
                                    .outputFormat(outputFormat)
                                    .toOutputStream(out);

                            byte[] bytes = out.toByteArray();
                            return ResponseEntity.ok()
                                    .contentType(MediaType.parseMediaType(contentType))
                                    .contentLength(bytes.length)
                                    .body(bytes);
                        }
                    }).subscribeOn(Schedulers.boundedElastic());
                })
                .onErrorResume(IllegalArgumentException.class, e -> Mono.error(e))
                .onErrorResume(Exception.class, e -> {
                    log.error("Failed to generate thumbnail for image {}", imageId, e);
                    return Mono.error(new RuntimeException("Failed to generate thumbnail: " + e.getMessage(), e));
                });
    }

    // ── helpers ───────────────────────────────────────────────────────────

    private Mono<Image> loadImage(UUID imageId) {
        return repositoryWrapper.findImageById(imageId)
                .flatMap(image -> {
                    if (Boolean.TRUE.equals(image.getDeleted())) {
                        return Mono.error(new IllegalArgumentException("Image not found: " + imageId));
                    }
                    return Mono.just(image);
                })
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Image not found: " + imageId)));
    }

    /**
     * Thumbnailator output-format token derived from the source mime type.
     * TIFF and BMP have no good browser support, so they fall through to JPEG.
     */
    private static String outputFormat(String mimeType) {
        if (mimeType == null) return "jpg";
        return switch (mimeType) {
            case "image/png"  -> "png";
            case "image/gif"  -> "gif";
            case "image/webp" -> "webp";
            default           -> "jpg";
        };
    }

    /**
     * HTTP Content-Type that matches the output format chosen above.
     */
    private static String outputContentType(String mimeType) {
        if (mimeType == null) return "image/jpeg";
        return switch (mimeType) {
            case "image/png"  -> "image/png";
            case "image/gif"  -> "image/gif";
            case "image/webp" -> "image/webp";
            default           -> "image/jpeg";
        };
    }
}
