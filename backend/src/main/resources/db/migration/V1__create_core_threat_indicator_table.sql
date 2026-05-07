-- Core threat intelligence table: normalized indicators of compromise (IOCs) / observables.
-- Targets MySQL 8.0+ / MariaDB 10.4+ (JSON, CHECK, generated STORED columns).

CREATE TABLE threat_indicator (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    external_id CHAR(36) NULL COMMENT 'UUID or STIX-style identifier',
    indicator_type VARCHAR(32) NOT NULL COMMENT 'e.g. ipv4, ipv6, domain, url, email, file_hash_md5, file_hash_sha256',
    value VARCHAR(2048) NOT NULL COMMENT 'Normalized observable',
    value_sha256 BINARY(32) AS (UNHEX(SHA2(CONCAT(indicator_type, CHAR(1), value), 256))) STORED NOT NULL COMMENT 'Dedup key for type+value',
    confidence TINYINT UNSIGNED NULL COMMENT '0-100',
    severity VARCHAR(16) NOT NULL DEFAULT 'medium',
    status VARCHAR(24) NOT NULL DEFAULT 'active',
    source VARCHAR(255) NOT NULL COMMENT 'Feed, vendor, or internal source name',
    source_reference VARCHAR(512) NULL,
    title VARCHAR(512) NULL,
    description TEXT NULL,
    first_observed DATETIME(3) NULL,
    last_observed DATETIME(3) NULL,
    valid_from DATETIME(3) NULL,
    valid_until DATETIME(3) NULL,
    tags JSON NULL,
    raw_metadata JSON NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    CONSTRAINT uk_threat_indicator_value_sha256 UNIQUE (value_sha256),
    CONSTRAINT chk_threat_indicator_severity CHECK (severity IN ('low', 'medium', 'high', 'critical')),
    CONSTRAINT chk_threat_indicator_status CHECK (status IN ('active', 'inactive', 'expired', 'false_positive')),
    CONSTRAINT chk_threat_indicator_confidence CHECK (confidence IS NULL OR (confidence >= 0 AND confidence <= 100))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_threat_indicator_type_last_observed
    ON threat_indicator (indicator_type, last_observed);

CREATE INDEX idx_threat_indicator_status_last_observed
    ON threat_indicator (status, last_observed);

CREATE INDEX idx_threat_indicator_source_created
    ON threat_indicator (source, created_at);

CREATE INDEX idx_threat_indicator_severity_last_observed
    ON threat_indicator (severity, last_observed);

CREATE INDEX idx_threat_indicator_external_id
    ON threat_indicator (external_id);
