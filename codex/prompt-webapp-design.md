load context from claude code response.


# Picture Model App - Domain Design Document

## 1. Executive Summary

The Picture Model App is a local image management system that crawls a hard drive for image files, stores metadata in a database, and provides a web-based interface for browsing, searching, and managing image metadata.

## 2. Domain Model

### 2.1 Core Domain Entities

#### Image
- **Attributes:**
    - `id`: Unique identifier (UUID or Long)
    - `fileName`: Original file name
    - `filePath`: Absolute path on the file system
    - `fileSize`: Size in bytes
    - `fileHash`: SHA-256 hash for duplicate detection
    - `mimeType`: Image MIME type (image/jpeg, image/png, etc.)
    - `createdDate`: File creation timestamp
    - `modifiedDate`: File modification timestamp
    - `indexedDate`: When the file was indexed by the crawler
    - `width`: Image width in pixels
    - `height`: Image height in pixels

- **Relationships:**
    - Has many: ImageMetadata
    - Has many: Tag

#### ImageMetadata
- **Attributes:**
    - `id`: Unique identifier
    - `imageId`: Foreign key to Image
    - `key`: Metadata key (e.g., "camera_model", "location", "description")
    - `value`: Metadata value
    - `source`: Source of metadata (EXIF, USER_ENTERED, AUTO_GENERATED)
    - `lastModified`: When this metadata was last updated

- **Relationships:**
    - Belongs to: Image

#### Tag
- **Attributes:**
    - `id`: Unique identifier
    - `name`: Tag name
    - `color`: Optional color code for UI

- **Relationships:**
    - Has many: Image (many-to-many relationship)

#### CrawlJob
- **Attributes:**
    - `id`: Unique identifier
    - `rootPath`: Directory path to crawl
    - `status`: PENDING, IN_PROGRESS, COMPLETED, FAILED
    - `startTime`: When crawl started
    - `endTime`: When crawl completed
    - `filesProcessed`: Number of files processed
    - `filesAdded`: Number of new images added
    - `filesUpdated`: Number of existing images updated
    - `errors`: Error messages if any

### 2.2 Domain Concepts

#### File System Layer
- Represents the physical storage where image files reside
- Accessed through Java NIO or File APIs
- Read-only from application perspective (no file modifications)

#### Crawler/Indexer
- Responsible for discovering images on the file system
- Extracts EXIF data from images
- Calculates file hashes for duplicate detection
- Updates database with discovered images
- Handles incremental updates (detecting new/modified/deleted files)

#### Database Layer
- Persists image metadata and relationships
- Provides query capabilities for search
- Technology options: PostgreSQL, H2, SQLite

#### HTTP Server
- Exposes RESTful API for the browser app
- Serves static image files
- Handles metadata updates
- Technology: Spring Boot with embedded Tomcat

#### Browser Application
- Single Page Application (SPA) for user interaction
- Communicates with HTTP Server via REST API
- Technology options: React, Vue.js, or vanilla JavaScript

## 3. System Architecture

### 3.1 Component Diagram

```
┌─────────────────────────────────────────────────────┐
│                   Browser (Client)                   │
│                                                       │
│  ┌───────────────────────────────────────────────┐  │
│  │         Browser App (SPA)                     │  │
│  │  - Image Gallery View                         │  │
│  │  - Search Interface                           │  │
│  │  - Metadata Editor                            │  │
│  └───────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────┘
                         │ HTTP/REST
                         ▼
┌─────────────────────────────────────────────────────┐
│              HTTP Server (Spring Boot)              │
│                                                       │
│  ┌──────────────┐  ┌──────────────┐  ┌───────────┐ │
│  │ REST API     │  │ Static File  │  │  Crawler  │ │
│  │ Controller   │  │ Server       │  │  Service  │ │
│  └──────────────┘  └──────────────┘  └───────────┘ │
│         │                 │                  │       │
│         └─────────────────┼──────────────────┘       │
│                           │                          │
│                  ┌────────▼────────┐                │
│                  │  Domain Services│                │
│                  │  - ImageService │                │
│                  │  - SearchService│                │
│                  │  - MetadataServ │                │
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
                  │  (PostgreSQL/H2)   │
                  └────────────────────┘

┌─────────────────────────────────────────────────────┐
│              File System (Hard Drive)               │
│                                                       │
│  /path/to/images/                                    │
│    ├── vacation/                                     │
│    │   ├── photo1.jpg                               │
│    │   └── photo2.png                               │
│    └── family/                                       │
│        └── photo3.jpg                                │
└─────────────────────────────────────────────────────┘
```

