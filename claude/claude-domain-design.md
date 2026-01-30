---
**Created by:** Claude (AI Coding Agent)
**Date:** 2026-01-27
**Version:** 2.0
---

# Picture Model App - Domain Design Document
## Updated for Remote File Drives Support

## 1. Executive Summary

The Picture Model App is an image management system that connects to multiple remote file drives (local, network shares, SFTP), crawls for image files, stores metadata in a database, and provides a web-based interface with multiple views (directory tree, tags, search) for browsing and managing image collections.

**Key Changes from v1:**
- Support for multiple remote file drives
- Dashboard-first landing page
- Three primary views: Directory Tree, Tags, Search
- Light mode UI theme
- Medium thumbnail size (250x250px)

## 2. Domain Model

### 2.1 Core Domain Entities

#### RemoteFileDrive (NEW)
- **Attributes:**
  - `id`: Unique identifier (UUID or Long)
  - `name`: User-friendly name (e.g., "Home NAS", "External Drive 1")
  - `type`: Drive type enum (LOCAL, SMB, SFTP, FTP)
  - `connectionUrl`: Connection string or path
  - `credentials`: Encrypted credentials JSON (username, password, etc.)
  - `status`: Connection status enum (CONNECTED, DISCONNECTED, ERROR, CONNECTING)
  - `rootPath`: Root directory on the drive
  - `autoConnect`: Boolean - auto-connect on startup
  - `autoCrawl`: Boolean - auto-crawl when connected
  - `lastConnected`: Timestamp of last successful connection
  - `lastCrawled`: Timestamp of last crawl completion
  - `imageCount`: Cached count of images on this drive
  - `createdDate`: When drive was added
  - `modifiedDate`: When drive config was last modified

- **Relationships:**
  - Has many: Image
  - Has many: CrawlJob

- **Connection Types:**
  - `LOCAL`: Direct file system access
  - `SMB`: Windows network share (SMB/CIFS protocol)
  - `SFTP`: SSH file transfer protocol
  - `FTP`: File transfer protocol (less secure, legacy)

#### Image (UPDATED)
- **Attributes:**
  - `id`: Unique identifier (UUID or Long)
  - `driveId`: Foreign key to RemoteFileDrive (NEW)
  - `fileName`: Original file name
  - `filePath`: Relative path on the drive (NEW - changed from absolute)
  - `fullPath`: Computed property (driveRootPath + filePath)
  - `fileSize`: Size in bytes
  - `fileHash`: SHA-256 hash for duplicate detection
  - `mimeType`: Image MIME type (image/jpeg, image/png, etc.)
  - `createdDate`: File creation timestamp
  - `modifiedDate`: File modification timestamp
  - `indexedDate`: When the file was indexed by the crawler
  - `width`: Image width in pixels
  - `height`: Image height in pixels
  - `thumbnailPath`: Path to cached thumbnail (optional)
  - `deleted`: Boolean flag (for soft delete)

- **Relationships:**
  - Belongs to: RemoteFileDrive
  - Has many: ImageMetadata
  - Has many: Tag (many-to-many)

#### ImageMetadata
- **Attributes:**
  - `id`: Unique identifier
  - `imageId`: Foreign key to Image
  - `key`: Metadata key (e.g., "camera_model", "location", "description")
  - `value`: Metadata value (TEXT type for long values)
  - `source`: Source enum (EXIF, USER_ENTERED, AUTO_GENERATED)
  - `lastModified`: When this metadata was last updated

- **Relationships:**
  - Belongs to: Image

#### Tag
- **Attributes:**
  - `id`: Unique identifier
  - `name`: Tag name (unique)
  - `color`: Optional color code for UI (hex format)
  - `createdDate`: When tag was created
  - `usageCount`: Cached count of images with this tag

- **Relationships:**
  - Has many: Image (many-to-many through ImageTag junction table)

