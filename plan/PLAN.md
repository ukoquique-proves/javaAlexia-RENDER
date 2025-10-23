# Alexia - Development Plan

**Universal Business Assistant** - Help any seller find buyers, suppliers, and grow their business.

---

## 📌 Important: Implementation Order

**See `FIRST_STEPS_PROMPT.md` for detailed implementation strategy.**

This document prioritizes features from the original LOVABLE-PROMPT while designing for future extensibility. Key approach:
- Build prompt features first (lead capture, RAG search, campaigns)
- Design database with extensibility (JSONB fields, future columns added now)
- Ensure compatibility with all other planned features
- No breaking changes when adding future features

**Timeline**: 15 weeks following FIRST_STEPS_PROMPT.md approach.

---

## 🎯 Mission

Make sellers say **"¡Wow, esto es increíble!"** by showing them:
- Who needs their products nearby
- Where to buy cheaper
- How to capture leads automatically
- What's trending in their market

**Target Demo**: Plastic store owner (daily household items)

---

## 📊 Current Status

**Completed (Steps 1-12)**:
- ✅ Spring Boot + Vaadin dashboard
- ✅ PostgreSQL database (Render)
- ✅ Telegram bot with AI (Grok)
- ✅ Basic business search
- ✅ Deployed to production
- ✅ Product catalog (Step 8)
- ⚠️ Business management with geolocation (Step 9 - PostGIS disabled on Render)
- ⚠️ Find buyers nearby (Step 11 - PostGIS disabled on Render)
- ⚠️ Find suppliers & compare prices (Step 12 - JSONB disabled on Render)

**Next**: Step 13 - Lead Capture & Auto-Quotes

---

## 🚀 Phase 1: Foundation (Steps 8-10) - 3 weeks

Basic platform for any retail business.

### Step 8: Universal Product Catalog (1 week) ✅

**Status**: Completed

**For any business type**:
- Plastic store: cups, plates, containers, bags
- Fashion store: clothes, shoes, accessories
- Hardware store: tools, materials, supplies
- Food store: packaged goods, ingredients

**Features**:
- Product CRUD with images
- Multiple images per product
- Prices, stock, variants (sizes/colors)
- Categories (customizable per business)
- Search and filters

**Database**:
```sql
CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    business_id BIGINT REFERENCES businesses(id),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10,2),
    category VARCHAR(100),
    images TEXT[],
    variants JSONB, -- sizes, colors, etc
    stock INTEGER DEFAULT 0,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT NOW()
);
```

---

### Step 9: Universal Business Management (1 week) ⚠️

**Status**: Partially completed - PostGIS features disabled on Render free tier

**For any business type**:
- Business type: retail, wholesale, service, manufacturing
- Product categories: customizable
- Target customers: B2C, B2B, both
- Service area: local, regional, national

**Features**:
- Complete business CRUD
- Logo and cover images
- Social media links (Instagram, Facebook, WhatsApp)
- Business hours (flexible schedule)
- Geolocation (lat/long for future search)
- CSV bulk import
- Self-registration via Telegram

---

### Step 10: Payment & Subscriptions (1 week)

**Revenue model**:
- COP $400,000/month subscription
- 30 days free trial
- Automated billing via Mercado Pago

**Features**:
- Payment integration
- Subscription tracking
- Invoice generation
- Renewal reminders
- Usage tracking

---

## 🌟 Phase 2: Amazement Features (Steps 11-16) - 6 weeks

Features that make sellers say "¡Wow!"

### Step 11: Find Buyers Nearby (1.5 weeks) ⚠️🔥

**Status**: Implemented but disabled - PostGIS extension not available on Render free tier

**Voice**: *"Alexia, quién necesita vasos plásticos cerca de mi tienda?"*

**What it does**:
1. Analyzes seller's products
2. Identifies businesses that need those products
3. Shows distance, contact, best time to visit
4. Suggests sales approach

**Examples**:
- Plastic store → finds restaurants, caterers, event venues
- Fashion store → finds boutiques, influencers, events
- Hardware store → finds construction sites, contractors
- Food store → finds restaurants, hotels, cafeterias