### 3.2 Component Responsibilities

#### REST API Controller Layer
- `/api/images` - List, search, filter images
- `/api/images/{id}` - Get single image details
- `/api/images/{id}/metadata` - Update metadata
- `/api/images/{id}/tags` - Manage tags
- `/api/crawler/start` - Initiate crawl job
- `/api/crawler/status` - Get crawl job status
- `/api/search` - Advanced search endpoint
- `/files/{imageId}` or `/files/{hash}` - Serve actual image files

#### Domain Services
- **ImageService**: CRUD operations for images
- **MetadataService**: Manage image metadata
- **CrawlerService**: Orchestrate file system crawling
- **SearchService**: Handle search queries with filters
- **ExifService**: Extract EXIF data from image files
- **ThumbnailService**: Generate and cache thumbnails

#### Repository Layer
- **ImageRepository**: Database access for Image entities
- **MetadataRepository**: Database access for metadata
- **TagRepository**: Tag management
- **CrawlJobRepository**: Crawl history and status

## 4. Use Cases

### UC-1: Initial Image Crawl
**Actor:** System Administrator
**Flow:**
1. User specifies root directory to crawl
2. System starts crawler service
3. Crawler recursively scans directory
4. For each image file:
    - Calculate file hash
    - Check if already indexed (by hash)
    - Extract EXIF metadata
    - Create database record
    - Generate thumbnail
5. Report completion status

### UC-2: Browse Images
**Actor:** End User
**Flow:**
1. User opens browser app
2. System displays paginated grid of image thumbnails
3. User clicks on thumbnail
4. System displays full-size image with metadata

### UC-3: Search Images
**Actor:** End User
**Flow:**
1. User enters search criteria (filename, tags, date range, metadata)
2. System queries database
3. System returns matching images
4. User views results in gallery view

### UC-4: Edit Image Metadata
**Actor:** End User
**Flow:**
1. User views image details
2. User clicks edit metadata
3. User modifies title, description, adds/removes tags
4. User saves changes
5. System updates database
6. System displays confirmation

### UC-5: Detect File Changes
**Actor:** System (scheduled task)
**Flow:**
1. System runs incremental crawl
2. For each indexed image:
    - Check if file still exists
    - Check if modified date changed
    - Update metadata if changed
3. Mark missing files as deleted
4. Scan for new files

## 5. Data Model

### 5.1 Relational Schema

```sql
-- Images table
CREATE TABLE images (
    id BIGSERIAL PRIMARY KEY,
    file_name VARCHAR(500) NOT NULL,
    file_path VARCHAR(2000) NOT NULL UNIQUE,
    file_size BIGINT NOT NULL,
    file_hash VARCHAR(64) NOT NULL,
    mime_type VARCHAR(50) NOT NULL,
    width INTEGER,
    height INTEGER,
    created_date TIMESTAMP,
    modified_date TIMESTAMP,
    indexed_date TIMESTAMP NOT NULL,
    deleted BOOLEAN DEFAULT FALSE,
    INDEX idx_file_hash (file_hash),
    INDEX idx_file_name (file_name),
    INDEX idx_indexed_date (indexed_date)
);

-- Image metadata table
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

-- Tags table
CREATE TABLE tags (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    color VARCHAR(7) -- Hex color code
);

-- Image-Tag junction table
CREATE TABLE image_tags (
    image_id BIGINT NOT NULL REFERENCES images(id) ON DELETE CASCADE,
    tag_id BIGINT NOT NULL REFERENCES tags(id) ON DELETE CASCADE,
    PRIMARY KEY (image_id, tag_id),
    INDEX idx_tag_id (tag_id)
);

-- Crawl jobs table
CREATE TABLE crawl_jobs (
    id BIGSERIAL PRIMARY KEY,
    root_path VARCHAR(2000) NOT NULL,
    status VARCHAR(20) NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP,
    files_processed INTEGER DEFAULT 0,
    files_added INTEGER DEFAULT 0,
    files_updated INTEGER DEFAULT 0,
    errors TEXT
);
```

