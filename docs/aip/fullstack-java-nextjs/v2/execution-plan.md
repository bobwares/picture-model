# Execution Plan Template (Agent-Oriented)

Keep each task small and reviewable. Prefer vertical slices.

## Task 1: Bootstrap

- Create `api/` Spring Boot project with starters:
  - web, validation, data-jpa, actuator, springdoc
- Create `ui/` Next.js project (TS + Tailwind).
- Add root `Makefile`:
  - `run`, `build`, `test`, `db-up`, `db-down`
- Add basic docs:
  - `docs/` + link this AIP from context

## Task 2: First Vertical Slice (End-to-End)

- Pick 1 core entity and implement:
  - JPA entity + repository
  - service method(s)
  - controller + DTOs
  - UI page that lists/creates/edits

## Task 3: Operational Baseline

- Actuator health + info endpoints
- OpenAPI available
- CORS configured for UI origin

## Task 4: Expand Feature Set

- Add remaining entities and endpoints iteratively.
- Add pagination and filtering for list endpoints early.

## Task 5: Tests

- API: unit tests for services + one integration test path.
- UI: minimal component test or smoke test.

