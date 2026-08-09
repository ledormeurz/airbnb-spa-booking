CREATE TABLE calendar_feeds (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    url VARCHAR(1000) NOT NULL,
    source VARCHAR(50) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    last_synced_at TIMESTAMP,
    last_sync_status VARCHAR(20),
    last_sync_message VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_calendar_feeds_url UNIQUE (url)
);

CREATE INDEX idx_calendar_feeds_enabled ON calendar_feeds(enabled);
CREATE INDEX idx_calendar_feeds_source ON calendar_feeds(source);