## 6. API Design

### 6.1 REST Endpoints

#### Image Management

**GET /api/images**
- Query params: `page`, `size`, `sort`, `fileName`, `tagIds`, `fromDate`, `toDate`
- Response: Paginated list of images with thumbnails

**GET /api/images/{id}**
- Response: Full image details including metadata

**PUT /api/images/{id}/metadata**
- Request body: `{ "key": "value", ... }`
- Response: Updated image

**POST /api/images/{id}/tags**
- Request body: `{ "tagIds": [1, 2, 3] }`
- Response: Updated image with tags

**DELETE /api/images/{id}/tags/{tagId}**
- Response: Success status

#### Search

**GET /api/search**
- Query params: `q` (search term), `fields` (fileName, metadata), `tagIds`, `fromDate`, `toDate`
- Response: Search results with highlighting

#### Crawler

**POST /api/crawler/start**
- Request body: `{ "rootPath": "/path/to/images", "incremental": true }`
- Response: CrawlJob with status

**GET /api/crawler/jobs/{id}**
- Response: CrawlJob status and statistics

#### File Serving

**GET /files/{imageId}**
- Response: Image file with appropriate headers

**GET /files/{imageId}/thumbnail**
- Query params: `width`, `height`
- Response: Thumbnail image

### 6.2 API Response Format

```json
{
  "success": true,
  "data": {
    "id": 123,
    "fileName": "vacation.jpg",
    "filePath": "/Users/user/Pictures/vacation.jpg",
    "fileSize": 2048576,
    "width": 1920,
    "height": 1080,
    "mimeType": "image/jpeg",
    "createdDate": "2025-06-15T10:30:00Z",
    "metadata": {
      "camera_model": "Canon EOS R5",
      "location": "Paris, France",
      "description": "Eiffel Tower at sunset"
    },
    "tags": [
      { "id": 1, "name": "vacation", "color": "#FF5733" },
      { "id": 2, "name": "travel", "color": "#33FF57" }
    ]
  }
}
```

## 7. Technology Stack Recommendations

### Backend
- **Language:** Java 21 (already configured)
- **Framework:** Spring Boot 3.x
- **ORM:** Spring Data JPA with Hibernate
- **Database:**
    - Development: H2 (embedded)
    - Production: PostgreSQL or SQLite
- **Image Processing:** Java ImageIO, or library like TwelveMonkeys, Apache Commons Imaging
- **EXIF Reading:** metadata-extractor library
- **Build Tool:** Maven (already configured)

### Frontend
- **Framework:** React or Vue.js (or vanilla JS for simplicity)
- **UI Library:** Material-UI, Ant Design, or Bootstrap
- **HTTP Client:** Fetch API or Axios
- **State Management:** React Context or Redux (if needed)

### Development Tools
- **Testing:** JUnit 5, Mockito, Spring Test
- **API Documentation:** SpringDoc OpenAPI
- **Hot Reload:** Spring Boot DevTools

## 8. Non-Functional Requirements

### Performance
- Image crawling should handle 10,000+ images efficiently
- Search results should return within 500ms
- Thumbnail generation should be lazy and cached
- Support pagination for large result sets

### Scalability
- Database should handle 100,000+ image records
- Concurrent user access (at least 10 simultaneous users)
- Incremental crawling to avoid re-processing entire directory

### Security
- Local deployment (no external authentication required initially)
- Optional: Add basic authentication for multi-user scenarios
- Validate file paths to prevent directory traversal attacks
- Sanitize user inputs for metadata

### Usability
- Responsive web interface
- Intuitive search and filter options
- Keyboard shortcuts for navigation
- Progress indicators for long-running operations (crawling)

### Reliability
- Handle file system errors gracefully
- Database transaction management
- Logging for troubleshooting
- Recover from incomplete crawl jobs

## 9. Implementation Phases

### Phase 1: Domain Design (Current)
- Define domain model
- Design database schema
- Outline API structure

