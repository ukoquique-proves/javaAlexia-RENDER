# Lead Capturing System - Alexia

**Version**: 1.0.0  
**Date**: 2025-10-21  
**Status**: Production Ready ✅

---

## 📋 Overview

The Lead Capturing System is a comprehensive GDPR/LGPD-compliant solution for capturing, managing, and tracking potential customers (leads) across multiple channels including Telegram, WhatsApp, Web, and organic sources. The system ensures explicit user consent before storing any personal information and provides a complete lifecycle management interface.

---

## 🎯 Key Features

### 1. **Multi-Channel Lead Capture**
Capture leads from various sources:
- **Telegram**: Automated bot conversations with consent flow
- **WhatsApp**: Integration ready (same structure as Telegram)
- **Web**: Direct form submissions
- **Organic**: Referrals and walk-ins
- **Data Alexia**: Internal data sources

### 2. **GDPR/LGPD Compliance**
Full compliance with data protection regulations:
- ✅ **Explicit Consent**: Users must explicitly agree before data storage
- ✅ **Consent Timestamp**: Records exact date/time of consent
- ✅ **Consent Validation**: Database constraints ensure consent integrity
- ✅ **Right to be Forgotten**: Soft delete (archive) and hard delete options
- ✅ **Data Transparency**: Users can view their stored information

### 3. **Lead Lifecycle Management**
Complete status tracking through the sales funnel:
- **New**: Initial contact, not yet engaged
- **Contacted**: First interaction completed
- **Qualified**: Meets criteria, potential customer
- **Converted**: Successfully became a customer
- **Lost**: Did not convert, opportunity closed
- **Archived**: Soft-deleted, can be restored

### 4. **CRM Integration Ready**
Prepared for future CRM system integration:
- `crm_sync_status`: Track synchronization state
- `crm_contact_id`: External CRM contact reference
- `crm_opportunity_id`: External CRM opportunity reference

---

## 🏗️ Architecture

### Database Schema

```sql
CREATE TABLE leads (
    id BIGSERIAL PRIMARY KEY,
    business_id BIGINT NOT NULL REFERENCES businesses(id),
    user_wa_id VARCHAR(50),              -- WhatsApp/Telegram ID
    campaign_id BIGINT,                  -- Future campaigns feature
    source VARCHAR(50) NOT NULL,         -- Channel origin
    status VARCHAR(50) NOT NULL,         -- Lifecycle stage
    
    -- Contact Information
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100),
    phone VARCHAR(50),
    email VARCHAR(255),
    city VARCHAR(100),
    country VARCHAR(50) DEFAULT 'CO',
    
    -- Business Fields
    value_estimated DECIMAL(10,2),       -- Estimated deal value
    notes TEXT,                          -- Internal notes
    
    -- GDPR/LGPD Compliance
    consent_given BOOLEAN NOT NULL DEFAULT false,
    consent_date TIMESTAMP,
    
    -- CRM Integration
    crm_sync_status VARCHAR(50),
    crm_contact_id VARCHAR(100),
    crm_opportunity_id VARCHAR(100),
    
    -- Timestamps
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);
```

### Constraints & Validation

**Database Level:**
```sql
-- At least one contact method required
CHECK (phone IS NOT NULL OR email IS NOT NULL)

-- Valid status values
CHECK (status IN ('new', 'contacted', 'qualified', 'converted', 'lost', 'archived'))

-- Valid source values
CHECK (source IN ('telegram', 'whatsapp', 'web', 'organic', 'data_alexia'))

-- Consent date must be set if consent is given
CHECK ((consent_given = false AND consent_date IS NULL) OR 
       (consent_given = true AND consent_date IS NOT NULL))
```

**Application Level (LeadValidator):**
- Name validation (2-100 characters, letters only)
- Contact method presence validation
- Consent validation (critical for GDPR/LGPD)
- Source and status enum validation
- User WhatsApp/Telegram ID format validation

### Performance Optimization

