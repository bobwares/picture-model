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
import com.picturemodel.domain.repository.ImageRepository;
import com.picturemodel.infrastructure.filesystem.FileSystemProvider;
import com.picturemodel.service.ConnectionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.UUID;

/**
 * Serves image files and thumbnails by reading from the connected drive
 * via the cached FileSystemProvider.  Thumbnails are generated on the fly
 * using Thumbnailator; no disk cache is used at this stage.
 */
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Slf4j
public class FileController {

    private final ImageRepository imageRepository;
    private final ConnectionManager connectionManager;

    private static final Map<String, Integer> THUMBNAIL_SIZES = Map.of(
            "small",  150,
            "medium", 250,
            "large",  350
    );

    /**
     * Stream the full-resolution image.
     * GET /api/files/{imageId}
     */
    @GetMapping("/{imageId}")
    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> getImage(@PathVariable UUID imageId) {
        Image image = loadImage(imageId);

        try {
            FileSystemProvider provider = connectionManager.getProvider(image.getDrive().getId());
            try (InputStream in = provider.readFile(image.getFilePath())) {
                byte[] bytes = in.readAllBytes();
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(image.getMimeType()))
                        .contentLength(bytes.length)
                        .body(bytes);
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to read image {}", imageId, e);
            throw new RuntimeException("Failed to read image: " + e.getMessage(), e);
        }
    }

    /**
     * Stream a resized thumbnail.  The original image is read from the drive
     * and resized in memory via Thumbnailator.  Output format matches the
     * source mime type; TIFF and BMP are downgraded to JPEG.
     * GET /api/files/{imageId}/thumbnail?size=small|medium|large
     */
    @GetMapping("/{imageId}/thumbnail")
    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> getThumbnail(
            @PathVariable UUID imageId,
            @RequestParam(defaultValue = "medium") String size) {

        Image image = loadImage(imageId);
        int targetSize = THUMBNAIL_SIZES.getOrDefault(size, 250);
        String outputFormat  = outputFormat(image.getMimeType());
        String contentType   = outputContentType(image.getMimeType());

        try {
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
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to generate thumbnail for image {}", imageId, e);
            throw new RuntimeException("Failed to generate thumbnail: " + e.getMessage(), e);
        }
    }

    // ── helpers ───────────────────────────────────────────────────────────

    private Image loadImage(UUID imageId) {
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new IllegalArgumentException("Image not found: " + imageId));

        if (Boolean.TRUE.equals(image.getDeleted())) {
            throw new IllegalArgumentException("Image not found: " + imageId);
        }
        return image;
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
