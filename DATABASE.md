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

Tables are automatically created by Hibernate on first run. The schema includes:

### Core Tables

1. **remote_file_drives** - Storage locations (LOCAL, SMB, SFTP, FTP)
   - Indexes: status, name

2. **images** - Indexed image files
   - Indexes: (drive_id, file_path) unique, file_hash, file_name, indexed_date, deleted

3. **image_metadata** - Key-value metadata for images
   - Indexes: image_id, metadata_key

4. **tags** - Organizational tags
   - Indexes: name unique, usage_count

5. **image_tags** - Many-to-many join table
   - Indexes: image_id, tag_id

6. **crawl_jobs** - File system crawl tracking
   - Indexes: drive_id, status, start_time

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
