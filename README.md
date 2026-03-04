# Promotion System - Multi-Service Architecture

A microservices-based promotion system built with Spring Boot for managing and calculating promotions in real-time.

## 🏗️ Architecture Overview

This project consists of multiple services:

```
promotion-system/
├── promotion-common/                 # Shared utilities, DTOs, and enums
├── promotion-management-service/     # CRUD operations for promotions (Port 8080)
├── promotion-engine-service/         # High-performance calculation engine (Port 8081)
├── docs/                            # API specs and documentation
└── docker-compose.yml               # Multi-service orchestration
```

### Services:

1. **Promotion Management Service** (Port 8080)
   - Manage promotions, rules, and conditions
   - Admin portal backend
   - Publishes events to Kafka
   - Database: MySQL (promotion_management)

2. **Promotion Engine Service** (Port 8081)
   - Calculate promotions for carts/orders
   - Validate and reserve promotion quotas
   - High-performance Redis caching
   - Database: MySQL (promotion_engine)

3. **Infrastructure**
   - **MySQL**: Separate databases for each service
   - **Redis**: Shared cache for high-performance reads
   - **Kafka**: Event streaming for service communication

## 📋 Prerequisites

- Java 17 or higher
- Maven 3.6+
- Docker & Docker Compose
- 8GB RAM minimum (for running all services)

## 🚀 Quick Start

### Option 1: Run with Docker Compose (Recommended)

1. **Build all services:**
   ```bash
   mvn clean package -DskipTests
   ```

2. **Start all services:**
   ```bash
   docker-compose up -d
   ```

3. **Check service status:**
   ```bash
   docker-compose ps
   ```

4. **View logs:**
   ```bash
   # All services
   docker-compose logs -f
   
   # Specific service
   docker-compose logs -f promotion-management
   docker-compose logs -f promotion-engine
   ```

5. **Stop all services:**
   ```bash
   docker-compose down
   ```

### Option 2: Run Locally (Development)

1. **Start infrastructure (MySQL, Redis, Kafka):**
   ```bash
   docker-compose up -d mysql-management mysql-engine redis zookeeper kafka
   ```

2. **Build the project:**
   ```bash
   mvn clean install -DskipTests
   ```

3. **Run Promotion Management Service:**
   ```bash
   cd promotion-management-service
   mvn spring-boot:run -Dspring-boot.run.profiles=dev
   ```

4. **Run Promotion Engine Service (in another terminal):**
   ```bash
   cd promotion-engine-service
   mvn spring-boot:run -Dspring-boot.run.profiles=dev
   ```

## 📖 API Documentation

### Promotion Management Service
- Swagger UI: http://localhost:8080/swagger-ui.html
- API Docs: http://localhost:8080/v3/api-docs
- Base URL: http://localhost:8080/api/v1

### Promotion Engine Service
- Swagger UI: http://localhost:8081/swagger-ui.html
- API Docs: http://localhost:8081/v3/api-docs
- Base URL: http://localhost:8081/api/v1/engine
- Health Check: http://localhost:8081/api/v1/engine/health

## 🔧 Build Commands

### Build all modules:
```bash
mvn clean install
```

### Build specific module:
```bash
# Build promotion-common only
mvn clean install -pl promotion-common

# Build promotion-management-service (with dependencies)
mvn clean install -pl promotion-management-service -am

# Build promotion-engine-service (with dependencies)
mvn clean install -pl promotion-engine-service -am
```

### Skip tests:
```bash
mvn clean install -DskipTests
```

### Run tests:
```bash
# All tests
mvn test

# Specific module
mvn test -pl promotion-management-service
```

## 🗄️ Database Setup

### Promotion Management Database
```sql
CREATE DATABASE promotion_management;
-- Tables will be auto-created by Hibernate
```

### Promotion Engine Database
```sql
CREATE DATABASE promotion_engine;
-- Tables will be auto-created by Hibernate
```

## 🌐 Service URLs

| Service | URL | Database | Port |
|---------|-----|----------|------|
| Promotion Management | http://localhost:8080 | promotion_management | 8080 |
| Promotion Engine | http://localhost:8081 | promotion_engine | 8081 |
| MySQL (Management) | localhost:3306 | promotion_management | 3306 |
| MySQL (Engine) | localhost:3307 | promotion_engine | 3307 |
| Redis | localhost:6379 | - | 6379 |
| Kafka | localhost:9092 | - | 9092 |

## 📦 Tech Stack

### Common
- Java 17
- Spring Boot 3.2.2
- Maven (Multi-module)
- Lombok
- Jackson

### Promotion Management Service
- Spring Boot Web
- Spring Data JPA
- Spring Security
- Spring Kafka (Producer)
- MySQL
- Redis
- Swagger/OpenAPI

### Promotion Engine Service
- Spring Boot Web
- Spring Data JPA
- Spring Cache (Redis)
- Spring Kafka (Consumer)
- Spring Actuator
- Micrometer Prometheus
- MySQL
- Redis

## 🔑 Key Features

