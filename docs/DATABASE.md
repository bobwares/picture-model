# Picture Model Database Setup

## Overview

The Picture Model application uses:
- **PostgreSQL 16** for production
- **H2** for development/testing (embedded, file-based)

JPA/Hibernate automatically creates tables based on entity definitions.

## Quick Start - Development Mode (H2)

The easiest way to get started is using the embedded H2 database:

```bash
cd api
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

**H2 Console Access:**
- URL: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:file:./data/picturemodel`
- Username: `sa`
- Password: _(leave empty)_

Database file location: `./data/picturemodel.mv.db`

## Production Mode - PostgreSQL with Docker

### Option 1: Docker Compose (Recommended)

Start PostgreSQL:
```bash
# From project root
docker-compose up -d postgres

# Check status
docker-compose ps

# View logs
docker-compose logs -f postgres
```

Stop PostgreSQL:
```bash
docker-compose down

# To remove data volumes as well:
docker-compose down -v
```

**Connection Details:**
- Host: `localhost`
- Port: `5432`
- Database: `picturemodel`
- Username: `postgres`
- Password: `postgres`

### Option 2: Local PostgreSQL Installation

If you have PostgreSQL installed locally:

```bash
# Create database
createdb -U postgres picturemodel

# Or using psql:
psql -U postgres
CREATE DATABASE picturemodel;
\q
```

### Run Application with PostgreSQL

```bash
cd api

# Using default credentials from application.yml
mvn spring-boot:run

# Or with custom credentials
mvn spring-boot:run \
  -Dspring-boot.run.arguments="--DB_USER=myuser --DB_PASSWORD=mypass"
```

## Database Schema

Tables are automatically created by Hibernate on first run. The canonical DDL is in
`db/schema.sql`. The sections below detail every table, column, constraint, and index.

---

### Entity Relationships

```
remote_file_drives
    ├── 1:N  images          (images.drive_id → remote_file_drives.id)
    └── 1:N  crawl_jobs      (crawl_jobs.drive_id → remote_file_drives.id)

images
    ├── 1:N  image_metadata  (image_metadata.image_id → images.id)
    └── N:M  tags            (via image_tags join table)
```

- Deleting a drive is **blocked** if it has images or crawl jobs (ON DELETE RESTRICT).
- Deleting an image **cascades** to its metadata rows and its `image_tags` rows.
- Deleting a tag **cascades** its `image_tags` rows.

---

### remote_file_drives

Represents a configured storage location. The `connectionUrl` identifies the server or
path; `rootPath` scopes the crawl and file reads to a subdirectory within that location.
Credentials for network drives are stored encrypted in `encryptedCredentials` (JSON,
encrypted at rest via Jasypt).

| Column                 | Type        | Nullable | Default        | Notes |
|------------------------|-------------|----------|----------------|-------|
| id                     | uuid        | NO       | (generated)    | Primary key |
| name                   | varchar(200)| NO       |                | Display name |
| type                   | varchar(20) | NO       |                | Enum: `LOCAL`, `SMB`, `SFTP`, `FTP` |
| connectionurl          | varchar(1000)| NO      |                | Server address or local path |
| encryptedcredentials   | text        | YES      |                | Encrypted JSON: username, password, port |
| status                 | varchar(20) | NO       | `DISCONNECTED` | Enum: `DISCONNECTED`, `CONNECTING`, `CONNECTED`, `ERROR` |
| rootpath               | varchar(1000)| NO      |                | Starting directory for crawl and file reads |
| autoconnect            | boolean     | NO       | `false`        | Connect on application startup |
| autocrawl              | boolean     | NO       | `false`        | Start crawl immediately after connect |
| lastconnected          | timestamp   | YES      |                | Last successful connection time |
| lastcrawled            | timestamp   | YES      |                | Last completed crawl end time |
| imagecount             | integer     | NO       | `0`            | Cached count of non-deleted images |
| createddate            | timestamp   | NO       |                | Set on insert |
| modifieddate           | timestamp   | NO       |                | Updated on every save |

**Indexes:**

| Index name        | Columns | Unique |
|-------------------|---------|--------|
| idx_drive_status  | status  | No     |
| idx_drive_name    | name    | No     |

---

### images

One row per image file discovered by the crawler. `filePath` is relative to the drive's
`rootPath`. The `deleted` flag is a soft delete — the row is retained so that incremental
crawls can detect re-added files.