#### CrawlJob (UPDATED)
- **Attributes:**
  - `id`: Unique identifier
  - `driveId`: Foreign key to RemoteFileDrive (NEW)
  - `rootPath`: Directory path to crawl (within the drive)
  - `status`: Status enum (PENDING, IN_PROGRESS, PAUSED, COMPLETED, FAILED, CANCELLED)
  - `startTime`: When crawl started
  - `endTime`: When crawl completed
  - `filesProcessed`: Number of files processed
  - `filesAdded`: Number of new images added
  - `filesUpdated`: Number of existing images updated
  - `filesDeleted`: Number of missing files marked as deleted
  - `currentPath`: Current path being crawled (for progress tracking)
  - `errors`: Error messages JSON array
  - `isIncremental`: Boolean - full crawl or incremental

- **Relationships:**
  - Belongs to: RemoteFileDrive

#### DirectoryNode (NEW - Optional for caching)
- **Attributes:**
  - `id`: Unique identifier
  - `driveId`: Foreign key to RemoteFileDrive
  - `path`: Directory path
  - `parentPath`: Parent directory path
  - `name`: Directory name
  - `imageCount`: Number of images in this directory (direct children)
  - `totalImageCount`: Total images including subdirectories
  - `lastUpdated`: When this node was last updated

- **Purpose:**
  - Cache directory structure for faster tree rendering
  - Optional - can be computed on-demand instead

### 2.2 Domain Concepts

#### File System Abstraction Layer (NEW)
- **Purpose:** Abstract different file system types behind common interface
- **Interface:** `FileSystemProvider`
  - Methods:
    - `connect()`: Establish connection
    - `disconnect()`: Close connection
    - `isConnected()`: Check connection status
    - `listDirectory(path)`: List files in directory
    - `getDirectoryTree(rootPath)`: Get directory structure
    - `readFile(path)`: Read file content
    - `getFileMetadata(path)`: Get file stats (size, dates, etc.)
    - `fileExists(path)`: Check if file exists

- **Implementations:**
  - `LocalFileSystemProvider`: Uses Java NIO for local file access
  - `SmbFileSystemProvider`: Uses jCIFS or similar for SMB/CIFS
  - `SftpFileSystemProvider`: Uses JSch or Apache MINA SSHD
  - `FtpFileSystemProvider`: Uses Apache Commons Net

#### Connection Manager (NEW)
- **Responsibilities:**
  - Manage connection lifecycle for all drives
  - Connection pooling for network drives
  - Health checks and auto-reconnection
  - Credential encryption/decryption
  - Connection status monitoring

#### Crawler/Indexer (UPDATED)
- Discovers images on any connected file system (via abstraction layer)
- Extracts EXIF data from images
- Calculates file hashes for duplicate detection
- Updates database with discovered images
- Handles incremental updates (detecting new/modified/deleted files)
- Supports pausing and resuming crawl jobs
- Multi-threaded for performance

#### Database Layer
- Persists image metadata, drives, and relationships
- Provides query capabilities for search
- Technology: PostgreSQL (production) or H2 (development)
- Indexing strategy for fast queries:
  - Drive ID + file path (unique together)
  - File hash (duplicate detection)
  - Tag joins (many-to-many queries)
  - Full-text search on filename and metadata

#### HTTP Server
- Exposes RESTful API for the browser app
- Serves static image files (proxied through drive connections)
- Handles metadata updates
- WebSocket support for real-time updates (crawl progress, drive status)
- Technology: Spring Boot 3.x with embedded Tomcat

#### Browser Application
- Single Page Application (SPA) with dashboard-first design
- Three primary views: Directory Tree, Tags, Search
- Light mode theme
- Communicates with HTTP Server via REST API and WebSocket
- Technology options: React, Vue.js, or vanilla JavaScript

## 3. System Architecture

### 3.1 Updated Component Diagram