### Phase 2: Backend Core
- Set up Spring Boot project structure
- Implement domain entities and repositories
- Create database schema with migrations

### Phase 3: Crawler Implementation
- Implement file system crawler
- EXIF metadata extraction
- Hash calculation and duplicate detection
- Thumbnail generation

### Phase 4: REST API
- Implement controllers for image CRUD
- Search and filter endpoints
- Crawler control endpoints
- File serving endpoints

### Phase 5: Frontend Development
- Set up frontend project
- Image gallery view
- Search interface
- Metadata editor
- Tag management

### Phase 6: Integration and Testing
- Integration testing
- End-to-end testing
- Performance optimization
- User acceptance testing

### Phase 7: Deployment and Documentation
- Deployment packaging
- User documentation
- API documentation
- Maintenance procedures

## 10. Open Questions and Decisions

1. **Database Choice:** H2 for simplicity or PostgreSQL for robustness?
2. **Thumbnail Storage:** Store in database or file system?
3. **Image File Serving:** Direct file access or stream through API?
4. **Frontend Technology:** React/Vue or vanilla JavaScript?
5. **Authentication:** Required in initial version or add later?
6. **Duplicate Handling:** Keep both duplicates or mark as duplicates?
7. **Video Support:** Should the system also handle video files?
8. **RAW Image Formats:** Support for RAW camera formats (CR2, NEF, etc.)?

## 11. Future Enhancements

- Face detection and recognition
- Automatic tagging using ML/AI
- Geolocation mapping of images
- Duplicate image detection with visual similarity
- Batch operations (bulk tagging, metadata updates)
- Export functionality (create albums, slideshows)
- Integration with cloud storage (Google Photos, Dropbox)
- Mobile app for viewing
- Sharing and collaboration features


# Picture Model App - Web Application Design

## 1. Application Overview

The web app is a single-page application (SPA) that provides an intuitive interface for browsing, searching, and managing image collections indexed from the local hard drive.

## 2. Page Structure & Navigation

### 2.1 Recommended Page Architecture

```
┌─────────────────────────────────────────────────┐
│              App Shell (Persistent)              │
│  ┌────────────────────────────────────────┐     │
│  │  Header / Navigation Bar               │     │
│  │  - Logo/App Name                       │     │
│  │  - Search Bar (global)                 │     │
│  │  - Settings Icon                       │     │
│  └────────────────────────────────────────┘     │
│                                                  │
│  ┌────────────────────────────────────────┐     │
│  │         Main Content Area              │     │
│  │         (Dynamic Pages)                │     │
│  │                                         │     │
│  │  - Gallery Page (Landing)              │     │
│  │  - Image Detail Page                   │     │
│  │  - Search Results Page                 │     │
│  │  - Crawler Management Page             │     │
│  │  - Tags Management Page                │     │
│  │  - Settings Page                       │     │
│  └────────────────────────────────────────┘     │
└─────────────────────────────────────────────────┘
```

### 2.2 Page Definitions

#### **LANDING PAGE: Gallery View** (`/` or `/gallery`)

**Purpose:** Primary interface for browsing all indexed images

**Layout:**
```
┌──────────────────────────────────────────────────────┐
│  Header: [Logo] [Search: ____________] [⚙ Settings]  │
├──────────────────────────────────────────────────────┤
│  Filters Sidebar (Collapsible)    │  Image Grid      │
│  ┌─────────────────────────┐      │  ┌───┐ ┌───┐    │
│  │ □ All Images            │      │  │img│ │img│    │
│  │ □ Recent (Last 30 days) │      │  └───┘ └───┘    │
│  │ □ This Year             │      │  ┌───┐ ┌───┐    │
│  │                         │      │  │img│ │img│    │
│  │ Tags:                   │      │  └───┘ └───┘    │
│  │ ☑ vacation              │      │                  │
│  │ ☐ family                │      │  [Load More...] │
│  │ ☐ work                  │      │                  │
│  │                         │      │                  │
│  │ Sort by:                │      │                  │
│  │ ● Date (newest)         │      │                  │
│  │ ○ Date (oldest)         │      │                  │
│  │ ○ File name             │      │                  │
│  │ ○ File size             │      │                  │
│  └─────────────────────────┘      │                  │
└──────────────────────────────────────────────────────┘
```

