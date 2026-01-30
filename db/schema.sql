-- Generated from JPA entities in api/src/main/java/com/picturemodel/domain/entity
-- Dialect: PostgreSQL
-- Note: unquoted camelCase column names fold to lowercase in PostgreSQL.

CREATE TABLE remote_file_drives (
    id uuid PRIMARY KEY,
    name varchar(200) NOT NULL,
    type varchar(20) NOT NULL,
    connectionurl varchar(1000) NOT NULL,
    encryptedcredentials text,
    status varchar(20) NOT NULL,
    rootpath varchar(1000) NOT NULL,
    autoconnect boolean NOT NULL,
    autocrawl boolean NOT NULL,
    lastconnected timestamp,
    lastcrawled timestamp,
    imagecount integer NOT NULL,
    createddate timestamp NOT NULL,
    modifieddate timestamp NOT NULL
);

CREATE TABLE images (
    id uuid PRIMARY KEY,
    drive_id uuid NOT NULL,
    filename varchar(500) NOT NULL,
    filepath varchar(2000) NOT NULL,
    filesize bigint NOT NULL,
    filehash varchar(64) NOT NULL,
    mimetype varchar(100) NOT NULL,
    width integer,
    height integer,
    thumbnailpath varchar(500),
    deleted boolean NOT NULL,
    capturedat timestamp,
    createddate timestamp NOT NULL,
    modifieddate timestamp NOT NULL,
    indexeddate timestamp NOT NULL,
    CONSTRAINT fk_images_drive FOREIGN KEY (drive_id) REFERENCES remote_file_drives(id) ON DELETE RESTRICT
);

CREATE TABLE image_metadata (
    id uuid PRIMARY KEY,
    image_id uuid NOT NULL,
    metadatakey varchar(255) NOT NULL,
    value_entry text,
    source varchar(20) NOT NULL,
    lastmodified timestamp NOT NULL,
    CONSTRAINT fk_image_metadata_image FOREIGN KEY (image_id) REFERENCES images(id) ON DELETE CASCADE
);

CREATE TABLE tags (
    id uuid PRIMARY KEY,
    name varchar(100) NOT NULL UNIQUE,
    color varchar(7),
    usagecount integer NOT NULL,
    createddate timestamp NOT NULL
);

CREATE TABLE crawl_jobs (
    id uuid PRIMARY KEY,
    drive_id uuid NOT NULL,
    rootpath varchar(2000) NOT NULL,
    status varchar(20) NOT NULL,
    starttime timestamp NOT NULL,
    endtime timestamp,
    filesprocessed integer NOT NULL,
    filesadded integer NOT NULL,
    filesupdated integer NOT NULL,
    filesdeleted integer NOT NULL,
    currentpathvalue varchar(2000),
    isincremental boolean NOT NULL,
    errors text,
    CONSTRAINT fk_crawl_jobs_drive FOREIGN KEY (drive_id) REFERENCES remote_file_drives(id) ON DELETE RESTRICT
);

CREATE TABLE image_tags (
    image_id uuid NOT NULL,
    tag_id uuid NOT NULL,
    PRIMARY KEY (image_id, tag_id),
    CONSTRAINT fk_image_tags_image FOREIGN KEY (image_id) REFERENCES images(id) ON DELETE CASCADE,
    CONSTRAINT fk_image_tags_tag FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE CASCADE
);

-- Indexes from @Table(indexes=...)
CREATE UNIQUE INDEX idx_image_drive_path ON images (drive_id, filepath);
CREATE INDEX idx_image_file_hash ON images (filehash);
CREATE INDEX idx_image_file_name ON images (filename);
CREATE INDEX idx_image_indexed_date ON images (indexeddate);
CREATE INDEX idx_image_deleted ON images (deleted);

CREATE INDEX idx_metadata_image_id ON image_metadata (image_id);
CREATE INDEX idx_metadata_key ON image_metadata (metadatakey);

CREATE UNIQUE INDEX idx_tag_name ON tags (name);
CREATE INDEX idx_tag_usage_count ON tags (usagecount);

CREATE INDEX idx_crawl_drive_id ON crawl_jobs (drive_id);
CREATE INDEX idx_crawl_status ON crawl_jobs (status);
CREATE INDEX idx_crawl_start_time ON crawl_jobs (starttime);

CREATE INDEX idx_image_tags_image ON image_tags (image_id);
CREATE INDEX idx_image_tags_tag ON image_tags (tag_id);
