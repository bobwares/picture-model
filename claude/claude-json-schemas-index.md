---
**Created by:** Claude (AI Coding Agent)
**Date:** 2026-01-27
**Version:** 2.0
---

# Picture Model App - JSON Schemas Index

## Overview

This directory contains JSON Schema definitions for all DTOs, requests, responses, and events used in the Picture Model App REST API and WebSocket interfaces.

All schemas follow the JSON Schema Draft-07 specification and can be used for:
- API documentation generation (OpenAPI/Swagger)
- Request/response validation
- Client code generation
- TypeScript/JavaScript type definitions
- Testing and mock data generation

## Schema Naming Convention

Schemas are organized with descriptive prefixes:

- **`common-enum-*`** - Enumeration types (shared across multiple schemas)
- **`api-request-*`** - Request body schemas for API endpoints
- **`api-response-*`** - Response schemas for API endpoints
- **`ws-event-*`** - WebSocket event schemas for real-time updates

## Directory Structure

```
./claude/schemas/
├── Common Enumerations (4 schemas)
│   ├── common-enum-connection-status.json
│   ├── common-enum-crawl-status.json
│   ├── common-enum-drive-type.json
│   └── common-enum-metadata-source.json
│
├── API Requests (8 schemas)
│   ├── api-request-add-tags.json
│   ├── api-request-create-drive.json
│   ├── api-request-create-tag.json
│   ├── api-request-search-query.json
│   ├── api-request-start-crawl.json
│   ├── api-request-update-drive.json
│   ├── api-request-update-metadata.json
│   └── api-request-update-tag.json
│
├── API Responses (12 schemas)
│   ├── api-response-connection-test-result.json
│   ├── api-response-crawl-job.json
│   ├── api-response-directory-tree.json
│   ├── api-response-drive-status.json
│   ├── api-response-error.json
│   ├── api-response-image-details.json
│   ├── api-response-image-metadata.json
│   ├── api-response-image-summary.json
│   ├── api-response-remote-file-drive.json
│   ├── api-response-search-result.json
│   ├── api-response-system-status.json
│   └── api-response-tag.json
│
└── WebSocket Events (1 schema)
    └── ws-event-crawl-progress.json
```

**Total:** 25 JSON Schema files

---

## Schema Catalog

### Common Enumerations

#### common-enum-drive-type.json
**Purpose:** Remote file drive type enumeration
**Values:** `LOCAL`, `SMB`, `SFTP`, `FTP`
**Used in:** api-request-create-drive, api-response-remote-file-drive

#### common-enum-connection-status.json
**Purpose:** Connection status enumeration for remote drives
**Values:** `DISCONNECTED`, `CONNECTING`, `CONNECTED`, `ERROR`
**Used in:** api-response-remote-file-drive, api-response-drive-status

#### common-enum-crawl-status.json
**Purpose:** Crawl job status enumeration
**Values:** `PENDING`, `IN_PROGRESS`, `PAUSED`, `COMPLETED`, `FAILED`, `CANCELLED`
**Used in:** api-response-crawl-job, ws-event-crawl-progress

#### common-enum-metadata-source.json
**Purpose:** Source of image metadata
**Values:** `EXIF`, `USER_ENTERED`, `AUTO_GENERATED`
**Used in:** api-response-image-metadata

---

### API Request Schemas

#### api-request-create-drive.json
**Endpoint:** `POST /api/drives`
**Purpose:** Create a new remote file drive
**Required:** `name`, `type`, `connectionUrl`, `rootPath`
**Optional:** `credentials`, `autoConnect`, `autoCrawl`

#### api-request-update-drive.json
**Endpoint:** `PUT /api/drives/{id}`
**Purpose:** Update existing drive configuration
**Required:** `name`
**Optional:** `credentials`, `autoConnect`, `autoCrawl`

#### api-request-create-tag.json
**Endpoint:** `POST /api/tags`
**Purpose:** Create a new tag
**Required:** `name`
**Optional:** `color` (hex color code)

#### api-request-update-tag.json
**Endpoint:** `PUT /api/tags/{id}`
**Purpose:** Update existing tag
**Optional:** `name`, `color`

#### api-request-add-tags.json
**Endpoint:** `POST /api/images/{id}/tags`
**Purpose:** Add tags to an image
**Required:** `tagIds` (array of UUIDs)

#### api-request-update-metadata.json
**Endpoint:** `PUT /api/images/{id}/metadata`
**Purpose:** Update image metadata
**Format:** Map of key-value pairs (e.g., `{"description": "Sunset", "location": "Beach"}`)