```
┌─────────────────────────────────────────────────────┐
│                   Browser (Client)                   │
│                                                       │
│  ┌───────────────────────────────────────────────┐  │
│  │         Browser App (SPA) - Light Mode        │  │
│  │  - Dashboard (Landing)                        │  │
│  │  - Directory Tree View                        │  │
│  │  - Tag View                                   │  │
│  │  - Search View                                │  │
│  │  - Image Detail (Right Sidebar)               │  │
│  └───────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────┘
                         │ HTTP/REST + WebSocket
                         ▼
┌─────────────────────────────────────────────────────┐
│              HTTP Server (Spring Boot)              │
│                                                       │
│  ┌──────────────┐  ┌──────────────┐  ┌───────────┐ │
│  │ REST API     │  │ WebSocket    │  │  Static   │ │
│  │ Controller   │  │ Handler      │  │  Files    │ │
│  └──────────────┘  └──────────────┘  └───────────┘ │
│         │                 │                  │       │
│         └─────────────────┼──────────────────┘       │
│                           │                          │
│                  ┌────────▼────────┐                │
│                  │  Domain Services│                │
│                  │  - DriveService │                │
│                  │  - ImageService │                │
│                  │  - CrawlerServ  │                │
│                  │  - SearchService│                │
│                  │  - MetadataServ │                │
│                  └────────┬────────┘                │
│                           │                          │
│                  ┌────────▼────────┐                │
│                  │ ConnectionMgr   │                │
│                  │ - Conn Pool     │                │
│                  │ - Health Checks │                │
│                  └────────┬────────┘                │
│                           │                          │
│                  ┌────────▼────────┐                │
│                  │ FileSystem      │                │
│                  │ Abstraction     │                │
│                  │ - Local         │                │
│                  │ - SMB           │                │
│                  │ - SFTP          │                │
│                  └────────┬────────┘                │
│                           │                          │
│                  ┌────────▼────────┐                │
│                  │   Repositories  │                │
│                  │   (JPA/JDBC)    │                │
│                  └────────┬────────┘                │
└───────────────────────────┼─────────────────────────┘
                            │
                  ┌─────────▼──────────┐
                  │     Database       │
                  │   (PostgreSQL)     │
                  └────────────────────┘

┌─────────────────────────────────────────────────────┐
│           Remote File Systems (Multiple)             │
│                                                       │
│  Local: /Users/user/Pictures/                        │
│    ├── 2024/                                         │
│    └── 2025/                                         │
│                                                       │
│  SMB: smb://nas.local/photos/                        │
│    ├── vacation/                                     │
│    └── family/                                       │
│                                                       │
│  SFTP: sftp://server.com/images/                     │
│    └── archive/                                      │
└─────────────────────────────────────────────────────┘
```

### 3.2 Component Responsibilities

#### REST API Controller Layer

**Drive Management:**
- `POST /api/drives` - Add new remote drive
- `GET /api/drives` - List all drives
- `GET /api/drives/{id}` - Get drive details
- `PUT /api/drives/{id}` - Update drive configuration
- `DELETE /api/drives/{id}` - Remove drive
- `POST /api/drives/{id}/connect` - Connect to drive
- `POST /api/drives/{id}/disconnect` - Disconnect from drive
- `GET /api/drives/{id}/status` - Get connection status
- `POST /api/drives/{id}/test` - Test connection

**Directory Tree:**
- `GET /api/drives/{id}/tree` - Get directory tree structure
- `GET /api/drives/{id}/tree?path=/some/path` - Get subtree
- `GET /api/drives/{id}/images?path=/path` - Get images in directory

**Image Management:**
- `GET /api/images` - List images (with filters: driveId, path, tags, etc.)
- `GET /api/images/{id}` - Get single image details
- `PUT /api/images/{id}/metadata` - Update metadata
- `POST /api/images/{id}/tags` - Add tags
- `DELETE /api/images/{id}/tags/{tagId}` - Remove tag

