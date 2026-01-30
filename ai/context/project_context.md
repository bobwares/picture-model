# Picture Model Project Context

## Project Metadata
- **Project Name:** Picture Model
- **Project Description:** Full-stack multi-drive image management system with indexing, tagging, and search capabilities
- **Project Author:** Bobwares (bobwares@outlook.com)
- **Created Date:** 2026-01-27
- **Last Updated:** 2026-01-28

## Application Implementation Pattern
- **Pattern Name:** full-stack-nextjs-spring
- **Base Pattern:** spring-boot-mvc-jpa-postgresql (backend execution)
- **Pattern Description:** Next.js 15 (App Router) frontend + Spring Boot 3.2+ backend
- **Architecture:** Separated UI and API with REST contract over local network
- **Execution:** Backend follows spring-boot-mvc-jpa-postgresql pattern, UI follows ui-nextjs pattern

## Technology Stack

### Frontend (UI)
- **Framework:** Next.js 15.x with App Router
- **Language:** TypeScript 5.9.x
- **UI Library:** React 19.x
- **Styling:** Tailwind CSS 3.4
- **State Management:** React Query 5 + Zustand 4
- **Forms:** react-hook-form 7 + zod 3
- **Testing:** Vitest 2 + React Testing Library 16 + Playwright 1.47
- **Runtime:** Node.js 20 LTS

### Backend (API)
- **Framework:** Spring Boot 3.2.1
- **Language:** Java 21
- **Database:** PostgreSQL 16 (production) / H2 (development)
- **ORM:** Spring Data JPA with Hibernate
- **Build Tool:** Maven
- **API Documentation:** Springdoc OpenAPI
- **WebSocket:** Spring WebSocket with STOMP

### File System Support
- **Local:** Java NIO.2
- **SMB/CIFS:** jCIFS-ng 2.1.7
- **SFTP:** JSch 0.1.55
- **FTP:** Apache Commons Net 3.9.0

### Additional Libraries
- **Jasypt 3.0.5** - Credential encryption
- **Thumbnailator 0.4.19** - Image processing
- **metadata-extractor 2.18.0** - EXIF extraction
- **Lombok** - Code generation

## Domain Overview

### Core Entities
1. **RemoteFileDrive** - Storage locations (LOCAL, SMB, SFTP, FTP)
2. **Image** - Indexed image files with metadata
3. **ImageMetadata** - Key-value metadata (EXIF, user-entered)
4. **Tag** - Organizational tags (many-to-many with images)
5. **CrawlJob** - File system crawl tracking

### Key Features
- Multi-drive connectivity with health monitoring
- Automated file system crawling
- EXIF metadata extraction
- Thumbnail generation (small: 150px, medium: 250px, large: 350px)
- Tag-based organization
- Full-text search
- Real-time progress updates via WebSocket

## UI Design Specifications

### Design Choices (from claude-webapp-design_v2.md)
- ✅ **Landing Page:** Dashboard-first with remote file drives
- ✅ **Image Detail Layout:** Right sidebar (image left, metadata right)
- ✅ **Thumbnail Size:** Medium (250x250px, 4-6 per row)
- ✅ **Color Theme:** Light mode

### Pages
1. **Dashboard (`/`)** - Landing page with drive list, statistics, recent activity
2. **Directory Tree View** - Hierarchical folder navigation
3. **Tag View** - Images organized by tags
4. **Search Results** - Full-text search with filters
5. **Image Detail** - Large image viewer with metadata editor
6. **Settings** - Drive management, crawl configuration

### UI Components
- SearchBar (global)
- ImageGrid (responsive, 4-6 columns)
- ImageThumbnail
- FilterSidebar (collapsible)
- MetadataEditor
- TagPill
- ProgressBar (crawl status)
- DriveCard (connection status)

## Project Structure
```
picture-model/
├── ui/                           # Next.js frontend (to be created)
│   ├── src/
│   │   ├── app/                 # App Router pages
│   │   ├── components/          # Reusable UI components
│   │   ├── features/            # Feature-specific components
│   │   ├── lib/                 # API client, utilities
│   │   ├── hooks/               # Custom React hooks
│   │   ├── store/               # Zustand stores
│   │   └── types/               # TypeScript types
│   ├── public/                  # Static assets
│   ├── package.json
│   └── next.config.mjs
│
├── api/                          # Spring Boot backend
│   ├── src/main/java/com/picturemodel/
│   │   ├── domain/              # Entities, repositories, enums
│   │   ├── infrastructure/      # File system providers, security
│   │   ├── service/             # Business logic
│   │   ├── api/                 # Controllers, DTOs, WebSocket
│   │   └── config/              # Spring configuration
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   └── application-dev.yml
│   └── pom.xml
│
├── db/                           # Database scripts
│   └── init/
├── claude/                       # Claude design documents
├── codex/                        # Codex design documents
└── ai/                           # Agentic pipeline artifacts
    ├── context/
    └── agentic-pipeline/
```

## API Endpoints