### Promotion Management Service
- ✅ CRUD operations for promotions
- ✅ Rule and condition management
- ✅ Promotion lifecycle (Draft → Active → Disabled/Expired)
- ✅ Audit logging
- ✅ Event publishing to Kafka
- ✅ Scheduler for auto-expiration

### Promotion Engine Service
- ✅ High-performance promotion calculation
- ✅ Redis caching for fast lookups
- ✅ Atomic quota reservation
- ✅ Validation engine
- ✅ Event-driven sync from Management Service
- ✅ Circuit breaker patterns
- ✅ Metrics and monitoring

## 🔄 Service Communication

### Synchronous (REST API)
- Order Service → Engine Service (calculate, validate, reserve)
- Cart Service → Engine Service (preview promotions)

### Asynchronous (Kafka)
- Management Service → Engine Service (promotion updates)
- Events: `promotion.lifecycle.events`, `promotion.rule.events`

## 📊 Monitoring

### Actuator Endpoints (Engine Service)
- Health: http://localhost:8081/actuator/health
- Metrics: http://localhost:8081/actuator/metrics
- Prometheus: http://localhost:8081/actuator/prometheus

## 🛠️ Development

### Project Structure
```
promotion-system/
├── promotion-common/
│   ├── src/main/java/
│   │   └── org/example/promotion/common/
│   │       ├── dto/           # Shared DTOs
│   │       ├── enums/         # Shared enums
│   │       ├── event/         # Event models
│   │       └── util/          # Utility classes
│   └── pom.xml
│
├── promotion-management-service/
│   ├── src/main/java/
│   │   └── org/example/promotion/management/
│   │       ├── controller/
│   │       ├── service/
│   │       ├── repository/
│   │       ├── entity/
│   │       ├── dto/
│   │       ├── config/
│   │       └── PromotionManagementApplication.java
│   ├── src/main/resources/
│   │   └── application.yml
│   ├── Dockerfile
│   └── pom.xml
│
├── promotion-engine-service/
│   ├── src/main/java/
│   │   └── org/example/promotion/engine/
│   │       ├── controller/
│   │       ├── service/
│   │       ├── repository/
│   │       ├── entity/
│   │       ├── dto/
│   │       ├── config/
│   │       └── PromotionEngineApplication.java
│   ├── src/main/resources/
│   │   └── application.yml
│   ├── Dockerfile
│   └── pom.xml
│
├── docs/
│   ├── api-specs/
│   │   ├── 01-promotion-management-api.md
│   │   └── 02-promotion-engine-api.md
│   └── SERVICE_INTEGRATION.md
│
├── pom.xml                    # Parent POM
├── docker-compose.yml
└── README.md
```

### Adding a New Module

1. Create module directory
2. Create pom.xml with parent reference
3. Add module to parent pom.xml
4. Create src/main/java and src/main/resources structure
5. Implement your service

## 🐛 Troubleshooting

### Port Already in Use
```bash
# Check what's using the port
netstat -ano | findstr :8080

# Kill the process (Windows)
taskkill /PID <process_id> /F
```

### Database Connection Issues
```bash
# Check MySQL is running
docker-compose ps

# Check MySQL logs
docker-compose logs mysql-management
docker-compose logs mysql-engine

# Restart MySQL
docker-compose restart mysql-management
```

### Kafka Connection Issues
```bash
# Check Kafka is running
docker-compose logs kafka

# Restart Kafka stack
docker-compose restart zookeeper kafka
```

### Redis Connection Issues
```bash
# Check Redis is running
docker exec -it promotion_redis redis-cli ping

# Should return: PONG
```

## 📚 Documentation

- [API Specification - Promotion Management](docs/api-specs/01-promotion-management-api.md)
- [API Specification - Promotion Engine](docs/api-specs/02-promotion-engine-api.md)
- [Service Integration Guide](docs/SERVICE_INTEGRATION.md)
- [User Stories - Promotion Management](docs/user-stories/01-promotion-management-service.md)
- [User Stories - Promotion Engine](docs/user-stories/02-promotion-engine-service.md)

## 🧪 Testing

### Run all tests:
```bash
mvn test
```

### Run tests for specific service:
```bash
mvn test -pl promotion-management-service
mvn test -pl promotion-engine-service
```

### Integration tests with Testcontainers:
```bash
mvn verify
```

## 📝 Environment Variables

### Promotion Management Service
```properties
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/promotion_management
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=root
SPRING_DATA_REDIS_HOST=localhost
SPRING_KAFKA_BOOTSTRAP_SERVERS=localhost:9092
PROMOTION_ENGINE_API_KEY=your-api-key
```

### Promotion Engine Service
```properties
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3307/promotion_engine
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=root
SPRING_DATA_REDIS_HOST=localhost
SPRING_KAFKA_BOOTSTRAP_SERVERS=localhost:9092
PROMOTION_MANAGEMENT_API_KEY=your-api-key
```

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License.

## 🙋 Support

For questions or issues, please create an issue in the GitHub repository.

---

**Note:** This is a multi-service architecture. Make sure to start all required infrastructure (MySQL, Redis, Kafka) before running the services.
