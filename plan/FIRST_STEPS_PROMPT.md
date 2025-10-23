# First Steps - Original LOVABLE-PROMPT Priority

Implementation order prioritizing the original LOVABLE-PROMPT vision while maintaining compatibility with other planned features.

---

## 🎯 Strategy

**Goal**: Implement core LOVABLE-PROMPT features first, but design them to support future enhancements.

**Principle**: Build foundation that allows both prompt features AND planned features to coexist.

---

## 📋 Phase 1: Foundation with Extensibility (Weeks 1-3)

### [x] Step 8: Universal Product Catalog (Week 1)

**From Prompt**: Products with images, prices, stock
**Future-Ready Design**:
- Use JSONB for `variants` (supports any product type: plastic, fashion, food)
- Array field for `images` (multiple photos)
- Flexible `category` field (not hardcoded to fashion)

**Database**:
```sql
CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    business_id BIGINT REFERENCES businesses(id),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10,2),
    category VARCHAR(100),
    images TEXT[], -- Multiple images
    variants JSONB, -- Flexible: {sizes: [], colors: [], materials: []}
    stock INTEGER DEFAULT 0,
    is_active BOOLEAN DEFAULT true,
    -- Future: Add fields without breaking existing code
    metadata JSONB, -- Extensible for future features
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);
```

**Why This Order**: Products are referenced by leads, campaigns, and suppliers. Build this first.

---

### [x] Step 9: Business Management with Geolocation (Week 2)

**Status**: ✅ **COMPLETED** - Backend implementation complete with full test coverage.

**From Prompt**: Businesses with location, hours, contact info
**Future-Ready Design**:
- Add `location` (PostGIS) from start (needed for "find buyers nearby")
- Add `business_hours` (JSONB) with timezone support
- Add `google_place_id` field (for future import feature)
- Add `owner_user_id` field (for future RBAC)

**Database**:
```sql
-- Enable PostGIS immediately
CREATE EXTENSION IF NOT EXISTS postgis;

ALTER TABLE businesses ADD COLUMN location GEOGRAPHY(POINT, 4326);
ALTER TABLE businesses ADD COLUMN business_hours JSONB;
ALTER TABLE businesses ADD COLUMN google_place_id VARCHAR(255) UNIQUE;
ALTER TABLE businesses ADD COLUMN owner_user_id BIGINT; -- For future RBAC
ALTER TABLE businesses ADD COLUMN logo_url VARCHAR(500);
ALTER TABLE businesses ADD COLUMN whatsapp VARCHAR(50);
ALTER TABLE businesses ADD COLUMN instagram VARCHAR(100);
ALTER TABLE businesses ADD COLUMN rating DECIMAL(3,2) DEFAULT 0;
ALTER TABLE businesses ADD COLUMN is_verified BOOLEAN DEFAULT false;

CREATE INDEX idx_businesses_location ON businesses USING GIST(location);
CREATE INDEX idx_businesses_google_place_id ON businesses(google_place_id);
```

**Why This Order**: Geolocation is critical for prompt's "find nearby" feature. Add it now, use it later.

---

### Step 10: Payment System (Week 3)

**From Prompt**: Mercado Pago subscriptions + usage billing
**Future-Ready Design**:
- Separate `subscriptions` and `payments` tables
- Add `campaign_id` field in payments (for future CPC/CPA charges)
- Support multiple payment types (subscription, campaign, one-time)

**Database**:
```sql
CREATE TABLE subscriptions (
    id BIGSERIAL PRIMARY KEY,
    business_id BIGINT REFERENCES businesses(id),
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
    business_id BIGINT REFERENCES businesses(id),
    subscription_id BIGINT REFERENCES subscriptions(id),
    campaign_id BIGINT, -- For future campaign charges
    payment_type VARCHAR(50), -- subscription, campaign_cpc, campaign_cpa, one_time
    amount_cop DECIMAL(10,2) NOT NULL,
    status VARCHAR(50) NOT NULL,
    mercado_pago_id VARCHAR(100),
    payment_date TIMESTAMP,
    created_at TIMESTAMP DEFAULT NOW()
);
```

**Why This Order**: Revenue generation is critical. Build payment foundation that supports future campaign billing.

---

## 📋 Phase 2: LOVABLE-PROMPT Core Features (Weeks 4-9)

### Step 11: Lead Capture System (Weeks 4-5)

**From Prompt**: Lead management with opt-in consent, status tracking

**Note**: This is when you should consider implementing the **Enhanced Validation Layer** from the archived TO_IMPROVE.md document. Lead capture will require complex validation rules (consent validation, contact info validation, cross-field dependencies).

**Implementation**:

