CREATE TABLE availability_blocks (
    id BIGSERIAL PRIMARY KEY,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    reason VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_block_dates CHECK (end_date >= start_date)
);
CREATE INDEX idx_blocks_dates ON availability_blocks(start_date, end_date);