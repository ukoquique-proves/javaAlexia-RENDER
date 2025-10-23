# Alexia - Master Development Plan

**Universal Business Assistant** - AI-powered platform to help any seller find buyers, suppliers, and opportunities.

---

## 🎯 Vision

**Alexia helps sellers:**
- 🔍 Find potential buyers nearby (restaurants, events, businesses)
- 🏭 Discover suppliers with better prices
- 🎯 Capture leads automatically
- 📊 Get market intelligence and trends
- 🗣️ Manage business with voice commands
- 💰 Increase sales through AI recommendations

**Target**: Any retail seller (plastic items, fashion, food, hardware, etc.)

**Demo Goal**: Make plastic store owner say "¡Wow, esto es increíble!"

---

## 📊 Current Status

**Completed (Steps 1-7):**
- ✅ Step 1: Base Project and Dashboard (Spring Boot + Vaadin)
- ✅ Step 2: Supabase Connection (PostgreSQL)
- ✅ Step 3: Telegram Bot (echo functionality)
- ✅ Step 4: Dashboard with Telegram Logs
- ✅ Step 5: Basic Bot Commands (/start, /help, /status)
- ✅ Step 6: Grok AI Integration (llama-3.1-8b-instant)
- ✅ Step 7: Business Search by Category
- ✅ **Deployment**: Production on Render with PostgreSQL

**Progress**: 7 steps completed | **Platform**: Spring Boot + Vaadin + Telegram (testing) → WhatsApp (production)

---

## 🎯 Phase 1: Core Business Platform (Steps 8-10)

Essential features to serve fashion SMEs and enable revenue generation.

### Step 8: Product Catalog System

**Priority**: 🔴 Critical  
**Estimated Time**: 1 week  
**Dependencies**: Step 7 (Business CRUD basic)

**Objective**: Complete product management system for fashion businesses.

**Tasks**:
- [ ] Create `Product` entity (name, description, price, images[], sizes, colors, stock)
- [ ] Create `ProductRepository` and `ProductService`
- [ ] Build `ProductsView.java` with CRUD operations
- [ ] Implement image upload/storage
- [ ] Add product search in Telegram: "buscar producto [nombre]"
- [ ] Create database table with indexes

**Database**:
```sql
CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    business_id BIGINT NOT NULL REFERENCES businesses(id),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10,2),
    category VARCHAR(100),
    images TEXT[],
    sizes VARCHAR(100),
    colors VARCHAR(100),
    stock INTEGER DEFAULT 0,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);
```

**Outcome**: Fashion businesses can showcase products with images and prices.

---

### Step 9: Complete Business CRUD

**Priority**: 🔴 Critical  
**Estimated Time**: 1 week  
**Dependencies**: Step 8

**Objective**: Full business management with self-registration capability.

**Tasks**:
- [ ] Enhance `Business` entity (logo, cover_image, description, social_media, hours, rating)
- [ ] Build `BusinessesView.java` with full CRUD
- [ ] Implement CSV bulk import
- [ ] Add Telegram registration wizard: `/registrar_negocio`
- [ ] Create business profile page
- [ ] Add geolocation fields (latitude, longitude) for future geo-search

**Database Updates**:
```sql
ALTER TABLE businesses ADD COLUMN logo_url VARCHAR(500);
ALTER TABLE businesses ADD COLUMN cover_image_url VARCHAR(500);
ALTER TABLE businesses ADD COLUMN description TEXT;
ALTER TABLE businesses ADD COLUMN instagram VARCHAR(100);
ALTER TABLE businesses ADD COLUMN facebook VARCHAR(100);
ALTER TABLE businesses ADD COLUMN whatsapp VARCHAR(50);
ALTER TABLE businesses ADD COLUMN business_hours JSONB;
ALTER TABLE businesses ADD COLUMN latitude DECIMAL(10,8);
ALTER TABLE businesses ADD COLUMN longitude DECIMAL(11,8);
ALTER TABLE businesses ADD COLUMN rating DECIMAL(3,2) DEFAULT 0;
ALTER TABLE businesses ADD COLUMN review_count INTEGER DEFAULT 0;
ALTER TABLE businesses ADD COLUMN subscription_status VARCHAR(50) DEFAULT 'free';
ALTER TABLE businesses ADD COLUMN subscription_expires_at TIMESTAMP;
```

**Outcome**: Businesses can self-register and manage their profiles.

---

### Step 10: Payment Integration (Mercado Pago)

