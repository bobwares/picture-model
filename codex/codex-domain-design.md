Creator: Codex

# Picture Model App — Domain Design (Phase 1)

Date: January 27, 2026

## 1. Purpose and Scope
The Picture Model App indexes image files on a local hard drive, stores metadata records in a database, and serves a browser-based application for searching, viewing, and editing image metadata.

In scope:
- Crawl local hard drives attached to the computer.
- Persist image file records and metadata.
- Serve images and metadata via an HTTP server.
- Provide a browser app for search, view, and edit.

Out of scope (for now):
- Cloud sync or remote storage.
- Face recognition, tagging automation, or ML-based labeling.
- Multi-user access control or authentication.

## 2. Domain Concepts

### Core Entities
- **ImageFile**: A file on disk representing a single image.
- **ImageRecord**: A database record for an ImageFile.
- **Metadata**: Descriptive attributes associated with an ImageFile.
- **CrawlJob**: A run that scans one or more directories.
- **Library**: A logical collection of image files (e.g., a root folder).

### Supporting Components
- **FileSystem**: The local hard drive and directory tree.
- **HTTP Server**: Serves API responses and image bytes.
- **Browser App**: Web UI for search, view, edit.
- **Database**: Stores records and metadata.
- **WiFi Network**: Connectivity between browser and server (if on separate devices).

## 3. Domain Model (Entity Sketch)

### ImageFile
- **identity**: absolute_path (string, unique)
- **attributes**: file_name, extension, size_bytes, created_at, modified_at
- **relationships**: has one ImageRecord

### ImageRecord
- **identity**: id (UUID or long)
- **attributes**: checksum, mime_type, width, height, captured_at
- **relationships**: belongs to ImageFile, has many Metadata

### Metadata
- **identity**: id
- **attributes**: key, value, source (user/system), updated_at
- **relationships**: belongs to ImageRecord

### CrawlJob
- **identity**: id
- **attributes**: started_at, finished_at, status, scanned_count, error_count
- **relationships**: scans many ImageFiles

### Library
- **identity**: id
- **attributes**: name, root_path, created_at
- **relationships**: contains many ImageFiles, has many CrawlJobs

## 4. Key Workflows

### Crawl and Index
1. User selects a Library root path.
2. CrawlJob starts and recursively scans directories.
3. For each image file found:
   - Read file attributes and generate checksum.
   - Extract intrinsic metadata (dimensions, mime type, EXIF if available).
   - Create or update ImageRecord and Metadata.
4. CrawlJob completes and reports results.

### Search
1. Browser App sends query (file name, metadata key/value, date range, size).
2. HTTP Server queries database.
3. Results returned with pagination and sorting.

### View
1. Browser App requests ImageRecord.
2. HTTP Server returns metadata and a signed/streamed image URL.
3. Browser App renders the image and metadata.

### Edit Metadata
1. User updates metadata in Browser App.
2. HTTP Server validates and persists updates.
3. Updated metadata is immediately searchable.

## 5. Data Rules and Constraints
- `absolute_path` is unique per ImageFile.
- `checksum` identifies file content; changes imply a new version or update.
- Metadata keys are case-insensitive but stored normalized.
- Only image files with supported MIME types are indexed (initially: image/jpeg, image/png, image/gif, image/webp, image/tiff).
- Deletes or moves are handled by a subsequent crawl (soft delete in DB until confirmed missing).

## 6. System Interfaces

### File System Interface
- Read-only access to files for crawl and image streaming.
- Optional EXIF extraction from supported formats.

### HTTP API (high level)
- `GET /api/images?query=...` search
- `GET /api/images/{id}` record details
- `GET /api/images/{id}/file` image bytes
- `PUT /api/images/{id}/metadata` update metadata
- `POST /api/crawls` start crawl
- `GET /api/crawls/{id}` crawl status

### Browser App
- Search and results list
- Image detail view
- Metadata edit form

## 7. Non-Functional Requirements
- Crawl performance: handle 10k+ images in reasonable time.
- Response time: search results within 500ms for common queries.
- Safe access: read-only file operations during crawl and view.
- Resilience: crawl errors are logged and non-fatal.

## 8. Open Questions
- What database is targeted (embedded vs. external)?
- Should metadata changes also write back to image EXIF?
- Is multi-library support required in phase 1?
- Should the server and browser run on the same device only?

## 9. Future Enhancements
- Tagging and albums.
- Duplicate detection with merge UI.
- Cloud backup or sync.
- Authentication and multi-user roles.
