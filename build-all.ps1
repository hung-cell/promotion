# Build Script for Multi-Module Project

Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "Building Promotion System" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""

# Clean previous builds
Write-Host "[1/4] Cleaning previous builds..." -ForegroundColor Yellow
mvn clean

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Clean failed!" -ForegroundColor Red
    exit 1
}

Write-Host "✅ Clean completed" -ForegroundColor Green
Write-Host ""

# Build promotion-common
Write-Host "[2/4] Building promotion-common..." -ForegroundColor Yellow
mvn install -pl promotion-common -am

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ promotion-common build failed!" -ForegroundColor Red
    exit 1
}

Write-Host "✅ promotion-common built successfully" -ForegroundColor Green
Write-Host ""

# Build promotion-management-service
Write-Host "[3/4] Building promotion-management-service..." -ForegroundColor Yellow
mvn install -pl promotion-management-service -am

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ promotion-management-service build failed!" -ForegroundColor Red
    exit 1
}

Write-Host "✅ promotion-management-service built successfully" -ForegroundColor Green
Write-Host ""

# Build promotion-engine-service
Write-Host "[4/4] Building promotion-engine-service..." -ForegroundColor Yellow
mvn install -pl promotion-engine-service -am

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ promotion-engine-service build failed!" -ForegroundColor Red
    exit 1
}

Write-Host "✅ promotion-engine-service built successfully" -ForegroundColor Green
Write-Host ""

Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "✅ All modules built successfully!" -ForegroundColor Green
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Next steps:" -ForegroundColor Yellow
Write-Host "  1. Start infrastructure: docker-compose up -d mysql-management mysql-engine redis kafka" -ForegroundColor White
Write-Host "  2. Run Management Service: cd promotion-management-service && mvn spring-boot:run" -ForegroundColor White
Write-Host "  3. Run Engine Service: cd promotion-engine-service && mvn spring-boot:run" -ForegroundColor White
Write-Host ""
