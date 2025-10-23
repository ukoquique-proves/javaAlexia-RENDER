-- Step 8: Products Table
-- Universal product catalog for any retail business
-- Uses JSONB for flexible variants and metadata

CREATE TABLE IF NOT EXISTS products (
    id BIGSERIAL PRIMARY KEY,
    business_id BIGINT NOT NULL REFERENCES businesses(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10,2),
    category VARCHAR(100),
    images TEXT[], -- Array of image URLs
    variants JSONB, -- Flexible variants: sizes, colors, materials, etc.
    stock INTEGER DEFAULT 0 NOT NULL,
    is_active BOOLEAN DEFAULT true NOT NULL,
    metadata JSONB, -- Extensible metadata for future features
    created_at TIMESTAMP DEFAULT NOW() NOT NULL,
    updated_at TIMESTAMP DEFAULT NOW() NOT NULL
);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_products_business_id ON products(business_id);
CREATE INDEX IF NOT EXISTS idx_products_category ON products(category);
CREATE INDEX IF NOT EXISTS idx_products_is_active ON products(is_active);
CREATE INDEX IF NOT EXISTS idx_products_name ON products(name);

-- GIN index for JSONB fields (for efficient JSON queries)
CREATE INDEX IF NOT EXISTS idx_products_variants ON products USING GIN(variants);
CREATE INDEX IF NOT EXISTS idx_products_metadata ON products USING GIN(metadata);

-- Comments for documentation
COMMENT ON TABLE products IS 'Universal product catalog for any retail business';
COMMENT ON COLUMN products.variants IS 'JSONB field for flexible product variants (sizes, colors, materials, etc.)';
COMMENT ON COLUMN products.metadata IS 'JSONB field for extensible metadata (SEO, barcodes, supplier info, etc.)';
COMMENT ON COLUMN products.images IS 'Array of image URLs for product photos';

-- Sample data for testing (optional)
-- Plastic store products
INSERT INTO products (business_id, name, description, price, category, images, variants, stock, is_active, created_at, updated_at) VALUES
(1, 'Vasos Plásticos 10oz', 'Vasos desechables transparentes de 10oz, ideales para bebidas frías', 55.00, 'Vasos', 
 ARRAY['https://example.com/vasos-10oz.jpg'], 
 '{"materials": ["PP", "PET"], "capacities": ["10oz", "300ml"]}'::jsonb, 
 500, true, NOW(), NOW()),
(1, 'Platos Desechables 9"', 'Platos plásticos de 9 pulgadas, resistentes y económicos', 70.00, 'Platos',
 ARRAY['https://example.com/platos-9in.jpg'],
 '{"materials": ["PP"], "sizes": ["9 inch", "23cm"]}'::jsonb,
 300, true, NOW(), NOW()),
(1, 'Cubiertos Set Completo', 'Set de cubiertos: tenedor, cuchillo y cuchara', 50.00, 'Cubiertos',
 ARRAY['https://example.com/cubiertos-set.jpg'],
 '{"materials": ["PS"], "pieces": ["3 piezas"]}'::jsonb,
 450, true, NOW(), NOW());

-- Fashion store products (example for different business type)
-- INSERT INTO products (business_id, name, description, price, category, images, variants, stock, is_active) VALUES
-- (2, 'Camiseta Básica', 'Camiseta de algodón 100%', 45000.00, 'Ropa',
--  ARRAY['https://example.com/camiseta-front.jpg', 'https://example.com/camiseta-back.jpg'],
--  '{"sizes": ["S", "M", "L", "XL"], "colors": ["Blanco", "Negro", "Azul"]}'::jsonb,
--  100, true);
