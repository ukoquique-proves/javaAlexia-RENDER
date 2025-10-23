-- Migration script for Step 12: Create Suppliers Table

CREATE TABLE IF NOT EXISTS suppliers (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    category VARCHAR(100),
    products JSONB,
    delivery_time_days INTEGER,
    phone VARCHAR(50),
    email VARCHAR(255),
    website VARCHAR(500),
    location GEOGRAPHY(POINT, 4326),
    rating DECIMAL(3,2),
    is_verified BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

COMMENT ON TABLE suppliers IS 'Stores information about product suppliers.';

INSERT INTO migration_log (version, description, applied_at) 
VALUES ('12.0', 'Create suppliers table', NOW());
