-- Picture Model Database Initialization Script
-- This script is automatically executed when the PostgreSQL container starts for the first time

-- Create the database (already created by POSTGRES_DB env variable)
-- But we can add additional configuration here

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE picturemodel TO postgres;

-- Set default schema
\c picturemodel;

-- Note: Tables will be created automatically by Hibernate based on JPA entities
-- when the Spring Boot application starts with spring.jpa.hibernate.ddl-auto=update

-- This file can be used for:
-- 1. Creating additional extensions
-- 2. Setting up database-level configurations
-- 3. Creating indexes not defined in entities
-- 4. Inserting seed data

-- Example: Insert default tags (optional)
-- This will be executed after tables are created by Hibernate on first run
-- Uncomment if needed:
-- INSERT INTO tags (id, name, color, usage_count, created_date)
-- VALUES
--   (uuid_generate_v4(), 'Vacation', '#FF6B6B', 0, NOW()),
--   (uuid_generate_v4(), 'Family', '#4ECDC4', 0, NOW()),
--   (uuid_generate_v4(), 'Work', '#45B7D1', 0, NOW()),
--   (uuid_generate_v4(), 'Nature', '#96CEB4', 0, NOW())
-- ON CONFLICT DO NOTHING;

COMMIT;