**Priority**: 🔴 Critical  
**Estimated Time**: 1 week  
**Dependencies**: Step 9

**Objective**: Enable subscription business model (COP $400,000/month).

**Tasks**:
- [ ] Add Mercado Pago SDK to `pom.xml`
- [ ] Create `Payment` and `Subscription` entities
- [ ] Create `PaymentService.java` for Mercado Pago API
- [ ] Build `BillingView.java` (subscription status, payment history, invoices)
- [ ] Implement payment flow (generate link, webhook, status update)
- [ ] Add Telegram commands: `/suscripcion`, `/pagar`
- [ ] Create scheduled job for subscription expiration checks

**Database**:
```sql
CREATE TABLE subscriptions (
    id BIGSERIAL PRIMARY KEY,
    business_id BIGINT NOT NULL REFERENCES businesses(id),
    status VARCHAR(50) NOT NULL,
    plan VARCHAR(50) DEFAULT 'standard',
    price_cop DECIMAL(10,2),
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    auto_renew BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    subscription_id BIGINT REFERENCES subscriptions(id),
    business_id BIGINT NOT NULL REFERENCES businesses(id),
    amount_cop DECIMAL(10,2) NOT NULL,
    status VARCHAR(50) NOT NULL,
    payment_method VARCHAR(50),
    mercado_pago_id VARCHAR(100),
    payment_date TIMESTAMP,
    created_at TIMESTAMP DEFAULT NOW()
);
```

**Outcome**: Revenue generation through subscriptions and freemium model.

---

## 🎯 Phase 2: Lead Generation & Monetization (Steps 11-13)

Critical features from original LOVABLE-PROMPT for lead capture and campaign monetization.

### Step 11: Lead Management System

**Priority**: 🔴 Critical (from original prompt)  
**Estimated Time**: 1.5 weeks  
**Dependencies**: Step 9

**Objective**: Capture and manage leads with opt-in consent (LOVABLE-PROMPT core feature).

**Tasks**:
- [ ] Create `Lead` entity (business_id, user_wa_id, campaign_id, source, status, contact info)
- [ ] Create `LeadRepository` and `LeadService`
- [ ] Build `LeadsView.java` with lead inbox per business
- [ ] Implement opt-in consent flow in Telegram
- [ ] Add interactive data capture (name, email, phone)
- [ ] Create lead status tracking (new → contacted → qualified → won/lost)
- [ ] Add timeline and notes per lead
- [ ] Implement privacy compliance (data minimization, retention policy)

**Database**:
```sql
CREATE TABLE leads (
    id BIGSERIAL PRIMARY KEY,
    business_id BIGINT NOT NULL REFERENCES businesses(id),
    user_wa_id VARCHAR(50),
    campaign_id BIGINT REFERENCES campaigns(id),
    source VARCHAR(50), -- data_alexia, web, organic
    status VARCHAR(50) DEFAULT 'new', -- new, contacted, qualified, won, lost
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    full_name VARCHAR(255),
    phone VARCHAR(50),
    email VARCHAR(255),
    address TEXT,
    city VARCHAR(100),
    country VARCHAR(50),
    value_estimated DECIMAL(10,2),
    notes TEXT,
    consent_given BOOLEAN DEFAULT false,
    consent_date TIMESTAMP,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_leads_business_id ON leads(business_id);
CREATE INDEX idx_leads_status ON leads(status);
CREATE INDEX idx_leads_created_at ON leads(created_at DESC);
```

**Telegram Flow**:
1. User shows interest in business
2. Bot asks for opt-in consent
3. If yes, capture name, email, phone
4. Create lead and notify business
5. Show lead in dashboard

**Outcome**: Businesses can capture and track customer leads with privacy compliance.

---

### Step 12: Campaign & Tracking System

**Priority**: 🔴 Critical (from original prompt)  
**Estimated Time**: 2 weeks  
**Dependencies**: Step 11

**Objective**: Implement CPC/CPA campaigns with event tracking (LOVABLE-PROMPT monetization core).

**Tasks**:
- [ ] Create `Campaign` entity (business_id, type, budget, bid, status, targeting)
- [ ] Create `AdEvent` entity (campaign_id, type, link_id, user_id, timestamp)
- [ ] Create `CampaignService` and `TrackingService`
- [ ] Build `CampaignsView.java` with campaign CRUD
- [ ] Implement link tracking redirector: `/r/{link_id}`
- [ ] Add sponsored results in search (marked "Patrocinado")
- [ ] Track events: impressions, clicks, contacts, visits, leads
- [ ] Create analytics dashboard per campaign
- [ ] Implement usage-based billing (CPC/CPA charges)

