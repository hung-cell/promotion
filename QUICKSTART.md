# Quick Start Guide - Multi-Service Promotion System

## 🎯 Mục đích

Document này giúp bạn nhanh chóng bắt đầu với hệ thống Promotion System mới (multi-service architecture).

---

## 📦 Cài đặt nhanh

### 1. Clone và Build

```bash
cd "c:\Users\night mare\Documents\GitHub\Promotion"

# Build tất cả modules
mvn clean install -DskipTests
```

### 2. Khởi động Infrastructure

```bash
# Start MySQL, Redis, Kafka
docker-compose up -d mysql-management mysql-engine redis zookeeper kafka

# Kiểm tra status
docker-compose ps
```

### 3. Khởi động Services

**Terminal 1 - Promotion Management Service:**
```bash
cd promotion-management-service
mvn spring-boot:run
```

**Terminal 2 - Promotion Engine Service:**
```bash
cd promotion-engine-service
mvn spring-boot:run
```

---

## 🌐 URLs

| Service | URL | Port |
|---------|-----|------|
| **Management API** | http://localhost:8080/swagger-ui.html | 8080 |
| **Engine API** | http://localhost:8081/swagger-ui.html | 8081 |
| **Engine Health** | http://localhost:8081/api/v1/engine/health | 8081 |

---

## 🧪 Test APIs

### Management Service

```bash
# Create Promotion
curl -X POST http://localhost:8080/api/v1/promotions \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Promotion",
    "type": "PERCENTAGE",
    "discountValue": 20,
    "startDate": "2026-03-01T00:00:00",
    "endDate": "2026-12-31T23:59:59"
  }'

# List Promotions
curl http://localhost:8080/api/v1/promotions
```

### Engine Service

```bash
# Health Check
curl http://localhost:8081/api/v1/engine/health

# Preview Promotions (Mock)
curl -X POST http://localhost:8081/api/v1/engine/preview \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "cust_001",
    "items": [{"sku": "SKU001", "quantity": 2}]
  }'
```

---

## 🗄️ Database

### Connection Details

**Management Database:**
- Host: localhost:3306
- Database: promotion_management
- Username: root
- Password: root

**Engine Database:**
- Host: localhost:3307
- Database: promotion_engine
- Username: root
- Password: root

### Access Database

```bash
# Management DB
docker exec -it promotion_mysql_management mysql -uroot -proot promotion_management

# Engine DB
docker exec -it promotion_mysql_engine mysql -uroot -proot promotion_engine
```

---

## 🔧 Useful Commands

### Docker

```bash
# Start all services
docker-compose up -d

# Stop all services
docker-compose down

# View logs
docker-compose logs -f promotion-management
docker-compose logs -f promotion-engine

# Restart a service
docker-compose restart promotion-management
```

### Maven

```bash
# Build all
mvn clean install

# Build specific module
mvn clean install -pl promotion-management-service -am

# Run tests
mvn test

# Skip tests
mvn install -DskipTests
```

### Redis

```bash
# Connect to Redis
docker exec -it promotion_redis redis-cli

# Common commands in redis-cli
PING                        # Test connection
KEYS *                      # List all keys
GET promotion:promo_123     # Get specific key
FLUSHALL                    # Clear all data (careful!)
```

### Kafka

```bash
# List topics
docker exec -it promotion_kafka kafka-topics --list --bootstrap-server localhost:9092

# Consume messages
docker exec -it promotion_kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic promotion.lifecycle.events \
  --from-beginning
```

---

## 🐛 Troubleshooting

### Port đã được sử dụng

```bash
# Windows: Tìm process đang dùng port
netstat -ano | findstr :8080

# Kill process
taskkill /PID <process_id> /F
```

### Service không start

```bash
# Kiểm tra logs
docker-compose logs mysql-management
docker-compose logs redis

# Restart infrastructure
docker-compose restart mysql-management redis kafka
```

### Build lỗi

```bash
# Clean và rebuild
mvn clean
mvn install -DskipTests

# Nếu vẫn lỗi, kiểm tra Java version
java -version  # Should be 17+
mvn -version   # Should be 3.6+
```

---

## 📊 Monitoring

### Check Service Health

```bash
# Management Service
curl http://localhost:8080/actuator/health

# Engine Service
curl http://localhost:8081/actuator/health
curl http://localhost:8081/api/v1/engine/metrics
```

### Check Infrastructure

```bash
# MySQL
docker exec promotion_mysql_management mysqladmin ping -h localhost

# Redis
docker exec promotion_redis redis-cli ping

# Kafka
docker exec promotion_kafka kafka-broker-api-versions --bootstrap-server localhost:9092
```

---

## 🎓 Next Steps

1. ✅ **Đọc API Documentation**
   - Management: http://localhost:8080/swagger-ui.html
   - Engine: http://localhost:8081/swagger-ui.html

2. ✅ **Xem chi tiết Integration**
   - [Service Integration Guide](docs/SERVICE_INTEGRATION.md)

3. ✅ **Test các APIs**
   - Sử dụng Postman hoặc Swagger UI
   - Import collection từ docs/

4. ✅ **Customize Configuration**
   - Update `application.yml` trong mỗi service
   - Thêm profiles cho dev/prod

---

## 📞 Support

Nếu gặp vấn đề:
1. Kiểm tra logs: `docker-compose logs -f`
2. Xem [MIGRATION_SUMMARY.md](MIGRATION_SUMMARY.md)
3. Đọc [README.md](README.md) đầy đủ
4. Tạo issue trên GitHub

---

**Happy Coding! 🚀**
