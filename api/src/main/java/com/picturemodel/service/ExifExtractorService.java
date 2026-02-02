/**
 * App: Picture Model
 * Package: com.picturemodel.service
 * File: ExifExtractorService.java
 * Version: 0.1.0
 * Turns: 10
 * Author: Bobwares (bobwares@outlook.com)
 * Date: 2026-02-01T09:51:47Z
 * Exports: ExifExtractorService
 * Description: Service for extracting EXIF metadata from image streams. Methods: extract - parse EXIF/GPS data.
 */

package com.picturemodel.service;

import com.drew.imaging.ImageMetadataReader;
import com.drew.lang.GeoLocation;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.GpsDirectory;
import com.drew.metadata.jpeg.JpegDirectory;
import com.drew.metadata.png.PngDirectory;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

@Service
@Slf4j
public class ExifExtractorService {

    public ExifExtractionResult extract(InputStream inputStream) {
        if (inputStream == null) {
            return ExifExtractionResult.failed();
        }

        try {
            Metadata metadata = ImageMetadataReader.readMetadata(inputStream);
            Map<String, String> tags = new LinkedHashMap<>();

            LocalDateTime capturedAt = extractCapturedAt(metadata, tags);
            Dimension dimension = extractDimensions(metadata, tags);
            extractCameraInfo(metadata, tags);
            extractGpsInfo(metadata, tags);

            return new ExifExtractionResult(false, capturedAt, dimension.width, dimension.height, tags);
        } catch (Exception e) {
            log.warn("Failed to extract EXIF metadata", e);
            return ExifExtractionResult.failed();
        }
    }

    private LocalDateTime extractCapturedAt(Metadata metadata, Map<String, String> tags) {
        ExifSubIFDDirectory subIFD = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
        if (subIFD == null) {
            return null;
        }

        Date original = subIFD.getDateOriginal();
        if (original == null) {
            return null;
        }

        LocalDateTime capturedAt = LocalDateTime.ofInstant(original.toInstant(), ZoneId.systemDefault());
        tags.put("datetime.original", capturedAt.toString());
        return capturedAt;
    }

    private Dimension extractDimensions(Metadata metadata, Map<String, String> tags) {
        Integer width = null;
        Integer height = null;

        ExifSubIFDDirectory subIFD = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
        if (subIFD != null) {
            width = subIFD.getInteger(ExifSubIFDDirectory.TAG_EXIF_IMAGE_WIDTH);
            height = subIFD.getInteger(ExifSubIFDDirectory.TAG_EXIF_IMAGE_HEIGHT);
        }

        if (width == null || height == null) {
            JpegDirectory jpegDirectory = metadata.getFirstDirectoryOfType(JpegDirectory.class);
            if (jpegDirectory != null) {
                width = width != null ? width : jpegDirectory.getInteger(JpegDirectory.TAG_IMAGE_WIDTH);
                height = height != null ? height : jpegDirectory.getInteger(JpegDirectory.TAG_IMAGE_HEIGHT);
            }
        }

        if (width == null || height == null) {
            PngDirectory pngDirectory = metadata.getFirstDirectoryOfType(PngDirectory.class);
            if (pngDirectory != null) {
                width = width != null ? width : pngDirectory.getInteger(PngDirectory.TAG_IMAGE_WIDTH);
                height = height != null ? height : pngDirectory.getInteger(PngDirectory.TAG_IMAGE_HEIGHT);
            }
        }

        if (width != null && height != null) {
            tags.put("image.width", width.toString());
            tags.put("image.height", height.toString());
        }

        return new Dimension(width, height);
    }

    private void extractCameraInfo(Metadata metadata, Map<String, String> tags) {
        ExifIFD0Directory ifd0 = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
        if (ifd0 == null) {
            return;
        }

        putIfPresent(tags, "camera.make", ifd0.getString(ExifIFD0Directory.TAG_MAKE));
        putIfPresent(tags, "camera.model", ifd0.getString(ExifIFD0Directory.TAG_MODEL));
        putIfPresent(tags, "camera.orientation", ifd0.getString(ExifIFD0Directory.TAG_ORIENTATION));
    }

    private void extractGpsInfo(Metadata metadata, Map<String, String> tags) {
        GpsDirectory gpsDirectory = metadata.getFirstDirectoryOfType(GpsDirectory.class);
        if (gpsDirectory == null) {
            return;
        }

        GeoLocation location = gpsDirectory.getGeoLocation();
        if (location != null && !location.isZero()) {
            tags.put("gps.latitude", formatCoordinate(location.getLatitude()));
            tags.put("gps.longitude", formatCoordinate(location.getLongitude()));
        }

        Double altitude = gpsDirectory.getDoubleObject(GpsDirectory.TAG_ALTITUDE);
        if (altitude != null) {
            tags.put("gps.altitude", String.format(Locale.US, "%.2f", altitude));
        }
    }

    private void putIfPresent(Map<String, String> tags, String key, String value) {
        if (value != null && !value.isBlank()) {
            tags.put(key, value.trim());
        }
    }

    private String formatCoordinate(double coordinate) {
        return String.format(Locale.US, "%.6f", coordinate);
    }

    private record Dimension(Integer width, Integer height) {}

    @Getter
    public static class ExifExtractionResult {
        private final boolean failed;
        private final LocalDateTime capturedAt;
        private final Integer width;
        private final Integer height;
        private final Map<String, String> metadata;

        public ExifExtractionResult(
                boolean failed,
                LocalDateTime capturedAt,
                Integer width,
                Integer height,
                Map<String, String> metadata
        ) {
            this.failed = failed;
            this.capturedAt = capturedAt;
            this.width = width;
            this.height = height;
            this.metadata = metadata != null ? metadata : new LinkedHashMap<>();
        }

        public static ExifExtractionResult failed() {
            return new ExifExtractionResult(true, null, null, null, new LinkedHashMap<>());
        }
    }
}
