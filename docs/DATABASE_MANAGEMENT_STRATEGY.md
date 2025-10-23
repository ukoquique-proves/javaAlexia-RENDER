# Database Management Strategy - Alexia Project

**Date**: 2025-10-20  
**Status**: Active Strategy

---

## 🎯 Overview

This document explains how Alexia manages database schema changes and migrations without using automatic tools like Flyway or Liquibase. Instead, we use a **manual, controlled approach** that provides full visibility and control over database changes.

---

## 📋 Current Approach: Manual SQL Migrations

### Why Manual Migrations?

1. **Full Control**: Complete visibility into every database change
2. **No Magic**: No hidden migrations or automatic schema generation
3. **Production Safety**: Explicit approval required for each change
4. **Supabase Compatibility**: Works perfectly with Supabase's SQL Editor
5. **Simple & Transparent**: Easy to understand and audit
6. **No Dependencies**: No additional libraries or frameworks needed

### How It Works

#### Step 1: Create Migration SQL File
Each feature/step gets its own SQL migration file:

```
database/
├── step2_connection_test.sql
├── step3_telegram_messages.sql
├── step9_business_enhancements.sql
├── step11_leads_table.sql
└── ...
```

#### Step 2: Write Idempotent SQL
All migrations use `IF NOT EXISTS` or `IF EXISTS` to be safely re-runnable:

```sql
-- Safe to run multiple times
CREATE TABLE IF NOT EXISTS leads (...);
CREATE INDEX IF NOT EXISTS idx_leads_business_id ON leads(business_id);
ALTER TABLE leads ADD COLUMN IF NOT EXISTS new_field VARCHAR(100);
```

#### Step 3: Include Constraints & Validation
Add database-level validation to complement application validation:

```sql
-- Ensure data integrity at database level
ALTER TABLE leads ADD CONSTRAINT check_contact_method 
    CHECK (phone IS NOT NULL OR email IS NOT NULL);

ALTER TABLE leads ADD CONSTRAINT check_status 
    CHECK (status IN ('new', 'contacted', 'qualified', 'converted', 'lost', 'archived'));
```

#### Step 4: Add Test Data
Include sample data for development and testing:

```sql
INSERT INTO leads (business_id, first_name, ...) 
VALUES (1, 'Carlos', ...) 
ON CONFLICT DO NOTHING; -- Safe for re-runs
```

#### Step 5: Add Documentation
Include comments and notices:

```sql
COMMENT ON TABLE leads IS 'Tabla de leads capturados con consentimiento GDPR/LGPD';
COMMENT ON COLUMN leads.consent_given IS 'Consentimiento explícito del usuario';

DO $$
BEGIN
    RAISE NOTICE '✅ Step 11: Leads table created successfully';
END $$;
```

---

## 🔧 Execution Methods

### Method 1: Supabase SQL Editor (Recommended for Production)

**Advantages**:
- ✅ Web-based, no local tools needed
- ✅ Query history automatically saved
- ✅ Syntax highlighting and validation
- ✅ Direct connection to production database
- ✅ Can save queries as snippets

**Steps**:
1. Open Supabase Dashboard → SQL Editor
2. Copy migration SQL from `database/stepXX_*.sql`
3. Paste into editor
4. Review the SQL carefully
5. Click "Run"
6. Verify success messages

### Method 2: psql Command Line

**Advantages**:
- ✅ Scriptable and automatable
- ✅ Can be integrated into CI/CD
- ✅ Works offline with local database

**Steps**:
```bash
# Using connection string from .env
psql "postgresql://postgres:PASSWORD@HOST:PORT/DATABASE" -f database/step11_leads_table.sql

# Or using our helper script
./scripts/run_migration_step11.sh
```

### Method 3: Supabase API (Programmatic Execution)

**Using Supabase APIs to run migrations programmatically**:

#### Option A: Supabase Management API

Execute SQL via REST API:

```bash
curl -X POST 'https://api.supabase.com/v1/projects/{project-ref}/database/query' \
  -H "Authorization: Bearer ${SUPABASE_SERVICE_ROLE_KEY}" \
  -H "Content-Type: application/json" \
  -d '{
    "query": "CREATE TABLE IF NOT EXISTS leads (...)"
  }'
```

**Pros**:
- ✅ RESTful API, language-agnostic
- ✅ Can be called from any tool/script
- ✅ Supabase handles connection pooling

**Cons**:
- ❌ Requires Management API key (separate from database credentials)
- ❌ Additional API to manage
- ❌ Network overhead for each query

#### Option B: Supabase Client Libraries

Use official Supabase clients (JavaScript, Python, etc.):

