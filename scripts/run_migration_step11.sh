#!/bin/bash

# Step 11: Lead Capture System - Database Migration Script

echo "🚀 Running Step 11 Migration: Lead Capture System"
echo "=================================================="

# Load environment variables
if [ -f .env ]; then
    export $(cat .env | grep -v '^#' | xargs)
fi

# Run migration
echo "📊 Creating leads table..."
psql "$DATABASE_URL" -f database/step11_leads_table.sql

if [ $? -eq 0 ]; then
    echo "✅ Step 11 migration completed successfully!"
else
    echo "❌ Migration failed. Check the error messages above."
    exit 1
fi