**Features:**
- Grid of image thumbnails (responsive: 2-8 columns based on screen size)
- Hover: Show filename, date, size
- Click: Navigate to Image Detail Page
- Infinite scroll or pagination
- Multi-select mode (checkbox overlay) for batch operations
- View options: Grid size (small/medium/large thumbnails)

**Key Actions:**
- Filter by date range, tags, file type
- Sort by various criteria
- Select multiple images for batch tagging
- Quick view (lightbox without leaving page)

---

#### **Image Detail Page** (`/image/{id}`)

**Purpose:** View full-resolution image with complete metadata

**Layout:**
```
┌──────────────────────────────────────────────────────┐
│  [← Back to Gallery]                    [⚙ Settings] │
├──────────────────────────────────────────────────────┤
│                                          │            │
│                                          │ METADATA   │
│                                          │ ────────── │
│         ┌────────────────────┐          │ Filename:  │
│         │                    │          │ vacation.  │
│         │                    │          │   jpg      │
│         │    FULL IMAGE      │          │            │
│         │     DISPLAY        │          │ Size:      │
│         │                    │          │ 2.1 MB     │
│         │                    │          │            │
│         └────────────────────┘          │ Dimensions:│
│                                          │ 1920x1080  │
│    [◄ Previous]  [Next ►]              │            │
│                                          │ Created:   │
│                                          │ 2025-06-15│
│                                          │            │
│                                          │ Camera:    │
│                                          │ Canon R5   │
│                                          │            │
│                                          │ Tags:      │
│                                          │ [vacation] │
│                                          │ [travel]   │
│                                          │ [+ Add]    │
│                                          │            │
│                                          │ Custom     │
│                                          │ Metadata:  │
│                                          │ ────────── │
│                                          │ Description│
│                                          │ [_______ ] │
│                                          │            │
│                                          │ Location:  │
│                                          │ [_______ ] │
│                                          │            │
│                                          │ [Save]     │
└──────────────────────────────────────────────────────┘
```

**Features:**
- Full-size image display (with zoom controls)
- Previous/Next navigation (keyboard arrows)
- Read-only metadata from EXIF
- Editable user metadata fields
- Tag management (add/remove)
- Copy file path button
- Download/Share options
- Delete button (marks as deleted in DB)

**Key Actions:**
- Navigate between images (← →)
- Zoom in/out/fit
- Edit custom metadata
- Add/remove tags
- Save changes

---

#### **Search Results Page** (`/search?q=...`)

**Purpose:** Display results from search queries

**Layout:**
```
┌──────────────────────────────────────────────────────┐
│  Search: [paris eiffel tower________] [🔍]           │
├──────────────────────────────────────────────────────┤
│  Showing 24 results for "paris eiffel tower"         │
│                                                       │
│  Filters: [All] [This Month] [Has Tags] [Large]     │
│                                                       │
│  ┌───┐ ┌───┐ ┌───┐ ┌───┐                            │
│  │img│ │img│ │img│ │img│  vacation.jpg               │
│  └───┘ └───┘ └───┘ └───┘  Matches: filename          │
│                                                       │
│  ┌───┐ ┌───┐ ┌───┐ ┌───┐                            │
│  │img│ │img│ │img│ │img│  paris_2024.jpg             │
│  └───┘ └───┘ └───┘ └───┘  Matches: filename, location│
│                                                       │
│  [Load More Results...]                              │
└──────────────────────────────────────────────────────┘
```

**Features:**
- Same grid layout as Gallery
- Search term highlighting/matching indicators
- Relevance sorting
- Advanced filters specific to search
- "Did you mean..." suggestions
- Search within results

**Key Actions:**
- Refine search
- Same actions as Gallery (view, select, batch operations)

---

#### **Crawler Management Page** (`/crawler`)

**Purpose:** Control and monitor image indexing process