```javascript
const { createClient } = require('@supabase/supabase-js')
const supabase = createClient(SUPABASE_URL, SUPABASE_KEY)

// Execute raw SQL via RPC function
const { data, error } = await supabase.rpc('exec_sql', {
  sql: 'CREATE TABLE IF NOT EXISTS leads (...)'
})
```

**Pros**:
- ✅ Official SDK with good documentation
- ✅ Handles authentication automatically
- ✅ Works well in Node.js/Python environments

**Cons**:
- ❌ Requires JavaScript/Python runtime
- ❌ Not native to Java/Spring Boot
- ❌ Need to create custom RPC function in Supabase

#### Option C: Direct JDBC Connection (Already Configured)

Execute migrations via Java JDBC using existing database connection:

```java
@Component
public class MigrationRunner implements ApplicationRunner {
    
    @Autowired
    private DataSource dataSource;
    
    @Value("${migration.enabled:false}")
    private boolean migrationEnabled;
    
    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (!migrationEnabled) {
            return; // Skip in production
        }
        
        log.info("Running database migrations...");
        executeMigrationFile("classpath:db/migration/step11_leads_table.sql");
    }
    
    private void executeMigrationFile(String filePath) throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            String sql = readSqlFile(filePath);
            
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(sql);
                log.info("Migration executed successfully: {}", filePath);
            }
        } catch (SQLException e) {
            log.error("Migration failed: {}", filePath, e);
            throw e;
        }
    }
    
    private String readSqlFile(String filePath) throws IOException {
        Resource resource = new ClassPathResource(filePath);
        return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    }
}
```

**Configuration** (`application.properties`):
```properties
# Enable migrations only in development
migration.enabled=false  # Set to true only in dev environment
```

**Pros**:
- ✅ Uses existing database connection (already in `.env`)
- ✅ No additional API keys or dependencies
- ✅ Native Java/Spring Boot solution
- ✅ Can read SQL files from classpath
- ✅ Full control over execution logic

**Cons**:
- ❌ Requires custom code to maintain
- ❌ No built-in version tracking (unlike Flyway)
- ❌ Need to implement error handling
- ❌ Must handle transaction management

### Method 4: Spring Boot Application Startup (Custom Implementation)

**Similar to Method 3 Option C, but more basic**:
- Use `@PostConstruct` or `ApplicationRunner`
- Execute SQL from classpath resources
- Only for development environments
- Would require additional error handling

**This is essentially a simpler version of Method 3 Option C without the full migration runner infrastructure.**

---

## 🔍 Comparison: Which Method to Use?

| Method | Complexity | Control | Production Ready | Recommendation |
|--------|-----------|---------|------------------|----------------|
| **Method 1: Supabase SQL Editor** | Low | Full | ✅ Yes | ✅ **Current choice** |
| **Method 2: psql CLI** | Low | Full | ✅ Yes | ✅ Good for scripts |
| **Method 3A: Supabase Management API** | Medium | Medium | ⚠️ Requires API key | ⚠️ Overkill for this project |
| **Method 3B: Supabase Client SDK** | Medium | Medium | ⚠️ Requires JS/Python | ❌ Not Java-native |
| **Method 3C: JDBC (Custom)** | Medium | Full | ⚠️ Needs safety checks | ⚠️ Consider for future |
| **Method 4: Spring Boot Startup** | Medium | Full | ❌ Risky | ❌ Not recommended |
| **Flyway/Liquibase** | Low | Medium | ✅ Yes | ⚠️ Future option |

### Current Recommendation: **Method 1 (Supabase SQL Editor)**

**Why**:
- ✅ Visual interface with syntax highlighting
- ✅ Query history automatically saved
- ✅ No code to maintain
- ✅ Safe: review before execution
- ✅ Works perfectly with Supabase
- ✅ No additional dependencies or API keys

**When to Switch**:
- Team grows and needs automated deployments
- Multiple environments need frequent syncing
- CI/CD pipeline requires automated migrations
- Manual execution becomes too tedious

---

## 🏗️ JPA/Hibernate Configuration

### Current Strategy: `ddl-auto=validate`

**Configuration** (`application.properties`):
```properties
spring.jpa.hibernate.ddl-auto=validate
```

**What This Means**:
- ✅ Hibernate **validates** entity mappings against database schema
- ✅ Application **fails to start** if schema doesn't match entities
- ❌ Hibernate **does NOT create or modify** tables automatically
- ❌ Hibernate **does NOT drop** tables

**Why This Is Safe**:
1. **No Surprises**: Hibernate never modifies your database
2. **Early Detection**: Schema mismatches caught at startup
3. **Explicit Changes**: All changes must be done via SQL migrations
4. **Production Ready**: Safe for production environments

### Alternative Options (Not Used)

