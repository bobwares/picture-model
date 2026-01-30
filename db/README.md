Creator: codex

# Database Scripts

## Domain Migration

The Picture Model schema migration lives in:
- `db/migrations/01_picture_model_tables.sql`

Run it locally with Docker Compose:

```bash
make db-up
make db-migrate
```

To load sample data and smoke-test:

```bash
make db-seed
make db-query
```