**Layout:**
```
┌──────────────────────────────────────────────────────┐
│  Crawler Management                                   │
├──────────────────────────────────────────────────────┤
│  ┌────────────────────────────────────────────────┐  │
│  │ Start New Crawl                                │  │
│  │                                                │  │
│  │ Directory Path: [/Users/user/Pictures____]    │  │
│  │                                                │  │
│  │ ☑ Incremental (only check for changes)        │  │
│  │ ☑ Extract EXIF metadata                       │  │
│  │ ☑ Generate thumbnails                         │  │
│  │                                                │  │
│  │         [Start Crawl]                          │  │
│  └────────────────────────────────────────────────┘  │
│                                                       │
│  Current Crawl Job                                    │
│  ────────────────────────────────────────────────    │
│  Status: IN PROGRESS                                  │
│  ████████████░░░░░░░░░░  60% complete                │
│  Files processed: 6,234 / 10,000                      │
│  New images: 145                                      │
│  Updated: 23                                          │
│  Errors: 2                                            │
│  Elapsed time: 5 minutes 32 seconds                   │
│                                                       │
│  [Pause] [Cancel]                                     │
│                                                       │
│  ─────────────────────────────────────────────────   │
│  Crawl History                                        │
│  ─────────────────────────────────────────────────   │
│  ┌────────────────────────────────────────────────┐  │
│  │ 2025-01-27 10:30 AM    COMPLETED    ✓         │  │
│  │ /Users/user/Pictures                           │  │
│  │ Processed: 10,000 | Added: 145 | Updated: 23   │  │
│  └────────────────────────────────────────────────┘  │
│  ┌────────────────────────────────────────────────┐  │
│  │ 2025-01-26 03:00 PM    COMPLETED    ✓         │  │
│  │ /Users/user/Pictures                           │  │
│  │ Processed: 9,850 | Added: 320 | Updated: 15    │  │
│  └────────────────────────────────────────────────┘  │
└──────────────────────────────────────────────────────┘
```

**Features:**
- Directory picker/input
- Crawl configuration options
- Real-time progress indicator
- Pause/resume/cancel controls
- History of previous crawl jobs
- Error log viewer
- Scheduled crawl setup (future feature)

**Key Actions:**
- Start new crawl
- Monitor progress
- View crawl history
- Review errors

---

#### **Tags Management Page** (`/tags`)

**Purpose:** Manage all tags used in the system

**Layout:**
```
┌──────────────────────────────────────────────────────┐
│  Tags Management                                      │
├──────────────────────────────────────────────────────┤
│  [+ Create New Tag]                                   │
│                                                       │
│  All Tags (24)                                        │
│  ─────────────────────────────────────────────────   │
│  ┌──────────────────────────────────────────┐        │
│  │ [vacation] 🟠           234 images  [✎][×]│        │
│  └──────────────────────────────────────────┘        │
│  ┌──────────────────────────────────────────┐        │
│  │ [family] 🔵             156 images  [✎][×]│        │
│  └──────────────────────────────────────────┘        │
│  ┌──────────────────────────────────────────┐        │
│  │ [work] 🟢                89 images  [✎][×]│        │
│  └──────────────────────────────────────────┘        │
│  ┌──────────────────────────────────────────┐        │
│  │ [travel] 🟡             201 images  [✎][×]│        │
│  └──────────────────────────────────────────┘        │
│                                                       │
│  Sort by: [Name ▼] [Most Used] [Recently Used]       │
└──────────────────────────────────────────────────────┘

Modal: Create/Edit Tag
┌────────────────────────────────────┐
│  Create New Tag                    │
│                                    │
│  Tag Name: [______________]        │
│                                    │
│  Color: [🔴][🟠][🟡][🟢][🔵][🟣] │
│                                    │
│  [Cancel]  [Save]                  │
└────────────────────────────────────┘
```

**Features:**
- List all tags with usage count
- Create new tags
- Edit tag name and color
- Delete tags (with confirmation)
- Merge tags (future feature)
- Click tag to view all images with that tag

**Key Actions:**
- Create tag
- Edit tag properties
- Delete unused tags
- View images by tag

---

#### **Settings Page** (`/settings`)

**Purpose:** Configure application preferences