**Tag Management:**
- `GET /api/tags` - List all tags
- `POST /api/tags` - Create new tag
- `PUT /api/tags/{id}` - Update tag
- `DELETE /api/tags/{id}` - Delete tag
- `GET /api/tags/{id}/images` - Get images with tag

**Crawler:**
- `POST /api/crawler/start` - Start crawl job
- `GET /api/crawler/jobs/{id}` - Get job status
- `POST /api/crawler/jobs/{id}/pause` - Pause job
- `POST /api/crawler/jobs/{id}/resume` - Resume job
- `POST /api/crawler/jobs/{id}/cancel` - Cancel job
- `GET /api/crawler/jobs?driveId={id}` - Get jobs for drive

**Search:**
- `GET /api/search?q=term&driveId=&tags=&from=&to=` - Search images

**File Serving:**
- `GET /files/{driveId}/{imageId}` - Serve full image
- `GET /files/{driveId}/{imageId}/thumbnail` - Serve thumbnail

**WebSocket:**
- `/ws/crawler` - Real-time crawl progress updates
- `/ws/drives` - Real-time drive status updates

#### Domain Services

**DriveService:**
- CRUD operations for RemoteFileDrive entities
- Connection management (connect, disconnect, test)
- Drive status monitoring
- Credential encryption/decryption

**ImageService:**
- CRUD operations for images
- Image retrieval with filtering (by drive, path, tags)
- Thumbnail management
- Duplicate detection (by hash)

**CrawlerService:**
- Orchestrate crawl jobs
- Schedule and execute crawls
- Progress tracking and reporting
- Incremental crawl logic
- Error handling and recovery

**MetadataService:**
- Manage image metadata
- EXIF extraction
- User metadata updates

**SearchService:**
- Full-text search across filename and metadata
- Advanced filtering (date, size, type, tags, drive)
- Relevance ranking

**ConnectionManager:**
- Manage FileSystemProvider instances
- Connection pooling
- Health checks and auto-reconnect
- Timeout handling

**ExifService:**
- Extract EXIF data from images
- Library: metadata-extractor or Apache Commons Imaging

**ThumbnailService:**
- Generate thumbnails
- Cache management
- Lazy loading

#### Repository Layer
- **DriveRepository**: Database access for RemoteFileDrive entities
- **ImageRepository**: Database access for Image entities
- **MetadataRepository**: Database access for ImageMetadata
- **TagRepository**: Tag management
- **CrawlJobRepository**: Crawl job history and status
- **DirectoryNodeRepository**: (Optional) Cached directory tree

## 4. Data Model

### 4.1 Updated Relational Schema

