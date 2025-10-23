# Environment Configuration Summary

Quick reference for database and environment variable configuration.

---

## 🔧 Development (Local)

**Database**: Supabase PostgreSQL (External)

**Files**:
- `.env` - Your actual credentials (gitignored)
- `.env.example` - Template with placeholders
- `application-dev.properties` - Spring Boot dev config

**Variables**:
```bash
SPRING_DATASOURCE_URL=jdbc:postgresql://db.xxx.supabase.co:6543/postgres?sslmode=disable&prepareThreshold=0
SPRING_DATASOURCE_USERNAME=postgres.xxx
SPRING_DATASOURCE_PASSWORD=xxx
TELEGRAM_BOT_TOKEN=xxx
TELEGRAM_BOT_USERNAME=xxx
GROK_API_KEY=xxx
GROK_MODEL=llama-3.1-8b-instant
GROK_API_URL=https://api.groq.com/openai/v1/chat/completions
```

**Run**:
```bash
./scripts/run_linux.sh
# or
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

---

## 🚀 Production (Render)

**Database**: Render PostgreSQL (Internal)

**Files**:
- Render Dashboard → Environment (web UI)
- `deployment/RENDER_VARIABLES_TEMPLATE.env` - Template reference only
- `application-prod.properties` - Spring Boot prod config

**Database Schema**: Hibernate auto-creates tables and indexes
- **First deployment**: `ddl-auto=create` (builds schema from scratch)
- **Subsequent deployments**: `ddl-auto=update` (updates existing schema)

**Deploy**:
```bash
git push origin main
# Render auto-deploys
```

---

## 📊 Comparison

| | Development | Production |
|---|---|---|
| **Database** | Supabase | Render PostgreSQL |
| **Location** | External | Internal |
| **Variables** | `.env` file | Render Dashboard |
| **Prefix** | `SPRING_DATASOURCE_*` | `DATABASE_*` |
| **Profile** | `dev` | `prod` |
| **Port** | 6543 (pooler) | 5432 (standard) |

---

## ⚠️ Known Limitations

**Render Free Tier PostgreSQL:**
- ❌ No PostGIS extension (geography/geometry types not available)
- ❌ Some advanced types may not work (jsonb, arrays)
- ✅ Solution: Comment out affected fields with TODO markers

**Supabase Development:**
- ✅ Full PostgreSQL features available
- ✅ PostGIS extension available
- ⚠️ Use port 6543 with `prepareThreshold=0`

## ✅ Current Status

- ✅ Development environment working with Supabase
- ✅ Production environment deployed on Render
- ✅ Clear separation of concerns
- ✅ No hardcoded credentials
- ✅ Proper HikariCP configuration for both environments
- ✅ PostGIS fields commented out for Render compatibility
- ✅ Documentation complete

---

See [DATABASE_STRATEGY.md](DATABASE_STRATEGY.md) for detailed explanation.
