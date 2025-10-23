-- Step 11: Lead Capture System
-- Creates leads table with GDPR/LGPD compliance and CRM readiness

-- Create leads table
CREATE TABLE IF NOT EXISTS leads (
    id BIGSERIAL PRIMARY KEY,
    business_id BIGINT NOT NULL REFERENCES businesses(id) ON DELETE CASCADE,
    user_wa_id VARCHAR(50), -- WhatsApp ID (works with Telegram ID too)
    campaign_id BIGINT, -- For future campaigns feature
    source VARCHAR(50) NOT NULL, -- telegram, whatsapp, web, organic, data_alexia
    status VARCHAR(50) NOT NULL DEFAULT 'new', -- new, contacted, qualified, converted, lost, archived
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100),
    phone VARCHAR(50),
    email VARCHAR(255),
    city VARCHAR(100),
    country VARCHAR(50) DEFAULT 'CO',
    value_estimated DECIMAL(10,2),
    notes TEXT,
    -- GDPR/LGPD Compliance
    consent_given BOOLEAN NOT NULL DEFAULT false,
    consent_date TIMESTAMP,
    -- Future CRM sync fields
    crm_sync_status VARCHAR(50), -- pending, synced, failed
    crm_contact_id VARCHAR(100),
    crm_opportunity_id VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_leads_business_id ON leads(business_id);
CREATE INDEX IF NOT EXISTS idx_leads_status ON leads(status);
CREATE INDEX IF NOT EXISTS idx_leads_user_wa_id ON leads(user_wa_id);
CREATE INDEX IF NOT EXISTS idx_leads_source ON leads(source);
CREATE INDEX IF NOT EXISTS idx_leads_created_at ON leads(created_at);
CREATE INDEX IF NOT EXISTS idx_leads_consent_given ON leads(consent_given);

-- Add constraint to ensure at least one contact method
ALTER TABLE leads ADD CONSTRAINT check_contact_method 
    CHECK (phone IS NOT NULL OR email IS NOT NULL);

-- Add constraint for valid status values
ALTER TABLE leads ADD CONSTRAINT check_status 
    CHECK (status IN ('new', 'contacted', 'qualified', 'converted', 'lost', 'archived'));

-- Add constraint for valid source values
ALTER TABLE leads ADD CONSTRAINT check_source 
    CHECK (source IN ('telegram', 'whatsapp', 'web', 'organic', 'data_alexia'));

-- Add constraint for consent date (must be set if consent is given)
ALTER TABLE leads ADD CONSTRAINT check_consent_date 
    CHECK ((consent_given = false AND consent_date IS NULL) OR 
           (consent_given = true AND consent_date IS NOT NULL));

-- Insert test data
INSERT INTO leads (business_id, user_wa_id, source, status, first_name, last_name, phone, email, city, consent_given, consent_date, notes, created_at)
VALUES 
    (1, '123456789', 'telegram', 'new', 'Carlos', 'Rodríguez', '+57 300 1234567', 'carlos@example.com', 'Bogotá', true, NOW(), 'Interesado en productos biodegradables', NOW() - INTERVAL '2 days'),
    (1, '987654321', 'telegram', 'contacted', 'María', 'González', '+57 301 9876543', 'maria@example.com', 'Medellín', true, NOW() - INTERVAL '5 days', 'Solicitó catálogo completo', NOW() - INTERVAL '5 days'),
    (2, '555666777', 'web', 'qualified', 'Juan', 'Pérez', '+57 302 5556677', 'juan@example.com', 'Cali', true, NOW() - INTERVAL '7 days', 'Lead calificado, presupuesto confirmado', NOW() - INTERVAL '7 days'),
    (2, NULL, 'organic', 'new', 'Ana', 'Martínez', NULL, 'ana@example.com', 'Barranquilla', true, NOW() - INTERVAL '1 day', 'Contacto por referencia', NOW() - INTERVAL '1 day'),
    (3, '111222333', 'telegram', 'converted', 'Pedro', 'López', '+57 303 1112223', 'pedro@example.com', 'Bogotá', true, NOW() - INTERVAL '10 days', 'Cliente convertido - primera compra realizada', NOW() - INTERVAL '10 days');

-- Add comment to table
COMMENT ON TABLE leads IS 'Tabla de leads capturados con consentimiento GDPR/LGPD y preparación para CRM';
COMMENT ON COLUMN leads.consent_given IS 'Consentimiento explícito del usuario para almacenar sus datos (GDPR/LGPD)';
COMMENT ON COLUMN leads.consent_date IS 'Fecha y hora en que se otorgó el consentimiento';
COMMENT ON COLUMN leads.user_wa_id IS 'ID de usuario de WhatsApp o Telegram para seguimiento multi-canal';
COMMENT ON COLUMN leads.source IS 'Canal de origen del lead (telegram, whatsapp, web, organic, data_alexia)';
COMMENT ON COLUMN leads.status IS 'Estado del lead en el ciclo de vida (new, contacted, qualified, converted, lost, archived)';

-- Success message
DO $$
BEGIN
    RAISE NOTICE '✅ Step 11: Leads table created successfully with 5 test records';
    RAISE NOTICE '✅ GDPR/LGPD compliance constraints added';
    RAISE NOTICE '✅ Indexes created for performance optimization';
END $$;
