# Definition of Done (DoD)

An increment is "done" when:

- It builds and runs locally.
- One end-to-end flow works in the browser.
- API endpoints have stable DTOs and consistent error handling.
- List endpoints are paginated (no unbounded responses).
- Basic docs exist for:
  - how to run
  - env vars
  - contract notes (OpenAPI path, file streaming path)
- At least one verification step is provided:
  - tests, or
  - manual steps that are reproducible

