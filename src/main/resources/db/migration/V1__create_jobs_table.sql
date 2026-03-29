-- V1__create_jobs_table.sql
-- Flyway runs this automatically on app startup
-- Creates the jobs table in PostgreSQL

CREATE TABLE jobs
(
    -- Identity
    id          UUID         NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,

    -- What to run
    job_type    VARCHAR(100) NOT NULL,
    payload     TEXT,

    -- State machine
    status      VARCHAR(20)  NOT NULL DEFAULT 'PENDING',

    -- Retry tracking
    attempts     INTEGER      NOT NULL DEFAULT 0,
    max_attempts INTEGER      NOT NULL DEFAULT 3,
    error        TEXT,

    -- Who submitted
    submitted_by VARCHAR(255),

    -- Timestamps
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    started_at  TIMESTAMP,
    finished_at TIMESTAMP
);

-- Indexes for frequent queries
-- Worker constantly queries WHERE status = 'PENDING'
CREATE INDEX ix_jobs_status ON jobs (status);

-- Useful for pagination and ordering
CREATE INDEX ix_jobs_created_at ON jobs (created_at);