**Indexes:**
```sql
CREATE INDEX idx_leads_business_id ON leads(business_id);
CREATE INDEX idx_leads_status ON leads(status);
CREATE INDEX idx_leads_user_wa_id ON leads(user_wa_id);
CREATE INDEX idx_leads_source ON leads(source);
CREATE INDEX idx_leads_created_at ON leads(created_at);
CREATE INDEX idx_leads_consent_given ON leads(consent_given);
```

---

## 💻 Backend Components

### 1. Lead Entity (`Lead.java`)

**Key Features:**
- Lombok annotations for boilerplate reduction
- JPA annotations for ORM mapping
- Validation annotations (@NotNull, @Size, @Email)
- Helper methods for business logic

**Helper Methods:**
```java
public String getFullName()           // Returns "FirstName LastName"
public boolean hasContactMethod()     // Checks if phone or email exists
public boolean isActive()             // Checks if status is not 'archived'
```

### 2. LeadRepository (`LeadRepository.java`)

**20+ Query Methods:**

**By Business:**
```java
List<Lead> findByBusinessId(Long businessId)
List<Lead> findByBusinessIdAndStatus(Long businessId, String status)
```

**By Status:**
```java
List<Lead> findByStatus(String status)
List<Lead> findByStatusIn(List<String> statuses)
List<Lead> findByStatusNot(String status)  // Active leads
```

**By Source:**
```java
List<Lead> findBySource(String source)
List<Lead> findByBusinessIdAndSource(Long businessId, String source)
```

**By User ID:**
```java
Optional<Lead> findByUserWaId(String userWaId)
List<Lead> findByBusinessIdAndUserWaId(Long businessId, String userWaId)
```

**Search & Filter:**
```java
List<Lead> findByFirstNameContainingIgnoreCase(String firstName)
List<Lead> findByEmailContainingIgnoreCase(String email)
List<Lead> findByPhoneContaining(String phone)
```

**Date Ranges:**
```java
List<Lead> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end)
List<Lead> findByConsentDateBetween(LocalDateTime start, LocalDateTime end)
```

**GDPR Compliance:**
```java
List<Lead> findByConsentGiven(Boolean consentGiven)
List<Lead> findByBusinessIdAndConsentGiven(Long businessId, Boolean consentGiven)
```

**CRM Integration:**
```java
List<Lead> findByCrmSyncStatus(String status)
List<Lead> findByCrmSyncStatusIsNull()
```

**Analytics:**
```java
long countByBusinessId(Long businessId)
long countByBusinessIdAndStatus(Long businessId, String status)
long countBySource(String source)
```

### 3. LeadService (`LeadService.java`)

**Core Operations:**

**Create:**
```java
Lead createLead(Lead lead)
// - Validates using LeadValidator
// - Sets timestamps automatically
// - Ensures consent date if consent given
```

**Update:**
```java
Lead updateLead(Long id, Lead lead)
Lead updateLeadStatus(Long id, String newStatus)
Lead updateConsent(Long id, Boolean consentGiven)
```

**Delete:**
```java
void deleteLead(Long id)              // Hard delete
void archiveLead(Long id)             // Soft delete (status = 'archived')
```

**Query Methods:**
All repository methods are exposed through the service layer with additional business logic and error handling.

### 4. LeadValidator (`LeadValidator.java`)

**Validation Rules:**

**Name Validation:**
```java
- First name: Required, 2-100 characters, letters only
- Last name: Optional, 2-100 characters, letters only
```

**Contact Validation:**
```java
- At least one contact method required (phone OR email)
- Email format validation if provided
- Phone format validation if provided
```

**Consent Validation:**
```java
- consent_given must be explicitly set
- consent_date required if consent_given = true
- consent_date must be null if consent_given = false
```

**Business Rules:**
```java
- Valid status values only
- Valid source values only
- user_wa_id format validation (if provided)
```

---

## 🎨 Frontend - LeadsView Dashboard

### Features

