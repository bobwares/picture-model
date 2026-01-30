<!--
/**
 * App: Picture Model
 * Package: e2e
 * File: README.md
 * Version: 0.1.1
 * Turns: 1,2
 * Author: codex
 * Date: 2026-01-29T23:17:09Z
 * Exports: N/A
 * Description: Overview of e2e HTTP request tests and API endpoint coverage.
 */
-->
Creator: codex

# E2E HTTP Tests

This folder contains one `.http` file per API endpoint for manual or IDE-based execution.

## OpenAPI Endpoint

The backend serves OpenAPI at:
- [`http://localhost:8080/v3/api-docs`](http://localhost:8080/v3/api-docs)

## Actuator Endpoint

Service health is available at:
- [`http://localhost:8080/actuator/health`](http://localhost:8080/actuator/health)

## Usage

- Open any `.http` file in an HTTP-capable IDE (VS Code REST Client, IntelliJ HTTP Client).
- Update placeholder IDs (e.g., `@driveId`, `@imageId`, `@tagId`) with real values.
- Execute the request to validate API behavior.

## Endpoints Covered

| Method | Path | Description |
| --- | --- | --- |
| GET | /api/drives | List drives |
| POST | /api/drives | Create drive |
| GET | /api/drives/{driveId} | Get drive |
| PUT | /api/drives/{driveId} | Update drive |
| DELETE | /api/drives/{driveId} | Delete drive |
| POST | /api/drives/{driveId}/connect | Connect drive |
| POST | /api/drives/{driveId}/disconnect | Disconnect drive |
| GET | /api/drives/{driveId}/status | Get drive status |
| POST | /api/drives/{driveId}/test | Test drive connection |
| GET | /api/drives/{driveId}/tree | Get drive directory tree |
| GET | /api/images | List images |
| GET | /api/images/{imageId} | Get image detail |
| DELETE | /api/images/{imageId} | Delete image |
| PUT | /api/images/{imageId}/metadata | Update image metadata |
| POST | /api/images/{imageId}/tags | Add tags to image |
| DELETE | /api/images/{imageId}/tags/{tagId} | Remove tag from image |
| GET | /api/tags | List tags |
| POST | /api/tags | Create tag |
| PUT | /api/tags/{tagId} | Update tag |
| DELETE | /api/tags/{tagId} | Delete tag |
| POST | /api/crawler/start | Start crawl job |
| GET | /api/crawler/jobs | List crawl jobs |
| GET | /api/crawler/jobs/{jobId} | Get crawl job |
| POST | /api/crawler/jobs/{jobId}/cancel | Cancel crawl job |
| GET | /api/search | Search images |
| GET | /api/files/{imageId} | Stream full image file |
| GET | /api/files/{imageId}/thumbnail | Stream image thumbnail |
