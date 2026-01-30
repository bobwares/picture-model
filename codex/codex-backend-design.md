Creator: Codex

# Picture Model App — Backend Design

Date: January 27, 2026

## 1. Overview
The backend is a local‑network server that indexes image files, stores metadata in a database, and serves a web app via REST APIs. It runs on the machine that has access to storage (file system + DB) and is reachable from devices on the same Wi‑Fi.

## 2. Responsibilities
- Crawl and index image files from configured libraries.
- Extract technical metadata (dimensions, EXIF where supported).
- Persist image records and metadata.
- Serve image files and thumbnails.
- Provide search and filter endpoints.
- Accept metadata edits and tag updates.

## 3. Architecture (Logical Layers)

### 3.1 API Layer
- REST controllers for images, metadata, tags, crawl jobs, libraries, settings.
- Input validation and error mapping.
- Auth hooks (optional, local‑network only by default).

### 3.2 Domain Services
- **ImageService**: image record retrieval and updates.
- **MetadataService**: key/value metadata persistence.
- **TagService**: tag CRUD and relationships.
- **CrawlService**: orchestrates scans and indexing.
- **SearchService**: query translation and ranking.
- **ThumbnailService**: generate and cache thumbnails.

### 3.3 Infrastructure
- File system access (read‑only for images).
- Database persistence (JPA/JDBC).
- Background job executor for crawling.
- Eventing / notification channel for crawl progress.

## 4. Data Model (High Level)
- **Image**: file path, hash, size, timestamps, mime, dimensions.
- **Metadata**: key/value pairs per image (source, updated_at).
- **Tag**: name + color; many‑to‑many with images.
- **Library**: root path + name.
- **CrawlJob**: status, counts, errors, timestamps.

## 5. Key Backend Workflows

### 5.1 Crawl and Index
1. Receive crawl request with root path.
2. Enumerate files and filter supported image types.
3. For each file: compute hash, extract metadata, persist or update.
4. Track progress in CrawlJob.
5. Emit progress events and finalize job.

### 5.2 Search
1. Translate query + filters into DB query.
2. Execute with pagination and sort.
3. Return image summaries + thumbnail URLs.

### 5.3 Image Serving
1. Validate access to file path.
2. Stream image bytes with correct headers.
3. Serve cached thumbnail or generate on demand.

### 5.4 Metadata Updates
1. Validate input fields.
2. Persist metadata changes.
3. Return updated record.

## 6. API Surface (High Level)
- `GET /api/status`
- `GET /api/libraries`, `POST /api/libraries`, `DELETE /api/libraries/{id}`
- `POST /api/crawls`, `GET /api/crawls`, `GET /api/crawls/{id}`
- `GET /api/images`, `GET /api/images/{id}`
- `PUT /api/images/{id}/metadata`
- `POST /api/images/{id}/tags`, `DELETE /api/images/{id}/tags/{tagId}`
- `GET /api/tags`, `POST /api/tags`, `DELETE /api/tags/{id}`
- `GET /api/settings`, `PUT /api/settings`
- `GET /files/{imageId}`, `GET /files/{imageId}/thumbnail`

## 7. Component Interfaces (Concrete)

### 7.1 Service Interfaces

```text
ImageService
  - getImage(id): ImageDetails
  - listImages(query, filters, paging, sort): ImagePage
  - updateMetadata(id, metadataPatch): ImageDetails
  - addTags(id, tagIds): ImageDetails
  - removeTag(id, tagId): ImageDetails

LibraryService
  - listLibraries(): LibraryList
  - createLibrary(payload): Library
  - deleteLibrary(id): void

CrawlService
  - startCrawl(libraryId, options): CrawlJob
  - getCrawl(id): CrawlJob
  - listCrawls(): CrawlJobList
  - cancelCrawl(id): CrawlJob

TagService
  - listTags(): TagList
  - createTag(payload): Tag
  - deleteTag(id): void

SettingsService
  - getSettings(): Settings
  - updateSettings(payload): Settings

FileService
  - streamImage(imageId): StreamResult
  - streamThumbnail(imageId, size): StreamResult
```

### 7.2 Repository Interfaces

```text
ImageRepository
  - findById(id)
  - findByPath(path)
  - search(query, filters, paging, sort)
  - save(image)

MetadataRepository
  - findByImageId(imageId)
  - saveAll(metadataList)

TagRepository
  - findById(id)
  - listAll()
  - save(tag)
  - delete(id)

LibraryRepository
  - listAll()
  - save(library)
  - delete(id)

CrawlJobRepository
  - save(job)
  - findById(id)
  - listRecent(limit)
```

## 8. Error Model

### 8.1 Error Response Shape

```json
{
  "error": {
    "code": "image_not_found",
    "message": "Image not found",
    "details": {
      "id": "123"
    }
  }
}
```

### 8.2 Standard Error Codes
- `bad_request` (validation or malformed input)
- `not_found` (resource missing)
- `conflict` (duplicate path or tag name)
- `unsupported_media` (unsupported image type)
- `io_error` (file system read failure)
- `timeout` (crawl or stream timeout)
- `server_error` (unexpected failure)

## 9. Deployment Details

### 9.1 Runtime
- Single process server on the storage host.
- Default bind: `0.0.0.0` with configurable port.
- Static assets served by the same process.

### 9.2 Configuration
- Config file or environment variables for:
  - Database connection
  - Library root paths
  - Crawl options (incremental, file types)
  - Thumbnail cache directory and size
  - Server port and base URL

### 9.3 Data Storage
- Database file or server (embedded or external).
- Thumbnail cache stored on disk (configurable).
- Log output to file with rotation (optional).

### 9.4 Operational Notes
- Intended for LAN use only.
- Optional simple auth (PIN) for shared networks.
- Graceful shutdown: stop crawl jobs, flush state, close DB.

## 10. Non‑Functional Requirements
- Crawl 10k+ images in a reasonable time.
- Search responses under 500ms for common queries.
- Read‑only file access for safety.
- Resilient to partial failures (skip unreadable files).
- Observability: logs + crawl error tracking.

## 11. Open Decisions
- Database choice (embedded vs external).
- Full‑text search index vs DB filters.
- Thumbnail storage strategy (file cache vs on‑demand).
- Crawl progress via polling vs SSE.
