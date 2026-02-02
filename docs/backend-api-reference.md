/**
 * App: Picture Model
 * Package: docs
 * File: backend-api-reference.md
 * Version: 0.1.0
 * Turns: 27
 * Author: Claude Sonnet 4.5
 * Date: 2026-02-02T21:00:00Z
 * Exports: None
 * Description: Comprehensive reference for backend REST controllers and services.
 */

# Backend API & Service Reference

This document provides complete JavaDoc-style documentation for all REST controllers and business services in the Picture Model backend.

---

## Table of Contents

### Controllers
1. [DriveController](#1-drivecontroller) - Drive management and file operations
2. [CrawlerController](#2-crawlercontroller) - Crawl job management
3. [ImageController](#3-imagecontroller) - Image search and retrieval
4. [TagController](#4-tagcontroller) - Tag CRUD operations
5. [FileController](#5-filecontroller) - Image streaming and thumbnails
6. [SystemController](#6-systemcontroller) - System status and health

### Services
7. [DriveService](#7-driveservice) - Drive business logic
8. [CrawlerService](#8-crawlerservice) - Crawl lifecycle management
9. [CrawlerJobRunner](#9-crawlerjobrunner) - Async crawl execution
10. [ConnectionManager](#10-connectionmanager) - File system connections
11. [ExifExtractorService](#11-exifextractorservice) - EXIF metadata parsing

---

## Controllers

## 1) DriveController

**Package:** `com.picturemodel.api.controller`
**Base Path:** `/api/drives`
**File:** `DriveController.java`

### Purpose
Manages remote file drive lifecycle: creation, connection, disconnection, deletion, and querying drive images.

### Dependencies
- `DriveService` - Business logic for drive operations
- `ImageRepository` - Queries for drive images
- `DtoMapper` - Entity ↔ DTO conversions
- `ObjectMapper` - JSON parsing for credentials

### Endpoints

#### POST /api/drives
**Create a new drive**

**Request Body:** `CreateDriveRequest`
```json
{
  "name": "My Drive",
  "type": "SMB|SFTP|FTP|LOCAL",
  "connectionUrl": "smb://server/share/",
  "rootPath": "/",
  "autoConnect": false,
  "autoCrawl": false,
  "credentials": "{\"username\":\"user\",\"password\":\"pass\",\"domain\":\"WORKGROUP\"}"
}
```

**Response:** `200 OK` with `RemoteFileDriveDto`

**Validation:**
- `name`: required, max 200 chars
- `type`: required enum
- `connectionUrl`: required, max 500 chars
- `credentials`: optional, must be valid JSON

---

#### GET /api/drives
**List all drives**

**Response:** `200 OK` with `List<RemoteFileDriveDto>`

---

#### GET /api/drives/{id}
**Get drive by ID**

**Path Parameters:**
- `id` (UUID) - Drive ID

**Response:** `200 OK` with `RemoteFileDriveDto`

---

#### PUT /api/drives/{id}
**Update drive settings**

**Path Parameters:**
- `id` (UUID) - Drive ID

**Request Body:** `UpdateDriveRequest`
```json
{
  "name": "Updated Name",
  "connectionUrl": "smb://new-server/share/",
  "rootPath": "/photos",
  "autoConnect": true,
  "autoCrawl": true,
  "credentials": "{\"username\":\"newuser\",\"password\":\"newpass\"}"
}
```

**Response:** `200 OK` with `RemoteFileDriveDto`

**Notes:**
- Drive type cannot be changed
- If connection parameters change, drive is automatically disconnected
- Empty credentials field preserves existing credentials

---

#### DELETE /api/drives/{id}
**Delete a drive**

**Path Parameters:**
- `id` (UUID) - Drive ID

**Response:** `204 No Content`

**Side Effects:**
- Disconnects drive if connected
- Deletes all associated images and crawl jobs (cascade)

---

#### POST /api/drives/{id}/connect
**Connect to a drive**

**Path Parameters:**
- `id` (UUID) - Drive ID

**Response:** `200 OK` with `RemoteFileDriveDto`

**Process:**
1. Sets status to `CONNECTING`
2. Creates FileSystemProvider via ConnectionManager
3. Calls `provider.connect()`
4. Sets status to `CONNECTED` on success
5. Sets status to `ERROR` on failure

**Errors:**
- `500` - Connection failed (e.g., authentication, network, host unreachable)

---

#### POST /api/drives/{id}/disconnect
**Disconnect from a drive**

**Path Parameters:**
- `id` (UUID) - Drive ID

**Response:** `200 OK` with `RemoteFileDriveDto`

**Side Effects:**
- Closes file system connection
- Sets status to `DISCONNECTED`

---

#### GET /api/drives/{id}/status
**Get current drive status**

**Path Parameters:**
- `id` (UUID) - Drive ID

**Response:** `200 OK` with `RemoteFileDriveDto`

**Notes:**
- Checks actual connection state and updates status if stale

---

#### POST /api/drives/{id}/test
**Test connection to a drive**

**Path Parameters:**
- `id` (UUID) - Drive ID

**Response:** `200 OK` with `ConnectionTestResult`
```json
{
  "success": true,
  "message": "Successfully connected to SMB share: smb://server/share/",
  "latencyMs": 245
}
```

---

#### GET /api/drives/{id}/tree
**Get directory tree for a drive**

**Path Parameters:**
- `id` (UUID) - Drive ID

**Query Parameters:**
- `path` (string, optional) - Subdirectory path (defaults to root)

**Response:** `200 OK` with `DirectoryTreeNode`
```json
{
  "name": "share",
  "path": "/",
  "imageCount": 25,
  "totalImageCount": 150,
  "children": [
    {
      "name": "Photos",
      "path": "/Photos",
      "imageCount": 50,
      "totalImageCount": 125,
      "children": [...]
    }
  ]
}
```

**Notes:**
- `imageCount`: images directly in this directory
- `totalImageCount`: images in this directory + all subdirectories

---

#### GET /api/drives/{id}/images
**Get images in a specific directory (non-recursive)**

**Path Parameters:**
- `id` (UUID) - Drive ID

**Query Parameters:**
- `path` (string, optional) - Directory path (e.g., "photos/"). Empty/null = root directory
- `sort` (string) - Sort order: `date` (default), `name`, `size`
- `page` (int) - Page number (default: 0)
- `size` (int) - Page size (default: 24)

**Response:** `200 OK` with paginated images
```json
{
  "content": [
    {
      "id": "uuid",
      "fileName": "IMG_001.jpg",
      "filePath": "photos/IMG_001.jpg",
      "fileSize": 2048000,
      "width": 1920,
      "height": 1080,
      "mimeType": "image/jpeg",
      "modifiedDate": "2026-02-01T12:00:00",
      "thumbnailUrl": "/api/files/uuid/thumbnail?size=medium",
      "imageUrl": "/api/files/uuid"
    }
  ],
  "totalElements": 150,
  "totalPages": 7,
  "currentPage": 0,
  "size": 24,
  "last": false
}
```

**Notes:**
- **NEW BEHAVIOR:** Only returns images directly in the specified directory (not subdirectories)
- Uses `findByDrive_IdAndDirectoryAndDeletedFalse` query
- Root directory: `path` is empty or null
- Subdirectories: `path` must end with `/` (e.g., `photos/`)

---

## 2) CrawlerController

**Package:** `com.picturemodel.api.controller`
**Base Path:** `/api/crawler`
**File:** `CrawlerController.java`

### Purpose
Manages crawl job lifecycle: starting, listing, canceling, and clearing job history.

### Dependencies
- `CrawlerService` - Crawl business logic
- `CrawlJobRepository` - Job persistence

### Endpoints

#### POST /api/crawler/start
**Start a new crawl job**

**Request Body:** `StartCrawlRequest`
```json
{
  "driveId": "uuid",
  "rootPath": "/photos",
  "isIncremental": false,
  "extractExif": true
}
```

**Response:** `202 Accepted` with `CrawlJob`

**Process:**
1. Creates `CrawlJob` with status `PENDING`
2. Launches async `CrawlerJobRunner.runJob()` task
3. Returns immediately (non-blocking)

**Notes:**
- `isIncremental`: If true, only processes files modified since last crawl
- `extractExif`: If true, extracts EXIF metadata (camera, GPS, dimensions)

---

#### GET /api/crawler/jobs
**List all crawl jobs (paginated)**

**Query Parameters:**
- `page` (int) - Page number (default: 0)
- `size` (int) - Page size (default: 10)

**Response:** `200 OK` with paginated `CrawlJob` list
```json
{
  "content": [
    {
      "id": "uuid",
      "drive": {...},
      "status": "COMPLETED|IN_PROGRESS|FAILED|CANCELLED|PENDING",
      "startTime": "2026-02-02T10:00:00",
      "endTime": "2026-02-02T10:15:00",
      "filesProcessed": 1250,
      "progressPercentage": 100.0,
      "currentPath": "/photos/vacation",
      "errors": "[\"Error message 1\", \"Error message 2\"]",
      "isIncremental": false
    }
  ],
  "totalElements": 50,
  "totalPages": 5,
  "currentPage": 0,
  "size": 10
}
```

**Sort:** By `startTime` descending (most recent first)

---

#### GET /api/crawler/drives/{driveId}/jobs
**List crawl jobs for a specific drive (paginated)**

**Path Parameters:**
- `driveId` (UUID) - Drive ID

**Query Parameters:**
- `page` (int) - Page number (default: 0)
- `size` (int) - Page size (default: 10)

**Response:** `200 OK` with paginated `CrawlJob` list

---

#### GET /api/crawler/jobs/{id}
**Get a specific crawl job by ID**

**Path Parameters:**
- `id` (UUID) - Job ID

**Response:** `200 OK` with `CrawlJob`

---

#### POST /api/crawler/jobs/{id}/cancel
**Cancel a running or pending crawl job**

**Path Parameters:**
- `id` (UUID) - Job ID

**Response:** `200 OK` with updated `CrawlJob`

**Process:**
1. Sets status to `CANCELLED`
2. Sets `endTime`
3. Signals `CrawlerJobRunner` to stop processing

**Notes:**
- No-op if job is already `COMPLETED` or `FAILED`
- Cancellation is cooperative (checks flag between directories)

---

#### DELETE /api/crawler/drives/{driveId}/jobs
**Clear all crawl job history for a drive**

**Path Parameters:**
- `driveId` (UUID) - Drive ID

**Response:** `204 No Content`

**Side Effects:**
- Deletes all `CrawlJob` records for the drive
- Does not affect images (only job history)

---

## 3) ImageController

**Package:** `com.picturemodel.api.controller`
**Base Path:** `/api/images`
**File:** `ImageController.java`

### Purpose
Provides advanced image search with filters and retrieves individual image metadata.

### Dependencies
- `ImageRepository` - JPA repository with Specification support

### Endpoints

#### GET /api/images
**Search/list images with filters**

**Query Parameters:**
- `query` (string, optional) - Text search in filename and path
- `driveId` (UUID, optional) - Filter by drive
- `tagIds` (List<UUID>, optional) - Filter by tags (OR logic)
- `fromDate` (string, optional) - Start date (ISO format: `2026-01-01`)
- `toDate` (string, optional) - End date (ISO format: `2026-12-31`)
- `sort` (string) - Sort: `date` (default), `name`, `size`, `relevance`
- `page` (int) - Page number (default: 0)
- `size` (int) - Page size (default: 24)

**Response:** `200 OK` with paginated images
```json
{
  "content": [...],
  "totalElements": 450,
  "totalPages": 19,
  "currentPage": 0,
  "size": 24
}
```

**Search Behavior:**
- `query`: Case-insensitive LIKE search on `fileName` and `filePath`
- `tagIds`: Images with ANY of the specified tags
- Date range: Based on `modifiedDate` field
- Always excludes deleted images

**Example:** `/api/images?query=sunset&tagIds=tag1-uuid,tag2-uuid&fromDate=2026-01-01&sort=date&page=0&size=24`

---

#### GET /api/images/{id}
**Get image details by ID**

**Path Parameters:**
- `id` (UUID) - Image ID

**Response:**
- `200 OK` with `Image` entity
- `404 Not Found` if image doesn't exist

---

## 4) TagController

**Package:** `com.picturemodel.api.controller`
**Base Path:** `/api/tags`
**File:** `TagController.java`

### Purpose
Manages tag CRUD operations and image-tag associations.

### Dependencies
- `TagRepository` - Tag persistence
- `ImageRepository` - For removing tag associations

### Endpoints

#### GET /api/tags
**List all tags**

**Response:** `200 OK` with `List<Tag>`
```json
[
  {
    "id": "uuid",
    "name": "Vacation",
    "color": "#FF5733",
    "usageCount": 125,
    "createdDate": "2026-01-15T10:00:00",
    "modifiedDate": "2026-02-01T15:30:00"
  }
]
```

**Sort:** By `name` ascending (alphabetical)

---

#### POST /api/tags
**Create a new tag**

**Request Body:** `TagCreateRequest`
```json
{
  "name": "Vacation",
  "color": "#FF5733"
}
```

**Response:**
- `201 Created` with `Tag`
- `409 Conflict` if tag name already exists (case-insensitive)

**Validation:**
- `name`: required, trimmed, max 100 chars
- `color`: optional, hex color code

---

#### PUT /api/tags/{id}
**Update a tag**

**Path Parameters:**
- `id` (UUID) - Tag ID

**Request Body:** `TagUpdateRequest`
```json
{
  "name": "Summer Vacation",
  "color": "#33A1FF"
}
```

**Response:**
- `200 OK` with updated `Tag`
- `404 Not Found` if tag doesn't exist
- `409 Conflict` if new name conflicts with existing tag

**Notes:**
- Both fields are optional (partial update)
- Name uniqueness check excludes current tag

---

#### DELETE /api/tags/{id}
**Delete a tag**

**Path Parameters:**
- `id` (UUID) - Tag ID

**Response:** `204 No Content`

**Side Effects:**
1. Removes tag from all associated images
2. Deletes tag entity

**Transaction:** Wrapped in `@Transactional` to ensure consistency

---

## 5) FileController

**Package:** `com.picturemodel.api.controller`
**Base Path:** `/api/files`
**File:** `FileController.java`

### Purpose
Streams image files and generates on-the-fly thumbnails from connected drives.

### Dependencies
- `ImageRepository` - Image metadata lookup
- `ConnectionManager` - File system access
- `Thumbnailator` - Thumbnail generation library

### Endpoints

#### GET /api/files/{imageId}
**Stream full-resolution image**

**Path Parameters:**
- `imageId` (UUID) - Image ID

**Response:**
- `200 OK` with image bytes
- Content-Type: matches image MIME type
- Content-Length: file size in bytes

**Process:**
1. Load image metadata
2. Get FileSystemProvider for drive
3. Read file via `provider.readFile()`
4. Stream bytes to response

**Errors:**
- `404` - Image not found or deleted
- `500` - Drive not connected or file read failed

---

#### GET /api/files/{imageId}/thumbnail
**Generate and stream thumbnail**

**Path Parameters:**
- `imageId` (UUID) - Image ID

**Query Parameters:**
- `size` (string) - Thumbnail size: `small` (150px), `medium` (250px, default), `large` (350px)

**Response:**
- `200 OK` with thumbnail bytes
- Content-Type: depends on source format
- Content-Length: thumbnail size

**Thumbnail Generation:**
- Reads original image from drive
- Resizes in-memory using Thumbnailator
- Maintains aspect ratio
- Output format:
  - PNG → PNG
  - GIF → GIF
  - WebP → WebP
  - JPEG, TIFF, BMP, others → JPEG

**Performance:**
- No disk cache (generated on-demand)
- Suitable for low-frequency access
- Consider adding Redis/disk cache for production

---

## 6) SystemController

**Package:** `com.picturemodel.api.controller`
**Base Path:** `/api/system`
**File:** `SystemController.java`

### Purpose
Provides system status information and health checks.

### Dependencies
- `RemoteFileDriveRepository` - Drive counts
- `ImageRepository` - Image counts
- `TagRepository` - Tag counts
- `CrawlJobRepository` - Active crawl counts

### Endpoints

#### GET /api/system/status
**Get system statistics**

**Response:** `200 OK` with status map
```json
{
  "totalDrives": 5,
  "connectedDrives": 3,
  "totalImages": 12450,
  "totalTags": 42,
  "activeCrawls": 1
}
```

**Metrics:**
- `totalDrives`: All drives regardless of status
- `connectedDrives`: Drives with `CONNECTED` status
- `totalImages`: All images (includes deleted)
- `totalTags`: All tags
- `activeCrawls`: Jobs with `IN_PROGRESS` status

---

#### GET /api/system/health
**Health check endpoint**

**Response:** `200 OK`
```json
{
  "status": "UP",
  "application": "Picture Model"
}
```

**Usage:**
- Load balancer health checks
- Monitoring/alerting systems
- Kubernetes liveness/readiness probes

---

## Services

## 7) DriveService

**Package:** `com.picturemodel.service`
**File:** `DriveService.java`

### Purpose
Business logic layer for drive management: CRUD operations, connection lifecycle, and credential encryption.

### Dependencies
- `RemoteFileDriveRepository` - Drive persistence
- `ConnectionManager` - Connection pooling
- `CredentialEncryptionService` - AES credential encryption
- `ObjectMapper` - JSON serialization

### Methods

#### createDrive(drive, credentials)
```java
public RemoteFileDrive createDrive(RemoteFileDrive drive, Map<String, String> credentials)
```

**Parameters:**
- `drive` - RemoteFileDrive entity (from DTO)
- `credentials` - Map of credential fields (username, password, domain, etc.)

**Returns:** Created `RemoteFileDrive` with ID

**Process:**
1. Encrypts credentials JSON with AES-256
2. Stores encrypted string in `encryptedCredentials` field
3. Sets initial status to `DISCONNECTED`
4. Sets `imageCount` to 0
5. Saves to database

---

#### getAllDrives()
```java
public List<RemoteFileDrive> getAllDrives()
```

**Returns:** All drives (no pagination)

---

#### getDrive(id)
```java
public RemoteFileDrive getDrive(UUID id)
```

**Parameters:**
- `id` - Drive ID

**Returns:** Drive entity

**Throws:** `IllegalArgumentException` if not found

---

#### updateDrive(id, updateData, credentials)
```java
@Transactional
public RemoteFileDrive updateDrive(UUID id, RemoteFileDrive updateData, Map<String, String> credentials)
```

**Parameters:**
- `id` - Drive ID
- `updateData` - Fields to update (null fields are ignored)
- `credentials` - New credentials (null = preserve existing)

**Returns:** Updated `RemoteFileDrive`

**Process:**
1. Loads existing drive
2. Updates non-null fields
3. Re-encrypts credentials if provided
4. If connection params changed, disconnects drive
5. Saves to database

**Updatable Fields:**
- `name`, `connectionUrl`, `rootPath`, `autoConnect`, `autoCrawl`, `credentials`

**Not Updatable:**
- `type` (drive type cannot change after creation)

---

#### deleteDrive(id)
```java
@Transactional
public void deleteDrive(UUID id)
```

**Parameters:**
- `id` - Drive ID

**Process:**
1. Disconnects drive if connected
2. Deletes drive entity (cascades to images and jobs)

---

#### connect(id)
```java
@Transactional
public RemoteFileDrive connect(UUID id)
```

**Parameters:**
- `id` - Drive ID

**Returns:** Drive with `CONNECTED` status

**Process:**
1. Calls `ConnectionManager.connect(drive)`
2. ConnectionManager creates FileSystemProvider
3. Calls `provider.connect()` to establish connection
4. Caches provider for reuse
5. Updates drive status and `lastConnected` timestamp

**Throws:** `RuntimeException` with error message if connection fails

---

#### disconnect(id)
```java
@Transactional
public RemoteFileDrive disconnect(UUID id)
```

**Parameters:**
- `id` - Drive ID

**Returns:** Drive with `DISCONNECTED` status

**Process:**
1. Calls `ConnectionManager.disconnect(id)`
2. Removes provider from cache
3. Updates drive status

---

#### testConnection(id)
```java
public ConnectionTestResult testConnection(UUID id)
```

**Parameters:**
- `id` - Drive ID

**Returns:** Test result with success/failure and latency

**Process:**
1. Gets cached provider (or creates new one)
2. Calls `provider.testConnection()`
3. Returns result with timing

---

#### getDirectoryTree(id, path)
```java
public DirectoryTreeNode getDirectoryTree(UUID id, String path)
```

**Parameters:**
- `id` - Drive ID
- `path` - Starting path (null = root)

**Returns:** Tree structure with image counts

**Process:**
1. Normalizes path (removes leading slash, keeps trailing slash)
2. Calls `provider.getDirectoryTree(path)`
3. Provider recursively builds tree with image counts

---

#### getStatus(id)
```java
public RemoteFileDrive getStatus(UUID id)
```

**Parameters:**
- `id` - Drive ID

**Returns:** Drive with current status

**Process:**
1. Checks actual connection state via ConnectionManager
2. Updates status in database if stale
3. Returns refreshed drive

---

## 8) CrawlerService

**Package:** `com.picturemodel.service`
**File:** `CrawlerService.java`

### Purpose
Orchestrates crawl job lifecycle: creation, async execution, and cancellation.

### Dependencies
- `CrawlJobRepository` - Job persistence
- `RemoteFileDriveRepository` - Drive lookup
- `CrawlerJobRunner` - Async task executor

### Methods

#### startCrawl(request)
```java
public CrawlJob startCrawl(StartCrawlRequest request)
```

**Parameters:**
- `request` - Start crawl request with driveId, rootPath, isIncremental, extractExif

**Returns:** Created `CrawlJob` with `PENDING` status

**Process:**
1. Validates drive exists
2. Normalizes `rootPath` (empty string if blank)
3. Creates `CrawlJob` entity:
   - Status: `PENDING`
   - Start time: now
   - `isIncremental`: from request
4. Saves job to database
5. Launches `crawlerJobRunner.runJob(jobId, extractExif)` asynchronously
6. Returns immediately (non-blocking)

**Notes:**
- Uses `@Async` executor pool for parallel crawls
- Job runs in background thread
- Status updates via database polling

---

#### cancelJob(jobId)
```java
public CrawlJob cancelJob(UUID jobId)
```

**Parameters:**
- `jobId` - Job ID to cancel

**Returns:** Updated `CrawlJob` with `CANCELLED` status

**Process:**
1. Loads job from database
2. If already `COMPLETED` or `FAILED`, returns immediately (no-op)
3. Sets status to `CANCELLED`
4. Sets `endTime` to now
5. Saves to database
6. Signals `CrawlerJobRunner` to stop (cooperative cancellation)

**Notes:**
- Cancellation is not immediate (cooperative)
- Runner checks cancel flag between directories
- In-progress directory completes before stopping

---

#### clearDriveHistory(driveId)
```java
@Transactional
public void clearDriveHistory(UUID driveId)
```

**Parameters:**
- `driveId` - Drive ID

**Process:**
- Deletes all `CrawlJob` records for the drive
- Does not affect images (only job history)

---

## 9) CrawlerJobRunner

**Package:** `com.picturemodel.service`
**File:** `CrawlerJobRunner.java`

### Purpose
Async task executor that traverses file trees, indexes images, extracts metadata, and updates job status.

### Dependencies
- `CrawlJobRepository` - Job updates
- `RemoteFileDriveRepository` - Drive updates
- `ImageRepository` - Image persistence
- `ImageMetadataRepository` - Metadata persistence
- `ConnectionManager` - File system access
- `ExifExtractorService` - EXIF extraction
- `ObjectMapper` - Error JSON serialization

### Configuration

#### Ignored Directories
```java
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
```

#### Save Interval
```java
private static final int SAVE_INTERVAL = 5;
```
Job progress saved every 5 files to reduce database load.

### Methods

#### runJob(jobId, extractExif)
```java
@Async("taskExecutor")
public void runJob(UUID jobId, boolean extractExif)
```

**Parameters:**
- `jobId` - Job ID to execute
- `extractExif` - Whether to extract EXIF metadata

**Process:**
1. **Setup Phase:**
   - Load job and drive from database
   - Set status to `IN_PROGRESS`
   - Get FileSystemProvider via ConnectionManager
   - Normalize root path

2. **Crawl Phase:**
   - Recursively call `crawlPath()` starting from root
   - For each directory:
     - Skip if ignored name (system directories)
     - Skip if cancelled
     - List files via `provider.listDirectory()`
     - For each file:
       - If directory: recurse
       - If image: process and save
   - Update `currentPath` and `progressPercentage` periodically

3. **Image Processing:**
   - Calculate SHA-256 hash for duplicate detection
   - Check if image already exists (by drive + path)
   - If new or modified:
     - Create/update `Image` entity
     - Extract EXIF if enabled (camera, GPS, dimensions)
     - Store metadata as key-value pairs
     - Generate `thumbnailUrl` and `imageUrl`

4. **Completion Phase:**
   - Set status to `COMPLETED` (or `CANCELLED` if interrupted)
   - Set `endTime`
   - Mark deleted images (full crawl only)
   - Update drive `imageCount` and `lastCrawled`
   - Save final job state

**Error Handling:**
- Catches exceptions at directory level (logs warning, continues)
- Catches exceptions at root level (fails job)
- Appends errors to `job.errors` JSON array
- Sets status to `FAILED` on fatal errors

**Incremental Crawl:**
- Only processes files modified since `drive.lastCrawled`
- Skips unchanged files
- Does not mark missing files as deleted

**Full Crawl:**
- Processes all files
- Tracks visited paths
- Marks images not visited as `deleted = true`

---

#### requestCancel(jobId)
```java
public void requestCancel(UUID jobId)
```

**Parameters:**
- `jobId` - Job ID to cancel

**Process:**
- Sets cancel flag in `cancelFlags` map
- Runner checks flag between directories and stops

---

### Key Private Methods

#### crawlPath(provider, job, rootPath, relativePath, incremental, lastCrawled, visitedPaths, counter, extractExif)
Recursive directory traversal algorithm.

**Process:**
1. Check if cancelled → return
2. Check if ignored path → return
3. Update `currentPath` in job
4. List directory contents
5. For each entry:
   - If directory: recurse
   - If image: process and save

#### processImage(...)
Creates or updates Image entity with metadata.

**Steps:**
1. Read file content
2. Calculate SHA-256 hash
3. Check for existing image (by drive + path)
4. Extract EXIF (if enabled)
5. Create/update Image entity
6. Create ImageMetadata entries
7. Save to database

#### markDeletedImages(driveId, visitedPaths, job)
Marks images not visited as deleted (full crawl only).

**Process:**
1. Load all images for drive
2. For each image:
   - If path not in visitedPaths: set `deleted = true`
3. Batch save

---

## 10) ConnectionManager

**Package:** `com.picturemodel.service`
**File:** `ConnectionManager.java`

### Purpose
Manages FileSystemProvider connection pool with health checks and automatic cleanup.

### Dependencies
- `FileSystemProviderFactory` - Provider instantiation
- `RemoteFileDriveRepository` - Status updates

### State
```java
private final Map<UUID, FileSystemProvider> providerCache = new ConcurrentHashMap<>();
```

Thread-safe cache of active connections keyed by drive ID.

### Methods

#### connect(drive)
```java
public FileSystemProvider connect(RemoteFileDrive drive) throws Exception
```

**Parameters:**
- `drive` - RemoteFileDrive entity

**Returns:** Connected `FileSystemProvider`

**Process:**
1. Check if already connected (return cached)
2. If stale connection, remove from cache
3. Set drive status to `CONNECTING`
4. Create provider via factory
5. Call `provider.connect()`
6. Cache provider
7. Set drive status to `CONNECTED`
8. Update `lastConnected` timestamp

**Thread Safety:** Uses `ConcurrentHashMap` for cache

---

#### disconnect(driveId)
```java
public void disconnect(UUID driveId)
```

**Parameters:**
- `driveId` - Drive ID

**Process:**
1. Remove provider from cache
2. Call `provider.disconnect()`
3. Set drive status to `DISCONNECTED`

---

#### getProvider(driveId)
```java
public FileSystemProvider getProvider(UUID driveId) throws Exception
```

**Parameters:**
- `driveId` - Drive ID

**Returns:** Connected `FileSystemProvider`

**Process:**
1. Check cache for existing provider
2. If not found or disconnected:
   - Load drive from database
   - Call `connect(drive)`
3. Return provider

**Usage:** Called by controllers and services to access file system

---

#### isConnected(driveId)
```java
public boolean isConnected(UUID driveId)
```

**Parameters:**
- `driveId` - Drive ID

**Returns:** `true` if connected, `false` otherwise

---

#### performHealthCheck()
```java
@Scheduled(fixedRate = 300000) // 5 minutes
public void performHealthCheck()
```

**Scheduled Task:** Runs every 5 minutes

**Process:**
1. Iterate through cached providers
2. Call `provider.isConnected()` for each
3. If disconnected:
   - Remove from cache
   - Set drive status to `DISCONNECTED`

**Purpose:** Detects stale connections and updates database

---

#### shutdown()
```java
@PreDestroy
public void shutdown()
```

**Lifecycle Hook:** Called on application shutdown

**Process:**
1. Disconnect all cached providers
2. Clear cache

---

#### getActiveConnectionCount()
```java
public int getActiveConnectionCount()
```

**Returns:** Number of active connections (cache size)

---

## 11) ExifExtractorService

**Package:** `com.picturemodel.service`
**File:** `ExifExtractorService.java`

### Purpose
Extracts EXIF metadata from image streams using metadata-extractor library.

### Dependencies
- `metadata-extractor` library (Drew Noakes)

### Methods

#### extract(inputStream)
```java
public ExifExtractionResult extract(InputStream inputStream)
```

**Parameters:**
- `inputStream` - Image file stream

**Returns:** `ExifExtractionResult` with extracted data

**Process:**
1. Read metadata via `ImageMetadataReader.readMetadata()`
2. Extract captured date from `ExifSubIFDDirectory`
3. Extract dimensions from EXIF, JPEG, or PNG directories
4. Extract camera info (make, model, orientation)
5. Extract GPS info (latitude, longitude, altitude)
6. Return result with all extracted tags

**Extracted Tags:**
- `datetime.original` - Capture timestamp
- `image.width` - Image width in pixels
- `image.height` - Image height in pixels
- `camera.make` - Camera manufacturer
- `camera.model` - Camera model
- `camera.orientation` - Orientation (rotation)
- `gps.latitude` - GPS latitude (6 decimal places)
- `gps.longitude` - GPS longitude (6 decimal places)
- `gps.altitude` - GPS altitude in meters

**Error Handling:**
- Returns `ExifExtractionResult.failed()` on any error
- Logs warning but doesn't throw

---

### ExifExtractionResult

**Inner Class:** Return type for extraction results

**Fields:**
- `boolean failed` - True if extraction failed
- `LocalDateTime capturedAt` - Capture date/time
- `Integer width` - Image width
- `Integer height` - Image height
- `Map<String, String> metadata` - All extracted tags

**Factory Method:**
```java
public static ExifExtractionResult failed()
```

Returns empty result with `failed = true`.

---

## Data Flow Diagrams

### Drive Connection Flow
```
Client → POST /api/drives/{id}/connect
  ↓
DriveController.connect(id)
  ↓
DriveService.connect(id)
  ↓
ConnectionManager.connect(drive)
  ↓
FileSystemProviderFactory.createProvider(drive)
  ↓
SmbFileSystemProvider.connect()
  ↓
jCIFS library → SMB server
  ↓
Response: RemoteFileDriveDto (status: CONNECTED)
```

### Crawl Job Flow
```
Client → POST /api/crawler/start
  ↓
CrawlerController.startCrawl(request)
  ↓
CrawlerService.startCrawl(request)
  ↓
Create CrawlJob (status: PENDING)
  ↓
@Async CrawlerJobRunner.runJob(jobId, extractExif)
  ↓
Set status: IN_PROGRESS
  ↓
Recursive crawlPath():
  - List directory
  - For each file:
    - Read file
    - Calculate hash
    - Extract EXIF (if enabled)
    - Save Image + ImageMetadata
  - Update progress
  ↓
Set status: COMPLETED
  ↓
Update drive.imageCount
  ↓
Response: CrawlJob entity
```

### Image Search Flow
```
Client → GET /api/images?query=sunset&tagIds=...
  ↓
ImageController.searchImages(...)
  ↓
Build JPA Specification:
  - deleted = false
  - driveId filter
  - text search (fileName LIKE, filePath LIKE)
  - tag join (tagIds IN)
  - date range (modifiedDate BETWEEN)
  ↓
ImageRepository.findAll(spec, pageable)
  ↓
JPA Criteria Query → Database
  ↓
Response: Paginated Image list
```

### Image Streaming Flow
```
Client → GET /api/files/{imageId}/thumbnail?size=medium
  ↓
FileController.getThumbnail(imageId, size)
  ↓
Load Image entity
  ↓
ConnectionManager.getProvider(drive.id)
  ↓
provider.readFile(image.filePath)
  ↓
InputStream → Thumbnailator.of(in)
                .size(250, 250)
                .outputFormat("jpg")
  ↓
Response: byte[] (Content-Type: image/jpeg)
```

---

## Error Handling

### HTTP Status Codes

| Code | Meaning | Common Causes |
|------|---------|---------------|
| 200 OK | Success | Request completed successfully |
| 201 Created | Resource created | Tag or drive created |
| 202 Accepted | Async operation started | Crawl job started |
| 204 No Content | Success, no body | Delete operations |
| 400 Bad Request | Validation error | Invalid input, malformed JSON |
| 404 Not Found | Resource not found | Drive/image/tag doesn't exist |
| 409 Conflict | Duplicate resource | Tag name already exists |
| 500 Internal Server Error | Server error | Connection failed, file read error |

### Exception Handling

**GlobalExceptionHandler** catches and formats all exceptions:

1. **MethodArgumentNotValidException** → `400 Bad Request`
   - Validation errors from `@Valid` annotations
   - Returns field errors in response

2. **IllegalArgumentException** → `404 Not Found`
   - Entity not found errors
   - Returns error message

3. **RuntimeException** → `500 Internal Server Error`
   - Connection failures
   - File system errors
   - Returns error message

4. **Exception** (catch-all) → `500 Internal Server Error`
   - Unexpected errors
   - Logs stack trace

**Error Response Format:**
```json
{
  "code": "not_found",
  "message": "Drive not found: 123e4567-e89b-12d3-a456-426614174000",
  "details": null
}
```

---

## Performance Considerations

### Caching
- **ConnectionManager:** Caches FileSystemProvider instances
- **No thumbnail cache:** Thumbnails generated on-demand (consider adding Redis)

### Async Operations
- **Crawl jobs:** Run in `@Async` thread pool
- **Default pool size:** 10 threads (configurable in `application.yml`)

### Database Queries
- **Pagination:** All list endpoints support paging
- **JPA Specifications:** Dynamic queries for image search
- **Indexes:** Should be added on:
  - `Image.drive_id`
  - `Image.file_path`
  - `Image.modified_date`
  - `Image.deleted`

### File System Access
- **Streaming:** Files streamed directly to response (no temp files)
- **Connection reuse:** Providers cached for duration of connection

---

## Security

### Credential Encryption
- **Algorithm:** AES-256-CBC
- **Service:** `CredentialEncryptionService`
- **Storage:** Encrypted credentials stored in `RemoteFileDrive.encryptedCredentials`
- **Key Management:** Encryption key stored in application properties (should use secrets manager in production)

### Access Control
- **Current:** No authentication/authorization implemented
- **Recommendation:** Add Spring Security with JWT tokens

### SMB Connection Security
- **Protocols:** SMB1-SMB311 supported
- **Authentication:** NTLM via username/password/domain
- **Recommendation:** Use SMB3+ for encryption

---

## Configuration

### Application Properties

```yaml
# Thread pool for async crawl jobs
spring:
  task:
    execution:
      pool:
        core-size: 5
        max-size: 10
        queue-capacity: 100

# Connection timeout (milliseconds)
file-system:
  connection:
    timeout: 10000
    response-timeout: 30000

# Thumbnail sizes
thumbnail:
  small: 150
  medium: 250
  large: 350
```

---

## API Quick Reference

### Drives
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/drives` | Create drive |
| GET | `/api/drives` | List all drives |
| GET | `/api/drives/{id}` | Get drive |
| PUT | `/api/drives/{id}` | Update drive |
| DELETE | `/api/drives/{id}` | Delete drive |
| POST | `/api/drives/{id}/connect` | Connect |
| POST | `/api/drives/{id}/disconnect` | Disconnect |
| GET | `/api/drives/{id}/status` | Get status |
| POST | `/api/drives/{id}/test` | Test connection |
| GET | `/api/drives/{id}/tree` | Get directory tree |
| GET | `/api/drives/{id}/images` | List images in directory |

### Crawler
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/crawler/start` | Start crawl |
| GET | `/api/crawler/jobs` | List all jobs |
| GET | `/api/crawler/drives/{driveId}/jobs` | List drive jobs |
| GET | `/api/crawler/jobs/{id}` | Get job |
| POST | `/api/crawler/jobs/{id}/cancel` | Cancel job |
| DELETE | `/api/crawler/drives/{driveId}/jobs` | Clear history |

### Images
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/images` | Search images |
| GET | `/api/images/{id}` | Get image |

### Tags
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/tags` | List tags |
| POST | `/api/tags` | Create tag |
| PUT | `/api/tags/{id}` | Update tag |
| DELETE | `/api/tags/{id}` | Delete tag |

### Files
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/files/{imageId}` | Stream full image |
| GET | `/api/files/{imageId}/thumbnail` | Stream thumbnail |

### System
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/system/status` | System stats |
| GET | `/api/system/health` | Health check |

---

## Related Files

- `api/src/main/java/com/picturemodel/api/controller/` - Controllers
- `api/src/main/java/com/picturemodel/service/` - Services
- `api/src/main/java/com/picturemodel/domain/entity/` - JPA entities
- `api/src/main/java/com/picturemodel/domain/repository/` - Repositories
- `api/src/main/java/com/picturemodel/infrastructure/filesystem/` - File system providers
- `api/src/main/java/com/picturemodel/api/dto/` - Request/response DTOs
- `api/src/main/resources/application.yml` - Configuration
