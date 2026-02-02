# Application Implementation Pattern (AIP): fullstack-java-nextjs (v2)

This is a generic, agent-oriented implementation pattern for building a full-stack application with:
- a Next.js (App Router) UI (`ui/`)
- a Spring Boot REST API (`api/`)

It is written to be used by a coding agent when creating a new app from scratch.

## Pattern Identity

- Pattern ID: `fullstack-java-nextjs`
- Version: `2`
- Scope: monorepo, two services (UI + API), REST contract, optional WebSocket, local dev via Makefile

## How A Coding Agent Should Use This Pattern

1. Load project context first:
   - `ai/context/project_context.md` (or equivalent in the target repo)
2. From the project context, locate and open this AIP directory:
   - `docs/aip/fullstack-java-nextjs/v2`
3. Follow `agent-guide.md` to:
   - gather requirements
   - produce a small, reviewable execution plan
   - implement incrementally with tests and checkpoints

## What This Pattern Produces

- `/api` Spring Boot service:
  - Java 21
  - Spring MVC + Validation + Actuator + Springdoc
  - Spring Data JPA + PostgreSQL (prod) + H2 (dev)
  - consistent error contract (global exception handler)
- `/ui` Next.js service:
  - Next.js App Router + TypeScript + Tailwind
  - data fetching via React Query
  - centralized `api-client` using `NEXT_PUBLIC_API_BASE`
- `/docs` for contract + operational notes
- Root `Makefile` to keep local workflows fast and repeatable

## Documents

- `agent-guide.md` - the primary "how to build a new app" guide for coding agents
- `context.md` - how to wire this AIP into a project's context so agents reliably reference it
- `repo-layout.md` - standard repo and layering rules
- `contract.md` - UI/API contract conventions and stability rules
- `backend.md` - Spring Boot implementation conventions and defaults
- `frontend.md` - Next.js implementation conventions and defaults
- `execution-plan.md` - turn-friendly task template for agents
- `definition-of-done.md` - quality gates and acceptance checklist