| Column         | Type          | Nullable | Notes |
|----------------|---------------|----------|-------|
| id             | uuid          | NO       | Primary key |
| drive_id       | uuid          | NO       | FK → remote_file_drives.id |
| filename       | varchar(500)  | NO       | Base file name |
| filepath       | varchar(2000) | NO       | Relative path from drive rootPath |
| filesize       | bigint        | NO       | Size in bytes |
| filehash       | varchar(64)   | NO       | SHA-256 hex digest |
| mimetype       | varchar(100)  | NO       | e.g. `image/jpeg` |
| width          | integer       | YES      | Pixel width (not yet populated) |
| height         | integer       | YES      | Pixel height (not yet populated) |
| thumbnailpath  | varchar(500)  | YES      | Reserved; thumbnails are generated on the fly |
| deleted        | boolean       | NO       | Soft delete flag |
| capturedat     | timestamp     | YES      | EXIF capture time (not yet populated) |
| createddate    | timestamp     | NO       | File creation/modification time from the drive |
| modifieddate   | timestamp     | NO       | File modification time from the drive |
| indexeddate    | timestamp     | NO       | When the crawler wrote this row |

**Indexes:**

| Index name                | Columns              | Unique |
|---------------------------|----------------------|--------|
| idx_image_drive_path      | drive_id, filepath   | Yes    |
| idx_image_file_hash       | filehash             | No     |
| idx_image_file_name       | filename             | No     |
| idx_image_indexed_date    | indexeddate          | No     |
| idx_image_deleted         | deleted              | No     |

---

### image_metadata

Flexible key-value store attached to an image. Keys are normalized to lowercase on
save. The `source` column tracks where the value came from so user edits and
auto-extracted values can coexist under the same key.

| Column        | Type         | Nullable | Notes |
|---------------|--------------|----------|-------|
| id            | uuid         | NO       | Primary key |
| image_id      | uuid         | NO       | FK → images.id (CASCADE on delete) |
| metadatakey   | varchar(255) | NO       | Normalized to lowercase |
| value_entry   | text         | YES      | The metadata value |
| source        | varchar(20)  | NO       | Enum: `EXIF`, `USER_ENTERED`, `AUTO_GENERATED` |
| lastmodified  | timestamp    | NO       | Updated on every save |

**Indexes:**

| Index name            | Columns       | Unique |
|-----------------------|---------------|--------|
| idx_metadata_image_id | image_id      | No     |
| idx_metadata_key      | metadatakey   | No     |

---

### tags

User-defined labels for organizing images. `usageCount` is maintained by the
application when tags are added to or removed from images.

| Column      | Type         | Nullable | Default | Notes |
|-------------|--------------|----------|---------|-------|
| id          | uuid         | NO       |         | Primary key |
| name        | varchar(100) | NO       |         | Unique across all tags |
| color       | varchar(7)   | YES      |         | Hex color code, e.g. `#FF5733` |
| usagecount  | integer      | NO       | `0`     | Number of images currently tagged |
| createddate | timestamp    | NO       |         | Set on insert |

**Indexes:**

| Index name            | Columns    | Unique |
|-----------------------|------------|--------|
| idx_tag_name          | name       | Yes    |
| idx_tag_usage_count   | usagecount | No     |

---

### image_tags

Join table for the many-to-many relationship between images and tags. Both foreign
keys cascade on delete.

| Column   | Type | Nullable | Notes |
|----------|------|----------|-------|
| image_id | uuid | NO       | FK → images.id (CASCADE on delete) |
| tag_id   | uuid | NO       | FK → tags.id (CASCADE on delete) |

**Primary key:** composite (image_id, tag_id)

**Indexes:**

| Index name              | Columns  | Unique |
|-------------------------|----------|--------|
| idx_image_tags_image    | image_id | No     |
| idx_image_tags_tag      | tag_id   | No     |

---

### crawl_jobs

Tracks each crawl operation against a drive. The `errors` column stores a JSON array
of error message strings collected during the run. An incremental crawl skips files
whose `lastModified` is on or before the drive's `lastCrawled` timestamp.

| Column             | Type          | Nullable | Default        | Notes |
|--------------------|---------------|----------|----------------|-------|
| id                 | uuid          | NO       | (generated)    | Primary key |
| drive_id           | uuid          | NO       |                | FK → remote_file_drives.id |
| rootpath           | varchar(2000) | NO       |                | Path the crawl started from |
| status             | varchar(20)   | NO       | `PENDING`      | Enum: `PENDING`, `IN_PROGRESS`, `PAUSED`, `COMPLETED`, `FAILED`, `CANCELLED` |
| starttime          | timestamp     | NO       |                | Set on insert if not provided |
| endtime            | timestamp     | YES      |                | Set on completion, failure, or cancellation |
| filesprocessed     | integer       | NO       | `0`            | Total files examined |
| filesadded         | integer       | NO       | `0`            | New image rows created |
| filesupdated       | integer       | NO       | `0`            | Existing rows updated (size/hash changed) |
| filesdeleted       | integer       | NO       | `0`            | Rows soft-deleted (not found on drive) |
| currentpathvalue   | varchar(2000) | YES      |                | Last directory visited; updated during the run |
| isincremental      | boolean       | NO       | `false`        | If true, only processes files newer than lastCrawled |
| errors             | text          | YES      |                | JSON array of error strings |

