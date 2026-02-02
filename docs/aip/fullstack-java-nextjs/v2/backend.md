# Backend (Spring Boot)

## Defaults

- Java: 21
- Spring Boot: 3.2+
- Web: Spring MVC
- Persistence: Spring Data JPA
- DB:
  - Postgres for production
  - H2 for dev profile
- Observability: Actuator
- Contract: Springdoc OpenAPI

## Service Boundaries

- Controller responsibilities:
  - validate inputs
  - map request -> service call
  - map service result -> response DTO
- Service responsibilities:
  - enforce business rules
  - call repositories
  - coordinate infrastructure

## Error Handling

- Use a `@ControllerAdvice` global handler.
- Convert exceptions to a stable `ErrorDto` with:
  - timestamp
  - status
  - path
  - message
  - optional validation field errors

## Long Running Work (Jobs)

If you have background work:
- model jobs as a domain entity (status, progress, timestamps)
- provide endpoints for start/cancel/status
- optionally provide WebSocket/SSE for progress updates

