#!/bin/bash
# Script to run Step 9 database migration on Supabase
# Adds geolocation, business hours, and enhanced fields to businesses table

echo "🚀 Starting Step 9 migration..."
echo "============================================================"

# Supabase connection details
DB_HOST="db.hgcesbylhkjoxtymxysy.supabase.co"
DB_PORT="5432"
DB_NAME="postgres"
DB_USER="postgres"

# Check if password is provided
if [ -z "$SUPABASE_DB_PASSWORD" ]; then
    echo "⚠️  SUPABASE_DB_PASSWORD environment variable not set"
    echo ""
    read -sp "Enter your Supabase database password: " DB_PASSWORD
    echo ""
else
    DB_PASSWORD="$SUPABASE_DB_PASSWORD"
fi

# Connection string
CONNECTION_STRING="postgresql://${DB_USER}:${DB_PASSWORD}@${DB_HOST}:${DB_PORT}/${DB_NAME}"

# Path to migration file
MIGRATION_FILE="database/step9_business_enhancements.sql"

# Check if migration file exists
if [ ! -f "$MIGRATION_FILE" ]; then
    echo "❌ Error: Migration file not found at $MIGRATION_FILE"
    exit 1
fi

echo "✅ Migration file found"
echo "✅ Connecting to Supabase..."
echo ""

# Run migration
psql "$CONNECTION_STRING" -f "$MIGRATION_FILE"

if [ $? -eq 0 ]; then
    echo ""
    echo "============================================================"
    echo "✅ Migration completed successfully!"
    echo "============================================================"
    echo ""
    echo "📊 Next steps:"
    echo "1. Verify the new columns in Supabase dashboard"
    echo "2. Check that PostGIS extension is enabled"
    echo "3. Restart your Spring Boot application"
    echo "4. Test geolocation queries"
else
    echo ""
    echo "============================================================"
    echo "❌ Migration failed"
    echo "============================================================"
    exit 1
fi