#### api-request-start-crawl.json
**Endpoint:** `POST /api/crawler/start`
**Purpose:** Start a new crawl job
**Required:** `driveId`
**Optional:** `rootPath`, `isIncremental`, `extractExif`, `generateThumbnails`

#### api-request-search-query.json
**Endpoint:** `GET /api/search` (query parameters)
**Purpose:** Search images with filters and pagination
**Optional Fields:**
- `text` - Search term
- `driveId` - Filter by drive
- `tagIds` - Filter by tags
- `mimeType`, `fromDate`, `toDate`, `minSize`, `maxSize`, `minWidth`, `minHeight`
- `page`, `size`, `sort`

---

### API Response Schemas

#### api-response-remote-file-drive.json
**Endpoints:**
- `POST /api/drives` (response)
- `GET /api/drives` (response array)
- `GET /api/drives/{id}` (response)
- `PUT /api/drives/{id}` (response)

**Purpose:** Complete drive information
**Key Fields:** `id`, `name`, `type`, `status`, `imageCount`, `lastConnected`, `lastCrawled`

#### api-response-drive-status.json
**Endpoint:** `GET /api/drives/{id}/status`
**WebSocket:** `/ws/drives/{driveId}` (event)
**Purpose:** Current connection status of a drive
**Key Fields:** `driveId`, `status`, `isHealthy`, `latencyMs`

#### api-response-connection-test-result.json
**Endpoint:** `POST /api/drives/{id}/test`
**Purpose:** Result of testing a drive connection
**Key Fields:** `success`, `message`, `latencyMs`, `driveInfo`

#### api-response-directory-tree.json
**Endpoint:** `GET /api/drives/{id}/tree`
**Purpose:** Hierarchical directory structure
**Key Fields:** `path`, `name`, `imageCount`, `totalImageCount`, `children` (recursive)

#### api-response-image-summary.json
**Endpoints:**
- `GET /api/images` (response array)
- `GET /api/drives/{id}/images` (response array)
- `GET /api/search` (response array)

**Purpose:** Lightweight image DTO for list/grid views
**Key Fields:** `id`, `fileName`, `thumbnailUrl`, `fileSize`, `mimeType`, `tags`

#### api-response-image-details.json
**Endpoints:**
- `GET /api/images/{id}` (response)
- `PUT /api/images/{id}/metadata` (response)
- `POST /api/images/{id}/tags` (response)

**Purpose:** Complete image details with full metadata
**Key Fields:** `id`, `fullPath`, `imageUrl`, `metadata` (array), `tags` (array)

#### api-response-image-metadata.json
**Used in:** api-response-image-details
**Purpose:** Individual metadata key-value pair
**Key Fields:** `key`, `value`, `source`, `lastModified`

#### api-response-tag.json
**Endpoints:**
- `GET /api/tags` (response array)
- `POST /api/tags` (response)
- `PUT /api/tags/{id}` (response)

**Purpose:** Tag information with usage statistics
**Key Fields:** `id`, `name`, `color`, `usageCount`, `createdDate`

#### api-response-crawl-job.json
**Endpoints:**
- `POST /api/crawler/start` (response)
- `GET /api/crawler/jobs/{id}` (response)
- `GET /api/crawler/jobs` (response array)

**Purpose:** Crawl job status and statistics
**Key Fields:** `id`, `status`, `filesProcessed`, `filesAdded`, `filesUpdated`, `errorCount`, `progressPercentage`

#### api-response-search-result.json
**Endpoint:** `GET /api/search` (response)
**Purpose:** Paginated search results
**Key Fields:** `content` (array of ImageSummaryDto), `page`, `size`, `totalElements`, `totalPages`

#### api-response-error.json
**Used in:** All endpoints (error responses)
**Purpose:** Standard error response format
**Structure:**
```json
{
  "error": {
    "code": "not_found",
    "message": "Resource not found",
    "timestamp": "2026-01-27T12:00:00Z",
    "details": {}
  }
}
```

**Error Codes:**
- `not_found`, `validation_error`, `io_error`, `drive_not_connected`
- `bad_request`, `conflict`, `unsupported_media`, `timeout`, `server_error`

#### api-response-system-status.json
**Endpoint:** `GET /api/system/status`
**Purpose:** Overall system health and statistics
**Key Fields:** `status`, `version`, `totalImages`, `totalDrives`, `connectedDrives`, `uptimeMs`

---

### WebSocket Event Schemas

#### ws-event-crawl-progress.json
**WebSocket:** `/ws/crawler/{jobId}`
**Purpose:** Real-time crawl progress updates
**Key Fields:** `jobId`, `status`, `filesProcessed`, `currentPath`, `progressPercentage`

