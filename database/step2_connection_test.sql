-- PASO 2: Tabla de prueba de conexión
-- Ejecutar este SQL en Supabase SQL Editor

CREATE TABLE IF NOT EXISTS connection_test (
    id SERIAL PRIMARY KEY,
    message VARCHAR(255),
    created_at TIMESTAMP DEFAULT NOW()
);

-- Insertar un registro de prueba inicial
INSERT INTO connection_test (message) 
VALUES ('Tabla creada correctamente desde SQL');

-- Verificar que la tabla se creó
SELECT * FROM connection_test;
