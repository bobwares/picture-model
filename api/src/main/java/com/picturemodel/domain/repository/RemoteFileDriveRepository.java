/**
 * App: Picture Model
 * Package: com.picturemodel.domain.repository
 * File: RemoteFileDriveRepository.java
 * Version: 0.1.0
 * Turns: 5
 * Author: Bobwares (bobwares@outlook.com)
 * Date: 2026-01-30T02:03:52Z
 * Exports: RemoteFileDriveRepository
 * Description: interface RemoteFileDriveRepository for RemoteFileDriveRepository responsibilities. Methods: findByStatus - find by status; findByAutoConnectTrue - find by auto connect true; countByStatus - count by status.
 */

package com.picturemodel.domain.repository;

import com.picturemodel.domain.entity.RemoteFileDrive;
import com.picturemodel.domain.enums.ConnectionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for RemoteFileDrive entities.
 *
 * @author Claude (AI Coding Agent)
 */
@Repository
public interface RemoteFileDriveRepository extends JpaRepository<RemoteFileDrive, UUID> {

    /**
     * Find all drives with a specific connection status.
     */
    List<RemoteFileDrive> findByStatus(ConnectionStatus status);

    /**
     * Find all drives that have autoConnect enabled.
     */
    List<RemoteFileDrive> findByAutoConnectTrue();

    /**
     * Count drives with a specific connection status.
     */
    long countByStatus(ConnectionStatus status);
}