**Database**:
```sql
CREATE TABLE campaigns (
    id BIGSERIAL PRIMARY KEY,
    business_id BIGINT NOT NULL REFERENCES businesses(id),
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL, -- CPC, CPA_CONTACT, CPA_VISIT, CPA_LEAD
    budget_daily DECIMAL(10,2),
    budget_monthly DECIMAL(10,2),
    bid_amount DECIMAL(10,2),
    status VARCHAR(50) DEFAULT 'draft', -- draft, active, paused, completed
    target_categories TEXT[],
    target_cities TEXT[],
    start_date TIMESTAMP,
    end_date TIMESTAMP,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE ad_events (
    id BIGSERIAL PRIMARY KEY,
    campaign_id BIGINT REFERENCES campaigns(id),
    business_id BIGINT NOT NULL REFERENCES businesses(id),
    lead_id BIGINT REFERENCES leads(id),
    event_type VARCHAR(50) NOT NULL, -- impression, click, contact, visit, lead
    link_id VARCHAR(100),
    user_wa_id VARCHAR(50),
    cost DECIMAL(10,2),
    metadata JSONB,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_campaigns_business_id ON campaigns(business_id);
CREATE INDEX idx_campaigns_status ON campaigns(status);
CREATE INDEX idx_ad_events_campaign_id ON ad_events(campaign_id);
CREATE INDEX idx_ad_events_event_type ON ad_events(event_type);
CREATE INDEX idx_ad_events_created_at ON ad_events(created_at DESC);
```

**Tracking Flow**:
1. Generate signed `link_id` for each button (WhatsApp, Maps, website)
2. All clicks go through `/r/{link_id}` redirector
3. Log event in `ad_events` table
4. Redirect to destination URL
5. Calculate costs based on campaign type

**Outcome**: Businesses can run paid campaigns and track ROI with transparent billing.

---

### Step 13: CRM Integration (HighLevel)

**Priority**: 🟡 High (from original prompt)  
**Estimated Time**: 2 weeks  
**Dependencies**: Step 11

**Objective**: Sync leads to HighLevel CRM automatically (LOVABLE-PROMPT key integration).

**Tasks**:
- [ ] Create `CrmConnection` entity (business_id, provider, config, status)
- [ ] Create `HighLevelService.java` for API integration
- [ ] Implement contact upsert (by email/phone)
- [ ] Implement opportunity creation (pipeline, stage, value)
- [ ] Add custom field management (alexia_query, alexia_distance_m, etc.)
- [ ] Build CRM configuration UI in dashboard
- [ ] Add "Send to CRM" button in leads view
- [ ] Implement retry logic with exponential backoff
- [ ] Track sync status per lead

**Database**:
```sql
CREATE TABLE crm_connections (
    id BIGSERIAL PRIMARY KEY,
    business_id BIGINT NOT NULL REFERENCES businesses(id),
    provider VARCHAR(50) NOT NULL, -- highlevel, hubspot, pipedrive, webhook
    api_key TEXT,
    location_id VARCHAR(100),
    pipeline_id VARCHAR(100),
    stage_id VARCHAR(100),
    custom_field_ids JSONB,
    webhook_url TEXT,
    status VARCHAR(50) DEFAULT 'active',
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

ALTER TABLE leads ADD COLUMN crm_sync_status VARCHAR(50);
ALTER TABLE leads ADD COLUMN crm_sync_date TIMESTAMP;
ALTER TABLE leads ADD COLUMN crm_contact_id VARCHAR(100);
ALTER TABLE leads ADD COLUMN crm_opportunity_id VARCHAR(100);
ALTER TABLE leads ADD COLUMN crm_sync_error TEXT;
```

**HighLevel Integration**:
- Base URL: `https://services.leadconnectorhq.com/`
- Auth: `Authorization: Bearer {api_key}`
- Endpoints: `/v1/contacts/`, `/v1/opportunities/`
- Custom fields: `alexia_query`, `alexia_business_id`, `alexia_campaign_id`

**Outcome**: Leads automatically sync to client CRMs for follow-up.

---

## 🎯 Phase 3: Intelligent Search & Location (Steps 14-16)

Advanced search capabilities from original LOVABLE-PROMPT.

### Step 14: RAG Search Strategy

