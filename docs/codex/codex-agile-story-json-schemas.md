Creator: Codex

# Agile Story — Create JSON Schemas for Project

Date: January 27, 2026

## Story
As a developer, I want JSON Schemas for the project so that API requests, responses, and persisted data are validated consistently and can be shared between the backend and UI.

## Context (from project_context.md)
- Project name: (not provided)
- Detailed description: (not provided)
- Short description: (not provided)
- Author: Bobwares (bobwares@outlook.com)
- Maven groupId: (not provided)
- Maven artifactId: (not provided)
- Maven name: (not provided)
- Maven description: (not provided)
- Domain:
  - Domain Object
  - REST API Request Schema
  - REST API Response Schema
  - Persisted Data schema

## Acceptance Criteria
- JSON Schemas are created for:
  - Domain Object
  - REST API Request
  - REST API Response
  - Persisted Data
- OpenAPI schema is created and published for the REST API.
- Schemas are versioned and stored in a dedicated directory (e.g., `project_root/schema`).
- Schemas include required fields, types, and validation constraints.
- Schemas are referenced by backend validation and shared with the frontend for form validation.

## Tasks
- Identify the domain objects and their fields.
- Define request/response payload structures for each REST endpoint.
- Define persisted data schema aligned to database design.
- Author JSON Schema files with `$id`, `$schema`, and versioned filenames.
- Author an OpenAPI specification that references the JSON Schemas.
- Add schema lint/validation step (optional).

## Notes
- Fill in the missing project metadata from `project_context.md` before finalizing schema IDs.
- Use lowercase, hyphen-delimited filenames for schemas.
