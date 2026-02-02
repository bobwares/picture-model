# Integration Contract (UI <-> API)

## Base URL

- UI reads `NEXT_PUBLIC_API_BASE` (default `http://localhost:8080`).
- UI calls API routes under `${NEXT_PUBLIC_API_BASE}/api`.

## API Shape

- REST routes are namespaced under `/api/*` (example groups: drives, images, crawler, tags, system).
- File streaming endpoints live under `/api/files/*` (full image + thumbnail variants).
- OpenAPI spec is exposed at `/v3/api-docs` (Springdoc).

### Example Endpoint Groups (As Implemented Here)

- Drives
  - `GET /api/drives`
  - `POST /api/drives`
  - `GET /api/drives/{id}`
  - `PUT /api/drives/{id}`
  - `DELETE /api/drives/{id}`
  - `POST /api/drives/{id}/connect`
  - `POST /api/drives/{id}/disconnect`
  - `GET /api/drives/{id}/status`
  - `POST /api/drives/{id}/test`
  - `GET /api/drives/{id}/tree`
  - `GET /api/drives/{id}/images`
- Images
  - `GET /api/images` (search/list)
  - `GET /api/images/{id}`
  - `PUT /api/images/{id}/metadata`
  - `POST /api/images/{id}/tags`
  - `DELETE /api/images/{id}/tags/{tagId}`
  - `DELETE /api/images/{id}` (soft delete)
- Crawler / Jobs
  - `POST /api/crawler/start`
  - `GET /api/crawler/jobs/{id}`
  - `POST /api/crawler/jobs/{id}/cancel`
  - `GET /api/crawler/jobs`
- Tags
  - `GET /api/tags`
  - `POST /api/tags`
  - `PUT /api/tags/{id}`
  - `DELETE /api/tags/{id}`
- Files
  - `GET /api/files/{imageId}`
  - `GET /api/files/{imageId}/thumbnail?size=small|medium|large`

## CORS

Configure allowed origins in `api/src/main/resources/application.yml` under:
- `picture-model.cors.allowed-origins`

Default allowed dev origins include:
- `http://localhost:3000`
- `http://127.0.0.1:3000`

## Error Contract

Return a consistent error DTO for non-2xx responses (mapped via a global exception handler) so the UI
can render a predictable error message and optional metadata (timestamp, path, status, message, etc.).
