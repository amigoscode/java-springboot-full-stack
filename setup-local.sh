#!/bin/bash

# Product Image Management - Local Testing Script
# This script helps you test the application locally with MinIO

echo "🚀 Starting Product Image Management Local Testing Setup"
echo "=================================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    print_error "Docker is not running. Please start Docker and try again."
    exit 1
fi

print_status "Docker is running ✓"

# Start the services
print_status "Starting PostgreSQL and MinIO services..."
docker compose -f docker-compose-services.yml up -d

# Wait for services to be ready
print_status "Waiting for services to be ready..."
sleep 10

# Check if services are running
if docker ps | grep -q "jfs-postgres-local"; then
    print_success "PostgreSQL is running ✓"
else
    print_error "PostgreSQL failed to start"
    exit 1
fi

if docker ps | grep -q "jfs-minio-local"; then
    print_success "MinIO is running ✓"
else
    print_error "MinIO failed to start"
    exit 1
fi

# Check if MinIO bucket was created
print_status "Checking MinIO bucket creation..."
sleep 5

if docker logs jfs-minio-init 2>&1 | grep -q "product-images"; then
    print_success "MinIO bucket 'product-images' created ✓"
else
    print_warning "MinIO bucket creation may have failed. You can create it manually in the MinIO console."
fi

echo ""
echo "🎉 Setup Complete!"
echo "=================="
echo ""
echo "📊 Service URLs:"
echo "  • PostgreSQL: localhost:5333"
echo "  • MinIO API: http://localhost:9000"
echo "  • MinIO Console: http://localhost:9001"
echo ""
echo "🔑 MinIO Credentials:"
echo "  • Username: minioadmin"
echo "  • Password: minioadmin123"
echo ""
echo "🚀 Next Steps:"
echo "  1. Start the Spring Boot application:"
echo "     mvn spring-boot:run"
echo ""
echo "  2. Open the web interface:"
echo "     http://localhost:8080"
echo ""
echo "  3. Access MinIO console to manage buckets:"
echo "     http://localhost:9001"
echo ""
echo "🧪 Test the application:"
echo "  • Create a product with an image"
echo "  • Upload images to existing products"
echo "  • View images in the product list"
echo ""
echo "📝 Useful Commands:"
echo "  • Stop services: docker compose -f docker-compose-db-only.yml down"
echo "  • View logs: docker compose -f docker-compose-db-only.yml logs -f"
echo "  • Restart services: docker compose -f docker-compose-db-only.yml restart"
echo ""

# Test MinIO connectivity
print_status "Testing MinIO connectivity..."
if curl -s http://localhost:9000/minio/health/live > /dev/null; then
    print_success "MinIO is accessible ✓"
else
    print_warning "MinIO may not be fully ready yet. Wait a few more seconds and try again."
fi

echo ""
print_success "Setup completed successfully! 🎉"