**Indexes:**

| Index name              | Columns   | Unique |
|-------------------------|-----------|--------|
| idx_crawl_drive_id      | drive_id  | No     |
| idx_crawl_status        | status    | No     |
| idx_crawl_start_time    | starttime | No     |

## Viewing the Database

### Option 1: H2 Console (Dev Mode Only)
Access at http://localhost:8080/h2-console when running in dev profile.

### Option 2: PgAdmin (PostgreSQL)

Start PgAdmin with Docker Compose:
```bash
docker-compose --profile tools up -d pgadmin
```

Access PgAdmin:
- URL: http://localhost:5050
- Email: `admin@picturemodel.com`
- Password: `admin`

Add Server in PgAdmin:
- Name: `Picture Model`
- Host: `postgres` (Docker network) or `host.docker.internal` (Mac/Windows)
- Port: `5432`
- Database: `picturemodel`
- Username: `postgres`
- Password: `postgres`

### Option 3: Command Line (psql)

```bash
# Via Docker
docker exec -it picturemodel-postgres psql -U postgres -d picturemodel

# Local installation
psql -U postgres -d picturemodel

# Common queries:
\dt                          # List tables
\d+ remote_file_drives       # Describe table
SELECT * FROM remote_file_drives;
SELECT count(*) FROM images;
```

## Database Migrations

For production deployments, consider using Flyway or Liquibase for schema versioning.

### Enable Flyway (Optional)

Add to `pom.xml`:
```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
```

Create migration scripts in `src/main/resources/db/migration/`:
- `V1__create_initial_schema.sql`
- `V2__add_indexes.sql`
- etc.

Update `application.yml`:
```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate  # Change from 'update' to 'validate'
  flyway:
    enabled: true
```

## Backup and Restore

### PostgreSQL Backup
```bash
# Backup
docker exec picturemodel-postgres pg_dump -U postgres picturemodel > backup.sql

# Restore
docker exec -i picturemodel-postgres psql -U postgres -d picturemodel < backup.sql
```

### H2 Backup
Simply copy the database file:
```bash
cp ./data/picturemodel.mv.db ./data/picturemodel.backup.mv.db
```

## Troubleshooting

### Connection Refused
- Ensure PostgreSQL is running: `docker-compose ps`
- Check port 5432 is available: `lsof -i :5432`
- Verify credentials in `application.yml`

### Tables Not Created
- Check application logs for Hibernate DDL errors
- Ensure `spring.jpa.hibernate.ddl-auto` is set to `update` or `create`
- Verify database user has CREATE TABLE privileges

### Permission Denied
```bash
# Grant all privileges
docker exec -it picturemodel-postgres psql -U postgres -d picturemodel
GRANT ALL PRIVILEGES ON DATABASE picturemodel TO postgres;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO postgres;
```

### Reset Database
```bash
# Docker (removes all data)
docker-compose down -v
docker-compose up -d postgres

# H2 (delete file)
rm -rf ./data/picturemodel.mv.db
```

## Environment Variables

Configure database connection via environment variables:

```bash
export DB_USER=postgres
export DB_PASSWORD=postgres
export ENCRYPTION_KEY=my-secure-encryption-key

mvn spring-boot:run
```

Or create `.env` file (not committed to git):
```bash
DB_USER=postgres
DB_PASSWORD=postgres
ENCRYPTION_KEY=your-secure-key-here
```

## Performance Tuning

For large image collections, consider:

1. **Connection Pool Sizing** (application.yml):
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
```

2. **PostgreSQL Configuration** (docker-compose.yml):
```yaml
command:
  - "postgres"
  - "-c"
  - "shared_buffers=256MB"
  - "-c"
  - "effective_cache_size=1GB"
```

3. **Indexes**: Already defined in entity annotations, automatically created.

## Data Directory Structure

```
picture-model/
├── data/                      # Created at runtime
│   ├── picturemodel.mv.db     # H2 database (dev mode)
│   └── thumbnails/            # Cached thumbnails
├── db/
│   └── init/
│       └── 01-init.sql        # PostgreSQL initialization
└── docker-compose.yml
```
