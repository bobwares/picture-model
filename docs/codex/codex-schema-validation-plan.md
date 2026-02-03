Creator: Codex

# Schema Validation Plan

Date: January 27, 2026

## Goal
Add automated validation for JSON Schemas and OpenAPI spec.

## Proposed Files (you will create)
- `scripts/validate-schemas.py`
- `.github/workflows/schema-validate.yml`

## Script Outline (validate-schemas.py)
- Validate all `*.schema.json` files under `codex/schema/` with AJV (draft 2020-12).
- Validate `codex/schema/openapi.yaml` with Spectral (OpenAPI 3.1) or `openapi-cli`.
- Exit non‑zero on any validation error.

## CI Workflow Outline (schema-validate.yml)
- Trigger: on `push` and `pull_request`.
- Setup Node (for AJV/Spectral) and Python (if needed).
- Install dependencies:
  - `npm i -D ajv ajv-formats @stoplight/spectral-cli`
- Run:
  - `python scripts/validate-schemas.py`
  - `npx spectral lint codex/schema/openapi.yaml`

## Notes
- Keep validation paths aligned with `codex/schema`.
- If you prefer a pure‑Node script, replace Python with a `node` validator.
