# Architecture Decision Record

Enable OpenAPI and actuator endpoints via dependencies

**Turn**: 3

**Date**: 2026-01-29 - 23:19

**Context**  
The API returned a `No static resource v3/api-docs` error, indicating OpenAPI endpoints were not registered.

**Options Considered**
1) Add Springdoc OpenAPI starter to provide `/v3/api-docs`.  
2) Implement a custom OpenAPI endpoint manually.

**Decision**  
Chose Springdoc OpenAPI starter to align with Spring Boot 3.x conventions and reduce maintenance overhead. Added actuator starter so `/actuator/health` is available as documented.

**Result**
- Updated `api/pom.xml` to include Springdoc OpenAPI and Spring Boot actuator dependencies.

**Consequences**  
- Increases build dependencies but provides standard OpenAPI and health endpoints.