**Tech**:
- PostGIS for geolocation
- Business category matching
- Distance calculation (radius search)
- Contact info extraction

**Database**:
```sql
-- Enable PostGIS
CREATE EXTENSION postgis;

-- Add location to businesses
ALTER TABLE businesses ADD COLUMN location GEOGRAPHY(POINT, 4326);
CREATE INDEX idx_businesses_location ON businesses USING GIST(location);

-- Search within radius
SELECT name, category, phone, whatsapp,
       ST_Distance(location, ST_Point(:my_long, :my_lat)::geography) as distance_m
FROM businesses
WHERE category IN ('restaurant', 'catering', 'events')
  AND ST_DWithin(location, ST_Point(:my_long, :my_lat)::geography, 3000)
ORDER BY distance_m;
```

**Demo Response**:
```
🎯 Encontré 12 negocios cerca que necesitan vasos:

1. 🍽️ Restaurante El Buen Sabor (500m)
   📞 +57 310 123 4567
   💡 Compran ~200 vasos/semana
   ⏰ Mejor hora: 10am-12pm

2. 🎉 Eventos La Fiesta (800m)
   📞 +57 320 987 6543
   💡 5-8 eventos/mes
```

---

### Step 12: Find Suppliers & Compare Prices (1.5 weeks) ⚠️🔥

**Status**: Implemented but JSONB fields disabled - Advanced PostgreSQL types not available on Render free tier

**Voice**: *"Alexia, busca proveedores de vasos con mejor precio"*

**What it does**:
1. Searches supplier database
2. Compares prices from multiple sources
3. Shows delivery times and minimums
4. Calculates savings vs current supplier

**Database**:
```sql
CREATE TABLE suppliers (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    category VARCHAR(100),
    products JSONB, -- [{name, price, unit, minimum}]
    delivery_time_days INTEGER,
    phone VARCHAR(50),
    email VARCHAR(255),
    website VARCHAR(500),
    location GEOGRAPHY(POINT, 4326),
    rating DECIMAL(3,2),
    is_verified BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT NOW()
);
```

**Demo Response**:
```
🏭 Encontré 8 proveedores de vasos:

💰 MEJOR PRECIO:
   Vaso 10oz: $45 COP/unidad
   Mínimo: 1000 unidades
   Entrega: 2-3 días
   
💡 Ahorro: $10,000 COP por pedido
```

---

### Step 13: Lead Capture & Auto-Quotes (2 weeks) ✅🔥

**Status**: Completed with GDPR/LGPD consent

**Scenario**: Customer sends message

**Customer**: *"Necesito 500 vasos para un evento el sábado"*

**What it does**:
1. Detects buying intent
2. Captures customer info (name, phone, event)
3. Checks inventory
4. Suggests complementary products
5. Calculates quote automatically
6. Notifies seller

**Database**:
```sql
CREATE TABLE leads (
    id BIGSERIAL PRIMARY KEY,
    business_id BIGINT REFERENCES businesses(id),
    customer_name VARCHAR(255),
    customer_phone VARCHAR(50),
    customer_email VARCHAR(255),
    source VARCHAR(50), -- telegram, whatsapp, web
    status VARCHAR(50) DEFAULT 'new', -- new, contacted, quoted, won, lost
    products_requested JSONB,
    quote_amount DECIMAL(10,2),
    notes TEXT,
    consent_given BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT NOW()
);
```

**Demo Response to Customer**:
```
¡Hola! Soy Alexia 👋

✅ 500 vasos disponibles
💰 $55 c/u = $27,500 total

💡 Paquete completo:
   🥤 Vasos (500) - $27,500
   🍽️ Platos (500) - $35,000
   🍴 Cubiertos (500) - $25,000
   📦 Total: $80,000 (ahorro $7,500)

¿Te interesa el paquete?
```

