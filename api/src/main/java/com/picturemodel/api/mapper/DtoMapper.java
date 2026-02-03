/**
 * App: Picture Model
 * Package: com.picturemodel.api.mapper
 * File: DtoMapper.java
 * Version: 0.1.1
 * Turns: 5,15
 * Author: Bobwares (bobwares@outlook.com)
 * Date: 2026-02-03T04:54:36Z
 * Exports: DtoMapper
 * Description: Mapper for converting between entities and API DTOs. Methods: toDto - drive dto; toEntity - drive entity; updateEntity - update drive; toImageSummaryDto - image summary dto; toImageDetailDto - image detail dto.
 */

package com.picturemodel.api.mapper;

import com.picturemodel.api.dto.request.CreateDriveRequest;
import com.picturemodel.api.dto.request.UpdateDriveRequest;
import com.picturemodel.api.dto.response.ImageDto;
import com.picturemodel.api.dto.response.ImageMetadataDto;
import com.picturemodel.api.dto.response.RemoteFileDriveDto;
import com.picturemodel.api.dto.response.TagDto;
import com.picturemodel.domain.entity.Image;
import com.picturemodel.domain.entity.ImageMetadata;
import com.picturemodel.domain.entity.RemoteFileDrive;
import com.picturemodel.domain.entity.Tag;
import com.picturemodel.domain.enums.ConnectionStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Mapper for converting between entities and DTOs.
 */
@Component
public class DtoMapper {

    /**
     * Convert RemoteFileDrive entity to DTO.
     */
    public RemoteFileDriveDto toDto(RemoteFileDrive drive) {
        if (drive == null) {
            return null;
        }

        return RemoteFileDriveDto.builder()
                .id(drive.getId())
                .name(drive.getName())
                .type(drive.getType())
                .connectionUrl(drive.getConnectionUrl())
                .status(drive.getStatus())
                .rootPath(drive.getRootPath())
                .autoConnect(drive.getAutoConnect())
                .autoCrawl(drive.getAutoCrawl())
                .imageCount(drive.getImageCount())
                .lastConnected(drive.getLastConnected())
                .lastCrawled(drive.getLastCrawled())
                .createdDate(drive.getCreatedDate())
                .modifiedDate(drive.getModifiedDate())
                .build();
    }

    /**
     * Convert CreateDriveRequest to RemoteFileDrive entity.
     */
    public RemoteFileDrive toEntity(CreateDriveRequest request) {
        if (request == null) {
            return null;
        }

        LocalDateTime now = LocalDateTime.now();

        return RemoteFileDrive.builder()
                .name(request.getName())
                .type(request.getType())
                .connectionUrl(request.getConnectionUrl())
                .rootPath(request.getRootPath() != null ? request.getRootPath() : "/")
                .autoConnect(request.getAutoConnect() != null ? request.getAutoConnect() : false)
                .autoCrawl(request.getAutoCrawl() != null ? request.getAutoCrawl() : false)
                .status(ConnectionStatus.DISCONNECTED)
                .imageCount(0)
                .createdDate(now)
                .modifiedDate(now)
                .build();
    }

    /**
     * Update entity fields from UpdateDriveRequest.
     */
    public void updateEntity(RemoteFileDrive drive, UpdateDriveRequest request) {
        if (drive == null || request == null) {
            return;
        }

        if (request.getName() != null) {
            drive.setName(request.getName());
        }
        if (request.getConnectionUrl() != null) {
            drive.setConnectionUrl(request.getConnectionUrl());
        }
        if (request.getRootPath() != null) {
            drive.setRootPath(request.getRootPath());
        }
        if (request.getAutoConnect() != null) {
            drive.setAutoConnect(request.getAutoConnect());
        }
        if (request.getAutoCrawl() != null) {
            drive.setAutoCrawl(request.getAutoCrawl());
        }

        drive.setModifiedDate(LocalDateTime.now());
    }

    /**
     * Convert Image entity to an ImageDto suitable for list/grid responses.
     * This method intentionally does not read lazy collections like metadata/tags.
     */
    public ImageDto toImageSummaryDto(Image image) {
        return toImageDto(image, false);
    }

    /**
     * Convert Image entity to an ImageDto suitable for detail responses.
     * This method reads metadata/tags and therefore requires those associations to be initialized.
     */
    public ImageDto toImageDetailDto(Image image) {
        return toImageDto(image, true);
    }

    private ImageDto toImageDto(Image image, boolean includeDetails) {
        if (image == null) {
            return null;
        }

        RemoteFileDrive drive = image.getDrive();
        String rootPath = drive != null ? drive.getRootPath() : null;
        String filePath = image.getFilePath();

        String fullPath = null;
        if (rootPath != null && filePath != null) {
            fullPath = rootPath + (rootPath.endsWith("/") ? "" : "/") + filePath;
        } else if (filePath != null) {
            fullPath = filePath;
        }

        ImageDto.ImageDtoBuilder builder = ImageDto.builder()
                .id(image.getId())
                .driveId(drive != null ? drive.getId() : null)
                .driveName(drive != null ? drive.getName() : null)
                .fileName(image.getFileName())
                .filePath(filePath)
                .fullPath(fullPath)
                .fileSize(image.getFileSize())
                .fileHash(image.getFileHash())
                .mimeType(image.getMimeType())
                .width(image.getWidth())
                .height(image.getHeight())
                .capturedAt(image.getCapturedAt())
                .createdDate(image.getCreatedDate())
                .modifiedDate(image.getModifiedDate())
                .indexedDate(image.getIndexedDate())
                // Keep these relative so Next.js can proxy via rewrites (/api/* -> backend).
                .imageUrl(image.getId() != null ? "/api/files/" + image.getId() : null)
                .thumbnailUrl(image.getId() != null ? "/api/files/" + image.getId() + "/thumbnail?size=medium" : null);

        if (!includeDetails) {
            return builder.build();
        }

        List<ImageMetadataDto> metadata = image.getMetadata() == null
                ? null
                : image.getMetadata().stream()
                .filter(Objects::nonNull)
                .map(this::toMetadataDto)
                .toList();

        List<TagDto> tags = image.getTags() == null
                ? null
                : image.getTags().stream()
                .filter(Objects::nonNull)
                .map(this::toTagDto)
                .toList();

        return builder.metadata(metadata).tags(tags).build();
    }

    private ImageMetadataDto toMetadataDto(ImageMetadata metadata) {
        if (metadata == null) {
            return null;
        }
        return ImageMetadataDto.builder()
                .id(metadata.getId())
                .key(metadata.getKey())
                .value(metadata.getValue_entry())
                .source(metadata.getSource())
                .lastModified(metadata.getLastModified())
                .build();
    }

    private TagDto toTagDto(Tag tag) {
        if (tag == null) {
            return null;
        }
        return TagDto.builder()
                .id(tag.getId())
                .name(tag.getName())
                .color(tag.getColor())
                .usageCount(tag.getUsageCount())
                .createdDate(tag.getCreatedDate())
                .build();
    }
}
