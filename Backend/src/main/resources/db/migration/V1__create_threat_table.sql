-- Core entity: normalized threat indicators (IOCs) for ingestion, correlation, and export.
-- Target: MySQL 8.0+ / MariaDB 10.5+ (InnoDB, utf8mb4).

CREATE TABLE threat_indicator (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    indicator_type VARCHAR(32) NOT NULL COMMENT 'e.g. IPV4, IPV6, DOMAIN, URL, EMAIL, FILE_HASH_SHA256',
    indicator_value VARCHAR(2048) NOT NULL COMMENT 'Raw IOC value as observed from the source',
    confidence TINYINT UNSIGNED NULL COMMENT '0-100 when provided by the feed',
    severity VARCHAR(16) NULL COMMENT 'e.g. LOW, MEDIUM, HIGH, CRITICAL',
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE' COMMENT 'e.g. ACTIVE, EXPIRED, FALSE_POSITIVE',
    source_name VARCHAR(256) NULL COMMENT 'Human-readable feed or publisher name',
    source_reference VARCHAR(512) NULL COMMENT 'URL or opaque reference to the originating report',
    first_seen_at DATETIME(3) NULL,
    last_seen_at DATETIME(3) NULL,
    description TEXT NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    CONSTRAINT chk_threat_indicator_confidence CHECK (confidence IS NULL OR confidence <= 100)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Deduplicate by type + value (prefix length required for utf8mb4 unique index on long VARCHAR).
CREATE UNIQUE INDEX uk_threat_indicator_type_value
    ON threat_indicator (indicator_type, indicator_value(768));

CREATE INDEX idx_threat_indicator_last_seen
    ON threat_indicator (last_seen_at);

CREATE INDEX idx_threat_indicator_status_last_seen
    ON threat_indicator (status, last_seen_at);

CREATE INDEX idx_threat_indicator_type_status
    ON threat_indicator (indicator_type, status);

CREATE INDEX idx_threat_indicator_source_name
    ON threat_indicator (source_name(191));
