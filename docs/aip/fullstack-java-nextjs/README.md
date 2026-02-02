# Application Implementation Pattern: fullstack-java-nextjs

Derived from the "Picture Model" application in this repo. This pattern standardizes how to build a
full-stack app with a Next.js (App Router) UI and a Spring Boot REST API (MVC + JPA), run locally on a
single machine, and optionally deploy as two services.

## What You Get

- Two deployable units:
  - `ui/`: Next.js 15 App Router UI (TypeScript, Tailwind, React Query).
  - `api/`: Spring Boot 3.2.x API (Java 21, Spring MVC, Spring Data JPA, Actuator, Springdoc OpenAPI).
- A clear contract:
  - REST under `/api/*`
  - Optional WebSocket for progress/events
  - OpenAPI at `/v3/api-docs`
- Local dev ergonomics via `Makefile` + Docker Compose for PostgreSQL.

## When To Use

Use this pattern when you want:
- A modern web UI (Next.js) that iterates quickly.
- A strongly-typed backend with transactional persistence (JPA).
- A clean separation of UI/API with a stable REST contract.
- Support for background jobs (e.g., crawls, imports) and streaming file responses.

## Stack (As Implemented Here)

Backend (`api/`):
- Java 21
- Spring Boot 3.2.1
- Spring MVC, Validation, WebSocket
- Spring Data JPA (Hibernate)
- PostgreSQL (prod) + H2 (dev)
- Spring Boot Actuator
- Springdoc OpenAPI (2.3.0)

Frontend (`ui/`):
- Node.js 20+
- Next.js 15.5.x
- React 19
- TypeScript 5.7.x
- Tailwind CSS 3.4.x
- React Query 5
- Zustand 4
- axios

## Quickstart (Local Dev)

- Start PostgreSQL:
  - `make db-up`
- Load schema/seed data (if configured):
  - `make db-load`
- Run backend + UI:
  - `make run`
- Or run individually:
  - `make run-backend`
  - `make run-ui`

Default URLs:
- API: `http://localhost:8080`
- UI: `http://localhost:3000`

## Key Conventions

- UI talks to API using `NEXT_PUBLIC_API_BASE` (default `http://localhost:8080`).
- API exposes routes under `/api` and the OpenAPI spec at `/v3/api-docs`.
- Prefer "thin controllers" and "thick services": controllers validate/translate, services own business logic.
- Use package-level documentation (`package-info.java`) to keep generated JavaDoc navigable.
- Source files include a metadata header comment at the top of the file (app/package/file/version/date/exports).

## Pattern Docs

- Repo layout: `docs/aip/fullstack-java-nextjs/repo-layout.md`
- Backend conventions: `docs/aip/fullstack-java-nextjs/backend.md`
- Frontend conventions: `docs/aip/fullstack-java-nextjs/frontend.md`
- Integration contract: `docs/aip/fullstack-java-nextjs/contract.md`
- Testing: `docs/aip/fullstack-java-nextjs/testing.md`
- Execution plan template: `docs/aip/fullstack-java-nextjs/execution-plan.md`
