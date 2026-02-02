# Contract (UI <-> API)

## Base URL

- UI reads `NEXT_PUBLIC_API_BASE` (default `http://localhost:8080`).
- UI calls REST routes under `${NEXT_PUBLIC_API_BASE}/api`.

## Stability Rules

- Controllers return DTOs, not entities.
- Error responses use a single stable DTO shape.
- Pagination is uniform:
  - request: `page`, `size`, optional `sort`
  - response: `content`, `totalElements`, `last` (or your chosen standard)

## OpenAPI

- API exposes an OpenAPI JSON document at `/v3/api-docs`.
- Prefer deriving UI client types from OpenAPI when a project grows large.

## File Streaming

If your app serves files:
- keep file bytes behind `/api/files/*`
- keep metadata behind `/api/*` JSON endpoints
- support thumbnails/variants via query params or separate endpoints