**Layout:**
```
┌──────────────────────────────────────────────────────┐
│  Settings                                             │
├──────────────────────────────────────────────────────┤
│  ┌─ Display ───────────────────────────────────────┐ │
│  │                                                  │ │
│  │ Thumbnail Size:                                  │ │
│  │ ○ Small  ● Medium  ○ Large                      │ │
│  │                                                  │ │
│  │ Images per page: [24 ▼]                         │ │
│  │                                                  │ │
│  │ Theme:                                           │ │
│  │ ○ Light  ● Dark  ○ Auto                         │ │
│  │                                                  │ │
│  └──────────────────────────────────────────────────┘ │
│                                                       │
│  ┌─ Crawler ───────────────────────────────────────┐ │
│  │                                                  │ │
│  │ Default Crawl Directory:                         │ │
│  │ [/Users/user/Pictures____________]               │ │
│  │                                                  │ │
│  │ ☑ Auto-crawl on startup                         │ │
│  │ ☑ Watch for file changes                        │ │
│  │                                                  │ │
│  └──────────────────────────────────────────────────┘ │
│                                                       │
│  ┌─ Performance ───────────────────────────────────┐ │
│  │                                                  │ │
│  │ Thumbnail quality: [High ▼]                      │ │
│  │                                                  │ │
│  │ Maximum concurrent crawls: [4 ▼]                 │ │
│  │                                                  │ │
│  │ ☑ Cache full images                             │ │
│  │                                                  │ │
│  └──────────────────────────────────────────────────┘ │
│                                                       │
│  ┌─ Database ──────────────────────────────────────┐ │
│  │                                                  │ │
│  │ Total Images: 10,234                             │ │
│  │ Database Size: 245 MB                            │ │
│  │                                                  │ │
│  │ [Clear Thumbnail Cache]                          │ │
│  │ [Rebuild Search Index]                           │ │
│  │ [Export Database]                                │ │
│  │                                                  │ │
│  └──────────────────────────────────────────────────┘ │
│                                                       │
│  [Save Settings]                                      │
└──────────────────────────────────────────────────────┘
```

**Features:**
- Display preferences
- Crawler configuration
- Performance tuning
- Database maintenance tools
- Theme selection
- Keyboard shortcuts reference

---

## 3. Navigation Structure

### 3.1 Primary Navigation (Always Visible)

**Top Navigation Bar:**
- **Logo/App Name** (left) → Click returns to Gallery
- **Global Search Bar** (center)
- **Navigation Links:**
    - Gallery (home icon)
    - Crawler (refresh icon)
    - Tags (tag icon)
- **Settings Icon** (right)

### 3.2 Routing Map

```
/ or /gallery          → Gallery View (Landing Page)
/image/:id             → Image Detail Page
/search?q=term         → Search Results Page
/crawler               → Crawler Management Page
/tags                  → Tags Management Page
/settings              → Settings Page
```

### 3.3 User Flow Examples

**Flow 1: First-time User**
1. Land on Gallery (empty state)
2. Click "Start Crawl" button in empty state
3. Navigate to Crawler page
4. Configure and start crawl
5. Return to Gallery to see results

**Flow 2: Browse and Tag**
1. Land on Gallery with images
2. Click thumbnail → Image Detail page
3. Add tags to image
4. Click Next to move through images
5. Return to Gallery

**Flow 3: Search and Organize**
1. Use global search bar
2. View Search Results page
3. Multi-select matching images
4. Batch add tags
5. Return to Gallery with filters applied

---

## 4. UI Components Library

### 4.1 Core Components

1. **ImageThumbnail**
    - Props: imageUrl, fileName, date, size, selected
    - Events: onClick, onSelect, onHover

2. **ImageGrid**
    - Props: images[], columns, loading
    - Features: Responsive, infinite scroll

3. **MetadataEditor**
    - Props: metadata{}, editable
    - Events: onSave, onCancel

4. **TagPill**
    - Props: tagName, color, removable
    - Events: onClick, onRemove

5. **SearchBar**
    - Props: placeholder, value
    - Events: onSearch, onChange
    - Features: Autocomplete, recent searches

6. **FilterSidebar**
    - Props: filters{}, activeFilters
    - Events: onFilterChange

7. **ProgressBar**
    - Props: current, total, status
    - Features: Percentage, time estimate

8. **Lightbox**
    - Props: imageUrl, metadata
    - Events: onClose, onNext, onPrevious
    - Features: Zoom, keyboard navigation

---

