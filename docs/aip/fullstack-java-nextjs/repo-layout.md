# Repo Layout

This pattern assumes a single repo with two first-class apps:

```
picture-model/
  api/                     # Spring Boot backend
    src/main/java/com/...  # Controllers, services, domain, infrastructure
    src/main/resources/    # application.yml, profiles
    pom.xml
  ui/                      # Next.js frontend
    app/                   # Next.js App Router pages
    components/            # Reusable UI components
    lib/                   # API client and utilities
    types/                 # Shared TS types for the UI
    package.json
  db/                      # SQL migrations / seed scripts (optional)
  docker/                  # Container helpers (optional)
  docs/                    # Documentation
  e2e/                     # E2E scripts and http/curl helpers (optional)
  ai/                      # Agentic pipeline artifacts (optional)
```

## Layering Rules (Backend)

- `com.picturemodel.api.*` is the HTTP boundary (controllers + DTOs + mapping + exception handling).
- `com.picturemodel.service` contains business logic and orchestration.
- `com.picturemodel.domain.*` contains entities/enums/repositories and should stay free of HTTP concerns.
- `com.picturemodel.infrastructure.*` contains IO boundaries (filesystem, security, external systems).
- `com.picturemodel.config` contains Spring configuration.

## Layering Rules (Frontend)

- `ui/app/*` contains route pages and layout composition.
- `ui/components/*` contains reusable presentational and interactive components.
- `ui/lib/*` contains API client wrappers, utilities, and cross-cutting helpers.
- `ui/types/*` contains shared UI-side types (DTO shapes, view models).

