# Backend (Spring Boot + JPA)

## Package Map (As Implemented)

- `com.picturemodel.api.controller`: REST controllers under `/api/*`
- `com.picturemodel.api.dto.*`: request/response DTOs
- `com.picturemodel.api.mapper`: mapping between domain and DTOs
- `com.picturemodel.api.exception`: global exception handling to a stable error shape
- `com.picturemodel.service`: service layer (business logic, orchestration, background jobs)
- `com.picturemodel.domain.entity|enums|repository`: domain model + persistence
- `com.picturemodel.infrastructure.filesystem`: drive providers and filesystem abstraction
- `com.picturemodel.infrastructure.security`: credential encryption utility
- `com.picturemodel.config`: CORS/JPA/Jackson/Async/Jasypt/etc.

## Configuration & Profiles

- `api/src/main/resources/application.yml` (base config):
  - server port `8080`
  - production-ish datasource defaults (PostgreSQL)
  - Actuator endpoints exposure (health/info)
  - app-specific config under `picture-model.*` (drives, crawler, thumbnails, CORS)
- `api/src/main/resources/application-dev.yml`:
  - H2 file DB for local dev
  - JPA `ddl-auto: update`
  - H2 console enabled

### App-Specific Config Keys (Example)

This pattern keeps feature config namespaced under an app root key. In this repo:
- `picture-model.drives.*` (timeouts, health checks)
- `picture-model.crawler.*` (mime types, thread pool, batching, progress update interval)
- `picture-model.thumbnail.*` (cache dir/size, sizes, quality)
- `picture-model.security.*` (encryption key)
- `picture-model.cors.*` (allowed origins/methods)

## REST Boundary Conventions

- Controllers:
  - Validate inputs (Bean Validation where applicable).
  - Return DTOs (not entities) for stable contracts.
  - Delegate to services for behavior.
- Services:
  - Own business logic and transactional work.
  - Use repositories for persistence.
  - Use infrastructure providers (filesystem, encryption) via clear abstractions.

## Error Handling Convention

Use a single global exception handler (e.g., `GlobalExceptionHandler`) to map failures to a consistent
error response payload. This keeps the UI error handling simple and predictable.

## Filesystem Provider Convention

Abstract drive access behind a `FileSystemProvider` interface with per-protocol implementations:
- LOCAL (NIO)
- SMB (jCIFS-ng)
- SFTP (JSch)
- FTP (Apache Commons Net)

Cache connected providers by drive ID and implement:
- `connect()`/`disconnect()`
- health checks and auto-reconnect
- directory tree + file listing
- stream reads for full images and thumbnails

### Drive Connection Lifecycle (Example)

In this repo, connecting a drive means:
- verify credentials (if remote) and verify the configured root is readable
- create/cache a provider per drive ID for reuse (avoid re-auth on every request)
- periodically health-check cached providers and evict/reconnect on failure

## Background Jobs (Crawler)

Long-running tasks should be represented as a job entity (e.g., `CrawlJob`) with status, progress,
timestamps, and error fields, plus a service that can start/cancel/track jobs.

## API Docs & Health

- OpenAPI:
  - JSON spec: `/v3/api-docs`
  - UI (if enabled): `/swagger-ui.html` (Springdoc)
- Health:
  - Actuator: `/actuator/health`