**Priority**: 🟡 High (from original prompt)  
**Estimated Time**: 2 weeks  
**Dependencies**: Step 9

**Objective**: Implement intelligent search with source citation (LOVABLE-PROMPT core AI feature).

**Tasks**:
- [ ] Create `SearchService.java` with RAG orchestration
- [ ] Implement internal DB search (data_alexia) with ranking
- [ ] Add Google Places API fallback for insufficient results
- [ ] Create `ExternalResultCache` entity for web results
- [ ] Implement source citation in responses (`data_alexia` vs `web`)
- [ ] Add confidence scoring for results
- [ ] Implement result deduplication by place_id
- [ ] Update AI system prompt: "Never invent data, always cite sources"

**Database**:
```sql
CREATE TABLE external_results_cache (
    id BIGSERIAL PRIMARY KEY,
    query_hash VARCHAR(64) NOT NULL,
    source VARCHAR(50), -- google_places, maps, etc
    source_place_id VARCHAR(255),
    business_name VARCHAR(255),
    category VARCHAR(100),
    latitude DECIMAL(10,8),
    longitude DECIMAL(11,8),
    rating DECIMAL(3,2),
    rating_count INTEGER,
    address TEXT,
    phone VARCHAR(50),
    website VARCHAR(500),
    confidence DECIMAL(3,2),
    fetched_at TIMESTAMP NOT NULL,
    ttl INTEGER DEFAULT 86400,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_external_cache_query_hash ON external_results_cache(query_hash);
CREATE INDEX idx_external_cache_place_id ON external_results_cache(source_place_id);
```

**Search Flow**:
1. Parse user query (category, filters, location)
2. Search internal `businesses` table first
3. If < 3 results, query Google Places API
4. Cache external results with TTL
5. Rank by: distance, rating, boost_paid, freshness
6. Format response with source citation
7. Mark sponsored results as "Patrocinado"

**Outcome**: Intelligent search that prioritizes internal data, cites sources, never invents information.

---

### Step 15: Geolocation Search (PostGIS)

**Priority**: 🟡 High (from original prompt)  
**Estimated Time**: 1.5 weeks  
**Dependencies**: Step 14

**Objective**: Enable "near me" searches with radius filtering (LOVABLE-PROMPT location feature).

**Tasks**:
- [ ] Enable PostGIS extension in PostgreSQL
- [ ] Add geometry column to `businesses` table
- [ ] Create spatial indexes
- [ ] Implement location sharing in Telegram
- [ ] Add radius-based search queries
- [ ] Calculate and display distances
- [ ] Implement "open now" filter with timezone support
- [ ] Add map visualization in dashboard

**Database**:
```sql
-- Enable PostGIS
CREATE EXTENSION IF NOT EXISTS postgis;

-- Add geometry column
ALTER TABLE businesses ADD COLUMN location GEOGRAPHY(POINT, 4326);

-- Update from lat/long
UPDATE businesses SET location = ST_SetSRID(ST_MakePoint(longitude, latitude), 4326)
WHERE latitude IS NOT NULL AND longitude IS NOT NULL;

-- Create spatial index
CREATE INDEX idx_businesses_location ON businesses USING GIST(location);

-- Example query: businesses within 3km
SELECT id, name, 
       ST_Distance(location, ST_SetSRID(ST_MakePoint(-74.0721, 4.7110), 4326)) as distance_m
FROM businesses
WHERE ST_DWithin(location, ST_SetSRID(ST_MakePoint(-74.0721, 4.7110), 4326), 3000)
ORDER BY distance_m;
```

**Telegram Flow**:
1. User sends location or enters address
2. Geocode address if needed
3. Search businesses within radius (default 3km)
4. Sort by distance
5. Show results with distance in meters/km

**Outcome**: Users can find businesses "near me" with accurate distance calculations.

---

### Step 16: WhatsApp Business API Integration

**Priority**: 🟡 High  
**Estimated Time**: 2 weeks  
**Dependencies**: Steps 11, 14 (tested in Telegram first)

**Objective**: Migrate from Telegram to WhatsApp for production (Colombian market requirement).

**Note**: See PLAN.md Phase 2B for interactive messages, business hours, and alerts features.

---

## 🎯 Phase 3B: Advanced Monetization & Onboarding (Steps 16A-16C)

Features from original LOVABLE-PROMPT for advanced monetization and rapid onboarding.

### Step 16A: Visit Verification (CPA_VISIT)

