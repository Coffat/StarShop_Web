#!/bin/bash

# Script to RESET database and load fresh seed data
# ⚠️  WARNING: This will DELETE ALL DATA in the database!
# Usage: ./reset-database.sh

set -e

# Add PostgreSQL to PATH if not already there
export PATH="/opt/homebrew/opt/postgresql@15/bin:$PATH"

# RDS Connection details
DB_HOST="flower-shop-db-instance.c5gc6e8m62lz.ap-southeast-2.rds.amazonaws.com"
DB_PORT="5432"
DB_NAME="flower_shop_system"
DB_USER="flower_admin"
DB_PASSWORD="flower_password_2024"

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${RED}⚠️  ⚠️  ⚠️  DATABASE RESET SCRIPT ⚠️  ⚠️  ⚠️${NC}"
echo ""
echo "This script will:"
echo "  1. Backup current database"
echo "  2. DROP ALL TABLES and data"
echo "  3. Load fresh seed data from docker/init/*.sql"
echo ""
echo "Database: $DB_HOST:$DB_PORT/$DB_NAME"
echo ""

# Export password for psql
export PGPASSWORD=$DB_PASSWORD

# Confirm before proceeding
echo -e "${RED}⚠️  THIS WILL DELETE ALL DATA IN PRODUCTION DATABASE!${NC}"
echo -e "${YELLOW}Type 'DELETE ALL DATA' to confirm:${NC}"
read -p "> " confirm

if [ "$confirm" != "DELETE ALL DATA" ]; then
    echo "Aborted. (You must type exactly: DELETE ALL DATA)"
    exit 0
fi

echo ""
echo -e "${BLUE}Starting database reset...${NC}"
echo ""

# Step 1: Backup current database
BACKUP_DIR="./backups"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
BACKUP_FILE="$BACKUP_DIR/backup_before_reset_$TIMESTAMP.sql"

mkdir -p $BACKUP_DIR

echo -e "${YELLOW}📦 Step 1/3: Backing up current database...${NC}"
if pg_dump -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -F p -f $BACKUP_FILE 2>/dev/null; then
    echo -e "${GREEN}✅ Backup saved: $BACKUP_FILE${NC}"
else
    echo -e "${YELLOW}⚠️  Backup failed or database is empty. Continuing...${NC}"
fi
echo ""

# Step 2: Drop all tables
echo -e "${YELLOW}🗑️  Step 2/3: Dropping all tables...${NC}"

# Generate DROP statements for all tables
DROP_SCRIPT=$(psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -t -c "
SELECT 'DROP TABLE IF EXISTS \"' || tablename || '\" CASCADE;' 
FROM pg_tables 
WHERE schemaname = 'public';
")

if [ -n "$DROP_SCRIPT" ]; then
    echo "$DROP_SCRIPT" | psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME
    echo -e "${GREEN}✅ All tables dropped${NC}"
else
    echo -e "${YELLOW}⚠️  No tables found to drop${NC}"
fi

# Also drop sequences, views, functions
echo "Dropping sequences, views, and functions..."
psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME <<EOF
-- Drop all sequences
DO \$\$ DECLARE
    r RECORD;
BEGIN
    FOR r IN (SELECT sequence_name FROM information_schema.sequences WHERE sequence_schema = 'public') LOOP
        EXECUTE 'DROP SEQUENCE IF EXISTS ' || quote_ident(r.sequence_name) || ' CASCADE';
    END LOOP;
END \$\$;

-- Drop all views
DO \$\$ DECLARE
    r RECORD;
BEGIN
    FOR r IN (SELECT table_name FROM information_schema.views WHERE table_schema = 'public') LOOP
        EXECUTE 'DROP VIEW IF EXISTS ' || quote_ident(r.table_name) || ' CASCADE';
    END LOOP;
END \$\$;

-- Drop all functions
DO \$\$ DECLARE
    r RECORD;
BEGIN
    FOR r IN (SELECT routine_name FROM information_schema.routines WHERE routine_schema = 'public' AND routine_type = 'FUNCTION') LOOP
        EXECUTE 'DROP FUNCTION IF EXISTS ' || quote_ident(r.routine_name) || ' CASCADE';
    END LOOP;
END \$\$;
EOF

echo -e "${GREEN}✅ Database cleaned${NC}"
echo ""

# Step 3: Load seed data
echo -e "${YELLOW}📝 Step 3/3: Loading fresh seed data...${NC}"
echo ""

# Function to run SQL file
run_sql_file() {
    local file=$1
    local description=$2
    
    echo -e "${BLUE}   → $description${NC}"
    
    if [ ! -f "$file" ]; then
        echo -e "${RED}   ❌ File not found: $file${NC}"
        return 1
    fi
    
    if psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -f "$file" > /dev/null 2>&1; then
        echo -e "${GREEN}   ✅ Success${NC}"
        return 0
    else
        echo -e "${RED}   ❌ Failed, showing errors:${NC}"
        psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -f "$file"
        return 1
    fi
}

# Run SQL files in order
run_sql_file "docker/init/01_schema.sql" "Creating schema"
run_sql_file "docker/init/02_functions_triggers_views.sql" "Creating functions, triggers, views"
run_sql_file "docker/init/03_seed_data.sql" "Loading main seed data"
run_sql_file "docker/init/04_seed_data_reviews.sql" "Loading review data"

# Unset password
unset PGPASSWORD

echo ""
echo -e "${GREEN}🎉 Database reset complete!${NC}"
echo ""
echo "Summary:"
echo "  ✅ Old data backed up to: $BACKUP_FILE"
echo "  ✅ Database cleaned"
echo "  ✅ Fresh seed data loaded"
echo ""
echo "You can verify by connecting:"
echo "  psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME"
