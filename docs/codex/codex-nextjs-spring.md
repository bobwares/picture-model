Creator: Codex

# Application Implementation Pattern: nextjs-spring

Date: January 27, 2026

## Purpose
Combine a scalable Next.js App Router UI with a production‑grade Spring Boot MVC + JPA + PostgreSQL backend. The UI runs as a separate web app and talks to the backend via REST over the local network.

## Use Case
Use when you want:
- A modern, scalable Next.js UI (App Router + Tailwind).
- A robust Spring Boot REST API with schema migrations and observability.
- Clear separation between UI and API layers with a shared contract.

## Tech Stack

### Frontend (UI)
- Node.js 20 LTS
- Next.js 15.x (App Router)
- React 19.x
- TypeScript 5.9.x
- Tailwind CSS 3.4
- State: React Query 5 + Zustand 4
- Forms: react-hook-form 7 + zod 3
- Testing: Vitest 2 + React Testing Library 16 + Playwright 1.47

### Backend (API)
- Java 21
- Spring Boot 3.5.5
- Spring MVC (spring-boot-starter-web)
- Spring Data JPA
- PostgreSQL 16.x
- Migrations: Liquibase
- API Docs: Springdoc OpenAPI
- Observability: Spring Boot Actuator
- Testing: JUnit 5 + Testcontainers (PostgreSQL)
- Build: Maven

## Repo Layout (Recommended)

```
project_root
  /ui
    /src
      /app
      /components
      /features
      /domains
      /server
      /lib
      /store
      /hooks
      /styles
    package.json
    next.config.mjs
  /api
    /src/main/java
    /src/main/resources
      application.yml
      db/changelog/db.changelog-master.yml
    pom.xml
  /db
    /sql
  /schema
  /docs
```

## Integration Contract
- UI calls backend via `API_BASE` environment variable.
- Backend serves OpenAPI at `/v3/api-docs` for contract visibility.
- UI services map to backend endpoints under `/api/*`.

## Configuration

### UI Environment
```
API_BASE=http://localhost:8080
```

### Backend application.yml
Use the Spring Boot MVC JPA PostgreSQL pattern defaults (Liquibase + Actuator + Springdoc).

## Runtime Flow
1. Backend runs on the host machine with DB access.
2. UI runs locally or is served as static bundle by the backend (optional).
3. UI makes REST calls to `API_BASE`.

## Standard Endpoints (Example)
- `GET /api/status`
- `GET /api/images`
- `GET /api/images/{id}`
- `PUT /api/images/{id}/metadata`

## Testing Strategy
- UI: unit/integration tests with Vitest + RTL; E2E with Playwright.
- API: JUnit + Testcontainers for DB integration.

## Deployment Notes
- Local‑network deployment by default.
- Configure CORS to allow UI origin.
- Optionally bundle UI static assets into Spring Boot resources.
