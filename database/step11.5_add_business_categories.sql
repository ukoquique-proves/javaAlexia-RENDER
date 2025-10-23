-- Migration script for Step 11.5: Add Business Categories
-- This script adds a categories column to the businesses table for better matching

-- Add categories column as text array
ALTER TABLE businesses ADD COLUMN IF NOT EXISTS categories TEXT[];

-- Create index for categories column for better performance
CREATE INDEX IF NOT EXISTS idx_businesses_categories ON businesses USING GIN(categories);

-- Add comment to describe the column
COMMENT ON COLUMN businesses.categories IS 'Array of categories for better matching in geolocation searches';

-- Update existing businesses with sample categories based on their current category
-- This is a simplified example - in a real implementation, you would have more sophisticated logic
UPDATE businesses 
SET categories = CASE 
    WHEN category ILIKE '%restaurante%' OR category ILIKE '%comida%' THEN ARRAY['restaurant', 'comida', 'restaurante']
    WHEN category ILIKE '%hotel%' OR category ILIKE '%alojamiento%' THEN ARRAY['hotel', 'alojamiento', 'lodging']
    WHEN category ILIKE '%escuela%' OR category ILIKE '%colegio%' THEN ARRAY['school', 'escuela', 'colegio', 'educación']
    WHEN category ILIKE '%hospital%' OR category ILIKE '%salud%' THEN ARRAY['hospital', 'salud', 'healthcare']
    WHEN category ILIKE '%tienda%' OR category ILIKE '%retail%' THEN ARRAY['store', 'tienda', 'retail']
    WHEN category ILIKE '%oficina%' OR category ILIKE '%negocio%' THEN ARRAY['office', 'oficina', 'business']
    ELSE ARRAY[category]
END
WHERE categories IS NULL OR array_length(categories, 1) IS NULL;

-- Log the migration
INSERT INTO migration_log (version, description, applied_at) 
VALUES ('11.5', 'Add business categories column and populate with sample data', NOW());