```sql
-- Remote file drives table
CREATE TABLE remote_file_drives (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    type VARCHAR(20) NOT NULL, -- LOCAL, SMB, SFTP, FTP
    connection_url VARCHAR(1000) NOT NULL,
    credentials TEXT, -- Encrypted JSON
    status VARCHAR(20) NOT NULL DEFAULT 'DISCONNECTED',
    root_path VARCHAR(1000) NOT NULL,
    auto_connect BOOLEAN DEFAULT FALSE,
    auto_crawl BOOLEAN DEFAULT FALSE,
    last_connected TIMESTAMP,
    last_crawled TIMESTAMP,
    image_count INTEGER DEFAULT 0,
    created_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_status (status),
    INDEX idx_name (name)
);

-- Images table (updated with driveId)
CREATE TABLE images (
    id BIGSERIAL PRIMARY KEY,
    drive_id BIGINT NOT NULL REFERENCES remote_file_drives(id) ON DELETE CASCADE,
    file_name VARCHAR(500) NOT NULL,
    file_path VARCHAR(2000) NOT NULL, -- Relative path on drive
    file_size BIGINT NOT NULL,
    file_hash VARCHAR(64) NOT NULL,
    mime_type VARCHAR(50) NOT NULL,
    width INTEGER,
    height INTEGER,
    created_date TIMESTAMP,
    modified_date TIMESTAMP,
    indexed_date TIMESTAMP NOT NULL,
    thumbnail_path VARCHAR(2000),
    deleted BOOLEAN DEFAULT FALSE,
    UNIQUE (drive_id, file_path), -- Unique within a drive
    INDEX idx_drive_id (drive_id),
    INDEX idx_file_hash (file_hash),
    INDEX idx_file_name (file_name),
    INDEX idx_indexed_date (indexed_date),
    INDEX idx_deleted (deleted)
);

-- Image metadata table (unchanged)
CREATE TABLE image_metadata (
    id BIGSERIAL PRIMARY KEY,
    image_id BIGINT NOT NULL REFERENCES images(id) ON DELETE CASCADE,
    key VARCHAR(100) NOT NULL,
    value TEXT,
    source VARCHAR(20) NOT NULL, -- EXIF, USER_ENTERED, AUTO_GENERATED
    last_modified TIMESTAMP NOT NULL,
    INDEX idx_image_id (image_id),
    INDEX idx_key (key)
);

-- Tags table (updated with usage count and created date)
CREATE TABLE tags (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    color VARCHAR(7), -- Hex color code
    created_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    usage_count INTEGER DEFAULT 0,
    INDEX idx_usage_count (usage_count),
    INDEX idx_name (name)
);

-- Image-Tag junction table (unchanged)
CREATE TABLE image_tags (
    image_id BIGINT NOT NULL REFERENCES images(id) ON DELETE CASCADE,
    tag_id BIGINT NOT NULL REFERENCES tags(id) ON DELETE CASCADE,
    tagged_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (image_id, tag_id),
    INDEX idx_tag_id (tag_id)
);

-- Crawl jobs table (updated with driveId)
CREATE TABLE crawl_jobs (
    id BIGSERIAL PRIMARY KEY,
    drive_id BIGINT NOT NULL REFERENCES remote_file_drives(id) ON DELETE CASCADE,
    root_path VARCHAR(2000) NOT NULL,
    status VARCHAR(20) NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP,
    files_processed INTEGER DEFAULT 0,
    files_added INTEGER DEFAULT 0,
    files_updated INTEGER DEFAULT 0,
    files_deleted INTEGER DEFAULT 0,
    current_path VARCHAR(2000),
    is_incremental BOOLEAN DEFAULT FALSE,
    errors TEXT, -- JSON array of error messages
    INDEX idx_drive_id (drive_id),
    INDEX idx_status (status),
    INDEX idx_start_time (start_time)
);

-- Directory nodes table (optional, for caching)
CREATE TABLE directory_nodes (
    id BIGSERIAL PRIMARY KEY,
    drive_id BIGINT NOT NULL REFERENCES remote_file_drives(id) ON DELETE CASCADE,
    path VARCHAR(2000) NOT NULL,
    parent_path VARCHAR(2000),
    name VARCHAR(500) NOT NULL,
    image_count INTEGER DEFAULT 0,
    total_image_count INTEGER DEFAULT 0,
    last_updated TIMESTAMP NOT NULL,
    UNIQUE (drive_id, path),
    INDEX idx_drive_id (drive_id),
    INDEX idx_parent_path (parent_path)
);
```

## 5. Use Cases (Updated)

### UC-1: Add and Connect to Remote Drive
**Actor:** User
**Flow:**
1. User opens Dashboard
2. User clicks "[+ Add New Remote Drive]"
3. User enters drive configuration (name, type, path/URL, credentials)
4. User clicks "Test Connection"
5. System validates connection
6. User clicks "Save & Connect"
7. System saves drive configuration and establishes connection
8. Dashboard displays connected drive with status indicator

### UC-2: Browse Images by Directory Tree
**Actor:** User
**Flow:**
1. User selects a connected drive from Dashboard
2. User clicks "Browse Tree"
3. System displays directory tree view
4. User expands folders to navigate hierarchy
5. User clicks on a folder
6. System displays images in that folder (thumbnail grid)
7. User clicks on thumbnail
8. System displays Image Detail page