**Database**:
```sql
CREATE TABLE leads (
    id BIGSERIAL PRIMARY KEY,
    business_id BIGINT REFERENCES businesses(id),
    user_wa_id VARCHAR(50), -- WhatsApp ID (works with Telegram ID too)
    campaign_id BIGINT, -- For future campaigns
    source VARCHAR(50), -- data_alexia, web, organic
    status VARCHAR(50) DEFAULT 'new',
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    phone VARCHAR(50),
    email VARCHAR(255),
    city VARCHAR(100),
    country VARCHAR(50) DEFAULT 'CO',
    value_estimated DECIMAL(10,2),
    notes TEXT,
    consent_given BOOLEAN DEFAULT false,
    consent_date TIMESTAMP,
    -- Future CRM sync fields
    crm_sync_status VARCHAR(50),
    crm_contact_id VARCHAR(100),
    crm_opportunity_id VARCHAR(100),
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_leads_business_id ON leads(business_id);
CREATE INDEX idx_leads_status ON leads(status);
CREATE INDEX idx_leads_user_wa_id ON leads(user_wa_id);
```

**Telegram Implementation** (test before WhatsApp):
```java
// Capture lead with consent
if (message.contains("sí") || message.contains("si")) {
    Lead lead = new Lead();
    lead.setBusinessId(businessId);
    lead.setUserWaId(String.valueOf(chatId)); // Works for Telegram, later WhatsApp
    lead.setConsentGiven(true);
    lead.setConsentDate(LocalDateTime.now());
    lead.setSource("telegram"); // Later: "whatsapp"
    leadService.save(lead);
}
```

**Why This Order**: Leads are the core of the prompt's monetization model.

---

### Step 12: Campaign & Tracking System (Weeks 6-7)

**From Prompt**: CPC/CPA campaigns with event tracking
**Implementation**:

**Database**:
```sql
CREATE TABLE campaigns (
    id BIGSERIAL PRIMARY KEY,
    business_id BIGINT REFERENCES businesses(id),
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL, -- CPC, CPA_CONTACT, CPA_VISIT, CPA_LEAD
    budget_daily DECIMAL(10,2),
    budget_monthly DECIMAL(10,2),
    bid_amount DECIMAL(10,2),
    status VARCHAR(50) DEFAULT 'draft',
    target_categories TEXT[],
    target_cities TEXT[],
    start_date TIMESTAMP,
    end_date TIMESTAMP,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE ad_events (
    id BIGSERIAL PRIMARY KEY,
    campaign_id BIGINT REFERENCES campaigns(id),
    business_id BIGINT REFERENCES businesses(id),
    lead_id BIGINT REFERENCES leads(id),
    event_type VARCHAR(50) NOT NULL, -- impression, click, contact, visit, lead
    link_id VARCHAR(100),
    user_wa_id VARCHAR(50),
    cost DECIMAL(10,2),
    metadata JSONB,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_campaigns_business_id ON campaigns(business_id);
CREATE INDEX idx_ad_events_campaign_id ON ad_events(campaign_id);
CREATE INDEX idx_ad_events_event_type ON ad_events(event_type);
```

**Link Tracking Redirector**:
```java
@GetMapping("/r/{linkId}")
public ResponseEntity<Void> trackAndRedirect(@PathVariable String linkId) {
    TrackingLink link = trackingService.getLink(linkId);
    
    // Log event
    AdEvent event = new AdEvent();
    event.setCampaignId(link.getCampaignId());
    event.setEventType("click");
    event.setCost(link.getCpcAmount());
    adEventService.save(event);
    
    // Redirect
    return ResponseEntity.status(HttpStatus.FOUND)
        .location(URI.create(link.getDestinationUrl()))
        .build();
}
```

**Why This Order**: Tracking is essential for proving ROI to sellers.

---

### Step 13: RAG Search Strategy (Weeks 8-9)

**From Prompt**: Search internal DB first, fallback to Google Places, always cite sources
**Implementation**:

**Database**:
```sql
CREATE TABLE external_results_cache (
    id BIGSERIAL PRIMARY KEY,
    query_hash VARCHAR(64) NOT NULL,
    source VARCHAR(50), -- google_places, maps
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
```

**Search Service**:
```java
public SearchResult search(String query, Double lat, Double lon, int radius) {
    // Step 1: Search internal database
    List<Business> internal = businessRepository.findNearby(lat, lon, radius);
    
    if (internal.size() >= 3) {
        return SearchResult.builder()
            .results(internal)
            .source("data_alexia")
            .build();
    }
    
    // Step 2: Fallback to Google Places
    List<Place> external = googlePlacesService.searchNearby(query, lat, lon, radius);
    
    // Cache results
    external.forEach(place -> cacheExternalResult(place));
    
    // Step 3: Combine and cite sources
    return SearchResult.builder()
        .results(combineResults(internal, external))
        .source("mixed")
        .internalCount(internal.size())
        .externalCount(external.size())
        .build();
}
```