## 5. Responsive Design Breakpoints

```
Mobile:      < 768px   → 1-2 columns, hamburger menu
Tablet:      768-1024  → 3-4 columns, side navigation
Desktop:     > 1024px  → 4-8 columns, full navigation
Large:       > 1440px  → 6-8 columns, spacious layout
```

---

## 6. Empty States

**Gallery (No Images):**
```
┌──────────────────────────────────────┐
│         📷                           │
│                                      │
│    No images found                   │
│                                      │
│    Start by crawling a directory     │
│    to index your images.             │
│                                      │
│    [Go to Crawler]                   │
└──────────────────────────────────────┘
```

**Search (No Results):**
```
┌──────────────────────────────────────┐
│         🔍                           │
│                                      │
│    No results for "xyz"              │
│                                      │
│    Try different keywords or         │
│    check your filters.               │
│                                      │
│    [Clear Search]                    │
└──────────────────────────────────────┘
```

---

## 7. Key Features Summary

### Must-Have (MVP)
- ✅ Gallery View with thumbnail grid
- ✅ Image Detail View with metadata
- ✅ Search functionality
- ✅ Tag management (add/remove)
- ✅ Crawler management interface
- ✅ Filtering by date and tags

### Nice-to-Have (v2)
- 🔹 Lightbox quick view
- 🔹 Batch operations (multi-select)
- 🔹 Keyboard shortcuts
- 🔹 Advanced search filters
- 🔹 Sort options
- 🔹 Tag color coding

### Future Enhancements
- 🔮 Slideshows
- 🔮 Albums/Collections
- 🔮 Image editing (crop, rotate)
- 🔮 Face detection
- 🔮 Map view (geolocation)
- 🔮 Timeline view
- 🔮 Favorites/Ratings
- 🔮 Comments on images

---

## 8. Design Questions for Decision

### 8.1 Landing Page Question
**Which landing page style do you prefer?**

**Option A: Gallery-First (Recommended)**
- Immediately show image grid
- Filters in collapsible sidebar
- Best for users who browse regularly
- Minimal friction to content

**Option B: Dashboard-First**
- Statistics overview (total images, recent additions)
- Quick actions (start crawl, recent searches)
- Trending tags widget
- Better for periodic users

**Option C: Search-First**
- Large search bar as hero element
- Suggested tags below
- Recent images underneath
- Best for users who know what they want

### 8.2 Image Detail Layout
**Metadata placement preference?**

**Option A: Right Sidebar (Recommended)**
- Image on left, metadata on right
- Good for landscape images
- Vertical scroll for long metadata

**Option B: Bottom Panel**
- Image on top, metadata below
- Better for portrait images
- Horizontal layout

**Option C: Overlay**
- Metadata overlays bottom of image
- Cleaner look, more screen space for image
- Toggleable (press 'i' for info)

### 8.3 Thumbnail Size
**Default thumbnail size?**

- Small: 150x150px (8-10 per row)
- Medium: 250x250px (4-6 per row) ← Recommended
- Large: 350x350px (2-4 per row)

### 8.4 Color Scheme
**Application theme?**

- Light mode (white/gray backgrounds)
- Dark mode (black/dark gray) ← Recommended for image viewing
- Auto (follow system preference)

---

## 9. Recommendations Summary

### Landing Page
**Recommendation:** Gallery-First (Option A)
- Most intuitive for image browsing app
- Users want to see their images immediately
- Filters available but not intrusive

### Layout Priority
1. **Desktop-first design** (primary use case)
2. Responsive mobile view as secondary
3. Focus on image browsing efficiency

### MVP Pages (Phase 5)
1. **Gallery View** (Landing) - Priority 1
2. **Image Detail View** - Priority 1
3. **Crawler Management** - Priority 2
4. **Settings** - Priority 3
5. **Tags Management** - Priority 3
6. **Search Results** - Can reuse Gallery view with filters

---

## 10. Next Steps

Once you decide on the design questions above, we can:
1. Create wireframes/mockups for each page
2. Define the component hierarchy
3. Choose a frontend framework (React/Vue/Vanilla)
4. Set up the frontend project structure
5. Begin implementing the UI components

What are your preferences for the design questions in Section 8?
