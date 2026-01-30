# Architecture Decision Record

Document OpenAPI + actuator endpoints in e2e README

**Turn**: 2

**Date**: 2026-01-29 - 23:17

**Context**  
The e2e README needed explicit, clickable links for the OpenAPI and health endpoints to reduce friction when diagnosing API availability.

**Options Considered**
1) Keep plain text hostnames without links.  
2) Add clickable links for the OpenAPI and actuator health endpoints.

**Decision**  
Chose option 2 so developers can jump directly to the OpenAPI JSON and health check.

**Result**
- Updated `e2e/README.md` with clickable OpenAPI and actuator health links.

**Consequences**  
- Improves discoverability and reduces copy/paste errors.