### UC-3: Browse Images by Tags
**Actor:** User
**Flow:**
1. User switches to Tag View
2. System displays all tags with image counts
3. User selects one or more tags
4. System displays images matching selected tags
5. User can filter by specific drive or all drives
6. User clicks thumbnail to view detail

### UC-4: Search Images Across All Drives
**Actor:** User
**Flow:**
1. User enters search query in global search bar
2. System searches across all drives (filename, metadata, tags)
3. User applies filters (drive, date range, file type, tags)
4. System returns matching results
5. User views results in grid
6. User clicks thumbnail to view detail

### UC-5: Initial Crawl of Remote Drive
**Actor:** System / User
**Flow:**
1. User connects to a new remote drive
2. System automatically starts crawl (if auto-crawl enabled) OR user manually starts crawl
3. System iterates through directory structure
4. For each image file:
   - Calculate file hash
   - Check if already indexed (duplicate detection)
   - Extract EXIF metadata
   - Create database record
   - Generate thumbnail
5. System reports progress via WebSocket
6. System updates drive statistics
7. Dashboard shows completion notification

### UC-6: Incremental Crawl
**Actor:** System (scheduled task)
**Flow:**
1. System runs scheduled incremental crawl
2. For each connected drive:
   - Scan for new files (add to database)
   - Check existing files for modifications (update metadata)
   - Detect deleted files (mark as deleted)
3. Update drive statistics
4. Log crawl job results

### UC-7: Edit Image Metadata and Tags
**Actor:** User
**Flow:**
1. User views Image Detail page
2. User edits custom metadata fields (description, location, notes)
3. User adds or removes tags
4. User clicks "Save Changes"
5. System updates database
6. System displays confirmation

### UC-8: Batch Tag Multiple Images
**Actor:** User
**Flow:**
1. User in any view (tree, tags, search) with image grid
2. User enables multi-select mode
3. User selects multiple images
4. User clicks "Batch Tag"
5. User adds or removes tags for all selected images
6. System updates all selected images
7. System displays confirmation

## 6. Non-Functional Requirements

### Performance
- Support 10+ remote drives simultaneously
- Directory tree should render within 500ms
- Image thumbnails should lazy-load
- Search results should return within 500ms for 100k+ images
- Crawling should handle 10,000+ images per drive efficiently

### Scalability
- Database should handle 1,000,000+ image records
- Support 50+ concurrent user sessions
- Connection pooling for efficient network drive access

### Security
- Credentials encrypted at rest (AES-256)
- Secure credential transmission (HTTPS only)
- Input validation to prevent directory traversal attacks
- Optional authentication for multi-user scenarios
- Audit logging for sensitive operations

### Usability
- Responsive web interface (desktop-first, mobile-friendly)
- Intuitive navigation between views
- Keyboard shortcuts for common actions
- Progress indicators for long operations
- Clear error messages with recovery suggestions

### Reliability
- Auto-reconnect for network drives
- Graceful degradation when drives are offline
- Transaction management for database operations
- Crawl job resumption after interruption
- Comprehensive logging for troubleshooting

### Maintainability
- Clean separation of concerns (layers)
- Extensible FileSystemProvider interface for new drive types
- Comprehensive unit and integration tests
- API documentation (SpringDoc OpenAPI)
- Code documentation for complex logic

## 7. Technology Stack

### Backend
- **Language:** Java 21
- **Framework:** Spring Boot 3.x
  - Spring Web (REST API)
  - Spring Data JPA (database access)
  - Spring WebSocket (real-time updates)
  - Spring Security (optional, for authentication)
- **ORM:** Hibernate
- **Database:**
  - Development: H2 (embedded)
  - Production: PostgreSQL 15+
- **File System Libraries:**
  - Local: Java NIO.2 (`java.nio.file`)
  - SMB: jCIFS-ng or SMBJv2
  - SFTP: JSch or Apache MINA SSHD
  - FTP: Apache Commons Net
