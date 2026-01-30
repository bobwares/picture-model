Creator: Codex

# Picture Model App - Domain Design Document

Date: January 27, 2026

## 1. Purpose and Scope
The Picture Model App indexes image files from one or more drives (local or remote), stores metadata in a database, and serves a browser-based application for searching, viewing, and editing image metadata.

In scope:
- Crawl image files from local and remote drives (SMB, SFTP, FTP, etc.).
- Persist image records and metadata in a database.
- Serve images and metadata via an HTTP server.
- Provide a browser app for search, view, and edit.

Out of scope (for now):
- Cloud sync or remote SaaS storage integrations.
- Face recognition, tagging automation, or ML labeling.
- Multi-user access control or external authentication.

## 2. Domain Concepts

### Core Entities
- **RemoteFileDrive**: A configured storage source (LOCAL, SMB, SFTP, FTP).
- **Image**: A file on a drive representing a single image.
- **ImageMetadata**: Key/value metadata associated with an Image.
- **Tag**: A label attached to images (many-to-many).
- **CrawlJob**: A crawl run for a drive and root path.
- **DirectoryNode** (optional): Cached directory tree nodes for fast browsing.

### Supporting Components
- **FileSystemProvider**: Abstraction over drive types.
- **ConnectionManager**: Drive connections, pooling, and health checks.
- **HTTP Server**: API and file serving.
- **Browser App**: Web UI for search, view, and edit.
- **Database**: Persisted records and metadata.

## 3. Domain Model (Entity Sketch)

### RemoteFileDrive
- identity: id
- attributes: name, type, connection_url, credentials, status, root_path, auto_connect, auto_crawl, last_connected, last_crawled, image_count, created_date, modified_date
- relationships: has many Images, has many CrawlJobs

### Image
- identity: id
- attributes: drive_id, file_name, file_path (relative), file_size, file_hash, mime_type, created_date, modified_date, indexed_date, width, height, thumbnail_path, deleted
- relationships: belongs to RemoteFileDrive, has many ImageMetadata, has many Tags

### ImageMetadata
- identity: id
- attributes: image_id, key, value, source, last_modified
- relationships: belongs to Image

### Tag
- identity: id
- attributes: name, color, created_date, usage_count
- relationships: many-to-many with Image

### CrawlJob
- identity: id
- attributes: drive_id, root_path, status, start_time, end_time, files_processed, files_added, files_updated, files_deleted, current_path, errors, is_incremental
- relationships: belongs to RemoteFileDrive

### DirectoryNode (optional)
- identity: id
- attributes: drive_id, path, parent_path, name, image_count, total_image_count, last_updated
- relationships: belongs to RemoteFileDrive

## 4. Key Workflows

### Crawl and Index
1. User selects a drive and root path.
2. CrawlJob starts and recursively scans directories via FileSystemProvider.
3. For each image file found:
   - Read file attributes and generate checksum.
   - Extract intrinsic metadata (dimensions, mime type, EXIF if available).
   - Create or update Image and ImageMetadata.
4. CrawlJob completes and reports results.

### Search
1. Browser App sends query (file name, metadata key/value, date range, size, tags, drive).
2. HTTP Server queries database.
3. Results returned with pagination and sorting.

### View
1. Browser App requests Image details.
2. HTTP Server returns metadata and streamed image URL.
3. Browser App renders the image and metadata.

### Edit Metadata
1. User updates metadata in Browser App.
2. HTTP Server validates and persists updates.
3. Updated metadata is immediately searchable.

## 5. Data Rules and Constraints
- (drive_id, file_path) is unique per Image.
- file_hash identifies file content for duplicate detection.
- Metadata keys are case-insensitive but stored normalized.
- Only supported MIME types are indexed (image/jpeg, image/png, image/gif, image/webp, image/tiff).
- Deletes or moves are handled by a subsequent crawl (soft delete until confirmed missing).
- Credentials are encrypted at rest.

## 6. System Interfaces (High Level)

### File System Interface
- Read-only access for image files.
- Optional EXIF extraction.
- Directory tree retrieval for UI.

### HTTP API (high level)
- Drives: CRUD + connect/disconnect/test
- Images: list, detail, metadata update, tag add/remove
- Crawls: start, status, pause/resume/cancel
- Search: query + filters
- File serving: full image + thumbnail

### Browser App
- Dashboard and drive management
- Directory tree, tags, search views
- Image detail with metadata editing

## 7. Non-Functional Requirements
- Crawl 10k+ images per drive in reasonable time.
- Search responses within 500ms for common queries.
- Read-only file access for safety.
- Resilience: crawl errors are logged and non-fatal.
- Support multiple connected drives.

## 8. Open Questions
- Database target for production (PostgreSQL vs SQLite).
- Thumbnail storage (file cache vs DB blob).
- Directory tree caching strategy (on-demand vs stored).
- Authentication (none vs PIN for local network).

## 9. Future Enhancements
- Tagging automation and duplicate detection UI.
- Cloud storage integrations.
- Multi-user access control and roles.

## 10. Schema Tasks (Derived from Domain Model)

### 10.1 JSON Schemas
Create JSON Schemas for the following:
- RemoteFileDrive
- Image
- ImageMetadata
- Tag
- CrawlJob
- DirectoryNode (optional)

### 10.2 API Schemas
- Request schemas for all REST endpoints.
- Response schemas for all REST endpoints.
- Error response schema (standardized).

### 10.3 Persisted Data Schema
- Schema reflecting the database tables and constraints.

### 10.4 OpenAPI
- OpenAPI spec that references the JSON Schemas for request/response bodies.

### 10.5 Tasks
- Enumerate fields and constraints per entity.
- Author versioned JSON Schema files under `project_root/schema`.
- Validate schemas (lint/validation step).
- Generate OpenAPI spec with schema references.
- Confirm schema alignment with database migrations.
