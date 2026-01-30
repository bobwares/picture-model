# Architecture Decision Record

Document e2e API coverage and OpenAPI endpoint visibility

**Turn**: 1

**Date**: 2026-01-29 - 23:08

**Context**  
The project needed a clear, single place describing the OpenAPI endpoint and the HTTP request tests covering each API route.

**Options Considered**
1) Leave e2e documentation implicit and rely on contributors to discover endpoints in the OpenAPI spec.  
2) Add a dedicated `e2e/README.md` with the OpenAPI endpoint and a complete endpoint catalog.

**Decision**  
Chose option 2 to make endpoint coverage explicit and guide manual execution of the `.http` files.

**Result**
- Added `e2e/README.md` documenting the OpenAPI endpoint and endpoint list.
- Captured turn artifacts for the agentic pipeline.

**Consequences**  
- Improves discoverability of the API contract and e2e coverage.  
- Requires maintenance if endpoints change.