**Demo Notification to Seller**:
```
🎉 Nuevo Lead!

💰 Valor: $80,000 COP
📅 Evento: Sábado
🎯 Productos: Vasos + platos + cubiertos

[Ver detalles] [Contactar]
```

---

### Step 14: Inventory Intelligence (1 week) 🔥

**Proactive alerts** when stock runs low.

**Alert Example**:
```
⚠️ ALERTA: Vasos 10oz se agotarán en 4 días

Stock: 250 unidades
Ventas: 400/semana

💡 Sugerencia:
   Ordenar 2000 unidades
   Proveedor: Plásticos Mayorista
   Costo: $90,000 COP
   
[Ordenar ahora]
```

**Voice**: *"Alexia, qué debo reordenar?"*

**Database**:
```sql
CREATE TABLE inventory_alerts (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT REFERENCES products(id),
    current_stock INTEGER,
    avg_sales_per_week DECIMAL(10,2),
    days_until_stockout INTEGER,
    suggested_order_qty INTEGER,
    suggested_supplier_id BIGINT REFERENCES suppliers(id),
    status VARCHAR(50) DEFAULT 'pending',
    created_at TIMESTAMP DEFAULT NOW()
);
```

---

### Step 15: Voice Commands & Photo Recognition (1.5 weeks) 🔥

**Voice commands** (hands-free):
- *"Cuánto vendí hoy?"* → Shows daily sales
- *"Quién es mi mejor cliente?"* → Top customer
- *"Envía promoción de bolsas"* → Creates campaign
- *"Qué está de moda?"* → Trending products

**Photo recognition**:
1. Take photo of product
2. AI identifies type
3. Suggests name, category, price
4. Auto-generates description
5. Add to catalog with one click

**Tech**:
- Telegram voice message API
- OpenAI Whisper for speech-to-text
- OpenAI Vision API for image recognition
- Grok AI for product description

---

### Step 16: Market Intelligence (1 week) 🔥

**Voice**: *"Alexia, qué productos están de moda en mi zona?"*

**What it does**:
1. Analyzes search patterns in area
2. Identifies trending products
3. Shows demand growth
4. Suggests new product lines
5. Calculates ROI

**Demo Response**:
```
📊 TENDENCIAS EN TU ZONA:

🔥 MÁS BUSCADOS:
1. ♻️ Productos biodegradables (+180%)
2. 🥤 Vasos térmicos (+95%)
3. 🍱 Contenedores herméticos (+67%)

💡 OPORTUNIDAD:
   Agregar línea eco-friendly
   Inversión: $150,000 COP
   Demanda: 12 negocios cercanos
   ROI: 3-4 semanas
```

**Database**:
```sql
CREATE TABLE search_trends (
    id BIGSERIAL PRIMARY KEY,
    keyword VARCHAR(255),
    category VARCHAR(100),
    search_count INTEGER DEFAULT 1,
    location_city VARCHAR(100),
    trend_percentage DECIMAL(5,2),
    period_start DATE,
    period_end DATE,
    created_at TIMESTAMP DEFAULT NOW()
);
```

---

## 🎯 Phase 2B: Enhanced UX Features (Steps 16A-16C) - 2 weeks

Critical features from original LOVABLE-PROMPT that dramatically improve user experience.

### Step 16A: WhatsApp Interactive Messages (1 week) 🔥🔥🔥

**Priority**: 🔴 CRITICAL (Most amazing missing feature)

**What it does**:
Transforms UX from "typing everything" to "tap tap done"

**Interactive Types**:

1. **Quick Reply Buttons**
```
Bot: "¿Te interesa el paquete completo?"
Buttons: [✅ Sí] [❌ No] [💬 Más info]
```

2. **List Menus** (up to 10 items)
```
Bot: "Selecciona el producto:"
List:
  🥤 Vasos 10oz - $55
  🍽️ Platos 9" - $70
  🍴 Cubiertos set - $50
  [Ver más...]
```

3. **Interactive Forms**
```
Bot: "Para cotizar, necesito:"
Form:
  📝 Nombre: [____]
  📞 Teléfono: [____]
  📍 Ubicación: [Compartir]
  [Enviar]
```

