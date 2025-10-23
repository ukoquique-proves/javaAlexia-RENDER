# Database Strategy - Development vs Production

This document explains how database connections are handled across different environments.

---

## Overview

The application uses **different PostgreSQL databases** for development and production:

- **Development**: Supabase PostgreSQL (external, cloud-hosted)
- **Production**: Render PostgreSQL (internal, same infrastructure)

This separation ensures:
- ✅ Development doesn't affect production data
- ✅ Production uses Render's internal network (faster, free tier compatible)
- ✅ Clear environment isolation

---

## Development Environment (Local)

### Database Provider
**Supabase PostgreSQL** - External cloud database

### Configuration Files
- **Variables**: `.env` (gitignored, contains actual credentials)
- **Template**: `.env.example` (committed, safe placeholders)
- **Properties**: `application-dev.properties`

### Variable Names
```bash
SPRING_DATASOURCE_URL=jdbc:postgresql://db.your-project.supabase.co:6543/postgres?sslmode=disable&prepareThreshold=0
SPRING_DATASOURCE_USERNAME=postgres.your_project_ref
SPRING_DATASOURCE_PASSWORD=your-supabase-password
```

### How It Works
1. `AlexiaApplication.java` loads `.env` file before Spring Boot starts
2. Variables are set as system properties via `System.setProperty()`
3. `application-dev.properties` references these properties: `${SPRING_DATASOURCE_URL}`
4. Spring Boot auto-configures HikariCP datasource
5. HikariCP settings optimized for Supabase connection pooler (port 6543)

### Activation
```bash
# Automatically activated by run_linux.sh
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

---

## Production Environment (Render)

### Database Provider
**Render PostgreSQL** - Internal database on same infrastructure

### Configuration Files
- **Variables**: Render Dashboard → Environment (web UI)
- **Template**: `deployment/RENDER_VARIABLES_TEMPLATE.env` (reference only, not used at runtime)
- **Properties**: `application-prod.properties`

### Variable Names
```bash
DATABASE_URL=jdbc:postgresql://dpg-xxxxx.oregon-postgres.render.com/your_database
DATABASE_USER=your_database_user
DATABASE_PASSWORD=your_database_password
```

### How It Works
1. Render sets `SPRING_PROFILES_ACTIVE=prod` automatically
2. `AlexiaApplication.java` detects `prod` profile and skips `.env` loading
3. `application-prod.properties` references Render environment variables: `${DATABASE_URL}`
4. Spring Boot auto-configures HikariCP datasource
5. Uses Render's internal database URL (fast, no external network)

### Activation
```bash
# Automatically set by Render
SPRING_PROFILES_ACTIVE=prod
```

---

## Why Different Variable Names?

| Environment | Variables | Reason |
|-------------|-----------|--------|
| **Development** | `SPRING_DATASOURCE_*` | Spring Boot standard, works with any PostgreSQL |
| **Production** | `DATABASE_*` | Render standard, portable across cloud platforms |

Both approaches work because Spring Boot's auto-configuration recognizes both patterns.

---

## Key Differences

| Aspect | Development (Supabase) | Production (Render) |
|--------|------------------------|---------------------|
| **Database** | External (Supabase) | Internal (Render) |
| **Port** | 6543 (pooler) | 5432 (standard) |
| **Network** | Internet | Internal network |
| **Speed** | Slower (external) | Faster (internal) |
| **Cost** | Free tier | Free tier |
| **SSL Mode** | `sslmode=disable` | Default |
| **Pooling** | Supabase pooler | Render native |

---

## Important Notes

### ⚠️ Render Free Tier Limitation
Render's free tier **blocks external database connections**. You cannot use Supabase in production on Render free tier. You must use Render's internal PostgreSQL.

### ✅ Supabase Connection Pooler
When using Supabase in development:
- **Use port 6543** (connection pooler) - Recommended ✅
- **Use port 5432** (direct connection) - Fallback option

**CRITICAL**: Add `prepareThreshold=0` to the JDBC URL when using port 6543:
```
jdbc:postgresql://db.xxx.supabase.co:6543/postgres?sslmode=disable&prepareThreshold=0
```

This disables server-side prepared statements and prevents the "prepared statement already exists" error that occurs with Supabase's connection pooler.

### DDL Auto Configuration

**For first deployment** with a fresh database:
```properties
spring.jpa.hibernate.ddl-auto=create
```

**For subsequent deployments** (recommended):
```properties
spring.jpa.hibernate.ddl-auto=update
```

**Why the change:**
- `create`: Builds entire schema from scratch (needed for fresh databases)
- `update`: Updates existing schema without data loss (safer for production)
- Change back to `update` after successful initial deployment

### 🔒 Security
- `.env` file is gitignored (never committed)
- `.env.example` has safe placeholders (can be committed)
- `deployment/RENDER_VARIABLES_TEMPLATE.env` has placeholders (can be committed)
- Actual credentials only in:
  - Local `.env` file (development)
  - Render Dashboard (production)

---

## Setup Instructions

### Development Setup

1. **Create Supabase Project**:
   - Go to [supabase.com](https://supabase.com)
   - Create new project
   - Note the connection details

2. **Copy `.env.example` to `.env`**:
   ```bash
   cp .env.example .env
   ```

3. **Edit `.env` with your Supabase credentials**:
   ```bash
   SPRING_DATASOURCE_URL=jdbc:postgresql://db.your-project.supabase.co:6543/postgres?sslmode=disable
   SPRING_DATASOURCE_USERNAME=postgres.your_project_ref
   SPRING_DATASOURCE_PASSWORD=your-actual-password
   ```

4. **Run the application**:
   ```bash
   ./scripts/run_linux.sh
   ```

### Production Setup (Render)

1. **Create PostgreSQL Database in Render**:
   - Dashboard → New → PostgreSQL
   - Region: Same as web service (e.g., Oregon)
   - Plan: Free

2. **Get Database Credentials**:
   - Go to database → Connect tab
   - Copy "Internal Database URL"
   - Convert to JDBC format: `jdbc:postgresql://[host]/[database]`

3. **Set Environment Variables in Render**:
   - Dashboard → Web Service → Environment
   - Add:
     - `DATABASE_URL=jdbc:postgresql://...`
     - `DATABASE_USER=...`
     - `DATABASE_PASSWORD=...`

4. **Deploy**:
   ```bash
   git push origin main
   ```
   Render auto-deploys from GitHub.

---

## Troubleshooting

### "prepared statement already exists" Error
**Cause**: Supabase connection pooler issue
**Solution**: Already configured in `application-dev.properties`:
```properties
spring.datasource.hikari.data-source-properties.cachePrepStmts=false
```

### "Failed to determine a suitable driver class"
**Cause**: Environment variables not loaded
**Solution**: 
- Check `.env` file exists
- Check variable names match `SPRING_DATASOURCE_*`
- Check `dev` profile is active

### "Connection refused" in Production
**Cause**: Using external database (Supabase) on Render free tier
**Solution**: Use Render's internal PostgreSQL database

---

## Migration Between Environments

To migrate data from Supabase (dev) to Render (prod):

```bash
# Export from Supabase
pg_dump -h db.your-project.supabase.co -U postgres -d postgres > backup.sql

# Import to Render
psql -h dpg-xxxxx.oregon-postgres.render.com -U your_user -d your_db < backup.sql
```

---

**Last Updated**: 2025-10-19
**Status**: ✅ Tested and working in both environments
