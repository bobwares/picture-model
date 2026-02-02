# Repo Layout (v2)

Target layout for new apps:

```
project/
  api/                     # Spring Boot backend
  ui/                      # Next.js frontend
  db/                      # SQL migrations / seeds (optional)
  docs/                    # docs, including AIP link in context
  e2e/                     # end-to-end checks (optional)
  Makefile                 # common dev commands
  docker-compose.yml       # optional local infra
```

## Backend Layering Rules

- `*.api.*` HTTP boundary only (controllers, DTOs, exception mapping).
- `*.service.*` business logic and orchestration.
- `*.domain.*` persistence model and domain primitives.
- `*.infrastructure.*` IO boundaries (filesystem, external services).
- `*.config.*` Spring configuration.

## Frontend Layering Rules

- `ui/app/*` route composition (pages, layouts).
- `ui/components/*` reusable UI blocks.
- `ui/lib/*` API client + utilities.
- `ui/types/*` stable TypeScript shapes for API DTOs and view models.