**Grid Display:**
- ID, Name, Phone, Email, Status, Source, Consent, Creation Date
- Color-coded status badges (primary, success, error, contrast)
- Consent indicators with visual badges (✓ Sí / ✗ No)
- Sortable columns
- Responsive layout

**Search & Filters:**
- **Search**: By name, email, or phone (real-time)
- **Status Filter**: All, new, contacted, qualified, converted, lost, archived
- **Source Filter**: All, telegram, whatsapp, web, organic, data_alexia
- **Refresh Button**: Manual data reload

**Actions:**
- **View Details**: Full lead information in dialog
- **Edit Status**: Update lead status and notes
- **Status Translation**: Spanish translations for better UX

**Detail View Dialog:**
Shows complete lead information:
- Personal info (name, phone, email, city, country)
- Status and source
- Consent status and date
- Creation and update timestamps
- Internal notes

**Edit Dialog:**
- Status dropdown with Spanish translations
- Notes text area (150px height)
- Save/Cancel buttons
- Success/Error notifications

---

## 🤖 Telegram Bot Integration

### Lead Capture Flow

**1. Intent Detection:**
```java
GrokIntent intent = grokService.detectIntent(messageText);
if (intent.getIntent() == IntentType.LEAD_CAPTURE) {
    handleLeadCapture(chatId, user, intent);
}
```

**2. Existing Lead Check:**
```java
Optional<Lead> existingLead = leadService.getLeadByUserWaId(String.valueOf(chatId));
if (existingLead.isPresent()) {
    // Show current lead information
    // Offer update options
}
```

**3. Consent Request (New Leads):**
```
👋 ¡Hola [Name]! Para brindarte el mejor servicio necesitamos tu consentimiento.

✅ ¿Estás de acuerdo con que almacenemos tu información de contacto para:
• Enviarte recomendaciones personalizadas
• Informarte sobre nuevos productos y negocios
• Contactarte para ofertas especiales

Responde 'Sí, acepto' para continuar o 'No, gracias' para cancelar.

🔒 Cumplimos con las normas GDPR/LGPD para proteger tu privacidad.
```

**4. Lead Creation (After Consent):**
```java
Lead lead = Lead.builder()
    .businessId(1L)
    .userWaId(String.valueOf(chatId))
    .source("telegram")
    .firstName(intent.getFirstName())
    .lastName(intent.getLastName())
    .phone(intent.getPhone())
    .email(intent.getEmail())
    .city(intent.getCity())
    .consentGiven(true)
    .consentDate(LocalDateTime.now())
    .build();

Lead savedLead = leadService.createLead(lead);
```

**5. Confirmation Message:**
```
🎉 ¡Perfecto [Name]! Hemos registrado tu información.

📋 Resumen de tu lead:
👤 Nombre: [Full Name]
📞 Teléfono: [Phone]
📧 Email: [Email]
🏙️ Ciudad: [City]
✅ Consentimiento: Otorgado
📅 Fecha: [DateTime]

💡 Ahora recibirás recomendaciones personalizadas y ofertas especiales.
```

### GrokIntent Extension

**New Fields for Lead Capture:**
```java
public class GrokIntent {
    // Existing fields
    private IntentType intent;
    private String searchTerm;
    private double confidence;
    
    // Lead capture fields
    private Boolean hasConsent;
    private String firstName;
    private String lastName;
    private String phone;
    private String email;
    private String city;
}
```

**New Intent Type:**
```java
public enum IntentType {
    PRODUCT_SEARCH,
    BUSINESS_SEARCH,
    LEAD_CAPTURE,    // NEW
    GENERAL_QUERY
}
```

---

## 📊 Usage Examples

### Creating a Lead Programmatically

