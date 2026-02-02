/**
 * App: Picture Model
 * Package: com.picturemodel.service
 * File: CrawlerJobRunner.java
 * Version: 0.1.9
 * Turns: 8,9,10,22,25,26
 * Author: Bobwares (bobwares@outlook.com)
 * Date: 2026-02-02T20:20:10Z
 * Exports: CrawlerJobRunner
 * Description: Async crawl job executor for indexing files into Image records.
 * CrawlerJobRunner - traverses file trees, updates crawl job status, and persists images.
 */

package com.picturemodel.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.picturemodel.domain.entity.CrawlJob;
import com.picturemodel.domain.entity.Image;
import com.picturemodel.domain.entity.ImageMetadata;
import com.picturemodel.domain.entity.RemoteFileDrive;
import com.picturemodel.domain.enums.CrawlStatus;
import com.picturemodel.domain.enums.MetadataSource;
import com.picturemodel.domain.repository.CrawlJobRepository;
import com.picturemodel.domain.repository.ImageMetadataRepository;
import com.picturemodel.domain.repository.ImageRepository;
import com.picturemodel.domain.repository.RemoteFileDriveRepository;
import com.picturemodel.infrastructure.filesystem.FileInfo;
import com.picturemodel.infrastructure.filesystem.FileSystemProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
@RequiredArgsConstructor
@Slf4j
public class CrawlerJobRunner {

    private static final int SAVE_INTERVAL = 5;
    private static final Set<String> IGNORED_ROOT_NAMES = Set.of(
            "$RECYCLE.BIN",
            "SYSTEM VOLUME INFORMATION",
            ".TRASHES",
            ".SPOTLIGHT-V100",
            "RECYCLER",
            "$WINDOWS.~BT",
            "$WINDOWS.~WS",
            "RECOVERY",
            "MSOCACHE",
            "PERFLOGS",
            "WINDOWSIMAGEBACKUP",
            "CONFIG.MSI",
            "FOUND.000",
            "FOUND.001"
    );

    private final CrawlJobRepository crawlJobRepository;
    private final RemoteFileDriveRepository driveRepository;
    private final ImageRepository imageRepository;
    private final ImageMetadataRepository imageMetadataRepository;
    private final ConnectionManager connectionManager;
    private final ExifExtractorService exifExtractorService;
    private final ObjectMapper objectMapper;

    private final Map<UUID, AtomicBoolean> cancelFlags = new ConcurrentHashMap<>();

    public void requestCancel(UUID jobId) {
        cancelFlags.computeIfAbsent(jobId, id -> new AtomicBoolean(false)).set(true);
    }

    @Async("taskExecutor")
    public void runJob(UUID jobId, boolean extractExif) {
        Optional<CrawlJob> jobOptional = crawlJobRepository.findById(jobId);
        if (jobOptional.isEmpty()) {
            log.warn("Crawl job not found: {}", jobId);
            return;
        }

        CrawlJob job = jobOptional.get();
        UUID driveId = job.getDrive() != null ? job.getDrive().getId() : null;
        if (driveId == null) {
            log.error("Crawl job {} missing drive reference", jobId);
            return;
        }

        RemoteFileDrive drive = driveRepository.findById(driveId)
                .orElseThrow(() -> new IllegalArgumentException("Drive not found: " + driveId));
        job.setDrive(drive);

        cancelFlags.computeIfAbsent(jobId, id -> new AtomicBoolean(false));

        try {
            job.setStatus(CrawlStatus.IN_PROGRESS);
            job.setStartTime(job.getStartTime() == null ? LocalDateTime.now() : job.getStartTime());
            crawlJobRepository.save(job);

            FileSystemProvider provider = connectionManager.getProvider(driveId);
            String startPath = normalizeRoot(job.getRootPath(), drive.getRootPath());
            boolean incremental = Boolean.TRUE.equals(job.getIsIncremental());
            LocalDateTime lastCrawled = drive.getLastCrawled();

            Set<String> visitedPaths = incremental ? null : new HashSet<>();
            Counter counter = new Counter();

            crawlPath(provider, job, startPath, startPath, incremental, lastCrawled, visitedPaths, counter, extractExif);

            if (isCancelled(jobId)) {
                job.setStatus(CrawlStatus.CANCELLED);
                if (job.getEndTime() == null) {
                    job.setEndTime(LocalDateTime.now());
                }
            } else {
                job.setStatus(CrawlStatus.COMPLETED);
                job.setEndTime(LocalDateTime.now());
                drive.setLastCrawled(LocalDateTime.now());
            }

            if (!incremental && visitedPaths != null) {
                markDeletedImages(driveId, visitedPaths, job);
            }

            long imageCount = imageRepository.countByDrive_IdAndDeletedFalse(driveId);
            drive.setImageCount((int) imageCount);
            driveRepository.save(drive);

            crawlJobRepository.save(job);
        } catch (Exception e) {
            log.error("Crawl job {} failed", jobId, e);
            job.setStatus(CrawlStatus.FAILED);
            job.setEndTime(LocalDateTime.now());
            appendError(job, e.getMessage());
            crawlJobRepository.save(job);
        } finally {
            cancelFlags.remove(jobId);
        }
    }

