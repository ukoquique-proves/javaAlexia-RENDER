-- Step 9: Business Management with Geolocation
-- Adds geolocation, business hours, and additional fields to businesses table

-- Enable PostGIS extension for geolocation support
CREATE EXTENSION IF NOT EXISTS postgis;

-- Add new columns to businesses table
ALTER TABLE businesses 
    ADD COLUMN IF NOT EXISTS location GEOGRAPHY(POINT, 4326),
    ADD COLUMN IF NOT EXISTS business_hours JSONB,
    ADD COLUMN IF NOT EXISTS google_place_id VARCHAR(255) UNIQUE,
    ADD COLUMN IF NOT EXISTS owner_user_id BIGINT,
    ADD COLUMN IF NOT EXISTS logo_url VARCHAR(500),
    ADD COLUMN IF NOT EXISTS whatsapp VARCHAR(50),
    ADD COLUMN IF NOT EXISTS instagram VARCHAR(100),
    ADD COLUMN IF NOT EXISTS rating DECIMAL(3,2) DEFAULT 0,
    ADD COLUMN IF NOT EXISTS is_verified BOOLEAN DEFAULT false;

-- Create spatial index for geolocation queries
CREATE INDEX IF NOT EXISTS idx_businesses_location ON businesses USING GIST(location);

-- Create index for Google Place ID lookups
CREATE INDEX IF NOT EXISTS idx_businesses_google_place_id ON businesses(google_place_id);

-- Create index for owner lookups (for future RBAC)
CREATE INDEX IF NOT EXISTS idx_businesses_owner_user_id ON businesses(owner_user_id);

-- Create index for verified businesses
CREATE INDEX IF NOT EXISTS idx_businesses_is_verified ON businesses(is_verified);

-- Add comments for documentation
COMMENT ON COLUMN businesses.location IS 'Geographic location (latitude, longitude) using PostGIS';
COMMENT ON COLUMN businesses.business_hours IS 'Operating hours in JSONB format: {"monday": [{"open": "09:00", "close": "18:00"}], ...}';
COMMENT ON COLUMN businesses.google_place_id IS 'Google Places API place_id for future import feature';
COMMENT ON COLUMN businesses.owner_user_id IS 'User ID of business owner (for future RBAC)';
COMMENT ON COLUMN businesses.logo_url IS 'URL to business logo image';
COMMENT ON COLUMN businesses.whatsapp IS 'WhatsApp number for direct contact';
COMMENT ON COLUMN businesses.instagram IS 'Instagram handle';
COMMENT ON COLUMN businesses.rating IS 'Average rating (0.00 to 5.00)';
COMMENT ON COLUMN businesses.is_verified IS 'Whether the business has been verified by admin';

-- Update existing businesses with sample data for testing
-- Bogotá coordinates: latitude 4.7110, longitude -74.0721

UPDATE businesses SET 
    location = ST_SetSRID(ST_MakePoint(-74.0721, 4.7110), 4326)::geography,
    business_hours = '{
        "monday": [{"open": "09:00", "close": "18:00"}],
        "tuesday": [{"open": "09:00", "close": "18:00"}],
        "wednesday": [{"open": "09:00", "close": "18:00"}],
        "thursday": [{"open": "09:00", "close": "18:00"}],
        "friday": [{"open": "09:00", "close": "18:00"}],
        "saturday": [{"open": "10:00", "close": "14:00"}],
        "sunday": []
    }'::jsonb,
    rating = 4.5,
    is_verified = true
WHERE id = 1;

-- Example: Business with different location (2km away)
UPDATE businesses SET 
    location = ST_SetSRID(ST_MakePoint(-74.0900, 4.7200), 4326)::geography,
    business_hours = '{
        "monday": [{"open": "08:00", "close": "20:00"}],
        "tuesday": [{"open": "08:00", "close": "20:00"}],
        "wednesday": [{"open": "08:00", "close": "20:00"}],
        "thursday": [{"open": "08:00", "close": "20:00"}],
        "friday": [{"open": "08:00", "close": "22:00"}],
        "saturday": [{"open": "08:00", "close": "22:00"}],
        "sunday": [{"open": "10:00", "close": "18:00"}]
    }'::jsonb,
    rating = 4.2,
    is_verified = true
WHERE id = 2;

-- Example query: Find businesses within 5km of a location
-- SELECT 
--     id, 
--     name, 
--     ST_Distance(location, ST_SetSRID(ST_MakePoint(-74.0721, 4.7110), 4326)::geography) / 1000 as distance_km
-- FROM businesses
-- WHERE ST_DWithin(location, ST_SetSRID(ST_MakePoint(-74.0721, 4.7110), 4326)::geography, 5000)
-- ORDER BY distance_km;

-- Example query: Check if business is open now
-- SELECT 
--     id, 
--     name,
--     business_hours->LOWER(TO_CHAR(NOW(), 'Day')) as today_hours
-- FROM businesses
-- WHERE business_hours IS NOT NULL;

COMMIT;
