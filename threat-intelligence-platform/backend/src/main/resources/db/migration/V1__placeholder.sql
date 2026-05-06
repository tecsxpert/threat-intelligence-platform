-- V1 — Create the main threat_intelligence table
-- This runs automatically when the app starts for the first time
-- Never edit this file after it has run — create V2 for changes

CREATE TABLE IF NOT EXISTS threat_intelligence (
    -- Primary key: auto-increments 1, 2, 3, 4...
    id                  BIGSERIAL PRIMARY KEY,

    -- Core fields
    title               VARCHAR(200)    NOT NULL,
    description         TEXT            NOT NULL,
    severity            VARCHAR(20)     NOT NULL,  -- LOW, MEDIUM, HIGH, CRITICAL
    status              VARCHAR(20)     NOT NULL,  -- OPEN, IN_PROGRESS, RESOLVED, CLOSED
    source              VARCHAR(500),
    affected_systems    TEXT,

    -- AI-generated fields (filled by Flask service, not by user)
    ai_description      TEXT,
    ai_recommendations  TEXT,
    ai_report           TEXT,

    -- Soft delete flag — true means hidden, not actually removed
    deleted             BOOLEAN         NOT NULL DEFAULT FALSE,

    -- Timestamps — set automatically by Spring
    created_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP
);

-- Indexes make queries fast
-- These are the columns you search and filter by most often
CREATE INDEX IF NOT EXISTS idx_threat_severity
    ON threat_intelligence(severity);

CREATE INDEX IF NOT EXISTS idx_threat_status
    ON threat_intelligence(status);

CREATE INDEX IF NOT EXISTS idx_threat_deleted
    ON threat_intelligence(deleted);

CREATE INDEX IF NOT EXISTS idx_threat_created_at
    ON threat_intelligence(created_at DESC);