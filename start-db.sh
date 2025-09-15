#!/bin/bash

echo "🌸 Starting Flower Shop Database..."

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
  echo "❌ Docker is not running. Please start Docker first."
  exit 1
fi

# Start PostgreSQL container
echo "🚀 Starting PostgreSQL container..."
docker-compose up -d

# Wait for database to be ready
echo "⏳ Waiting for database to be ready..."
sleep 5

# Check if container is running
if docker ps | grep -q flower_shop_db; then
  echo "✅ Database is running!"
  echo ""
  echo "📊 Connection details:"
  echo "   Host: localhost"
  echo "   Port: 5432"
  echo "   Database: flower_shop_system"
  echo "   Username: flower_admin"
  echo "   Password: flower_password_2024"
  echo ""
  echo "💡 Tips:"
  echo "   - View logs: docker-compose logs -f postgres"
  echo "   - Stop database: docker-compose down"
  echo "   - Connect via psql: docker exec -it flower_shop_db psql -U flower_admin -d flower_shop_system"
else
  echo "❌ Failed to start database. Check logs with: docker-compose logs postgres"
  exit 1
fi