    private void crawlPath(
            FileSystemProvider provider,
            CrawlJob job,
            String rootPath,
            String relativePath,
            boolean incremental,
            LocalDateTime lastCrawled,
            Set<String> visitedPaths,
            Counter counter,
            boolean extractExif
    ) throws Exception {
        if (isCancelled(job.getId())) {
            return;
        }

        if (isIgnoredPath(relativePath)) {
            return;
        }

        job.setCurrentPathValue(relativePath.isEmpty() ? rootPath : relativePath);
        maybeSave(job, counter);

        List<FileInfo> entries;
        try {
            entries = provider.listDirectory(relativePath.isEmpty() ? "" : relativePath);
        } catch (Exception e) {
            String targetPath = relativePath.isEmpty() ? rootPath : relativePath;
            log.warn("Skipping unreadable path '{}' during crawl job {}", targetPath, job.getId(), e);
            appendError(job, "Failed to list path '" + targetPath + "': " + e.getMessage());
            if (relativePath.isEmpty() || relativePath.equals(rootPath)) {
                throw e;
            }
            return;
        }

        for (FileInfo entry : entries) {
            if (isCancelled(job.getId())) {
                return;
            }

            String entryName = entry.getName();
            if (entryName == null || entryName.isEmpty()) {
                continue;
            }

            boolean isDirectory = Boolean.TRUE.equals(entry.getIsDirectory());
            String childRelativePath = relativePath.isEmpty() ? entryName : relativePath + "/" + entryName;

            if (isDirectory) {
                if (isIgnoredPath(childRelativePath) || isIgnoredName(entryName)) {
                    continue;
                }
                crawlPath(provider, job, rootPath, childRelativePath, incremental, lastCrawled, visitedPaths, counter, extractExif);
                continue;
            }

            if (!isImageFile(entry)) {
                continue;
            }

            if (visitedPaths != null) {
                visitedPaths.add(childRelativePath);
            }

            LocalDateTime lastModified = entry.getLastModified();
            if (incremental && lastCrawled != null && lastModified != null && !lastModified.isAfter(lastCrawled)) {
                Optional<Image> existing = imageRepository.findByDrive_IdAndFilePath(job.getDrive().getId(), childRelativePath);
                if (existing.isPresent()) {
                    continue;
                }
            }

            upsertImage(provider, job, entry, childRelativePath, extractExif);
            job.setFilesProcessed(job.getFilesProcessed() + 1);
            maybeSave(job, counter);
        }
    }