**Priority**: 🟡 Medium  
**Estimated Time**: 1.5 weeks  
**Dependencies**: Step 12 (Campaign system)

**Objective**: Verify when customers actually visit the store (unique monetization feature).

**Why This Matters**:
- Seller only pays when customer shows up
- Higher value than CPC (Cost Per Click)
- Proves ROI of campaigns
- Builds trust with sellers

**Implementation Options**:

**Option A: Check-in with PIN/QR Code**
1. Customer receives check-in link after clicking business
2. Customer arrives at store
3. Shows QR code or PIN to seller
4. Seller scans/enters PIN in dashboard
5. Visit confirmed → CPA_VISIT event logged
6. Seller charged visit fee

**Option B: Geofencing (Automatic)**
1. Customer shares location permission
2. System monitors location in background
3. When enters store radius (50m) + stays 5+ minutes
4. Visit auto-confirmed
5. Customer notified: "Gracias por visitar [Business]"

**Database**:
```sql
CREATE TABLE visit_verifications (
    id BIGSERIAL PRIMARY KEY,
    lead_id BIGINT REFERENCES leads(id),
    business_id BIGINT REFERENCES businesses(id),
    campaign_id BIGINT REFERENCES campaigns(id),
    verification_method VARCHAR(50), -- qr_code, pin, geofence
    verification_code VARCHAR(20),
    verified_at TIMESTAMP,
    verified_by_user_id BIGINT,
    customer_location GEOGRAPHY(POINT, 4326),
    time_spent_minutes INTEGER,
    status VARCHAR(50) DEFAULT 'pending', -- pending, verified, expired
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_visit_verifications_lead_id ON visit_verifications(lead_id);
CREATE INDEX idx_visit_verifications_status ON visit_verifications(status);
```

**Seller Dashboard**:
- "Verify Visit" button
- Scan QR code with camera
- Enter PIN manually
- View pending/verified visits
- See CPA_VISIT charges

**Customer Flow (QR)**:
```
Bot: "¡Genial! Aquí está la info de Plásticos del Hogar"
     📍 Dirección: Calle 45 #12-34
     📞 WhatsApp: +57 310 123 4567
     
     🎟️ Código de visita: ABC123
     Muestra este código al llegar para confirmar tu visita.
     
     [Ver mapa] [Llamar ahora]
```

**Outcome**: Sellers pay only for real visits, not just clicks. Proves campaign effectiveness.

---

### Step 16B: Import by Google Place ID

**Priority**: 🟡 Medium  
**Estimated Time**: 1 week  
**Dependencies**: Step 9 (Business CRUD)

**Objective**: Rapid business onboarding by importing from Google Places.

**Why This Matters**:
- Add 100 businesses in minutes (not hours)
- Auto-fill all data (name, address, phone, hours, photos, rating)
- Keep data updated from Google
- Reduce manual data entry errors

**Features**:

1. **Single Import**
   - Admin pastes Google Place ID
   - System fetches all data via Google Places API
   - Preview before saving
   - One-click import

2. **Bulk Import**
   - Upload CSV with Place IDs
   - System fetches all in batch
   - Shows progress bar
   - Review and confirm

3. **Auto-Update**
   - Scheduled job runs daily
   - Checks for updated info (hours, phone, rating)
   - Updates database automatically
   - Logs changes

**Database**:
```sql
ALTER TABLE businesses ADD COLUMN google_place_id VARCHAR(255) UNIQUE;
ALTER TABLE businesses ADD COLUMN google_data_last_updated TIMESTAMP;
ALTER TABLE businesses ADD COLUMN auto_update_enabled BOOLEAN DEFAULT true;

CREATE TABLE business_import_logs (
    id BIGSERIAL PRIMARY KEY,
    place_id VARCHAR(255),
    business_id BIGINT REFERENCES businesses(id),
    status VARCHAR(50), -- success, failed, duplicate
    data_fetched JSONB,
    error_message TEXT,
    imported_by_user_id BIGINT,
    created_at TIMESTAMP DEFAULT NOW()
);
```

**Admin UI**:
```
[Import Business from Google]

Option 1: Single Import
Place ID: [ChIJN1t_tDeuEmsRUsoyG83frY4___]
[Fetch Data]

Preview:
  Name: Plásticos Mayorista S.A.
  Address: Cra 7 #45-23, Bogotá
  Phone: +57 601 234 5678
  Hours: Mon-Fri 8am-6pm, Sat 9am-2pm
  Rating: 4.3 ⭐ (127 reviews)
  Photos: 8 images
  
[Import] [Cancel]

Option 2: Bulk Import
Upload CSV: [Choose file]
Format: place_id,category
Example: ChIJN1t...,restaurant

[Upload & Import]
```