- **Image Processing:**
  - Thumbnails: Thumbnailator or Java ImageIO
  - EXIF: metadata-extractor library
- **Encryption:** Jasypt (for credentials)
- **Build:** Maven

### Frontend
- **Framework:** React or Vue.js (or vanilla JavaScript for simplicity)
- **UI Library:**
  - Tailwind CSS or Bootstrap for light theme styling
  - Component library: Material-UI or Ant Design
- **Tree Component:**
  - React: `rc-tree` or `react-vtree`
  - Vue: `vue-jstree`
- **State Management:** React Context or Redux/Vuex
- **HTTP Client:** Axios or Fetch API
- **WebSocket:** SockJS + STOMP

### Development Tools
- **Testing:** JUnit 5, Mockito, Spring Test, Testcontainers
- **API Docs:** SpringDoc OpenAPI (Swagger UI)
- **Logging:** SLF4J + Logback
- **Hot Reload:** Spring Boot DevTools

## 8. Implementation Phases

### Phase 1: Foundation & Local Drives
- Set up Spring Boot project
- Define domain entities (Drive, Image, Metadata, Tag, CrawlJob)
- Implement database schema with migrations (Flyway or Liquibase)
- Create repositories (Spring Data JPA)
- Implement FileSystemProvider interface
- Implement LocalFileSystemProvider
- Build DriveService (add, connect, disconnect for local drives)

### Phase 2: Crawler & Indexer
- Implement CrawlerService
- EXIF metadata extraction
- Thumbnail generation
- Hash calculation
- Initial crawl functionality
- Incremental crawl logic
- Crawl job management (start, pause, cancel)

### Phase 3: REST API & File Serving
- Implement REST controllers (drives, images, tags, crawler)
- Image file serving endpoint
- Thumbnail serving endpoint
- Error handling and validation
- API documentation (SpringDoc)

### Phase 4: Frontend - Dashboard & Core UI
- Set up frontend project (React/Vue)
- Implement app shell (header, navigation)
- Build Dashboard landing page
- Add/Edit Drive modal
- Drive list and status display
- Light theme styling
- Connection status indicators

### Phase 5: Frontend - Directory Tree View
- Implement directory tree component
- Tree navigation and expansion
- Image grid component (medium thumbnails)
- Integrate with backend API
- Lazy loading and pagination

### Phase 6: Frontend - Tag & Search Views
- Build Tag View with sidebar
- Implement Search View with filters
- View switcher component
- Cross-view navigation
- Filter and sort functionality

### Phase 7: Frontend - Image Detail & Metadata
- Build Image Detail page (right sidebar layout)
- Metadata editor component
- Tag management UI
- Previous/Next navigation
- Save functionality

### Phase 8: Network Drives (SMB, SFTP)
- Implement SmbFileSystemProvider
- Implement SftpFileSystemProvider
- Connection pooling
- Credential management UI
- Test with real network drives

### Phase 9: Real-time Updates & Polish
- WebSocket integration (crawl progress, drive status)
- Keyboard shortcuts
- Responsive design (mobile/tablet)
- Performance optimization (caching, indexing)
- Error handling and edge cases

### Phase 10: Testing & Deployment
- Integration testing
- End-to-end testing
- Performance testing
- Security audit
- Deployment packaging (JAR with embedded Tomcat)
- User documentation

## 9. Open Questions & Decisions

### Resolved (Based on User Input):
- ✅ Landing Page: Dashboard-First
- ✅ Image Detail Layout: Right Sidebar
- ✅ Thumbnail Size: Medium (250x250px)
- ✅ Color Theme: Light mode
- ✅ Remote Drives: Support for multiple drives

### Still Open:
1. **Database Choice for Production:** PostgreSQL or SQLite?
   - PostgreSQL: Better for large datasets, concurrent access
   - SQLite: Simpler deployment, embedded database