    private void upsertImage(
            FileSystemProvider provider,
            CrawlJob job,
            FileInfo entry,
            String relativePath,
            boolean extractExif
    ) throws Exception {
        UUID driveId = job.getDrive().getId();
        Optional<Image> existing = imageRepository.findByDrive_IdAndFilePath(driveId, relativePath);

        if (existing.isPresent()) {
            Image image = existing.get();
            boolean changed = false;
            if (entry.getSize() != null && !entry.getSize().equals(image.getFileSize())) {
                image.setFileSize(entry.getSize());
                changed = true;
            }
            if (entry.getLastModified() != null && !entry.getLastModified().equals(image.getModifiedDate())) {
                image.setModifiedDate(entry.getLastModified());
                changed = true;
            }
            if (entry.getMimeType() != null && !entry.getMimeType().equals(image.getMimeType())) {
                image.setMimeType(entry.getMimeType());
                changed = true;
            }
            if (changed) {
                image.setFileHash(computeHash(provider, relativePath));
                image.setDeleted(false);
                imageRepository.save(image);
                job.setFilesUpdated(job.getFilesUpdated() + 1);
            }
            if (extractExif && shouldExtractExifForExisting(image, changed)) {
                ExifExtractorService.ExifExtractionResult exifData = extractExif(provider, relativePath);
                if (!exifData.isFailed()) {
                    applyExifToImage(image, exifData);
                    imageRepository.save(image);
                    replaceMetadata(image, exifData);
                }
            }
            return;
        }

        String fileName = entry.getName();
        if (fileName == null || fileName.isBlank()) {
            fileName = extractFileName(relativePath);
        }
        String mimeType = entry.getMimeType() != null ? entry.getMimeType() : guessContentType(fileName);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime modified = entry.getLastModified() != null ? entry.getLastModified() : now;

        Image image = Image.builder()
                .drive(job.getDrive())
                .fileName(fileName)
                .filePath(relativePath)
                .fileSize(entry.getSize() != null ? entry.getSize() : 0L)
                .fileHash(computeHash(provider, relativePath))
                .mimeType(mimeType != null ? mimeType : "application/octet-stream")
                .createdDate(modified)
                .modifiedDate(modified)
                .indexedDate(LocalDateTime.now())
                .deleted(false)
                .build();

        if (extractExif) {
            ExifExtractorService.ExifExtractionResult exifData = extractExif(provider, relativePath);
            if (!exifData.isFailed()) {
                applyExifToImage(image, exifData);
            }
            imageRepository.save(image);
            if (!exifData.isFailed()) {
                replaceMetadata(image, exifData);
            }
        } else {
            imageRepository.save(image);
        }
        job.setFilesAdded(job.getFilesAdded() + 1);
    }

    private ExifExtractorService.ExifExtractionResult extractExif(FileSystemProvider provider, String relativePath) {
        try (InputStream inputStream = provider.readFile(relativePath)) {
            return exifExtractorService.extract(inputStream);
        } catch (Exception e) {
            log.warn("Failed to read image for EXIF extraction: {}", relativePath, e);
            return ExifExtractorService.ExifExtractionResult.failed();
        }
    }

    private void applyExifToImage(Image image, ExifExtractorService.ExifExtractionResult exifData) {
        image.setCapturedAt(exifData.getCapturedAt());
        image.setWidth(exifData.getWidth());
        image.setHeight(exifData.getHeight());
    }

    private void replaceMetadata(Image image, ExifExtractorService.ExifExtractionResult exifData) {
        imageMetadataRepository.deleteByImageIdAndSource(image.getId(), MetadataSource.EXIF);
        if (exifData.getMetadata().isEmpty()) {
            return;
        }
        List<ImageMetadata> metadataEntries = new ArrayList<>();
        for (Map.Entry<String, String> entry : exifData.getMetadata().entrySet()) {
            if (entry.getValue() == null || entry.getValue().isBlank()) {
                continue;
            }
            metadataEntries.add(ImageMetadata.builder()
                    .image(image)
                    .key(entry.getKey())
                    .value_entry(entry.getValue())
                    .source(MetadataSource.EXIF)
                    .build());
        }
        if (!metadataEntries.isEmpty()) {
            imageMetadataRepository.saveAll(metadataEntries);
        }
    }

    private boolean shouldExtractExifForExisting(Image image, boolean changed) {
        if (changed) {
            return true;
        }
        return !imageMetadataRepository.existsByImageIdAndSource(image.getId(), MetadataSource.EXIF);
    }

    private void markDeletedImages(UUID driveId, Set<String> visitedPaths, CrawlJob job) {
        List<Image> images = imageRepository.findAllByDrive_Id(driveId);
        for (Image image : images) {
            if (Boolean.TRUE.equals(image.getDeleted())) {
                continue;
            }
            if (!visitedPaths.contains(image.getFilePath())) {
                image.setDeleted(true);
                imageRepository.save(image);
                job.setFilesDeleted(job.getFilesDeleted() + 1);
            }
        }
    }

    private boolean isImageFile(FileInfo entry) {
        if (entry.getMimeType() != null) {
            return entry.getMimeType().startsWith("image/");
        }
        return guessContentType(entry.getName()) != null;
    }