**Google Places API Call**:
```java
PlaceDetails details = googlePlacesService.getPlaceDetails(placeId);

Business business = new Business();
business.setName(details.getName());
business.setAddress(details.getFormattedAddress());
business.setPhone(details.getPhoneNumber());
business.setWebsite(details.getWebsite());
business.setLatitude(details.getGeometry().getLocation().getLat());
business.setLongitude(details.getGeometry().getLocation().getLng());
business.setRating(details.getRating());
business.setReviewCount(details.getUserRatingsTotal());
business.setBusinessHours(parseOpeningHours(details.getOpeningHours()));
business.setGooglePlaceId(placeId);
business.setGoogleDataLastUpdated(LocalDateTime.now());
```

**Outcome**: Onboard 100+ businesses in 10 minutes instead of 10 hours.

---

### Step 16C: RBAC (Role-Based Access Control)

**Priority**: 🟡 Medium  
**Estimated Time**: 1 week  
**Dependencies**: Step 9 (Business CRUD)

**Objective**: Multi-user access with different permission levels.

**Why This Matters**:
- Teams can collaborate
- Sellers manage only their businesses
- Analysts view reports without editing
- Billing team handles payments only
- Security and audit trails

**Roles**:

1. **SUPERADMIN**
   - Full access to everything
   - Manage all businesses
   - System configuration
   - User management

2. **COMERCIANTE (Merchant)**
   - Manage only their own businesses
   - View their leads and campaigns
   - Update products and prices
   - View their billing

3. **ANALISTA (Analyst)**
   - Read-only access to analytics
   - Export reports
   - View all metrics
   - No editing permissions

4. **FACTURACION (Billing)**
   - Manage payments and subscriptions
   - Generate invoices
   - View billing reports
   - No access to business data

5. **EDITOR**
   - Edit content and products
   - Manage campaigns
   - No access to billing
   - No user management

**Database**:
```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    role VARCHAR(50) NOT NULL, -- superadmin, comerciante, analista, facturacion, editor
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE user_business_access (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    business_id BIGINT REFERENCES businesses(id),
    access_level VARCHAR(50), -- owner, editor, viewer
    created_at TIMESTAMP DEFAULT NOW(),
    UNIQUE(user_id, business_id)
);

ALTER TABLE businesses ADD COLUMN owner_user_id BIGINT REFERENCES users(id);
```

**Spring Security Configuration**:
```java
@PreAuthorize("hasRole('SUPERADMIN') or @businessSecurity.isOwner(#businessId)")
public Business updateBusiness(Long businessId, Business updates) {
    // Only superadmin or business owner can update
}

@PreAuthorize("hasAnyRole('SUPERADMIN', 'ANALISTA', 'COMERCIANTE')")
public List<Metric> getMetrics() {
    // Analysts and merchants can view metrics
}

@PreAuthorize("hasAnyRole('SUPERADMIN', 'FACTURACION')")
public Invoice generateInvoice(Long businessId) {
    // Only superadmin and billing team
}
```

**Vaadin UI**:
```java
// Show menu items based on role
if (SecurityUtils.hasRole("SUPERADMIN")) {
    menu.add("System Configuration");
    menu.add("User Management");
}

if (SecurityUtils.hasRole("COMERCIANTE")) {
    menu.add("My Businesses");
    menu.add("My Campaigns");
}

if (SecurityUtils.hasRole("ANALISTA")) {
    menu.add("Analytics");
    menu.add("Reports");
}
```

**Outcome**: Teams can collaborate securely with appropriate access levels.

**Tasks**:
- [ ] Register WhatsApp Business API account (Meta)
- [ ] Add WhatsApp SDK to `pom.xml`
- [ ] Create `WhatsAppService.java` similar to Telegram bot
- [ ] Implement webhook for incoming messages
- [ ] Create interactive templates (buttons, lists, location requests)
- [ ] Migrate successful Telegram flows to WhatsApp
- [ ] Add WhatsApp-specific features (status updates, catalogs)
- [ ] Update dashboard to show WhatsApp conversations
- [ ] Maintain Telegram as secondary channel

