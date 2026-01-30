/*
 * App: Picture Model
 * Package: db
 * File: 01_picture_model_tables.sql
 * Version: 0.1.0
 * Turns: 5
 * Author: codex
 * Date: 2026-01-30T00:39:15Z
 * Exports: tables, types, indexes, view
 * Description: Creates normalized PostgreSQL tables, enums, indexes, and views for the Picture Model domain.
 */

BEGIN;

CREATE EXTENSION IF NOT EXISTS pgcrypto;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'drive_type_enum') THEN
        CREATE TYPE drive_type_enum AS ENUM ('LOCAL', 'SMB', 'SFTP', 'FTP');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'connection_status_enum') THEN
        CREATE TYPE connection_status_enum AS ENUM ('DISCONNECTED', 'CONNECTING', 'CONNECTED', 'ERROR');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'crawl_status_enum') THEN
        CREATE TYPE crawl_status_enum AS ENUM ('PENDING', 'IN_PROGRESS', 'PAUSED', 'COMPLETED', 'FAILED', 'CANCELLED');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'metadata_source_enum') THEN
        CREATE TYPE metadata_source_enum AS ENUM ('EXIF', 'USER_ENTERED', 'AUTO_GENERATED');
    END IF;
END $$;

CREATE TABLE IF NOT EXISTS remote_file_drives (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(200) NOT NULL,
    type drive_type_enum NOT NULL,
    connection_url VARCHAR(1000) NOT NULL,
    encrypted_credentials TEXT,
    status connection_status_enum NOT NULL DEFAULT 'DISCONNECTED',
    root_path VARCHAR(1000) NOT NULL,
    auto_connect BOOLEAN NOT NULL DEFAULT FALSE,
    auto_crawl BOOLEAN NOT NULL DEFAULT FALSE,
    last_connected TIMESTAMP,
    last_crawled TIMESTAMP,
    image_count INTEGER NOT NULL DEFAULT 0,
    created_date TIMESTAMP NOT NULL DEFAULT NOW(),
    modified_date TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_drive_status ON remote_file_drives (status);
CREATE INDEX IF NOT EXISTS idx_drive_name ON remote_file_drives (name);

CREATE TABLE IF NOT EXISTS images (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    drive_id UUID NOT NULL REFERENCES remote_file_drives(id),
    file_name VARCHAR(500) NOT NULL,
    file_path VARCHAR(2000) NOT NULL,
    file_size BIGINT NOT NULL,
    file_hash VARCHAR(64) NOT NULL,
    mime_type VARCHAR(100) NOT NULL,
    width INTEGER,
    height INTEGER,
    thumbnail_path VARCHAR(500),
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    captured_at TIMESTAMP,
    created_date TIMESTAMP NOT NULL,
    modified_date TIMESTAMP NOT NULL,
    indexed_date TIMESTAMP NOT NULL,
    CONSTRAINT uq_images_drive_path UNIQUE (drive_id, file_path)
);

CREATE INDEX IF NOT EXISTS idx_image_file_hash ON images (file_hash);
CREATE INDEX IF NOT EXISTS idx_image_file_name ON images (file_name);
CREATE INDEX IF NOT EXISTS idx_image_indexed_date ON images (indexed_date);
CREATE INDEX IF NOT EXISTS idx_image_deleted ON images (deleted);
CREATE INDEX IF NOT EXISTS idx_image_drive_id ON images (drive_id);

CREATE TABLE IF NOT EXISTS image_metadata (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    image_id UUID NOT NULL REFERENCES images(id) ON DELETE CASCADE,
    metadata_key VARCHAR(255) NOT NULL,
    value_entry TEXT,
    source metadata_source_enum NOT NULL,
    last_modified TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_metadata_image_id ON image_metadata (image_id);
CREATE INDEX IF NOT EXISTS idx_metadata_key ON image_metadata (metadata_key);

CREATE TABLE IF NOT EXISTS tags (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    color VARCHAR(7),
    usage_count INTEGER NOT NULL DEFAULT 0,
    created_date TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_tags_name UNIQUE (name)
);

CREATE INDEX IF NOT EXISTS idx_tag_usage_count ON tags (usage_count);

CREATE TABLE IF NOT EXISTS image_tags (
    image_id UUID NOT NULL REFERENCES images(id) ON DELETE CASCADE,
    tag_id UUID NOT NULL REFERENCES tags(id) ON DELETE CASCADE,
    PRIMARY KEY (image_id, tag_id)
);

CREATE INDEX IF NOT EXISTS idx_image_tags_image ON image_tags (image_id);
CREATE INDEX IF NOT EXISTS idx_image_tags_tag ON image_tags (tag_id);

CREATE TABLE IF NOT EXISTS crawl_jobs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    drive_id UUID NOT NULL REFERENCES remote_file_drives(id) ON DELETE CASCADE,
    root_path VARCHAR(2000) NOT NULL,
    status crawl_status_enum NOT NULL DEFAULT 'PENDING',
    start_time TIMESTAMP NOT NULL DEFAULT NOW(),
    end_time TIMESTAMP,
    files_processed INTEGER NOT NULL DEFAULT 0,
    files_added INTEGER NOT NULL DEFAULT 0,
    files_updated INTEGER NOT NULL DEFAULT 0,
    files_deleted INTEGER NOT NULL DEFAULT 0,
    current_path_value VARCHAR(2000),
    is_incremental BOOLEAN NOT NULL DEFAULT FALSE,
    errors TEXT
);

CREATE INDEX IF NOT EXISTS idx_crawl_drive_id ON crawl_jobs (drive_id);
CREATE INDEX IF NOT EXISTS idx_crawl_status ON crawl_jobs (status);
CREATE INDEX IF NOT EXISTS idx_crawl_start_time ON crawl_jobs (start_time);

CREATE VIEW IF NOT EXISTS image_search_view AS
SELECT
    i.id AS image_id,
    i.file_name,
    i.file_path,
    i.file_size,
    i.file_hash,
    i.mime_type,
    i.width,
    i.height,
    i.thumbnail_path,
    i.deleted,
    i.captured_at,
    i.created_date,
    i.modified_date,
    i.indexed_date,
    d.id AS drive_id,
    d.name AS drive_name,
    d.type AS drive_type,
    array_remove(array_agg(DISTINCT t.name), NULL) AS tags,
    jsonb_object_agg(im.metadata_key, im.value_entry) FILTER (WHERE im.metadata_key IS NOT NULL) AS metadata
FROM images i
JOIN remote_file_drives d ON d.id = i.drive_id
LEFT JOIN image_tags it ON it.image_id = i.id
LEFT JOIN tags t ON t.id = it.tag_id
LEFT JOIN image_metadata im ON im.image_id = i.id
GROUP BY i.id, d.id;

COMMIT;
