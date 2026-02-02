# Testing

## Backend

- Unit/integration tests:
  - `cd api && mvn test`
- Generate Javadoc:
  - `cd api && mvn javadoc:javadoc` (outputs to `api/target/site/apidocs/`)
- Build without tests:
  - `make build-backend`

## Frontend

- Lint:
  - `cd ui && npm run lint`
- Type-check:
  - `cd ui && npm run type-check`

## Combined Flows

- Run UI + API:
  - `make run`
- Build both:
  - `make build`

## End-to-End (Optional)

If you add Playwright or scripted HTTP tests, keep them under `e2e/` with runnable shell scripts.
