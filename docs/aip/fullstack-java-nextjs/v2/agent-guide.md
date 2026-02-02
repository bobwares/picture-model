# Agent Guide

This document is written for a coding agent creating a new application using this AIP.

## Inputs The Agent Should Ask For (If Not Provided)

- Product goal and core user journeys (3-5 bullets).
- Data model candidates (entities + relationships).
- API surface:
  - endpoint groups
  - which endpoints need pagination, filtering, sorting
  - which endpoints stream files
- Non-functional requirements:
  - auth (none/basic/session/jwt)
  - performance constraints (image/video, large lists, background jobs)
  - deployment target (local only vs. cloud)

If the user gives incomplete requirements, default to a minimal vertical slice.

## Agent Execution Strategy

Prioritize reviewable increments:

1. Skeleton: repo layout + build/run scripts.
2. Domain + persistence: entities, repositories, migrations or dev ddl.
3. Core services: business logic boundaries.
4. API boundary: DTOs + controllers + error handling.
5. UI baseline: navigation + one working feature flow.
6. Observability: health, logs, OpenAPI.
7. Tests: a small, meaningful baseline.

Do not build "all features" before one end-to-end flow works.

## Default Conventions (Unless The User Overrides)

- Backend is the source of truth for domain invariants.
- Controllers are thin; services hold behavior.
- UI uses React Query for server state and caches.
- Contracts are stable:
  - DTOs are explicit
  - errors are consistent
  - pagination is uniform across endpoints

## Output Artifacts For Each Turn

For each meaningful change, the agent should produce:

- a clear set of diffs (small)
- updated docs when behavior/contract changes
- a runnable verification step (tests or manual steps)

## Common Pitfalls To Avoid

- returning JPA entities directly from controllers (unstable JSON shape)
- mixing filesystem IO into controllers instead of isolating behind a service/provider
- introducing frontend state duplication when React Query caching is sufficient
- unbounded list endpoints (always support `page` + `size` or cursor)