```java
@Autowired
private LeadService leadService;

public void createNewLead() {
    Lead lead = Lead.builder()
        .businessId(1L)
        .userWaId("123456789")
        .source("web")
        .status("new")
        .firstName("Carlos")
        .lastName("Rodríguez")
        .phone("+57 300 1234567")
        .email("carlos@example.com")
        .city("Bogotá")
        .country("CO")
        .consentGiven(true)
        .consentDate(LocalDateTime.now())
        .notes("Interested in biodegradable products")
        .build();
    
    Lead savedLead = leadService.createLead(lead);
    System.out.println("Lead created with ID: " + savedLead.getId());
}
```

### Querying Leads

```java
// Get all new leads for a business
List<Lead> newLeads = leadService.getLeadsByBusinessIdAndStatus(1L, "new");

// Get leads by source
List<Lead> telegramLeads = leadService.getLeadsBySource("telegram");

// Search by name
List<Lead> leads = leadService.searchLeadsByName("Carlos");

// Get leads without consent
List<Lead> noConsentLeads = leadService.getLeadsByConsentGiven(false);

// Get leads created in last 7 days
LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
List<Lead> recentLeads = leadService.getLeadsCreatedAfter(weekAgo);
```

### Updating Lead Status

```java
// Update status
leadService.updateLeadStatus(leadId, "contacted");

// Update consent
leadService.updateConsent(leadId, true);

// Archive lead (soft delete)
leadService.archiveLead(leadId);

// Hard delete
leadService.deleteLead(leadId);
```

---

## 🔒 GDPR/LGPD Compliance Details

### Data Protection Principles

**1. Lawfulness, Fairness, and Transparency**
- ✅ Explicit consent request before data collection
- ✅ Clear explanation of data usage
- ✅ Transparent storage and processing

**2. Purpose Limitation**
- ✅ Data collected only for specified purposes
- ✅ Purpose clearly communicated to user
- ✅ No secondary use without additional consent

**3. Data Minimization**
- ✅ Only essential data collected
- ✅ Optional fields for non-critical information
- ✅ No excessive data storage

**4. Accuracy**
- ✅ Users can update their information
- ✅ Validation ensures data quality
- ✅ Timestamps track data freshness

**5. Storage Limitation**
- ✅ Soft delete (archive) for inactive leads
- ✅ Hard delete option available
- ✅ Retention policies can be implemented

**6. Integrity and Confidentiality**
- ✅ Database constraints ensure data integrity
- ✅ Validation at multiple levels
- ✅ Secure storage in PostgreSQL

**7. Accountability**
- ✅ Consent timestamp recorded
- ✅ Audit trail via created_at/updated_at
- ✅ Source tracking for compliance reporting

### User Rights Implementation

**Right to Access:**
```java
// Users can view their data via Telegram bot
Optional<Lead> lead = leadService.getLeadByUserWaId(userWaId);
// Display all stored information
```

**Right to Rectification:**
```java
// Users can update their information
leadService.updateLead(leadId, updatedLead);
```

**Right to Erasure (Right to be Forgotten):**
```java
// Soft delete (archive)
leadService.archiveLead(leadId);

// Hard delete (permanent removal)
leadService.deleteLead(leadId);
```

**Right to Restrict Processing:**
```java
// Archive lead to stop active processing
leadService.updateLeadStatus(leadId, "archived");
```

**Right to Data Portability:**
```java
// Export lead data (can be implemented)
Lead lead = leadService.getLeadById(leadId);
// Convert to JSON/CSV for export
```

---

## 🚀 Future Enhancements

### Planned Features

**1. Campaign Integration**
- Link leads to specific marketing campaigns
- Track campaign effectiveness
- ROI calculation per campaign

**2. CRM Synchronization**
- Automatic sync with external CRM systems
- Bi-directional data flow
- Conflict resolution strategies

**3. Lead Scoring**
- Automatic lead quality scoring
- Prioritization based on engagement
- Predictive conversion probability

**4. Email Integration**
- Automated email sequences
- Drip campaigns for nurturing
- Email open/click tracking

**5. WhatsApp Integration**
- Direct WhatsApp Business API integration
- Similar flow to Telegram bot
- Multi-channel conversation history

