-- Add external calendar identity for iCal imports (Airbnb, Booking.com, etc.)
ALTER TABLE availability_blocks ADD COLUMN external_uid VARCHAR(255);
ALTER TABLE availability_blocks ADD COLUMN source VARCHAR(50);

CREATE UNIQUE INDEX idx_blocks_external_uid ON availability_blocks(external_uid);
CREATE INDEX idx_blocks_source ON availability_blocks(source);
