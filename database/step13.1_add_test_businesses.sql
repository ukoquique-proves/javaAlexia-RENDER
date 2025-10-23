-- Step 13.1: Add test businesses for RAG search testing
-- This script adds a few sample bakeries (panaderías) in Bogotá to test the internal search functionality.

-- Delete any existing test businesses first
DELETE FROM businesses WHERE name IN ('Panadería y Pastelería El Trigal', 'La Baguette de la 93', 'Masa Panaderia Artesanal', 'Panaderia y Pasteleria El Trigal');

INSERT INTO businesses (name, category, phone, location, is_active, is_verified, created_at, updated_at)
VALUES
    ('Panaderia y Pasteleria El Trigal', 'panaderia', '+57 310 1234567', ST_SetSRID(ST_MakePoint(-74.066, 4.715), 4326), true, true, NOW(), NOW()),
    ('La Baguette de la 93', 'panaderia', '+57 311 9876543', ST_SetSRID(ST_MakePoint(-74.053, 4.676), 4326), true, true, NOW(), NOW()),
    ('Masa Panaderia Artesanal', 'panaderia', '+57 312 5558899', ST_SetSRID(ST_MakePoint(-74.078, 4.658), 4326), true, false, NOW(), NOW());

-- Log the migration
INSERT INTO migration_log (script_name, execution_date)
VALUES ('step13.1_add_test_businesses.sql', NOW());

-- COMMIT;