2. **Thumbnail Storage:** Database (BLOB) or file system?
   - Database: Simpler management, backup included
   - File system: Better performance, less database bloat

3. **Frontend Framework:** React, Vue, or Vanilla JS?
   - React: Most popular, large ecosystem
   - Vue: Simpler learning curve, good performance
   - Vanilla: No dependencies, full control

4. **Directory Tree Caching:** Compute on-demand or cache in database?
   - On-demand: Always accurate, simpler code
   - Cached: Faster rendering, requires invalidation logic

5. **Authentication:** Required in MVP or add later?
   - MVP: Single-user, no auth
   - Later: Multi-user with authentication

6. **Cloud Storage Support:** When to add (S3, Google Drive, Dropbox)?
   - Phase 11 (after network drives are stable)

## 10. Architectural Patterns

### Layered Architecture
```
┌────────────────────────────────┐
│     Presentation Layer         │ ← REST Controllers, WebSocket
├────────────────────────────────┤
│     Service Layer              │ ← Business logic, orchestration
├────────────────────────────────┤
│     Domain Layer               │ ← Entities, repositories
├────────────────────────────────┤
│     Infrastructure Layer       │ ← FileSystemProvider, external APIs
└────────────────────────────────┘
```

### Design Patterns Used
- **Strategy Pattern:** FileSystemProvider interface with multiple implementations
- **Factory Pattern:** FileSystemProviderFactory to create appropriate provider
- **Repository Pattern:** Data access abstraction (Spring Data JPA)
- **Facade Pattern:** Service layer simplifies complex subsystem interactions
- **Observer Pattern:** WebSocket for real-time updates
- **Builder Pattern:** For complex entity creation (Image, CrawlJob)

## 11. Security Considerations

### Credential Management
- Never store plaintext credentials
- Use AES-256 encryption for credentials at rest
- Use application-level encryption key (configurable, not in code)
- Consider using key management service (KMS) for production

### Network Security
- HTTPS only for all API communication
- Validate all file paths to prevent directory traversal
- Sanitize user inputs (XSS prevention)
- Rate limiting for API endpoints
- CORS configuration for frontend

### Access Control (Future)
- Role-based access control (RBAC)
- Drive-level permissions (which users can access which drives)
- Audit logging for sensitive operations

## 12. Monitoring & Observability

### Logging
- Structured logging (JSON format)
- Log levels: DEBUG, INFO, WARN, ERROR
- Log crawler progress and errors
- Log drive connection events
- Correlation IDs for request tracing

### Metrics (Future)
- Drive connection status
- Crawl job statistics
- API response times
- Database query performance
- Active user sessions

### Health Checks
- Endpoint: `/actuator/health`
- Check database connectivity
- Check drive connection status
- Check disk space

## 13. Next Steps

1. ✅ **Phase 2 Complete:** Web App Design with remote drives
2. ✅ **Phase 2 Complete:** Updated Domain Design with RemoteFileDrive entity
3. **Phase 3:** Finalize open questions (database choice, frontend framework, etc.)
4. **Phase 4:** Create detailed database schema with sample data
5. **Phase 5:** Set up Spring Boot project structure
6. **Phase 6:** Begin implementation (Phase 1 - Foundation & Local Drives)

---

## Summary of Key Changes from v1

1. **New Entity:** RemoteFileDrive - supports multiple drives
2. **Updated Entity:** Image - now belongs to a drive, file path is relative
3. **Updated Entity:** CrawlJob - now associated with a drive
4. **New Layer:** File System Abstraction - pluggable providers for different drive types
5. **New Component:** Connection Manager - manages drive connections
6. **Updated API:** New endpoints for drive management and directory tree
7. **Updated UI:** Dashboard-first landing page with drive management
8. **New Views:** Directory Tree, Tag, Search (three primary views)
9. **Design Choices:** Light theme, medium thumbnails, right sidebar layout

Ready to proceed with Phase 3 (finalize open questions) or move directly to implementation?
