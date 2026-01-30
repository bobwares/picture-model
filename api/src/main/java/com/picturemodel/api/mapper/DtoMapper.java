/**
 * App: Picture Model
 * Package: com.picturemodel.api.mapper
 * File: DtoMapper.java
 * Version: 0.1.0
 * Turns: 5
 * Author: Bobwares (bobwares@outlook.com)
 * Date: 2026-01-30T02:03:52Z
 * Exports: DtoMapper
 * Description: class DtoMapper for DtoMapper responsibilities. Methods: toDto - to dto; toEntity - to entity; updateEntity - update entity.
 */

package com.picturemodel.api.mapper;

import com.picturemodel.api.dto.request.CreateDriveRequest;
import com.picturemodel.api.dto.request.UpdateDriveRequest;
import com.picturemodel.api.dto.response.RemoteFileDriveDto;
import com.picturemodel.domain.entity.RemoteFileDrive;
import com.picturemodel.domain.enums.ConnectionStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

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
}
