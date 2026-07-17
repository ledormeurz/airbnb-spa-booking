CREATE TABLE price_rules (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    booking_type VARCHAR(30) NOT NULL,
    day_type VARCHAR(20) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE
);
CREATE INDEX idx_price_rules_type ON price_rules(booking_type, day_type);