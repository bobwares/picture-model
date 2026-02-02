# Execution Plan (Template)

This is a practical, turn-friendly plan for building a new app following the fullstack-java-nextjs
pattern. Adapt tasks to your domain and keep each task "reviewable" (small diffs, clear outputs).

## 1) Bootstrap Repo

- Create `api/` Spring Boot project (Java 21) with:
  - web, validation, data-jpa, actuator, springdoc
  - Postgres + H2 runtime drivers
- Create `ui/` Next.js App Router project (TS + Tailwind).
- Add root `Makefile` targets for common flows.

## 2) Define Domain + Persistence (API)

- Create entities, enums, repositories.
- Add migrations or ensure dev profile uses schema auto-update for early iteration.

Suggested domain primitives (inspired by Picture Model):
- `RemoteFileDrive` (type, root path, credentials ref, connection status)
- `Image` (drive, path, hash, dimensions, timestamps, soft delete)
- `ImageMetadata` (key/value, source, editable)
- `Tag` (name/color, many-to-many with images)
- `CrawlJob` (status/progress/error, drive, path scope)

## 3) Implement Core Services (API)

- Write service layer with clear boundaries:
  - domain rules + persistence
  - infrastructure integration
- Add background job orchestration where needed (crawler/importer).

Suggested service slices:
- drive connection management (connect/disconnect/status/health checks)
- listing directory trees and paged image lists
- image metadata updates and tag assignment
- crawl start/cancel/status and progress emission

## 4) Implement Controllers + DTOs (API)

- Add request/response DTOs.
- Add controller endpoints.
- Add global exception handler + error DTO.

## 5) Implement UI Pages + Components

- Create `ui/lib/api-client.ts` and typed wrappers.
- Build pages:
  - dashboard, tree, image detail, tags, search, settings
- Add shared components:
  - header/nav, image grid, filters, metadata editor

## 6) Add Observability + Operational Glue

- Actuator health/info endpoints.
- Local DB via Docker Compose.
- Document env vars and run steps.

## 7) Add Tests

- API: JUnit 5 + integration tests.
- UI: component tests + E2E (optional).