| Option | Behavior | Use Case |
|--------|----------|----------|
| `create` | Drop and recreate schema on startup | ❌ Never use |
| `create-drop` | Create on startup, drop on shutdown | ❌ Never use |
| `update` | Auto-update schema (dangerous) | ❌ Avoid in production |
| `validate` | Validate only, no changes | ✅ **Current choice** |
| `none` | Do nothing | ⚠️ No validation |

---

## 📊 Migration Workflow

### Development Workflow

```
1. Design Feature
   ↓
2. Create Entity Class (Java)
   ↓
3. Create Migration SQL File
   ↓
4. Run Migration on Dev Database
   ↓
5. Start Application (validates schema)
   ↓
6. Test Feature
   ↓
7. Commit Both Entity & SQL to Git
```

### Production Deployment Workflow

```
1. Review Migration SQL
   ↓
2. Test on Staging Database
   ↓
3. Backup Production Database
   ↓
4. Run Migration on Production
   ↓
5. Verify Migration Success
   ↓
6. Deploy Application Code
   ↓
7. Verify Application Startup
```

---

## 🔒 Safety Practices

### 1. Always Use Transactions
```sql
BEGIN;
-- Your migration here
COMMIT;
-- Or ROLLBACK if something goes wrong
```

### 2. Test Migrations Locally First
- Never run untested SQL on production
- Use a local PostgreSQL instance or Supabase staging project

### 3. Backup Before Major Changes
```bash
# Backup before migration
pg_dump $DATABASE_URL > backup_before_step11.sql
```

### 4. Use Idempotent SQL
```sql
-- Good: Can run multiple times safely
CREATE TABLE IF NOT EXISTS leads (...);

-- Bad: Fails on second run
CREATE TABLE leads (...);
```

### 5. Add Rollback Scripts
For complex migrations, create a rollback script:
```sql
-- step11_leads_table_rollback.sql
DROP TABLE IF EXISTS leads CASCADE;
```

---

## 🚀 Future Enhancements (Optional)

### Option 1: Add Flyway (Automatic Migration Tool)

**Pros**:
- Automatic migration tracking
- Version control for database
- Rollback support
- CI/CD integration

**Cons**:
- Additional dependency
- Learning curve
- Less control

**How to Add**:
```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
```

```properties
# application.properties
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
```

### Option 2: Add Liquibase (Alternative to Flyway)

**Pros**:
- XML/YAML/SQL formats
- Advanced rollback features
- Database-agnostic

**Cons**:
- More complex than Flyway
- Heavier dependency

### Option 3: Keep Current Manual Approach

**Recommendation**: ✅ **Keep manual approach for now**

**Reasons**:
- Project is small enough to manage manually
- Full control and transparency
- No additional dependencies
- Works perfectly with Supabase
- Easy to understand and audit

---

## 📝 Migration Naming Convention

```
stepXX_feature_description.sql
```

**Examples**:
- `step2_connection_test.sql` - Initial connection test table
- `step3_telegram_messages.sql` - Telegram messages storage
- `step9_business_enhancements.sql` - PostGIS and business fields
- `step11_leads_table.sql` - Lead capture system

**Pattern**:
- `stepXX` - Corresponds to development phase
- `feature` - Short feature name
- `description` - What it does

---

## 🎓 Best Practices Summary

1. ✅ **One SQL file per feature/step**
2. ✅ **Use `IF NOT EXISTS` / `IF EXISTS`**
3. ✅ **Include test data in migrations**
4. ✅ **Add comments and documentation**
5. ✅ **Test locally before production**
6. ✅ **Keep migrations in version control**
7. ✅ **Use `ddl-auto=validate` in production**
8. ✅ **Create rollback scripts for complex changes**
9. ✅ **Backup before major migrations**
10. ✅ **Document all schema changes**

---

## 🔗 Related Files

- **Migration Scripts**: `database/stepXX_*.sql`
- **Helper Scripts**: `scripts/run_migration_stepXX.sh`
- **Entity Classes**: `src/main/java/com/alexia/entity/*.java`
- **Configuration**: `src/main/resources/application.properties`
- **Environment**: `.env` (database credentials)

---

## 📚 References

- [Spring Boot JPA Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/data.html#data.sql.jpa-and-spring-data)
- [Hibernate DDL Auto Options](https://docs.jboss.org/hibernate/orm/5.6/userguide/html_single/Hibernate_User_Guide.html#configurations-hbmddl)
- [PostgreSQL Best Practices](https://wiki.postgresql.org/wiki/Don%27t_Do_This)
- [Supabase SQL Editor Guide](https://supabase.com/docs/guides/database/overview)

---

**Last Updated**: 2025-10-20  
**Maintained By**: Development Team  
**Status**: Active & Working Well ✅
