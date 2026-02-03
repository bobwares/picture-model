# Architecture Decision Record

Return Image DTOs (summary/detail) instead of JPA entities to avoid lazy-loading serialization errors.

**Turn**: 15

**Date**: 2026-02-03 - 04:57

**Context**  
The API was returning JPA entities (Image) directly from paged endpoints. Jackson attempted to serialize lazy collections (Image.metadata), but the Hibernate session was already closed (async dispatch), causing LazyInitializationException and breaking UI pages.

**Options Considered**
- Enable Open Session In View (OSIV).
- Eagerly fetch metadata for all list endpoints.
- Return explicit DTOs for list/detail responses and only fetch associations when needed.

**Decision**  
Use DTO responses for Image list/detail endpoints. List endpoints return a summary DTO (no metadata/tags), and the detail endpoint returns a DTO with metadata/tags loaded via an entity graph.

**Result**
- Image list endpoints no longer serialize lazy collections.
- Image detail response can still include metadata and tags.

**Consequences**  
- Adds mapping code and DTO classes, but makes API responses stable and avoids runtime serialization failures.
