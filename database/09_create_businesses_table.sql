-- =====================================================
-- PASO 9: Tabla de Negocios (Búsqueda Simple)
-- =====================================================
-- Descripción: Tabla simplificada para almacenar negocios
--              y permitir búsqueda básica por categoría
-- Fecha: 2025-10-16
-- =====================================================

-- Crear tabla de negocios
CREATE TABLE IF NOT EXISTS businesses (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    category VARCHAR(100),
    address VARCHAR(500),
    phone VARCHAR(50),
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- Índices para optimización de búsquedas
CREATE INDEX IF NOT EXISTS idx_businesses_category ON businesses(category);
CREATE INDEX IF NOT EXISTS idx_businesses_is_active ON businesses(is_active);
CREATE INDEX IF NOT EXISTS idx_businesses_name ON businesses(name);

-- Comentarios en las columnas
COMMENT ON TABLE businesses IS 'Tabla de negocios registrados en la plataforma';
COMMENT ON COLUMN businesses.id IS 'ID único del negocio';
COMMENT ON COLUMN businesses.name IS 'Nombre del negocio';
COMMENT ON COLUMN businesses.category IS 'Categoría del negocio (ej: panadería, restaurante)';
COMMENT ON COLUMN businesses.address IS 'Dirección física del negocio';
COMMENT ON COLUMN businesses.phone IS 'Teléfono de contacto';
COMMENT ON COLUMN businesses.is_active IS 'Indica si el negocio está activo';
COMMENT ON COLUMN businesses.created_at IS 'Fecha de creación del registro';
COMMENT ON COLUMN businesses.updated_at IS 'Fecha de última actualización';

-- =====================================================
-- DATOS DE PRUEBA
-- =====================================================

-- Insertar negocios de prueba
INSERT INTO businesses (name, category, address, phone) VALUES
-- Panaderías
('Panadería El Sol', 'panadería', 'Calle 123, Costa Azul', '555-0001'),
('Pan Caliente', 'panadería', 'Av. Principal 456', '555-0002'),
('Panadería Artesanal', 'panadería', 'Calle del Horno 789', '555-0003'),

-- Restaurantes
('Restaurante La Costa', 'restaurante', 'Playa Norte 789', '555-0004'),
('El Buen Sabor', 'restaurante', 'Centro Comercial, Local 12', '555-0005'),
('Restaurante Mar y Tierra', 'restaurante', 'Av. Marítima 321', '555-0006'),

-- Cafeterías
('Café Aroma', 'cafetería', 'Plaza Central 45', '555-0007'),
('Cafetería Express', 'cafetería', 'Estación de Tren', '555-0008'),

-- Farmacias
('Farmacia San José', 'farmacia', 'Calle Principal 100', '555-0009'),
('Farmacia 24 Horas', 'farmacia', 'Av. Libertad 200', '555-0010'),

-- Supermercados
('Supermercado Central', 'supermercado', 'Av. Principal 500', '555-0011'),
('Mini Market Express', 'supermercado', 'Barrio Norte, Calle 8', '555-0012')

ON CONFLICT DO NOTHING;

-- Verificar datos insertados
SELECT 
    category,
    COUNT(*) as total_negocios
FROM businesses
WHERE is_active = true
GROUP BY category
ORDER BY category;

-- =====================================================
-- FIN DEL SCRIPT
-- =====================================================