**Configuration**:
```bash
# WhatsApp Business API (Meta Cloud API)
WHATSAPP_VERIFY_TOKEN=verify_token_here
WHATSAPP_PHONE_NUMBER_ID=1234567890
WHATSAPP_APP_ID=123456789012345
WHATSAPP_APP_SECRET=appsecret_xxxx
WHATSAPP_ACCESS_TOKEN=your_access_token
```

**Templates to Create**:
- `alexia_welcome` - Welcome message
- `alexia_location_request` - Ask for location
- `alexia_capture_name` - Capture user name
- `alexia_capture_email` - Capture email
- `alexia_optin_privacy` - Privacy consent
- `alexia_lead_created` - Lead confirmation

**Outcome**: Production-ready WhatsApp integration for Colombian market.

---

## 🎯 Phase 4: Visibility & Analytics (Steps 17-18)

Monitoring and business intelligence features.

### Step 17: AI Conversations Dashboard

**Priority**: 🟢 Medium  
**Estimated Time**: 3 days  
**Dependencies**: Step 6

**Objective**: Monitor and analyze AI-powered conversations.

**Tasks**:
- [ ] Create `AIConversationsView.java` in Vaadin
- [ ] Display conversation history with Grid
- [ ] Add metrics: total conversations, avg response time, active users
- [ ] Implement filters: date range, user, AI provider
- [ ] Add chart: conversations per day
- [ ] Implement auto-refresh (10 seconds)
- [ ] Add export to CSV

**Outcome**: Visibility into AI usage and conversation quality for testing/refinement.

---

### Step 18: Advanced Metrics Dashboard

**Priority**: 🟢 Medium  
**Estimated Time**: 1 week  
**Dependencies**: Steps 12, 17

**Objective**: Comprehensive business intelligence dashboard.

**Tasks**:
- [ ] Create `MetricsView.java` with multiple charts
- [ ] Implement metric cards (messages, searches, businesses, users, revenue)
- [ ] Add charts: messages/day, top categories, command usage, campaign ROI
- [ ] Create `MetricsService.java` for data aggregation
- [ ] Implement date range filters
- [ ] Add export functionality (PDF/CSV)
- [ ] Create scheduled job for daily metrics calculation
- [ ] Add comparison with previous period

**Database**:
```sql
CREATE TABLE daily_metrics (
    id BIGSERIAL PRIMARY KEY,
    metric_date DATE NOT NULL UNIQUE,
    total_messages INTEGER DEFAULT 0,
    total_searches INTEGER DEFAULT 0,
    total_commands INTEGER DEFAULT 0,
    total_leads INTEGER DEFAULT 0,
    total_revenue_cop DECIMAL(10,2) DEFAULT 0,
    active_users INTEGER DEFAULT 0,
    active_businesses INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT NOW()
);
```

**Outcome**: Data-driven insights for business growth and optimization.

---

## 🎯 Phase 5: Advanced Features (Steps 19-22)

Optional enhancements for competitive advantage.

### Step 19: OpenAI Integration (Alternative AI)

**Priority**: 🟢 Low  
**Estimated Time**: 3 days  
**Dependencies**: Step 6

**Objective**: Add OpenAI as fallback/alternative AI provider.

**Tasks**:
- [ ] Create `OpenAIService.java`
- [ ] Create `IAIProvider.java` interface
- [ ] Implement Strategy pattern with `AIProviderFactory.java`
- [ ] Add provider switching commands
- [ ] Update dashboard with provider selector

**Outcome**: Fallback option if Grok API has issues, enables A/B testing.

---

### Step 20: Image Generation (DALL·E)

**Priority**: 🟢 Low (from original prompt)  
**Estimated Time**: 1 week  
**Dependencies**: Step 8

**Objective**: AI-generated images for fashion brands (original vision feature).

**Tasks**:
- [ ] Add DALL·E API integration
- [ ] Create image generation service
- [ ] Add "Generate Image" button in product form
- [ ] Implement brand style customization
- [ ] Create image library management
- [ ] Add image editing/refinement

**Outcome**: Fashion businesses can generate product images with AI.

---

### Step 21: Marketing Automation Workflows

**Priority**: 🟢 Low (from original prompt)  
**Estimated Time**: 2 weeks  
**Dependencies**: Steps 11, 16

**Objective**: Automated marketing sequences (original vision feature).

**Tasks**:
- [ ] Create workflow builder UI
- [ ] Implement trigger system (new lead, abandoned cart, etc.)
- [ ] Add action nodes (send message, wait, condition)
- [ ] Create template library
- [ ] Implement scheduling and delays
- [ ] Add workflow analytics

