-- Add sample data for suppliers

INSERT INTO suppliers (name, category, products, delivery_time_days, phone, email, website, rating, is_verified)
VALUES 
('Proveedor Plásticos ABC', 'Plásticos', '{"vasos 7oz": 150.00, "platos": 300.50, "cubiertos": 50.25}', 2, '3101234567', 'ventas@plasticosabc.com', 'www.plasticosabc.com', 4.5, true),
('Distri-Todo Plásticos', 'Plásticos', '{"vasos 7oz": 145.50, "platos": 290.00, "botellas": 500.00}', 1, '3119876543', 'info@distritodo.com', 'www.distritodo.com', 4.8, true),
('Suministros Institucionales', 'General', '{"vasos 7oz": 160.00, "servilletas": 120.00}', 3, '3205551212', 'contacto@suministros.com.co', 'www.suministros.com.co', 4.2, false);

INSERT INTO migration_log (version, description, applied_at) 
VALUES ('12.1', 'Add sample supplier data', NOW());
