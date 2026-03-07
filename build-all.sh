#!/bin/bash

echo "====================================="
echo "Building Promotion System"
echo "====================================="
echo ""

# Clean previous builds
echo "[1/4] Cleaning previous builds..."
mvn clean

if [ $? -ne 0 ]; then
    echo "❌ Clean failed!"
    exit 1
fi

echo "✅ Clean completed"
echo ""

# Build promotion-common
echo "[2/4] Building promotion-common..."
mvn install -pl promotion-common -am

if [ $? -ne 0 ]; then
    echo "❌ promotion-common build failed!"
    exit 1
fi

echo "✅ promotion-common built successfully"
echo ""

# Build promotion-management-service
echo "[3/4] Building promotion-management-service..."
mvn install -pl promotion-management-service -am

if [ $? -ne 0 ]; then
    echo "❌ promotion-management-service build failed!"
    exit 1
fi

echo "✅ promotion-management-service built successfully"
echo ""

# Build promotion-engine-service
echo "[4/4] Building promotion-engine-service..."
mvn install -pl promotion-engine-service -am

if [ $? -ne 0 ]; then
    echo "❌ promotion-engine-service build failed!"
    exit 1
fi

echo "✅ promotion-engine-service built successfully"
echo ""

echo "====================================="
echo "✅ All modules built successfully!"
echo "====================================="
echo ""
echo "Next steps:"
echo "  1. Start infrastructure: docker-compose up -d mysql-management mysql-engine redis kafka"
echo "  2. Run Management Service: cd promotion-management-service && mvn spring-boot:run"
echo "  3. Run Engine Service: cd promotion-engine-service && mvn spring-boot:run"
echo ""