**Bot Response Format**:
```
🔍 Resultados para "proveedores de vasos":

📍 CERCA DE TI (base interna):
1. Plásticos Mayorista (2.3km)
2. Distribuidora Express (4.1km)

🌐 OTROS PROVEEDORES (web - Google Places):
3. Importadora Premium (8.5km)
   Fuente: Google Places
   Última actualización: hace 2 horas
```

**Why This Order**: RAG is the core intelligence of the prompt. Never invent data.

---

## 📋 Phase 3: Enhanced UX (Weeks 10-11)

### Step 14: WhatsApp Interactive Messages (Week 10)

**From Prompt**: Buttons, lists, interactive forms
**Implementation**:

**WhatsApp Templates** (create in Meta Business Manager):
1. `alexia_capture_name` - Quick reply buttons
2. `alexia_capture_email` - Email validation
3. `alexia_optin_privacy` - Consent buttons
4. `alexia_product_list` - Product selection list

**Java Implementation**:
```java
// Send interactive buttons
public void sendQuickReply(String chatId, String message, List<String> buttons) {
    InteractiveMessage interactive = new InteractiveMessage();
    interactive.setType("button");
    interactive.setBody(message);
    
    buttons.forEach(button -> {
        interactive.addButton(new Button()
            .setId(UUID.randomUUID().toString())
            .setTitle(button));
    });
    
    whatsAppService.sendInteractive(chatId, interactive);
}

// Send list menu
public void sendList(String chatId, String message, List<Product> products) {
    InteractiveMessage interactive = new InteractiveMessage();
    interactive.setType("list");
    interactive.setBody(message);
    
    products.forEach(product -> {
        interactive.addRow(new ListRow()
            .setId(product.getId().toString())
            .setTitle(product.getName())
            .setDescription(String.format("$%,.0f COP", product.getPrice())));
    });
    
    whatsAppService.sendInteractive(chatId, interactive);
}
```

**Why This Order**: Interactive messages dramatically improve UX. Build after core features work.

---

### Step 15: Business Hours Filter (Week 11)

**From Prompt**: "Open now", "open after 10pm" filters
**Implementation**:

**PostgreSQL Function**:
```sql
CREATE OR REPLACE FUNCTION is_open_now(
    hours JSONB,
    check_time TIMESTAMP WITH TIME ZONE
) RETURNS BOOLEAN AS $$
DECLARE
    day_name TEXT;
    current_time TIME;
    day_hours JSONB;
    period JSONB;
BEGIN
    -- Get day name (monday, tuesday, etc)
    day_name := LOWER(TO_CHAR(check_time, 'Day'));
    day_name := TRIM(day_name);
    
    -- Get current time
    current_time := check_time::TIME;
    
    -- Get hours for this day
    day_hours := hours->day_name;
    
    IF day_hours IS NULL OR jsonb_array_length(day_hours) = 0 THEN
        RETURN FALSE;
    END IF;
    
    -- Check if current time is within any period
    FOR period IN SELECT * FROM jsonb_array_elements(day_hours)
    LOOP
        IF current_time >= (period->>'open')::TIME 
           AND current_time <= (period->>'close')::TIME THEN
            RETURN TRUE;
        END IF;
    END LOOP;
    
    RETURN FALSE;
END;
$$ LANGUAGE plpgsql;
```

**Query Usage**:
```sql
-- Find businesses open now
SELECT * FROM businesses
WHERE is_open_now(business_hours, NOW() AT TIME ZONE 'America/Bogota')
  AND ST_DWithin(location, ST_Point(-74.0721, 4.7110)::geography, 3000);

-- Find businesses open after 10pm
SELECT * FROM businesses
WHERE is_open_now(business_hours, 
    (CURRENT_DATE + TIME '22:00:00') AT TIME ZONE 'America/Bogota');
```

**Why This Order**: Hours filter improves search quality significantly.

---

## 📋 Phase 4: Production Ready (Weeks 12-15)

### Step 16: WhatsApp Migration (Week 12)

**From Prompt**: WhatsApp Business Cloud API
**Implementation**: Migrate tested Telegram flows to WhatsApp

**Configuration**:
```bash
# .env
WHATSAPP_VERIFY_TOKEN=verify_token_here
WHATSAPP_PHONE_NUMBER_ID=1234567890
WHATSAPP_ACCESS_TOKEN=your_access_token
```

