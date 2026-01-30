# Architecture Decision Record

Expose actuator endpoints in application config

**Turn**: 4

**Date**: 2026-01-29 - 23:22

**Context**  
The actuator health endpoint was not responding, indicating that actuator endpoints were not exposed by configuration.

**Options Considered**
1) Leave actuator exposure at defaults (health not exposed).  
2) Explicitly expose health and info endpoints and show health details.

**Decision**  
Chose option 2 to align with documentation and enable /actuator/health for local diagnostics.

**Result**
- Updated `api/src/main/resources/application.yml` with management endpoint exposure settings.

**Consequences**  
- Health and info are now publicly exposed in dev; adjust for production if needed.
