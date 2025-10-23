-- Step 13: External Results Cache Table for RAG Search
-- This table stores cached results from external sources like Google Places

CREATE TABLE external_results_cache (
    id BIGSERIAL PRIMARY KEY,
    query_hash VARCHAR(64) NOT NULL,
    source VARCHAR(50), -- google_places, maps, etc.
    source_place_id VARCHAR(255),
    business_name VARCHAR(255),
    category VARCHAR(100),
    latitude DECIMAL(10,8),
    longitude DECIMAL(11,8),
    rating DECIMAL(3,2),
    address TEXT,
    phone VARCHAR(50),
    confidence DECIMAL(3,2),
    fetched_at TIMESTAMP NOT NULL,
    ttl INTEGER DEFAULT 86400, -- 24 hours
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_external_cache_query_hash ON external_results_cache(query_hash);
CREATE INDEX idx_external_cache_place_id ON external_results_cache(source_place_id);
CREATE INDEX idx_external_cache_source ON external_results_cache(source);
CREATE INDEX idx_external_cache_fetched_at ON external_results_cache(fetched_at);