**Webhook**:
```java
@PostMapping("/webhooks/whatsapp")
public ResponseEntity<String> handleWebhook(@RequestBody WhatsAppWebhook webhook) {
    webhook.getEntry().forEach(entry -> {
        entry.getChanges().forEach(change -> {
            if (change.getValue().getMessages() != null) {
                change.getValue().getMessages().forEach(message -> {
                    processMessage(message);
                });
            }
        });
    });
    return ResponseEntity.ok("OK");
}
```

---

### Step 17: Save Alerts & Notifications (Week 13)

**From Prompt**: "Avísame cuando..." feature
**Implementation**:

**Database**:
```sql
CREATE TABLE alerts (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    alert_type VARCHAR(50),
    keywords TEXT[],
    category VARCHAR(100),
    max_distance_km INTEGER,
    min_discount_percent INTEGER,
    is_active BOOLEAN DEFAULT true,
    last_checked_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT NOW()
);
```

**Scheduled Job**:
```java
@Scheduled(fixedRate = 3600000) // Every hour
public void checkAlerts() {
    List<Alert> activeAlerts = alertRepository.findByIsActive(true);
    
    activeAlerts.forEach(alert -> {
        List<Match> matches = findMatches(alert);
        
        matches.forEach(match -> {
            sendNotification(alert.getUserId(), 
                "¡Alerta! " + formatMatch(match));
        });
    });
}
```

---

### Step 18: Analytics Dashboard (Week 14)

**From Prompt**: Metrics, charts, export
**Implementation**: Build comprehensive analytics view

---

### Step 19: CRM Integration (Week 15)

**From Prompt**: HighLevel connector
**Implementation**: Sync leads to HighLevel CRM

---

## 🎯 Key Design Principles

### 1. **Database Extensibility**
- Use JSONB for flexible fields (`variants`, `metadata`, `business_hours`)
- Add future fields now (even if unused): `google_place_id`, `owner_user_id`, `crm_sync_status`
- Use arrays for multi-value fields: `images[]`, `keywords[]`, `target_categories[]`

### 2. **Source Agnostic**
- Use `user_wa_id` (works for both Telegram and WhatsApp)
- Use `source` field to track origin (`telegram`, `whatsapp`, `web`)
- Design for multi-channel from start

### 3. **Geolocation First**
- Enable PostGIS in Step 9
- Add `location` field to businesses immediately
- Create spatial indexes
- Use in Step 11 (find buyers nearby)

### 4. **Monetization Ready**
- Separate `subscriptions` and `payments` tables
- Add `campaign_id` to payments
- Support multiple payment types
- Track all events in `ad_events`

### 5. **Never Invent Data**
- Always cite source (`data_alexia` vs `web`)
- Cache external results with TTL
- Show last updated timestamp
- Mark sponsored results clearly

---

## 📊 Timeline Summary

| Week | Step | Prompt Feature | Future Compatibility |
|------|------|----------------|---------------------|
| 1 | 8 | Product catalog | ✅ Flexible variants (JSONB) |
| 2 | 9 | Business + location | ✅ PostGIS, RBAC fields ready |
| 3 | 10 | Payments | ✅ Campaign billing ready |
| 4-5 | 11 | Lead capture | ✅ CRM sync fields ready |
| 6-7 | 12 | Campaigns + tracking | ✅ All event types supported |
| 8-9 | 13 | RAG search | ✅ Source citation built-in |
| 10 | 14 | Interactive messages | ✅ Works with Telegram too |
| 11 | 15 | Business hours filter | ✅ Timezone support |
| 12 | 16 | WhatsApp migration | ✅ Telegram stays as backup |
| 13 | 17 | Alerts | ✅ Proactive engagement |
| 14 | 18 | Analytics | ✅ All metrics tracked |
| 15 | 19 | CRM integration | ✅ Fields already in leads table |

**Total**: 15 weeks to full LOVABLE-PROMPT implementation

---

## ✅ Compatibility Checklist

Every step includes:
- [x] JSONB fields for future extensibility
- [x] Indexes for performance
- [ ] Source tracking (data_alexia vs web)
- [x] Multi-channel support (Telegram + WhatsApp) - Foundation laid.
- [ ] Geolocation ready (PostGIS)
- [ ] RBAC ready (owner_user_id fields)
- [ ] Campaign ready (campaign_id references)
- [ ] CRM ready (sync status fields)

**Result**: Implement prompt features first, but design allows adding ALL other planned features later without breaking changes.

---

**Document Purpose**: Guide implementation prioritizing LOVABLE-PROMPT while maintaining architectural flexibility for future enhancements.

**Next Step**: Start Step 8 (Product Catalog) with extensible design.