**Outcome**: Businesses can automate customer communication sequences.

---

### Step 22: Multi-language Support (i18n)

**Priority**: 🟢 Low (from original prompt)  
**Estimated Time**: 1 week  
**Dependencies**: All messaging features

**Objective**: Support Spanish and English (original vision feature).

**Tasks**:
- [ ] Implement language detection
- [ ] Create translation files
- [ ] Update AI system prompt for bilingual support
- [ ] Add language selector in dashboard
- [ ] Translate all UI strings
- [ ] Test with both languages

**Outcome**: Serve both Spanish and English-speaking markets.

---

## 📋 Development Principles

### From Original LOVABLE-PROMPT:

1. **Never Invent Data** - Always cite sources, indicate `data_alexia` vs `web`
2. **Privacy First** - Explicit opt-in consent before capturing leads
3. **Transparency** - Mark sponsored results, show data sources
4. **Local First** - Search internal database before external sources
5. **Incremental** - Build and test one feature at a time

### Best Practices:

- ✅ Test in Telegram before migrating to WhatsApp
- ✅ Create database migrations for all schema changes
- ✅ Add logging for debugging and audit trails
- ✅ Implement retry logic for external API calls
- ✅ Write tests for critical business logic
- ✅ Update CHANGELOG.md after each step
- ✅ Keep code clean and documented

---

## 📋 Development Guidelines

### Before Starting Each Step:
1. ✅ Review the task list
2. ✅ Check database schema requirements
3. ✅ Verify dependencies in `pom.xml`
4. ✅ Create a git branch for the step

### During Development:
1. ✅ Write code incrementally
2. ✅ Test frequently (`mvn spring-boot:run`)
3. ✅ Add logging for debugging
4. ✅ Keep code clean and documented

### After Completing Each Step:
1. ✅ Run full application test
2. ✅ Verify in both Telegram and Dashboard
3. ✅ Update `CHANGELOG.md`
4. ✅ Commit with descriptive message
5. ✅ Update progress in plan documents

---

## 🎯 Milestones

### Milestone 1: Business-Ready Platform (Steps 8-10)
**Timeline**: 3 weeks  
**Deliverables**: Product catalog, business CRUD, payment system  
**Revenue**: COP $400k/month subscriptions enabled

### Milestone 2: Lead Generation System (Steps 11-13)
**Timeline**: 5-6 weeks  
**Deliverables**: Lead management, campaigns, CRM integration  
**Revenue**: CPC/CPA monetization enabled

### Milestone 3: Intelligent Search (Steps 14-16)
**Timeline**: 5-6 weeks  
**Deliverables**: RAG search, geolocation, WhatsApp integration  
**Market**: Production-ready for Colombian fashion SMEs

### Milestone 4: Analytics & Optimization (Steps 17-18)
**Timeline**: 2 weeks  
**Deliverables**: AI dashboard, metrics dashboard  
**Value**: Data-driven decision making

### Milestone 5: Advanced Features (Steps 19-22)
**Timeline**: 5-6 weeks  
**Deliverables**: OpenAI, DALL·E, workflows, i18n  
**Competitive**: Premium features for differentiation

---

## 📊 Total Timeline Estimate

- **Phase 1** (Steps 8-10): 3 weeks
- **Phase 2** (Steps 11-13): 5-6 weeks
- **Phase 3** (Steps 14-16): 5-6 weeks
- **Phase 4** (Steps 17-18): 2 weeks
- **Phase 5** (Steps 19-22): 5-6 weeks (optional)

**Total to Production**: ~15-17 weeks (4-5 months)  
**Total with Advanced Features**: ~20-23 weeks (5-6 months)

---

## 🎯 Immediate Next Steps

**Start with**: Step 8 - Product Catalog System

**Why**:
1. Core feature for fashion SME target market
2. Enables businesses to showcase products
3. Foundation for e-commerce
4. Required before payment system
5. Dashboard already prepared (minimal changes needed)

**Estimated Time**: 1 week  
**Complexity**: Medium  
**Value**: High

---

**Document Created**: 2025-10-19  
**Last Updated**: 2025-10-19  
**Current Step**: 7 completed, starting Step 8  
**Sources**: NEXT_STEPS.md + LOVABLE-PROMPT + Project alignment analysis  
**Strategy**: Telegram testing → WhatsApp production | Internal DB first → External fallback
