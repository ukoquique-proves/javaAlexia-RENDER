-- Migration script for creating the migration_log table

CREATE TABLE IF NOT EXISTS migration_log (
    id SERIAL PRIMARY KEY,
    version VARCHAR(255) NOT NULL,
    description TEXT,
    applied_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE migration_log IS 'Tracks the execution of database migration scripts.';