**Example Usage:**
```javascript
const socket = new SockJS('/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({}, () => {
  stompClient.subscribe('/topic/crawler/' + jobId, (message) => {
    const event = JSON.parse(message.body);
    // event conforms to ws-event-crawl-progress.json
  });
});
```

---

## API Endpoint to Schema Mapping

### Drive Management

| Method | Endpoint | Request Schema | Response Schema |
|--------|----------|----------------|-----------------|
| POST | `/api/drives` | api-request-create-drive | api-response-remote-file-drive |
| GET | `/api/drives` | - | api-response-remote-file-drive[] |
| GET | `/api/drives/{id}` | - | api-response-remote-file-drive |
| PUT | `/api/drives/{id}` | api-request-update-drive | api-response-remote-file-drive |
| DELETE | `/api/drives/{id}` | - | 204 No Content |
| POST | `/api/drives/{id}/connect` | - | api-response-remote-file-drive |
| POST | `/api/drives/{id}/disconnect` | - | 204 No Content |
| GET | `/api/drives/{id}/status` | - | api-response-drive-status |
| POST | `/api/drives/{id}/test` | - | api-response-connection-test-result |
| GET | `/api/drives/{id}/tree` | Query: `path` | api-response-directory-tree |
| GET | `/api/drives/{id}/images` | Query: `path`, pagination | api-response-search-result |

### Image Management

| Method | Endpoint | Request Schema | Response Schema |
|--------|----------|----------------|-----------------|
| GET | `/api/images` | Query: api-request-search-query | api-response-search-result |
| GET | `/api/images/{id}` | - | api-response-image-details |
| PUT | `/api/images/{id}/metadata` | api-request-update-metadata | api-response-image-details |
| POST | `/api/images/{id}/tags` | api-request-add-tags | api-response-image-details |
| DELETE | `/api/images/{id}/tags/{tagId}` | - | 204 No Content |
| DELETE | `/api/images/{id}` | - | 204 No Content |

### Tag Management

| Method | Endpoint | Request Schema | Response Schema |
|--------|----------|----------------|-----------------|
| GET | `/api/tags` | - | api-response-tag[] |
| POST | `/api/tags` | api-request-create-tag | api-response-tag |
| PUT | `/api/tags/{id}` | api-request-update-tag | api-response-tag |
| DELETE | `/api/tags/{id}` | - | 204 No Content |
| GET | `/api/tags/{id}/images` | Pagination | api-response-search-result |

### Crawler

| Method | Endpoint | Request Schema | Response Schema |
|--------|----------|----------------|-----------------|
| POST | `/api/crawler/start` | api-request-start-crawl | api-response-crawl-job |
| GET | `/api/crawler/jobs/{id}` | - | api-response-crawl-job |
| POST | `/api/crawler/jobs/{id}/pause` | - | api-response-crawl-job |
| POST | `/api/crawler/jobs/{id}/resume` | - | api-response-crawl-job |
| POST | `/api/crawler/jobs/{id}/cancel` | - | api-response-crawl-job |
| GET | `/api/crawler/jobs` | Query: `driveId`, `limit` | api-response-crawl-job[] |

### Search

| Method | Endpoint | Request Schema | Response Schema |
|--------|----------|----------------|-----------------|
| GET | `/api/search` | Query: api-request-search-query | api-response-search-result |

### System

| Method | Endpoint | Request Schema | Response Schema |
|--------|----------|----------------|-----------------|
| GET | `/api/system/status` | - | api-response-system-status |

### File Serving