4. **Location Request**
```
Bot: "¿Dónde está tu tienda?"
Button: [📍 Compartir ubicación]
```

**Templates to Create**:
- `alexia_capture_name` - Name with quick replies
- `alexia_capture_email` - Email validation
- `alexia_capture_phone` - Phone with format
- `alexia_optin_privacy` - Consent buttons
- `alexia_product_list` - Product selection
- `alexia_location_request` - One-tap location

**Tech**:
- WhatsApp Business API interactive messages
- Button types: `reply`, `list`, `location`
- Template approval in Meta Business Manager

**Demo Impact**: 
- Customer taps [Sí] instead of typing "si"
- Selects from list instead of typing product name
- Shares location with one tap
- **10x better UX than plain text**

---

### Step 16B: Business Hours + "Open Now" Filter (0.5 weeks) 🔥

**Priority**: 🔴 CRITICAL (Essential for search quality)

**What it does**:
Filter businesses by operating hours and current open status.

**Features**:
1. **Complex Schedule Support**
   - "Lunes-Viernes 9am-6pm, Sábados 10am-2pm"
   - "24 horas" / "Cerrado domingos"
   - Multiple time ranges per day

2. **Smart Filters**
   - "open now" (current time)
   - "open after 10pm" (specific time)
   - "open on Sunday" (specific day)
   - Timezone support (Colombia: UTC-5)

3. **Voice Queries**
   - *"Restaurantes abiertos ahora"*
   - *"Proveedores que abren después de las 8pm"*
   - *"Tiendas abiertas los domingos"*

**Database**:
```sql
ALTER TABLE businesses ADD COLUMN business_hours JSONB;

-- Example structure:
{
  "monday": [{"open": "09:00", "close": "18:00"}],
  "tuesday": [{"open": "09:00", "close": "18:00"}],
  "saturday": [{"open": "10:00", "close": "14:00"}],
  "sunday": [],
  "timezone": "America/Bogota",
  "is_24_7": false
}

-- Query for open now
SELECT * FROM businesses
WHERE is_open_now(business_hours, CURRENT_TIMESTAMP AT TIME ZONE 'America/Bogota');
```

**Demo Impact**:
- Find suppliers open late at night
- Discover 24-hour options
- Never show closed businesses

---

### Step 16C: Save Alerts & Notifications (0.5 weeks) 🔥

**Priority**: 🟡 High (Proactive engagement)

**What it does**:
AI remembers what customers need and notifies them automatically.

**Use Cases**:

1. **Price Alerts**
   - Customer: *"Avísame cuando haya vasos con descuento"*
   - System saves alert
   - When price drops 20%+ → notify customer

2. **Stock Alerts**
   - Customer: *"Avísame cuando tengas contenedores grandes"*
   - Product out of stock
   - When back in stock → notify customer

3. **New Supplier Alerts**
   - Seller: *"Avísame cuando aparezca proveedor de bolsas biodegradables"*
   - New supplier registers
   - Matches criteria → notify seller

4. **Opportunity Alerts**
   - Seller: *"Avísame cuando haya eventos cerca que necesiten platos"*
   - Event venue posts need
   - Within 5km → notify seller

**Database**:
```sql
CREATE TABLE alerts (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    alert_type VARCHAR(50), -- price_drop, stock_available, new_supplier, opportunity
    keywords TEXT[],
    category VARCHAR(100),
    max_distance_km INTEGER,
    min_discount_percent INTEGER,
    is_active BOOLEAN DEFAULT true,
    last_checked_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE alert_notifications (
    id BIGSERIAL PRIMARY KEY,
    alert_id BIGINT REFERENCES alerts(id),
    triggered_by_id BIGINT, -- product_id, business_id, etc
    message TEXT,
    sent_at TIMESTAMP DEFAULT NOW()
);
```

**Demo Impact**:
- Proactive engagement (AI reaches out first)
- Never miss opportunities
- Customers feel cared for

---

## 🎯 Phase 3: Production (Steps 17-19) - 4 weeks

