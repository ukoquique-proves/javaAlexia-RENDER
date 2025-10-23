# Deployment Guides

This directory contains documentation for deploying the Alexia application.

## Platform Choice: Render

**Render is the recommended platform** for this project due to its excellent developer experience, integrated services, and generous free tier.

- **All-in-One Free Tier**: Render provides a free web service and a free PostgreSQL database, which simplifies setup.
- **Docker Support**: It has first-class support for Docker, allowing for consistent and reliable deployments.
- **Automation**: `render.yaml` allows for fully automated deployments (Infrastructure as Code).

## Critical Deployment Issues

Deploying a Vaadin Spring Boot application can be tricky. Here are the most common issues encountered and their solutions:

### 1. Vaadin Development Mode in Production ❌

- **Problem**: Application crashes on startup, complaining that it's not a Maven/Gradle project.
- **Root Cause**: Vaadin tries to run in development mode inside the production container, which lacks the necessary project files.
- **Solution**: You **must** enable Vaadin's production mode. This is done by adding the `vaadin-maven-plugin` to your `pom.xml` to build the frontend assets during the compile phase.

### 2. Port Configuration Mismatch ❌

- **Problem**: Health checks fail because the application is listening on the wrong port.
- **Root Cause**: Different platforms expect different ports (e.g., 8080, 8000, 10000).
- **Solution**: The application should dynamically bind to the port specified by the `PORT` environment variable. This project is configured to do so with `server.port=${PORT:8080}`.

### 3. Database Connection on Free Tiers ❌

- **Problem**: Application cannot connect to an external database like Supabase in production.
- **Root Cause**: Many free tiers (including Render's) have network restrictions that block outbound connections to external databases.
- **Solution**: Use the platform's integrated database service (e.g., Render PostgreSQL). It's on the internal network and works seamlessly.

**Important**: This limitation only affects **production**. You can (and should) use Supabase or any external PostgreSQL for local development.

### 4. PostgreSQL Extensions (PostGIS) Not Available ❌

- **Problem**: Schema creation fails with `ERROR: type "geography" does not exist` or similar PostGIS-related errors.
- **Root Cause**: Render's free PostgreSQL tier does not include PostGIS extension. Entity fields using `geography`, `geometry`, or PostGIS functions will fail.
- **Solution**: 
  - Comment out PostGIS-dependent fields in entities (e.g., `@Column(columnDefinition = "geography(Point, 4326)")`)
  - Comment out repository methods using PostGIS functions (`ST_DWithin`, `ST_Distance`, etc.)
  - Comment out service code referencing these fields
  - Mark all with `// TODO: Re-enable when PostGIS extension is available`
- **Alternative**: Use standard PostgreSQL types (e.g., separate `latitude` and `longitude` DOUBLE columns) or upgrade to a paid tier with PostGIS support.

### 5. Advanced PostgreSQL Types (JSONB, Arrays) ❌

- **Problem**: Schema creation fails with `ERROR: type "jsonb" does not exist` or array type errors.
- **Root Cause**: Some PostgreSQL features may not be available or configured on free tiers.
- **Solution**: 
  - Temporarily comment out fields using `@JdbcTypeCode(SqlTypes.JSON)` or array types
  - Use standard types (VARCHAR, TEXT) as fallback
  - Re-enable when upgrading to a tier with full PostgreSQL support

## Development vs Production Database Strategy

This project uses **different databases** for development and production environments:

### Development (Local)
- **Database**: Supabase PostgreSQL (external, cloud-hosted)
- **Why**: Easy setup, no local installation needed, works on minimal systems (like Puppy Linux)
- **Connection**: Port 6543 (connection pooler) with `prepareThreshold=0`
- **Variables**: `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`

### Production (Render)
- **Database**: Render PostgreSQL (internal, same infrastructure)
- **Why**: Free tier compatible, faster (internal network), required by Render's network restrictions
- **Connection**: Port 5432 (direct connection)
- **Variables**: `DATABASE_URL`, `DATABASE_USER`, `DATABASE_PASSWORD`

### Why This Works Seamlessly

**✅ No Deployment Conflicts:**
- Spring Boot profiles handle the switch automatically (`dev` vs `prod`)
- No code changes needed between environments
- Same PostgreSQL engine, compatible schemas
- JPA/Hibernate generates compatible SQL for both

**✅ Zero Impact on Deployments:**
- Render deployment uses `SPRING_PROFILES_ACTIVE=prod` automatically
- Production configuration in `application-prod.properties` takes over
- Supabase is never referenced in production
- Each deployment works identically regardless of local database

**✅ Schema Synchronization:**
- For **first deployment**: `spring.jpa.hibernate.ddl-auto=create` creates tables from scratch
- For **subsequent deployments**: `spring.jpa.hibernate.ddl-auto=update` updates existing schema
- Same entity classes work with both databases
- No manual migration needed for simple schema changes

**📚 Detailed Documentation:**
- See [DATABASE_STRATEGY.md](DATABASE_STRATEGY.md) for complete database separation guide
- See [SUPABASE.md](SUPABASE.md) for Supabase-specific configuration and troubleshooting
- See [ENVIRONMENT_SUMMARY.md](ENVIRONMENT_SUMMARY.md) for quick reference

### Deployment Guarantee

**Using Supabase for development will NOT make Render deployments harder.** The separation is clean, tested, and production-proven. Your app is already deployed on Render successfully with this exact setup.

## Universal Deployment Checklist

- [ ] `vaadin.productionMode=true` is enabled (via `pom.xml` plugin).
- [ ] Port is dynamically configured using `server.port=${PORT:8080}`.
- [ ] A multi-stage `Dockerfile` is used to create a small, secure final image.
- [ ] For free tiers, use the platform's internal database (Render PostgreSQL).
- [ ] Development can use any PostgreSQL (Supabase, local, Docker).
- [ ] Test the production Docker image locally before deploying.
- [ ] Comment out PostGIS-dependent fields and queries if PostGIS extension is not available.
- [ ] Comment out JSONB and array type fields if they cause schema creation errors.
- [ ] Remove explicit `hibernate.dialect` setting (auto-detected by Hibernate).
- [ ] Set `spring.jpa.open-in-view=false` to avoid lazy loading warnings.
- [ ] Test local compilation with `mvn clean compile -DskipTests` before pushing.