**6. Analytics Dashboard**
- Conversion funnel visualization
- Source performance metrics
- Time-to-conversion analysis
- Revenue attribution

**7. Advanced Filtering**
- Custom filter combinations
- Saved filter presets
- Export filtered results

**8. Bulk Operations**
- Bulk status updates
- Bulk assignment to campaigns
- Bulk export/import

---

## 📈 Metrics & KPIs

### Key Performance Indicators

**Lead Generation:**
- Total leads captured
- Leads per source
- Leads per day/week/month
- Consent rate (% who give consent)

**Lead Quality:**
- Conversion rate by source
- Average time to conversion
- Lead score distribution
- Contact method completeness

**Engagement:**
- Response time to new leads
- Status progression speed
- Notes per lead (engagement indicator)
- Re-engagement rate for lost leads

**Compliance:**
- Consent compliance rate (100% target)
- Data completeness
- Archive rate
- Deletion requests handled

---

## 🛠️ Maintenance & Operations

### Database Maintenance

**Regular Tasks:**
```sql
-- Clean up old archived leads (after retention period)
DELETE FROM leads 
WHERE status = 'archived' 
AND updated_at < NOW() - INTERVAL '2 years';

-- Analyze table for query optimization
ANALYZE leads;

-- Reindex for performance
REINDEX TABLE leads;
```

**Monitoring:**
```sql
-- Check consent compliance
SELECT COUNT(*) as total,
       SUM(CASE WHEN consent_given THEN 1 ELSE 0 END) as with_consent,
       SUM(CASE WHEN NOT consent_given THEN 1 ELSE 0 END) as without_consent
FROM leads
WHERE status != 'archived';

-- Check lead distribution by source
SELECT source, COUNT(*) as count
FROM leads
GROUP BY source
ORDER BY count DESC;

-- Check conversion funnel
SELECT status, COUNT(*) as count
FROM leads
GROUP BY status
ORDER BY CASE status
    WHEN 'new' THEN 1
    WHEN 'contacted' THEN 2
    WHEN 'qualified' THEN 3
    WHEN 'converted' THEN 4
    WHEN 'lost' THEN 5
    WHEN 'archived' THEN 6
END;
```

### Backup Strategy

**Recommended:**
- Daily automated backups
- Point-in-time recovery enabled
- Backup retention: 30 days minimum
- Test restore procedures monthly

---

## 📚 API Reference

### REST Endpoints (Future)

While currently integrated into the dashboard, these endpoints can be exposed:

```
GET    /api/leads                    # List all leads
GET    /api/leads/{id}               # Get lead by ID
POST   /api/leads                    # Create new lead
PUT    /api/leads/{id}               # Update lead
DELETE /api/leads/{id}               # Delete lead
PATCH  /api/leads/{id}/status        # Update status
PATCH  /api/leads/{id}/consent       # Update consent

GET    /api/leads/business/{id}      # Get leads by business
GET    /api/leads/source/{source}    # Get leads by source
GET    /api/leads/status/{status}    # Get leads by status
GET    /api/leads/search?q={query}   # Search leads
```

---

## 🔗 Related Documentation

- **Database Management**: `plan/DATABASE_MANAGEMENT_STRATEGY.md`
- **Technical Plan**: `plan/PLAN_TECHNICAL.md`
- **First Steps**: `plan/FIRST_STEPS_PROMPT.md`
- **Changelog**: `CHANGELOG.md`
- **Deployment**: `deployment/RENDER.md`, `deployment/SUPABASE.md`

---

## 📝 Changelog

**Version 1.0.0** (2025-10-21)
- ✅ Initial release
- ✅ Complete GDPR/LGPD compliance
- ✅ Multi-channel support
- ✅ Telegram bot integration
- ✅ Dashboard with full CRUD operations
- ✅ 20+ query methods
- ✅ Comprehensive validation
- ✅ Production-ready

---

**Maintained By**: Alexia Development Team  
**Last Updated**: 2025-10-21  
**Status**: Active & Production Ready ✅