### Step 17: WhatsApp Integration (2 weeks)

Migrate from Telegram to WhatsApp for production.

**Features**:
- WhatsApp Business API (Meta)
- Interactive templates (buttons, lists)
- Location sharing
- Media messages (images, voice)
- Keep Telegram as secondary channel

---

### Step 18: Campaign System (1 week)

**Campaign types**:
- Promotional discount (20% OFF)
- New product launch
- Seasonal sale
- Targeted to customer segments

**Features**:
- Campaign CRUD
- Link tracking
- Event tracking (impressions, clicks, leads)
- Analytics per campaign

---

### Step 19: Analytics Dashboard (1 week)

**Metrics**:
- Daily/weekly/monthly sales
- Top products and customers
- Lead conversion rate
- Revenue trends
- Inventory turnover

**Charts**:
- Sales over time
- Product performance
- Customer segments
- Geographic distribution

---

## 📋 Timeline Summary

| Phase | Steps | Duration | Deliverable |
|-------|-------|----------|-------------|
| **Phase 1** | 8-10 | 3 weeks | Basic platform |
| **Phase 2** | 11-16 | 6 weeks | Amazement features |
| **Phase 2B** | 16A-16C | 2 weeks | Enhanced UX |
| **Phase 3** | 17-19 | 4 weeks | Production ready |
| **TOTAL** | | **15 weeks** | **~4 months** |

---

## 🎯 Demo Milestones

### Week 3: Basic Demo
- Product catalog working
- Business management
- Payment system

### Week 9: "WOW" Demo 🔥
- Find buyers nearby
- Find suppliers
- Lead capture with auto-quotes
- Inventory alerts
- Voice commands
- Market intelligence
- **Interactive buttons** (tap instead of type)
- **Business hours filter** (open now)
- **Save alerts** (proactive notifications)

**This is the demo for plastic store owner!**

### Week 11: Enhanced UX
- WhatsApp interactive messages (buttons, lists)
- Business hours + "open now" filter
- Save alerts & notifications

### Week 15: Production Launch
- WhatsApp integration complete
- Campaign system
- Full analytics
- Ready for market

---

## 🎬 Demo Script (10 minutes)

**1. Introduction (1 min)**
"María, imagina tener un asistente que trabaja 24/7 para ti..."

**2. Find Buyers (2 min)**
Voice: *"Alexia, quién necesita platos cerca?"*
→ Shows 12 restaurants within 3km

**3. Find Suppliers (2 min)**
Voice: *"Alexia, busca proveedores de vasos"*
→ Compares 8 suppliers, shows savings

**4. Lead Capture (2 min)**
Simulate customer: *"Necesito 500 vasos"*
→ Bot captures info, quotes automatically

**5. Inventory Alert (1 min)**
Shows proactive alert: *"Vasos se agotan en 4 días"*
→ Suggests reorder with supplier

**6. Voice Commands (1 min)**
Voice: *"Cuánto vendí hoy?"*
→ Shows sales instantly

**7. Market Intelligence (1 min)**
Voice: *"Qué está de moda?"*
→ Shows biodegradable products trending

**Closing**: "Todo desde tu teléfono, con tu voz, mientras atiendes clientes. ¿Te imaginas cuánto más podrías vender?"

---

## 💡 Key Principles

1. **Universal** - Works for any retail business
2. **Voice-First** - Manage while working (hands-free)
3. **Proactive** - AI suggests, doesn't just respond
4. **Amazement** - Every feature must impress
5. **Simple** - Complex tech, simple UX
6. **ROI-Focused** - Features must increase sales or save time

---

## 🚀 Next Steps

**Start**: Step 8 - Universal Product Catalog
**Goal**: Reach Week 9 demo (amazement features)
**Target**: Make plastic store owner a customer

---

**See also**:
- `SELLER_FEATURES.md` - Detailed feature descriptions with examples
- `PLAN_TECHNICAL.md` - Complete technical roadmap (22 steps)
- `NEXT_STEPS.md` - Original step-by-step plan