    private String guessContentType(String fileName) {
        if (fileName == null) {
            return null;
        }
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".gif")) return "image/gif";
        if (lower.endsWith(".bmp")) return "image/bmp";
        if (lower.endsWith(".webp")) return "image/webp";
        if (lower.endsWith(".tiff") || lower.endsWith(".tif")) return "image/tiff";
        if (lower.endsWith(".heic")) return "image/heic";
        if (lower.endsWith(".heif")) return "image/heif";
        return null;
    }

    private String computeHash(FileSystemProvider provider, String relativePath) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (InputStream inputStream = provider.readFile(relativePath)) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
            }
        }
        byte[] hash = digest.digest();
        StringBuilder builder = new StringBuilder();
        for (byte b : hash) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }

    private String extractFileName(String relativePath) {
        if (relativePath == null || relativePath.isBlank()) {
            return "";
        }
        String normalized = relativePath.replace("\\", "/");
        int lastSlash = normalized.lastIndexOf('/');
        return lastSlash >= 0 ? normalized.substring(lastSlash + 1) : normalized;
    }

    private void appendError(CrawlJob job, String message) {
        if (message == null || message.isBlank()) {
            return;
        }
        List<String> errors = new ArrayList<>();
        if (job.getErrors() != null && !job.getErrors().isBlank()) {
            try {
                errors = objectMapper.readValue(job.getErrors(), new TypeReference<List<String>>() {});
            } catch (Exception e) {
                log.warn("Failed to parse crawl job errors", e);
            }
        }
        errors.add(message);
        try {
            job.setErrors(objectMapper.writeValueAsString(errors));
        } catch (Exception e) {
            log.warn("Failed to serialize crawl job errors", e);
        }
    }

    private boolean isCancelled(UUID jobId) {
        AtomicBoolean flag = cancelFlags.get(jobId);
        return flag != null && flag.get();
    }

    private boolean isIgnoredPath(String path) {
        if (path == null || path.isBlank()) {
            return false;
        }
        String normalized = path.replace('\\', '/');
        String[] parts = normalized.split("/");
        for (String part : parts) {
            if (isIgnoredName(part)) {
                return true;
            }
        }
        return false;
    }

    private boolean isIgnoredName(String name) {
        if (name == null) {
            return false;
        }
        String trimmed = name.trim();
        String upper = trimmed.toUpperCase();

        // Check against known system directories
        if (IGNORED_ROOT_NAMES.contains(upper)) {
            return true;
        }

        // Skip hidden files/directories (starting with .)
        if (trimmed.startsWith(".")) {
            return true;
        }

        // Skip temp files and Office lock files
        if (trimmed.startsWith("~$") || trimmed.startsWith("~")) {
            return true;
        }

        // Skip thumbs.db and desktop.ini
        if (upper.equals("THUMBS.DB") || upper.equals("DESKTOP.INI")) {
            return true;
        }

        return false;
    }

    private String normalizeRoot(String rootPath, String driveRootPath) {
        if (rootPath == null) {
            return "";
        }
        String trimmed = rootPath.trim();
        if (trimmed.isEmpty() || "/".equals(trimmed)) {
            return "";
        }
        String driveRoot = driveRootPath != null ? driveRootPath.trim() : "";
        if (!driveRoot.isEmpty()) {
            String normalizedRoot = stripTrailingSeparators(trimmed);
            String normalizedDriveRoot = stripTrailingSeparators(driveRoot);
            if (normalizedRoot.equals(normalizedDriveRoot)) {
                return "";
            }
            if (normalizedRoot.startsWith(normalizedDriveRoot + "/")
                    || normalizedRoot.startsWith(normalizedDriveRoot + "\\")) {
                trimmed = normalizedRoot.substring(normalizedDriveRoot.length());
            }
        }
        if (trimmed.startsWith("/") || trimmed.startsWith("\\")) {
            return trimmed.substring(1);
        }
        return trimmed;
    }

    private String stripTrailingSeparators(String path) {
        if (path == null || path.isEmpty()) {
            return "";
        }
        int end = path.length();
        while (end > 1) {
            char c = path.charAt(end - 1);
            if (c == '/' || c == '\\') {
                end -= 1;
            } else {
                break;
            }
        }
        return path.substring(0, end);
    }

    private void maybeSave(CrawlJob job, Counter counter) {
        counter.increment();
        if (counter.shouldSave()) {
            crawlJobRepository.save(job);
        }
    }

    private static final class Counter {
        private int steps = 0;

        void increment() {
            steps += 1;
        }

        boolean shouldSave() {
            if (steps >= SAVE_INTERVAL) {
                steps = 0;
                return true;
            }
            return false;
        }
    }
}