| Method | Endpoint | Request Schema | Response Schema |
|--------|----------|----------------|-----------------|
| GET | `/api/files/{driveId}/{imageId}` | - | Binary (image/*) |
| GET | `/api/files/{driveId}/{imageId}/thumbnail` | Query: `size` | Binary (image/jpeg) |

---

## Schema Dependencies

### Cross-References

```
api-request-create-drive
  └─> common-enum-drive-type

api-response-remote-file-drive
  ├─> common-enum-drive-type
  └─> common-enum-connection-status

api-response-drive-status
  └─> common-enum-connection-status

api-response-image-details
  ├─> api-response-image-metadata
  └─> api-response-tag

api-response-image-metadata
  └─> common-enum-metadata-source

api-response-crawl-job
  └─> common-enum-crawl-status

api-response-search-result
  └─> api-response-image-summary

ws-event-crawl-progress
  └─> common-enum-crawl-status
```

---

## Usage Examples

### Validating Request Bodies (JavaScript/Node.js)

```javascript
const Ajv = require('ajv');
const ajv = new Ajv();

// Load schema
const createDriveSchema = require('./schemas/api-request-create-drive.json');

// Validate request
const requestBody = {
  name: "Home NAS",
  type: "SMB",
  connectionUrl: "smb://nas.local/photos",
  rootPath: "/",
  autoConnect: true
};

const valid = ajv.validate(createDriveSchema, requestBody);
if (!valid) {
  console.error('Validation errors:', ajv.errors);
}
```

### Generating TypeScript Types

```bash
# Using json-schema-to-typescript
npx json-schema-to-typescript ./claude/schemas/*.json \
  --output ./src/types/ \
  --bannerComment "/* Auto-generated from JSON Schema */"
```

### OpenAPI/Swagger Integration

```yaml
openapi: 3.0.3
info:
  title: Picture Model API
  version: 2.0.0

components:
  schemas:
    RemoteFileDrive:
      $ref: './schemas/api-response-remote-file-drive.json'
    CreateDriveRequest:
      $ref: './schemas/api-request-create-drive.json'
    ErrorResponse:
      $ref: './schemas/api-response-error.json'

paths:
  /api/drives:
    post:
      requestBody:
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/CreateDriveRequest'
      responses:
        '201':
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/RemoteFileDrive'
```

---

## Testing with Schemas

### Jest Example

```javascript
const Ajv = require('ajv');
const crawlJobSchema = require('./schemas/api-response-crawl-job.json');

describe('Crawler API', () => {
  const ajv = new Ajv();
  const validate = ajv.compile(crawlJobSchema);

  test('GET /api/crawler/jobs/{id} returns valid schema', async () => {
    const response = await fetch('/api/crawler/jobs/123');
    const data = await response.json();

    expect(validate(data)).toBe(true);
    if (!validate(data)) {
      console.error(validate.errors);
    }
  });
});
```

### Postman Collection

You can import these schemas into Postman for automatic request/response validation.

---

## Schema Validation Rules

### Common Patterns

- **UUIDs:** All IDs use format `uuid` (RFC 4122)
- **Timestamps:** Use format `date-time` (ISO 8601)
- **Hex Colors:** Pattern `^#[0-9A-Fa-f]{6}$`
- **File Hashes:** SHA-256, pattern `^[a-f0-9]{64}$`
- **Paths:** Max length 2000 characters
- **Names:** Max length varies (see individual schemas)

### Naming Conventions

**Files:**
- Enums: `common-enum-{name}.json`
- Requests: `api-request-{action}.json`
- Responses: `api-response-{entity}.json`
- Events: `ws-event-{name}.json`

**JSON Properties:**
- Use camelCase: `fileName`, `driveId`, `imageCount`
- Boolean flags: `isHealthy`, `autoConnect`, `isIncremental`
- Timestamps: suffix with `Date` or `At`: `createdDate`, `capturedAt`

---

## Schema Versioning

**Current Version:** 2.0

All schemas are versioned with the API version.

**Versioning Rules:**
- Breaking changes → New major version (e.g., 2.0 → 3.0)
- Adding optional fields → Minor version (e.g., 2.0 → 2.1)
- Documentation updates → No version change

---

## Schema Statistics

| Category | Count | Description |
|----------|-------|-------------|
| **Common Enums** | 4 | Shared enumeration types |
| **API Requests** | 8 | Request body schemas |
| **API Responses** | 12 | Response DTO schemas |
| **WebSocket Events** | 1 | Real-time event schemas |
| **Total** | **25** | All schemas |

---

## Future Enhancements

### Planned Schemas (v2.1+)

- `api-response-settings.json` - Application settings
- `api-response-user.json` - User account (multi-user support)
- `api-response-album.json` - Image albums/collections
- `api-response-share-link.json` - Public share links
- `ws-event-drive-status.json` - Drive connection events

---

## Contributing

When adding new schemas:

1. **Use appropriate prefix:**
   - `common-enum-*` for enums
   - `api-request-*` for request bodies
   - `api-response-*` for responses
   - `ws-event-*` for WebSocket events

2. **Follow JSON Schema Draft-07 specification**

3. **Include required fields:**
   - `$schema`: JSON Schema version
   - `$id`: Unique schema identifier
   - `title`: Human-readable name
   - `description`: Purpose and usage

4. **Update this index document:**
   - Add to Schema Catalog
   - Update API Endpoint Mapping
   - Update Schema Statistics

5. **Add validation tests**

---

## References

- [JSON Schema Specification](https://json-schema.org/specification.html)
- [OpenAPI 3.0 Specification](https://swagger.io/specification/)
- [Picture Model Backend Design v2.0](./claude-backend-design.md)
- [Picture Model Domain Design v2.0](./claude-domain-design.md)

---

**Last Updated:** 2026-01-27
**Schema Version:** 2.0
**Maintained by:** Claude (AI Coding Agent)
