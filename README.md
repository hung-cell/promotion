# Promotion Service

A microservice built with Spring Boot for managing promotions, coupons, stacking rules, and automated expiration schedulers.

## Prerequisites

- Java 17
- Maven
- Docker & Docker Compose

## Quick Start (Local Development)

The service requires a MySQL database. A `docker-compose.yml` file is provided to spin one up quickly.

1. **Start the database:**
   ```bash
   docker-compose up -d
   ```
   This will start a MySQL 8 container mapped to port `3306` with the database `promotion_db` created.

2. **Run the application:**
   ```bash
   mvn spring-boot:run -Dspring-boot.run.profiles=dev
   ```

3. **Access API Documentation:**
   Once running, you can access the Swagger UI:
   - http://localhost:8080/swagger-ui.html

## Tech Stack
- Spring Boot 3
- Spring Data JPA
- MySQL
- Spring Retry (For optimistic locking handling)
- Swagger OpenAPI

## Key Features
- **Coupon Concurrency Control**: Uses `@Version` Optimistic locking and Spring Retry to ensure coupons are never over-redeemed.
- **JPA Auditing**: Automatically tracks `createdBy`, `createdAt`, `updatedBy`, `updatedAt` for all entities.
- **Background Jobs**: 
  - Automatically expires promotions past their end date.
  - Automatically releases locked coupon reservations that exceed 15 minutes.