### Drive Management
- `POST /api/drives` - Create drive
- `GET /api/drives` - List all drives
- `GET /api/drives/{id}` - Get drive details
- `PUT /api/drives/{id}` - Update drive
- `DELETE /api/drives/{id}` - Delete drive
- `POST /api/drives/{id}/connect` - Connect
- `POST /api/drives/{id}/disconnect` - Disconnect
- `GET /api/drives/{id}/status` - Connection status
- `POST /api/drives/{id}/test` - Test connection
- `GET /api/drives/{id}/tree` - Directory tree

### Image Management
- `GET /api/images` - Search/list with filters
- `GET /api/images/{id}` - Image details
- `PUT /api/images/{id}/metadata` - Update metadata
- `POST /api/images/{id}/tags` - Add tags
- `DELETE /api/images/{id}/tags/{tagId}` - Remove tag
- `DELETE /api/images/{id}` - Soft delete

### Crawler
- `POST /api/crawler/start` - Start crawl job
- `GET /api/crawler/jobs/{id}` - Job status
- `POST /api/crawler/jobs/{id}/cancel` - Cancel job
- `GET /api/crawler/jobs` - List recent jobs

### Tags
- `GET /api/tags` - List all tags
- `POST /api/tags` - Create tag
- `PUT /api/tags/{id}` - Update tag
- `DELETE /api/tags/{id}` - Delete tag

### File Serving
- `GET /api/files/{imageId}` - Stream full image
- `GET /api/files/{imageId}/thumbnail` - Stream thumbnail

### WebSocket
- `/ws/crawler/{jobId}` - Real-time crawl progress
- `/ws/drives/{driveId}` - Drive status updates

## Integration Contract

### UI → API Communication
- **Base URL:** `http://localhost:8080` (configurable via `API_BASE` env var)
- **Protocol:** REST over HTTP
- **Format:** JSON
- **CORS:** Configured for `http://localhost:3000`

### API Contract
- Backend exposes OpenAPI spec at `/v3/api-docs`
- JSON schemas in `claude/schemas/` directory (25 schemas)
- Request/response DTOs match JSON schemas exactly

## Database Schema
- **Tables:** remote_file_drives, images, image_metadata, tags, image_tags, crawl_jobs
- **Indexes:** On frequently queried fields (drive+path unique, fileHash, status, etc.)
- **Generation:** JPA auto-generation via Hibernate (dev), Flyway migrations (prod - optional)

## Build & Run

### Backend
```bash
# Development (H2 database)
cd api
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
./run-dev.sh

# Production (PostgreSQL)
docker-compose up -d postgres
mvn spring-boot:run
```

### Frontend (to be created)
```bash
cd ui
npm install
npm run dev         # Development server (port 3000)
npm run build       # Production build
npm run start       # Production server
```

## Testing Strategy

### Backend
- **Unit Tests:** JUnit 5
- **Integration Tests:** Testcontainers (PostgreSQL)
- **H2:** Fast test execution

### Frontend
- **Unit/Component Tests:** Vitest + React Testing Library
- **E2E Tests:** Playwright
- **Coverage Target:** 80%+

## Current Status (Turn 0 - Pre-Pipeline)

### ✅ Completed
- Project structure created
- Domain layer complete (5 entities, 5 repositories, 4 enums)
- Infrastructure layer complete (4 file system providers + factory)
- Core services implemented:
  - CredentialEncryptionService
  - ConnectionManager (with health checks)
  - DriveService (full CRUD + connection management)
- Configuration classes (5 files: Jasypt, JPA, CORS, Async, Jackson)
- Database setup (Docker Compose, H2 dev config)
- Health check command created
- Agentic pipeline structure initialized

### ⏳ Pending (Turn 1+)
1. **Backend Services:**
   - ImageService (search, CRUD, metadata)
   - CrawlerService (multi-threaded crawling)
   - ExifExtractorService (metadata extraction)
   - ThumbnailService (async generation)
   - SearchService (JPA Specifications)
   - TagService (CRUD + usage counting)

2. **Backend API Layer:**
   - REST Controllers (6 controllers)
   - DTOs (15+ request/response classes)
   - GlobalExceptionHandler
   - WebSocket configuration + handlers

3. **Frontend (Next.js):**
   - Project initialization
   - API client setup (React Query)
   - Core pages (Dashboard, Tree, Tags, Search, Image Detail)
   - Component library
   - State management (Zustand stores)
   - WebSocket integration

4. **Testing:**
   - Backend unit + integration tests
   - Frontend component + E2E tests

5. **Documentation:**
   - OpenAPI/Swagger UI
   - API documentation
   - User guide
   - Deployment guide

## Design Reference Documents
- **Backend:** `claude/claude-backend-design.md`
- **Domain:** `claude/claude-domain-design.md`
- **Web App:** `claude/claude-webapp-design_v2.md`
- **JSON Schemas:** `claude/schemas/` (25 files)
- **Pattern Guide:** `codex/codex-nextjs-spring.md`

## Next Steps
See execution plan for nextjs-spring pattern (custom) for turn-by-turn implementation strategy.
