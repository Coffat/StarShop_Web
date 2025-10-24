#!/bin/bash

# Script to load seed data into RDS PostgreSQL
# Usage: ./load-seed-data.sh

set -e

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
NC='\033[0m' # No Color

echo -e "${YELLOW}🗄️  Loading seed data into RDS PostgreSQL...${NC}"
echo "Database: $DB_HOST:$DB_PORT/$DB_NAME"
echo ""

# Export password for psql
export PGPASSWORD=$DB_PASSWORD

# Function to run SQL file
run_sql_file() {
    local file=$1
    local description=$2
    
    echo -e "${YELLOW}📝 Running: $description${NC}"
    echo "   File: $file"
    
    if [ ! -f "$file" ]; then
        echo -e "${RED}❌ File not found: $file${NC}"
        return 1
    fi
    
    if psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -f "$file" > /dev/null 2>&1; then
        echo -e "${GREEN}✅ Success: $description${NC}"
        echo ""
        return 0
    else
        echo -e "${RED}❌ Failed: $description${NC}"
        echo "   Trying with error output..."
        psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -f "$file"
        return 1
    fi
}

# Confirm before proceeding
echo -e "${YELLOW}⚠️  WARNING: This will modify the production database!${NC}"
read -p "Do you want to continue? (yes/no): " confirm

if [ "$confirm" != "yes" ]; then
    echo "Aborted."
    exit 0
fi

echo ""
echo -e "${GREEN}Starting data load...${NC}"
echo ""

# Run SQL files in order
run_sql_file "docker/init/01_schema.sql" "Schema creation"
run_sql_file "docker/init/02_functions_triggers_views.sql" "Functions, triggers, and views"
run_sql_file "docker/init/03_seed_data.sql" "Main seed data"
run_sql_file "docker/init/04_seed_data_reviews.sql" "Review seed data"

# Unset password
unset PGPASSWORD

echo ""
echo -e "${GREEN}🎉 All seed data loaded successfully!${NC}"
echo ""
echo "You can verify the data by connecting to the database:"
echo "psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME"
